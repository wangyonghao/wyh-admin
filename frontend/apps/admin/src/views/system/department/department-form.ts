import type { DepartmentWrite } from '#/api/system/department';

export interface DepartmentFormValues {
  code?: string;
  description?: string;
  name?: string;
  parentId?: string | null;
  sort?: unknown;
  status?: unknown;
  type?: unknown;
}

/** 部门类型按字符串原样提交，不做数字解析。 */
export function departmentTypeValue(value: unknown): string {
  if (value == null) return '';
  return String(value);
}

export function departmentTypeOptions(
  list: unknown,
): { label: string; value: string }[] {
  if (!Array.isArray(list)) return [];
  return list.flatMap((item) => {
    if (!item || typeof item !== 'object') return [];
    const record = item as { label?: unknown; value?: unknown };
    if (record.value == null || record.value === '') return [];
    const value = departmentTypeValue(record.value);
    return [
      {
        label: record.label == null ? value : String(record.label),
        value,
      },
    ];
  });
}

export function buildDepartmentSubmit(
  values: DepartmentFormValues,
  id = '',
): DepartmentWrite {
  return {
    id,
    parentId: values.parentId,
    code: values.code,
    name: values.name,
    type: departmentTypeValue(values.type),
    sort: typeof values.sort === 'number' ? values.sort : 1,
    description: values.description ?? '',
    status: values.status === 2 ? 2 : 1,
  };
}
