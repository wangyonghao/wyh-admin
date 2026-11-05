<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { TooltipProviderProps } from 'reka-ui';

import { TooltipProvider } from 'reka-ui';

const props = defineProps<TooltipProviderProps>();
</script>

<template>
  <TooltipProvider v-bind="props">
    <slot></slot>
  </TooltipProvider>
</template>
