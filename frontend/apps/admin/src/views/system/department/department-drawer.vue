<script setup lang="ts">
import type { DepartmentResult } from '#/api/system/department';

import { computed, ref, watch } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';
import { $t } from '@vben/locales';

import { useVbenForm } from '#/components/ui-patterns/form';
import { departmentApi } from '#/api/system/department';
import { useDict } from '#/hooks/app';
import { toast } from '#/ui-patterns/toast';

import {
  buildDepartmentSubmit,
  departmentTypeOptions,
  departmentTypeValue,
} from './department-form';

interface Props {
  visible?: boolean;
  data?: DepartmentResult;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  data: undefined,
});

const emits = defineEmits<{
  success: [];
  'update:visible': [value: boolean];
}>();

const { department_type } = useDict('department_type');

const recordId = ref('');
const isUpdate = computed(() => !!recordId.value);
const drawerTitle = computed(() =>
  isUpdate.value ? $t('pages.common.edit') : $t('pages.common.add'),
);

interface DeptTreeOption {
  children?: DeptTreeOption[];
  key: string;
  label: string;
  value: string;
}

function convertToTreeSelectOptions(depts: DepartmentResult[]): DeptTreeOption[] {
  return depts.map((dept) => ({
    label: dept.name,
    key: dept.id,
    value: dept.id,
    children: dept.children
      ? convertToTreeSelectOptions(dept.children)
      : undefined,
  }));
}

const [Form, formApi] = useVbenForm({
  showDefaultActions: false,
  layout: 'horizontal',
  commonConfig: {
    labelWidth: 100,
    componentProps: { class: 'w-full' },
  },
  schema: [
    {
      component: 'TreeSelect',
      fieldName: 'parentId',
      label: $t('system.dept.parentId'),
      componentProps: {
        options: [],
        clearable: true,
        defaultExpandAll: true,
        placeholder: $t('ui.formRules.selectRequired'),
      },
      rules: 'selectRequired',
    },
    {
      component: 'Input',
      fieldName: 'code',
      label: $t('system.dept.code'),
      componentProps: { placeholder: $t('ui.formRules.required') },
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.dept.name'),
      componentProps: { placeholder: $t('ui.formRules.required') },
      rules: 'required',
    },
    {
      component: 'Select',
      fieldName: 'type',
      label: $t('system.dept.type'),
      componentProps: {
        options: [],
        clearable: true,
        placeholder: $t('ui.formRules.selectRequired'),
      },
      rules: 'selectRequired',
    },
    {
      component: 'InputNumber',
      fieldName: 'sort',
      label: $t('system.dept.sort'),
      defaultValue: 1,
      componentProps: {
        min: 0,
        placeholder: $t('ui.formRules.required'),
      },
      rules: 'required',
    },
    {
      component: 'Textarea',
      fieldName: 'description',
      label: $t('system.dept.description'),
      componentProps: {
        rows: 3,
        placeholder: $t('system.dept.description'),
      },
    },
    {
      component: 'Switch',
      fieldName: 'status',
      label: $t('system.dept.status'),
      defaultValue: 1,
      componentProps: {
        checkedValue: 1,
        uncheckedValue: 2,
      },
    },
  ],
});

async function loadDeptOptions() {
  try {
    const deptArray = await departmentApi.tree({});
    formApi.updateSchema([
      {
        fieldName: 'parentId',
        componentProps: {
          options: convertToTreeSelectOptions(deptArray),
          clearable: true,
          defaultExpandAll: true,
          placeholder: $t('ui.formRules.selectRequired'),
        },
      },
      {
        fieldName: 'type',
        componentProps: {
          options: departmentTypeOptions(department_type?.value),
          clearable: true,
          placeholder: $t('ui.formRules.selectRequired'),
        },
      },
    ]);
  } catch (error) {
    console.error('Failed to load dept options:', error);
  }
}

async function resetForm() {
  recordId.value = '';
  await formApi.resetForm();
  await formApi.setValues({
    parentId: null,
    code: '',
    name: '',
    type: null,
    sort: 1,
    description: '',
    status: 1,
  });
}

async function loadDeptDetail(id: string) {
  const res = await departmentApi.get(id);
  recordId.value = String(res.id);
  await formApi.setValues({
    parentId:
      res.parentId == null || res.parentId === '' ? null : String(res.parentId),
    code: res.code,
    name: res.name,
    type: departmentTypeValue(res.type) || null,
    sort: res.sort,
    description: res.description ?? '',
    status: res.status === 2 ? 2 : 1,
  });
}

const [Drawer, drawerApi] = useVbenDrawer({
  class: 'w-[600px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const submitData = buildDepartmentSubmit(values, recordId.value);
    drawerApi.lock();
    try {
      if (isUpdate.value) {
        await departmentApi.update(submitData, recordId.value);
        toast.success($t('pages.common.modifySuccess'));
      } else {
        await departmentApi.create(submitData);
        toast.success($t('pages.common.addSuccess'));
      }
      emits('success');
      drawerApi.close();
    } catch (error) {
      console.error('Form validation or submission failed:', error);
      drawerApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      emits('update:visible', false);
      await resetForm();
      return;
    }
    drawerApi.setState({ loading: true });
    try {
      await resetForm();
      await loadDeptOptions();
      if (props.data?.id) {
        await loadDeptDetail(props.data.id);
      }
    } catch (error) {
      console.error('Failed to load dept detail:', error);
    } finally {
      drawerApi.setState({ loading: false });
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

watch(
  () => department_type?.value,
  (options) => {
    formApi.updateSchema([
      {
        fieldName: 'type',
        componentProps: {
          options: departmentTypeOptions(options),
          clearable: true,
          placeholder: $t('ui.formRules.selectRequired'),
        },
      },
    ]);
  },
);
</script>

<template>
  <Drawer :title="drawerTitle">
    <Form />
  </Drawer>
</template>
