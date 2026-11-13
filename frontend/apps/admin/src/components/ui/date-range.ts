/**
 * 手写日期范围原先把时间戳交给 `toISOString` 再去掉时区字母。
 * 表单里的 DatePicker 清空后是 null，这里当成没有范围。
 */

export function formatDateTimeParam(value: unknown): string | undefined {
  if (value == null || value === '') return undefined;
  const date =
    value instanceof Date ? value : new Date(value as number | string);
  if (Number.isNaN(date.getTime())) return undefined;
  return date.toISOString().slice(0, 19).replace('T', ' ');
}

export function formatDateTimeRange(value: unknown): {
  end?: string;
  start?: string;
} {
  if (!Array.isArray(value) || value.length < 2) return {};
  return {
    start: formatDateTimeParam(value[0]),
    end: formatDateTimeParam(value[1]),
  };
}

export function joinDateTimeRange(value: unknown): string | undefined {
  const { start, end } = formatDateTimeRange(value);
  if (!start || !end) return undefined;
  return `${start},${end}`;
}
