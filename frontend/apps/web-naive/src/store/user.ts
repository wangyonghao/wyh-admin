import type { Recordable } from '@vben/types';

import type { UserProfile } from '#/api/auth';

import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import {
  resetAllStores,
  useAccessStore,
  useUserStore as useVbenUserStore,
} from '@vben/stores';

import { defineStore } from 'pinia';

import { message } from '#/adapter/naive';
import { AuthTypeConstants } from '#/api';
import { authApi } from '#/api/auth';
import { $t } from '#/locales';
import { encryptByRsa } from '#/utils/crypto';

export const useUserStore = defineStore(
  'user',
  () => {
    const accessStore = useAccessStore();
    const userStore = useVbenUserStore();
    const router = useRouter();

    // 状态
    const token = ref<string>();
    const tokenExpireTime = ref<null | number>(null);
    const user = ref<null | UserProfile>(null);
    const roles = ref<string[]>([]);
    const permissions = ref<string[]>([]);
    const menus = ref<MenuResp[]>([]);
    const isRouteAdded = ref<boolean>(false);
    const loginLoading = ref(false);

    // 计算属性
    const tokenExpired = computed((): boolean => {
      if (!tokenExpireTime.value) return false;
      return tokenExpireTime.value > Date.now();
    });

    /**
     * 处理登录操作
     * @param params 登录表单数据
     */
    async function login(
      params: Recordable<any>,
      onSuccess?: () => Promise<void> | void,
    ) {
      try {
        loginLoading.value = true;
        params.password = encryptByRsa(params.password) || '';
        params.clientId = import.meta.env.VITE_CLIENT_ID;
        params.authType = AuthTypeConstants.ACCOUNT;
        const loginResult = await authApi.login(params);

        // 检查是否密码过期
        if (loginResult.code === 'PASSWORD_EXPIRED') {
          return {
            userInfo: null,
            passwordExpired: true,
            userId: loginResult.userId,
            tempToken: loginResult.tempToken,
          };
        }

        token.value = loginResult.token;
        // 将 accessToken 存储到 accessStore 中
        accessStore.setAccessToken(loginResult.token);
        accessStore.setLoginExpired(false);

        // 获取用户信息（包含菜单、权限等）
        await fetchAuthInfo();
        onSuccess
          ? await onSuccess?.()
          : await router.push(preferences.app.defaultHomePath);

        if (user.value?.nickname) {
          message.success(
            `${$t('authentication.loginSuccessDesc')}:${user.value?.nickname}`,
          );
        }
        return { user, passwordExpired: false };
      } finally {
        loginLoading.value = false;
      }
    }

    async function logout(redirect: boolean = true) {
      try {
        await authApi.logout();
      } catch {
        // 不做任何处理
      }
      resetAllStores();
      accessStore.setLoginExpired(false);

      // 回登录页带上当前路由地址
      await router.replace({
        path: LOGIN_PATH,
        query: redirect
          ? { redirect: encodeURIComponent(router.currentRoute.value.fullPath) }
          : {},
      });
    }

    async function fetchAuthInfo() {
      const info = await authApi.getAuthInfo();

      user.value = info.user;
      roles.value = info.roles;
      permissions.value = info.permissions;
      menus.value = info.menus;

      userStore.setUserInfo(info.user);
      accessStore.setAccessMenus(info.menus);
      accessStore.setAccessCodes(info.permissions);
    }

    function $reset() {
      loginLoading.value = false;
    }
    /** 检查权限 */
    function hasPermission(permission: string): boolean {
      if (!permissions.value) return false;
      if (permissions.value.includes('*:*:*')) return true;
      return permissions.value.includes(permission);
    }

    /** 检查角色 */
    function hasRole(role: string): boolean {
      if (!roles.value) return false;
      return roles.value.includes(role);
    }

    return {
      token,
      tokenExpired,
      user,
      roles,
      permissions,
      menus,
      isRouteAdded,
      $reset,
      login,
      fetchAuthInfo,
      loginLoading,
      logout,
      hasPermission,
      hasRole,
    };
  },
  {
    persist: {
      key: 'user',
      pick: ['token'],
    },
  },
);
