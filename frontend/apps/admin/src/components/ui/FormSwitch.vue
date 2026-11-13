<script lang="ts" setup>
import type { SwitchScalar } from '#/ui/switch';

import { Switch } from '#/ui/switch';

defineOptions({
  name: 'FormSwitch',
  inheritAttrs: false,
});

defineProps<{
  checkedValue?: SwitchScalar;
  disabled?: boolean;
  uncheckedValue?: SwitchScalar;
  value?: null | SwitchScalar;
}>();

const emit = defineEmits<{
  'update:value': [value: SwitchScalar];
}>();
</script>

<template>
  <Switch
    :model-value="value"
    :disabled="disabled"
    :checked-value="checkedValue"
    :unchecked-value="uncheckedValue"
    @update:model-value="emit('update:value', $event)"
  />
</template>
