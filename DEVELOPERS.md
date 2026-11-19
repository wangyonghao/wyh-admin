# 开发 WYH-ADMIN

## 入门

### 安装依赖

- [Git](https://git-scm.com/)
- [NodeJS](https://nodejs.org/)（`frontend/package.json` 的 `engines.node`：`^20.19.0 || ^22.18.0 || ^24.0.0`）
- [pnpm](https://pnpm.io/)（`frontend/package.json` 的 `packageManager`，当前 `pnpm@10.33.4`）
- [Docker](https://docs.docker.com/get-started/get-docker/)（本地起中间件）
- [OpenJDK](https://adoptium.net/temurin/releases?version=17)（后端必须 **JDK 17**）
- [Maven](https://maven.apache.org/)（构建与启动后端）

### 本地开发

1. 克隆项目

```bash
git clone https://github.com/wangyonghao/wyh-admin.git
cd wyh-admin
```

2. 配置环境变量

宿主机跑后端时需要：


3. 运行中间件容器

口令须与上一步环境变量一致。已有同名容器时用 `docker start <name>`，不要重复 `run`。数据落在命名 volume，删容器不会丢数据（`docker volume rm` 才会清）。

```bash
# Postgres
docker volume create wyh-postgres-data
docker run -d --name wyh-postgres \
  -p 5432:5432 \
  -v /etc/localtime:/etc/localtime:ro \
  -v wyh-postgres-data:/var/lib/postgresql/data \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD='postgres@2026' \
  -e POSTGRES_DB=postgres \
  postgres:17.6-alpine

# Redis
docker volume create wyh-redis-data
docker run -d --name wyh-redis \
  -p 6379:6379 \
  -v /etc/localtime:/etc/localtime:ro \
  -v wyh-redis-data:/data \
  redis:8-alpine \
  redis-server --requirepass 'redis@2026' --appendonly yes

# Inbucket（开发用邮件黑洞：SMTP 收信不外发，浏览器查看验证码等测试邮件）
docker volume create wyh-mail-data
docker run -d --name wyh-mail \
  -p 2500:2500 -p 9000:9000 -p 1100:1100 \
  -v /etc/localtime:/etc/localtime:ro \
  -v wyh-mail-data:/storage \
  inbucket/inbucket:3.0.3

# MinIO（可选；宿主机 9000 已被 Inbucket 占用，API 映射到 9002）
docker volume create wyh-minio-data
docker run -d --name wyh-minio \
  -p 9002:9000 -p 9001:9001 \
  -v /etc/localtime:/etc/localtime:ro \
  -v wyh-minio-data:/data \
  -e MINIO_ROOT_USER=wyh-minio \
  -e MINIO_ROOT_PASSWORD='minio@2026' \
  cgr.dev/chainguard/minio \
  server --console-address ":9001" /data

# 创建默认桶（MinIO 就绪后执行一次）
docker run --rm --add-host=host.docker.internal:host-gateway \
  --entrypoint /bin/sh \
  cgr.dev/chainguard/minio-client:latest-dev \
  -ec 'mc alias set local http://host.docker.internal:9002 wyh-minio minio@2026 && mc mb --ignore-existing local/tide'
```

默认端口：Postgres `5432`、Redis `6379`、Inbucket SMTP `2500` / Web `9000`、MinIO API `9002` / Console `9001`。

4. 运行后端工程

确认 `java -version` 为 **17.x**，且中间件已监听：

```bash

# 设置环境变量
export DB_HOST='localhost'
export DB_PORT='5432'
export DB_NAME='postgres'
export DB_USER='postgres'
export DB_PWD='postgres@2026'
export REDIS_HOST='localhost'
export REDIS_PORT='6379'
export REDIS_PWD='redis@2026'
export AES_KEY='0123456789abcdef0123456789abcdef'
export WYH_JWT_SECRET='local-dev-jwt-secret-change-me-please'

# 启动
cd backend
mvn -pl start -am spring-boot:run
```

就绪后：

```bash
curl -s http://127.0.0.1:8000/actuator/health
# 期望含 "status":"UP"
```

API 文档：http://127.0.0.1:8000/doc.html 。
空库会由 Liquibase 自动建表。

5. 运行前端工程

```bash
cd frontend
corepack enable
pnpm install   # 安装依赖
pnpm dev       # 启动产品后台 @wyh/admin
```

页面：http://localhost:5888 。接口见 `frontend/apps/admin/.env.development`（默认 `http://127.0.0.1:8000`）。

其他前端入口：

```bash
pnpm dev:docs  # 文档站
pnpm dev:ds    # 组件演示
pnpm build     # 打包产品后台
```
