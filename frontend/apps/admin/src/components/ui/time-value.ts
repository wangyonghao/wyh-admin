/**
 * useVbenForm 的空值是 null（见 adapter/form.ts）。
 * 时间把 null 画成未选；清空回写 null。0 仍是时间戳。
 * 没有 valueFormat 时提交当天日期上的时间戳；有 valueFormat 时提交格式化字符串。
 * 控件是原生 time 输入，只覆盖时分秒。12 小时制、禁用时刻不做。
 */

export interface TimePart {
  hour: number;
  minute: number;
  second: number;
}

export function isTimeEmpty(value: unknown): boolean {
  if (value == null || value === '') return true;
  if (typeof value === 'number') return !Number.isFinite(value);
  return typeof value !== 'string';
}

export function isRealTime(part: TimePart): boolean {
  return (
    part.hour >= 0 &&
    part.hour <= 23 &&
    part.minute >= 0 &&
    part.minute <= 59 &&
    part.second >= 0 &&
    part.second <= 59
  );
}

export function includesSeconds(
  format?: string,
  valueFormat?: string,
): boolean {
  const chosen =
    typeof format === 'string' && format.trim()
      ? format
      : typeof valueFormat === 'string' && valueFormat.trim()
        ? valueFormat
        : 'HH:mm:ss';
  return chosen.includes('s');
}

export function nativeTimePattern(
  format?: string,
  valueFormat?: string,
): 'HH:mm' | 'HH:mm:ss' {
  return includesSeconds(format, valueFormat) ? 'HH:mm:ss' : 'HH:mm';
}

export function timeInputStep(format?: string, valueFormat?: string): number {
  return includesSeconds(format, valueFormat) ? 1 : 60;
}

function escapeRegExp(text: string): string {
  return text.replaceAll(/[.*+?^${}()|[\]\\]/g, String.raw`\$&`);
}

export function parseTimeString(
  value: string,
  pattern: string,
): TimePart | null {
  let source = '^';
  const tokens: Array<'H' | 'm' | 's'> = [];
  let index = 0;
  while (index < pattern.length) {
    const two = pattern.slice(index, index + 2);
    if (two === 'HH' || two === 'mm' || two === 'ss') {
      tokens.push(two === 'HH' ? 'H' : two === 'mm' ? 'm' : 's');
      source += String.raw`(\d{2})`;
      index += 2;
      continue;
    }
    const one = pattern[index] ?? '';
    if (one === 'H' || one === 'm' || one === 's') {
      tokens.push(one === 'H' ? 'H' : one === 'm' ? 'm' : 's');
      source += String.raw`(\d{1,2})`;
      index += 1;
      continue;
    }
    source += escapeRegExp(one);
    index += 1;
  }
  source += '$';
  const match = new RegExp(source).exec(value.trim());
  if (!match) return null;
  const part: TimePart = { hour: 0, minute: 0, second: 0 };
  tokens.forEach((token, tokenIndex) => {
    const parsed = Number(match[tokenIndex + 1]);
    if (token === 'H') part.hour = parsed;
    if (token === 'm') part.minute = parsed;
    if (token === 's') part.second = parsed;
  });
  return isRealTime(part) ? part : null;
}

export function formatTimePart(part: TimePart, pattern: string): string {
  return pattern.replaceAll(/HH|H|mm|ss|m|s/g, (token) => {
    const number = token.startsWith('H')
      ? part.hour
      : token.startsWith('m')
        ? part.minute
        : part.second;
    return token.length === 2
      ? String(number).padStart(2, '0')
      : String(number);
  });
}

function timePartFrom(
  value: unknown,
  format?: string,
  valueFormat?: string,
): TimePart | null {
  if (typeof value === 'number' && Number.isFinite(value)) {
    const date = new Date(value);
    return {
      hour: date.getHours(),
      minute: date.getMinutes(),
      second: date.getSeconds(),
    };
  }
  if (typeof value === 'string') {
    const pattern =
      (typeof valueFormat === 'string' && valueFormat.trim()) ||
      (typeof format === 'string' && format.trim()) ||
      'HH:mm:ss';
    return parseTimeString(value, pattern);
  }
  return null;
}

/** 原生 time 输入只接受 HH:mm 或 HH:mm:ss。null 显示为空字符串。 */
export function displayTimeInput(
  value: unknown,
  format?: string,
  valueFormat?: string,
): string {
  if (isTimeEmpty(value)) return '';
  const part = timePartFrom(value, format, valueFormat);
  if (!part) return '';
  return formatTimePart(part, nativeTimePattern(format, valueFormat));
}

export function commitTimeValue(
  input: string,
  options: { base?: number; format?: string; valueFormat?: string } = {},
): null | number | string {
  if (input.trim() === '') return null;
  const part = parseTimeString(
    input,
    nativeTimePattern(options.format, options.valueFormat),
  );
  if (!part) return null;
  if (typeof options.valueFormat === 'string' && options.valueFormat.trim()) {
    return formatTimePart(part, options.valueFormat);
  }
  const date = new Date(
    typeof options.base === 'number' && Number.isFinite(options.base)
      ? options.base
      : Date.now(),
  );
  date.setHours(part.hour, part.minute, part.second, 0);
  return date.getTime();
}
