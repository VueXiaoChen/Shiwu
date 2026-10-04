<template>
  <aside v-if="leftMenuItems.length > 0" class="fixed top-[60px] left-0 w-[220px] h-[calc(100vh-60px)] overflow-y-auto bg-white border-r border-gray-100 shadow-[2px_0_8px_rgba(0,23,45,0.04)] z-[1000]">
    <nav class="py-2">
      <LeftSidebarItem v-for="(item, index) in leftMenuItems"
                       :key="item.path + index"
                       :item="item"
                       :base-path="activeTopMenu.path + '/' + item.path"
      />
    </nav>
  </aside>
</template>

<script setup>
import {computed} from "vue";
import {useRoute} from "vue-router";
import LeftSidebarItem from "./Item.vue";
import useRouteStore from "@/stores/modules/routeStore.js";

const route = useRoute()
const routeStore = useRouteStore()

const topMenuItems = computed(() => routeStore.sidebarRouters.filter(r => !r.hidden))

const activeTopMenu = computed(() => {
  const path = route.path
  let best = null
  let bestLen = 0
  for (const item of topMenuItems.value) {
    const itemPath = item.path
    if (path === itemPath || path.startsWith(itemPath + '/')) {
      if (itemPath.length > bestLen) { best = item; bestLen = itemPath.length }
    }
  }
  return best
})

const leftMenuItems = computed(() => {
  const top = activeTopMenu.value
  if (!top?.children) return []
  const visible = top.children.filter(c => !c.hidden)
  if (visible.length <= 1 && !top.alwaysShow) return []
  return visible
})
</script>
