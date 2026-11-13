<script setup lang="ts">
import { ref } from 'vue';

import FormRadioGroup from '#/components/ui/FormRadioGroup.vue';
import { Input } from '#/ui/input';
import FormSelect from '#/components/ui/FormSelect.vue';
import { $t } from '#/locales';
import { Button } from '#/ui/button';
import { Separator } from '#/ui/separator';
import { Switch } from '#/ui/switch';
import {
  ConfirmAction,
  type ConfirmActionExpose,
} from '#/ui-patterns/confirm-action';
import { preferences } from '@vben/preferences';
import { toast } from '#/ui-patterns/toast';

const confirmAction = ref<ConfirmActionExpose | null>(null);

// 语言选项
const languageOptions = [
  { label: '简体中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' },
];

// 主题模式选项
const themeModeOptions = [
  { label: $t('page.profile.preferences.light'), value: 'light' },
  { label: $t('page.profile.preferences.dark'), value: 'dark' },
  { label: $t('page.profile.preferences.auto'), value: 'auto' },
];

// 布局模式选项
const layoutOptions = [
  { label: $t('page.profile.preferences.sidebarLayout'), value: 'sidebar-nav' },
  { label: $t('page.profile.preferences.topLayout'), value: 'header-nav' },
  { label: $t('page.profile.preferences.mixLayout'), value: 'mixed-nav' },
];

// 内容宽度选项
const contentWidthOptions = [
  { label: $t('page.profile.preferences.fixed'), value: 'fixed' },
  { label: $t('page.profile.preferences.fluid'), value: 'fluid' },
];

// 偏好设置
const settings = ref({
  language: preferences.app.locale,
  themeMode: preferences.theme.mode,
  colorScheme: preferences.theme.colorPrimary,
  layout: preferences.app.layout,
  contentWidth: preferences.app.contentCompact ? 'fixed' : 'fluid',
  compactMode: preferences.app.compact,
  showBreadcrumb: preferences.breadcrumb.enable,
  showTabs: preferences.tabbar.enable,
  showFooter: preferences.footer.enable,
  defaultHomePath: preferences.app.defaultHomePath,
});

// 保存设置
const handleSave = () => {
  // 更新主题 / 布局（通过 preferences 可变对象写回）
  preferences.theme.mode = settings.value.themeMode as typeof preferences.theme.mode;
  preferences.theme.colorPrimary = settings.value.colorScheme;

  preferences.app.layout = settings.value.layout as typeof preferences.app.layout;
  preferences.app.contentCompact = (
    settings.value.contentWidth === 'fixed' ? 'compact' : 'wide'
  ) as typeof preferences.app.contentCompact;
  preferences.app.compact = settings.value.compactMode;
  preferences.app.locale = settings.value.language as typeof preferences.app.locale;

  // 更新显示设置
  preferences.breadcrumb.enable = settings.value.showBreadcrumb;
  preferences.tabbar.enable = settings.value.showTabs;
  preferences.footer.enable = settings.value.showFooter;

  // 更新首页路径
  preferences.app.defaultHomePath = settings.value.defaultHomePath;

  toast.success($t('page.profile.preferences.saveSuccess'));
};

// 恢复默认
const confirmReset = async () => {
  const ok = await confirmAction.value?.ask({
    title: $t('common.tips'),
    description: $t('page.profile.preferences.resetConfirm'),
    confirmText: $t('common.confirm'),
    cancelText: $t('common.cancel'),
  });
  if (!ok) return;
  handleReset();
};

const handleReset = () => {
  settings.value = {
    language: 'zh-CN',
    themeMode: 'auto',
    colorScheme: '#1890ff',
    layout: 'sidebar-nav',
    contentWidth: 'fluid',
    compactMode: false,
    showBreadcrumb: true,
    showTabs: true,
    showFooter: true,
    defaultHomePath: '/dashboard',
  };
  handleSave();
};

// 预设主题色
const presetColors = [
  '#1890ff', // 拂晓蓝
  '#722ed1', // 酱紫
  '#eb2f96', // 法式洋红
  '#f5222d', // 薄暮
  '#fa8c16', // 日暮
  '#52c41a', // 极光绿
  '#13c2c2', // 明青
];
</script>

