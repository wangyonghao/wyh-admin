/**
 * 动态表单控件实际接受的 props。
 * 以前这里用 naive-ui 的 props 类型做 schema 提示，控件本身已经不是 Naive。
 * 空值仍由各控件回写（文本清空是空字符串，下拉 / 数字 / 开关的空是 null）。
 */

import type { SwitchScalar } from '#/ui/switch';

export interface FormTextProps {
  autosize?: boolean | { maxRows?: number; minRows?: number };
  class?: unknown;
  clearable?: boolean;
  disabled?: boolean;
  maxLength?: number;
  maxlength?: number;
  pair?: boolean;
  placeholder?: string | [string, string];
  rows?: number;
  separator?: string;
  showCount?: boolean;
  showPasswordOn?: string;
  showWordLimit?: boolean;
  type?: string;
  value?: null | number | string | [unknown, unknown];
}

export interface FormSelectProps {
  class?: unknown;
  clearable?: boolean;
  disabled?: boolean;
  filterable?: boolean;
  multiple?: boolean;
  options?: unknown;
  placeholder?: string;
  style?: unknown;
  tag?: boolean;
  value?: unknown;
}

export interface FormCheckboxProps {
  checked?: boolean | null;
  class?: unknown;
  disabled?: boolean;
}

export interface FormCheckboxGroupProps {
  class?: unknown;
  disabled?: boolean;
  options?: unknown;
  value?: unknown;
}

export interface FormRadioGroupProps {
  buttonStyle?: string;
  class?: unknown;
  disabled?: boolean;
  isButton?: boolean;
  optionType?: string;
  options?: unknown;
  value?: unknown;
}

export interface FormSwitchProps {
  checkedValue?: SwitchScalar;
  class?: unknown;
  disabled?: boolean;
  uncheckedValue?: SwitchScalar;
  value?: null | SwitchScalar;
}

export interface FormInputNumberProps {
  class?: unknown;
  clearable?: boolean;
  disabled?: boolean;
  max?: number;
  min?: number;
  placeholder?: string;
  precision?: number;
  step?: number;
  value?: null | number | string;
}
