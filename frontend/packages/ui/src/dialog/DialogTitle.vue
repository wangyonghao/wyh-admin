<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { DialogTitleProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../lib/cn';

import { DialogTitle, useForwardProps } from 'reka-ui';

const props = defineProps<DialogTitleProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <DialogTitle
    v-bind="forwardedProps"
    :class="
      cn('text-lg leading-none font-semibold tracking-tight', props.class)
    "
  >
    <slot></slot>
  </DialogTitle>
</template>
