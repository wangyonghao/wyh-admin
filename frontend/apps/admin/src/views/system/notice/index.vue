<script setup lang="ts">
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { NoticeDetailResp, NoticeResp } from '#/api/system/notice';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useVbenDrawer } from '#/components/popup/drawer';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { joinDateTimeRange } from '#/components/ui/date-range';
import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { noticeApi } from '#/api/system/notice';
import { useDict } from '#/hooks';
import { Badge } from '#/ui/badge';
import { badgeVariantForDictItem } from '#/ui/badge/variant';
import { Button } from '#/ui/button';
import { toast } from '#/ui-patterns/toast';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';
import { ToolbarActions } from '#/ui-patterns/toolbar-actions';

import NoticeForm from './components/notice-form.vue';
import NoticeView from './components/notice-view.vue';
import { useNoticeColumns, useNoticeSearchSchema } from './data';

const confirmAction = ref<ConfirmActionExpose | null>(null);
const {
  notice_type,
  notice_scope_enum,
  notice_method_enum,
  notice_status_enum,
} = useDict(
  'notice_type',
  'notice_scope_enum',
  'notice_method_enum',
  'notice_status_enum',
);

const currentNoticeId = ref<string>();

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  class: 'w-[1000px]',
  footer: false,
  destroyOnClose: true,
});

const [ViewDrawer, viewDrawerApi] = useVbenDrawer({
  class: 'w-[900px]',
  footer: false,
  title: $t('common.detail'),
});
const currentNoticeDetail = ref<NoticeDetailResp>();

function textValue(value: unknown) {
  return typeof value === 'string' ? value.trim() : '';
}

function noticeQuery(formValues: Record<string, unknown>) {
  const title = textValue(formValues.title);
  const type = textValue(formValues.type);
  const status = textValue(formValues.status);
  return {
    title: title || undefined,
    type: type || undefined,
    status: status || undefined,
    publishTime: joinDateTimeRange(formValues.publishTime),
  };
}

interface DictLike {
  label?: string;
  value?: unknown;
}

function findDict(list: unknown, value: unknown): DictLike | undefined {
  if (!Array.isArray(list)) return undefined;
  return list.find((item) => {
    if (!item || typeof item !== 'object') return false;
    return String((item as DictLike).value) === String(value);
  }) as DictLike | undefined;
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useNoticeSearchSchema({
      typeOptions: () => notice_type?.value ?? [],
      statusOptions: () => notice_status_enum?.value ?? [],
    }),
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
  },
  gridOptions: {
    columns: useNoticeColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await noticeApi.list({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...noticeQuery(formValues ?? {}),
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
  } as VxeTableGridOptions<NoticeResp>,
});

function handleAdd() {
  currentNoticeId.value = undefined;
  formDrawerApi.setState({ title: $t('common.create') }).open();
}

async function handlePreview(row: NoticeResp) {
  try {
    currentNoticeDetail.value = await noticeApi.detail(row.id);
    viewDrawerApi.open();
  } catch (error) {
    console.error('加载公告详情失败:', error);
    toast.error('加载数据失败');
  }
}

function handleEdit(row: NoticeResp) {
  currentNoticeId.value = row.id;
  formDrawerApi.setState({ title: $t('common.edit') }).open();
}

async function handleDelete(row: NoticeResp) {
  const ok = await confirmAction.value?.ask({
    title: '删除确认',
    description: `确定删除公告"${row.title}"吗？`,
    tone: 'destructive',
  });
  if (!ok) return;
  try {
    await noticeApi.delete(row.id);
    toast.success($t('pages.common.deleteSuccess'));
    await gridApi.query();
  } catch (error) {
    console.error('删除公告失败:', error);
    toast.error('删除失败');
  }
}

function handleFormSuccess() {
  formDrawerApi.close();
  gridApi.query();
}

function methodItems(row: NoticeResp) {
  const methods = row.noticeMethods?.split(',') || [];
  return methods
    .map((method) => findDict(notice_method_enum?.value ?? [], method))
    .filter((item) => item != null);
}
</script>

<template>
  <Page auto-content-height>
    <ConfirmAction ref="confirmAction" />
    <Grid>
      <template #toolbar-tools>
        <ToolbarActions>
          <Button
            type="button"
            @click="handleAdd"
          >
            <IconifyIcon
              icon="lucide:plus"
              class="mr-1 size-4"
            />
            {{ $t('pages.common.add') }}
          </Button>
        </ToolbarActions>
      </template>
      <template #type="{ row }">
        <Badge
          :variant="badgeVariantForDictItem(findDict(notice_type, row.type))"
        >
          {{ findDict(notice_type, row.type)?.label || row.type }}
        </Badge>
      </template>
      <template #scope="{ row }">
        <Badge
          :variant="
            badgeVariantForDictItem(
              findDict(notice_scope_enum, row.noticeScope),
            )
          "
        >
          {{
            findDict(notice_scope_enum, row.noticeScope)?.label ||
              row.noticeScope
          }}
        </Badge>
      </template>
      <template #methods="{ row }">
        <div class="flex flex-wrap items-center justify-center gap-1">
          <Badge
            v-for="item in methodItems(row)"
            :key="String(item.value)"
            :variant="badgeVariantForDictItem(item)"
          >
            {{ item.label }}
          </Badge>
        </div>
      </template>
      <template #timing="{ row }">
        <Badge :variant="row.isTiming === 'true' ? 'success' : 'secondary'">
          {{ row.isTiming === 'true' ? '是' : '否' }}
        </Badge>
      </template>
      <template #isTop="{ row }">
        <Badge :variant="row.isTop === 'true' ? 'warning' : 'secondary'">
          {{ row.isTop === 'true' ? '是' : '否' }}
        </Badge>
      </template>
      <template #status="{ row }">
        <Badge
          :variant="
            badgeVariantForDictItem(findDict(notice_status_enum, row.status))
          "
        >
          {{ findDict(notice_status_enum, row.status)?.label || row.status }}
        </Badge>
      </template>
      <template #action="{ row }">
        <div class="inline-flex items-center gap-2">
          <Button
            type="button"
            variant="link"
            size="sm"
            class="h-auto px-1"
            @click="handlePreview(row)"
          >
            <IconifyIcon
              icon="lucide:eye"
              class="mr-1 size-3.5"
            />
            预览
          </Button>
          <Button
            type="button"
            variant="link"
            size="sm"
            class="h-auto px-1"
            @click="handleEdit(row)"
          >
            <IconifyIcon
              icon="lucide:pencil"
              class="mr-1 size-3.5"
            />
            编辑
          </Button>
          <Button
            type="button"
            variant="link"
            size="sm"
            class="text-destructive h-auto px-1"
            @click="handleDelete(row)"
          >
            <IconifyIcon
              icon="lucide:trash-2"
              class="mr-1 size-3.5"
            />
            删除
          </Button>
        </div>
      </template>
    </Grid>

    <FormDrawer
      :title="currentNoticeId ? $t('common.edit') : $t('common.create')"
    >
      <NoticeForm
        :notice-id="currentNoticeId"
        @success="handleFormSuccess"
        @cancel="formDrawerApi.close()"
      />
    </FormDrawer>

    <ViewDrawer>
      <NoticeView
        v-if="currentNoticeDetail"
        :notice="currentNoticeDetail"
      />
    </ViewDrawer>
  </Page>
</template>
