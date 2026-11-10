import { describe, expect, it } from 'vitest';

import {
  passwordMeetsPolicy,
  passwordRuleStates,
  type PasswordPolicyView,
} from './password-rules';

const policy: PasswordPolicyView = {
  minLength: 10,
  maxLength: 32,
  requireLowercase: true,
  requireDigit: true,
  requireSpecial: false,
};

describe('passwordRuleStates', () => {
  it('follows the supplied policy instead of a fixed 8-character rule', () => {
    const short = passwordRuleStates('abc123', policy);
    expect(short.find((rule) => rule.id === 'length')?.met).toBe(false);
    expect(passwordMeetsPolicy('abc1234567', policy)).toBe(true);
    expect(passwordMeetsPolicy('ABC1234567', policy)).toBe(false);
    expect(passwordMeetsPolicy('abcdefghij', policy)).toBe(false);
  });

  it('adds the special-character rule only when the policy asks for it', () => {
    expect(passwordRuleStates('abc', policy).some((rule) => rule.id === 'special')).toBe(
      false,
    );
    const withSpecial = passwordRuleStates('abc123456!', {
      ...policy,
      requireSpecial: true,
    });
    expect(withSpecial.find((rule) => rule.id === 'special')?.met).toBe(true);
  });
});
