<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { SeparatorProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../../../../lib/cn';

import { Separator } from 'reka-ui';

const props = defineProps<SeparatorProps & { class?: any; label?: string }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <Separator
    v-bind="delegatedProps"
    :class="
      cn(
        'bg-border relative shrink-0',
        props.orientation === 'vertical' ? 'h-full w-px' : 'h-px w-full',
        props.class,
      )
    "
  >
    <span
      v-if="props.label"
      :class="
        cn(
          'bg-background text-muted-foreground absolute top-1/2 left-1/2 flex -translate-x-1/2 -translate-y-1/2 items-center justify-center text-xs',
          props.orientation === 'vertical'
            ? 'w-0.25 px-1 py-2'
            : 'h-0.25 px-2 py-1',
        )
      "
    >
      {{ props.label }}
    </span>
  </Separator>
</template>
