import { describe, expect, it } from 'vitest';

import {
  buildDepartmentSubmit,
  departmentTypeOptions,
  departmentTypeValue,
} from './department-form';

const TYPE_20 = '1A-BRANCH-ABCDEFGHIJ';

describe('department type form value', () => {
  it('keeps a non-numeric type that parseInt would truncate', () => {
    expect(TYPE_20).toHaveLength(20);
    expect(departmentTypeValue('1A')).toBe('1A');
    expect(departmentTypeValue(TYPE_20)).toBe(TYPE_20);
    expect(buildDepartmentSubmit({ type: '1A', sort: 2, status: 1 }).type).toBe(
      '1A',
    );
    expect(buildDepartmentSubmit({ type: TYPE_20 }).type).toBe(TYPE_20);
  });

  it('does not invent a numeric type when the field is empty', () => {
    expect(departmentTypeValue(null)).toBe('');
    expect(departmentTypeValue(undefined)).toBe('');
    expect(buildDepartmentSubmit({}).type).toBe('');
  });

  it('stringifies dictionary options without parsing them as numbers', () => {
    expect(
      departmentTypeOptions([
        { label: '分公司', value: '1' },
        { label: '专项组', value: '1A' },
        { label: '二十字', value: TYPE_20 },
      ]),
    ).toEqual([
      { label: '分公司', value: '1' },
      { label: '专项组', value: '1A' },
      { label: '二十字', value: TYPE_20 },
    ]);
  });
});
