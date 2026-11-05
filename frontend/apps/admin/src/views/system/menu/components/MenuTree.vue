<script setup lang="ts">
/* global HTMLElement, HTMLInputElement, KeyboardEvent, window */
import type { MenuRecord } from '../types';

import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { useUserStore } from '#/store/user';

import { ChevronRight, Plus, Search } from '@vben/icons';
import { $t } from '@vben/locales';

import { useSortable } from '@vben-core/composables';

import {
  AlertDialog,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogTitle,
} from '#/ui/alert-dialog';
import { Button } from '#/ui/button';
import { Tooltip, TooltipContent, TooltipTrigger } from '#/ui/tooltip';

import { toast } from '#/ui-patterns/toast';


import { childrenOf } from '../composables/useMenuTree';
import { useMenuState } from '../composables/useMenuState';
import AppTreeNode from './AppTreeNode.vue';

const props = defineProps<{ coarse: boolean }>();
const emit = defineEmits<{ open: [] }>();

const userStore = useUserStore();
const state = useMenuState();
const searchOpen = ref(false);
const searchRef = ref<HTMLInputElement | null>(null);
const listRef = ref<HTMLElement | null>(null);
let sortable: { destroy: () => void } | null = null;

const pendingDelete = ref<MenuRecord | null>(null);

const canCreate = computed(
  () =>
    userStore.hasPermission('system:menu:create') ||
    userStore.hasPermission('*:*:*'),
);

const visibleApps = computed(() => {
  const query = state.searchQuery.value.trim().toLowerCase();
  return [...state.apps.value]
    .sort((a, b) => a.sort - b.sort || a.name.localeCompare(b.name))
    .filter((app) => {
      if (!query) return true;
      if (app.name.toLowerCase().includes(query) || app.code.toLowerCase().includes(query)) {
        return true;
      }
      return state.treeSource.value.some((node) => node.appId === app.id);
    });
});

const allOpen = computed(() => {
  const ids = [
    ...state.apps.value.map((app) => app.id),
    ...state.nodes.value
      .filter((node) => node.type === 'folder')
      .map((node) => node.id),
  ];
  return ids.length > 0 && ids.every((id) => state.isExpanded(id));
});

const deleteImpact = computed(() =>
  pendingDelete.value ? state.impactOf(pendingDelete.value.id) : null,
);

type TreeRow =
  | { id: string; kind: 'module' }
  | {
      appId: string;
      id: string;
      kind: 'node';
      parentId: null | string;
      type: MenuRecord['type'];
    };

function visibleRows() {
  const rows: TreeRow[] = [];
  const searching = !!state.searchQuery.value.trim();
  for (const app of visibleApps.value) {
    rows.push({ id: app.id, kind: 'module' });
    if (!searching && !state.isExpanded(app.id)) continue;
    const walk = (parentId: null | string) => {
      for (const node of childrenOf(state.treeSource.value, app.id, parentId)) {
        rows.push({
          appId: app.id,
          id: node.id,
          kind: 'node',
          parentId: node.parentId,
          type: node.type,
        });
        const opened =
          searching ||
          (node.type === 'folder' && state.isExpanded(node.id)) ||
          (node.type === 'function' &&
            state.isExpanded(node.id) &&
            childrenOf(state.treeSource.value, node.appId, node.id).length > 0);
        if (opened) walk(node.id);
      }
    };
    walk(null);
  }
  return rows;
}

async function selectRow(row: TreeRow) {
  const ok =
    row.kind === 'module'
      ? await state.selectApp(row.id)
      : await state.selectNode(row.id);
  if (ok) emit('open');
}

async function onKeydown(event: KeyboardEvent) {
  const rows = visibleRows();
  const index = state.selectedNodeId.value
    ? rows.findIndex(
        (row) => row.kind === 'node' && row.id === state.selectedNodeId.value,
      )
    : rows.findIndex(
        (row) => row.kind === 'module' && row.id === state.selectedAppId.value,
      );
  const current = rows[index];
  const nextRow = rows[index + 1];
  const prevRow = rows[index - 1];
  if (event.key === 'ArrowDown' && nextRow) {
    event.preventDefault();
    await selectRow(nextRow);
  } else if (event.key === 'ArrowUp' && prevRow) {
    event.preventDefault();
    await selectRow(prevRow);
  } else if (event.key === 'ArrowRight' && current) {
    event.preventDefault();
    const expandable =
      current.kind === 'module' ||
      (current.kind === 'node' && current.type === 'folder');
    if (expandable) {
      if (!state.isExpanded(current.id)) state.toggleExpand(current.id);
      else {
        const child = childrenOf(
          state.treeSource.value,
          current.kind === 'module' ? current.id : current.appId,
          current.kind === 'module' ? null : current.id,
        )[0];
        if (child) {
          await selectRow({
            appId: child.appId,
            id: child.id,
            kind: 'node',
            parentId: child.parentId,
            type: child.type,
          });
        }
      }
    }
  } else if (event.key === 'ArrowLeft' && current) {
    event.preventDefault();
    const expandable =
      current.kind === 'module' ||
      (current.kind === 'node' && current.type === 'folder');
    if (expandable && state.isExpanded(current.id)) {
      state.toggleExpand(current.id);
    } else if (current.kind === 'node' && current.parentId) {
      await state.selectNode(current.parentId);
    } else if (current.kind === 'node') {
      await state.selectApp(current.appId);
    }
  }
}

