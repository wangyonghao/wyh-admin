<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { cn } from '../../../../lib/cn';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <div
    :class="
      cn(
        'relative [&>[data-slot=input]]:has-[[data-slot=decrement]]:pl-5 [&>[data-slot=input]]:has-[[data-slot=increment]]:pr-5',
        props.class,
      )
    "
  >
    <slot></slot>
  </div>
</template>
