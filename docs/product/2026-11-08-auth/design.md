# 认证、会话与权限边界 — 设计草案

> **设计草案。** 文档已随 PR [#57](https://github.com/wangyonghao/wyh-admin/pull/57) 进入 `main`。合入文档不等于批准按后文推荐去改代码。
>
> 本次核对基线：`main` `8c260136df3156bdc974232b7b40138d621f210b`（2026-11-09）。加入组织的流程仍见 [`../2026-11-08-user-registration/design.md`](../2026-11-08-user-registration/design.md)。两文都还含未实施的推荐。推荐没有对应提交和测试时，不能当成现状。
>
> 读的时候分三档：
>
> | 档 | 含义 |
> |----|------|
> | 已实现 | 当前 `main` 上有提交，并且有测试锁住这个行为 |
> | 现状 | 代码现在就是这样，但不等于采纳了后文的推荐 |
> | 推荐，未实施 | 还没有代码。未评审通过前不要做 |

## 0. 已实现（有提交和测试）

只列这次核对能对上代码和测试的项。其余安全建议不在此列。

| 项 | 依据 | 测试 |
|----|------|------|
| 模块地图与 Java 约定使用 `admin-system`、`admin-auth`、`admin-system-client`、`start`、`commons` | PR #58，`33c8ddfe`。`docs/architecture/repo-map.md`、`docs/conventions/backend-java.md` | 文档变更，无单测 |
| 部门 `type` 为字符串，对齐 `sys_department.type varchar(20)`，没有 Java 枚举 | PR #59，`c3cc3749`。`Department.type`、`SysDepartmentDO.type` | `DepartmentTypeMappingTest` |
| 自助注册提交原密码，不再调用 `encryptByRsa`。`RegisterRequest` 写明经 HTTPS 传输、服务端慢哈希 | PR #60，`c0131477`。`toRegisterRequest`、`RegisterRequest` | `register-request.test.ts`；`AuthServiceImplRegisterTest.registerForwardsSubmittedPassword`；`UserProfileServiceImplRegisterTest.registerLocalStoresBcryptOfSubmittedPassword` |
| 写入用户名长度为 4–64，含 4 和 64。这是用户确认的长度 | PR #60，`c0131477`。`RegexConstants.USERNAME` = `^[a-zA-Z][a-zA-Z0-9_]{3,63}$`，前端 `USERNAME_WRITE_PATTERN` 相同 | `UsernameRulesTest.lengthIsInclusiveFrom4To64`；`username.test.ts` |
| 已有短用户名仍可登录。登录不套用 4 位下限 | 同上。`acceptsLoginUsername` 只拒绝空值 | `username.test.ts` 的 `login username compatibility`；`AuthServiceImplRegisterTest.passwordLoginForwardsShortExistingUsername` |

用户确认的是长度 4–64。正则里的字母、数字、下划线是现有实现，测试把它锁住了。不要把这套字符类写成用户新确认的限制。

注册取消 RSA 只覆盖自助注册。登录本来就提交原密码。个人改密（`security-settings.vue`、`ProfileController`）和平台创建租户管理员（`tenant/management` 的 `encryptByRsa`、`TenantDataAppServiceImpl`）仍用 RSA。`RsaUtils` 还在。

PR #60 的测试同时锁住了另一件**现状**：`registerForwardsSubmittedPassword` 仍把部门 `1` 和角色 id `3` 传给 `registerLocalUser`。这不是「注册不再分配部门和角色」已经实现。

下面这些推荐在 `8c260136` **没有**对应实现，不能因为文档曾合并就当成已批准：

- 无租户会话不再落到平台租户 `0`
- 切换组织时重算部门、角色、数据范围
- 权限 SQL 按当前租户裁剪；数据范围 `ALL` 被收成「仅当前租户」
- 注册开关拆成「允许自助建租户 / 允许申请」
- `sys_login_log.username` 仍是 `varchar(50)`。`platform-schema.sql` 第 516 行，master changelog 没有后续 changeset 把它加长。51–64 位用户名写入登录日志时，列长不够。这是缺口，不是已修复项

## 1. 已明确的原则和决定

这些是评审约束，不是代码已经做成的样子。

| 项 | 内容 |
|----|------|
| 租户管边界 | 一个租户的数据不能因为另一个租户的部门上下级被看见 |
| 组织树管结构 | 部门树只描述租户内部结构 |
| 成员管归属 | 用户属于哪些租户、哪个部门，看成员关系 |
| 角色管权限 | 菜单、权限码、数据范围来自角色 |
| 上下级不跨租户 | 父部门、父组织不自动获得子租户的可见性 |
| 用户名长度 | **已实现**（PR #60）：写入 4–64，含两端。字符类沿用现有正则，不是另一次「只允许 ASCII」的产品决定。登录兼容短用户名 |
| 注册传输 | **已实现**（PR #60）：仅自助注册不再 RSA。登录、改密、租户管理员口令的 RSA 不在这次删除范围内 |
| 无租户会话 | **目标**：没有租户成员时，上下文不得回退到平台租户 `0` |
| 切换组织 | **目标**：先校验成员，再整份重建会话里的租户、部门、角色和数据范围 |
| 数据范围 ALL | **目标**：只表示当前租户内不做部门收窄，不表示跨租户 |
| 请求头 | **目标**：`X-Tenant-Id` 不能单独把一个人授权进另一个租户 |

产品仍是可二开的模块化单体。本文不引入刷新令牌协议、不把账号改成全局身份、不接新的短信厂商。

## 2. 和注册草案的分工

| 文档 | 写什么 | 不写什么 |
|------|--------|----------|
| 本文 | 认证模块边界、现有登录方式、会话、鉴权、安全边界、与注册草案的衔接 | 邀请状态机的逐步展开 |
| 注册草案 | 注册与加入/创建租户拆开、邀请、申请、开关、验收里的两租户案例 | 不要再把它读成「注册应继续 RSA」 |

注册草案第 8 节除「注册不再 RSA」和「用户名 4–64」外，仍是未实施的推荐。注册不再要求先解密 RSA 密文，因为请求里已经是原密码。无租户拦截、邀请角色白名单和阶段顺序以注册草案第 8.4 节为准，本文不另写一套。

## 3. 模块与目录

磁盘目录（基线 `8c260136`）是：

- `backend/admin-auth`：认证限界上下文。`pom.xml` 依赖 `admin-system-client`、`cmn-security`、`cmn-web`、`cmn-db`、`cmn-cache`、`cmn-tenant`、`spring-security-crypto`。不依赖 `admin-system`。
- `backend/admin-system`：用户、角色、部门、租户产品、成员、组织 API 的实现。
- `backend/admin-system-client`：跨模块契约。`UserApi`、`UserCredential` 在 `top.wyhao.identity.client`。权限与菜单契约在 `top.wyhao.security.client`。OTP 场景枚举在 `top.wyhao.security.client.otp`。
- `backend/start`：装配、`application.yml`、Liquibase、`ApiSignConfig`。
- `backend/commons`：`cmn-security`（Sa-Token、密码哈希）、`cmn-tenant`、`cmn-core`（`LoginUser`、`RegexConstants`）、`cmn-db`（数据权限）。

`docs/architecture/repo-map.md` 和 `docs/conventions/backend-java.md` 已按磁盘目录改过（PR #58，`33c8ddfe`）。根目录 `AGENTS.md` 仍写 `admin-biz-system`、`admin-start`、`admin-commons`。本文引用磁盘路径，不在这里改 `AGENTS.md`。

职责：

| 问题 | 放在哪 |
|------|--------|
| 登录、注册、登出、图形验证码、OTP 发码与校验、社交登录入口 | `admin-auth` 的 `AuthController` / `AuthServiceImpl` |
| 账号资料、改密、管理员重置密码、角色分配、部门 | `admin-system`。认证通过 `UserApi`、`PasswordApi`、`RoleApi` 调用 |
| 会话里的 `LoginUser` | `backend/commons/cmn-core` 的 `top.wyhao.cmn.identity.LoginUser`。字段有 `userId`、`username`、`tenantId`、`departmentId`、`roleDataScopes` 等。没有权限码列表 |
| 权限码与角色码 | `StpInterfaceImpl` 调 `PermissionApi`。实现读 `sys_user_role`，不读 `sys_membership` 的 `ROLE` |
| 租户行 | `TenantInterceptor` + `DefaultTenantLineHandler`。只作用于实现了 `TenantedEntity` 的表 |

`cmn-security` 的 `SaTokenConfig` 注释仍写「业务模块（admin-biz-system）」。实现类在 `admin-auth` 的 `StpInterfaceImpl`。

## 4. 现状：认证方式与 API

下列接口在基线 `8c260136` 的代码里存在。没有单独的刷新令牌接口，也没有 OAuth2 refresh grant。

### 4.1 `admin-auth` 已有

`AuthController`：

| 方法 | 路径 | 登录要求 | 说明 |
|------|------|----------|------|
| POST | `/auth/login?grantType=PASSWORD\|OTP\|OAUTH` | `@SaIgnore` | 成功返回 `AccessTokenResponse`（`code`、`token`），不是包在 `Result` 里 |
| POST | `/auth/logout` | 要登录 | `StpUtil.logout()`，注销当前 token |
| GET | `/captcha` | `@SaIgnore` | 图形验证码。功能关闭时由策略返回空图 |
| POST | `/auth/register` | `@SaIgnore` | 见注册草案。成功同样返回 token |
| POST | `auth/otp` | `@SaIgnore` | 发邮箱或短信验证码，带 `@RateLimiter` |
| POST | `auth/otp/verify` | `@SaIgnore` | 校验验证码 |
| GET | `/oauth/{provider}` | `@SaIgnore` | 返回第三方授权地址 |
| GET | `/auth/user` | 要登录 | 资料、角色码、权限码、菜单树 |

`GrantType` 只有 `PASSWORD`、`OTP`、`OAUTH`。`LoginRequest` 没有 `tenantId`。

前端 `frontend/apps/admin/src/api/auth.ts` 与上述路径对应。`store/user.ts` 的登录会删掉 `params.tenantId`，密码原样提交。注册经 `toRegisterRequest` 原样提交密码，不再 `encryptByRsa`（`c0131477`）。

### 4.2 会话与账号的相关接口（不在 `admin-auth`，但是认证边界的一部分）

| 方法 | 路径 | 模块 | 现状 |
|------|------|------|------|
| PUT | `/system/user/{id}/password/reset` | `admin-system` | 管理员重置，权限 `system:user:resetPwd`，返回随机新密码 |
| PATCH | `/user/profile/password` | `admin-system` | 已登录用户改密。`ProfileController` 用 RSA 私钥解密旧密码和新密码 |
| GET/DELETE | `/monitor/online` | `admin-system` | 在线会话。踢出要 `monitor:online:kickout`。按 token 或全部 |
| POST | `/org/switch` 等 | `admin-system` | 见注册草案。切换不重算 `LoginUser` |

前端 `/auth/forget-password` 有页面。`forget-password.vue` 的 `handleSubmit` 不调用接口。自助找回密码**没有实现**。下文只把它列为可选建议。

### 4.3 拟新增（都还没有）

只列认证边界上要补的，邀请与申请的新增接口仍以注册草案第 12 节为准，且那一节未被批准。

| 接口 | 用途 | 现在 |
|------|------|------|
| `GET /auth/register/options` | 匿名读取注册相关布尔开关 | 无。`GET /system/config/register` 要登录 |
| 无租户会话白名单的强制执行点 | 没有租户时不能访问业务 API | 无。见第 6 节 |
| 自助找回密码 | 可选，未决定做不做 | 页面空提交 |

不新增 refresh token、不把访问令牌改成另一套 JWT 库。

## 5. 现状：凭证、会话、登出

### 5.1 令牌形态

`SaTokenConfig` 注册两件事：

- `StpLogicJwtForSimple`：Sa-Token 的 JWT 简单模式，令牌字符串按该模式签发。
- `SaTokenDaoForRedisson`：登录态放在 Redis。

因此这不是「只靠 JWT、服务端无会话」的实现，也不是带 refresh token 的协议。配置项名是 `sa-token.jwt-secret-key`，值来自环境变量 `WYH_JWT_SECRET`。

`application.yml`：

| 配置 | 值 | 注释里的含义 |
|------|----|----------------|
| `token-name` | `Authorization` | 头名称，也是 Sa-Token 的 cookie 名 |
| `token-prefix` | `Bearer` | |
| `is-read-header` | `true` | |
| `is-read-cookie` | `false` | 不从 cookie 读令牌 |
| `is-read-body` | `true` | |
| `timeout` | `86400`（秒） | 配置注释写的是令牌有效期 |
| `active-timeout` | `1800`（秒） | 配置注释写超过此时长不访问会冻结 |
| `auto-renew` | `true` | 配置注释写每次直接或间接 `getLoginId()` 会做过期检查与续签 |
| `dynamic-active-timeout` | `true` | |
| `is-concurrent` | `true` | 同一账号可多处同时登录 |
| `is-share` | `false` | 每次登录新建一个 token，不共用 |
| `is-log` | `false` | |

续签的细节以依赖的 Sa-Token 行为为准。仓库没有第二个「用 refresh 换 access」的接口。前端 `access` store 有 `refreshToken` 字段和偏好项文案，`utils/request.ts` 只把 `user` store 里的 `token` 放到 `Authorization: Bearer`。请求层没有刷新流程。

前端 `user` store 把 `token` 持久化在 key `user`。这是浏览器存储，不是 cookie 会话。

### 5.2 签发

`TokenGatewayImpl.issue`：

1. `StpUtil.login(user.id())`。
2. `LoginUserAssembler` 写入用户 id、用户名、部门 id、改密时间、设备类型，以及 `loginContextApi.listRoleDataScopes(userId, departmentId)`。
3. 会话另存 IP、浏览器、登录时间等。
4. `resolveLoginTenantId` 后 `LoginUtil.setTenantId`。没有 `TENANT` 成员且不是超管时，这个值可以是 `null`。

角色数据范围在签发时算一次。`switchOrg` 只改租户 id，不调用 `LoginUserAssembler`。

### 5.3 密码登录

`PasswordSecurityService`（代码注释与实现一致的部分）：

- 按账号和 IP 检查是否已锁定。
- 失败次数达到策略阈值才要图形验证码。验证码功能可关。
- 用 `RegexConstants.PASSWORD` 做格式快检，不通过也算凭证失败。
- `PasswordEncoder.matches` 比对哈希。编码器是 `PasswordEncoderFactories.createDelegatingPasswordEncoder()`。种子口令带 `{bcrypt}` 前缀。
- 用户不存在、格式错误、密码不匹配都走 `rejectCredential`：计数，前端文案是「账号或密码错误」或锁定/需要验证码。`realReason` 进登录审计。

登录和注册提交的都是原密码，不经 RSA。注册后端直接做策略校验和 `PasswordEncoder.encode`。这是 `c0131477` 之后的现状。个人改密仍在 `ProfileController` 里解密。

### 5.4 登出与作废

| 动作 | 实现 | 范围 |
|------|------|------|
| `POST /auth/logout` | `StpUtil.logout()` | 当前 token |
| 禁用用户、删除用户 | `LoginUtil.kickout(userId)` → `StpUtil.kickout(userId)` | 该登录 id 下的会话 |
| 在线用户踢出 | `StpUtil.kickoutByTokenValue` | 指定 token |
| 前端 401 | `handleUnauthorized` 清本地 store，不调用登出接口 | 只清浏览器 |

并发策略就是配置里的「允许并发、每次新 token」。没有「最多 N 个会话」的业务上限。新登录不会挤掉旧登录，因为 `is-concurrent` 为 true。`NotLoginException` 仍准备了「已被顶下线」文案，那是 Sa-Token 在被顶替时的类型，当前配置不会因为第二次登录去顶替第一次。

### 5.5 登录方式里另外两条

- OTP：`OtpScene` 含 `LOGIN`、`REGISTER` 等。发码和 OTP 登录会用验证码。注册接口本身不调用 OTP。
- 社交：`loadUserWithProvider` 找不到绑定时会 `registerSocialUser`，部门用 `SUPER_DEPARTMENT_ID`（`1L`，种子名「总公司」），并 `assignGeneralUserRole`。这不在「取消注册 RSA」的范围内。注册草案写明社交注册不在其首期改动里。

## 6. 现状：租户、切换、权限

### 6.1 请求上的租户从哪来

`TenantInterceptor` 调用 `TenantSelection`：

1. 已登录且会话里有租户 id：用会话，不用 `X-Tenant-Id`。
2. 未登录，或会话租户 id 为 `null`：解析 `X-Tenant-Id`，非法则 `TenantException.invalidId()`。
3. 都没有：默认租户。`application.yml` 的 `default-tenant-id` 是 `0`。

所以：**已登录且会话租户非空时，请求头不能改数据租户。** 未登录时，请求头会选中租户上下文。登录页当前不发送这个头。`docs/runbook/tenant-demo.md` 仍写登录页有租户 ID 并带 `X-Tenant-Id`，与 `login.vue`、`user.ts` 不符。以代码为准。

无租户账号签发的会话租户是 `null`。下一个请求会落到第 3 步，上下文变成租户 `0`。租户 `0` 上有种子管理员和部门「总公司」。这是当前选择规则，不是已经挂上的额外拦截器。

`SaExtensionInterceptor` 没有注册到拦截器链，不能当作无租户拒绝的实现。注册草案第 8.4 节 B2 把落点定在 `ApiSignConfig` 的 `checkLogin` 之后，并同时改 `TenantSelection`，避免已登录且会话租户为空时先落到平台租户 `0`。这仍是未实现的设计。

### 6.2 组织切换

`OrgServiceImpl.switchOrg`：不是该租户的有效成员且不是超管则 `ORG_NOT_MEMBER`；非超管会 `setPrimaryTenant`；然后 `LoginUtil.setTenantId` 并设置 `TenantContextHolder`。不重算 `roleDataScopes`，不改 `LoginUser.departmentId`。

成员是否有效看 `MembershipRules`：未删除、状态 `NORMAL`、未过期。生产代码里没有把 `TENANT` 成员写成停用的产品接口。`ensureTenantMembership` 会把非 `NORMAL` 改回 `NORMAL`。

### 6.3 四层权限现在怎么叠

| 层 | 现状 | 是否按当前租户 |
|----|------|----------------|
| 套餐菜单 | `PackageMenuBinding.menusForTenantAdmin`：租户初始化时，角色码 `admin` 的菜单等于套餐菜单 | 初始化发生在该租户上下文 |
| 角色功能 | 运行时权限 SQL（`SysMenuMapper.xml` 的 `selectPermissionByUserId`）和角色 SQL（`SysRoleMapper.selectRolesByUserId`）只按 `user_id` 连接 `sys_user_role` | SQL 文本没有 `tenant_id` |
| 数据范围 | `LoginUser.roleDataScopes` 在登录时计算。`DataPermissionPredicate` 遇到任一角色 `ALL` 就返回 `null`，该方法不再加部门条件。超管在 `DefaultDataPermissionHandler` 直接不加数据权限 | 计算角色时未按会话租户过滤。`ALL` 本身不带租户 id |
| 租户行 | `TenantedEntity` 的表由插件加 `tenant_id = 当前上下文` | 是。未实现 `TenantedEntity` 的表不加 |

`ALL` 今天的含义是「这条 `@DataScope` 查询不再加部门谓词」。若表同时是 `TenantedEntity`，行插件仍按上下文租户限制。若表不是，`ALL` 不会替它补上租户条件。超管连数据权限谓词也跳过，但仍走租户插件，除非别处 `ignore`。

`RoleCodeEnum.TENANT_ADMIN` 的码是 `admin`，不是字符串 `TENANT_ADMIN`。`isSuperadmin()` 只认 `super_admin`。`isSuperRoleCode` 同时包含 `super_admin` 和 `admin`，用于禁止通过角色接口创建这两种码（`RoleServiceImpl`）。邀请和审批用 `hasRole("admin")`。因为角色列表不按租户裁剪，用户只要在任一租户有 `admin`，其它会话里 `hasRole("admin")` 仍可能为真。

部门上下级只出现在 `RoleDataScopeRules` 的部门 id 列表（本部门、本部门及下级、自定义部门）。没有「父部门可读另一个 `tenant.id`」的代码。

平台管理员与租户管理员：

| | 平台超管 `super_admin` | 租户角色 `admin` |
|--|------------------------|------------------|
| 谁 | 种子角色 id `1`，`LoginUtil.isSuperadmin()` | 每个新建租户在 `TenantDataAppServiceImpl.initRoleData` 里建一条，数据范围 `ALL` |
| 权限码 | `findUserPermissions` 见到该角色码就返回 `*:*:*` | 来自该角色的 `sys_role_menu`，初始化时等于套餐 |
| 无组织时 | `resolveLoginTenantId` 回到默认租户 `0` | 不会因为角色码 `admin` 走这条分支 |
| 切换 | 可以切到非成员租户 | 必须是有效成员 |
| 跨租户 | 能切换会话租户，从而让行插件指向目标租户 | 不应自动看见其它租户。当前角色 SQL 未按租户裁剪，所以「不应」还不是运行时保证 |

### 6.4 账号行与多成员

`sys_user` 是 `TenantedEntity`。唯一索引是 `(username, tenant_id)`、`(email, tenant_id)`、`(phone, tenant_id)`（`platform-schema.sql` 末尾）。`existsUsername` 走带租户插件的查询。`selectByUsername` 的 SQL 文本没有 `tenant_id`，也没有 `@InterceptorIgnore`。

同一个人加入多个租户，现有邀请接受是给同一个 `user_id` 增加 `TENANT` 成员，不新建用户行。`sys_user.tenant_id` 仍是一行一个值。两套语义并存。

**不把「改成全局用户名、去掉 `sys_user.tenant_id`」当作默认方案。** 收敛方式与注册草案第 9 节相同，且仍待评审：自助注册和公开登录的查重留在默认租户上下文；加入其它租户只加成员和该租户的角色；禁止为了查用户名而 `ignore` 租户插件扫全表。

## 7. 现状：安全措施（只写代码里有的）

| 主题 | 代码里有的 | 没有的 |
|------|------------|--------|
| 口令存储 | DelegatingPasswordEncoder；注册与管理员创建都 `encode`。注册收到的是原密码 | 仓库没有强制 HTTPS 的过滤器 |
| 口令策略 | `UserDomainService.assertCompliant`：`StrUtil.length`、`PASSWORD_TEMPLATE`、可选特殊字符、不可含用户名、历史口令。策略边界见 `PasswordPolicy`：最短下限 8、上限 32，默认最大长度常量 32 | 不按 Unicode 字素计数 |
| 登录防爆破 | 账号与 IP 计数、锁定、失败到阈值后图形验证码 | 注册接口没有 `@RateLimiter` |
| OTP 限流 | `POST auth/otp` 上多条 `@RateLimiter` | 不覆盖注册 |
| 管理员重置 | `PUT /system/user/{id}/password/reset` 返回随机密码 | 自助找回没有接口 |
| 传输 | 令牌走 `Authorization` 头。开发配置 CORS：`wyhao-starter.web.cors.enabled=true`，`allowed-origins: '*'`。源为 `*` 时 `CorsConfig` 使用 `addAllowedOriginPattern("*")`，不把 `allowCredentials` 设为 true | 仓库内没有 CSRF 过滤器，也没有强制 HTTPS 重定向 |
| 日志 | 登录失败把模糊文案给前端，`realReason` 给审计。用户表邮箱电话用 `EncryptTypeHandler`。权限失败日志打出请求 URI 和异常 message | 登录日志存用户名，没有单独的口令字段。未发现对令牌的统一脱敏工具。`sys_login_log.username` 仍是 `varchar(50)`，没有 Liquibase 把列加长到 64 |

CSRF：当前令牌不从 cookie 读取，前端从持久化的 `token` 放到头里。典型的「浏览器自动带 cookie」CSRF 不作用于这条读令牌的路径。CORS 在开发配置里放开了任意源。这不等于已经做了 CSRF 令牌方案。生产配置同样有 `wyhao-starter.web.cors` 段，是否收紧源列表以 `application-prod.yml` 为准，不在本文改配置。

HTTPS：第 1 节要求传输依赖 HTTPS。代码没有拒绝明文 HTTP。这是部署要求，不是已实现的过滤器。

枚举：密码登录对「无此用户」和「密码错误」使用同一句「账号或密码错误」（锁定和需要验证码除外）。注册同名仍是 `USERNAME_EXISTS` / 「用户名已被占用」。申请编码不存在时 `ORG_CODE_INVALID` 的文案带上调用方输入的编码。这些是现状。注册草案建议公开注册改成不区分文案，该建议未被批准。

## 8. 用户名与密码的长度口径

不要把「字符」理解成 Unicode 码点或字素簇。用户名长度以 PR #60 的表达式为准。密码正则这次没有改。

### 8.1 用户名

`RegexConstants.USERNAME`：

```text
^[a-zA-Z][a-zA-Z0-9_]{3,63}$
```

`c0131477` 起后端 `RegexConstants.USERNAME`、前端 `USERNAME_WRITE_PATTERN` 和注册页都是这一条。

- 用户确认并已实现的是总长度 **4 到 64**，含两端。首位 1 个字母，后面 `{3,63}`。
- 当前字符类是正则里的字母、数字、下划线。中文、空格、连字符匹配失败。这是 `UsernameRulesTest.keepsLetterDigitUnderscoreClass` 锁住的现有行为，不是用户另行确认的新限制。
- 登录不套 4 位下限。`acceptsLoginUsername` 只拒绝空字符串；3 位已有用户名可以提交。

不要为了「统一」去改登录下限，也不要在没有新决定时放宽或收紧字符类。

### 8.2 密码

两条口径同时存在，不要合成一个新数字：

1. 登录快检 `RegexConstants.PASSWORD`：`^(?=.*\d)(?=.*[a-z]).{8,32}$`。`ReUtil.isMatch` 使用 Java 正则。` . ` 默认匹配一个 UTF-16 代码单元（不匹配换行）。`(?=.*[a-z])` 要求至少一个 ASCII 小写字母，`\d` 要求一个数字。长度 8–32 指的是这个匹配单位，不是「一个 emoji 算 1」。
2. 入库策略 `UserDomainService.assertMinLengthAndFormat`：先用 Hutool `StrUtil.length`（Java `String.length()`，同样是 UTF-16 代码单元个数）比最短长度，再用 `PASSWORD_TEMPLATE` 套上策略里的 `minLength`、`maxLength`。策略允许的最短在 8–32 之间，最大长度不超过 32（`PasswordPolicy.MIN_LENGTH_UPPER`）。

注册入库走第 2 条；密码登录快检走第 1 条。若策略把最短改成大于 8，两边会不一致。本文不新造第三套长度。

## 9. 异常码与前端路由

业务失败多是 HTTP 422 的 `Result`（`code` + `message`）。`@Valid` 失败是 HTTP 400。未单独处理的异常，包括唯一索引冲突，是 HTTP 500，`code` 为 `SYSTEM_ERROR`。

认证类（`AuthenticationException`）已有的码包括：`PASSWORD_ERROR`、`NEED_CAPTCHA`、`TOO_MANY_REQUEST`、`CAPTCHA_IS_REQUIRED`、`CAPTCHA_INVALID`、`CAPTCHA_EXPIRED`、`AUTH_CAPTCHA_OUTDATED`、`AUTH_CAPTCHA_INCORRECT`、`OTP_ERROR`、`AUTH_PHONE_NOT_BOUND`、`AUTH_EMAIL_INVALID`、`AUTH_PHONE_INVALID`、`AUTH_EMAIL_NOT_BOUND`、`AUTH_SOCIAL_AUTH_FAILED`、`CAPTCHA_SEND_FAILED`、`ACCOUNT_LOCKED`、`PLATFORM_NOT_SUPPORT`、`AUTH_ACCOUNT_DISABLED`、`AUTH_ACCOUNT_DEPARTMENT_DISABLED`、`REGISTRATION_DISABLED`、`PASSWORD_MISMATCH`、`REGISTER_ROLE_REQUIRED`。`PASSWORD_MISMATCH` 在仓库里没有被注册流程引用。

未登录（`SaTokenExceptionHandler`）：HTTP 401，`code` 为 `UNAUTHORIZED`。文案按踢下线、被顶下线、其它（过期）区分。无权限码或角色：HTTP 403，`code` 为 `FORBIDDEN`，文案「没有访问权限，请联系管理员授权」。

前端路由（`requiresAuth: false`）：`/auth/login`、`/auth/code-login`、`/auth/qrcode-login`、`/auth/forget-password`、`/auth/register`、社交回调。登录守卫：无 token 去登录并带 `redirect`；已登录后若 `needsOrganization` 且不是 `/org/setup` 或 `/org/invite`，去 `/org/setup`。注册成功当前是 `completeLogin` 后进默认首页，再被守卫转到待加入页。登录页始终有注册链接，不读开关。

## 10. 推荐设计（确认后才实现）

下列条目是建议。未确认不写代码。与注册草案重复的加入组织规则不在这里再展开。

1. **注册与入会分开。** 细节用注册草案：注册不建租户、不授予 `admin` / `super_admin`、不放入部门 `1`。草案本身仍待批准。
2. **注册传输。** PR #60 已让自助注册提交原密码。不要把这次改动扩大成取消个人改密或平台管理员口令的 RSA。
3. **用户名。** 写入长度 4–64 已实现。没有新的字符类决定，保持 PR #60 的现有正则。登录继续允许更短的已有用户名。
4. **无租户不回退到 0。** 与注册草案 B2 相同：`TenantSelection` 对已登录且会话租户为空不使用默认租户 `0`；拒绝名单外请求的代码放在 `ApiSignConfig` 的 `checkLogin` 之后。不挂载 `SaExtensionInterceptor`。匿名登录和注册不在这道 403 里。
5. **请求头不授权。** 保持「已登录且会话租户非空时忽略 `X-Tenant-Id`」。另外：`/auth/login` 与 `/auth/register` 的查账号固定在默认租户上下文，不因请求头换一套用户行。这不是扫全库。
6. **切换组织。** 成员校验保持。通过后重算并写回：会话租户、该租户内的主部门、`sys_role.tenant_id = 当前租户` 的角色、这些角色的 `roleDataScopes`。`super_admin` 仍走现有 `*:*:*` 短路。前端继续拉 `/auth/user` 并刷新。
7. **ALL 只限当前租户。** 数据范围 `ALL` 继续表示「不再加部门谓词」，但角色必须已经是当前租户的角色。不给 `ALL` 增加跨租户可见。不是 `TenantedEntity` 的业务表不能靠 `ALL` 获得隔离，缺隔离的表要单独点名，不在本文一次性改全库。
8. **平台管理员与租户管理员。** 不把角色码 `admin` 放进 `isSuperadmin()`。租户 `admin` 不能仅凭角色码操作其它租户。平台超管仍用现有切换能力进入某个租户，行插件随会话租户走。
9. **查重。** 维持 `(username, tenant_id)`。不默认做全局身份重构。
10. **会话。** 保持 Sa-Token 简单 JWT + Redis、并发登录、每次新 token、登出只注销当前 token、管理员可踢 token 或用户。不新增 refresh token。若要限制并发个数，另作决定（A1）。
11. **审计。** 注册成功和拒绝补上现有 `AuthLogGateway`。不记口令、RSA 私钥、邀请 token。

## 11. 兼容与迁移（只设计）

不写 Liquibase。不改已发布 changeset。

- 用户名 4–64 和自助注册取消 RSA 已在 PR #60，不是待迁移项。
- `sys_login_log.username` 仍是 `varchar(50)`。若要让 51–64 位用户名的登录审计写得进去，需要**新的** Liquibase changeset，不能改已经发布的建表 changeset。本文不写这条迁移。在它落地之前，这是已知缺口。
- 无租户会话、切换时重算角色，不要求新表。
- `sys_user.department_id` 改为可空、邀请表增加部门与角色、成员写入真实 `tenant_id`，仍以注册草案第 17 节为草案，未被批准。历史成员行的 `tenant_id` 默认是 `0`，首期不回填。
- 已有注册用户若已被写入部门 `1` 和角色 `general`，不做批量删除。新注册才按新规则。

## 12. 验收矩阵

A11、A12 已有单元测试覆盖提交口令和用户名边界（见第 0 节）。下表其余行仍是未实现推荐的验收，这次没有跑。

| ID | 场景 | 预期 |
|----|------|------|
| A1 | 密码登录错误口令 | 前端「账号或密码错误」，审计有真实原因，无口令 |
| A2 | 同一账号两处登录 | 两个 token 都有效（当前并发配置）。登出只失效当前这一个 |
| A3 | 管理员踢出指定 token | 该 token 再访问得到 401「您已被踢下线」；同账号其它 token 仍可用 |
| A4 | 禁用用户 | 不能登录；已有会话被 `kickout(userId)` |
| A5 | 已登录用户带别人的 `X-Tenant-Id` | 数据租户仍是会话租户 |
| A6 | 未加入租户的账号访问 `/system/user` | 不能读到租户 `0` 的用户。待加入页仍可用 |
| A7 | 只属于租户 A 的用户切换到 B | `ORG_NOT_MEMBER` |
| A8 | 在 A 是 `admin`、在 B 无角色 | 切到 B 后没有 A 的菜单，不能对 B 发邀请 |
| A9 | A 的角色数据范围是 `ALL` | 能看 A 内该查询的各行（仍受租户插件约束的表），看不到 B |
| A10 | 部门上下级 | 不因此看见另一租户 |
| A11 | 注册后再用同一口令登录 | 成功。注册提交的是原密码，入库的是其慢哈希 |
| A12 | 用户名长度 4、64 可注册，3 与 65 拒绝 | 与 `c0131477` 的 `^[a-zA-Z][a-zA-Z0-9_]{3,63}$` 一致 |
| A13 | 超管切换到某租户 | 仍有 `*:*:*`。普通成员不会因此得到 `*:*:*` |
| A14 | 自助忘记密码页 | 在决定做找回之前，提交不产生口令重置 |

两租户隔离的更多案例在注册草案第 18 节（G2–G4 等），那些案例同样未实施。

## 13. 待决

| ID | 问题 | 草案倾向 |
|----|------|----------|
| A1 | 是否限制同一账号的并发会话数 | 保持现在的允许并发。要上限再单独定 |
| A2 | 自助找回密码做不做 | 不做进首期。页面保持不能重置口令，或暂时从登录页拿掉入口。二选一等确认 |
| A3 | 登录查用户名的手写 SQL 是否已被租户插件改写 | 实现时看 SQL 日志。无论是否改写，都不加 `ignore` 去扫其它租户 |
| A4 | 注册草案 R1–R8 | 仍以注册草案为准，本表不覆盖。总设计不把它们标成已同意 |
| A5 | 开发配置 CORS `*` 是否在生产收紧 | 不在本设计改配置。生产源列表若要改，单独确认 |

已实现、不必再做一遍的：用户名写入长度 4–64、登录兼容更短的已有用户名、自助注册取消 RSA。字符类维持现有正则。改密和租户管理员口令的 RSA 保持不动。

仍未实现、本文也不把它标成已批准的：无租户不回退平台 `0`（落点见注册草案 B2）、切换时重建会话上下文、`ALL` 与角色按当前租户裁剪、请求头不能单独授权、不默认做全局身份重构、把 `sys_login_log.username` 加长到至少 64、邀请角色白名单。R1–R8 只记了评审倾向，没有用户逐项最终确认。A1–A5 都还没有选择，也没有代码。

## 14. 评审之后

两份草案都确认之前不实现。确认后可以再补 feature-spec 的三件套。不改 `docs/mission.md`、`docs/tech-stack.md`、`docs/roadmap.md`。不把未实现的行为写入根目录 `CHANGELOG.md`。
