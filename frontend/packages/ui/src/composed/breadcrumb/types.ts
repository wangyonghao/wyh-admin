// Adapted from vue-vben-admin @vben-core/shadcn-ui (MIT).
// Copyright (c) 2024-present, Vben.

import type { Component } from 'vue';

import type { BreadcrumbStyleType } from '@wyh/typings';

export interface IBreadcrumb {
  icon?: Component | string;
  isHome?: boolean;
  items?: IBreadcrumb[];
  path?: string;
  title?: string;
}

export interface BreadcrumbProps {
  breadcrumbs: IBreadcrumb[];
  showIcon?: boolean;
  styleType?: BreadcrumbStyleType;
}
