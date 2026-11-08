<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { ContextMenuPortalProps } from 'reka-ui';

import { ContextMenuPortal } from 'reka-ui';

const props = defineProps<ContextMenuPortalProps>();
</script>

<template>
  <ContextMenuPortal v-bind="props">
    <slot></slot>
  </ContextMenuPortal>
</template>
