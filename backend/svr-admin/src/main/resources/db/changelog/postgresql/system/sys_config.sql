-- liquibase formatted sql

-- changeset wyhao:sys_config_1
-- comment 创建系统配置中心表
CREATE TABLE IF NOT EXISTS "sys_config"
(
    "id"           BIGSERIAL PRIMARY KEY,
    "config_key"   VARCHAR(100)             NOT NULL UNIQUE,
    "config_value" JSONB,
    "description"  VARCHAR(255),
    "version"      INT         DEFAULT 1    NOT NULL,
    "created_at"   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at"   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "sys_config" IS '系统配置中心主表';
COMMENT ON COLUMN "sys_config"."id" IS 'ID';
COMMENT ON COLUMN "sys_config"."config_key" IS '配置键，如 site, login, email, sms, storage, security';
COMMENT ON COLUMN "sys_config"."config_value" IS '配置值，JSON格式存储';
COMMENT ON COLUMN "sys_config"."description" IS '配置说明';
COMMENT ON COLUMN "sys_config"."version" IS '乐观锁版本号，防并发覆盖';
COMMENT ON COLUMN "sys_config"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_config"."updated_at" IS '更新时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS "idx_sys_config_key" ON "sys_config" ("config_key");

-- changeset wyhao:sys_config_2
-- comment 初始化系统配置数据
INSERT INTO "sys_config" ("config_key", "config_value", "description")
VALUES
    -- 站点配置
    ('site', '{
        "siteName": "WYH Admin",
        "siteLogo": "",
        "siteCopyright": "Copyright © 2024 WYH Admin",
        "siteIcp": ""
    }'::jsonb, '站点配置'),
    
    -- 注册配置
    ('register', '{
        "enabled": true,
        "verifyEmail": false,
        "verifyPhone": false,
        "defaultRoleId": ""
    }'::jsonb, '注册配置'),
    
    -- 登录配置
    ('login', '{
        "captchaEnabled": true,
        "captchaType": "graphic",
        "maxRetry": 5,
        "lockTime": 30
    }'::jsonb, '登录配置'),
    
    -- 邮件配置
    ('email', '{
        "host": "",
        "port": 465,
        "username": "",
        "password": "",
        "fromName": "WYH Admin"
    }'::jsonb, '邮件配置'),
    
    -- 短信配置
    ('sms', '{
        "provider": "aliyun",
        "accessKey": "",
        "secretKey": "",
        "signName": ""
    }'::jsonb, '短信配置'),
    
    -- 存储配置
    ('storage', '{
        "type": "local",
        "endpoint": "",
        "accessKey": "",
        "secretKey": "",
        "bucket": ""
    }'::jsonb, '存储配置'),
    
    -- 安全配置
    ('security', '{
        "passwordMinLength": 8,
        "passwordRequireUppercase": true,
        "passwordRequireLowercase": true,
        "passwordRequireNumber": true,
        "passwordRequireSpecial": false,
        "sessionTimeout": 30
    }'::jsonb, '安全配置');
