-- liquibase formatted sql

-- changeset wangyonghao:email-registration-1
-- comment 邮箱注册改为点击验证链接后才建号。加宽 email 以容纳 AES Base64 密文，并新增待验证表。不改旧 changeset，不回填存量用户。
-- 回滚时先删待验证表，再把 email 收窄。若已写入超过 64 字符的密文，收窄会失败，需先清掉这些新注册。
-- rollback ALTER TABLE "sys_user" ALTER COLUMN "email" TYPE varchar(64);
-- rollback DROP TABLE IF EXISTS "sys_email_registration";

ALTER TABLE "sys_user" ALTER COLUMN "email" TYPE varchar(512);
COMMENT ON COLUMN "sys_user"."email" IS '邮箱（AES 密文）。明文最长 64 个字符；Base64 密文长于明文，列宽因此大于 64。存量密文保持不变。';

CREATE TABLE IF NOT EXISTS "sys_email_registration" (
    "id"             int8         NOT NULL,
    "tenant_id"      int8         NOT NULL,
    "email"          varchar(512) NOT NULL,
    "username"       varchar(64)  NOT NULL,
    "password_hash"  varchar(100) NOT NULL,
    "token_hash"     varchar(64)  NOT NULL,
    "department_id"  int8         NOT NULL,
    "role_id"        int8         NOT NULL,
    "expires_at"     timestamp    NOT NULL,
    "sent_at"        timestamp             DEFAULT NULL,
    "send_status"    varchar(16)  NOT NULL,
    "consumed_at"    timestamp             DEFAULT NULL,
    "create_time"    timestamp    NOT NULL,
    "update_time"    timestamp    NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_email_registration_token" ON "sys_email_registration" ("token_hash");
CREATE UNIQUE INDEX "uk_email_registration_open" ON "sys_email_registration" ("tenant_id", "email") WHERE "consumed_at" IS NULL;
CREATE INDEX "idx_email_registration_expires" ON "sys_email_registration" ("expires_at");
COMMENT ON TABLE "sys_email_registration" IS '邮箱注册待验证。只存慢哈希密码和验证 token 的 SHA-256，不存原密码或明文 token。确认前不写 sys_user。';
COMMENT ON COLUMN "sys_email_registration"."email" IS '邮箱 AES 密文，与 sys_user.email 同一套 TypeHandler。';
COMMENT ON COLUMN "sys_email_registration"."password_hash" IS 'DelegatingPasswordEncoder 慢哈希，含算法前缀。';
COMMENT ON COLUMN "sys_email_registration"."token_hash" IS '一次性验证 token 的 SHA-256 十六进制。链接里的原文不入库。';
COMMENT ON COLUMN "sys_email_registration"."send_status" IS 'PENDING 尚未投递，SENT 邮件服务器已接受，FAILED 发送失败可立即重试。';
COMMENT ON COLUMN "sys_email_registration"."consumed_at" IS '确认完成时间。非空表示链接已使用，不能再建号。';
