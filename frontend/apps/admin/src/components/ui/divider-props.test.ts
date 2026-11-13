import { describe, expect, it } from 'vitest';

import { dividerClass, dividerOrientation } from './divider-props';

describe('form divider', () => {
  it('stays horizontal unless vertical is set', () => {
    expect(dividerOrientation(undefined)).toBe('horizontal');
    expect(dividerOrientation(false)).toBe('horizontal');
    expect(dividerOrientation(true)).toBe('vertical');
    expect(dividerClass(false, false)).toBeUndefined();
  });

  it('uses a dashed border when dashed is set', () => {
    expect(dividerClass(false, true)).toContain('border-dashed');
    expect(dividerClass(false, true)).toContain('border-t');
    expect(dividerClass(true, true)).toContain('border-l');
  });
});
