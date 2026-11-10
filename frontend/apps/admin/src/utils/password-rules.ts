/**
 * 与后端 EffectivePasswordRules 一致的注册密码规则。
 * 小写字母和数字来自密码模板；大写字母开关不单独生效。
 */
export interface PasswordPolicyView {
  minLength: number;
  maxLength: number;
  requireLowercase: boolean;
  requireDigit: boolean;
  requireSpecial: boolean;
}

export interface PasswordRuleState {
  id: string;
  label: string;
  met: boolean;
}

const SPECIAL =
  /[-_`~!@#$%^&*()+=|{}':;',[\].<>/?！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\n\r\t]/;

export function passwordRuleStates(
  password: string,
  policy: PasswordPolicyView,
): PasswordRuleState[] {
  const value = password ?? '';
  const rules: PasswordRuleState[] = [
    {
      id: 'length',
      label: `${policy.minLength}–${policy.maxLength} 个字符`,
      met:
        value.length >= policy.minLength && value.length <= policy.maxLength,
    },
  ];
  if (policy.requireLowercase) {
    rules.push({
      id: 'lower',
      label: '包含小写字母',
      met: /[a-z]/.test(value),
    });
  }
  if (policy.requireDigit) {
    rules.push({
      id: 'digit',
      label: '包含数字',
      met: /\d/.test(value),
    });
  }
  if (policy.requireSpecial) {
    rules.push({
      id: 'special',
      label: '包含特殊字符',
      met: SPECIAL.test(value),
    });
  }
  return rules;
}

export function passwordMeetsPolicy(
  password: string,
  policy: PasswordPolicyView,
): boolean {
  return passwordRuleStates(password, policy).every((rule) => rule.met);
}
