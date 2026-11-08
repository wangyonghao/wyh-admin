<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { PaginationEllipsisProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../../../../lib/cn';

import { MoreHorizontal } from 'lucide-vue-next';
import { PaginationEllipsis } from 'reka-ui';

const props = defineProps<PaginationEllipsisProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <PaginationEllipsis
    v-bind="delegatedProps"
    :class="cn('flex size-8 items-center justify-center', props.class)"
  >
    <slot>
      <MoreHorizontal class="size-4" />
    </slot>
  </PaginationEllipsis>
</template>
