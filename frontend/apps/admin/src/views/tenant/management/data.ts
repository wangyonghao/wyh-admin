import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { $t } from '@vben/locales';

import { z } from '#/adapter/form';
import {
  USERNAME_WRITE_MESSAGE,
  USERNAME_WRITE_PATTERN,
} from '#/utils/username';

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 2 },
];

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('tenant.management.name'),
    },
    {
      component: 'Select',
      fieldName: 'status',
      label: $t('tenant.management.status'),
      componentProps: {
        clearable: true,
        options: statusOptions,
      },
    },
  ];
}

export function useColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'seq', width: 70, fixed: 'left' },
    {
      field: 'name',
      title: $t('tenant.management.name'),
      minWidth: 140,
      showOverflow: 'tooltip',
    },
    {
      field: 'code',
      title: $t('tenant.management.code'),
      minWidth: 140,
      showOverflow: 'tooltip',
    },
    {
      field: 'domain',
      title: $t('tenant.management.domain'),
      minWidth: 160,
      showOverflow: 'tooltip',
    },
    {
      field: 'packageName',
      title: $t('tenant.management.packageId'),
      minWidth: 140,
      showOverflow: 'tooltip',
    },
    {
      field: 'adminUsername',
      title: $t('tenant.management.adminUsername'),
      minWidth: 140,
    },
    {
      field: 'expireTime',
      title: $t('tenant.management.expireTime'),
      minWidth: 170,
    },
    {
      field: 'status',
      title: $t('tenant.management.status'),
      width: 100,
      cellRender: {
        name: 'CellTag',
        attrs: { options: statusOptions },
      },
    },
    {
      field: 'action',
      title: $t('common.operation'),
      width: 140,
      fixed: 'right',
      slots: { default: 'action' },
    },
  ];
}

export function useFormSchema(creating: () => boolean): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('tenant.management.name'),
      rules: z.string().min(1, '名称不能为空').max(30, '名称最多 30 个字符'),
    },
    {
      component: 'Input',
      fieldName: 'domain',
      label: $t('tenant.management.domain'),
      componentProps: { placeholder: 'example.com' },
      rules: z.string().max(255).optional(),
    },
    {
      component: 'DatePicker',
      fieldName: 'expireTime',
      label: $t('tenant.management.expireTime'),
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        class: 'w-full',
      },
    },
    {
      component: 'Textarea',
      fieldName: 'description',
      label: $t('tenant.management.description'),
      componentProps: { maxlength: 200, rows: 3 },
      rules: z.string().max(200).optional(),
    },
    {
      component: 'Select',
      fieldName: 'packageId',
      label: $t('tenant.management.packageId'),
      componentProps: {
        options: [],
        filterable: true,
      },
      rules: 'selectRequired',
    },
    {
      component: 'RadioGroup',
      fieldName: 'status',
      label: $t('tenant.management.status'),
      defaultValue: 1,
      componentProps: {
        isButton: true,
        options: statusOptions,
      },
    },
    {
      component: 'Input',
      fieldName: 'adminUsername',
      label: $t('tenant.management.adminUsername'),
      rules: z.string().regex(USERNAME_WRITE_PATTERN, USERNAME_WRITE_MESSAGE),
      dependencies: {
        if: () => creating(),
        triggerFields: ['name'],
      },
    },
    {
      component: 'Input',
      fieldName: 'adminPassword',
      label: $t('tenant.management.adminPassword'),
      componentProps: { type: 'password' },
      rules: z.string().min(1, '管理员密码不能为空'),
      dependencies: {
        if: () => creating(),
        triggerFields: ['name'],
      },
    },
  ];
}
