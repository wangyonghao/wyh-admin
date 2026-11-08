<script lang="ts" setup>
import { computed, useAttrs } from 'vue';

import { cn } from '@wyh/ui/lib/cn';

import { RadioGroup, RadioGroupItem } from '#/ui/radio-group';

import {
  commitSelectValue,
  normalizeSelectOptions,
  selectDisplayKey,
} from './select-value';

defineOptions({
  name: 'FormRadioGroup',
  inheritAttrs: false,
});

const props = defineProps<{
  disabled?: boolean;
  isButton?: boolean;
  options?: unknown;
  value?: unknown;
}>();

const emit = defineEmits<{
  'update:value': [value: null | boolean | number | string];
}>();

const attrs = useAttrs();

const options = computed(() =>
  normalizeSelectOptions(props.options ?? attrs.options),
);
const button = computed(
  () => props.isButton === true || attrs.optionType === 'button',
);
const displayKey = computed(() => selectDisplayKey(props.value, options.value));

const restClass = computed(() =>
  typeof attrs.class === 'string' ? attrs.class : undefined,
);

function onUpdate(key: unknown) {
  emit('update:value', commitSelectValue(key, options.value));
}
</script>

<template>
  <RadioGroup
    :model-value="displayKey"
    :disabled="disabled"
    :class="cn(button ? 'flex flex-wrap gap-2' : 'grid gap-2', restClass)"
    @update:model-value="onUpdate"
  >
    <label
      v-for="option in options"
      :key="option.key"
      :class="
        button
          ? cn(
              'inline-flex cursor-pointer items-center rounded-md border px-3 py-1.5 text-sm',
              displayKey === option.key
                ? 'border-primary bg-primary text-primary-foreground'
                : 'border-input bg-background',
              (disabled || option.disabled) && 'cursor-not-allowed opacity-50',
            )
          : 'inline-flex cursor-pointer items-center gap-2 text-sm'
      "
    >
      <RadioGroupItem
        :value="option.key"
        :disabled="disabled || option.disabled"
        :class="button ? 'sr-only' : undefined"
      />
      <span>{{ option.label }}</span>
    </label>
  </RadioGroup>
</template>
