<script setup lang="ts">
import type { CustomRenderType } from '../types';

import {
  VbenHelpTooltip,
  VbenRenderContent,
} from '@wyh/ui/composed';
import { FormLabel } from '@wyh/ui/form';
import { cn } from '@wyh/ui/lib/cn';

interface Props {
  class?: string;
  colon?: boolean;
  help?: CustomRenderType;
  label?: CustomRenderType;
  required?: boolean;
}

const props = defineProps<Props>();
</script>

<template>
  <FormLabel :class="cn('flex items-center', props.class)">
    <span v-if="required" class="mr-0.5 text-destructive">*</span>
    <slot></slot>
    <VbenHelpTooltip v-if="help" trigger-class="size-3.5 ml-1">
      <VbenRenderContent :content="help" />
    </VbenHelpTooltip>
    <slot name="extra"></slot>
    <span v-if="colon && label" class="ml-0.5">:</span>
  </FormLabel>
</template>
