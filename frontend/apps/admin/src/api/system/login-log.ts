import type { PageQuery, PageResult } from '#/types/api';

import http from '#/api/http';

/* ==================== API 定义 ==================== */
export const loginLogApi = {
  /** 分页查询登录日志列表 */
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

/* ==================== Schema 定义 ==================== */

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
