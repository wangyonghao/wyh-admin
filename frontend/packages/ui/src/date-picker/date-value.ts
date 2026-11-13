/**
 * useVbenForm 的空值是 null（见 `#/components/ui-patterns/form`）。
 * 日期把 null 画成未选；清除、未选完的范围都回写 null。
 * 没有 valueFormat 时提交时间戳（页面筛选就是这样读的）。
 * 有 valueFormat 时提交格式化字符串，供 LocalDateTime（yyyy-MM-dd HH:mm:ss）。
 * 套件里没有 Calendar 原子，月历放在 @wyh/ui/date-picker，周/月/季/快捷范围不做。
 */

export type DatePickerKind = 'date' | 'datetime' | 'daterange' | 'datetimerange';

export interface FormDatePickerProps {
  clearable?: boolean;
  disabled?: boolean;
  format?: string;
  placeholder?: string;
  showTime?: boolean;
  type?: DatePickerKind;
  valueFormat?: string;
}

export interface DatePart {
  day: number;
  hour: number;
  minute: number;
  month: number;
  second: number;
  year: number;
}

export interface CalendarCell {
  day: number;
  month: number;
  outside: boolean;
  year: number;
}

const TOKEN = /YYYY|yyyy|MM|DD|dd|HH|mm|ss/g;

export function resolveDateKind(
  type: unknown,
  showTime: unknown,
): DatePickerKind {
  const named: DatePickerKind =
    type === 'datetime' ||
    type === 'daterange' ||
    type === 'datetimerange' ||
    type === 'date'
      ? type
      : 'date';
  if (showTime === true && named === 'date') return 'datetime';
  if (showTime === true && named === 'daterange') return 'datetimerange';
  return named;
}

export function isRangeKind(kind: DatePickerKind): boolean {
  return kind === 'daterange' || kind === 'datetimerange';
}

export function hasTimeKind(kind: DatePickerKind): boolean {
  return kind === 'datetime' || kind === 'datetimerange';
}

export function defaultDatePattern(kind: DatePickerKind): string {
  return hasTimeKind(kind) ? 'yyyy-MM-dd HH:mm:ss' : 'yyyy-MM-dd';
}

export function isDateEmpty(value: unknown): boolean {
  if (value == null || value === '') return true;
  if (Array.isArray(value)) {
    if (value.length < 2) return true;
    return isDateEmpty(value[0]) || isDateEmpty(value[1]);
  }
  if (typeof value === 'number') return !Number.isFinite(value);
  return typeof value !== 'string';
}

export function startOfDay(part: DatePart): DatePart {
  return { ...part, hour: 0, minute: 0, second: 0 };
}

export function isRealDate(part: DatePart): boolean {
  if (
    part.month < 1 ||
    part.month > 12 ||
    part.day < 1 ||
    part.hour < 0 ||
    part.hour > 23 ||
    part.minute < 0 ||
    part.minute > 59 ||
    part.second < 0 ||
    part.second > 59
  ) {
    return false;
  }
  const date = new Date(
    part.year,
    part.month - 1,
    part.day,
    part.hour,
    part.minute,
    part.second,
  );
  return (
    date.getFullYear() === part.year &&
    date.getMonth() === part.month - 1 &&
    date.getDate() === part.day
  );
}

export function partToTimestamp(part: DatePart): number {
  return new Date(
    part.year,
    part.month - 1,
    part.day,
    part.hour,
    part.minute,
    part.second,
  ).getTime();
}

export function timestampToPart(value: number): DatePart | null {
  if (!Number.isFinite(value)) return null;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;
  return {
    year: date.getFullYear(),
    month: date.getMonth() + 1,
    day: date.getDate(),
    hour: date.getHours(),
    minute: date.getMinutes(),
    second: date.getSeconds(),
  };
}

export function formatDatePart(part: DatePart, pattern: string): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return pattern.replace(TOKEN, (token) => {
    switch (token) {
      case 'DD':
      case 'dd': {
        return pad(part.day);
      }
      case 'HH': {
        return pad(part.hour);
      }
      case 'MM': {
        return pad(part.month);
      }
      case 'YYYY':
      case 'yyyy': {
        return String(part.year).padStart(4, '0');
      }
      case 'mm': {
        return pad(part.minute);
      }
      case 'ss': {
        return pad(part.second);
      }
      default: {
        return token;
      }
    }
  });
}

