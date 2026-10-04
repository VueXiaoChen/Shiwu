<template>
  <div v-if="!item.hidden">
    <!-- 单个菜单项（不含子菜单） -->
    <template v-if="shouldShowSingleItem">
      <app-link :to="singleItemPath">
        <div
          class="mx-2 my-0.5 h-11 flex items-center px-3 rounded-lg text-sm font-medium cursor-pointer transition-colors duration-200"
          :class="route.path === singleItemPath
            ? 'text-[var(--color-primary)] bg-gradient-to-r from-[var(--color-primary-bg)] to-[var(--color-primary-bg)]/60 font-semibold'
            : 'text-gray-600 hover:text-[var(--color-primary)] hover:bg-[var(--color-primary-bg)]'"
        >
          <svg-icon :icon-class="onlyOneChild.meta?.icon || (item.meta && item.meta.icon)" class="w-4 h-4 mr-1.5"/>
          <span>{{ onlyOneChild.meta?.title }}</span>
        </div>
      </app-link>
    </template>

    <!-- 含子菜单（可展开折叠） -->
    <div v-else>
      <div
        class="mx-2 my-0.5 h-11 flex items-center justify-between px-3 rounded-lg text-sm font-medium cursor-pointer transition-colors duration-200"
        :class="isSubMenuActive
          ? 'text-[var(--color-primary)] bg-gradient-to-r from-[var(--color-primary-bg)] to-[var(--color-primary-bg)]/60 font-semibold'
          : 'text-gray-600 hover:text-[var(--color-primary)] hover:bg-[var(--color-primary-bg)]'"
        @click="subOpen = !subOpen"
      >
        <div class="flex items-center">
          <svg-icon v-if="item.meta" :icon-class="item.meta.icon" class="w-4 h-4 mr-1.5"/>
          <span>{{ item.meta?.title }}</span>
        </div>
        <svg class="w-3 h-3 transition-transform duration-200" :class="{ 'rotate-90': subOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
      </div>

      <!-- 子菜单 -->
      <div v-show="subOpen" class="overflow-hidden">
        <LeftSidebarItem v-for="child in item.children"
                         :key="child.path"
                         :item="child"
                         :base-path="resolvePath(child.path)"
                         is-next
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from "vue";
import {useRoute} from "vue-router";
import AppLink from "@/views/layout/components/Sidebar/AppLink.vue";
import SvgIcon from "@/components/SvgIcon/index.vue";

const props = defineProps({
  item: { type: Object, required: true },
  isNext: { type: Boolean, default: false },
  basePath: { type: String, default: '' }
})

const route = useRoute()
const subOpen = ref(false)

const onlyOneChild = computed(() => {
  const children = props.item.children || []
  const showingChildren = children.filter(item => !item.hidden)
  if (showingChildren.length === 1) return showingChildren[0]
  if (showingChildren.length === 0) return { ...props.item, path: '', noShowingChildren: true }
  return null
})

const shouldShowSingleItem = computed(() => {
  return onlyOneChild.value && (!onlyOneChild.value.children || onlyOneChild.value.noShowingChildren)
      && !props.item.alwaysShow
})

const singleItemPath = computed(() => resolvePath(onlyOneChild.value.path))

const isSubMenuActive = computed(() => {
  const children = props.item.children || []
  return children.some(c => {
    const resolved = resolvePath(c.path)
    return route.path === resolved || route.path.startsWith(resolved + '/')
  })
})

// Auto-open if current route is in this submenu
if (isSubMenuActive.value) {
  subOpen.value = true
}

const resolvePath = (routePath, base) => {
  const basePath = base !== undefined ? base : props.basePath
  const fullPath = basePath + '/' + routePath
  if (!fullPath) return fullPath
  return fullPath.replace('//', '/').replace(/\/$/, '')
}
</script>
