<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { ContextMenuLabelProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../../../../lib/cn';

import { ContextMenuLabel } from 'reka-ui';

const props = defineProps<
  ContextMenuLabelProps & { class?: any; inset?: boolean }
>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <ContextMenuLabel
    v-bind="delegatedProps"
    :class="
      cn(
        'text-foreground px-2 py-1.5 text-sm font-semibold',
        inset && 'pl-8',
        props.class,
      )
    "
  >
    <slot></slot>
  </ContextMenuLabel>
</template>
