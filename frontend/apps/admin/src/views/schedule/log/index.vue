<script setup lang="ts">
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { JobLogResp } from '#/api/schedule';

import { onMounted } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { Badge } from '#/ui/badge';
import { badgeVariantForTag } from '#/ui/badge/variant';
import { listJobLog } from '#/api/schedule';

import { useGridFieldColumns, useGridSearchFormSchema } from './data-scope';

const route = useRoute();

const [TableGrid, tableGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridSearchFormSchema(),
    submitOnChange: true,
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  },
  gridOptions: {
    columns: useGridFieldColumns(),
    border: true,
    height: 'auto',
    keepSource: true,
    columnConfig: { resizable: true },
    proxyConfig: {
      autoLoad: false,
      response: { list: 'records' },
      ajax: {
        query: async ({ page }, formValues) => {
          return await listJobLog({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: {
      custom: true,
      refresh: true,
      refreshOptions: { code: 'query' },
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<JobLogResp>,
});

function statusLabel(status: number) {
  if (status === 1) return { text: '运行中', type: 'info' as const };
  if (status === 2) return { text: '成功', type: 'success' as const };
  return { text: '失败', type: 'error' as const };
}

onMounted(async () => {
  if (route.query.jobId) {
    await tableGridApi.formApi.form.setValues({
      jobId: String(route.query.jobId),
    });
  }
  await tableGridApi.query();
});
</script>

<template>
  <Page auto-content-height>
    <TableGrid>
      <template #triggerType="{ row }">
        {{ row.triggerType === 'MANUAL' ? '手动' : '调度' }}
      </template>
      <template #status="{ row }">
        <Badge
          :variant="
            badgeVariantForTag(statusLabel(row.status).type) || 'secondary'
          "
        >
          {{ statusLabel(row.status).text }}
        </Badge>
      </template>
    </TableGrid>
  </Page>
</template>
