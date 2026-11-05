<script setup lang="ts">
// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import { useScrollLock } from '@wyh/composables';

useScrollLock();
</script>
<template>
  <div class="z-popup bg-overlay inset-0"></div>
</template>
