<script setup lang="ts">
import { ref } from 'vue';

import FormRadioGroup from '#/components/ui/FormRadioGroup.vue';
import { $t } from '@vben/locales';
import { Button } from '#/ui/button';
import { Checkbox } from '#/ui/checkbox';
import { isValueChecked, toggleCheckedValue } from '#/ui/checkbox/group';
import { Separator } from '#/ui/separator';
import { Switch } from '#/ui/switch';
import { toast } from '#/ui-patterns/toast';

// 通知设置
const notificationSettings = ref({
  systemNotice: true,
  emailNotice: false,
  smsNotice: false,
});

// 通知类型
const noticeTypes = ref<string[]>([
  'systemMessage',
  'taskReminder',
  'securityAlert',
]);

// 告警级别
const alertLevel = ref('warning');

// 保存设置
const handleSave = () => {
  toast.success($t('page.profile.notification.saveSuccess'));
};

// 告警级别选项
const alertLevelOptions = [
  { label: $t('page.profile.notification.info'), value: 'info' },
  { label: $t('page.profile.notification.warning'), value: 'warning' },
  { label: $t('page.profile.notification.error'), value: 'error' },
  { label: $t('page.profile.notification.critical'), value: 'critical' },
];

// 通知类型选项
const noticeTypeOptions = [
  {
    label: $t('page.profile.notification.systemMessage'),
    value: 'systemMessage',
  },
  {
    label: $t('page.profile.notification.taskReminder'),
    value: 'taskReminder',
  },
  {
    label: $t('page.profile.notification.securityAlert'),
    value: 'securityAlert',
  },
  {
    label: $t('page.profile.notification.operationLog'),
    value: 'operationLog',
  },
];
</script>

<template>
  <div class="notification-settings">
    <h3 class="text-lg font-semibold mb-6">
      {{ $t('page.profile.tabs.notification') }}
    </h3>

    <div class="flex flex-col gap-6">
      <!-- 通知渠道 -->
      <div class="rounded-xl bg-card p-4 shadow-sm">
        <h4 class="font-medium mb-4">通知渠道</h4>

        <div class="flex flex-col gap-4">
          <div class="flex items-center justify-between">
            <div>
              <div class="font-medium mb-1">
                {{ $t('page.profile.notification.systemNotice') }}
              </div>
              <div class="text-sm text-gray-500">
                {{ $t('page.profile.notification.enableSystemNotice') }}
              </div>
            </div>
            <Switch v-model="notificationSettings.systemNotice" />
          </div>

          <Separator class="my-2" />

          <div class="flex items-center justify-between">
            <div>
              <div class="font-medium mb-1">
                {{ $t('page.profile.notification.emailNotice') }}
              </div>
              <div class="text-sm text-gray-500">
                {{ $t('page.profile.notification.enableEmailNotice') }}
              </div>
            </div>
            <Switch v-model="notificationSettings.emailNotice" />
          </div>

          <Separator class="my-2" />

          <div class="flex items-center justify-between">
            <div>
              <div class="font-medium mb-1">
                {{ $t('page.profile.notification.smsNotice') }}
              </div>
              <div class="text-sm text-gray-500">
                {{ $t('page.profile.notification.enableSmsNotice') }}
              </div>
            </div>
            <Switch v-model="notificationSettings.smsNotice" />
          </div>
        </div>
      </div>

      <!-- 通知类型 -->
      <div class="rounded-xl bg-card p-4 shadow-sm">
        <h4 class="font-medium mb-4">
          {{ $t('page.profile.notification.noticeTypes') }}
        </h4>

        <div class="flex flex-col gap-3">
          <label
            v-for="option in noticeTypeOptions"
            :key="option.value"
            class="inline-flex cursor-pointer items-center gap-2 text-sm"
          >
            <Checkbox
              :model-value="isValueChecked(noticeTypes, option.value)"
              @update:model-value="
                (checked) =>
                  (noticeTypes = toggleCheckedValue(
                    noticeTypes,
                    option.value,
                    checked === true,
                  ))
              "
            />
            <span>{{ option.label }}</span>
          </label>
        </div>
      </div>

      <!-- 告警级别 -->
      <div class="rounded-xl bg-card p-4 shadow-sm">
        <h4 class="font-medium mb-4">
          {{ $t('page.profile.notification.alertLevel') }}
        </h4>

        <FormRadioGroup v-model:value="alertLevel" :options="alertLevelOptions" />
      </div>

      <!-- 保存按钮 -->
      <div class="flex justify-end">
        <Button type="button" @click="handleSave">
          {{ $t('page.profile.basic.save') }}
        </Button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.notification-settings {
  max-width: 800px;
}
</style>
