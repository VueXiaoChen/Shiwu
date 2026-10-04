import {createRouter, createWebHistory} from 'vue-router'
//导入布局组件
import Layout from '@/views/layout'

//路由配置
export const constantRouters = [
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/404'),
    hidden: true
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile'),
        name: 'Profile',
        meta: {title: '个人中心'}
      },
    ]
  },
  {
    path: '/user',
    component: () => import('@/views/userPage/index'),
    redirect: '/user/home',
    hidden: true,
    children: [
      {
        path: 'home',
        component: () => import('@/views/userPage/home'),
        hidden: true,
      },
      {
        path: 'self',
        component: () => import('@/views/system/user/profile'),
        hidden: true,
      }
    ]
  }
]

//创建路由器实例
const router = createRouter({
  history: createWebHistory(),
  routes: constantRouters,
  scrollBehavior(to, from, savedPosition) {
    //滚动行为配置: 控台路由切换时的页面滚动位置
    if (savedPosition) {
      //如果有保存的位置, 恢复到之前的位置
      return savedPosition
    }
    //否则滚动到顶部
    return {top: 0}
  }
})

export default router
