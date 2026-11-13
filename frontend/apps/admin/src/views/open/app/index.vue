<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/components/ui-patterns/vxe-table';
import type { OpenAppApi } from '#/api';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useVbenDrawer } from '#/components/popup/drawer';
import { $t } from '@vben/locales';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { openAppApi } from '#/api/open';
import { Button } from '#/ui/button';
import { toast } from '#/ui-patterns/toast';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';
import { ToolbarActions } from '#/ui-patterns/toolbar-actions';

import { useColumns, useGridFormSchema } from './data';
import AppDetail from './modules/detail.vue';
import AppForm from './modules/form.vue';

defineOptions({ name: 'OpenApp' });
const confirmAction = ref<ConfirmActionExpose | null>(null);

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: AppForm,
  destroyOnClose: true,
});

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: AppDetail,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await openAppApi.list({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
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
  } as VxeTableGridOptions<OpenAppApi.AppResp>,
});

function onActionClick(e: OnActionClickParams<OpenAppApi.AppResp>) {
  switch (e.code) {
    case 'delete': {
      onDelete(e.row);
      break;
    }
    case 'detail': {
      onDetail(e.row);
      break;
    }
    case 'edit': {
      onEdit(e.row);
      break;
    }
    case 'resetSecret': {
      onResetSecret(e.row);
      break;
    }
  }
}

function onEdit(row: OpenAppApi.AppResp) {
  formDrawerApi.setData(row).open();
}

function onDetail(row: OpenAppApi.AppResp) {
  detailDrawerApi.setData(row).open();
}

function onDelete(row: OpenAppApi.AppResp) {
  openAppApi
    .delete(row.id)
    .then(() => {
      toast.success(`删除应用 "${row.name}" 成功`);
      onRefresh();
    })
    .catch(() => {
      toast.error(`删除应用 "${row.name}" 失败`);
    });
}

async function onShowSecret(row: OpenAppApi.AppResp) {
  try {
    const { secretKey } = await openAppApi.getSecretKey(row.id);
    row.secretKey = secretKey;
  } catch {
    toast.error('获取密钥失败');
  }
}

function onHideSecret(row: OpenAppApi.AppResp) {
  row.secretKey = undefined;
}

async function onResetSecret(row: OpenAppApi.AppResp) {
  const ok = await confirmAction.value?.ask({
    title: '确认重置密钥',
    description: `确定要重置应用 "${row.name}" 的密钥吗？重置后原密钥将失效。`,
  });
  if (!ok) return;
  try {
    await openAppApi.resetSecretKey(row.id);
    toast.success('密钥重置成功');
    gridApi.query();
  } catch {
    toast.error('密钥重置失败');
  }
}

/**
 * 复制密钥到剪贴板
 * @param secretKey 要复制的密钥
 */
async function onCopySecret(secretKey: string) {
  try {
    await navigator.clipboard.writeText(secretKey);
    toast.success('密钥已复制到剪贴板');
  } catch {
    // 降级方案：使用传统的复制方法
    const textArea = document.createElement('textarea');
    textArea.value = secretKey;
    textArea.style.position = 'fixed';
    textArea.style.opacity = '0';
    document.body.append(textArea);
    textArea.focus();
    textArea.select();
    try {
      document.execCommand('copy');
      toast.success('密钥已复制到剪贴板');
    } catch {
      toast.error('复制失败，请手动复制');
    } finally {
      textArea.remove();
    }
  }
}

function onRefresh() {
  gridApi.query();
}

function onCreate() {
  formDrawerApi.setData({}).open();
}

// 导出
async function onExport() {
  try {
    const blob = await openAppApi.export(gridApi.formApi.form.values);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '应用列表.xlsx';
    link.click();
    window.URL.revokeObjectURL(url);
    toast.success('导出成功');
  } catch {
    toast.error('导出失败');
  }
}
</script>

<template>
  <Page auto-content-height>
    <ConfirmAction ref="confirmAction" />
    <FormDrawer @success="onRefresh" />
    <DetailDrawer />
    <Grid :table-title="$t('open.app.listTitle')">
      <template #toolbar-tools>
        <ToolbarActions>
          <Button type="button" @click="onCreate">
            {{ $t('pages.common.add') }}
          </Button>
          <Button type="button" variant="outline" @click="onExport">
            {{ $t('pages.common.export') }}
          </Button>
        </ToolbarActions>
      </template>
      <template #secretKey="{ row }">
        <div class="flex items-center justify-center gap-2">
          <span v-if="row.secretKey" class="font-mono">
            {{ row.secretKey }}
          </span>
          <span v-else class="text-gray-400">***********</span>

          <div class="flex gap-1">
            <Button
              v-if="row.secretKey"
              type="button"
              variant="link"
              size="sm"
              class="h-auto px-1"
              @click="onCopySecret(row.secretKey)"
            >
              {{ $t('open.app.copy') }}
            </Button>
            <Button
              v-if="row.secretKey"
              type="button"
              variant="link"
              size="sm"
              class="text-warning h-auto px-1"
              @click="onHideSecret(row)"
            >
              {{ $t('open.app.hide') }}
            </Button>
            <Button
              v-else
              type="button"
              variant="link"
              size="sm"
              class="h-auto px-1"
              @click="onShowSecret(row)"
            >
              {{ $t('open.app.show') }}
            </Button>
          </div>
        </div>
      </template>
    </Grid>
  </Page>
</template>
