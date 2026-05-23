# GitHub Actions 工作流说明

## 概述

本项目使用 GitHub Actions 进行持续集成和部署。工作流文件已从 `backend/` 和 `frontend/` 目录统一移至根目录 `.github/workflows/`，并添加了 `on.push.paths` 过滤规则以精准区分前后端工作流。

## 工作流文件说明

### 后端工作流

#### 1. `build.yml` - 后端构建检查
- **触发条件**：Pull Request 到 `dev` 分支，且修改了 `backend/**` 或工作流文件本身
- **功能**：编译后端代码，检查是否存在编译错误
- **运行环境**：Ubuntu Latest，Java 17

#### 2. `deploy.yml` - 后端部署
- **触发条件**：推送到 `dev` 分支，且修改了 `backend/**` 或工作流文件本身
- **功能**：
  - 编译打包后端代码
  - 上传构建产物到服务器
  - 启动 Docker 容器
- **运行环境**：Ubuntu Latest
- **依赖密钥**：
  - `SSH_HOST` - 服务器地址
  - `SSH_PORT` - SSH 端口
  - `SSH_USER` - SSH 用户名
  - `SSH_KEY` - SSH 私钥

#### 3. `scan.yml` - 代码扫描
- **触发条件**：
  - 推送到 `dev` 分支，且修改了 `backend/**` 或工作流文件本身
  - Pull Request 到 `dev` 分支，且修改了 `backend/**` 或工作流文件本身
- **功能**：使用 SonarCloud 进行代码质量扫描
- **运行环境**：Ubuntu Latest，Java 17
- **依赖密钥**：
  - `SONAR_TOKEN` - SonarCloud Token

#### 4. `release-tag.yml` - 发布版本
- **触发条件**：推送标签，格式为 `v*`（如 `v1.0.0`）
- **功能**：自动创建 GitHub Release
- **运行环境**：Ubuntu Latest

### 前端工作流

#### 1. `frontend-build.yml` - 前端构建检查
- **触发条件**：Pull Request 到 `main` 分支，且修改了 `frontend/**` 或工作流文件本身
- **功能**：构建前端代码，检查是否存在构建错误
- **运行环境**：Ubuntu Latest 和 Windows Latest

#### 2. `frontend-ci.yml` - 前端 CI
- **触发条件**：
  - Pull Request，且修改了 `frontend/**` 或工作流文件本身
  - 推送到 `main` 或 `releases/*` 分支，且修改了 `frontend/**` 或工作流文件本身
- **功能**：
  - 运行单元测试（Vitest）
  - 代码检查（ESLint、Stylelint 等）
  - 类型检查（TypeScript）
- **运行环境**：Ubuntu Latest 和 Windows Latest

#### 3. `frontend-deploy.yml` - 前端部署
- **触发条件**：推送到 `main` 分支，且修改了 `frontend/**` 或工作流文件本身
- **功能**：
  - 构建前端应用（Playground、Docs、Antd、Element、Naive）
  - 上传构建产物到 FTP 服务器
- **运行环境**：Ubuntu Latest
- **依赖密钥**：
  - `PRO_FTP_HOST` - FTP 服务器地址
  - `WEB_PLAYGROUND_FTP_ACCOUNT` - Playground FTP 账户
  - `WEB_PLAYGROUND_FTP_PWSSWORD` - Playground FTP 密码
  - `WEBSITE_FTP_ACCOUNT` - 文档 FTP 账户
  - `WEBSITE_FTP_PASSWORD` - 文档 FTP 密码
  - `WEB_ANTD_FTP_ACCOUNT` - Antd FTP 账户
  - `WEB_ANTD_FTP_PASSWORD` - Antd FTP 密码
  - `WEB_ELE_FTP_ACCOUNT` - Element FTP 账户
  - `WEB_ELE_FTP_PASSWORD` - Element FTP 密码
  - `WEB_NAIVE_FTP_ACCOUNT` - Naive FTP 账户
  - `WEB_NAIVE_FTP_PASSWORD` - Naive FTP 密码

## 路径过滤规则

### 后端工作流路径
```yaml
paths:
  - 'backend/**'
  - '.github/workflows/build.yml'
  - '.github/workflows/deploy.yml'
  - '.github/workflows/scan.yml'
```

### 前端工作流路径
```yaml
paths:
  - 'frontend/**'
  - '.github/workflows/frontend-build.yml'
  - '.github/workflows/frontend-ci.yml'
  - '.github/workflows/frontend-deploy.yml'
```

## 工作流触发流程

### 后端开发流程
1. 创建功能分支，修改 `backend/**` 代码
2. 提交 PR 到 `dev` 分支
3. 自动触发 `build.yml` 和 `scan.yml`
4. PR 审核通过后合并到 `dev`
5. 自动触发 `deploy.yml` 进行部署

### 前端开发流程
1. 创建功能分支，修改 `frontend/**` 代码
2. 提交 PR 到 `main` 分支
3. 自动触发 `frontend-build.yml` 和 `frontend-ci.yml`
4. PR 审核通过后合并到 `main`
5. 自动触发 `frontend-deploy.yml` 进行部署

### 版本发布流程
1. 在 `dev` 分支创建版本标签（如 `v1.0.0`）
2. 推送标签到远程仓库
3. 自动触发 `release-tag.yml` 创建 GitHub Release

## 注意事项

1. **路径过滤的重要性**：
   - 后端工作流只在修改 `backend/**` 时触发，避免不必要的构建
   - 前端工作流只在修改 `frontend/**` 时触发，提高 CI/CD 效率

2. **工作流文件修改**：
   - 修改工作流文件本身也会触发相应的工作流，便于测试工作流变更

3. **密钥管理**：
   - 所有敏感信息（SSH 密钥、FTP 密码等）应存储在 GitHub Secrets 中
   - 不要在工作流文件中硬编码密钥

4. **分支策略**：
   - 后端使用 `dev` 分支作为开发分支
   - 前端使用 `main` 分支作为主分支
   - 版本发布使用标签（`v*`）

## 常见问题

### Q: 为什么我的工作流没有触发？
A: 检查以下几点：
1. 确认修改的文件路径是否匹配 `paths` 规则
2. 确认推送的分支是否正确（后端 `dev`，前端 `main`）
3. 检查工作流文件中的 `if` 条件是否满足

### Q: 如何手动触发工作流？
A: 某些工作流支持 `workflow_dispatch` 手动触发，可在 GitHub Actions 页面手动运行。

### Q: 如何调试工作流？
A: 
1. 查看 GitHub Actions 的详细日志
2. 在本地使用 `act` 工具模拟工作流执行
3. 添加 `debug` 步骤输出调试信息

## 相关文档

- [GitHub Actions 官方文档](https://docs.github.com/en/actions)
- [工作流语法参考](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [路径过滤文档](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#onpushpull_requestpaths)
