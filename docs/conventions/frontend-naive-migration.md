# Naive UI 迁移清单

原子与页面模式的源码现已在 `frontend/packages/ui`（`@wyh/ui`）和 `frontend/packages/ui-patterns`（`@wyh/ui-patterns`）。产品页面在 `frontend/apps/admin/src`。下文里的 `frontend/src` 是迁移当时的路径；应用导入 `#/ui`、`#/ui-patterns` 仍然可用。

统计范围：`frontend/src` 里 `from 'naive-ui'` 的具名导入。一个文件导入一次记 1。共 **50** 个文件。  
`adapter/component/index.ts` 另有 `naive-ui/es/*` 动态导入，给 `useVbenForm` 供控件，不计入上表，但在下面单独标出。

下面表格是 **Phase 4 之后仍在导入** 的数量。Naive **保持安装**。Vxe / `NDataTable` 与表单适配器未动。

## Phase 2 已换

同一 PR 在 Phase 1 原子层上继续换页面，不改架构。

- **工具栏按钮**：列表页里 `#toolbar-tools` 或明显的主/次操作按钮改为 `#/ui/button`。两个及以上按钮用 `ToolbarActions`。只给这些按钮留白的 `NSpace` 去掉。单按钮工具栏直接放 `Button`。
- **筛选框**：无前缀、字符串、清除后当空的搜索框改为 `FilterInput`（登录日志 IP、操作日志的操作类型与 IP、短信日志手机号）。带 `#prefix`、密码、文本域、写在 `NForm` / `useVbenForm` 里的输入未动。
- **勾选**：代码生成配置抽屉里四个列表勾选改为 `#/ui/checkbox`，绑定是 `v-model`，不是 Naive 的 `v-model:checked`。
- **反馈**：只改 `views/open/app/index.vue`。成功/失败从 `useMessage` 换成 `#/ui/sonner` 的 `toast`。当时 `useDialog` 仍留在该页，Phase 3 改成 `ConfirmAction`。
- **未做 drop-in**：部门/角色上的 `NBadge` 是圆点加自定义颜色，不是 Badge 胶囊。

`NButton` 具名导入 34 → **31**，正文里的 `NButton` 出现次数 265 → **189**。`NSpace` 具名导入 22 → **20**。`useMessage` 27 → **26**。`NCheckbox` 6 → **5**。`NInput` 的文件数仍是 23（换掉的四个框所在文件还留着别的 `NInput`）。

彻底不再导入 `NButton` 的页面：`views/code/generator/index.vue`、`views/monitor/log/login-log.vue`、`views/user/profile/components/operation-logs.vue`。不再导入 `NSpace` 的：代码生成列表、`views/user/message/components/my-message.vue`。

仍跳过：表单适配器与 Vxe `CellLink`；带 `:loading` 的抽屉/弹层底栏；`NPopconfirm` 里的行内文字按钮；`demos/naive`；带前缀的搜索框；角色权限树勾选；`NSwitch` 的 `1/0`。

## Phase 3 已换

- **`loading`**：原子 `Button` 增加可选 `loading`（`LoaderCircle` + `disabled`），和壳上的 `VbenButton` 一样。部门、用户、角色三个抽屉底栏，以及角色权限工具栏的「保存」，已经用上。
- **确认**：`ui-patterns/confirm-action` 包一层 AlertDialog，`ask()` 返回 `Promise<boolean>`。开放应用「重置密钥」改走这条。取消不再弹出失败提示。该页不再导入 `naive-ui`。
- **按钮**：角色列表工具栏、我的消息/公告标题、应用详情复制、用户详情底栏、个人资料基本信息和通知设置里的普通按钮。`NButton` 具名导入 31 → **19**，正文出现次数 189 → **131**。`NSpace` 20 → **17**。
- **分割线**：视图里的 `NDivider` 具名导入 5 → **0**，换成 `#/ui/separator`。适配器里的动态导入还在。
- **勾选 / 徽标**：系统配置里 8 个布尔勾选改为 `Checkbox`（`v-model`）。角色用户性别改为 `Badge`。部门状态圆点 `NBadge` 仍留着。`NCheckbox` 5 → **4**，`NBadge` 2 → **1**。`useDialog` 9 → **8**。

仍跳过：`NPopconfirm` 行、菜单/通知/选项/系统配置里还带加载的底栏、`NCheckboxGroup`、角色权限树里的勾选、`NSwitch` 的 `1/0`、`demos/naive`。这些底栏在 Phase 4 换完。

## Phase 4 已换

- **加载底栏**：菜单弹层、通知表单（草稿/发布）、选项抽屉、系统配置保存和邮件抽屉，以及发送验证码按钮，改为带 `loading` 的原子 `Button`。通知范围/定时/置顶那几组只包按钮的 `NSpace` 改成 flex。
- **确认**：`ConfirmAction` 接了部门删除、菜单删除、用户删除与重置密码、选项删除、在线用户强退（单条和批量）、短信日志删除、定时任务删除。都是「布尔确认后再请求」。选项「清除缓存」仍是 `dialog.info`。定时任务「立即执行」仍是 `NPopconfirm`。
- **数量**：`NButton` 19 → **15**（正文 131 → **96**）。`NSpace` 17 → **11**。`useDialog` 8 → **4**。`NPopconfirm` 7 → **5**。

留给当时的下一波：表单适配器、`NSwitch` 的 `1/0`、`NDataTable` / Vxe、`demos/naive`、字典 `NTag`、成组勾选、部门圆点徽标、公告列表和短信日志表格里的渲染函数确认、个人设置里的重置/解绑确认、角色页和「全部已读」、配置页「发送测试邮件」。Naive 包不删。这些确认和一部分开关、按钮、表单控件在下面这一波换了。

## 下一波已换

口径和上面一样：`from 'naive-ui'` 的具名导入记文件数。正文 token 含闭合标签，不含 import 行。本波开始时按这个口径复测：`NButton` 15 / 正文 95（Phase 4 记正文 96），`useDialog` 4，`NPopconfirm` 5 / 正文 8，`NSwitch` 具名 6 / 正文 20，`NSpace` 具名 11。

