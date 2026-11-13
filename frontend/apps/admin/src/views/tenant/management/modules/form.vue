<script lang="ts" setup>
import type { TenantResp } from '#/api/tenant/management';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';
import { $t } from '@vben/locales';

import { useVbenForm } from '#/components/ui-patterns/form';
import { addTenant, listAllTenantPackage, updateTenant } from '#/api/tenant';
import { encryptByRsa } from '#/utils/crypto';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const recordId = ref<string>();

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(() => !recordId.value),
  showDefaultActions: false,
});

function blankToNull(value: unknown) {
  const text = typeof value === 'string' ? value.trim() : '';
  return text || null;
}

function asId(value: unknown) {
  const text = String(value ?? '');
  if (/^\d+$/.test(text)) {
    const numeric = Number(text);
    if (Number.isSafeInteger(numeric)) return numeric;
  }
  return text;
}

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const body: Record<string, unknown> = {
      name: String(values.name ?? '').trim(),
      domain: blankToNull(values.domain),
      expireTime: values.expireTime || null,
      description: blankToNull(values.description),
      status: values.status,
      packageId: asId(values.packageId),
    };
    if (!recordId.value) {
      body.adminUsername = String(values.adminUsername ?? '').trim();
      body.adminPassword = encryptByRsa(String(values.adminPassword ?? '')) || '';
    }
    drawerApi.lock();
    const request = recordId.value
      ? updateTenant(body, recordId.value)
      : addTenant(body);
    request
      .then(() => {
        emits('success');
        drawerApi.close();
      })
      .catch(() => {
        drawerApi.unlock();
      });
  },
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = drawerApi.getData<TenantResp>();
    recordId.value = data?.id ? String(data.id) : undefined;
    formApi.resetForm();
    const packages = await listAllTenantPackage();
    formApi.updateSchema([
      {
        fieldName: 'packageId',
        componentProps: {
          options: packages.map((item) => ({
            label: item.name,
            value: String(item.id),
          })),
          filterable: true,
        },
      },
    ]);
    if (data?.id) {
      formApi.setValues({
        ...data,
        id: String(data.id),
        packageId: data.packageId == null ? undefined : String(data.packageId),
      });
    }
  },
});

const title = computed(() =>
  recordId.value ? $t('pages.common.edit') : $t('pages.common.add'),
);
</script>

<template>
  <Drawer :title="title">
    <Form />
  </Drawer>
</template>
