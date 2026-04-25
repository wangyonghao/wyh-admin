<script setup lang="ts">
import type { FormInst, FormRules, SelectOption, TreeSelectOption } from 'naive-ui';
import { reactive, ref, watch } from 'vue';

import { $t } from '@vben/locales';

import {
  NButton,
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NRadio,
  NRadioGroup,
  NSelect,
  NTreeSelect,
  useMessage,
} from 'naive-ui';

import { userApi } from '#/api/system/user';

interface Props {
  visible: boolean;
  userId?: string;
  deptData: TreeSelectOption[];
  roleOptions: SelectOption[];
  defaultDeptId?: string;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'success'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const message = useMessage();

const formRef = ref<FormInst | null>(null);
const submitLoading = ref(false);
const isUpdate = ref(false);

const formData = reactive({
  username: '',
  nickname: '',
  password: '',
  gender: 0,
  email: '',
  phone: '',
  deptId: undefined as string | undefined,
  roleIds: [] as Array<number | string>,
  status: 1,
  description: '',
});

const formRules: FormRules = {
  username: [
    {
      required: true,
      message: `请输入 ${$t('system.user.field.username')}`,
      trigger: 'blur',
    },
  ],
  nickname: [
    {
      required: true,
      message: '请输入昵称',
      trigger: 'blur',
    },
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur',
    },
  ],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: 'blur',
    },
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur',
    },
  ],
};

watch(
  () => props.visible,
  async (newVal) => {
    if (newVal) {
      if (props.userId) {
        // 编辑模式
        isUpdate.value = true;
        await loadUserDetail();
      } else {
        // 新增模式
        resetForm();
      }
    }
  },
);

async function loadUserDetail() {
  if (!props.userId) return;

  try {
    const res = await userApi.detail(props.userId);
    formData.username = res.username ?? '';
    formData.nickname = res.nickname ?? '';
    formData.gender = res.gender ?? 0;
    formData.email = res.email ?? '';
    formData.phone = res.phone ?? '';
    formData.deptId = res.deptId ?? undefined;
    formData.roleIds = res.roleIds ?? [];
    formData.status = res.status ?? 1;
    formData.description = res.description ?? '';
    // 编辑时不需要密码
    formData.password = '';
  } catch (error) {
    console.error('加载用户详情失败:', error);
    message.error('加载用户详情失败');
  }
}

function resetForm() {
  isUpdate.value = false;
  formData.username = '';
  formData.nickname = '';
  formData.password = '';
  formData.gender = 0;
  formData.email = '';
  formData.phone = '';
  formData.deptId = props.defaultDeptId;
  formData.roleIds = [];
  formData.status = 1;
  formData.description = '';
  formRef.value?.restoreValidation();
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  submitLoading.value = true;
  try {
    if (isUpdate.value && props.userId) {
      // 编辑时不传递 username
      const { username: _username, ...updateData } = formData;
      await userApi.update(updateData, props.userId);
      message.success('修改成功');
    } else {
      await userApi.create({ ...formData });
      message.success('新增成功');
    }
    handleClose();
    emit('success');
  } catch (error) {
    console.error('保存用户失败:', error);
  } finally {
    submitLoading.value = false;
  }
}

function handleClose() {
  emit('update:visible', false);
}

function handleAfterLeave() {
  resetForm();
}
</script>

<template>
  <NDrawer
    :show="visible"
    :width="480"
    placement="right"
    @update:show="handleClose"
    @after-leave="handleAfterLeave"
  >
    <NDrawerContent :title="isUpdate ? '编辑用户' : '新增用户'" closable>
      <NForm
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="80"
      >
        <NFormItem label="用户名" path="username">
          <NInput
            v-model:value="formData.username"
            placeholder="请输入用户名"
            :disabled="isUpdate"
          />
        </NFormItem>
        <NFormItem label="昵称" path="nickname">
          <NInput v-model:value="formData.nickname" placeholder="请输入昵称" />
        </NFormItem>
        <NFormItem v-if="!isUpdate" label="密码" path="password">
          <NInput
            v-model:value="formData.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
          />
        </NFormItem>
        <NFormItem label="性别" path="gender">
          <NRadioGroup v-model:value="formData.gender">
            <NRadio :value="0">未知</NRadio>
            <NRadio :value="1">男</NRadio>
            <NRadio :value="2">女</NRadio>
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="邮箱" path="email">
          <NInput v-model:value="formData.email" placeholder="请输入邮箱" />
        </NFormItem>
        <NFormItem label="手机号" path="phone">
          <NInput v-model:value="formData.phone" placeholder="请输入手机号" />
        </NFormItem>
        <NFormItem label="部门" path="deptId">
          <NTreeSelect
            v-model:value="formData.deptId"
            :options="deptData"
            key-field="id"
            label-field="name"
            children-field="children"
            placeholder="请选择部门"
            clearable
          />
        </NFormItem>
        <NFormItem label="角色" path="roleIds">
          <NSelect
            v-model:value="formData.roleIds"
            :options="roleOptions"
            placeholder="请选择角色"
            filterable
            multiple
            clearable
          />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NRadioGroup v-model:value="formData.status">
            <NRadio :value="1">启用</NRadio>
            <NRadio :value="0">禁用</NRadio>
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput
            v-model:value="formData.description"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 5 }"
            placeholder="请输入描述"
          />
        </NFormItem>
      </NForm>

      <template #footer>
        <div class="flex justify-end gap-2">
          <NButton @click="handleClose"> 取消 </NButton>
          <NButton
            type="primary"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            确定
          </NButton>
        </div>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
