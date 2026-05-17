<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';

import type { DeptResp } from '#/api/system/dept';

import { h, ref, watch } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { useDebounceFn } from '@vueuse/core';
import {
  NButton,
  NDataTable,
  NInput,
  NSpace,
  NTag,
  useDialog,
  useMessage,
} from 'naive-ui';

import { deptApi } from '#/api/system/dept';
import { useDownload } from '#/hooks/app/useDownload';

import EditModal from './dept-drawer.vue';

// 搜索表单
const searchForm = ref({
  name: '',
});

const message = useMessage();
const dialog = useDialog();
const loading = ref(false);
const tableData = ref<DeptResp[]>([]);
const expandedRowKeys = ref<string[]>([]);

// 创建列配置
const createColumns = (): DataTableColumns<DeptResp> => {
  return [
    { title: $t('system.dept.name'), key: 'name', align: 'left', width: 300 },
    {
      title: $t('system.dept.status'),
      key: 'status',
      align: 'center',
      width: 80,
      render: (row) => {
        return h(
          NTag,
          { type: row.status === 1 ? 'success' : 'error' },
          {
            default: () =>
              row.status === 1 ? $t('common.enabled') : $t('common.disabled'),
          },
        );
      },
    },
    {
      title: $t('system.dept.description'),
      key: 'description',
      align: 'left',
      ellipsis: {
        tooltip: true,
      },
    },
    {
      title: $t('pages.common.operation'),
      key: 'action',
      align: 'center',
      width: 150,
      fixed: 'right',
      render: (row) => {
        return h(
          NSpace,
          { justify: 'center' },
          {
            default: () => [
              h(
                'span',
                { 'v-access:code': ['system:dept:update'] },
                h(
                  NButton,
                  {
                    text: true,
                    onClick: () => handleEdit(row),
                  },
                  {
                    icon: () => h(IconifyIcon, { icon: 'lucide:pencil' }),
                  },
                ),
              ),
              h(
                'span',
                { 'v-access:code': ['system:dept:delete'] },
                h(
                  NButton,
                  {
                    text: true,
                    onClick: () => handleDelete(row),
                  },
                  {
                    icon: () =>
                      h(IconifyIcon, {
                        icon: 'lucide:trash-2',
                        class: 'text-red-500',
                      }),
                  },
                ),
              ),
            ],
          },
        );
      },
    },
  ];
};

const columns = ref(createColumns());

// 加载数据
async function loadData() {
  try {
    loading.value = true;
    const res = await deptApi.list({
      description: searchForm.value.name || undefined,
    });
    tableData.value = res;
    // 默认展开所有节点
    if (res.length > 0) {
      expandAllNodes(res);
    }
  } catch (error) {
    console.error('Failed to load dept data:', error);
    message.error($t('pages.common.loadFailed'));
  } finally {
    loading.value = false;
  }
}

// 递归展开所有节点
function expandAllNodes(data: DeptResp[]) {
  const keys: string[] = [];
  const traverse = (items: DeptResp[]) => {
    items.forEach((item) => {
      if (item.children && item.children.length > 0) {
        keys.push(item.id);
        traverse(item.children);
      }
    });
  };
  traverse(data);
  expandedRowKeys.value = keys;
}

// 折叠所有节点
function collapseAllNodes() {
  expandedRowKeys.value = [];
}

const editModalVisible = ref(false);
const editModalData = ref<DeptResp | undefined>(undefined);

const handleEdit = (record: DeptResp) => {
  editModalData.value = record;
  editModalVisible.value = true;
};

const handleAdd = () => {
  editModalData.value = undefined;
  editModalVisible.value = true;
};

const handleDelete = (row: DeptResp) => {
  dialog.warning({
    title: $t('system.dept.deleteTitle'),
    content: $t('ui.actionMessage.deleteConfirm', [row.name]),
    positiveText: $t('common.confirm'),
    negativeText: $t('common.cancel'),
    showIcon: false,
    onPositiveClick: async () => {
      try {
        await deptApi.delete(row.id);
        message.success($t('pages.common.deleteSuccess'));
        await loadData();
      } catch (error) {
        console.error('Failed to delete dept:', error);
      }
    },
  });
};

const handleExport = () => {
  useDownload(() =>
    deptApi.export({ description: searchForm.value.name || undefined }),
  );
};

// 搜索（防抖）
const handleSearch = useDebounceFn(() => {
  loadData();
}, 300);

// 监听搜索表单变化
watch(
  () => searchForm.value.name,
  () => {
    handleSearch();
  },
);

// 树列表折叠状态
const expanded = ref<boolean>(true);
const handleExpand = () => {
  expanded.value = !expanded.value;
  if (expanded.value) {
    expandAllNodes(tableData.value);
  } else {
    collapseAllNodes();
  }
};

// 初始加载
loadData();
</script>

<template>
  <Page auto-content-height>
    <div class="flex flex-col h-full bg-background p-4">
      <!-- 工具栏 -->
      <div class="flex items-center justify-between w-full pb-4 gap-4">
        <!-- 左侧搜索框 -->
        <div class="w-64">
          <NInput
            v-model:value="searchForm.name"
            :placeholder="$t('system.dept.name')"
            clearable
          >
            <template #prefix>
              <IconifyIcon icon="lucide:search" class="w-4 h-4" />
            </template>
          </NInput>
        </div>

        <!-- 右侧操作按钮 -->
        <NSpace>
          <span v-access:code="['system:dept:create']">
            <NButton @click="handleAdd" secondary>
              <template #icon>
                <IconifyIcon icon="lucide:plus" />
              </template>
              {{ $t('pages.common.add') }}
            </NButton>
          </span>
          <span v-access:code="['system:dept:export']">
            <NButton secondary @click="handleExport">
              <template #icon>
                <IconifyIcon icon="lucide:download" />
              </template>
              {{ $t('pages.common.export') }}
            </NButton>
          </span>
          <NButton secondary @click="handleExpand">
            <template #icon>
              <IconifyIcon
                :icon="expanded ? 'lucide:chevrons-up' : 'lucide:chevrons-down'"
              />
            </template>
            {{
              expanded ? $t('pages.common.collapse') : $t('pages.common.expand')
            }}
          </NButton>
        </NSpace>
      </div>

      <!-- 表格 -->
      <div class="flex-1 overflow-hidden">
        <NDataTable
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :row-key="(row: DeptResp) => row.id"
          :expanded-row-keys="expandedRowKeys"
          @update:expanded-row-keys="
            (keys) => (expandedRowKeys = keys as string[])
          "
          children-key="children"
          striped
          bordered
          scroll-x="800"
          size="small"
          flex-height
        />
      </div>
    </div>

    <EditModal
      v-model:visible="editModalVisible"
      :data="editModalData"
      @success="loadData()"
    />
  </Page>
</template>

<style scoped>
:deep(.n-data-table) {
  height: 100%;
}
</style>