function openSearch() {
  searchOpen.value = true;
  void nextTick(() => searchRef.value?.focus());
}

function closeSearch() {
  searchOpen.value = false;
  state.searchQuery.value = '';
}

function toggleAll() {
  if (allOpen.value) state.collapseAll();
  else state.expandAll();
}

function defaultName(type: 'folder' | 'function') {
  return $t(type === 'folder' ? 'appMenu.untitledDirectory' : 'appMenu.untitledMenu');
}

async function addApp() {
  const choice = await state.guardDirty();
  if (choice === 'cancel') return;
  const created = await state.createApp($t('appMenu.untitledApp'));
  if (created) emit('open');
}

async function addRoot(type: 'folder' | 'function') {
  if (!state.selectedAppId.value) return;
  const choice = await state.guardDirty();
  if (choice === 'cancel') return;
  const created = await state.createNode(null, type, defaultName(type));
  if (created) emit('open');
}

function restoreDom(evt: { from: HTMLElement; item: HTMLElement; oldIndex?: number }) {
  if (evt.oldIndex == null) return;
  evt.item.remove();
  evt.from.insertBefore(evt.item, evt.from.children[evt.oldIndex] ?? null);
}

async function mountSortable() {
  sortable?.destroy();
  sortable = null;
  if (props.coarse || !listRef.value || state.searchQuery.value.trim()) return;
  const { initializeSortable } = useSortable(listRef.value, {
    animation: 0,
    delay: 0,
    draggable: '.tree-app',
    filter: '.tree-node',
    group: { name: 'app-order', pull: false, put: false },
    preventOnFilter: false,
    handle: '.drag-handle',
    onEnd: (evt) => {
      const id = (evt.item as HTMLElement).dataset.id;
      const index = evt.newIndex ?? 0;
      restoreDom(evt as { from: HTMLElement; item: HTMLElement; oldIndex?: number });
      if (!id) return;
      void state.moveApp(id, index);
    },
  });
  sortable = await initializeSortable();
}

function onWindowKey(event: KeyboardEvent) {
  if (event.key !== '/' || event.metaKey || event.ctrlKey || event.altKey) return;
  const tag = (event.target as HTMLElement | null)?.tagName;
  if (tag === 'INPUT' || tag === 'TEXTAREA') return;
  event.preventDefault();
  openSearch();
}

async function confirmDelete() {
  if (!pendingDelete.value) return;
  const ok = await state.removeNode(pendingDelete.value.id);
  if (!ok) return;
  pendingDelete.value = null;
  toast.success($t('appMenu.removed'));
}

onMounted(() => {
  window.addEventListener('keydown', onWindowKey);
  void nextTick(mountSortable);
});
watch(
  () => [
    visibleApps.value.map((app) => app.id).join(),
    props.coarse,
    state.searchQuery.value,
  ],
  () => nextTick(mountSortable),
);
onBeforeUnmount(() => {
  window.removeEventListener('keydown', onWindowKey);
  sortable?.destroy();
});
</script>

