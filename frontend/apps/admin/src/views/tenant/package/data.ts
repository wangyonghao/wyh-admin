import type { VbenFormSchema } from '#/components/ui-patterns/form';
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';

import { $t } from '@vben/locales';

import { z } from '#/components/ui-patterns/form';

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 2 },
];

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('tenant.package.name'),
    },
    {
      component: 'Select',
      fieldName: 'status',
      label: $t('tenant.package.status'),
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
      title: $t('tenant.package.name'),
      minWidth: 160,
      showOverflow: 'tooltip',
    },
    {
      field: 'sort',
      title: $t('tenant.package.sort'),
      width: 90,
    },
    {
      field: 'description',
      title: $t('tenant.package.description'),
      minWidth: 200,
      showOverflow: 'tooltip',
    },
    {
      field: 'status',
      title: $t('tenant.package.status'),
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

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('tenant.package.name'),
      rules: z.string().min(1, '名称不能为空').max(30, '名称最多 30 个字符'),
    },
    {
      component: 'InputNumber',
      fieldName: 'sort',
      label: $t('tenant.package.sort'),
      defaultValue: 1,
      componentProps: { min: 0, class: 'w-full' },
    },
    {
      component: 'RadioGroup',
      fieldName: 'menuCheckStrictly',
      label: $t('tenant.package.menuCheckStrictly'),
      defaultValue: true,
      componentProps: {
        options: [
          { label: '是', value: true },
          { label: '否', value: false },
        ],
      },
    },
    {
      component: 'Textarea',
      fieldName: 'description',
      label: $t('tenant.package.description'),
      componentProps: { maxlength: 200, rows: 3 },
      rules: z.string().max(200).optional(),
    },
    {
      component: 'RadioGroup',
      fieldName: 'status',
      label: $t('tenant.package.status'),
      defaultValue: 1,
      componentProps: {
        isButton: true,
        options: statusOptions,
      },
    },
    {
      component: 'CheckboxGroup',
      fieldName: 'menuIds',
      label: $t('tenant.package.menuIds'),
      defaultValue: [],
      componentProps: { options: [] },
      rules: z.array(z.union([z.string(), z.number()])).min(1, '请选择菜单'),
    },
  ];
}
