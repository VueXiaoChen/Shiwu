import {defineStore} from "pinia";
import {getRouters} from "@/api/system/menu.js";
import { constantRouters } from '@/router/index.js'
import Layout from '@/views/layout/index.vue'

//使用vite的自动导入功能: 一次性导入views目录下的所有.vue的文件
const modules = import.meta.glob('./../../views/**/*.vue')

const useRouteStore = defineStore(
    'permission', //这里全局必须唯一
    {
      state: () => ({
        routes: [], //存储所有路由(固定路由 + 用户专属路由)
        sidebarRouters: [], //存储侧边栏菜单路由 (用于显示菜单)
      }),
      actions: {
        //设置路由数据 - 更新仓库里面的路由立碑哦啊
        setRoutes(routes) {
          this.sidebarRouters = routes
        },
        //设置侧边栏路由数据 - 专门用来显示菜单的路由
        setSidebarRouters(routes) {
          this.sidebarRouters = routes
        },
        //核心方法: 生成用户的专属路由
        generateRoutes() {
          return new Promise(resolve => {
            //调用API从后端获取路由数据
            getRouters().then(res => {
              //深拷贝一份数据, 避免修改原始数据
              const routerData = JSON.parse(JSON.stringify(res.data));

              //转换数据格式: 把后端的数据变成前端路由能用的格式
              const sidebarRoutes = convertToRoutes(routerData);

              //保存到仓库
              this.setRoutes(sidebarRoutes)
              //侧边栏显示: 侧边栏显示: 固定路由 + 用户路由
              this.setSidebarRouters(constantRouters.concat(sidebarRoutes))

              //告诉调用者: 我处理完了, 这是处理好的路由数据
              resolve(sidebarRoutes);
            })
          })
        }
      }
    }
)

/**
 * 核心数据转换: 把后端的数据变成前端路由能用的格式
 * 后端给的格式可能是这样的:
 * [
 *   {
 *     "name": "System",
 *     "path": "/system/xxx",
 *     "component": "Layout",
 *     "children": [...]
 *   }
 * ]
 *
 * 前端需要的格式是这样的:
 * [
 *   {
 *     "name": "System",
 *     "path": "/system/xxx",
 *     "component": Layout组件对象,
 *     "children": [...]
 *   }
 * ]
 *
 * @param routes 后端返回的路由数组数据
 */
const convertToRoutes = (routes) => {
  //对每个路由进行处理
  return routes.map(route => {
    //1. 把字符串转换成真正的组件
    if (route.component){
      if (route.component === 'Layout') {
        //如果是Layout, 就用导入的主布局组件
        route.component = Layout
      } else {
        //如果是页面组件, 就动态加载
        //比如把'system/user/index'变成对应的vue组件
        route.component = loadView(route.component)
      }
    }

    //2.递归处理子路由
    if (route.children && route.children.length ) {
      //继续处理子路由
      route.children = convertToRoutes(route.children)
    } else {
      //如果没有子路由, 就删除children属性
      delete route.children
    }

    return route
  })
}

//动态加载视图组件
const loadView = (view) => {
  for (const path in modules) {
    //从完整路径中提取相对路径
    //完整路径: ./../../views/system/user/index.vue -> 相对路径: /system/user/index
    const dir = path.split('views/')[1].split('.vue')[0]

    //如果找到了匹配的路径
    if (dir === view){
      return () => modules[path]()
    }
  }
  //如果找不到对应的组件, 直接返回null
  return null
}

//导出这个store, 其他地方可以导入使用了
export default useRouteStore
