<script setup lang="ts">
import type { CatalogNode } from '#/api/system/menu-catalog';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { rows } from './catalog';

const props = defineProps<{
  nodes: CatalogNode[];
  selectedId?: string;
}>();

const emit = defineEmits<{
  create: [];
  move: [id: string, parentId: string, index: number];
  select: [id: string];
}>();

const list = computed(() => rows(props.nodes));

let draggingId = '';

function iconOf(icon?: string) {
  if (!icon) return 'lucide:file';
  return icon.includes(':') ? icon : `lucide:${icon}`;
}

function onDragStart(id: string, event: DragEvent) {
  draggingId = id;
  event.dataTransfer?.setData('text/plain', id);
}

function onDrop(target: CatalogNode, event: DragEvent) {
  const id = draggingId || event.dataTransfer?.getData('text/plain');
  if (!id || id === target.id) return;
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
  const offset = event.clientY - rect.top;
  const before = offset < rect.height * 0.35;
  const inside = !before && offset < rect.height * 0.7 && target.type === 1;
  if (inside) {
    emit('move', id, target.id, 999);
    return;
  }
  const siblings = list.value.filter((row) => row.node.parentId === target.parentId);
  const index = siblings.findIndex((row) => row.node.id === target.id);
  emit('move', id, target.parentId || '0', before ? index : index + 1);
}
</script>

<template>
  <section class="flex min-h-0 flex-col rounded-xl border border-border bg-card">
    <header class="flex items-center justify-between border-b border-border px-4 py-3">
      <h2 class="text-sm font-semibold">菜单结构</h2>
      <button
        class="rounded-md border border-border px-3 py-1 text-sm hover:bg-accent"
        type="button"
        @click="emit('create')"
      >
        新增菜单
      </button>
    </header>
    <div class="min-h-0 flex-1 overflow-auto p-2">
      <button
        v-for="row in list"
        :key="row.node.id"
        :class="[
          row.node.id === selectedId ? 'bg-primary/10 text-primary' : 'hover:bg-accent',
          row.node.pendingDelete ? 'opacity-50 line-through' : '',
        ]"
        class="flex w-full items-center gap-2 rounded-md px-2 py-2 text-left text-sm"
        :style="{ paddingLeft: `${row.depth * 16 + 8}px` }"
        draggable="true"
        type="button"
        @click="emit('select', row.node.id)"
        @dragover.prevent
        @dragstart="onDragStart(row.node.id, $event)"
        @drop.prevent="onDrop(row.node, $event)"
      >
        <IconifyIcon :icon="iconOf(row.node.icon)" class="size-4 shrink-0" />
        <span class="truncate">{{ row.node.name }}</span>
        <span
          v-if="row.node.required"
          class="ml-auto rounded bg-muted px-1.5 py-0.5 text-xs text-muted-foreground"
        >
          必含
        </span>
        <span
          v-else-if="row.node.pendingDelete"
          class="ml-auto text-xs text-destructive"
        >
          待删除
        </span>
      </button>
      <p v-if="list.length === 0" class="px-3 py-8 text-center text-sm text-muted-foreground">
        还没有菜单
      </p>
    </div>
    <p class="border-t border-border px-4 py-2 text-xs text-muted-foreground">可拖动排序</p>
  </section>
</template>
