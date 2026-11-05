<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { NumberFieldRootEmits, NumberFieldRootProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../lib/cn';

import { NumberFieldRoot, useForwardPropsEmits } from 'reka-ui';

const props = defineProps<NumberFieldRootProps & { class?: any }>();
const emits = defineEmits<NumberFieldRootEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <NumberFieldRoot v-bind="forwarded" :class="cn('grid gap-1.5', props.class)">
    <slot></slot>
  </NumberFieldRoot>
</template>
