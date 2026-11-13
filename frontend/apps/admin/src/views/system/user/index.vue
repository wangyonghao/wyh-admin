<script setup lang="ts">
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { DepartmentResult } from '#/api/system/department';
import type { UserResp } from '#/api/system/user';
import type { Option } from '#/types/global';

import { computed, onMounted, ref } from 'vue';

import { ColPage } from '@vben/common-ui';
import { useVbenModal } from '#/components/popup/modal';
import { IconifyIcon } from '@vben/icons';

import { filterRawTree } from '#/components/ui/tree-select-value';
import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { departmentApi, roleApi, userApi } from '#/api/system';
import { Badge } from '#/ui/badge';
import { badgeVariantForTag } from '#/ui/badge/variant';
import { Button } from '#/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '#/ui/dropdown-menu';
import { Input } from '#/ui/input';
import { VbenTree } from '#/ui/tree';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';
import { ToolbarActions } from '#/ui-patterns/toolbar-actions';

import { toast } from '#/ui-patterns/toast';

import UserDetailDrawer from './components/user-detail-drawer.vue';
import UserEditDrawer from './components/user-edit-drawer.vue';
import {
  displayCell,
  useUserColumns,
  useUserSearchSchema,
  userKeyword,
  userStatus,
} from './data';

const confirmAction = ref<ConfirmActionExpose | null>(null);

const deptSearchKeyword = ref('');
const departmentData = ref<DeptTreeNode[]>([]);
const selectedDeptId = ref<string | undefined>(undefined);
const roleOptions = ref<Option[]>([]);

interface DeptTreeNode {
  children?: DeptTreeNode[];
  id: string;
  key: string;
  label: string;
  name: string;
  value: string;
}

const visibleDept = computed(() =>
  filterRawTree(departmentData.value, deptSearchKeyword.value, 'name', 'children'),
);

function treeHasKey(nodes: DeptTreeNode[], key: string | undefined): boolean {
  if (!key) return false;
  return nodes.some(
    (node) => node.key === key || treeHasKey(node.children ?? [], key),
  );
}

const treeModel = computed(() =>
  treeHasKey(visibleDept.value, selectedDeptId.value)
    ? selectedDeptId.value
    : undefined,
);

async function loadDeptData() {
  try {
    const deptArray = await departmentApi.tree({});
    departmentData.value = convertToTreeOptions(deptArray);
  } catch (error) {
    console.error('Failed to load dept options:', error);
  }
}

function convertToTreeOptions(depts: DepartmentResult[]): DeptTreeNode[] {
  return depts.map((dept) => ({
    id: dept.id,
    name: dept.name,
    label: dept.name,
    key: dept.id,
    value: dept.id,
    children: dept.children?.length
      ? convertToTreeOptions(dept.children)
      : undefined,
  }));
}

