-- liquibase formatted sql
-- changeset wangyonghao:1
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
      "lockTime": 30,
      "sessionTimeout": 30
    }'::jsonb, '登录配置'),

    -- 邮件配置（本地 Inbucket：SMTP 127.0.0.1:2500，Web UI http://localhost:9000）
    ('mail', '{
      "host": "127.0.0.1",
      "port": 2500,
      "username": "noreply@localhost",
      "password": "",
      "from": "WYH Admin",
      "sslEnabled": false
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

    -- 密码策略配置
    ('password-policy', '{
      "passwordMinLength": 8,
      "passwordRequireUppercase": true,
      "passwordRequireLowercase": true,
      "passwordRequireNumber": true,
      "passwordRequireSpecial": false,
      "passwordExpireDays": 90,
      "passwordAllowContainUsername": false,
      "passwordRepetitionTimes": 3
    }'::jsonb, '密码策略配置');

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


-- 初始化默认部门
INSERT INTO "sys_department" ("id", code, name, type, "parent_id", "ancestors", "description", "sort", "status", "is_builtin", "create_user", "create_time","update_user","update_time")
VALUES (547887852587843590,'A01', 'Xxx（天津）科技有限公司', 1,1, '/1/', NULL, 1, 1, FALSE, 1, NOW(),1, NOW()),
       (547887852587843591, 'A0101','研发部', 2,547887852587843590, '/1/547887852587843590/', NULL, 1, 1, FALSE, 1, NOW(),1, NOW()),
       (547887852587843592, 'A0102','UI部', 2,547887852587843590, '/1/547887852587843590/', NULL, 2, 1, FALSE, 1, NOW(),1, NOW()),
       (547887852587843593, 'A0103','测试部',2, 547887852587843590, '/1/547887852587843590/', NULL, 3, 1, FALSE, 1, NOW(),1, NOW()),
       (547887852587843594, 'A0104','运维部', 2,547887852587843590, '/1/547887852587843590/', NULL, 4, 1, FALSE, 1, NOW(),1, NOW()),
       (547887852587843595, 'A010101','研发一组', 3, 547887852587843591, '/1/547887852587843590/547887852587843591/', NULL, 1, 1, FALSE, 1, NOW(),1, NOW()),
       (547887852587843596, 'A010102','研发二组', 3,547887852587843591, '/1/547887852587843590/547887852587843591/', NULL, 2, 2, FALSE, 1, NOW(), 1, NOW()),
       (547887852587843597, 'A02','Xxx（四川）科技有限公司', 1,1, '/1/', NULL, 2, 1, FALSE, 1, NOW(),1, NOW()),
       (547887852587843598, 'A0201','研发部', 2,  547887852587843597, '/1/547887852587843597/', NULL, 1, 1, FALSE, 1, NOW(),1, NOW()),
       (547887852587843599, 'A020101','研发一组', 3, 547887852587843598, '/1/547887852587843597/547887852587843598/', NULL, 1, 1, FALSE, 1, NOW(),1, NOW());

-- 初始化默认用户：admin/admin123；test/test123
INSERT INTO "sys_user"
("id", "username", "display_name", "password", "gender", "email", "phone", "avatar", "description", "status", "is_builtin", "pwd_update_time", "department_id", "create_user", "create_time")
VALUES
    (801822, 'test', '测试员', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 2, NULL, NULL, NULL,
     NULL, 1, FALSE, NOW(), 547887852587843593, 1, NOW()),
    (801823, 'Charles', 'Charles', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL,
     NULL, '代码写到极致，就是艺术。', 1, FALSE, NOW(), 547887852587843595, 1, NOW()),
    (801824, 'Yoofff', 'Yoofff', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL,
     NULL, '弱小和无知不是生存的障碍，傲慢才是。', 1, FALSE, NOW(), 1, 1, NOW()),
    (801825, 'Jasmine', 'Jasmine', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL,
     NULL, '干就完事了！', 1, FALSE, NOW(), 547887852587843605, 1, NOW()),
    (801826, 'AutumnSail', '秋登', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL,
     NULL, '只有追求完美，才能创造奇迹。', 1, FALSE, NOW(), 547887852587843602, 1, NOW()),
    (801827, 'Kils', 'Kils', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL,
     '可以摆烂，但不能真的菜。', 1, FALSE, NOW(), 547887852587843599, 1, NOW()),
    (801828, 'mochou', '莫愁', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL,
     '万事莫愁，皆得所愿。', 1, FALSE, NOW(), 547887852587843602, 1, NOW()),
    (801829, 'Jing', 'MS-Jing', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL,
     '路虽远，行则将至。', 2, FALSE, NOW(), 547887852587843599, 1, NOW()),
    (801830, 'domw', '梓陌', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL,
     '胜利是奖赏，挫折是常态。', 1, FALSE, NOW(), 547887852587843608, 1, NOW()),
    (801831, 'xtanyu', '小熊', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL,
     '不想上班。', 1, FALSE, NOW(), 547887852587843611, 1, NOW()),
    (801832, 'ppxb', '番茄', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL,
     'one day smile one day cry.', 1, FALSE, NOW(), 1, 547887852587843599, NOW()),
    (801833, 'luoqiz', '老罗', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL, NULL,
     '行者无疆，丈量四方。', 1, FALSE, NOW(), 1, 1, NOW()),
    (801834, 'lishuyanla', '颜如玉', '{bcrypt}$2a$10$xAsoeMJ.jc/kSxhviLAg7.j2iFrhi6yYAdniNdjLiIUWU/BRZl2Ti', 1, NULL, NULL,
     NULL, '书中自有颜如玉，世间多是李莫愁。', 1, FALSE, NOW(), 1, 1, NOW());

-- 初始化默认角色
INSERT INTO "sys_role"
    ("id", "name", "code", "data_scope", "description", "sort", "is_builtin", "create_user", "create_time")
VALUES
    (2, '系统管理员', 'sys_admin', 1, NULL, 1, false, 1, NOW()),
    (3, '普通用户', 'general', 4, NULL, 2, false, 1, NOW()),
    (547888897925840927, '测试人员', 'tester', 5, NULL, 3, false, 1, NOW()),
    (547888897925840928, '研发人员', 'developer', 4, NULL, 4, false, 1, NOW());

-- 初始化默认用户和角色关联数据
INSERT INTO "sys_user_role" ("id", "user_id", "role_id")
VALUES
    (2, 801822, 547888897925840927),
    (3, 801823, 547888897925840928),
    (4, 801824, 547888897925840928),
    (5, 801825, 547888897925840928),
    (6, 801826, 547888897925840928),
    (7, 801827, 547888897925840928),
    (8, 801828, 547888897925840928),
    (9, 801829, 547888897925840928),
    (10, 801830, 547888897925840928),
    (11, 801831, 547888897925840928),
    (12, 801832, 547888897925840928),
    (13, 801833, 547888897925840928),
    (14, 801834, 547888897925840928);

-- 测试用户成员关系（与 department_id / user_role 对齐；admin 已在 platform-data.sql）
INSERT INTO "sys_membership" ("id", "user_id", "scope_type", "scope_id", "is_primary", "status", "joined_at", "create_user", "create_time", "update_time", "deleted")
SELECT 10000 + row_number() OVER (ORDER BY u.id),
       u.id,
       'DEPARTMENT',
       u.department_id,
       TRUE,
       1,
       NOW(),
       1,
       NOW(),
       NOW(),
       0
FROM "sys_user" u
WHERE u.id >= 801822
  AND u.department_id IS NOT NULL;

INSERT INTO "sys_membership" ("id", "user_id", "scope_type", "scope_id", "is_primary", "status", "joined_at", "create_user", "create_time", "update_time", "deleted")
SELECT 20000 + row_number() OVER (ORDER BY ur.id),
       ur.user_id,
       'ROLE',
       ur.role_id,
       FALSE,
       1,
       NOW(),
       1,
       NOW(),
       NOW(),
       0
FROM "sys_user_role" ur
WHERE ur.id >= 2;

-- 初始化默认角色和部门关联数据
INSERT INTO "sys_role_department" ("role_id", "department_id")
VALUES (547888897925840927, 547887852587843593);
