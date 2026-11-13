import { describe, expect, it } from 'vitest';

import {
  textCountLabel,
  textMaxLength,
  textRows,
  textShowsCount,
  textUsesPair,
  textUsesPassword,
  textUsesTextarea,
} from './text-mode';

describe('form text field mode', () => {
  it('detects pair inputs separately from textarea', () => {
    expect(textUsesPair({ pair: true })).toBe(true);
    expect(textUsesPair({ pair: false })).toBe(false);
    expect(textUsesPair({})).toBe(false);
    expect(textUsesTextarea({ type: 'textarea' })).toBe(true);
    expect(textUsesTextarea({ textarea: true })).toBe(true);
    expect(textUsesTextarea({ autosize: { minRows: 3 } })).toBe(true);
    expect(textUsesTextarea({ type: 'text' })).toBe(false);
  });

  it('treats password and character count as shadcn input extras', () => {
    expect(textUsesPassword({ type: 'password' })).toBe(true);
    expect(textUsesPassword({ showPasswordOn: 'click' })).toBe(true);
    expect(textShowsCount({ showCount: true })).toBe(true);
    expect(textShowsCount({ showWordLimit: true })).toBe(true);
    expect(textShowsCount({})).toBe(false);
    expect(textMaxLength({ maxLength: 200 })).toBe(200);
    expect(textMaxLength({ maxlength: '20' })).toBe(20);
    expect(textRows({ rows: 3 })).toBe(3);
    expect(textRows({ autosize: { minRows: 2 } })).toBe(2);
    expect(textCountLabel('tide', 10)).toBe('4/10');
    expect(textCountLabel('tide')).toBe('4');
  });
});
