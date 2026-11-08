<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { cn } from '../../../../lib/cn';

import { NumberFieldInput } from 'reka-ui';
</script>

<template>
  <NumberFieldInput
    :class="
      cn(
        'border-input placeholder:text-muted-foreground focus-visible:ring-ring flex h-9 w-full rounded-md border bg-transparent py-1 text-center text-sm shadow-xs transition-colors focus-visible:ring-1 focus-visible:outline-hidden disabled:cursor-not-allowed disabled:opacity-50',
      )
    "
    data-slot="input"
  />
</template>
