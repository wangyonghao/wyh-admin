# 认证安全主 PRD — 现状分析

> 本文是 [《认证安全主 PRD》](./auth_主PRD.md) 的实现对照附件，不是实施授权，也不构成新的代码核实或实测结论。下文只复述拆分前主 PRD 原文已经写下的代码证据；本轮没有重新访问源码，也没有新的测试结果。
>
> 核对范围（原文）：只读远端提交 `e7aa1d52c0b9a67fd3240799c5c6c33478c7644e`（`docs: 用 stories 目录承接产品设计并收紧文档索引`）。云端工作区当时停在这个提交上。用户本机还有未提交改动，原文没有读取那些 WIP，也不代表它们已经核对。本附件沿用这一边界。
>
> 相对更早文档基线 `8c260136`，中间已有邮箱注册（`c43a401e`、`66da229d`、`ad392f61`）。`docs/stories/2026-11-08-auth/design.md` 与 `docs/stories/2026-11-08-user-registration/design.md` 仍自述 `8c260136`，不能当现状。
>
> | 档 | 含义 |
> |----|------|
> | 代码现状 | 上述提交里读到的行为（来自拆分前主 PRD 原文） |
> | 缺口 | 原文已写明的实现与观察限制，不是本轮新发现的漏洞证实 |
> | 需达目标 | 指向主 PRD 的规范性要求。需求目标本身不等于「当前漏洞已被证实」 |

关联子文档计划名在拆分前记录为当时为空、待补：`auth_注册表单.md`、`auth_登录页.md`、`auth字段清单.md`。

D-EMAIL 已于 2026-11-19（非洲/金沙萨）确认，但原文写明：当时只更新需求决定，未修改功能代码，不代表代码已按该决定修改。

## 1. 范围与入口的代码现状

端方面，原文只承诺对照当时已有 Web 管理后台及其 HTTP API，并写明不承诺 App、小程序当时已经共用。

| 流程 | 代码现状（原文） |
|------|------------------|
| 注册 | `POST /auth/register` 只受理发信。`POST /auth/register/verify` 消费 token 后才写 `sys_user`。成功都不签发登录 token |
| 密码登录 | `POST /auth/login?grantType=PASSWORD` |
| 退出 | `POST /auth/logout` → `StpUtil.logout()` |
| 改密 | `PATCH /user/profile/password`，旧新密码仍 RSA 解密 |
| 找回 | `forget-password.vue` 提交是空函数。无重置 API。两个拟新增找回接口都还没有 |
| 会话 | Sa-Token 1.44.0，`StpLogicJwtForSimple`，Redis `SaTokenDaoForRedisson`。无 refresh 接口 |

已有、原文要求本期不扩建也不删除的入口：`GET /oauth/{provider}` 与 `grantType=OAUTH`（JustAuth）；`grantType=OTP` 与 `POST auth/otp`。原文写明 MFA、企业 SSO、生物识别当时没有产品入口。

对应首期目标、后续项与上线阻断见 [主 PRD 第 1 节](./auth_主PRD.md)。

## 2. 身份与凭证的代码现状

### 2.1 字段与表

| 概念 | 代码现状（原文） |
|------|------------------|
| 登录名 | `sys_user.username varchar(64)`，唯一 `(username, tenant_id)`。注册由 `RegisterUsernameAllocator` 生成 `u` + 11 位，匹配 `RegexConstants.USERNAME`。登录可短于 4 |
| 邮箱 | 接口明文最长 64。列在 `platform-email-registration.sql` 改为 `varchar(512)` 存 AES Base64。唯一 `(email, tenant_id)`。`EncryptTypeHandler` 调用 Hutool 5.8.41 `SecureUtil.aes`，业务未指定模式或 IV |
| 手机 | 加密列，唯一 `(phone, tenant_id)`，可作登录标识 |
| 口令 | `sys_user.password`；待验证在 `sys_email_registration.password_hash` |
| 注册 token | `token_hash varchar(64)`，SHA-256 十六进制。原文在邮件 fragment |
| 失败次数 | 不在用户表。`PasswordFailGateway` |
| 邮箱验证证据 | 没有 `email_verified_at`，也没有邮箱版本号。待验证行用 `consumed_at` 表示链接是否用过。后台建号、改邮箱接口都不写「已证实所有权」 |

