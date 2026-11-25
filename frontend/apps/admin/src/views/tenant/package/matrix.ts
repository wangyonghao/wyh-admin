import type { MatrixMenu, MatrixPackage } from '#/api/tenant/package-catalog';

export interface MatrixRow {
  menu: MatrixMenu;
  depth: number;
  pageIds: string[];
}

export function matrixRows(menus: MatrixMenu[]): MatrixRow[] {
  const children = new Map<string, MatrixMenu[]>();
  for (const menu of menus) {
    const list = children.get(menu.parentId || '0') ?? [];
    list.push(menu);
    children.set(menu.parentId || '0', list);
  }
  const pagesUnder = (id: string): string[] => {
    const node = menus.find((menu) => menu.id === id);
    if (!node) return [];
    if (node.type === 2) return [node.id];
    return (children.get(id) ?? []).flatMap((child) => pagesUnder(child.id));
  };
  const result: MatrixRow[] = [];
  const visit = (parentId: string, depth: number) => {
    for (const menu of children.get(parentId) ?? []) {
      result.push({ depth, menu, pageIds: pagesUnder(menu.id) });
      if (menu.type === 1) visit(menu.id, depth + 1);
    }
  };
  visit('0', 0);
  return result;
}

export type CheckState = 'indeterminate' | boolean;

export function checkState(pageIds: string[], selected: Set<string>, requiredIds: Set<string>): CheckState {
  if (pageIds.length === 0) return false;
  const on = pageIds.filter((id) => selected.has(id) || requiredIds.has(id));
  if (on.length === 0) return false;
  if (on.length === pageIds.length) return true;
  return 'indeterminate';
}

export function togglePages(pageIds: string[], selected: Set<string>, requiredIds: Set<string>) {
  const next = new Set(selected);
  const state = checkState(pageIds, selected, requiredIds);
  if (state === true) {
    for (const id of pageIds) {
      if (!requiredIds.has(id)) next.delete(id);
    }
  } else {
    for (const id of pageIds) next.add(id);
  }
  for (const id of requiredIds) next.add(id);
  return next;
}

export function differs(pageIds: string[], baseId: string, packages: MatrixPackage[]) {
  const base = new Set(packages.find((item) => item.id === baseId)?.menuIds ?? []);
  return packages.some((item) => {
    if (item.id === baseId) return false;
    const selected = new Set(item.menuIds);
    return pageIds.some((id) => base.has(id) !== selected.has(id));
  });
}

export function previewGroups(menus: MatrixMenu[], packages: MatrixPackage[], selectedPackageIds: string[]) {
  const included = new Set<string>();
  for (const pkg of packages) {
    if (!selectedPackageIds.includes(pkg.id)) continue;
    for (const id of pkg.menuIds) included.add(id);
  }
  const visibleMenus = menus.filter((menu) => menu.enabled && (menu.type === 1 || included.has(menu.id)));
  const rows = matrixRows(visibleMenus).filter((row) => {
    if (row.menu.type === 2) return included.has(row.menu.id);
    return row.pageIds.some((id) => included.has(id));
  });
  const groups: { pages: string[]; title: string }[] = [];
  for (const row of rows) {
    if (row.depth === 0) {
      groups.push({
        pages: row.menu.type === 2 ? [] : [],
        title: row.menu.name,
      });
      if (row.menu.type === 2) groups[groups.length - 1]!.pages = [];
    } else if (groups.length > 0 && row.menu.type === 2) {
      groups[groups.length - 1]!.pages.push(row.menu.name);
    }
  }
  return groups.filter((group) => group.title);
}
