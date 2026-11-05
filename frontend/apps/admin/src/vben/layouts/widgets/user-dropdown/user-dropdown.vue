<script setup lang="ts">
import type { Component } from 'vue';

import type { SupportedLanguagesType } from '@vben/locales';
import type { AnyFunction } from '@vben/types';

import { computed, onMounted, ref, useTemplateRef, watch } from 'vue';

import { SUPPORT_LANGUAGES } from '@vben/constants';
import { useHoverToggle } from '@vben/hooks';
import { Languages, LockKeyhole, LogOut } from '@vben/icons';
import { $t, loadLocaleMessages } from '@vben/locales';
import { preferences, updatePreferences, usePreferences } from '@vben/preferences';
import { useAccessStore, useTimezoneStore } from '@vben/stores';
import { isWindowsOs } from '@vben/utils';

import { createIconifyIcon } from '@wyh/icons';

import { useVbenModal } from '@wyh/popup-ui';
import {
  VbenAvatar,
  VbenIcon,
} from '#/ui/composed';

import { Badge } from '#/ui/badge';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from '#/ui/dropdown-menu';

import { useMagicKeys, whenever } from '@vueuse/core';

import { LockScreenModal } from '../lock-screen';

interface Props {
  /**
   * 头像
   */
  avatar?: string;
  /**
   * @zh_CN 描述
   */
  description?: string;
  /**
   * 是否启用快捷键
   */
  enableShortcutKey?: boolean;
  /**
   * 菜单数组
   */
  menus?: Array<{
    handler: AnyFunction;
    icon?: Component | Function | string;
    text: string;
  }>;

  /**
   * 标签文本
   */
  tagText?: string;
  /**
   * 文本
   */
  text?: string;
  /** 触发方式 */
  trigger?: 'both' | 'click' | 'hover';
  /** hover触发时，延迟响应的时间 */
  hoverDelay?: number;
}

defineOptions({
  name: 'UserDropdown',
});

const props = withDefaults(defineProps<Props>(), {
  avatar: '',
  description: '',
  enableShortcutKey: true,
  menus: () => [],
  showShortcutKey: true,
  tagText: '',
  text: '',
  trigger: 'click',
  hoverDelay: 500,
});

const emit = defineEmits<{ logout: [] }>();

const TimezoneIcon = createIconifyIcon('fluent-mdl2:world-clock');
const timezoneStore = useTimezoneStore();
const timezoneOptions = ref<{ label: string; value: string }[]>([]);

const avatarLabel = computed(() => {
  const chars = [...(props.text?.trim() ?? '')];
  const first = chars[0];
  if (!first) return '';
  return /[\u4e00-\u9fff]/.test(first) ? first : chars.slice(0, 2).join('');
});

const { globalLockScreenShortcutKey, globalLogoutShortcutKey } =
  usePreferences();
const accessStore = useAccessStore();
const [LockModal, lockModalApi] = useVbenModal({
  connectedComponent: LockScreenModal,
});
const [LogoutModal, logoutModalApi] = useVbenModal({
  onConfirm() {
    handleSubmitLogout();
  },
});

const refTrigger = useTemplateRef('refTrigger');
const refContent = useTemplateRef('refContent');
const [openPopover, hoverWatcher] = useHoverToggle(
  [refTrigger, refContent],
  () => props.hoverDelay,
);

watch(
  () => props.trigger === 'hover' || props.trigger === 'both',
  (val) => {
    if (val) {
      hoverWatcher.enable();
    } else {
      hoverWatcher.disable();
    }
  },
  {
    immediate: true,
  },
);

const altView = computed(() => (isWindowsOs() ? 'Alt' : '⌥'));

const enableLogoutShortcutKey = computed(() => {
  return props.enableShortcutKey && globalLogoutShortcutKey.value;
});

const enableLockScreenShortcutKey = computed(() => {
  return props.enableShortcutKey && globalLockScreenShortcutKey.value;
});

const enableShortcutKey = computed(() => {
  return props.enableShortcutKey && preferences.shortcutKeys.enable;
});

function handleOpenLock() {
  lockModalApi.open();
}

function handleSubmitLock(lockScreenPassword: string) {
  lockModalApi.close();
  accessStore.lockScreen(lockScreenPassword);
}

function handleLogout() {
  // emit
  logoutModalApi.open();
  openPopover.value = false;
}

function handleSubmitLogout() {
  emit('logout');
  logoutModalApi.close();
}

async function handleLocale(value: string | undefined) {
  if (!value) return;
  const locale = value as SupportedLanguagesType;
  updatePreferences({
    app: { locale },
  });
  await loadLocaleMessages(locale);
}

async function handleTimezone(value: string | undefined) {
  if (!value || value === timezoneStore.timezone) return;
  await timezoneStore.setTimezone(value);
}

onMounted(async () => {
  if (!preferences.widget.timezone) return;
  timezoneOptions.value = await timezoneStore.getTimezoneOptions();
});

if (enableShortcutKey.value) {
  const keys = useMagicKeys();
  const logoutKey = keys['Alt+KeyQ'];
  const lockKey = keys['Alt+KeyL'];

  if (logoutKey) {
    whenever(logoutKey, () => {
      if (enableLogoutShortcutKey.value) {
        handleLogout();
      }
    });
  }

  if (lockKey) {
    whenever(lockKey, () => {
      if (enableLockScreenShortcutKey.value) {
        handleOpenLock();
      }
    });
  }
}
</script>

