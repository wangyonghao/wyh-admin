<script setup lang="ts">
/* global HTMLElement */
import type { AppRecord } from '../types';

import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { useUserStore } from '#/store/user';

import { ChevronRight, Ellipsis, GripVertical } from '@vben/icons';
import { $t } from '@vben/locales';

import { useSortable } from '@vben-core/composables';

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '#/ui/dropdown-menu';
import { Tooltip, TooltipContent, TooltipTrigger } from '#/ui/tooltip';

import { toast } from '#/ui-patterns/toast';


import { childrenOf } from '../composables/useMenuTree';
import { useMenuState } from '../composables/useMenuState';
import MenuTreeNode from './MenuTreeNode.vue';
import NodeIcon from './NodeIcon.vue';

const props = defineProps<{
  app: AppRecord;
  coarse: boolean;
}>();

const emit = defineEmits<{ open: []; remove: [id: string] }>();

const userStore = useUserStore();
const state = useMenuState();
const listRef = ref<HTMLElement | null>(null);
let sortable: { destroy: () => void } | null = null;

const canCreate = computed(
  () =>
    userStore.hasPermission('system:menu:create') ||
    userStore.hasPermission('*:*:*'),
);
const canUpdate = computed(
  () =>
    userStore.hasPermission('system:menu:update') ||
    userStore.hasPermission('*:*:*'),
);
const canDelete = computed(
  () =>
    userStore.hasPermission('system:menu:delete') ||
    userStore.hasPermission('*:*:*'),
);

const children = computed(() =>
  childrenOf(state.treeSource.value, props.app.id, null),
);
const opened = computed(() => {
  if (state.searchQuery.value.trim()) return true;
  return state.isExpanded(props.app.id);
});
const selected = computed(
  () => state.selectedAppId.value === props.app.id && !state.selectedNodeId.value,
);

function toggle() {
  if (state.searchQuery.value.trim()) return;
  state.toggleExpand(props.app.id);
}

async function select() {
  const ok = await state.selectApp(props.app.id);
  if (ok) emit('open');
}

function defaultName(type: 'folder' | 'function') {
  return $t(type === 'folder' ? 'appMenu.untitledDirectory' : 'appMenu.untitledMenu');
}

async function addChild(type: 'folder' | 'function') {
  const ok = await state.selectApp(props.app.id);
  if (!ok) return;
  const created = await state.createNode(null, type, defaultName(type));
  if (created) emit('open');
}

async function shiftApp(delta: number) {
  const ordered = [...state.apps.value].sort(
    (a, b) => a.sort - b.sort || a.name.localeCompare(b.name),
  );
  const index = ordered.findIndex((app) => app.id === props.app.id);
  const next = index + delta;
  if (index < 0 || next < 0 || next >= ordered.length) return;
  await state.moveApp(props.app.id, next);
}

async function copyApp() {
  const choice = await state.guardDirty();
  if (choice === 'cancel') return;
  const created = await state.duplicateApp(props.app.id, $t('appMenu.copySuffix'));
  if (!created) return;
  toast.success($t('appMenu.copied'));
  emit('open');
}

async function toggleStatus() {
  const prev = await state.toggleAppStatus(props.app.id);
  if (prev === undefined) return;
  toast.success(
    props.app.status === 'enabled' ? $t('appMenu.enabled') : $t('appMenu.disabled'),
  );
}

function askDelete() {
  globalThis.setTimeout(() => {
    state.askRemoveApp(props.app.id);
  }, 0);
}

function restoreDom(evt: { from: HTMLElement; item: HTMLElement; oldIndex?: number }) {
  const { from, item, oldIndex } = evt;
  if (oldIndex == null) return;
  item.remove();
  from.insertBefore(item, from.children[oldIndex] ?? null);
}

async function mountSortable() {
  sortable?.destroy();
  sortable = null;
  if (props.coarse || !listRef.value || !opened.value) return;
  if (state.searchQuery.value.trim()) return;
  const { initializeSortable } = useSortable(listRef.value, {
    animation: 0,
    delay: 0,
    draggable: '.tree-node',
    group: { name: 'menu-tree', pull: true, put: true },
    handle: '.drag-handle',
    onEnd: (evt) => {
      const id = (evt.item as HTMLElement).dataset.id;
      const parentId = (evt.to as HTMLElement).dataset.parentId || null;
      const appId = (evt.to as HTMLElement).dataset.appId || undefined;
      const index = evt.newIndex ?? 0;
      restoreDom(evt as { from: HTMLElement; item: HTMLElement; oldIndex?: number });
      if (!id) return;
      void (async () => {
        const result = await state.moveNode(id, parentId, index, appId);
        if (!result.ok) {
          if (result.reason !== 'persist') toast.warning($t('appMenu.dropRejected'));
          return;
        }
        if (result.shouldAsk) {
          void state.finishMovePrefix(id, result.oldPrefix, result.newPrefix);
        }
      })();
    },
  });
  sortable = await initializeSortable();
}

