# wyh-admin — DESIGN.md

> **Status:** draft outline（九段齐，token 对齐现仓库；视觉微调待确认后升为正式版）  
> **Scope:** 主应用 `frontend/apps/admin`（`@wyh/admin`，shadcn-vue / Vben 原子 + Vben 壳）中后台产品 UI  
> **Token source of truth:** `packages/core/design/src/design-tokens/{default,dark}.css`  
> **Page patterns:** [`docs/conventions/ui-patterns.md`](../docs/conventions/ui-patterns.md)  
> **Not for:** 营销落地页 / 盲目套用 Linear·Stripe 等品牌 DESIGN.md

颜色值以 CSS 变量为准；文中 hex 仅为阅读辅助（由 HSL token 近似换算）。

---

## 1. Visual Theme & Atmosphere

wyh-admin 是**企业中后台脚手架**：信息密度偏高、结构清晰、克制用色。默认 **light-first**（灰蓝内容底 + 白表面），完整支持 **dark**；气质接近「可靠的运营控制台」，不是营销站、不是极简 IDE。

| 维度 | 取向 |
|------|------|
| 密度 | 表格/表单区紧凑；仪表盘可略松 |
| 色调 | 冷灰蓝底（`--background-deep`）+ 单一主色蓝 |
| 圆角 | 中等（`--radius: 0.5rem` / 8px），不药丸化主按钮 |
| 阴影 | 少用；层级主要靠表面色差 + `--border` |
| 动效 | 短、功能性（抽屉/折叠）；不做装饰性大动画 |
| 品牌 | 主色仅用于 CTA、链接、选中、焦点；禁止大面积铺色 |

**哲学：** 统一来自「同一套 token + 同一套页面范式」，而不是每页手写排版。

---

## 2. Color Palette & Roles

### Surfaces（表面阶梯）

| Token | Light（约） | Dark（约） | Role |
|-------|-------------|------------|------|
| `--background-deep` | `#F1F4F8` | `#14171B` | 主内容区底（layout） |
| `--background` / `--card` | `#FFFFFF` | `#1C1F24` | 卡片、表格容器、侧栏内面板 |
| `--popover` | `#FFFFFF` | `#242424` | 弹出层（暗色略抬起，避免与 card 糊在一起） |
| `--sidebar` / `--header` | `#FFFFFF` | （见 dark.css） | 壳层：侧栏、顶栏 |
| `--muted` | `#F4F4F5` | `#27272A` | 次级底：Tabs、Skeleton、弱区块 |
| `--heavy` | `#E1E6E8` | `#3A3F46` | 更重的强调底 / hover 阶 |

### Ink（文字）

| Token | Light | Role |
|-------|-------|------|
| `--foreground` | `hsl(210 6% 21%)` | 正文、标题 |
| `--muted-foreground` | `hsl(240 3.8% 46.1%)` | 辅助说明、表头弱化、空态 |
| `--card-foreground` / `--popover-foreground` | 近黑 / 近白 | 对应表面上的正文 |
| `--input-placeholder` | `hsl(217 10.6% 65%)` | 占位符 |

### Brand & semantic

| Token | Approx | Role |
|-------|--------|------|
| `--primary` | `#0077E6`（`212 100% 45%`） | 主按钮、链接、选中、关键焦点 |
| `--primary-foreground` | `#FAFAFA` | 主色上的文字 |
| `--primary-600` / `--primary-700` | （palette） | hover / pressed |
| `--destructive` | `#FF4D6A` | 删除、危险确认 |
| `--success` | `#3ECF8E` | 成功 Message / Tag |
| `--warning` | `#F0B429` | 警告 |
| `--info` / `--info-foreground` | 浅灰底 + 灰字 | 弱信息提示（非第二主色） |
| `--secondary` | 浅灰 | 次按钮底 |
| `--accent` / `--accent-hover` | 浅灰阶 | 列表项 hover、菜单高亮底 |

### Lines & focus

| Token | Role |
|-------|------|
| `--border` | 默认分割线、卡片描边 |
| `--input` | 输入框边框 |
| `--ring` | 焦点环（与主色体系配合） |
| `--overlay` | 模态遮罩（`0 0% 0% / 45%`） |

### Rules

- **单一色相强调：** 交互强调只用 `--primary` 阶；成功/警告/危险仅语义场景。
- **禁止**页面内写死 `#hex` / `rgb()`；用 CSS 变量或 Tailwind 语义类（`bg-background`、`text-muted-foreground`、`border-border`）。
- 主题切换依赖 `.light` / `.dark`；自定义主题色走 preferences + token，不另开一套。

---

## 3. Typography Rules

| Token / 约定 | 值 | 用途 |
|--------------|-----|------|
| `--font-family` | 系统栈（-apple-system, Segoe UI, …） | 全局 sans |
| `--font-size-base` | `16px` | 根字号 |
| `--menu-font-size` | `0.875 × base`（14px） | 侧栏菜单 |

### Hierarchy（中后台实用表）

