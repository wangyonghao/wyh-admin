<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { NoticeResp, NoticeDetailResp } from '#/api/system/notice';

import { h, onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { SearchOutline } from '@vicons/ionicons5';
import {
  NButton,
  NDataTable,
  NDatePicker,
  NDrawer,
  NDrawerContent,
  NIcon,
  NInput,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  useMessage,
} from 'naive-ui';

import { noticeApi } from '#/api/system/notice';
import { useDict } from '#/hooks';

import NoticeForm from './components/notice-form.vue';
import NoticeView from './components/notice-view.vue';

const message = useMessage();

// ==================== 字典数据 ====================
const {
  notice_type,
  notice_scope_enum,
  notice_method_enum,
  notice_status_enum,
} = useDict(
  'notice_type',
  'notice_scope_enum',
  'notice_method_enum',
  'notice_status_enum',
);

// ==================== 搜索表单 ====================
const searchForm = ref({
  title: '',
  type: null as string | null,
  publishTime: null as [number, number] | null,
  status: null as string | null,
});

// ==================== 表格数据 ====================
const tableData = ref<NoticeResp[]>([]);
const tableLoading = ref(false);
const tablePagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => {
    tablePagination.value.page = page;
    loadTableData();
  },
  onUpdatePageSize: (pageSize: number) => {
    tablePagination.value.pageSize = pageSize;
    tablePagination.value.page = 1;
    loadTableData();
  },
});

// ==================== 抽屉状态 ====================
const showFormDrawer = ref(false);
const showViewDrawer = ref(false);
const currentNoticeId = ref<string>();
const currentNoticeDetail = ref<NoticeDetailResp>();

// ==================== 表格列定义 ====================
const tableColumns: DataTableColumns<NoticeResp> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render: (_row, index) =>
      (tablePagination.value.page - 1) * tablePagination.value.pageSize +
      index +
      1,
  },
  {
    title: $t('system.notice.title'),
    key: 'title',
    minWidth: 200,
  },
  {
    title: $t('system.notice.createUser'),
    key: 'createUserString',
    minWidth: 120,
  },
  {
    title: $t('system.notice.type'),
    key: 'type',
    minWidth: 100,
    render(row) {
      const typeItem = notice_type?.value?.find((item) => String(item.value) === row.type);
      if (!typeItem) return row.type;
      return h(
        NTag,
        { type: (typeItem as any).tagType || 'default', size: 'small' },
        { default: () => typeItem.label },
      );
    },
  },
  {
    title: $t('system.notice.noticeScope'),
    key: 'noticeScope',
    minWidth: 120,
    render(row) {
      const scopeItem = notice_scope_enum?.value?.find(
        (item) => String(item.value) === row.noticeScope,
      );
      if (!scopeItem) return row.noticeScope;
      return h(
        NTag,
        { type: (scopeItem as any).tagType || 'default', size: 'small' },
        { default: () => scopeItem.label },
      );
    },
  },
  {
    title: $t('system.notice.noticeMethods'),
    key: 'noticeMethods',
    minWidth: 150,
    render(row) {
      const methods = row.noticeMethods?.split(',') || [];
      return h(
        NSpace,
        { size: 'small' },
        {
          default: () =>
            methods.map((method) => {
              const methodItem = notice_method_enum?.value?.find(
                (item) => String(item.value) === method,
              );
              if (!methodItem) return null;
              return h(
                NTag,
                { type: (methodItem as any).tagType || 'default', size: 'small' },
                { default: () => methodItem.label },
              );
            }),
        },
      );
    },
  },
  {
    title: $t('system.notice.isTiming'),
    key: 'isTiming',
    minWidth: 100,
    render(row) {
      return h(
        NTag,
        {
          type: row.isTiming === 'true' ? 'success' : 'default',
          size: 'small',
        },
        { default: () => (row.isTiming === 'true' ? '是' : '否') },
      );
    },
  },
  {
    title: $t('system.notice.isTop'),
    key: 'isTop',
    minWidth: 100,
    render(row) {
      return h(
        NTag,
        {
          type: row.isTop === 'true' ? 'warning' : 'default',
          size: 'small',
        },
        { default: () => (row.isTop === 'true' ? '是' : '否') },
      );
    },
  },
  {
    title: $t('system.notice.status'),
    key: 'status',
    minWidth: 100,
    render(row) {
      const statusItem = notice_status_enum?.value?.find(
        (item) => Number(item.value) === row.status,
      );
      if (!statusItem) return row.status;
      return h(
        NTag,
        { type: (statusItem as any).tagType || 'default', size: 'small' },
        { default: () => statusItem.label },
      );
    },
  },
  {
    title: $t('system.notice.publishTime'),
    key: 'publishTime',
    minWidth: 160,
  },
  {
    title: $t('common.operation'),
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
                type: 'info',
                text: true,
                onClick: () => handlePreview(row),
              },
              {
                icon: () => h(IconifyIcon, { icon: 'lucide:eye' }),
                default: () => '预览',
              },
            ),
            h(
              NButton,
              {
                size: 'small',
                type: 'primary',
                text: true,
                onClick: () => handleEdit(row),
              },
              {
                icon: () => h(IconifyIcon, { icon: 'lucide:pencil' }),
                default: () => '编辑',
              },
            ),
            h(
              NPopconfirm,
              {
                onPositiveClick: () => handleDelete(row),
              },
              {
                trigger: () =>
                  h(
                    NButton,
                    {
                      size: 'small',
                      type: 'error',
                      text: true,
                    },
                    {
                      icon: () => h(IconifyIcon, { icon: 'lucide:trash-2' }),
                      default: () => '删除',
                    },
                  ),
                default: () => `确定删除公告"${row.title}"吗？`,
              },
            ),
          ],
        },
      );
    },
  },
];

