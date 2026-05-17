<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';

import type { RoleDetailResp, RoleResp, RoleUserResp } from '#/api/system/role';
import type { UserResp } from '#/api/system/user';

import { computed, h, onMounted, ref, watch } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { VbenButton } from '@vben-core/shadcn-ui';

import {
  NBadge,
  NButton,
  NDataTable,
  NDropdown,
  NInput,
  NPagination,
  NScrollbar,
  NSpin,
  NSplit,
  NTabPane,
  NTabs,
  useDialog,
  useMessage,
} from 'naive-ui';

import { roleApi } from '#/api/system/role';
import { useUserStore } from '#/store';

import RoleEditDrawer from './components/role-edit-drawer.vue';
import RolePermission from './components/role-permission.vue';

defineOptions({ name: 'SystemRole' });

const message = useMessage();
const dialog = useDialog();
const userStore = useUserStore();

// ==================== 左侧角色列表状态 ====================
const roleData = ref<RoleResp[]>([]);
const selectedRoleId = ref<null | number | string>(null);
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
      size: 1000,
      sort: 'createTime,desc',
      description: undefined,
    });
    roleData.value = res;

    // 自动选中第一个角色
    const firstRole = res[0];
    if (firstRole && !selectedRoleId.value) {
      selectRole(firstRole);
    }
  } catch {
    message.error('加载角色列表失败');
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
const showRoleDeleteDialog = (role: RoleResp) => {
  dialog.error({
    title: $t('system.role.deleteTitle'),
    content: $t('ui.actionMessage.deleteConfirm', [role.name]),
    positiveText: $t('common.confirm'),
    negativeText: $t('common.cancel'),
    onPositiveClick: async () => {
      if (!role.id) return;
      try {
        await roleApi.delete(role.id);
        message.success($t('pages.common.deleteSuccess'));
        const wasSelected = selectedRoleId.value === role.id;
        await loadRoles();

        // 如果删除的是当前选中的角色，自动选中第一个角色
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
    },
  });
};

// ==================== 角色列表下拉菜单 ====================
const dropdownOptions = () => [
  { label: $t('pages.common.edit'), key: 'edit' },
  { 
    label: '复制', 
    key: 'copy',
    icon: () => h(IconifyIcon, { icon: 'lucide:copy' })
  },
  { type: 'divider', key: 'divider' },
  { label: $t('pages.common.delete'), key: 'delete' },
];

const handleDropdownSelect = (key: string, role: RoleResp) => {
  if (key === 'edit') handleEdit(role);
  if (key === 'copy') handleCopy(role);
  if (key === 'delete') showRoleDeleteDialog(role);
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
const selectKeys = ref<any>([]);
const permissionTreeLoaded = ref(false);

// 用户表格相关
const userSearchKeyword = ref('');
const userData = ref<RoleUserResp[] | UserResp[]>([]);
const userLoading = ref(false);
const userPagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    userPagination.value.page = page;
    loadUserData();
  },
  onUpdatePageSize: (pageSize: number) => {
    userPagination.value.page = 1;
    userPagination.value.pageSize = pageSize;
    loadUserData();
  },
});
const userColumns: DataTableColumns<RoleUserResp> = [
  {
    title: '序号',
    key: 'index',
    width: 50,
    fixed: 'left',
    render: (_row, index) =>
      (userPagination.value.page - 1) * userPagination.value.pageSize +
      index +
      1,
  },
  {
    title: $t('system.user.nickname'),
    key: 'nickname',
    minWidth: 180,
    fixed: 'left',
    render: (row) =>
      h('div', { class: 'flex items-center gap-2' }, [
        h(
          'div',
          {
            class:
              'w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-sm font-medium',
          },
          row.nickname?.charAt(0)?.toUpperCase() || 'U',
        ),
        h('span', row.nickname),
      ]),
  },
  { title: $t('system.user.username'), key: 'username', minWidth: 100 },
  { title: $t('system.user.deptId'), key: 'deptName', minWidth: 130 },
  {
    title: $t('system.user.gender'),
    key: 'gender',
    width: 80,
    align: 'center',
    render: (row) => {
      if (row.gender === 1) return h(NBadge, () => '男');
      if (row.gender === 2) return h(NBadge, () => '女');
      return h(NBadge, () => '未知');
    },
  },
  {
    title: $t('system.user.status'),
    key: 'status',
    width: 90,
    align: 'center',
    render: (row) =>
      row.status === 1
        ? h('span', $t('common.enabled'))
        : h('span', { class: 'bg-red-100 p-2' }, $t('common.disabled')),
  },
  {
    title: $t('system.user.description'),
    key: 'description',
    minWidth: 180,
    ellipsis: { tooltip: true },
  },
  {
    title: $t('pages.common.operation'),
    key: 'action',
    width: 80,
    fixed: 'right',
    render: (row) =>
      h('div', { class: 'flex items-center gap-2' }, [
        h('span', { 'v-access:code': "['system:role:unassign']" }, [
          h(
            VbenButton,
            {
              variant: 'ghost',
              size: 'icon',
              disabled: row.isBuiltin,
              onClick: () => showUserDeleteDialog(row),
            },
            () =>
              h(IconifyIcon, {
                icon: 'lucide:user-minus',
                class: 'text-destructive h-4 w-4',
              }),
          ),
        ]),
      ]),
  },
];

