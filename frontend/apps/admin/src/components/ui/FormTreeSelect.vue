<script lang="ts" setup>
import type { VisibleTreeRow } from './tree-select-value';

import { computed, ref, useAttrs, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Checkbox } from '#/ui/checkbox';
import { Input } from '#/ui/input';
import { Popover, PopoverContent, PopoverTrigger } from '#/ui/popover';

import {
  commitTreeValue,
  filterTreeNodes,
  findTreeLabel,
  isTreeKey,
  isTreeSelectEmpty,
  normalizeTreeOptions,
  parentKeyIds,
  toggleTreeValue,
  treeKeyId,
  treeSummary,
  visibleTreeRows,
} from './tree-select-value';

defineOptions({
  name: 'FormTreeSelect',
  inheritAttrs: false,
});

const props = withDefaults(
  defineProps<{
    childrenField?: string;
    clearable?: boolean;
    defaultExpandAll?: boolean;
    disabled?: boolean;
    filterable?: boolean;
    keyField?: string;
    labelField?: string;
    multiple?: boolean;
    options?: unknown;
    placeholder?: string;
    value?: unknown;
  }>(),
  {
    childrenField: 'children',
    defaultExpandAll: false,
    keyField: 'key',
    labelField: 'label',
  },
);

const emit = defineEmits<{
  'update:value': [value: null | string | number | Array<string | number>];
  visibleChange: [open: boolean];
}>();

const attrs = useAttrs();
const open = ref(false);
const query = ref('');
const expanded = ref<Set<string>>(new Set());

const triggerLook =
  'border-input bg-background ring-offset-background focus-visible:ring-ring flex h-10 w-full items-center justify-between rounded-md border px-3 py-2 text-sm shadow-xs focus-visible:ring-1 focus-visible:outline-hidden disabled:cursor-not-allowed disabled:opacity-50';

const nodes = computed(() =>
  normalizeTreeOptions(
    props.options ?? attrs.options,
    props.keyField,
    props.labelField,
    props.childrenField,
  ),
);
const shown = computed(() => filterTreeNodes(nodes.value, query.value));
const rows = computed(() =>
  visibleTreeRows(
    shown.value,
    query.value.trim()
      ? new Set(parentKeyIds(shown.value))
      : expanded.value,
  ),
);
const empty = computed(() => isTreeSelectEmpty(props.value));
const singleLabel = computed(() => findTreeLabel(nodes.value, props.value));
const summary = computed(() => treeSummary(props.value, nodes.value));
const triggerText = computed(() => {
  if (props.multiple) return summary.value || props.placeholder || '';
  return singleLabel.value || props.placeholder || '';
});
const triggerMuted = computed(() => {
  if (props.multiple) return summary.value.length === 0;
  return singleLabel.value == null;
});

watch(
  nodes,
  (list) => {
    if (!props.defaultExpandAll) return;
    expanded.value = new Set(parentKeyIds(list));
  },
  { immediate: true },
);

function onOpen(next: boolean) {
  open.value = next;
  emit('visibleChange', next);
  if (!next) query.value = '';
}

function clear() {
  emit('update:value', null);
}

function toggleExpand(row: VisibleTreeRow) {
  const id = treeKeyId(row.key);
  const next = new Set(expanded.value);
  if (next.has(id)) next.delete(id);
  else next.add(id);
  expanded.value = next;
}

function choose(row: VisibleTreeRow) {
  if (row.disabled || props.disabled) return;
  if (props.multiple) {
    const checked = Array.isArray(props.value)
      ? props.value.some((item) => item === row.key)
      : props.value === row.key;
    emit('update:value', toggleTreeValue(props.value, row.key, !checked));
    return;
  }
  emit('update:value', commitTreeValue(row.key));
  open.value = false;
}

function rowChecked(row: VisibleTreeRow): boolean {
  if (Array.isArray(props.value)) return props.value.some((item) => item === row.key);
  return props.value === row.key;
}

function onCheck(row: VisibleTreeRow, checked: boolean | 'indeterminate') {
  if (row.disabled || props.disabled || !isTreeKey(row.key)) return;
  emit('update:value', toggleTreeValue(props.value, row.key, checked === true));
}
</script>

<template>
  <div
    class="min-w-0"
    :class="attrs.class || 'w-full'"
    :style="attrs.style"
  >
    <Popover
      :open="open"
      @update:open="onOpen"
    >
      <PopoverTrigger as-child>
        <button
          type="button"
          :disabled="disabled"
          :class="triggerLook"
        >
          <span
            class="line-clamp-1 flex-auto text-left"
            :class="triggerMuted ? 'text-muted-foreground' : ''"
          >
            {{ triggerText }}
          </span>
          <span
            v-if="clearable && !empty"
            class="mr-1 inline-flex size-4 shrink-0 cursor-pointer items-center opacity-50 hover:opacity-100"
            @pointerdown.stop
            @click.stop.prevent="clear"
          >
            <IconifyIcon
              icon="lucide:x"
              class="size-4"
            />
          </span>
          <slot name="arrow">
            <IconifyIcon
              icon="lucide:chevron-down"
              class="size-4 shrink-0 opacity-50"
            />
          </slot>
        </button>
      </PopoverTrigger>
      <PopoverContent
        align="start"
        class="z-[4000] max-h-72 w-[var(--reka-popover-trigger-width)] min-w-56 overflow-y-auto p-1"
      >
        <div
          v-if="filterable"
          class="p-1"
          @keydown.stop
          @pointerdown.stop
        >
          <Input
            :model-value="query"
            class="h-8"
            placeholder="搜索"
            @update:model-value="query = String($event ?? '')"
          />
        </div>
        <p
          v-if="rows.length === 0"
          class="text-muted-foreground px-2 py-3 text-center text-sm"
        >
          无数据
        </p>
        <div
          v-for="row in rows"
          :key="treeKeyId(row.key)"
          class="flex items-center rounded-sm text-sm hover:bg-accent"
          :style="{ paddingLeft: `${row.depth * 12 + 4}px` }"
        >
          <button
            type="button"
            class="inline-flex size-6 shrink-0 items-center justify-center"
            :class="row.hasChildren ? '' : 'invisible'"
            @click.stop="toggleExpand(row)"
          >
            <IconifyIcon
              icon="lucide:chevron-right"
              class="size-3.5 transition"
              :class="expanded.has(treeKeyId(row.key)) || query.trim() ? 'rotate-90' : ''"
            />
          </button>
          <label
            v-if="multiple"
            class="flex min-w-0 flex-1 cursor-pointer items-center gap-2 py-1.5 pr-2"
            :class="row.disabled ? 'pointer-events-none opacity-50' : ''"
          >
            <Checkbox
              :model-value="rowChecked(row)"
              :disabled="row.disabled || disabled"
              @update:model-value="(checked) => onCheck(row, checked)"
            />
            <span class="truncate">{{ row.label }}</span>
          </label>
          <button
            v-else
            type="button"
            class="min-w-0 flex-1 truncate py-1.5 pr-2 text-left"
            :class="[
              value === row.key ? 'font-medium' : '',
              row.disabled ? 'pointer-events-none opacity-50' : '',
            ]"
            :disabled="row.disabled || disabled"
            @click="choose(row)"
          >
            {{ row.label }}
          </button>
        </div>
      </PopoverContent>
    </Popover>
  </div>
</template>
