<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { HoverCardTriggerProps } from 'reka-ui';

import { HoverCardTrigger } from 'reka-ui';

const props = defineProps<HoverCardTriggerProps>();
</script>

<template>
  <HoverCardTrigger v-bind="props">
    <slot></slot>
  </HoverCardTrigger>
</template>
