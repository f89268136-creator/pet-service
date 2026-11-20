-- 宠物服务预约系统建库脚本（MySQL 8.0）
DROP DATABASE IF EXISTS pet_service;
CREATE DATABASE pet_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_service;

-- ----------------------------
-- 员工
-- ----------------------------
CREATE TABLE employee (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(32)  NOT NULL COMMENT '姓名',
    username    VARCHAR(32)  NOT NULL COMMENT '用户名',
    password    VARCHAR(64)  NOT NULL COMMENT '密码',
    phone       VARCHAR(11)  NOT NULL COMMENT '手机号',
    sex         VARCHAR(2)   NOT NULL COMMENT '性别',
    id_number   VARCHAR(18)  NOT NULL COMMENT '身份证号',
    status      INT          NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    create_user BIGINT       NOT NULL,
    update_user BIGINT       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工信息';

-- ----------------------------
-- 分类（服务分类 / 套餐分类）
-- ----------------------------
CREATE TABLE category (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    type        INT         NOT NULL COMMENT '类型 1服务分类 2套餐分类',
    name        VARCHAR(32) NOT NULL COMMENT '分类名称',
    sort        INT         NOT NULL DEFAULT 0 COMMENT '排序',
    status      INT         NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME    NOT NULL,
    update_time DATETIME    NOT NULL,
    create_user BIGINT      NOT NULL,
    update_user BIGINT      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_category_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分类';

-- ----------------------------
-- 服务项目（沿用 dish 表）
-- ----------------------------
CREATE TABLE dish (
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(64)    NOT NULL COMMENT '服务名称',
    category_id BIGINT         NOT NULL COMMENT '分类id',
    price       DECIMAL(10, 2) NOT NULL COMMENT '价格',
    image       VARCHAR(255)   DEFAULT NULL COMMENT '图片',
    description VARCHAR(255)   DEFAULT NULL COMMENT '描述',
    status      INT            NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
    create_time DATETIME       NOT NULL,
    update_time DATETIME       NOT NULL,
    create_user BIGINT         NOT NULL,
    update_user BIGINT         NOT NULL,
    PRIMARY KEY (id),
    KEY idx_dish_category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '服务项目';

CREATE TABLE dish_flavor (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    dish_id BIGINT       NOT NULL,
    name    VARCHAR(64)  NOT NULL,
    `value` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_flavor_dish (dish_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '服务规格（兼容保留）';

-- ----------------------------
-- 服务套餐（沿用 setmeal 表）
-- ----------------------------
CREATE TABLE setmeal (
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    category_id BIGINT         NOT NULL COMMENT '分类id',
    name        VARCHAR(64)    NOT NULL COMMENT '套餐名称',
    price       DECIMAL(10, 2) NOT NULL COMMENT '套餐价格',
    status      INT            NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
    description VARCHAR(255)   DEFAULT NULL COMMENT '描述',
    image       VARCHAR(255)   DEFAULT NULL COMMENT '图片',
    create_time DATETIME       NOT NULL,
    update_time DATETIME       NOT NULL,
    create_user BIGINT         NOT NULL,
    update_user BIGINT         NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_setmeal_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '服务套餐';

CREATE TABLE setmeal_dish (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    setmeal_id BIGINT         NOT NULL,
    dish_id    BIGINT         NOT NULL,
    name       VARCHAR(64)    NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    copies     INT            NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    KEY idx_setmeal_dish (setmeal_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '套餐服务关系';

-- ----------------------------
-- 用户
-- ----------------------------
CREATE TABLE user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    openid      VARCHAR(64)  DEFAULT NULL COMMENT '微信openid',
    name        VARCHAR(32)  DEFAULT NULL COMMENT '姓名',
    phone       VARCHAR(11)  DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(128) DEFAULT NULL COMMENT '邮箱（验证码登录账号）',
    sex         VARCHAR(2)   DEFAULT NULL COMMENT '性别',
    id_number   VARCHAR(18)  DEFAULT NULL COMMENT '身份证号',
    avatar      VARCHAR(500) DEFAULT NULL COMMENT '头像',
    points      INT          NOT NULL DEFAULT 0 COMMENT '积分',
    create_time DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_user_openid (openid),
    UNIQUE KEY idx_user_email (email),
    KEY idx_user_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户';

-- ----------------------------
-- 宠物档案
-- ----------------------------
CREATE TABLE pet (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT        NOT NULL COMMENT '主人id',
    name        VARCHAR(32)   NOT NULL COMMENT '宠物名',
    species     VARCHAR(16)   NOT NULL DEFAULT 'DOG' COMMENT 'DOG/CAT/OTHER',
    breed       VARCHAR(32)   DEFAULT NULL COMMENT '品种',
    gender      INT           NOT NULL DEFAULT 0 COMMENT '0未知 1公 2母',
    birthday    DATE          DEFAULT NULL COMMENT '生日',
    weight      DECIMAL(5, 2) DEFAULT NULL COMMENT '体重kg',
    sterilized  INT           NOT NULL DEFAULT 0 COMMENT '0否 1是',
    vaccinated  INT           NOT NULL DEFAULT 0 COMMENT '0否 1是',
    avatar      VARCHAR(500)  DEFAULT NULL COMMENT '头像',
    note        VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    create_time DATETIME      NOT NULL,
    update_time DATETIME      NOT NULL,
    PRIMARY KEY (id),
    KEY idx_pet_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '宠物档案';

-- ----------------------------
-- 门店
-- ----------------------------
CREATE TABLE store (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(64)   NOT NULL COMMENT '门店名称',
    address     VARCHAR(255)  NOT NULL COMMENT '地址',
    phone       VARCHAR(20)   DEFAULT NULL COMMENT '电话',
    longitude   DOUBLE        DEFAULT NULL COMMENT '经度',
    latitude    DOUBLE        DEFAULT NULL COMMENT '纬度',
    open_time   TIME          DEFAULT NULL COMMENT '开始营业时间',
    close_time  TIME          DEFAULT NULL COMMENT '结束营业时间',
    images      VARCHAR(1000) DEFAULT NULL COMMENT '门店图片',
    description VARCHAR(255)  DEFAULT NULL COMMENT '简介',
    status      INT           NOT NULL DEFAULT 1 COMMENT '0停业 1营业',
    create_time DATETIME      NOT NULL,
    update_time DATETIME      NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '门店';

-- ----------------------------
-- 可预约时段（库存）
-- ----------------------------
CREATE TABLE appointment_slot (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    store_id    BIGINT   NOT NULL COMMENT '门店id',
    service_id  BIGINT   NOT NULL COMMENT '服务项目id',
    slot_date   DATE     NOT NULL COMMENT '预约日期',
    start_time  TIME     NOT NULL COMMENT '开始时间',
    end_time    TIME     NOT NULL COMMENT '结束时间',
    capacity    INT      NOT NULL DEFAULT 1 COMMENT '容量',
    booked      INT      NOT NULL DEFAULT 0 COMMENT '已预约数',
    status      INT      NOT NULL DEFAULT 1 COMMENT '0关闭 1开放',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_slot (store_id, service_id, slot_date, start_time),
    KEY idx_slot_date (slot_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '可预约时段';

-- ----------------------------
-- 预约单（沿用 orders 表）
-- ----------------------------
CREATE TABLE orders (
    id                      BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    number                  VARCHAR(50)    NOT NULL COMMENT '预约单号',
    status                  INT            NOT NULL DEFAULT 1 COMMENT '1待支付 2待确认 3待服务 4服务中 5已完成 6已取消',
    user_id                 BIGINT         NOT NULL COMMENT '用户id',
    address_book_id         BIGINT         DEFAULT NULL COMMENT '地址id（兼容保留）',
    order_time              DATETIME       NOT NULL COMMENT '下单时间',
    checkout_time           DATETIME       DEFAULT NULL COMMENT '支付时间',
    pay_method              INT            DEFAULT NULL COMMENT '支付方式 1微信 2支付宝',
    pay_status              INT            NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付 2已退款',
    amount                  DECIMAL(10, 2) NOT NULL COMMENT '金额',
    remark                  VARCHAR(100)   DEFAULT NULL COMMENT '备注',
    phone                   VARCHAR(11)    DEFAULT NULL COMMENT '手机号',
    address                 VARCHAR(255)   DEFAULT NULL COMMENT '地址（兼容保留）',
    consignee               VARCHAR(32)    DEFAULT NULL COMMENT '联系人（兼容保留）',
    cancel_reason           VARCHAR(255)   DEFAULT NULL COMMENT '取消原因',
    rejection_reason        VARCHAR(255)   DEFAULT NULL COMMENT '拒绝原因',
    cancel_time             DATETIME       DEFAULT NULL COMMENT '取消时间',
    estimated_delivery_time DATETIME       DEFAULT NULL COMMENT '预计送达时间（兼容保留）',
    delivery_status         INT            DEFAULT NULL COMMENT '配送状态（兼容保留）',
    delivery_time           DATETIME       DEFAULT NULL COMMENT '送达时间（兼容保留）',
    pack_amount             INT            NOT NULL DEFAULT 0 COMMENT '打包费（兼容保留）',
    tableware_number        INT            NOT NULL DEFAULT 0 COMMENT '餐具数量（兼容保留）',
    tableware_status        INT            DEFAULT NULL COMMENT '餐具状态（兼容保留）',
    store_id                BIGINT         DEFAULT NULL COMMENT '门店id',
    pet_id                  BIGINT         DEFAULT NULL COMMENT '宠物id',
    slot_id                 BIGINT         DEFAULT NULL COMMENT '时段id',
    service_date            DATE           DEFAULT NULL COMMENT '服务日期',
    service_time            VARCHAR(32)    DEFAULT NULL COMMENT '服务时间段',
    verify_code             VARCHAR(16)    DEFAULT NULL COMMENT '核销码',
    verify_time             DATETIME       DEFAULT NULL COMMENT '核销时间',
    service_start_time      DATETIME       DEFAULT NULL COMMENT '服务开始时间',
    service_end_time        DATETIME       DEFAULT NULL COMMENT '服务完成时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_number (number),
    UNIQUE KEY uk_verify_code (verify_code),
    KEY idx_order_user (user_id),
    KEY idx_order_pet (pet_id),
    KEY idx_order_service_date (service_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '预约单';

CREATE TABLE order_detail (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    name        VARCHAR(64)    NOT NULL,
    image       VARCHAR(255)   DEFAULT NULL,
    order_id    BIGINT         NOT NULL,
    dish_id     BIGINT         DEFAULT NULL,
    setmeal_id  BIGINT         DEFAULT NULL,
    dish_flavor VARCHAR(255)   DEFAULT NULL,
    number      INT            NOT NULL DEFAULT 1,
    amount      DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_detail_order (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '预约单明细';

-- ----------------------------
-- 宠物社区
-- ----------------------------
CREATE TABLE community_post (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id       BIGINT       NOT NULL COMMENT '发布人',
    pet_id        BIGINT       DEFAULT NULL COMMENT '关联宠物',
    store_id      BIGINT       DEFAULT NULL COMMENT '关联门店',
    content       VARCHAR(500) NOT NULL COMMENT '正文',
    images        VARCHAR(1000) DEFAULT NULL COMMENT '图片JSON数组',
    like_count    INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    comment_count INT          NOT NULL DEFAULT 0 COMMENT '评论数',
    status        INT          NOT NULL DEFAULT 1 COMMENT '0隐藏 1展示',
    create_time   DATETIME     NOT NULL,
    update_time   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_post_user (user_id),
    KEY idx_post_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '社区帖子';

CREATE TABLE community_comment (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    post_id     BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    content     VARCHAR(300) NOT NULL,
    create_time DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_comment_post (post_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '社区评论';

CREATE TABLE community_like (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    post_id     BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    create_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_user (post_id, user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '社区点赞';

-- ----------------------------
-- 每日签到
-- ----------------------------
CREATE TABLE user_sign_in (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    user_id     BIGINT   NOT NULL,
    sign_date   DATE     NOT NULL,
    points      INT      NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_sign_date (user_id, sign_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户签到记录';

-- ----------------------------
-- 套餐秒杀
-- ----------------------------
CREATE TABLE seckill_activity (
    id            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    name          VARCHAR(64)    NOT NULL COMMENT '活动名称',
    setmeal_id    BIGINT         NOT NULL COMMENT '服务套餐id',
    seckill_price DECIMAL(10, 2) NOT NULL COMMENT '秒杀价',
    stock         INT            NOT NULL COMMENT '库存',
    limit_per_user INT           NOT NULL DEFAULT 1 COMMENT '每人限购',
    begin_time    DATETIME       NOT NULL COMMENT '开始时间',
    end_time      DATETIME       NOT NULL COMMENT '结束时间',
    status        INT            NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
    create_time   DATETIME       NOT NULL,
    update_time   DATETIME       NOT NULL,
    PRIMARY KEY (id),
    KEY idx_seckill_time (begin_time, end_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '套餐秒杀活动';

CREATE TABLE seckill_order (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    order_no    VARCHAR(40)    NOT NULL COMMENT '秒杀单号',
    activity_id BIGINT         NOT NULL,
    user_id     BIGINT         NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    status      INT            NOT NULL DEFAULT 0 COMMENT '0待支付 1待使用 2已使用 3已取消',
    verify_code VARCHAR(16)    NOT NULL COMMENT '核销码',
    create_time DATETIME       NOT NULL,
    pay_time    DATETIME       DEFAULT NULL,
    verify_time DATETIME       DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_seckill_order_no (order_no),
    UNIQUE KEY uk_seckill_user (activity_id, user_id),
    UNIQUE KEY uk_seckill_verify (verify_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '套餐秒杀订单';

-- ----------------------------
-- 以下两表仅用于兼容原有接口，预约流程不再使用
-- ----------------------------
CREATE TABLE shopping_cart (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    name        VARCHAR(64)    DEFAULT NULL,
    image       VARCHAR(255)   DEFAULT NULL,
    user_id     BIGINT         NOT NULL,
    dish_id     BIGINT         DEFAULT NULL,
    setmeal_id  BIGINT         DEFAULT NULL,
    dish_flavor VARCHAR(255)   DEFAULT NULL,
    number      INT            NOT NULL DEFAULT 1,
    amount      DECIMAL(10, 2) NOT NULL,
    create_time DATETIME       NOT NULL,
    PRIMARY KEY (id),
    KEY idx_cart_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '购物车（兼容保留）';

CREATE TABLE address_book (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    consignee     VARCHAR(32)  NOT NULL,
    sex           VARCHAR(2)   DEFAULT NULL,
    phone         VARCHAR(11)  NOT NULL,
    province_code VARCHAR(12)  DEFAULT NULL,
    province_name VARCHAR(32)  DEFAULT NULL,
    city_code     VARCHAR(12)  DEFAULT NULL,
    city_name     VARCHAR(32)  DEFAULT NULL,
    district_code VARCHAR(12)  DEFAULT NULL,
    district_name VARCHAR(32)  DEFAULT NULL,
    detail        VARCHAR(255) NOT NULL,
    label         VARCHAR(32)  DEFAULT NULL,
    is_default    INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_address_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '地址簿（兼容保留）';

-- ----------------------------
-- 种子数据
-- ----------------------------
INSERT INTO employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', '1', '110101199001010001', 1, NOW(), NOW(), 1, 1);

INSERT INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user) VALUES
(1, 1, '洗护美容', 1, 1, NOW(), NOW(), 1, 1),
(2, 1, '健康医疗', 2, 1, NOW(), NOW(), 1, 1),
(3, 1, '寄养托管', 3, 1, NOW(), NOW(), 1, 1),
(4, 2, '洗护套餐', 4, 1, NOW(), NOW(), 1, 1);

INSERT INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) VALUES
(1, '基础洗护', 1, 88.00, 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400', '洗澡、吹干、基础梳理', 1, NOW(), NOW(), 1, 1),
(2, '精致造型', 1, 158.00, 'https://images.unsplash.com/photo-1596492784531-6e6eb5ea9993?w=400', '洗护加全身造型修剪', 1, NOW(), NOW(), 1, 1),
(3, '深层SPA护理', 1, 198.00, 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=400', '深层清洁与毛发护理', 1, NOW(), NOW(), 1, 1),
(4, '宠物疫苗接种', 2, 120.00, 'https://images.unsplash.com/photo-1576201836106-db1758fd1c97?w=400', '常规疫苗注射与健康评估', 1, NOW(), NOW(), 1, 1),
(5, '基础健康体检', 2, 99.00, 'https://images.unsplash.com/photo-1628009368231-7bb7cfcb0def?w=400', '体温、口腔、皮肤等基础检查', 1, NOW(), NOW(), 1, 1),
(6, '猫咪专属洗护', 1, 108.00, 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400', '低应激猫咪洗护', 1, NOW(), NOW(), 1, 1),
(7, '日间寄养', 3, 68.00, 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=400', '单日托管，含两次遛弯', 1, NOW(), NOW(), 1, 1),
(8, '宠物口腔护理', 2, 128.00, 'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=400', '牙齿清洁与口腔检查', 1, NOW(), NOW(), 1, 1);

INSERT INTO setmeal (id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) VALUES
(1, 4, '洗护三次卡', 228.00, 1, '基础洗护 3 次，有效期 90 天', 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400', NOW(), NOW(), 1, 1),
(2, 4, '健康守护套餐', 199.00, 1, '基础体检 + 疫苗注射各 1 次', 'https://images.unsplash.com/photo-1628009368231-7bb7cfcb0def?w=400', NOW(), NOW(), 1, 1);

INSERT INTO setmeal_dish (setmeal_id, dish_id, name, price, copies) VALUES
(1, 1, '基础洗护', 88.00, 3),
(2, 4, '宠物疫苗接种', 120.00, 1),
(2, 5, '基础健康体检', 99.00, 1);

INSERT INTO user (id, openid, name, phone, sex, avatar, points, create_time) VALUES
(1, 'demo-openid', '演示用户', '13800000000', '1', '', 0, NOW()),
(2, 'demo-openid-2', '邻家铲屎官', '13900000000', '0', '', 0, NOW());

INSERT INTO pet (id, user_id, name, species, breed, gender, birthday, weight, sterilized, vaccinated, avatar, note, create_time, update_time) VALUES
(1, 1, '豆豆', 'DOG', '柯基', 1, '2021-05-01', 12.50, 1, 1, '', '怕生，需要单独安抚', NOW(), NOW()),
(2, 1, '咪咪', 'CAT', '英国短毛猫', 2, '2022-08-15', 4.20, 1, 1, '', '对吹风机敏感', NOW(), NOW()),
(3, 2, '布丁', 'CAT', '布偶猫', 2, '2023-03-20', 5.10, 1, 1, '', '喜欢晒太阳', NOW(), NOW());

INSERT INTO store (id, name, address, phone, longitude, latitude, open_time, close_time, images, description, status, create_time, update_time)
VALUES
(1, '宠物生活馆·中心店', '北京市海淀区上地十街10号', '010-88886666', 116.3065, 40.0415, '09:00:00', '21:00:00', '', '提供洗护、医疗、寄养一站式服务', 1, NOW(), NOW()),
(2, '宠物生活馆·望京店', '北京市朝阳区望京街9号', '010-86668888', 116.4810, 39.9960, '09:00:00', '21:00:00', '', '主打猫咪洗护与造型', 1, NOW(), NOW());

INSERT INTO seckill_activity (id, name, setmeal_id, seckill_price, stock, limit_per_user, begin_time, end_time, status, create_time, update_time) VALUES
(1, '基础洗护三次卡限时秒杀', 1, 99.00, 20, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, NOW(), NOW()),
(2, '健康守护套餐限时秒杀', 2, 129.00, 10, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, NOW(), NOW());

INSERT INTO community_post (id, user_id, pet_id, store_id, content, images, like_count, comment_count, status, create_time, update_time) VALUES
(1, 1, 1, 1, '豆豆今天做了基础洗护，回来香喷喷的，毛也顺了很多。', '["https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=600"]', 1, 1, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
(2, 2, 3, 2, '布丁第一次体验猫咪专属洗护，全程低应激，店员很有耐心。', '["https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600"]', 1, 0, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW());

INSERT INTO community_comment (post_id, user_id, content, create_time) VALUES
(1, 2, '看起来状态很好，下次也带我家布丁去。', DATE_SUB(NOW(), INTERVAL 1 HOUR));

INSERT INTO community_like (post_id, user_id, create_time) VALUES
(1, 2, DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(2, 1, DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- 未来两天、多个服务项目的可预约时段
INSERT INTO appointment_slot (store_id, service_id, slot_date, start_time, end_time, capacity, booked, status, create_time, update_time) VALUES
(1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '11:00:00', 2, 0, 1, NOW(), NOW()),
(1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '15:00:00', 2, 0, 1, NOW(), NOW()),
(1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '16:00:00', '17:00:00', 2, 0, 1, NOW(), NOW()),
(1, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '12:00:00', 1, 0, 1, NOW(), NOW()),
(1, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '16:00:00', 1, 0, 1, NOW(), NOW()),
(1, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00:00', '13:00:00', 1, 0, 1, NOW(), NOW()),
(1, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00', '10:00:00', 3, 0, 1, NOW(), NOW()),
(1, 5, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00:00', '16:00:00', 3, 0, 1, NOW(), NOW()),
(1, 6, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:00:00', '14:30:00', 2, 0, 1, NOW(), NOW()),
(1, 7, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '21:00:00', 5, 0, 1, NOW(), NOW()),
(1, 8, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '16:00:00', '17:00:00', 2, 0, 1, NOW(), NOW()),
(1, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00', '11:00:00', 2, 0, 1, NOW(), NOW()),
(1, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', '15:00:00', 2, 0, 1, NOW(), NOW()),
(1, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00', '12:00:00', 1, 0, 1, NOW(), NOW()),
(1, 3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', '16:00:00', 1, 0, 1, NOW(), NOW()),
(1, 4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:30:00', '10:00:00', 3, 0, 1, NOW(), NOW()),
(1, 5, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:00:00', '16:00:00', 3, 0, 1, NOW(), NOW()),
(1, 6, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '13:00:00', '14:30:00', 2, 0, 1, NOW(), NOW()),
(1, 7, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', '21:00:00', 5, 0, 1, NOW(), NOW());
