<script setup lang="ts">
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { OnlineUser } from '#/api/monitor/online';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { onlineApi } from '#/api/monitor/online';
import { Button } from '#/ui/button';
import { toast } from '#/ui-patterns/toast';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';

const confirmAction = ref<ConfirmActionExpose | null>(null);
const accessStore = useAccessStore();
const currentToken = accessStore.accessToken;

function useColumns(): VxeTableGridOptions<OnlineUser>['columns'] {
  return [
    { type: 'checkbox', width: 50, fixed: 'left' },
    { type: 'seq', width: 70, fixed: 'left' },
    { field: 'loginName', title: '用户名', minWidth: 120 },
    { field: 'ip', title: 'IP地址', minWidth: 140 },
    { field: 'location', title: '登录地点', minWidth: 150 },
    { field: 'browser', title: '浏览器', minWidth: 150 },
    { field: 'os', title: '操作系统', minWidth: 120 },
    { field: 'loginTime', title: '登录时间', minWidth: 160 },
    { field: 'lastActiveTime', title: '最后活跃时间', minWidth: 160 },
    {
      field: 'action',
      title: '操作',
      width: 100,
      fixed: 'right',
      align: 'center',
      slots: { default: 'action' },
    },
  ];
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        component: 'Input',
        fieldName: 'keyword',
        label: '用户名',
        componentProps: {
          placeholder: '搜索用户名',
        },
      },
    ],
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  },
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const keyword =
            typeof formValues.keyword === 'string'
              ? formValues.keyword.trim()
              : '';
          return await onlineApi.list({
            page: page.currentPage,
            pageSize: page.pageSize,
            keyword: keyword || undefined,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'token',
    },
    checkboxConfig: {
      highlight: true,
      checkMethod: ({ row }) => row.token !== currentToken,
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: { code: 'query' },
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<OnlineUser>,
});

function selectedTokens() {
  const rows = gridApi.grid?.getCheckboxRecords?.() ?? [];
  return rows.map((row: OnlineUser) => row.token);
}

async function refresh() {
  await gridApi.query();
  gridApi.grid?.clearCheckboxRow?.();
}

async function confirmKickout(row: OnlineUser) {
  const ok = await confirmAction.value?.ask({
    title: '强退用户',
    description: `确定要强退用户"${row.loginName}"吗？`,
    tone: 'destructive',
  });
  if (!ok) return;
  await onlineApi.kickout(row.token);
  toast.success('强退成功');
  await refresh();
}

async function handleBatchKickout() {
  const tokens = selectedTokens();
  if (tokens.length === 0) {
    toast.warning('请选择要强退的用户');
    return;
  }
  const ok = await confirmAction.value?.ask({
    title: '批量强退',
    description: `确定要强退选中的 ${tokens.length} 个用户吗？`,
    tone: 'destructive',
  });
  if (!ok) return;
  await onlineApi.batchKickout(tokens);
  toast.success('批量强退成功');
  await refresh();
}
</script>

<template>
  <Page auto-content-height>
    <ConfirmAction ref="confirmAction" />
    <Grid>
      <template #toolbar-tools>
        <Button type="button" variant="destructive" @click="handleBatchKickout">
          <IconifyIcon icon="lucide:user-x" class="mr-1 size-4" />
          批量强退
        </Button>
      </template>
      <template #action="{ row }">
        <Button
          type="button"
          variant="link"
          size="sm"
          class="text-destructive h-auto px-1"
          :disabled="row.token === currentToken"
          @click="confirmKickout(row)"
        >
          强退
        </Button>
      </template>
    </Grid>
  </Page>
</template>