function escapeRegExp(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export function parseByPattern(text: string, pattern: string): DatePart | null {
  const names: string[] = [];
  let regex = '^';
  let last = 0;
  for (const match of pattern.matchAll(TOKEN)) {
    const index = match.index ?? 0;
    regex += escapeRegExp(pattern.slice(last, index));
    regex += '(\\d{1,4})';
    names.push(match[0]);
    last = index + match[0].length;
  }
  if (names.length === 0) return null;
  regex += `${escapeRegExp(pattern.slice(last))}$`;
  const found = new RegExp(regex).exec(text.trim());
  if (!found) return null;
  const part: DatePart = {
    year: 1970,
    month: 1,
    day: 1,
    hour: 0,
    minute: 0,
    second: 0,
  };
  for (const [index, token] of names.entries()) {
    const num = Number(found[index + 1]);
    if (!Number.isFinite(num)) return null;
    switch (token) {
      case 'DD':
      case 'dd': {
        part.day = num;
        break;
      }
      case 'HH': {
        part.hour = num;
        break;
      }
      case 'MM': {
        part.month = num;
        break;
      }
      case 'YYYY':
      case 'yyyy': {
        part.year = num;
        break;
      }
      case 'mm': {
        part.minute = num;
        break;
      }
      case 'ss': {
        part.second = num;
        break;
      }
      default: {
        break;
      }
    }
  }
  return isRealDate(part) ? part : null;
}

export function parseDateValue(
  value: unknown,
  valueFormat?: string,
): DatePart | null {
  if (typeof value === 'number') return timestampToPart(value);
  if (typeof value !== 'string' || value.trim() === '') return null;
  if (valueFormat) {
    const parsed = parseByPattern(value, valueFormat);
    if (parsed) return parsed;
  }
  const stamp = Number(value);
  if (value.trim() !== '' && Number.isFinite(stamp) && /^-?\d+$/.test(value.trim())) {
    return timestampToPart(stamp);
  }
  return (
    parseByPattern(value, 'yyyy-MM-dd HH:mm:ss') ??
    parseByPattern(value, 'yyyy-MM-dd')
  );
}

export function sameDay(a: DatePart, b: DatePart): boolean {
  return a.year === b.year && a.month === b.month && a.day === b.day;
}

export function compareDay(a: DatePart, b: DatePart): number {
  if (a.year !== b.year) return a.year - b.year;
  if (a.month !== b.month) return a.month - b.month;
  return a.day - b.day;
}

export function commitDateSelection(
  kind: DatePickerKind,
  start: DatePart | null,
  end: DatePart | null,
  valueFormat?: string,
):
  | null
  | number
  | string
  | [number, number]
  | [string, string] {
  if (!start || !isRealDate(start)) return null;
  const normalizedStart = hasTimeKind(kind) ? start : startOfDay(start);
  if (!isRangeKind(kind)) {
    return valueFormat
      ? formatDatePart(normalizedStart, valueFormat)
      : partToTimestamp(normalizedStart);
  }
  if (!end || !isRealDate(end)) return null;
  let normalizedEnd = hasTimeKind(kind) ? end : startOfDay(end);
  let rangeStart = normalizedStart;
  if (compareDay(normalizedEnd, rangeStart) < 0) {
    const swap = rangeStart;
    rangeStart = normalizedEnd;
    normalizedEnd = swap;
  }
  if (valueFormat) {
    return [
      formatDatePart(rangeStart, valueFormat),
      formatDatePart(normalizedEnd, valueFormat),
    ];
  }
  return [partToTimestamp(rangeStart), partToTimestamp(normalizedEnd)];
}

export function displayDateLabel(
  value: unknown,
  kind: DatePickerKind,
  format?: string,
  valueFormat?: string,
): string {
  if (isDateEmpty(value)) return '';
  const pattern = format || defaultDatePattern(kind);
  if (Array.isArray(value)) {
    const start = parseDateValue(value[0], valueFormat);
    const end = parseDateValue(value[1], valueFormat);
    if (!start || !end) return '';
    return `${formatDatePart(start, pattern)} ~ ${formatDatePart(end, pattern)}`;
  }
  const part = parseDateValue(value, valueFormat);
  return part ? formatDatePart(part, pattern) : '';
}

export function readDateRange(
  value: unknown,
  valueFormat?: string,
): { end: DatePart | null; start: DatePart | null } {
  if (!Array.isArray(value)) {
    return { start: parseDateValue(value, valueFormat), end: null };
  }
  return {
    start: parseDateValue(value[0], valueFormat),
    end: parseDateValue(value[1], valueFormat),
  };
}

/** 周一为一周的第一天。2024-05-01 是周三，前面补 4 月 29、30 日。 */
export function buildMonthCells(year: number, month: number): CalendarCell[] {
  const first = new Date(year, month - 1, 1);
  const offset = (first.getDay() + 6) % 7;
  const daysInMonth = new Date(year, month, 0).getDate();
  const cells: CalendarCell[] = [];
  const prevDays = new Date(year, month - 1, 0).getDate();
  const prev = shiftMonth(year, month, -1);
  for (let index = 0; index < offset; index += 1) {
    cells.push({
      day: prevDays - offset + 1 + index,
      month: prev.month,
      outside: true,
      year: prev.year,
    });
  }
  for (let day = 1; day <= daysInMonth; day += 1) {
    cells.push({ day, month, outside: false, year });
  }
  const next = shiftMonth(year, month, 1);
  let nextDay = 1;
  while (cells.length % 7 !== 0) {
    cells.push({
      day: nextDay,
      month: next.month,
      outside: true,
      year: next.year,
    });
    nextDay += 1;
  }
  return cells;
}

export function shiftMonth(
  year: number,
  month: number,
  delta: number,
): { month: number; year: number } {
  const index = year * 12 + (month - 1) + delta;
  return { year: Math.floor(index / 12), month: (index % 12) + 1 };
}

export function timeText(part: DatePart | null): string {
  if (!part) return '00:00:00';
  return formatDatePart(part, 'HH:mm:ss');
}

export function applyTime(part: DatePart, text: string): DatePart | null {
  const match = /^(\d{2}):(\d{2})(?::(\d{2}))?$/.exec(text);
  if (!match) return null;
  const next: DatePart = {
    ...part,
    hour: Number(match[1]),
    minute: Number(match[2]),
    second: match[3] ? Number(match[3]) : 0,
  };
  return isRealDate(next) ? next : null;
}
