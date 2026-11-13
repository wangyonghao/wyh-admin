import type { VbenFormSchema } from '#/components/ui-patterns/form';
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';

export function useGridSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'jobId',
      label: '任务 ID',
      component: 'Input',
    },
    {
      fieldName: 'status',
      label: '结果',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [
          { label: '运行中', value: 1 },
          { label: '成功', value: 2 },
          { label: '失败', value: 3 },
        ],
      },
    },
  ];
}

export function useGridFieldColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'seq', width: 70, fixed: 'left' },
    { field: 'jobName', title: '任务名称', minWidth: 140 },
    { field: 'handlerCode', title: '任务编码', minWidth: 120 },
    { field: 'triggerType', title: '触发方式', width: 100, slots: { default: 'triggerType' } },
    { field: 'startTime', title: '开始时间', minWidth: 170 },
    { field: 'endTime', title: '结束时间', minWidth: 170 },
    { field: 'durationMs', title: '耗时(ms)', width: 100 },
    { field: 'status', title: '结果', width: 90, slots: { default: 'status' } },
    { field: 'errorMessage', title: '错误', minWidth: 180 },
  ];
}
