-- liquibase formatted sql

-- changeset wangyonghao:menu-package-catalog
-- comment 菜单范围、草稿发布与套餐生命周期

ALTER TABLE "sys_menu" ADD COLUMN "code" varchar(64);
ALTER TABLE "sys_menu" ADD COLUMN "scope" varchar(16) NOT NULL DEFAULT 'PLATFORM';
ALTER TABLE "sys_menu" ADD COLUMN "is_system" bool NOT NULL DEFAULT FALSE;
ALTER TABLE "sys_menu" ADD COLUMN "is_required" bool NOT NULL DEFAULT FALSE;
ALTER TABLE "sys_menu" ADD COLUMN "deleted_at" timestamp;

UPDATE "sys_menu" SET "code" = 'm' || "id" WHERE "code" IS NULL;
UPDATE "sys_menu" SET "is_system" = TRUE WHERE "id" IN (1050, 2030) OR "parent_id" IN (1050, 2030);

COMMENT ON COLUMN "sys_menu"."code" IS '稳定标识，创建后不可修改、不可复用';
COMMENT ON COLUMN "sys_menu"."scope" IS 'PLATFORM 平台菜单，TENANT 租户菜单';
COMMENT ON COLUMN "sys_menu"."is_system" IS '系统保护，不能停用或删除';
COMMENT ON COLUMN "sys_menu"."is_required" IS '必含，不能停用或删除';
COMMENT ON COLUMN "sys_menu"."deleted_at" IS '软删除时间';

DROP INDEX IF EXISTS "uk_menu_name_parent_id";
CREATE UNIQUE INDEX "uk_menu_scope_parent_name" ON "sys_menu" ("scope", "parent_id", "name");
CREATE UNIQUE INDEX "uk_menu_code" ON "sys_menu" ("code") WHERE "code" IS NOT NULL;
CREATE INDEX "idx_menu_scope" ON "sys_menu" ("scope");

ALTER TABLE "tenant_package" ADD COLUMN "code" varchar(64);
ALTER TABLE "tenant_package" ADD COLUMN "kind" varchar(16) NOT NULL DEFAULT 'MAIN';
ALTER TABLE "tenant_package" ADD COLUMN "lifecycle" varchar(16) NOT NULL DEFAULT 'PUBLISHED';
ALTER TABLE "tenant_package" ADD COLUMN "published_at" timestamp;
ALTER TABLE "tenant_package" ADD COLUMN "delisted_at" timestamp;
ALTER TABLE "tenant_package" ADD COLUMN "version" int4 NOT NULL DEFAULT 1;

UPDATE "tenant_package" SET "code" = 'p' || "id" WHERE "code" IS NULL;
UPDATE "tenant_package" SET "lifecycle" = 'DELISTED', "delisted_at" = COALESCE("update_time", "create_time") WHERE "status" = 2;
UPDATE "tenant_package" SET "published_at" = "create_time" WHERE "lifecycle" = 'PUBLISHED' AND "published_at" IS NULL;

COMMENT ON COLUMN "tenant_package"."code" IS '稳定标识';
COMMENT ON COLUMN "tenant_package"."kind" IS 'MAIN 主套餐，ADDON 增购包';
COMMENT ON COLUMN "tenant_package"."lifecycle" IS 'DRAFT 未发布，PUBLISHED 已发布，DELISTED 已下架';
COMMENT ON COLUMN "tenant_package"."published_at" IS '首次发布时间，发布后名称锁定';
COMMENT ON COLUMN "tenant_package"."delisted_at" IS '下架时间';
COMMENT ON COLUMN "tenant_package"."version" IS '乐观锁版本';

CREATE UNIQUE INDEX "uk_tenant_package_code" ON "tenant_package" ("code") WHERE "code" IS NOT NULL;

CREATE TABLE "menu_catalog_draft" (
    "scope" varchar(16) PRIMARY KEY,
    "version" int4 NOT NULL,
    "base_version" int4 NOT NULL DEFAULT 0,
    "document" text NOT NULL,
    "update_user" int8,
    "update_time" timestamp
);
COMMENT ON TABLE "menu_catalog_draft" IS '菜单草稿，按平台或租户各一份';

CREATE TABLE "menu_catalog_release" (
    "id" int8 PRIMARY KEY,
    "scope" varchar(16) NOT NULL,
    "version" int4 NOT NULL,
    "document" text NOT NULL,
    "create_user" int8,
    "create_time" timestamp NOT NULL
);
CREATE INDEX "idx_menu_release_scope" ON "menu_catalog_release" ("scope", "create_time");
COMMENT ON TABLE "menu_catalog_release" IS '已发布菜单快照，用于回滚';

CREATE TABLE "tenant_package_menu_draft" (
    "package_id" int8 NOT NULL,
    "menu_id" int8 NOT NULL,
    PRIMARY KEY ("package_id", "menu_id")
);
COMMENT ON TABLE "tenant_package_menu_draft" IS '套餐待发布菜单';

INSERT INTO "tenant_package_menu_draft" ("package_id", "menu_id")
SELECT "package_id", "menu_id" FROM "tenant_package_menu";

CREATE TABLE "tenant_package_grant" (
    "tenant_id" int8 NOT NULL,
    "package_id" int8 NOT NULL,
    "start_at" timestamp,
    "end_at" timestamp,
    PRIMARY KEY ("tenant_id", "package_id")
);
COMMENT ON TABLE "tenant_package_grant" IS '企业生效中的主套餐和增购包';

INSERT INTO "tenant_package_grant" ("tenant_id", "package_id", "start_at")
SELECT "id", "package_id", "create_time" FROM "tenant" WHERE "package_id" IS NOT NULL;

CREATE TABLE "catalog_audit_log" (
    "id" int8 PRIMARY KEY,
    "module" varchar(32) NOT NULL,
    "action" varchar(32) NOT NULL,
    "object_id" varchar(64),
    "object_name" varchar(100),
    "before_value" text,
    "after_value" text,
    "result" varchar(16) NOT NULL,
    "operator_id" int8,
    "operator_name" varchar(64),
    "ip" varchar(64),
    "user_agent" varchar(200),
    "create_time" timestamp NOT NULL
);
CREATE INDEX "idx_catalog_audit_time" ON "catalog_audit_log" ("create_time");
COMMENT ON TABLE "catalog_audit_log" IS '菜单与套餐操作审计，只追加';
