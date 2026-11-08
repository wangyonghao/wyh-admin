<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { PaginationNextProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../../../../lib/cn';

import { ChevronRight } from 'lucide-vue-next';
import { PaginationNext } from 'reka-ui';

import { Button } from '../button';

const props = withDefaults(
  defineProps<PaginationNextProps & { class?: any }>(),
  {
    asChild: true,
  },
);

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <PaginationNext v-bind="delegatedProps">
    <Button :class="cn('size-8 p-0', props.class)" variant="outline">
      <slot>
        <ChevronRight class="size-4" />
      </slot>
    </Button>
  </PaginationNext>
</template>
