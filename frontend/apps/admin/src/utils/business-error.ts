// utils/business-error.ts

/**
 * 业务异常：HTTP 请求成功（2xx），但后端返回的业务码表示失败。
 * 例如：库存不足、用户名已存在、优惠券已过期。
 */
export class BusinessError<T = unknown> extends Error {
  readonly name = 'BusinessError';

  constructor(
    /** 后端业务码 */
    public readonly code: number | string,
    /** 后端返回的提示文案 */
    message: string,
    /** 后端附带的数据（如校验失败的字段详情） */
    public readonly data?: T,
  ) {
    super(message);
    // 保证编译到 ES5 目标时 instanceof 依然有效
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** 类型守卫：用于 catch 里区分业务异常 */
export function isBusinessError<T = unknown>(e: unknown): e is BusinessError<T> {
  return e instanceof BusinessError;
}
