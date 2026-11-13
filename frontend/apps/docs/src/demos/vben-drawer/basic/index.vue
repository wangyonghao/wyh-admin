<script lang="ts" setup>
import { VbenButton } from '@vben/common-ui';
import { useVbenDrawer } from '#/components/popup/drawer';

const [Drawer, drawerApi] = useVbenDrawer();
</script>
<template>
  <div>
    <VbenButton @click="() => drawerApi.open()">Open</VbenButton>
    <Drawer class="w-150" title="基础示例"> drawer content </Drawer>
  </div>
</template>
