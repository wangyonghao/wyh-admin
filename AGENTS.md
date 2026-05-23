# WYH Admin 项目 - AI Agent指南

## 项目目录结构说明

> 本文档为 AI Agent 提供项目目录结构的详细说明，帮助快速理解项目组织方式。

### 项目整体结构

```
项目根目录/
├── backend/          # 后端 Java 项目（Spring Boot 3 + Java 17）
├── frontend/         # 前端 Vue 3 项目（Monorepo 架构）
├── docs/             # 文档项目（VitePress）
├── AGENTS.md         # AI Agent 上下文文档
└── README.md         # 项目说明文档
```

---

### 后端目录结构 (backend/)

#### 根目录文件

```
backend/
├── pom.xml           # Maven 根配置文件（版本管理、依赖管理）
├── lombok.config     # Lombok 全局配置
├── .gitignore        # Git 忽略配置
└── LICENSE           # 开源协议文件
```

#### 主要模块

##### 1. 服务模块 (svr-*)
```
backend/svr-admin/    # 主服务模块（打包部署）
├── src/main/
│   ├── java/top/wyhao/admin/
│   │   ├── config/           # 配置类
│   │   ├── controller/       # 通用 API
│   │   ├── job/              # 定时任务
│   │   └── AdminApplication.java  # 启动类
│   └── resources/
│       ├── config/           # 配置文件目录
│       │   ├── application.yml
│       │   ├── application-dev.yml
│       │   └── application-prod.yml
│       ├── db/changelog/     # Liquibase 数据库脚本
│       ├── templates/        # 模板文件（邮件等）
│       └── logback-spring.xml
└── pom.xml

backend/svr-job/      # 任务调度服务模块
└── src/main/
    ├── java/top/wyhao/job/
    │   └── JobServerApplication.java
    └── resources/
```

##### 2. 业务模块 (biz/)
```
backend/biz/
├── biz-system/       # 系统管理核心模块
│   └── src/main/java/top/wyhao/system/
│       ├── auth/             # 认证相关
│       │   ├── controller/   # 登录、登出等 API
│       │   ├── service/      # 认证业务逻辑
│       │   ├── model/        # 认证相关模型
│       │   └── config/       # 认证配置
│       └── system/           # 系统管理
│           ├── controller/   # 用户、角色、菜单等 API
│           ├── service/      # 业务逻辑
│           ├── mapper/       # MyBatis Mapper
│           ├── model/        # 数据模型
│           │   ├── entity/   # 实体类
│           │   ├── query/    # 查询条件
│           │   ├── req/      # 请求参数
│           │   └── resp/     # 响应参数
│           ├── enums/        # 枚举
│           ├── constant/     # 常量
│           └── config/       # 配置
│
├── biz-coding/       # 代码生成器插件
│   └── src/main/
│       ├── java/top/wyhao/generator/
│       └── resources/templates/  # 代码生成模板
│           ├── backend/          # 后端模板
│           └── frontend/         # 前端模板
│
├── biz-openapi/      # 能力开放插件（第三方应用接入）
│   └── src/main/java/top/wyhao/open/
│       ├── controller/       # 应用管理 API
│       ├── service/          # 业务逻辑
│       ├── model/            # 数据模型
│       └── sign/             # API 签名算法
│
├── biz-tenant/       # 多租户插件
│   └── src/main/java/top/wyhao/tenant/
│       ├── api/              # 租户公共 API
│       ├── controller/       # 租户管理 API
│       ├── service/          # 业务逻辑
│       └── model/            # 数据模型
│
├── biz-job/          # 任务调度插件
│   └── src/main/java/top/wyhao/schedule/
│       ├── controller/       # 任务管理 API
│       ├── service/          # 业务逻辑
│       ├── api/              # Feign API
│       └── model/            # 数据模型
│
└── pom.xml           # 业务模块父 POM
```

