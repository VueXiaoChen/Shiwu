/**
 * fast-wx 小程序入口
 * 
 * 整个小程序启动时最先执行的文件
 * 在这里做全局初始化：检查登录状态、预加载用户信息
 */
App({
  /**
   * 小程序启动时触发
   * 全局只触发一次
   */
  onLaunch() {
    // 从本地存储中恢复登录状态
    this.restoreLoginState()
  },

  /**
   * 从本地存储恢复登录状态
   * 如果之前登录过（有token），就自动拉取最新的用户信息
   */
  restoreLoginState() {
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')

    if (token && userInfo) {
      // 有缓存，先用缓存数据展示（不用等着加载）
      this.globalData.token = token
      this.globalData.userInfo = userInfo
      this.globalData.isLoggedIn = true
    }
  },

  /**
   * 全局共享数据
   * 所有页面都能通过 getApp() 访问到这里的数据
   */
  globalData: {
    // 用户登录后的JWT令牌
    token: '',
    // 用户信息（昵称、头像等）
    userInfo: null,
    // 是否已登录
    isLoggedIn: false,
    // 发布页类型意图（lost/found），首页入口跳转 tabBar 发布页时传递
    publishType: '',
    // 后端API基础地址（开发环境使用本地，生产环境需替换为正式域名）
    baseUrl: 'http://localhost:8080'
  },

  /**
   * 检查是否已登录
   * @returns {boolean}
   */
  checkLogin() {
    return this.globalData.isLoggedIn && !!this.globalData.token
  },

  /**
   * 保存登录状态到本地存储 + 全局数据
   * @param {string} token - JWT令牌
   * @param {object} userInfo - 用户信息对象
   */
  setLoginState(token, userInfo) {
    this.globalData.token = token
    this.globalData.userInfo = userInfo
    this.globalData.isLoggedIn = true

    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', userInfo)
  },

  /**
   * 清除登录状态（退出登录时调用）
   */
  clearLoginState() {
    this.globalData.token = ''
    this.globalData.userInfo = null
    this.globalData.isLoggedIn = false

    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
  }
})
