-- 已有库升级：邮箱验证码登录
USE pet_service;

ALTER TABLE user
    ADD COLUMN email VARCHAR(128) DEFAULT NULL COMMENT '邮箱（验证码登录账号）' AFTER phone;

ALTER TABLE user
    ADD UNIQUE KEY idx_user_email (email);