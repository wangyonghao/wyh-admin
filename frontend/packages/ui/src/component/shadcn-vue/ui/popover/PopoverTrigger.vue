<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { PopoverTriggerProps } from 'reka-ui';

import { PopoverTrigger } from 'reka-ui';

const props = defineProps<PopoverTriggerProps>();
</script>

<template>
  <PopoverTrigger v-bind="props">
    <slot></slot>
  </PopoverTrigger>
</template>
