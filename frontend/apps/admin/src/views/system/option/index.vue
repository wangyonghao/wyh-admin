<script setup lang="ts">
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { OptionResult } from '#/api/system/option';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { optionApi } from '#/api/system';
import { Badge } from '#/ui/badge';
import { Button } from '#/ui/button';
import { toast } from '#/ui-patterns/toast';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';

import OptionEditDrawer from './components/option-edit-drawer.vue';
import { useGridFieldColumns, useGridSearchFormSchema } from './data';

const confirmAction = ref<ConfirmActionExpose | null>(null);
const editDrawerVisible = ref(false);
const editOptionData = ref<OptionResult | undefined>();

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridSearchFormSchema(),
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  },
  gridOptions: {
    columns: useGridFieldColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const keyword =
            typeof formValues.keyword === 'string'
              ? formValues.keyword.trim()
              : '';
          return await optionApi.page({
            page: page.currentPage,
            pageSize: page.pageSize,
            keyword: keyword || undefined,
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
  } as VxeTableGridOptions<OptionResult>,
});

function handleAdd() {
  editOptionData.value = undefined;
  editDrawerVisible.value = true;
}

function handleEdit(row: OptionResult) {
  editOptionData.value = row;
  editDrawerVisible.value = true;
}

async function handleDelete(row: OptionResult) {
  const ok = await confirmAction.value?.ask({
    title: '删除确认',
    description: `确定要删除选项 "${row.label}" 吗？此操作不可恢复！`,
    tone: 'destructive',
  });
  if (!ok) return;
  try {
    await optionApi.delete([row.id]);
    toast.success('删除成功');
    await gridApi.query();
  } catch {
    toast.error('删除失败');
  }
}

async function handleClearCache(row: OptionResult) {
  const ok = await confirmAction.value?.ask({
    title: '清除缓存',
    description: `确定要清除选项类型 "${row.optionType}" 的缓存吗？`,
  });
  if (!ok) return;
  try {
    await optionApi.clearCache(row.optionType);
    toast.success('缓存清除成功');
  } catch {
    toast.error('清除缓存失败');
  }
}
</script>

<template>
  <Page auto-content-height>
    <ConfirmAction ref="confirmAction" />
    <Grid>
      <template #toolbar-tools>
        <Button type="button" @click="handleAdd"> 新建选项 </Button>
      </template>
      <template #enabled="{ row }">
        <Badge :variant="row.enabled ? 'success' : 'destructive'">
          {{ row.enabled ? '启用' : '禁用' }}
        </Badge>
      </template>
      <template #action="{ row }">
        <div class="inline-flex items-center gap-2">
          <Button
            type="button"
            variant="link"
            size="sm"
            class="h-auto px-1"
            @click="handleEdit(row)"
          >
            编辑
          </Button>
          <Button
            type="button"
            variant="link"
            size="sm"
            class="text-destructive h-auto px-1"
            @click="handleDelete(row)"
          >
            删除
          </Button>
          <Button
            type="button"
            variant="link"
            size="sm"
            class="h-auto px-1"
            @click="handleClearCache(row)"
          >
            清除缓存
          </Button>
        </div>
      </template>
    </Grid>
    <OptionEditDrawer
      v-model:visible="editDrawerVisible"
      :option-data="editOptionData"
      @success="gridApi.query()"
    />
  </Page>
</template>
