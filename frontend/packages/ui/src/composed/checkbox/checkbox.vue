<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { CheckboxRootEmits, CheckboxRootProps } from 'reka-ui';

import { useId } from 'vue';

import { useForwardPropsEmits } from 'reka-ui';

import { Checkbox } from '../../checkbox';

const props = defineProps<CheckboxRootProps & { indeterminate?: boolean }>();

const emits = defineEmits<CheckboxRootEmits>();

const checked = defineModel<boolean>();

const forwarded = useForwardPropsEmits(props, emits);

const id = useId();
</script>

<template>
  <div class="flex items-center">
    <Checkbox v-bind="forwarded" :id="id" v-model="checked" />
    <label :for="id" class="ml-2 cursor-pointer text-sm"> <slot></slot> </label>
  </div>
</template>