// ==================== 加载数据 ====================
async function loadTableData() {
  tableLoading.value = true;
  try {
    let publishTime: string | undefined;

    if (searchForm.value.publishTime) {
      const start = new Date(searchForm.value.publishTime[0])
        .toISOString()
        .slice(0, 19)
        .replace('T', ' ');
      const end = new Date(searchForm.value.publishTime[1])
        .toISOString()
        .slice(0, 19)
        .replace('T', ' ');
      publishTime = `${start},${end}`;
    }

    const res = await noticeApi.list({
      page: tablePagination.value.page,
      size: tablePagination.value.pageSize,
      title: searchForm.value.title || undefined,
      type: searchForm.value.type || undefined,
      publishTime,
      status: searchForm.value.status || undefined,
    });

    tableData.value = res.list;
    tablePagination.value.itemCount = res.total;
  } catch (error) {
    console.error('加载公告列表失败:', error);
    message.error('加载数据失败');
  } finally {
    tableLoading.value = false;
  }
}

// ==================== 搜索 ====================
function handleSearch() {
  tablePagination.value.page = 1;
  loadTableData();
}

// ==================== 重置 ====================
function handleReset() {
  searchForm.value = {
    title: '',
    type: null,
    publishTime: null,
    status: null,
  };
  handleSearch();
}

// ==================== 新增 ====================
function handleAdd() {
  currentNoticeId.value = undefined;
  showFormDrawer.value = true;
}

// ==================== 预览 ====================
async function handlePreview(record: NoticeResp) {
  try {
    const detail = await noticeApi.detail(record.id);
    currentNoticeDetail.value = detail;
    showViewDrawer.value = true;
  } catch (error) {
    console.error('加载公告详情失败:', error);
    message.error('加载数据失败');
  }
}

// ==================== 编辑 ====================
function handleEdit(record: NoticeResp) {
  currentNoticeId.value = record.id;
  showFormDrawer.value = true;
}

// ==================== 删除 ====================
async function handleDelete(row: NoticeResp) {
  try {
    await noticeApi.delete(row.id);
    message.success($t('pages.common.deleteSuccess'));
    await loadTableData();
  } catch (error) {
    console.error('删除公告失败:', error);
    message.error('删除失败');
  }
}

