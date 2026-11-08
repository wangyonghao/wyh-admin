<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { RadioGroupRootEmits, RadioGroupRootProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../../../../lib/cn';

import { RadioGroupRoot, useForwardPropsEmits } from 'reka-ui';

const props = defineProps<RadioGroupRootProps & { class?: any }>();
const emits = defineEmits<RadioGroupRootEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <RadioGroupRoot :class="cn('grid gap-2', props.class)" v-bind="forwarded">
    <slot></slot>
  </RadioGroupRoot>
</template>
