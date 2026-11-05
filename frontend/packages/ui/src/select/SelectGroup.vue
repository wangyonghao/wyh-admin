<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { SelectGroupProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../lib/cn';

import { SelectGroup } from 'reka-ui';

const props = defineProps<SelectGroupProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <SelectGroup :class="cn('w-full p-1', props.class)" v-bind="delegatedProps">
    <slot></slot>
  </SelectGroup>
</template>
