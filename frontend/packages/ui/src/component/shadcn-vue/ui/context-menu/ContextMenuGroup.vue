<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { ContextMenuGroupProps } from 'reka-ui';

import { ContextMenuGroup } from 'reka-ui';

const props = defineProps<ContextMenuGroupProps>();
</script>

<template>
  <ContextMenuGroup v-bind="props">
    <slot></slot>
  </ContextMenuGroup>
</template>
