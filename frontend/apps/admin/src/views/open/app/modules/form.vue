<script lang="ts" setup>
import type { OpenAppApi } from '#/api/open/app';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';
import { $t } from '@vben/locales';

import { useVbenForm } from '#/components/ui-patterns/form';
import { openAppApi } from '#/api/open/app';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const formData = ref<OpenAppApi.AppResp>();

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});

const id = ref();
const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    drawerApi.lock();
    (id.value ? openAppApi.update(id.value, values as OpenAppApi.AppUpdateReq) : openAppApi.create(values as OpenAppApi.AppAddReq))
      .then(() => {
        emits('success');
        drawerApi.close();
      })
      .catch(() => {
        drawerApi.unlock();
      });
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<OpenAppApi.AppResp>();
      formApi.resetForm();
      if (data) {
        formData.value = data;
        id.value = data.id;
        formApi.setValues(data);
      } else {
        id.value = undefined;
      }
    }
  },
});

const getDrawerTitle = computed(() => {
  return formData.value?.id ? $t('pages.common.edit') : $t('pages.common.add');
});
</script>

<template>
  <Drawer :title="getDrawerTitle">
    <Form />
  </Drawer>
</template>
