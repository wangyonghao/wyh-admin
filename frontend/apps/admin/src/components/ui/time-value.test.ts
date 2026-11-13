import { describe, expect, it } from 'vitest';

import {
  commitTimeValue,
  displayTimeInput,
  formatTimePart,
  includesSeconds,
  isTimeEmpty,
  parseTimeString,
} from './time-value';

describe('form time empty value', () => {
  it('treats null as empty and keeps timestamp 0', () => {
    expect(isTimeEmpty(null)).toBe(true);
    expect(isTimeEmpty(undefined)).toBe(true);
    expect(isTimeEmpty('')).toBe(true);
    expect(isTimeEmpty(0)).toBe(false);
    expect(displayTimeInput(null, 'HH:mm')).toBe('');
    expect(displayTimeInput(undefined)).toBe('');
  });

  it('parses and formats hour, minute, and second', () => {
    expect(parseTimeString('09:05', 'HH:mm')).toEqual({
      hour: 9,
      minute: 5,
      second: 0,
    });
    expect(parseTimeString('9:5:3', 'H:m:s')).toEqual({
      hour: 9,
      minute: 5,
      second: 3,
    });
    expect(parseTimeString('24:00', 'HH:mm')).toBeNull();
    expect(formatTimePart({ hour: 9, minute: 5, second: 3 }, 'HH:mm:ss')).toBe(
      '09:05:03',
    );
  });

  it('writes null when cleared, a string when valueFormat is set, and a timestamp otherwise', () => {
    expect(commitTimeValue('', { format: 'HH:mm' })).toBeNull();
    expect(commitTimeValue('   ', { format: 'HH:mm' })).toBeNull();
    expect(commitTimeValue('nope', { format: 'HH:mm' })).toBeNull();
    expect(
      commitTimeValue('09:05', { format: 'HH:mm', valueFormat: 'HH:mm' }),
    ).toBe('09:05');
    expect(
      commitTimeValue('09:05:03', {
        format: 'HH:mm:ss',
        valueFormat: 'H:m:s',
      }),
    ).toBe('9:5:3');

    const base = new Date(2020, 0, 2, 1, 2, 3).getTime();
    const next = commitTimeValue('09:05', { format: 'HH:mm', base });
    expect(typeof next).toBe('number');
    const date = new Date(next as number);
    expect(date.getFullYear()).toBe(2020);
    expect(date.getMonth()).toBe(0);
    expect(date.getDate()).toBe(2);
    expect(date.getHours()).toBe(9);
    expect(date.getMinutes()).toBe(5);
    expect(date.getSeconds()).toBe(0);
  });

  it('shows a timestamp on the native input and hides seconds when the format has none', () => {
    const stamp = new Date(2024, 4, 6, 8, 7, 5).getTime();
    expect(displayTimeInput(stamp, 'HH:mm:ss')).toBe('08:07:05');
    expect(displayTimeInput(stamp, 'HH:mm')).toBe('08:07');
    expect(displayTimeInput('09:05', 'HH:mm', 'HH:mm')).toBe('09:05');
    expect(includesSeconds('HH:mm')).toBe(false);
    expect(includesSeconds()).toBe(true);
    expect(isTimeEmpty(0)).toBe(false);
    expect(displayTimeInput(0, 'HH:mm:ss')).not.toBe('');
  });
});
