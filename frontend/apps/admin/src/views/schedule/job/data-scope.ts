import type { VbenFormSchema } from '#/components/ui-patterns/form';
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';

export function useGridSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '任务名称',
      component: 'Input',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [
          { label: '已停止', value: 0 },
          { label: '已激活', value: 1 },
        ],
      },
    },
  ];
}

export function useGridFieldColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'seq', width: 70, fixed: 'left' },
    { field: 'name', title: '任务名称', minWidth: 140 },
    { field: 'handlerName', title: '执行任务', minWidth: 120 },
    { field: 'scheduleLabel', title: '执行时间', minWidth: 180, slots: { default: 'scheduleLabel' } },
    { field: 'nextFireTime', title: '下次执行', minWidth: 170 },
    { field: 'status', title: '状态', width: 100, align: 'center', slots: { default: 'status' } },
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
