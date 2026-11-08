<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { SelectValueProps } from 'reka-ui';

import { SelectValue } from 'reka-ui';

const props = defineProps<SelectValueProps>();
</script>

<template>
  <SelectValue v-bind="props">
    <slot></slot>
  </SelectValue>
</template>
