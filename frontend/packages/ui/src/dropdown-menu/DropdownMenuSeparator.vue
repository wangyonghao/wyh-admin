<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { DropdownMenuSeparatorProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../lib/cn';

import { DropdownMenuSeparator } from 'reka-ui';

const props = defineProps<
  DropdownMenuSeparatorProps & {
    class?: any;
  }
>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <DropdownMenuSeparator
    v-bind="delegatedProps"
    :class="cn('bg-border -mx-1 my-1 h-px', props.class)"
  />
</template>
