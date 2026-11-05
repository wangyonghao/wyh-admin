<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { NumberFieldIncrementProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../lib/cn';

import { Plus } from 'lucide-vue-next';
import { NumberFieldIncrement, useForwardProps } from 'reka-ui';

const props = defineProps<NumberFieldIncrementProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwarded = useForwardProps(delegatedProps);
</script>

<template>
  <NumberFieldIncrement
    data-slot="increment"
    v-bind="forwarded"
    :class="
      cn(
        'absolute top-1/2 right-0 -translate-y-1/2 p-3 disabled:cursor-not-allowed disabled:opacity-20',
        props.class,
      )
    "
  >
    <slot>
      <Plus class="h-4 w-4" />
    </slot>
  </NumberFieldIncrement>
</template>
