# pet-service 宠物服务预约系统

在苍穹外卖项目基础上改造的宠物服务预约系统，包含 L2 预约闭环与 L3 业务融合能力。

预约核心闭环：

```
服务项目/套餐展示 -> 选择门店与时段 -> 提交预约(锁定库存) -> 模拟支付
-> 商家确认 -> 到店核销(开始服务) -> 完成服务
                        \-> 用户/商家取消、超时自动取消 -> 释放时段库存
```

L3 在预约业务上叠加：

- 服务项目/套餐/门店列表 Redis 缓存治理（TTL + 随机抖动、空值缓存、重建锁、前缀失效）
- Redisson 分布式锁保护“同一宠物并发提交预约”
- 套餐限时秒杀：Lua 原子扣库存 + Redis Stream 异步落单 + 定时消费补偿
- 宠物社区：时间/热度双信息流（Redis ZSet）、点赞评论、后台隐藏
- GEO 附近门店：Redis GEO 半径检索，Redis 不可用时回退数据库 haversine
- 每日签到：Redis Bitmap 打卡 + 积分 + 连续签到统计

## 模块

| 模块 | 说明 |
| --- | --- |
| `pet-common` | 通用工具、常量、异常、返回结果 |
| `pet-pojo` | 实体、DTO、VO |
| `pet-server` | Web、Service、Mapper、定时任务、WebSocket |
| `pet-admin` | Vue2 + Element UI 管理端 |

技术栈：Spring Boot 2.7.18、MyBatis、PageHelper、Druid、MySQL 8、Redis、Redisson、JWT、Vue2、Element UI。

## 1. 环境要求

- JDK 21（源码编译级别为 1.8）
- Maven 3.9+
- MySQL 8.0+
- Redis 6.0+（L3 缓存、秒杀、社区、GEO、签到依赖；库存扣减使用 Lua，异步落单使用 Stream）
- Node.js（仅运行管理端时需要）

## 2. 初始化数据库

> 脚本会执行 `DROP DATABASE IF EXISTS pet_service`，请确认本地没有需要保留的同名库。

```bash
mysql -uroot -p --default-character-set=utf8mb4 < pet-server/src/main/resources/db/pet_service.sql
```

Windows 下 mysql 客户端默认字符集常为 GBK，缺少 `--default-character-set=utf8mb4` 会导入中文乱码或报 `Data too long`。

脚本创建 `pet_service` 库，并写入：

- 管理员：`admin / 123456`
- 演示用户：`13800000000`
- 门店：`id=1` 中心店、`id=2` 望京店，均带经纬度
- 宠物：`豆豆`、`咪咪`（属于演示用户）、`布丁`（属于用户 2）
- 服务项目：洗护、造型、SPA、疫苗、体检、寄养等
- 未来两天的可预约时段
- 秒杀活动：`基础洗护三次卡`、`健康守护套餐`（各限购 1 件）
- 社区动态与评论、点赞种子数据

## 3. 修改本地配置

编辑 `pet-server/src/main/resources/application-dev.yml`：

```yaml
pet:
  demo-login-enabled: true   # 演示登录开关
  datasource:
    username: root
    password: 你的数据库密码
  redis:
    host: localhost
    port: 6379
    password: 你的 Redis 密码
    database: 10
```

`application.yml` 中的 JWT 密钥、微信支付参数为原项目演示值。正式部署前必须改为环境变量或配置中心注入，并轮换已泄露的密钥。

## 4. 启动后端

```bash
mvn -DskipTests package
java -jar pet-server/target/pet-server-1.0-SNAPSHOT.jar
```

或在 IDE 中运行 `com.pet.PetServiceApplication`。

- 服务地址：`http://localhost:8080`
- 接口文档：`http://localhost:8080/doc.html`

## 5. 启动管理端

```bash
cd pet-admin
npm install --legacy-peer-deps
NODE_OPTIONS=--openssl-legacy-provider npm run serve
```

该项目使用 Vue CLI 3 + webpack 4，Node 18 及以上版本启动/构建时需要附加 `NODE_OPTIONS=--openssl-legacy-provider`；更推荐使用 Node 16。

访问 `http://localhost:8888`，使用 `admin / 123456` 登录。管理端新增：

