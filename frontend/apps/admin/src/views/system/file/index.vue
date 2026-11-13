<script setup lang="ts">
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { FileCategory, FileResult } from '#/api/system/file';

import { ref, watch } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import FormTreeSelect from '#/components/ui/FormTreeSelect.vue';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { fileApi, resolveFilePreviewUrl } from '#/api/system/file';
import { Button } from '#/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '#/ui/dropdown-menu';
import { toast } from '#/ui-patterns/toast';
import { FileUpload } from '#/ui/upload';

const category = ref<FileCategory>('ALL');
const checkedCount = ref(0);

const categoryOptions = [
  { label: '全部', value: 'ALL' },
  { label: '压缩包', value: 'ARCHIVE' },
  {
    label: '文档',
    value: 'DOCUMENT',
    children: [
      { label: '全部', value: 'DOCUMENT' },
      { label: '文本', value: 'DOCUMENT_TEXT' },
      { label: '表格', value: 'DOCUMENT_SPREADSHEET' },
      { label: '幻灯片', value: 'DOCUMENT_PRESENTATION' },
      { label: 'PDF', value: 'DOCUMENT_PDF' },
    ],
  },
  { label: '图片', value: 'IMAGE' },
  { label: '多媒体', value: 'MEDIA' },
];

function getExtension(fileName?: string) {
  if (!fileName || !fileName.includes('.')) return '';
  return fileName.slice(fileName.lastIndexOf('.') + 1).toLowerCase();
}

function getFileIcon(row: FileResult) {
  const ext = getExtension(row.fileName);
  if (
    row.contentType?.startsWith('image/') ||
    ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(ext)
  ) {
    return { icon: 'vscode-icons:file-type-image', color: '' };
  }
  if (['doc', 'docx'].includes(ext)) {
    return { icon: 'vscode-icons:file-type-word', color: '' };
  }
  if (['xls', 'xlsx', 'csv'].includes(ext)) {
    return { icon: 'vscode-icons:file-type-excel', color: '' };
  }
  if (['ppt', 'pptx'].includes(ext)) {
    return { icon: 'vscode-icons:file-type-powerpoint', color: '' };
  }
  if (ext === 'pdf') return { icon: 'vscode-icons:file-type-pdf2', color: '' };
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) {
    return { icon: 'vscode-icons:file-type-zip', color: '' };
  }
  if (['mp4', 'avi', 'mov', 'mkv', 'mp3', 'wav'].includes(ext)) {
    return { icon: 'vscode-icons:file-type-video', color: '' };
  }
  return { icon: 'lucide:file', color: 'text-muted-foreground' };
}

function formatFileSize(bytes?: number) {
  if (bytes == null) return '-';
  if (bytes === 0) return '0B';
  if (bytes < 1024) return `${bytes}B`;
  if (bytes < 1024 * 1024) {
    const kb = bytes / 1024;
    return `${kb >= 10 ? kb.toFixed(0) : kb.toFixed(1)}K`;
  }
  if (bytes < 1024 * 1024 * 1024) {
    const mb = bytes / (1024 * 1024);
    return `${mb >= 10 ? mb.toFixed(0) : mb.toFixed(1)}M`;
  }
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)}G`;
}

function formatRelativeTime(value?: string) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  const now = new Date();
  const startOfToday = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate(),
  );
  const startOfTarget = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate(),
  );
  const dayDiff = Math.round(
    (startOfToday.getTime() - startOfTarget.getTime()) / 86_400_000,
  );
  const hm = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;

  if (dayDiff === 0) return `今天 ${hm}`;
  if (dayDiff === 1) return `昨天 ${hm}`;
  if (date.getFullYear() === now.getFullYear()) {
    return `${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')} ${hm}`;
  }
  return `${date.getFullYear()}/${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')} ${hm}`;
}

function resolveStorageLabel(row: FileResult) {
  const storage = row.storageType;
  if (!storage) return '-';
  if (typeof storage === 'string') return storage;
  return storage.description || '-';
}

function textValue(value: unknown) {
  return typeof value === 'string' ? value.trim() : '';
}

function currentSelection(): FileResult[] {
  return (gridApi.grid?.getCheckboxRecords?.() ?? []) as FileResult[];
}

