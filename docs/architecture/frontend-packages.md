# 前端包边界

适用范围：`frontend/packages` 与 `apps/admin/src/vben` 的归属。  
最后更新：2026-11-08

包按能力平铺。目录名要能回答「谁可以依赖谁」。`core` / `shell` 回答不了这个问题，已经去掉。

## 现在放哪

```text
packages/ui            原子。button、dialog、tabs、form 字段
packages/ui-patterns   页面组合。confirm、filter、toast、toolbar。只依赖 ui
packages/form-ui       schema 表单运行时
packages/popup-ui      命令式 modal / drawer / alert
packages/design        CSS token
packages/icons         图标
packages/shared        纯函数、通用类型、组件注册表
packages/composables   Vue hook，位于 ui 之下

apps/admin/src/vben/layouts/frame            后台布局框架
apps/admin/src/vben/layouts/navigation       侧栏 / 顶栏导航
apps/admin/src/vben/layouts/workspace-tabs   多路由页签
apps/admin/src/vben/preferences              偏好
apps/admin/src/vben/types                    菜单、页签、布局、路由 meta、用户信息
```

`@wyh/ui/tabs` 是页内选项卡。多路由工作区是 `layouts/workspace-tabs`，数据是 `TabDefinition`（路由）。`@wyh/ui/dropdown-menu` 是菜单原子。后台导航是 `layouts/navigation`，数据是 `MenuRecordRaw`。`@wyh/ui/form` 是字段原子。`@wyh/form-ui` 是 schema 运行时。

## 依赖方向

```text
shared ← composables ← ui ← ui-patterns
                         ← form-ui
                         ← popup-ui
icons ← ui / form-ui / popup-ui / 应用
design ← 应用与 design-system（只引 CSS）
应用 → 以上全部
```

可以：

- 应用依赖任何共享包
- `form-ui`、`popup-ui` 依赖 `ui`
- `ui-patterns` 依赖 `ui`

不可以：

- `ui` 依赖 `form-ui`、`popup-ui`、`ui-patterns`、应用
- `ui` 或 `ui-patterns` 引用 `@wyh/shared/global-state`
- `shared`、`composables`、`icons`、`design` 依赖 `ui`、`form-ui`、`popup-ui` 或应用
- `form-ui` 与 `popup-ui` 互相依赖
- 任何共享包依赖 `apps/admin`

门禁：`frontend/scripts/package-boundaries.test.ts`，随 `pnpm test:unit` 运行。

## 为什么这样分

`design`、`icons`、`shared`、`composables` 在原子下面，表单和弹层在原子上面。把它们都叫 core 或 shell，依赖方向是反的。

布局、导航、多页签只被产品后台使用，并且认识路由和偏好。它们跟偏好一样留在 `apps/admin/src/vben`。design-system 只演示 `ui` 和 `ui-patterns`，不带这套壳。

`form-ui` 和 `popup-ui` 被业务页和文档示例共用，所以留在 `packages`。抽屉里的表单由页面自己组装，两个包互不引用。

`ui-patterns` 保持四个页面组合。表单运行时和弹层运行时不放进去，否则 design-system 会带上 zod 和路由类型。

## 这次搬了什么

| 原来 | 现在 |
|------|------|
| `packages/core/design` | `packages/design` |
| `packages/core/icons` | `packages/icons` |
| `packages/core/shared` | `packages/shared` |
| `packages/core/composables` | `packages/composables` |
| `packages/core/typings` 的 `ClassType`、`Recordable`、`DeepPartial` 等 | `@wyh/shared/types` |
| `packages/core/typings` 的布局、菜单、页签、路由 meta、`BasicUserInfo` | `apps/admin/src/vben/types` |
| `packages/shell/form-ui` | `packages/form-ui` |
| `packages/shell/popup-ui` | `packages/popup-ui` |
| `packages/shell/layout-ui` | `apps/admin/src/vben/layouts/frame` |
| `packages/shell/menu-ui` | `apps/admin/src/vben/layouts/navigation` |
| `packages/shell/tabs-ui` | `apps/admin/src/vben/layouts/workspace-tabs` |
| `ui` 里读注册表的 `VbenCollapsibleParams` | `packages/form-ui` |
| `packages/core/preferences` | 本来就不存在。偏好仍在 `apps/admin/src/vben/preferences` |

`@wyh/layout-ui`、`@wyh/menu-ui`、`@wyh/tabs-ui`、`@wyh/typings` 不再是包。

纯展示的 `VbenButton`、`VbenIcon`、`VbenCollapsible` 仍在 `@wyh/ui/composed`。业务页和壳都在用，没有拆成第三套包。

## 刻意留下的两处

组件注册表 `globalShareState` 仍在 `@wyh/shared/global-state`。表单和弹层都要读它，又不能互相依赖，也不能去依赖应用。原子包禁止引用这个入口。动态字段组件已经跟表单运行时走。

主内容区 id 和布局 CSS 变量名仍在 `@wyh/shared/constants`。弹层要往这块 DOM 上挂载，布局框架要写出同一个 id。字符串常量留在 shared，避免 popup 和布局互相依赖。

面包屑样式 `'background' | 'normal'` 在偏好模型和面包屑组件上各有一份同名联合类型。组件不引用应用类型。

## 验证

在 `frontend/`：

```bash
pnpm install --frozen-lockfile
pnpm test:unit
pnpm -F @wyh/admin run typecheck
pnpm -F @wyh/admin run build
pnpm -F @wyh/docs run build
pnpm -F @wyh/design-system run build
```

浏览器再看三条入口：admin 登录壳、design-system 演示、docs 里实际会转译的示例。只看 HTTP 200 不够。

## 剩余问题

各包 `package.json` 的 `homepage` / `repository` 仍指向 Vben 上游，`repository.directory` 仍是上游目录。这是来源说明，不是本仓库路径。

`@vben/common-ui` 仍是应用内桶，再导出 `form-ui` 和 `popup-ui`。业务页继续从这个别名取 `useVbenForm` / `useVbenDrawer`。这次没有改调用方式。

`pnpm -F @wyh/docs run build` 仍会失败：`preferences-drawer.vue` 里的 `@apply -top-3` 在文档站的 Tailwind 编译里不认识。这个文件和这行样式与 main `66b069cb` 相同，这次没有改它。开发服务器打开 `/components/common-ui/vben-form` 可以渲染表单示例。

Tailwind 入口仍是 `apps/admin/src/styles/theme.css`，别名 `@vben/tailwind-config`。`@wyh/design` 的 `global.css` 和少量组件用 `@reference` 指向它。文件里的 `@source` 会扫应用目录，这次不搬。
