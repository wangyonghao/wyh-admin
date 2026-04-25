import { baseRequestClient, requestClient as http } from '#/api/request';

/* ==================== API 定义 ==================== */
export const authApi = {
  login(params: AuthReq) {
    return http.post<LoginResult>('/auth/login', params);
  },
  /** 账号登录 */
  accountLogin(req: AccountLoginReq) {
    return http.post<LoginResult>('/auth/login', req);
  },
  /** 手机号登录 */
  phoneLogin(req: PhoneLoginReq) {
    return http.post<LoginResult>('/auth/login', req);
  },
  /** 邮箱登录 */
  emailLogin(req: EmailLoginReq) {
    return http.post<LoginResult>('/auth/login', req);
  },
  /** 三方账号登录 */
  socialLogin(req: any) {
    return http.post<LoginResult>('/auth/login', req);
  },
  /** 三方账号登录授权 */
  socialAuth(source: string) {
    return http.get<SocialAuthAuthorizeResp>(`/auth/${source}`);
  },
  /** 退出登录 */
  logout() {
    return baseRequestClient.post('/auth/logout');
  },
  /** 获取用户简介、菜单、权限信息 */
  getAuthInfo() {
    return http.get<AuthInfo>('/auth/info');
  },
  /** 强制修改密码（密码过期时使用） */
  forceChangePassword(req: ForceChangePasswordReq) {
    return http.post<ForceChangePasswordResp>(
      '/auth/force-change-password',
      req,
    );
  },
};

/* ==================== 常量定义 ==================== */

/** 认证类型 */
export const AuthTypeConstants = {
  ACCOUNT: 'ACCOUNT',
  PHONE: 'PHONE',
  EMAIL: 'EMAIL',
  SOCIAL: 'SOCIAL',
} as const;

/* ==================== Schema 定义 ==================== */

/** 认证信息 */
export interface AuthInfo {
  user: UserProfile;
  roles: string[];
  permissions: string[];
  menus: RouteItem[];
}

/** 用户类型 */
export interface UserProfile {
  userId: string;
  username: string;
  nickname: string;
  gender: 0 | 1 | 2;
  email: string;
  phone: string;
  avatar: string;
  pwdUpdateTime: string;
  pwdExpireDate: string;
  pwdExpired: boolean;
  registrationDate: string;
  deptName: string;
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

/** 基础认证请求接口 */
export interface AuthReq {
  clientId?: string;
  authType?: 'ACCOUNT' | 'EMAIL' | 'PHONE' | 'SOCIAL';
}

/** 账号登录请求参数 */
export interface AccountLoginReq extends AuthReq {
  username: string;
  password: string;
  captcha: string;
  uuid: string;
}

/** 手机号登录请求参数 */
export interface PhoneLoginReq extends AuthReq {
  phone: string;
  captcha: string;
}

/** 邮箱登录请求参数 */
export interface EmailLoginReq extends AuthReq {
  email: string;
  captcha: string;
}

/** 登录接口返回值 */
export interface LoginResult {
  token: string;
  code?: string; // PASSWORD_EXPIRED 表示密码过期
  userId?: string; // 用户ID（密码过期时返回）
  tempToken?: string; // 临时令牌（密码过期时返回）
}

/** 第三方登录授权类型 */
export interface SocialAuthAuthorizeResp {
  authorizeUrl: string;
}

/** 强制修改密码请求参数 */
export interface ForceChangePasswordReq {
  userId: string;
  tempToken: string;
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/** 强制修改密码响应类型 */
export interface ForceChangePasswordResp {
  token: string;
}
