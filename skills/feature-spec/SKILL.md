---
name: feature-spec
description: >-
  Starts or refreshes a wyh-admin roadmap phase by interviewing for intent,
  then writing docs/product/YYYY-MM-DD-<slug>/{requirements,plan,validation}.md.
  Use when starting a phase, writing a feature spec, before implementing a
  roadmap item, or when the user says feature-spec / 写规格 / 开阶段.
---

# feature-spec

为 **一个** roadmap 阶段（或明确划定的子范围）产出规格三件套。确认前 **不写业务代码**。

## Preconditions

1. 阅读并遵守：
   - `docs/mission.md`
   - `docs/tech-stack.md`
   - `docs/roadmap.md`
2. 确定目标阶段：用户指定，或 roadmap 中第一个非 `complete` 且需要规格的阶段。
3. 若宪法与请求冲突：先停，问用户是否改宪法；禁止静默违反 `tech-stack.md` 规则。

## Workflow

### 1. Size check（两句话内说明路径）

- **可逆 / 小改**（文档、单点修复、无边界变动）：可轻量——在聊天记录意图，roadmap 备注即可；不强制三件套。
- **单向门 / 跨模块 / 租户·权限·API 契约**：完整规格。默认 wyh-admin 平台域工作走完整规格。

用户可用一词覆盖：`full` / `light`。

### 2. Intent interview（缺一不可则停，禁止臆造）

最多 **2 轮 × 每轮最多 3 问**。必须覆盖这五类信息（可合并提问）：

| # | 信息 | 说明 |
|---|------|------|
| 1 | Wanted | 要交付什么（用户可感知结果） |
| 2 | Constraints | 期限、兼容、安全、性能、不可动区域 |
| 3 | Failure | 失败 / 边界 / 不允许发生的事 |
| 4 | Success | 怎样算完成（优先可执行检查） |
| 5 | Connections | 触及的模块、API、表、前端路由 |

结合仓库证据提问；已知事实勿重复问。

### 3. Branch（可选）

用户同意后再建分支。建议：`feat/<phase-id>-<slug>` 或 `refactor/<phase-id>-<slug>`（如 `feat/p1-tenant-liquibase`）。未同意则仅写规格。

### 4. Write spec trio

目录：`docs/product/YYYY-MM-DD-<slug>/`（日期用当天 UTC+ 本地日历日；slug 短横线英文或拼音）。

创建三个文件（模板见下）。写入后 **暂停**，请用户确认（`确认` / `修改：…`）。未确认不得实现。

### 5. After confirm

- 将对应 roadmap 阶段标为 `in-progress`（若仍是 `pending`）。
- 实现交给用户显式指令，或由 `sdd-continue` 在确认后推进。
- 实现时按 `plan.md` 任务组 **一组一组** 做；每组对照 `validation.md`。

## File templates

### requirements.md

```markdown
# Requirements — <title>

## Phase
- Roadmap: P? — <name>
- Status: draft | confirmed

## In scope
- …

## Out of scope
- …

## Requirements
| ID | Statement | Source |
|----|-----------|--------|
| R1 | … | user / code / doc |

## Failure / edge cases
- …

## Connections
- Backend modules: …
- Frontend: …
- DB / Liquibase: …
- APIs: …

## Open questions
- …
```

### plan.md

```markdown
# Plan — <title>

## Approach
（对齐 tech-stack：模块归属、依赖方向、禁止事项）

## Task groups
### G1 — <name>
- [ ] …
### G2 — <name>
- [ ] …

## Risks
- …
```

### validation.md

```markdown
# Validation — <title>

## Executable checks（优先）
- [ ] `mvn -pl wyh-bootstrap -am …` 或约定编译命令
- [ ] 关键 API / 页面行为：…
- [ ] Liquibase / 迁移（如有）：…

## Acceptance（用户表述）
- [ ] …

## Done = all boxes above
```

## Hard rules

- 不修改 `mission.md` / `tech-stack.md` / `roadmap.md` 的目标与约束，除非用户明确要求宪法变更。
- 不引入微服务拆分；调度默认 Quartz；主库默认 PostgreSQL。
- 领域错误走 `XxxException` 工厂（见 `AGENTS.md`）；DB 变更走 Liquibase。
- 规格未确认 → 零业务代码、零无关重构。
