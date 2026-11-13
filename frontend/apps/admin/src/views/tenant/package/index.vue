<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { TenantPackageResp } from '#/api/tenant/package';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useVbenDrawer } from '#/components/popup/drawer';
import { $t } from '@vben/locales';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { deleteTenantPackage, listTenantPackage } from '#/api/tenant';
import { Button } from '#/ui/button';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';
import { toast } from '#/ui-patterns/toast';

import { useColumns, useGridFormSchema } from './data';
import PackageForm from './modules/form.vue';

defineOptions({ name: 'TenantPackage' });

const confirmAction = ref<ConfirmActionExpose | null>(null);

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: PackageForm,
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
          const name = typeof formValues?.name === 'string' ? formValues.name.trim() : '';
          return await listTenantPackage({
            page: page.currentPage,
            pageSize: page.pageSize,
            name: name || undefined,
            status: formValues?.status,
            sort: ['sort,asc', 'id,desc'],
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
  } as VxeTableGridOptions<TenantPackageResp>,
});

function onCreate() {
  formDrawerApi.setData({}).open();
}

function onEdit(row: TenantPackageResp) {
  formDrawerApi.setData(row).open();
}

async function onDelete(row: TenantPackageResp) {
  const ok = await confirmAction.value?.ask({
    title: '删除套餐',
    description: `确定删除「${row.name}」？仍有租户使用时不能删除。`,
    tone: 'destructive',
  });
  if (!ok) return;
  await deleteTenantPackage(row.id);
  toast.success('删除成功');
  await gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <ConfirmAction ref="confirmAction" />
    <FormDrawer @success="gridApi.query()" />
    <Grid :table-title="$t('tenant.package.listTitle')">
      <template #toolbar-tools>
        <span v-access:code="['tenant:package:create']">
          <Button type="button" @click="onCreate">
            {{ $t('pages.common.add') }}
          </Button>
        </span>
      </template>
      <template #action="{ row }">
        <div class="inline-flex items-center gap-2">
          <span v-access:code="['tenant:package:update']">
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
          <span v-access:code="['tenant:package:delete']">
            <Button
              type="button"
              variant="link"
              size="sm"
              class="text-destructive h-auto px-1"
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
