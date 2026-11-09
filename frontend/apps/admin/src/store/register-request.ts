/**
 * 自助注册页提交值。confirmPassword 只在页面校验，不进入请求体。
 */
export interface RegisterSubmit {
  username: string;
  password: string;
  confirmPassword?: string;
}

/**
 * POST /auth/register 的请求体，对齐后端 RegisterRequest。
 */
export interface RegisterRequestBody {
  username: string;
  password: string;
}

/**
 * 组装自助注册请求体。
 * password 原样提交，传输安全依赖 HTTPS；服务端按密码策略校验后做慢哈希存储。
 */
export function toRegisterRequest(input: RegisterSubmit): RegisterRequestBody {
  return {
    username: input.username,
    password: input.password,
  };
}
