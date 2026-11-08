import { describe, expect, it } from 'vitest';

import {
  badgeVariantForDictItem,
  badgeVariantForTag,
  dictTagColor,
} from './variant';

describe('dictionary tag colors', () => {
  it('maps the named naive tag colors onto badge variants', () => {
    expect(badgeVariantForTag('success')).toBe('success');
    expect(badgeVariantForTag('warning')).toBe('warning');
    expect(badgeVariantForTag('error')).toBe('destructive');
    expect(badgeVariantForTag('default')).toBe('secondary');
    expect(badgeVariantForTag('primary')).toBe('default');
    expect(badgeVariantForTag('info')).toBe('default');
    expect(badgeVariantForTag('purple')).toBeNull();
    expect(badgeVariantForTag(null)).toBeNull();
  });

  it('reads color from dict extra and falls back when it is missing', () => {
    expect(dictTagColor({ extra: { color: 'success' } })).toBe('success');
    expect(dictTagColor({ extra: '{"color":"warning"}' })).toBe('warning');
    expect(
      dictTagColor({ tagType: 'error', extra: { color: 'success' } }),
    ).toBe('error');
    expect(badgeVariantForDictItem({ extra: { color: 'primary' } })).toBe(
      'default',
    );
    expect(badgeVariantForDictItem({ label: '无色' })).toBe('secondary');
    expect(badgeVariantForDictItem({ extra: { color: 'purple' } })).toBe(
      'secondary',
    );
  });
});
