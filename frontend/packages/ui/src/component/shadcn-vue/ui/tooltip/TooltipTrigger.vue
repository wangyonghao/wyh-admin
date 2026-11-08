<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { TooltipTriggerProps } from 'reka-ui';

import { TooltipTrigger } from 'reka-ui';

const props = defineProps<TooltipTriggerProps>();
</script>

<template>
  <TooltipTrigger v-bind="props">
    <slot></slot>
  </TooltipTrigger>
</template>
