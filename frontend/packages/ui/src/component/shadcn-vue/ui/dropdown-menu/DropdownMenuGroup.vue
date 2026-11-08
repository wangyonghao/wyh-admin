<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { DropdownMenuGroupProps } from 'reka-ui';

import { DropdownMenuGroup } from 'reka-ui';

const props = defineProps<DropdownMenuGroupProps>();
</script>

<template>
  <DropdownMenuGroup v-bind="props">
    <slot></slot>
  </DropdownMenuGroup>
</template>
