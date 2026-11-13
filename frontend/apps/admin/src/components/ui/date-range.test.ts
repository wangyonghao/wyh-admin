import { describe, expect, it } from 'vitest';

import {
  formatDateTimeParam,
  formatDateTimeRange,
  joinDateTimeRange,
} from './date-range';

describe('form date range', () => {
  const start = Date.parse('2020-01-02T03:04:05.000Z');
  const end = Date.parse('2020-01-03T04:05:06.000Z');

  it('keeps null as an empty range', () => {
    expect(formatDateTimeParam(null)).toBeUndefined();
    expect(formatDateTimeRange(null)).toEqual({});
    expect(joinDateTimeRange(null)).toBeUndefined();
    expect(joinDateTimeRange([])).toBeUndefined();
  });

  it('formats a timestamp pair the same way the old tables did', () => {
    expect(formatDateTimeParam(start)).toBe('2020-01-02 03:04:05');
    expect(formatDateTimeRange([start, end])).toEqual({
      start: '2020-01-02 03:04:05',
      end: '2020-01-03 04:05:06',
    });
    expect(joinDateTimeRange([start, end])).toBe(
      '2020-01-02 03:04:05,2020-01-03 04:05:06',
    );
  });
});
