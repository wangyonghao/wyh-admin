-- liquibase formatted sql

-- changeset wyhao:sys_config_1
-- comment 创建系统配置中心表
CREATE TABLE IF NOT EXISTS "sys_config"
(
    "id"           BIGSERIAL PRIMARY KEY,
    "config_key"   VARCHAR(100) NOT NULL UNIQUE,
    "config_value" JSONB,
    "description"  VARCHAR(255),
    "create_time"  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "update_time"  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "sys_config" IS '系统配置主表';
COMMENT ON COLUMN "sys_config"."id" IS 'ID';
COMMENT ON COLUMN "sys_config"."config_key" IS '配置键，如 site, login, email, sms, storage, security';
COMMENT ON COLUMN "sys_config"."config_value" IS '配置值，JSON格式存储';
COMMENT ON COLUMN "sys_config"."description" IS '配置说明';
COMMENT ON COLUMN "sys_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_config"."update_time" IS '更新时间';

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
    ('mail', '{
        "host": "",
        "port": 465,
        "username": "",
        "password": "",
        "fromName": "WYH Admin"
    }'::jsonb, '邮件配置'),
    
    -- 短信配置
    ('sms', '{
        "supplier": "alibaba",
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

--
-- -- 初始化默认参数
-- INSERT INTO sys_settings
-- ("id", "category", "name", "code", "value", "default_value", "description")
-- VALUES
--     (1, 'SITE', '系统名称', 'SITE_TITLE', NULL, 'Tide Admin', '显示在浏览器标题栏和登录界面的系统名称'),
--     (2, 'SITE', '系统描述', 'SITE_DESCRIPTION', NULL, '持续迭代优化的前后端分离中后台管理系统框架', '用于 SEO 的网站元描述'),
--     (3, 'SITE', '版权声明', 'SITE_COPYRIGHT', NULL, 'Copyright © 2022 - present Tide Admin 版权所有', '显示在页面底部的版权声明文本'),
--     (4, 'SITE', '备案号', 'SITE_BEIAN', NULL, NULL, '工信部 ICP 备案编号（如：京ICP备12345678号）'),
--     (5, 'SITE', '系统图标', 'SITE_FAVICON', NULL, '/favicon.ico', '浏览器标签页显示的网站图标（建议 .ico 格式）'),
--     (6, 'SITE', '系统LOGO', 'SITE_LOGO', NULL, '/logo.svg', '显示在登录页面和系统导航栏的网站图标（建议 .svg 格式）'),
--     (10, 'PASSWORD', '密码错误锁定阈值', 'PASSWORD_ERROR_LOCK_COUNT', NULL, '5', '连续登录失败次数达到该值将锁定账号（0-10次，0表示禁用锁定）'),
--     (11, 'PASSWORD', '账号锁定时长（分钟）', 'PASSWORD_ERROR_LOCK_MINUTES', NULL, '5', '账号锁定后自动解锁的时间（1-1440分钟，即24小时）'),
--     (12, 'PASSWORD', '密码有效期（天）', 'PASSWORD_EXPIRATION_DAYS', NULL, '0', '密码强制修改周期（0-999天，0表示永不过期）'),
--     (13, 'PASSWORD', '密码到期提醒（天）', 'PASSWORD_EXPIRATION_WARNING_DAYS', NULL, '0', '密码过期前的提前提醒天数（0表示不提醒）'),
--     (14, 'PASSWORD', '历史密码重复校验次数', 'PASSWORD_REPETITION_TIMES', NULL, '3', '禁止使用最近 N 次的历史密码（3-32次）'),
--     (15, 'PASSWORD', '密码最小长度', 'PASSWORD_MIN_LENGTH', NULL, '8', '密码最小字符长度要求（8-32个字符）'),
--     (16, 'PASSWORD', '是否允许密码包含用户名', 'PASSWORD_ALLOW_CONTAIN_USERNAME', NULL, '1', '是否允许密码包含正序或倒序的用户名字符'),
--     (17, 'PASSWORD', '密码是否必须包含特殊字符', 'PASSWORD_REQUIRE_SYMBOLS', NULL, '0', '是否要求密码必须包含特殊字符（如：!@#$%）'),
--     (20, 'MAIL', '邮件协议', 'MAIL_PROTOCOL', NULL, 'smtp', '邮件发送协议类型'),
--     (21, 'MAIL', '服务器地址', 'MAIL_HOST', NULL, 'smtp.126.com', '邮件服务器地址'),
--     (22, 'MAIL', '服务器端口', 'MAIL_PORT', NULL, '465', '邮件服务器连接端口'),
--     (23, 'MAIL', '邮箱账号', 'MAIL_USERNAME', NULL, 'charles7c@126.com', '发件人邮箱地址'),
--     (24, 'MAIL', '邮箱密码', 'MAIL_PASSWORD', NULL, NULL, '服务授权密码/客户端专用密码'),
--     (25, 'MAIL', '启用SSL加密', 'MAIL_SSL_ENABLED', NULL, '1', '是否启用SSL/TLS加密连接'),
--     (26, 'MAIL', 'SSL端口号', 'MAIL_SSL_PORT', NULL, '465', 'SSL加密连接的备用端口（通常与主端口一致）'),
--     (27, 'LOGIN', '是否启用验证码', 'LOGIN_CAPTCHA_ENABLED', NULL, '1', NULL);
