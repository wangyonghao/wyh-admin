export { default as VbenCollapsibleParams } from './collapsible-params.vue';
export type {
  CollapsibleParamSchema,
  CollapsibleParamsProps,
} from './collapsible-params-type';

export { setupVbenForm } from './config';

export type {
  BaseFormComponentType,
  ExtendedFormApi,
  FormLayout,
  VbenFormProps,
  FormSchema as VbenFormSchema,
} from './types';

export * from './use-vben-form';
// export { default as VbenForm } from './vben-form.vue';
export * as z from 'zod';
