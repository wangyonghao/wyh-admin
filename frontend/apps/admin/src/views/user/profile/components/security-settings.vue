<script setup lang="ts">
import { h, onMounted, ref } from 'vue';

import { useVbenModal } from '#/components/popup/modal';

import { useVbenForm, z } from '#/components/ui-patterns/form';
import { loginLogApi, type LoginLogResult } from '#/api/system/login-log';
import {
  userProfileApi,
  type BindSocialAccountRes,
} from '#/api/system/user-profile';
import { $t } from '#/locales';
import { useUserStore } from '#/store/user';
import { Badge } from '#/ui/badge';
import { Button } from '#/ui/button';
import { toast } from '#/ui-patterns/toast';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';
import { encryptByRsa } from '#/utils/crypto';

const userStore = useUserStore();
const confirmAction = ref<ConfirmActionExpose | null>(null);

const loginDevices = ref<LoginLogResult[]>([]);
const loadingDevices = ref(false);
const socialAccounts = ref<BindSocialAccountRes[]>([]);
const loadingSocial = ref(false);

const panelClass = 'rounded-xl bg-card p-4 shadow-sm';

const [PasswordForm, passwordFormApi] = useVbenForm({
  showDefaultActions: false,
  layout: 'horizontal',
  commonConfig: { labelWidth: 100, componentProps: { class: 'w-full' } },
  schema: [
    {
      component: 'Input',
      fieldName: 'oldPassword',
      label: $t('page.profile.security.oldPassword'),
      componentProps: {
        type: 'password',
        placeholder: $t('page.profile.security.oldPassword'),
      },
    },
    {
      component: 'Input',
      fieldName: 'newPassword',
      label: $t('page.profile.security.newPassword'),
      componentProps: {
        type: 'password',
        placeholder: $t('page.profile.security.newPassword'),
      },
    },
    {
      component: 'Input',
      fieldName: 'confirmPassword',
      label: $t('page.profile.security.confirmPassword'),
      componentProps: {
        type: 'password',
        placeholder: $t('page.profile.security.confirmPassword'),
      },
      dependencies: {
        rules(values) {
          return z
            .string()
            .nullish()
            .refine((value) => (value ?? '') === (values.newPassword ?? ''), {
              message: '两次输入的密码不一致',
            });
        },
        triggerFields: ['newPassword'],
      },
    },
  ],
});

const [PhoneForm, phoneFormApi] = useVbenForm({
  showDefaultActions: false,
  layout: 'horizontal',
  commonConfig: { labelWidth: 100, componentProps: { class: 'w-full' } },
  schema: [
    {
      component: 'Input',
      fieldName: 'phone',
      label: $t('page.profile.basic.phone'),
      componentProps: { placeholder: $t('page.profile.basic.phone') },
    },
    {
      component: 'Input',
      fieldName: 'captcha',
      label: $t('page.profile.security.captcha'),
      componentProps: { placeholder: $t('page.profile.security.captcha') },
      suffix: () =>
        h(
          Button,
          { type: 'button', variant: 'outline' },
          () => $t('page.profile.security.sendCaptcha'),
        ),
    },
    {
      component: 'Input',
      fieldName: 'oldPassword',
      label: $t('page.profile.security.oldPassword'),
      componentProps: {
        type: 'password',
        placeholder: $t('page.profile.security.oldPassword'),
      },
    },
  ],
});

const [EmailForm, emailFormApi] = useVbenForm({
  showDefaultActions: false,
  layout: 'horizontal',
  commonConfig: { labelWidth: 100, componentProps: { class: 'w-full' } },
  schema: [
    {
      component: 'Input',
      fieldName: 'email',
      label: $t('page.profile.basic.email'),
      componentProps: { placeholder: $t('page.profile.basic.email') },
    },
    {
      component: 'Input',
      fieldName: 'captcha',
      label: $t('page.profile.security.captcha'),
      componentProps: { placeholder: $t('page.profile.security.captcha') },
      suffix: () =>
        h(
          Button,
          { type: 'button', variant: 'outline' },
          () => $t('page.profile.security.sendCaptcha'),
        ),
    },
    {
      component: 'Input',
      fieldName: 'oldPassword',
      label: $t('page.profile.security.oldPassword'),
      componentProps: {
        type: 'password',
        placeholder: $t('page.profile.security.oldPassword'),
      },
    },
  ],
});

const [PasswordModal, passwordModalApi] = useVbenModal({
  class: 'w-[500px]',
  title: $t('page.profile.security.changePassword'),
  confirmText: $t('common.confirm'),
  cancelText: $t('common.cancel'),
  onConfirm: handleChangePassword,
});

