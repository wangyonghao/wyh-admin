import type { VbenFormSchema } from '#/components/ui-patterns/form';
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';

import { $t } from '@vben/locales';

export function useNoticeSearchSchema(source: {
  statusOptions: () => unknown;
  typeOptions: () => unknown;
}): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.notice.title'),
    },
    {
      component: 'Select',
      fieldName: 'type',
      label: $t('system.notice.type'),
      componentProps: () => ({
        clearable: true,
        options: source.typeOptions(),
      }),
    },
    {
      component: 'Select',
      fieldName: 'status',
      label: $t('system.notice.status'),
      componentProps: () => ({
        clearable: true,
        options: source.statusOptions(),
      }),
    },
    {
      component: 'DatePicker',
      fieldName: 'publishTime',
      label: $t('system.notice.publishTime'),
      componentProps: {
        type: 'datetimerange',
        clearable: true,
        format: 'yyyy-MM-dd HH:mm:ss',
      },
    },
  ];
}

export function useNoticeColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'seq', width: 70, fixed: 'left' },
    {
      field: 'title',
      title: $t('system.notice.title'),
      minWidth: 200,
      align: 'left',
    },
    {
      field: 'createUserString',
      title: $t('system.notice.createUser'),
      minWidth: 120,
    },
    {
      field: 'type',
      title: $t('system.notice.type'),
      minWidth: 100,
      slots: { default: 'type' },
    },
    {
      field: 'noticeScope',
      title: $t('system.notice.noticeScope'),
      minWidth: 120,
      slots: { default: 'scope' },
    },
    {
      field: 'noticeMethods',
      title: $t('system.notice.noticeMethods'),
      minWidth: 160,
      slots: { default: 'methods' },
    },
    {
      field: 'isTiming',
      title: $t('system.notice.isTiming'),
      minWidth: 100,
      slots: { default: 'timing' },
    },
    {
      field: 'isTop',
      title: $t('system.notice.isTop'),
      minWidth: 100,
      slots: { default: 'isTop' },
    },
    {
      field: 'status',
      title: $t('system.notice.status'),
      minWidth: 100,
      slots: { default: 'status' },
    },
    {
      field: 'publishTime',
      title: $t('system.notice.publishTime'),
      minWidth: 160,
    },
    {
      field: 'action',
      title: $t('common.operation'),
      width: 220,
      fixed: 'right',
      slots: { default: 'action' },
    },
  ];
}
