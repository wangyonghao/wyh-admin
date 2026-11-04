<script setup lang="ts">
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { LoginLogQuery, LoginLogResult } from '#/api/system/login-log';

import { IconifyIcon } from '@vben/icons';

import { formatDateTimeRange } from '#/adapter/component/date-range';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { loginLogApi } from '#/api/system/login-log';
import { $t } from '#/locales';
import { Badge } from '#/ui/badge';
import { Button } from '#/ui/button';

const deviceTypeMap: Record<string, string> = {
  MOBILE: '应用程序',
  WEB: '网页端',
  OTHER: '其他',
};

function useSearchSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'username',
      label: '用户名',
    },
    {
      component: 'Input',
      fieldName: 'ipAddress',
      label: 'IP地址',
    },
    {
      component: 'Select',
      fieldName: 'loginStatus',
      label: $t('monitor.loginLog.loginStatus'),
      componentProps: {
        clearable: true,
        options: [
          { label: '成功', value: 'SUCCESS' },
          { label: '失败', value: 'FAILURE' },
        ],
      },
    },
    {
      component: 'DatePicker',
      fieldName: 'loginTime',
      label: $t('monitor.loginLog.loginTime'),
      componentProps: {
        type: 'datetimerange',
        clearable: true,
        format: 'yyyy-MM-dd HH:mm:ss',
      },
    },
  ];
}

function textValue(value: unknown) {
  return typeof value === 'string' ? value.trim() : '';
}

function loginQuery(formValues: Record<string, unknown>): LoginLogQuery {
  const range = formatDateTimeRange(formValues.loginTime);
  const username = textValue(formValues.username);
  const ipAddress = textValue(formValues.ipAddress);
  const loginStatus = formValues.loginStatus;
  return {
    username: username || undefined,
    ipAddress: ipAddress || undefined,
    loginStatus:
      loginStatus === 'SUCCESS' || loginStatus === 'FAILURE'
        ? loginStatus
        : undefined,
    loginTimeStart: range.start,
    loginTimeEnd: range.end,
  };
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useSearchSchema(),
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
  },
  gridOptions: {
    columns: [
      { type: 'seq', width: 70, fixed: 'left' },
      { field: 'username', title: '用户名', minWidth: 100 },
      {
        field: 'loginTime',
        title: $t('monitor.loginLog.loginTime'),
        minWidth: 160,
        sortable: true,
      },
      {
        field: 'loginStatus',
        title: $t('monitor.loginLog.loginStatus'),
        minWidth: 100,
        slots: { default: 'status' },
      },
      {
        field: 'ipAddress',
        title: $t('monitor.loginLog.ipAddress'),
        minWidth: 120,
      },
      {
        field: 'location',
        title: $t('monitor.loginLog.location'),
        minWidth: 120,
      },
      {
        field: 'deviceType',
        title: $t('monitor.loginLog.deviceType'),
        minWidth: 100,
        formatter: ({ cellValue }) =>
          deviceTypeMap[String(cellValue ?? '')] || cellValue || '-',
      },
      {
        field: 'browser',
        title: $t('monitor.loginLog.browser'),
        minWidth: 150,
      },
      { field: 'os', title: $t('monitor.loginLog.os'), minWidth: 120 },
      {
        field: 'failureReason',
        title: $t('monitor.loginLog.failureReason'),
        minWidth: 180,
        formatter: ({ cellValue }) => cellValue || '-',
      },
    ],
    height: 'auto',
    keepSource: true,
    pagerConfig: { pageSize: 10 },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await loginLogApi.listLoginLog({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...loginQuery(formValues ?? {}),
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
  } as VxeTableGridOptions<LoginLogResult>,
});

async function handleExport() {
  const formValues = (await gridApi.formApi?.getValues?.()) ?? {};
  loginLogApi.exportLoginLog(loginQuery(formValues));
}
</script>

<template>
  <div class="h-full">
    <Grid>
      <template #toolbar-tools>
        <Button type="button" variant="outline" @click="handleExport">
          <IconifyIcon icon="lucide:download" class="mr-1 size-4" />
          导出
        </Button>
      </template>
      <template #status="{ row }">
        <Badge
          :variant="row.loginStatus === 'SUCCESS' ? 'success' : 'destructive'"
        >
          {{ row.loginStatus === 'SUCCESS' ? '成功' : '失败' }}
        </Badge>
      </template>
    </Grid>
  </div>
</template>
