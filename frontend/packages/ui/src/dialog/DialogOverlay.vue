<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { inject } from 'vue';

import { useScrollLock } from '@wyh/composables';

useScrollLock();
const id = inject('DISMISSABLE_MODAL_ID');
</script>
<template>
  <div :data-dismissable-modal="id" class="z-popup bg-overlay inset-0"></div>
</template>
