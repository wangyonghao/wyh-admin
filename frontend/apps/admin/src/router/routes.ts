import type { RouteRecordRaw } from 'vue-router';

import { preferences } from '@vben/preferences';

import { $t } from '#/locales';

/** 全局404页面 */
const fallbackNotFoundRoute: RouteRecordRaw = {
  name: 'FallbackNotFound',
  path: '/:path(.*)*',
  component: () => import('#/views/_core/fallback/not-found.vue'),
  meta: {
    hideInBreadcrumb: true,
    hideInMenu: true,
    hideInTab: true,
    title: '404',
  },
};

/** 基本路由，这些路由是必须存在的 */
const routes: RouteRecordRaw[] = [
  /** 根路由，基础布局 */
  {
    name: 'Root',
    path: '/',
    component: () => import('#/components/layouts/basic.vue'),
    meta: { hideInBreadcrumb: true, title: 'Root' },
    redirect: preferences.app.defaultHomePath,
    children: [
      // dashboard
      {
        name: 'Dashboard',
        path: '/dashboard',
        meta: {
          icon: 'lucide:layout-dashboard',
          order: -1,
          title: $t('page.dashboard.title'),
        },
        children: [
          {
            name: 'Analytics',
            path: '/analytics',
            component: () => import('#/views/dashboard/analytics/index.vue'),
            meta: {
              affixTab: true,
              icon: 'lucide:area-chart',
              title: $t('page.dashboard.analytics'),
            },
          },
          {
            name: 'Workspace',
            path: '/workspace',
            component: () => import('#/views/dashboard/workspace/index.vue'),
            meta: {
              icon: 'carbon:workspace',
              title: $t('page.dashboard.workspace'),
            },
          },
        ],
      },
      {
        path: '/user',
        name: 'User',
        meta: { hideInBreadcrumb: true, hideInMenu: true, hideInTab: false },
        children: [
          {
            path: '/user/profile',
            name: 'Profile',
            component: () => import('#/views/user/profile/index.vue'),
            meta: { title: '个人中心', showInTabs: false },
          },
          {
            path: '/user/message',
            name: 'UserMessage',
            component: () => import('#/views/user/message/index.vue'),
            meta: { title: '消息中心', showInTabs: false },
          },
          {
            path: '/user/notice',
            name: 'UserNotice',
            component: () => import('#/views/user/message/components/view/index.vue'),
            meta: { title: '查看公告' },
          },
        ],
      },
    ],
  },
  {
    name: 'Authentication',
    path: '/auth',
    component: () => import('#/components/layouts/auth.vue'),
    meta: { hideInTab: true, requiresAuth: false, title: 'Authentication' },
    redirect: '/auth/login',
    children: [
      {
        name: 'Login',
        path: 'login',
        component: () => import('#/views/_core/authentication/login.vue'),
        meta: {
          requiresAuth: false,
          title: $t('page.auth.login'),
        },
      },
      {
        name: 'CodeLogin',
        path: 'code-login',
        component: () => import('#/views/_core/authentication/code-login.vue'),
        meta: {
          requiresAuth: false,
          title: $t('page.auth.codeLogin'),
        },
      },
      {
        name: 'QrCodeLogin',
        path: 'qrcode-login',
        component: () =>
          import('#/views/_core/authentication/qrcode-login.vue'),
        meta: {
          requiresAuth: false,
          title: $t('page.auth.qrcodeLogin'),
        },
      },
      {
        name: 'ForgetPassword',
        path: 'forget-password',
        component: () =>
          import('#/views/_core/authentication/forget-password.vue'),
        meta: { requiresAuth: false, title: $t('page.auth.forgetPassword') },
      },
      {
        name: 'Register',
        path: 'register',
        component: () => import('#/views/_core/authentication/register.vue'),
        meta: { requiresAuth: false, title: $t('page.auth.register') },
      },
      {
        name: 'RegisterVerify',
        path: 'register/verify',
        component: () => import('#/views/_core/authentication/register-verify.vue'),
        meta: { requiresAuth: false, title: '确认注册' },
      },
      {
        name: 'GithubCallback',
        path: 'github/callback',
        component: () =>
          import('#/views/_core/authentication/social-callback.vue'),
        meta: {
          hideInTab: true,
          requiresAuth: false,
          title: 'GitHub Callback',
        },
      },
    ],
  },

  {
    name: 'OrgSetup',
    path: '/org/setup',
    component: () => import('#/views/org/setup.vue'),
    meta: {
      hideInMenu: true,
      hideInTab: true,
      title: '组织引导',
    },
  },
  {
    name: 'OrgInvite',
    path: '/org/invite',
    component: () => import('#/views/org/invite.vue'),
    meta: {
      hideInMenu: true,
      hideInTab: true,
      title: '组织邀请',
    },
  },
  {
    name: 'OrgInviteAdmin',
    path: '/org/invite-admin',
    component: () => import('#/views/org/invite-admin.vue'),
    meta: {
      hideInMenu: true,
      title: '邀请成员',
    },
  },

  // User Center
  

  // =================== demos ===================
  {
    name: 'Demos',
    path: '/demos',
    meta: {
      icon: 'ic:baseline-view-in-ar',
      keepAlive: true,
      order: 1000,
      title: $t('demos.title'),
    },
    children: [
      {
        meta: {
          title: $t('demos.form'),
        },
        name: 'BasicForm',
        path: '/demos/form',
        component: () => import('#/views/demos/form/basic.vue'),
      },
    ],
  },
  {
    name: 'FallbackNotFound',
    path: '/:path(.*)*',
    component: () => import('#/views/_core/fallback/not-found.vue'),
    meta: {
      hideInBreadcrumb: true,
      hideInMenu: true,
      hideInTab: true,
      title: '404',
    },
  },
];

export { fallbackNotFoundRoute, routes };


