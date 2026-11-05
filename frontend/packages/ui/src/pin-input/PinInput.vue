<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { PinInputRootEmits, PinInputRootProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '../lib/cn';

import { PinInputRoot, useForwardPropsEmits } from 'reka-ui';

const props = defineProps<PinInputRootProps & { class?: any }>();
const emits = defineEmits<PinInputRootEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;
  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <PinInputRoot
    v-bind="forwarded"
    :class="cn('flex items-center gap-2', props.class)"
  >
    <slot></slot>
  </PinInputRoot>
</template>
