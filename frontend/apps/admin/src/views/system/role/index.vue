<script setup lang="ts">
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';

import type { RoleDetailResp, RoleResp, RoleUserResp } from '#/api/system/role';

import { computed, onMounted, ref, watch } from 'vue';

import { ColPage } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { roleApi } from '#/api/system/role';
import { useUserStore } from '#/store';
import { Badge } from '#/ui/badge';
import { Button } from '#/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '#/ui/dropdown-menu';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '#/ui/tabs';
import { FilterInput } from '#/ui-patterns/filter-input';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';

import RoleEditDrawer from './components/role-edit-drawer.vue';
import RolePermission from './components/role-permission.vue';
import { toast } from '#/ui-patterns/toast';

defineOptions({ name: 'SystemRole' });

const confirmAction = ref<ConfirmActionExpose | null>(null);
const userStore = useUserStore();

// ==================== 左侧角色列表状态 ====================
const roleData = ref<RoleResp[]>([]);
const selectedRoleId = ref<null | string>(null);
const roleSearchKeyword = ref('');
const roleLoading = ref(false);

const filteredRoles = computed(() => {
  if (!roleSearchKeyword.value) return roleData.value;
  const kw = roleSearchKeyword.value.toLowerCase();
  return roleData.value.filter(
    (r) =>
      r.name?.toLowerCase().includes(kw) ||
      r.description?.toLowerCase().includes(kw),
  );
});

const loadRoles = async () => {
  roleLoading.value = true;
  try {
    const res = await roleApi.list({
      page: 1,
      pageSize: 1000,
      sort: 'createTime,desc',
      description: undefined,
    });
    roleData.value = res;

    // 自动选中第一个角色
    const firstRole = roleData.value[0];
    if (firstRole && !selectedRoleId.value) {
      selectRole(firstRole);
    }
  } catch {
    toast.error('加载角色列表失败');
  } finally {
    roleLoading.value = false;
  }
};

const selectRole = (role: RoleResp) => {
  selectedRoleId.value = role.id;
  selectedRole.value = role;
};

// ==================== 角色编辑表单 ====================
const drawerVisible = ref(false);
const editingRole = ref<RoleResp | null>(null);
const copyMode = ref(false);

const handleAdd = () => {
  editingRole.value = null;
  copyMode.value = false;
  drawerVisible.value = true;
};

const handleEdit = (role: RoleResp) => {
  editingRole.value = role;
  copyMode.value = false;
  drawerVisible.value = true;
};

const handleCopy = (role: RoleResp) => {
  editingRole.value = role;
  copyMode.value = true;
  drawerVisible.value = true;
};

const handleDrawerSuccess = async (newRoleId?: string) => {
  const previousSelectedId = selectedRoleId.value;
  await loadRoles();

  // 如果是复制模式且有新角色ID，选中新创建的角色
  if (copyMode.value && newRoleId) {
    const newRole = roleData.value.find((r) => r.id === newRoleId);
    if (newRole) {
      selectRole(newRole);
      return;
    }
  }

  // 如果之前有选中的角色，重新选中它
  if (previousSelectedId) {
    const role = roleData.value.find((r) => r.id === previousSelectedId);
    if (role) {
      selectRole(role);
    }
  }
};

// ==================== 角色删除 ====================
const showRoleDeleteDialog = async (role: RoleResp) => {
  const ok = await confirmAction.value?.ask({
    title: $t('system.role.deleteTitle'),
    description: $t('ui.actionMessage.deleteConfirm', [role.name]),
    confirmText: $t('common.confirm'),
    cancelText: $t('common.cancel'),
    tone: 'destructive',
  });
  if (!ok || !role.id) return;
  try {
    await roleApi.delete(role.id);
    toast.success($t('pages.common.deleteSuccess'));
    const wasSelected = selectedRoleId.value === role.id;
    await loadRoles();

    const firstRole = roleData.value[0];
    if (wasSelected && firstRole) {
      selectRole(firstRole);
    } else if (wasSelected) {
      selectedRoleId.value = null;
      selectedRole.value = null;
    }
  } catch {
    // ignore
  }
};

