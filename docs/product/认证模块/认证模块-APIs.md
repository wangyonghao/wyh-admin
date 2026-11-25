# 认证模块 API 契约

以下接口沿用仓库现有路径，不加 `/api` 前缀。成功响应按 Controller 返回 DTO；登录当前返回 `AccessTokenResponse { code, token }`。异常使用 `Result { code, message, data?, timestamp?, traceId? }`，业务异常 HTTP 422；OTP 异常按现有 `OtpException` 状态映射。HTTPS 必需；验证码、密码、完整手机号不得写日志或出现在 URL。

`grantType=OTP` 为兼容现有统一登录 API 的技术 discriminator；产品文案与流程统一称“手机号短信验证码登录”。该分支请求只提交邮箱账号和验证码，手机号由服务端从账号绑定信息中解析，禁止将手机号作为账号标识。OTP 发码的 `scene=LOGIN` 使用相同邮箱定位账号并向其绑定手机号投递。登录成功 DTO 的实际字段维持 `code`、`token`；token 类型、过期时间及首次改密标志需按 R-08 与登录配置 DTO 核对后映射。

| 方法/路径                                       | 权限         | 请求字段                                                                 | 成功与主要错误                                                                                       |
| ------------------------------------------- | ---------- | -------------------------------------------------------------------- | --------------------------------------------------------------------------------------------- |
| GET `/captcha`                              | 公开         | 无                                                                    | 返回当前 `CaptchaResponse`（图像 Base64、UUID、有效期及是否始终要求）；参数/错误按现有控制器                                 |
| POST `/auth/login?grantType=PASSWORD`       | 公开         | `clientId`, `email`, `password`, `captchaUuid?`, `captchaChallenge?` | 返回 `AccessTokenResponse {code, token}`；凭证错误、锁定、验证码错误按认证异常                                     |
| POST `/auth/login?grantType=`               | 公开         | `scene=LOGIN`, `email`, `locale?`                                    | 返回 `OtpSendResponse {expiresIn, message, otpUuid}`；服务端用 email 定位账号并向绑定手机号发送短信；未知账号/未绑定时外部提示一致 |
| POST `/auth/login?grantType=OTP`            | 公开         | `clientId`, `email`, `codeChallenge`                                 | 返回 `AccessTokenResponse {code, token}`；短信验证码错误不计入账号锁定或登录限流；该 grantType 仅为兼容字段                 |
| GET `/oauth/{provider}`                     | 公开         | `provider`=`github`/`wechat`                                         | 返回第三方授权 URL；不支持的 provider 返回平台不支持错误                                                           |
| POST `/auth/login?grantType=OAUTH`          | 公开         | `clientId`, `provider`, `code`, `state`                              | 返回 `AccessTokenResponse {code, token}`；仅匹配已绑定账号，不自动注册                                         |
| POST `/auth/logout`                         | 已登录        | 无                                                                    | 204/空 DTO；当前会话立即失效                                                                            |
| GET `/auth/register/options`                | 公开         | 无                                                                    | 返回公开注册开关、密码规则、验证链接期限和重发间隔                                                                     |
| POST `/auth/register`                       | 公开         | `email`, `displayName`, `password`, 验证码字段                            | 返回注册邮件受理 DTO；不登录、不签发 token；错误不泄露邮箱是否存在                                                        |
| POST `/auth/register/resend`                | 公开         | `email`                                                              | 返回与注册相同的邮件受理 DTO；未有待验证记录时不得泄露账号状态                                                             |
| POST `/auth/register/verify`                | 公开         | `token`                                                              | 消费一次性 token 并创建账号，不签发登录 token                                                                 |
| POST `/auth/otp`（手机号绑定场景）                   | 已登录        | `scene=BIND_PHONE`, `phone`, `locale?`                               | 发送手机号绑定验证码；限频按 R-16                                                                           |
| POST `/auth/otp/verify`（手机号绑定场景）            | 已登录        | `otpUuid`, `phone`, `code`                                           | 校验并确认绑定；错误按 OTP 异常码                                                                           |
| POST `/auth/password/reset/request`（新增）     | 公开         | `email`                                                              | 统一受理文案；限频按 R-24；供应商故障提示稍后重试                                                                   |
| POST `/auth/password/reset/confirm`（新增）     | 公开         | `token`, `newPassword`                                               | 重置密码并立即使所有会话失效；token 失效/过期或密码策略失败返回对应业务错误                                                     |
| POST `/auth/password/change`（新增）            | 已登录        | `currentPassword`, `newPassword`                                     | 修改本人密码；需验证当前密码                                                                                |
| POST `/system/user`                         | 租户管理员，当前租户 | `email`, `displayName`（目标字段）；现有创建接口需调整                               | 发起组织邀请；新用户通过邀请完成注册/设密，接受后建立组织成员关系；不接受临时密码字段                                                   |
| PATCH `/system/user/{id}`                   | 租户管理员，当前租户 | 现有用户更新 DTO                                                           | 仅更新本租户用户                                                                                      |
| PUT `/system/user/{id}/password/reset`      | 租户管理员，当前租户 | 无                                                                    | 向用户邮箱发送 R-13 一次性重置链接；旧会话立即失效；不返回明文密码                                                          |
| PUT `/system/user/{id}/role`                | 租户管理员，当前租户 | 现有角色更新 DTO                                                           | 仅授予本租户业务角色                                                                                    |
| GET `/org/session`                          | 已登录        | 无                                                                    | 返回当前组织、可加入组织、是否需选择组织及待处理邀请                                                                    |
| POST `/org/switch`                          | 已登录        | `orgId`                                                              | 切换到本人有效成员组织并设置当前会话 tenantId                                                                   |
| POST `/org/invites`                         | 租户管理员，当前组织 | `email`                                                              | 创建组织邀请；接受后建立 Members 关系                                                                       |
| POST `/org/invites/accept?token=...`        | 已登录受邀用户    | `token`                                                              | 接受邀请，建立组织成员关系；角色随后由租户管理员分配                                                                    |
| GET `/monitor/login-log`                    | 平台管理员      | 现有查询条件及分页参数                                                          | 现有分页响应；按租户、账号、时间、结果筛选                                                                         |
| GET `/monitor/login-log/export`             | 平台管理员      | 与查询一致                                                                | 导出筛选结果                                                                                        |
| GET/PUT `/system/config/login`（安全配置增加租户作用域） | 平台管理员      | 现有登录配置 DTO；新增/绑定 `tenantId`                                          | PUT 保存后立即生效并审计；不得由普通业务接口改写                                                                    |

密码找回端点为新增接口，其他已实现接口保留当前路径。租户作用域、手机号短信收件人解析、社交登录不自动开户和安全配置按租户生效属于本 PRD 的目标语义；现有实现与目标不一致处须按确认后的 PRD 调整。
错误结构沿用仓库 `Result` 与 `OtpException` 约定，不另造统一 `AUTH_xxx` 前缀。OAuth redirect URI、分页上限和导出格式在 OpenAPI 定稿时补齐。
