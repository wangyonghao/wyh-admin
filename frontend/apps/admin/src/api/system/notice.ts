import type { BaseEntity, PageQuery, PageResult } from '#/types/api';

import http from '#/utils/request';

/* ==================== API 定义 ==================== */
export const noticeApi = {
  /** 分页查询公告列表 */
  list: (query: NoticePageQuery) => {
    return http.get<PageResult<NoticeResp[]>>('/system/notice', {
      params: query,
    });
  },

  /** 查询公告详情 */
  detail: (id: string) => {
    return http.get<NoticeDetailResp>(`/system/notice/${id}`);
  },

  /** 新增公告 */
  create: (data: NoticeCreateReq) => {
    return http.post('/system/notice', data);
  },

  /** 修改公告 */
  update: (id: string, data: NoticeUpdateReq) => {
    return http.put(`/system/notice/${id}`, data);
  },

  /** 删除公告 */
  delete: (id: string) => {
    return http.delete('/system/notice', { data: { ids: [id] } });
  },
};

/* ==================== Schema 定义 ==================== */

/** 公告响应 */
export interface NoticeResp extends BaseEntity {
  /** ID */
  id: string;
  /** 标题 */
  title: string;
  /** 内容 */
  content: string;
  /** 分类 */
  type: string;
  /** 通知范围（1：所有人；2：指定用户） */
  noticeScope: string;
  /** 通知用户 */
  noticeUsers: string;
  /** 通知方式（1：系统消息；2：登录弹窗） */
  noticeMethods: string;
  /** 是否定时 */
  isTiming: string;
  /** 发布时间 */
  publishTime: string;
  /** 是否置顶 */
  isTop: string;
  isRead: boolean;
  /** 状态（1：草稿；2：待发布；3：已发布） */
  status: number;
  /** 创建人 */
  createUser: string;
  /** 创建时间 */
  createTime: string;
}

/** 公告详情响应 */
export interface NoticeDetailResp extends BaseEntity {
  /** ID */
  id: string;
  /** 标题 */
  title: string;
  /** 内容 */
  content: string;
  /** 分类 */
  type: string;
  /** 通知范围 */
  noticeScope: string;
  /** 通知用户 */
  noticeUsers: string;
  /** 通知方式 */
  noticeMethods: string;
  /** 是否定时 */
  isTiming: string;
  /** 发布时间 */
  publishTime: string;
  /** 是否置顶 */
  isTop: string;
  /** 状态 */
  status: number;
  /** 创建人 */
  createUser: string;
  /** 创建时间 */
  createTime: string;
  /** 更新人 */
  updateUser: string;
  /** 更新时间 */
  updateTime: string;
  /** 租户ID */
  tenantId: string;
  /** 创建人名称 */
  createUserString: string;
  /** 更新人名称 */
  updateUserString: string;
}

/** 公告查询条件 */
export interface NoticeQuery {
  /** 标题 */
  title?: string;
  /** 分类 */
  type?: string;
  /** 发布时间 */
  publishTime?: string;
  /** 是否置顶 */
  isTop?: string;
  /** 状态 */
  status?: string;
  /** 排序 */
  sort?: Array<string> | string;
}

/** 公告分页查询 */
export interface NoticePageQuery extends NoticeQuery, PageQuery {}

/** 公告创建请求 */
export interface NoticeCreateReq {
  /** 标题 */
  title: string;
  /** 内容 */
  content: string;
  /** 分类 */
  type: string;
  /** 通知范围 */
  noticeScope: string;
  /** 通知用户 */
  noticeUsers?: string;
  /** 通知方式 */
  noticeMethods: string;
  /** 是否定时 */
  isTiming: string;
  /** 发布时间 */
  publishTime?: string;
  /** 是否置顶 */
  isTop: string;
}

/** 公告更新请求 */
export interface NoticeUpdateReq extends NoticeCreateReq {}
