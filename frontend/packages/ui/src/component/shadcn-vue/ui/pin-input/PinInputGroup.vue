<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { PrimitiveProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../../../../lib/cn';

import { Primitive, useForwardProps } from 'reka-ui';

const props = defineProps<PrimitiveProps & { class?: any }>();
const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;
  return delegated;
});
const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <Primitive
    v-bind="forwardedProps"
    :class="cn('flex items-center', props.class)"
  >
    <slot></slot>
  </Primitive>
</template>
