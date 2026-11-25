import http from '#/utils/request';

export type PackageLifecycle = 'DRAFT' | 'DELISTED' | 'PUBLISHED';
export type PackageKind = 'ADDON' | 'MAIN';

export interface MatrixMenu {
  id: string;
  parentId: string;
  name: string;
  type: 1 | 2;
  required: boolean;
  enabled: boolean;
}

export interface MatrixPackage {
  id: string;
  name: string;
  kind: PackageKind;
  lifecycle: PackageLifecycle;
  version: number;
  tenantCount: number;
  dirty: boolean;
  everPublished: boolean;
  menuIds: string[];
}

export interface PackageMatrix {
  menus: MatrixMenu[];
  packages: MatrixPackage[];
}

export interface PackageMenusResult {
  id: string;
  version: number;
  dirty: boolean;
  menuIds: string[];
}

function text(value: unknown) {
  return value == null ? '' : String(value);
}

/** 拦截器已把 AxiosResponse 换成响应体，这里只收窄类型。 */
function body<T>(value: unknown): T {
  return value as T;
}

function column(raw: MatrixPackage): MatrixPackage {
  return {
    ...raw,
    id: text(raw.id),
    kind: raw.kind === 'ADDON' ? 'ADDON' : 'MAIN',
    menuIds: (raw.menuIds ?? []).map(text),
  };
}

export const packageCatalogApi = {
  async matrix() {
    const data = body<PackageMatrix>(await http.get('/tenant/package/matrix'));
    return {
      menus: (data.menus ?? []).map((menu) => ({
        ...menu,
        id: text(menu.id),
        parentId: text(menu.parentId || '0') || '0',
        type: Number(menu.type) as MatrixMenu['type'],
      })),
      packages: (data.packages ?? []).map(column),
    };
  },
  async saveMenus(id: string, version: number, menuIds: string[]) {
    const data = body<PackageMenusResult>(await http.put(`/tenant/package/${id}/menus`, { menuIds, version }));
    return { ...data, id: text(data.id), menuIds: (data.menuIds ?? []).map(text) };
  },
  publish(id: string, version: number) {
    return http.post<PackageMenusResult>(`/tenant/package/${id}/publish`, { menuIds: [], version });
  },
  discard(id: string, version: number) {
    return http.post<PackageMenusResult>(`/tenant/package/${id}/discard`, { menuIds: [], version });
  },
  delist(id: string, version: number) {
    return http.post(`/tenant/package/${id}/delist`, { menuIds: [], version });
  },
  relist(id: string, version: number) {
    return http.post(`/tenant/package/${id}/relist`, { menuIds: [], version });
  },
  async copy(id: string, name: string, kind?: PackageKind) {
    return column(body<MatrixPackage>(await http.post(`/tenant/package/${id}/copy`, { kind, name })));
  },
  async create(name: string, kind: PackageKind) {
    return column(body<MatrixPackage>(await http.post('/tenant/package/catalog', { kind, name })));
  },
  rename(id: string, version: number, name: string) {
    return http.patch(`/tenant/package/${id}/name`, { name, version });
  },
  remove(id: string) {
    return http.delete(`/tenant/package/catalog/${id}`);
  },
};