const [PhoneModal, phoneModalApi] = useVbenModal({
  class: 'w-[500px]',
  title: $t('page.profile.security.changePhone'),
  confirmText: $t('common.confirm'),
  cancelText: $t('common.cancel'),
  onConfirm: handleChangePhone,
});

const [EmailModal, emailModalApi] = useVbenModal({
  class: 'w-[500px]',
  title: $t('page.profile.security.changeEmail'),
  confirmText: $t('common.confirm'),
  cancelText: $t('common.cancel'),
  onConfirm: handleChangeEmail,
});

async function openPassword() {
  await passwordFormApi.resetForm();
  passwordModalApi.open();
}

async function openPhone() {
  await phoneFormApi.resetForm();
  phoneModalApi.setState({
    title: userStore.user?.phone
      ? $t('page.profile.security.changePhone')
      : $t('page.profile.security.bindPhone'),
  });
  phoneModalApi.open();
}

async function openEmail() {
  await emailFormApi.resetForm();
  emailModalApi.setState({
    title: userStore.user?.email
      ? $t('page.profile.security.changeEmail')
      : $t('page.profile.security.bindEmail'),
  });
  emailModalApi.open();
}

const fetchLoginDevices = async () => {
  try {
    loadingDevices.value = true;
    const res = await loginLogApi.listLoginLog({
      username: userStore.user?.username,
      loginStatus: 'SUCCESS',
      page: 1,
      pageSize: 10,
    });
    loginDevices.value = res.records || [];
  } catch (error) {
    console.error('获取登录设备失败:', error);
  } finally {
    loadingDevices.value = false;
  }
};

const fetchSocialAccounts = async () => {
  try {
    loadingSocial.value = true;
    socialAccounts.value = await userProfileApi.listSocial();
  } catch (error) {
    console.error('获取三方账号失败:', error);
  } finally {
    loadingSocial.value = false;
  }
};

async function handleChangePassword() {
  const { valid } = await passwordFormApi.validate();
  if (!valid) return;
  const values = await passwordFormApi.getValues();
  try {
    await userProfileApi.updatePassword({
      oldPassword: encryptByRsa(String(values.oldPassword ?? '')) || '',
      newPassword: encryptByRsa(String(values.newPassword ?? '')) || '',
    });
    toast.success($t('page.profile.security.passwordChanged'));
    passwordModalApi.close();
    setTimeout(() => {
      userStore.logout();
    }, 1500);
  } catch (error) {
    console.error('修改密码失败:', error);
  }
}

async function handleChangePhone() {
  const values = await phoneFormApi.getValues();
  try {
    await userProfileApi.updatePhone({
      phone: String(values.phone ?? ''),
      captcha: String(values.captcha ?? ''),
      oldPassword: encryptByRsa(String(values.oldPassword ?? '')) || '',
    });
    toast.success($t('page.profile.security.phoneChanged'));
    phoneModalApi.close();
    await userStore.fetchAuthInfo();
  } catch (error) {
    console.error('修改手机号失败:', error);
  }
}

async function handleChangeEmail() {
  const values = await emailFormApi.getValues();
  try {
    await userProfileApi.updateEmail({
      email: String(values.email ?? ''),
      captcha: String(values.captcha ?? ''),
      oldPassword: encryptByRsa(String(values.oldPassword ?? '')) || '',
    });
    toast.success($t('page.profile.security.emailChanged'));
    emailModalApi.close();
    await userStore.fetchAuthInfo();
  } catch (error) {
    console.error('修改邮箱失败:', error);
  }
}

const confirmUnbind = async (source: string) => {
  const ok = await confirmAction.value?.ask({
    title: $t('common.tips'),
    description: $t('page.profile.security.unbindConfirm'),
    confirmText: $t('common.confirm'),
    cancelText: $t('common.cancel'),
    tone: 'destructive',
  });
  if (!ok) return;
  try {
    await userProfileApi.unbindSocial(source);
    toast.success('解绑成功');
    await fetchSocialAccounts();
  } catch (error) {
    console.error('解绑失败:', error);
  }
};

const handleLogoutAllDevices = () => {
  toast.info('功能开发中');
};

onMounted(() => {
  fetchLoginDevices();
  fetchSocialAccounts();
});
</script>

