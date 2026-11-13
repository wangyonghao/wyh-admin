/**
 * useVbenForm 的空值是 null（见 adapter/form.ts）。
 * 下拉把 null 画成未选择；清除后回写 null，重置才会把字段清掉。
 * 选项原值（数字、布尔、字符串）原样回写。reka Select 只接受字符串，
 * 所以内部用带类型前缀的 key，避免 `1` 和 `"1"` 撞在一起。
 */

export type SelectScalar = boolean | number | string;

export interface NormalizedSelectOption {
  disabled: boolean;
  key: string;
  label: string;
  value: SelectScalar;
}

export function isSelectScalar(value: unknown): value is SelectScalar {
  return (
    typeof value === 'string' ||
    typeof value === 'number' ||
    typeof value === 'boolean'
  );
}

export function selectOptionKey(value: SelectScalar): string {
  if (typeof value === 'string') return `s:${value}`;
  if (typeof value === 'number') return `n:${value}`;
  return `b:${value ? '1' : '0'}`;
}

export function normalizeSelectOptions(raw: unknown): NormalizedSelectOption[] {
  if (!Array.isArray(raw)) return [];
  const options: NormalizedSelectOption[] = [];
  for (const item of raw) {
    if (item == null || typeof item !== 'object') continue;
    const record = item as Record<string, unknown>;
    if (!isSelectScalar(record.value)) continue;
    options.push({
      disabled: record.disabled === true,
      key: selectOptionKey(record.value),
      label: record.label == null ? String(record.value) : String(record.label),
      value: record.value,
    });
  }
  return options;
}

/** 多选画到 Select 上的 key。对不上的项丢掉，空选择是 []。 */
export function selectMultipleKeys(
  value: unknown,
  options: readonly NormalizedSelectOption[],
): string[] {
  if (!Array.isArray(value)) return [];
  const keys: string[] = [];
  for (const item of value) {
    if (!isSelectScalar(item)) continue;
    const key = options.find((option) => option.value === item)?.key;
    if (key) keys.push(key);
  }
  return keys;
}

/** 对不上选项时不把内部 key 画出来。null / 空数组都是未选择。 */
export function selectDisplayKey(
  value: unknown,
  options: readonly NormalizedSelectOption[],
): string | undefined {
  if (!isSelectScalar(value)) return undefined;
  return options.find((option) => option.value === value)?.key;
}

export function commitSelectValue(
  key: unknown,
  options: readonly NormalizedSelectOption[],
): null | SelectScalar {
  if (typeof key !== 'string' || key === '') return null;
  const match = options.find((option) => option.key === key);
  return match ? match.value : null;
}

export function isSelectEmpty(value: unknown): boolean {
  if (value == null) return true;
  if (Array.isArray(value)) return !value.some((item) => isSelectScalar(item));
  return !isSelectScalar(value);
}

export function isOptionSelected(
  value: unknown,
  option: NormalizedSelectOption,
): boolean {
  if (Array.isArray(value)) {
    return value.some((item) => item === option.value);
  }
  return value === option.value;
}

/** 多选清空后也是 null，和单选清除、表单重置一致。 */
export function toggleSelectValues(
  current: unknown,
  option: NormalizedSelectOption,
  checked: boolean,
): null | SelectScalar[] {
  const list = Array.isArray(current) ? current.filter(isSelectScalar) : [];
  const without = list.filter((item) => item !== option.value);
  const next = checked ? [...without, option.value] : without;
  return next.length === 0 ? null : next;
}

export function selectSummary(
  value: unknown,
  options: readonly NormalizedSelectOption[],
): string {
  if (!Array.isArray(value)) return '';
  return value
    .filter(isSelectScalar)
    .map(
      (item) =>
        options.find((option) => option.value === item)?.label ?? String(item),
    )
    .join('、');
}

export function filterSelectOptions(
  options: readonly NormalizedSelectOption[],
  query: string,
): NormalizedSelectOption[] {
  const keyword = query.trim().toLowerCase();
  if (!keyword) return [...options];
  return options.filter((option) => {
    return (
      option.label.toLowerCase().includes(keyword) ||
      String(option.value).toLowerCase().includes(keyword)
    );
  });
}

/**
 * 手写表单的多选字段是数组。适配器清空回写 null，提交前收成数组，避免把 null 传给接口。
 */
export function asSelectList(value: unknown): SelectScalar[] {
  if (!Array.isArray(value)) return [];
  return value.filter(isSelectScalar);
}

/**
 * `tag` 允许输入列表外的字符串。命中已有选项时回写原值（数字、布尔不改成字符串）。
 * 命中项若禁用，不创建同名新值。
 */
export function commitTaggedQuery(
  query: string,
  options: readonly NormalizedSelectOption[],
): null | SelectScalar {
  const text = query.trim();
  if (!text) return null;
  const exact = options.find(
    (option) => option.label === text || String(option.value) === text,
  );
  if (exact) return exact.disabled ? null : exact.value;
  return text;
}
