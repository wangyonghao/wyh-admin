import type { CatalogNode } from '#/api/system/menu-catalog';

export interface MenuRow {
  node: CatalogNode;
  depth: number;
}

export function navigationNodes(nodes: CatalogNode[]) {
  return nodes.filter((node) => node.type !== 3);
}

export function rows(nodes: CatalogNode[]): MenuRow[] {
  const nav = navigationNodes(nodes);
  const children = new Map<string, CatalogNode[]>();
  for (const node of nav) {
    const parent = node.parentId || '0';
    const list = children.get(parent) ?? [];
    list.push(node);
    children.set(parent, list);
  }
  for (const list of children.values()) {
    list.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
  }
  const result: MenuRow[] = [];
  const visit = (parentId: string, depth: number) => {
    for (const node of children.get(parentId) ?? []) {
      result.push({ depth, node });
      visit(node.id, depth + 1);
    }
  };
  visit('0', 0);
  return result;
}

export function directories(nodes: CatalogNode[]) {
  return navigationNodes(nodes).filter((node) => node.type === 1 && !node.pendingDelete);
}

export function isDescendant(nodes: CatalogNode[], ancestorId: string, nodeId: string) {
  const byId = new Map(nodes.map((node) => [node.id, node]));
  let current = byId.get(nodeId);
  const guard = new Set<string>();
  while (current && current.parentId && current.parentId !== '0') {
    if (!guard.add(current.id)) return false;
    if (current.parentId === ancestorId) return true;
    current = byId.get(current.parentId);
  }
  return false;
}

export function canParent(nodes: CatalogNode[], node: CatalogNode, parentId: string) {
  if (node.type === 3) return false;
  if (parentId === '0') return true;
  if (parentId === node.id || isDescendant(nodes, node.id, parentId)) return false;
  const parent = nodes.find((item) => item.id === parentId);
  return parent?.type === 1 && !parent.pendingDelete;
}

export function moveNode(nodes: CatalogNode[], id: string, parentId: string, index: number) {
  const node = nodes.find((item) => item.id === id);
  if (!node || !canParent(nodes, node, parentId)) return nodes;
  const siblings = navigationNodes(nodes)
    .filter((item) => (item.parentId || '0') === parentId && item.id !== id)
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
  const nextSiblings = [...siblings];
  nextSiblings.splice(Math.max(0, Math.min(index, nextSiblings.length)), 0, { ...node, parentId });
  const sortOf = new Map(nextSiblings.map((item, position) => [item.id, position + 1]));
  return nodes.map((item) => {
    if (item.id === id) return { ...item, parentId, sort: sortOf.get(id) };
    const sort = sortOf.get(item.id);
    return sort == null ? item : { ...item, sort };
  });
}

export function moveSibling(nodes: CatalogNode[], id: string, direction: -1 | 1) {
  const node = nodes.find((item) => item.id === id);
  if (!node) return nodes;
  const siblings = navigationNodes(nodes)
    .filter((item) => (item.parentId || '0') === (node.parentId || '0'))
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
  const index = siblings.findIndex((item) => item.id === id);
  const target = index + direction;
  if (index < 0 || target < 0 || target >= siblings.length) return nodes;
  return moveNode(nodes, id, node.parentId || '0', target);
}

export function removeNode(nodes: CatalogNode[], id: string) {
  const node = nodes.find((item) => item.id === id);
  if (!node || node.system || node.required) return nodes;
  const navChildren = navigationNodes(nodes).some((item) => item.parentId === id && !item.pendingDelete);
  if (node.type === 1 && navChildren && !node.pendingDelete) return nodes;
  if (!node.published) {
    const drop = new Set<string>([id]);
    let growing = true;
    while (growing) {
      growing = false;
      for (const item of nodes) {
        if (!drop.has(item.id) && drop.has(item.parentId)) {
          drop.add(item.id);
          growing = true;
        }
      }
    }
    return nodes.filter((item) => !drop.has(item.id));
  }
  return nodes.map((item) => {
    if (item.id === id) return { ...item, pendingDelete: true };
    if (item.parentId === id && item.type === 3) return { ...item, pendingDelete: true };
    return item;
  });
}

export function restoreNode(nodes: CatalogNode[], id: string) {
  return nodes.map((item) => (item.id === id ? { ...item, pendingDelete: false } : item));
}

export function createNode(parentId: string): CatalogNode {
  return {
    enabled: true,
    icon: 'lucide:file',
    id: `tmp-${crypto.randomUUID()}`,
    name: '未命名菜单',
    parentId,
    path: `/page-${crypto.randomUUID().slice(0, 8)}`,
    pendingDelete: false,
    published: false,
    required: false,
    sort: 999,
    system: false,
    type: parentId === '0' ? 2 : 2,
  };
}

export function visibleTree(nodes: CatalogNode[], allowed?: Set<string>) {
  const nav = navigationNodes(nodes).filter((node) => node.enabled && !node.pendingDelete);
  const byId = new Map(nav.map((node) => [node.id, node]));
  const keep = new Set<string>();
  for (const node of nav) {
    if (node.type !== 2) continue;
    if (allowed && !allowed.has(node.id)) continue;
    let current: CatalogNode | undefined = node;
    const guard = new Set<string>();
    while (current && guard.add(current.id)) {
      keep.add(current.id);
      current = current.parentId === '0' ? undefined : byId.get(current.parentId);
    }
  }
  return rows(nav.filter((node) => keep.has(node.id)));
}
