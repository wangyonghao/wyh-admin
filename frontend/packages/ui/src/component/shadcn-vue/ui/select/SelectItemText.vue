<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { SelectItemTextProps } from 'reka-ui';

import { SelectItemText } from 'reka-ui';

const props = defineProps<SelectItemTextProps>();
</script>

<template>
  <SelectItemText v-bind="props">
    <slot></slot>
  </SelectItemText>
</template>
