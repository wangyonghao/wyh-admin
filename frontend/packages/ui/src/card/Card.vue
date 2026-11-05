<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { cn } from '../lib/cn';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <div
    :class="
      cn(
        'border-border bg-card text-card-foreground rounded-xl border',
        props.class,
      )
    "
  >
    <slot></slot>
  </div>
</template>
