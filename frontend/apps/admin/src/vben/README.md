# src/vben — 内联的 Vben 壳

原 workspace 薄包 + `effects`（layouts / hooks / common-ui / plugins 等）。

通过本应用的 [`vben.aliases.mts`](../../vben.aliases.mts) 以 `@vben/*` 解析；**业务代码仍用 `@vben/...` import，不必改业务文件。**

| 目录 | 原包名 |
|------|--------|
| `layouts` | `@vben/layouts` |
| `hooks` | `@vben/hooks` |
| `common-ui` | `@vben/common-ui` |
| `plugins` | `@vben/plugins` |
| `stores` / `locales` / `preferences` / … | 同名 `@vben/*` |

UI 内核在 `frontend/packages/core` 与 `frontend/packages/shell`（`@wyh/*`）。