<template>
  <div class="flex h-full min-h-0 flex-col">
    <div class="flex items-center gap-1 px-3 py-2">
      <Tooltip>
        <TooltipTrigger as-child>
          <button
            type="button"
            class="inline-flex size-6 items-center justify-center text-muted-foreground transition-colors duration-150 hover:text-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            :aria-label="searchOpen ? $t('appMenu.closeSearch') : $t('appMenu.search')"
            @click="searchOpen ? closeSearch() : openSearch()"
          >
            <Search
              class="size-4"
              :stroke-width="1.5"
            />
          </button>
        </TooltipTrigger>
        <TooltipContent>{{ $t('appMenu.search') }}</TooltipContent>
      </Tooltip>
      <input
        v-if="searchOpen"
        ref="searchRef"
        v-model="state.searchQuery.value"
        :aria-label="$t('appMenu.search')"
        class="h-7 min-w-0 flex-1 border-0 border-b border-border bg-transparent text-sm outline-none"
      >
      <Tooltip>
        <TooltipTrigger as-child>
          <button
            type="button"
            class="ml-auto inline-flex size-6 items-center justify-center text-muted-foreground transition-colors duration-150 hover:text-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            :aria-label="allOpen ? $t('appMenu.collapseAll') : $t('appMenu.expandAll')"
            @click="toggleAll"
          >
            <ChevronRight
              class="size-4"
              :class="allOpen ? 'rotate-90' : ''"
              :stroke-width="1.5"
            />
          </button>
        </TooltipTrigger>
        <TooltipContent>
          {{ allOpen ? $t('appMenu.collapseAll') : $t('appMenu.expandAll') }}
        </TooltipContent>
      </Tooltip>
    </div>

    <div
      v-if="state.loading.value"
      class="space-y-3 px-4 py-2"
    >
      <div
        v-for="row in 8"
        :key="row"
        class="h-3 animate-pulse rounded-md bg-muted"
      />
    </div>
    <div
      v-else-if="state.loadError.value"
      class="px-4 py-6 text-sm"
    >
      <p class="text-muted-foreground">
        {{ $t('appMenu.loadFailed') }}
      </p>
      <button
        type="button"
        class="mt-2 text-sm text-foreground underline-offset-4 hover:underline"
        @click="state.load()"
      >
        {{ $t('appMenu.retry') }}
      </button>
    </div>
    <div
      v-else
      class="min-h-0 flex-1 overflow-auto outline-none focus-visible:ring-1 focus-visible:ring-ring"
      tabindex="0"
      @keydown="onKeydown"
    >
      <p
        v-if="visibleApps.length === 0 && state.searchQuery.value.trim()"
        class="px-4 py-6 text-sm text-muted-foreground"
      >
        {{ $t('appMenu.emptySearch') }}
      </p>
      <ul
        v-else
        ref="listRef"
        role="tree"
        class="py-1"
        :aria-label="$t('appMenu.apps')"
      >
        <AppTreeNode
          v-for="app in visibleApps"
          :key="app.id"
          :app="app"
          :coarse="coarse"
          @open="emit('open')"
          @remove="pendingDelete = state.nodes.value.find((item) => item.id === $event) ?? null"
        />
      </ul>
    </div>

    <div
      v-if="canCreate"
      class="flex flex-wrap gap-x-4 gap-y-2 px-4 py-3"
    >
      <button
        type="button"
        class="inline-flex items-center gap-1 text-sm text-foreground/80 transition-colors duration-150 hover:text-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
        @click="addApp"
      >
        <Plus
          class="size-4"
          :stroke-width="1.5"
        />
        {{ $t('appMenu.addApp') }}
      </button>
      <button
        v-if="state.selectedAppId.value"
        type="button"
        class="inline-flex items-center gap-1 text-sm text-foreground/80 transition-colors duration-150 hover:text-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
        @click="addRoot('folder')"
      >
        <Plus
          class="size-4"
          :stroke-width="1.5"
        />
        {{ $t('appMenu.addDirectory') }}
      </button>
      <button
        v-if="state.selectedAppId.value"
        type="button"
        class="inline-flex items-center gap-1 text-sm text-foreground/80 transition-colors duration-150 hover:text-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
        @click="addRoot('function')"
      >
        <Plus
          class="size-4"
          :stroke-width="1.5"
        />
        {{ $t('appMenu.addMenu') }}
      </button>
    </div>

    <AlertDialog
      :open="!!pendingDelete"
      @update:open="(open) => !open && (pendingDelete = null)"
    >
      <AlertDialogContent class="shadow-none">
        <AlertDialogTitle class="text-base font-medium">
          {{ $t('appMenu.deleteMenuTitle', { name: pendingDelete?.name ?? '' }) }}
        </AlertDialogTitle>
        <AlertDialogDescription v-if="deleteImpact">
          {{
            $t('appMenu.deleteMenuBody', {
              buttons: deleteImpact.buttons,
              children: deleteImpact.children,
            })
          }}
        </AlertDialogDescription>
        <div class="mt-4 flex justify-end gap-2">
          <AlertDialogCancel type="button">
            {{ $t('appMenu.cancel') }}
          </AlertDialogCancel>
          <Button
            type="button"
            variant="ghost"
            class="text-destructive shadow-none"
            @click="confirmDelete"
          >
            {{ $t('appMenu.confirmDelete') }}
          </Button>
        </div>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>