async function loadRoleOptions() {
  try {
    const res = await roleApi.list({ status: 1 } as any);
    roleOptions.value = res.map((item) => {
      return {
        label: item.name,
        value: item.id,
      };
    });
  } catch (error) {
    console.error('加载角色选项失败:', error);
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useUserSearchSchema(),
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  },
  gridOptions: {
    columns: useUserColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
      pageSizes: [10, 20, 50],
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await userApi.list({
            page: page.currentPage,
            pageSize: page.pageSize,
            departmentId: selectedDeptId.value,
            keyword: userKeyword(formValues?.keyword) || undefined,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: { code: 'query' },
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<UserResp>,
});

function onDeptSelect(value: unknown) {
  const next =
    typeof value === 'string' || typeof value === 'number'
      ? String(value)
      : undefined;
  // 搜索把已选部门滤掉时，树会回写空值。这时不要清掉右侧列表的部门条件。
  if (
    next == null &&
    selectedDeptId.value &&
    !treeHasKey(visibleDept.value, selectedDeptId.value)
  ) {
    return;
  }
  if (next === selectedDeptId.value) return;
  selectedDeptId.value = next;
  if (typeof gridApi.grid?.commitProxy === 'function') {
    gridApi.reload();
  }
}

async function handleExport() {
  const values = await gridApi.formApi.getValues();
  userApi.export({
    departmentId: selectedDeptId.value,
    keyword: userKeyword(values?.keyword) || undefined,
  });
}

function handleImport() {
  // TODO: 实现导入用户功能
}

const detailDrawerVisible = ref(false);
const detailUserId = ref<string>();

function handleDetail(row: UserResp) {
  detailUserId.value = row.id;
  detailDrawerVisible.value = true;
}

const editDrawerVisible = ref(false);
const editUserId = ref<string>();

function handleAdd() {
  editUserId.value = undefined;
  editDrawerVisible.value = true;
}

function handleEdit(row: UserResp) {
  editUserId.value = row.id;
  editDrawerVisible.value = true;
}

function handleEditSuccess() {
  gridApi.query();
}

const newPassword = ref('');

const [PasswordModal, passwordModalApi] = useVbenModal({
  centered: true,
  title: '密码重置成功',
  showCancelButton: false,
  confirmText: '确定',
  onConfirm() {
    passwordModalApi.close();
  },
});

async function handleResetPassword(row: UserResp) {
  const ok = await confirmAction.value?.ask({
    title: '重置密码',
    description: `确定要重置用户"${row.username}"的密码吗？`,
    tone: 'destructive',
  });
  if (!ok) return;
  try {
    const password = await userApi.resetPassword(row.id);
    newPassword.value = password;
    passwordModalApi.open();
  } catch (error) {
    console.error('重置密码失败:', error);
    toast.error('重置密码失败');
  }
}

async function handleCopyPassword() {
  try {
    await navigator.clipboard.writeText(newPassword.value);
    toast.success('密码已复制到剪贴板');
  } catch (error) {
    console.error('复制失败:', error);
    toast.error('复制失败');
  }
}

async function handleDelete(row: UserResp) {
  const ok = await confirmAction.value?.ask({
    title: '删除用户',
    description: `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
    tone: 'destructive',
  });
  if (!ok) return;
  try {
    await userApi.delete(row.id);
    toast.success('删除成功');
    await gridApi.query();
  } catch (error) {
    console.error('删除用户失败:', error);
    toast.error('删除失败');
  }
}

onMounted(() => {
  loadDeptData();
  loadRoleOptions();
});
</script>

<template>
  <ColPage
    auto-content-height
    :left-width="20"
    :left-min-width="16"
    :left-max-width="32"
    :right-width="80"
    resizable
    split-line
    split-handle
    content-class="p-0"
  >
    <template #left>
      <div class="flex h-full flex-col bg-background p-4">
        <div class="relative mb-4">
          <IconifyIcon
            icon="lucide:search"
            class="text-muted-foreground pointer-events-none absolute top-1/2 left-2.5 size-4 -translate-y-1/2"
          />
          <Input
            v-model="deptSearchKeyword"
            placeholder="搜索部门"
            class="pl-8"
            :class="deptSearchKeyword ? 'pr-8' : undefined"
          />
          <button
            v-if="deptSearchKeyword"
            type="button"
            class="text-muted-foreground absolute top-1/2 right-2 -translate-y-1/2 text-sm leading-none"
            @click="deptSearchKeyword = ''"
          >
            ×
          </button>
        </div>
        <div class="min-h-0 flex-1 overflow-auto">
          <VbenTree
            v-if="visibleDept.length"
            :tree-data="visibleDept"
            :model-value="treeModel"
            value-field="key"
            label-field="name"
            children-field="children"
            :default-expanded-level="99"
            allow-clear
            :show-icon="false"
            @update:model-value="onDeptSelect"
          />
          <p
            v-else
            class="text-muted-foreground py-6 text-center text-sm"
          >
            无数据
          </p>
        </div>
      </div>
    </template>
    <template #default>
      <ConfirmAction ref="confirmAction" />
      <div class="h-full min-h-0 bg-background">
        <Grid>
          <template #toolbar-tools>
            <ToolbarActions>
              <Button
                type="button"
                @click="handleAdd"
              >
                <IconifyIcon
                  icon="lucide:plus"
                  class="mr-1 size-4"
                />
                新增
              </Button>
              <Button
                type="button"
                variant="outline"
                @click="handleImport"
              >
                <IconifyIcon
                  icon="lucide:upload"
                  class="mr-1 size-4"
                />
                导入
              </Button>
              <Button
                type="button"
                variant="outline"
                @click="handleExport"
              >
                <IconifyIcon
                  icon="lucide:download"
                  class="mr-1 size-4"
                />
                导出
              </Button>
            </ToolbarActions>
          </template>
          <template #departmentName="{ row }">
            {{ displayCell(row.departmentName) }}
          </template>
          <template #roleNames="{ row }">
            {{ displayCell(row.roleNames) }}
          </template>
          <template #phone="{ row }">
            {{ displayCell(row.phone) }}
          </template>
          <template #status="{ row }">
            <Badge
              :variant="
                badgeVariantForTag(userStatus(row.status).type) ?? 'secondary'
              "
            >
              {{ userStatus(row.status).label }}
            </Badge>
          </template>
          <template #action="{ row }">
            <div class="inline-flex items-center gap-2">
              <Button
                type="button"
                variant="link"
                size="sm"
                class="h-auto px-1"
                @click="handleDetail(row)"
              >
                详情
              </Button>
              <Button
                type="button"
                variant="link"
                size="sm"
                class="h-auto px-1"
                @click="handleEdit(row)"
              >
                修改
              </Button>
              <DropdownMenu>
                <DropdownMenuTrigger as-child>
                  <Button
                    type="button"
                    variant="link"
                    size="sm"
                    class="h-auto px-1"
                  >
                    更多
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem @select="handleResetPassword(row)">
                    <IconifyIcon
                      icon="lucide:key"
                      class="mr-2 size-4"
                    />
                    重置密码
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    class="text-destructive"
                    @select="handleDelete(row)"
                  >
                    <IconifyIcon
                      icon="lucide:trash-2"
                      class="mr-2 size-4"
                    />
                    删除
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </template>
        </Grid>
      </div>

      <UserDetailDrawer
        v-model:visible="detailDrawerVisible"
        :user-id="detailUserId"
        @edit="handleEdit"
      />

      <UserEditDrawer
        v-model:visible="editDrawerVisible"
        :user-id="editUserId"
        :department-data="departmentData"
        :role-options="roleOptions"
        :default-department-id="selectedDeptId"
        @success="handleEditSuccess"
      />

      <PasswordModal class="w-[420px]">
        <div class="space-y-4">
          <div class="flex items-center gap-2 text-orange-500">
            <IconifyIcon
              icon="lucide:alert-triangle"
              class="text-lg"
            />
            <span class="font-medium">新密码只显示一次，请妥善保管！</span>
          </div>
          <div
            class="flex items-center gap-2 rounded bg-gray-100 p-3 dark:bg-gray-800"
          >
            <span class="flex-1 font-mono text-lg select-all">{{
              newPassword
            }}</span>
            <Button
              type="button"
              size="sm"
              @click="handleCopyPassword"
            >
              <IconifyIcon
                icon="lucide:copy"
                class="mr-1 size-4"
              />
              复制
            </Button>
          </div>
        </div>
      </PasswordModal>
    </template>
  </ColPage>
</template>
