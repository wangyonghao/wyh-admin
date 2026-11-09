# docs/ — 项目知识库索引

适用范围：wyh-admin 仓库内所有架构、规范、领域与运维文档。  
维护：架构负责人；改动规范或目录结构时同步更新本文与根目录 [`AGENTS.md`](../AGENTS.md) 中的链接。  
最后更新：2026-11-08

本文说明 **`docs/` 怎么分、各目录写什么、怎么维护**。细则正文放在子目录里；根 [`AGENTS.md`](../AGENTS.md) 只做短索引。

---

## 1. 设计原则（写文档前先看）

| 原则 | 做法 |
|------|------|
| 索引短、正文按需读 | `AGENTS.md` / 本文保持短；细节用相对路径链出去，AI 按需打开 |
| 可执行，不写空原则 | 写「谁可以/不可以做什么 + 命令」；避免「保持整洁」这类无法验收的话 |
| 正反例优先 | 关键约束配 ✅ / ❌ 代码或路径对比 |
| 写清边界 | 明确「不要做什么」（例如禁止改 `generated/`、新依赖先问） |
| 能自动化就不写进文档 | Checkstyle / ESLint / Prettier / ArchUnit / lefthook 管得到的，文档只补工具管不了的部分 |
| 单一真相 | 同一条规则只在一处写全，别处链接；禁止 conventions 与 `.cursor/rules` 各抄一版又不互链 |

---

## 2. 目录地图

```text
docs/
├── README.md           # 本文件：地图 + 维护约定
├── mission.md          # 为什么做（愿景、用户、非目标）
├── roadmap.md          # 做什么、阶段完成标准
├── tech-stack.md       # 用什么技术、版本、硬规则
├── architecture/       # 架构总览、模块地图、边界与对齐记录
├── adr/                # 架构决策记录（为什么这样设计）
├── conventions/        # 编码 / UI / 交付等长期规范（一主题一文件）
├── product/            # 产品文档（PRD、用户故事、阶段规格）
└── runbook/            # 部署、运维、排障、演示步骤
```

| 目录 | 职责 | 主要读者 | 现有文件 |
|------|------|----------|----------|
| [`architecture/`](architecture/) | 系统长什么样、模块边界 | 人 + AI（设计/大改前） | [`repo-map.md`](architecture/repo-map.md)、[`frontend-packages.md`](architecture/frontend-packages.md) |
| [`adr/`](adr/) | **为什么**选这个方案（背景 → 决策 → 后果） | 人 + AI（缺上下文时） | `template.md`（待补）、`NNNN-….md` |
| [`conventions/`](conventions/) | **怎么写**才算合规 | 人 + AI（改代码时按需打开） | [`backend-java.md`](conventions/backend-java.md)、[`frontend.md`](conventions/frontend.md)、[`ui-patterns.md`](conventions/ui-patterns.md)、[`frontend-naive-migration.md`](conventions/frontend-naive-migration.md) |
| [`product/`](product/) | PRD、用户故事、领域说明、阶段规格三件套；评审前的设计草案也放这里 | 人 + AI（写需求 / 开阶段时） | [`2026-11-08-auth/design.md`](product/2026-11-08-auth/design.md)（认证总设计，草案）、[`2026-11-08-user-registration/design.md`](product/2026-11-08-user-registration/design.md)（注册与加入组织，草案）。二者都待评审、未实施。确认后再写 `YYYY-MM-DD-<slug>/{requirements,plan,validation}.md` |
| [`mission.md`](mission.md) / [`roadmap.md`](roadmap.md) / [`tech-stack.md`](tech-stack.md) | 产品使命、阶段完成标准、技术选型硬规则 | 人 + AI（改宪法或开工前） | 三篇已落地 |
| [`runbook/`](runbook/) | 可重复启动、演示、排障步骤 | 人 + AI（验收/值班） | [`tenant-demo.md`](runbook/tenant-demo.md) |

---

## 3. 与仓库其它入口的关系

| 位置 | 写什么 | 不写什么 |
|------|--------|----------|
| [`AGENTS.md`](../AGENTS.md) | 硬边界摘要、常用命令、**Delivery 交付约束**、指向本文与 conventions 的链接 | 长教程、大段代码规范、决策全文 |
| [`.cursor/rules/`](../.cursor/rules/) | Cursor 自动加载的硬约束（尽量短）；细则链到 `conventions/` | 与 conventions 重复的百科 |
| [`skills/`](../skills/) | 可复用工作流（开规格、续作、changelog）；技能目录留在仓库根 | 业务与产品知识（放 `product/`） |
| `backend/AGENTS.md` / `frontend/AGENTS.md`（可选） | 该侧独有的短规则 | 全仓共通规则（仍放根 AGENTS + docs/） |

**选型口诀**

- 改代码时要遵守 → `conventions/`
- 解释「为何选 A 不选 B」→ `adr/`
- 画模块怎么拆、模块边界 → `architecture/`
- 使命 / 路线图 / 技术选型 → `mission.md`、`roadmap.md`、`tech-stack.md`
- 业务词、PRD、用户故事、阶段规格 → `product/`
- 「按这几步能起服务 / 能演示 / 能排障」→ `runbook/`

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
