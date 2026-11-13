import type {
  MailConfig,
  LoginConfig,
  RegisterConfig,
  SecurityConfig,
  SiteConfig,
  SmsConfig,
  StorageConfig,
} from '#/api/system/config';

import type { Ref } from 'vue';

import { useVbenForm, type VbenFormSchema } from '#/components/ui-patterns/form';

function text(label: string, fieldName: string, placeholder: string): VbenFormSchema {
  return {
    component: 'Input',
    fieldName,
    label,
    componentProps: { placeholder },
  };
}

function secret(label: string, fieldName: string, placeholder: string): VbenFormSchema {
  return {
    component: 'Input',
    fieldName,
    label,
    componentProps: { type: 'password', placeholder },
  };
}

function numberField(
  label: string,
  fieldName: string,
  placeholder: string,
  min: number,
  max: number,
  help?: string,
): VbenFormSchema {
  return {
    component: 'InputNumber',
    fieldName,
    label,
    help,
    componentProps: { min, max, placeholder, class: 'w-full' },
  };
}

function check(
  label: string,
  fieldName: string,
  textLabel: string,
): VbenFormSchema {
  return {
    component: 'Checkbox',
    fieldName,
    label,
    renderComponentContent: () => ({
      default: () => textLabel,
    }),
  };
}

const formShell = {
  showDefaultActions: false,
  layout: 'horizontal' as const,
  commonConfig: {
    labelWidth: 120,
    componentProps: { class: 'w-full' },
  },
};

