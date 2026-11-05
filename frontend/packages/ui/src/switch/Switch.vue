<script lang="ts" setup>
import { computed } from 'vue';

import SwitchRoot from './switch-root.vue';

import { switchChecked, switchNextValue, type SwitchScalar } from './map';

const props = withDefaults(
  defineProps<{
    checkedValue?: SwitchScalar;
    class?: any;
    disabled?: boolean;
    modelValue?: null | SwitchScalar;
    uncheckedValue?: SwitchScalar;
  }>(),
  {
    checkedValue: true,
    disabled: false,
    uncheckedValue: false,
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: SwitchScalar];
}>();

const on = computed(() => switchChecked(props.modelValue, props.checkedValue));

function onUpdate(checked: boolean) {
  emit(
    'update:modelValue',
    switchNextValue(checked, props.checkedValue, props.uncheckedValue),
  );
}
</script>

<template>
  <SwitchRoot
    :model-value="on"
    :disabled="disabled"
    :class="props.class"
    @update:model-value="onUpdate"
  />
</template>
