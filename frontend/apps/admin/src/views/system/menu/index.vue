<script setup lang="ts">
import type { CatalogNode, MenuRelease, MenuScope, RoleMenus } from '#/api/system/menu-catalog';
import type { MatrixPackage } from '#/api/tenant/package-catalog';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { menuCatalogApi } from '#/api/system/menu-catalog';
import { packageCatalogApi } from '#/api/tenant/package-catalog';
import { Button } from '#/ui/button';
import { toast } from '#/ui-patterns/toast';
import { isBusinessError } from '#/utils/business-error';

import { canParent, createNode, moveNode, moveSibling, removeNode, restoreNode } from './catalog';
import MenuForm from './menu-form.vue';
import MenuPreview from './menu-preview.vue';
import MenuTree from './menu-tree.vue';

const scope = ref<MenuScope>('PLATFORM');
const nodes = ref<CatalogNode[]>([]);
const version = ref(1);
const dirty = ref(false);
const selectedId = ref<string>();
const saving = ref(false);
const history = ref<CatalogNode[][]>([]);
const roles = ref<RoleMenus[]>([]);
const packages = ref<MatrixPackage[]>([]);
const releases = ref<MenuRelease[]>([]);
const roleId = ref('');
const packageIds = ref<string[]>([]);
const publishOpen = ref(false);
const removeOpen = ref(false);
const changes = ref<string[]>([]);
const affectedRoles = ref<string[]>([]);
const publishing = ref(false);

const selected = computed(() => nodes.value.find((node) => node.id === selectedId.value));

function fail(error: unknown) {
  if (isBusinessError(error)) {
    toast.error(error.message || '操作失败');
    if (error.code === 'MENU_VERSION_CONFLICT') void load();
  }
}

async function load() {
  saving.value = true;
  try {
    const workspace = await menuCatalogApi.workspace(scope.value);
    nodes.value = workspace.nodes;
    version.value = workspace.version;
    dirty.value = workspace.dirty;
    history.value = [];
    if (!nodes.value.some((node) => node.id === selectedId.value)) {
      selectedId.value = nodes.value.find((node) => node.type !== 3)?.id;
    }
    const [roleRows, releaseRows] = await Promise.all([
      menuCatalogApi.roles(scope.value),
      menuCatalogApi.releases(scope.value),
    ]);
    roles.value = roleRows;
    releases.value = releaseRows;
    if (scope.value === 'TENANT') {
      packages.value = (await packageCatalogApi.matrix()).packages;
    }
  } catch (error) {
    fail(error);
  } finally {
    saving.value = false;
  }
}

async function persist(next: CatalogNode[], remember: boolean) {
  if (remember) history.value.push(nodes.value.map((node) => ({ ...node })));
  nodes.value = next;
  saving.value = true;
  try {
    const saved = await menuCatalogApi.save(scope.value, version.value, next);
    const previousIds = new Set((history.value.at(-1) ?? []).map((node) => node.id));
    nodes.value = saved.nodes;
    version.value = saved.version;
    dirty.value = saved.dirty;
    if (!saved.nodes.some((node) => node.id === selectedId.value)) {
      selectedId.value = saved.nodes.find((node) => node.type !== 3 && !previousIds.has(node.id))?.id
        ?? saved.nodes.find((node) => node.type !== 3)?.id;
    }
  } catch (error) {
    fail(error);
    await load();
  } finally {
    saving.value = false;
  }
}

function apply(next: CatalogNode[]) {
  return persist(next, true);
}

function patch(value: Partial<CatalogNode>) {
  const current = selected.value;
  if (!current) return;
  if (value.parentId && value.parentId !== current.parentId) {
    if (!canParent(nodes.value, current, value.parentId)) {
      toast.error('无法移动到该位置');
      return;
    }
    return apply(moveNode(nodes.value, current.id, value.parentId, 999));
  }
  if (value.type != null && value.type !== current.type) {
    if (current.published) return;
    const hasChild = nodes.value.some(
      (item) => item.type !== 3 && item.parentId === current.id && !item.pendingDelete,
    );
    if (value.type === 2 && hasChild) {
      toast.error('请先移走子菜单后再改为页面');
      return;
    }
    const next = nodes.value
      .filter((item) => value.type !== 1 || item.type !== 3 || item.parentId !== current.id)
      .map((node) => (
        node.id === current.id
          ? { ...node, ...value, permission: value.type === 1 ? '' : node.permission }
          : node
      ));
    return apply(next);
  }
  return apply(nodes.value.map((node) => (node.id === current.id ? { ...node, ...value } : node)));
}

async function undo() {
  const previous = history.value.pop();
  if (!previous) return;
  await persist(previous, false);
}

async function discard() {
  saving.value = true;
  try {
    const saved = await menuCatalogApi.discard(scope.value, version.value);
    nodes.value = saved.nodes;
    version.value = saved.version;
    dirty.value = saved.dirty;
    history.value = [];
    toast.success('已放弃未发布修改');
  } catch (error) {
    fail(error);
  } finally {
    saving.value = false;
  }
}

async function openPublish() {
  publishing.value = true;
  try {
    const result = await menuCatalogApi.publish(scope.value, version.value, false, true);
    changes.value = result.changes ?? [];
    affectedRoles.value = result.affectedRoleNames ?? [];
    publishOpen.value = true;
  } catch (error) {
    fail(error);
  } finally {
    publishing.value = false;
  }
}

async function confirmPublish() {
  publishing.value = true;
  try {
    const result = await menuCatalogApi.publish(scope.value, version.value, true, false);
    if (result.workspace) {
      nodes.value = result.workspace.nodes ?? nodes.value;
      version.value = result.workspace.version;
      dirty.value = result.workspace.dirty;
    }
    history.value = [];
    publishOpen.value = false;
    releases.value = await menuCatalogApi.releases(scope.value);
    toast.success('已发布');
  } catch (error) {
    fail(error);
  } finally {
    publishing.value = false;
  }
}

