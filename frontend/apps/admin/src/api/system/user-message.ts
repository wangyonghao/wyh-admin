import type { NoticePageQuery, NoticeResp } from './notice';

import type { PageQuery, PageResult } from '#/types/api';

import http from '#/utils/request';

/* ==================== API 定义 ==================== */
export const userMessageApi = {
  /** 查询未读消息数量 */
  getUnreadCount: () => {
    return http.get(`/user/message/unread`);
  },
  /** 查询消息列表 */
  list: (query: MessagePageQuery) => {
    return http.get<PageResult<MessageResp[]>>(`/user/message`, {
      params: query,
    });
  },
  /** 获取用户消息详情 */
  detail: (id: string) => {
    return http.get<MessageResp>(`/user/message/${id}`);
  },
  /** 删除消息 */
  delete: (ids: Array<string>) => {
    return http.delete(`/user/message`, { data: { ids } });
  },
  /** 标记已读 */
  read: (ids: Array<string>) => {
    return http.patch(`/user/message/read`, { data: { ids } });
  },
  /** 全部已读 */
  readAll: () => {
    return http.patch(`/user/message/readAll`);
  },
  /** 查询未读公告数量 */
  getUnreadNoticeCount: () => {
    return http.get(`/user/message/notice/unread`);
  },
  /** 查询未读公告 ID 列表 */
  getUnreadNoticeIds: (method: string) => {
    return http.get<string[]>(`/user/message/notice/unread/${method}`);
  },
  /** 分页查询用户公告 */
  listNotice: (query: NoticePageQuery) => {
    return http.get<PageResult<NoticeResp[]>>(`/user/message/notice`, {
      params: query,
    });
  },
  /** 获取用户公告详情 */
  getNoticeDetail: (id: string) => {
    return http.get<NoticeResp>(`/user/message/notice/${id}`);
  },
};

/* ==================== Schema 定义 ==================== */

/** 系统消息类型 */
export interface MessageResp {
  id: string;
  title: string;
  content: string;
  type: number;
  path: string;
  isRead: boolean;
  readTime?: string;
  createUserString?: string;
  createTime: string;
}

export interface MessageQuery {
  title?: string;
  type?: number;
  isRead?: boolean;
  sort: Array<string>;
}

export interface MessagePageQuery extends MessageQuery, PageQuery {}