| Level | 建议 | 使用 |
|-------|------|------|
| Page title | 16–18px / 600 | 少用独立大标题；多数页靠路由 `meta.title` + 表格 `table-title` |
| Section | 14–16px / 600 | 抽屉内分组标题、卡片标题 |
| Body | 14px / 400 | 表单、表格单元格（原子控件 / Vxe 默认） |
| Caption | 12–13px / 400 | 辅助说明、时间戳、`--muted-foreground` |
| Mono | `font-mono` | 密钥、ID、代码片段 |

**Do：** 依赖组件默认字号，不在每个页面自定义字阶。  
**Don't：** 引入第二套展示字体、营销级 display（48px+）、标题负字距炫技。

---

## 4. Component Stylings

实现以 **Vben 封装**（`Page`、`useVbenForm`、`useVbenDrawer`、`useVbenVxeGrid`）和 `#/ui` 原子为准。颜色走本文件 token，经 `apps/admin/src/styles/theme.css` 的 `--color-*` 接到 Tailwind。

**原子层：** 新代码按路径导入 `@wyh/ui/<atom>` 或别名 `#/ui/<atom>`（Button、Input、Label、Checkbox、Switch、Dialog、AlertDialog、Badge、Skeleton、Sonner、Select、Popover、Card、Tabs 等）。源码在 `frontend/packages/ui`；底子留在 `@vben-core/shadcn-ui/src/ui`（reka-ui + CVA + `cn`）。页面组合放 `frontend/packages/ui-patterns`（`@wyh/ui-patterns`，别名 `#/ui-patterns`），按目录路径导入，不建总桶。组件演示在 `frontend/apps/design-system`，只消费这两个包。`naive-ui` 已从依赖里去掉。清单见 [`docs/conventions/frontend-naive-migration.md`](../docs/conventions/frontend-naive-migration.md)。

### Buttons

| 角色 | 用法 | 约定 |
|------|------|------|
| Primary | `#/ui/button` | 每页主操作唯一优先：新建 |
| Default | `variant="outline"` | 导出、次要操作 |
| Text / Tertiary | `variant="link"` 或 `ghost` | 行内：复制、显示/隐藏 |
| Destructive | 删除走 `ConfirmAction`，按钮可用 `text-destructive` | 敏感操作先确认 |

工具栏顺序（左→右）：**主操作（新建）→ 次操作（导出）→ …**；行内操作：查看 / 编辑 / 更多（删除放菜单或确认）。

### Inputs & forms

- 搜索与编辑表单优先 `useVbenForm` + Zod schema（`#/adapter/form`）。
- 栅格：搜索区常用 `grid-cols-1 md:grid-cols-2 lg:grid-cols-3/4`；`showCollapseButton` 字段多时开启。
- 校验错误态走表单控件自己的错误样式，不单页覆写边框色。

### Tables

- **默认：** `useVbenVxeGrid`（`#/adapter/vxe-table`），`height: 'auto'`，`Page auto-content-height`。
- 工具栏：`refresh` / `search` / `custom` / `zoom` 按需；业务按钮放 `#toolbar-tools`。
- 新 CRUD 用 Vxe，不再手写另一套表格。

### Overlays

| 场景 | 组件 |
|------|------|
| 创建/编辑 | `useVbenDrawer`（或 Modal，同一模块内统一） |
| 详情只读 | Drawer / 独立详情组件 |
| 危险确认 | `ConfirmAction` |
| 轻反馈 | `#/ui-patterns/toast`（成功/失败） |

### Tags & status

- 状态用字典 + `#/ui/badge`；颜色映射语义 token（success/warning/error/default），禁止每页自定义色板。

### Navigation (shell)

- 侧栏 / 顶栏 / 标签页：跟 Vben preferences，应用页不重做壳。
- 内容区底为 `--background-deep`；面板用 `--background` / `bg-background`。

### Cards

- 仪表盘统计/图表：`@vben/common-ui` 已有 Analysis/Workbench 组件；统一 gutter，避免每卡不同 padding。
- CRUD 列表页：**不要**再套一层无意义卡片包住整个 Grid（`Page` + Grid 即可）。

---

## 5. Layout Principles

### Spacing

| 来源 | 值 | 用法 |
|------|-----|------|
| Tailwind `--spacing` | `0.25rem`（4px） | 基数；`p-2`/`p-4`/`gap-3`/`mb-4` |
| 内容区内边距 | 常用 `p-4`（16px） | 树面板、表头工具行 |
| 区块间距 | `gap-3` / `mb-4` | 搜索行与表格 |
| 壳层 | 由 Layout 组件管理 | 页面勿再加顶栏级 margin |

### Grid & structure

```
┌─ Layout (sidebar + header + tab) ─────────────┐
│  ┌─ Page (auto-content-height) ─────────────┐ │
│  │  [可选 Split 左树]  [Form + Grid / 内容]  │ │
│  │  Drawer / Modal 挂在 Page 内              │ │
│  └───────────────────────────────────────────┘ │
└───────────────────────────────────────────────┘
```

- 列表页：纵向「筛选 → 工具栏+表 → 分页」一条节奏。
- 树+表：`ColPage` 左约 16%–32%，左右同为 `bg-background`。右侧是 Vxe。
- 最大内容宽：跟壳走，**不**为后台页设营销站式 `max-w-7xl` 居中。