- **确认**：`useDialog` 4 → **0**，`NPopconfirm` 5 → **0**。选项清除缓存、配置测试邮件、消息全部已读、角色删除与取消分配、公告删除、短信日志表格删除、任务立即执行、偏好重置、三方账号解绑，都是 `ConfirmAction` 返回 true 后再请求。
- **行内按钮**：菜单、部门、用户、文件、公告操作列，公告表单的范围 / 定时 / 置顶，选项行，操作日志「详情」，任务行的执行 / 编辑 / 日志，改为 `#/ui/button`。文字用 `variant="link"`，图标用 `ghost`，删除加上 `text-destructive`。`NButton` 具名 15 → **4**，正文 95 → **59**。剩下 `adapter/vxe-table.ts` 的 `CellLink`、安全设置弹层底栏、`demos/naive`、`demos/form`。
- **开关**：`#/ui/switch` 在布尔之外接受 `checked-value` / `unchecked-value`（默认 `true` / `false`，用 `===`）。任务状态 `1/0`、菜单外链 / 可见 / 状态 `1/0`、部门状态 `1/2`，以及选项、偏好、通知里的布尔开关已经接上。页面不再具名导入 `NSwitch`。表单适配器里的 `Switch` 仍是 Naive。
- **表单适配器**：只换了能对上 `value` / `checked` 的切片。`DefaultButton` 与 `PrimaryButton`、单行 `Input`、`Checkbox` 走原子控件。`emptyStateValue` 仍是 `null`。单行输入把 `null` 画成空字符串，用户清空后回写空字符串，避免 zod 的 `string()` 收到 `null`。文本域、`showWordLimit`、密码显隐仍回退 `NInput`，回写保持 Naive 原来的值（可以是 `null`）。
- **表单里明确没动**：`Select`。套件里的 Select 是 Trigger / Content / Item，页面 schema 用的是 `options`，还带 `clearable`、`multiple`、`filterable`，空值也是 `null`。`CheckboxGroup`、`RadioGroup`、`Switch`、`InputNumber`、日期时间、树选择、上传同样留在 Naive。
- **未做**：角色权限树里的勾选、部门状态圆点 `NBadge`、`NCheckboxGroup`、`NDataTable` / Vxe、`demos/naive`。安全设置改密 / 改手机 / 改邮箱的弹层按钮仍是 `NButton`。

`NSpace` 具名 11 → **9**。用户操作列和短信日志操作列不再用它包按钮。Naive 包不删。Vxe 仍是表格默认。

## 本波已换

口径和上面一样。本波开始时：`NButton` 4（正文 59），`NCheckbox` 4，`NCheckboxGroup` 3，`NBadge` 1，页面 `NSelect` 12，`NSpace` 9。仍有 **47** 个文件导入 `naive-ui`。

- **表单 Select**：注册表里的 `Select` 改为适配器 `FormSelect`。单选和多选走 `#/ui/select`（reka Select 的 `multiple`）。`options` 的数字、布尔、字符串原样回写，`1` 和 `"1"` 不会并成一个。`null` 显示为未选；`clearable` 和多选清空都回写 `null`。`0` 和 `false` 仍是已选。`filterable` 用 Popover 加搜索框：套件下拉的可视高度跟触发器绑在一起，搜索框放不进去。应用路径补了 `select` 和 `popover`。
- **没换的 Select**：`ApiSelect` 仍包 `NSelect`。`ApiComponent` 自己管加载和 `arrow` 插槽，和 schema 上的 `Select` 不是同一条。页面上手写的 `NSelect` 仍是 **12** 个文件（公告接收人、用户编辑、任务执行器这类可搜索多选没进注册表）。
- **勾选**：角色权限树的全选、菜单、权限和「节点关联」改为 `#/ui/checkbox`，全选保留半选。权限矩阵仍是 `NDataTable`。它有菜单列和权限列，父子关联也是页面自己的规则；套件里的树只有一个节点插槽，整表换掉会改保存结果。页面上的 `NCheckboxGroup`（通知方式、通知类型、任务星期）和表单 `CheckboxGroup` 改为 Checkbox 列表。用户取消最后一项回写空数组，和原来的分组一样；`null` 只来自表单重置，显示为全不选。`NCheckbox` 4 → **0**。`NCheckboxGroup` 3 → **0**。
- **表格周边**：`CellLink` 改为 `variant="link"` 的原子按钮。`CellImage` 仍是 `NImage`。部门状态从圆点改为 `success` / `destructive` 的 Badge。安全设置的操作按钮和三个弹层底栏，以及表单演示页的两个按钮，改为原子按钮。`NButton` 4 → **1**，只剩 `views/demos/naive/index.vue`。`NBadge` 1 → **0**。`NSpace` 9 → **8**（公告表单不再用它包勾选）。
- **NDataTable**：14 个文件都留着，没有整页改成 Vxe，也没有新做 shadcn Table。在线用户、公告、用户、部门、角色、菜单、文件、日志都是手写分页或树表，不是薄包装。`views/demos/table` 是示例。Vxe 仍是 CRUD 默认。权限树只换了勾选。
- **`demos/naive`**：仍是 Naive 组件陈列，这一波不改。

Naive 包不删。

## 页面 Select 与薄表

口径不变。本波开始时：页面 `NSelect` **12**，`NDataTable` **14**，`NSpace` **8**，`from 'naive-ui'` 的文件 **47**。

- **手写下拉**：12 个文件里的 `NSelect` 都改成适配器 `FormSelect`（`#/ui/select`，空值仍是 `null`）。单选、`clearable`、`multiple`、`filterable` 和页面上的 `style` / `class` 宽度沿用原来的字段。多选清空在适配器里是 `null`；公告接收人和用户角色在写回页面前收成数组，空选择不把 `null` 交给接口。代码生成的字段类型带 `tag`：输入列表外的名字会当成字符串提交，命中已有选项仍回写原值。个人日志筛选里原来的「全部」（`value: null`）不是合法选项，改成占位符加清除。
- **ApiSelect**：注册表里的内层控件从 `NSelect` 换成 `FormSelect`。加载中仍由 `ApiComponent` 往 `arrow` 插槽放转圈；可搜索时这个插槽换掉箭头，普通下拉时转圈出现在箭头旁边（套件触发器自带箭头，没有替换入口）。`ApiTreeSelect` 仍是 `NTreeSelect`。
- **表格**：系统选项、在线用户两页改成 `useVbenVxeGrid`（搜索 schema、远程分页、行操作）。选项状态用 Badge。在线用户当前登录不可勾选、不可强退；批量强退留在工具栏。Vxe 仍是 CRUD 默认。没有再抽一套空态/加载组件，这两页用表格自带的加载和空数据。
- **NSpace**：公告通知方式上只包标签的 `NSpace`，以及个人日志筛选那一行，改成 flex。`NSpace` 8 → **6**。`demos/naive` 仍是组件陈列，不改。
- **数量**：页面 `NSelect` 12 → **0**。`NDataTable` 14 → **12**。`useMessage` 26 → **24**，`NInput` 23 → **21**，`NTag` 18 → **17**，`NIcon` 12 → **10**。后四项是因为选项页和在线用户页不再导入 naive-ui，不是把输入框、标签或图标换成原子控件。仍有 **45** 个文件导入 naive-ui。Naive 包不删。

