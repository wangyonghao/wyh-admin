<script setup lang="ts">
import type { NoticeCreateReq, NoticeUpdateReq } from '#/api/system/notice';

import { computed, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import { VbenTiptap } from '@vben/plugins/tiptap';

import { z, useVbenForm } from '#/components/ui-patterns/form';
import { noticeApi } from '#/api/system/notice';
import { userApi } from '#/api/system/user';
import { useDict } from '#/hooks';
import { Button } from '#/ui/button';
import { toast } from '#/ui-patterns/toast';

import { buildNoticePayload } from '../form-values';

defineOptions({ name: 'NoticeForm' });

const props = defineProps<{
  noticeId?: string;
}>();

const emit = defineEmits<{
  success: [];
  cancel: [];
}>();

const { notice_type, notice_scope_enum, notice_method_enum } = useDict(
  'notice_type',
  'notice_scope_enum',
  'notice_method_enum',
);

const isUpdate = computed(() => !!props.noticeId);
const disabledEdit = ref(false);
const loading = ref(false);
const userOptions = ref<{ label: string; value: string }[]>([]);

const yesNoOptions = [
  { label: '是', value: 'true' },
  { label: '否', value: 'false' },
];

function dictOptions(list: unknown) {
  if (!Array.isArray(list)) return [];
  return list.flatMap((item) => {
    if (!item || typeof item !== 'object') return [];
    const record = item as { label?: unknown; value?: unknown };
    if (record.value == null) return [];
    return [
      {
        label: record.label == null ? String(record.value) : String(record.label),
        value: String(record.value),
      },
    ];
  });
}

const [Form, formApi] = useVbenForm({
  showDefaultActions: false,
  layout: 'horizontal',
  wrapperClass: 'grid-cols-1 md:grid-cols-2',
  commonConfig: {
    labelWidth: 120,
    componentProps: { class: 'w-full' },
  },
  schema: [
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.notice.title'),
      formItemClass: 'md:col-span-2',
      componentProps: { placeholder: $t('system.notice.title') },
      rules: 'required',
    },
    {
      component: 'Select',
      fieldName: 'type',
      label: $t('system.notice.type'),
      componentProps: () => ({
        options: dictOptions(notice_type?.value),
        placeholder: $t('system.notice.type'),
      }),
      rules: 'selectRequired',
    },
    {
      component: 'RadioGroup',
      fieldName: 'noticeScope',
      label: $t('system.notice.noticeScope'),
      defaultValue: '1',
      componentProps: () => ({
        isButton: true,
        options: dictOptions(notice_scope_enum?.value),
      }),
      rules: 'selectRequired',
    },
    {
      component: 'Select',
      fieldName: 'noticeUsers',
      label: $t('system.notice.noticeUsers'),
      formItemClass: 'md:col-span-2',
      componentProps: () => ({
        options: userOptions.value,
        placeholder: $t('system.notice.noticeUsers'),
        multiple: true,
        filterable: true,
        clearable: true,
      }),
      dependencies: {
        if: (values) => values.noticeScope === '2',
        triggerFields: ['noticeScope'],
        rules: () =>
          z.array(z.union([z.string(), z.number()])).min(1, '请选择通知用户'),
      },
    },
    {
      component: 'CheckboxGroup',
      fieldName: 'noticeMethods',
      label: $t('system.notice.noticeMethods'),
      formItemClass: 'md:col-span-2',
      defaultValue: [],
      componentProps: () => ({
        options: dictOptions(notice_method_enum?.value),
      }),
      rules: z.array(z.union([z.string(), z.number()])).min(1, '请选择通知方式'),
    },
    {
      component: 'RadioGroup',
      fieldName: 'isTiming',
      label: $t('system.notice.isTiming'),
      defaultValue: 'false',
      componentProps: { isButton: true, options: yesNoOptions },
      rules: 'selectRequired',
    },
    {
      component: 'DatePicker',
      fieldName: 'publishTime',
      label: $t('system.notice.publishTime'),
      componentProps: {
        type: 'datetime',
        placeholder: $t('system.notice.publishTime'),
        format: 'yyyy-MM-dd HH:mm:ss',
        class: 'w-full',
      },
      dependencies: {
        if: (values) => values.isTiming === 'true',
        triggerFields: ['isTiming'],
        rules: () =>
          z.number({
            required_error: '请选择发布时间',
            invalid_type_error: '请选择发布时间',
          }),
      },
    },
    {
      component: 'RadioGroup',
      fieldName: 'isTop',
      label: $t('system.notice.isTop'),
      defaultValue: 'false',
      componentProps: { isButton: true, options: yesNoOptions },
      rules: 'selectRequired',
    },
    {
      component: VbenTiptap,
      fieldName: 'content',
      label: $t('system.notice.content'),
      formItemClass: 'md:col-span-2',
      modelPropName: 'modelValue',
      componentProps: { minHeight: 400 },
      rules: 'required',
    },
  ],
});

