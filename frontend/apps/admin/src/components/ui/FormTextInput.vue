<script lang="ts" setup>
import { computed, ref, useAttrs } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button } from '#/ui/button';
import { Input } from '#/ui/input';
import { Textarea } from '#/ui/textarea';

import {
  commitFormText,
  commitPairText,
  displayFormText,
  displayPairText,
} from './empty-value';
import {
  textCountLabel,
  textMaxLength,
  textRows,
  textShowsCount,
  textUsesPair,
  textUsesPassword,
  textUsesTextarea,
} from './text-mode';

defineOptions({
  name: 'FormTextInput',
  inheritAttrs: false,
});

const props = defineProps<{
  textarea?: boolean;
  value?: null | number | string | [unknown, unknown];
}>();

const emit = defineEmits<{
  'update:value': [value: null | string | [string, string]];
}>();

const attrs = useAttrs();
const inputRef = ref<{ $el?: { focus?: () => void } } | null>(null);
const areaRef = ref<{ $el?: { focus?: () => void } } | null>(null);
const pairRef = ref<{ $el?: { focus?: () => void } } | null>(null);
const revealed = ref(false);

const OMIT = new Set([
  'autosize',
  'clearable',
  'maxLength',
  'maxlength',
  'modelValue',
  'onUpdate:modelValue',
  'onUpdate:value',
  'pair',
  'rows',
  'separator',
  'showCount',
  'showPasswordOn',
  'showWordLimit',
  'textarea',
  'type',
  'value',
]);

const fieldAttrs = computed(() => ({
  ...attrs,
  textarea: props.textarea,
}));

const pairMode = computed(() => textUsesPair(fieldAttrs.value));
const asTextarea = computed(
  () => !pairMode.value && textUsesTextarea(fieldAttrs.value),
);
const asPassword = computed(
  () =>
    !pairMode.value && !asTextarea.value && textUsesPassword(fieldAttrs.value),
);
const maxLength = computed(() => textMaxLength(fieldAttrs.value));
const showCount = computed(() => textShowsCount(fieldAttrs.value));
const rows = computed(() => textRows(fieldAttrs.value));
const shown = computed(() => displayFormText(props.value));
const countLabel = computed(() => textCountLabel(shown.value, maxLength.value));
const pairText = computed(() => displayPairText(props.value));
const pairPlaceholder = computed(() => {
  const raw = attrs.placeholder;
  if (Array.isArray(raw)) {
    return [
      raw[0] == null ? '' : String(raw[0]),
      raw[1] == null ? '' : String(raw[1]),
    ];
  }
  const text = typeof raw === 'string' ? raw : '';
  return [text, text];
});
const separator = computed(() => {
  return typeof attrs.separator === 'string' && attrs.separator
    ? attrs.separator
    : '-';
});

const plainAttrs = computed(() => {
  const next: Record<string, unknown> = {};
  for (const [key, val] of Object.entries(attrs)) {
    if (OMIT.has(key)) continue;
    next[key] = val;
  }
  return next;
});

const inputType = computed(() => {
  if (asPassword.value) return revealed.value ? 'text' : 'password';
  const type = attrs.type;
  if (typeof type === 'string' && type !== 'textarea') return type;
  return 'text';
});

function onText(value: number | string) {
  emit('update:value', commitFormText(value));
}

function onPair(index: 0 | 1, next: unknown) {
  const current = displayPairText(props.value);
  const left = index === 0 ? next : current[0];
  const right = index === 1 ? next : current[1];
  emit('update:value', commitPairText(left, right));
}

defineExpose({
  focus: () => {
    if (pairMode.value) {
      pairRef.value?.$el?.focus?.();
      return;
    }
    const target = asTextarea.value ? areaRef.value : inputRef.value;
    target?.$el?.focus?.();
  },
});
</script>

<template>
  <div
    v-if="pairMode"
    class="flex w-full min-w-0 items-center gap-2"
    :class="attrs.class"
    :style="attrs.style"
  >
    <Input
      ref="pairRef"
      :model-value="pairText[0]"
      :placeholder="pairPlaceholder[0]"
      :disabled="plainAttrs.disabled === true"
      @update:model-value="onPair(0, $event)"
    />
    <span class="text-muted-foreground shrink-0 text-sm">{{ separator }}</span>
    <Input
      :model-value="pairText[1]"
      :placeholder="pairPlaceholder[1]"
      :disabled="plainAttrs.disabled === true"
      @update:model-value="onPair(1, $event)"
    />
  </div>
  <div
    v-else
    class="w-full"
  >
    <div :class="asPassword ? 'relative' : undefined">
      <Textarea
        v-if="asTextarea"
        ref="areaRef"
        v-bind="plainAttrs"
        :rows="rows"
        :maxlength="maxLength"
        :model-value="shown"
        @update:model-value="onText"
      />
      <Input
        v-else
        ref="inputRef"
        v-bind="plainAttrs"
        :type="inputType"
        :maxlength="maxLength"
        :class="asPassword ? 'pr-10' : undefined"
        :model-value="shown"
        @update:model-value="onText"
      />
      <Button
        v-if="asPassword"
        type="button"
        variant="ghost"
        size="icon"
        class="absolute top-1/2 right-1 size-8 -translate-y-1/2"
        tabindex="-1"
        @click="revealed = !revealed"
      >
        <IconifyIcon
          :icon="revealed ? 'lucide:eye-off' : 'lucide:eye'"
          class="size-4"
        />
      </Button>
    </div>
    <div
      v-if="showCount"
      class="text-muted-foreground mt-1 text-right text-xs"
    >
      {{ countLabel }}
    </div>
  </div>
</template>
