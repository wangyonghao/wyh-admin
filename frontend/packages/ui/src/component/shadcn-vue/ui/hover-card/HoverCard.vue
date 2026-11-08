<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { HoverCardRootEmits, HoverCardRootProps } from 'reka-ui';

import { HoverCardRoot, useForwardPropsEmits } from 'reka-ui';

const props = defineProps<HoverCardRootProps>();
const emits = defineEmits<HoverCardRootEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <HoverCardRoot v-bind="forwarded">
    <slot></slot>
  </HoverCardRoot>
</template>
