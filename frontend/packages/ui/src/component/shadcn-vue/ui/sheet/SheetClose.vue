<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { DialogCloseProps } from 'reka-ui';

import { DialogClose } from 'reka-ui';

const props = defineProps<DialogCloseProps>();
</script>

<template>
  <DialogClose v-bind="props">
    <slot></slot>
  </DialogClose>
</template>
