<script lang="ts" setup>
import FormTextInput from './FormTextInput.vue';

defineOptions({
  name: 'FormTextarea',
  inheritAttrs: false,
});

defineProps<{
  value?: null | number | string;
}>();

const emit = defineEmits<{
  'update:value': [value: null | string];
}>();

function onUpdate(value: null | string | [string, string]) {
  if (Array.isArray(value)) {
    emit('update:value', value.join('\n'));
    return;
  }
  emit('update:value', value);
}
</script>

<template>
  <FormTextInput
    textarea
    v-bind="$attrs"
    :value="value"
    @update:value="onUpdate"
  />
</template>
