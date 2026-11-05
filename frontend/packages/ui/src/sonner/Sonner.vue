<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { ToasterProps } from 'vue-sonner';

import { cn } from '../lib/cn';

import { Toaster as Sonner } from 'vue-sonner';

import 'vue-sonner/style.css';

const props = defineProps<ToasterProps>();
</script>

<template>
  <Sonner
    v-bind="props"
    :class="cn('toaster group', props.class)"
    :style="{
      '--normal-bg': 'var(--color-popover)',
      '--normal-text': 'var(--color-popover-foreground)',
      '--normal-border': 'var(--color-border)',
      '--border-radius': 'var(--radius)',
      ...props.style,
    }"
  />
</template>
