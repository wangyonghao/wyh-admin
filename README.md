# wyh-admin

前后端分离的多租户中后台。后端是 Spring Boot 3 模块化单体，前端是 Vue 3 + shadcn-vue / Vben 原子。技术选型以 [`docs/tech-stack.md`](docs/tech-stack.md) 和依赖声明为准。

仓库：<https://github.com/wangyonghao/wyh-admin>

## 快速开始

### 环境要求

| 环境 | 要求 |
|------|------|
| Java | **17**（必须；本机若默认是 JDK 21/26 会编译或启动失败） |
| Maven | 3.6+ |
| PostgreSQL | 17+ |
| Redis | 6+ |
| Node.js | ^20.19.0 \|\| ^22.18.0 \|\| ^24.0.0 |
| pnpm | >= 10（当前 `packageManager`：10.33.4） |

**推荐验收路径（非 Compose）**：中间件（Postgres + Redis）用本机已有服务或 **Apple Container** 拉起 → 复制 `backend/scripts/env.example.sh` 为 `env.sh` → `JAVA_HOME=17` → 宿主机 `mvn -pl admin-start -am spring-boot:run` → `curl /actuator/health` 为 UP。细则见 [`backend/docker/README.md`](backend/docker/README.md)「宿主机启动（推荐）」与「Apple Container」。

**Docker Compose** 仅作可选全栈 / CI 路径，不是本机验收必选项。旧路径 `backend/scripts/docker/` 已删除；勿再引用。

### 1. 克隆

```bash
git clone https://github.com/wangyonghao/wyh-admin.git
cd wyh-admin
```

### 2. 启动

先确认中间件已监听（默认 Postgres `5432`、Redis `6379`），且 `java -version` 是 17：

```bash
java -version   # 须为 17.x
cd backend && mvn -pl admin-start -am compile
cd backend && mvn -pl admin-start -am spring-boot:run
```

就绪后：`curl -s http://127.0.0.1:8000/actuator/health` 应返回 `"status":"UP"`。数据库名约定为 `postgres`。`application-dev.yml` 已开启 Liquibase，空库会自动建表。

中间件可用 Apple Container 起（见 [`backend/docker/README.md`](backend/docker/README.md)）；也可用已有本机 Postgres/Redis。

### 3. Docker Compose（可选 / CI）

有 Docker 时可用全栈一键（含容器内 API / Vite）。**不是**本机 W2 验收必选项：

```bash
cd backend/docker
cp .env.example .env
# 编辑 .env，替换占位密码与 AES_KEY、WYH_JWT_SECRET
docker compose --env-file .env -f docker-compose.dev.yml up
```

没有 `docker` 时跳过本节，勿标成已绿。

`backend/scripts/env.sh` 已被忽略，不要提交。开发 profile 的 JWT / RSA 写在 `application-dev.yml`。生产配置另要 `WYH_JWT_SECRET`、`WYH_RSA_PUBLIC_KEY`、`WYH_RSA_PRIVATE_KEY`。宿主机上的文件目录是 `application-dev.yml` 的 `file.local.root-path`。

宿主机前端（不要和 compose 里的 `web` 同时对同一份 `frontend/` 执行 `pnpm install`）：

```bash
cd frontend
pnpm install
pnpm dev
```

页面 `http://localhost:5888`，接口见 `frontend/apps/admin/.env.development`（`http://127.0.0.1:8000`）。文档站：`pnpm dev:docs`。组件演示：`pnpm dev:ds`。

### 常用命令

```bash
# 后端关键模块单测（CI 同此范围）
cd backend && mvn -pl platform/job,platform/iam,platform/cmn-crypto -am test

# 前端单测（Vitest，不含 Playwright）
cd frontend && pnpm test:unit
```

## 项目结构

```
wyh-admin/
├── AGENTS.md                      # AI 入口：全局规则 + 文档导航
├── skills/                        # Agent skills 
│
├── docs/                          # 一切「非代码」知识
│   ├── README.md                  # 文档怎么分、怎么维护
│   ├── mission.md                 # 为什么做(愿景、目标用户、非目标)
│   ├── roadmap.md                 # 做什么、什么时候做
│   ├── tech-stack.md              # 用什么技术、版本、选型理由
│   ├── glossary.md                # 领域术语表: 业务名词、缩写、易混概念
│   ├── architecture/              # 模块地图、边界（已有 repo-map.md）
│   ├── conventions/               # 编码规范(后端、前端、命名、Git、测试)
│   ├── adr/                       # 架构决策记录（目录已建，正文待写）
│   ├── product/                   # 产品文档(PRD、用户故事、需求)
│   ├── prototype/                 # 原型：有 Figma / HTML / 截图时再建
│   ├── templates/                 # ADR、PRD、spec、runbook 的模板
│   ├── specs/                     # 进行中的功能规格:每个功能一个子目录,含 spec(要什么)、plan(怎么做)、tasks(拆分步骤)
│   └── runbook/                   # 运维手册(部署、回滚、故障处理)
│
├── backend/                       # 后端代码
│   ├── AGENTS.md                  # 后端特有规则
│   ├── start/                     # 启动模块、项目配置
│   └── ...
├── frontend/                      # 前端代码
│   ├── AGENTS.md                  # 前端特有规则
│   ├── apps/*                     # 可运行应用
│   ├── packages/                  # ui、ui-patterns、core、shell
│   └── ...
├── README.md                      # 本文件：简介、快速启动、目录结构
└── CHANGELOG.md
```

## 参与贡献

1. Fork 后基于 `main` 开分支。
2. 提交说明使用 [Conventional Commits](https://www.conventionalcommits.org/)。
3. 向 `main` 提 Pull Request。改 `backend/**` 会跑后端编译和关键模块单测；改 `frontend/**` 会跑 `pnpm test:unit` 和前端构建。

## License

Apache-2.0。
