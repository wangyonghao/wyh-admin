<script lang="ts" setup>
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { provide, useId } from 'vue';

import { cn } from '../../../../lib/cn';

import { FORM_ITEM_INJECTION_KEY } from './injectionKeys';

const props = defineProps<{
  class?: any;
}>();

const id = useId() as string;
provide(FORM_ITEM_INJECTION_KEY, id);
</script>

<template>
  <div :class="cn(props.class)">
    <slot></slot>
  </div>
</template>
