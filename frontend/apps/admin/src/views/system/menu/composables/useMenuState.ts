/* global structuredClone */
import type { AppRecord, BasicFormValues, MenuImpact, MenuRecord, RoleGrant } from '../types';

import { computed, ref } from 'vue';

import { menuApi } from '#/api/system/menu';

import {
  buttonWrite,
  folderUpdate,
  functionUpdate,
  moduleCreate,
  moduleUpdate,
  nodeCreate,
  splitMenus,
} from '../adapt';
import {
  ancestorIds,
  childrenOf,
  dropIssue,
  filterTree,
  type DropIssue,
} from './useMenuTree';
import { permissionTaken, replacePrefix } from './usePermCode';

export type DirtyChoice = 'cancel' | 'discard' | 'save';
export type PrefixChoice = 'abort' | 'keep' | 'sync';

/**
 * 根目录（parentId 为 0 的目录）是模块。目录、功能和按钮都在 sys_menu，
 * 树里不展示按钮，按钮按 parentId 读取。
 * 状态挂在 globalThis 上：热更新会再执行本模块，树和详情必须继续共用同一份列表。
 */
const menuState = (() => {
  const key = '__wyhMenuAdminState';
  type MenuStore = {
    apps: ReturnType<typeof ref<AppRecord[]>>;
    deleteAppId: ReturnType<typeof ref<null | string>>;
    dirty: ReturnType<typeof ref<boolean>>;
    expandedIds: ReturnType<typeof ref<Set<string>>>;
    formEpoch: ReturnType<typeof ref<number>>;
    hydrated: ReturnType<typeof ref<boolean>>;
    loadError: ReturnType<typeof ref<boolean>>;
    loading: ReturnType<typeof ref<boolean>>;
    nodes: ReturnType<typeof ref<MenuRecord[]>>;
    roles: ReturnType<typeof ref<RoleGrant[]>>;
    searchQuery: ReturnType<typeof ref<string>>;
    selectedAppId: ReturnType<typeof ref<null | string>>;
    selectedNodeId: ReturnType<typeof ref<null | string>>;
  };
  const host = globalThis as typeof globalThis & { [key]?: MenuStore };
  let current = host[key];
  if (!current) {
    current = {
      apps: ref<AppRecord[]>([]),
      deleteAppId: ref<null | string>(null),
      dirty: ref(false),
      expandedIds: ref<Set<string>>(new Set()),
      formEpoch: ref(0),
      hydrated: ref(false),
      loadError: ref(false),
      loading: ref(false),
      nodes: ref<MenuRecord[]>([]),
      roles: ref<RoleGrant[]>([]),
      searchQuery: ref(''),
      selectedAppId: ref<null | string>(null),
      selectedNodeId: ref<null | string>(null),
    };
    host[key] = current;
  }
  return current;
})();

const apps = menuState.apps;
const nodes = menuState.nodes;
const roles = menuState.roles;
const selectedAppId = menuState.selectedAppId;
const selectedNodeId = menuState.selectedNodeId;
const expandedIds = menuState.expandedIds;
const searchQuery = menuState.searchQuery;
const dirty = menuState.dirty;
const formEpoch = menuState.formEpoch;
const loading = menuState.loading;
const loadError = menuState.loadError;
const hydrated = menuState.hydrated;
const deleteAppId = menuState.deleteAppId;
let menuLoad: Promise<void> | null = null;

const dirtyWait = ref<((choice: DirtyChoice) => void) | null>(null);
const prefixWait = ref<((choice: PrefixChoice) => void) | null>(null);
const prefixPair = ref({ from: '', to: '' });

let saveHandler: (() => Promise<boolean>) | null = null;

function bySort<T extends { name: string; sort: number }>(list: T[]) {
  return [...list].sort((a, b) => a.sort - b.sort || a.name.localeCompare(b.name));
}

