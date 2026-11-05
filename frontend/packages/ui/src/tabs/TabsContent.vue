<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { TabsContentProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../lib/cn';

import { TabsContent } from 'reka-ui';

const props = defineProps<TabsContentProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <TabsContent
    :class="
      cn(
        'ring-offset-background focus-visible:ring-ring mt-2 focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:outline-hidden',
        props.class,
      )
    "
    v-bind="delegatedProps"
  >
    <slot></slot>
  </TabsContent>
</template>