##### 3. 公共模块 (cmn/)
```
backend/cmn/
├── biz-base/         # 业务基础模块（所有业务模块的通用能力）
│   └── src/main/java/top/wyhao/common/
│       ├── api/              # 公共业务 API
│       ├── base/             # 基类
│       │   ├── controller/   # Controller 基类
│       │   ├── service/      # Service 基类
│       │   ├── mapper/       # Mapper 基类
│       │   └── model/        # 模型基类
│       ├── model/            # 公共模型
│       ├── context/          # 上下文
│       ├── enums/            # 公共枚举
│       ├── constant/         # 公共常量
│       ├── util/             # 工具类
│       └── config/           # 公共配置
│           ├── crud/         # CRUD 配置
│           ├── mybatis/      # MyBatis 配置
│           ├── websocket/    # WebSocket 配置
│           ├── doc/          # 接口文档配置
│           └── exception/    # 全局异常处理
│
├── cmn-base/         # Tide Starter 核心模块
├── cmn-web/          # Web 模块（跨域、异常处理等）
├── cmn-json/         # JSON 处理模块（Jackson）
├── cmn-apidoc/       # API 文档模块（NextDoc4j）
│
├── cmn-security/     # 认证模块（SaToken）
├── cmn-justauth/     # 第三方登录模块（JustAuth）
│
├── cmn-mybatis-plus/ # MyBatis Plus 集成
├── cmn-redis/        # Redisson 集成
├── cmn-springcache/  # Spring Cache 集成
├── cmn-jetcache/     # JetCache 集成
│
├── cmn-captcha-graphic/    # 图形验证码
├── cmn-captcha-behavior/   # 行为验证码（滑动拼图）
│
├── cmn-encrypt-core/       # 加密核心模块
├── cmn-encrypt-field/      # 字段加密
├── cmn-encrypt-api/        # API 加密
│
├── cmn-fastexcel/    # FastExcel 集成
├── cmn-poi/          # Apache POI 集成
├── cmn-storage/      # 文件存储（本地、S3 等）
│
├── cmn-trace/        # 链路追踪（TLog）
├── cmn-email/        # 邮件发送
├── cmn-websocket/    # WebSocket 支持
├── cmn-tenant/       # 多租户支持
├── cmn-license/      # License 管理
│   ├── cmn-generator/      # License 生成器
│   ├── cmn-license-core/   # License 核心
│   └── cmn-license-verifier/  # License 校验器
│
└── pom.xml           # 公共模块父 POM
```

##### 4. 脚本和配置
```
backend/scripts/
├── docker/           # Docker 部署配置
│   ├── docker-compose.yml   # Docker Compose 配置
│   ├── apiserver/           # API 服务配置
│   ├── jobserver/           # 任务服务配置
│   ├── nginx/               # Nginx 配置
│   └── redis/               # Redis 配置
├── package-and-build-images.sh  # 打包构建脚本
└── tag-and-push-images.sh       # 镜像推送脚本

backend/.github/      # GitHub 配置
├── workflows/        # GitHub Actions 工作流
│   ├── build.yml     # 构建工作流
│   ├── deploy.yml    # 部署工作流
│   ├── release-tag.yml  # 发布工作流
│   └── scan.yml      # 代码扫描工作流
└── ISSUE_TEMPLATE/   # Issue 模板
```

---

### 前端目录结构 (frontend/)

#### 根目录配置文件
```
frontend/
├── package.json      # 根 package.json（Monorepo 配置）
├── pnpm-workspace.yaml  # pnpm workspace 配置
├── turbo.json        # Turbo 构建配置
├── tsconfig.json     # TypeScript 配置
├── vite.config.ts    # Vite 配置
├── vitest.config.ts  # Vitest 测试配置
│
├── eslint.config.mjs # ESLint 配置
├── stylelint.config.mjs  # Stylelint 配置
├── oxfmt.config.ts   # oxfmt 格式化配置
├── oxlint.config.ts  # oxlint 配置
├── lefthook.yml      # Git hooks 配置
├── .commitlintrc.js  # Commit 规范配置
│
├── .npmrc            # npm 配置
├── .node-version     # Node 版本要求
├── .browserslistrc   # 浏览器兼容配置
├── .editorconfig     # 编辑器配置
└── README.md         # 前端说明文档
```

#### 应用目录 (apps/)
```
frontend/apps/
└── web-naive/        # Naive UI 版本的主应用
    ├── src/
    │   ├── api/              # API 接口定义
    │   │   └── modules/      # 按模块组织的 API
    │   ├── views/            # 页面视图
    │   │   ├── _core/        # 核心页面（登录、个人中心等）
    │   │   ├── dashboard/    # 仪表盘
    │   │   ├── system/       # 系统管理
    │   │   ├── monitor/      # 系统监控
    │   │   ├── schedule/     # 任务调度
    │   │   ├── tenant/       # 租户管理
    │   │   ├── open/         # 能力开放
    │   │   ├── code/         # 代码生成器
    │   │   ├── user/         # 用户中心
    │   │   └── demos/        # 示例页面
    │   ├── router/           # 路由配置
    │   ├── store/            # 状态管理（Pinia）
    │   ├── components/       # 组件
    │   ├── layouts/          # 布局
    │   ├── locales/          # 国际化
    │   ├── utils/            # 工具函数
    │   ├── types/            # 类型定义
    │   ├── adapter/          # 适配器
    │   ├── constants/        # 常量
    │   ├── hooks/            # Hooks
    │   ├── app.vue           # 根组件
    │   ├── main.ts           # 入口文件
    │   ├── bootstrap.ts      # 启动配置
    │   └── preferences.ts    # 偏好设置
    ├── public/               # 静态资源
    │   └── favicon.ico
    ├── .env                  # 环境变量
    ├── .env.development      # 开发环境变量
    ├── .env.production       # 生产环境变量
    ├── .env.analyze          # 分析环境变量
    ├── index.html            # HTML 入口
    ├── vite.config.ts        # Vite 配置
    ├── tsconfig.json         # TypeScript 配置
    └── package.json          # 应用 package.json
```

