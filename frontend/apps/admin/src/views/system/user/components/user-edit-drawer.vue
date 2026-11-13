<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';

import { asSelectList } from '#/components/ui/select-value';
import { useVbenForm, z } from '#/components/ui-patterns/form';
import { userApi } from '#/api/system/user';
import { toast } from '#/ui-patterns/toast';
import {
  USERNAME_WRITE_MESSAGE,
  USERNAME_WRITE_PATTERN,
  acceptsLoginUsername,
} from '#/utils/username';

interface DeptNode {
  children?: DeptNode[];
  id?: number | string;
  name?: string;
}

interface RoleOption {
  label?: unknown;
  value?: unknown;
}

interface Props {
  visible: boolean;
  userId?: string;
  departmentData: DeptNode[];
  roleOptions: RoleOption[];
  defaultDepartmentId?: string;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'success'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const isUpdate = ref(false);

const roleSelectOptions = computed(() =>
  props.roleOptions.flatMap((item) => {
    if (item.label == null || item.value == null) return [];
    if (typeof item.value !== 'string' && typeof item.value !== 'number') {
      return [];
    }
    return [{ label: String(item.label), value: String(item.value) }];
  }),
);

const optionalEmail = z
  .string()
  .email('请输入正确的邮箱格式')
  .or(z.literal(''))
  .nullish();

const optionalPhone = z
  .string()
  .regex(/^1[3-9]\d{9}$/, '请输入正确的手机号码')
  .or(z.literal(''))
  .nullish();

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
      fieldName: 'username',
      label: '用户名',
      componentProps: { placeholder: USERNAME_WRITE_MESSAGE },
      rules: z.string().regex(USERNAME_WRITE_PATTERN, USERNAME_WRITE_MESSAGE),
      dependencies: {
        disabled: () => isUpdate.value,
        // 编辑不改用户名。已有用户名只要求非空，短账号仍可保存其他资料。
        rules: () =>
          isUpdate.value
            ? z.string().refine((value) => acceptsLoginUsername(value), {
                message: '请输入用户名',
              })
            : z
                .string()
                .regex(USERNAME_WRITE_PATTERN, USERNAME_WRITE_MESSAGE),
        triggerFields: ['username'],
      },
    },
    {
      component: 'Input',
      fieldName: 'displayName',
      label: '显示名称',
      componentProps: { placeholder: '请输入显示名称' },
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'password',
      label: '密码',
      componentProps: {
        type: 'password',
        placeholder: '请输入密码',
      },
      rules: 'required',
      dependencies: {
        if: () => !isUpdate.value,
        triggerFields: ['username'],
      },
    },
    {
      component: 'RadioGroup',
      fieldName: 'gender',
      label: '性别',
      defaultValue: 0,
      componentProps: {
        options: [
          { label: '未知', value: 0 },
          { label: '男', value: 1 },
          { label: '女', value: 2 },
        ],
      },
    },
    {
      component: 'Input',
      fieldName: 'email',
      label: '邮箱',
      componentProps: { placeholder: '请输入邮箱' },
      rules: optionalEmail,
    },
    {
      component: 'Input',
      fieldName: 'phone',
      label: '手机号',
      componentProps: { placeholder: '请输入手机号' },
      rules: optionalPhone,
    },
    {
      component: 'TreeSelect',
      fieldName: 'departmentId',
      label: '部门',
      componentProps: () => ({
        options: props.departmentData,
        keyField: 'id',
        labelField: 'name',
        childrenField: 'children',
        placeholder: '请选择部门',
        clearable: true,
        defaultExpandAll: true,
      }),
    },
    {
      component: 'Select',
      fieldName: 'roleIds',
      label: '角色',
      componentProps: () => ({
        options: roleSelectOptions.value,
        placeholder: '请选择角色',
        filterable: true,
        multiple: true,
        clearable: true,
      }),
    },
    {
      component: 'RadioGroup',
      fieldName: 'status',
      label: '状态',
      defaultValue: 1,
      componentProps: {
        options: [
          { label: '启用', value: 1 },
          { label: '禁用', value: 0 },
        ],
      },
    },
    {
      component: 'Textarea',
      fieldName: 'description',
      label: '描述',
      componentProps: {
        placeholder: '请输入描述',
        rows: 3,
      },
    },
  ],
});

async function fillCreate() {
  isUpdate.value = false;
  await formApi.resetForm();
  await formApi.setValues({
    username: '',
    displayName: '',
    password: '',
    gender: 0,
    email: '',
    phone: '',
    departmentId: props.defaultDepartmentId ?? null,
    roleIds: [],
    status: 1,
    description: '',
  });
}

async function fillEdit() {
  if (!props.userId) return;
  isUpdate.value = true;
  drawerApi.lock(true);
  try {
    const res = await userApi.detail(props.userId);
    await formApi.resetForm();
    await formApi.setValues({
      username: res.username ?? '',
      displayName: res.displayName ?? '',
      password: '',
      gender: res.gender ?? 0,
      email: res.email ?? '',
      phone: res.phone ?? '',
      departmentId:
        res.departmentId == null || res.departmentId === '' ? null : String(res.departmentId),
      roleIds: (res.roleIds ?? []).map(String),
      status: res.status ?? 1,
      description: res.description ?? '',
    });
  } catch (error) {
    console.error('加载用户详情失败:', error);
    toast.error('加载用户详情失败');
  } finally {
    drawerApi.unlock();
  }
}

const [Drawer, drawerApi] = useVbenDrawer({
  class: 'w-[480px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const payload = {
      username: String(values.username ?? ''),
      displayName: String(values.displayName ?? ''),
      password: String(values.password ?? ''),
      gender: values.gender ?? 0,
      email: values.email ?? '',
      phone: values.phone ?? '',
      departmentId:
        values.departmentId == null || values.departmentId === ''
          ? undefined
          : String(values.departmentId),
      roleIds: asSelectList(values.roleIds).map(String),
      status: values.status ?? 1,
      description: values.description ?? '',
    };
    drawerApi.lock();
    try {
      if (isUpdate.value && props.userId) {
        const { username: _username, ...updateData } = payload;
        await userApi.update(updateData, props.userId);
        toast.success('修改成功');
      } else {
        await userApi.create(payload);
        toast.success('新增成功');
      }
      emit('success');
      drawerApi.close();
    } catch (error) {
      console.error('保存用户失败:', error);
      drawerApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      emit('update:visible', false);
      return;
    }
    drawerApi.setState({
      title: props.userId ? '编辑用户' : '新增用户',
    });
    if (props.userId) await fillEdit();
    else await fillCreate();
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
  <Drawer :title="isUpdate ? '编辑用户' : '新增用户'">
    <Form />
  </Drawer>
</template>