function syncChecked() {
  checkedCount.value = currentSelection().length;
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridEvents: {
    checkboxAll: syncChecked,
    checkboxChange: syncChecked,
  },
  formOptions: {
    schema: [
      {
        component: 'Input',
        fieldName: 'fileName',
        label: '文件名',
        componentProps: {
          placeholder: '搜索文件名',
        },
      },
    ],
    showCollapseButton: false,
    submitOnChange: true,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  },
  gridOptions: {
    columns: [
      { type: 'checkbox', width: 48, fixed: 'left' },
      {
        field: 'fileName',
        title: '文件',
        minWidth: 220,
        align: 'left',
        slots: { default: 'fileName' },
      },
      {
        field: 'fileSize',
        title: '大小',
        width: 100,
        slots: { default: 'fileSize' },
      },
      {
        field: 'createTime',
        title: '时间',
        width: 160,
        sortable: true,
        slots: { default: 'createTime' },
      },
      {
        field: 'storageType',
        title: '存储',
        width: 140,
        slots: { default: 'storage' },
      },
      {
        field: 'action',
        title: '操作',
        width: 80,
        fixed: 'right',
        slots: { default: 'action' },
      },
    ],
    height: 'auto',
    keepSource: true,
    pagerConfig: { pageSize: 20 },
    sortConfig: {
      defaultSort: { field: 'createTime', order: 'desc' },
      remote: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page, sorts }, formValues) => {
          const order = sorts?.find(
            (item) => item.field === 'createTime',
          )?.order;
          const fileName = textValue(formValues?.fileName);
          return await fileApi.page({
            fileName: fileName || undefined,
            category: category.value === 'ALL' ? undefined : category.value,
            sortOrder: order === 'asc' ? 'asc' : 'desc',
            page: page.currentPage,
            pageSize: page.pageSize,
          });
        },
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: { code: 'query' },
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<FileResult>,
});

async function handleRowAction(key: string | number, row: FileResult) {
  switch (String(key)) {
    case 'download': {
      await fileApi.download(row.id, row.fileName);
      break;
    }
    case 'preview': {
      const url = resolveFilePreviewUrl(row.id);
      if (url) window.open(url, '_blank');
      break;
    }
    case 'delete': {
      await handleDelete(row);
      break;
    }
  }
}

async function reload() {
  await gridApi.query();
  gridApi.grid?.clearCheckboxRow?.();
  checkedCount.value = 0;
}

async function handleDelete(row: FileResult) {
  try {
    await fileApi.delete(row.id);
    toast.success('删除成功');
    await reload();
  } catch (error) {
    toast.error('删除失败');
    console.error(error);
  }
}

async function handleBatchDelete() {
  const ids = currentSelection()
    .map((row) => row.id)
    .filter((id) => id);
  if (!ids.length) return;
  try {
    await fileApi.batchDelete(ids);
    toast.success('批量删除成功');
    await reload();
  } catch (error) {
    toast.error('批量删除失败');
    console.error(error);
  }
}

async function handleBatchDownload() {
  const rows = currentSelection();
  if (!rows.length) return;
  for (const row of rows) {
    await fileApi.download(row.id, row.fileName);
  }
  toast.success(`已开始下载 ${rows.length} 个文件`);
}

async function handleUpload(files: File[]) {
  const file = files[0];
  if (!file) return;
  try {
    await fileApi.upload(file);
    toast.success('上传成功');
    await reload();
  } catch (error) {
    toast.error('上传失败');
    console.error(error);
  }
}

watch(category, () => {
  gridApi.reload();
});
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #toolbar-actions>
        <div class="flex flex-wrap items-center gap-2">
          <Button
            type="button"
            variant="outline"
            :disabled="checkedCount === 0"
            @click="handleBatchDownload"
          >
            <IconifyIcon
              icon="lucide:download"
              class="mr-1 size-4"
            />
            下载
          </Button>
          <FormTreeSelect
            :value="category"
            :options="categoryOptions"
            key-field="value"
            placeholder="筛选"
            clearable
            default-expand-all
            class="w-40"
            @update:value="
              (value) => (category = (value as FileCategory) || 'ALL')
            "
          />
          <Button
            v-if="checkedCount > 0"
            type="button"
            variant="destructive"
            @click="handleBatchDelete"
          >
            删除所选 ({{ checkedCount }})
          </Button>
        </div>
      </template>
      <template #toolbar-tools>
        <FileUpload @select="handleUpload">
          <Button type="button">
            <IconifyIcon
              icon="lucide:upload"
              class="mr-1 size-4"
            />
            上传
          </Button>
        </FileUpload>
      </template>
      <template #fileName="{ row }">
        <div class="flex min-w-0 items-center gap-2">
          <IconifyIcon
            :icon="getFileIcon(row).icon"
            :class="['shrink-0 text-xl', getFileIcon(row).color]"
          />
          <span
            class="truncate"
            :title="row.fileName"
          >{{ row.fileName }}</span>
        </div>
      </template>
      <template #fileSize="{ row }">
        {{ formatFileSize(row.fileSize) }}
      </template>
      <template #createTime="{ row }">
        {{ formatRelativeTime(row.createTime) }}
      </template>
      <template #storage="{ row }">
        {{ resolveStorageLabel(row) }}
      </template>
      <template #action="{ row }">
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <Button
              type="button"
              variant="ghost"
              size="icon"
            >
              <IconifyIcon
                icon="lucide:ellipsis"
                class="size-4"
              />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem @select="handleRowAction('download', row)">
              <IconifyIcon
                icon="lucide:download"
                class="mr-2 size-4"
              />
              下载
            </DropdownMenuItem>
            <DropdownMenuItem @select="handleRowAction('preview', row)">
              <IconifyIcon
                icon="lucide:eye"
                class="mr-2 size-4"
              />
              预览
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              class="text-destructive"
              @select="handleRowAction('delete', row)"
            >
              <IconifyIcon
                icon="lucide:trash-2"
                class="mr-2 size-4"
              />
              删除
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </template>
    </Grid>
  </Page>
</template>