仍不换表的页面，以及原因：

| 页面 | 原因 |
|------|------|
| `system/role/components/role-permission.vue` | 菜单列加权限列，父子关联是页面自己的规则 |
| `system/menu/index.vue`、`system/department/index.vue` | 树表，展开状态和子节点不在现有 Vxe CRUD 样板里 |
| `system/user/index.vue` | 左树右表，表还嵌在分割布局里 |
| `system/role/index.vue` | 角色列表里再嵌用户分配表和分页 |
| `system/file/index.vue` | 上传、分类级联和批量勾选绑在同一张手写表上 |
| `system/notice/index.vue` | 时间范围、预览抽屉、多种标签列，不是薄列表 |
| `monitor/log/login-log.vue`、`operation-log.vue`、`sms-log.vue` | 三个列表塞在同一个 Tab 里，高度跟着 Tab 走；只改其中一张会和旁边两张脱节 |
| `user/profile/components/operation-logs.vue` | 个人中心里的一块，不是独立 `Page` |
| `demos/table/index.vue` | Naive 表格示例 |

## 表格、表单适配器与徽标

口径不变。本波开始时：`NDataTable` **12**，`NTag` **17**，`NSpace` **6**，`from 'naive-ui'` 的文件 **45**。Naive 包不删。

- **表格**：公告、登录日志、操作日志、短信日志、文件列表改成 `useVbenVxeGrid`。公告和文件是独立 `Page`。三张日志在同一个日志页的 Tab 里一起换，Tab 内容区拉满剩余高度，避免只换一张和旁边脱节。搜索走表单 schema，行操作和状态走插槽。公告的编辑/预览抽屉、操作日志详情抽屉、文件的上传、分类级联和行内菜单留在表格旁边。文件勾选只作用于当前页。`NDataTable` 12 → **7**。`DataTableColumns` 11 → **6**。
- **仍不换表**：

| 页面 | 原因 |
|------|------|
| `system/role/components/role-permission.vue` | 菜单列加权限列，父子关联是页面自己的规则 |
| `system/menu/index.vue`、`system/department/index.vue` | 树表，展开状态不在现有 Vxe CRUD 样板里 |
| `system/user/index.vue` | 左树右表，表嵌在分割布局里 |
| `system/role/index.vue` | 角色列表里再嵌用户分配表和分页 |
| `user/profile/components/operation-logs.vue` | 个人中心里的一块，不是独立 `Page`；日期筛选仍是 `NDatePicker` |
| `demos/table/index.vue` | Naive 表格示例 |

- **表单适配器**：文本域、`autosize`、密码显隐、字数统计改为 `#/ui/textarea` / `#/ui/input`。`null` 显示成空字符串，用户清空后回写空字符串，和单行输入一样；重置仍由外层写成 `null`。成对输入（`pair`）套件没有对应控件，仍回退 `NInput`。`RadioGroup` 走 `#/ui/radio-group`，`1` / `true` 原样回写，`null` 是未选。`isButton` 和 `optionType: 'button'` 画成按钮样式。`InputNumber` 走 `#/ui/number-field`，清空回写 `null`，`0` 仍是数字。`Switch` 走 `#/ui/switch`，`null` 显示为关，`checked-value` / `unchecked-value` 用严格相等。注册表补了 `Textarea`。
- **日期 / 树选择**：`@vben-core/shadcn-ui` 没有 DatePicker，也没有带下拉和选项值的 TreeSelect（只有树视图）。`DatePicker`、`TimePicker`、`TreeSelect`、`ApiTreeSelect` 继续动态加载 Naive。
- **徽标**：`success` / `warning` / `error` / `default` 对上 Badge 的 `success` / `warning` / `destructive` / `secondary`。`primary` 和 `info` 没有单独色，用 Badge `default`。对不上的名字不猜颜色，字典项退回 `secondary`。字典颜色读 `tagType`，没有则读 `extra.color`。页面上的 `NTag` 具名导入 17 → **0**。
- **NSpace**：个人设置里只做纵向留白的 `NSpace` 改成 flex。`NSpace` 6 → **1**，只剩 `demos/naive`。`demos/naive` 仍是组件陈列，不改。

仍有 **40** 个文件导入 naive-ui（`utils/render.tsx` 不再导入 `NTag`）。`useMessage` 24 → **21**，`NInput` 21 → **16**，`NIcon` 10 → **5**，`NCard` 12 → **10**，`NDatePicker` 5 → **2**。后几项主要是公告、日志和文件页不再为了表格去导入这些控件。

## 日期、树选择与树表

口径不变。本波开始时：`NDatePicker` **2**，`NTreeSelect` **3**，`NDataTable` **7**，`NSplit` **3**，`NTree` **2**，`from 'naive-ui'` 的文件 **40**。Naive 包不删。

- **日期**：`#/ui/date-picker` 用已有 Popover（reka）加本地月历，不包 Naive。表单注册表的 `DatePicker` 走这个控件，所以公告、登录日志、操作日志的 schema 日期范围一起换掉。`date` / `datetime` / `daterange` / `datetimerange`，以及 `showTime`。没有 `valueFormat` 时提交时间戳。有 `valueFormat` 时提交格式化字符串；`yyyy-MM-dd` 和 `YYYY-MM-DD` 都认，开放应用失效时间因此对得上 `LocalDateTime`。空值、没选完的范围、清除都是 `null`。`0` 仍是时间戳。点「确定」时如果范围只选了一端，不把已有值写成 `null`。周、月、季和快捷范围没有做：`@vben-core/shadcn-ui` 没有 Calendar 原子，也不再引入新的日期库；页面上没有这些模式。
- **树选择**：表单 `TreeSelect` 和 `ApiTreeSelect` 改为 `FormTreeSelect`。选项键优先 `keyField`，没有再退回 `value` / `key`，所以 Api 转成 `value` 之后仍能对上。`0` 是顶级菜单，不是空。清除和多选清空回写 `null`。搜索只留下命中的节点和它们的上级。没有用 `VbenTree` 做下拉：它在选项还没到时会把对不上的值写成空。菜单上级、部门上级、用户部门都改成这个控件。
- **成对输入**：表单 `Input` 的 `pair` 改成两个原子输入。`null` 画成两段空字符串；两段都空回写 `null`；只填一段时另一段是空字符串。文本域、字数和密码显隐仍走上一波的原子控件。当前 schema 没有 `pair`。
- **部门树表**：`system/department/index.vue` 改为 `useVbenVxeGrid`，`treeConfig.childrenField = children`，没有分页。搜索仍是输入后防抖查询。每次查出结果默认展开。导出的查询参数改为 `keyword`，和列表、后端 `DeptQuery` 一致。
- **菜单树表**：`system/menu/index.vue` 同样改为 Vxe 树。名称、状态走表格自己的搜索表单。弹层里的上级菜单改成 `FormTreeSelect`。弹层其余字段仍是 Naive 表单。类型、可见、状态用 Badge。
- **用户左树右表**：分割从 `NSplit` 改为已有的 `ColPage`（reka Splitter）。左侧部门树改为 `VbenTree`，按名称过滤时保留上级。点同一节点可取消，右侧列表跟着刷新。搜索把已选部门滤掉时，不清掉右侧的部门条件。右侧用户表仍是 `NDataTable`：分页、下拉操作和重置密码弹层还绑在这张表上。状态用 Badge。
- **个人日志**：`user/profile/components/operation-logs.vue` 改成一块 Vxe（远程分页）。筛选里的日期范围走新的 DatePicker。状态用 Badge。这一页不再导入 naive-ui。
- **数量**：`NDatePicker` 2 → **0**。`NTreeSelect` 3 → **0**。`NDataTable` 7 → **4**。`DataTableColumns` 6 → **3**。`NSplit` 3 → **2**。`NTree` 2 → **1**。`NInput` 16 → **15**（部门列表搜索改 `FilterInput`）。`NIcon` 5 → **4**。`NCard` 10 → **9**（菜单列表不再用）。`NTag` 仍是 **0**。`useMessage` 仍是 **21**。`NSpace` 仍是 **1**。仍有 **39** 个文件导入 naive-ui。Naive 包不删。

