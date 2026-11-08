<script lang="ts" setup>
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { toValue } from 'vue';

import { ErrorMessage } from 'vee-validate';

import { useFormField } from './useFormField';

const { formMessageId, name } = useFormField();
</script>

<template>
  <ErrorMessage
    :id="formMessageId"
    :name="toValue(name)"
    as="p"
    class="text-destructive text-[0.8rem]"
  />
</template>