// ==================== 右侧详情区域 ====================
const selectedRole = ref<null | RoleResp>(null);
const activeTab = ref('permission');
const roleDetail = ref<
  null | (Partial<RoleDetailResp> & { id?: null | string })
>(null);
const detailLoading = ref(false);

// 权限相关
const menuTree = ref<any>([]);
const selectKeys = ref<string[]>([]);
const permissionTreeLoaded = ref(false);

function roleUserKeyword(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}

function genderLabel(gender: number) {
  if (gender === 1) return '男';
  if (gender === 2) return '女';
  return '未知';
}

const [UserGrid, userGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        component: 'Input',
        fieldName: 'keyword',
        label: $t('system.user.searchKey'),
        componentProps: {
          clearable: true,
          placeholder: $t('system.user.searchKey'),
        },
      },
    ],
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2',
  },
  separator: false,
  gridOptions: {
    columns: [
      { type: 'seq', width: 50, fixed: 'left' },
      {
        field: 'displayName',
        title: $t('system.user.displayName'),
        minWidth: 180,
        fixed: 'left',
        align: 'left',
        showOverflow: false,
        slots: { default: 'displayName' },
      },
      {
        field: 'username',
        title: $t('system.user.username'),
        minWidth: 100,
        align: 'left',
      },
      {
        field: 'departmentName',
        title: $t('system.user.departmentId'),
        minWidth: 130,
        align: 'left',
      },
      {
        field: 'gender',
        title: $t('system.user.gender'),
        width: 80,
        slots: { default: 'gender' },
      },
      {
        field: 'status',
        title: $t('system.user.status'),
        width: 90,
        slots: { default: 'status' },
      },
      {
        field: 'description',
        title: $t('system.user.description'),
        minWidth: 180,
        align: 'left',
      },
      {
        field: 'action',
        title: $t('pages.common.operation'),
        width: 80,
        fixed: 'right',
        slots: { default: 'action' },
      },
    ],
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
      pageSizes: [10, 20, 50],
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!selectedRoleId.value) {
            return { records: [], total: 0 };
          }
          try {
            const res: unknown = await roleApi.pageMember(
              selectedRoleId.value,
              {
                page: page.currentPage,
                pageSize: page.pageSize,
                keyword: roleUserKeyword(formValues?.keyword),
                sort: [],
              },
            );
            if (Array.isArray(res)) {
              const records = res as RoleUserResp[];
              return { records, total: records.length };
            }
            const pageResult = (res ?? {}) as {
              records?: RoleUserResp[];
              total?: number;
            };
            return {
              records: pageResult.records || [],
              total: pageResult.total || 0,
            };
          } catch (error) {
            toast.warning('加载用户数据失败');
            console.error('加载用户数据失败:', error);
            return { records: [], total: 0 };
          }
        },
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: {
      custom: true,
      refresh: { code: 'query' },
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<RoleUserResp>,
});

async function reloadAssignedUsers() {
  const formValues = await userGridApi.formApi.getValues();
  userGridApi.formApi.setLatestSubmissionValues(formValues);
  await userGridApi.reload(formValues);
}

// 加载权限数据
async function loadPermissionData() {
  if (!selectedRoleId.value) return;

  detailLoading.value = true;
  try {
    const fullRoleDetail = await roleApi.detail(selectedRoleId.value);
    roleDetail.value = fullRoleDetail;
    selectKeys.value = roleDetail.value.menuIds ?? [];

    if (!permissionTreeLoaded.value) {
      const menus = await roleApi.treePermission();
      menuTree.value = menus;
      permissionTreeLoaded.value = true;
    }
  } catch (error) {
    console.error('加载角色数据失败:', error);
  } finally {
    detailLoading.value = false;
  }
}

// 刷新权限数据
const handleRefreshPermission = async () => {
  if (roleDetail.value?.id) {
    try {
      detailLoading.value = true;
      const detail = await roleApi.detail(roleDetail.value.id);
      roleDetail.value = detail;
      selectKeys.value = roleDetail.value.menuIds ?? [];
      if (permissionTreeLoaded.value) {
        menuTree.value = await roleApi.treePermission();
      }
    } catch (error) {
      console.error('刷新角色数据失败:', error);
    } finally {
      detailLoading.value = false;
    }
  }
};

