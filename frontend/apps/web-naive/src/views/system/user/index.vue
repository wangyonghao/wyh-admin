<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';

import type { DeptResp } from '#/api/system/dept';
import type { UserResp } from '#/api/system/user';
import type { Option } from '#/types/global';

import { h, onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { SearchOutline } from '@vicons/ionicons5';
import { Page } from '@Vben/common-ui'
import { NButton, NDataTable, NDropdown, NIcon, NInput, NModal, NSpace, NSplit, NTag, NTree, useDialog, useMessage, } from 'naive-ui';

import { deptApi,roleApi,userApi } from '#/api/system';

import UserDetailDrawer from './components/user-detail-drawer.vue';
import UserEditDrawer from './components/user-edit-drawer.vue';

const message = useMessage();
const dialog = useDialog();

// ==================== 部门树逻辑 ====================
const deptSearchKeyword = ref('');
const deptData = ref<DeptResp[]>([]);
const selectedDeptKeys = ref<string[]>([]);
const selectedDeptId = ref<string | undefined>(undefined);

// ==================== 角色数据 ====================
const roleOptions = ref<Option[]>([]);

const deptNodeProps = ({ option }: { option: any }) => {
  return {
    onClick() {
      const id = option.id as string;
      if (selectedDeptId.value === id) {
        selectedDeptId.value = undefined;
        selectedDeptKeys.value = [];
      } else {
        selectedDeptId.value = id;
        selectedDeptKeys.value = [id];
      }
      handlePageChange(1);
    },
  };
};

async function loadDeptData() {
  try {
    const res = await deptApi.list({});
    deptData.value = Array.isArray(res) ? res : [];
  } catch (error) {
    console.error('加载部门树失败:', error);
  }
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

// ==================== 用户列表逻辑 ====================
const userSearchForm = ref({ description: '' });
const userData = ref<UserResp[]>([]);
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
    userPagination.value.pageSize = pageSize;
    userPagination.value.page = 1;
    loadUserData();
  },
});

const userColumns: DataTableColumns<UserResp> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    fixed: 'left',
    render: (_row, index) =>
      (userPagination.value.page - 1) * userPagination.value.pageSize +
      index +
      1,
  },
  { title: '昵称', key: 'nickname', minWidth: 100, fixed: 'left' },
  { title: '用户名', key: 'username', minWidth: 100 },
  {
    title: '部门',
    key: 'deptName',
    minWidth: 100,
    render(row) {
      return row.deptName || '-';
    },
  },
  {
    title: '角色',
    key: 'roleNames',
    width: 120,
    render(row) {
      return row.roleNames || '-';
    },
  },
  // {
  //   title: '用户类型',
  //   key: 'userType',
  //   width: 110,
  //   render(row) {
  //     const typeMap: Record<string, { type: 'info' | 'success' | 'warning'; label: string }> = {
  //       admin: { type: 'info', label: '后台管理员' },
  //       pc: { type: 'success', label: 'PC前台' },
  //       app: { type: 'warning', label: 'App/小程序' }
  //     }
  //     const t = typeMap[row.userType || 'admin'] || { type: 'info', label: row.userType || '未知' }
  //     return h(NTag, { type: t.type, size: 'small' }, { default: () => t.label })
  //   }
  // },
  {
    title: '手机号',
    key: 'phone',
    width: 120,
    render(row) {
      return row.phone || '-';
    },
  },
  // {
  //   title: '离职',
  //   key: 'isQuit',
  //   width: 80,
  //   render(row) {
  //     const quit = row.isQuit === 1
  //     return h(NTag, { type: quit ? 'error' : 'success', size: 'small' }, { default: () => (quit ? '是' : '否') })
  //   }
  // },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const statusMap: Record<
        number,
        { label: string; type: 'error' | 'info' | 'success' | 'warning' }
      > = {
        0: { type: 'error', label: '禁用' },
        1: { type: 'success', label: '启用' },
        2: { type: 'warning', label: '待审核' },
        3: { type: 'error', label: '审核拒绝' },
      };
      const status = statusMap[row.status] || { type: 'info', label: '未知' };
      return h(
        NTag,
        { type: status.type, size: 'small' },
        { default: () => status.label },
      );
    },
  },
  {
    title: '操作',
    key: 'action',
    width: 130,
    fixed: 'right',
    render(row) {
      const dropdownOptions = [
        {
          label: '详情',
          key: 'detail',
          icon: () => h(IconifyIcon, { icon: 'lucide:eye' }),
        },
        {
          label: '修改',
          key: 'edit',
          icon: () => h(IconifyIcon, { icon: 'lucide:pencil' }),
        },
        {
          label: '重置密码',
          key: 'resetPwd',
          icon: () => h(IconifyIcon, { icon: 'lucide:key' }),
        },
        {
          type: 'divider',
          key: 'divider',
        },
        {
          label: '删除',
          key: 'delete',
          icon: () =>
            h(IconifyIcon, { icon: 'lucide:trash-2', class: 'text-red-500' }),
        },
      ];

      return h(
        NSpace,
        { size: 'small' },
        {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                type: 'primary',
                text: true,
                onClick: () => handleDetail(row),
              },
              { default: () => '详情' },
            ),
            h(
              NButton,
              {
                size: 'small',
                type: 'primary',
                text: true,
                onClick: () => handleEdit(row),
              },
              { default: () => '修改' },
            ),
            h(
              NDropdown,
              {
                options: dropdownOptions.slice(2),
                onSelect: (key: string) => handleDropdownSelect(key, row),
              },
              {
                default: () =>
                  h(
                    NButton,
                    { size: 'small', text: true },
                    { default: () => '更多' },
                  ),
              },
            ),
          ],
        },
      );
    },
  },
];

