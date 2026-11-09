# Backend conventions

配合 [`.cursor/rules/backend-java.mdc`](../../.cursor/rules/backend-java.mdc)、[`.cursor/rules/biz-exception.mdc`](../../.cursor/rules/biz-exception.mdc)。架构硬规则以 [`docs/tech-stack.md`](../tech-stack.md) 为准。模块目录与依赖以 [`docs/architecture/repo-map.md`](../architecture/repo-map.md) 为准。

## Module & packaging

- 目录名与 Maven `artifactId` 相同：装配 `start`（`backend/start`），Admin 管理能力 `admin-system`，认证 `admin-auth`，跨模块契约 `admin-system-client`，开放 API `admin-openapi`，基础设施 `commons` 下的 `cmn-*`。Java 包名不跟目录走。
- `admin-system` 的包是 `top.wyhao.system.admin`，`admin-auth` 的包是 `top.wyhao.system.auth`。两者用 COLA：`adapter`（web/client）/ `app`（service、assembler）/ `domain` / `infrastructure`。
- `start` 只做装配，不放业务域。`admin-auth` 只依赖 `admin-system-client`，不依赖 `admin-system`。
- `commons` 不要求 COLA，且禁止依赖 `admin-system-client`、`admin-system`、`admin-auth`、`admin-openapi`、`start`。
- `admin-system-client` 只放跨模块契约，依赖止于 `cmn-core` 与注解（Swagger / Jackson）。契约包名（如 `top.wyhao.identity.client`、`top.wyhao.security.client`）保持不变。
- 开放 API `admin-openapi`（包 `top.wyhao.system.open`）用经典分层：`controller` / `service` / `mapper` / `entity`。租户、仪表盘、通知、审计已在 `admin-system` 的 COLA 层中，不再另做经典切片。

## Naming

- 类：PascalCase；方法/字段：camelCase；常量：`UPPER_SNAKE`；包名全小写。
- API 模型：`XxxQuery`、`XxxRequest`、响应 `XxxResult` / `XxxVO`（与现有模块一致即可）。
- Service 常用动词：`detail` / `list` / `page` / `create` / `update` / `delete` / `export` / `import`。

## Controller

- `@RestController`；**在方法上写完整路径**，类上避免笼统 `@RequestMapping` 前缀包办一切。
- Controller 负责组装 API 结构（Result/VO）；领域逻辑在 app/service。
- 文档注解：`@Tag`、`@Operation`（Swagger / OpenAPI）。

## API

- REST：GET 查、POST 建、PUT 全量、PATCH 部分、DELETE 删。
- 成功响应一般不强制包装；错误走统一 `code` / `msg`（`BizException` 体系）。
- 分页请求常见：`page`、`pageSize`；响应含 `records`、`total`、`page`、`pageSize`、`pages`。

## Database

- 表/字段：`snake_case`；系统表 `sys_` 前缀；主键 `id`。
- 审计字段常见：`create_time`、`update_time`、`create_user`、`update_user`；逻辑删除 `deleted`。
- 索引：`pk_` / `uk_` / `idx_` 前缀约定。
- **变更必须走 Liquibase**（`backend/start/src/main/resources/db/changelog/postgresql/`）。

## Exceptions

- 一功能一 `XxxException extends BizException`，错误码与文案在**静态工厂**。
- 禁止业务代码 `throw new BizException(...)` / `BadRequestException(...)` 表达领域错误。
- 入参格式：Bean Validation；`Check` / `ValidationUtils` 禁止新增。
- 详见 [`.cursor/rules/biz-exception.mdc`](../../.cursor/rules/biz-exception.mdc) 与现有 `UserException`（`admin-system`，包 `top.wyhao.system.admin.domain.exception`）、`AuthenticationException`（`admin-auth`，包 `top.wyhao.system.auth.domain.exception`）。

## Transactions & logging

- `@Transactional` 只放在 Service / app 层；粒度小；避免事务内远程调用与长耗时 IO。
- 日志：关键业务用 INFO；异常带堆栈用 ERROR；排查用 DEBUG。

## Dependencies

- 版本只在后端根 `pom.xml` 的 `properties` / `dependencyManagement` 登记后再引用。

## Testing

- 工具：JUnit 5 + Mockito + AssertJ（`commons` 父 POM `backend/commons/pom.xml` 为 `cmn-*` 统一了 `test` 依赖）；默认执行 Surefire（`-DskipUnitTests=true` 可临时跳过）。
- 命名：`XxxTest`（单元）；`XxxIT` / `*ApplicationTests` 不进单元套件（Surefire exclude）。单元测**不起 Spring**。
- **必测 / 不强制**：

  | 范围 | 要求 |
  |------|------|
  | `domain` / 纯规则（策略、解析器、校验器、工具） | 改逻辑必须同步 `XxxTest` |
  | 可 Mock 的 app 决策与编排（Gateway / API 可注入） | 同上 |
  | 基础设施重的 app（Redis / Sa-Token / Servlet 等静态依赖） | 抽纯决策函数测透；**不要求**整段主路径（如完整 `authenticate()`）在单元套件测满 |
  | 手写、含分支的转换器 | 改逻辑必须同步 `XxxTest` |
  | MapStruct `*Assembler`、Spring MVC Converter、FastExcel `Excel*Converter` | **不强制**（脚手架 / adapter）；仅非平凡自写分支可作次要补测 |
  | `adapter`（Controller / Web DTO 组装） | 不强制 |
  | Repository / Mapper | 偏集成测试（`XxxIT`） |

- **`SpringUtil.getBean`**：存量可只测无 getBean 路径，getBean 分支标待抽依赖或 IT；**禁止**在 domain / 可测 app 路径新增 `SpringUtil.getBean`，应构造注入或方法参数传入协作接口。
- **覆盖率**：不设 jacoco / 百分比门禁。「高覆盖」= 改动同步有用例，并以当前补测清单 P0 为实践底线。
- 示范：`backend/admin-system/src/test/java/top/wyhao/job/app/service/CronComposerTest.java`（被测类在 `top.wyhao.system.admin.app.service`）、`backend/admin-system/src/test/java/top/wyhao/system/admin/domain/service/PasswordRulesTest.java`。
