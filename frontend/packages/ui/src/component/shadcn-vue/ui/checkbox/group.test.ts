import { describe, expect, it } from 'vitest';

import {
  isValueChecked,
  normalizeCheckOptions,
  toggleCheckedValue,
} from './group';

describe('checkbox group values', () => {
  it('treats null as unchecked and keeps an empty array when the last item is cleared', () => {
    expect(isValueChecked(null, 'A')).toBe(false);
    expect(isValueChecked(undefined, 1)).toBe(false);
    expect(toggleCheckedValue(null, 'A', true)).toEqual(['A']);
    expect(toggleCheckedValue(['A', 'C'], 'A', false)).toEqual(['C']);
    expect(toggleCheckedValue<string>([], 'A', false)).toEqual([]);
    expect(toggleCheckedValue([1, 2], 1, false)).toEqual([2]);
    expect(toggleCheckedValue([1], 1, false)).toEqual([]);
  });

  it('does not coerce numeric option values', () => {
    const options = normalizeCheckOptions([
      { label: '周一', value: 1 },
      { label: '字符', value: '1' },
      { label: '缺值' },
    ]);
    expect(options.map((option) => option.value)).toEqual([1, '1']);
    expect(isValueChecked([1], '1')).toBe(false);
    expect(isValueChecked([1], 1)).toBe(true);
  });
});
