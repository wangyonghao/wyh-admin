<script lang="ts" setup>
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { cn } from '../../../../lib/cn';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <li
    :class="
      cn('hover:text-foreground inline-flex items-center gap-1.5', props.class)
    "
  >
    <slot></slot>
  </li>
</template>
