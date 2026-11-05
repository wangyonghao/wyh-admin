import type { BaseEntity, PageQuery,  PageResult } from '#/types/api';

import http from '#/utils/request';

export interface TenantResp extends BaseEntity {
  /** ID */
  id: string;
  /** 名称 */
  name: string;
  /** 编码 */
  code: string;
  /** 域名 */
  domain: string;
  /** 过期时间 */
  expireTime: string;
  /** 描述 */
  description: string;
  /** 状态（1：启用；2：禁用） */
  status: number;
  /** 管理员用户 */
  adminUser: string;
  /** 管理员用户名 */
  adminUsername: string;
  /** 管理员密码 */
  adminPassword: string;
  /** 套餐ID */
  packageId: string;
  /** 套餐名称 */
  packageName?: string;
}

export interface TenantDetailResp extends BaseEntity {
  id: string;
  name: string;
  code: string;
  domain: string;
  expireTime: string;
  description: string;
  status: string;
  adminUser: string;
  adminUsername: string;
  adminPassword: string;
  packageId: string;
}

export interface TenantQuery {
  name: string | undefined;
  code: string | undefined;
  domain: string | undefined;
  packageId: string | undefined;
  status?: number;
  sort: Array<string> | string;
}

export interface TenantPageQuery extends PageQuery, TenantQuery {}

function toId(id: unknown) {
  return id == null ? '' : String(id);
}

function normalizeTenant(row: TenantResp): TenantResp {
  return {
    ...row,
    id: toId(row.id),
    packageId: toId(row.packageId),
    adminUser: row.adminUser == null ? row.adminUser : toId(row.adminUser),
  };
}

/** 查询租户列表 */
export async function listTenant(query: TenantPageQuery) {
  const page = await http.get<PageResult<TenantResp[]>>(`/tenant/management`, {
    params: query,
  });
  return {
    ...page,
    records: (page.records ?? []).map((row) => normalizeTenant(row)),
  };
}

/** 查询租户详情 */
export async function getTenant(id: string) {
  const row = await http.get<TenantResp>(`/tenant/management/${id}`);
  return normalizeTenant(row);
}

/** 新增租户 */
export function addTenant(data: any) {
  return http.post(`/tenant/management`, data);
}

/** 修改租户 */
export function updateTenant(data: any, id: string) {
  return http.put(`/tenant/management/${id}`, data);
}

/** 删除租户 */
export function deleteTenant(id: string) {
  return http.delete(`/tenant/management/${id}`);
}
