import type { PageQuery, PageResult } from '#/types/api';

import http, { download } from '#/utils/request';

/* ==================== API 定义 ==================== */
export const smsLogApi = {
  /** 分页查询短信日志列表 */
  list: (query: SmsLogPageQuery) => {
    return http.get<PageResult<SmsLogResp>>(`/system/sms/log`, {
      params: query,
    });
  },

  /** 查询短信日志详情 */
  detail: (id: string) => {
    return http.get<SmsLogResp>(`/system/sms/log/${id}`);
  },

  /** 导出短信日志 */
  export: (query: SmsLogQuery) => {
    return download(`/system/sms/log/export`, { params: query });
  },
};

/* ==================== Schema 定义 ==================== */

/** 短信日志响应 */
export interface SmsLogResp {
  /** ID */
  id: string;
  /** 配置ID */
  configId: string;
  /** 手机号 */
  phone: string;
  /** 参数配置 */
  params: string;
  /** 发送状态 */
  status: number;
  /** 返回数据 */
  resMsg: string;
  /** 创建人 */
  createUser: string;
  /** 创建时间 */
  createTime: string;
  /** 更新人 */
  updateUser: string;
  /** 更新时间 */
  updateTime: string;
  /** 创建人名称 */
  createUserString: string;
  /** 更新人名称 */
  updateUserString: string;
}

/** 短信日志查询条件 */
export interface SmsLogQuery {
  /** 配置ID */
  configId?: string;
  /** 手机号 */
  phone?: string;
  /** 发送状态 */
  status?: number;
  /** 排序 */
  sort?: Array<string>;
}

/** 短信日志分页查询 */
export interface SmsLogPageQuery extends PageQuery, SmsLogQuery {}
