import http from '#/utils/request';

/* ==================== API 定义 ==================== */
export const userProfileApi = {
  detail: () => {
    return http.get(`/user/profile`);
  },
  /** 上传头像 */
  uploadAvatar: (data: FormData) => {
    return http.patch(`/user/profile/avatar`, data);
  },
  /** 修改用户基本信息 */
  updateBaseInfo: (data: { gender: number; displayName: string }) => {
    return http.patch(`/user/profile/basic/info`, data);
  },
  /** 修改密码 */
  updatePassword: (data: {
    newPassword: string;
    oldPassword: string;
  }) => {
    return http.patch(`/user/profile/password`, data);
  },
  /** 修改手机号 */
  updatePhone: (data: {
    captcha: string;
    oldPassword: string;
    phone: string;
  }) => {
    return http.patch(`/user/profile/phone`, data);
  },
  /** 修改邮箱 */
  updateEmail: (data: {
    captcha: string;
    email: string;
    oldPassword: string;
  }) => {
    return http.patch(`/user/profile/email`, data);
  },
  /** 获取绑定的三方账号 */
  listSocial: () => {
    return http.get<BindSocialAccountRes[]>(`/user/profile/social`);
  },
  /** 绑定三方账号 */
  bindSocial: (source: string, data: any) => {
    return http.post(`/user/profile/social/${source}`, data);
  },
  /** 解绑三方账号 */
  unbindSocial: (source: string) => {
    return http.delete(`/user/profile/social/${source}`);
  },
};

/* ==================== Schema 定义 ==================== */

/** 绑定三方账号信息 */
export interface BindSocialAccountRes {
  source: string;
  description: string;
}
