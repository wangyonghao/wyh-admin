<script lang="ts" setup>
import { useVbenModal } from '#/components/popup/modal';

const [Modal] = useVbenModal();
</script>
<template>
  <Modal title="组件抽离示例"> extra modal content </Modal>
</template>
