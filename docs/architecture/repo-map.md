# wyh-admin — Repo map

Agent 按需阅读。完整目录树不必背诵；以本表与 `ls` / 搜索为准。

## Layout

```
wyh-admin/
├── backend/                         # Java 17 + Spring Boot（模块化单体）
│   ├── admin-start/                 # 装配：启动、配置、Liquibase
│   ├── admin-biz-system/            # 当前 COLA 业务单体
│   ├── admin-biz-system-client/     # 跨模块契约
│   ├── admin-openapi/               # 开放 API
│   └── admin-commons/               # 基础设施（无 COLA 要求）
│       ├── cmn-core
│       ├── cmn-web / cmn-db / cmn-cache   # 替代原 cmn-boot
│       ├── cmn-security / cmn-messaging / cmn-crypto / cmn-fastexcel
│       ├── cmn-storage/             # core + local / minio / aliyun / cos
│       ├── cmn-job                   # Quartz 内核
│       ├── cmn-tenant                # 隔离与上下文
│       └── cmn-license-*             # 未接入 admin-start
├── frontend/          # 产品后台（src/）+ apps + packages/ui + vendor 壳
├── specs/             # SDD 宪法
├── skills/            # Agent 工作流技能
├── docs/              # 架构、约定、runbook（见 docs/README.md）
├── frontend/DESIGN.md # 前端视觉宪法（中后台 UI）
└── AGENTS.md          # 短索引
```

仓库里已没有 `platform/`、`interfaces/`、`biz/` 目录，也没有 `cmn-boot` 模块。

## Backend modules

```
admin-start                         # 只装配
  → admin-biz-system
  → admin-openapi
  → 装配所需 cmn-*（web / db / cache / messaging / job / tenant）
admin-biz-system                    # COLA 业务单体（当前）
  → admin-biz-system-client
  → admin-commons/cmn-*
admin-openapi
  → admin-biz-system-client
  → admin-commons/cmn-*
admin-biz-system-client             # 仅 cmn-core + 注解
  → cmn-core
admin-commons/cmn-*
  → 其它 cmn-*
  禁止依赖 admin-biz-*-client、admin-biz-*、admin-openapi、admin-start
```

| 模块 | 职责 |
|------|------|
| `admin-start` | 只做装配：启动类、运行配置、Liquibase、API 文档与全局异常等接线。不放业务域 |
| `admin-biz-system` | 当前的 COLA 业务单体：身份、组织、授权、租户产品、文件、任务管理、设置、通知、审计、仪表盘 |
| `admin-biz-system-client` | 跨模块契约（接口与 DTO）。依赖止于 `cmn-core` 以及 Swagger / Jackson 注解 |
| `admin-openapi` | 开放 API：第三方应用接入与签名鉴权 |
| `admin-commons` | 基础设施聚合。不要求 COLA。禁止依赖 `admin-biz-*-client` |
| `cmn-core` | 共享内核：枚举、异常、常量、工具 |
| `cmn-web` | Web：Undertow / MVC / JSON / 校验 / API 文档 / Trace |
| `cmn-db` | 持久化：MyBatis-Plus / PostgreSQL / CosId / 数据权限 |
| `cmn-cache` | 缓存：Redisson / JetCache |
| `cmn-security` | Sa-Token / JustAuth / 图形验证码 / 密码哈希 |
| `cmn-messaging` | 邮件 / 短信 / WebSocket |
| `cmn-crypto` | 字段与 API 加密 |
| `cmn-fastexcel` | Excel |
| `cmn-storage` | 对象存储 SPI，以及 local / minio / aliyun / cos |
| `cmn-job` | Quartz 内核：调度器、Dispatcher、`JobHandler` SPI。任务管理 API 在 `admin-biz-system` 的 `system.admin` |
| `cmn-tenant` | 租户隔离与上下文（上下文、行隔离、Web 拦截）。租户与套餐的产品 CRUD 在 `admin-biz-system` 的 `system.admin` |
| `cmn-license-*` | 许可证生成 / 校验。未接入 `admin-start` |

`cmn-web`、`cmn-db`、`cmn-cache` 替代原来的 `cmn-boot`。

`admin-biz-system` 内两个限界上下文：`system.admin`（Admin 管理能力，一层 COLA：`adapter` / `app` / `domain` / `infrastructure`，用户/组织/角色菜单/部门/租户产品/文件/任务/设置/通知/审计/仪表盘等统一落在 `top.wyhao.system.admin`）与 `system.auth`（认证，独立 COLA）。**不要**再按 identity / security / department / tenant / file / job 拆出与 Admin 同级的完整 BC 切片；包内仅用子目录整理。`admin-biz-system-client` 里如 `top.wyhao.security.client`、`top.wyhao.identity.client` 等契约包名可保留，实现同属 Admin BC。模块内部的 `client` 包与 `adapter/client` 实现供本模块使用；给其它模块调用的契约放在 `admin-biz-system-client`。

