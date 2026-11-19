# docs/ — 项目知识库

---

## 1. 目录地图

```text
docs/
├── mission.md          # 项目使命（愿景、用户、非目标）
├── roadmap.md          # 做什么、阶段完成标准
├── tech-stack.md       # 用什么技术、版本、硬规则
├── glossary.md         # 领域术语表:业务名词、缩写、易混概念
├── architecture/       # 架构总览、功能模块地图、边界与对齐记录
├── adr/                # 架构决策记录（为什么这样设计）
├── conventions/        # 编码 / UI / 交付等长期规范（一主题一文件）
├── product/            # PRD文档 + 验收标准(一功能模块一文件, 带编号）
├── specs/              # 进行中的功能规格:每个功能一个子目录,含 spec(要什么)、plan(怎么做)、tasks(拆分步骤); 实现完成后可归档或删除
└── contracts/
    └── API 接口文档
```

---

## 3. 与仓库其它入口的关系

| 位置 | 写什么 | 不写什么 |
|------|--------|----------|
| [`AGENTS.md`](../AGENTS.md) | 硬边界摘要、常用命令、**Delivery 交付约束**、指向本文与 conventions 的链接 | 长教程、大段代码规范、决策全文 |
| [`.cursor/rules/`](../.cursor/rules/) | Cursor 自动加载的硬约束（尽量短）；细则链到 `conventions/` | 与 conventions 重复的百科 |
| [`skills/`](../skills/) | 可复用工作流（开规格、续作、changelog）；技能目录留在仓库根 | 业务与产品知识（放 `product/`） |
| `backend/AGENTS.md` / `frontend/AGENTS.md`（可选） | 该侧独有的短规则 | 全仓共通规则（仍放根 AGENTS + docs/） |
| [`frontend/apps/docs/DEVELOPERS.md`](../frontend/apps/docs/DEVELOPERS.md) | 本地开发：依赖、中间件、前后端启动 | 产品规格、架构决策 |

**选型口诀**

- 改代码时要遵守 → `conventions/`
- 解释「为何选 A 不选 B」→ `adr/`
- 画模块怎么拆、模块边界 → `architecture/`
- 使命 / 路线图 / 技术选型 → `mission.md`、`roadmap.md`、`tech-stack.md`
- 业务词、PRD、用户故事、阶段规格 → `product/`
- 「按这几步能起服务 / 能演示 / 能排障」→ 本地开发见 [`frontend/apps/docs/DEVELOPERS.md`](../frontend/apps/docs/DEVELOPERS.md)；运维见 `runbook/`

---

## 4. 如何新增或修改一篇文档

1. **先选型**：用上一节口诀选目录；拿不准时优先 `adr/`（决策）或 `architecture/`（现状边界），不要塞进 `AGENTS.md`。
2. **一主题一文件**：文件名用英文 kebab-case；开头写适用范围 + 更新日期。
3. **正文结构建议**
   - conventions：规则条目 → ✅/❌ → 相关命令 → 「不要做什么」
   - adr：背景 → 决策 → 后果（可选备选方案）
   - architecture：范围 → 图（Mermaid）→ 边界与禁止依赖
   - runbook：前置条件 → 步骤（可复制命令）→ 验收 → 常见失败
4. **互链**：在 `AGENTS.md` 或本 README 补链接；`.cursor/rules` 只保留摘要并链到 conventions。
5. **能测则测**：分层依赖、命名等尽量落 ArchUnit / lint；文档删掉已自动化的重复段落。

### ✅ / ❌ 示例

```markdown
<!-- ✅ 好：可执行 + 边界 -->
Controller 只做参数校验与调用应用服务，禁止写业务规则；统一返回 `Result<T>`。
不要在 `adapter` 包直接访问其它模块的 `infrastructure`。

<!-- ❌ 差：无法验收 -->
代码要保持整洁，注意分层。
```

---

## 5. ADR 约定

- 目录：[`adr/`](adr/)；模板：`adr/template.md`（若尚未创建，新增第一条 ADR 时一并补上）。
- 命名：`NNNN-短横线标题.md`，编号递增。
- **何时必须写**：改变模块边界、持久化方案、鉴权模型、对外 API 兼容策略、引入或替换核心中间件。
- **何时不必写**：局部重构、重命名、与已有 ADR 一致的常规实现。
- 决策被推翻时：保留旧 ADR，新开一条并在旧文顶部标注 superseded-by。

---

## 6. 日常维护清单

- [ ] 正文仍是「可执行指令」，不是空原则
- [ ] `AGENTS.md` 链接未失效；长文没有回流进 `AGENTS.md`
- [ ] 同一规则没有在 conventions 与 `.cursor/rules` 各写完整版（最多一处正文 + 其它处链接）
- [ ] 架构图是 Mermaid/PlantUML 文本，能进 Git diff
- [ ] runbook 里的命令与当前脚本/模块名一致

---

## 7. 与 AI 协作时的用法

1. 先读短索引：[`AGENTS.md`](../AGENTS.md) → 需要时再打开本 README。
2. 改某层代码：打开对应 `conventions/*`，不要整目录塞进上下文。
3. 做方案取舍：先搜 `adr/`，没有再写新 ADR。
4. 启动 / 排障 / 租户演示：打开 `runbook/`；本机验收以宿主机 spring-boot 路径为准时，以 runbook 与 `AGENTS.md` Commands 为准。
5. Claude Code 等工具：根目录可保留 `CLAUDE.md` 仅 `@AGENTS.md`，避免维护两份百科。

---

## 8. 计划中仍可补的 conventions

| 计划路径 | 内容 |
|----------|------|
| `conventions/api-design.md` | HTTP / 开放 API 约定 |
| `conventions/database.md` | PostgreSQL + Liquibase |
| `conventions/git-workflow.md` | 分支、提交、评审 |

## 用户故事的写法

每个故事带稳定编号和验收标准。
编号用 `US-<模块缩写>-<序号>`。
验收标准是测试用例的参考依据。

```
### US-USER-003 管理员重置用户密码

作为 **系统管理员**,我想为忘记密码的用户重置密码,以便用户能重新登录。

**验收标准**
- Given 管理员已登录且有"用户管理"权限,When 点击重置密码,Then 系统生成一次性临时密码并要求用户首次登录时修改
- Given 操作者没有该权限,When 调用重置接口,Then 返回 403
- 重置操作写入审计日志
```