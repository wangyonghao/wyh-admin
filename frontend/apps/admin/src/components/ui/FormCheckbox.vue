<script lang="ts" setup>
import { computed, useAttrs } from 'vue';

import { Checkbox } from '#/ui/checkbox';

import { displayFormChecked } from './empty-value';

defineOptions({
  name: 'FormCheckbox',
  inheritAttrs: false,
});

defineProps<{
  checked?: boolean | null;
}>();

const emit = defineEmits<{
  'update:checked': [value: boolean];
}>();

const attrs = useAttrs();

const rest = computed(() => {
  const next: Record<string, unknown> = {};
  for (const [key, val] of Object.entries(attrs)) {
    if (
      key === 'checked' ||
      key === 'modelValue' ||
      key === 'onUpdate:checked' ||
      key === 'onUpdate:modelValue'
    ) {
      continue;
    }
    next[key] = val;
  }
  return next;
});

function onUpdate(value: boolean | 'indeterminate') {
  emit('update:checked', value === true);
}
</script>

<template>
  <span class="inline-flex items-center gap-2">
    <Checkbox
      v-bind="rest"
      :model-value="displayFormChecked(checked)"
      @update:model-value="onUpdate"
    />
    <slot />
  </span>
</template>
