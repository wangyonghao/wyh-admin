<script lang="ts" setup>
import { VbenButton } from '@vben/common-ui';
import { useVbenDrawer } from '#/components/popup/drawer';

import ExtraDrawer from './drawer.vue';

const [Drawer, drawerApi] = useVbenDrawer({
  // 连接抽离的组件
  connectedComponent: ExtraDrawer,
});

function open() {
  drawerApi.open();
}
</script>

<template>
  <div>
    <Drawer />
    <VbenButton @click="open">Open</VbenButton>
  </div>
</template>
