//导入路由实例
import router from './router'
import {getToken} from "@/utils/auth.js";
import useUserStore from "@/stores/modules/userStore.js";
import {isReLogin} from "@/utils/request.js";
import {ElMessage} from "@/utils/toast.js";
import useRouteStore from "@/stores/modules/routeStore.js";

//白名单 定义不需要登录就可以访问的路由路径
const whiteList = ['/login']

//判断路径是否在白名单中
const isWhiteList = (path) => {
  return whiteList.includes(path)
}

//全局路由守卫 每次路由跳转前都会执行这个函数
router.beforeEach((to, form, next) => {
  //先检查用户是否有token (相当于检查有没有会员卡)
  if (getToken()) {
    //情况1: 用户有token(已登录)

    //情况1.1 用户已经登录, 但是要去登录页
    if (to.path === '/login') {
      //已经登录了还要去登录页? 直接跳转到首页
      next({path: '/'})
    }

    //情况1.2 用户已登录 访问白名单页面
    else if (isWhiteList(to.path)) {
      //允许访问
      next()
    }

    //情况1.3 用户已登录 访问需要权限的页面
    else {
      //检查store中是否有用户信息
      if (useUserStore().name === '') {
        //用户信息为空, 需要先获取用户信息

        //设置"正在重新登录"的标志
        isReLogin.show = true

        //调用获取用户信息的方法
        useUserStore().getInfo().then(res => {
          //清除标志
          isReLogin.show = false

          //调用路由状态工具方法
          useRouteStore().generateRoutes().then(accessRoutes => {
            //根据角色权限生成可访问的路由表
            accessRoutes.forEach(route => {
              router.addRoute(route) //动态添加可访问的路由表
            })

            //根据角色名称确定跳转路径
            const userRoleName = res.data.roleName
            let redirectPath = to.path
            //如果是跟路径, 则根据角色名称跳转页面
            if (to.path === '/' || to.path === '/index' || to.path === '/user') {
              if (userRoleName === 'admin') {
                //管理员角色默认跳转到默认路径
                redirectPath = '/index'
              } else if (userRoleName === 'user') {
                //普通用户跳转到用户前台路径
                redirectPath = '/user/home'
              }
            }
            //如果需要跳转到特定页面
            if (redirectPath !== to.path) {
              next({ path: redirectPath, replace: true })
            } else {
              next({ ...to, replace: true })
            }
          })
        }).catch(err => {
          //获取用户信息失败(token可能过期了)
          //调用退出登录的办法
          useUserStore().logOut().then(() => {
            //显示错误信息
            ElMessage.error(err)

            //跳转到登录页
            next({path: '/login'})
          })
        })
      } else {
        //用户信息已存在, 直接放行
        next()
      }
    }
  } else {
    //没有token
    if (isWhiteList(to.path)) {
      //在免登录白名单, 直接进入
      next()
    } else {
      //否则全部重定向到登录页
      next({path: '/login'})
    }
  }
})
