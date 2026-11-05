<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.
import type { PrimitiveProps } from 'reka-ui';

import type { ButtonVariants, ButtonVariantSize } from './types';

import { computed } from 'vue';

import { LoaderCircle } from 'lucide-vue-next';
import { Primitive } from 'reka-ui';

import { cn } from '../lib/cn';
import { buttonVariants } from './button';

interface Props extends PrimitiveProps {
  class?: any;
  disabled?: boolean;
  loading?: boolean;
  size?: ButtonVariantSize;
  variant?: ButtonVariants;
}

const props = withDefaults(defineProps<Props>(), {
  as: 'button',
  class: '',
  disabled: false,
  loading: false,
});

const isDisabled = computed(() => props.disabled || props.loading);
</script>

<template>
  <Primitive
    :as="as"
    :as-child="asChild"
    :disabled="isDisabled"
    :aria-busy="loading || undefined"
    :class="cn(buttonVariants({ variant, size }), props.class)"
  >
    <LoaderCircle
      v-if="loading"
      class="mr-2 size-4 shrink-0 animate-spin"
    />
    <slot />
  </Primitive>
</template>
