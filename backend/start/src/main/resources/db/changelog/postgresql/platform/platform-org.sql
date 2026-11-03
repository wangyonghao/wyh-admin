-- liquibase formatted sql

-- changeset wangyonghao:org-1
-- comment 组织成员主路径：邀请、申请、主组织唯一索引

CREATE TABLE IF NOT EXISTS "sys_org_invite"
(
    "id"               int8         NOT NULL,
    "tenant_id"        int8         NOT NULL,
    "email"            varchar(255) NOT NULL,
    "token_hash"       varchar(64)  NOT NULL,
    "status"           int2         NOT NULL DEFAULT 0,
    "invited_by"       int8         NOT NULL,
    "expire_time"      timestamp    NOT NULL,
    "accepted_user_id" int8                  DEFAULT NULL,
    "responded_at"     timestamp             DEFAULT NULL,
    "create_user"      int8                  DEFAULT NULL,
    "create_time"      timestamp    NOT NULL,
    "update_user"      int8                  DEFAULT NULL,
    "update_time"      timestamp             DEFAULT NULL,
    "deleted"          int2         NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_org_invite_token_hash" ON "sys_org_invite" ("token_hash") WHERE "deleted" = 0;
CREATE INDEX "idx_org_invite_tenant_email" ON "sys_org_invite" ("tenant_id", "email") WHERE "deleted" = 0;
CREATE INDEX "idx_org_invite_email_status" ON "sys_org_invite" ("email", "status") WHERE "deleted" = 0;
COMMENT ON TABLE "sys_org_invite" IS '组织邮箱邀请';
COMMENT ON COLUMN "sys_org_invite"."status" IS '0待处理 1已接受 2已拒绝 3已过期 4已撤销';

CREATE TABLE IF NOT EXISTS "sys_org_application"
(
    "id"           int8         NOT NULL,
    "tenant_id"    int8         NOT NULL,
    "user_id"      int8         NOT NULL,
    "message"      varchar(200)          DEFAULT NULL,
    "status"       int2         NOT NULL DEFAULT 0,
    "reviewed_by"  int8                  DEFAULT NULL,
    "reviewed_at"  timestamp             DEFAULT NULL,
    "create_user"  int8                  DEFAULT NULL,
    "create_time"  timestamp    NOT NULL,
    "update_user"  int8                  DEFAULT NULL,
    "update_time"  timestamp             DEFAULT NULL,
    "deleted"      int2         NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_org_application_pending"
    ON "sys_org_application" ("tenant_id", "user_id")
    WHERE "deleted" = 0 AND "status" = 0;
CREATE INDEX "idx_org_application_user" ON "sys_org_application" ("user_id") WHERE "deleted" = 0;
CREATE INDEX "idx_org_application_tenant_status" ON "sys_org_application" ("tenant_id", "status") WHERE "deleted" = 0;
COMMENT ON TABLE "sys_org_application" IS '加入组织申请';
COMMENT ON COLUMN "sys_org_application"."status" IS '0待审核 1已通过 2已拒绝 3已撤销';

CREATE UNIQUE INDEX IF NOT EXISTS "uk_membership_primary_tenant"
    ON "sys_membership" ("user_id")
    WHERE "deleted" = 0
      AND "scope_type" = 'TENANT'
      AND "is_primary" = TRUE;

COMMENT ON COLUMN "sys_membership"."is_primary" IS '是否主范围（DEPARTMENT 主部门 / TENANT 主组织）';
COMMENT ON COLUMN "sys_membership"."scope_id" IS '范围 ID（部门 / 角色 / 组织=租户 ID）';

-- changeset wangyonghao:org-2
-- comment 种子：默认基础套餐，供自助创建组织使用
INSERT INTO "tenant_package" ("id", "name", "sort", "menu_check_strictly", "description", "status", "create_user", "create_time")
VALUES (1, '基础套餐', 1, true, '自助创建组织默认套餐', 1, 1, NOW())
ON CONFLICT ("id") DO NOTHING;

INSERT INTO "tenant_package_menu" ("package_id", "menu_id")
VALUES
  (1, 1000), (1, 1010), (1, 1011), (1, 1012), (1, 1013), (1, 1014), (1, 1015),
  (1, 1030), (1, 1031), (1, 1032),
  (1, 1070), (1, 1071), (1, 1072), (1, 1073)
ON CONFLICT DO NOTHING;
