<script setup lang="ts">
import type { VbenFormSchema } from '#/components/ui-patterns/form';
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { SmsLogResp } from '#/api/system/sms-log';

import { IconifyIcon } from '@vben/icons';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { smsLogApi } from '#/api/system/sms-log';
import { Badge } from '#/ui/badge';
import { Button } from '#/ui/button';

function useSearchSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'configId', label: '配置ID' },
    { component: 'Input', fieldName: 'phone', label: '手机号' },
    {
      component: 'Select',
      fieldName: 'status',
      label: '发送状态',
      componentProps: {
        clearable: true,
        options: [
          { label: '成功', value: 1 },
          { label: '失败', value: 0 },
        ],
      },
    },
  ];
}

function textValue(value: unknown) {
  return typeof value === 'string' ? value.trim() : '';
}

function smsQuery(formValues: Record<string, unknown>) {
  const configId = textValue(formValues.configId);
  const phone = textValue(formValues.phone);
  const status = formValues.status;
  return {
    configId: configId || undefined,
    phone: phone || undefined,
    status: status === 0 || status === 1 ? status : undefined,
    sort: ['createTime,desc'] as string[],
  };
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useSearchSchema(),
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  },
  gridOptions: {
    columns: [
      { type: 'seq', width: 70, fixed: 'left' },
      { field: 'phone', title: '手机号', minWidth: 120 },
      { field: 'params', title: '参数配置', minWidth: 200, align: 'left' },
      {
        field: 'status',
        title: '发送状态',
        minWidth: 100,
        slots: { default: 'status' },
      },
      { field: 'resMsg', title: '返回数据', minWidth: 200, align: 'left' },
      { field: 'createUserString', title: '创建人', minWidth: 120 },
      {
        field: 'createTime',
        title: '创建时间',
        minWidth: 160,
        sortable: true,
      },
    ],
    height: 'auto',
    keepSource: true,
    pagerConfig: { pageSize: 10 },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await smsLogApi.list({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...smsQuery(formValues ?? {}),
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
  } as VxeTableGridOptions<SmsLogResp>,
});

async function handleExport() {
  const formValues = (await gridApi.formApi?.getValues?.()) ?? {};
  smsLogApi.export(smsQuery(formValues));
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
        <Badge :variant="row.status === 1 ? 'success' : 'destructive'">
          {{ row.status === 1 ? '成功' : '失败' }}
        </Badge>
      </template>
    </Grid>
  </div>
</template>
