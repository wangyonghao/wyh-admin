<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { ContextMenuRootEmits, ContextMenuRootProps } from 'reka-ui';

import { ContextMenuRoot, useForwardPropsEmits } from 'reka-ui';

const props = withDefaults(defineProps<ContextMenuRootProps>(), {
  modal: false,
});
const emits = defineEmits<ContextMenuRootEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <ContextMenuRoot v-bind="forwarded">
    <slot></slot>
  </ContextMenuRoot>
</template>