#### 共享包目录 (packages/)
```
frontend/packages/
├── @core/            # 核心包
│   ├── base/                 # 基础功能
│   │   ├── design/           # 设计系统
│   │   ├── icons/            # 图标
│   │   ├── shared/           # 共享工具
│   │   └── typings/          # 类型定义
│   ├── ui-kit/               # UI 组件套件
│   │   ├── form-ui/          # 表单组件
│   │   ├── menu-ui/          # 菜单组件
│   │   ├── tabs-ui/          # 标签页组件
│   │   └── shadcn-ui/        # Shadcn UI 组件
│   ├── composables/          # 组合式函数
│   ├── preferences/          # 偏好设置
│   └── forward/              # 转发组件
│
├── effects/          # 副作用相关
│   ├── access/               # 权限控制
│   ├── common-ui/            # 通用 UI
│   ├── hooks/                # Hooks
│   ├── layouts/              # 布局
│   ├── plugins/              # 插件
│   └── request/              # 请求封装
│
├── constants/        # 常量定义
├── icons/            # 图标库
├── locales/          # 国际化资源
├── preferences/      # 偏好设置
├── stores/           # 共享状态（Pinia）
├── styles/           # 样式文件
├── types/            # 类型定义
└── utils/            # 工具函数
```

#### 内部工具 (internal/)
```
frontend/internal/
├── lint-configs/     # 代码检查配置
│   ├── commitlint-config/    # Commitlint 配置
│   ├── eslint-config/        # ESLint 配置
│   ├── oxfmt-config/         # oxfmt 配置
│   ├── oxlint-config/        # oxlint 配置
│   └── stylelint-config/     # Stylelint 配置
│
├── node-utils/       # Node 工具
├── tailwind-config/  # Tailwind 配置
├── tsconfig/         # TypeScript 配置
│   ├── base.json             # 基础配置
│   ├── library.json          # 库配置
│   ├── node.json             # Node 配置
│   ├── web.json              # Web 配置
│   └── web-app.json          # Web 应用配置
│
└── vite-config/      # Vite 配置
```

#### 脚本工具 (scripts/)
```
frontend/scripts/
├── deploy/           # 部署脚本
│   ├── Dockerfile            # Docker 镜像配置
│   ├── nginx.conf            # Nginx 配置
│   └── build-local-docker-image.sh
│
├── turbo-run/        # Turbo 运行脚本
├── vsh/              # Shell 脚本工具
└── clean.mjs         # 清理脚本
```

#### GitHub 配置
```
frontend/.github/
├── workflows/        # GitHub Actions 工作流
│   ├── build.yml             # 构建工作流
│   ├── ci.yml                # CI 工作流
│   ├── deploy.yml            # 部署工作流
│   ├── codeql.yml            # 代码扫描
│   ├── changeset-version.yml # 版本管理
│   └── ...
├── ISSUE_TEMPLATE/   # Issue 模板
│   ├── bug-report.yml
│   ├── feature-request.yml
│   └── docs.yml
└── actions/          # 自定义 Actions
    └── setup-node/
```

---

### 文档目录 (docs/)

```
docs/
├── src/              # 文档源文件
│   ├── guide/                # 指南
│   ├── api/                  # API 文档
│   └── ...
├── .vitepress/       # VitePress 配置
│   ├── config.ts             # 配置文件
│   └── theme/                # 主题配置
├── package.json      # 文档项目配置
└── tsconfig.json     # TypeScript 配置
```

---

### 关键目录说明

#### 后端关键目录

