import type { BaseEntity, PageQuery, PageResult } from '#/types/api';
import type { Gender, Option } from '#/types/global';

import http from '#/api/http';

/* ==================== API 定义 ==================== */
export const userApi = {
  /** 查询用户列表 */
  list: (query: UserPageQuery) => {
    return http.get<PageResult<UserResp[]>>('/system/user', {
      params: query,
    });
  },
  /** 查询用户详情 */
  detail: (id: string) => {
    return http.get<UserDetailResp>(`/system/user/${id}`);
  },
  /** 新增用户 */
  create: (data: any) => {
    return http.post('/system/user', data);
  },
  /** 修改用户 */
  update: (data: any, id: string) => {
    return http.patch(`/system/user/${id}`, data);
  },
  /** 删除用户 */
  delete: (id: string) => {
    return http.delete('/system/user', { data: { ids: [id] } });
  },
  /** 导出用户 */
  export: (query: UserQuery) => {
    return http.download('/system/user/export', { params: query, });
  },
  /** 下载用户导入模板 */
  downloadTemplate: () => {
    return http.download(`/system/user/import/template`);
  },
  /** 解析用户导入数据 */
  parseImport: (data: FormData) => {
    return http.post('/system/user/import/parse', data);
  },
  /** 导入用户 */
  import: (data: any) => {
    return http.post('/system/user/import', data);
  },
  /** 重置密码 */
  resetPassword: (data: any, id: string) => {
    return http.put(`/system/user/${id}/password`, data);
  },
  /** 分配角色 */
  updateRole: (data: { roleIds: Array<number | string> }, id: string) => {
    return http.put(`/system/user/${id}/role`, data);
  },
  /** 查询用户字典 */
  dict: (query?: { status: number }) => {
    return http.get<Option[]>('/system/user/dict', {
      params: query,
    });
  },
};

/* ==================== Schema 定义 ==================== */

export interface UserResp extends BaseEntity {
  /** ID */
  id: string;
  /** 用户名 */
  username: string;
  /** 昵称 */
  nickname: string;
  /** 密码 */
  password: string;
  /** 性别（0：未知；1：男；2：女） */
  gender: Gender;
  /** 邮箱 */
  email: string;
  /** 手机号码 */
  phone: string;
  /** 头像 */
  avatar: string;
  /** 描述 */
  description: string;
  /** 状态（1：启用；2：禁用） */
  status: 1 | 2;
  /** 是否为系统内置数据 */
  isBuiltin: boolean;
  /** 最后一次修改密码时间 */
  pwdResetTime: string;
  /** 部门ID */
  deptId: string;
  /** 租户ID */
  tenantId: string;
  deptName: string;
  roleIds: Array<number | string>;
  roleNames: Array<string>;
  disabled: boolean;
}

export interface UserDetailResp extends UserResp {
  /** 最后一次修改密码时间 */
  pwdResetTime: string;
  /** 创建人 */
  createUser: string;
  /** 创建时间 */
  createTime: string;
  /** 修改人 */
  updateUser: string;
  /** 修改时间 */
  updateTime: string;
}

export interface UserQuery {
  keyword?: string;
  email?: string;
  phone?: string;
  status?: string;
  deptId?: string;
  sort?: Array<string> | string;
  userIds?: Array<string>;
  roleId?: string;
  createTime?: Array<string>;
}

export interface UserPageQuery extends PageQuery, UserQuery {}