async function loadUserData() {
  userLoading.value = true;
  try {
    const res = await userApi.list({
      page: userPagination.value.page,
      size: userPagination.value.pageSize,
      deptId: selectedDeptId.value,
      description: userSearchForm.value.description || undefined,
    });
    userData.value = res.list;
    userPagination.value.itemCount = res.total;
  } catch (error) {
    console.error('加载用户数据失败:', error);
  } finally {
    userLoading.value = false;
  }
}

function handleSearch() {
  handlePageChange(1);
}

function handleImport() {
  // TODO: 实现导入用户功能
}

function handleExport() {
  userApi.export({
    deptId: selectedDeptId.value,
    description: userSearchForm.value.description || undefined,
  });
}

// ==================== 用户操作逻辑 ====================
// 详情抽屉
const detailDrawerVisible = ref(false);
const detailUserId = ref<string>();

function handleDetail(row: UserResp) {
  detailUserId.value = row.id;
  detailDrawerVisible.value = true;
}

// 编辑抽屉
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
  loadUserData();
}

// 重置密码
const resetPasswordDialogVisible = ref(false);
const newPassword = ref('');

function handleResetPassword(row: UserResp) {
  dialog.warning({
    title: '重置密码',
    content: `确定要重置用户"${row.username}"的密码吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const password = await userApi.resetPassword(row.id);
        newPassword.value = password;
        resetPasswordDialogVisible.value = true;
      } catch (error) {
        console.error('重置密码失败:', error);
        message.error('重置密码失败');
      }
    },
  });
}

// 复制密码
async function handleCopyPassword() {
  try {
    await navigator.clipboard.writeText(newPassword.value);
    message.success('密码已复制到剪贴板');
  } catch (error) {
    console.error('复制失败:', error);
    message.error('复制失败');
  }
}

function handleDelete(row: UserResp) {
  dialog.warning({
    title: '删除用户',
    content: `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await userApi.delete(row.id);
        message.success('删除成功');
        loadUserData();
      } catch (error) {
        console.error('删除用户失败:', error);
        message.error('删除失败');
      }
    },
  });
}

function handleDropdownSelect(key: string, row: UserResp) {
  switch (key) {
    case 'delete': {
      handleDelete(row);
      break;
    }
    case 'detail': {
      handleDetail(row);
      break;
    }
    case 'edit': {
      handleEdit(row);
      break;
    }
    case 'resetPwd': {
      handleResetPassword(row);
      break;
    }
  }
}

