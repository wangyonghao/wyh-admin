<script lang="ts" setup>
import { VbenButton } from '@vben/common-ui';
import { useVbenModal } from '#/components/popup/modal';

import ExtraModal from './modal.vue';

const [Modal, modalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: ExtraModal,
});

function openModal() {
  modalApi.open();
}
</script>

<template>
  <div>
    <Modal />
    <VbenButton @click="openModal">Open</VbenButton>
  </div>
</template>
