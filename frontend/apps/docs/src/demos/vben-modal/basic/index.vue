<script lang="ts" setup>
import { VbenButton } from '@vben/common-ui';
import { useVbenModal } from '#/components/popup/modal';

const [Modal, modalApi] = useVbenModal();
</script>
<template>
  <div>
    <VbenButton @click="() => modalApi.open()">Open</VbenButton>
    <Modal class="w-150" title="基础示例"> modal content </Modal>
  </div>
</template>
