<script lang="ts" setup>
import { computed } from 'vue';

import { Button } from '#/ui/button';
import { Input } from '#/ui/input';

import {
  commitTimeValue,
  displayTimeInput,
  isTimeEmpty,
  timeInputStep,
} from './time-value';

defineOptions({
  name: 'FormTimePicker',
  inheritAttrs: false,
});

const props = defineProps<{
  clearable?: boolean;
  disabled?: boolean;
  format?: string;
  placeholder?: string;
  value?: null | number | string;
  valueFormat?: string;
}>();

const emit = defineEmits<{
  'update:value': [value: null | number | string];
}>();

const shown = computed(() =>
  displayTimeInput(props.value, props.format, props.valueFormat),
);
const step = computed(() => timeInputStep(props.format, props.valueFormat));
const canClear = computed(
  () => props.clearable === true && !isTimeEmpty(props.value),
);

function onInput(raw: unknown) {
  emit(
    'update:value',
    commitTimeValue(raw == null ? '' : String(raw), {
      base: typeof props.value === 'number' ? props.value : undefined,
      format: props.format,
      valueFormat: props.valueFormat,
    }),
  );
}
</script>

<template>
  <div class="flex items-center gap-2">
    <Input
      type="time"
      :step="step"
      :disabled="disabled"
      :placeholder="placeholder"
      :model-value="shown"
      class="w-40"
      @update:model-value="onInput"
    />
    <Button
      v-if="canClear"
      type="button"
      variant="link"
      size="sm"
      class="text-muted-foreground h-auto px-1"
      @click="emit('update:value', null)"
    >
      清除
    </Button>
  </div>
</template>