`sys_email_registration` 另有 `tenant_id`、`username`、`department_id`、`role_id`、`expires_at`、`send_status`、`sent_at`。确认前不写 `sys_user`。

用户持久状态取值：正常为 `sys_user.status = ENABLE(1)`，禁用为 `DISABLE(2)`。临时锁不是用户列，而是主体失败计数的 overlay。注销无单独产品流程；删除用户会 kickout。

### 2.2 口令实现

原文代码现状：`PasswordEncoderConfiguration` 使用 `PasswordEncoderFactories.createDelegatingPasswordEncoder()`，未传 cost。Spring Security 无参 `BCryptPasswordEncoder` 的 strength 是 10。本仓库没有断言 cost 的测试。注册和登录长度门都是 8–32（`EffectivePasswordRules.LOGIN_MIN_LENGTH/MAX` 与 `RegexConstants.PASSWORD`：至少一个数字、一个小写字母）。比较单位是 Java 正则的 UTF-16 代码单元，不是 UTF-8 字节。

`RegisterUsernameAllocator.containsName` 只在分配用户名时用，不是主 PRD 2.3 要求的完整「忽略大小写，口令不得等于用户名或邮箱本地部分」规则。

### 2.3 邮箱比较实现

`normalizeEmail` 只 trim，不改大小写。查重走 TypeHandler，比的是密文。`User@a.com` 与 `user@a.com` 是不同明文。

### 2.4 注册确认时的角色与部门写入

前置与写入（原文称为现状，并写明不是「零角色」已实现）：`register.enabled == true`；能解析默认角色（`defaultRoleId` 或角色码 `general`）。确认时写入部门 id `1` 和该角色。D-ROLE 未决，原文要求本期不改这个写入。

## 3. 各流程的代码现状

### 3.1 注册：步骤、原成功/失败返回、重发与发信失败

原文记录的步骤：

1. `POST /auth/register`，字段仅 `email`、`password`（`RegisterRequest`）。
2. trim 邮箱；口令按 8–32 门。
3. 分配用户名并写入 `sys_email_registration`（慢哈希、token 的 SHA-256）。事务提交后再 `MailClient.sendText`。
4. 邮件链接：`{publicBaseUrl}/auth/register/verify#t={token}`。确认接口要求 token 在 POST body（`EmailRegistrationVerifyRequest.token`）。
5. 用户在页面上再调用 `POST /auth/register/verify`。不要在打开链接时自动调用。
6. 成功插入一个 `sys_user`，待验证行写下 `consumed_at`。响应无登录 token。确认服务锚点：`EmailRegistrationService.confirm`。

原成功响应：`EmailRegistrationResponse`：`code=200`，`status=PENDING` 或 `COOLDOWN`，`delivered`，`resendAfterSeconds`，`expiresMinutes`。没有 access token。

原失败（原文明确这是代码现状；D-EMAIL 已确认改变对外目标，不代表代码已改）：

| 条件 | 对外（原返回） |
|------|----------------|
| 注册关闭 | `REGISTRATION_DISABLED` / 系统未开放注册 |
| 邮箱格式 | `EMAIL_INVALID` |
| 邮箱已在 `sys_user` | `EMAIL_EXISTS` / 邮箱已被占用。HTTP 422 |
| 口令不符合门 | `USER_PASSWORD_FORMAT_INVALID` |
| 发信失败 | `REGISTER_MAIL_UNSENT`。行标 `FAILED`，允许立即重试。原文亦写：注册发信失败直接返回未发出 / 仍返回邮件未发出 |
| 无待验证记录却重发 | `REGISTER_NOTHING_TO_RESEND`。HTTP 422 |
| 链接空、过期、找不到 | `REGISTER_LINK_INVALID` / 链接无效或已过期 |
| 已消费，或确认时邮箱已有账号 | 结果 `ALREADY_VERIFIED`，不建第二个用户 |

