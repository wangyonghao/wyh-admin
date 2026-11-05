<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { ContextMenuTriggerProps } from 'reka-ui';

import { ContextMenuTrigger, useForwardProps } from 'reka-ui';

const props = defineProps<ContextMenuTriggerProps>();

const forwardedProps = useForwardProps(props);
</script>

<template>
  <ContextMenuTrigger v-bind="forwardedProps">
    <slot></slot>
  </ContextMenuTrigger>
</template>