function handlePageChange(page: number) {
  userPagination.value.page = page;
  loadUserData();
}

onMounted(() => {
  loadDeptData();
  loadRoleOptions();
  loadUserData();
});
</script>

<template>
  <Page>
    <NSplit
      direction="horizontal"
      default-size="200px"
      min="200px"
      max="320px"
      :resizable="true"
      class="h-full p-2"
    >
      <template #1>
        <!-- 左侧部门树 -->
        <div class="h-full bg-background p-4">
          <NInput
            v-model:value="deptSearchKeyword"
            placeholder="搜索部门"
            clearable
            class="mb-4"
          >
            <template #prefix>
              <NIcon><SearchOutline /></NIcon>
            </template>
          </NInput>
          <NTree
            :data="deptData"
            :show-irrelevant-nodes="false"
            :pattern="deptSearchKeyword"
            :default-expand-all="true"
            :selected-keys="selectedDeptKeys"
            :node-props="deptNodeProps"
            key-field="id"
            label-field="name"
            children-field="children"
            selectable
            block-line
          />
        </div>
      </template>
      <template #2>
        <!-- 右侧用户列表 -->
        <div class="h-full bg-background p-4">
          <!-- 用户搜索和操作栏 -->
          <div class="flex items-center justify-between mb-4 gap-3">
            <div class="flex items-center gap-2">
              <NInput
                v-model:value="userSearchForm.description"
                placeholder="搜索关键字（用户名/昵称）"
                clearable
                class="w-[240px]"
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <NIcon><SearchOutline /></NIcon>
                </template>
              </NInput>
              <NButton type="primary" @click="handleSearch">
                <template #icon><IconifyIcon icon="lucide:search" /></template>
                查询
              </NButton>
            </div>
            <NSpace>
              <NButton type="primary" @click="handleAdd">
                <template #icon><IconifyIcon icon="lucide:plus" /></template>
                新增
              </NButton>
              <NButton @click="handleImport">
                <template #icon><IconifyIcon icon="lucide:upload" /></template>
                导入
              </NButton>
              <NButton @click="handleExport">
                <template #icon><IconifyIcon icon="lucide:download" /></template>
                导出
              </NButton>
            </NSpace>
          </div>
          <!-- 用户表格 -->
          <NDataTable
            :columns="userColumns"
            :data="userData"
            :loading="userLoading"
            :row-key="(row) => row.id"
            :pagination="userPagination"
            scroll-x="1000px"
            remote
          />
        </div>
      </template>
    </NSplit>

    <!-- 用户详情抽屉 -->
    <UserDetailDrawer
      v-model:visible="detailDrawerVisible"
      :user-id="detailUserId"
      @edit="handleEdit"
    />

    <!-- 用户编辑抽屉 -->
    <UserEditDrawer
      v-model:visible="editDrawerVisible"
      :user-id="editUserId"
      :dept-data="deptData"
      :role-options="roleOptions"
      :default-dept-id="selectedDeptId"
      @success="handleEditSuccess"
    />

    <!-- 重置密码对话框 -->
    <NModal
      v-model:show="resetPasswordDialogVisible"
      preset="dialog"
      title="密码重置成功"
      positive-text="确定"
      @positive-click="resetPasswordDialogVisible = false"
    >
      <div class="space-y-4">
        <div class="text-orange-500 flex items-center gap-2">
          <IconifyIcon icon="lucide:alert-triangle" class="text-lg" />
          <span class="font-medium">新密码只显示一次，请妥善保管！</span>
        </div>
        <div class="flex items-center gap-2 p-3 bg-gray-100 dark:bg-gray-800 rounded">
          <span class="flex-1 font-mono text-lg select-all">{{ newPassword }}</span>
          <NButton
            type="primary"
            size="small"
            @click="handleCopyPassword"
          >
            <template #icon>
              <IconifyIcon icon="lucide:copy" />
            </template>
            复制
          </NButton>
        </div>
      </div>
    </NModal>
  </Page>
</template>
