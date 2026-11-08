<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { SplitterGroupEmits, SplitterGroupProps } from 'reka-ui';

import type { HTMLAttributes } from 'vue';

import { computed } from 'vue';

import { cn } from '../../../../lib/cn';

import { SplitterGroup, useForwardPropsEmits } from 'reka-ui';

const props = defineProps<
  SplitterGroupProps & { class?: HTMLAttributes['class'] }
>();
const emits = defineEmits<SplitterGroupEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;
  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <SplitterGroup
    v-bind="forwarded"
    :class="
      cn(
        'flex h-full w-full data-[panel-group-direction=vertical]:flex-col',
        props.class,
      )
    "
  >
    <slot></slot>
  </SplitterGroup>
</template>