- **预约管理**：查询、确认、核销、完成、取消预约
- **时段管理**：查询/新增/批量生成/开关时段
- **秒杀管理**：活动创建/启停、秒杀订单查询与核销
- **社区管理**：动态查询、隐藏/展示（同步维护 Redis 信息流）
- **门店管理**：门店信息与经纬度维护、重建 GEO 索引

## 6. 演示登录（用户端）

用户端原本依赖微信授权。开发环境使用手机号登录：

```bash
curl -X POST http://localhost:8080/user/user/dev-login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800000000"}'
```

返回的 `data.token` 作为后续用户端请求头 `authentication` 的值。

## 7. 预约闭环调用示例

### 7.1 用户端

```bash
TOKEN=<dev-login 返回的 token>
DATE=$(date -d tomorrow +%F)

# 我的宠物
curl "http://localhost:8080/user/pet/list" -H "authentication: $TOKEN"

# 门店
curl "http://localhost:8080/user/store/list" -H "authentication: $TOKEN"

# 服务项目（经 Redis 缓存，Redis 异常时回源数据库）
curl "http://localhost:8080/user/service-item/list" -H "authentication: $TOKEN"

# 可预约时段
curl "http://localhost:8080/user/appointment/slots?storeId=1&serviceId=1&date=$DATE" \
  -H "authentication: $TOKEN"

# 提交预约（slotId 取上一步返回的 id）
curl -X POST "http://localhost:8080/user/appointment/submit" \
  -H "authentication: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"storeId":1,"petId":1,"slotId":1,"serviceId":1,"remark":"第一次到店"}'

# 模拟支付（把 1 换成预约单 id）
curl -X POST "http://localhost:8080/user/appointment/1/pay" \
  -H "authentication: $TOKEN"

# 我的预约
curl "http://localhost:8080/user/appointment/page?page=1&pageSize=10" \
  -H "authentication: $TOKEN"
```

### 7.2 管理端

```bash
TOKEN=<管理端登录返回的 token>

# 预约列表
curl "http://localhost:8080/admin/appointment/page?page=1&pageSize=10" \
  -H "token: $TOKEN"

# 确认预约
curl -X PUT "http://localhost:8080/admin/appointment/1/confirm" -H "token: $TOKEN"

# 核销到店（核销码见预约详情 verifyCode）
curl -X PUT "http://localhost:8080/admin/appointment/verify" \
  -H "token: $TOKEN" -H "Content-Type: application/json" \
  -d '{"verifyCode":"123456"}'

# 完成服务
curl -X PUT "http://localhost:8080/admin/appointment/1/complete" -H "token: $TOKEN"

# 新增时段
curl -X POST "http://localhost:8080/admin/appointment/slot" \
  -H "token: $TOKEN" -H "Content-Type: application/json" \
  -d '{"storeId":1,"serviceId":1,"slotDate":"'$DATE'","startTime":"18:00:00","endTime":"19:00:00","capacity":2}'

# 批量生成时段
curl -X POST "http://localhost:8080/admin/appointment/slot/batch" \
  -H "token: $TOKEN" -H "Content-Type: application/json" \
  -d '{"storeId":1,"serviceIds":[1,2],"startDate":"'$DATE'","days":3,"startTime":"09:00:00","endTime":"18:00:00","intervalMinutes":60,"capacity":2}'
```

管理端登录接口：

