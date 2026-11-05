<script lang="ts" setup>
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { AlertDialogDescriptionProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../lib/cn';

import { AlertDialogDescription, useForwardProps } from 'reka-ui';

const props = defineProps<AlertDialogDescriptionProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <AlertDialogDescription
    v-bind="forwardedProps"
    :class="cn('text-muted-foreground text-sm', props.class)"
  >
    <slot></slot>
  </AlertDialogDescription>
</template>
