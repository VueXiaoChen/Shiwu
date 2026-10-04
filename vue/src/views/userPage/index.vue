<template>
  <div class="min-h-screen bg-[var(--color-bg-page)]">
    <!-- 头部导航栏 — 毛玻璃现代风 -->
    <header class="sticky top-0 z-[1000]">
      <div class="flex items-center h-[64px] bg-white/75 backdrop-blur-xl border-b border-[var(--color-border)]/50 shadow-sm">
        <!-- 左侧 Logo -->
        <div class="flex items-center flex-shrink-0 pl-6 pr-8">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-sky-400 to-sky-600 flex items-center justify-center shadow-lg shadow-sky-200 flex-shrink-0">
            <svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
          </div>
          <span @click="router.push('/user/home')" class="ml-3 text-lg font-bold cursor-pointer bg-gradient-to-r from-sky-500 to-cyan-500 bg-clip-text text-transparent tracking-tight">前台用户端</span>
        </div>

        <!-- 中间导航 -->
        <nav class="flex-1 flex items-center h-full gap-1">
          <router-link
            to="/user/home"
            class="nav-link h-full flex items-center gap-1.5 px-4 text-sm font-semibold transition-all duration-200"
            :class="route.path === '/user/home' ? 'text-sky-600' : 'text-slate-500 hover:text-slate-700'"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor"><path d="M12 3L4 9v12h5v-7h6v7h5V9z"/></svg>
            首页
          </router-link>
          <router-link
            to="/user/self"
            class="nav-link h-full flex items-center gap-1.5 px-4 text-sm font-semibold transition-all duration-200"
            :class="route.path === '/user/self' ? 'text-sky-600' : 'text-slate-500 hover:text-slate-700'"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v1.2c0 .66.54 1.2 1.2 1.2h16.8c.66 0 1.2-.54 1.2-1.2v-1.2c0-3.2-6.4-4.8-9.6-4.8z"/></svg>
            个人中心
          </router-link>
        </nav>

        <!-- 右侧用户区域 -->
        <div class="flex items-center flex-shrink-0 pr-6">
          <!-- 未登录 -->
          <div v-if="!userStore.name" class="flex items-center gap-3">
            <button
              @click="router.push('/login')"
              class="px-5 py-2 text-sm font-semibold text-sky-600 border-2 border-sky-200 rounded-xl hover:bg-sky-50 hover:border-sky-300 active:scale-[0.97] transition-all"
            >
              登录
            </button>
          </div>

          <!-- 已登录 -->
          <div v-else class="flex items-center">
            <div class="relative" ref="dropdownRef">
              <div
                @click="dropdownOpen = !dropdownOpen"
                class="flex items-center gap-2 py-1.5 px-2 rounded-xl cursor-pointer hover:bg-slate-100 transition-colors"
              >
                <img :src="userStore.avatar" alt="" class="w-9 h-9 rounded-full object-cover ring-2 ring-sky-200 ring-offset-2">
                <span class="text-sm font-semibold text-slate-700">{{ userStore.name }}</span>
                <svg
                  class="w-3.5 h-3.5 text-slate-400 transition-transform duration-200"
                  :class="{ 'rotate-180': dropdownOpen }"
                  viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                ><polyline points="6 9 12 15 18 9"/></svg>
              </div>

              <Transition name="dropdown-fade">
                <div
                  v-if="dropdownOpen"
                  class="absolute right-0 top-full mt-2 w-36 bg-white rounded-xl shadow-xl border border-slate-100 py-1.5 z-50 overflow-hidden"
                >
                  <button
                    class="w-full text-left px-4 py-2.5 text-sm text-slate-600 hover:bg-slate-50 hover:text-sky-600 transition-colors"
                    @click="logout"
                  >
                    退出登录
                  </button>
                </div>
              </Transition>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from '@/utils/toast.js'
import useUserStore from '@/stores/modules/userStore.js'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const dropdownOpen = ref(false)

const dropdownRef = ref(null)
onMounted(() =>
  document.addEventListener('click', (e) => {
    if (dropdownRef.value && !dropdownRef.value.contains(e.target)) dropdownOpen.value = false
  })
)

const logout = () => {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      userStore.logOut().then(() => {
        location.href = '/index'
      })
    })
    .catch(() => {})
}
</script>

<style scoped>
/* 导航链接下划线动效 */
.nav-link {
  position: relative;
}
.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2.5px;
  background: linear-gradient(90deg, #0EA5E9, #06B6D4);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(-50%);
  border-radius: 2px;
}
.nav-link:hover::after {
  width: 65%;
}
.nav-link.router-link-active::after {
  width: 80%;
}

/* 下拉菜单动画 */
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
