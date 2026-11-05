<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { DropdownMenuTriggerProps } from 'reka-ui';

import { DropdownMenuTrigger, useForwardProps } from 'reka-ui';

const props = defineProps<DropdownMenuTriggerProps>();

const forwardedProps = useForwardProps(props);
</script>

<template>
  <DropdownMenuTrigger class="outline-hidden" v-bind="forwardedProps">
    <slot></slot>
  </DropdownMenuTrigger>
</template>