重发现状：默认链接 30 分钟（夹紧 5–1440），重发间隔默认 60 秒（夹紧 30–3600）。`SENT` 且未过间隔则 HTTP 200、`status=COOLDOWN` 且不换 token。`FAILED` 立即重试会换 token 哈希。`POST /auth/register/resend` 请求类型为 `EmailRegistrationResendRequest`；原文写明字段名以该类为准，不猜未读字段。

前端：注册页不调用 `encryptByRsa`。用户名重名对外走 `UserException.usernameExists`。邮箱重名走 `UserException.emailExists`。

已确认的统一受理目标（HTTP 200、`code=200`、`status=ACCEPTED`，不暴露是否已注册，且不以 `delivered` / `PENDING` / `COOLDOWN` 做状态探测）见 [主 PRD 第 3.1、8.1、10.1 节](./auth_主PRD.md)。不要把上表原失败返回当成目标契约。

### 3.2 密码登录

登录标识按代码顺序取 `username`、`email`、`phone` 第一个非空，加 `password`。请求字段见 `LoginRequest`：必填 `clientId`；需要图形验证码时加 `captchaUuid`、`captchaChallenge`。

原文对失败文案与锁定的观察：不存在的标识会计失败，文案相同，但是否与真实账号共用同一套锁定码未按虚拟主体写死。图形验证码默认失败 2 次后；配置 0 为始终，-1 为不要。账号失败窗口：锁定时长默认 30 分钟，种子 JSON 同为 5 次 / 30 分钟；观察窗口没有单独字段。登录失败有 IP 维，具体每分钟阈值不在种子 JSON。

成功响应：`AccessTokenResponse` 仅 `code` 与 `token`。

原失败码（原文写明不是目标码）：口令失败仍是 `PASSWORD_ERROR`；禁用仍可能是 `AUTH_ACCOUNT_DISABLED`；锁定是 `ACCOUNT_LOCKED`。

业务异常走 `AuthenticationException` → `GlobalExceptionHandler.handleBusinessException`，HTTP 422，body 为 `Result`，其中 `code` 是业务码，`message` 是文案。未登录走 `SaTokenExceptionHandler`，HTTP 401，`code` 为 `UNAUTHORIZED`。

目标统一失败码与虚拟主体锁定见 [主 PRD 第 3.2、4、8.1 节](./auth_主PRD.md)。

### 3.3 退出、改密、找回、会话

退出：`POST /auth/logout` 调用 `StpUtil.logout()`。未登录走上述 401。

改密：旧新密码仍 RSA 解密。原文未把 `credential_version` 记为已有字段；版本门在主 PRD 中是拟新增方案。

禁用账号：原文现状为禁用会 `kickout(userId)`。目标「已有 token 下一次 API 为 401」见主 PRD 2.2。

找回：页面提交为空函数，无重置 API，两个拟新增接口都还没有。

会话配置观察值：`timeout` 86400 秒，`active-timeout` 1800 秒，`auto-renew` true，`is-concurrent` true，`is-share` false，只读 `Authorization: Bearer`，不读 cookie。无 remember-me，无 refresh。主 PRD 第 4 节拟定首期不改这些数。

### 3.4 注册入口租户

公开注册在上下文为空时，`currentTenantId()` 取 0。原文写明这只决定新账号写到哪，不表示该请求被授权读该租户业务数据。

## 4. 已观察的配置与参数

下表只记录原文「代码现状」列与相关观察，便于对照主 PRD 第 4 节拟定值。拟定值本身不是已配置。

