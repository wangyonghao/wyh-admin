import http from '#/api/http';

/* ==================== API 定义 ==================== */
export const captchaApi = {
  /** 获取验证码配置 */
  getConfig: () => {
    return http.get<boolean>(`/captcha/config`);
  },
  /** 获取图片验证码 */
  getImage: () => {
    return http.get<ImageCaptchaResp>(`/captcha/image`);
  },
  /** 获取短信验证码 */
  getSms: (phone: string, captchaReq: BehaviorCaptchaReq) => {
    return http.get<boolean>(
      `/captcha/sms?phone=${phone}&captchaVerification=${encodeURIComponent(captchaReq.captchaVerification || '')}`,
    );
  },
  /** 获取邮箱验证码 */
  getEmail: (email: string, captchaReq: BehaviorCaptchaReq) => {
    return http.get<boolean>(
      `/captcha/email?email=${email}&captchaVerification=${encodeURIComponent(captchaReq.captchaVerification || '')}`,
    );
  },
  /** 获取行为验证码 */
  getBehavior: (req: any) => {
    return http.get<BehaviorCaptchaResp>(`/captcha/behavior`, req);
  },
  /** 校验行为验证码 */
  checkBehavior: (req: any) => {
    return http.post<CheckBehaviorCaptchaResp>(`/captcha/behavior`, req);
  },
};

/* ==================== Schema 定义 ==================== */

/** 图形验证码类型 */
export interface ImageCaptchaResp {
  uuid: string;
  img: string;
  expireTime: number;
  isEnabled: boolean;
}

/* 行为验证码类型 */
export interface BehaviorCaptchaResp {
  originalImageBase64: string;
  point: {
    x: number;
    y: number;
  };
  jigsawImageBase64: string;
  token: string;
  secretKey: string;
  wordList: string[];
}

export interface BehaviorCaptchaReq {
  captchaType?: string;
  captchaVerification?: string;
  clientUid?: string;
}

export interface CheckBehaviorCaptchaResp {
  repCode: string;
  repMsg: string;
}