// 用户删除对话框
const showUserDeleteDialog = async (row: RoleUserResp) => {
  const ok = await confirmAction.value?.ask({
    title: '取消分配',
    description: $t('system.role.cancelRoleConfirm', [
      row.displayName,
      roleDetail.value?.name || '',
    ]),
    confirmText: $t('common.confirm'),
    cancelText: $t('common.cancel'),
    tone: 'destructive',
  });
  if (!ok || !roleDetail.value?.id) return;
  try {
    await roleApi.removeMember(roleDetail.value.id, [row.id]);
    toast.success($t('pages.common.deleteSuccess'));
    if (typeof userGridApi.grid?.commitProxy === 'function') {
      await userGridApi.query();
    }
  } catch {
    // ignore
  }
};

// 监听选中角色变化
watch(
  selectedRole,
  async (role) => {
    if (role) {
      roleDetail.value = {
        id: role.id,
        name: role.name,
        description: role.description,
      };
      selectKeys.value = [];

      // 如果在权限标签页，加载权限数据
      if (activeTab.value === 'permission') {
        await loadPermissionData();
      }
      if (
        activeTab.value === 'users' &&
        typeof userGridApi.grid?.commitProxy === 'function'
      ) {
        await userGridApi.reload();
      }
    } else {
      roleDetail.value = null;
      selectKeys.value = [];
    }
  },
  { immediate: true },
);

// 监听标签页切换
watch(activeTab, async (newTab) => {
  if (newTab === 'permission' && selectedRoleId.value) {
    await loadPermissionData();
  }
});

onMounted(() => loadRoles());
</script>

