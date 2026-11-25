<script lang="ts" setup>
import type { RouteLocationNormalizedLoadedGeneric } from 'vue-router';

import { unref } from 'vue';
import { RouterView } from 'vue-router';

import { usePreferences } from '@vben/preferences';
import { getTabKey, storeToRefs, useTabbarStore } from '@vben/stores';

import { transformComponent, useLayoutHook } from '../../hooks';
import { IFrameRouterView } from '../../iframe';
import { RouteCachedPage, RouteCachedView } from '../../route-cached';

defineOptions({ name: 'LayoutContent' });

const tabbarStore = useTabbarStore();
const { keepAlive } = usePreferences();

const { getCachedTabs, getExcludeCachedTabs, renderRouteView } =
  storeToRefs(tabbarStore);

const { getTransitionName } = useLayoutHook();

/**
 * 是否显示component
 * @param route
 */
const showComponent = (route: RouteLocationNormalizedLoadedGeneric) => {
  return !route.meta.domCached && unref(renderRouteView);
};
</script>

<template>
  <div class="relative h-full">
    <IFrameRouterView />
    <RouteCachedView />
    <RouterView v-slot="{ Component, route }">
      <RouteCachedPage
        v-if="route.meta.domCached"
        :component="Component"
        :route="route"
      />
      <template v-else>
        <!-- 缓存保持挂载。已打开页签直接从这里显示，不再套 out-in。 -->
        <KeepAlive
          v-if="keepAlive"
          :exclude="getExcludeCachedTabs"
          :include="getCachedTabs"
        >
          <component
            :is="transformComponent(Component, route)"
            v-if="showComponent(route) && !getTransitionName(route)"
            v-show="!route.meta.iframeSrc"
            :key="getTabKey(route)"
          />
        </KeepAlive>
        <!-- 首次进入才过渡，并且只包这一页，不把 KeepAlive 放进 out-in。 -->
        <Transition
          v-if="getTransitionName(route)"
          appear
          mode="out-in"
          :name="getTransitionName(route)"
        >
          <component
            :is="keepAlive ? transformComponent(Component, route) : Component"
            v-if="showComponent(route)"
            v-show="!route.meta.iframeSrc"
            :key="getTabKey(route)"
          />
        </Transition>
        <component
          v-else-if="!keepAlive && showComponent(route)"
          :is="Component"
          :key="getTabKey(route)"
        />
      </template>
    </RouterView>
  </div>
</template>
