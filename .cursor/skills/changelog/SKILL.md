---
name: changelog
description: >-
  Updates the root CHANGELOG.md (Keep a Changelog) before merge or when a
  roadmap phase completes. Use when the user says changelog, 更新日志,
  写 CHANGELOG, before merge, or after finishing an SDD phase.
---

# changelog

在 **合并前** 或 **roadmap 阶段收尾时**，把用户可感知的变更写入仓库根目录 `CHANGELOG.md`。不替代 git commit message。

## When to run

- 用户明确要求更新 changelog
- 阶段 validation 通过、准备把阶段标 `complete` 之前
- 开 PR / 合并前（若本 PR 有用户可见或架构影响）

跳过：纯错别字、无行为变化的格式化、仅本地调试。

## Preconditions

1. 读 `docs/roadmap.md`（当前阶段 ID / 名称，如有）。
2. 若存在阶段规格，读对应 `requirements.md` 的 In scope 与 `validation.md` 勾选结果，避免漏记或夸大。
3. 用 `git log` / `git diff`（相对合并基线或用户指定范围）核对实际改动；**以 diff 为准**，不以聊天记忆为准。

## CHANGELOG.md location & format

- 路径：仓库根目录 `CHANGELOG.md`
- 规范：[Keep a Changelog](https://keepachangelog.com/) 1.1.0 + [SemVer](https://semver.org/)（项目版本见 `backend/pom.xml` 的 `revision` / 发布标签）
- 若不存在：先创建文件头，再写入首个区段

### File header（仅首次）

```markdown
# Changelog

本文件记录 wyh-admin 对用户与集成方有意义的变更。

格式基于 Keep a Changelog，版本遵循 Semantic Versioning。
```

### Section order（每种类型有条目才写标题）

在 `## [Unreleased]` 下（无则新建在文件最上方、header 之后）：

1. `### Added`
2. `### Changed`
3. `### Deprecated`
4. `### Removed`
5. `### Fixed`
6. `### Security`

条目规则：

- 中文短句；说明 **行为/能力**，不堆文件名列表
- 一条一个要点；可附阶段引用，如 `（P1）` 或规格目录名
- 禁止：`更新了代码`、`修复了一些问题` 等空话
- 不写密钥、内网地址、未公开漏洞细节（Security 只写已修复问题的用户侧描述）

### Example entry

```markdown
## [Unreleased]

### Added
- 租户 Liquibase 主路径启用，套餐与隔离可随开箱流程交付（P1）

### Changed
- 平台域收口后，文件能力统一由 `platform/file` 提供

### Fixed
- …
```

## Workflow

1. 确定范围：Unreleased（默认）或用户指定的版本号 `## [x.y.z] - YYYY-MM-DD`。
2. 归纳 diff → 归入上述类型；拿不准的标 `Changed` 并在汇报里说明。
3. 写入 / 合并到 `CHANGELOG.md`（同类型条目追加，去重语义重复行）。
4. 用 ≤5 行汇报：写了哪些类型、条目数、是否建议在发布时把 Unreleased 封成版本区段。
5. **不**自动改 `pom.xml` 版本、不打 tag、不 commit，除非用户另有明确要求。

## Release cut（仅当用户要求发布时）

1. 将 `## [Unreleased]` 内容移到 `## [x.y.z] - YYYY-MM-DD`。
2. 保留空的 `## [Unreleased]` 在上方。
3. 版本号与用户确认；与 `revision` / 标签不一致时先问再写。

## Hard rules

- 只反映已存在于工作区或指定范围内的变更，不预支未做完的计划。
- 不把 `docs/` 中使命 / 技术栈 / 路线图等宪法措辞微调记成产品 Added（除非改变对外承诺）。
- 与 `sdd-continue` 收尾衔接：阶段标 `complete` 前应已跑过本 skill 或用户显式跳过。
