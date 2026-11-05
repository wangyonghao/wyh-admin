<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { cn } from '../lib/cn';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <h3 :class="cn('leading-none font-semibold tracking-tight', props.class)">
    <slot></slot>
  </h3>
</template>
