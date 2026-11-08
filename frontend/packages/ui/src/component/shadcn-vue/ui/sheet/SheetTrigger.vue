<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { DialogTriggerProps } from 'reka-ui';

import { DialogTrigger } from 'reka-ui';

const props = defineProps<DialogTriggerProps>();
</script>

<template>
  <DialogTrigger v-bind="props">
    <slot></slot>
  </DialogTrigger>
</template>