<template>
  <LockModal
    v-if="preferences.widget.lockScreen"
    :avatar="avatar"
    :text="text"
    @submit="handleSubmitLock"
  />

  <LogoutModal
    :cancel-text="$t('common.cancel')"
    :confirm-text="$t('common.confirm')"
    :fullscreen-button="false"
    :title="$t('common.prompt')"
    centered
    content-class="px-8 min-h-10"
    footer-class="border-none mb-3 mr-3"
    header-class="border-none"
  >
    {{ $t('ui.widgets.logoutTip') }}
  </LogoutModal>

  <DropdownMenu v-model:open="openPopover">
    <DropdownMenuTrigger ref="refTrigger" :disabled="props.trigger === 'hover'">
      <div class="mr-2 ml-1 cursor-pointer rounded-full p-1.5 hover:bg-accent">
        <div class="flex-center hover:text-accent-foreground">
          <VbenAvatar
            :alt="avatarLabel"
            :src="avatar"
            class="size-8"
            dot
          />
        </div>
      </div>
    </DropdownMenuTrigger>
    <DropdownMenuContent class="mr-2 min-w-60 p-0 pb-1">
      <div ref="refContent">
        <DropdownMenuLabel class="flex items-center p-3">
          <VbenAvatar
            :alt="avatarLabel"
            :src="avatar"
            class="size-12"
            dot
            dot-class="bottom-0 right-1 border-2 size-4 bg-green-500"
          />
          <div class="ml-2 w-full">
            <div
              v-if="tagText || text || $slots.tagText"
              class="mb-1 flex items-center text-sm font-medium text-foreground"
            >
              {{ text }}
              <slot name="tagText">
                <Badge v-if="tagText" class="ml-2 text-green-400">
                  {{ tagText }}
                </Badge>
              </slot>
            </div>
            <div class="text-xs font-normal text-muted-foreground">
              {{ description }}
            </div>
          </div>
        </DropdownMenuLabel>
        <DropdownMenuSeparator v-if="menus?.length" />
        <DropdownMenuItem
          v-for="menu in menus"
          :key="menu.text"
          class="mx-1 flex cursor-pointer items-center rounded-sm py-1 leading-8"
          @click="menu.handler"
        >
          <VbenIcon :icon="menu.icon" class="mr-2 size-4" />
          {{ menu.text }}
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuSub v-if="preferences.widget.languageToggle">
          <DropdownMenuSubTrigger
            class="mx-1 cursor-pointer py-1 leading-8"
          >
            <Languages class="mr-2 size-4" />
            {{ $t('preferences.language') }}
          </DropdownMenuSubTrigger>
          <DropdownMenuSubContent class="min-w-36" side="left">
            <DropdownMenuRadioGroup
              :model-value="preferences.app.locale"
              @update:model-value="handleLocale"
            >
              <DropdownMenuRadioItem
                v-for="lang in SUPPORT_LANGUAGES"
                :key="lang.value"
                :value="lang.value"
              >
                {{ lang.label }}
              </DropdownMenuRadioItem>
            </DropdownMenuRadioGroup>
          </DropdownMenuSubContent>
        </DropdownMenuSub>
        <DropdownMenuSub v-if="preferences.widget.timezone">
          <DropdownMenuSubTrigger class="mx-1 cursor-pointer py-1 leading-8">
            <TimezoneIcon class="mr-2 size-4" />
            {{ $t('ui.widgets.timezone.setTimezone') }}
          </DropdownMenuSubTrigger>
          <DropdownMenuSubContent class="min-w-56" side="left">
            <DropdownMenuRadioGroup
              :model-value="timezoneStore.timezone"
              @update:model-value="handleTimezone"
            >
              <DropdownMenuRadioItem
                v-for="item in timezoneOptions"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </DropdownMenuRadioItem>
            </DropdownMenuRadioGroup>
          </DropdownMenuSubContent>
        </DropdownMenuSub>
        <DropdownMenuSeparator
          v-if="preferences.widget.languageToggle || preferences.widget.timezone"
        />
        <DropdownMenuItem
          v-if="preferences.widget.lockScreen"
          class="mx-1 flex cursor-pointer items-center rounded-sm py-1 leading-8"
          @click="handleOpenLock"
        >
          <LockKeyhole class="mr-2 size-4" />
          {{ $t('ui.widgets.lockScreen.title') }}
          <DropdownMenuShortcut v-if="enableLockScreenShortcutKey">
            {{ altView }} L
          </DropdownMenuShortcut>
        </DropdownMenuItem>
        <DropdownMenuSeparator v-if="preferences.widget.lockScreen" />
        <DropdownMenuItem
          class="mx-1 flex cursor-pointer items-center rounded-sm py-1 leading-8"
          @click="handleLogout"
        >
          <LogOut class="mr-2 size-4" />
          {{ $t('common.logout') }}
          <DropdownMenuShortcut v-if="enableLogoutShortcutKey">
            {{ altView }} Q
          </DropdownMenuShortcut>
        </DropdownMenuItem>
      </div>
    </DropdownMenuContent>
  </DropdownMenu>
</template>
