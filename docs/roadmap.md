# wyh-admin — Roadmap

状态：`pending` | `in-progress` | `complete`  
Agent 每次会话读取本文件，从第一个非 `complete` 的阶段继续；阶段变更须经人工确认。

## Phases

### P0 — SDD 宪法落地

- **Status:** `in-progress`
- **Goal:** 建立可被 Agent 稳定消费的项目级宪法，替代跨会话记忆。
- **Done when:**
  - [x] `docs/mission.md`
  - [x] `docs/tech-stack.md`
  - [x] `docs/roadmap.md`（本文件）经确认落地
  - [x] `skills/feature-spec`、`skills/sdd-continue`、`skills/changelog`（并镜像至 `.cursor/skills/`）
- **Notes:** 宪法变更须显式决策，不得作为功能开发的副作用修改。核心文件与首批 skills 已齐；可将本阶段标为 `complete`。

### P1 — Platform 领域重构收口（含租户底座）

- **Status:** `in-progress`
- **Goal:** 完成 platform 模块化与分层治理收口，租户能力可随主路径交付；前后端与文档定位一致。
- **Scope:**
  - 后端域边界与 COLA/分层收口（iam、tenant、file、job、ops、cmn-*）
  - 消除半迁移模块与过时路径依赖；编译与 `wyh-bootstrap` 启动全绿
  - 租户：changelog / 套餐与隔离等开箱路径打通（原独立「租户底座」阶段并入本阶段）
  - 前端 API、路由与后端域拆分对齐
  - `AGENTS.md` 等导航文档与真实目录一致（支撑收口，不单列为成功标准时可并行）
- **Done when:**
  - [ ] **A.** 编译 / 启动全绿，无半迁移模块（tenant / ops / license / biz-system 边界已钉死，见 `docs/architecture/repo-map.md`；启动可重复与死代码清理另见 W2-①②）
  - [x] **C.** 租户相关纳入本阶段并达到可交付（含 Liquibase 等主路径启用与闭环）（#35）
  - [ ] **D.** 前端 API / 路由与后端域拆分对齐
- **Notes:** 不引入微服务拆分。本阶段完成前，避免大面积新开与边界冲突的业务能力。P1-A 四域收口不以「全部 COLA 化」为完成标准；ops notification/audit 与 tenant 经典分层为已接受边界。

### P2 — 脚手架体验与规范打磨

- **Status:** `pending`
- **Goal:** 对齐 mission 重心 1——稳定、舒适、可交付的开源脚手架体验。
- **Scope（摘要）:**
  - 克隆 → 配置 → 启动 → 品牌定制路径顺畅
  - 通用中后台能力缺口与体验问题持续收敛
  - 质量门禁与工程规范保持可执行
- **Done when:**
  - [ ] 新贡献者 / Agent 可按文档在合理时间内完成本地跑通与基础定制
  - [ ] 已知阻塞级体验问题清零或有明确延期记录
- **Depends on:** P1 收口（可小步并行文档与 DX，但不以破坏域边界为代价）

### P3 — AI 友好强化

- **Status:** `pending`
- **Goal:** 对齐 mission 重心 2——降低 Agent 协作成本与漂移风险。
- **Scope（摘要）:**
  - feature-spec 工作流（requirements / plan / validation）按阶段启用
  - skills、生成模板与 Agent 可消费约定补齐
  - 规格与实现可对照校验
- **Done when:**
  - [ ] 新阶段默认「先规格、后实现」，且规格存放约定固定
  - [ ] Agent 能仅凭仓库内宪法 + 阶段规格推进，无需依赖聊天长上下文
- **Depends on:** P0 完成；建议在 P1 基本稳定后加大投入

## Ordering rationale

mission 产品重心为「体验 → AI → platform」，但代码上 platform 重构已在进行，故执行顺序为：

**P0 → P1（含租户）→ P2 → P3**

体验（P2）与 AI（P3）可在 P1 后期小步并行，大范围能力扩展以 P1 Done when 为准。

## Out of roadmap (for now)

- 微服务拆分（见 `mission.md` Non-goals / `tech-stack.md` Exclusions）
- 以替换 Quartz 或主库为默认前提的栈迁移（须先改宪法）
