import type { Router } from 'vue-router';

import type { ComponentRecordType } from '@vben/types';

import type { RouteItem } from '#/api';

import { LOGIN_PATH } from '@vben/constants';
import { IFrameView } from '@vben/layouts';
import { preferences } from '@vben/preferences';
import { startProgress, stopProgress } from '@vben/utils';
import axios from 'axios';

import { orgApi } from '#/api/org';
import { useUserStore } from '#/store';
import { toast } from '#/ui-patterns/toast';
import { isBusinessError } from '#/utils/business-error';

// 动态导入所有页面组件
const pageMap: ComponentRecordType = import.meta.glob('/src/views/**/*.vue');

// 已添加的动态路由
const addedRouteNames = new Set<string>();

/**
 * 根据用户菜单动态添加新路由
 */
export function addRoutes(router: Router, menus: RouteItem[]) {
  const addRoutes = (menuList: RouteItem[]) => {
    for (const menu of menuList) {
      if (menu.type === 2 && menu.path) {
        const routeName = `dynamic-${menu.id}`;

        // 检查是否已经有同路径的静态路由
        const menuPath = menu.path.startsWith('/')
          ? menu.path.slice(1)
          : menu.path;

        // const existingRoutes = router.getRoutes()
        // const pathExists = existingRoutes.some(r => r.path === '/' + menuPath || r.path === menuPath)
        const pathExists = router.hasRoute(routeName);
        if (pathExists) {
          continue;
        }
        if (addedRouteNames.has(routeName)) {
          continue;
        }

        // 判断菜单是否是外链
        if (/^https?:\/\//.test(menu.path)) {
          router.addRoute('Root', {
            path: menuPath,
            name: routeName,
            component: IFrameView,
            meta: {
              icon: menu.icon?.includes(':') ? menu.icon : `svg:${menu.icon}`,
              keepAlive: !menu.isCache,
              title: menu.name,
              link: menu.path,
            },
          });
          addedRouteNames.add(routeName);
        } else {
          // 文件路由：根据路径自动推导组件
          const componentPath = `/src/views/${menuPath}/index.vue`;
          const component =
            pageMap[componentPath] ||
            pageMap[`/src/views/${menuPath}.vue`] ||
            (() => import('#/views/_core/fallback/not-found.vue'));

          router.addRoute('Root', {
            path: menuPath,
            name: routeName,
            component,
            meta: {
              title: menu.name,
              icon: menu.icon,
              permission: menu.permission,
            },
          });
          addedRouteNames.add(routeName);
        }
      }

      if (menu.children && menu.children.length > 0) {
        addRoutes(menu.children);
      }
    }
  };
  addRoutes(menus);
}

export function resetRouter(router: Router) {
  addedRouteNames.forEach((name) => {
    if (router.hasRoute(name)) {
      router.removeRoute(name);
    }
  });
  addedRouteNames.clear();
}

/**
 * 通用守卫配置
 * @param router
 */
function setupCommonGuard(router: Router) {
  // 记录已经加载的页面
  const loadedPaths = new Set<string>();

  router.beforeEach((to) => {
    to.meta.loaded = loadedPaths.has(to.path);

    // 页面加载进度条
    if (!to.meta.loaded && preferences.transition.progress) {
      startProgress();
    }
    return true;
  });

  router.afterEach((to) => {
    // 记录页面是否加载,如果已经加载，后续的页面切换动画等效果不在重复执行

    loadedPaths.add(to.path);

    // 关闭页面加载进度条
    if (preferences.transition.progress) {
      stopProgress();
    }
  });
}

/**
 * 权限访问守卫配置
 * @param router
 */
function setupAccessGuard(router: Router) {
  router.beforeEach(async (to) => {
    const userStore = useUserStore();

    // 这些路由不需要进入权限拦截
    if (to.meta.requiresAuth === false) {
      return true;
    }

    // accessToken 检查
    if (!userStore.token) {
      return {
        path: LOGIN_PATH,
        query: { redirect: encodeURIComponent(to.fullPath) },
        replace: true,
      };
    }

    // 是否已经生成过动态路由
    if (userStore.isRouteAdded) {
      return true;
    }

    // 生成路由表
    try {
      await userStore.fetchAuthInfo();
      addRoutes(router, userStore.menus as RouteItem[]);
      const orgSession = await orgApi.session();
      if (
        orgSession.needsOrganization &&
        to.path !== '/org/setup' &&
        to.path !== '/org/invite'
      ) {
        userStore.isRouteAdded = true;
        return { path: '/org/setup', replace: true };
      }
    } catch (error) {
      // HTTP 错误已由请求层提示；这里只提示业务异常。
      if (isBusinessError(error)) {
        toast.error(error.message);
      } else if (!axios.isAxiosError(error)) {
        toast.error('用户菜单加载失败');
      }
      return {
        path: LOGIN_PATH,
        query: { redirect: encodeURIComponent(to.fullPath) },
        replace: true,
      };
    }
    userStore.isRouteAdded = true;
    return {
      ...router.resolve(decodeURIComponent(to.path)),
      replace: true,
    };
  });
}

/**
 * 项目守卫配置
 * @param router
 */
function createRouterGuard(router: Router) {
  /** 通用 */
  setupCommonGuard(router);
  /** 权限访问 */
  setupAccessGuard(router);
}

export { createRouterGuard };