仍不换的，以及原因：

| 页面 | 原因 |
|------|------|
| `system/role/components/role-permission.vue` | 菜单列加权限列，父子关联是页面自己的规则；整表换成树会改保存结果 |
| `system/role/index.vue` | 左侧角色列表加右侧 Tab，用户分配表还有独立分页。不是一张树表 |
| `system/user/index.vue` | 左树和分割已换。右侧仍是手写分页、下拉操作和重置密码弹层 |
| `system/config/index.vue` | 配置分组用 `NSplit`，不是树表 |
| `code/generator/modules/gen-preview-modal.vue` | 预览弹层里的目录树，选中后要拼路径 |
| `demos/table/index.vue`、`demos/naive` | 组件陈列，这一波不改 |

## 用户表、角色分配与表单壳

口径不变。本波开始时：`NDataTable` **4**，`DataTableColumns` **3**，`NSplit` **2**，`NPagination` **1**，`NTimePicker` **1**，`NDropdown` **3**，`from 'naive-ui'` 的文件 **39**。Naive 包不删。

- **用户右表**：`system/user/index.vue` 右侧改为 `useVbenVxeGrid`。左侧部门树和 `ColPage` 不动。搜索、远程分页（10 / 20 / 50）、详情、修改、更多里的重置密码和删除都还在。重置密码仍是确认后弹出只显示一次的新密码。点部门回到第一页。状态仍是 Badge。行上的「更多」改成 `#/ui/dropdown-menu`。部门搜索框仍带前缀，还是 `NInput`。
- **角色用户分配**：`system/role/index.vue` 的分配表改为 Vxe，远程分页和取消分配还在。「分配用户」仍是按当前关键字重新查询，没有新做分配弹层。左右栏从 `NSplit` 改成 `ColPage`（左 30%，可拖到 20%–35%）。功能权限那个 Tab 没动。
- **权限矩阵**：`role-permission.vue` 仍是 `NDataTable`。它有菜单列和权限列，勾选还要按 `menuCheckStrictly` 把父子一起写进 `menuIds`。`VbenTree` 只有一个节点插槽，换成它会改保存结果。这一波不换。
- **表单壳**：`TimePicker` 用 `#/ui/input` 的 `type="time"`。`null` 显示为空，清空回写 `null`，`0` 仍是时间戳。没有 `valueFormat` 时提交时间戳，有则提交格式化字符串。任务编辑里的 `HH:mm` 走这个控件。12 小时制和禁用时刻没做。`Upload` 绑 `fileList`，没文件回写 `null`，不回写 `[]`。单选会替换列表，`max` 封顶。带自定义请求的页面上传（文件列表、头像、`image-upload` 的 `n-upload`）仍是 Naive。`Divider` 走 `#/ui/separator`，`dashed` 画虚线。`Space` 是 flex，`small` / `medium` / `large` 为 8 / 12 / 16，默认换行。
- **没动**：配置页 `NSplit` 用的是 200–320 像素，`ColPage` 只吃百分比，换了宽度会对不上。代码生成预览树要按文件类型画图标，选中后再打开内容，不是把 `VbenTree` 套上去就能保持现在的点选。`demos` 仍是陈列。
- **数量**：`NDataTable` 4 → **2**（权限矩阵、`demos/table`）。`DataTableColumns` 3 → **1**。`NSplit` 2 → **1**（只剩配置页）。`NPagination` 1 → **0**。`NTimePicker` 1 → **0**。`NDropdown` 3 → **2**。`NUpload` 具名仍是 **2**。仍有 **39** 个文件导入 naive-ui。适配器不再动态加载 Divider、Space、TimePicker、Upload。Naive 包不删。

## 权限矩阵、像素分割与页面上传

口径不变。本波开始时：`NDataTable` **2**，`NSplit` **1**，`NUpload` **2**，`from 'naive-ui'` 的文件 **39**。Naive 包不删。

- **权限矩阵**：`role-permission.vue` 不再用 `NDataTable`。勾选和保存抽到 `permission-tree.ts`，并用单测锁住结果。`menuIds` 仍是已选菜单 id，加上 `checked` 的权限 id，顺序仍是深度优先。`menuCheckStrictly` 为真时，勾菜单会连带下级菜单和权限；勾权限只带上它所在的菜单，不带上级；取消权限不取消菜单。关闭关联时，菜单和权限各自独立。已保存的 key 回填不级联。表头全选仍只看菜单列，不看权限列。页面是 Checkbox 加表格。折叠只藏行，不改保存结果。成功提示改为 toast。
- **配置分割**：`ColPage` 增加 `left-size-unit` / `right-size-unit`（`%` 或 `px`，默认 `%`）。用户页和角色页仍是百分比，宽度不变。一侧是像素时，另一侧的百分比默认宽度不参与初始布局，用来吃掉剩余空间。配置页左栏 200px，可拖到 200–320px，不再用 `NSplit`。
- **页面上传**：`#/ui/upload` 的 `FileUpload` 只负责选出 `File`。文件列表仍调用 `fileApi.upload`，头像仍调用 `userProfileApi.uploadAvatar`，`image-upload` 仍调用 `fileApi.upload` 并回写 fileId。`multiple` 默认 false，和原来的单文件按钮一样。表单 schema 的 Upload 不动。
- **没动**：代码生成预览树。选中只在顶层 key 里找节点，目录还会合并，图标按扩展名画。换成 `VbenTree` 会改点选。`demos/naive` 和 `demos/table` 仍是陈列，用来留住 provider。
- **数量**：`NDataTable` 2 → **1**（只剩 `demos/table`）。`DataTableColumns` 1 → **0**。`NSplit` 1 → **0**。`NUpload` 2 → **0**。`useMessage` 21 → **20**。`role-permission.vue` 和 `components/image-upload.vue` 不再导入 naive-ui。仍有 **37** 个文件 `from 'naive-ui'`。Naive 包不删。

