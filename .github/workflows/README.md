# GitHub Actions

GitHub 只执行仓库根目录 `.github/workflows/` 下的文件。`frontend/.github/workflows/` 来自上游 Vben 脚手架，**不会**在本仓库运行；其中 `ci.yml` 的 `github.repository == 'vbenjs/vue-vben-admin'` 因此不会挡住 wyh-admin。本仓库的前端单测在 `frontend-build.yml`。

默认分支是 `main`。

## 工作流

| 文件 | 触发 | 做什么 |
|------|------|--------|
| `backend-build.yml` | PR → `main`，且改了 `backend/**` 或该文件 | Java 17 编译整个后端，再跑 `commons/cmn-job`、`cmn-web`、`cmn-db`、`cmn-cache`、`cmn-crypto`、`admin-system-client` 及依赖模块的单测 |
| ~~`backend-scan.yml`~~ | — | **临时移除**：Sonar token 已失效，避免 CI 失败。`backend/pom.xml` 的 `-Psonar` profile 仍保留，token 恢复后可重建 workflow |
| `backend-deploy.yml` | push → `main`（`backend/**`）或手动 | 打包 `backend/start`，推送镜像 `wangyonghao/wyh-admin:latest`，再 SSH 到服务器拉起。Secret：`DOCKERHUB_USERNAME`、`DOCKERHUB_TOKEN`、`SSH_HOST`、`SSH_PORT`、`SSH_USER`、`SSH_KEY` |
| `frontend-build.yml` | PR → `main`，且改了 `frontend/**` 或该文件 | 在 `frontend/` 执行 `pnpm test:unit`（Vitest）和 `pnpm run build`。不跑 Playwright |
| `frontend-deploy.yml` | push → `main`，且改了 `frontend/**` 或该文件 | 构建 `@wyh/admin`（产物 `frontend/apps/admin/dist.zip`）并上传到服务器。Secret：`SSH_HOST`、`SSH_PORT`、`SSH_USER`、`SSH_KEY` |
| `release-tag.yml` | 推送 `v*` 标签 | 仅当 `github.repository == 'wangyonghao/wyh-admin'` 时创建 GitHub Release，正文指向 `main` 上的 `CHANGELOG.md` |

## 路径过滤

后端：

```yaml
paths:
  - 'backend/**'
  - '.github/workflows/backend-build.yml' # 扫描、部署换成对应文件名
```

前端：

```yaml
paths:
  - 'frontend/**'
  - '.github/workflows/frontend-build.yml' # 部署换成 frontend-deploy.yml
```

只改文档、且不碰上述路径时，对应工作流不会跑。

## 注意

- 密钥放在 GitHub Secrets，不要写进工作流。
- `backend-build.yml` 会去掉 `backend/pom.xml` 里的镜像 `<repositories>`，让 CI 走 Maven Central。
- 前端任务在 `ubuntu-latest`，时限 30 分钟，并用 pnpm 缓存。`HUSKY=0`，`TZ=Asia/Shanghai`，安装使用 `--frozen-lockfile`。Vitest 使用与应用相同的 `@vben/*` 别名。
