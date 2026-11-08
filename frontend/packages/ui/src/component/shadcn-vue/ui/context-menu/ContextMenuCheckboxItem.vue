<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type {
  ContextMenuCheckboxItemEmits,
  ContextMenuCheckboxItemProps,
} from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../../../../lib/cn';

import { Check } from 'lucide-vue-next';
import {
  ContextMenuCheckboxItem,
  ContextMenuItemIndicator,
  useForwardPropsEmits,
} from 'reka-ui';

const props = defineProps<ContextMenuCheckboxItemProps & { class?: any }>();
const emits = defineEmits<ContextMenuCheckboxItemEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <ContextMenuCheckboxItem
    v-bind="forwarded"
    :class="
      cn(
        'focus:bg-accent focus:text-accent-foreground relative flex cursor-default items-center rounded-sm py-1.5 pr-2 pl-8 text-sm outline-hidden select-none data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
        props.class,
      )
    "
  >
    <span class="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
      <ContextMenuItemIndicator>
        <Check class="h-4 w-4" />
      </ContextMenuItemIndicator>
    </span>
    <slot></slot>
  </ContextMenuCheckboxItem>
</template>