| 项 | 代码现状（原文） |
|----|------------------|
| 口令 | 8–32；cost 未单独配置，库默认 10；无断言 cost 的测试；长度比较单位为 UTF-16 代码单元 |
| 同账号口令 | 分配用户名时避免口令包含用户名（`containsName`），不是完整目标规则 |
| 账号失败窗口 | 锁定时长默认 30 分钟，种子 JSON 同为 5 次 / 30 分钟。观察窗口没有单独字段 |
| 锁定存储 | `PasswordFailGateway` |
| IP | 登录失败有 IP 维，具体每分钟阈值不在种子 JSON |
| 不存在的标识 | 会计失败，文案相同，但是否与真实账号共用同一套锁定码未按虚拟主体写死 |
| 图形验证码 | 默认失败 2 次后。0 为始终，-1 为不要 |
| 注册发送 | 重发 60 秒，链接 30 分钟（夹紧见 3.1）。失败可立即换 token。发信失败直接返回未发出 |
| 找回申请 / 校验 | 无 |
| 会话 | 见 3.3 观察值 |
| 通知重试 | 注册发信失败直接返回未发出 |
| 时序验收 | 无实测 |

## 5. 技术锚点与原文已写的现状偏差

技术栈观察值（原文）：父 POM `revision` 4.1.0-SNAPSHOT，Spring Boot 3.4.13，Sa-Token 1.44.0，Hutool 5.8.41。认证在 `admin-auth`，注册落库在 `admin-system`，契约在 `admin-system-client`。`spring-security-crypto` 只提供 `PasswordEncoder`。发信类：`top.wyhao.system.cmn.mail.MailClient`。短信 OTP 路径存在，找回首期不用。

Redis key 形如 `auth:{purpose}:{tenantId}:{hmac}` 是原文给出的设计形态。pepper 与 `WYH_JWT_SECRET` 一样只在环境里。存储不可用时，原文要求登录与找回拒绝；注册发信失败在当时实现中仍返回邮件未发出。

拦截与租户：

- 拦截顺序：先 `TenantInterceptor`（`+100`），再 `ApiSignConfig` 的 `checkLogin`（`+200`）。
- `SaExtensionInterceptor` 未注册。
- 原文写明：会话租户为 null 时仍会落到默认租户或请求头。这是与「已登录后数据租户以会话为准、无租户已登录业务 API 默认拒绝」这一目标相对的现状偏差，不是本轮新的漏洞证实。
- 无租户已登录最小允许列表在原文中标明为「目标，未实现」。列表中的方法与路径，原文写明均已在 `OrganizationController` 或 `AuthController` 中存在；匿名预览仍靠 `@SaIgnore`。排除名单名为 `web.security.excludes`。
- `GET /auth/user`：菜单按会话租户过滤在原文中写明是目标，不是现状。无租户时不得变成读取租户 0 业务数据的口子，同样是目标。

`GET /auth/register/options`：响应字段来自 `RegisterOptionsResponse`：`enabled`，口令 `minLength`、`maxLength`，`requireLowercase`，`requireDigit`，`requireSpecial`，`verificationTtlMinutes`，`resendIntervalSeconds`。无角色 id。无限流注解。改密请求类型为 `ProfilePasswordUpdateRequest`。图形验证码载荷类型为 `CaptchaResponse`。OTP 现有限流注解不作为找回的限额。

## 6. 审计与通知现状

`sys_login_log.username` 仍是 `varchar(50)`，用户名允许 64。原文写明：新 changeset 把列加到至少 64 之前，长用户名的登录审计可能写不进去。这是原文已指出的缺口，不是本轮新测试结论。需达目标见 [主 PRD 第 7 节与 P1](./auth_主PRD.md)。

可信 IP 沿用 `ServletUtils.getRequestIp()`。查询登录日志保持现有监控权限。保留天数沿用登录日志配置 365 天。

| 事件 | 现状（原文） |
|------|----------------|
| 登录成功 / 失败 | 已有 `LoginEvent` |
| 锁定 / 解锁 | 无单独事件 |
| 强制下线 | 禁用和在线踢出会 kickout |
| 重置申请 / 成功 / 失败 | 无 |
| 注册受理 / 发信失败 / 确认 | 无专门认证审计 |

