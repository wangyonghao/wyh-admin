<script setup lang="ts">
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { DepartmentResult } from '#/api/system/department';

import { nextTick, ref, watch } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { useDebounceFn } from '@vueuse/core';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { departmentApi } from '#/api/system/department';
import { useDownload } from '#/hooks/app/useDownload';
import { useUserStore } from '#/store/user';
import { Badge } from '#/ui/badge';
import { Button } from '#/ui/button';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';
import { FilterInput } from '#/ui-patterns/filter-input';
import { ToolbarActions } from '#/ui-patterns/toolbar-actions';

import EditModal from './department-drawer.vue';
import { toast } from '#/ui-patterns/toast';

const keyword = ref('');
const confirmAction = ref<ConfirmActionExpose | null>(null);
const userStore = useUserStore();
const expanded = ref(true);
const editModalVisible = ref(false);
const editModalData = ref<DepartmentResult | undefined>(undefined);

function presentTree(nodes: DepartmentResult[]): DepartmentResult[] {
  return nodes.map((node) => ({
    ...node,
    children: node.children?.length ? presentTree(node.children) : undefined,
  })) as DepartmentResult[];
}

const [Grid, gridApi] = useVbenVxeGrid({
  showSearchForm: false,
  gridOptions: {
    columns: [
      {
        field: 'name',
        title: $t('system.dept.name'),
        treeNode: true,
        align: 'left',
        minWidth: 240,
      },
      {
        field: 'code',
        title: $t('system.dept.code'),
        align: 'left',
        width: 140,
      },
      {
        field: 'status',
        title: $t('system.dept.status'),
        width: 100,
        slots: { default: 'status' },
      },
      {
        field: 'description',
        title: $t('system.dept.description'),
        align: 'left',
        minWidth: 160,
      },
      {
        field: 'action',
        title: $t('pages.common.operation'),
        width: 120,
        fixed: 'right',
        slots: { default: 'action' },
      },
    ],
    height: 'auto',
    keepSource: true,
    pagerConfig: { enabled: false },
    rowConfig: { keyField: 'id' },
    treeConfig: {
      childrenField: 'children',
      expandAll: true,
      rowField: 'id',
    },
    toolbarConfig: {
      custom: true,
      refresh: true,
      zoom: true,
    },
    proxyConfig: {
      ajax: {
        query: async () => {
          const res = await departmentApi.tree({
            keyword: keyword.value.trim() || undefined,
          });
          const records = presentTree(res ?? []);
          return { records, total: records.length };
        },
        querySuccess: async () => {
          expanded.value = true;
          await nextTick();
          gridApi.grid?.setAllTreeExpand?.(true);
        },
      },
    },
  } as VxeTableGridOptions<DepartmentResult>,
});

const reload = useDebounceFn(() => {
  gridApi.reload();
}, 300);

watch(keyword, () => {
  reload();
});

function toggleExpand() {
  expanded.value = !expanded.value;
  gridApi.grid?.setAllTreeExpand?.(expanded.value);
}

function handleEdit(record: DepartmentResult) {
  editModalData.value = record;
  editModalVisible.value = true;
}

function handleAdd() {
  editModalData.value = undefined;
  editModalVisible.value = true;
}

async function handleDelete(row: DepartmentResult) {
  const ok = await confirmAction.value?.ask({
    title: $t('system.dept.deleteTitle'),
    description: $t('ui.actionMessage.deleteConfirm', [row.name]),
    confirmText: $t('common.confirm'),
    cancelText: $t('common.cancel'),
    tone: 'destructive',
  });
  if (!ok) return;
  try {
    await departmentApi.delete(row.id);
    toast.success($t('pages.common.deleteSuccess'));
    await gridApi.query();
  } catch (error) {
    console.error('Failed to delete dept:', error);
  }
}

function handleExport() {
  useDownload(() =>
    departmentApi.export({ keyword: keyword.value.trim() || undefined }),
  );
}
</script>

<template>
  <Page auto-content-height>
    <ConfirmAction ref="confirmAction" />
    <Grid>
      <template #toolbar-actions>
        <FilterInput
          v-model="keyword"
          class="w-64"
          :placeholder="$t('system.dept.name')"
        />
      </template>
      <template #toolbar-tools>
        <ToolbarActions>
          <span v-access:code="['system:dept:create']">
            <Button
              type="button"
              variant="secondary"
              @click="handleAdd"
            >
              <IconifyIcon
                icon="lucide:plus"
                class="mr-1 size-4"
              />
              {{ $t('pages.common.add') }}
            </Button>
          </span>
          <span v-access:code="['system:dept:export']">
            <Button
              type="button"
              variant="secondary"
              @click="handleExport"
            >
              <IconifyIcon
                icon="lucide:download"
                class="mr-1 size-4"
              />
              {{ $t('pages.common.export') }}
            </Button>
          </span>
          <Button
            type="button"
            variant="secondary"
            @click="toggleExpand"
          >
            <IconifyIcon
              :icon="expanded ? 'lucide:chevrons-up' : 'lucide:chevrons-down'"
              class="mr-1 size-4"
            />
            {{
              expanded ? $t('pages.common.collapse') : $t('pages.common.expand')
            }}
          </Button>
        </ToolbarActions>
      </template>
      <template #status="{ row }">
        <Badge :variant="row.status === 1 ? 'success' : 'destructive'">
          {{ row.status === 1 ? $t('common.enabled') : $t('common.disabled') }}
        </Badge>
      </template>
      <template #action="{ row }">
        <div class="flex items-center justify-center gap-2">
          <Button
            v-if="userStore.hasPermission('system:dept:update')"
            type="button"
            variant="ghost"
            size="icon"
            @click="handleEdit(row)"
          >
            <IconifyIcon
              icon="lucide:pencil"
              class="size-4"
            />
          </Button>
          <Button
            v-if="userStore.hasPermission('system:dept:delete')"
            type="button"
            variant="ghost"
            size="icon"
            class="text-destructive"
            @click="handleDelete(row)"
          >
            <IconifyIcon
              icon="lucide:trash-2"
              class="size-4"
            />
          </Button>
        </div>
      </template>
    </Grid>

    <EditModal
      v-model:visible="editModalVisible"
      :data="editModalData"
      @success="gridApi.query()"
    />
  </Page>
</template>