<template>
  <div class="preferences-settings">
    <ConfirmAction ref="confirmAction" />
    <h3 class="text-lg font-semibold mb-6">
      {{ $t('page.profile.tabs.preferences') }}
    </h3>

    <div class="flex flex-col gap-6">
      <!-- 基础设置 -->
      <div class="rounded-xl bg-card p-4 shadow-sm">
        <h4 class="font-medium mb-4">基础设置</h4>

        <div class="flex flex-col gap-4">
          <div class="setting-item">
            <div class="setting-label">
              {{ $t('page.profile.preferences.language') }}
            </div>
            <FormSelect
              v-model:value="settings.language"
              :options="languageOptions"
              style="width: 200px"
            />
          </div>

          <Separator class="my-2" />

          <div class="setting-item">
            <div class="setting-label">
              {{ $t('page.profile.preferences.themeMode') }}
            </div>
            <FormRadioGroup
              v-model:value="settings.themeMode"
              :options="themeModeOptions"
            />
          </div>

          <Separator class="my-2" />

          <div class="setting-item">
            <div class="setting-label">
              {{ $t('page.profile.preferences.colorScheme') }}
            </div>
            <div class="flex gap-3">
              <div
                v-for="color in presetColors"
                :key="color"
                class="color-item"
                :class="{ active: settings.colorScheme === color }"
                :style="{ backgroundColor: color }"
                @click="settings.colorScheme = color"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 布局设置 -->
      <div class="rounded-xl bg-card p-4 shadow-sm">
        <h4 class="font-medium mb-4">布局设置</h4>

        <div class="flex flex-col gap-4">
          <div class="setting-item">
            <div class="setting-label">
              {{ $t('page.profile.preferences.layoutMode') }}
            </div>
            <FormRadioGroup
              v-model:value="settings.layout"
              :options="layoutOptions"
            />
          </div>

          <Separator class="my-2" />

          <div class="setting-item">
            <div class="setting-label">
              {{ $t('page.profile.preferences.contentWidth') }}
            </div>
            <FormRadioGroup
              v-model:value="settings.contentWidth"
              :options="contentWidthOptions"
            />
          </div>

          <Separator class="my-2" />

          <div class="setting-item">
            <div class="setting-label">
              {{ $t('page.profile.preferences.compactMode') }}
            </div>
            <Switch v-model="settings.compactMode" />
          </div>
        </div>
      </div>

      <!-- 显示设置 -->
      <div class="rounded-xl bg-card p-4 shadow-sm">
        <h4 class="font-medium mb-4">显示设置</h4>

        <div class="flex flex-col gap-4">
          <div class="setting-item">
            <div class="setting-label">
              {{ $t('page.profile.preferences.showBreadcrumb') }}
            </div>
            <Switch v-model="settings.showBreadcrumb" />
          </div>

          <Separator class="my-2" />

          <div class="setting-item">
            <div class="setting-label">
              {{ $t('page.profile.preferences.showTabs') }}
            </div>
            <Switch v-model="settings.showTabs" />
          </div>

          <Separator class="my-2" />

          <div class="setting-item">
            <div class="setting-label">
              {{ $t('page.profile.preferences.showFooter') }}
            </div>
            <Switch v-model="settings.showFooter" />
          </div>
        </div>
      </div>

      <!-- 首页设置 -->
      <div class="rounded-xl bg-card p-4 shadow-sm">
        <h4 class="font-medium mb-4">
          {{ $t('page.profile.preferences.homePage') }}
        </h4>

        <div class="setting-item">
          <div class="setting-label">
            {{ $t('page.profile.preferences.defaultHomePath') }}
          </div>
          <Input
            v-model="settings.defaultHomePath"
            :placeholder="$t('page.profile.preferences.defaultHomePath')"
            class="w-[300px]"
          />
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="flex justify-between">
        <Button type="button" variant="outline" @click="confirmReset">
          {{ $t('page.profile.preferences.resetToDefault') }}
        </Button>

        <Button type="button" @click="handleSave">
          {{ $t('page.profile.basic.save') }}
        </Button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.preferences-settings {
  max-width: 800px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .setting-label {
    font-weight: 500;
    color: var(--vben-text-primary-color);
  }
}

.color-item {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;

  &:hover {
    transform: scale(1.1);
  }

  &.active {
    border-color: var(--vben-text-primary-color);
    box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
  }
}
</style>
