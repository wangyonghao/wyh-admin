import http, { download } from '#/utils/request';

/* ==================== API 定义 ==================== */
export const openAppApi = {
  /**
   * 应用列表查询
   */
  list: (params: OpenAppApi.AppQuery) => {
    return http.get<{
      page: number;
      pageSize: number;
      pages: number;
      records: OpenAppApi.AppResp[];
      total: number;
    }>('/open/app', { params });
  },
  /**
   * 应用详情查询
   */
  detail: (id: string) => {
    return http.get<OpenAppApi.AppResp>(`/open/app/${id}`);
  },
  /**
   * 新增应用
   */
  create: (data: OpenAppApi.AppAddReq) => {
    return http.post<OpenAppApi.AppResp>('/open/app', data);
  },
  /**
   * 修改应用
   */
  update: (id: string, data: OpenAppApi.AppUpdateReq) => {
    return http.put<OpenAppApi.AppResp>(`/open/app/${id}`, data);
  },
  /**
   * 删除应用
   */
  delete: (id: string) => {
    return http.delete(`/open/app/${id}`);
  },
  /**
   * 导出应用
   */
  export: (params: OpenAppApi.AppQuery) => {
    return download('/open/app/export', {
      params,
    });
  },
  /**
   * 查看密钥
   */
  getSecretKey: (id: string) => {
    return http.get<OpenAppApi.SecretKeyResp>(`/open/app/${id}/secret`);
  },
  /**
   * 重置密钥
   */
  resetSecretKey: (id: string) => {
    return http.patch<OpenAppApi.SecretKeyResp>(
      `/open/app/${id}/secret`,
    );
  },
};

/* ==================== Schema 定义 ==================== */

export namespace OpenAppApi {
  /**
   * 应用查询参数
   */
  export interface AppQuery {
    /** 应用名称 */
    name?: string;
    /** 状态 */
    status?: number;
    /** 创建时间 */
    createTime?: [string, string];
  }

  /**
   * 应用响应数据
   */
  export interface AppResp {
    /** ID */
    id: string;
    /** 应用名称 */
    name: string;
    /** Access Key */
    accessKey: string;
    /** Secret Key */
    secretKey?: string;
    /** 失效时间 */
    expireTime?: string;
    /** 描述 */
    description?: string;
    /** 状态：1-启用，2-禁用 */
    status: number;
    /** 是否禁用 */
    disabled?: boolean;
    /** 创建人 */
    createUserString?: string;
    /** 创建时间 */
    createTime?: string;
    /** 修改人 */
    updateUserString?: string;
    /** 修改时间 */
    updateTime?: string;
  }

  /**
   * 应用新增参数
   */
  export interface AppAddReq {
    /** 应用名称 */
    name: string;
    /** 失效时间 */
    expireTime?: string;
    /** 描述 */
    description?: string;
    /** 状态：1-启用，2-禁用 */
    status: number;
  }

  /**
   * 应用修改参数
   */
  export interface AppUpdateReq extends AppAddReq {
    /** ID */
    id: string;
  }

  /**
   * 密钥响应数据
   */
  export interface SecretKeyResp {
    /** Secret Key */
    secretKey: string;
  }
}
