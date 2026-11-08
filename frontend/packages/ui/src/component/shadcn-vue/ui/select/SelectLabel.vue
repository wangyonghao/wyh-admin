<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { SelectLabelProps } from 'reka-ui';

import { cn } from '../../../../lib/cn';

import { SelectLabel } from 'reka-ui';

const props = defineProps<SelectLabelProps & { class?: any }>();
</script>

<template>
  <SelectLabel :class="cn('px-2 py-1.5 text-sm font-semibold', props.class)">
    <slot></slot>
  </SelectLabel>
</template>