export function useConfigForms(options: {
  email: Ref<MailConfig>;
  emailVerified: Ref<boolean>;
  login: Ref<LoginConfig>;
  register: Ref<RegisterConfig>;
  security: Ref<SecurityConfig>;
  site: Ref<SiteConfig>;
  sms: Ref<SmsConfig>;
  storage: Ref<StorageConfig>;
  captchaTypeOptions: { label: string; value: string }[];
  emailProtectionOptions: { label: string; value: boolean }[];
  smsProviderOptions: { label: string; value: string }[];
  storageTypeOptions: { label: string; value: string }[];
}) {
  function mirror<T extends object>(target: Ref<T>) {
    return (values: Record<string, unknown>) => {
      target.value = { ...target.value, ...values } as T;
    };
  }

  const [SiteForm, siteFormApi] = useVbenForm({
    ...formShell,
    handleValuesChange: mirror(options.site),
    schema: [
      text('站点名称', 'siteName', '请输入站点名称'),
      text('站点Logo', 'siteLogo', '请输入Logo URL'),
      text('版权信息', 'siteCopyright', '请输入版权信息'),
      text('ICP备案号', 'siteIcp', '请输入ICP备案号'),
    ],
  });

  const [LoginForm, loginFormApi] = useVbenForm({
    ...formShell,
    handleValuesChange: mirror(options.login),
    schema: [
      numberField(
        '验证码开启阈值',
        'captchaErrorThreshold',
        '-1 不开启；0 始终开启；1~5 为错误次数阈值',
        -1,
        5,
        '-1 不开启；0 始终开启；1~5 表示密码错误达到该次数后开启（须小于最大重试次数）',
      ),
      {
        component: 'Select',
        fieldName: 'captchaType',
        label: '验证码类型',
        componentProps: {
          options: options.captchaTypeOptions,
          placeholder: '请选择验证码类型',
        },
      },
      numberField('最大重试次数', 'maxRetry', '请输入最大重试次数', 1, 10),
      numberField('锁定时间(分钟)', 'lockTime', '请输入锁定时间', 1, 1440),
      numberField(
        '会话超时时间(分钟)',
        'sessionTimeout',
        '请输入会话超时时间',
        5,
        1440,
      ),
    ],
  });

  const [RegisterForm, registerFormApi] = useVbenForm({
    ...formShell,
    handleValuesChange: mirror(options.register),
    schema: [
      check('开启注册', 'enabled', '启用'),
      check('邮箱验证', 'verifyEmail', '需要邮箱验证'),
      check('手机验证', 'verifyPhone', '需要手机验证'),
      text('默认角色ID', 'defaultRoleId', '请输入默认角色ID'),
    ],
  });

  const [SmsForm, smsFormApi] = useVbenForm({
    ...formShell,
    handleValuesChange: mirror(options.sms),
    schema: [
      {
        component: 'Select',
        fieldName: 'provider',
        label: '服务商',
        componentProps: {
          options: options.smsProviderOptions,
          placeholder: '请选择短信服务商',
        },
      },
      secret('AccessKey', 'accessKey', '请输入AccessKey'),
      secret('SecretKey', 'secretKey', '请输入SecretKey'),
      text('短信签名', 'signName', '请输入短信签名'),
    ],
  });

  const [StorageForm, storageFormApi] = useVbenForm({
    ...formShell,
    handleValuesChange: mirror(options.storage),
    schema: [
      {
        component: 'Select',
        fieldName: 'type',
        label: '存储类型',
        componentProps: {
          options: options.storageTypeOptions,
          placeholder: '请选择存储类型',
        },
      },
      text('存储端点', 'endpoint', '请输入存储端点'),
      secret('AccessKey', 'accessKey', '请输入AccessKey'),
      secret('SecretKey', 'secretKey', '请输入SecretKey'),
      text('存储桶名称', 'bucket', '请输入存储桶名称'),
    ],
  });

  const [SecurityForm, securityFormApi] = useVbenForm({
    ...formShell,
    commonConfig: { labelWidth: 140, componentProps: { class: 'w-full' } },
    handleValuesChange: mirror(options.security),
    schema: [
      numberField(
        '密码最小长度',
        'passwordMinLength',
        '请输入密码最小长度',
        6,
        32,
      ),
      check('需要大写字母', 'passwordRequireUppercase', '启用'),
      check('需要小写字母', 'passwordRequireLowercase', '启用'),
      check('需要数字', 'passwordRequireNumber', '启用'),
      check('需要特殊字符', 'passwordRequireSpecial', '启用'),
      numberField(
        '密码过期天数',
        'passwordExpireDays',
        '请输入密码过期天数',
        0,
        999,
      ),
      check('允许包含用户名', 'passwordAllowContainUsername', '允许'),
      numberField(
        '历史密码重复次数',
        'passwordRepetitionTimes',
        '请输入历史密码重复校验次数',
        3,
        32,
      ),
    ],
  });

  function emailField(
    label: string,
    fieldName: string,
    placeholder: string,
    extra: { number?: boolean; props?: Record<string, unknown> } = {},
  ): VbenFormSchema {
    return {
      component: extra.number ? 'InputNumber' : 'Input',
      fieldName,
      label,
      componentProps: () => ({
        placeholder,
        disabled: options.emailVerified.value,
        class: 'w-full',
        ...extra.props,
      }),
    };
  }

  const [EmailForm, emailFormApi] = useVbenForm({
    showDefaultActions: false,
    layout: 'vertical',
    handleValuesChange: mirror(options.email),
    schema: [
      emailField('发件人名称', 'from', '显示在邮件中的发件人名称'),
      emailField('发件人邮箱', 'username', '请输入发件人邮箱地址'),
      emailField('邮箱密码', 'password', '请输入邮箱授权码或密码', {
        props: { type: 'password' },
      }),
      emailField('SMTP服务器', 'host', '例如：smtp.qq.com'),
      emailField('SMTP端口', 'port', '例如：465', {
        number: true,
        props: { min: 1, max: 65535 },
      }),
      {
        component: 'Select',
        fieldName: 'sslEnabled',
        label: '加密方式',
        componentProps: () => ({
          options: options.emailProtectionOptions,
          placeholder: '请选择加密方式',
          disabled: options.emailVerified.value,
        }),
      },
    ],
  });

  const apis = {
    site: siteFormApi,
    login: loginFormApi,
    register: registerFormApi,
    email: emailFormApi,
    sms: smsFormApi,
    storage: storageFormApi,
    security: securityFormApi,
  };

  async function apply(key: string, data: object) {
    const api = apis[key as keyof typeof apis];
    if (!api) return;
    await api.setValues(data as Record<string, unknown>);
  }

  return {
    EmailForm,
    LoginForm,
    RegisterForm,
    SecurityForm,
    SiteForm,
    SmsForm,
    StorageForm,
    apply,
  };
}
