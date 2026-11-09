/**
 * 写入用户名。首字符为字母，其余为字母、数字或下划线，总长度 4-64。
 * 与 {@code RegexConstants.USERNAME} 一致。
 */
export const USERNAME_WRITE_PATTERN = /^[a-zA-Z][a-zA-Z0-9_]{3,63}$/;

export const USERNAME_WRITE_MESSAGE =
  '用户名长度为 4-64 个字符，支持大小写字母、数字、下划线，以字母开头';

export function acceptsWriteUsername(
  value: string | null | undefined,
): boolean {
  return typeof value === 'string' && USERNAME_WRITE_PATTERN.test(value);
}

/**
 * 登录只要求非空，不套用写入时的 4-64，也不去掉首尾空白。
 * 已有短用户名因此仍可提交。
 */
export function acceptsLoginUsername(
  value: string | null | undefined,
): boolean {
  return typeof value === 'string' && value.length >= 1;
}