`admin-commons` 按技术适配分包，不套 COLA。

边界：

- 会话模型 `LoginUser` 在 `admin-commons/cmn-core` 的 `top.wyhao.cmn.identity`，供 `cmn-security` / `cmn-web` / `cmn-db` 读取登录会话。`top.wyhao.identity.client` 只放 `admin-biz-system-client` 的出站契约（`UserProfile`、`UserProfileApi`）。
- `PackageMenuBinding`、`TenantLifecycleRules` 在 `admin-biz-system` 的 `system.admin.domain`。`cmn-tenant` 只保留上下文、行隔离、租户选择与 SPI。
- 邮件发送 `MailClient`、`MailConfig`、`MailConfigProvider`、`MailException` 在 `admin-commons/cmn-messaging` 的 `top.wyhao.admin.cmn.mail`。设置模块实现 `MailConfigProvider`，把库存的邮件配置映射成 SMTP 账户。设置接口的 `MailConfigVO` 仍在 `admin-biz-system-client`。

## Quick locate

### Backend

| 用途 | 路径 |
|------|------|
| 启动类 | `backend/admin-start/src/main/java/top/wyhao/admin/AdminApplication.java` |
| 开发配置 | `backend/admin-start/src/main/resources/config/application-dev.yml` |
| Liquibase | `backend/admin-start/src/main/resources/db/changelog/postgresql/` |
| Admin 管理能力（用户/组织/角色菜单/部门/租户产品/文件/任务/设置/通知/审计/仪表盘） | `backend/admin-biz-system/.../system/admin/`（`top.wyhao.system.admin`，一层 COLA） |
| 认证（登录/注册/OTP 等） | `backend/admin-biz-system/.../system/auth/`（`top.wyhao.system.auth`，独立 BC） |
| 租户隔离 / 上下文 | `backend/admin-commons/cmn-tenant/` |
| 对象存储 | `backend/admin-commons/cmn-storage/` |
| Quartz 内核 | `backend/admin-commons/cmn-job/` |
| 邮件发送 | `backend/admin-commons/cmn-messaging/.../cmn/mail/` |
| 随进程装配的 Job | `backend/admin-start/.../job/` |
| 开放 API | `backend/admin-openapi/` |
| 跨模块契约 | `backend/admin-biz-system-client/` |

本地全套：`cd backend/docker && docker compose --env-file .env -f docker-compose.dev.yml up`（含 API）。只在宿主机起后端：`cd backend && mvn -pl admin-start -am spring-boot:run`

### Frontend

| 用途 | 路径 |
|------|------|
| 产品后台 | `frontend/apps/admin`（`@wyh/admin`）。`pnpm dev` / `pnpm build` 与 `pnpm dev:admin` / `pnpm build:admin` 相同 |
| 入口 | `frontend/apps/admin/src/main.ts` |
| 路由 | `frontend/apps/admin/src/router/` |
| 登录 | `frontend/apps/admin/src/views/_core/authentication/` |
| 系统页 | `frontend/apps/admin/src/views/system/` |
| 租户页 | `frontend/apps/admin/src/views/tenant/` |
| API | `frontend/apps/admin/src/api/` |
| 前后端对齐 | `docs/architecture/repo-map.md` |
| 代码生成 | `AGENTS.md`（已从宪法删除，未实现） |
| 壳 / 内核 | `frontend/apps/admin/src/vben/`（`@vben/*`）；`frontend/vendor/@core`（慎改） |
| 原子 / 页面模式 | `frontend/packages/ui`（`@wyh/ui`）、`frontend/packages/ui-patterns`（`@wyh/ui-patterns`）。应用导入仍可用 `#/ui`、`#/ui-patterns` |
| 文档站 | `frontend/apps/docs`（`@wyh/docs`，VitePress）。`pnpm dev:docs` / `pnpm build:docs` |
| 组件演示 | `frontend/apps/design-system`（`@wyh/design-system`）。只演示上述两个包，不放业务页。`pnpm dev:ds` / `pnpm build:ds` |
| 工程配置 | 应用：`frontend/apps/admin` 的 `vite.config.ts`、env、`tsconfig.json`。仓库级：`frontend/eslint.config.mjs`、`frontend/tsconfig.json` |

开发：`cd frontend && pnpm dev`（产品后台，源码在 `apps/admin`）
