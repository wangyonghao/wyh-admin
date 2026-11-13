import { describe, expect, it } from 'vitest';

import {
  commitSelectValue,
  normalizeSelectOptions,
  selectDisplayKey,
} from './select-value';

describe('form radio empty value', () => {
  const options = normalizeSelectOptions([
    { label: '启用', value: 1 },
    { label: '禁用', value: 2 },
    { label: '是', value: true },
    { label: '否', value: false },
  ]);

  it('renders null as no selection and writes the original option', () => {
    expect(selectDisplayKey(null, options)).toBeUndefined();
    expect(selectDisplayKey(undefined, options)).toBeUndefined();
    expect(selectDisplayKey(1, options)).toBe('n:1');
    expect(selectDisplayKey('1', options)).toBeUndefined();
    expect(commitSelectValue('n:1', options)).toBe(1);
    expect(commitSelectValue('b:0', options)).toBe(false);
    expect(commitSelectValue(undefined, options)).toBeNull();
    expect(commitSelectValue('', options)).toBeNull();
  });
});