仍在 `from 'naive-ui'` 的文件：

`adapter/component/index.ts`、`adapter/naive.ts`、`adapter/vxe-table.ts`、`app.vue`、`components/icon-select.vue`、`components/password-modal.vue`、`components/profile-modal.vue`、`views/code/generator/modules/gen-config-drawer.vue`、`views/code/generator/modules/gen-preview-modal.vue`、`views/demos/form/basic.vue`、`views/demos/naive/index.vue`、`views/demos/table/index.vue`、`views/monitor/log/operation-log.vue`、`views/monitor/sms/log/index.vue`、`views/open/app/modules/detail.vue`、`views/schedule/job/edit-drawer.vue`、`views/schedule/job/index.vue`、`views/system/config/index.vue`、`views/system/department/department-drawer.vue`、`views/system/department/index.vue`、`views/system/file/index.vue`、`views/system/menu/index.vue`、`views/system/notice/components/notice-form.vue`、`views/system/notice/index.vue`、`views/system/option/components/option-edit-drawer.vue`、`views/system/role/components/role-edit-drawer.vue`、`views/system/role/index.vue`、`views/system/user/components/user-detail-drawer.vue`、`views/system/user/components/user-edit-drawer.vue`、`views/system/user/index.vue`、`views/user/message/components/my-message.vue`、`views/user/profile/components/basic-info.vue`、`views/user/profile/components/notification-settings.vue`、`views/user/profile/components/preferences-settings.vue`、`views/user/profile/components/profile-summary.vue`、`views/user/profile/components/security-settings.vue`、`views/user/profile/index.vue`。

`adapter/component/index.ts` 另外还动态加载 `naive-ui/es/input` 给图标选择器。

还不能卸掉 `naive-ui`。`app.vue` 里的 `NConfigProvider`、`NMessageProvider`、`NDialogProvider`、`NNotificationProvider` 不是最后的硬依赖。卸掉 provider 之后，下面这些仍会直接导入这个包：

- `useMessage` 还有 **20** 个文件。`adapter/naive.ts` 的 `createDiscreteApi` 在组件外面发消息。
- 手写表单：`NForm` / `NFormItem`、`NInput`、`NInputNumber`、`NRadioGroup`。
- `NDrawer`、`NModal`、`NCard`、`NAlert`、`NCascader`（文件分类）、`NTree`（代码生成预览）、`NDropdown`、`NTabs`。
- `NDataTable` 只剩 `views/demos/table/index.vue`。`NImage` 还在 Vxe `CellImage`。
- `views/demos/naive/index.vue` 仍是 `NButton`、`NSpace`、`useNotification` 的陈列。

包继续安装。

## 提示、手写表单与抽屉

口径不变。本波开始时：`useMessage` **20**，`NForm` / `NFormItem` **10 / 10**，`NDrawer` **8**，`NModal` **4**，`NCascader` **1**，`NImage` **2**，`from 'naive-ui'` 的文件 **37**。Naive 包不删。

- **提示**：`#/ui-patterns/toast` 包一层 Sonner。`success` / `error` / `warning` / `info` / `loading` 的第一个参数是文案，第二个可以是毫秒，或 `{ duration, description }`。`duration: 0` 不自动关闭。页面上的 `useMessage`，以及 `http`、登录、路由、下载这些在组件外面的 `message`，都改走它。`adapter/naive.ts` 的 `createDiscreteApi` 删了，没有别的地方再用 dialog / notification / loadingBar。`views/demos/naive` 仍调用 `useMessage` 和 `useNotification`，所以 `app.vue` 里的 Message / Dialog / Notification provider 还留着。
- **手写表单**：系统选项、角色、部门三个抽屉改成 `useVbenForm`，提交和取消用抽屉自己的按钮。空值仍是 `null`。选项类型在编辑时禁用；角色数据范围仍是按钮样式的单选；部门状态仍是 `1/2`。任务编辑里的名称、Cron、备注、每月日期和间隔改成原子输入和数字框，频率仍按模式显隐。个人资料的基本信息改成 Label + Input + 单选，头像弹层改成 `useVbenModal`。偏好里的主题、布局、内容宽度和首页路径，以及通知里的告警级别，改成同一套单选和输入。
- **抽屉 / 弹层**：公告编辑和预览、操作日志详情、用户详情、邮件配置改成 `useVbenDrawer`。用户重置密码后的新密码改成 `useVbenModal`。这些页原来用 `visible` 打开的，父页面的打开方式没改，除了公告和邮件配置改成直接 `open()`。
- **文件分类**：`NCascader` 改成 `FormTreeSelect`，键用选项的 `value`。清除后仍回到「全部」。文档这一组里，父节点和子节点「全部」都是 `DOCUMENT`，触发器显示先碰到的「文档」。没有悬停展开的级联面板。
- **图片和图标**：Vxe `CellImage` 改成 `img`。表单图标选择器不再把内层输入设成 `NInput`，用选择器自带的输入。`components/icon-select.vue` 的两个输入改成原子 Input；图标网格、弹出层和滚动条仍是 Naive。
- **没动**：菜单弹层、用户编辑抽屉、公告表单、安全设置三个弹层、系统配置主体，仍是手写 `NForm`。菜单弹层里还有图标选择。代码生成预览树和字段表里的输入没动。`demos/naive`、`demos/table` 仍是陈列。`components/password-modal.vue` 和 `components/profile-modal.vue` 没有页面引用，模板里仍是全局 `n-modal` / `n-form`。

`useMessage` 20 → **1**（只剩 `views/demos/naive/index.vue`）。`NForm` / `NFormItem` 10 → **5**。`NDrawer` / `NDrawerContent` 8 → **1**（用户编辑）。`NModal` 4 → **2**（菜单、安全设置）。`NInput` 15 → **8**。`NInputNumber` 6 → **2**（菜单、系统配置）。`NRadioGroup` 7 → **3**。`NCascader` 1 → **0**。`NImage` 2 → **0**。`NCard` 仍是 **9**。仍有 **25** 个文件 `from 'naive-ui'`。Naive 包不删。