// ==================== 导出 ====================
function handleExport() {
  let publishTime: string | undefined;

  if (searchForm.value.publishTime) {
    const start = new Date(searchForm.value.publishTime[0])
      .toISOString()
      .slice(0, 19)
      .replace('T', ' ');
    const end = new Date(searchForm.value.publishTime[1])
      .toISOString()
      .slice(0, 19)
      .replace('T', ' ');
    publishTime = `${start},${end}`;
  }

  noticeApi.export({
    title: searchForm.value.title || undefined,
    type: searchForm.value.type || undefined,
    publishTime,
    status: searchForm.value.status || undefined,
  });
}

// ==================== 表单提交成功 ====================
function handleFormSuccess() {
  showFormDrawer.value = false;
  loadTableData();
}

// ==================== 初始化 ====================
onMounted(() => {
  loadTableData();
});
</script>

<template>
  <div class="h-full bg-background p-4">
    <!-- 搜索和操作栏 -->
    <div class="mb-4">
      <!-- 搜索表单 - 响应式网格布局 -->
      <div
        class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3 mb-3"
      >
        <NInput
          v-model:value="searchForm.title"
          :placeholder="$t('system.notice.title')"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <NIcon><SearchOutline /></NIcon>
          </template>
        </NInput>
        <NSelect
          v-model:value="searchForm.type"
          :options="notice_type as any"
          :placeholder="$t('system.notice.type')"
          clearable
        />
        <NSelect
          v-model:value="searchForm.status"
          :options="notice_status_enum as any"
          :placeholder="$t('system.notice.status')"
          clearable
        />
        <div class="sm:col-span-2 lg:col-span-1 xl:col-span-1">
          <NDatePicker
            v-model:value="searchForm.publishTime"
            type="datetimerange"
            clearable
            class="w-full"
            format="yyyy-MM-dd HH:mm:ss"
          />
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="flex items-center gap-2 flex-wrap">
        <NButton type="primary" @click="handleSearch">
          <template #icon><IconifyIcon icon="lucide:search" /></template>
          {{ $t('pages.common.search') }}
        </NButton>
        <NButton @click="handleReset">
          <template #icon><IconifyIcon icon="lucide:rotate-ccw" /></template>
          {{ $t('pages.common.reset') }}
        </NButton>
        <NButton type="success" @click="handleAdd">
          <template #icon><IconifyIcon icon="lucide:plus" /></template>
          {{ $t('pages.common.add') }}
        </NButton>
        <NButton type="error" @click="handleExport">
          <template #icon><IconifyIcon icon="lucide:download" /></template>
          {{ $t('pages.common.export') }}
        </NButton>
      </div>
    </div>

    <!-- 数据表格 -->
    <NDataTable
      :columns="tableColumns"
      :data="tableData"
      :loading="tableLoading"
      :row-key="(row) => row.id"
      :pagination="tablePagination"
      scroll-x="1600px"
    />

    <!-- 新增/编辑抽屉 -->
    <NDrawer
      v-model:show="showFormDrawer"
      :width="1000"
      placement="right"
    >
      <NDrawerContent
        :title="currentNoticeId ? $t('common.edit') : $t('common.create')"
        closable
      >
        <NoticeForm
          :notice-id="currentNoticeId"
          @success="handleFormSuccess"
          @cancel="showFormDrawer = false"
        />
      </NDrawerContent>
    </NDrawer>

    <!-- 查看抽屉 -->
    <NDrawer
      v-model:show="showViewDrawer"
      :width="900"
      placement="right"
    >
      <NDrawerContent
        :title="$t('common.detail')"
        closable
      >
        <NoticeView
          v-if="currentNoticeDetail"
          :notice="currentNoticeDetail"
        />
      </NDrawerContent>
    </NDrawer>
  </div>
</template>

<style lang="scss" scoped></style>
