<script lang="ts" setup>
import { useVbenDrawer } from '#/components/popup/drawer';

const [Drawer] = useVbenDrawer();
</script>
<template>
  <Drawer title="组件抽离示例"> extra drawer content </Drawer>
</template>
