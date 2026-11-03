import type { AxiosRequestConfig } from 'axios';

import http from '#/api/http';
import type { Menu } from './system';
import type { PageQuery, PageResult } from '#/types/api';

/* ==================== API 定义 ==================== */
function postLogin(grantType: AuthGrantType, body: Omit<AuthReq, 'grantType'>, config?: AxiosRequestConfig) {
  return http.post<LoginResult>('/auth/login', body, {
    ...config,
    params: {
      ...(config?.params ?? {}),
      grantType,
    },
  });
}

export const authApi = {
  login(params: AuthReq, config?: AxiosRequestConfig) {
    const { grantType = AuthTypeConstants.PASSWORD, ...body } = params;
    return postLogin(grantType, body, config);
  },
  /** 自助注册（成功后返回登录 token） */
  register(req: RegisterReq) {
    return http.post<LoginResult>('/auth/register', req);
  },
  /** 密码登录：用户名、邮箱或手机号 */
  passwordLogin(req: PasswordLoginReq, config?: AxiosRequestConfig) {
    const { grantType = AuthTypeConstants.PASSWORD, ...body } = req;
    return postLogin(grantType, body, config);
  },
  /** 验证码登录：邮箱或手机号 */
  otpLogin(req: OtpLoginReq, config?: AxiosRequestConfig) {
    const { grantType = AuthTypeConstants.OTP, ...body } = req;
    return postLogin(grantType, body, config);
  },
  /** 三方账号登录 */
  socialLogin(req: AuthReq & Record<string, any>, config?: AxiosRequestConfig) {
    const { grantType = AuthTypeConstants.OAUTH, ...body } = req;
    return postLogin(grantType, body, config);
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
  /** 分页查询登录日志列表（System /monitor/login-log） */
  listLoginLog: (query: LoginLogPageQuery) => {
    return http.get<PageResult<LoginLogResult[]>>('/monitor/login-log', {
      params: query,
    });
  },
  /** 导出登录日志 */
  exportLoginLog: (query: LoginLogQuery) => {
    return http.download('/monitor/login-log/export', { params: query });
  },
};

/* ==================== 常量定义 ==================== */

/** 认证类型（对齐后端 GrantType：PASSWORD / OTP / OAUTH） */
export const AuthTypeConstants = {
  PASSWORD: 'PASSWORD',
  OTP: 'OTP',
  OAUTH: 'OAUTH',
} as const;

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

/* ==================== 登录日志 Schema 定义 ==================== */

/** 登录日志响应 */
export interface LoginLogResult {
  /** 主键ID */
  id: string;
  /** 用户名 */
  username: string;
  /** IP地址 */
  ipAddress: string;
  /** 地理位置 */
  location: string;
  /** 设备类型 */
  deviceType: string;
  /** 浏览器 */
  browser: string;
  /** 操作系统 */
  os: string;
  /** 登录状态 */
  loginStatus: 'SUCCESS' | 'FAILURE';
  /** 登录时间 */
  loginTime: string;
  /** 失败原因 */
  failureReason?: string;
  /** 租户ID */
  tenantId?: string;
}

/** 登录日志查询条件 */
export interface LoginLogQuery {
  /** 用户名（模糊查询） */
  username?: string;
  /** IP地址（模糊查询） */
  ipAddress?: string;
  /** 登录状态 */
  loginStatus?: 'SUCCESS' | 'FAILURE';
  /** 登录开始时间 */
  loginTimeStart?: string;
  /** 登录结束时间 */
  loginTimeEnd?: string;
  /** 租户ID */
  tenantId?: string;
}

/** 登录日志分页查询 */
export interface LoginLogPageQuery extends LoginLogQuery, PageQuery {}
