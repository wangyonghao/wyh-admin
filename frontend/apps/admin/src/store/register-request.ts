/**
 * 自助注册页提交值。密码规则在页面上对照服务端策略校验。
 */
export interface RegisterSubmit {
  email: string;
  password: string;
}

/**
 * POST /auth/register 的请求体。不包含用户名。
 */
export interface RegisterRequestBody {
  email: string;
  password: string;
}

/**
 * 组装自助注册请求体。
 * password 原样提交，传输安全依赖 HTTPS。邮箱去掉首尾空白，不改大小写。
 */
export function toRegisterRequest(input: RegisterSubmit): RegisterRequestBody {
  return {
    email: input.email.trim(),
    password: input.password,
  };
}