function applyDisabled() {
  formApi.setState({
    commonConfig: {
      disabled: disabledEdit.value || loading.value,
      labelWidth: 120,
      componentProps: { class: 'w-full' },
    },
  });
}

async function handleReset() {
  disabledEdit.value = false;
  await formApi.resetForm();
  await formApi.setValues({
    title: '',
    content: '',
    type: '',
    noticeScope: '1',
    noticeUsers: null,
    noticeMethods: [],
    isTiming: 'false',
    publishTime: null,
    isTop: 'false',
  });
  applyDisabled();
}

async function handleSubmit() {
  const { valid } = await formApi.validate();
  if (!valid) return;
  loading.value = true;
  applyDisabled();
  try {
    const values = await formApi.getValues();
    const submitData: NoticeCreateReq | NoticeUpdateReq = buildNoticePayload(values);
    if (isUpdate.value) {
      await noticeApi.update(props.noticeId!, submitData);
      toast.success($t('pages.common.modifySuccess'));
    } else {
      await noticeApi.create(submitData);
      toast.success($t('pages.common.addSuccess'));
    }
    emit('success');
  } catch (error) {
    console.error('提交失败:', error);
  } finally {
    loading.value = false;
    applyDisabled();
  }
}

async function loadData() {
  loading.value = true;
  applyDisabled();
  try {
    const users = await userApi.dict({ status: 1 });
    userOptions.value = users.map((item) => ({
      label: item.label,
      value: String(item.value),
    }));

    await handleReset();
    if (!props.noticeId) return;

    const detail = await noticeApi.detail(props.noticeId);
    disabledEdit.value = detail.status === 3;
    await formApi.setValues({
      title: detail.title,
      content: detail.content,
      type: detail.type,
      noticeScope: detail.noticeScope,
      noticeUsers: detail.noticeUsers ? detail.noticeUsers.split(',') : null,
      noticeMethods: detail.noticeMethods ? detail.noticeMethods.split(',') : [],
      isTiming: detail.isTiming,
      publishTime: detail.publishTime
        ? new Date(detail.publishTime).getTime()
        : null,
      isTop: detail.isTop,
    });
  } catch (error) {
    console.error('加载数据失败:', error);
    toast.error('加载数据失败');
  } finally {
    loading.value = false;
    applyDisabled();
  }
}

watch(
  () => props.noticeId,
  () => {
    loadData();
  },
  { immediate: true },
);
</script>

<template>
  <div>
    <Form />
    <div class="mt-6 flex justify-end gap-3">
      <Button
        v-if="!disabledEdit"
        type="button"
        variant="secondary"
        :loading="loading"
        @click="handleSubmit"
      >
        <IconifyIcon icon="lucide:save" class="mr-1 size-4" />
        保存为草稿
      </Button>
      <Button
        v-if="!disabledEdit"
        type="button"
        :loading="loading"
        @click="handleSubmit"
      >
        <IconifyIcon icon="lucide:send" class="mr-1 size-4" />
        发布
      </Button>
      <Button
        v-if="!disabledEdit"
        type="button"
        variant="outline"
        :disabled="loading"
        @click="handleReset"
      >
        <IconifyIcon icon="lucide:rotate-ccw" class="mr-1 size-4" />
        重置
      </Button>
      <Button
        type="button"
        variant="outline"
        :disabled="loading"
        @click="emit('cancel')"
      >
        <IconifyIcon icon="lucide:x" class="mr-1 size-4" />
        取消
      </Button>
      <Button
        v-if="disabledEdit"
        type="button"
        variant="outline"
        disabled
      >
        已发布不可编辑
      </Button>
    </div>
  </div>
</template>
