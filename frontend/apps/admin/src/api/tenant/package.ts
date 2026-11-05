import type { BaseEntity, PageQuery, PageResult } from '#/types/api';

import http from '#/utils/request';

function toId(id: unknown) {
  return id == null ? '' : String(id);
}

/** 租户套餐 */
export interface TenantPackageResp extends BaseEntity {
  id: string;
  name: string;
  sort: number;
  menuCheckStrictly: string;
  description: string;
  status: number;
  menuIds: string[];
  disabled: boolean;
}

export interface TenantPackageQuery {
  name?: string | undefined;
  status?: number | string;
  description?: string;
  sort: Array<string> | string;
}

export interface TenantPackagePageQuery extends PageQuery, TenantPackageQuery {}

/** 查询租户套餐列表 */
export async function listTenantPackage(query: TenantPackagePageQuery) {
  const page = await http.get<PageResult<TenantPackageResp[]>>('/tenant/package', {
    params: query,
  });
  return {
    ...page,
    records: (page.records ?? []).map((row) => ({ ...row, id: toId(row.id) })),
  };
}

/** 查询租户套餐详情 */
export async function getTenantPackage(id: string) {
  const row = await http.get<TenantPackageResp>(`/tenant/package/${id}`);
  return {
    ...row,
    id: toId(row.id),
    menuIds: (row.menuIds ?? []).map((menuId) => toId(menuId)),
  };
}

/** 新增租户套餐 */
export function addTenantPackage(data: any) {
  return http.post('/tenant/package', data);
}

/** 修改租户套餐 */
export function updateTenantPackage(data: any, id: string) {
  return http.put(`/tenant/package/${id}`, data);
}

/** 删除租户套餐 */
export function deleteTenantPackage(id: string) {
  return http.delete(`/tenant/package/${id}`);
}

/** 查询所有套餐 */
export async function listAllTenantPackage() {
  const rows = await http.get<TenantPackageResp[]>(`/tenant/package/list`);
  return (rows ?? []).map((row) => ({ ...row, id: toId(row.id) }));
}
