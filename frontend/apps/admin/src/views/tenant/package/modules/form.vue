<script lang="ts" setup>
import type { TenantPackageResp } from '#/api/tenant/package';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';
import { $t } from '@vben/locales';

import { useVbenForm } from '#/components/ui-patterns/form';
import { menuApi } from '#/api/system/menu';
import {
  addTenantPackage,
  getTenantPackage,
  updateTenantPackage,
} from '#/api/tenant';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const recordId = ref<string>();

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});

function asId(value: unknown) {
  const text = String(value ?? '');
  if (/^\d+$/.test(text)) {
    const numeric = Number(text);
    if (Number.isSafeInteger(numeric)) return numeric;
  }
  return text;
}

function menuLabel(menu: { name?: null | string; permission?: null | string }) {
  const name = menu.name?.trim() || '未命名';
  const permission = menu.permission?.trim();
  return permission ? `${name} (${permission})` : name;
}

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const menuIds = Array.isArray(values.menuIds) ? values.menuIds.map(asId) : [];
    const body = {
      name: String(values.name ?? '').trim(),
      sort: values.sort ?? 1,
      menuCheckStrictly: values.menuCheckStrictly ?? true,
      description:
        typeof values.description === 'string' && values.description.trim()
          ? values.description.trim()
          : null,
      status: values.status,
      menuIds,
    };
    drawerApi.lock();
    const request = recordId.value
      ? updateTenantPackage(body, recordId.value)
      : addTenantPackage(body);
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
    const data = drawerApi.getData<TenantPackageResp>();
    recordId.value = data?.id ? String(data.id) : undefined;
    formApi.resetForm();
    const menus = await menuApi.all();
    formApi.updateSchema([
      {
        fieldName: 'menuIds',
        componentProps: {
          options: menus.map((menu) => ({
            label: menuLabel(menu),
            value: String(menu.id),
          })),
        },
      },
    ]);
    if (!recordId.value) return;
    const detail = await getTenantPackage(recordId.value);
    formApi.setValues({
      ...detail,
      menuIds: detail.menuIds ?? [],
    });
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
