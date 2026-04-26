-- liquibase formatted sql

-- changeset wyhao:sys_config_1
-- comment 创建系统配置中心表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `config_key`   varchar(100) NOT NULL                COMMENT '配置键，如 site, login, email, sms, storage, security',
    `config_value` json                                 COMMENT '配置值，JSON格式存储',
    `description`  varchar(255)                         COMMENT '配置说明',
    `version`      int(11)      NOT NULL DEFAULT 1      COMMENT '乐观锁版本号，防并发覆盖',
    `created_at`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_config_key` (`config_key`),
    KEY `idx_sys_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置中心主表';

-- changeset wyhao:sys_config_2
-- comment 初始化系统配置数据
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`)
VALUES
    -- 站点配置
    ('site', '{
        "siteName": "WYH Admin",
        "siteLogo": "",
        "siteCopyright": "Copyright © 2024 WYH Admin",
        "siteIcp": ""
    }', '站点配置'),
    
    -- 注册配置
    ('register', '{
        "enabled": true,
        "verifyEmail": false,
        "verifyPhone": false,
        "defaultRoleId": ""
    }', '注册配置'),
    
    -- 登录配置
    ('login', '{
        "captchaEnabled": true,
        "captchaType": "graphic",
        "maxRetry": 5,
        "lockTime": 30
    }', '登录配置'),
    
    -- 邮件配置
    ('email', '{
        "host": "",
        "port": 465,
        "username": "",
        "password": "",
        "fromName": "WYH Admin"
    }', '邮件配置'),
    
    -- 短信配置
    ('sms', '{
        "provider": "aliyun",
        "accessKey": "",
        "secretKey": "",
        "signName": ""
    }', '短信配置'),
    
    -- 存储配置
    ('storage', '{
        "type": "local",
        "endpoint": "",
        "accessKey": "",
        "secretKey": "",
        "bucket": ""
    }', '存储配置'),
    
    -- 安全配置
    ('security', '{
        "passwordMinLength": 8,
        "passwordRequireUppercase": true,
        "passwordRequireLowercase": true,
        "passwordRequireNumber": true,
        "passwordRequireSpecial": false,
        "sessionTimeout": 30
    }', '安全配置');
