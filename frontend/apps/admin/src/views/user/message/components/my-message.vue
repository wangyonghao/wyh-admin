<script setup lang="ts">
import type { VbenFormSchema } from '@vben/common-ui';

import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { MessageResp } from '#/api/system/user-message';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useVbenModal } from '#/components/popup/modal';
import { $t } from '@vben/locales';


import { Badge } from '#/ui/badge';

import { useVbenVxeGrid } from '#/components/ui-patterns/vxe-table';
import { userMessageApi } from '#/api/system/user-message';
import { useDict } from '#/hooks';
import { Button } from '#/ui/button';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';
import { ToolbarActions } from '#/ui-patterns/toolbar-actions';

import MyMessageDetailModal from './my-message-detail-modal.vue';
import { toast } from '#/ui-patterns/toast';

defineOptions({ name: 'UserMyMessage' });

const { message_type_enum } = useDict('message_type_enum');
const confirmAction = ref<ConfirmActionExpose | null>(null);

function useTenantGridSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'title',
      label: $t('system.msg.search.title'),
      component: 'Input',
    },
    {
      fieldName: 'type',
      label: $t('system.msg.search.type'),
      component: 'Select',
      componentProps: {
        options: message_type_enum,
      },
    },
    {
      fieldName: 'isRead',
      label: $t('system.msg.search.isRead'),
      component: 'Select',
      componentProps: {
        options: [
          { label: '已读', value: true },
          { label: '未读', value: false },
        ],
      },
    },
  ];
}

function useTenantGridFieldColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 50, fixed: 'left' },
    { type: 'seq', width: 70, fixed: 'left' },
    {
      field: 'title',
      title: $t('system.msg.title'),
      align: 'center',
      fixed: 'left',
      slots: { default: 'title' },
    },
    {
      field: 'type',
      title: $t('system.msg.type'),
      slots: { default: 'type' },
      align: 'center',
    },
    {
      field: 'isRead',
      title: $t('system.msg.isRead'),
      slots: { default: 'isRead' },
      align: 'center',
    },
    {
      field: 'createTime',
      title: $t('system.msg.createTime'),
      align: 'center',
      width: 180,
    },
  ];
}

const [TableGrid, tableGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useTenantGridSearchFormSchema(),
    submitOnChange: true,
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
  },
  gridOptions: {
    columns: useTenantGridFieldColumns(),
    border: true,
    height: 'auto',
    keepSource: true,
    columnConfig: {
      resizable: true,
    },
    proxyConfig: {
      response: {
        list: 'records',
      },
      ajax: {
        query: async ({ page }, formValues) => {
          const res = await userMessageApi.list({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
          return res;
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    checkboxConfig: {
      highlight: true,
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      refreshOptions: {
        code: 'query',
      },
      search: true,
      zoom: true,
      zoomOptions: {},
    },
  } as VxeTableGridOptions<MessageResp>,
});

// const router = useRouter();
// 查看
const onView = (record: MessageResp) => {
  // router.push({ path: '/user/notice', query: { id: record.id } });
  formModalApi.setData(record).open();
};

// 获取选中的记录
const getCheckBoxRecordIds = () => {
  const checkBoxRecords = tableGridApi.grid.getCheckboxRecords(false);
  if (checkBoxRecords.length > 0) {
    return checkBoxRecords.map((item) => item.id);
  }
  return [];
};

// 删除
const onDelete = () => {
  const selectedKeys = getCheckBoxRecordIds();
  if (selectedKeys.length === 0) {
    return toast.warning('请选择数据');
  }
  userMessageApi.delete(selectedKeys);
  tableGridApi.reload();
};

// 标记为已读
const onRead = async () => {
  const selectedKeys = getCheckBoxRecordIds();
  if (selectedKeys.length === 0) {
    return toast.warning('请选择数据');
  }
  await userMessageApi.read(selectedKeys);
  toast.success('操作成功');
  tableGridApi.reload();
};

// 全部已读事件
const onReadAll = async () => {
  const ok = await confirmAction.value?.ask({
    title: $t('common.tips'),
    description: $t('system.msg.tips.markAllReadTips'),
    confirmText: $t('common.confirm'),
    cancelText: $t('common.cancel'),
  });
  if (!ok) return;
  await readAll();
};

// 全部已读
const readAll = async () => {
  await userMessageApi.readAll();
  toast.success('操作成功');
  tableGridApi.reload();
};

const [DetailModal, formModalApi] = useVbenModal({
  connectedComponent: MyMessageDetailModal,
  destroyOnClose: true,
});

// 表格更新回调
const onDetailModalClose = () => {
  // mittBus.emit('count-refresh');
  tableGridApi.reload();
};
</script>

<template>
  <Page auto-content-height>
    <ConfirmAction ref="confirmAction" />
    <TableGrid>
      <template #toolbar-tools>
        <ToolbarActions>
          <Button type="button" variant="destructive" @click="onDelete">
            {{ $t('pages.common.delete') }}
          </Button>
          <Button type="button" @click="onRead">
            {{ $t('system.msg.markRead') }}
          </Button>
          <Button type="button" @click="onReadAll">
            {{ $t('system.msg.markAllRead') }}
          </Button>
        </ToolbarActions>
      </template>

      <template #title="{ row }">
        <Button
          type="button"
          variant="link"
          class="h-auto px-0"
          @click="onView(row)"
        >
          {{ row.title }}
        </Button>
      </template>

      <template #type="{ row }">
        <DictTag :value="row.type" :dict-list="message_type_enum as []" />
      </template>

      <template #isRead="{ row }">
        <Badge :variant="row.isRead ? 'secondary' : 'default'">
          {{ row.isRead ? '已读' : '未读' }}
        </Badge>
      </template>
    </TableGrid>
    <DetailModal @close="onDetailModalClose" />
  </Page>
</template>

<style scoped lang="scss"></style>