仍在 `from 'naive-ui'` 的文件：

`adapter/component/index.ts`（只剩类型）、`app.vue`、`components/icon-select.vue`、`components/password-modal.vue`、`components/profile-modal.vue`、`views/code/generator/modules/gen-config-drawer.vue`、`views/code/generator/modules/gen-preview-modal.vue`、`views/demos/form/basic.vue`、`views/demos/naive/index.vue`、`views/demos/table/index.vue`、`views/open/app/modules/detail.vue`、`views/schedule/job/index.vue`、`views/system/config/index.vue`、`views/system/file/index.vue`、`views/system/menu/index.vue`、`views/system/notice/components/notice-form.vue`、`views/system/role/index.vue`、`views/system/user/components/user-detail-drawer.vue`、`views/system/user/components/user-edit-drawer.vue`、`views/system/user/index.vue`、`views/user/profile/components/notification-settings.vue`、`views/user/profile/components/preferences-settings.vue`、`views/user/profile/components/profile-summary.vue`、`views/user/profile/components/security-settings.vue`、`views/user/profile/index.vue`。

还不能卸掉 `naive-ui`。卸掉 provider 之后，下面这些仍会直接导入这个包：

- `app.vue` 的 `NConfigProvider` 还要托住剩下的 Naive 控件；`NMessageProvider` / `NDialogProvider` / `NNotificationProvider` 只为 `demos/naive` 的 `useMessage` 和 `useNotification`。
- 手写表单还在：菜单弹层、用户编辑、公告表单、安全设置、系统配置。`NCard` 还有 **9** 个文件。
- `NDropdown`（角色列表、文件行菜单）、`NTabs`（角色页、个人中心）、`NTree`（代码生成预览）、`NDataTable`（只剩 `demos/table`）、`NAlert`、`NEmpty`、`NDescriptions`（开放应用详情、用户详情）。
- `components/password-modal.vue`、`components/profile-modal.vue` 没有引用，但仍写着 Naive 表单。
- `adapter/component/index.ts` 的类型仍从 `naive-ui` 引入，运行时不再加载 `naive-ui/es/input`。

## 表单壳、下拉和提示提供者

口径不变。本波开始时：`NForm` / `NFormItem` **5 / 5**，`NDrawer` **1**，`NModal` **2**，`NDropdown` **2**，`NTabs` / `NTabPane` **2 / 2**，`NCard` **9**，`NAlert` **1**，`NEmpty` **1**，`NDescriptions` **2**，`useMessage` **1**，`from 'naive-ui'` 的文件 **25**。Naive 包不删。

- **手写表单**：菜单弹层、用户编辑抽屉、公告表单、安全设置三个弹层、系统配置主体和邮件抽屉里的表单改成 `useVbenForm`。菜单上级仍是字符串 `'0'`。外链地址和路由组件在表单里分成两个字段，提交时按类型写回原来的 `component`。公告定时发布时间仍是 UTC 的 `YYYY-MM-DD HH:mm:ss`。安全设置的验证码按钮仍没有发送动作。配置页把表单值同步回原来的 ref，保存和邮件摘要还读这些 ref。邮件表单在抽屉打开之后再写入，避免抽屉没挂载时 `setValues` 一直等待。
- **下拉和标签**：角色列表和文件行菜单改成 `#/ui/dropdown-menu`。角色页和个人中心改成 `#/ui/tabs`。这两个页面的标签内容用 `force-mount` 留在页面上，切走再回来时权限树、用户表和个人资料不会被卸掉。角色搜索改成 `FilterInput`，清除后仍是空字符串。
- **卡片和说明**：没有边框的 `NCard` 改成 `rounded-xl bg-card` 的块。表单演示和系统配置用 `#/ui/card`。开放应用详情和用户详情改成描述列表。邮件验证成功改成带边框的提示块。安全设置的空列表改成一段文字。
- **提示壳**：`views/demos/naive` 的按钮、消息和通知改成原子按钮和 `#/ui-patterns/toast`。`app.vue` 去掉 `NMessageProvider`、`NDialogProvider`、`NNotificationProvider`。`NConfigProvider` 还在，用来托住剩下的 Naive 控件。
- **删掉**：`components/password-modal.vue`、`components/profile-modal.vue` 没有页面引用，已删除。
- **没动**：代码生成预览的 `NTree`（连同旁边的卡片和滚动条）、代码生成步骤和输入、用户页左侧带图标的部门搜索、定时任务时间线弹出层、图标选择器的弹出层。`demos/table` 仍是 `NDataTable` 陈列。

`NForm` / `NFormItem` / `NDrawer` / `NModal` / `NDropdown` / `NTabs` / `NAlert` / `NEmpty` / `NDescriptions` 都是 **0**。`NCard` **1**（代码生成预览）。`NInput` **2**（用户部门搜索、代码生成配置）。`NTree` **1**。`NDataTable` **1**。`useMessage` / `useNotification` / `useDialog` 在页面上是 **0**（`vben/plugins/types.ts` 里只剩一个可选字段名）。仍有 **8** 个文件 `from 'naive-ui'`。Naive 包不删。

仍在 `from 'naive-ui'` 的文件：

`adapter/component/index.ts`（只剩类型）、`app.vue`（`NConfigProvider` 和主题）、`components/icon-select.vue`、`views/code/generator/modules/gen-config-drawer.vue`、`views/code/generator/modules/gen-preview-modal.vue`、`views/demos/table/index.vue`、`views/schedule/job/index.vue`、`views/system/user/index.vue`。

还不能卸掉 `naive-ui`，也不能去掉 `NConfigProvider`。挡住的是：

- 代码生成预览的 `NTree`、`NCard`、`NScrollbar`、`NIcon`。树选择器已有，这棵预览树的数据和右键还没对上，本波不换。
- 代码生成配置的 `NInput`、`NSteps`。
- 用户页左侧部门搜索的 `NInput` 和前缀 `NIcon`。
- 定时任务的 `NPopover`、`NTimeline`。
- 图标选择器的 `NPopover`、`NScrollbar`、`NIcon`。
- `demos/table` 的 `NDataTable`。这是剩下的表格陈列，不是产品表单。
- `adapter/component/index.ts` 的控件 props 类型仍从 `naive-ui` 引入。

应用路径补了 `tabs` 和 `card`。

## 去掉 Naive 包

口径不变。本波开始时：`from 'naive-ui'` 的文件 **8**（`app.vue`、代码生成预览、代码生成配置、用户部门搜索、定时任务、图标选择器、`demos/table`、`adapter/component/index.ts`）。

