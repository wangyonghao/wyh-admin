<script setup lang="ts">
import type { VbenFormSchema } from '#/components/ui-patterns/form';
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { OperationLogResp } from '#/api/monitor/log';

import { ref } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';
import { IconifyIcon } from '@vben/icons';

import { formatDateTimeRange } from '#/components/ui/date-range';
import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { logApi } from '#/api/monitor/log';
import { Badge } from '#/ui/badge';
import { badgeVariantForTag } from '#/ui/badge/variant';
import { Button } from '#/ui/button';

const operationTypeMap: Record<string, { label: string; type: string }> = {
  create: { type: 'success', label: '新增' },
  update: { type: 'info', label: '修改' },
  delete: { type: 'error', label: '删除' },
  login: { type: 'success', label: '登录' },
  logout: { type: 'warning', label: '登出' },
  send: { type: 'info', label: '发送' },
};

function operationLabel(operation: string) {
  return operationTypeMap[operation] || { type: 'default', label: operation };
}

function useSearchSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'operatorName', label: '操作人' },
    { component: 'Input', fieldName: 'operation', label: '操作类型' },
    { component: 'Input', fieldName: 'operatorIp', label: 'IP地址' },
    {
      component: 'DatePicker',
      fieldName: 'createTime',
      label: '操作时间',
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

function operationQuery(formValues: Record<string, unknown>) {
  const range = formatDateTimeRange(formValues.createTime);
  const operatorName = textValue(formValues.operatorName);
  const operation = textValue(formValues.operation);
  const operatorIp = textValue(formValues.operatorIp);
  return {
    operatorName: operatorName || undefined,
    operation: operation || undefined,
    operatorIp: operatorIp || undefined,
    createTime: range.start && range.end ? [range.start, range.end] : undefined,
  };
}

const detailData = ref<null | Record<string, any>>(null);
const detailLoading = ref(false);

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  class: 'w-[600px]',
  footer: false,
  title: '操作日志详情',
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useSearchSchema(),
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
  },
  gridOptions: {
    columns: [
      { type: 'seq', width: 70, fixed: 'left' },
      {
        field: 'operateTime',
        title: '操作时间',
        minWidth: 160,
        sortable: true,
      },
      { field: 'operatorName', title: '操作人', minWidth: 120 },
      {
        field: 'operation',
        title: '操作类型',
        minWidth: 120,
        slots: { default: 'operation' },
      },
      { field: 'objectType', title: '业务对象', minWidth: 120 },
      { field: 'operatorIp', title: 'IP地址', minWidth: 140 },
      { field: 'operatorLocation', title: '操作地点', minWidth: 150 },
      {
        field: 'action',
        title: '操作',
        width: 100,
        fixed: 'right',
        slots: { default: 'action' },
      },
    ],
    height: 'auto',
    keepSource: true,
    pagerConfig: { pageSize: 10 },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await logApi.list({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...operationQuery(formValues ?? {}),
            sort: ['operateTime,desc'],
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
  } as VxeTableGridOptions<OperationLogResp>,
});

async function handleExport() {
  const formValues = (await gridApi.formApi?.getValues?.()) ?? {};
  logApi.exportOperationLog(operationQuery(formValues));
}

async function handleDetail(row: OperationLogResp) {
  detailDrawerApi.open();
  detailLoading.value = true;
  try {
    detailData.value = await logApi.detail(row.id);
  } finally {
    detailLoading.value = false;
  }
}

function extraText(extra: unknown) {
  if (typeof extra !== 'string' || extra === '') return '';
  try {
    return JSON.stringify(JSON.parse(extra), null, 2);
  } catch {
    return extra;
  }
}
</script>

<template>
  <div class="h-full">
    <Grid>
      <template #toolbar-tools>
        <Button
          type="button"
          variant="outline"
          @click="handleExport"
        >
          <IconifyIcon
            icon="lucide:download"
            class="mr-1 size-4"
          />
          导出
        </Button>
      </template>
      <template #operation="{ row }">
        <Badge
          :variant="
            badgeVariantForTag(operationLabel(row.operation).type) ||
              'secondary'
          "
        >
          {{ operationLabel(row.operation).label }}
        </Badge>
      </template>
      <template #action="{ row }">
        <Button
          type="button"
          variant="link"
          size="sm"
          class="h-auto px-1"
          @click="handleDetail(row)"
        >
          详情
        </Button>
      </template>
    </Grid>

    <DetailDrawer>
      <div
        v-if="detailLoading"
        class="flex h-64 items-center justify-center"
      >
        加载中...
      </div>
      <div
        v-else-if="detailData"
        class="space-y-4"
      >
        <div class="grid grid-cols-2 gap-4">
          <div>
            <div class="text-muted-foreground mb-1 text-sm">
              操作人
            </div>
            <div class="font-medium">
              {{ detailData.operatorName }}
            </div>
          </div>
          <div>
            <div class="text-muted-foreground mb-1 text-sm">
              操作时间
            </div>
            <div class="font-medium">
              {{ detailData.operateTime }}
            </div>
          </div>
          <div>
            <div class="text-muted-foreground mb-1 text-sm">
              操作类型
            </div>
            <div class="font-medium">
              {{ detailData.operation }}
            </div>
          </div>
          <div>
            <div class="text-muted-foreground mb-1 text-sm">
              业务对象
            </div>
            <div class="font-medium">
              {{ detailData.objectType }}
            </div>
          </div>
          <div>
            <div class="text-muted-foreground mb-1 text-sm">
              IP地址
            </div>
            <div class="font-medium">
              {{ detailData.operatorIp }}
            </div>
          </div>
          <div>
            <div class="text-muted-foreground mb-1 text-sm">
              操作地点
            </div>
            <div class="font-medium">
              {{ detailData.operatorLocation }}
            </div>
          </div>
          <div class="col-span-2">
            <div class="text-muted-foreground mb-1 text-sm">
              状态
            </div>
            <Badge
              :variant="
                detailData.status === 'success' ? 'success' : 'destructive'
              "
            >
              {{ detailData.status === 'success' ? '成功' : '失败' }}
            </Badge>
          </div>
          <div
            v-if="detailData.remark"
            class="col-span-2"
          >
            <div class="text-muted-foreground mb-1 text-sm">
              备注
            </div>
            <div class="font-medium">
              {{ detailData.remark }}
            </div>
          </div>
          <div
            v-if="detailData.extra"
            class="col-span-2"
          >
            <div class="text-muted-foreground mb-1 text-sm">
              额外信息
            </div>
            <pre class="bg-muted overflow-auto rounded p-3 text-sm">{{
                extraText(detailData.extra)
            }}</pre>
          </div>
        </div>
      </div>
    </DetailDrawer>
  </div>
</template>
