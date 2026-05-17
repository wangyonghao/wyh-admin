<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { DictResult } from '#/api/system/dict';

import { h, onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { Page } from '@vben/common-ui';
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NSpace,
  NTag,
  useDialog,
  useMessage,
} from 'naive-ui';

import { dictApi } from '#/api/system';

import DictEditDrawer from './components/dict-edit-drawer.vue';

const message = useMessage();
const dialog = useDialog();

// ==================== 查询表单 ====================
const searchForm = ref({
  dictType: undefined as string | undefined,
  value: undefined as string | undefined,
  label: undefined as string | undefined,
  enabled: undefined as boolean | undefined,
});

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '启用', value: true },
  { label: '禁用', value: false },
];

// ==================== 数据表格 ====================
const tableData = ref<DictResult[]>([]);
const tableLoading = ref(false);
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => {
    pagination.value.page = page;
    loadTableData();
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.value.pageSize = pageSize;
    pagination.value.page = 1;
    loadTableData();
  },
});

const columns: DataTableColumns<DictResult> = [
  {
    title: '序号',
    key: 'index',
    width: 80,
    fixed: 'left',
    render: (_row, index) =>
      (pagination.value.page - 1) * pagination.value.pageSize + index + 1,
  },
  {
    title: '字典类型',
    key: 'dictType',
    minWidth: 150,
    fixed: 'left',
  },
  {
    title: '字典值',
    key: 'value',
    minWidth: 120,
  },
  {
    title: '显示名称',
    key: 'label',
    minWidth: 150,
  },
  {
    title: '状态',
    key: 'enabled',
    width: 100,
    render(row) {
      return h(
        NTag,
        {
          type: row.enabled ? 'success' : 'error',
          size: 'small',
        },
        { default: () => (row.enabled ? '启用' : '禁用') },
      );
    },
  },
  {
    title: '排序',
    key: 'sort',
    width: 100,
  },
  {
    title: '描述',
    key: 'description',
    minWidth: 200,
    ellipsis: {
      tooltip: true,
    },
    render(row) {
      return row.description || '-';
    },
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    render(row) {
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
                onClick: () => handleEdit(row),
              },
              { default: () => '编辑' },
            ),
            h(
              NButton,
              {
                size: 'small',
                type: 'error',
                text: true,
                onClick: () => handleDelete(row),
              },
              { default: () => '删除' },
            ),
            h(
              NButton,
              {
                size: 'small',
                text: true,
                onClick: () => handleClearCache(row),
              },
              { default: () => '清除缓存' },
            ),
          ],
        },
      );
    },
  },
];

// ==================== 数据加载 ====================
async function loadTableData() {
  tableLoading.value = true;
  try {
    // 构建查询条件
    const description = [
      searchForm.value.dictType,
      searchForm.value.value,
      searchForm.value.label,
    ]
      .filter(Boolean)
      .join(' ');

    const res = await dictApi.page({
      page: pagination.value.page,
      size: pagination.value.pageSize,
      description: description || undefined,
      dictType: searchForm.value.dictType,
    });

    // 根据状态过滤
    let filteredData = res.list;
    if (searchForm.value.enabled !== undefined) {
      filteredData = filteredData.filter(
        (item) => item.enabled === searchForm.value.enabled,
      );
    }

    tableData.value = filteredData;
    pagination.value.itemCount = res.total;
  } catch (error) {
    console.error('加载字典数据失败:', error);
    message.error('加载数据失败');
  } finally {
    tableLoading.value = false;
  }
}

function handleSearch() {
  pagination.value.page = 1;
  loadTableData();
}

function handleReset() {
  searchForm.value = {
    dictType: undefined,
    value: undefined,
    label: undefined,
    enabled: undefined,
  };
  handleSearch();
}

// ==================== 编辑抽屉 ====================
const editDrawerVisible = ref(false);
const editDictData = ref<DictResult | undefined>();

function handleAdd() {
  editDictData.value = undefined;
  editDrawerVisible.value = true;
}

function handleEdit(row: DictResult) {
  editDictData.value = row;
  editDrawerVisible.value = true;
}

function handleEditSuccess() {
  loadTableData();
}

// ==================== 删除操作 ====================
function handleDelete(row: DictResult) {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除字典项 "${row.label}" 吗？此操作不可恢复！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await dictApi.delete([row.id]);
        message.success('删除成功');
        loadTableData();
      } catch (error) {
        console.error('删除字典失败:', error);
        message.error('删除失败');
      }
    },
  });
}

// ==================== 清除缓存 ====================
function handleClearCache(row: DictResult) {
  dialog.info({
    title: '清除缓存',
    content: `确定要清除字典类型 "${row.dictType}" 的缓存吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await dictApi.clearCache(row.dictType);
        message.success('缓存清除成功');
      } catch (error) {
        console.error('清除缓存失败:', error);
        message.error('清除缓存失败');
      }
    },
  });
}

onMounted(() => {
  loadTableData();
});
</script>

<template>
  <Page>
    <div class="h-full bg-background p-4">
      <!-- 查询表单 -->
      <NForm
        :model="searchForm"
        label-placement="left"
        label-width="auto"
        class="mb-4"
      >
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <NFormItem label="字典类型" path="dictType">
            <NInput
              v-model:value="searchForm.dictType"
              placeholder="请输入字典类型"
              clearable
              @keyup.enter="handleSearch"
            />
          </NFormItem>
          <NFormItem label="字典值" path="value">
            <NInput
              v-model:value="searchForm.value"
              placeholder="请输入字典值"
              clearable
              @keyup.enter="handleSearch"
            />
          </NFormItem>
          <NFormItem label="显示名称" path="label">
            <NInput
              v-model:value="searchForm.label"
              placeholder="请输入显示名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </NFormItem>
          <NFormItem label="状态" path="enabled">
            <NSelect
              v-model:value="searchForm.enabled"
              :options="statusOptions"
              placeholder="请选择状态"
              clearable
            />
          </NFormItem>
        </div>
        <div class="flex justify-end gap-2">
          <NButton @click="handleReset">
            <template #icon>
              <IconifyIcon icon="lucide:rotate-ccw" />
            </template>
            重置
          </NButton>
          <NButton type="primary" @click="handleSearch">
            <template #icon>
              <IconifyIcon icon="lucide:search" />
            </template>
            查询
          </NButton>
        </div>
      </NForm>

      <!-- 操作按钮 -->
      <div class="flex justify-end mb-4">
        <NButton type="primary" @click="handleAdd">
          <template #icon>
            <IconifyIcon icon="lucide:plus" />
          </template>
          新建字典项
        </NButton>
      </div>

      <!-- 数据表格 -->
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="tableLoading"
        :row-key="(row) => row.id"
        :pagination="pagination"
        :scroll-x="1200"
        remote
      />
    </div>

    <!-- 编辑抽屉 -->
    <DictEditDrawer
      v-model:visible="editDrawerVisible"
      :dict-data="editDictData"
      @success="handleEditSuccess"
    />
  </Page>
</template>
