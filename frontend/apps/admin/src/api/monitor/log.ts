import type { PageQuery, PageResult } from '#/types/api';
import http, { download } from '#/utils/request';

/* ==================== API 定义 ==================== */
export const logApi = {
  /** 分页查询操作日志列表 */
  list: (query: LogPageQuery) => {
    return http.get<PageResult<OperationLogResp[]>>('/system/log', {
      params: query,
    });
  },

  /** 查询操作日志详情 */
  detail: (id: string) => {
    return http.get<OperationLogDetailResp>(`/system/log/${id}`);
  },

  /** 导出登录日志 */
  exportLoginLog: (query: LogQuery) => {
    return download('/monitor/login-log/export', { params: query });
  },

  /** 导出操作日志 */
  exportOperationLog: (query: LogQuery) => {
    return download('/system/log/export/operation', { params: query });
  },
};

/* ==================== Schema 定义 ==================== */

/** 操作日志响应 */
export interface OperationLogResp {
  /** ID */
  id: string;
  /** 业务对象类型 */
  objectType: string;
  /** 业务对象ID */
  objectId: string;
  /** 操作类型 */
  operation: string;
  /** 操作者ID */
  operatorId: string;
  /** 操作者名称 */
  operatorName: string;
  /** 操作者IP */
  operatorIp: string;
  /** 操作者位置 */
  operatorLocation: string;
  /** 操作时间 */
  operateTime: string;
}

/** 操作日志详情响应 */
export interface OperationLogDetailResp extends OperationLogResp {
  /** 状态 */
  status: string;
  /** 备注 */
  remark: string;
  /** 额外信息 */
  extra: string;
}

/** 日志查询条件 */
export interface LogQuery {
  /** 业务对象类型 */
  objectType?: string;
  /** 业务对象ID */
  objectId?: string;
  /** 操作IP */
  operatorIp?: string;
  /** 操作地点 */
  operatorLocation?: string;
  /** 操作人 */
  operatorName?: string;
  /** 日志描述 */
  operation?: string;
  /** 操作时间 */
  createTime?: Array<string>;
  /** 状态 */
  status?: number;
  /** 排序 */
  sort?: Array<string>;
}

/** 日志分页查询 */
export interface LogPageQuery extends LogQuery, PageQuery {}
