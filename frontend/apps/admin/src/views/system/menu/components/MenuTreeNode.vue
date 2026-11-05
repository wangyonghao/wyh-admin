<script setup lang="ts">
/* global HTMLElement */
import type { MenuRecord } from '../types';

import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { useUserStore } from '#/store/user';

import { ChevronRight, Ellipsis, GripVertical } from '@vben/icons';
import { $t } from '@vben/locales';

import { useSortable } from '@wyh/composables';

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from '#/ui/dropdown-menu';
import { Tooltip, TooltipContent, TooltipTrigger } from '#/ui/tooltip';

import { toast } from '#/ui-patterns/toast';


import { childrenOf, isDescendant } from '../composables/useMenuTree';
import { useMenuState } from '../composables/useMenuState';
import NodeIcon from './NodeIcon.vue';

defineOptions({ name: 'MenuTreeNode' });

const props = defineProps<{
  coarse: boolean;
  depth: number;
  node: MenuRecord;
}>();

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

const emit = defineEmits<{ open: []; remove: [id: string] }>();

const children = computed(() =>
  childrenOf(state.treeSource.value, props.node.appId, props.node.id),
);
const showArrow = computed(
  () => props.node.type === 'folder' || children.value.length > 0,
);
const opened = computed(() => {
  if (!showArrow.value) return false;
  if (state.searchQuery.value.trim()) return true;
  return state.isExpanded(props.node.id);
});
const selected = computed(() => state.selectedNodeId.value === props.node.id);
const muted = computed(() => {
  const app = state.apps.value.find((item) => item.id === props.node.appId);
  return props.node.status === 'disabled' || app?.status === 'disabled';
});
const directories = computed(() =>
  state.nodes.value.filter((item) => {
    if (item.type !== 'folder') return false;
    if (item.id === props.node.id) return false;
    return !isDescendant(state.nodes.value, props.node.id, item.id);
  }),
);
const moveApps = computed(() =>
  [...state.apps.value]
    .sort((a, b) => a.sort - b.sort || a.name.localeCompare(b.name))
    .filter((app) => app.id !== props.node.appId),
);

function toggle() {
  if (state.searchQuery.value.trim()) return;
  state.toggleExpand(props.node.id);
}

async function select() {
  const ok = await state.selectNode(props.node.id);
  if (ok) emit('open');
}

async function relocate(parentId: null | string, appId?: string) {
  const result = await state.moveNode(props.node.id, parentId, 0, appId);
  if (!result.ok) {
    if (result.reason !== 'persist') toast.warning($t('appMenu.dropRejected'));
    return;
  }
  if (result.shouldAsk) {
    void state.finishMovePrefix(props.node.id, result.oldPrefix, result.newPrefix);
  }
}

function folderLabel(folder: MenuRecord) {
  if (folder.appId === props.node.appId) return folder.name;
  const app = state.apps.value.find((item) => item.id === folder.appId);
  return app ? `${app.name} / ${folder.name}` : folder.name;
}

function defaultName(type: 'folder' | 'function') {
  return $t(type === 'folder' ? 'appMenu.untitledDirectory' : 'appMenu.untitledMenu');
}

async function addChild(type: 'folder' | 'function') {
  const created = await state.createNode(props.node.id, type, defaultName(type));
  if (created) await state.selectNode(created.id);
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
  if (props.coarse || !listRef.value || state.searchQuery.value.trim()) return;
  const { initializeSortable } = useSortable(listRef.value, {
    animation: 0,
    delay: 0,
    draggable: '.tree-node',
    group: {
      name: 'menu-tree',
      pull: true,
      put: () => props.node.type === 'folder',
    },
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
    class="tree-node"
    role="treeitem"
    :aria-expanded="showArrow ? opened : undefined"
    :aria-level="depth"
    :aria-selected="selected"
    :data-id="node.id"
  >
    <div
      class="group flex items-center gap-1 border-l-2 pr-2 transition-colors duration-150"
      :class="[
        selected ? 'border-foreground' : 'border-transparent',
        coarse ? 'min-h-11' : 'min-h-8',
      ]"
      style="padding-left: 8px"
    >
      <button
        v-if="showArrow"
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
      <span
        v-else
        class="inline-flex size-6 shrink-0"
      />
      <button
        type="button"
        class="flex min-w-0 flex-1 items-center gap-2 text-left text-sm transition-colors duration-150 focus-visible:outline-none"
        :class="muted ? 'text-muted-foreground' : selected ? 'font-medium' : ''"
        @click="select"
      >
        <NodeIcon :icon="node.icon" />
        <span class="truncate">{{ node.name }}</span>
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
            v-if="canCreate && node.type === 'folder'"
            @click="addChild('folder')"
          >
            {{ $t('appMenu.addChildDirectory') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canCreate && node.type === 'folder'"
            @click="addChild('function')"
          >
            {{ $t('appMenu.addChildMenu') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canUpdate && coarse"
            @click="state.shift(node.id, -1)"
          >
            {{ $t('appMenu.moveUp') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canUpdate && coarse"
            @click="state.shift(node.id, 1)"
          >
            {{ $t('appMenu.moveDown') }}
          </DropdownMenuItem>
          <DropdownMenuSub v-if="canUpdate && coarse">
            <DropdownMenuSubTrigger>{{ $t('appMenu.moveTo') }}</DropdownMenuSubTrigger>
            <DropdownMenuSubContent>
              <DropdownMenuItem @click="relocate(null, node.appId)">
                {{ $t('appMenu.root') }}
              </DropdownMenuItem>
              <DropdownMenuItem
                v-for="app in moveApps"
                :key="app.id"
                @click="relocate(null, app.id)"
              >
                {{ app.name }}
              </DropdownMenuItem>
              <DropdownMenuItem
                v-for="folder in directories"
                :key="folder.id"
                @click="relocate(folder.id)"
              >
                {{ folderLabel(folder) }}
              </DropdownMenuItem>
            </DropdownMenuSubContent>
          </DropdownMenuSub>
          <DropdownMenuItem
            v-if="canCreate"
            @click="state.duplicateNode(node.id, $t('appMenu.copySuffix'))"
          >
            {{ $t('appMenu.copy') }}
          </DropdownMenuItem>
          <DropdownMenuItem
            v-if="canDelete"
            class="text-destructive focus:text-destructive"
            @click="emit('remove', node.id)"
          >
            {{ $t('appMenu.delete') }}
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
    <ul
      v-if="opened && showArrow"
      ref="listRef"
      role="group"
      :class="children.length === 0 ? 'min-h-6' : ''"
      :data-app-id="node.appId"
      :data-parent-id="node.id"
    >
      <MenuTreeNode
        v-for="child in children"
        :key="child.id"
        :coarse="coarse"
        :depth="depth + 1"
        :node="child"
        @open="emit('open')"
        @remove="emit('remove', $event)"
      />
    </ul>
  </li>
</template>
