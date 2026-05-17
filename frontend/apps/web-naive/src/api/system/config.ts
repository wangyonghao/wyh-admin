import http from '#/api/http';

/* ==================== 类型定义 ==================== */

/** 站点配置 */
export interface SiteConfig {
  siteName: string;
  siteLogo: string;
  siteCopyright: string;
  siteIcp: string;
}

/** 登录配置 */
export interface LoginConfig {
  captchaEnabled: boolean;
  captchaType: string;
  maxRetry: number;
  lockTime: number;
}

/** 注册配置 */
export interface RegisterConfig {
  enabled: boolean;
  verifyEmail: boolean;
  verifyPhone: boolean;
  defaultRoleId: string;
}

/** 邮件配置 */
export interface MailConfig {
  host: string;
  port: number;
  username: string;
  password: string;
  from: string;
  sslEnabled?: boolean;
}

/** 短信配置 */
export interface SmsConfig {
  provider: string;
  accessKey: string;
  secretKey: string;
  signName: string;
}

/** 存储配置 */
export interface StorageConfig {
  type: string;
  endpoint: string;
  accessKey: string;
  secretKey: string;
  bucket: string;
}

/** 安全配置 */
export interface SecurityConfig {
  passwordMinLength: number;
  passwordRequireUppercase: boolean;
  passwordRequireLowercase: boolean;
  passwordRequireNumber: boolean;
  passwordRequireSpecial: boolean;
  sessionTimeout: number;
}

/* ==================== API 定义 ==================== */

export const configApi = {
  // 站点配置
  getSiteConfig: () => {
    return http.get<SiteConfig>('/system/config/site');
  },
  updateSiteConfig: (data: SiteConfig) => {
    return http.put('/system/config/site', data);
  },

  // 登录配置
  getLoginConfig: () => {
    return http.get<LoginConfig>('/system/config/login');
  },
  updateLoginConfig: (data: LoginConfig) => {
    return http.put('/system/config/login', data);
  },

  // 注册配置
  getRegisterConfig: () => {
    return http.get<RegisterConfig>('/system/config/register');
  },
  updateRegisterConfig: (data: RegisterConfig) => {
    return http.put('/system/config/register', data);
  },

  // 邮件配置
  getEmailConfig: () => {
    return http.get<MailConfig>('/system/config/mail');
  },
  updateEmailConfig: (data: MailConfig) => {
    return http.put('/system/config/mail', data);
  },
  sendTestEmail: () => {
    return http.post('/system/config/mail/test');
  },

  // 短信配置
  getSmsConfig: () => {
    return http.get<SmsConfig>('/system/config/sms');
  },
  updateSmsConfig: (data: SmsConfig) => {
    return http.put('/system/config/sms', data);
  },

  // 存储配置
  getStorageConfig: () => {
    return http.get<StorageConfig>('/system/config/storage');
  },
  updateStorageConfig: (data: StorageConfig) => {
    return http.put('/system/config/storage', data);
  },

  // 安全配置
  getSecurityConfig: () => {
    return http.get<SecurityConfig>('/system/config/security');
  },
  updateSecurityConfig: (data: SecurityConfig) => {
    return http.put('/system/config/security', data);
  },
};
