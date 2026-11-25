<script setup lang="ts">
import type { CatalogNode } from '#/api/system/menu-catalog';

import { computed } from 'vue';

import { Button } from '#/ui/button';
import { Checkbox } from '#/ui/checkbox';
import { Input } from '#/ui/input';
import { Label } from '#/ui/label';
import { VbenSelect } from '#/ui/composed/select';

import { canParent, directories } from './catalog';

const props = defineProps<{
  node?: CatalogNode;
  nodes: CatalogNode[];
}>();

const emit = defineEmits<{
  buttons: [nodes: CatalogNode[]];
  change: [patch: Partial<CatalogNode>];
  move: [direction: -1 | 1];
  remove: [];
  restore: [];
}>();

const ICONS = [
  { label: 'file', value: 'lucide:file' },
  { label: 'house', value: 'lucide:house' },
  { label: 'users', value: 'lucide:users' },
  { label: 'settings', value: 'lucide:settings' },
  { label: 'folder', value: 'lucide:folder' },
  { label: 'star', value: 'lucide:star' },
  { label: 'shield', value: 'lucide:shield' },
  { label: 'menu', value: 'lucide:menu' },
];

const parentOptions = computed(() => {
  const node = props.node;
  if (!node) return [];
  const options = [{ label: '顶级', value: '0' }];
  for (const directory of directories(props.nodes)) {
    if (canParent(props.nodes, node, directory.id)) {
      options.push({ label: directory.name, value: directory.id });
    }
  }
  return options;
});

const iconOptions = computed(() => {
  const current = props.node?.icon;
  if (current && !ICONS.some((item) => item.value === current)) {
    return [{ label: current, value: current }, ...ICONS];
  }
  return ICONS;
});

const locked = computed(() => Boolean(props.node?.system || props.node?.required));

const buttons = computed(() =>
  props.nodes.filter((item) => item.type === 3 && item.parentId === props.node?.id),
);

function writeButtons(next: CatalogNode[]) {
  emit('buttons', next);
}

function addButton() {
  const parent = props.node;
  if (!parent) return;
  writeButtons([
    ...buttons.value,
    {
      enabled: true,
      id: `tmp-${crypto.randomUUID()}`,
      name: '未命名权限',
      parentId: parent.id,
      permission: '',
      published: false,
      sort: buttons.value.length + 1,
      type: 3,
    },
  ]);
}

function patchButton(id: string, patch: Partial<CatalogNode>) {
  writeButtons(buttons.value.map((item) => (item.id === id ? { ...item, ...patch } : item)));
}

function dropButton(button: CatalogNode) {
  if (button.published) {
    patchButton(button.id, { pendingDelete: !button.pendingDelete });
    return;
  }
  writeButtons(buttons.value.filter((item) => item.id !== button.id));
}
</script>

<template>
  <section class="flex min-h-0 flex-col rounded-xl border border-border bg-card">
    <div v-if="node" class="grid gap-4 overflow-auto p-4">
      <div class="grid gap-2">
        <Label>类型</Label>
        <div class="inline-flex w-fit rounded-lg bg-muted p-1">
          <button
            :class="node.type === 1 ? 'bg-background shadow-sm' : 'text-muted-foreground'"
            :disabled="node.published"
            class="rounded-md px-3 py-1 text-sm disabled:cursor-not-allowed"
            type="button"
            @click="emit('change', { type: 1, permission: '' })"
          >
            目录
          </button>
          <button
            :class="node.type === 2 ? 'bg-background shadow-sm' : 'text-muted-foreground'"
            :disabled="node.published"
            class="rounded-md px-3 py-1 text-sm disabled:cursor-not-allowed"
            type="button"
            @click="emit('change', { type: 2 })"
          >
            页面
          </button>
        </div>
        <p v-if="node.published" class="text-xs text-muted-foreground">已有菜单不能修改类型</p>
      </div>
      <div class="grid gap-2">
        <Label>名称</Label>
        <Input :model-value="node.name" @update:model-value="emit('change', { name: String($event ?? '') })" />
      </div>
      <div class="grid gap-2">
        <Label>图标</Label>
        <VbenSelect
          :model-value="node.icon"
          :options="iconOptions"
          placeholder="选择图标"
          @update:model-value="emit('change', { icon: $event })"
        />
      </div>
      <div class="grid gap-2">
        <Label>路由地址</Label>
        <Input
          :model-value="node.path"
          placeholder="/orders"
          @update:model-value="emit('change', { path: String($event ?? '') })"
        />
      </div>
      <div v-if="node.type === 2" class="grid gap-2">
        <Label>权限标识</Label>
        <Input
          :model-value="node.permission"
          placeholder="system:order:list"
          @update:model-value="emit('change', { permission: String($event ?? '') })"
        />
      </div>
      <div class="grid gap-2">
        <Label>上级目录</Label>
        <VbenSelect
          :model-value="node.parentId || '0'"
          :options="parentOptions"
          @update:model-value="emit('change', { parentId: $event || '0' })"
        />
      </div>
      <div v-if="node.type === 2" class="grid gap-2">
        <div class="flex items-center justify-between">
          <Label>权限点</Label>
          <button class="text-sm text-primary" type="button" @click="addButton">添加</button>
        </div>
        <div v-for="button in buttons" :key="button.id" class="grid grid-cols-[1fr_1fr_auto] items-center gap-2">
          <Input
            :class="button.pendingDelete ? 'line-through' : ''"
            :model-value="button.name"
            placeholder="名称"
            @update:model-value="patchButton(button.id, { name: String($event ?? '') })"
          />
          <Input
            :model-value="button.permission"
            placeholder="system:order:list"
            @update:model-value="patchButton(button.id, { permission: String($event ?? '') })"
          />
          <Button type="button" variant="outline" @click="dropButton(button)">
            {{ button.published ? (button.pendingDelete ? '恢复' : '删除') : '移除' }}
          </Button>
        </div>
        <p v-if="buttons.length === 0" class="text-xs text-muted-foreground">权限点不出现在导航里，跟随这个页面。</p>
      </div>
      <label class="flex items-center gap-2 text-sm">
        <Checkbox
          :disabled="locked"
          :model-value="node.enabled !== false"
          @update:model-value="emit('change', { enabled: Boolean($event) })"
        />
        启用
      </label>
      <div class="mt-2 flex gap-2">
        <Button type="button" variant="outline" @click="emit('move', -1)">上移</Button>
        <Button type="button" variant="outline" @click="emit('move', 1)">下移</Button>
        <Button
          v-if="node.pendingDelete"
          type="button"
          variant="outline"
          @click="emit('restore')"
        >
          恢复
        </Button>
        <Button
          v-else
          :disabled="locked"
          class="text-destructive"
          type="button"
          variant="outline"
          @click="emit('remove')"
        >
          删除
        </Button>
      </div>
    </div>
    <p v-else class="p-8 text-center text-sm text-muted-foreground">选择左侧菜单后编辑</p>
  </section>
</template>