1. **svr-admin/src/main/resources/config/**
   - 存放所有环境的配置文件
   - 开发时主要修改 `application-dev.yml`

2. **biz-system/src/main/java/top/wyhao/system/**
   - 系统管理核心业务代码
   - 包含用户、角色、菜单、部门等管理功能

3. **cmn/biz-base/src/main/java/top/wyhao/common/**
   - 所有业务模块的基类和公共工具
   - CRUD 套件的核心实现

4. **biz-coding/src/main/resources/templates/**
   - 代码生成器模板
   - 修改模板可自定义生成代码风格

#### 前端关键目录

1. **apps/web-naive/src/views/**
   - 所有页面组件
   - 按业务模块组织（system、monitor、schedule 等）

2. **apps/web-naive/src/api/**
   - API 接口定义
   - 与后端 API 一一对应

3. **packages/@core/**
   - 核心共享包
   - 包含基础组件、UI 套件、工具函数等

4. **packages/effects/**
   - 副作用相关功能
   - 权限控制、请求封装、布局等

5. **internal/lint-configs/**
   - 代码规范配置
   - ESLint、Stylelint、Commitlint 等

---

### 模块依赖关系

#### 后端模块依赖层次
```
svr-admin (主服务)
    ↓ 依赖
biz-* (业务模块)
    ↓ 依赖
biz-base (业务基础)
    ↓ 依赖
cmn-* (公共模块)
```

#### 前端包依赖层次
```
apps/web-naive (应用)
    ↓ 依赖
packages/effects (副作用)
    ↓ 依赖
packages/@core (核心包)
    ↓ 依赖
packages/utils, packages/types (工具和类型)
```

---

#### 开发时常用目录

##### 后端开发
- **新增业务功能**: `backend/biz/biz-system/src/main/java/top/wyhao/system/`
- **修改配置**: `backend/svr-admin/src/main/resources/config/`
- **数据库脚本**: `backend/svr-admin/src/main/resources/db/changelog/postgresql/`
- **代码生成模板**: `backend/biz/biz-coding/src/main/resources/templates/`

##### 前端开发
- **新增页面**: `frontend/apps/web-naive/src/views/`
- **新增 API**: `frontend/apps/web-naive/src/api/modules/`
- **修改路由**: `frontend/apps/web-naive/src/router/`
- **修改状态**: `frontend/apps/web-naive/src/store/`
- **环境配置**: `frontend/apps/web-naive/.env.development`

---

### 注意事项

#### 后端
1. 不要破坏模块依赖层次（svr → biz → cmn）
2. 新增依赖必须在根 pom.xml 的 `<dependencyManagement>` 中声明
4. 业务功能应放在对应的 `biz-*` 模块

#### 前端
1. 不要直接修改 `packages/@core` 核心包
2. 新增依赖应在 `pnpm-workspace.yaml` 的 `catalog` 中声明
3. 共享组件应放在 `packages/` 目录
4. 应用特定代码应放在 `apps/web-naive/` 目录

---

### 快速定位文件

#### 后端
- **启动类**: `backend/svr-admin/src/main/java/top/wyhao/admin/AdminApplication.java`
- **配置文件**: `backend/svr-admin/src/main/resources/config/application-dev.yml`
- **用户管理**: `backend/biz/biz-system/src/main/java/top/wyhao/system/user/`
- **角色管理**: `backend/biz/biz-system/src/main/java/top/wyhao/system/role/`
- **菜单管理**: `backend/biz/biz-system/src/main/java/top/wyhao/system/menu/`
- **文件管理**: `backend/biz/biz-system/src/main/java/top/wyhao/system/file/`

#### 前端
- **入口文件**: `frontend/apps/web-naive/src/main.ts`
- **路由配置**: `frontend/apps/web-naive/src/router/`
- **登录页面**: `frontend/apps/web-naive/src/views/_core/authentication/`
- **用户管理页**: `frontend/apps/web-naive/src/views/system/user/`
- **API 配置**: `frontend/apps/web-naive/src/api/`

---

## 开发规范和约定

### 后端开发规范

#### 代码规范

命名规范

- **类名**：大驼峰（PascalCase），如 `UserController`、`UserService`
- **方法名**：小驼峰（camelCase），如 `getUserById`、`saveUser`
- **常量**：全大写下划线分隔，如 `MAX_SIZE`、`DEFAULT_PAGE_SIZE`
- **包名**：全小写，如 `top.wyhao.system.user`

包结构规范

```
top.wyhao.system.user/
├── controller/       # 控制器层（API 接口）
├── service/          # 业务逻辑层
│   └── impl/         # 业务逻辑实现
├── mapper/           # 数据访问层（MyBatis Mapper）
├── entity/           # 实体类（对应数据库表）
├── model/            # 数据模型(API 查询、请求、响应 Schema)
│   ├── query/        # 查询条件类
│   ├── request/      # 请求参数类
│   └── result/       # 响应参数类
├── enums/            # 枚举类
├── constant/         # 常量类
└── config/           # 配置类
```

注解使用规范

- **Controller 层**：使用 `@RestController`、
- **Service 层**：使用 `@Service`
- **Mapper 层**：使用 `@Mapper`
- **实体类**：使用 `@Data`（Lombok）、`@TableName`（MyBatis Plus）
- **API 文档**：使用 `@Tag`、`@Operation`（Swagger）

controller层规范：

* 在方法上定义完整api路径，而不在Controller类上使用统一的`@RequestMapping`
* Controller类负责将Service返回的实体类转换为接口需要的结构（XxxResult）。

service层能用方法命名示例:

- detail 获取详情
- list 查询列表
- page 分页查询
- create 创建
- update 更新
- delete 删除
- export 导出
- import 导入

#### API 设计规范

##### RESTful API 规范

- **GET**：查询资源，如 `GET /api/users/{id}`
- **POST**：创建资源，如 `POST /api/users`
- **PUT**：更新资源（全量），如 `PUT /api/users/{id}`
- **PATCH**：更新资源（部分），如 `PATCH /api/users/{id}`
- **DELETE**：删除资源，如 `DELETE /api/users/{id}`

##### API 响应格式
正常响应的API不需要包装，仅错误响应时使用下述统一结构
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {},
  "timestamp": 1234567890
}
```

##### 分页响应格式
```json
{
    "list": [],
    "total": 100,
  }
}
```

#### 数据库规范

##### 表命名规范
- 使用小写字母和下划线，如 `sys_user`、`sys_role`
- 表名使用复数形式（可选）
- 系统表使用 `sys_` 前缀

##### 字段命名规范
- 使用小写字母和下划线，如 `user_name`、`create_time`
- 主键统一使用 `id`
- 创建时间：`create_time`
- 更新时间：`update_time`
- 创建人：`create_user`
- 更新人：`update_user`
- 逻辑删除：`deleted`（0-未删除，1-已删除）

##### 索引规范
- 主键索引：`pk_表名`
- 唯一索引：`uk_表名_字段名`
- 普通索引：`idx_表名_字段名`

#### 异常处理规范

##### 自定义异常
- 业务异常：`BusinessException`
- 参数异常：`ParamException`
- 认证异常：`AuthException`

##### 全局异常处理
- 使用 `@RestControllerAdvice` 统一处理异常
- 返回统一的错误响应格式

#### 日志规范

##### 日志级别
- **ERROR**：错误信息，需要立即处理
- **WARN**：警告信息，可能存在问题
- **INFO**：重要信息，如业务流程关键节点
- **DEBUG**：调试信息，开发时使用

##### 日志内容
- 记录关键业务操作（登录、修改、删除等）
- 记录异常信息（包含堆栈信息）
- 记录性能信息（慢查询、慢接口等）

#### 事务管理规范

##### 事务注解
- 使用 `@Transactional` 注解
- 只在 Service 层使用事务
- 指定事务传播行为和隔离级别

##### 事务粒度
- 事务粒度尽量小
- 避免在事务中调用外部接口
- 避免在事务中执行耗时操作

---

### 前端开发规范

#### 代码规范

##### 命名规范
- **组件名**：大驼峰（PascalCase），如 `UserList.vue`、`UserForm.vue`
- **文件名**：kebab-case，如 `user-list.vue`、`user-form.vue`
- **变量名**：小驼峰（camelCase），如 `userName`、`userList`
- **常量名**：全大写下划线分隔，如 `MAX_SIZE`、`API_BASE_URL`
- **CSS 类名**：kebab-case，如 `.user-list`、`.user-form`

##### 目录结构规范
```
src/
├── api/              # API 接口定义
│   └── modules/      # 按模块组织
├── views/            # 页面视图
│   ├── _core/        # 核心页面（登录、个人中心等）
│   └── system/       # 系统管理页面
├── components/       # 组件
│   ├── common/       # 通用组件
│   └── business/     # 业务组件
├── router/           # 路由配置
├── store/            # 状态管理
├── utils/            # 工具函数
├── types/            # 类型定义
├── constants/        # 常量定义
├── hooks/            # 组合式函数
└── styles/           # 样式文件
```

#### Vue 3 规范

##### 组合式 API（Composition API）
- 优先使用 Composition API
- 使用 `<script setup>` 语法糖
- 使用 `ref` 和 `reactive` 管理响应式数据

##### 组件规范
```vue
<script setup lang="ts">
// 1. 导入依赖
import { ref, computed, onMounted } from 'vue'

// 2. 定义 Props
interface Props {
  userId: string
}
const props = defineProps<Props>()

// 3. 定义 Emits
interface Emits {
  (e: 'update', value: string): void
}
const emit = defineEmits<Emits>()

// 4. 定义响应式数据
const userName = ref('')

// 5. 定义计算属性
const displayName = computed(() => userName.value.toUpperCase())

// 6. 定义方法
const handleUpdate = () => {
  emit('update', userName.value)
}

// 7. 生命周期钩子
onMounted(() => {
  // 初始化逻辑
})
</script>

<template>
  <div class="user-info">
    <p>{{ displayName }}</p>
    <button @click="handleUpdate">更新</button>
  </div>
</template>

<style scoped lang="scss">
.user-info {
  padding: 16px;
}
</style>
```

#### TypeScript 规范

##### 类型定义
- 优先使用 `interface` 定义对象类型
- 使用 `type` 定义联合类型、交叉类型等
- 避免使用 `any`，使用 `unknown` 替代

##### 类型文件组织
```typescript
// types/user.ts
export interface User {
  id: string
  name: string
  email: string
}

export interface UserQuery {
  name?: string
  page: number
  size: number
}

export interface UserListResponse {
  records: User[]
  total: number
}
```

#### API 调用规范

##### API 文件组织
```typescript
// api/modules/user.ts
import { request } from '@/utils/request'
import type { User, UserQuery, UserListResponse } from '@/types/user'

export const userApi = {
  // 获取用户列表
  getList: (params: UserQuery) => {
    return request.get<UserListResponse>('/api/users', { params })
  },
  
  // 获取用户详情
  getById: (id: string) => {
    return request.get<User>(`/api/users/${id}`)
  },
  
  // 创建用户
  create: (data: Omit<User, 'id'>) => {
    return request.post<User>('/api/users', data)
  },
  
  // 更新用户
  update: (id: string, data: Partial<User>) => {
    return request.put<User>(`/api/users/${id}`, data)
  },
  
  // 删除用户
  delete: (id: string) => {
    return request.delete(`/api/users/${id}`)
  }
}
```

#### 路由规范

##### 路由配置
```typescript
// router/modules/system.ts
import type { RouteRecordRaw } from 'vue-router'

export const systemRoutes: RouteRecordRaw[] = [
  {
    path: '/system',
    name: 'System',
    component: () => import('@/layouts/default.vue'),
    meta: {
      title: '系统管理',
      icon: 'system'
    },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'user'
        }
      }
    ]
  }
]
```

#### 状态管理规范

##### Pinia Store
```typescript
// store/modules/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  // State
  const userInfo = ref<User | null>(null)
  const token = ref<string>('')
  
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  
  // Actions
  const setUserInfo = (user: User) => {
    userInfo.value = user
  }
  
  const setToken = (newToken: string) => {
    token.value = newToken
  }
  
  const logout = () => {
    userInfo.value = null
    token.value = ''
  }
  
  return {
    userInfo,
    token,
    isLoggedIn,
    setUserInfo,
    setToken,
    logout
  }
}, {
  persist: true // 持久化
})
```

#### 样式规范

##### Tailwind CSS 使用
- 优先使用 Tailwind 原子类
- 复杂样式使用 `@apply` 组合
- 自定义样式使用 `<style scoped>`

##### 样式组织
```vue
<template>
  <div class="user-card">
    <h3 class="text-lg font-bold">{{ user.name }}</h3>
    <p class="text-sm text-gray-600">{{ user.email }}</p>
  </div>
</template>

<style scoped lang="scss">
.user-card {
  @apply p-4 rounded-lg shadow-md;
  
  &:hover {
    @apply shadow-lg;
  }
}
</style>
```

---

### Git 规范

#### 分支管理

##### 分支命名
- **主分支**：`main` 或 `master`
- **开发分支**：`develop`
- **功能分支**：`feature/功能名称`，如 `feature/user-management`
- **修复分支**：`fix/问题描述`，如 `fix/login-error`
- **发布分支**：`release/版本号`，如 `release/1.0.0`

#### 提交规范

##### Commit Message 格式
```
<type>(<scope>): <subject>

<body>

<footer>
```

##### Type 类型
- **feat**：新功能
- **fix**：修复 Bug
- **docs**：文档更新
- **style**：代码格式调整（不影响功能）
- **refactor**：重构（不是新功能，也不是修复 Bug）
- **perf**：性能优化
- **test**：测试相关
- **chore**：构建过程或辅助工具的变动

##### 示例
```
feat(user): 添加用户管理功能

- 添加用户列表页面
- 添加用户新增/编辑表单
- 添加用户删除功能

Closes #123
```

---

### 代码审查规范

#### 审查要点

##### 后端
- 代码是否符合规范
- 是否有潜在的性能问题
- 是否有安全漏洞
- 异常处理是否完善
- 日志记录是否合理
- 单元测试是否完善

##### 前端
- 代码是否符合规范
- 组件是否可复用
- 是否有性能问题（如不必要的重渲染）
- 类型定义是否完善
- 是否有无障碍问题
- 是否有浏览器兼容性问题

---

### 测试规范

#### 后端测试

##### 单元测试
- 使用 JUnit 5
- 测试覆盖率 > 80%
- 测试类命名：`XxxTest`
- 测试方法命名：`testXxx` 或 `shouldXxxWhenYyy`

##### 集成测试
- 使用 Spring Boot Test
- 测试关键业务流程
- 测试数据库操作

#### 前端测试

##### 单元测试
- 使用 Vitest
- 测试工具函数
- 测试组合式函数（Composables）

##### 组件测试
- 使用 @vue/test-utils
- 测试组件渲染
- 测试用户交互

##### E2E 测试
- 使用 Playwright
- 测试关键业务流程
- 测试跨浏览器兼容性

---

### 性能优化规范

#### 后端优化
- 使用缓存减少数据库查询
- 使用分页避免一次性加载大量数据
- 使用异步处理耗时操作
- 优化 SQL 查询（避免 N+1 问题）
- 使用连接池管理数据库连接

#### 前端优化
- 使用懒加载（路由、组件、图片）
- 使用虚拟滚动处理大列表
- 使用防抖和节流优化事件处理
- 使用 Web Worker 处理计算密集型任务
- 优化打包体积（Tree Shaking、代码分割）

---

### 安全规范

#### 后端安全
- 使用参数化查询防止 SQL 注入
- 使用 HTTPS 传输敏感数据
- 使用 JWT 或 Session 管理用户认证
- 使用 RBAC 实现权限控制
- 敏感数据加密存储
- 接口限流防止恶意攻击

#### 前端安全
- 防止 XSS 攻击（使用 Vue 的自动转义）
- 防止 CSRF 攻击（使用 Token）
- 敏感数据不存储在 LocalStorage
- 使用 HTTPS
- 验证用户输入

---

### 文档规范

#### 代码注释
- 类和方法必须有注释
- 复杂逻辑必须有注释
- 使用 JavaDoc（后端）或 JSDoc（前端）格式

#### API 文档
- 使用 Swagger 注解生成 API 文档
- 包含请求参数、响应格式、错误码说明

#### README 文档
- 项目介绍
- 技术栈
- 快速开始
- 目录结构
- 开发指南
- 部署指南

## 技术栈


### 后端技术栈

#### 核心框架
- **Spring Boot 3.4.13** - 应用框架
- **Spring Cloud 2024.0.2** - 微服务支持
- **Java 17** - 编程语言

#### 数据访问
- **MyBatis Plus 3.5.14** - ORM 框架，提供 CRUD 增强
- **Redisson 3.52.0** - Redis 客户端
- **JetCache 2.7.8** - 缓存框架（支持两级缓存）
- **P6Spy 3.9.1** - SQL 性能分析

#### 认证授权
- **Sa-Token 1.44.0** - 权限认证框架
- **JustAuth 1.16.7** - 第三方登录集成

#### 任务调度
- **SnailJob 1.8.0** - 分布式任务调度平台

#### 验证码
- **AJ-Captcha 1.4.0** - 行为验证码（滑动拼图）
- **Easy Captcha 1.6.2** - 图形验证码

#### 文件处理
- **FastExcel 1.3.0** - Excel 处理（高性能）
- **Apache POI 5.4.1** - Office 文档处理
- **X-File-Storage 2.2.1** - 文件存储（支持本地、S3 等）
- **Thumbnailator 0.4.21** - 图片缩略图

#### API 文档
- **NextDoc4j 1.1.7** - 现代化 API 文档 UI（替代 Swagger UI）
- **Swagger Annotations 2.2.36** - API 注解

#### 工具库
- **Hutool 5.8.41** - Java 工具类库
- **Lombok** - 简化 JavaBean 编写
- **CosId 2.13.3** - 分布式 ID 生成器
- **TLog 1.5.2** - 分布式日志追踪
- **Ip2region 3.4.7** - IP 地址定位
- **OkHttp 4.12.0** - HTTP 客户端

#### 数据库
- **Liquibase** - 数据库版本管理
- **Postgresql** - 关系型数据库
- **Redis** - 缓存数据库

#### 构建工具
- **Maven 3.x** - 项目构建管理
- **flatten-maven-plugin** - 版本号管理

---

### 前端技术栈

#### 核心框架
- **Vue 3.5.32** - 渐进式 JavaScript 框架
- **TypeScript 6.0.2** - 类型安全的 JavaScript 超集
- **Vite 8.0.8** - 下一代前端构建工具

#### UI 框架
- **Naive UI 2.44.1** - Vue 3 组件库（主应用使用）

#### 路由和状态管理
- **Vue Router 5.0.4** - 官方路由管理器
- **Pinia 3.0.4** - 官方状态管理库
- **pinia-plugin-persistedstate 4.7.1** - Pinia 持久化插件

#### 工具库
- **VueUse 14.2.1** - Vue 组合式 API 工具集
- **Axios 1.15.0** - HTTP 客户端
- **Day.js 1.11.20** - 日期处理库

#### 表单和验证
- **Zod 3.25.76** - TypeScript 优先的模式验证

#### 图表和可视化
- **ECharts 6.0.0** - 数据可视化图表库

#### 表格
- **VxeTable 4.18.11** - Vue 表格组件
- **VxePC UI 4.13.21** - VxeTable 配套 UI

#### 富文本编辑器
- **TipTap 3.22.3** - 无头富文本编辑器

#### 样式和 CSS
- **Tailwind CSS 4.2.2** - 原子化 CSS 框架
- **tailwind-merge 3.5.0** - Tailwind 类名合并
- **class-variance-authority 0.7.1** - CSS 变体管理

#### 图标
- **Iconify** - 统一的图标框架

#### 国际化
- **Vue I18n 11.3.2** - 国际化插件

#### 代码质量
- **ESLint 10.2.0** - JavaScript 代码检查
- **Stylelint 17.6.0** - CSS 代码检查
- **oxfmt 0.44.0** - 代码格式化工具
- **oxlint 1.60.0** - 快速 Linter
- **Commitlint** - Git 提交信息规范检查
- **Lefthook 2.1.5** - Git hooks 管理

#### 构建和部署
- **pnpm 10.33.0** - 快速、节省磁盘空间的包管理器
- **Turbo 2.9.6** - 高性能构建系统（Monorepo）
- **Vite Plugin PWA** - PWA 支持
- **Vite Plugin Compression** - 构建压缩

#### 测试
- **Vitest 4.1.4** - 单元测试框架
- **Playwright 1.59.1** - E2E 测试框架
- **@vue/test-utils 2.4.6** - Vue 组件测试工具

#### 开发工具
- **Vue DevTools** - Vue 开发者工具
- **Vite Plugin Vue DevTools 8.1.1** - Vite 集成的 Vue DevTools

#### 其他功能库
- **@tanstack/vue-query 5.97.0** - 数据获取和缓存
- **nprogress 0.2.0** - 页面加载进度条
- **qrcode 1.5.4** - 二维码生成
- **sortablejs 1.15.7** - 拖拽排序
- **watermark-js-plus 1.6.3** - 水印
- **secure-ls 2.0.0** - 本地存储加密
- **json-bigint 1.0.0** - 大整数 JSON 处理

---

### 架构特点

#### 后端架构
1. **模块化设计** - Maven 多模块架构，按功能拆分
2. **依赖管理** - 使用 BOM 统一版本管理
3. **多级缓存** - Spring Cache + Redis 两级缓存
4. **插件化** - 业务功能以插件形式组织（租户、任务调度、代码生成等）

#### 前端架构
1. **Monorepo** - pnpm workspace + Turbo 管理多包
2. **组件化** - 共享组件库 + 应用层分离
3. **类型安全** - 全面使用 TypeScript
4. **原子化 CSS** - Tailwind CSS 提高开发效率
5. **多 UI 框架支持** - 支持 Naive UI、Ant Design、Element Plus 等

---

### 版本要求

#### 后端
- Java 17+
- Maven 3.6+
- Postgres 17+
- Redis 6.0+

#### 前端
- Node.js 20.19.0+ / 22.18.0+ / 24.0.0+
- pnpm 10.0.0+

---

### 关键技术决策

#### 为什么选择 MyBatis Plus？
- 提供强大的 CRUD 增强，减少样板代码
- 支持 Lambda 表达式查询，类型安全
- 内置分页、性能分析等插件

#### 为什么选择 Sa-Token？
- 轻量级，学习成本低
- 功能完善（登录认证、权限验证、单点登录等）
- 与 Spring Boot 集成简单

#### 为什么选择 Naive UI？
- 完整的 TypeScript 支持
- 组件丰富，设计现代
- 性能优秀，体积小

#### 为什么选择 pnpm + Turbo？
- pnpm 节省磁盘空间，安装速度快
- Turbo 提供增量构建，大幅提升构建速度
- 适合 Monorepo 架构

#### 为什么选择 Vite？
- 开发服务器启动快
- HMR（热模块替换）速度快
- 原生 ESM 支持
- 构建产物优化好

---

### 总结

- **后端**：基于 Spring Boot 3 + MyBatis Plus 的现代化 Java 技术栈
- **前端**：基于 Vue 3 + TypeScript + Vite 的现代化前端技术栈
- **架构**：前后端分离，模块化设计，易于扩展和维护
- **工具链**：完善的开发工具链，提高开发效率和代码质量
