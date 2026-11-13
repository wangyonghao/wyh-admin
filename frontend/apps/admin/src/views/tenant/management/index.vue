<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { TenantResp } from '#/api/tenant/management';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useVbenDrawer } from '#/components/popup/drawer';
import { $t } from '@vben/locales';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { deleteTenant, listTenant } from '#/api/tenant';
import { Button } from '#/ui/button';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';
import { toast } from '#/ui-patterns/toast';

import { useColumns, useGridFormSchema } from './data';
import TenantForm from './modules/form.vue';

defineOptions({ name: 'TenantManagement' });

const confirmAction = ref<ConfirmActionExpose | null>(null);

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: TenantForm,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
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
          return await listTenant({
            page: page.currentPage,
            pageSize: page.pageSize,
            name: text(formValues?.name),
            status: formValues?.status,
            code: undefined,
            domain: undefined,
            packageId: undefined,
            sort: ['id,desc'],
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
  } as VxeTableGridOptions<TenantResp>,
});

function text(value: unknown) {
  const trimmed = typeof value === 'string' ? value.trim() : '';
  return trimmed || undefined;
}

function onCreate() {
  formDrawerApi.setData({}).open();
}

function onEdit(row: TenantResp) {
  formDrawerApi.setData(row).open();
}

async function onDelete(row: TenantResp) {
  if (row.id === '0') return;
  const ok = await confirmAction.value?.ask({
    title: '删除租户',
    description: `确定删除「${row.name}」？`,
    tone: 'destructive',
  });
  if (!ok) return;
  await deleteTenant(row.id);
  toast.success('删除成功');
  await gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <ConfirmAction ref="confirmAction" />
    <FormDrawer @success="gridApi.query()" />
    <Grid :table-title="$t('tenant.management.listTitle')">
      <template #toolbar-tools>
        <span v-access:code="['tenant:management:create']">
          <Button type="button" @click="onCreate">
            {{ $t('pages.common.add') }}
          </Button>
        </span>
      </template>
      <template #action="{ row }">
        <div class="inline-flex items-center gap-2">
          <span v-access:code="['tenant:management:update']">
            <Button
              type="button"
              variant="link"
              size="sm"
              class="h-auto px-1"
              @click="onEdit(row)"
            >
              编辑
            </Button>
          </span>
          <span v-access:code="['tenant:management:delete']">
            <Button
              type="button"
              variant="link"
              size="sm"
              class="text-destructive h-auto px-1"
              :disabled="row.id === '0'"
              @click="onDelete(row)"
            >
              删除
            </Button>
          </span>
        </div>
      </template>
    </Grid>
  </Page>
</template>
