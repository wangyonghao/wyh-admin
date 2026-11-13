import { describe, expect, it } from 'vitest';

import {
  asSelectList,
  commitSelectValue,
  commitTaggedQuery,
  filterSelectOptions,
  isSelectEmpty,
  normalizeSelectOptions,
  selectDisplayKey,
  selectMultipleKeys,
  selectSummary,
  toggleSelectValues,
} from './select-value';

const options = normalizeSelectOptions([
  { label: '已读', value: true },
  { label: '未读', value: false },
  { label: '已停止', value: 0 },
  { label: '已激活', value: 1 },
  { label: '字符一', value: '1' },
  { label: '停用项', value: 'off', disabled: true },
  { label: '缺值' },
  null,
]);

describe('form select empty value', () => {
  it('renders null as unselected and keeps 0 / false selected', () => {
    expect(selectDisplayKey(null, options)).toBeUndefined();
    expect(selectDisplayKey(undefined, options)).toBeUndefined();
    expect(isSelectEmpty(null)).toBe(true);
    expect(isSelectEmpty(undefined)).toBe(true);
    expect(isSelectEmpty([])).toBe(true);
    expect(selectDisplayKey(0, options)).toBe('n:0');
    expect(selectDisplayKey(false, options)).toBe('b:0');
    expect(isSelectEmpty(0)).toBe(false);
    expect(isSelectEmpty(false)).toBe(false);
  });

  it('writes null when cleared and returns the original option value', () => {
    expect(commitSelectValue(undefined, options)).toBeNull();
    expect(commitSelectValue('', options)).toBeNull();
    expect(commitSelectValue('missing', options)).toBeNull();
    expect(commitSelectValue('n:0', options)).toBe(0);
    expect(commitSelectValue('n:1', options)).toBe(1);
    expect(commitSelectValue('b:1', options)).toBe(true);
    expect(commitSelectValue('b:0', options)).toBe(false);
    expect(commitSelectValue('s:1', options)).toBe('1');
  });

  it('does not treat string 1 as number 1', () => {
    expect(selectDisplayKey(1, options)).toBe('n:1');
    expect(selectDisplayKey('1', options)).toBe('s:1');
    expect(selectDisplayKey(1, options)).not.toBe(
      selectDisplayKey('1', options),
    );
  });

  it('clears a multiple selection back to null', () => {
    const stopped = options.find((option) => option.value === 0)!;
    const active = options.find((option) => option.value === 1)!;
    expect(toggleSelectValues(null, stopped, true)).toEqual([0]);
    expect(toggleSelectValues([0], active, true)).toEqual([0, 1]);
    expect(toggleSelectValues([0, 1], stopped, false)).toEqual([1]);
    expect(toggleSelectValues([0], stopped, false)).toBeNull();
    expect(selectSummary([false, 1], options)).toBe('未读、已激活');
    expect(selectSummary(null, options)).toBe('');
    expect(selectMultipleKeys(null, options)).toEqual([]);
    expect(selectMultipleKeys([0, '1', 'missing'], options)).toEqual([
      'n:0',
      's:1',
    ]);
  });

  it('filters by label or value and drops options without a scalar value', () => {
    expect(filterSelectOptions(options, '  已读 ')).toEqual([
      expect.objectContaining({ value: true }),
    ]);
    expect(
      filterSelectOptions(options, 'OFF').map((option) => option.value),
    ).toEqual(['off']);
    expect(filterSelectOptions(options, '   ')).toHaveLength(options.length);
    expect(options.map((option) => option.value)).toEqual([
      true,
      false,
      0,
      1,
      '1',
      'off',
    ]);
    expect(options.find((option) => option.value === 'off')?.disabled).toBe(
      true,
    );
  });

  it('drops a null option so an explicit 全部 entry is not a real value', () => {
    const withAll = normalizeSelectOptions([
      { label: '全部', value: null },
      { label: '成功', value: 'SUCCESS' },
    ]);
    expect(withAll.map((option) => option.value)).toEqual(['SUCCESS']);
    expect(selectDisplayKey(null, withAll)).toBeUndefined();
  });

  it('turns a cleared multiple value into an empty list for page forms', () => {
    expect(asSelectList(null)).toEqual([]);
    expect(asSelectList(undefined)).toEqual([]);
    expect(asSelectList('1')).toEqual([]);
    expect(asSelectList([0, false, '1', null, { id: 1 }])).toEqual([
      0,
      false,
      '1',
    ]);
  });

  it('commits a typed tag as text, or the original option when it matches', () => {
    expect(commitTaggedQuery('  ', options)).toBeNull();
    expect(commitTaggedQuery('  已停止 ', options)).toBe(0);
    expect(commitTaggedQuery('1', options)).toBe(1);
    expect(commitTaggedQuery('停用项', options)).toBeNull();
    expect(commitTaggedQuery('LocalDate', options)).toBe('LocalDate');
  });
});