```bash
curl -X POST http://localhost:8080/admin/employee/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

## 8. 预约状态机

| 状态 | 含义 | 允许操作 |
| --- | --- | --- |
| 1 | 待支付 | 支付、取消、超时自动取消 |
| 2 | 待确认 | 商家确认、取消 |
| 3 | 待服务 | 到店核销、取消 |
| 4 | 服务中 | 完成服务 |
| 5 | 已完成 | - |
| 6 | 已取消 | - |

支付状态：`0 未支付`、`1 已支付`、`2 已退款`。

关键实现：

- 提交预约时通过 `update appointment_slot set booked = booked + 1 where booked < capacity` 原子扣减库存，失败即返回“已约满”。
- 取消、拒单、超时取消均通过带原状态条件的 `update` 实现状态流转，并释放时段库存，避免并发重复操作。
- 支付为演示模拟接口；正式接入微信支付时替换 `AppointmentServiceImpl.pay`，并补充回调幂等处理。

## 9. 数据兼容说明

- 物理表沿用 `dish`（服务项目）、`setmeal`（服务套餐）、`setmeal_dish`（套餐项目关系），Java 层已统一为 `ServiceItem` / `ServicePackage` / `ServicePackageItem`。
- `orders` 表沿用原订单表，新增 `store_id / pet_id / slot_id / service_date / service_time / verify_code / verify_time / service_start_time / service_end_time`。
- L3 新增 `seckill_activity`、`seckill_order`、`community_post`、`community_comment`、`community_like`、`user_sign_in`；`user` 表新增 `points`。
- 购物车、地址簿、微信支付、WebSocket 等外卖链路代码已移除；对应 `shopping_cart`、`address_book` 表不再被业务使用。

## 10. L3 业务融合实现

### 10.1 缓存治理

`CatalogCacheService` 统一处理服务项目、套餐、门店列表缓存：

- Key：`pet:catalog:service-item:{categoryId}`、`pet:catalog:service-package:{categoryId}`、`pet:catalog:store:list`
- TTL：1800 秒 + 0~300 秒随机抖动，避免同一时刻集中过期
- 空列表缓存 60 秒，防止不存在的分类反复穿透到数据库
- 缓存未命中时先抢 `pet:cache:lock:{key}` 重建锁，只放行一个请求回源
- Redis 异常时直接回源数据库，不影响列表接口可用性
- 管理端新增/修改/删除/上架下架后按前缀删除：`pet:catalog:service-item:*`、`pet:catalog:service-package:*`、`pet:catalog:store:*`

### 10.2 预约并发控制

提交预约时按宠物维度加 Redisson 锁 `pet:lock:appointment:pet:{petId}`（等待 5 秒、持有 10 秒）。锁内完成“宠物归属校验 -> 重复预约校验 -> 时段库存原子扣减 -> 写预约单”，事务提交后释放锁；抢锁失败返回“当前宠物有预约正在提交，请稍后重试”。

时段库存仍以 `update appointment_slot set booked = booked + 1 where booked < capacity` 作为最终兜底，避免锁失效时超卖。

### 10.3 套餐秒杀

活动创建后把库存预热到 `pet:seckill:stock:{activityId}`；抢购时执行 Lua 脚本，在一个原子操作里完成：

```
已购集合 SISMEMBER 判重 -> 读取库存 -> 库存 <= 0 返回售罄
-> DECR 库存 -> SADD 用户 -> 返回剩余库存
```

Redis 返回 `>=0` 剩余库存、`-1` 售罄、`-2` 重复购买、`-3` 库存未初始化。抢购成功后生成订单号并 `XADD` 到 `pet:stream:seckill-order`，接口立即返回“排队中”；`SeckillOrderStreamConsumer` 每秒消费一次，在事务中幂等落库并扣减数据库库存。落库失败会回补 Redis 库存、移除用户购买标记；Stream 写入失败则同步补偿。

数据库另有 `(activity_id, user_id)` 唯一索引和 `stock > 0` 条件扣减作为最终一致性保障。

相关接口：

```bash
# 进行中的秒杀活动
curl "http://localhost:8080/user/seckill/list" -H "authentication: $TOKEN"

# 抢购（1 为活动 id）
curl -X POST "http://localhost:8080/user/seckill/1/grab" -H "authentication: $TOKEN"

# 我的秒杀订单（异步落库，稍后刷新）
curl "http://localhost:8080/user/seckill/order/page?page=1&pageSize=10" \
  -H "authentication: $TOKEN"

# 模拟支付
curl -X POST "http://localhost:8080/user/seckill/order/<orderNo>/pay" \
  -H "authentication: $TOKEN"
```

管理端：

```bash
# 活动分页
curl "http://localhost:8080/admin/seckill/page?page=1&pageSize=10" -H "token: $TOKEN"

# 新增活动
curl -X POST "http://localhost:8080/admin/seckill" \
  -H "token: $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"洗护限时秒杀","setmealId":1,"seckillPrice":88.00,"stock":30,"limitPerUser":1,"beginTime":"2026-11-02 10:00","endTime":"2026-11-09 10:00","status":1}'

# 启用/停用（status=0 停用并删除库存 key）
curl -X POST "http://localhost:8080/admin/seckill/status/1?id=1" -H "token: $TOKEN"

# 秒杀订单与核销
curl "http://localhost:8080/admin/seckill/order/page?page=1&pageSize=10" -H "token: $TOKEN"
curl -X PUT "http://localhost:8080/admin/seckill/order/verify" \
  -H "token: $TOKEN" -H "Content-Type: application/json" \
  -d '{"verifyCode":"123456"}'
