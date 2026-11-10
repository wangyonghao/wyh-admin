# 用户注册与加入组织 — 设计草案

> **设计草案。** 本文已随 PR [#57](https://github.com/wangyonghao/wyh-admin/pull/57) 进入 `main`。合入文档不等于批准实施后文推荐。
>
> 本次核对基线：`main` `8c260136df3156bdc974232b7b40138d621f210b`（2026-11-09）。认证边界以 [`../2026-11-08-auth/design.md`](../2026-11-08-auth/design.md) 为准。
>
> 已实现、且有测试的只有两件事，都在 PR #60（`c0131477`）：
>
> - 自助注册提交原密码，不再 `encryptByRsa`。登录、个人改密、平台创建租户管理员的 RSA 没有取消。
> - 写入用户名长度为 4–64，含两端。字符类仍是现有正则（字母开头，其余为字母、数字、下划线），这是代码和测试的现状，不是用户新确认的字符限制。登录不套 4 位下限，短用户名仍可登录。
>
> `AuthServiceImplRegisterTest.registerForwardsSubmittedPassword` 仍期望注册写入部门 `1` 和角色 id `3`。第 8 节「注册不分配部门和角色」没有实现。
>
> R1–R8 在这个基线上都没有对应代码，仍待评审。下文推荐在确认前不要实现。
>
> 适用范围：自助注册账号、用邀请加入已有租户、已有账号申请或切换组织、管理员创建租户后邀请人员。政务机关只作为一套开关组合，不把底座做成税务或机关专用系统。

## 1. 已采纳的原则

这些原则作为本文的设计约束。它们还不是代码里的名词。

| 原则 | 含义 |
|------|------|
| 租户管边界 | 一个租户里的数据，不能因为另一个租户的部门上下级而被看见 |
| 组织树管结构 | 租户内部的部门树表示结构，不表示另一套租户 |
| 成员管归属 | 用户属于哪些租户、哪个部门，看成员关系 |
| 角色管权限 | 菜单、权限码、数据范围来自角色，不来自「是某个部门的上级」 |
| 上下级不跨租户 | 父部门、父组织都不会自动获得子租户的访问权 |

产品定位仍是可二开的模块化单体 B 端底座：稳定、够用。本文不补短信厂商、不补邮箱验证码产品、不把身份模型一次改成全局账号。

## 2. 术语对照

代码和文档里的「组织」今天就是租户。本文写目标时用「租户」表示边界；写到现有接口时保留路径 `/org`，并注明它操作的是 `tenant` 表。

| 本文用词 | 当前代码 | 不要理解成 |
|----------|----------|------------|
| 租户 | 表 `tenant`。没有 `sys_tenant` | 部门树上的一个节点 |
| 组织 API | 路径仍是 `OrganizationController` 的 `/org/**`，操作的是租户。验收和正文说「租户」，不说成部门树或租户树（C6） | 一套独立于租户的组织主数据 |
| 部门树 | `sys_department`：`parent_id`、`ancestors`、`tenant_id`，类型走字典 | 跨租户的机关树 |
| 成员关系 | `sys_membership.scope_type` = `TENANT` / `DEPARTMENT` / `ROLE` | 已经替代 `sys_user_role` 的权限表 |
| 账号 | 一行 `sys_user` | 每个租户自动再插一行同名用户 |

部门类型没有 Java 枚举。字典在 `backend/start/src/main/resources/db/changelog/postgresql/platform/platform-data.sql`：`1` 分公司、`2` 部门、`3` 用户组。`c3cc3749` 起 Java 字段与建表列都是字符串（`varchar(20)`）。

`docs/architecture/repo-map.md` 与 `docs/conventions/backend-java.md` 已改成磁盘目录（PR #58）。根目录 `AGENTS.md` 仍写 `admin-biz-system`、`admin-start`、`admin-commons`。代码目录是 `backend/admin-system`、`backend/admin-system-client`、`backend/admin-auth`、`backend/start`、`backend/commons`。本文引用磁盘路径，不改 `AGENTS.md`。

## 3. 现状

### 3.1 自助注册已经存在，并且会直接登录

后端：

- `POST /auth/register`，`@SaIgnore`。`backend/admin-auth/src/main/java/top/wyhao/system/auth/adapter/web/AuthController.java`
- 入参只有 `username`、`password`。`c0131477` 起 `RegisterRequest` 写明密码是经 HTTPS 传输的原密码，服务端做慢哈希。
- `AuthServiceImpl.register` 先读注册配置。`enabled` 不是 `true` 时抛 `REGISTRATION_DISABLED`。默认角色取配置里的 `defaultRoleId`，空则用角色码 `general`。然后调用 `userApi.registerLocalUser(username, password, SystemConstants.SUPER_DEPARTMENT_ID, List.of(roleId))`，接着 `tokenGateway.issue`，返回 token。
- `SUPER_DEPARTMENT_ID` 是 `1L`（`backend/commons/cmn-core/.../SystemConstants.java`）。种子部门 `id=1` 名称是「总公司」（`platform-data.sql`）。
- `UserProfileServiceImpl.registerLocal` 校验密码策略和用户名占用，写入启用用户，`displayName` 等于用户名，再 `replacePrimaryDepartment`，再 `roleApi.assignRolesToUser`。不创建租户，不写 `TENANT` 成员，不写邮箱和手机。
- 注册方法上有 `@Transactional`。方法内签发 token。注册路径没有调用 `AuthLogGateway`。`AuthController.register` 上没有 `@RateLimiter`。OTP 发送接口上有限流。

前端：

- 路由 `/auth/register`，`requiresAuth: false`。`frontend/apps/admin/src/router/routes.ts`
- 表单：用户名、密码、确认密码、同意协议。确认密码不到后端。`frontend/apps/admin/src/views/_core/authentication/register.vue`
- `c0131477` 起经 `toRegisterRequest` 原样提交密码，不再 `encryptByRsa`。成功后 `completeLogin`：保存 token、拉 `/auth/user`、进入默认首页。`frontend/apps/admin/src/store/user.ts`
- 登录页始终有「去注册」链接，不读注册开关。`login.vue`
- 守卫在 `needsOrganization` 时把已登录用户送到 `/org/setup`，邀请页除外。`frontend/apps/admin/src/router/guard.ts`

用户名写入规则在 `c0131477` 已对齐：前后端都是 `^[a-zA-Z][a-zA-Z0-9_]{3,63}$`，总长度 4–64，含两端。登录用户名不套这条规则。

密码：

- `c0131477` 起注册和登录都提交原密码。`registerLocal` 用 `PasswordEncoder.encode` 存储。注册方法不调用 RSA 解密。
- 改密接口 `ProfileController` 仍解密。平台创建租户管理员时，`TenantDataAppServiceImpl.initUserData` 仍解密。
- 哈希 Bean 是 `PasswordEncoderFactories.createDelegatingPasswordEncoder()`（`backend/commons/cmn-security/.../PasswordEncoderConfiguration.java`）。种子管理员密码带 `{bcrypt}` 前缀。

注册成功会直接签发会话。同一口令再登录应能通过，因为两边现在都是原密码。

### 3.2 注册开关在配置里，页面不看它

- 键 `register`。种子 JSON：`enabled: true`，`verifyEmail: false`，`verifyPhone: false`，`defaultRoleId: ""`。`platform-test.sql`。该文件被 `db.changelog-master.yaml` 引入，不是一份可有可无的旁路脚本。
- `RegisterConfigVO.isRegistrationEnabled()` 只有 `Boolean.TRUE.equals(enabled)` 才为真。缺省读出时 `enabled` 被收成 `false`。
- `GET/PUT /system/config/register`。PUT 要 `system:config:edit`。GET 没有 `@SaCheckPermission`，也没有 `@SaIgnore`，所以要登录，但不查这项权限。
- 配置页有「邮箱验证」「手机验证」「默认角色 ID」。`use-config-forms.ts`。
- `AuthServiceImpl.register` 不读取 `verifyEmail` / `verifyPhone`。
- `OtpScene.REGISTER` 存在于 `backend/admin-system-client/.../otp/OtpScene.java`。注册方法不发送、不校验 OTP。`frontend/apps/docs/prd/otp-*.md` 是文档站里的 OTP 说明，不是这条注册链路的实现。

角色 `general` 来自 `platform-test.sql`：`id=3`，名称「普通用户」，`data_scope=4`。`DataScopeEnum.SELF` 的值是 4，即仅本人。同一段种子没有把菜单绑到角色 `3`。`assignRolesToUser` 同时写 `sys_user_role` 和 `ROLE` 成员（`RoleServiceImpl`）。

### 3.3 登录会话与租户上下文

- `POST /auth/login?grantType=PASSWORD|OTP|OAUTH`。`LoginRequest` 没有 `tenantId`。
- `user.ts` 的 `login` 会删掉 `params.tenantId`。当前 `login.vue` 没有租户输入框。
- `docs/runbook/tenant-demo.md` 仍写登录页有「租户 ID」并在登录请求上带 `X-Tenant-Id`。以当前页面和 `user.ts` 为准。本草案不改 runbook。
- 未登录时，`TenantInterceptor` 用 `TenantSelection`：无会话租户则看 `X-Tenant-Id`，否则用默认租户。`application.yml` 里 `wyhao-starter.tenant.default-tenant-id` 是 `0`。
- `TokenGatewayImpl.issue` 执行 `StpUtil.login(user.id())`，写入 `LoginUser`（含 `departmentId`、`roleDataScopes`），再 `resolveLoginTenantId`，然后 `LoginUtil.setTenantId`。
- `OrgServiceImpl.resolveLoginTenantId`：若用户行上的 `tenantId` 不是默认租户且还没有 `TENANT` 成员，会补一条主组织成员。然后取主组织。没有主组织时，只有超级管理员回到默认租户 `0`，其他人返回 `null`。
- `LoginUtil.isSuperadmin()` 只判断角色码 `super_admin`。
- 权限列表来自 `StpInterfaceImpl` → `PermissionAppServiceImpl`。超管角色码得到 `*:*:*`。其他人的权限 SQL 在 `SysMenuMapper.xml` 的 `selectPermissionByUserId`：`sys_menu` ← `sys_role_menu` ← `sys_role` ← `sys_user_role` ← `sys_user`，条件是用户 id 和菜单状态。角色码 SQL 在 `SysUserRoleMapper.selectRoleCodesByUserId`，连接 `sys_role` 与 `sys_user_role`，没有 `tenant_id` 条件。
- `sys_user_role` 建表没有 `tenant_id`。`sys_membership` 的 `ROLE` 行会被双写，权限读取不查这张表。
- `SaExtensionInterceptor` 里有 `loginUser.getTenantId().equals(...)` 的比较，不相等时写「您当前没有访问该租户的权限」。全仓库只有这个类自己，没有注册进拦截器链。实际登录校验在 `ApiSignConfig` 的 `StpUtil.checkLogin()`。因此这段比较当前不会在请求上执行。

新注册用户没有 `TENANT` 成员，解析结果可以是 `null`，签发时会把 `null` 写入会话。下一次请求里，拦截器一侧可能是 `null`，租户上下文一侧会落到默认租户 `0`。本文不把这条路径写成已经抓到的异常；首期要规定无租户会话的合法值，并在验收里走到 `/org/session`。

### 3.4 组织创建、邀请、申请、切换

`OrganizationController`（均需登录，除预览）：

| 方法 | 路径 | 现在谁能调用 |
|------|------|----------------|
| GET | `/org/session` | 已登录 |
| POST | `/org` | 已登录。方法上没有权限注解 |
| POST | `/org/switch` | 已登录，且是该租户成员或超管 |
| POST | `/org/applications` | 已登录 |
| POST | `/org/applications/{id}/review` | 服务内要求超管或角色码 `admin` |
| POST | `/org/invites` | 服务内要求当前租户不是默认租户，且是超管或角色码 `admin` |
| GET | `/org/invites/preview` | `@SaIgnore` |
| POST | `/org/invites/accept` | 已登录 |
| POST | `/org/invites/reject` | 已登录 |

`POST /org` 调用 `tenantApi.createOwnedBy`。`TenantServiceImpl` 选第一个启用套餐，没有套餐则 `ORG_PACKAGE_MISSING`。`TenantDataAppServiceImpl.init` 在该租户内：建根部门（名称等于租户名，`type` 为 `"1"`，`parentId=0`）、建角色码 `admin`（`RoleCodeEnum.TENANT_ADMIN`，数据范围 `ALL`）、把套餐菜单绑到这个角色、给创建者写入 `sys_user_role` 和 `ROLE` 成员、`ensureTenantMembership(..., true)`。自助创建不新建用户行。

种子套餐 `id=1`「基础套餐」在 `platform-org.sql`，并绑了一批系统菜单。所以今天任何已登录用户都可以创建租户，并成为该租户的管理员角色。

平台管理员创建租户仍是 `POST /tenant/management`，权限 `tenant:management:create`。那条路径会为租户准备管理员口令（RSA 解密后哈希）。本文不改平台建租户的权限模型。

邀请表 `sys_org_invite`：只存 `token_hash`（SHA-256 十六进制），状态注释为 0 待处理、1 已接受、2 已拒绝、3 已过期、4 已撤销。`OrgInviteStatus` 含 `REVOKED(4)`。服务里没有把状态写成撤销的方法。

`OrgServiceImpl.invite`：同租户同邮箱若有未过期的待处理邀请，会换新的 `token_hash` 并延长过期时间。注释写「旧链接仍可用」，赋值已经换成新哈希。过期天数默认 7，链接前缀来自 `wyhao-starter.org.invite-base-url`。原始 token 会放进邮件，也会放进接口返回值。发信在同一个 `@Transactional` 里，失败抛 `ORG_INVITE_MAIL_FAILED`，邀请插入随之回滚。

接受邀请：当前用户必须已有邮箱，且与邀请邮箱规范化后一致；然后 `ensureTenantMembership`，状态改为已接受，并 `switchOrg`。不分配部门，不分配角色。拒绝只改邀请状态。

申请表 `sys_org_application`：同一租户同一用户仅一条 `status=0` 的部分唯一索引。通过后写 `TENANT` 成员，状态为成员枚举的 `NORMAL`，不是 `INVITED`。没有申请列表接口，没有撤销申请的服务方法。`frontend` 里 `orgApi.reviewApplication` 没有对应页面。

待加入页 `views/org/setup.vue` 默认停在「创建组织」，另有按编码申请、以及待处理邀请。待处理邀请的接受框要求用户自己粘贴邮件 token。邀请落地页 `views/org/invite.vue`：未登录先去登录，并带 `redirect` 回到当前链接；已登录才预览。注册成功不读这个 `redirect`。

### 3.5 账号行、唯一约束、成员表

`sys_user`（`platform-schema.sql`）：

- 初始唯一索引是全局的 `username` / `email` / `phone`。
- 同文件后部 `ALTER` 增加 `tenant_id int8 NOT NULL DEFAULT 0`，并改成 `("username","tenant_id")`、`("email","tenant_id")`、`("phone","tenant_id")`。
- `department_id` 在建表时是 `NOT NULL`。
- `email`、`phone` 注释为加密存储。`SysUserDO` 使用 `EncryptTypeHandler`。
- `SysUserDO` 与领域 `User` 实现 `TenantedEntity`。`DefaultTenantLineHandler` 对已登记的租户表加租户条件；上下文没有租户 id 时条件值是 `NULL`。
- `UserRepositoryImpl.existsUsername` 使用 `lambdaQuery`，会走租户插件。因此查重只看见当前租户上下文里的行。
- `SysUserMapper.selectByUsername` 的 SQL 文本本身没有 `tenant_id`。Mapper 上没有 `@InterceptorIgnore`。登录按用户名查找走这条语句。插件是否改写它，以实现时的 SQL 为准。首期不允许为了「找全库同名账号」给它加忽略租户。

`sys_membership`：

- 列含 `user_id`、`scope_type`、`scope_id`、`is_primary`、`status`、`expired_at`、`deleted`。
- 后加 `tenant_id int8 NOT NULL DEFAULT 0`。活跃唯一索引改为 `("tenant_id","user_id","scope_type","scope_id") WHERE deleted=0`。主部门唯一索引带上 `tenant_id`。
- `platform-org.sql` 另有主租户唯一索引：`("user_id") WHERE deleted=0 AND scope_type='TENANT' AND is_primary=TRUE`。这个索引不带 `tenant_id`。一个用户同时只能有一条主租户成员。
- `SysMembershipDO` 没有 `tenantId` 字段，也不实现 `TenantedEntity`。当前插入不会给 `tenant_id` 列赋业务值，列保持默认 `0`。
- `MembershipStatus`：`DISABLED(0)`、`NORMAL(1)`、`INVITED(2)`。`MembershipRules.isEffective` 要求未删除、状态为 `NORMAL`、未过期。生产写入路径里，`ensureTenantMembership` 会把非 `NORMAL` 改回 `NORMAL`。没有找到把成员改成停用或邀请中的产品服务。
- `replacePrimaryDepartment` 会软删该用户全部 `DEPARTMENT` 成员，再插入一条。它不能表示「两个租户各有一个主部门」。

成员表不进租户行插件是有原因的：`/org/session` 要按 `user_id` 列出这个人加入的多个租户。若成员表被当前会话租户滤掉，切换器就只剩当前租户。

### 3.6 两套归属同时存在

| 数据 | 谁在写 | 谁在读 |
|------|--------|--------|
| `sys_user.tenant_id` | 租户插件 / 显式赋值。注册方法自己不 set | 行隔离；`resolveLoginTenantId` 用它给旧数据补 `TENANT` 成员 |
| `sys_user.department_id` | 注册、用户管理、租户初始化 | 登录用户上的部门；登录时检查部门是否禁用 |
| `sys_membership` `TENANT` | 创建组织、接受邀请、审批通过、上面的兼容补写 | 组织列表、能否切换、登录选哪个租户 |
| `sys_membership` `ROLE` | 与 `sys_user_role` 一起写 | 权限 SQL 不读 |
| `sys_user_role` | `assignRolesToUser`、租户初始化直写 | 角色码、权限码、菜单、登录时的数据范围。这是权限读路径 |

`RoleCodeEnum.getSuperRoleCodes()` 同时包含 `super_admin` 和 `admin`。`isSuperadmin()` 只用 `super_admin`。邀请和审批用 `LoginUtil.hasRole("admin")`。角色码 SQL 不按租户过滤，所以用户只要在任一租户拥有 `code=admin` 的角色，其它会话租户里 `hasRole("admin")` 仍可能为真。这是当前查询形状，首期切换租户时要收紧到「当前租户的角色」。

部门父子只在 `RoleDataScopeRules` 里计算部门 id。没有「父部门自动拥有另一个 `tenant.id`」的代码。

用户禁用：`CredentialUser.canLogin()` 拒绝 `status=DISABLE(2)`；管理端改禁用会 `kickout`。这是整账号禁用，不是某一个租户里的成员停用。

登录审计：`AuthLogGateway` 在密码登录成功和失败时发布 `LoginEvent`。失败文案对前端是「账号或密码错误」，`realReason` 进审计。注册不走这里。操作日志切面存在；本草案没有把注册 Controller 标成已经挂了 `@OperationLogAnn`。邮箱、手机在用户表加密。邀请表上的邮箱是明文列。登录日志保存用户名，没有单独的密码字段。

错误体系统一是 `Result`：`code`、`message`、可选 `data`。业务异常 `BizException` 为 HTTP 422。参数校验（`BindException`，含 `@Valid`）为 HTTP 400，`code` 为 `"400"`。未单独处理的异常，包括唯一索引冲突，落到 HTTP 500，`code` 为 `SYSTEM_ERROR`，`message` 为「系统繁忙」。

## 4. 差距

相对第 1 节的原则，当前行为差在这些地方：

1. 注册和「进入一个租户并成为管理员」没有分开。注册把人放进部门 `1` 和角色 `general`，登录后 `/org/setup` 默认就能 `POST /org`，初始化会授予角色码 `admin` 和套餐菜单。
2. 公开注册开关后端有，登录页和注册路由不看。`verifyEmail` / `verifyPhone` 只出现在配置里。
3. 邀请能加入已有账号，但不能撤销；重发注释与哈希更新不一致；注册成功不会回到邀请链接。
4. 申请没有审核列表。编码不存在时，`ORG_CODE_INVALID` 的文案带上调用方刚输入的编码；存在时返回已是成员或已有待审。调用方可以据此区分编码是否存在。
5. 权限读路径不按会话租户裁剪。切换组织只改会话里的租户 id，不重算 `LoginUser.roleDataScopes` 和部门。
6. 账号唯一是「用户名 + `sys_user.tenant_id`」。匿名注册落在默认租户上下文。同一 `user_id` 又可以有多条 `TENANT` 成员。两套语义并存。
7. 无租户会话的 `tenantId` 可以是 `null`，而请求上下文会落到租户 `0`。租户 `0` 上有种子管理员和「总公司」。
8. 成员表上的 `tenant_id` 列没有被 Java 写入，主部门唯一约束因此区分不开两个租户。

这些是差距，不是一份漏洞公告。未在本环境执行注册或切换的运行时结果，下文一律写成要验收钉死的行为。

## 5. 目标与非目标

### 5.1 目标

评审通过并另开实现之后，底座应能：

- 打开或关闭公开注册。关闭时，注册接口与登录页入口一致拒绝。
- 注册只建立账号。不创建租户，不授予 `super_admin` 或 `admin`，不把人放进「总公司」。
- 已有账号凭邀请加入指定租户，或在开关允许时申请加入。
- 管理员用现有「租户管理」创建租户，再用邀请把人员加进来。
- 一个人可以先后属于多个租户。切换后，菜单、权限码和数据范围只反映当前租户的角色。超管仍按现有 `super_admin` 短路。
- 邀请单次有效、绑定目标租户和邮箱、可过期、可撤销。库中只存哈希。
- 两个租户的成员互相看不到对方的用户和部门。

### 5.2 非目标

- 不拆微服务，不新增登录中间件或另一套会话框架。
- 不在本阶段删除 `sys_user.tenant_id`，不把用户名改成全库唯一，不按用户名扫描其它租户。
- 不把手机号、邮箱验证码、短信或邮件厂商接进注册。配置字段可以留着，但首期逻辑不读它们，也不能在界面上显示成已经生效。
- 不做税务、机关、区划、编制字段。不建「县局注册中心」。
- 不改社交登录注册（它仍使用总公司与 `general`）。不改平台 `POST /tenant/management` 的权限码。
- 不在本文落地 Liquibase、页面或测试。
- 不把 `sys_membership.ROLE` 提升为权限读模型。权限仍读 `sys_user_role`，只是查询要加上当前租户。

## 6. 注册与加入租户解耦

三条动作分开：

```text
注册账号          只写 sys_user（以及首期明确允许的零条角色、零条租户成员）
加入已有租户      邀请接受或申请通过 → TENANT 成员，可选该租户内的部门与非管理角色
创建租户          单独开关。平台管理员走现有租户管理；自助创建默认关闭
```

账号没有租户成员时，可以登录到待加入页，不能进入业务菜单，不能看见租户 `0` 的业务数据。

创建租户成功之后，创建者成为该租户的管理员，这只发生在「创建」这条动作上，不发生在注册或接受普通邀请上。

## 7. 场景

### 7.1 公开注册

开关 `register.enabled`。种子今天是 `true`。受控部署应设为 `false`。新装种子是否改成 `false` 见待确认项 R1。已有库不在未确认时被改写。

关闭：`POST /auth/register` 仍返回 `REGISTRATION_DISABLED`。登录页不显示注册入口。直接打开 `/auth/register` 时展示未开放，不提交。

打开：表单仍是用户名、密码、确认密码、协议。不增加手机、邮箱、验证码字段。成功后的去向见第 11 节。

### 7.2 组织邀请注册

发邀请的人必须是**当前会话租户**里角色码为 `admin` 的成员。服务端用该租户的角色行判断，不看别的租户上的 `admin`，也不把平台 `super_admin` 或部门管理员算进来（D1）。部门管理员发邀请不在首期，也不预留开关。

收件人：

1. 未登录：页面同时给出登录和注册。注册或登录成功后回到同一个 `token`，不进首页。
2. 已登录：可以接受或拒绝。页面可以显示这封邀请当初发给哪个邮箱，仅作提示。邮箱不一致、账号还没有邮箱，都不阻断（D2）。不要求先改邮箱。

首期的接受凭据是 token 持有，不是邮箱所有权。转发链接的人在过期、接受、拒绝或撤销之前都能用自己的登录账号接受。有效期、单次、撤销和审计用来限制这件事，不把它说成防转发。若以后业务要求必须是受邀邮箱本人，再单独立项做邮箱验证。首期不接 OTP，也不新选短信或邮件厂商。

### 7.3 已有账号加入、申请、切换

- 加入：接受邀请，或（申请开关打开时）审批通过。
- 申请：`register.allowApply`，首期推荐默认 `false`。关闭时接口直接拒绝，不查询租户编码。
- 切换：已有 `POST /org/switch`。只允许有效 `TENANT` 成员和超管。切换后按第 16 节重算权限上下文。

### 7.4 受控部署（含政务）

这是开关组合，不是新领域：

| 开关 | 受控部署 | 允许自助开租户的部署 |
|------|----------|----------------------|
| `register.enabled` | 按需。人员也可以由管理员建账号，而不开放注册 | 按需 |
| `register.allowCreateOrganization` | `false` | 显式打开才允许 `POST /org` |
| `register.allowApply` | `false` | 按需 |

机关租户由已有权限 `tenant:management:create` 的管理员创建，初始管理员来自现有租户管理表单。人员通过邀请进入，不能自己注册出一个县局。底座不出现机关、税种、区划模型。部门树仍然用现有的分公司 / 部门 / 用户组字典，由实施方自己命名节点。

## 8. 首期推荐、替代方案、待确认

### 8.1 首期推荐（确认后才实现）

1. 三个开关都进现有 `register` JSON，不新建配置表。`allowCreateOrganization`、`allowApply` 缺省按 `false`。已存在的 `enabled` 不强制改库。
2. 公开注册不再传入部门 `1`，不再解析 `general` 或 `defaultRoleId`。`department_id` 允许为空（见第 17 节）。不写角色，不写 `TENANT` 成员。
3. 注册成功仍签发会话，进入待加入页。邀请链接的 `redirect` 优先。
4. 无租户会话不落到租户 `0`。拒绝落点只放在 `ApiSignConfig` 的 `checkLogin` 之后（B2），不启用未挂载的 `SaExtensionInterceptor`。
5. `POST /org` 增加 `allowCreateOrganization` 判断。平台 `POST /tenant/management` 不变。
6. 接受邀请写入该租户的 `TENANT` 成员。部门可选，且必须属于该租户。角色只有 `invite_grantable=true` 时才能写入（B1）。空角色可以加入（评审倾向 R6，未最终确认）。禁止仅靠角色码黑名单。邀请带角色的实现排在阶段 3，且阶段 2 的权限裁剪必须先完成（B3）。
7. 不调用 `replacePrimaryDepartment`。部门成员按「该租户一条主部门」新增。这依赖第 17 节把 `sys_membership.tenant_id` 写成真实租户。
8. 补邀请撤销。重发继续更换哈希，并改掉「旧链接仍可用」的注释。
9. 切换租户时重算当前租户的角色、数据范围和部门。权限 SQL 增加 `sys_role.tenant_id = 会话租户`。`super_admin` 仍走现有全权限短路，不靠这条过滤。
10. 注册传输已在 `c0131477` 落地：原密码经 HTTPS 上传，后端做现有密码策略和 `PasswordEncoder.encode`。不改个人改密和平台建租户管理员仍在用的 RSA。
11. 注册接口复用现有 `@RateLimiter`。成功和失败走现有登录审计，审计不记密码和 token。
12. 公开注册的冲突文案依 R4 / 待确认：统一一句，审计留真实原因。本轮没有把它升成已批准。

### 8.2 替代方案（不作为首期承诺）

| 编号 | 方案 | 不放进首期的原因 |
|------|------|------------------|
| A | 注册不签发会话，只回登录页 | 改动小，但邀请注册要多一次登录。若评审选它，第 11 节改走备选 |
| B | 每个租户一行 `sys_user`，同名用户互不关联 | 与已经在用的「一个 user_id 多条 TENANT 成员」相反，邀请接受要改成建新用户 |
| C | 去掉 `sys_user.tenant_id`，用户名全库唯一，登录不依赖租户上下文 | 这是身份模型重做。要单独评审，本文不承诺 |
| D | 首期就做邮箱或短信 OTP | `OtpScene.REGISTER` 和邮件配置已经存在，但注册没接。厂商、模板、验证码是否算注册完成条件都未确认 |
| E | 接受邀请时自动复制一套「普通成员」角色和菜单 | 容易把套餐菜单或 `admin` 带进去。首期宁可不给菜单 |
| F | 成员表继续不写 `tenant_id`，部门仍只留 `sys_user.department_id` | 更小，但一个人不能在两个租户各有主部门。切换后部门上下文会错 |

### 8.3 待确认

截至基线 `8c260136`，下表没有一项已有代码。种子里 `register.enabled` 仍是 `true`，这是现状，不是 R1 已经选定。

三列不要混：

| 标记 | 含义 |
|------|------|
| 已确认 | 用户已经明确说过。目前只有写作原则，以及更早确认的「写入用户名 4–64」「自助注册不再 RSA」 |
| 评审倾向 | 本轮评审写明的倾向，不是逐项最终批准 |
| 文档推荐 | 本文据此写成的设计。未标成已确认之前，不能当作用户已经批准实施 |

| ID | 评审倾向 | 文档推荐 | 确认 |
|----|----------|----------|------|
| R1 | 新装种子里公开注册改为 `false`；已有库不动。产品作者可以把这写成建议 | 跟随该倾向，作为建议，不改已有库的 `enabled` | 未最终确认。种子现状仍是 `true` |
| R2 | 注册成功自动签发会话；待加入页；邀请 `redirect` 优先 | 跟随 | 未最终确认 |
| R3 | 成员 `tenant_id` 落库和每个租户一条主部门，是邀请授予角色之前的必需前置 | 跟随。见 B3 | 未最终确认 |
| R4 | 公开注册失败用统一文案，审计保留真实原因 | 跟随，但凡依赖这句文案的正文都标「依 R4 / 待确认」 | 未最终确认。本轮不把它升成已批准 |
| R5 | 申请默认关闭则不做审核列表；一旦打开，列表同一批交付 | 跟随 | 未最终确认 |
| R6 | 加入时可以没有菜单，之后再授权 | 跟随 | 未最终确认 |
| R7 | 只按会话里的 userId，在账号行所属租户上下文读本人。禁止用请求参数或用户名跨租户查，并加测试 | 跟随 | 未最终确认 |
| R8 | 没有其它租户时回待加入页，账号仍可登录 | 跟随。选择下一条主租户的规则见 C5 | 未最终确认 |

### 8.4 本轮评审处理（B / D / C）

这些是本轮要写进设计的决定。都还没有代码。合入文档不等于开工。

| 编号 | 处理 | 文档推荐（单一方案） |
|------|------|----------------------|
| B1 | 采纳。不再用「排除 `super_admin` / `admin`」当授权规则 | 显式白名单。见下文「邀请可授予角色」 |
| B2 | 采纳。不启用未挂载的 `SaExtensionInterceptor` | 无租户默认拒绝。落点只选 `ApiSignConfig` 里 `checkLogin` 之后。见第 12 节 |
| B3 | 采纳。阶段重排 | 权限按会话租户裁剪、成员有效、会话重建、R3 的 `tenant_id` 与主部门唯一，全部在「邀请可以带角色」之前完成 |
| D1 | 采纳 | 首期只有当前租户的 `admin` 能发邀请，服务端校验。部门管理员发邀请不是目标 |
| D2 | 采纳，并选定一种 | 首期凭 token 持有接受。邮箱不一致只提示，不阻断，不要求改邮箱。这不承诺防转发 |
| D3 | 采纳 | 账号事务成功后签发失败，提示「注册成功，请登录」。邀请先提交再发信。重发轮换哈希。冷却内不轮换 |
| D4 | 采纳 | 唯一冲突在事务方法外面捕获并映射，回滚中的事务里不再查、不再写 |
| C1 | 采纳 | R4 的对外文案一律带「依 R4 / 待确认」 |
| C2 | 采纳 | 匿名页只调 `GET /auth/register/options`。`GET /system/config/register` 加上 `system:config:get`，管理端同一批改掉 |
| C3 | 采纳为设计建议值，不是现有配置 | 见第 15.2 节。不引用 OTP 上限 |
| C4 | 采纳 | 过期但仍是 `status=0` 的行在重发时就地更新，不先删除 |
| C5 | 采纳 | 用已有 `joined_at`，再按 `scope_id`。没有「最近使用」字段，不新增 |
| C6 | 采纳 | 路径继续叫 `/org`。正文验收写「租户」。部门树不是租户树 |

#### 邀请可授予角色（B1）

现状：`acceptInvite` 不分配角色。若按旧草案只禁止角色码 `super_admin` 和 `admin`，租户里自建的、菜单已经配满的角色仍能被邀请发出去。那条黑名单不够。

首期只做显式白名单，不并列「权限不超过邀请人」的第二套规则。

拟增加、本文件不写迁移：`sys_role.invite_grantable boolean NOT NULL DEFAULT false`。默认每个角色都不可通过邀请授予，包括 `admin`、`super_admin` 和自建角色。

谁可以把它改成 `true`：只有持有独立权限 `system:role:inviteGrant` 的人，走角色维护接口，不走 `POST /org/invites`。这个权限不因为「能发邀请」而拥有，也不放进租户初始化时 `admin` 角色的套餐菜单。邀请人不能在发邀请时顺手打标。

服务端三处都读这个标志，不看角色码黑名单：

1. 创建或重发邀请时，`roleId` 为空则不授予。非空则必须属于目标租户，且 `invite_grantable` 为 true，否则 `ORG_INVITE_ROLE_FORBIDDEN`，不发出邀请。
2. 角色的菜单、数据范围或 code 在一次事务里发生变化时，把该角色的 `invite_grantable` 清回 false。要再允许，维护者必须重新打开。
3. 接受邀请时再读一次。标志已关、角色已删或不属于该租户：整单拒绝，不写成员、不写 `sys_user_role`。避免先加入再漏授角色。

#### 无租户拦截落点（B2）

现状：`TenantInterceptor` 的顺序是 `Ordered.HIGHEST_PRECEDENCE + 100`，`ApiSignConfig` 里登录拦截是 `+200`。租户上下文更早建立。`TenantSelection` 在会话租户为 null 时会使用 `X-Tenant-Id` 或默认租户 `0`。`ApiSignConfig` 对非 `/open/**` 调用 `StpUtil.checkLogin()`，排除路径来自 `web.security.excludes`（静态页、文档、健康检查）。`SaExtensionInterceptor` 没有 `addInterceptor`，不能当落点。

设计只改两处，都还没写代码：

1. `TenantSelection`：请求已经登录，且会话租户 id 为 null 时，不要落到 `0`，也不要采用 `X-Tenant-Id`。匿名请求仍按现在的规则（注册和登录查号用默认租户）。
2. 在 `ApiSignConfig` 的同一个 `SaInterceptor` 回调里，`checkLogin` 通过之后，若 `StpUtil.isLogin()` 为真且会话租户为 null，再查方法加路径白名单。不在名单里：`response.setStatus(403)`，正文 `Result.code` 为 `"403"`，message 为「您当前没有访问该租户的权限」。

未登录不进这道判断，因此 `/auth/login`、`/auth/register`、`/captcha`、`auth/otp`、`GET /org/invites/preview` 这些现有匿名接口不会被它拦住。`excludes` 里的静态资源根本不进这个拦截器。

白名单是一份方法加路径的清单，集中放在一个类里。没写进去的新接口，对无租户登录会话默认 403。`GET` 允许不等于 `POST` 允许。

| 方法与路径 | 放进名单的理由 |
|------------|----------------|
| `POST /auth/logout` | 无租户时也要能退出 |
| `GET /auth/user` | 现有守卫登录后会拉它。无租户时只返回本人资料和空菜单，不因此放开 `/system/menu` |
| `GET /org/session` | 待加入页要看自己有没有租户 |
| `GET /org/invites/preview` | 已登录打开邀请链接。匿名访问仍靠现有 `@SaIgnore`，不靠这份名单 |
| `POST /org/invites/accept` | 接受邀请 |
| `POST /org/invites/reject` | 拒绝邀请 |
| `POST /org/applications` | 待加入页的申请。服务里仍看 `allowApply`，关则 `ORG_APPLY_DISABLED` |
| `POST /org` | 待加入页的创建。服务里仍看 `allowCreateOrganization`，关则 `ORG_CREATE_DISABLED` |

不放入：`/system/menu`、字典、用户列表、`/user/profile/email`（D2 不再要求先改邮箱）、`POST /org/switch`（接受和创建在服务端自己切换；租户仍为空时调用 switch 应 403）。`GET /auth/register/options` 是匿名接口，不属于这份已登录名单。

验收：扫一遍现有 Controller 的映射。未登录打匿名接口应成功；未登录打业务接口仍是 401。已登录且无租户，只有上表返回非 403；同一路径换一个未列出的 HTTP 方法也是 403。之后新增的映射默认拒绝。

#### 阶段顺序（B3）

原先把「邀请带角色」放在权限裁剪和 `membership.tenant_id` 之前，中间会有一段能写上角色、但读权限仍不看租户的窗口。取消这个顺序。

| 阶段 | 进入条件 | 做完才算离开 | 明确不做 |
|------|----------|----------------|----------|
| 0 | 本文评审 | 没有代码 | 不把 R1–R8 当成已批准 |
| 1 | 阶段 0 之后另有开工指令 | 注册不再写部门 `1` 和任何角色；`department_id` 可空；匿名 `GET /auth/register/options`；管理端 `GET /system/config/register` 改为要 `system:config:get` 且前端改调用；无租户不回落 `0`，并在 `ApiSignConfig` 做默认拒绝；注册限流用第 15.2 节的建议值；登录审计。注册签发失败的文案按 D3 | 不发带角色的邀请。不做 OTP |
| 2 | 阶段 1 已在测试里证明：无租户访问 `/system/user` 为 403，且权限查询只返回会话租户的角色 | 权限读路径（角色码、权限码、菜单、`LoginUser.roleDataScopes`）按会话租户和有效成员裁剪；`switchOrg` 重算会话；`sys_membership.tenant_id` 按 R3 写入；主部门唯一按租户生效。验证不只一条 `role.code` SQL，还要看菜单、数据范围和另一个租户的角色行 | 邀请请求里的 `roleId` 即使传入也拒绝授予 |
| 3 | 阶段 2 的裁剪和 `tenant_id` 已生效 | 邀请、撤销、重发、发信在事务外、白名单角色。接受时再次核验 `invite_grantable` | 部门管理员发邀请。申请审核列表（除非届时 R5 已确认且开关打开） |
| 4 | 阶段 3 完成 | 第 18 节里尚未被现有单测覆盖的案例 | 用 `ignore` 按用户名扫全库 |

阶段 2 没完成时，邀请接口可以存在，但不得写入 `sys_user_role`。

#### 发信、重发和唯一冲突（D3、D4、C3、C4）

注册：插入用户的事务提交成功之后再签发会话。签发抛错时，接口 message 为「注册成功，请登录」，不报注册失败，也不再插入第二行。

邀请：事务只写邀请行。提交成功后再发信。不承诺信恰好送到一次。

重发：

- 同一租户、同一规范化邮箱、`deleted=0` 且 `status=0` 的行只有一条。过期了但状态仍是 0 的，也是这一行。重发就地改 `token_hash` 和 `expire_time`，不先删除。
- 建议冷却 60 秒（设计建议，现有代码没有这个配置）。冷却内再次点击：不轮换哈希、不再发信，返回请稍后再试。
- 冷却结束后的重发会轮换哈希，旧链接失效。这是取舍：若 SMTP 超时但信后来仍送达，旧链接已经无效。
- 两个并发重发只允许一个更新成功。另一个看到哈希已变或仍在冷却内，不得再换一次哈希。

唯一冲突：`registerLocal` 保持事务。捕获数据库唯一冲突的代码放在这个事务方法的调用方，在事务已经回滚之后映射成对外错误。依 R4 / 待确认：公开接口用统一文案，不在回滚中的事务里再查用户名或再插入。管理端创建用户仍可返回现有「用户名已被占用」。

#### 主租户与会话（C5）

`Membership` 已有 `joinedAt`、`scopeId`、`isPrimary`、`status`。没有最近使用时间，不新增这种字段。

主租户是 `is_primary=true` 且仍然有效的那条 `TENANT` 成员。当前会话是 `LoginUser.tenantId`。两者分开。

停用一条 `TENANT` 成员之后，若它原来是主租户：在其余有效成员里，按 `joined_at` 升序，相同则按 `scope_id` 升序，选第一条改成主租户。若被停用的是当前会话租户：会话改到这条新主租户并重算权限；一条都不剩则会话租户清空，进入待加入页，不写成 `0`。被停用的既不是主租户也不是当前会话：只改状态。

#### 术语（C6）

浏览器路径保持 `/org/**`，因为现有 `OrganizationController` 就是这些路径。表和验收用例写「租户」。`/org` 表示租户空间，不表示部门树，也没有「租户上下级」这棵树。部门上下级只在 `sys_department`。

## 9. 账号唯一性与同一身份多租户

目标：同一个人用一个账号加入多个租户。查重和登录查找不得绕开租户隔离去扫账号。

当前约束：

- 唯一索引是 `(username, tenant_id)`，邮箱和手机同样。
- 匿名请求的租户上下文是默认租户 `0`（没有 `X-Tenant-Id` 时）。
- `existsUsername` 只查这个上下文。
- 登录按用户名查找也必须只在这个上下文里命中。公开登录页现在不选租户。
- 邀请接受把 `TENANT` 成员加到当前 `user_id` 上，不新建 `sys_user`。

首期增量：

- 自助注册的那一行继续落在默认租户上下文。并发下由 `(username, tenant_id)` 挡住同名。应用先查再插入，并捕获唯一冲突，避免 500。
- 同一个人加入其它租户：只增加成员和该租户内的角色关联，不复制用户行，不用 `TenantContextHolder` 的 ignore 按用户名搜索。
- 业务租户里由「租户管理 / 用户管理」另建的同名用户，仍是另一行。公开登录看不见他。这是现有模型。首期不合并这两行，也不做登录页选租户。
- 方案 C（全局用户名）不做。因此「任意租户里用户名全局唯一」不是首期验收条件。首期验收的是：默认租户上下文内不能有两行同名；加入第二租户后仍然是同一个 `user_id`。

读取自己的资料：切换到业务租户后，租户插件会给 `sys_user` 加上会话租户条件，而账号行的 `tenant_id` 仍是 `0`。按代码路径，`selectById` 可能看不到自己。这还没有在本环境跑出来，所以写成验收项 G1，不写成已观测故障。

R7 的做法：已登录用户读自己的账号、改自己的密码或邮箱时，用会话中的 userId，在**该行记录的** `tenant_id` 上执行现有 `TenantUtils.execute`。不接受请求体里的用户名作为查找键。部门、角色、业务数据仍用会话中的组织租户。这和「忽略插件，按用户名扫全部租户」不是同一件事。

## 10. 状态机

### 10.1 注册

```text
开关关闭 → REGISTRATION_DISABLED，不写库
校验失败 → HTTP 400，不写库
策略不符或解密失败 → 现有密码类错误码，不写库
同名（含并发唯一冲突）→ 公开接口统一文案，不写第二行
成功 → 一行启用用户；无角色；无 TENANT 成员；department_id 为空
```

成功之后账号状态用现有 `StatusEnum`：`ENABLE(1)` / `DISABLE(2)`。禁用是整账号不能登录，与某个租户成员停用分开。

### 10.2 邀请

| 状态 | 值 | 进入 | 离开 |
|------|----|------|------|
| 待处理 | 0 | 创建 | 接受、拒绝、过期、撤销、被重发替换哈希 |
| 已接受 | 1 | 接受成功 | 终态 |
| 已拒绝 | 2 | 拒绝 | 终态。同一邮箱可以再发新邀请 |
| 已过期 | 3 | 超过 `expire_time` 后被访问或被列出时写回 | 终态。可另发新邀请 |
| 已撤销 | 4 | 管理员撤销 | 终态 |

接受条件：状态仍是待处理、未过期、未删除、目标租户仍存在且启用、调用者已登录。不要求账号邮箱与邀请邮箱相同（D2）。若邀请带了角色，接受时 `invite_grantable` 仍须为 true（B1）。同一邀请并发接受时，只允许一条 `status=0` 的更新成功，另一条得到 `ORG_INVITE_HANDLED`。

重发：见第 8.4 节。过期但仍为 `status=0` 的行就地更新，不先删除（C4）。冷却内不轮换哈希。

`MembershipStatus.INVITED` 继续不参与邀请主流程。邀请的生命周期在 `sys_org_invite`。成员行在接受时直接是 `NORMAL`。

### 10.3 申请

| 状态 | 值 | 首期 |
|------|----|------|
| 待审核 | 0 | 仅当 `allowApply=true` |
| 已通过 | 1 | 写 `TENANT` 成员。角色、部门规则与邀请相同：不授予 `admin` |
| 已拒绝 | 2 | 不写成员 |
| 已撤销 | 3 | 枚举已有。首期仅在 R5 把申请打开时，由申请人撤销自己的待审记录 |

开关关闭时不插入申请。

### 10.4 租户成员

```text
不存在 → 接受邀请或审批通过 → NORMAL
NORMAL → 管理员停用 → DISABLED（该租户不可见、不可切换）
DISABLED → 管理员恢复 → NORMAL
```

停用不改 `sys_user.status`，不删账号，不删其它租户的成员。现有 `ensureTenantMembership` 会把非 `NORMAL` 改回 `NORMAL`，首期停用之后不能再走这个方法把人自动恢复。恢复要单独的显式操作。

## 11. 界面与会话时机

页面沿用认证范式 P-AUTH（`docs/conventions/ui-patterns.md`）和现有 `/org/setup`、`/org/invite`。视觉以 `frontend/DESIGN.md` 为准，本文不新开一套认证皮肤。

### 11.1 推荐流程（R2）

```text
公开注册成功
  → 签发会话（tenantId 为空，无业务角色）
  → 若带邀请 redirect：打开 /org/invite?token=
  → 否则打开 /org/setup
  → 不打开 dashboard

/org/setup（无租户）
  → 默认页是「待处理邀请 / 输入已有邀请」
  → 「创建组织」仅当 allowCreateOrganization
  → 「申请加入」仅当 allowApply
  → 没有可用动作时，说明需要等待管理员邀请

邀请页
  → 未登录：登录或注册，两者都带回 token
  → 已登录：预览组织名与过期状态，接受或拒绝
  → 接受成功：switch 到该租户，再拉 /auth/user，进入首页
  → 拒绝：若仍无租户，回到 /org/setup

已有多个租户
  → 现有组织切换器
  → 切换成功后整页刷新（当前 org-switcher 已 reload）
```

会话在「账号已成立」时签发，不在「已加入租户」时才签发。无租户会话不能访问用户管理、部门、租户管理、配置写接口。

### 11.2 备选（若 R2 选不签发）

注册接口返回成功而不返回 token。前端去登录页。邀请场景在登录成功后才按 `redirect` 进入邀请页。待加入页的全部接口仍要求登录。其余数据规则不变。

### 11.3 表单

| 页面 | 字段 | 不收集 |
|------|------|--------|
| 注册 | 用户名、密码、确认密码、协议 | 邮箱、手机、验证码、租户名、部门 |
| 邀请预览 | 组织名称、状态、过期时间 | 原始 token 回显、完整邮箱（见第 15 节） |
| 接受邀请 | 登录后接受或拒绝。可看到邮箱提示 | 不要求先把账号邮箱改成邀请邮箱 |
| 管理员发邀请 | 邮箱，可选部门 id、可选角色 id | 不填初始密码。新用户自己注册 |
| 创建组织 | 现有名称、描述。受开关控制 | 不在注册页上出现 |
| 申请 | 现有组织编码、说明。受开关控制 | 关闭时连编码输入也不提交 |

确认密码只在前端校验。后端继续不接收 `confirmPassword`。写入用户名已是 4–64（含两端），前后端正则都是 `{3,63}`。字符类保持现有实现。长度口径见 auth 总设计。

## 12. API 契约草案

成功响应保持现在的习惯：登录和注册直接返回 `AccessTokenResponse`，不是包一层 `Result`。业务失败仍是 `Result.code` + `Result.message`，HTTP 422。校验失败 HTTP 400。

### 12.1 已有，首期改变行为

| 接口 | 认证 | 行为变化 |
|------|------|----------|
| `POST /auth/register` | 匿名 | 仍受 `enabled` 控制。不分配部门 `1` 和任何角色。原密码慢哈希。限流用第 15.2 节的建议值。写审计。冲突文案依 R4 / 待确认 |
| `GET /system/config/register` | 登录，且补上 `system:config:get` | 现有路径就是这个。管理端配置页继续用它。匿名页面不要再调它（C2）。响应可多两个布尔字段 |
| `PUT /system/config/register` | `system:config:edit` | 可写两个新布尔。忽略或拒绝把 `verifyEmail` / `verifyPhone` 设成「已生效」的语义：首期即使为 `true` 也不执行验证 |
| `POST /org` | 登录 | 增加 `allowCreateOrganization`，否则 `ORG_CREATE_DISABLED` |
| `POST /org/applications` | 登录 | `allowApply=false` 时 `ORG_APPLY_DISABLED`，不查编码 |
| `POST /org/invites` | 当前会话租户的 `admin`（D1）。不是部门管理员，也不因 `super_admin` 放行 | 可选 `departmentId`、`roleId`。`roleId` 必须已是 `invite_grantable`。返回体不含原始 token |
| `GET /org/invites/preview` | 匿名可调用；页面在未登录时也可以看组织名 | 不返回邮箱明文，不返回 token |
| `POST /org/invites/accept` | 登录 | 不要求邮箱一致。角色只在接受时仍可授予时写入。条件更新状态 |
| `POST /org/switch` | 登录成员或超管 | 重算角色、数据范围、部门并写回会话 |
| `GET /auth/user` | 登录 | 角色与权限按第 16 节的当前租户计算 |

已有错误码继续使用：`REGISTRATION_DISABLED`、`USERNAME_EXISTS`（仅管理端或 R4 选择公开区分时）、`PASSWORD_*`、`ORG_NOT_MEMBER`、`ORG_ALREADY_MEMBER`、`ORG_INVITE_NOT_FOUND`、`ORG_INVITE_EXPIRED`、`ORG_INVITE_HANDLED`、`ORG_INVITE_EMAIL_MISMATCH`、`ORG_EMAIL_REQUIRED`、`ORG_FORBIDDEN`、`ORG_APPLICATION_PENDING`、`ORG_APPLICATION_NOT_FOUND`、`ORG_APPLICATION_HANDLED`、`ORG_PACKAGE_MISSING`、`ORG_CODE_INVALID`。

`ORG_CODE_INVALID` 首期不再把编码拼进 message。编码不存在与开关关闭使用不同错误码：关闭是 `ORG_APPLY_DISABLED`，打开后才可能是 `ORG_CODE_INVALID`。

### 12.2 拟新增

| 接口 | 认证 | 说明 |
|------|------|------|
| `GET /auth/register/options` | 匿名 | 登录页和注册页改调这个，替代它们对 `GET /system/config/register` 的需求。只返回三个布尔。管理端仍用原来的已登录接口，并在同一批加上 `system:config:get`（C2） |
| `POST /org/invites/{id}/revoke` | 该邀请所属租户的 `admin` 或超管 | 仅待处理可撤销，状态改为 4 |
| `GET /org/applications` | 该租户 `admin` 或超管 | 仅 R5 要求申请进入首期时才做。分页沿用现有 `page` / `pageSize` |
| `POST /org/members/{userId}/disable` 与 `.../enable` | 该租户 `admin` 或超管 | 只改这个租户的 `TENANT` 成员状态。不能操作自己的唯一超管。不能借此修改别的租户 |

无租户已登录白名单以第 8.4 节 B2 的表为准，按方法加路径维护。当前没有任何拦截器在执行它。不要把匿名登录、注册和 `web.security.excludes` 里的静态资源算进这道 403。

### 12.3 拟新增错误码

放在现有 `AuthenticationException` 或 `OrgException` 的静态工厂里，不直接 `new BizException`。

| code | 何时 | 调用方文案 |
|------|------|------------|
| `REGISTER_UNAVAILABLE` | 依 R4 / 待确认：公开注册无法完成（含同名） | 依 R4 / 待确认：「无法完成注册，请更换用户名或稍后再试」 |
| `REGISTER_RATE_LIMITED` | 限流 | 请求过于频繁，请稍后再试 |
| `ORG_CREATE_DISABLED` | 自助创建关闭 | 当前不允许创建组织 |
| `ORG_APPLY_DISABLED` | 申请关闭 | 当前不允许申请加入 |
| `ORG_INVITE_REVOKED` | 已撤销 | 邀请不存在或已失效 |
| `ORG_INVITE_ROLE_FORBIDDEN` | 角色不属于该租户，或 `invite_grantable` 不是 true（B1）。不是只看角色码 | 不能使用该角色 |
| `ORG_INVITE_DEPARTMENT_FORBIDDEN` | 部门不属于该租户 | 不能使用该部门 |
| `ORG_MEMBER_DISABLED` | 成员已停用仍要切换 | 你在该组织中已被停用 |

撤销后的预览与「找不到」使用同一句「邀请不存在或已失效」，避免用错误码告诉外人这封邀请曾经存在。`ORG_INVITE_REVOKED` 只写审计。若实现时不便拆两个码，对外统一 `ORG_INVITE_NOT_FOUND` 即可。

## 13. 数据、并发、事务、默认角色与部门

### 13.1 注册写入

一个事务：

1. 在默认租户上下文检查用户名并插入 `sys_user`。
2. `status=ENABLE`，`is_builtin=false`，`displayName=username`，密码是提交的原密码的慢哈希，`department_id` 为 null。
3. 不插入 `sys_user_role`，不插入 `sys_membership`。

事务提交之后再签发会话。签发失败时账号保留，对外 message 为「注册成功，请登录」（D3）。不报注册失败，不插入第二行。

唯一冲突在事务外层映射（D4）。依 R4 / 待确认时，公开接口不返回 `SYSTEM_ERROR`，也不返回可区分的「用户名已被占用」。

### 13.2 邀请与成员

邀请行：`tenant_id`、规范化邮箱、`token_hash`、状态、邀请人、过期时间。可选 `department_id`、`role_id` 可空。

接受时的一个事务：

1. 条件更新邀请：`status` 从 0 到 1，写入 `accepted_user_id`。更新行数不是 1 则失败。
2. `ensureTenantMembership`。若该租户成员已是 `DISABLED`，不得自动改回 `NORMAL`。
3. 若有部门：插入该租户的主部门成员。`scope_id` 是部门 id，`sys_membership.tenant_id` 是该租户 id。
4. 若有角色：当时 `invite_grantable` 仍为 true，才写 `sys_user_role` 并双写 `ROLE` 成员。否则整单失败。不使用角色码黑名单。
5. 不修改其它租户的角色和部门。
6. 事务提交后再 `switchOrg` 并重算会话。

审批通过使用第 3–4 步的同一套规则。申请行上首期不增加角色字段；通过后不分配角色，部门留空，直到管理员分配。避免审批接口变成第二条授予管理员的路。

### 13.3 不得授予的权限

| 动作 | 允许 | 禁止 |
|------|------|------|
| 公开注册 | 无角色 | `general`、`admin`、`super_admin`、部门 `1` |
| 接受邀请 / 申请通过 | 无角色，或该租户内 `invite_grantable=true` 的角色 | 未打标的角色，包括自建的满菜单角色；邀请人在请求里自行打标；其它租户的角色 |
| 自助创建租户（开关打开） | 沿用现有 `init`：该新租户内的 `admin` | 把这个 `admin` 变成 `super_admin`；写入别的租户的 `user_role` |
| 平台创建租户 | 现有租户管理 | 不在注册接口里调用 |

`general` 留在种子数据里，只是不再被注册使用。已有用户的 `general` 角色不批量删除。

### 13.4 并发与幂等

| 操作 | 约束 | 重复请求 |
|------|------|----------|
| 注册同名 | `(username, tenant_id)` | 一行成功。另一行在事务外映射。依 R4 / 待确认时用统一文案 |
| 发邀请 | 部分唯一：同租户、同邮箱、`status=0`、`deleted=0`。过期但未改状态的行也算这一条 | 冷却外就地更新哈希。冷却内不轮换（C3、C4） |
| token | `token_hash` 已有唯一索引 | 碰撞则重新生成。SHA-256 的十六进制是 64 字符，列长已是 64 |
| 接受 | 条件更新 `status=0` | 第二人 `ORG_INVITE_HANDLED` |
| 申请 | 已有待审部分唯一索引 | `ORG_APPLICATION_PENDING` |
| 成员 | 活跃唯一 `(tenant_id, user_id, scope_type, scope_id)` | `ensure` 已有则返回原记录，停用状态除外 |

发信移出数据库事务：先提交邀请，再发送。发送失败保留待处理邀请，管理员可重发（重发更换 token）。不把 SMTP 耗时放在行锁里。当前实现是同事务发信，失败整单回滚。首期改成提交后发送。

## 14. 邀请 token

- 生成：保持足够长的随机串（现有两个 `fastSimpleUUID` 拼接）。只在发信时出现一次。
- 存储：只存 SHA-256 十六进制。不存原文，不写日志，不放进 `GET` 预览响应，不放进邀请列表。
- 管理员发邀请的 HTTP 响应也不再带回原文。管理员需要链接时重新发送。
- 绑定：行上的 `tenant_id` + 邮箱。不能拿 A 租户的 token 加入 B。
- 单次：接受、拒绝、撤销、过期之后都不能再接受。
- 过期：默认仍 7 天，沿用 `OrgProperties.inviteExpireDays`。
- 撤销：仅邀请所属租户的 `admin` 或超管。撤销不删除行。
- 重发：旧哈希作废。
- 邮件正文可以含链接。日志里的邮箱沿用现有做法时要避免打印完整 URL。

预览可以带邀请邮箱的掩码，供页面提示。接受不做邮箱全等，也不因不一致失败（D2）。用户表邮箱仍经现有加密类型处理器读出，只用于提示，不用于放行。

## 15. 密码、限流、身份、枚举、审计

### 15.1 密码

- 传输：注册已不再使用 RSA（`c0131477`）。口令原样到达后端再哈希。不要再给注册补一层 RSA。
- 存储：只存 `PasswordEncoder` 结果。算法保持 DelegatingPasswordEncoder，不新增编码器。
- 策略：对收到的口令走现有 `assertCompliant`。注册不再先解密。
- 登录：本来就是原密码，这次没有改成 RSA，也没有取消别的 RSA。验收用同一口令再登录。个人改密、平台创建租户管理员的 RSA 仍在。
- 登录日志：`sys_login_log.username` 仍是 `varchar(50)`，changelog 没有加长它。51–64 位用户名写入登录审计会超出列长。这不是已完成的修复。若要修，用新 changeset，不在本文写迁移。
- 日志、审计、异常 message 都不含密码、私钥、token。

### 15.2 限流

`POST /auth/register` 使用现有 `@RateLimiter` 机制（`wyhao-starter.rate-limiter.enabled` 已为 true）。下面的次数是设计建议，不是现有配置，也不对照 OTP 的档位（C3）：

- 同一 IP：建议 5 次/分钟，并且 50 次/日。只数注册提交。
- 同一规范化用户名，且只在默认租户的注册查重上下文里：建议 10 次/小时。不和登录失败次数混用，也不按每个业务租户另计一套。
- 邀请重发：建议同一邀请行 60 秒冷却。冷却内不轮换 token。

触发注册限流返回 `REGISTER_RATE_LIMITED`，不说明是用户名维度还是 IP 维度。这些数字实现前可以再改配置，但不要写成代码里已经有的值。

登录已有失败次数、锁定和图验。注册不复用图验，除非 R 系列另有决定。首期不加图形验证码，避免把「未确认的验证码」写进注册完成条件。

### 15.3 枚举与错误反馈

| 入口 | 对外 | 对内 |
|------|------|------|
| 公开注册同名、策略失败以外的拒绝 | 依 R4 / 待确认：同一句「无法完成注册，请更换用户名或稍后再试」 | 审计 `realReason`：同名或唯一冲突 |
| 注册开关关闭 | `REGISTRATION_DISABLED`，可以明确说未开放 | 同文案 |
| 申请开关关闭 | `ORG_APPLY_DISABLED`，不查库 | — |
| 申请编码不存在 | 若开关打开：`ORG_CODE_INVALID`，message 不回显编码 | 可记编码 |
| 邀请 token 无效、过期、撤销 | 同一句「邀请不存在或已失效」 | 审计区分 |
| 邀请邮箱与账号邮箱不同 | 不返回错误，不阻断接受（D2）。页面可提示邀请上的邮箱 | 审计不把这当成失败 |
| 登录密码错误 | 保持现有「账号或密码错误」 | `realReason` 已存在 |

`GET /auth/register/options` 公开布尔值是产品开关，不是账号是否存在。

### 15.4 审计与脱敏

- 注册成功、注册拒绝：走现有 `AuthLogGateway` / `LoginEvent`。不要新日志表。
- 邀请创建、重发、撤销、接受、拒绝：走现有操作日志机制。记录租户 id、邀请 id、操作者。不记录 token、不记录完整链接。
- 邮箱在操作说明里只保留掩码（本地部分留首尾）。用户表里的邮箱继续走现有加密类型处理器。
- 邀请表的邮箱仍为明文，便于按邮箱找待处理邀请。不在列表 API 里把完整邮箱返回给非管理员。管理员列表可以看自己租户的邀请邮箱。

## 16. 停用、撤销、切换后的权限上下文

### 16.1 成员停用

管理员停用的是 `scope_type=TENANT` 且 `scope_id=当前租户` 的那一行，`status=DISABLED`。

效果：

- `isEffective` 为假，`/org/session` 不再列出该租户。
- `switch` 返回 `ORG_MEMBER_DISABLED`。
- 主租户和当前会话按第 8.4 节 C5 分开处理。没有其余有效租户时，会话清空并回待加入页（评审倾向 R8，未最终确认），不写成平台租户 `0`。
- 不 `kickout` 整账号，除非同时把 `sys_user.status` 设为禁用（那是现有用户管理，保持原样）。

恢复是显式的 enable，不经过 `ensureTenantMembership` 的自动改状态。

### 16.2 邀请撤销

见第 10.2 节与 `POST /org/invites/{id}/revoke`。撤销不影响已经接受并写入的成员。要去掉已经加入的人，用成员停用，而不是撤销历史邀请。

### 16.3 切换时重建上下文

`switchOrg` 在成员校验通过后：

1. 非超管：`setPrimaryTenant`。超管沿用现在的规则。
2. `LoginUtil.setTenantId(orgId)`，请求上下文改为该租户。
3. 角色 id 取 `sys_user_role` 连接 `sys_role`，且 `sys_role.tenant_id = orgId`。写回会话前，用这组角色重算 `LoginUser.roleDataScopes`（现有 `LoginContextApi.listRoleDataScopes` 的输入要收成这组角色，而不是用户的全部角色）。
4. `LoginUser.departmentId` 改为该租户主部门成员指向的部门；没有则为 null。`isDepartmentDisabled(null)` 今天在部门不存在时返回 false，空部门不拦截登录。
5. 前端继续 `fetchAuthInfo` 并刷新。`StpInterface` 的 SQL 与会话里的数据范围必须同一规则，避免界面菜单和接口鉴权不一致。

`hasRole("admin")` 因此只在当前租户真有这个角色时为真。用户在租户 A 是 `admin`、在租户 B 只是无角色成员时，切到 B 之后不能发 B 的邀请，也看不到 A 的菜单。

超管：`findUserPermissions` 已对 `super_admin` 返回 `*:*:*`。过滤业务租户角色时保留这条短路。超管切换仍可进入非成员租户，与现在 `switchOrg` 一致。

部门上下级：不增加任何「父部门可读子租户」的规则。数据范围继续只计算部门 id，且这些部门必须属于当前会话租户。

## 17. Liquibase 策略（只设计，不写迁移）

目录以磁盘为准：`backend/start/src/main/resources/db/changelog/postgresql/`。主文件 `db.changelog-master.yaml` 依次引入 `platform-schema.sql`、`platform-data.sql`、`platform-org.sql`、`platform-test.sql`。

规则：

- 不修改已经发布的 changeset（`wangyonghao:1`、`org-1`、`org-2`）。
- 新变更单独文件，例如 `platform-registration.sql`，新 changeset id，并在 master 末尾 include。
- 不在本草案的 PR 里添加该文件。

确认实现时预期的变更：

1. `sys_user.department_id` 改为可空。已有行不动。
2. `sys_org_invite` 增加可空 `department_id`、`role_id`。
3. 部分唯一索引：`sys_org_invite (tenant_id, email) WHERE deleted=0 AND status=0`。
4. 不改 `(username, tenant_id)` 唯一索引。
5. 不删除 `sys_user.tenant_id`。
6. 配置仍用 `sys_config.register` 的 JSON。不为两个新布尔建表。是否更新已有 JSON，取决于 R1，且只在确认后写 changeset。
7. Java 侧给 `SysMembershipDO` 补 `tenantId` 并在插入时赋值：`TENANT` 用 `scope_id`，`DEPARTMENT` 用部门行的 `tenant_id`，`ROLE` 用角色行的 `tenant_id`。成员表仍然不实现 `TenantedEntity`。这一步没有新的唯一索引，只是让现有 `uk_membership_active` 和主部门唯一索引按真实租户生效。历史行的 `tenant_id` 仍是 `0`。首期不写批量回填；旧数据的主部门约束保持原样。新插入的跨租户主部门必须带真实 `tenant_id`，否则仍会和默认 `0` 冲突。回填脚本若要做，另开评审，不塞进注册首期。

`.cursor/rules/liquibase.mdc` 仍写 `wyh-bootstrap/...`。那条路径在本提交不存在。实现时以 `backend/start` 下的 changelog 为准。

## 18. 验收案例

确认实现后，这些案例要能执行。本案只列出预期，不在本 PR 跑库。

| ID | 案例 | 预期 |
|----|------|------|
| G1 | 用户甲在公开注册后没有加入租户 | 能打开待加入页和 `/org/session`。`needsOrganization=true`。不能打开用户列表，也看不到种子部门「总公司」下的用户。个人资料仍能按会话里的 userId 读到自己 |
| G2 | 租户 A、B 各有管理员。甲只接受 A 的邀请，乙只接受 B 的邀请 | 甲的用户列表和部门树没有乙和 B 的部门。乙同理。甲 `POST /org/switch` 到 B 得到 `ORG_NOT_MEMBER` |
| G3 | 甲在 A 被设为 `admin` 后，又被邀请进 B 且不带角色 | 切到 B 后没有 A 的菜单，`hasRole("admin")` 为假，不能给 B 发邀请 |
| G4 | A 的部门树有上下级 | 上级部门成员不能因此读到 B 的任何行 |
| G5 | 同一用户名并发注册两次 | 只有一行 `sys_user`。另一请求不是 HTTP 500。公开文案按 R4 |
| G6 | 同一邀请并发接受 | 只有一次 `status=1`，一条 `TENANT` 成员 |
| G7 | 同邮箱并发发两封待处理邀请 | 只有一行待处理。后一次是重发，旧 token 不能再接受 |
| G8 | 注册事务在插入后、提交前失败 | 没有用户行，没有 token 可使用 |
| G9 | 发信失败 | 邀请行按第 13.4 节：已提交则可重发；不出现「邮件失败但旧 token 已失效且没有新信」之外的不可恢复状态。原始 token 不在日志里 |
| G10 | `enabled=false` | 接口 `REGISTRATION_DISABLED`，登录页无注册入口，直链注册页不能提交 |
| G11 | `allowCreateOrganization=false` | 待加入页没有创建。`POST /org` 为 `ORG_CREATE_DISABLED`。`POST /tenant/management` 在原权限下仍可创建 |
| G12 | `allowApply=false` | 不查编码。不返回该编码是否存在 |
| G13 | 无租户账号 | 注册行的 `department_id` 为空，`sys_user_role` 无记录，`TENANT` 成员无记录。角色码不是 `admin` 或 `super_admin` |
| G14 | 邀请指定未打开 `invite_grantable` 的角色，包括 `admin` 和自建的满菜单角色 | `ORG_INVITE_ROLE_FORBIDDEN`，不写 `user_role`，也不写成员 |
| G15 | 撤销后再用原链接 | 与无效 token 相同的对外文案，不加入 |
| G16 | 停用甲在 A 的成员，甲仍是 B 的成员 | 甲不能再进入 A，仍可进入 B。账号 `status` 仍是启用 |
| G17 | 注册后用同一原密码登录 | 登录成功。入库的是该原密码的慢哈希。PR #60 的单测已覆盖哈希与转发，整段 HTTP 仍待阶段 4 |
| G18 | 超管切换到 A | 仍有超管能力。普通成员不会因为超管规则拿到 `*:*:*` |

两租户隔离以 G2、G3、G4 为准。重复注册、邀请并发、回滚、开关、无租户账号分别是 G5、G6、G8、G10–G12、G13。

## 19. 实施阶段

顺序以第 8.4 节 B3 的表为准。这里不再保留「先邀请角色、后裁剪权限」的旧阶段 2 / 阶段 3。本文件合并不等于开工。

规格三件套 `requirements.md` / `plan.md` / `validation.md` 只是以后若要开阶段时的建议，本轮不拆。

## 20. 评审通过之后才发生的事

- 规格三件套以后可以再补。本轮不新增那三个文件。
- 决策稳定之后再补 ADR。现在选项还在 R1–R8，不写已决 ADR。
- 不改 `docs/mission.md`、`docs/tech-stack.md`、`docs/roadmap.md`。
- 不把本文的推荐方案当成已经上线的行为写入根目录 `CHANGELOG.md`。

确认方式：回复「确认」或「修改：…」。未确认不开始实现。
