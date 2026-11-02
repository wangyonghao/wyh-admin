import type { Recordable } from '@vben/types';

import type { UserProfile } from '#/api/auth';
import type { Menu } from '#/api/system/menu';

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

import { toast } from '#/ui-patterns/toast';
import { AuthTypeConstants } from '#/api';
import { authApi } from '#/api/auth';
import { $t } from '#/locales';
import { encryptByRsa } from '#/utils/crypto';
import { toAccessMenus } from '#/utils/menu';

export const useUserStore = defineStore(
  'user',
  () => {
    const accessStore = useAccessStore();
    const userStore = useVbenUserStore();
    const router = useRouter();

    // 状态
    const token = ref<string>();
    const tokenExpireTime = ref<number>();
    const user = ref<UserProfile>();
    const roles = ref<string[]>([]);
    const permissions = ref<string[]>([]);
    const menus = ref<Menu[]>([]);
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
        delete params.tenantId;
        params.clientId = import.meta.env.VITE_CLIENT_ID;
        params.grantType = AuthTypeConstants.PASSWORD;
        delete params.authType;
        const loginResult = await authApi.login(params);
        await completeLogin(loginResult.token, onSuccess);
        return { user };
      } finally {
        loginLoading.value = false;
      }
    }

    /**
     * 自助注册并直接进入系统
     */
    async function register(
      params: { username: string; password: string; confirmPassword: string },
      onSuccess?: () => Promise<void> | void,
    ) {
      try {
        loginLoading.value = true;
        const result = await authApi.register({
          username: params.username,
          password: encryptByRsa(params.password) || '',
          confirmPassword: encryptByRsa(params.confirmPassword) || '',
        });
        await completeLogin(result.token, onSuccess);
        return { user };
      } finally {
        loginLoading.value = false;
      }
    }

    async function completeLogin(
      accessToken: string,
      onSuccess?: () => Promise<void> | void,
    ) {
      token.value = accessToken;
      accessStore.setAccessToken(accessToken);
      accessStore.setLoginExpired(false);

      await fetchAuthInfo();
      onSuccess
        ? await onSuccess?.()
        : await router.push(preferences.app.defaultHomePath);

      if (user.value?.displayName) {
        toast.success(
          `${$t('authentication.loginSuccessDesc')}:${user.value?.displayName}`,
        );
      }
    }

    async function logout(redirect: boolean = true) {
      try {
        await authApi.logout();
      } catch {
        // 不做任何处理
      }
      resetAllStores();
      accessStore.setLoginExpired(true);

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

      user.value = info.user
        ? { ...info.user, userId: String(info.user.userId ?? '') }
        : info.user;
      roles.value = info.roles;
      permissions.value = info.permissions;
      menus.value = info.menus;

      userStore.setUserInfo(
        info.user
          ? {
              ...info.user,
              userId: String(info.user.userId ?? ''),
              realName: info.user.displayName,
              avatar: info.user.avatar,
            }
          : null,
      );
      accessStore.setAccessMenus(toAccessMenus(info.menus));
      accessStore.setAccessCodes(info.permissions);
    }

    function $reset() {
      token.value = undefined;
      tokenExpireTime.value = undefined;
      user.value = undefined;
      roles.value = [];
      permissions.value = [];
      menus.value = [];
      isRouteAdded.value = false;
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
      register,
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