## 7. 接口原返回摘要

业务异常与未登录处理见 3.2。下列为原文记录的原契约/原返回，目标契约以 [主 PRD 第 8 节](./auth_主PRD.md) 为准。

| 接口 | 原返回或原契约要点（原文） |
|------|----------------------------|
| `GET /auth/register/options` | 匿名，200，字段见第 5 节。无限流注解 |
| `POST /auth/register` | 成功带 `status`、`delivered`、`resendAfterSeconds`、`expiresMinutes`；邮箱占用 HTTP 422，`EMAIL_EXISTS`，「邮箱已被占用」 |
| `POST /auth/register/resend` | 无记录：HTTP 422，`REGISTER_NOTHING_TO_RESEND`。冷却：HTTP 200，`status=COOLDOWN`。请求字段名未在原文展开 |
| `POST /auth/register/verify` | 成功 HTTP 200，`EmailRegistrationConfirmResponse` 的 `code=200` 与 `status`（`VERIFIED` 或 `ALREADY_VERIFIED`），无登录 token。失败 HTTP 422，`REGISTER_LINK_INVALID` |
| `POST /auth/login?grantType=PASSWORD` | 成功 HTTP 200，`AccessTokenResponse.code=200`，含 `token`。失败原码见 3.2 |
| `POST /auth/logout` | 要登录。成功空。旧 token 再请求 HTTP 401 |
| `GET /auth/user` | 要登录。返回资料、角色码、权限码、菜单 |
| `GET /captcha` | 匿名，返回 `CaptchaResponse` |
| `PATCH /user/profile/password` | 要登录，RSA 密文。成功空 body |
| `GET /oauth/{provider}`、`POST auth/otp` | 保持当时契约，本期原文要求不改 |
| `POST /auth/password/forgot`、`POST /auth/password/reset` | 当时没有这两个接口 |

## 8. 验证边界（原文已声明、本轮未新测）

- 第 9 节场景原文写明都要准备数据后才能测，本文（及拆分前主 PRD）没有跑过。
- 时序验收无实测。第 4 节中位数之比与 p95 差值是门限，不是已测结果。
- 本仓库没有断言 cost 的测试。
- `EmailRegistrationResendRequest` 等未在原文展开的字段，原文不猜未读字段；本附件也不补猜。
- 用户本机 WIP 未读取、未核对。
- 加密列业务未指定 AES 模式或 IV，原文至此为止，本轮不增加新的密码学核实结论。
- 虚拟主体是否与真实账号共用同一套锁定码，原文写明未按虚拟主体写死。
- 登录失败 IP 维的具体每分钟阈值不在种子 JSON。
- 旧数据邮箱验证证据未验证：原文写明无版本字段。

## 9. 对照需求的缺口与待办

下表「缺口」只收录原文已经写出的实现差距或观察限制；「需达目标」指向主 PRD，避免把目标写成「当前漏洞已证实」。

