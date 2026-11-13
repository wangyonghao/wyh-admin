<script lang="ts" setup>
import { computed, useAttrs } from 'vue';

import { Button } from '#/ui/button';
import {
  NumberField,
  NumberFieldContent,
  NumberFieldDecrement,
  NumberFieldIncrement,
  NumberFieldInput,
} from '#/ui/number-field';

import {
  commitFormNumber,
  displayFormNumber,
  numberBound,
  numberStep,
} from './number-value';

defineOptions({
  name: 'FormInputNumber',
  inheritAttrs: false,
});

const props = defineProps<{
  clearable?: boolean;
  disabled?: boolean;
  max?: number;
  min?: number;
  placeholder?: string;
  precision?: number;
  step?: number;
  value?: null | number | string;
}>();

const emit = defineEmits<{
  'update:value': [value: null | number];
}>();

const attrs = useAttrs();

const OMIT = new Set([
  'clearable',
  'max',
  'min',
  'modelValue',
  'onUpdate:modelValue',
  'onUpdate:value',
  'placeholder',
  'precision',
  'step',
  'value',
]);

const shown = computed(() => displayFormNumber(props.value));
const step = computed(() =>
  numberStep(props.step ?? attrs.step, props.precision ?? attrs.precision),
);
const min = computed(() => numberBound(props.min ?? attrs.min));
const max = computed(() => numberBound(props.max ?? attrs.max));

const rest = computed(() => {
  const next: Record<string, unknown> = {};
  for (const [key, val] of Object.entries(attrs)) {
    if (OMIT.has(key)) continue;
    next[key] = val;
  }
  return next;
});

function onUpdate(value: null | number) {
  emit('update:value', commitFormNumber(value));
}
</script>

<template>
  <div class="flex w-full items-center gap-1">
    <NumberField
      class="w-full"
      :model-value="shown"
      :min="min"
      :max="max"
      :step="step"
      :disabled="disabled"
      @update:model-value="onUpdate"
    >
      <NumberFieldContent>
        <NumberFieldDecrement />
        <NumberFieldInput :placeholder="placeholder" v-bind="rest" />
        <NumberFieldIncrement />
      </NumberFieldContent>
    </NumberField>
    <Button
      v-if="clearable && shown != null"
      type="button"
      variant="ghost"
      size="sm"
      class="shrink-0 px-2"
      @click="emit('update:value', null)"
    >
      清除
    </Button>
  </div>
</template>