export function useMenuState() {
  const selectedApp = computed(
    () => apps.value.find((app) => app.id === selectedAppId.value) ?? null,
  );
  const selectedNode = computed(
    () => nodes.value.find((node) => node.id === selectedNodeId.value) ?? null,
  );

  const treeSource = computed(() => {
    const query = searchQuery.value.trim().toLowerCase();
    const kept = new Set<string>();
    for (const app of apps.value) {
      const appHit =
        !query ||
        app.name.toLowerCase().includes(query) ||
        app.code.toLowerCase().includes(query);
      const matched = filterTree(
        nodes.value,
        app.id,
        appHit ? '' : searchQuery.value,
      );
      if (!query || appHit || matched.length > 0) {
        for (const node of matched) kept.add(node.id);
      }
    }
    return nodes.value.filter((node) => kept.has(node.id));
  });

  const confirmOpen = computed(() => dirtyWait.value !== null);
  const prefixOpen = computed(() => prefixWait.value !== null);

  function buttonsOf(parentId: string) {
    return bySort(
      nodes.value.filter(
        (node) => node.parentId === parentId && node.type === 'button',
      ),
    );
  }

  function buttonCount(menuId: string) {
    return nodes.value.filter(
      (node) => node.parentId === menuId && node.type === 'button',
    ).length;
  }

  function rolesOf(menuId: string) {
    return roles.value.filter((role) => role.menuIds.includes(menuId));
  }

  function subtree(id: string) {
    const result: MenuRecord[] = [];
    const walk = (parentId: string) => {
      for (const child of nodes.value.filter((node) => node.parentId === parentId)) {
        result.push(child);
        walk(child.id);
      }
    };
    walk(id);
    return result;
  }

  function impactOf(id: string): MenuImpact {
    const rest = subtree(id);
    const ids = [id, ...rest.map((node) => node.id)];
    return {
      buttons: rest.filter((node) => node.type === 'button').length,
      children: rest.filter((node) => node.type !== 'button').length,
      roles: roles.value.filter((role) =>
        role.menuIds.some((menuId) => ids.includes(menuId)),
      ).length,
    };
  }

  function effectivelyDisabled(node: MenuRecord) {
    const app = apps.value.find((item) => item.id === node.appId);
    if (app?.status === 'disabled') return true;
    let current: MenuRecord | undefined = node;
    const guard = new Set<string>();
    while (current) {
      if (guard.has(current.id)) break;
      guard.add(current.id);
      if (current.status === 'disabled') return true;
      current = current.parentId
        ? nodes.value.find((item) => item.id === current?.parentId)
        : undefined;
    }
    return false;
  }

  function parentPrefix(parentId: null | string, appId: string) {
    if (!parentId) {
      return apps.value.find((app) => app.id === appId)?.code ?? '';
    }
    return nodes.value.find((node) => node.id === parentId)?.permission ?? '';
  }

  function rememberSelection() {
    const current = nodes.value.find(
      (node) => node.id === selectedNodeId.value && node.type !== 'button',
    );
    if (current) {
      selectedNodeId.value = current.id;
      selectedAppId.value = current.appId;
    } else {
      selectedNodeId.value = null;
    }
    if (!apps.value.some((app) => app.id === selectedAppId.value)) {
      selectedAppId.value =
        apps.value.find((app) => app.status === 'enabled')?.id ??
        apps.value[0]?.id ??
        null;
    }
  }

  async function syncFromServer() {
    try {
      const split = splitMenus(await menuApi.all());
      apps.value = split.apps;
      nodes.value = split.nodes;
      rememberSelection();
    } catch {
      loadError.value = true;
    }
  }

  async function load() {
    if (menuLoad) return menuLoad;
    menuLoad = (async () => {
      loading.value = true;
      loadError.value = false;
      try {
        const split = splitMenus(await menuApi.all());
        apps.value = split.apps;
        nodes.value = split.nodes;
        roles.value = [];
        hydrated.value = true;
        rememberSelection();
        expandedIds.value = new Set([
          ...apps.value.map((app) => app.id),
          ...nodes.value
            .filter((node) => node.type === 'folder')
            .map((node) => node.id),
        ]);
      } catch {
        loadError.value = true;
      } finally {
        loading.value = false;
        menuLoad = null;
      }
    })();
    return menuLoad;
  }

  function ensureMenusLoaded() {
    if (hydrated.value) return Promise.resolve();
    return load();
  }

  function registerSave(handler: (() => Promise<boolean>) | null) {
    saveHandler = handler;
  }

  function settleDirty(choice: DirtyChoice) {
    const wait = dirtyWait.value;
    dirtyWait.value = null;
    wait?.(choice);
  }

  function settlePrefix(choice: PrefixChoice) {
    const wait = prefixWait.value;
    prefixWait.value = null;
    wait?.(choice);
  }

  async function guardDirty(): Promise<DirtyChoice> {
    if (!dirty.value) return 'discard';
    const choice = await new Promise<DirtyChoice>((resolve) => {
      dirtyWait.value = resolve;
    });
    if (choice === 'cancel') return 'cancel';
    if (choice === 'save') {
      const ok = (await saveHandler?.()) ?? false;
      if (!ok) return 'cancel';
      return 'save';
    }
    dirty.value = false;
    formEpoch.value += 1;
    return 'discard';
  }

  function askPrefix(from: string, to: string) {
    prefixPair.value = { from, to };
    return new Promise<PrefixChoice>((resolve) => {
      prefixWait.value = resolve;
    });
  }

  async function selectApp(id: string) {
    if (!apps.value.some((app) => app.id === id)) return false;
    if (id === selectedAppId.value && !selectedNodeId.value) return true;
    const choice = await guardDirty();
    if (choice === 'cancel') return false;
    selectedAppId.value = id;
    selectedNodeId.value = null;
    expandedIds.value.add(id);
    return true;
  }

  async function selectNode(id: string) {
    if (id === selectedNodeId.value) return true;
    const node = nodes.value.find((item) => item.id === id);
    if (!node || node.type === 'button') return false;
    const choice = await guardDirty();
    if (choice === 'cancel') return false;
    selectedNodeId.value = id;
    selectedAppId.value = node.appId;
    expandedIds.value.add(node.appId);
    for (const ancestor of ancestorIds(nodes.value, id)) {
      expandedIds.value.add(ancestor);
    }
    return true;
  }

  function setDirty(value: boolean) {
    dirty.value = value;
  }

  function toggleExpand(id: string) {
    const next = new Set(expandedIds.value);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    expandedIds.value = next;
  }

  function expandAll() {
    const next = new Set<string>();
    for (const app of apps.value) next.add(app.id);
    for (const node of nodes.value) {
      if (node.type === 'folder') next.add(node.id);
    }
    expandedIds.value = next;
  }

  function collapseAll() {
    expandedIds.value = new Set();
  }

  function isExpanded(id: string) {
    return expandedIds.value.has(id);
  }

  function reindex(parentId: null | string, appId: string) {
    childrenOf(nodes.value, appId, parentId).forEach((node, index) => {
      node.sort = (index + 1) * 10;
    });
  }

  async function persistPlacement(
    groups: { appId: string; parentId: null | string }[],
  ) {
    const seen = new Set<string>();
    const items: { id: string; parentId: number | string; sort: number }[] = [];
    for (const group of groups) {
      const key = `${group.appId}:${group.parentId ?? ''}`;
      if (seen.has(key)) continue;
      seen.add(key);
      childrenOf(nodes.value, group.appId, group.parentId).forEach((node, index) => {
        node.sort = (index + 1) * 10;
        items.push({
          id: node.id,
          parentId: group.parentId ?? group.appId,
          sort: node.sort,
        });
      });
    }
    if (items.length === 0) return;
    await menuApi.sort({ items });
  }

  function writeOf(node: MenuRecord) {
    if (node.type === 'button') return buttonWrite(node);
    if (node.type === 'folder') return { ...folderUpdate(node), permission: node.permission };
    return functionUpdate(node);
  }

  async function createNode(
    parentId: null | string,
    type: 'folder' | 'function',
    name: string,
  ) {
    let appId = selectedAppId.value;
    if (parentId) {
      const parent = nodes.value.find((item) => item.id === parentId);
      if (!parent || parent.type !== 'folder') return null;
      appId = parent.appId;
    }
    if (!appId) return null;
    const siblings = childrenOf(nodes.value, appId, parentId);
    const created: MenuRecord = {
      apis: [],
      appId,
      cache: false,
      component: '',
      external: false,
      icon: '',
      id: '',
      name,
      parentId,
      path: '',
      permission: '',
      remark: '',
      sort: (siblings.at(-1)?.sort ?? 0) + 10,
      status: 'enabled',
      type,
      visible: true,
    };
    try {
      const saved = await menuApi.create(nodeCreate(created));
      created.id = String(saved.id);
    } catch {
      return null;
    }
    nodes.value.push(created);
    expandedIds.value.add(appId);
    if (parentId) expandedIds.value.add(parentId);
    selectedAppId.value = appId;
    selectedNodeId.value = created.id;
    return created;
  }

  function valuesFromNode(node: MenuRecord): BasicFormValues {
    return {
      cache: node.cache,
      component: node.component,
      external: node.external,
      icon: node.icon,
      name: node.name,
      path: node.path,
      permission: node.permission,
      remark: node.remark,
      sort: node.sort,
      status: node.status,
      type: node.type === 'folder' ? 'folder' : 'function',
      visible: node.visible,
    };
  }

  function applyValues(node: MenuRecord, values: BasicFormValues) {
    node.cache = values.cache;
    node.component = values.type === 'function' ? values.component : '';
    node.external = values.external;
    node.icon = values.icon;
    node.name = values.name.trim();
    node.path = values.path.trim();
    node.permission = values.permission.trim();
    node.remark = values.remark;
    node.sort = values.sort;
    node.status = values.status;
    node.type = values.type;
    node.visible = values.visible;
  }

  async function saveBasic(id: string, values: BasicFormValues) {
    const node = nodes.value.find((item) => item.id === id);
    const appId = node?.appId;
    if (!node || !appId || node.type === 'button') return false;
    if (node.type === 'folder') {
      const previous = { icon: node.icon, name: node.name, visible: node.visible };
      node.icon = values.icon.trim();
      node.name = values.name.trim();
      node.visible = values.visible;
      try {
        await menuApi.update(node.id, folderUpdate(node));
      } catch {
        Object.assign(node, previous);
        return false;
      }
      dirty.value = false;
      return true;
    }
    if (permissionTaken(nodes.value, appId, values.permission, id)) return false;
    const oldPerm = node.permission;
    const nextPerm = values.permission.trim();
    const affected = buttonsOf(id).filter(
      (button) => oldPerm && button.permission.startsWith(`${oldPerm}:`),
    );
    let sync = false;
    if (oldPerm && nextPerm !== oldPerm && affected.length > 0) {
      const choice = await askPrefix(oldPerm, nextPerm);
      if (choice === 'abort') return false;
      sync = choice === 'sync';
    }
    applyValues(node, { ...values, type: node.type });
    if (sync) {
      for (const button of affected) {
        button.permission = replacePrefix(button.permission, oldPerm, nextPerm);
      }
    }
    try {
      await menuApi.update(node.id, functionUpdate(node));
      if (sync) {
        for (const button of affected) {
          await menuApi.update(button.id, buttonWrite(button));
        }
      }
    } catch {
      await syncFromServer();
      return false;
    }
    dirty.value = false;
    return true;
  }

  async function removeNode(id: string) {
    const target = nodes.value.find((node) => node.id === id);
    const parentId = target?.parentId ?? null;
    const previous = nodes.value;
    const previousSelection = selectedNodeId.value;
    const ids = new Set([id, ...subtree(id).map((node) => node.id)]);
    nodes.value = nodes.value.filter((node) => !ids.has(node.id));
    for (const role of roles.value) {
      role.menuIds = role.menuIds.filter((menuId) => !ids.has(menuId));
    }
    if (selectedNodeId.value && ids.has(selectedNodeId.value)) {
      const parent = parentId
        ? nodes.value.find(
            (node) => node.id === parentId && node.type !== 'button',
          )
        : null;
      selectedNodeId.value = parent?.id ?? null;
    }
    dirty.value = false;
    try {
      await menuApi.delete(id);
      return true;
    } catch {
      nodes.value = previous;
      selectedNodeId.value = previousSelection;
      return false;
    }
  }

  async function toggleNodeStatus(id: string) {
    const node = nodes.value.find((item) => item.id === id);
    if (!node || node.type === 'button') return;
    const prev = node.status;
    node.status = prev === 'enabled' ? 'disabled' : 'enabled';
    try {
      await menuApi.update(node.id, writeOf(node));
      return prev;
    } catch {
      node.status = prev;
    }
  }

  async function moveNode(
    id: string,
    parentId: null | string,
    index: number,
    targetAppId?: string,
  ): Promise<
    | { ok: false; reason: 'persist' | DropIssue }
    | { newPrefix: string; ok: true; oldPrefix: string; shouldAsk: boolean }
  > {
    const node = nodes.value.find((item) => item.id === id);
    if (!node || node.type === 'button') {
      return { ok: false as const, reason: 'self' as const };
    }
    const issue = dropIssue(nodes.value, id, parentId);
    if (issue) return { ok: false as const, reason: issue };
    let nextAppId = node.appId;
    if (parentId) {
      const parent = nodes.value.find((item) => item.id === parentId);
      if (!parent || parent.type !== 'folder') {
        return { ok: false as const, reason: 'into-function' as const };
      }
      nextAppId = parent.appId;
    } else if (targetAppId) {
      if (!apps.value.some((app) => app.id === targetAppId)) {
        return { ok: false as const, reason: 'self' as const };
      }
      nextAppId = targetAppId;
    }
    const previous = nodes.value.map((item) => ({ ...item, apis: [...item.apis] }));
    const oldParent = node.parentId;
    const oldApp = node.appId;
    const oldPrefix = parentPrefix(oldParent, oldApp);
    const newPrefix = parentPrefix(parentId, nextAppId);
    const descendants = subtree(id);
    node.parentId = parentId;
    if (oldApp !== nextAppId) {
      node.appId = nextAppId;
      for (const child of descendants) child.appId = nextAppId;
    }
    const ordered = childrenOf(nodes.value, nextAppId, parentId).filter(
      (item) => item.id !== id,
    );
    ordered.splice(Math.max(0, index), 0, node);
    ordered.forEach((item, order) => {
      item.sort = (order + 1) * 10;
    });
    if (oldParent !== parentId || oldApp !== nextAppId) reindex(oldParent, oldApp);
    if (parentId) expandedIds.value.add(parentId);
    else expandedIds.value.add(nextAppId);
    const groups = [{ appId: nextAppId, parentId }];
    if (oldParent !== parentId || oldApp !== nextAppId) {
      groups.push({ appId: oldApp, parentId: oldParent });
    }
    try {
      await persistPlacement(groups);
    } catch {
      nodes.value = previous;
      return { ok: false as const, reason: 'persist' as const };
    }
    return {
      newPrefix,
      ok: true as const,
      oldPrefix,
      shouldAsk:
        (oldParent !== parentId || oldApp !== nextAppId) &&
        !!oldPrefix &&
        oldPrefix !== newPrefix &&
        (node.permission.startsWith(`${oldPrefix}:`) ||
          node.permission === oldPrefix ||
          buttonsOf(id).some((button) =>
            button.permission.startsWith(`${oldPrefix}:`),
          )),
    };
  }

  async function moveApp(id: string, index: number) {
    const ordered = [...apps.value].sort(
      (a, b) => a.sort - b.sort || a.name.localeCompare(b.name),
    );
    const current = ordered.findIndex((app) => app.id === id);
    if (current < 0) return;
    const previous = new Map(ordered.map((app) => [app.id, app.sort]));
    const [item] = ordered.splice(current, 1);
    if (!item) return;
    ordered.splice(Math.max(0, Math.min(index, ordered.length)), 0, item);
    ordered.forEach((app, order) => {
      app.sort = (order + 1) * 10;
    });
    try {
      await menuApi.sort({
        items: ordered.map((app) => ({ id: app.id, parentId: 0, sort: app.sort })),
      });
    } catch {
      for (const app of apps.value) {
        const sort = previous.get(app.id);
        if (sort != null) app.sort = sort;
      }
    }
  }

  async function finishMovePrefix(
    id: string,
    oldPrefix: string,
    newPrefix: string,
  ) {
    const choice = await askPrefix(oldPrefix, newPrefix);
    if (choice !== 'sync') return;
    const node = nodes.value.find((item) => item.id === id);
    if (!node) return;
    const changed: MenuRecord[] = [];
    const rewrite = (item: MenuRecord) => {
      const next = replacePrefix(item.permission, oldPrefix, newPrefix);
      if (next === item.permission) return;
      item.permission = next;
      changed.push(item);
    };
    rewrite(node);
    for (const button of buttonsOf(id)) rewrite(button);
    for (const child of subtree(id)) rewrite(child);
    try {
      for (const item of changed) {
        await menuApi.update(item.id, writeOf(item));
      }
    } catch {
      await syncFromServer();
    }
  }

  async function shift(id: string, delta: number) {
    const node = nodes.value.find((item) => item.id === id);
    if (!node) return;
    const siblings = childrenOf(nodes.value, node.appId, node.parentId);
    const index = siblings.findIndex((item) => item.id === id);
    const next = index + delta;
    if (index < 0 || next < 0 || next >= siblings.length) return;
    await moveNode(id, node.parentId, next);
  }

  async function duplicateNode(id: string, copyLabel: string) {
    const source = nodes.value.find((node) => node.id === id);
    if (!source || source.type === 'button') return null;
    let createdId = '';
    try {
      const walk = async (item: MenuRecord, parentId: null | string, root: boolean) => {
        let permission = item.permission;
        let path = item.path;
        if (permission) {
          let candidate = `${permission}:copy`;
          while (permissionTaken(nodes.value, item.appId, candidate)) {
            candidate = `${candidate}:copy`;
          }
          permission = candidate;
        }
        if (path && item.type !== 'button') {
          let candidate = `${path}-copy`;
          while (
            nodes.value.some(
              (node) =>
                node.appId === item.appId &&
                node.type !== 'button' &&
                node.path === candidate,
            )
          ) {
            candidate = `${candidate}-copy`;
          }
          path = candidate;
        }
        const children = nodes.value.filter((node) => node.parentId === item.id);
        const draft: MenuRecord = {
          ...structuredClone(item),
          id: '',
          name: root ? `${item.name}${copyLabel}` : item.name,
          parentId,
          path,
          permission,
        };
        const saved = await menuApi.create(nodeCreate(draft));
        draft.id = String(saved.id);
        nodes.value.push(draft);
        if (root) createdId = draft.id;
        for (const child of children) {
          await walk(child, draft.id, false);
        }
      };
      await walk(source, source.parentId, true);
      if (createdId) selectedNodeId.value = createdId;
      return nodes.value.find((node) => node.id === createdId) ?? null;
    } catch {
      await syncFromServer();
      return null;
    }
  }

  async function upsertApp(input: Omit<AppRecord, 'id'> & { id?: string }) {
    const code = input.code.trim();
    const codeTaken =
      code !== '' &&
      apps.value.some((app) => app.code === code && app.id !== input.id);
    if (codeTaken) return { error: 'code' as const, ok: false as const };
    if (input.id) {
      const current = apps.value.find((app) => app.id === input.id);
      if (!current) return { error: 'missing' as const, ok: false as const };
      const previous = { ...current };
      Object.assign(current, { ...input, code, name: input.name.trim() });
      try {
        await menuApi.update(current.id, moduleUpdate(current));
      } catch {
        Object.assign(current, previous);
        return { error: 'save' as const, ok: false as const };
      }
      return { id: current.id, ok: true as const };
    }
    const created: AppRecord = {
      ...input,
      code,
      id: '',
      name: input.name.trim(),
    };
    try {
      const saved = await menuApi.create(moduleCreate(created));
      created.id = String(saved.id);
    } catch {
      return { error: 'save' as const, ok: false as const };
    }
    apps.value.push(created);
    selectedAppId.value = created.id;
    selectedNodeId.value = null;
    return { id: created.id, ok: true as const };
  }

  async function toggleAppStatus(id: string) {
    const app = apps.value.find((item) => item.id === id);
    if (!app) return;
    const prev = app.status;
    app.status = prev === 'enabled' ? 'disabled' : 'enabled';
    try {
      await menuApi.update(app.id, moduleUpdate(app));
      return prev;
    } catch {
      app.status = prev;
    }
  }

  async function duplicateApp(id: string, copyLabel: string) {
    const source = apps.value.find((app) => app.id === id);
    if (!source) return null;
    let code = source.code ? `${source.code}_copy` : '';
    while (code && apps.value.some((app) => app.code === code)) code = `${code}_copy`;
    const sort = apps.value.reduce((max, app) => Math.max(max, app.sort), 0) + 10;
    const created: AppRecord = {
      ...structuredClone(source),
      code,
      id: '',
      name: `${source.name}${copyLabel}`,
      sort,
    };
    try {
      const saved = await menuApi.create(moduleCreate(created));
      created.id = String(saved.id);
      apps.value.push(created);
      const copyLevel = async (oldParent: null | string, newParent: null | string) => {
        const children = nodes.value.filter(
          (node) => node.appId === id && node.parentId === oldParent,
        );
        for (const child of children) {
          let path = child.path;
          if (
            path &&
            child.type !== 'button' &&
            nodes.value.some((node) => node.type !== 'button' && node.path === path)
          ) {
            path = `${path}-copy`;
          }
          const clone: MenuRecord = {
            ...structuredClone(child),
            appId: created.id,
            id: '',
            parentId: newParent,
            path,
            permission: replacePrefix(child.permission, source.code, code),
          };
          const copied = await menuApi.create(nodeCreate(clone));
          clone.id = String(copied.id);
          nodes.value.push(clone);
          await copyLevel(child.id, clone.id);
        }
      };
      await copyLevel(null, null);
      selectedAppId.value = created.id;
      selectedNodeId.value = null;
      expandedIds.value.add(created.id);
      return created;
    } catch {
      await syncFromServer();
      return null;
    }
  }

  async function createApp(name: string) {
    const sort = apps.value.reduce((max, app) => Math.max(max, app.sort), 0) + 10;
    const created: AppRecord = {
      code: '',
      entry: '',
      icon: '',
      id: '',
      name,
      sort,
      status: 'enabled',
    };
    try {
      const saved = await menuApi.create(moduleCreate(created));
      created.id = String(saved.id);
    } catch {
      return null;
    }
    apps.value.push(created);
    selectedAppId.value = created.id;
    selectedNodeId.value = null;
    expandedIds.value.add(created.id);
    return created;
  }

  async function removeApp(id: string) {
    const previousApps = apps.value;
    const previousNodes = nodes.value;
    const previousAppId = selectedAppId.value;
    const previousNodeId = selectedNodeId.value;
    const removingSelection =
      selectedAppId.value === id ||
      nodes.value.some(
        (node) => node.id === selectedNodeId.value && node.appId === id,
      );
    const ids = new Set(
      nodes.value.filter((node) => node.appId === id).map((node) => node.id),
    );
    nodes.value = nodes.value.filter((node) => node.appId !== id);
    apps.value = apps.value.filter((app) => app.id !== id);
    for (const role of roles.value) {
      role.menuIds = role.menuIds.filter((menuId) => !ids.has(menuId));
    }
    if (removingSelection) {
      selectedAppId.value = apps.value[0]?.id ?? null;
      selectedNodeId.value = null;
      dirty.value = false;
    }
    try {
      await menuApi.delete(id);
      return true;
    } catch {
      apps.value = previousApps;
      nodes.value = previousNodes;
      selectedAppId.value = previousAppId;
      selectedNodeId.value = previousNodeId;
      return false;
    }
  }

  function askRemoveApp(id: string) {
    deleteAppId.value = id;
  }

  function appImpact(id: string) {
    const list = nodes.value.filter((node) => node.appId === id);
    return {
      buttons: list.filter((node) => node.type === 'button').length,
      menus: list.filter((node) => node.type !== 'button').length,
    };
  }

  function saveButtons(parentId: string, next: MenuRecord[]) {
    return replaceButtons(parentId, next);
  }

  async function replaceButtons(parentId: string, incoming: MenuRecord[]) {
    const parent = nodes.value.find((node) => node.id === parentId);
    if (!parent) return false;
    const current = nodes.value.filter(
      (node) => node.parentId === parentId && node.type === 'button',
    );
    const kept = nodes.value.filter(
      (node) => !(node.parentId === parentId && node.type === 'button'),
    );
    nodes.value = [...kept, ...incoming];
    try {
      const incomingIds = new Set(incoming.map((row) => row.id));
      for (const row of current) {
        if (!incomingIds.has(row.id)) await menuApi.delete(row.id);
      }
      for (const row of incoming) {
        const prev = current.find((item) => item.id === row.id);
        if (!prev) {
          const saved = await menuApi.create(nodeCreate(row));
          row.id = String(saved.id);
        } else if (
          prev.name !== row.name ||
          prev.permission !== row.permission ||
          prev.status !== row.status ||
          prev.sort !== row.sort
        ) {
          await menuApi.update(row.id, buttonWrite(row));
        }
      }
      return true;
    } catch {
      await syncFromServer();
      return false;
    }
  }

  return {
    appImpact,
    apps,
    askPrefix,
    askRemoveApp,
    buttonCount,
    buttonsOf,
    collapseAll,
    confirmOpen,
    createApp,
    createNode,
    deleteAppId,
    dirty,
    duplicateApp,
    duplicateNode,
    effectivelyDisabled,
    ensureMenusLoaded,
    expandAll,
    expandedIds,
    finishMovePrefix,
    formEpoch,
    guardDirty,
    impactOf,
    isExpanded,
    load,
    loadError,
    loading,
    moveApp,
    moveNode,
    nodes,
    parentPrefix,
    permissionTaken: (permission: string, exceptId?: string) =>
      permissionTaken(
        nodes.value,
        selectedAppId.value ?? '',
        permission,
        exceptId,
      ),
    prefixOpen,
    prefixPair,
    registerSave,
    removeApp,
    removeNode,
    replaceButtons,
    rolesOf,
    saveBasic,
    saveButtons,
    searchQuery,
    selectApp,
    selectNode,
    selectedApp,
    selectedAppId,
    selectedNode,
    selectedNodeId,
    setDirty,
    settleDirty,
    settlePrefix,
    shift,
    toggleAppStatus,
    toggleExpand,
    toggleNodeStatus,
    treeSource,
    upsertApp,
    valuesFromNode,
  };
}
