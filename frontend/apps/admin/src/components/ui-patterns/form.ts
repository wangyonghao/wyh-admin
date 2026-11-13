import type {
  VbenFormProps as FormProps,
  VbenFormSchema as FormSchema,
} from '@vben/common-ui';

import type { ComponentPropsMap, ComponentType } from './component';

import { setupVbenForm, useVbenForm as useForm, z } from '@vben/common-ui';
import { $t } from '@vben/locales';

async function initSetupVbenForm() {
  setupVbenForm<ComponentType>({
    config: {
      // 空值保持 null，不能是 undefined，否则重置不生效。
      // 单行、文本域、密码框把 null 显示成空字符串，用户清空后回写空字符串。
      // 成对输入两段都空时回写 null。
      // Select / Radio、树选择和日期把 null 显示成未选，清除后回写 null。
      // 日期没有 valueFormat 时提交时间戳；有 valueFormat 时提交格式化字符串。
      // InputNumber 清空回写 null，0 仍是数字。Switch 把 null 显示为关。
      // TimePicker 清空回写 null，0 仍是时间戳；有 valueFormat 时提交格式化字符串。
      // Upload 的 fileList 为空时回写 null，不回写空数组。
      emptyStateValue: null,
      baseModelPropName: 'value',
      modelPropNameMap: {
        Checkbox: 'checked',
        Radio: 'checked',
        Upload: 'fileList',
      },
    },
    defineRules: {
      required: (value, _params, ctx) => {
        if (value === undefined || value === null || value.length === 0) {
          return $t('ui.formRules.required', [ctx.label]);
        }
        return true;
      },
      selectRequired: (value, _params, ctx) => {
        if (value === undefined || value === null) {
          return $t('ui.formRules.selectRequired', [ctx.label]);
        }
        return true;
      },
    },
  });
}

const useVbenForm = useForm<ComponentType, ComponentPropsMap>;

export { initSetupVbenForm, useVbenForm, z };

export type VbenFormSchema = FormSchema<ComponentType, ComponentPropsMap>;
export type VbenFormProps = FormProps<ComponentType, ComponentPropsMap>;
