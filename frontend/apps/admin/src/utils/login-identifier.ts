/** 与后端邮箱列 varchar(64) 对齐的登录/注册邮箱判断。 */
export const LOGIN_EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const EMAIL_MAX_LENGTH = 64;

export function isLoginEmail(value: string | null | undefined): boolean {
  const trimmed = value?.trim() ?? '';
  return trimmed.length > 0 && trimmed.length <= EMAIL_MAX_LENGTH && LOGIN_EMAIL_PATTERN.test(trimmed);
}

/**
 * 密码登录已支持 email 字段。标识符是邮箱时只传 email，避免被用户名查找截走。
 * 其它值仍作为用户名原样提交，短用户名因此可以登录。
 */
export function toPasswordLoginFields(identifier: string): {
  username?: string;
  email?: string;
} {
  if (isLoginEmail(identifier)) {
    return { email: identifier.trim() };
  }
  return { username: identifier };
}