onMounted(() => {
  void nextTick(mountSortable);
});
watch(
  () => [children.value.map((node) => node.id).join(), props.coarse, opened.value],
  () => nextTick(mountSortable),
);
onBeforeUnmount(() => sortable?.destroy());
</script>

<template>
  <li
    class="tree-app"
    role="treeitem"
    :aria-expanded="opened"
    :aria-level="1"
    :aria-selected="selected"
    :data-id="app.id"
  >
    <div
      class="group flex items-center gap-1 border-l-2 pr-2 transition-colors duration-150"
      :class="[selected ? 'border-foreground' : 'border-transparent', coarse ? 'min-h-11' : 'min-h-8']"
      style="padding-left: 8px"
    >
      <button
        type="button"
        class="inline-flex size-6 shrink-0 items-center justify-center text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
        :aria-label="opened ? $t('appMenu.collapseAll') : $t('appMenu.expandAll')"
        @click.stop="toggle"
      >
        <ChevronRight
          class="size-4 transition-colors duration-150"
          :class="opened ? 'rotate-90' : ''"
          :stroke-width="1.5"
        />
      </button>
      <button
        type="button"
        class="flex min-w-0 flex-1 items-center gap-2 text-left text-sm transition-colors duration-150 focus-visible:outline-none"
        :class="
          app.status === 'disabled'
            ? 'text-muted-foreground'
            : selected
              ? 'font-medium'
              : ''
        "
        @click="select"
      >
        <NodeIcon :icon="app.icon" />
        <span class="truncate">{{ app.name }}</span>
      </button>
      <Tooltip v-if="canUpdate && !coarse">
        <TooltipTrigger as-child>
          <button
            type="button"
            class="drag-handle inline-flex size-6 items-center justify-center text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            :class="selected ? 'opacity-100' : 'opacity-0 group-hover:opacity-100 focus-visible:opacity-100'"
            :aria-label="$t('appMenu.drag')"
          >
            <GripVertical
              class="size-4"
              :stroke-width="1.5"
            />
          </button>
        </TooltipTrigger>
        <TooltipContent>{{ $t('appMenu.drag') }}</TooltipContent>
      </Tooltip>
      <DropdownMenu v-if="canCreate || canUpdate || canDelete">
        <DropdownMenuTrigger as-child>
          <button
            type="button"
            class="inline-flex size-6 items-center justify-center text-muted-foreground transition-colors duration-150 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring aria-expanded:opacity-100 max-md:opacity-100"
            :class="selected ? 'opacity-100' : 'opacity-0 group-hover:opacity-100 focus-visible:opacity-100'"
            :aria-label="$t('appMenu.more')"
          >
            <Ellipsis
              class="size-4"
              :stroke-width="1.5"
            />
          </button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuItem
            v-if="canCreate"
            @click="addChild('folder')"
          >
            {{ $t('appMenu.addChildDirectory') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canCreate"
            @click="addChild('function')"
          >
            {{ $t('appMenu.addChildMenu') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canUpdate && coarse"
            @click="shiftApp(-1)"
          >
            {{ $t('appMenu.moveUp') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canUpdate && coarse"
            @click="shiftApp(1)"
          >
            {{ $t('appMenu.moveDown') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canUpdate"
            @click="toggleStatus"
          >
            {{ app.status === 'enabled' ? $t('appMenu.disable') : $t('appMenu.enable') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canCreate"
            @click="copyApp"
          >
            {{ $t('appMenu.copy') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canDelete"
            class="text-destructive focus:text-destructive"
            @click="askDelete"
          >
            {{ $t('appMenu.delete') }}
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
    <ul
      v-if="opened"
      ref="listRef"
      role="group"
      :class="children.length === 0 ? 'min-h-6' : ''"
      data-parent-id=""
      :data-app-id="app.id"
    >
      <MenuTreeNode
        v-for="node in children"
        :key="node.id"
        :coarse="coarse"
        :depth="2"
        :node="node"
        @open="emit('open')"
        @remove="emit('remove', $event)"
      />
    </ul>
  </li>
</template>
