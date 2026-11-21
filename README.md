# 宠物服务预约系统

面向宠物门店的预约服务平台，包含**用户端小程序**、**商家管理后台**和**后端服务**三部分。核心业务是宠物洗护/造型/SPA/寄养等服务的在线预约、到店核销，并在此基础上提供套餐秒杀、宠物社区、附近门店、每日签到等能力。

## 目录

- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [重点功能](#重点功能)
- [功能实现](#功能实现)
- [启动方法](#启动方法)

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端 | Spring Boot 2.7.18、MyBatis、PageHelper、Druid、JWT（jjwt）、Knife4j |
| 存储 | MySQL 8.0、Redis（缓存 / 分布式锁 / Lua / ZSet / GEO / Bitmap） |
| 中间件 | RabbitMQ 3.x（秒杀异步落单）、Redisson 3.23.5、阿里云 OSS SDK |
| 管理端 | Vue 2 + Element UI + Vue CLI 3（webpack 4） |
| 用户端 | 原生微信小程序 |

## 项目结构

| 模块 | 说明 |
| --- | --- |
| `pet-common` | 通用工具、常量、业务异常、统一返回结果、JWT 工具 |
| `pet-pojo` | 实体、DTO、VO |
| `pet-server` | Web 层、Service、Mapper、拦截器、定时任务、RabbitMQ 消费者 |
| `pet-admin` | Vue 2 商家管理后台 |
| `pet-weixin` | 微信小程序用户端（15 个页面） |

## 重点功能

### 1. 预约闭环

服务项目/套餐浏览 → 选择门店与可约时段 → 提交预约（锁定库存）→ 支付 → 商家确认 → 到店核销 → 完成服务。

- 状态机：`1 待支付 → 2 待确认 → 3 待服务 → 4 服务中 → 5 已完成`，`6 已取消`
- 用户、商家均可取消；待支付超时由定时任务自动关闭并释放时段
- 时段支持单条新增与批量生成（按门店、服务项目、时间区间、容量一次生成多天）

### 2. 服务套餐秒杀

- 活动库存预热到 Redis，抢购入口先经 Redis 令牌桶限流，再用 Lua 脚本在一个原子操作内完成判重、扣减、记录
- 抢购成功不直接落库，投递 RabbitMQ 排队，消费者异步幂等入库，接口立即返回「排队中」
- 数据库以 `(activity_id, user_id)` 唯一索引 + `stock > 0` 条件扣减兜底
- 后台支持活动创建、启停、订单查询与核销

### 3. 宠物社区

- 时间流与热度流双信息流，支持发布动态（图文、关联宠物/门店）、点赞、评论
- 热度按「点赞数 × 1000 + 评论数 × 100」计算
- 后台可隐藏/展示违规动态，信息流同步更新

### 4. 附近门店

- 按用户经纬度做半径检索，返回距离（米）
- Redis GEO 不可用时自动回退到数据库全量门店 + haversine 距离计算，接口不中断

### 5. 每日签到

- 按「用户 + 年月」维度记录签到，统计当月签到天数与连续签到天数
- 签到成功累加用户积分

### 6. 商家管理后台

预约管理（查询/确认/核销/完成/取消）、时段管理、秒杀活动与订单、社区内容审核、门店与坐标维护、服务项目、服务套餐、分类、员工、营业数据统计。

### 7. 登录与令牌

- 用户端通过微信小程序 code 换取 openid 登录，首次登录自动创建账号
- JWT 同时写入 Redis，拦截器校验令牌存在性；剩余有效期低于 30 分钟时自动续期，并通过响应头 `authentication` 下发新令牌

## 功能实现

### 时段库存不超卖

提交预约时对时段做条件更新：

```sql
update appointment_slot set booked = booked + 1
where id = #{id} and status = 1 and booked < capacity
```

影响行数为 0 即判定「已约满」，取消与超时关闭同样以带原状态条件的 `update` 完成状态流转并回退 `booked`，杜绝并发下的重复扣减与超卖。

### 同一宠物并发提交控制

按宠物维度加 Redisson 分布式锁 `pet:lock:appointment:pet:{petId}`（等待 5 秒、持有 10 秒），锁内完成「宠物归属校验 → 重复预约校验 → 库存扣减 → 写预约单」，事务提交后释放。锁只是第一道闸，最终一致性仍由上面的条件更新保证。

### 秒杀原子扣减

`SeckillServiceImpl` 内联 Lua 脚本，一次 RTT 完成：

```
SISMEMBER 已购集合判重 → GET 库存 → 库存 <= 0 返回售罄
→ DECR 库存 → SADD 用户 → 返回剩余库存
```

返回值语义：`>=0` 剩余库存、`-1` 售罄、`-2` 重复购买、`-3` 库存未初始化。

抢购入口先经过令牌桶限流（`pet:seckill:bucket:global`，容量与速率可配置），再执行 Lua 脚本。

抢购成功后投递到 RabbitMQ（`pet.seckill.exchange` / `pet.seckill.order.queue`），`SeckillOrderConsumer` 消费消息，在事务中幂等落库并扣减数据库库存；落库失败回补 Redis 库存与购买标记，投递失败则同步补偿。

### 缓存治理

`CatalogCacheService` 统一管理服务项目、套餐、门店列表缓存：

- TTL 1800 秒 + 0~300 秒随机抖动，避免集中过期造成缓存雪崩
- 空结果缓存 60 秒，防止不存在的分类反复穿透
- 缓存未命中先抢 `pet:cache:lock:{key}` 重建锁，只放行一个请求回源
- 后台增删改、上下架后按前缀批量失效
- Redis 异常时直接回源数据库，列表接口保持可用

### 社区信息流

只存帖子 id，不存整份文档：

- `pet:community:feed:time`：以发布时间戳为 score
- `pet:community:feed:hot`：以热度分为 score
- 发布时双写并裁剪到 500 条，翻页取 id 后回表组装
- 点赞/取消点赞、评论后刷新热度分值；后台隐藏同步移除，重新展示补回
- ZSet 为空时从数据库预热

### 签到统计

- Key `pet:signin:{userId}:{yyyyMM}`，当月第 N 天对应第 `N-1` 位
- 先写 `user_sign_in`（唯一索引防重），成功后 `SETBIT` 并加积分
- 当月天数用 `BITCOUNT`，连续天数按最近 60 天记录回溯

### Redis Key 设计

| Key | 类型 | 用途 |
| --- | --- | --- |
| `pet:catalog:service-item:{categoryId}` | String | 服务项目列表缓存 |
| `pet:catalog:service-package:{categoryId}` | String | 服务套餐列表缓存 |
| `pet:catalog:store:list` | String | 门店列表缓存 |
| `pet:cache:lock:{key}` | String | 缓存重建锁 |
| `pet:lock:appointment:pet:{petId}` | String | 预约提交分布式锁 |
| `pet:seckill:stock:{activityId}` | String | 秒杀库存 |
| `pet:seckill:users:{activityId}` | Set | 已购用户集合 |
| `pet:seckill:bucket:global` | Hash | 秒杀令牌桶 |
| `pet:login:token:{userId}` | String | 用户端 JWT 令牌 |
| `pet:community:feed:time` | ZSet | 社区时间流 |
| `pet:community:feed:hot` | ZSet | 社区热度流 |
| `pet:store:geo` | GEO | 门店坐标索引 |
| `pet:signin:{userId}:{yyyyMM}` | Bitmap | 月度签到 |

## 启动方法

### 1. 环境要求

- JDK 8+（源码编译级别 1.8）
- Maven 3.9+
- MySQL 8.0+
- Redis 6.0+（秒杀与限流依赖 Lua 与 ZSet）
- RabbitMQ 3.x（秒杀订单异步落单）
- Node.js 16（运行管理端）

### 2. 初始化数据库

```bash
mysql -uroot -p --default-character-set=utf8mb4 < pet-server/src/main/resources/db/pet_service.sql
```

Windows 下 mysql 客户端默认字符集常为 GBK，缺少 `--default-character-set=utf8mb4` 会导致中文乱码或 `Data too long`。

脚本会执行 `DROP DATABASE IF EXISTS pet_service`，请先确认本地没有需要保留的同名库。导入后自带门店、宠物、服务项目、未来两天可约时段、秒杀活动、社区动态等演示数据。

已有旧库（没有 `user.email` 字段）需要执行一次升级脚本：

```bash
mysql -uroot -p --default-character-set=utf8mb4 < pet-server/src/main/resources/db/upgrade_v2_email_login.sql
```

### 3. 配置

`pet-server/src/main/resources/application.yml` 与 `application-dev.yml` 全部通过环境变量注入，默认值为本地开发占位符。按需设置：

| 环境变量 | 说明 | 默认值 |
| --- | --- | --- |
| `PET_DB_USERNAME` / `PET_DB_PASSWORD` | 数据库账号 | `root` / `change-me` |
| `PET_REDIS_HOST` / `PET_REDIS_PORT` | Redis 地址 | `localhost` / `6379` |
| `PET_REDIS_PASSWORD` / `PET_REDIS_DATABASE` | Redis 密码与库号 | 空 / `10` |
| `PET_ADMIN_JWT_SECRET` / `PET_USER_JWT_SECRET` | JWT 签名密钥 | `change-me-*` |
| `ALIOSS_ACCESS_KEY_ID` / `ALIOSS_ACCESS_KEY_SECRET` | 阿里云 OSS 密钥 | 占位值 |
| `WECHAT_APPID` / `WECHAT_SECRET` | 小程序 AppID 与密钥 | 占位值 |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | RabbitMQ 地址 | `localhost` / `5672` |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | RabbitMQ 账号 | `guest` / `guest` |

上线前请替换所有 `change-me` 密钥与占位凭据。

### 4. 启动后端

```bash
mvn -DskipTests package
java -jar pet-server/target/pet-server-1.0-SNAPSHOT.jar
```

或在 IDE 中运行 `com.pet.PetServiceApplication`。

- 服务地址：`http://localhost:8080`
- 接口文档：`http://localhost:8080/doc.html`

### 5. 启动管理后台

```bash
cd pet-admin
npm install --legacy-peer-deps
npm run serve
```

管理端使用 Vue CLI 3 + webpack 4，Node 18 及以上需要附加 `NODE_OPTIONS=--openssl-legacy-provider`，推荐直接用 Node 16。

访问 `http://localhost:8888`，默认账号 `admin / 123456`。

### 6. 运行用户端小程序

用微信开发者工具导入 `pet-weixin` 目录即可，`project.config.json` 中的 AppID 为占位值（`touristappid`），可先用测试号运行。开发时需勾选「不校验合法域名」，或在小程序后台把后端地址加入白名单。

### 7. 登录令牌

登录接口返回的 `data.token` 作为用户端后续请求头 `authentication` 的值；令牌临近过期时响应头 `authentication` 会带回新令牌。管理端登录后返回的 token 放在请求头 `token` 中，退出登录调用 `POST /user/user/logout`。

## 说明

- 支付为模拟接口，未接入真实支付渠道。上线需替换预约与秒杀订单的支付逻辑，并补充回调幂等与对账。
- 秒杀、社区、签到、附近门店与登录令牌校验强依赖 Redis，Redis 不可用时相关接口报错；预约闭环与后台基础管理仍可正常运行。
- 秒杀订单通过 RabbitMQ 异步落库，抢购成功后需短暂等待再查询订单列表；RabbitMQ 未启动时抢购接口返回「秒杀请求处理失败」。