<template>
  <ColPage
    auto-content-height
    :left-width="30"
    :left-min-width="20"
    :left-max-width="35"
    :right-width="70"
    resizable
    split-line
    split-handle
    content-class="p-0"
  >
    <ConfirmAction ref="confirmAction" />
    <template #left>
      <div class="flex flex-col h-full bg-background p-4 overflow-auto">
        <!-- 搜索栏 -->
        <div class="flex items-center gap-2 mb-2">
          <FilterInput
            v-model="roleSearchKeyword"
            :placeholder="$t('system.role.searchKey')"
          />
          <Button
            type="button"
            size="icon"
            variant="outline"
            @click="handleAdd"
          >
            <IconifyIcon
              icon="lucide:plus"
              class="size-4"
            />
          </Button>
        </div>

        <div class="flex-1 overflow-hidden">
          <div
            v-if="roleLoading"
            class="flex items-center justify-center py-12"
          >
            <IconifyIcon
              icon="lucide:loader-2"
              class="size-6 animate-spin text-primary"
            />
          </div>
          <div v-else class="h-full overflow-y-auto">
            <div
              v-for="role in filteredRoles"
              :key="role.id ?? role.name"
              class="group flex cursor-pointer items-center gap-2 pl-4 px-2 py-2 transition-colors hover:bg-gray-100 dark:hover:bg-gray-800"
              :class="{
                'bg-gray-100 text-primary dark:bg-gray-800':
                  selectedRoleId === role.id,
              }"
              @click="selectRole(role)"
            >
              <div class="min-w-0 flex-1">
                <div class="truncate text-sm">
                  {{ role.name }}
                </div>
                <div
                  v-if="role.description"
                  class="truncate text-xs text-gray-400"
                >
                  {{ role.description }}
                </div>
              </div>
              <DropdownMenu>
                <DropdownMenuTrigger as-child>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    class="opacity-0 group-hover:opacity-100"
                    @click.stop
                  >
                    <IconifyIcon
                      icon="lucide:more-vertical"
                      class="size-3.5"
                    />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem @select="handleEdit(role)">
                    {{ $t('pages.common.edit') }}
                  </DropdownMenuItem>
                  <DropdownMenuItem @select="handleCopy(role)">
                    <IconifyIcon
                      icon="lucide:copy"
                      class="mr-2 size-4"
                    />
                    复制
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    class="text-destructive"
                    @select="showRoleDeleteDialog(role)"
                  >
                    {{ $t('pages.common.delete') }}
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>

            <div
              v-if="filteredRoles.length === 0"
              class="py-8 text-center text-sm text-gray-400"
            >
              {{ $t('common.noData') }}
            </div>
          </div>
        </div>
      </div>
    </template>

    <div class="flex h-full min-h-0 flex-col bg-background">
      <div
        v-if="!selectedRole"
        class="h-full flex items-center justify-center"
      >
        <div class="text-center text-muted-foreground">
          <IconifyIcon
            icon="lucide:info"
            class="w-12 h-12 mx-auto mb-2"
          />
          <p>请从左侧选择一个角色</p>
        </div>
      </div>

      <Tabs
        v-else
        v-model="activeTab"
        class="flex h-full min-h-0 flex-col px-4"
      >
        <TabsList class="h-auto w-full justify-start bg-transparent p-0">
          <TabsTrigger value="permission">功能权限</TabsTrigger>
          <TabsTrigger value="users">{{ $t('system.role.userTab') }}</TabsTrigger>
        </TabsList>
        <TabsContent
          value="permission"
          force-mount
          class="mt-0 min-h-0 flex-1 overflow-auto data-[state=inactive]:hidden"
        >
          <div
            v-if="detailLoading"
            class="flex items-center justify-center py-12"
          >
            <IconifyIcon
              icon="lucide:loader-2"
              class="w-8 h-8 animate-spin text-primary"
            />
          </div>
          <RolePermission
            v-else-if="selectedRoleId && roleDetail"
            :role-id="selectedRoleId"
            :role-detail="roleDetail"
            :menu-tree="menuTree"
            :select-keys="selectKeys"
            @refresh="handleRefreshPermission"
          />
          <div
            v-else
            class="flex flex-col items-center justify-center py-12"
          >
            <IconifyIcon
              icon="lucide:shield"
              class="w-16 h-16 text-muted-foreground mb-4"
            />
            <p class="text-muted-foreground">
              请从左侧选择一个角色以配置权限
            </p>
          </div>
        </TabsContent>

        <TabsContent
          value="users"
          force-mount
          class="mt-0 min-h-0 flex-1 data-[state=inactive]:hidden"
        >
          <div class="h-full min-h-[420px]">
            <UserGrid>
              <template #toolbar-tools>
                <Button
                  v-if="userStore.hasPermission('system:user:create')"
                  type="button"
                  @click="reloadAssignedUsers"
                >
                  <IconifyIcon
                    icon="lucide:user-plus"
                    class="mr-1 size-4"
                  />
                  {{ $t('system.role.assignUser') }}
                </Button>
              </template>
              <template #displayName="{ row }">
                <div class="flex items-center gap-2">
                  <div
                    class="bg-primary/10 flex h-8 w-8 items-center justify-center rounded-full text-sm font-medium"
                  >
                    {{ row.displayName?.charAt(0)?.toUpperCase() || 'U' }}
                  </div>
                  <span>{{ row.displayName }}</span>
                </div>
              </template>
              <template #gender="{ row }">
                <Badge variant="secondary">
                  {{ genderLabel(row.gender) }}
                </Badge>
              </template>
              <template #status="{ row }">
                <span v-if="row.status === 1">{{ $t('common.enabled') }}</span>
                <span
                  v-else
                  class="bg-red-100 p-2"
                >{{
                  $t('common.disabled')
                }}</span>
              </template>
              <template #action="{ row }">
                <Button
                  v-if="userStore.hasPermission('system:role:unassign')"
                  type="button"
                  variant="ghost"
                  size="icon"
                  :disabled="row.isBuiltin"
                  @click="showUserDeleteDialog(row)"
                >
                  <IconifyIcon
                    icon="lucide:user-minus"
                    class="text-destructive size-4"
                  />
                </Button>
              </template>
            </UserGrid>
          </div>
        </TabsContent>
      </Tabs>
    </div>

    <!-- 角色编辑抽屉 -->
    <RoleEditDrawer
      v-model:visible="drawerVisible"
      :editing-role="editingRole"
      :copy-mode="copyMode"
      @success="handleDrawerSuccess"
    />
  </ColPage>
</template>

