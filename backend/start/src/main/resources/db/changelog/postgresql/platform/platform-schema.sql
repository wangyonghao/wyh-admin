-- liquibase formatted sql

-- changeset wangyonghao:1
-- comment system-初始化表结构
CREATE TABLE IF NOT EXISTS "sys_config"
(
    "id"           BIGSERIAL PRIMARY KEY,
    "config_key"   VARCHAR(100) NOT NULL UNIQUE,
    "config_value" JSONB,
    "description"  VARCHAR(255),
    "create_time"  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "update_time"  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS "idx_sys_config_key" ON "sys_config" ("config_key");
COMMENT ON TABLE "sys_config" IS '系统配置表';
COMMENT ON COLUMN "sys_config"."id" IS 'ID';
COMMENT ON COLUMN "sys_config"."config_key" IS '配置键，如 site, login, mail, sms, storage, password-policy';
COMMENT ON COLUMN "sys_config"."config_value" IS '配置值，JSON格式存储';
COMMENT ON COLUMN "sys_config"."description" IS '配置说明';
COMMENT ON COLUMN "sys_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_config"."update_time" IS '更新时间';

CREATE TABLE IF NOT EXISTS "sys_menu"
(
    "id"          int8      NOT NULL,
    "parent_id"   int8      NOT NULL DEFAULT 0,
    "type"        int2      NOT NULL DEFAULT 1,
    "path"        varchar(255)       DEFAULT NULL,
    "name"        varchar(50)        DEFAULT NULL,
    "component"   varchar(255)       DEFAULT NULL,
    "redirect"    varchar(255)       DEFAULT NULL,
    "icon"        varchar(50)        DEFAULT NULL,
    "is_external" bool               DEFAULT FALSE,
    "is_cache"    bool               DEFAULT FALSE,
    "is_hidden"   bool               DEFAULT FALSE,
    "permission"  varchar(100)       DEFAULT NULL,
    "sort"        int4      NOT NULL DEFAULT 999,
    "status"      int2      NOT NULL DEFAULT 1,
    "create_user" int8      NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_user" int8               DEFAULT NULL,
    "update_time" timestamp          DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_menu_parent_id" ON "sys_menu" ("parent_id");
CREATE UNIQUE INDEX "uk_menu_name_parent_id" ON "sys_menu" ("name", "parent_id");
COMMENT ON COLUMN "sys_menu"."id" IS 'ID';
COMMENT ON COLUMN "sys_menu"."name" IS '名称';
COMMENT ON COLUMN "sys_menu"."parent_id" IS '上级菜单ID';
COMMENT ON COLUMN "sys_menu"."type" IS '类型（1：目录；2：菜单；3：按钮）';
COMMENT ON COLUMN "sys_menu"."path" IS '路由地址';
COMMENT ON COLUMN "sys_menu"."component" IS '组件路径';
COMMENT ON COLUMN "sys_menu"."redirect" IS '重定向地址';
COMMENT ON COLUMN "sys_menu"."icon" IS '图标';
COMMENT ON COLUMN "sys_menu"."is_external" IS '是否外链';
COMMENT ON COLUMN "sys_menu"."is_cache" IS '是否缓存';
COMMENT ON COLUMN "sys_menu"."is_hidden" IS '是否隐藏';
COMMENT ON COLUMN "sys_menu"."permission" IS '权限标识';
COMMENT ON COLUMN "sys_menu"."sort" IS '排序';
COMMENT ON COLUMN "sys_menu"."status" IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_menu"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_menu"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_menu"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_menu"."update_time" IS '修改时间';
COMMENT ON TABLE "sys_menu" IS '菜单表';

CREATE TABLE IF NOT EXISTS "sys_department"
(
    "id"          int8         NOT NULL,
    "code"        varchar(20)  NOT NULL,
    "name"        varchar(30)  NOT NULL,
    "type"        varchar(20)  NOT NULL,
    "parent_id"   int8         NOT NULL DEFAULT 0,
    "ancestors"   varchar(512) NOT NULL DEFAULT '',
    "description" varchar(255)          DEFAULT NULL,
    "sort"        int4         NOT NULL DEFAULT 999,
    "status"      int2         NOT NULL DEFAULT 1,
    "is_builtin"  bool         NOT NULL DEFAULT FALSE,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8                  DEFAULT NULL,
    "update_time" timestamp             DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_department_parent_id" ON "sys_department" ("parent_id");
CREATE UNIQUE INDEX "uk_department_name_parent_id" ON "sys_department" ("name", "parent_id");
COMMENT ON COLUMN "sys_department"."id" IS 'ID';
COMMENT ON COLUMN "sys_department"."code" IS '编码';
COMMENT ON COLUMN "sys_department"."name" IS '名称';
COMMENT ON COLUMN "sys_department"."type" IS '部门类型';
COMMENT ON COLUMN "sys_department"."parent_id" IS '上级部门ID';
COMMENT ON COLUMN "sys_department"."ancestors" IS '祖级路径';
COMMENT ON COLUMN "sys_department"."description" IS '备注';
COMMENT ON COLUMN "sys_department"."sort" IS '排序';
COMMENT ON COLUMN "sys_department"."status" IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_department"."is_builtin" IS '是否为系统内置数据';
COMMENT ON COLUMN "sys_department"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_department"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_department"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_department"."update_time" IS '修改时间';
COMMENT ON TABLE "sys_department" IS '部门表';

CREATE TABLE IF NOT EXISTS "sys_role"
(
    "id"                  int8        NOT NULL,
    "name"                varchar(30) NOT NULL,
    "code"                varchar(30) NOT NULL,
    "data_scope"          int2        NOT NULL DEFAULT 4,
    "description"         varchar(255)         DEFAULT NULL,
    "sort"                int4        NOT NULL DEFAULT 999,
    "is_builtin"          bool        NOT NULL DEFAULT FALSE,
    "menu_check_strictly" bool                 DEFAULT TRUE,
    "department_check_strictly" bool                 DEFAULT TRUE,
    "create_user"         int8        NOT NULL,
    "create_time"         timestamp   NOT NULL,
    "update_user"         int8                 DEFAULT NULL,
    "update_time"         timestamp            DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_role_name" ON "sys_role" ("name");
CREATE UNIQUE INDEX "uk_role_code" ON "sys_role" ("code");
COMMENT ON COLUMN "sys_role"."id" IS 'ID';
COMMENT ON COLUMN "sys_role"."name" IS '名称';
COMMENT ON COLUMN "sys_role"."id" IS 'ID';
COMMENT ON COLUMN "sys_role"."name" IS '名称';
COMMENT ON COLUMN "sys_role"."code" IS '编码';
COMMENT ON COLUMN "sys_role"."data_scope" IS '数据权限（1：全部数据权限；2：本部门及以下数据权限；3：本部门数据权限；4：仅本人数据权限；5：自定义数据权限）';
COMMENT ON COLUMN "sys_role"."description" IS '描述';
COMMENT ON COLUMN "sys_role"."sort" IS '排序';
COMMENT ON COLUMN "sys_role"."is_builtin" IS '是否为系统内置数据';
COMMENT ON COLUMN "sys_role"."menu_check_strictly" IS '菜单选择是否父子节点关联';
COMMENT ON COLUMN "sys_role"."department_check_strictly" IS '部门选择是否父子节点关联';
COMMENT ON COLUMN "sys_role"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_role"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_role"."update_time" IS '修改时间';
COMMENT ON TABLE "sys_role" IS '角色表';

CREATE TABLE IF NOT EXISTS "sys_user"
(
    "id"                         int8        NOT NULL,
    "username"                   varchar(64) NOT NULL,
    "display_name"                   varchar(30) NOT NULL,
    "password"                   varchar(255)         DEFAULT NULL,
    "gender"                     int2        NOT NULL DEFAULT 0,
    "email"                      varchar(64)          DEFAULT NULL,
    "phone"                      varchar(64)          DEFAULT NULL,
    "avatar"                     int8                 DEFAULT NULL,
    "description"                varchar(255)         DEFAULT NULL,
    "status"                     int2        NOT NULL DEFAULT 1,
    "is_builtin"                 bool        NOT NULL DEFAULT FALSE,
    "pwd_update_time"            timestamp            DEFAULT NULL,
    "pwd_expire_date"            date                 DEFAULT NULL,
    "password_history"           text                 DEFAULT NULL,
    "department_id"                    int8        NOT NULL,
    "create_user"                int8                 DEFAULT NULL,
    "create_time"                timestamp   NOT NULL,
    "update_user"                int8                 DEFAULT NULL,
    "update_time"                timestamp            DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_user_username" ON "sys_user" ("username");
CREATE UNIQUE INDEX "uk_user_email" ON "sys_user" ("email");
CREATE UNIQUE INDEX "uk_user_phone" ON "sys_user" ("phone");
CREATE INDEX "idx_user_department_id" ON "sys_user" ("department_id");
CREATE INDEX "idx_user_create_user" ON "sys_user" ("create_user");
CREATE INDEX "idx_user_update_user" ON "sys_user" ("update_user");
COMMENT ON COLUMN "sys_user"."id" IS 'ID';
COMMENT ON COLUMN "sys_user"."username" IS '用户名';
COMMENT ON COLUMN "sys_user"."display_name" IS '显示名称';
COMMENT ON COLUMN "sys_user"."password" IS '密码';
COMMENT ON COLUMN "sys_user"."gender" IS '性别（0：未知；1：男；2：女）';
COMMENT ON COLUMN "sys_user"."email" IS '邮箱(加密）';
COMMENT ON COLUMN "sys_user"."phone" IS '手机号(加密）';
COMMENT ON COLUMN "sys_user"."avatar" IS '头像文件 ID（关联 file.id）';
COMMENT ON COLUMN "sys_user"."description" IS '描述';
COMMENT ON COLUMN "sys_user"."status" IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_user"."is_builtin" IS '是否为系统内置数据';
COMMENT ON COLUMN "sys_user"."pwd_update_time" IS '上次改密时间';
COMMENT ON COLUMN "sys_user"."pwd_expire_date" IS '密码过期日';
COMMENT ON COLUMN "sys_user"."password_history" IS '曾用密码哈希（| 拼接，最多 10 个，新在前）';
COMMENT ON COLUMN "sys_user"."department_id" IS '部门ID';
COMMENT ON COLUMN "sys_user"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_user"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_user"."update_time" IS '修改时间';
COMMENT ON TABLE "sys_user" IS '用户表';

CREATE TABLE IF NOT EXISTS "sys_user_social"
(
    "id"              int8         NOT NULL,
    "source"          varchar(255) NOT NULL,
    "open_id"         varchar(255) NOT NULL,
    "user_id"         int8         NOT NULL,
    "meta_json"       text      DEFAULT NULL,
    "last_login_time" timestamp DEFAULT NULL,
    "create_time"     timestamp    NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_user_source_open_id" ON "sys_user_social" ("source", "open_id");
COMMENT ON COLUMN "sys_user_social"."id" IS 'ID';
COMMENT ON COLUMN "sys_user_social"."source" IS '来源';
COMMENT ON COLUMN "sys_user_social"."open_id" IS '开放ID';
COMMENT ON COLUMN "sys_user_social"."user_id" IS '用户ID';
COMMENT ON COLUMN "sys_user_social"."meta_json" IS '附加信息';
COMMENT ON COLUMN "sys_user_social"."last_login_time" IS '最后登录时间';
COMMENT ON COLUMN "sys_user_social"."create_time" IS '创建时间';
COMMENT ON TABLE "sys_user_social" IS '用户社会化关联表';

CREATE TABLE IF NOT EXISTS "sys_user_role"
(
    "id"      int8 NOT NULL,
    "user_id" int8 NOT NULL,
    "role_id" int8 NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_user_id_role_id" ON "sys_user_role" ("user_id", "role_id");
COMMENT ON COLUMN "sys_user_role"."id" IS 'ID';
COMMENT ON COLUMN "sys_user_role"."user_id" IS '用户ID';
COMMENT ON COLUMN "sys_user_role"."role_id" IS '角色ID';
COMMENT ON TABLE "sys_user_role" IS '用户和角色关联表';

CREATE TABLE IF NOT EXISTS "sys_role_menu"
(
    "role_id" int8 NOT NULL,
    "menu_id" int8 NOT NULL,
    PRIMARY KEY ("role_id", "menu_id")
);
COMMENT ON COLUMN "sys_role_menu"."role_id" IS '角色ID';
COMMENT ON COLUMN "sys_role_menu"."menu_id" IS '菜单ID';
COMMENT ON TABLE "sys_role_menu" IS '角色和菜单关联表';

CREATE TABLE IF NOT EXISTS "sys_role_department"
(
    "role_id" int8 NOT NULL,
    "department_id" int8 NOT NULL,
    PRIMARY KEY ("role_id", "department_id")
);
COMMENT ON COLUMN "sys_role_department"."role_id" IS '角色ID';
COMMENT ON COLUMN "sys_role_department"."department_id" IS '部门ID';
COMMENT ON TABLE "sys_role_department" IS '角色和部门关联表';

CREATE TABLE IF NOT EXISTS "sys_membership"
(
    "id"          int8        NOT NULL,
    "user_id"     int8        NOT NULL,
    "scope_type"  varchar(32) NOT NULL,
    "scope_id"    int8        NOT NULL,
    "is_primary"  bool        NOT NULL DEFAULT FALSE,
    "status"      int2        NOT NULL DEFAULT 1,
    "joined_at"   timestamp            DEFAULT NULL,
    "expired_at"  timestamp            DEFAULT NULL,
    "create_user" int8                 DEFAULT NULL,
    "create_time" timestamp   NOT NULL,
    "update_user" int8                 DEFAULT NULL,
    "update_time" timestamp            DEFAULT NULL,
    "deleted"     int2        NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "sys_membership" IS '成员关系（用户-部门 / 用户-角色）';
COMMENT ON COLUMN "sys_membership"."id" IS 'ID';
COMMENT ON COLUMN "sys_membership"."user_id" IS '用户 ID';
COMMENT ON COLUMN "sys_membership"."scope_type" IS '范围类型：DEPARTMENT / ROLE / TENANT';
COMMENT ON COLUMN "sys_membership"."scope_id" IS '范围 ID（部门 ID 或角色 ID）';
COMMENT ON COLUMN "sys_membership"."is_primary" IS '是否主部门（仅 DEPARTMENT）';
COMMENT ON COLUMN "sys_membership"."status" IS '状态（1：正常；0：停用；2：邀请中）';
COMMENT ON COLUMN "sys_membership"."joined_at" IS '加入时间';
COMMENT ON COLUMN "sys_membership"."expired_at" IS '过期时间（空表示长期有效）';
COMMENT ON COLUMN "sys_membership"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_membership"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_membership"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_membership"."update_time" IS '修改时间';
COMMENT ON COLUMN "sys_membership"."deleted" IS '是否删除（0：否；1：是）';
CREATE UNIQUE INDEX "uk_membership_active"
    ON "sys_membership" ("user_id", "scope_type", "scope_id")
    WHERE "deleted" = 0;
CREATE UNIQUE INDEX "uk_membership_primary_department"
    ON "sys_membership" ("user_id")
    WHERE "deleted" = 0
      AND "scope_type" = 'DEPARTMENT'
      AND "is_primary" = TRUE;
CREATE INDEX "idx_membership_scope"
    ON "sys_membership" ("scope_type", "scope_id")
    WHERE "deleted" = 0;
CREATE INDEX "idx_membership_user"
    ON "sys_membership" ("user_id")
    WHERE "deleted" = 0;
CREATE INDEX "idx_membership_scope_active"
    ON "sys_membership" ("scope_type", "scope_id", "status")
    WHERE "deleted" = 0;

-- 字典选项表
DROP TABLE IF EXISTS "sys_options";

CREATE TABLE IF NOT EXISTS "sys_options"
(
    "id"          BIGSERIAL PRIMARY KEY,
    "option_type" varchar(100) NOT NULL,
    "value"       varchar(255) NOT NULL,
    "label"       varchar(255) NOT NULL,
    "ext"         jsonb        DEFAULT NULL,
    "sort"        int4         DEFAULT 0,
    "enabled"     bool         DEFAULT TRUE,
    "description" varchar(500) DEFAULT NULL,
    "create_time" timestamp    DEFAULT CURRENT_TIMESTAMP,
    "create_user" int8,
    "update_time" timestamp    DEFAULT CURRENT_TIMESTAMP,
    "update_user" int8,
    CONSTRAINT "uk_option_type_value" UNIQUE ("option_type", "value")
);
CREATE INDEX "idx_option_type_sort" ON "sys_options" ("option_type", "sort");
CREATE INDEX "idx_option_ext_jsonb" ON "sys_options" USING gin ("ext");

COMMENT ON COLUMN "sys_options"."id" IS 'ID';
COMMENT ON COLUMN "sys_options"."option_type" IS '选项类型';
COMMENT ON COLUMN "sys_options"."value" IS '字典值';
COMMENT ON COLUMN "sys_options"."label" IS '字典标签';
COMMENT ON COLUMN "sys_options"."ext" IS '扩展信息(JSON)';
COMMENT ON COLUMN "sys_options"."sort" IS '排序';
COMMENT ON COLUMN "sys_options"."enabled" IS '是否启用';
COMMENT ON COLUMN "sys_options"."description" IS '描述';
COMMENT ON COLUMN "sys_options"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_options"."update_time" IS '更新时间';
COMMENT ON COLUMN "sys_options"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_options"."update_user" IS '更新人';
COMMENT ON TABLE "sys_options" IS '字典选项表';

DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log
(
    id            BIGSERIAL PRIMARY KEY NOT NULL,
    object_type   VARCHAR(50)   DEFAULT NULL,
    object_id     BIGINT        DEFAULT NULL,
    operation     VARCHAR(50)   DEFAULT NULL,
    operator_id   BIGINT        DEFAULT NULL,
    operator_name VARCHAR(50)   DEFAULT NULL,
    operator_ip   VARCHAR(50)   DEFAULT NULL,
    operate_time  TIMESTAMP     DEFAULT NULL,
    status        VARCHAR(20)   DEFAULT NULL,
    remark        VARCHAR(1000) DEFAULT NULL,
    extra         TEXT          DEFAULT NULL
);
CREATE INDEX idx_sys_operation_log_object ON sys_operation_log (object_type, object_id);
CREATE INDEX idx_sys_operation_log_operator ON sys_operation_log (operator_id);
CREATE INDEX idx_sys_operation_log_operate_time ON sys_operation_log (operate_time);

COMMENT ON TABLE sys_operation_log IS '系统操作日志表';
COMMENT ON COLUMN sys_operation_log.object_type IS '业务对象类型';
COMMENT ON COLUMN sys_operation_log.object_id IS '业务对象ID';
COMMENT ON COLUMN sys_operation_log.operation IS '操作类型';
COMMENT ON COLUMN sys_operation_log.operator_id IS '操作者ID';
COMMENT ON COLUMN sys_operation_log.operator_name IS '操作者名称';
COMMENT ON COLUMN sys_operation_log.operator_ip IS '操作者IP';
COMMENT ON COLUMN sys_operation_log.operate_time IS '操作时间';
COMMENT ON COLUMN sys_operation_log.status IS '状态';
COMMENT ON COLUMN sys_operation_log.remark IS '备注';
COMMENT ON COLUMN sys_operation_log.extra IS '额外JSON格式数据';


CREATE TABLE IF NOT EXISTS "sys_message"
(
    "id"          int8        NOT NULL,
    "title"       varchar(50) NOT NULL,
    "content"     text                 DEFAULT NULL,
    "type"        int2        NOT NULL DEFAULT 1,
    "path"        varchar(255)         DEFAULT NULL,
    "scope"       int2        NOT NULL DEFAULT 1,
    "users"       json                 DEFAULT NULL,
    "create_time" timestamp   NOT NULL,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "sys_message"."id" IS 'ID';
COMMENT ON COLUMN "sys_message"."title" IS '标题';
COMMENT ON COLUMN "sys_message"."content" IS '内容';
COMMENT ON COLUMN "sys_message"."type" IS '类型（1：系统消息；2：安全消息）';
COMMENT ON COLUMN "sys_message"."path" IS '跳转路径';
COMMENT ON COLUMN "sys_message"."scope" IS '通知范围（1：所有人；2：指定用户）';
COMMENT ON COLUMN "sys_message"."users" IS '通知用户';
COMMENT ON COLUMN "sys_message"."create_time" IS '创建时间';
COMMENT ON TABLE "sys_message" IS '消息表';

CREATE TABLE IF NOT EXISTS "sys_message_log"
(
    "message_id" int8 NOT NULL,
    "user_id"    int8 NOT NULL,
    "read_time"  timestamp DEFAULT NULL,
    PRIMARY KEY ("message_id", "user_id")
);
COMMENT ON COLUMN "sys_message_log"."message_id" IS '消息ID';
COMMENT ON COLUMN "sys_message_log"."user_id" IS '用户ID';
COMMENT ON COLUMN "sys_message_log"."read_time" IS '读取时间';
COMMENT ON TABLE "sys_message_log" IS '消息日志表';

CREATE TABLE IF NOT EXISTS "sys_notice"
(
    "id"             int8         NOT NULL,
    "title"          varchar(150) NOT NULL,
    "content"        text         NOT NULL,
    "type"           varchar(30)  NOT NULL,
    "notice_scope"   int2         NOT NULL DEFAULT 1,
    "notice_users"   json                  DEFAULT NULL,
    "notice_methods" json                  DEFAULT NULL,
    "is_timing"      bool         NOT NULL DEFAULT FALSE,
    "publish_time"   timestamp             DEFAULT NULL,
    "is_top"         bool         NOT NULL DEFAULT FALSE,
    "status"         int2         NOT NULL DEFAULT 1,
    "create_user"    int8         NOT NULL,
    "create_time"    timestamp    NOT NULL,
    "update_user"    int8                  DEFAULT NULL,
    "update_time"    timestamp             DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_notice_create_user" ON "sys_notice" ("create_user");
CREATE INDEX "idx_notice_update_user" ON "sys_notice" ("update_user");
COMMENT ON COLUMN "sys_notice"."id" IS 'ID';
COMMENT ON COLUMN "sys_notice"."title" IS '标题';
COMMENT ON COLUMN "sys_notice"."content" IS '内容';
COMMENT ON COLUMN "sys_notice"."type" IS '分类';
COMMENT ON COLUMN "sys_notice"."notice_scope" IS '通知范围（1：所有人；2：指定用户）';
COMMENT ON COLUMN "sys_notice"."notice_users" IS '通知用户';
COMMENT ON COLUMN "sys_notice"."notice_methods" IS '通知方式（1：系统消息；2：登录弹窗）';
COMMENT ON COLUMN "sys_notice"."is_timing" IS '是否定时';
COMMENT ON COLUMN "sys_notice"."publish_time" IS '发布时间';
COMMENT ON COLUMN "sys_notice"."is_top" IS '是否置顶';
COMMENT ON COLUMN "sys_notice"."status" IS '状态（1：草稿；2：待发布；3：已发布）';
COMMENT ON COLUMN "sys_notice"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_notice"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_notice"."update_user" IS '修改人';
COMMENT ON COLUMN "sys_notice"."update_time" IS '修改时间';
COMMENT ON TABLE "sys_notice" IS '公告表';

CREATE TABLE IF NOT EXISTS "sys_notice_log"
(
    "notice_id" int8 NOT NULL,
    "user_id"   int8 NOT NULL,
    "read_time" timestamp DEFAULT NULL,
    PRIMARY KEY ("notice_id", "user_id")
);
COMMENT ON COLUMN "sys_notice_log"."notice_id" IS '消息ID';
COMMENT ON COLUMN "sys_notice_log"."user_id" IS '用户ID';
COMMENT ON COLUMN "sys_notice_log"."read_time" IS '读取时间';
COMMENT ON TABLE "sys_notice_log" IS '公告日志表';

--
-- CREATE TABLE IF NOT EXISTS "sys_sms_config"
-- (
--     "id"              int8         NOT NULL,
--     "name"            varchar(100) NOT NULL,
--     "supplier"        varchar(50)  NOT NULL,
--     "access_key"      varchar(255) NOT NULL,
--     "secret_key"      varchar(255) NOT NULL,
--     "signature"       varchar(100)          DEFAULT NULL,
--     "template_id"     varchar(50)           DEFAULT NULL,
--     "weight"          int4                  DEFAULT NULL,
--     "retry_interval"  int4                  DEFAULT NULL,
--     "max_retries"     int4                  DEFAULT NULL,
--     "maximum"         int4                  DEFAULT NULL,
--     "supplier_config" text                  DEFAULT NULL,
--     "is_default"      bool         NOT NULL DEFAULT false,
--     "status"          int2         NOT NULL DEFAULT 1,
--     "create_user"     int8         NOT NULL,
--     "create_time"     timestamp    NOT NULL,
--     "update_user"     int8                  DEFAULT NULL,
--     "update_time"     timestamp             DEFAULT NULL,
--     PRIMARY KEY ("id")
-- );
-- CREATE INDEX "idx_sms_config_create_user" ON "sys_sms_config" ("create_user");
-- CREATE INDEX "idx_sms_config_update_user" ON "sys_sms_config" ("update_user");
-- COMMENT ON COLUMN "sys_sms_config"."id" IS 'ID';
-- COMMENT ON COLUMN "sys_sms_config"."name" IS '名称';
-- COMMENT ON COLUMN "sys_sms_config"."supplier" IS '厂商';
-- COMMENT ON COLUMN "sys_sms_config"."access_key" IS 'Access Key';
-- COMMENT ON COLUMN "sys_sms_config"."secret_key" IS 'Secret Key';
-- COMMENT ON COLUMN "sys_sms_config"."signature" IS '短信签名';
-- COMMENT ON COLUMN "sys_sms_config"."template_id" IS '模板ID';
-- COMMENT ON COLUMN "sys_sms_config"."weight" IS '负载均衡权重';
-- COMMENT ON COLUMN "sys_sms_config"."retry_interval" IS '重试间隔（单位：秒）';
-- COMMENT ON COLUMN "sys_sms_config"."max_retries" IS '重试次数';
-- COMMENT ON COLUMN "sys_sms_config"."maximum" IS '发送上限';
-- COMMENT ON COLUMN "sys_sms_config"."supplier_config" IS '各个厂商独立配置';
-- COMMENT ON COLUMN "sys_sms_config"."is_default" IS '是否为默认配置';
-- COMMENT ON COLUMN "sys_sms_config"."status" IS '状态（1：启用；2：禁用）';
-- COMMENT ON COLUMN "sys_sms_config"."create_user" IS '创建人';
-- COMMENT ON COLUMN "sys_sms_config"."create_time" IS '创建时间';
-- COMMENT ON COLUMN "sys_sms_config"."update_user" IS '修改人';
-- COMMENT ON COLUMN "sys_sms_config"."update_time" IS '修改时间';
-- COMMENT ON TABLE "sys_sms_config" IS '短信配置表';

CREATE TABLE IF NOT EXISTS "sys_sms_log"
(
    "id"          int8        NOT NULL,
    "config_id"   int8        NOT NULL,
    "phone"       varchar(25) NOT NULL,
    "params"      text                 DEFAULT NULL,
    "status"      int2        NOT NULL DEFAULT 1,
    "res_msg"     text                 DEFAULT NULL,
    "create_user" int8        NOT NULL,
    "create_time" timestamp   NOT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_sms_log_config_id" ON "sys_sms_log" ("config_id");
CREATE INDEX "idx_sms_log_create_user" ON "sys_sms_log" ("create_user");
COMMENT ON COLUMN "sys_sms_log"."id" IS 'ID';
COMMENT ON COLUMN "sys_sms_log"."config_id" IS '配置ID';
COMMENT ON COLUMN "sys_sms_log"."phone" IS '手机号';
COMMENT ON COLUMN "sys_sms_log"."params" IS '参数配置';
COMMENT ON COLUMN "sys_sms_log"."status" IS '发送状态（1：成功；2：失败）';
COMMENT ON COLUMN "sys_sms_log"."res_msg" IS '返回数据';
COMMENT ON COLUMN "sys_sms_log"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_sms_log"."create_time" IS '创建时间';
COMMENT ON TABLE "sys_sms_log" IS '短信日志表';

CREATE TABLE IF NOT EXISTS "sys_login_log"
(
    "id"             int8         NOT NULL,
    "username"       varchar(50)  NOT NULL,
    "ip_address"     varchar(50)  NOT NULL,
    "location"       varchar(200) NOT NULL,
    "device_type"    varchar(20)  NOT NULL,
    "browser"        varchar(100) NOT NULL,
    "os"             varchar(100) NOT NULL,
    "login_status"   varchar(20)  NOT NULL,
    "login_time"     timestamp    NOT NULL,
    "failure_reason" varchar(500) DEFAULT NULL,
    "user_agent"     varchar(500) DEFAULT NULL,
    "tenant_id"      int8         DEFAULT NULL,
    PRIMARY KEY ("id")
);
-- 创建索引
CREATE INDEX "idx_login_log_username" ON "sys_login_log" ("username");
CREATE INDEX "idx_login_log_ip_address" ON "sys_login_log" ("ip_address");
CREATE INDEX "idx_login_log_login_status" ON "sys_login_log" ("login_status");
CREATE INDEX "idx_login_log_login_time" ON "sys_login_log" ("login_time");
CREATE INDEX "idx_login_log_tenant_id" ON "sys_login_log" ("tenant_id");
-- 添加字段注释
COMMENT ON COLUMN "sys_login_log"."id" IS '主键ID';
COMMENT ON COLUMN "sys_login_log"."username" IS '用户名';
COMMENT ON COLUMN "sys_login_log"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "sys_login_log"."location" IS '地理位置';
COMMENT ON COLUMN "sys_login_log"."device_type" IS '设备类型（WEB：浏览器端，MOBILE：移动端，WECHAT_MINI_PROGRAM：微信小程序）';
COMMENT ON COLUMN "sys_login_log"."browser" IS '浏览器';
COMMENT ON COLUMN "sys_login_log"."os" IS '操作系统';
COMMENT ON COLUMN "sys_login_log"."login_status" IS '登录状态（SUCCESS：成功，FAILURE：失败）';
COMMENT ON COLUMN "sys_login_log"."login_time" IS '登录时间';
COMMENT ON COLUMN "sys_login_log"."failure_reason" IS '失败原因';
COMMENT ON COLUMN "sys_login_log"."user_agent" IS 'User-Agent';
COMMENT ON COLUMN "sys_login_log"."tenant_id" IS '租户ID';
COMMENT ON TABLE "sys_login_log" IS '登录日志表';

-- 文件元数据
CREATE TABLE IF NOT EXISTS "sys_file"
(
    "id"           BIGSERIAL PRIMARY KEY,
    "file_name"    VARCHAR(255) NOT NULL,
    "content_type" VARCHAR(100),
    "file_size"    BIGINT       NOT NULL,
    "sha256"       VARCHAR(64)  NOT NULL,
    "storage_type" INT2         NOT NULL DEFAULT 1,
    "storage_key"  VARCHAR(500) NOT NULL,
    "status"       INT2         NOT NULL DEFAULT 1,
    "create_user"  BIGINT       NOT NULL,
    "create_time"  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_user"  BIGINT,
    "update_time"  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);
-- 创建索引
CREATE INDEX IF NOT EXISTS "idx_file_sha256" ON "sys_file" ("sha256");
CREATE INDEX IF NOT EXISTS "idx_file_storage_key" ON "sys_file" ("storage_key");
CREATE INDEX IF NOT EXISTS "idx_file_status" ON "sys_file" ("status");
CREATE INDEX IF NOT EXISTS "idx_file_create_time" ON "sys_file" ("create_time");
-- 添加注释
COMMENT ON TABLE "sys_file" IS '文件表（物理文件元数据）';
COMMENT ON COLUMN "sys_file"."id" IS '文件 ID';
COMMENT ON COLUMN "sys_file"."file_name" IS '文件名（原始文件名）';
COMMENT ON COLUMN "sys_file"."content_type" IS '内容类型（MIME Type）';
COMMENT ON COLUMN "sys_file"."file_size" IS '文件大小（字节）';
COMMENT ON COLUMN "sys_file"."sha256" IS 'SHA-256 哈希值（用于完整性校验和去重）';
COMMENT ON COLUMN "sys_file"."storage_type" IS '存储类型（1-本地存储；2-OSS 对象存储）';
COMMENT ON COLUMN "sys_file"."storage_key" IS '存储键（全局唯一，格式：[{key-prefix}/]{yyyy}/{MM}/{dd}/{uuid}[.ext]）';
COMMENT ON COLUMN "sys_file"."status" IS '文件状态（1-正常；2-已删除；3-已清除）';
COMMENT ON COLUMN "sys_file"."create_user" IS '创建人';
COMMENT ON COLUMN "sys_file"."create_time" IS '创建时间';
COMMENT ON COLUMN "sys_file"."update_user" IS '更新人';
COMMENT ON COLUMN "sys_file"."update_time" IS '更新时间';

-- =============================================================================
-- schedule
-- =============================================================================
-- comment 定时任务业务表
CREATE TABLE IF NOT EXISTS "sys_job"
(
    "id"                BIGINT       NOT NULL,
    "name"              VARCHAR(64)  NOT NULL,
    "handler_code"      VARCHAR(64)  NOT NULL,
    "cron"              VARCHAR(128) NOT NULL,
    "schedule_mode"     VARCHAR(32)  NOT NULL,
    "schedule_payload"  TEXT,
    "schedule_label"    VARCHAR(255),
    "params"            TEXT,
    "status"            INT2         NOT NULL DEFAULT 0,
    "remark"            VARCHAR(500),
    "create_user"       BIGINT       NOT NULL,
    "create_time"       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_user"       BIGINT,
    "update_time"       TIMESTAMP,
    PRIMARY KEY ("id")
);

CREATE INDEX IF NOT EXISTS "idx_sys_job_handler_code" ON "sys_job" ("handler_code");
CREATE INDEX IF NOT EXISTS "idx_sys_job_status" ON "sys_job" ("status");

COMMENT ON TABLE "sys_job" IS '定时任务';
COMMENT ON COLUMN "sys_job"."handler_code" IS '已注册任务编码';
COMMENT ON COLUMN "sys_job"."cron" IS 'Quartz Cron';
COMMENT ON COLUMN "sys_job"."schedule_mode" IS 'DAILY/WEEKLY/MONTHLY/INTERVAL/CRON';
COMMENT ON COLUMN "sys_job"."status" IS '0 停止 1 激活';

CREATE TABLE IF NOT EXISTS "sys_job_log"
(
    "id"             BIGINT      NOT NULL,
    "job_id"         BIGINT      NOT NULL,
    "job_name"       VARCHAR(64),
    "handler_code"   VARCHAR(64) NOT NULL,
    "trigger_type"   VARCHAR(16) NOT NULL,
    "start_time"     TIMESTAMP   NOT NULL,
    "end_time"       TIMESTAMP,
    "duration_ms"    BIGINT,
    "status"         INT2        NOT NULL,
    "error_message"  VARCHAR(2000),
    PRIMARY KEY ("id")
);

CREATE INDEX IF NOT EXISTS "idx_sys_job_log_job_id" ON "sys_job_log" ("job_id");
CREATE INDEX IF NOT EXISTS "idx_sys_job_log_start_time" ON "sys_job_log" ("start_time");

COMMENT ON TABLE "sys_job_log" IS '定时任务执行日志';
COMMENT ON COLUMN "sys_job_log"."trigger_type" IS 'CRON / MANUAL';
COMMENT ON COLUMN "sys_job_log"."status" IS '1 运行中 2 成功 3 失败';


-- comment Quartz
CREATE TABLE IF NOT EXISTS qrtz_job_details
(
    sched_name        VARCHAR(120) NOT NULL,
    job_name          VARCHAR(200) NOT NULL,
    job_group         VARCHAR(200) NOT NULL,
    description       VARCHAR(250) NULL,
    job_class_name    VARCHAR(250) NOT NULL,
    is_durable        BOOL         NOT NULL,
    is_nonconcurrent  BOOL         NOT NULL,
    is_update_data    BOOL         NOT NULL,
    requests_recovery BOOL         NOT NULL,
    job_data          BYTEA        NULL,
    PRIMARY KEY (sched_name, job_name, job_group)
);

CREATE TABLE IF NOT EXISTS qrtz_triggers
(
    sched_name     VARCHAR(120) NOT NULL,
    trigger_name   VARCHAR(200) NOT NULL,
    trigger_group  VARCHAR(200) NOT NULL,
    job_name       VARCHAR(200) NOT NULL,
    job_group      VARCHAR(200) NOT NULL,
    description    VARCHAR(250) NULL,
    next_fire_time BIGINT       NULL,
    prev_fire_time BIGINT       NULL,
    priority       INTEGER      NULL,
    trigger_state  VARCHAR(16)  NOT NULL,
    trigger_type   VARCHAR(8)   NOT NULL,
    start_time     BIGINT       NOT NULL,
    end_time       BIGINT       NULL,
    calendar_name  VARCHAR(200) NULL,
    misfire_instr  SMALLINT     NULL,
    job_data       BYTEA        NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, job_name, job_group)
        REFERENCES qrtz_job_details (sched_name, job_name, job_group)
);

CREATE TABLE IF NOT EXISTS qrtz_simple_triggers
(
    sched_name      VARCHAR(120) NOT NULL,
    trigger_name    VARCHAR(200) NOT NULL,
    trigger_group   VARCHAR(200) NOT NULL,
    repeat_count    BIGINT       NOT NULL,
    repeat_interval BIGINT       NOT NULL,
    times_triggered BIGINT       NOT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS qrtz_cron_triggers
(
    sched_name      VARCHAR(120) NOT NULL,
    trigger_name    VARCHAR(200) NOT NULL,
    trigger_group   VARCHAR(200) NOT NULL,
    cron_expression VARCHAR(120) NOT NULL,
    time_zone_id    VARCHAR(80),
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS qrtz_simprop_triggers
(
    sched_name    VARCHAR(120)   NOT NULL,
    trigger_name  VARCHAR(200)   NOT NULL,
    trigger_group VARCHAR(200)   NOT NULL,
    str_prop_1    VARCHAR(512)   NULL,
    str_prop_2    VARCHAR(512)   NULL,
    str_prop_3    VARCHAR(512)   NULL,
    int_prop_1    INT            NULL,
    int_prop_2    INT            NULL,
    long_prop_1   BIGINT         NULL,
    long_prop_2   BIGINT         NULL,
    dec_prop_1    NUMERIC(13, 4) NULL,
    dec_prop_2    NUMERIC(13, 4) NULL,
    bool_prop_1   BOOL           NULL,
    bool_prop_2   BOOL           NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS qrtz_blob_triggers
(
    sched_name    VARCHAR(120) NOT NULL,
    trigger_name  VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    blob_data     BYTEA        NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS qrtz_calendars
(
    sched_name    VARCHAR(120) NOT NULL,
    calendar_name VARCHAR(200) NOT NULL,
    calendar      BYTEA        NOT NULL,
    PRIMARY KEY (sched_name, calendar_name)
);

CREATE TABLE IF NOT EXISTS qrtz_paused_trigger_grps
(
    sched_name    VARCHAR(120) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    PRIMARY KEY (sched_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS qrtz_fired_triggers
(
    sched_name        VARCHAR(120) NOT NULL,
    entry_id          VARCHAR(95)  NOT NULL,
    trigger_name      VARCHAR(200) NOT NULL,
    trigger_group     VARCHAR(200) NOT NULL,
    instance_name     VARCHAR(200) NOT NULL,
    fired_time        BIGINT       NOT NULL,
    sched_time        BIGINT       NOT NULL,
    priority          INTEGER      NOT NULL,
    state             VARCHAR(16)  NOT NULL,
    job_name          VARCHAR(200) NULL,
    job_group         VARCHAR(200) NULL,
    is_nonconcurrent  BOOL         NULL,
    requests_recovery BOOL         NULL,
    PRIMARY KEY (sched_name, entry_id)
);

CREATE TABLE IF NOT EXISTS qrtz_scheduler_state
(
    sched_name        VARCHAR(120) NOT NULL,
    instance_name     VARCHAR(200) NOT NULL,
    last_checkin_time BIGINT       NOT NULL,
    checkin_interval  BIGINT       NOT NULL,
    PRIMARY KEY (sched_name, instance_name)
);

CREATE TABLE IF NOT EXISTS qrtz_locks
(
    sched_name VARCHAR(120) NOT NULL,
    lock_name  VARCHAR(40)  NOT NULL,
    PRIMARY KEY (sched_name, lock_name)
);

CREATE INDEX IF NOT EXISTS idx_qrtz_j_req_recovery ON qrtz_job_details (sched_name, requests_recovery);
CREATE INDEX IF NOT EXISTS idx_qrtz_j_grp ON qrtz_job_details (sched_name, job_group);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_j ON qrtz_triggers (sched_name, job_name, job_group);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_jg ON qrtz_triggers (sched_name, job_group);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_c ON qrtz_triggers (sched_name, calendar_name);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_g ON qrtz_triggers (sched_name, trigger_group);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_state ON qrtz_triggers (sched_name, trigger_state);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_n_state ON qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_n_g_state ON qrtz_triggers (sched_name, trigger_group, trigger_state);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_next_fire_time ON qrtz_triggers (sched_name, next_fire_time);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_nft_st ON qrtz_triggers (sched_name, trigger_state, next_fire_time);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_nft_misfire ON qrtz_triggers (sched_name, misfire_instr, next_fire_time);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_nft_st_misfire ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);
CREATE INDEX IF NOT EXISTS idx_qrtz_t_nft_st_misfire_grp ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);
CREATE INDEX IF NOT EXISTS idx_qrtz_ft_trig_inst_name ON qrtz_fired_triggers (sched_name, instance_name);
CREATE INDEX IF NOT EXISTS idx_qrtz_ft_inst_job_req_rcvry ON qrtz_fired_triggers (sched_name, instance_name, requests_recovery);
CREATE INDEX IF NOT EXISTS idx_qrtz_ft_j_g ON qrtz_fired_triggers (sched_name, job_name, job_group);
CREATE INDEX IF NOT EXISTS idx_qrtz_ft_jg ON qrtz_fired_triggers (sched_name, job_group);
CREATE INDEX IF NOT EXISTS idx_qrtz_ft_t_g ON qrtz_fired_triggers (sched_name, trigger_name, trigger_group);
CREATE INDEX IF NOT EXISTS idx_qrtz_ft_tg ON qrtz_fired_triggers (sched_name, trigger_group);

-- =============================================================================
-- openapi
-- =============================================================================
-- comment openapi-初始化能力开放插件数据表
-- 初始化表结构
CREATE TABLE IF NOT EXISTS "sys_app" (
    "id"          int8         NOT NULL,
    "name"        varchar(100) NOT NULL,
    "access_key"  varchar(255) NOT NULL,
    "secret_key"  varchar(255) NOT NULL,
    "expire_time" timestamp    DEFAULT NULL,
    "description" varchar(200) DEFAULT NULL,
    "status"      int2         NOT NULL DEFAULT 1,
    "create_user" int8         NOT NULL,
    "create_time" timestamp    NOT NULL,
    "update_user" int8         DEFAULT NULL,
    "update_time" timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_app_access_key" ON "sys_app" ("access_key");
CREATE INDEX "idx_app_create_user" ON "sys_app" ("create_user");
CREATE INDEX "idx_app_update_user" ON "sys_app" ("update_user");
COMMENT ON COLUMN "sys_app"."id"              IS 'ID';
COMMENT ON COLUMN "sys_app"."name"            IS '名称';
COMMENT ON COLUMN "sys_app"."access_key"      IS 'Access Key（访问密钥）';
COMMENT ON COLUMN "sys_app"."secret_key"      IS 'Secret Key（私有密钥）';
COMMENT ON COLUMN "sys_app"."expire_time"     IS '失效时间';
COMMENT ON COLUMN "sys_app"."description"     IS '描述';
COMMENT ON COLUMN "sys_app"."status"          IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "sys_app"."create_user"     IS '创建人';
COMMENT ON COLUMN "sys_app"."create_time"     IS '创建时间';
COMMENT ON COLUMN "sys_app"."update_user"     IS '修改人';
COMMENT ON COLUMN "sys_app"."update_time"     IS '修改时间';
COMMENT ON TABLE  "sys_app"                   IS '应用表';

-- =============================================================================
-- tenant 租户
-- =============================================================================
-- comment tenant-初始化租户插件数据表
CREATE TABLE IF NOT EXISTS "tenant" (
    "id"             int8         NOT NULL,
    "name"           varchar(30)  NOT NULL,
    "code"           varchar(30)  NOT NULL,
    "domain"         varchar(255) DEFAULT NULL,
    "expire_time"    timestamp    DEFAULT NULL,
    "description"    varchar(200) DEFAULT NULL,
    "status"         int2         NOT NULL DEFAULT 1,
    "admin_user"     int8         DEFAULT NULL,
    "admin_username" varchar(64)  DEFAULT NULL,
    "package_id"     int8         NOT NULL,
    "create_user"    int8         NOT NULL,
    "create_time"    timestamp    NOT NULL,
    "update_user"    int8         DEFAULT NULL,
    "update_time"    timestamp    DEFAULT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "uk_tenant_code" ON "tenant" ("code");
CREATE INDEX "idx_tenant_admin_user" ON "tenant" ("admin_user");
CREATE INDEX "idx_tenant_package_id" ON "tenant" ("package_id");
CREATE INDEX "idx_tenant_create_user" ON "tenant" ("create_user");
CREATE INDEX "idx_tenant_update_user" ON "tenant" ("update_user");
COMMENT ON COLUMN "tenant"."id" IS 'ID';
COMMENT ON COLUMN "tenant"."name" IS '名称';
COMMENT ON COLUMN "tenant"."code" IS '编码';
COMMENT ON COLUMN "tenant"."domain" IS '域名';
COMMENT ON COLUMN "tenant"."expire_time" IS '过期时间';
COMMENT ON COLUMN "tenant"."description" IS '描述';
COMMENT ON COLUMN "tenant"."status" IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "tenant"."package_id" IS '套餐ID';
COMMENT ON COLUMN "tenant"."admin_user" IS '管理员用户';
COMMENT ON COLUMN "tenant"."admin_username" IS '管理员用户名';
COMMENT ON COLUMN "tenant"."create_user" IS '创建人';
COMMENT ON COLUMN "tenant"."create_time" IS '创建时间';
COMMENT ON COLUMN "tenant"."update_user" IS '修改人';
COMMENT ON COLUMN "tenant"."update_time" IS '修改时间';
COMMENT ON TABLE "tenant" IS '租户表';

CREATE TABLE IF NOT EXISTS "tenant_package" (
    "id"                  int8         NOT NULL, 
    "name"                varchar(30)  NOT NULL, 
    "sort"                int4         NOT NULL DEFAULT 999, 
    "menu_check_strictly" bool         DEFAULT true, 
    "description"         varchar(200) DEFAULT NULL, 
    "status"              int2         NOT NULL DEFAULT 1, 
    "create_user"         int8         NOT NULL, 
    "create_time"         timestamp    NOT NULL, 
    "update_user"         int8         DEFAULT NULL, 
    "update_time"         timestamp    DEFAULT NULL, 
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_tenant_package_create_user" ON "tenant_package" ("create_user");
CREATE INDEX "idx_tenant_package_update_user" ON "tenant_package" ("update_user");
COMMENT ON COLUMN "tenant_package"."id" IS 'ID';
COMMENT ON COLUMN "tenant_package"."name" IS '名称';
COMMENT ON COLUMN "tenant_package"."sort" IS '排序';
COMMENT ON COLUMN "tenant_package"."menu_check_strictly" IS '菜单选择是否父子节点关联';
COMMENT ON COLUMN "tenant_package"."description" IS '描述';
COMMENT ON COLUMN "tenant_package"."status" IS '状态（1：启用；2：禁用）';
COMMENT ON COLUMN "tenant_package"."create_user" IS '创建人';
COMMENT ON COLUMN "tenant_package"."create_time" IS '创建时间';
COMMENT ON COLUMN "tenant_package"."update_user" IS '修改人';
COMMENT ON COLUMN "tenant_package"."update_time" IS '修改时间';
COMMENT ON TABLE "tenant_package" IS '租户套餐表';

CREATE TABLE IF NOT EXISTS "tenant_package_menu" (
    "package_id" int8 NOT NULL, 
    "menu_id"    int8 NOT NULL, 
    PRIMARY KEY ("package_id", "menu_id")
);
COMMENT ON COLUMN "tenant_package_menu"."package_id" IS '套餐ID';
COMMENT ON COLUMN "tenant_package_menu"."menu_id" IS '菜单ID';
COMMENT ON TABLE "tenant_package_menu" IS '租户套餐和菜单关联表';

-- 为已有表增加租户字段
ALTER TABLE "sys_department" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_department"."tenant_id" IS '租户ID';
CREATE INDEX "idx_department_tenant_id" ON "sys_department" ("tenant_id");

ALTER TABLE "sys_role" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_role"."tenant_id" IS '租户ID';
CREATE INDEX "idx_role_tenant_id" ON "sys_role" ("tenant_id");

ALTER TABLE "sys_user" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_user"."tenant_id" IS '租户ID';
CREATE INDEX "idx_user_tenant_id" ON "sys_user" ("tenant_id");

ALTER TABLE "sys_user_social" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_user_social"."tenant_id" IS '租户ID';
CREATE INDEX "idx_user_source_tenant_id" ON "sys_user_social" ("tenant_id");

ALTER TABLE "sys_role_menu" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_role_menu"."tenant_id" IS '租户ID';
CREATE INDEX "idx_role_menu_tenant_id" ON "sys_role_menu" ("tenant_id");

ALTER TABLE "sys_role_department" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_role_department"."tenant_id" IS '租户ID';
CREATE INDEX "idx_role_department_tenant_id" ON "sys_role_department" ("tenant_id");

ALTER TABLE "sys_membership" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_membership"."tenant_id" IS '租户ID';
CREATE INDEX "idx_membership_tenant_id" ON "sys_membership" ("tenant_id");
DROP INDEX IF EXISTS "uk_membership_active";
CREATE UNIQUE INDEX "uk_membership_active"
    ON "sys_membership" ("tenant_id", "user_id", "scope_type", "scope_id")
    WHERE "deleted" = 0;
DROP INDEX IF EXISTS "uk_membership_primary_department";
CREATE UNIQUE INDEX "uk_membership_primary_department"
    ON "sys_membership" ("tenant_id", "user_id")
    WHERE "deleted" = 0
      AND "scope_type" = 'DEPARTMENT'
      AND "is_primary" = TRUE;

ALTER TABLE "sys_operation_log" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_operation_log"."tenant_id" IS '租户ID';
CREATE INDEX "idx_log_tenant_id" ON "sys_operation_log" ("tenant_id");

ALTER TABLE "sys_message" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_message"."tenant_id" IS '租户ID';
CREATE INDEX "idx_message_tenant_id" ON "sys_message" ("tenant_id");

ALTER TABLE "sys_message_log" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_message_log"."tenant_id" IS '租户ID';
CREATE INDEX "idx_message_log_tenant_id" ON "sys_message_log" ("tenant_id");

ALTER TABLE "sys_notice" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_notice"."tenant_id" IS '租户ID';
CREATE INDEX "idx_notice_tenant_id" ON "sys_notice" ("tenant_id");

ALTER TABLE "sys_notice_log" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_notice_log"."tenant_id" IS '租户ID';
CREATE INDEX "idx_notice_log_tenant_id" ON "sys_notice_log" ("tenant_id");

ALTER TABLE "sys_file" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_file"."tenant_id" IS '租户ID';
CREATE INDEX "idx_file_tenant_id" ON "sys_file" ("tenant_id");

ALTER TABLE "sys_app" ADD COLUMN "tenant_id" int8 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "sys_app"."tenant_id" IS '租户ID';
CREATE INDEX "idx_app_tenant_id" ON "sys_app" ("tenant_id");

-- 调整唯一索引
DROP INDEX IF EXISTS "uk_department_name_parent_id";
CREATE UNIQUE INDEX "uk_department_name_parent_id" ON "sys_department" ("name", "parent_id", "tenant_id");

DROP INDEX IF EXISTS "uk_role_name", "uk_role_code";
CREATE UNIQUE INDEX "uk_role_name" ON "sys_role" ("name", "tenant_id");
CREATE UNIQUE INDEX "uk_role_code" ON "sys_role" ("code", "tenant_id");

DROP INDEX IF EXISTS "uk_user_username", "uk_user_email", "uk_user_phone";
CREATE UNIQUE INDEX "uk_user_username" ON "sys_user" ("username", "tenant_id");
CREATE UNIQUE INDEX "uk_user_email" ON "sys_user" ("email", "tenant_id");
CREATE UNIQUE INDEX "uk_user_phone" ON "sys_user" ("phone", "tenant_id");

DROP INDEX IF EXISTS "uk_app_access_key";
CREATE UNIQUE INDEX "uk_app_access_key" ON "sys_app" ("access_key", "tenant_id");
