import http from '#/api/http';
import type { Menu } from './system';

/* ==================== API 定义 ==================== */
export const authApi = {
  /** 自助注册（成功后返回登录 token） */
  register(req: RegisterReq) {
    return http.post<LoginResult>('/auth/register', req);
  },
  /** 密码登录：用户名、邮箱或手机号 */
  passwordLogin(req: PasswordLoginReq) {
    const { grantType = 'PASSWORD', ...body } = req;
    return http.post<LoginResult>('/auth/login', body, {
      params: { grantType },
    });
  },
  /** 验证码登录：邮箱或手机号 */
  otpLogin(req: OtpLoginReq) {
    const { grantType = 'OTP', ...body } = req;
    return http.post<LoginResult>('/auth/login', body, {
      params: { grantType },
    });
  },
  /** 三方账号登录 */
  socialLogin(req: AuthReq & Record<string, any>) {
    const { grantType = 'OAUTH', ...body } = req;
    return http.post<LoginResult>('/auth/login', body, {
      params: { grantType },
    });
  },
  /** 三方账号登录授权 */
  socialAuth(source: string) {
    return http.get<SocialAuthAuthorizeResp>(`/oauth/${source}`);
  },
  /** 发送邮箱/短信 OTP */
  sendOtp(req: OtpSendReq) {
    return http.post<OtpSendResp>('/auth/otp', req);
  },
  /** 校验 OTP */
  verifyOtp(req: OtpVerifyReq) {
    return http.post<OtpVerifyResp>('/auth/otp/verify', req);
  },
  /** 退出登录 */
  logout() {
    return http.post('/auth/logout');
  },
  /** 获取用户简介、菜单、权限信息 */
  getAuthInfo() {
    return http.get<AuthInfo>('/auth/user');
  },
};

/* ==================== 常量定义 ==================== */

/** OTP 业务场景（对齐 OtpScene） */
export const OtpSceneConstants = {
  LOGIN: 'LOGIN',
  REGISTER: 'REGISTER',
  BIND_EMAIL: 'BIND_EMAIL',
  BIND_PHONE: 'BIND_PHONE',
  RESET_PASSWORD: 'RESET_PASSWORD',
  CHANGE_EMAIL: 'CHANGE_EMAIL',
  CHANGE_PHONE: 'CHANGE_PHONE',
  DELETE_ACCOUNT: 'DELETE_ACCOUNT',
} as const;

/* ==================== Schema 定义 ==================== */

/** 认证信息 */
export interface AuthInfo {
  user: UserProfile;
  roles: string[];
  permissions: string[];
  menus: Menu[];
}

/** 用户类型 */
export interface UserProfile {
  userId: string;
  username: string;
  displayName: string;
  gender: 0 | 1 | 2;
  email: string;
  phone: string;
  avatar: string | null;
  pwdUpdateTime: string;
  pwdExpireDate: string;
  pwdExpired: boolean;
  registrationDate: string;
  departmentName: string;
  homePath: string;
}

/** 路由类型 */
export interface RouteItem {
  id: string;
  title: string;
  parentId: string;
  type: 1 | 2 | 3 | 4 | 5; // 1目录 2菜单 3按钮 4内嵌 5外链
  path: string;
  name: string;
  component: string;
  redirect: string;
  icon: string;
  isExternal: boolean;
  isHidden: boolean;
  isCache: boolean;
  permission: string;
  roles: string[];
  sort: number;
  status: 0 | 1;
  children: RouteItem[];
  activeMenu: string;
  alwaysShow: boolean;
  breadcrumb: boolean;
  showInTabs: boolean;
  affix: boolean;
}

/** 登录方式（走 QueryString） */
export type AuthGrantType = 'PASSWORD' | 'OTP' | 'OAUTH';

export type OtpScene =
  | 'LOGIN'
  | 'REGISTER'
  | 'BIND_EMAIL'
  | 'BIND_PHONE'
  | 'RESET_PASSWORD'
  | 'CHANGE_EMAIL'
  | 'CHANGE_PHONE'
  | 'DELETE_ACCOUNT';

/** 基础认证请求接口。grantType 由 API 层放到 Query，不会进 Body。 */
export interface AuthReq {
  clientId?: string;
  grantType?: AuthGrantType;
}

/** 自助注册请求（对齐 RegisterRequest：无 confirmPassword） */
export interface RegisterReq {
  username: string;
  password: string;
}

/** 密码登录请求参数。username / email / phone 三选一 */
export interface PasswordLoginReq extends AuthReq {
  username?: string;
  email?: string;
  phone?: string;
  password: string;
  captchaUuid?: string;
  captchaChallenge?: string;
}

/** 验证码登录请求参数。email / phone 二选一 */
export interface OtpLoginReq extends AuthReq {
  email?: string;
  phone?: string;
  codeChallenge: string;
}

/** 发送 OTP */
export interface OtpSendReq {
  scene: OtpScene;
  email?: string;
  phone?: string;
  locale?: string;
}

export interface OtpSendResp {
  expiresIn: number;
  message: string;
  otpUuid: string;
}

/** 校验 OTP */
export interface OtpVerifyReq {
  otpUuid: string;
  email?: string;
  phone?: string;
  code: string;
}

export interface OtpVerifyResp {
  verified: boolean;
  message: string;
}

/** 登录接口返回值 */
export interface LoginResult {
  token: string;
  code?: string;
}

/** 第三方登录授权类型 */
export interface SocialAuthAuthorizeResp {
  authorizeUrl: string;
}