// 加载用户表格数据
async function loadUserData() {
  if (!selectedRoleId.value) {
    userData.value = [];
    userPagination.value.itemCount = 0;
    return;
  }

  userLoading.value = true;
  try {
    const res = await roleApi.pageMember(selectedRoleId.value as string, {
      page: userPagination.value.page,
      size: userPagination.value.pageSize,
      keyword: userSearchKeyword.value || '',
      sort: [],
    });

    // Handle both array response and PageRes response
    if (Array.isArray(res)) {
      userData.value = res;
      userPagination.value.itemCount = res.length;
    } else {
      userData.value = res.list || [];
      userPagination.value.itemCount = res.total || 0;
    }
  } catch (error) {
    message.warning('加载用户数据失败');
    console.error('加载用户数据失败:', error);
  } finally {
    userLoading.value = false;
  }
}

const handleUserSearch = () => {
  userPagination.value.page = 1;
  loadUserData();
};

const handlePageChange = (page: number) => {
  userPagination.value.page = page;
  loadUserData();
};

const handlePageSizeChange = (pageSize: number) => {
  userPagination.value.pageSize = pageSize;
  userPagination.value.page = 1;
  loadUserData();
};

// 加载权限数据
async function loadPermissionData() {
  if (!selectedRoleId.value) return;

  detailLoading.value = true;
  try {
    const fullRoleDetail = await roleApi.detail(
      selectedRoleId.value.toString(),
    );
    roleDetail.value = fullRoleDetail;
    selectKeys.value = fullRoleDetail.menuIds;

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
      const detail = await roleApi.detail(String(roleDetail.value.id));
      roleDetail.value = detail;
      selectKeys.value = detail.menuIds;
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
const showUserDeleteDialog = (row: RoleUserResp) => {
  dialog.error({
    title: '取消分配',
    content: $t('system.role.cancelRoleConfirm', [
      row.nickname,
      roleDetail.value?.name || '',
    ]),
    positiveText: $t('common.confirm'),
    negativeText: $t('common.cancel'),
    onPositiveClick: async () => {
      try {
        if (roleDetail.value?.id) {
          await roleApi.removeMember(roleDetail.value.id, [row.id]);
          message.success($t('pages.common.deleteSuccess'));
          loadUserData();
        }
      } catch {
        // ignore
      }
    },
  });
};

// 监听选中角色变化
watch(
  selectedRole,
  async (role) => {
    if (role) {
      roleDetail.value = {
        id: role.id as string,
        name: role.name,
        description: role.description,
      };
      selectKeys.value = [];

      // 如果在权限标签页，加载权限数据
      if (activeTab.value === 'permission') {
        await loadPermissionData();
      }
    } else {
      roleDetail.value = null;
      selectKeys.value = [];
      userData.value = [];
    }
    userPagination.value.page = 1;

    // 如果在用户标签页，加载用户数据
    if (activeTab.value === 'users') {
      loadUserData();
    }
  },
  { immediate: true },
);

// 监听标签页切换
watch(activeTab, async (newTab) => {
  if (newTab === 'users' && selectedRoleId.value) {
    loadUserData();
  } else if (newTab === 'permission' && selectedRoleId.value) {
    await loadPermissionData();
  }
});

onMounted(() => loadRoles());
</script>

<template>
  <Page class="h-full">
    <NSplit
      direction="horizontal"
      :default-size="0.3"
      :min="0.2"
      :max="0.35"
      :resizable="true"
    >
      <!-- 左侧角色列表 -->
      <template #1>
        <div class="flex flex-col h-full bg-background p-4 overflow-auto">
          <!-- 搜索栏 -->
          <div class="flex items-center gap-2 mb-2">
            <NInput v-model:value="roleSearchKeyword" :placeholder="$t('system.role.searchKey')" clearable >
              <template #prefix>
                <IconifyIcon icon="lucide:search" class="h-4 w-4 text-gray-400" />
              </template>
            </NInput>
            <NButton size="small" @click="handleAdd">
              <template #icon>
                <IconifyIcon icon="lucide:plus" class="h-6 w-6" />
              </template>
            </NButton>
          </div>

          <div class="flex-1 overflow-hidden">
            <div v-if="roleLoading" class="flex items-center justify-center py-12" >
              <NSpin size="medium" />
            </div>
            <NScrollbar v-else>
              <div
                v-for="role in filteredRoles"
                :key="role.id ?? role.name"
                class="group flex cursor-pointer items-center gap-2 pl-4 px-2 py-2 transition-colors hover:bg-gray-100 dark:hover:bg-gray-800"
                :class="{
                  'bg-gray-100 text-primary dark:bg-gray-800':
                    selectedRoleId === (role.id ?? undefined),
                }"
                @click="selectRole(role)"
              >
                <div class="min-w-0 flex-1">
                  <div class="truncate text-sm">{{ role.name }}</div>
                  <div
                    v-if="role.description"
                    class="truncate text-xs text-gray-400"
                  >
                    {{ role.description }}
                  </div>
                </div>
                <NDropdown
                  trigger="click"
                  :options="dropdownOptions()"
                  @select="(key: string) => handleDropdownSelect(key, role)"
                >
                  <NButton
                    quaternary
                    circle
                    size="tiny"
                    class="opacity-0 group-hover:opacity-100"
                    @click.stop
                  >
                    <template #icon>
                      <IconifyIcon
                        icon="lucide:more-vertical"
                        class="h-3.5 w-3.5"
                      />
                    </template>
                  </NButton>
                </NDropdown>
              </div>

              <div
                v-if="filteredRoles.length === 0"
                class="py-8 text-center text-sm text-gray-400"
              >
                {{ $t('common.noData') }}
              </div>
            </NScrollbar>
          </div>
        </div>
      </template>

      <!-- 右侧详情区域 -->
      <template #2>
        <div class="flex flex-col h-full bg-background">
          <div v-if="!selectedRole" class="h-full flex items-center justify-center" >
            <div class="text-center text-muted-foreground">
              <IconifyIcon icon="lucide:info" class="w-12 h-12 mx-auto mb-2" />
              <p>请从左侧选择一个角色</p>
            </div>
          </div>

          <NTabs
            v-else
            v-model:value="activeTab"
            type="line"
            animated
            class="h-full pl-4 pr-4"
          >
            <!-- 功能权限标签页 -->
            <NTabPane name="permission" tab="功能权限">
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
            </NTabPane>

            <!-- 角色用户标签页 -->
            <NTabPane name="users" :tab="$t('system.role.userTab')">
              <div class="flex flex-col h-full">
                <!-- 搜索和操作栏 -->
                <div
                  class="flex items-center justify-between mb-4 flex-shrink-0"
                >
                  <div class="flex items-center gap-2 flex-1 max-w-md">
                    <NInput
                      v-model:value="userSearchKeyword"
                      :placeholder="$t('system.user.searchKey')"
                      clearable
                      @keyup.enter="handleUserSearch"
                    >
                      <template #prefix>
                        <IconifyIcon
                          icon="lucide:search"
                          class="text-muted-foreground"
                        />
                      </template>
                    </NInput>
                    <NButton type="primary" @click="handleUserSearch">
                      {{ $t('common.search') }}
                    </NButton>
                  </div>
                  <NButton
                    v-if="userStore.hasPermission('system:user:create')"
                    type="primary"
                    @click="handleUserSearch"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:user-plus" />
                    </template>
                    {{ $t('system.role.assignUser') }}
                  </NButton>
                </div>

                <!-- 数据表格容器 - 填充剩余空间 -->
                <div class="flex-1 min-h-0 overflow-auto">
                  <NDataTable
                    :columns="userColumns"
                    :data="userData"
                    :loading="userLoading"
                    :row-key="(row) => row.id"
                    :scroll-x="1000"
                    size="small"
                    remote
                  />
                </div>
                <!-- 分页器 -->
                <div class="flex justify-end mt-4 flex-shrink-0">
                  <NPagination
                    v-model:page="userPagination.page"
                    v-model:page-size="userPagination.pageSize"
                    :item-count="userPagination.itemCount"
                    :page-sizes="userPagination.pageSizes"
                    show-size-picker
                    @update:page="handlePageChange"
                    @update:page-size="handlePageSizeChange"
                  />
                </div>
              </div>
            </NTabPane>
          </NTabs>
        </div>
      </template>
    </NSplit>

    <!-- 角色编辑抽屉 -->
    <RoleEditDrawer
      v-model:visible="drawerVisible"
      :editing-role="editingRole"
      :copy-mode="copyMode"
      @success="handleDrawerSuccess"
    />
  </Page>
</template>
