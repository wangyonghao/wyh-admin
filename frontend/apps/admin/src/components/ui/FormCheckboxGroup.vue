<script lang="ts" setup>
import type { CheckScalar } from '#/ui/checkbox/group';

import { computed, useAttrs } from 'vue';

import { Checkbox } from '#/ui/checkbox';
import {
  isCheckScalar,
  isValueChecked,
  normalizeCheckOptions,
  toggleCheckedValue,
} from '#/ui/checkbox/group';

defineOptions({
  name: 'FormCheckboxGroup',
  inheritAttrs: false,
});

const props = defineProps<{
  disabled?: boolean;
  options?: unknown;
  value?: unknown;
}>();

const emit = defineEmits<{
  'update:value': [value: CheckScalar[]];
}>();

const attrs = useAttrs();

const options = computed(() =>
  normalizeCheckOptions(props.options ?? attrs.options),
);

function onToggle(value: CheckScalar, checked: boolean | 'indeterminate') {
  const current = Array.isArray(props.value)
    ? props.value.filter(isCheckScalar)
    : [];
  emit('update:value', toggleCheckedValue(current, value, checked === true));
}
</script>

<template>
  <div
    class="flex flex-wrap items-center gap-x-4 gap-y-2"
    :class="attrs.class"
  >
    <label
      v-for="option in options"
      :key="option.key"
      class="inline-flex cursor-pointer items-center gap-2 text-sm"
      :class="
        option.disabled || disabled ? 'pointer-events-none opacity-50' : ''
      "
    >
      <Checkbox
        :model-value="isValueChecked(value, option.value)"
        :disabled="option.disabled || disabled"
        @update:model-value="(checked) => onToggle(option.value, checked)"
      />
      <span>{{ option.label }}</span>
    </label>
  </div>
</template>
