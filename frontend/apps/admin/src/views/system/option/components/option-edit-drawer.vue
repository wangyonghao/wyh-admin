<script setup lang="ts">
import type { OptionRequest, OptionResult } from '#/api/system/option';

import { computed, ref, watch } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';

import { useVbenForm, z, type VbenFormSchema } from '#/components/ui-patterns/form';
import { optionApi } from '#/api/system';
import { toast } from '#/ui-patterns/toast';

interface Props {
  visible: boolean;
  optionData?: OptionResult;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'success'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const isEdit = computed(() => !!props.optionData);
const drawerTitle = computed(() => (isEdit.value ? '编辑选项' : '新建选项'));
const extra = ref<OptionRequest['extra']>();

const optionTypePattern =
  /^[a-zA-Z][a-zA-Z0-9_]{1,29}$/;
const optionTypeMessage =
  '选项类型长度为 2-30 个字符，支持大小写字母、数字、下划线，以字母开头';

function useOptionSchema(typeDisabled: boolean): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'optionType',
      label: '选项类型',
      componentProps: {
        placeholder: '请输入选项类型，如：notice_type',
        disabled: typeDisabled,
        maxLength: 30,
        showCount: true,
      },
      rules: z
        .string({
          required_error: '请输入选项类型',
          invalid_type_error: '请输入选项类型',
        })
        .min(1, '请输入选项类型')
        .regex(optionTypePattern, optionTypeMessage),
    },
    {
      component: 'Input',
      fieldName: 'value',
      label: '选项值',
      componentProps: {
        placeholder: '请输入选项值，如：1',
        maxLength: 255,
        showCount: true,
      },
      rules: z
        .string({
          required_error: '请输入选项值',
          invalid_type_error: '请输入选项值',
        })
        .min(1, '请输入选项值')
        .max(255, '选项值长度不能超过 255 个字符'),
    },
    {
      component: 'Input',
      fieldName: 'label',
      label: '选项标签',
      componentProps: {
        placeholder: '请输入选项标签，如：产品新闻',
        maxLength: 255,
        showCount: true,
      },
      rules: z
        .string({
          required_error: '请输入选项标签',
          invalid_type_error: '请输入选项标签',
        })
        .min(1, '请输入选项标签')
        .max(255, '选项标签长度不能超过 255 个字符'),
    },
    {
      component: 'InputNumber',
      fieldName: 'sort',
      label: '排序',
      defaultValue: 0,
      componentProps: {
        placeholder: '请输入排序',
        min: 0,
        class: 'w-full',
      },
    },
    {
      component: 'Switch',
      fieldName: 'enabled',
      label: '状态',
      defaultValue: true,
    },
    {
      component: 'Textarea',
      fieldName: 'description',
      label: '描述',
      componentProps: {
        placeholder: '请输入描述',
        rows: 3,
        maxLength: 500,
        showCount: true,
      },
      rules: z
        .string()
        .max(500, '描述长度不能超过 500 个字符')
        .nullish(),
    },
  ];
}

const [Form, formApi] = useVbenForm({
  schema: useOptionSchema(false),
  showDefaultActions: false,
  layout: 'horizontal',
  commonConfig: {
    labelWidth: 100,
    componentProps: { class: 'w-full' },
  },
});

async function fillForm() {
  await formApi.resetForm();
  if (props.optionData) {
    extra.value = props.optionData.ext;
    await formApi.setValues({
      optionType: props.optionData.optionType,
      value: props.optionData.value,
      label: props.optionData.label,
      sort: props.optionData.sort ?? 0,
      enabled: props.optionData.enabled,
      description: props.optionData.description || '',
    });
  } else {
    extra.value = undefined;
    await formApi.setValues({
      optionType: '',
      value: '',
      label: '',
      sort: 0,
      enabled: true,
      description: '',
    });
  }
  formApi.updateSchema([
    {
      fieldName: 'optionType',
      componentProps: {
        placeholder: '请输入选项类型，如：notice_type',
        disabled: isEdit.value,
        maxLength: 30,
        showCount: true,
      },
    },
  ]);
}

const [Drawer, drawerApi] = useVbenDrawer({
  class: 'w-[600px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      toast.error('请检查表单填写是否正确');
      return;
    }
    const values = await formApi.getValues();
    const payload: OptionRequest = {
      optionType: String(values.optionType ?? ''),
      value: String(values.value ?? ''),
      label: String(values.label ?? ''),
      extra: extra.value,
      sort: typeof values.sort === 'number' ? values.sort : 0,
      enabled: values.enabled !== false,
      description: values.description ? String(values.description) : '',
    };
    drawerApi.lock();
    try {
      if (isEdit.value && props.optionData) {
        await optionApi.update(props.optionData.id, payload);
        toast.success('修改成功');
      } else {
        await optionApi.create(payload);
        toast.success('新建成功');
      }
      emit('success');
      drawerApi.close();
    } catch (error) {
      console.error('提交失败:', error);
      toast.error(isEdit.value ? '修改失败' : '新建失败');
      drawerApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      emit('update:visible', false);
      return;
    }
    await fillForm();
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
