import type { VbenFormSchema } from '#/components/ui-patterns/form';
import type { VxeTableGridOptions } from '#/components/ui-patterns/vxe-table';
import type { UserResp } from '#/api/system/user';

export function useUserSearchSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'keyword',
      label: '关键字',
      componentProps: {
        clearable: true,
        placeholder: '搜索关键字（用户名/显示名称）',
      },
    },
  ];
}

export function useUserColumns(): VxeTableGridOptions<UserResp>['columns'] {
  return [
    { type: 'seq', width: 60, fixed: 'left' },
    {
      field: 'displayName',
      title: '显示名称',
      minWidth: 100,
      fixed: 'left',
      align: 'left',
    },
    {
      field: 'username',
      title: '用户名',
      minWidth: 100,
      align: 'left',
    },
    {
      field: 'departmentName',
      title: '部门',
      minWidth: 100,
      slots: { default: 'departmentName' },
    },
    {
      field: 'roleNames',
      title: '角色',
      width: 120,
      slots: { default: 'roleNames' },
    },
    {
      field: 'phone',
      title: '手机号',
      width: 120,
      slots: { default: 'phone' },
    },
    {
      field: 'status',
      title: '状态',
      width: 80,
      slots: { default: 'status' },
    },
    {
      field: 'action',
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'action' },
    },
  ];
}

export function userKeyword(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}

export function userStatus(status: number): {
  label: string;
  type: 'error' | 'info' | 'success' | 'warning';
} {
  const statusMap: Record<
    number,
    { label: string; type: 'error' | 'info' | 'success' | 'warning' }
  > = {
    0: { type: 'error', label: '禁用' },
    1: { type: 'success', label: '启用' },
    2: { type: 'warning', label: '待审核' },
    3: { type: 'error', label: '审核拒绝' },
  };
  return statusMap[status] ?? { type: 'info', label: '未知' };
}

export function displayCell(value: unknown): string {
  if (Array.isArray(value)) {
    const text = value.filter(Boolean).join('、');
    return text || '-';
  }
  if (value == null || value === '') return '-';
  return String(value);
}
