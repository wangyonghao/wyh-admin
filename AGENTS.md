# wyh-admin — Agent Guide

面向 AI Agent 的**短索引**。细则按需阅读链接；勿把本文件当百科全书扩写。

## Constitution & skills

| 文档 / 技能 | 用途 |
|-------------|------|
| [`docs/mission.md`](docs/mission.md) | 产品使命、用户、成功标准、非目标 |
| [`docs/tech-stack.md`](docs/tech-stack.md) | 技术选型、版本、架构硬规则 |
| [`docs/roadmap.md`](docs/roadmap.md) | 阶段状态与完成标准 |
| [`skills/feature-spec`](skills/feature-spec/SKILL.md) | 开阶段：访谈 + 写规格三件套 |
| [`skills/sdd-continue`](skills/sdd-continue/SKILL.md) | 续作：读宪法 → 推进下一阶段 |
| [`skills/changelog`](skills/changelog/SKILL.md) | 更新根目录 `CHANGELOG.md` |

Cursor 同步加载 [`.cursor/skills/`](.cursor/skills/) 同名技能；编码约束见 [`.cursor/rules/`](.cursor/rules/)。

文档目录契约见 [`docs/README.md`](docs/README.md)。

地图与详细约定（按需打开，非每次必读）：

- [`docs/architecture/repo-map.md`](docs/architecture/repo-map.md) — 模块地图与快速定位
- [`docs/conventions/backend-java.md`](docs/conventions/backend-java.md)
- [`docs/conventions/frontend.md`](docs/conventions/frontend.md)
- [`frontend/DESIGN.md`](frontend/DESIGN.md) — 前端视觉宪法（UI 风格）
- [`docs/conventions/ui-patterns.md`](docs/conventions/ui-patterns.md) — 页面范式与样板清单

## Commands

```bash
# JDK 必须 17（本机默认若为 26 会失败）
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
export PATH="$JAVA_HOME/bin:$PATH"

# 推荐：中间件（Apple Container / 本机）+ env.sh + 宿主机 API（见 backend/docker/README.md）
cp backend/scripts/env.example.sh backend/scripts/env.sh   # 首次
set -a && source backend/scripts/env.sh && set +a
cd backend && mvn -pl admin-start -am spring-boot:run
curl -s http://127.0.0.1:8000/actuator/health
# 可选 / CI：Docker Compose 全栈（非本机验收必选）
# cd backend/docker && cp .env.example .env && docker compose --env-file .env -f docker-compose.dev.yml up
# 单元测试（默认开启；临时跳过 -DskipUnitTests=true）。范围与 .github/workflows/backend-build.yml 一致
cd backend && mvn -pl admin-commons/cmn-job,admin-commons/cmn-web,admin-commons/cmn-db,admin-commons/cmn-cache,admin-commons/cmn-crypto,admin-biz-system-client -am test

# 前端产品后台（@wyh/admin）。pnpm dev 与 pnpm dev:admin 相同
cd frontend && pnpm install && pnpm dev
# 文档站 / 组件演示
cd frontend && pnpm dev:docs
cd frontend && pnpm dev:ds
```

配置：`backend/admin-start/src/main/resources/config/`  
环境变量示例：`backend/scripts/env.example.sh`（复制为 `backend/scripts/env.sh`，已被 `backend/.gitignore` 忽略）  
本地基础设施：`backend/docker/`（Compose 与 `.env.example`；无 Docker 时用 Apple Container 起中间件 + `backend/scripts/env.sh`；旧 `backend/scripts/docker/` 已删除）。JDK **17**。  
DB 变更：`backend/admin-start/src/main/resources/db/changelog/postgresql/`

## Hard boundaries

1. **不做微服务拆分**；保持模块化单体（见 mission / tech-stack）。
2. **依赖方向**：`admin-start`（只装配）→ `admin-openapi` / `admin-biz-*` → `admin-biz-*-client` 与 `admin-commons`（`cmn-*`）。`admin-commons` 禁止依赖 `admin-biz-*-client`。禁止下层依赖上层。
3. **新依赖**：后端进根 `pom.xml` 的 `dependencyManagement`；前端进 `pnpm-workspace.yaml` catalog。
4. **DB**：PostgreSQL + Liquibase；禁止只改库不改 changelog。
5. **调度**：Spring Quartz / `cmn-job`；勿并行引入另一套调度内核。
6. **领域错误**：`XxxException` 静态工厂；禁止业务里直接 `new BizException` / `BadRequestException`；禁止新增 `Check` / `ValidationUtils`。
7. **前端**：产品后台在 `frontend/apps/admin`（`@wyh/admin`）+ shadcn-vue / Vben 原子；壳、偏好、布局导航和多页签在 `apps/admin/src/components/vben`。共享包平铺在 `frontend/packages`（`ui`、`ui-patterns`、`form-ui`、`design`、`icons`、`shared`、`composables`），不得依赖 `apps/admin`。弹层运行时在 `apps/admin/src/components/popup`（按 `modal` / `drawer` / `alert` 分别引入，不再经过聚合 barrel）。`ui` 不得依赖 `form-ui`、该弹层目录或 `@wyh/shared/global-state`。细则见 [`docs/architecture/frontend-packages.md`](docs/architecture/frontend-packages.md)。
8. **宪法**：改 mission / tech-stack / roadmap 须用户显式确认，禁止作实现副作用修改。

## Where to change what

| 意图 | 位置 |
|------|------|
| 启动 / 打包 | `backend/admin-start`（只装配） |
| Admin 管理能力（身份/组织/授权/租户产品/文件/任务/审计/通知/设置/仪表盘） | `backend/admin-biz-system` 的 `system/admin`（`top.wyhao.system.admin`，一层 COLA；勿再拆 identity/security/department 等完整 BC） |
| 认证 | `backend/admin-auth`（`system.auth`，独立模块） |
| 租户隔离 / 上下文 | `backend/admin-commons/cmn-tenant` |
| 对象存储 | `backend/admin-commons/cmn-storage` |
| Quartz 内核 | `backend/admin-commons/cmn-job` |
| 开放 API | `backend/admin-openapi` |
| 跨模块契约 | `backend/admin-biz-system-client`（仅 `cmn-core` + 注解；`top.wyhao.security.client` / `top.wyhao.identity.client` 等契约包名可保留，实现在 Admin BC） |
| 前端页面 / API | `frontend/apps/admin/src/views`、`frontend/apps/admin/src/api` |
| 文档站 | `frontend/apps/docs`（`@wyh/docs`） |
| 组件演示 | `frontend/apps/design-system`（只演示 `@wyh/ui` / `@wyh/ui-patterns`） |
| UI 原子 / 页面模式 | `frontend/packages/ui`（`@wyh/ui`）、`frontend/packages/ui-patterns`（`@wyh/ui-patterns`）。应用仍可用 `#/ui`、`#/ui-patterns` |

完整表见 [`docs/architecture/repo-map.md`](docs/architecture/repo-map.md)。

## Delivery

细则见 [`.cursor/rules/delivery.mdc`](.cursor/rules/delivery.mdc)。摘要：

- 交付态干净：无「已修正此处」类痕迹；注释只解释非显而易见的 why。
- 向用户说明变更时，与正文分离，用独立 Changelog 要点（仓库变更日志走 `changelog` skill）。
- 默认中文回复；未要求不 commit / 不 push。
- 需求不清先问；重要结论尽量用仓库内多源交叉验证。
- **后端单测**：`domain`/规则与可 Mock 的 app 决策高覆盖；`adapter` 不强制。细则见 [`docs/conventions/backend-java.md`](docs/conventions/backend-java.md#testing)。
