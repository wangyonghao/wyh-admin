import http from '#/utils/request';

export type MenuScope = 'PLATFORM' | 'TENANT';

export interface CatalogNode {
  id: string;
  code?: string;
  parentId: string;
  type: 1 | 2 | 3;
  name: string;
  icon?: string;
  path?: string;
  permission?: string;
  sort?: number;
  enabled?: boolean;
  system?: boolean;
  required?: boolean;
  published?: boolean;
  pendingDelete?: boolean;
}

export interface MenuWorkspace {
  scope: MenuScope;
  version: number;
  dirty: boolean;
  nodes: CatalogNode[];
}

export interface MenuPublishResult {
  confirmRequired: boolean;
  applied: boolean;
  changes: string[];
  affectedRoleNames: string[];
  workspace: MenuWorkspace;
}

export interface MenuRelease {
  id: string;
  version: number;
  createTime?: string;
}

export interface RoleMenus {
  id: string;
  name: string;
  menuIds: string[];
}

function text(value: unknown) {
  return value == null ? '' : String(value);
}

/** 拦截器已把 AxiosResponse 换成响应体，这里只收窄类型。 */
function body<T>(value: unknown): T {
  return value as T;
}

function node(raw: CatalogNode): CatalogNode {
  return {
    ...raw,
    id: text(raw.id),
    parentId: text(raw.parentId || '0') || '0',
    type: Number(raw.type) as CatalogNode['type'],
  };
}

export const menuCatalogApi = {
  async workspace(scope: MenuScope) {
    const data = body<MenuWorkspace>(await http.get('/system/menu/catalog', { params: { scope } }));
    return { ...data, nodes: (data.nodes ?? []).map(node) };
  },
  async save(scope: MenuScope, version: number, nodes: CatalogNode[]) {
    const data = body<MenuWorkspace>(await http.put('/system/menu/catalog', { scope, version, nodes }));
    return { ...data, nodes: (data.nodes ?? []).map(node) };
  },
  async discard(scope: MenuScope, version: number) {
    const data = body<MenuWorkspace>(await http.post('/system/menu/catalog/discard', { scope, version }));
    return { ...data, nodes: (data.nodes ?? []).map(node) };
  },
  async publish(scope: MenuScope, version: number, confirm: boolean, dryRun: boolean) {
    return body<MenuPublishResult>(await http.post('/system/menu/catalog/publish', {
      confirm,
      dryRun,
      scope,
      version,
    }));
  },
  async releases(scope: MenuScope) {
    return body<MenuRelease[]>(await http.get('/system/menu/catalog/releases', { params: { scope } }));
  },
  async rollback(scope: MenuScope, version: number, releaseId: string) {
    const data = body<MenuWorkspace>(await http.post('/system/menu/catalog/rollback', {
      releaseId,
      scope,
      version,
    }));
    return { ...data, nodes: (data.nodes ?? []).map(node) };
  },
  async roles(scope: MenuScope) {
    const rows = body<RoleMenus[]>(await http.get('/system/menu/catalog/roles', { params: { scope } }));
    return (rows ?? []).map((row) => ({
      ...row,
      id: text(row.id),
      menuIds: (row.menuIds ?? []).map(text),
    }));
  },
};
