import type { PageQuery, PageResult } from '#/types/api';
import type { Option } from '#/types/global';
import http from '#/utils/request';

/* ==================== API 定义 ==================== */
export const optionApi = {
  /** 分页查询选项列表 */
  page: (query: OptionPageQuery) => {
    return http.get<PageResult<OptionResult>>('/system/option/page', { params: query });
  },
  /** 新增选项 */
  create: (data: OptionRequest) => {
    return http.post('/system/option', data);
  },
  /** 修改选项 */
  update: (id: string, data: OptionRequest) => {
    return http.put(`/system/option/${id}`, data);
  },
  /** 批量删除选项 */
  delete: (ids: string[]) => {
    return http.delete('/system/option', { data: ids });
  },
  /** 清除选项缓存 */
  clearCache: (type: string) => {
    return http.delete(`/system/option/cache/${type}`);
  },
  /** 按类型查询已启用的选项 */
  list: (type: string) => {
    return http.get<Option[]>(`/system/option/${type}`);
  },
};

/* ==================== Schema 定义 ==================== */

/**
 * 选项响应参数
 */
export interface OptionResult {
  /** ID */
  id: string;
  /** 选项类型 */
  optionType: string;
  /** 选项值 */
  value: string;
  /** 选项标签 */
  label: string;
  /** 扩展信息 */
  ext?: Record<string, any>;
  /** 排序 */
  sort: number;
  /** 是否启用 */
  enabled: boolean;
  /** 描述 */
  description?: string;
}

/**
 * 选项查询条件
 */
export interface OptionQuery {
  /** 关键词（搜索类型、标签、值、描述） */
  keyword?: string;
  /** 选项类型 */
  optionType?: string;
  /** 是否启用 */
  enabled?: boolean;
  /** 状态，对应 enabled */
  status?: boolean;
}

/**
 * 选项创建或修改请求参数
 */
export interface OptionRequest {
  /** 选项类型 */
  optionType: string;
  /** 选项值 */
  value: string;
  /** 选项标签 */
  label: string;
  /** 扩展信息 */
  extra?: Record<string, any>;
  /** 排序 */
  sort?: number;
  /** 是否启用 */
  enabled?: boolean;
  /** 描述 */
  description?: string;
}

/**
 * 选项分页查询参数
 */
export interface OptionPageQuery extends OptionQuery, PageQuery {}