| 需求（见主 PRD） | 现状锚点 / 原文证据 | 缺口或观察限制（原文已写） | 需达目标 |
|------------------|---------------------|----------------------------|----------|
| 注册不填用户名 | `RegisterRequest` | 无额外缺口 | 保持 |
| 用户名兼容，不删除 | `RegisterUsernameAllocator`，登录可短于 4 | 无额外缺口 | 不删除；存量短名可登录 |
| 注册不走前端 RSA；改密 RSA 保留 | 注册页不调用 `encryptByRsa`；改密仍 RSA 解密 | 无额外缺口 | 保持。改密 RSA 保留 |
| 用户名重名明确提示 | `UserException.usernameExists` | 无额外缺口 | 「用户名已被占用」（R4） |
| 邮箱注册统一受理（D-EMAIL 已确认） | `UserException.emailExists`；原成功含 `PENDING`/`COOLDOWN`/`delivered`；原失败含 `EMAIL_EXISTS`、`REGISTER_NOTHING_TO_RESEND`、`REGISTER_MAIL_UNSENT` | 原文写明代码尚未按本次决定修改；当时未改功能代码 | 统一 HTTP 200、`code=200`、`status=ACCEPTED`，不暴露是否已注册，不做投递/冷却状态探测 |
| 确认后才建号，不自动登录 | `EmailRegistrationService.confirm`；成功无登录 token | 无额外缺口 | 保持，不自动登录 |
| 注册发信对外形态 | 原文：事务提交后再 `MailClient.sendText`；发信失败直接返回未发出 / 仍返回邮件未发出 | 与 D-EMAIL 统一受理、不把投递失败对外区分的目标不一致。此处不把「同步」写成新的源码核实结论 | 见主 PRD 3.1、D-EMAIL |
| 完整同账号口令拒绝 | `containsName` 仅用于分配用户名 | 不是完整目标规则 | 忽略大小写，不得等于用户名或邮箱本地部分 |
| 72 字节拒绝；cost 10 | 未传 cost，库默认 10；无断言测试；长度按 UTF-16 代码单元 | 未见 72 字节门；cost 未单独配置或断言 | 见主 PRD 2.3、第 4 节（拟定） |
| 登录统一失败与虚拟主体锁定 | 文案对不存在标识已相同；`PASSWORD_ERROR` / `AUTH_ACCOUNT_DISABLED` / `ACCOUNT_LOCKED`；锁定 5 次 / 30 分钟；虚拟主体锁定码未写死；观察窗口无单独字段；IP 每分钟阈值不在种子 JSON | 目标码与滚动 15 分钟窗口、虚拟主体同一状态机尚未按主 PRD 写死为已实现 | 见主 PRD 3.2、4、8.1（拟定，登录码未逐项批准为已实施） |
| 重置证据 | 无版本字段；后台建号、改邮箱不写所有权证据 | 旧数据未验证 | 拟新增，旧数据默认未验证。D-RESET-PROOF 仍待确认 |
| 找回 | `forget-password.vue` 空函数，无 API | 接口不存在 | 本期不对外上线；P3 取决于 D-RESET-PROOF |
| 会话与 refresh | `AccessTokenResponse` 仅 `code` 与 `token`；无 refresh | 无 refresh，与首期目标一致 | 成功登录可含 token；无 refresh |
| 凭证版本门、Redis 滚动窗口、验证码数据库原子消费 | 原文只作为拟新增 / 拟定方案写出 | 不得把这些方案当成已实现或已批准 | 见主 PRD 3.4、第 4 节，均为拟定 |
| 登录审计用户名长度 | `sys_login_log.username varchar(50)`，用户名允许 64 | 原文指出长用户名登录审计可能写不进去 | 列长至少 64（P1） |
| 审计事件完整性 | 已有 `LoginEvent`；锁定/重置/注册无专门认证审计或无单独事件 | 事件覆盖不完整 | 见主 PRD 第 7 节 |
| 无租户默认拒绝、菜单按会话租户过滤 | `SaExtensionInterceptor` 未注册；会话租户为 null 时仍落到默认租户或请求头；允许列表目标未实现；菜单过滤不是现状 | 与目标存在现状偏差 | 见主 PRD 5、8.3 |
| 禁用后旧 token | 禁用会 `kickout(userId)` | 原文将「下一次 API 为 401」写为目标，未把 kickout 失败路径写成已证实可绕过 | 已有 token 下一次 API 为 401 |
| 匿名登录租户定位 | 注册上下文空则 `currentTenantId()` 取 0；登录定位须覆盖非 0 租户账号 | 定位方式未决；原文禁止未决前改成一律租户 0 | D-LOGIN-TENANT 未决前维持既有定位 |
| 确认写入部门与角色 | 部门 id `1` 与 `general`（或配置角色） | 不是零角色已实现 | D-ROLE 未决，本期不改既有写入 |
