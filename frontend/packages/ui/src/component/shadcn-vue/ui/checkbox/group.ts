export type CheckScalar = boolean | number | string;

export interface CheckOption {
  disabled: boolean;
  key: string;
  label: string;
  value: CheckScalar;
}

export function isCheckScalar(value: unknown): value is CheckScalar {
  return (
    typeof value === 'string' ||
    typeof value === 'number' ||
    typeof value === 'boolean'
  );
}

export function normalizeCheckOptions(raw: unknown): CheckOption[] {
  if (!Array.isArray(raw)) return [];
  const options: CheckOption[] = [];
  for (const item of raw) {
    if (item == null || typeof item !== 'object') continue;
    const record = item as Record<string, unknown>;
    if (!isCheckScalar(record.value)) continue;
    options.push({
      disabled: record.disabled === true,
      key: `${typeof record.value}:${String(record.value)}`,
      label:
        record.label == null ? String(record.value) : String(record.label),
      value: record.value,
    });
  }
  return options;
}

export function isValueChecked(current: unknown, value: CheckScalar): boolean {
  return Array.isArray(current) && current.some((item) => item === value);
}

/**
 * 取消最后一项时回写空数组，和 NCheckboxGroup 一样。
 * null 只由 useVbenForm 重置写入，显示为全不选。
 */
export function toggleCheckedValue<T extends CheckScalar>(
  current: readonly T[] | null | undefined,
  value: T,
  checked: boolean,
): T[] {
  const list = Array.isArray(current) ? [...current] : [];
  const without = list.filter((item) => item !== value);
  return checked ? [...without, value] : without;
}
