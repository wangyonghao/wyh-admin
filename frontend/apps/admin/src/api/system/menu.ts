import http from '#/utils/request';

export const menuApi = {
  /** 查询登录和权限用的启用菜单树 */
  tree(query?: MenuQuery) {
    return http.get<Menu[]>(`/system/menu`, { params: query });
  },
  /** 管理页：全部状态的扁平菜单，含按钮 */
  all() {
    return http.get<MenuNode[]>(`/system/menu/all`);
  },
  /** 查询菜单详情 */
  detail(id: string) {
    return http.get<Menu>(`/system/menu/${id}`);
  },
  /** 新增菜单 */
  create(data: MenuWrite) {
    return http.post<{ id: number | string }>(`/system/menu`, data);
  },
  /** 修改菜单 */
  update(id: string, data: MenuWrite) {
    return http.put(`/system/menu/${id}`, { ...data, id });
  },
  /** 删除菜单 */
  delete(id: string) {
    return http.delete(`/system/menu/${id}`);
  },
  /** 保存同级顺序和上级 */
  sort(data: { items: MenuSortItem[] }) {
    return http.put(`/system/menu/sort`, data);
  },
};

/** 菜单类型 */
export interface Menu {
  id: string;
  /** 菜单名称 */
  name: string;
  /** 菜单标题（可选，兼容性字段） */
  title?: string;
  parentId: string;
  type: 1 | 2 | 3;
  path: string;
  component: string;
  redirect: string;
  icon: string;
  isExternal: boolean;
  isCache: boolean;
  isHidden: boolean;
  permission: string;
  sort: number;
  status: 1 | 2;
  children?: Menu[];
}

export interface MenuQuery {
  title?: string;
  status?: number;
}

/** GET /system/menu/all 的一行 */
export interface MenuNode {
  id: number | string;
  name?: null | string;
  parentId?: null | number | string;
  type?: null | number | string;
  path?: null | string;
  component?: null | string;
  icon?: null | string;
  isExternal?: boolean | null;
  isCache?: boolean | null;
  isHidden?: boolean | null;
  permission?: null | string;
  sort?: null | number;
  status?: null | number | string;
}

export interface MenuWrite {
  id?: string;
  name: string;
  parentId: number | string;
  type?: 1 | 2 | 3;
  path?: string;
  component?: string;
  icon?: string;
  isExternal?: boolean;
  isCache?: boolean;
  isHidden?: boolean;
  permission?: string;
  sort: number;
  status: 1 | 2;
}

export interface MenuSortItem {
  id: string;
  parentId: number | string;
  sort: number;
}