```

### 10.4 宠物社区

- 信息流使用两个 ZSet：`pet:community:feed:time`（发布时间戳）与 `pet:community:feed:hot`（`点赞数 * 1000 + 评论数 * 100`）
- 发布时写入两个 ZSet 并裁剪到 500 条，只保留帖子 id；翻页取 id 后回表组装，避免 ZSet 中维护整份帖子
- ZSet 为空时从数据库预热；后台隐藏动态时同步移除，重新展示时补回
- 点赞使用唯一索引 + 计数更新，点赞/取消后刷新热度分值；评论同步累加评论数

```bash
# 时间流 / 热度流
curl "http://localhost:8080/user/community/feed?sort=time&page=1&pageSize=10" \
  -H "authentication: $TOKEN"
curl "http://localhost:8080/user/community/feed?sort=hot&page=1&pageSize=10" \
  -H "authentication: $TOKEN"

# 发布动态
curl -X POST "http://localhost:8080/user/community" \
  -H "authentication: $TOKEN" -H "Content-Type: application/json" \
  -d '{"petId":1,"storeId":1,"content":"今天做了 SPA","images":["https://example.com/a.jpg"]}'

# 点赞切换、评论
curl -X POST "http://localhost:8080/user/community/1/like" -H "authentication: $TOKEN"
curl -X POST "http://localhost:8080/user/community/1/comment" \
  -H "authentication: $TOKEN" -H "Content-Type: application/json" \
  -d '{"content":"毛孩子好可爱"}'

# 后台隐藏/展示（status=0 隐藏，1 展示）
curl -X POST "http://localhost:8080/admin/community/status/0?id=1" -H "token: $TOKEN"
```

### 10.5 GEO 附近门店

门店经纬度写入 `pet:store:geo`，按用户坐标做半径检索并返回距离（米）：

```bash
curl "http://localhost:8080/user/store/nearby?longitude=116.3065&latitude=40.0415&radius=5&limit=10" \
  -H "authentication: $TOKEN"
```

管理端修改门店坐标后会同步 GEO；也可手动重建：

```bash
curl -X POST "http://localhost:8080/admin/store/sync-geo" -H "token: $TOKEN"
```

Redis GEO 不可用时回退为数据库全量门店 + haversine 计算距离，接口仍可用。

### 10.6 Bitmap 每日签到

- Key：`pet:signin:{userId}:{yyyyMM}`，本月第 N 天对应 bit `N-1`
- 签到先写 `user_sign_in`（唯一索引防重），成功后 `SETBIT` 并给用户加 10 积分
- 本月签到天数用 `BITCOUNT` 统计；连续天数按最近 60 天签到记录回溯

```bash
curl -X POST "http://localhost:8080/user/signin" -H "authentication: $TOKEN"
curl "http://localhost:8080/user/signin/stats" -H "authentication: $TOKEN"
```

### 10.7 Redis Key 一览

| Key | 类型 | 用途 |
| --- | --- | --- |
| `pet:catalog:service-item:{categoryId}` | String | 服务项目列表缓存 |
| `pet:catalog:service-package:{categoryId}` | String | 服务套餐列表缓存 |
| `pet:catalog:store:list` | String | 门店列表缓存 |
| `pet:cache:lock:{key}` | String | 缓存重建锁 |
| `pet:lock:appointment:pet:{petId}` | String | 预约提交分布式锁 |
| `pet:seckill:stock:{activityId}` | String | 秒杀库存 |
| `pet:seckill:users:{activityId}` | Set | 已购用户 |
| `pet:stream:seckill-order` | Stream | 秒杀订单异步队列 |
| `pet:community:feed:time` | ZSet | 社区时间流 |
| `pet:community:feed:hot` | ZSet | 社区热度流 |
| `pet:store:geo` | GEO | 门店坐标索引 |
| `pet:signin:{userId}:{yyyyMM}` | Bitmap | 月度签到 |

## 11. 已知边界

- 秒杀订单为异步落库，抢购成功后需短暂等待再查询订单。
- 秒杀、社区、签到、GEO 强依赖 Redis；Redis 不可用时这些接口会报错，预约闭环仍可运行。
- 支付、签到积分为演示逻辑，正式接入需替换为真实支付回调与积分账本。
