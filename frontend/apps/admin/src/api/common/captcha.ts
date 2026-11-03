import http from '#/api/http';

/* ==================== API 定义 ==================== */
export const captchaApi = {
  /** 获取图片验证码（Base64，含是否启用） */
  getImage: () => {
    return http.get<ImageCaptchaResp>(`/captcha`);
  },
};

/* ==================== Schema 定义 ==================== */

/** 图形验证码类型（对齐 CaptchaResponse） */
export interface ImageCaptchaResp {
  uuid: string;
  img: string;
  expireTime: number;
  isEnabled: boolean;
}
