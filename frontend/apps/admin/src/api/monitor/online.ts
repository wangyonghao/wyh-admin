import type { PageQuery, PageResult } from '#/types/api';
import http from '#/utils/request';

/* ==================== API 定义 ==================== */
export const onlineApi = {
  /** 查询在线用户列表 */
  list: (query: OnlineUserPageQuery) => {
    return http.get<PageResult<OnlineUser[]>>('/monitor/online', {
      params: query,
    });
  },

  /** 强退在线用户 */
  kickout: (token: string) => {
    return http.delete(`/monitor/online/${token}`);
  },

  /** 批量强退在线用户 */
  batchKickout: (tokens: string[]) => {
    return http.delete('/monitor/online', { data: tokens });
  },
};

/* ==================== Schema 定义 ==================== */
/** 在线用户 */
export interface OnlineUser {
  sessionId: string;
  token: string;
  loginName: string;
  ip: string;
  location: string;
  browser: string;
  os: string;
  loginTime: string;
  lastActiveTime: string;
}
export interface OnlineUserQuery {
  keyword?: string;
  sort?: Array<string>;
}
export interface OnlineUserPageQuery extends OnlineUserQuery, PageQuery {}
