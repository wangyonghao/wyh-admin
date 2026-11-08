<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { TabsListProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../../../../lib/cn';

import { TabsList } from 'reka-ui';

const props = defineProps<TabsListProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <TabsList
    v-bind="delegatedProps"
    :class="
      cn(
        'bg-muted text-muted-foreground inline-flex h-9 items-center justify-center rounded-md p-1',
        props.class,
      )
    "
  >
    <slot></slot>
  </TabsList>
</template>