- **预览树**：`gen-preview-modal.vue` 改为 `VbenTree`。文件 key 仍是文件名，目录 key 仍是自增字符串，目录合并规则没改。点文件（包括子目录里的文件）打开右侧内容；点目录只选中目录，不换预览。以前的点击只在顶层 key 里找，子目录里的文件点了不会换内容。图标按目录和后缀用 Lucide。原来的 `SvgFile*` 没有导入，画不出来。右侧改成 `#/ui/card`，内容区自己滚动。标题链接不再读 `--n-text-color`。
- **生成配置**：两个步骤改成和当前面板一致的指示。第 0 步是生成配置，下一步之后第 1 步是字段配置。字段名和注释改成 `#/ui/input`。`null` 显示成空字符串，清空后回写空字符串。
- **部门搜索**：左侧搜索改成带搜索图标的 `Input`。清除后仍是空字符串，和 `FilterInput` 一样。
- **下次执行**：定时任务的「接下来 5 次」改成 `Popover` 加一列时间。没有新做时间线控件。
- **图标选择器**：弹出层改成 `Popover`，图标网格自己滚动。图标仍是 `@vicons/ionicons5`，不再套 `NIcon`。
- **示例表**：`demos/table` 改成静态表格。这页没有挂到路由上。
- **类型**：`adapter/component/index.ts` 的 schema props 改成各控件自己的类型，不再从 `naive-ui` 引入。
- **壳**：`app.vue` 去掉 `NConfigProvider` 和 Naive 主题。提示仍是 Sonner。

`from 'naive-ui'` 的文件 **8 → 0**。`frontend/package.json` 和 catalog 去掉 `naive-ui`。

卸包时先留着的样式、token hook 和演示文案，已在下一节清掉。

## 卸包后清扫

`naive-ui` 仍不在 `frontend/package.json`、catalog 和 lockfile 里。这一节只清不挡包的残留，不改表单空值和控件行为。

- **校验样式**：删掉 `src/vben/styles/naive/index.css`，以及 `bootstrap.ts` 的 `@vben/styles/naive` 和 `tsconfig.json` 里的同名路径。文件里只有 `.form-valid-error` 下的 `.n-*` 边框。当前表单控件不用这些类。`form-valid-error` 仍由壳层表单字段加上，错误样式走控件自己。
- **主题 hook**：删掉 `vben/hooks/use-design-tokens.ts`，壳层 `hooks/index.ts` 不再导出。`useNaiveDesignTokens` 在卸掉 `NConfigProvider` 之后没有调用方。同文件里的 `useAntdDesignTokens`、`useElementPlusDesignTokens` 应用也不调用，一并去掉。
- **演示文案**：`locales` 里 `demos.naive` 从 “Naive UI” 改为 “原子控件” / “Atoms”。`demos.vben.naive-ui` 从 “Naive UI 版本” 改为 “shadcn-vue / Vben 原子”（英文 “shadcn-vue / Vben atoms”）。键名没改。这两句目前没有路由引用。同组里的 Ant Design Vue、Antdv Next、TDesign 文案仍是上游模板留下的，页面没有用到。
- **脚本名**：`frontend/package.json` 去掉 `dev:naive`、`build:naive`。它们只是转到现有的 `dev` / `build`。
- **指向当前 UI 的说明**：`DESIGN.md`、`AGENTS.md`、`README.md`、`docs/conventions/frontend.md`、`.cursor/rules/frontend-vue.mdc` 里把当前界面写成 Naive 的句子，改成 shadcn-vue / Vben 原子。没有改控件实现。

`frontend/src` 里没有 `from 'naive-ui'`，也没有已删除适配器（例如 `adapter/naive.ts`）的导入。

还留着、这次不动：

- `docs/tech-stack.md` 仍把主 UI 写成 Naive UI 2.44.1。改技术栈宪法要单独确认，不在这次清扫里改。
- `frontend/apps/docs` 是上游 Vben 文档（`@wyh/docs`），仍介绍多组件库（含 Naive）。不是本应用的界面基座说明。文档站 `site-layout.vue` 里有一行已注释的 `useAntdDesignTokens` 导入，没有执行。
- `vendor/@core` 里表单 `emptyStateValue` 的注释仍提到 naive-ui 的空值是 `null`。应用适配器继续用 `null`，注释留在厂商代码里。
- `src/vben/styles` 里还有未引用的 `antd`、`antdv-next`、`ele` 样式，不是这次要删的 `.n-*` 文件。
- `views/demos/naive/` 目录名还在。页面本身已经是原子按钮和 Sonner，没有挂到路由上。
- `.kiro` 里的历史改造记录仍按当时的 Naive 页面来写。

上面的技术栈、文档站说明和 `views/demos/naive/` 目录名在下一节收口。

## 命名与文档收尾

上一节留下的技术栈、演示目录和文档站说明，这一节收口。不改控件行为，也不改 `vendor/@core` 里空值为 `null` 的注释。

- **技术栈**：`docs/tech-stack.md` 的主 UI 改为 shadcn-vue / Vben 原子。应用入口是 `#/ui`、`#/ui-patterns`，实现是 `@vben-core/shadcn-ui` 5.7.0（catalog：reka-ui 2.9.5、CVA 0.7.1、vue-sonner 2.0.9）。不再把 Naive UI 写成当前界面。
- **演示目录**：`views/demos/naive/` 改为 `views/demos/atoms/`。页面仍是原子按钮和 Sonner。`routes.ts` 里挂上的演示只有 `/demos/form`，没有旧路径。语言包 `demos.naive` 改为 `demos.atoms`，`demos.vben.naive-ui` 改为 `demos.vben.atoms`，文案不变。后端菜单里没有 `demos/naive`。
- **文档站**：`frontend/apps/docs` 仍保留上游 Vben 的多组件库正文。中英文「关于」和「组件库切换」页首加了一句：wyh-admin 的产品界面是 shadcn-vue / Vben 原子。没有改写那些上游列表。

还留着：

- `vendor/@core` 里表单空值为 `null` 的注释仍提到 naive-ui。应用继续用 `null`。
- `src/vben/styles` 里未引用的 `antd`、`antdv-next`、`ele` 样式。
- 文档站 `site-layout.vue` 里已注释、未执行的 `useAntdDesignTokens`。
- `.kiro` 里的历史改造记录。

## 目录选择

原子实现继续放在 `@vben-core/shadcn-ui/src/ui`（已有 reka-ui、CVA、`cn()`，颜色经 `frontend/apps/admin/src/styles/theme.css` 的 `--color-*` 接 HSL token）。  
应用侧只加路径入口，避免第二套 Button：

