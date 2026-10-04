<template>
  <nav class="w-full h-[60px] flex items-center gap-1">
    <template v-for="(item, index) in topMenuItems" :key="item.path + index">
      <app-link :to="getTopMenuPath(item)">
        <div
          class="h-10 flex items-center px-4 mx-0.5 rounded-lg text-sm font-medium cursor-pointer transition-colors duration-200"
          :class="activeMenu === item.path
            ? 'text-white bg-gradient-to-r from-[var(--color-primary)] to-[var(--color-primary-light)] shadow-[0_4px_12px_rgba(64,158,255,0.35)] font-semibold'
            : 'text-gray-600 hover:text-[var(--color-primary)] hover:bg-[var(--color-primary-bg)]'"
        >
          <svg-icon :icon-class="item.displayIcon" class="w-4 h-4 mr-1.5 transition-transform duration-200" :class="{ 'scale-110': activeMenu === item.path }"/>
          <span>{{ item.displayTitle }}</span>
        </div>
      </app-link>
    </template>
  </nav>
</template>

<script setup>
import {computed} from "vue";
import {useRoute, useRouter} from "vue-router";
import AppLink from "@/views/layout/components/Sidebar/AppLink.vue";
import SvgIcon from "@/components/SvgIcon/index.vue";
import useRouteStore from "@/stores/modules/routeStore.js";

const route = useRoute()
const router = useRouter()
const routeStore = useRouteStore()

const topMenuItems = computed(() => {
  return routeStore.sidebarRouters
    .filter(r => !r.hidden)
    .map(item => {
      let displayTitle = item.meta?.title
      let displayIcon = item.meta?.icon
      if (!displayTitle) {
        const firstChild = (item.children || []).find(c => !c.hidden)
        if (firstChild?.meta) {
          displayTitle = firstChild.meta.title
          displayIcon = firstChild.meta.icon
        }
      }
      return { ...item, displayTitle, displayIcon }
    })
})

const activeMenu = computed(() => {
  const path = route.path
  let best = null
  let bestLen = 0
  for (const item of topMenuItems.value) {
    const itemPath = item.path
    // 根路径特殊处理：startsWith('/'+'/') 会变成 '//' 导致匹配失败
    const matches = itemPath === '/'
      ? (path === '/' || path === '/index')
      : (path === itemPath || path.startsWith(itemPath + '/'))
    if (matches) {
      if (itemPath.length > bestLen) { best = itemPath; bestLen = itemPath.length }
    }
  }
  // 当只有一个顶部菜单时，直接选中它
  if (!best && topMenuItems.value.length === 1) {
    return topMenuItems.value[0].path
  }
  return best || path
})

const getTopMenuPath = (item) => {
  const visibleChildren = (item.children || []).filter(c => !c.hidden)
  if (visibleChildren.length === 0) return item.path
  const findLeaf = (node) => {
    const children = (node.children || []).filter(c => !c.hidden)
    if (children.length === 0) return node.path
    return findLeaf(children[0])
  }
  const leafPath = findLeaf(visibleChildren[0])
  const base = item.path === '/' ? '' : item.path
  return (base + '/' + leafPath).replace(/\/\//g, '/')
}
</script>
