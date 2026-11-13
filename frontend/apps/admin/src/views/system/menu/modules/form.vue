<script setup lang="ts">
import type { Menu } from '#/api/system/menu';

import { ref } from 'vue';

import { useVbenModal } from '#/components/popup/modal';

import { useVbenForm } from '#/components/ui-patterns/form';
import { menuApi } from '#/api/system/menu';
import IconSelect from '#/components/ui/icon-select.vue';
import { toast } from '#/ui-patterns/toast';

import { menuPayload, normalizeMenuParentId } from '../form-values';

interface MenuOption {
  children?: MenuOption[];
  key: number | string;
  label: string;
}

interface MenuFormData {
  menuOptions?: MenuOption[];
  parentId?: string;
  record?: Menu & { isFrame?: number; visible?: number };
}

const emits = defineEmits<{ success: [] }>();

const menuOptions = ref<MenuOption[]>([{ key: '0', label: '顶级菜单' }]);
const editingId = ref<string>();

const typeOptions = [
  { label: '目录', value: 1 },
  { label: '菜单', value: 2 },
  { label: '按钮', value: 3 },
];

const [Form, formApi] = useVbenForm({
  showDefaultActions: false,
  layout: 'horizontal',
  commonConfig: {
    labelWidth: 80,
    componentProps: { class: 'w-full' },
  },
  schema: [
    {
      component: 'TreeSelect',
      fieldName: 'parentId',
      label: '上级菜单',
      defaultValue: '0',
      componentProps: () => ({
        options: menuOptions.value,
        placeholder: '请选择上级菜单',
        defaultExpandAll: true,
        clearable: true,
      }),
    },
    {
      component: 'RadioGroup',
      fieldName: 'type',
      label: '菜单类型',
      defaultValue: 1,
      componentProps: { options: typeOptions },
      rules: 'selectRequired',
    },
    {
      component: 'Input',
      fieldName: 'name',
      label: '菜单名称',
      componentProps: { placeholder: '请输入菜单名称' },
      rules: 'required',
    },
    {
      component: 'Switch',
      fieldName: 'isFrame',
      label: '是否外链',
      defaultValue: 0,
      help: '外链点击后将在新窗口打开',
      componentProps: { checkedValue: 1, uncheckedValue: 0 },
      dependencies: {
        if: (values) => values.type !== 3,
        triggerFields: ['type'],
      },
    },
    {
      component: 'Input',
      fieldName: 'path',
      label: '路由地址',
      componentProps: { placeholder: '请输入路由地址' },
      dependencies: {
        if: (values) => values.type !== 3 && !values.isFrame,
        triggerFields: ['type', 'isFrame'],
      },
    },
    {
      component: 'Input',
      fieldName: 'frameUrl',
      label: '外链地址',
      componentProps: { placeholder: '请输入外链地址，如：https://example.com' },
      dependencies: {
        if: (values) => values.type !== 3 && !!values.isFrame,
        triggerFields: ['type', 'isFrame'],
      },
    },
    {
      component: 'Input',
      fieldName: 'routeComponent',
      label: '组件路径',
      componentProps: { placeholder: '请输入组件路径' },
      dependencies: {
        if: (values) => values.type === 2 && !values.isFrame,
        triggerFields: ['type', 'isFrame'],
      },
    },
    {
      component: 'Input',
      fieldName: 'permission',
      label: '权限标识',
      componentProps: { placeholder: '请输入权限标识，如：system:user:create' },
      dependencies: {
        if: (values) => values.type === 3,
        triggerFields: ['type'],
      },
    },
    {
      component: IconSelect,
      fieldName: 'icon',
      label: '图标',
      modelPropName: 'modelValue',
      dependencies: {
        if: (values) => values.type !== 3,
        triggerFields: ['type'],
      },
    },
    {
      component: 'InputNumber',
      fieldName: 'sort',
      label: '排序',
      defaultValue: 0,
      componentProps: { min: 0, class: 'w-full' },
    },
    {
      component: 'Switch',
      fieldName: 'visible',
      label: '是否可见',
      defaultValue: 1,
      help: (values) => (values.visible === 1 ? '显示' : '隐藏'),
      componentProps: { checkedValue: 1, uncheckedValue: 0 },
      dependencies: {
        if: (values) => values.type !== 3,
        triggerFields: ['type'],
      },
    },
    {
      component: 'Switch',
      fieldName: 'status',
      label: '状态',
      defaultValue: 1,
      help: (values) => (values.status === 1 ? '启用' : '禁用'),
      componentProps: { checkedValue: 1, uncheckedValue: 0 },
    },
  ],
});

const [Modal, modalApi] = useVbenModal({
  class: 'w-[650px]',
  closeOnClickModal: false,
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const payload = menuPayload({
      ...values,
      id: editingId.value,
      icon: values.icon == null ? '' : String(values.icon),
    }) as import('#/api/system/menu').MenuWrite;
    modalApi.lock();
    try {
      if (editingId.value) {
        await menuApi.update(editingId.value, payload);
        toast.success('更新成功');
      } else {
        await menuApi.create(payload);
        toast.success('创建成功');
      }
      emits('success');
      modalApi.close();
    } catch {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<MenuFormData>();
    menuOptions.value = data?.menuOptions?.length
      ? data.menuOptions
      : [{ key: '0', label: '顶级菜单' }];
    const record = data?.record;
    editingId.value = record?.id;
    modalApi.setState({ title: record?.id ? '编辑菜单' : '新增菜单' });
    await formApi.resetForm();
    const component = record?.component ?? '';
    await formApi.setValues({
      parentId: normalizeMenuParentId(record?.parentId ?? data?.parentId),
      name: record?.name ?? '',
      type: record?.type ?? 1,
      path: record?.path ?? '',
      frameUrl: component,
      routeComponent: component,
      permission: record?.permission ?? '',
      icon: record?.icon ?? '',
      sort: record?.sort ?? 0,
      visible: record?.visible ?? 1,
      status: record?.status ?? 1,
      isFrame: record?.isFrame ?? 0,
    });
  },
});
</script>

<template>
  <Modal>
    <Form />
  </Modal>
</template>
