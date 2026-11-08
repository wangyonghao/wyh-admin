<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { BadgeVariants } from './badge';

import { cn } from '../../../../lib/cn';

import { badgeVariants } from './badge';

const props = defineProps<{
  class?: any;
  variant?: BadgeVariants['variant'];
}>();
</script>

<template>
  <div :class="cn(badgeVariants({ variant }), props.class)">
    <slot></slot>
  </div>
</template>
