import type { BaseEntity, PageQuery, PageResult } from '#/types/api';

import http from '#/utils/request';

function toId(id: unknown): string {
  return String(id);
}

/* ==================== API 定义 ==================== */
export const roleApi = {
  /** 查询角色列表 */
  async list(query?: RolePageQuery) {
    const result = await http.get<RoleResp[]>(`/system/role/list`, { params: query });
    return result.map((role) => ({ ...role, id: toId(role.id) }));
  },
  /** 查询角色详情 */
  async detail(id: string) {
    const result = await http.get<RoleDetailResp>(`/system/role/${id}`);
    return {
      ...result,
      id: toId(result.id),
      menuIds: (result.menuIds ?? []).map(toId),
      departmentIds: (result.departmentIds ?? []).map(toId),
    };
  },
  /** 新增角色 */
  async create(data: any) {
    const result = await http.post<{ id: string }>('/system/role', data);
    return { ...result, id: toId(result.id) };
  },
  /** 修改角色 */
  update(data: any, id: string) {
    return http.put(`/system/role/${id}`, data);
  },
  /** 删除角色 */
  delete(id: string) {
    return http.delete(`/system/role/${id}`);
  },
  /** 查询角色权限树 */
  treePermission() {
    return http.get<RolePermissionResp[]>(`/system/permission/tree`);
  },
  /** 修改角色权限 */
  updatePermission(id: string, data: any) {
    return http.put(`/system/role/${id}/permission`, { ...data });
  },
  /** 查询角色关联用户 */
  pageMember(id: string, query: RoleUserPageQuery) {
    return http.get<PageResult<RoleUserResp[]>>(`/system/role/${id}/user`, { params: query, });
  },
  /** 分配角色给用户 */
  addMember(id: string, userIds: Array<string>) {
    return http.post(`/system/role/${id}/user`, userIds);
  },
  /** 取消分配角色给用户 */
  removeMember(id: string | undefined, userIds: string[]) {
    return http.delete(`/system/role/${id}/user`, { data: {userIds}, });
  },
  /** 角色下拉。后端没有 /option，用列表映射。 */
  async listOption(query?: { name?: string; status?: number }) {
    const roles = await roleApi.list({
      description: undefined,
      sort: [],
      page: 1,
      pageSize: 500,
    });
    const keyword = query?.name?.trim().toLowerCase();
    return roles
      .filter((role) => !keyword || role.name.toLowerCase().includes(keyword))
      .map((role) => ({ label: role.name, value: role.id }));
  },
};

/* ==================== Schema 定义 ==================== */
export interface RoleUserResp {
  id: string;
  username: string;
  displayName: string;
  gender: number;
  description: string;
  status: 1 | 2;
  isBuiltin?: boolean;
  departmentId: string;
  departmentName: string;
  roleIds: string[];
  roleNames: Array<string>;
  disabled: boolean;
}

export interface RoleResp extends BaseEntity {
  /** ID */
  id: string;
  /** 名称 */
  name: string;
  /** 编码 */
  code: string;
  /** 数据权限（1：全部数据权限；2：本部门及以下数据权限；3：本部门数据权限；4：仅本人数据权限；5：自定义数据权限） */
  dataScope: string;
  /** 描述 */
  description: string;
  /** 排序 */
  sort: number;
  /** 是否为系统内置数据 */
  isBuiltin: string;
  /** 菜单选择是否父子节点关联 */
  menuCheckStrictly: string;
  /** 部门选择是否父子节点关联 */
  departmentCheckStrictly: string;
}

export interface RoleDetailResp extends BaseEntity {
  id: string;
  name: string;
  code: string;
  dataScope: string;
  description: string;
  sort: string;
  isBuiltin: string;
  menuCheckStrictly: boolean;
  departmentCheckStrictly: boolean;
  menuIds: string[];
  departmentIds: string[];
}

export interface RoleQuery {
  description: string | undefined;
  sort: Array<string> | string;
}

export interface RolePermissionResp {
  id: string;
  title: string;
  parentId: string;
  permission?: string;
  children?: RolePermissionResp[];
  permissions?: RolePermissionResp[];
  isChecked?: boolean;
}

export interface RolePageQuery extends PageQuery, RoleQuery {}

export interface RoleUserQuery {
  keyword?: string;
  sort: Array<string>;
}

export interface RoleUserPageQuery extends PageQuery, RoleUserQuery {}