| 层 | 路径 | 导入 |
|----|------|------|
| 原子 | `frontend/src/ui/<atom>` | `import { Button } from '#/ui/button'` |
| 模式 | `frontend/src/ui-patterns/<pattern>` | `ToolbarActions`、`FilterInput`、`ConfirmAction` |

包增加子路径 `./ui/*`，新原子（Skeleton、Sonner）**不**进入 `@vben-core/shadcn-ui` 根桶，避免壳层整包把 `vue-sonner` 带进去。

第一波入口：`button`、`input`、`label`、`checkbox`、`switch`、`dialog`、`alert-dialog`、`badge`、`skeleton`、`sonner`。Phase 3 补了应用路径 `separator`。后来补了 `select`、`popover`、`radio-group`、`textarea`、`number-field`。日期和树那波补了 `date-picker`、`tree`（`VbenTree`）。后来补了 `dropdown-menu`。上传那波补了 `upload`（只选文件，不发请求）。提示那波补了 `ui-patterns/toast`。表单壳那波补了 `tabs`、`card`。用户页和角色页的左右分割用 `ColPage`。`ColPage` 现在可以按百分比或像素给宽度。`naive-ui` 已从依赖里去掉。

## 可先换

页面上的轻控件都已换成套件原子。`frontend/src` 里不再 `from 'naive-ui'`。

| Naive | 文件数 | 换成 | 例子 |
|-------|--------|------|------|
| `NButton` | 0 | `#/ui/button`（含 `loading`） | 页面已换完 |
| `NSpace` | 0 | `flex` + `gap-*` 或 `ToolbarActions` | 页面已换完 |
| `NInput` | 0 | `#/ui/input` 或 `FilterInput` | 部门搜索和代码生成字段已换。部门搜索清除后是空字符串 |
| `NCard` | 0 | `#/ui/card` | 代码生成预览已换 |
| `NDivider` | 0 | `#/ui/separator` | 视图已换完 |
| `NCheckbox` | 0 | `#/ui/checkbox` | 绑定用 `v-model`（`modelValue`） |
| `NBadge` | 0 | `#/ui/badge` | 部门状态已改为 `success` / `destructive` 徽标 |
| `NSpin` | 0 | 已有 Spinner | — |
| `NText` | 0 | 排版类（`text-foreground` 等） | — |

## 后换

这些控件的页面用法已经换完。

| Naive | 文件数 | 原因 | 例子 |
|-------|--------|------|------|
| `useMessage` | 0 | 页面和组件外的提示已改 `#/ui-patterns/toast` | — |
| `NTag` | 0 | 具名颜色已映射到 Badge | — |
| `NSelect` | 0 | 注册表和页面下拉都已接 `FormSelect` | — |
| `NIcon` | 0 | 图标选择器和部门搜索改成直接画图标 | `@vicons/ionicons5` 仍留在图标选择器 |
| `NForm` / `NFormItem` | 0 / 0 | 已改 `useVbenForm` | — |
| `useDialog` | 0 | 布尔确认已改 `ConfirmAction` | — |
| `NDrawer` / `NDrawerContent` | 0 / 0 | 已改 `useVbenDrawer` | — |
| `NPopconfirm` | 0 | 已改 `ConfirmAction` | — |
| `NRadioGroup` / `NRadio` | 0 / 0 | 已接单选 | — |
| `NSwitch` | 0 | `null` 显示为关；`1/0` 用 `checked-value` / `unchecked-value` | — |
| `NInputNumber` | 0 | 已接 NumberField | — |
| `NModal` | 0 | 已改 `useVbenModal` | — |
| `NDropdown` | 0 | 已走 `#/ui/dropdown-menu` | — |
| `NCheckboxGroup` | 0 | 已改为 Checkbox 列表 | — |
| `NPopover` | 0 | 定时任务和图标选择器已走 `#/ui/popover` | — |
| `NTabs` / `NTabPane` | 0 / 0 | 已走 `#/ui/tabs` | — |
| `NDescriptions` / `NDescriptionsItem` | 0 / 0 | 已改成描述列表 | — |
| `NImage` | 0 | Vxe `CellImage` 已改成 `img` | — |
| `useNotification` | 0 | 已改 toast | — |
| `NAlert` | 0 | 已改成带边框的提示块 | — |
| `NEmpty` | 0 | 已改成一段文字 | — |
| `NPagination` | 0 | 已随 Vxe 去掉 | — |
| `NSteps` / `NList` / `NTimeline` / `NScrollbar` | 0 | 步骤、下次执行时间和滚动都改成页面上的标记 | 代码生成配置、定时任务、图标选择器、代码预览 |

`adapter/component/index.ts` 里，提交/重置按钮、Input（含文本域、密码、字数、成对输入）、Textarea、Checkbox、CheckboxGroup、Select、ApiSelect、RadioGroup、InputNumber、Switch、DatePicker、TreeSelect、ApiTreeSelect、TimePicker、Upload、Divider、Space 已经换成原子控件或薄包装，空值仍是 `null`（`adapter/form.ts`）。Select / Radio、树选择、日期和时间清除后回写 `null`。日期和时间没有 `valueFormat` 时是时间戳，有 `valueFormat` 时是格式化字符串。InputNumber 清空回写 `null`。Upload 的 `fileList` 为空时回写 `null`。图标选择器不再传入 `NInput`。这个文件不再导入 `naive-ui`。

## 暂留

`frontend/src` 里已经没有 Naive 控件。Vxe Grid 继续当表格默认。

| Naive | 文件数 | 例子 |
|-------|--------|------|
| `NDataTable` + `DataTableColumns` | 0 / 0 | `demos/table` 已改成静态表格。产品列表用 Vxe |
| `NDatePicker` | 0 | 表单和页面都改 `#/ui/date-picker` |
| `NTreeSelect` | 0 | 表单 `TreeSelect` / `ApiTreeSelect` 已改 `FormTreeSelect` |
| `NSplit` | 0 | 用户页、角色页和配置页都改 `ColPage` |
| `NTree` | 0 | 用户部门树和代码生成预览都改 `VbenTree` |
| `NUpload` | 0 | 表单 schema 和页面上传都已换 |
| `NCascader` | 0 | 文件分类改成 `FormTreeSelect` |
| `NTimePicker` | 0 | 已改 `FormTimePicker` |
| `NConfigProvider` | 0 | `app.vue` 已去掉。提示走 Sonner |
| `createDiscreteApi` | 0 | 已删除 |

## 新代码

- 优先 `#/ui/<atom>` 与 `#/ui-patterns/<pattern>`。
- 不要新增 `naive-ui` 依赖或导入。表格用 Vxe。
- 不要新增 `Foo` + `FooShadcn` 两套名字。壳上已有的 `VbenButton` 等封装留在壳里，业务页用原子 `Button`。