<template>
  <div class="security-settings">
    <ConfirmAction ref="confirmAction" />
    <h3 class="mb-6 text-lg font-semibold">
      {{ $t('page.profile.tabs.security') }}
    </h3>

    <div class="flex flex-col gap-6">
      <div :class="panelClass">
        <div class="flex items-center justify-between">
          <div>
            <h4 class="mb-1 font-medium">
              {{ $t('page.profile.security.changePassword') }}
            </h4>
            <p class="text-sm text-gray-500">
              {{ $t('page.profile.security.passwordRule') }}
            </p>
          </div>
          <Button type="button" variant="outline" @click="openPassword">
            {{ $t('page.profile.security.changePassword') }}
          </Button>
        </div>
      </div>

      <div :class="panelClass">
        <div class="flex items-center justify-between">
          <div>
            <h4 class="mb-1 font-medium">
              {{ $t('page.profile.basic.phone') }}
            </h4>
            <p class="text-sm text-gray-500">
              {{
                userStore.user?.phone || $t('page.profile.summary.phoneUnbound')
              }}
            </p>
          </div>
          <Button type="button" variant="outline" @click="openPhone">
            {{
              userStore.user?.phone
                ? $t('page.profile.security.changePhone')
                : $t('page.profile.security.bindPhone')
            }}
          </Button>
        </div>
      </div>

      <div :class="panelClass">
        <div class="flex items-center justify-between">
          <div>
            <h4 class="mb-1 font-medium">
              {{ $t('page.profile.basic.email') }}
            </h4>
            <p class="text-sm text-gray-500">
              {{
                userStore.user?.email || $t('page.profile.summary.emailUnbound')
              }}
            </p>
          </div>
          <Button type="button" variant="outline" @click="openEmail">
            {{
              userStore.user?.email
                ? $t('page.profile.security.changeEmail')
                : $t('page.profile.security.bindEmail')
            }}
          </Button>
        </div>
      </div>

      <div :class="panelClass">
        <div class="mb-4 flex items-center justify-between">
          <h4 class="font-medium">
            {{ $t('page.profile.security.loginDevices') }}
          </h4>
          <Button
            type="button"
            variant="destructive"
            size="sm"
            @click="handleLogoutAllDevices"
          >
            {{ $t('page.profile.security.logoutAllDevices') }}
          </Button>
        </div>
        <p
          v-if="loadingDevices"
          class="text-muted-foreground py-6 text-center text-sm"
        >
          加载中
        </p>
        <ul v-else-if="loginDevices.length > 0" class="divide-y">
          <li v-for="device in loginDevices" :key="device.id" class="py-3">
            <div class="flex items-center gap-2">
              <span>{{ device.browser }} / {{ device.os }}</span>
              <Badge
                v-if="device.id === loginDevices[0]?.id"
                variant="success"
              >
                {{ $t('page.profile.security.currentDevice') }}
              </Badge>
            </div>
            <div class="text-muted-foreground mt-1 flex flex-col gap-1 text-xs">
              <span>
                {{ $t('page.profile.security.ipAddress') }}:
                {{ device.ipAddress }}
              </span>
              <span>
                {{ $t('page.profile.security.location') }}:
                {{ device.location }}
              </span>
              <span>
                {{ $t('page.profile.security.loginTime') }}:
                {{ device.loginTime }}
              </span>
            </div>
          </li>
        </ul>
        <p v-else class="text-muted-foreground py-6 text-center text-sm">
          暂无登录记录
        </p>
      </div>

      <div :class="panelClass">
        <h4 class="mb-4 font-medium">
          {{ $t('page.profile.security.socialAccount') }}
        </h4>
        <p
          v-if="loadingSocial"
          class="text-muted-foreground py-6 text-center text-sm"
        >
          加载中
        </p>
        <ul v-else-if="socialAccounts.length > 0" class="divide-y">
          <li
            v-for="account in socialAccounts"
            :key="account.source"
            class="flex items-center justify-between py-3"
          >
            <span>{{ account.description }}</span>
            <Button
              type="button"
              variant="link"
              size="sm"
              class="text-destructive h-auto px-1"
              @click="confirmUnbind(account.source)"
            >
              {{ $t('page.profile.security.unbind') }}
            </Button>
          </li>
        </ul>
        <p v-else class="text-muted-foreground py-6 text-center text-sm">
          暂无绑定的三方账号
        </p>
      </div>
    </div>

    <PasswordModal>
      <PasswordForm />
    </PasswordModal>
    <PhoneModal>
      <PhoneForm />
    </PhoneModal>
    <EmailModal>
      <EmailForm />
    </EmailModal>
  </div>
</template>

<style lang="scss" scoped>
.security-settings {
  max-width: 900px;
}
</style>