### Whitespace

- 中后台靠 **对齐与分区**，不靠大留白。
- 空态：表格 empty 文案短句即可；避免整页插画（除非 fallback 页）。

---

## 6. Depth & Elevation

| Level | Treatment | Use |
|-------|-----------|-----|
| 0 | `--background-deep` | 画布 |
| 1 | `--background` / `--card` + `--border` | 主面板、表格容器 |
| 2 | `--popover` 或 `--accent` 抬起 | 下拉、弹出、hover 行 |
| 3 | `--overlay` + 模态表面 | Drawer / Modal / Dialog |
| float | `--shadow-float`（theme.css） | 少数悬浮元素；表格区默认不靠重阴影 |

暗色模式：**表面阶梯 + 边框**，避免大阴影堆叠。

---

## 7. Do's and Don'ts

### Do

- 新列表页复制黄金样板：`apps/admin/src/views/open/app/`（`Page` + VxeGrid + Drawer + `data.ts`）。
- 颜色/圆角只用 token 与语义 Tailwind 类。
- 主色只作强调；每视图一个明显 Primary CTA。
- 文案走 i18n（`$t`）；列定义、表单 schema 放 `data.ts` / `data-scope.ts`。
- 先读本文件 + [`ui-patterns.md`](../docs/conventions/ui-patterns.md) 再写 UI。

### Don't

- 不要照搬 Linear/Vercel 等营销 DESIGN.md 的暗色画布、巨标字、药丸 CTA。
- 不要在新 CRUD 页手写整页 `NDataTable` + 本地 pagination（与 Vxe 范式分叉）。
- 不要硬编码颜色、随意 `style="color:#…"`。
- 不要大改 `vendor/@core` 只为单页视觉。
- 不要把应用特有布局塞进 `@core`；应用代码放 `frontend/apps/admin/src`。
- 不要同一模块混用 Drawer 与 Modal 作为主编辑容器（选一种）。

---

## 8. Responsive Behavior

| Breakpoint（Tailwind） | 行为 |
|------------------------|------|
| `< md` | 搜索表单单列；树+表可考虑改为上树下表或抽屉挂树（现有页未统一，新增时优先上树下表） |
| `md–lg` | 搜索 2 列 |
| `lg+` | 搜索 3–4 列；树+表左右 Split |

- 触控：按钮用原子控件默认高度；行内 `text` 按钮避免过密（`gap-1`/`gap-2`）。
- 表格：横向滚动可接受；关键列 `minWidth`，操作列固定右侧（Vxe 配置）。
- 抽屉：小屏全宽；表单字段单列优先。

---

## 9. Agent Prompt Guide

### Quick reference

```
canvas:        --background-deep
panel:         --background / --card
text:          --foreground
text-muted:    --muted-foreground
border:        --border
primary:       --primary  (~#0077E6)
danger:        --destructive
radius:        --radius (8px)
stack:         Vue3 + shadcn-vue / Vben 原子 + Page + useVbenVxeGrid + useVbenForm + useVbenDrawer
```

### Ready prompts（可直接对 Agent 说）

1. **新 CRUD 页：**「按 `frontend/DESIGN.md` 与 `docs/conventions/ui-patterns.md` 的 CRUD List 范式，以 `views/open/app` 为样板，实现 xxx 列表（Page + VxeGrid + Drawer + data.ts），不要手写 NDataTable。」
2. **改已有页视觉：**「只使用 design tokens / 语义 Tailwind；禁止硬编码色值；保持与 open/app 工具栏按钮顺序一致。」
3. **树+表页：**「左右 `ColPage`，左约 16%–32% 部门/组织树，右 `useVbenVxeGrid`；两侧 `bg-background`。」
4. **审查：**「对照 DESIGN.md §7 Do/Don't 与 ui-patterns checklist 检查本页。」

### File map for agents

| 需求 | 打开 |
|------|------|
| 视觉规则 | `frontend/DESIGN.md`（本文件） |
| 页面骨架 | `docs/conventions/ui-patterns.md` |
| Token CSS | `frontend/packages/core/design/src/design-tokens/` |
| 工程约定 | `docs/conventions/frontend.md` |
| 原子 / 模式 | `frontend/packages/ui`（`@wyh/ui`）、`frontend/packages/ui-patterns`（`@wyh/ui-patterns`） |
| Naive 迁移清单 | `docs/conventions/frontend-naive-migration.md` |

---

## Changelog (doc)

| Date | Note |
|------|------|
| 2026-09-27 | 初稿大纲：九段 + 现有 token 映射；待确认气质后定稿 |
| 2026-10-09 | 补原子层路径：`src/ui`、`src/ui-patterns`；Naive 分波退出，见迁移清单 |
| 2026-10-10 | 当前栈改为 shadcn-vue / Vben 原子。去掉已删除的 Naive token 桥接入口 |
| 2026-10-10 | 原子与页面模式抽到 `packages/ui`、`packages/ui-patterns`；`#/ui` 仍是别名 |
