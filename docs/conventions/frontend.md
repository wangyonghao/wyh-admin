# Frontend conventions

配合 `.cursor/rules/frontend-vue.mdc`。主应用在 `frontend/apps/admin`（`@wyh/admin`）：新 UI 走 shadcn-vue 原子与 Vben 封装。

**UI 风格与页面骨架（写页面必读）：**

- [`frontend/DESIGN.md`](../../frontend/DESIGN.md) — 视觉 token、Do/Don't、Agent 提示
- [`docs/conventions/ui-patterns.md`](ui-patterns.md) — CRUD / 树表等范式与黄金样板
- [`docs/conventions/frontend-naive-migration.md`](naive-migration-inventory.md) — 迁移记录。`naive-ui` 已从依赖去掉

## UI atoms

新界面优先 shadcn-vue 原子，路径导入，不建总桶：

```ts
import { Button } from '#/ui/button';
import { ToolbarActions } from '#/ui-patterns/toolbar-actions';
// 等价：@wyh/ui/button 、 @wyh/ui-patterns/toolbar-actions
```

- 源码：`frontend/packages/ui`（`@wyh/ui`）、`frontend/packages/ui-patterns`（`@wyh/ui-patterns`）
- 应用在这一版仍可写 `#/ui/<atom>`、`#/ui-patterns/<pattern>`。这两条路径是别名，指向包源码，不是第二套实现
- 原子底子：`@vben-core/shadcn-ui/src/ui`（`cn` + CVA + reka-ui）。颜色用现有 HSL token，经 `apps/admin/src/styles/theme.css` 的 `--color-*`（`@source` 包含 `packages/`、`vendor/` 和 `apps/`）
- 新组件不要导入 `naive-ui`。表格用 Vxe。清单见 [`docs/conventions/frontend-naive-migration.md`](naive-migration-inventory.md)。
- 壳层可以继续用 `@vben-core/shadcn-ui` 根导出里的 `Vben*` 封装；业务页不要再复制一套同名原子

## Layout

```
frontend/
├── apps/
│   ├── admin/           # @wyh/admin：产品后台。pnpm dev 与 pnpm dev:admin 相同
│   │   └── src/         # 业务 + src/vben 壳 + src/styles/theme.css
│   ├── docs/            # @wyh/docs：VitePress，pnpm dev:docs
│   └── design-system/   # @wyh/design-system：只演示 ui / ui-patterns，pnpm dev:ds
├── packages/
│   ├── ui/              # @wyh/ui：原子路径（再导出 @vben-core/shadcn-ui，或本地控件）
│   └── ui-patterns/     # @wyh/ui-patterns：页面组合，按目录导入
└── vendor/@core         # UI 内核（慎改；原子实现已在 shadcn-ui）
```

- 业务代码 → `frontend/apps/admin/src/`（不含 `vben/`）。不要放进 `apps/design-system`
- 壳层封装 → `frontend/apps/admin/src/vben/`
- 应用构建 → `frontend/apps/admin` 的 `vite.config.ts`、env、`tsconfig.json`
- 仓库级检查 → 根目录 `eslint.config.mjs`、`stylelint.config.mjs`、`vitest.config.ts`
- **慎改** `vendor/@core`

## Naming

- 组件名 PascalCase；文件名 kebab-case（如 `user-list.vue`）
- 变量 camelCase；常量 `UPPER_SNAKE`；CSS 类 kebab-case

## Vue / TS

- 优先 `<script setup lang="ts">` + Composition API
- 对象类型优先 `interface`；避免 `any`，不确定用 `unknown`
- 样式优先 Tailwind；复杂样式再用 scoped SCSS

## API modules

- 按域拆文件（如 `api/modules/user.ts`）
- 使用项目既有 `request` 封装；类型与后端契约对齐

## Dependencies

- 新依赖写入根 `pnpm-workspace.yaml` 的 **catalog**，应用内用 `catalog:` 引用
- Node / pnpm 版本见 `frontend/package.json` `engines` 与 `docs/tech-stack.md`

## Quality

- 遵循仓库 ESLint / Stylelint / oxfmt / oxlint；勿绕过 hooks 提交
- 路由 `meta`（title、权限等）与现有模块保持一致风格
