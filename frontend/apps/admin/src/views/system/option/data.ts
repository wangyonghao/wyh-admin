import type { VbenFormSchema } from '#/components/ui-patterns/form';
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';

export function useGridSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'keyword',
      label: '关键词',
      componentProps: {
        placeholder: '搜索选项类型/选项值/显示名称',
      },
    },
  ];
}

export function useGridFieldColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'seq', width: 70, fixed: 'left' },
    { field: 'optionType', title: '选项类型', minWidth: 150, fixed: 'left' },
    { field: 'value', title: '选项值', minWidth: 120 },
    { field: 'label', title: '显示名称', minWidth: 150 },
    {
      field: 'enabled',
      title: '状态',
      width: 100,
      align: 'center',
      slots: { default: 'enabled' },
    },
    { field: 'sort', title: '排序', width: 100 },
    {
      field: 'description',
      title: '描述',
      minWidth: 200,
      formatter: ({ cellValue }) => cellValue || '-',
    },
    {
      field: 'action',
      title: '操作',
      width: 220,
      fixed: 'right',
      align: 'center',
      slots: { default: 'action' },
    },
  ];
}
