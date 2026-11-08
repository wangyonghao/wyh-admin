<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { cn } from '../../../../lib/cn';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <div :class="cn('flex flex-col gap-y-1.5 p-5', props.class)">
    <slot></slot>
  </div>
</template>
