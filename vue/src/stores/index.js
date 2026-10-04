//导入Pinia的创建函数
import { createPinia } from 'pinia'

/**
 * 创建一个全局的pinia状态管理仓库
 * 作用: 整个应用共享的状态管理中心
 * 就像公司的"总档案库", 所有部门都可以在这里存储共享数据
 *
 * pinia是vue3官方推荐的状态管理工具
 */
const store = createPinia()

//导出仓库实例
export default store
