<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { VariantProps } from 'class-variance-authority';
import type { ToggleGroupItemProps } from 'reka-ui';

import { computed, inject } from 'vue';

import { cn } from '../lib/cn';

import { ToggleGroupItem, useForwardProps } from 'reka-ui';

import { toggleVariants } from '../toggle';

type ToggleGroupVariants = VariantProps<typeof toggleVariants>;

const props = defineProps<
  ToggleGroupItemProps & {
    class?: any;
    size?: ToggleGroupVariants['size'];
    variant?: ToggleGroupVariants['variant'];
  }
>();

const context = inject<ToggleGroupVariants>('toggleGroup');

const delegatedProps = computed(() => {
  const { class: _, size: _size, variant: _variant, ...delegated } = props;
  return delegated;
});

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <ToggleGroupItem
    v-bind="forwardedProps"
    :class="
      cn(
        toggleVariants({
          variant: context?.variant || variant,
          size: context?.size || size,
        }),
        props.class,
      )
    "
  >
    <slot></slot>
  </ToggleGroupItem>
</template>
