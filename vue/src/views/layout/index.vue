<template>
  <div>
    <!-- 顶部导航栏 -->
    <div class="fixed top-0 left-0 right-0 h-[60px] flex items-center bg-white/85 backdrop-blur-md shadow-sm z-[1001] px-6">
      <!-- Logo -->
      <div class="flex items-center flex-shrink-0 min-w-[200px]">
        <img :src="logo" alt="" class="w-9 h-9 mr-2.5 drop-shadow-sm">
        <span class="font-bold text-xl tracking-wide bg-gradient-to-r from-[var(--color-primary)] to-[var(--color-primary-light)] bg-clip-text text-transparent">失物招领小程序后台管理端</span>
      </div>

      <!-- 水平菜单 -->
      <div class="flex-1 min-w-0 h-full flex items-center mx-5">
        <SideBar/>
      </div>

      <!-- 用户区域 -->
      <div class="flex items-center flex-shrink-0 gap-2">
        <span class="text-sm text-gray-500 px-1.5">您好: {{ userStore.name }}</span>

        <!-- 退出按钮 -->
        <button @click="logout" class="flex items-center h-[34px] px-3.5 rounded-full text-sm text-gray-500 hover:text-red-500 hover:bg-red-50 transition-colors duration-200">
          <SvgIcon icon-class="logout" class="w-4 h-4"/>
          <span class="ml-1">退出登录</span>
        </button>

        <!-- 头像下拉 -->
        <div class="relative" ref="dropdownRef">
          <div @click="dropdownOpen = !dropdownOpen" class="flex items-center py-1 pl-1 pr-1.5 rounded-full cursor-pointer hover:bg-[var(--color-primary)]/10 transition-colors">
            <img :src="userStore.avatar" alt="" class="w-[34px] h-[34px] rounded-full object-cover border-2 border-white shadow-[0_0_0_2px_var(--color-primary-border)]">
            <svg class="w-3 h-3 text-gray-400 ml-1 transition-transform duration-200" :class="{ 'rotate-180': dropdownOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </div>

          <!-- 下拉菜单 -->
          <Transition name="dropdown-fade">
            <div v-if="dropdownOpen" class="absolute right-0 top-full mt-2 w-40 bg-white rounded-lg shadow-lg border border-gray-100 py-1 z-50">
              <router-link to="/user/profile" class="block px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors" @click="dropdownOpen = false">
                个人中心
              </router-link>
            </div>
          </Transition>
        </div>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="flex relative top-[60px] min-h-[calc(100vh-60px)]">
      <LeftSidebar/>

      <!-- 主内容 -->
      <main class="flex-1 min-h-[calc(100vh-60px)] bg-gradient-to-b from-[#f5f7fa] to-[#eef2f7] transition-[margin] duration-300 ease-out" :class="{ 'ml-[220px]': hasSidebar }">
        <!-- 面包屑 -->
        <div class="px-5 pt-3.5 pb-1 text-[13px]">
          <nav class="flex items-center gap-1.5 text-gray-400">
            <template v-for="(item, index) in breadItems" :key="index">
              <span v-if="index > 0" class="text-gray-300">/</span>
              <span :class="index === breadItems.length - 1 ? 'text-gray-800 font-medium' : ''">{{ item.meta.title }}</span>
            </template>
          </nav>
        </div>

        <AppMain/>
      </main>
    </div>
  </div>
</template>

<script setup>
import logo from '@/assets/logo/logo.png'
import {computed, onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import useUserStore from "@/stores/modules/userStore.js";
import useRouteStore from "@/stores/modules/routeStore.js";
import {ElMessageBox} from "@/utils/toast.js";
import SvgIcon from "@/components/SvgIcon/index.vue";
import AppMain from "@/views/layout/components/AppMain.vue";
import SideBar from './components/Sidebar'
import LeftSidebar from './components/LeftSidebar/index.vue'

const userStore = useUserStore()
const breadItems = ref([])
const route = useRoute()
const routeStore = useRouteStore()
const dropdownOpen = ref(false)

// 点击外部关闭下拉
const dropdownRef = ref(null)
const handleClickOutside = (e) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}
onMounted(() => document.addEventListener('click', handleClickOutside))

const hasSidebar = computed(() => {
  const path = route.path
  const topItems = routeStore.sidebarRouters.filter(r => !r.hidden)
  let best = null
  let bestLen = 0
  for (const item of topItems) {
    const itemPath = item.path
    if (path === itemPath || path.startsWith(itemPath + '/')) {
      if (itemPath.length > bestLen) { best = item; bestLen = itemPath.length }
    }
  }
  if (!best?.children) return false
  const visible = best.children.filter(c => !c.hidden)
  return !(visible.length <= 1 && !best.alwaysShow)
})

const logout = () => {
  ElMessageBox.confirm('确定退出系统吗?', '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => { location.href = '/login' })
  }).catch(() => {})
}

const isDashboard = (route) => {
  const path = route && route.path
  return path === '/' || path === '/index'
}

const getBread = () => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  if (matched.length === 0 || !isDashboard(matched[0])) {
    matched.unshift({ path: '/', meta: { title: '首页' }, name: 'Index' })
  }
  breadItems.value = matched
}

onMounted(() => getBread())
watch(() => route.path, () => getBread())
</script>

<style scoped>
.dropdown-fade-enter-active, .dropdown-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.dropdown-fade-enter-from, .dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
