<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { DropdownMenuSubTriggerProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../lib/cn';

import { ChevronRight } from 'lucide-vue-next';
import { DropdownMenuSubTrigger, useForwardProps } from 'reka-ui';

const props = defineProps<DropdownMenuSubTriggerProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <DropdownMenuSubTrigger
    v-bind="forwardedProps"
    :class="
      cn(
        'focus:bg-accent data-[state=open]:bg-accent flex cursor-default items-center rounded-sm px-2 py-1.5 text-sm outline-hidden select-none',
        props.class,
      )
    "
  >
    <slot></slot>
    <ChevronRight class="ml-auto h-4 w-4" />
  </DropdownMenuSubTrigger>
</template>