async function rollback(release: MenuRelease) {
  saving.value = true;
  try {
    const saved = await menuCatalogApi.rollback(scope.value, version.value, release.id);
    nodes.value = saved.nodes;
    version.value = saved.version;
    dirty.value = saved.dirty;
    toast.success('已放回草稿，确认后发布');
  } catch (error) {
    fail(error);
  } finally {
    saving.value = false;
  }
}

function confirmRemove() {
  const node = selected.value;
  if (!node) return;
  removeOpen.value = false;
  void apply(removeNode(nodes.value, node.id));
}

function setButtons(next: CatalogNode[]) {
  const page = selected.value;
  if (!page) return;
  const rest = nodes.value.filter((node) => !(node.type === 3 && node.parentId === page.id));
  return apply([...rest, ...next]);
}

function addMenu() {
  const parentId = selected.value?.type === 1 ? selected.value.id : '0';
  const created = createNode(parentId);
  selectedId.value = created.id;
  return apply([...nodes.value, created]);
}

function switchScope(next: MenuScope) {
  if (scope.value === next) return;
  scope.value = next;
  selectedId.value = undefined;
  roleId.value = '';
  packageIds.value = [];
  void load();
}

onMounted(load);
</script>

<template>
  <Page auto-content-height>
    <div class="flex h-full min-h-0 flex-col gap-3">
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div class="inline-flex rounded-lg bg-muted p-1">
          <button
            v-for="item in [
              { label: '平台菜单', value: 'PLATFORM' },
              { label: '租户菜单', value: 'TENANT' },
            ]"
            :key="item.value"
            :class="scope === item.value ? 'bg-background shadow-sm' : 'text-muted-foreground'"
            class="rounded-md px-4 py-1.5 text-sm"
            type="button"
            @click="switchScope(item.value as MenuScope)"
          >
            {{ item.label }}
          </button>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <span class="text-sm text-muted-foreground">{{ dirty ? '有未发布修改' : '已是最新' }}</span>
          <Button :disabled="history.length === 0 || saving" type="button" variant="outline" @click="undo">
            撤销
          </Button>
          <Button :disabled="!dirty || saving" type="button" variant="outline" @click="discard">
            放弃修改
          </Button>
          <Button :disabled="!dirty || saving" :loading="publishing" type="button" @click="openPublish">
            发布
          </Button>
        </div>
      </header>

      <div class="grid min-h-0 flex-1 grid-cols-1 gap-3 xl:grid-cols-[280px_minmax(0,1fr)_280px]">
        <MenuTree
          :nodes="nodes"
          :selected-id="selectedId"
          @create="addMenu"
          @move="(id, parentId, index) => apply(moveNode(nodes, id, parentId, index))"
          @select="selectedId = $event"
        />
        <MenuForm
          :node="selected"
          :nodes="nodes"
          @buttons="setButtons"
          @change="patch"
          @move="(direction) => selected && apply(moveSibling(nodes, selected.id, direction))"
          @remove="removeOpen = true"
          @restore="selected && apply(restoreNode(nodes, selected.id))"
        />
        <MenuPreview
          v-model:package-ids="packageIds"
          v-model:role-id="roleId"
          :nodes="nodes"
          :packages="packages"
          :roles="roles"
          :scope="scope"
        />
      </div>

      <div v-if="releases.length" class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
        <span>版本记录</span>
        <button
          v-for="release in releases.slice(0, 8)"
          :key="release.id"
          class="rounded-md border border-border px-2 py-1 hover:bg-accent"
          type="button"
          @click="rollback(release)"
        >
          v{{ release.version }}
        </button>
      </div>
    </div>

    <div
      v-if="removeOpen && selected"
      class="fixed inset-0 z-50 flex items-center justify-center bg-overlay p-4"
      @click.self="removeOpen = false"
    >
      <div class="w-full max-w-sm rounded-xl border border-border bg-card p-5">
        <h3 class="text-base font-semibold">删除「{{ selected.name }}」</h3>
        <p class="mt-2 text-sm text-muted-foreground">
          {{ selected.published ? '将标记为待删除，发布后生效。发布前可以恢复。' : '这个菜单还没发布过，会直接从草稿里移除。' }}
        </p>
        <div class="mt-5 flex justify-end gap-2">
          <Button type="button" variant="outline" @click="removeOpen = false">取消</Button>
          <Button
            type="button"
            @click="confirmRemove"
          >
            删除
          </Button>
        </div>
      </div>
    </div>

    <div
      v-if="publishOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-overlay p-4"
      @click.self="publishOpen = false"
    >
      <div class="w-full max-w-md rounded-xl border border-border bg-card p-5">
        <h3 class="text-base font-semibold">发布菜单</h3>
        <p v-if="changes.length === 0" class="mt-3 text-sm text-muted-foreground">没有可发布的改动。</p>
        <ul v-else class="mt-3 max-h-64 list-disc space-y-1 overflow-auto pl-5 text-sm">
          <li v-for="change in changes" :key="change">{{ change }}</li>
        </ul>
        <p v-if="affectedRoles.length" class="mt-3 text-sm text-destructive">
          将影响角色：{{ affectedRoles.join('、') }}。确认后这些角色不再拥有被删除的菜单。
        </p>
        <div class="mt-5 flex justify-end gap-2">
          <Button type="button" variant="outline" @click="publishOpen = false">取消</Button>
          <Button :disabled="changes.length === 0" :loading="publishing" type="button" @click="confirmPublish">
            确认发布
          </Button>
        </div>
      </div>
    </div>
  </Page>
</template>
