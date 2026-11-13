<script setup lang="ts">
import type { RoleResp } from '#/api/system/role';

import { computed, ref, watch } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';
import { $t } from '@vben/locales';

import { useVbenForm } from '#/components/ui-patterns/form';
import { roleApi } from '#/api/system/role';
import { toast } from '#/ui-patterns/toast';

interface Props {
  visible?: boolean;
  editingRole?: RoleResp | null;
  copyMode?: boolean;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'success', roleId?: string): void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  editingRole: null,
  copyMode: false,
});

const emit = defineEmits<Emits>();

const copiedMenuIds = ref<string[]>([]);

const isUpdate = computed(() => !!props.editingRole?.id && !props.copyMode);
const isCopy = computed(() => props.copyMode);

const dataScopeOptions = [
  { label: '全部', value: '1' },
  { label: '本部门及以下', value: '2' },
  { label: '本部门', value: '3' },
  { label: '仅本人', value: '4' },
  { label: '自定义', value: '5' },
];

const drawerTitle = computed(() => {
  if (isUpdate.value) return $t('pages.common.edit');
  if (isCopy.value) return '复制角色';
  return $t('pages.common.add');
});

const [Form, formApi] = useVbenForm({
  showDefaultActions: false,
  layout: 'horizontal',
  commonConfig: {
    labelWidth: 80,
    componentProps: { class: 'w-full' },
  },
  schema: [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.role.name'),
      componentProps: { placeholder: $t('system.role.name') },
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'code',
      label: $t('system.role.code'),
      componentProps: { placeholder: $t('system.role.code') },
      rules: 'required',
    },
    {
      component: 'Textarea',
      fieldName: 'description',
      label: $t('system.role.description'),
      componentProps: { rows: 2 },
    },
    {
      component: 'InputNumber',
      fieldName: 'sort',
      label: $t('system.role.sort'),
      defaultValue: 0,
      componentProps: { min: 0, class: 'w-full' },
      rules: 'required',
    },
    {
      component: 'RadioGroup',
      fieldName: 'dataScope',
      label: $t('system.role.dataScope'),
      defaultValue: '1',
      componentProps: {
        optionType: 'button',
        options: dataScopeOptions,
      },
      rules: 'selectRequired',
    },
  ],
});

async function resetForm() {
  copiedMenuIds.value = [];
  await formApi.resetForm();
  await formApi.setValues({
    name: '',
    code: '',
    description: '',
    sort: 0,
    dataScope: '1',
  });
}

async function loadRoleDetail(roleId: string, isCopyMode = false) {
  try {
    const res = await roleApi.detail(roleId);
    if (isCopyMode) {
      copiedMenuIds.value = (res.menuIds ?? []).map(String);
      await formApi.setValues({
        name: `${res.name ?? ''} - 副本`,
        code: `${res.code ?? ''}_copy`,
        description: res.description ?? '',
        sort: Number(res.sort) || 0,
        dataScope: res.dataScope ?? '1',
      });
      return;
    }
    copiedMenuIds.value = [];
    await formApi.setValues({
      name: res.name ?? '',
      code: res.code ?? '',
      description: res.description ?? '',
      sort: Number(res.sort) || 0,
      dataScope: res.dataScope ?? '1',
    });
  } catch {
    toast.error('加载角色详情失败');
  }
}

const [Drawer, drawerApi] = useVbenDrawer({
  class: 'w-[420px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const payload = {
      name: String(values.name ?? ''),
      code: String(values.code ?? ''),
      description: String(values.description ?? ''),
      sort: typeof values.sort === 'number' ? values.sort : 0,
      dataScope: String(values.dataScope ?? '1'),
    };
    drawerApi.lock();
    try {
      if (isUpdate.value && props.editingRole?.id) {
        await roleApi.update(payload, props.editingRole.id);
        toast.success($t('pages.common.modifySuccess'));
        emit('success');
        drawerApi.close();
        return;
      }
      const createRes = await roleApi.create(payload);
      toast.success($t('pages.common.addSuccess'));
      if (isCopy.value && copiedMenuIds.value.length > 0 && createRes?.id) {
        try {
          await roleApi.updatePermission(createRes.id, {
            menuIds: copiedMenuIds.value,
          });
          toast.success('权限复制成功');
        } catch {
          toast.warning('角色创建成功，但权限复制失败');
        }
      }
      emit('success', createRes?.id);
      drawerApi.close();
    } catch {
      drawerApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      emit('update:visible', false);
      await resetForm();
      return;
    }
    await resetForm();
    if (props.editingRole?.id) {
      await loadRoleDetail(props.editingRole.id, props.copyMode);
    }
  },
});

watch(
  () => props.visible,
  (open) => {
    if (open) drawerApi.open();
    else drawerApi.close();
  },
);
</script>

<template>
  <Drawer :title="drawerTitle">
    <Form />
  </Drawer>
</template>
