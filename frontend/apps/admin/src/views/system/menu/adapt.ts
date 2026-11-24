import type { MenuNode, MenuWrite } from '#/api/system/menu';

import type { AppRecord, MenuRecord, MenuStatus } from './types';

function text(value: null | string | undefined) {
  return value ?? '';
}

function asId(value: null | number | string | undefined) {
  if (value == null || value === '') return '0';
  return String(value);
}

export function statusCode(status: MenuStatus): 1 | 2 {
  return status === 'disabled' ? 2 : 1;
}

/** 界面里模块的直接子节点 parentId 为空，接口里上级是模块 id。 */
export function apiParent(node: Pick<MenuRecord, 'appId' | 'parentId'>) {
  return node.parentId ?? node.appId;
}

function kindOf(type: number): MenuRecord['type'] {
  if (type === 1) return 'folder';
  if (type === 3) return 'button';
  return 'function';
}

function typeCode(value: unknown): number {
  if (typeof value === 'number' && Number.isFinite(value)) return value;
  if (typeof value === 'string') {
    const named: Record<string, number> = {
      BUTTON: 3,
      DIR: 1,
      MENU: 2,
      button: 3,
      folder: 1,
      function: 2,
    };
    if (value in named) return named[value];
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }
  if (value && typeof value === 'object' && 'value' in value) {
    return typeCode((value as { value: unknown }).value);
  }
  return 0;
}

function disabledStatus(value: unknown): boolean {
  if (value && typeof value === 'object' && 'value' in value) {
    return disabledStatus((value as { value: unknown }).value);
  }
  return value === 2 || value === '2' || value === 'DISABLE' || value === 'disabled';
}

type MenuPayload = MenuNode & { children?: MenuPayload[] | null; title?: null | string };

/** 接口可能是扁平数组、包在 data 里，或带着 children 的树。 */
export function menuRows(payload: unknown): MenuNode[] {
  const root = unwrapMenuPayload(payload);
  const flat: MenuNode[] = [];
  const walk = (rows: MenuPayload[], parentId?: string) => {
    for (const row of rows) {
      const { children, ...rest } = row;
      if ((rest.parentId == null || rest.parentId === '') && parentId) {
        rest.parentId = parentId;
      }
      if (!rest.name && rest.title) rest.name = rest.title;
      flat.push(rest);
      if (Array.isArray(children) && children.length > 0) walk(children, asId(rest.id));
    }
  };
  walk(root);
  return flat;
}

function unwrapMenuPayload(payload: unknown): MenuPayload[] {
  if (Array.isArray(payload)) return payload as MenuPayload[];
  if (payload && typeof payload === 'object') {
    const body = payload as { data?: unknown; rows?: unknown };
    if (Array.isArray(body.data)) return body.data as MenuPayload[];
    if (Array.isArray(body.rows)) return body.rows as MenuPayload[];
  }
  return [];
}

/** 根目录（parentId 为 0 且类型为目录）是模块，其余行是树节点。 */
export function splitMenus(payload: unknown) {
  const flat = menuRows(payload).map((raw) => {
    const parentId = asId(raw.parentId);
    return {
      cache: !!raw.isCache,
      component: text(raw.component),
      external: !!raw.isExternal,
      icon: text(raw.icon),
      id: asId(raw.id),
      name: text(raw.name),
      parentId: parentId === '0' ? '0' : parentId,
      path: text(raw.path),
      permission: text(raw.permission),
      sort: raw.sort ?? 0,
      status: (disabledStatus(raw.status) ? 'disabled' : 'enabled') as MenuStatus,
      type: typeCode(raw.type),
      visible: !raw.isHidden,
    };
  });
  const byId = new Map(flat.map((row) => [row.id, row]));
  const moduleIds = new Set(
    flat.filter((row) => row.parentId === '0' && row.type === 1).map((row) => row.id),
  );
  const moduleOf = (id: string) => {
    const seen = new Set<string>();
    let current = byId.get(id);
    while (current) {
      if (moduleIds.has(current.id)) return current.id;
      if (current.parentId === '0' || seen.has(current.id)) return null;
      seen.add(current.id);
      current = byId.get(current.parentId);
    }
    return null;
  };
  const apps: AppRecord[] = flat
    .filter((row) => moduleIds.has(row.id))
    .map((row) => ({
      code: row.permission,
      entry: row.path,
      icon: row.icon,
      id: row.id,
      name: row.name,
      sort: row.sort,
      status: row.status,
    }));
  const nodes: MenuRecord[] = [];
  for (const row of flat) {
    if (moduleIds.has(row.id)) continue;
    const appId = moduleOf(row.id);
    if (!appId) continue;
    nodes.push({
      apis: [],
      appId,
      cache: row.cache,
      component: row.component,
      external: row.external,
      icon: row.icon,
      id: row.id,
      name: row.name,
      parentId: moduleIds.has(row.parentId) ? null : row.parentId,
      path: row.path,
      permission: row.permission,
      remark: '',
      sort: row.sort,
      status: row.status,
      type: kindOf(row.type),
      visible: row.visible,
    });
  }
  return { apps, nodes };
}

export function moduleCreate(app: Omit<AppRecord, 'id'>): MenuWrite {
  return {
    component: 'Layout',
    icon: app.icon,
    isCache: false,
    isExternal: false,
    isHidden: false,
    name: app.name.trim(),
    parentId: 0,
    path: app.entry,
    permission: app.code.trim(),
    sort: Math.max(0, app.sort),
    status: statusCode(app.status),
    type: 1,
  };
}

export function moduleUpdate(app: AppRecord): MenuWrite {
  return {
    icon: app.icon,
    id: app.id,
    isHidden: false,
    name: app.name.trim(),
    parentId: 0,
    path: app.entry,
    permission: app.code.trim(),
    sort: Math.max(0, app.sort),
    status: statusCode(app.status),
  };
}

export function nodeCreate(node: MenuRecord): MenuWrite {
  const type = node.type === 'folder' ? 1 : node.type === 'button' ? 3 : 2;
  const body: MenuWrite = {
    icon: node.icon,
    isCache: node.type === 'function' ? node.cache : false,
    isExternal: node.type === 'function' ? node.external : false,
    isHidden: node.type === 'button' ? false : !node.visible,
    name: node.name.trim(),
    parentId: apiParent(node),
    path: node.type === 'function' ? node.path : '',
    permission: node.permission,
    sort: Math.max(0, node.sort),
    status: statusCode(node.status),
    type,
  };
  if (node.type === 'folder') body.component = node.component || 'Layout';
  if (node.type === 'function') body.component = node.component;
  return body;
}

export function folderUpdate(node: MenuRecord): MenuWrite {
  return {
    icon: node.icon,
    id: node.id,
    isHidden: !node.visible,
    name: node.name.trim(),
    parentId: apiParent(node),
    sort: Math.max(0, node.sort),
    status: statusCode(node.status),
  };
}

export function functionUpdate(node: MenuRecord): MenuWrite {
  return {
    component: node.component,
    icon: node.icon,
    id: node.id,
    isCache: node.cache,
    isExternal: node.external,
    isHidden: !node.visible,
    name: node.name.trim(),
    parentId: apiParent(node),
    path: node.path,
    permission: node.permission,
    sort: Math.max(0, node.sort),
    status: statusCode(node.status),
  };
}

export function buttonWrite(node: MenuRecord): MenuWrite {
  return {
    icon: '',
    id: node.id,
    isHidden: false,
    name: node.name.trim(),
    parentId: node.parentId ?? node.appId,
    permission: node.permission,
    sort: Math.max(0, node.sort),
    status: statusCode(node.status),
  };
}
