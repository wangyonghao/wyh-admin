---
name: sdd-continue
description: >-
  Continues wyh-admin Spec-Driven Development from the next unfinished
  roadmap phase: re-read constitution, resume or start specs, then implement
  confirmed task groups. Use when the user says continue, what's next, 继续,
  下一步, 当前阶段, or wants to resume SDD without restating context.
---

# sdd-continue

跨会话续作入口：只依赖仓库内宪法与阶段规格，不依赖聊天长记忆。

## Every run（固定顺序）

1. 读 `docs/mission.md`
2. 读 `docs/tech-stack.md`
3. 读 `docs/roadmap.md`
4. 选定 **第一个** `Status` 非 `complete` 的阶段（用户点名则用点名阶段）
5. 用 **≤5 行** 汇报：阶段 ID、状态、Done when 缺口、下一步动作
6. 按下方状态机推进；遇歧义先问再改

## State machine

### A. 无阶段规格，或规格仍为 draft

→ 调用 / 遵循 `skills/feature-spec/SKILL.md`（完整或 light 由其 size check 决定）。  
本 skill 不跳过访谈与确认门闩。

### B. 规格已 confirmed，plan 未做完

1. 打开该阶段目录：`docs/product/YYYY-MM-DD-<slug>/`（取与阶段匹配、最新且已确认者）。
2. 读 `requirements.md`、`plan.md`、`validation.md`。
3. 找到 `plan.md` 中第一个未完成任务组 `G?`。
4. **只实现该组**；遵守 tech-stack 与 AGENTS 编码约定。
5. 组内完成后：勾选 plan 项；对照 validation 能跑的检查先跑。
6. 停下来简述结果与下一组；除非用户说「继续做下一组」，否则不连做多组。

### C. plan 全部完成，validation 未全过

1. 逐项执行 / 核对 `validation.md`。
2. 失败则修复或标出阻塞，不假装完成。
3. 全过 → 进入 D。

### D. 阶段可收尾

1. 遵循 `skills/changelog/SKILL.md` 更新根目录 `CHANGELOG.md`（用户可显式跳过）。
2. 请用户确认将 roadmap 中该阶段标为 `complete`（**须人工确认**）。
3. 确认后更新 `docs/roadmap.md` 状态与 Done when 勾选。
4. 再用 ≤5 行指出下一阶段；若用户要继续，从头跑本 skill。

## Constitution changes

- 禁止把「实现方便」写成对 mission / tech-stack / roadmap 的悄悄改写。
- 若实现证明宪法过时：提出变更建议并 **等待确认**，再改宪法文件。

## wyh-admin-specific guards

- 依赖方向：`wyh-bootstrap` → `interfaces` / `biz` → `platform` 域 → `cmn-*`。
- P1 收口期间：优先 A/C/D（编译启动、租户可交付、前后端对齐），避免无关新功能膨胀。
- DB：Liquibase + PostgreSQL changelog；任务：Quartz / `platform-job`。

## Output style

- 中文、短句；先结论后细节。
- 需要用户决定时：一次最多 3 个具体问题。
- 不主动 commit / push，除非用户明确要求。

## Handoff

| 情况 | 去做 |
|------|------|
| 要开新规格 / 重写规格 | `feature-spec` |
| 规格已确认、要写代码 | 本 skill 状态 B |
| 合并前 / 阶段收尾写日志 | `changelog` |
| 用户只要状态、不改动 | 只做「Every run」1–5 步后停止 |
