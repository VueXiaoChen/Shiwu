/**
 * 认证管理工具
 * 
 * 负责 Token 和用户信息的本地存储管理
 * 与全局 app.globalData 配合使用，保证数据一致性
 */

const app = getApp()

/**
 * 获取当前 JWT Token
 * @returns {string}
 */
function getToken() {
  return app.globalData.token || wx.getStorageSync('token') || ''
}

/**
 * 获取缓存的用户信息
 * @returns {object|null}
 */
function getUserInfo() {
  return app.globalData.userInfo || wx.getStorageSync('userInfo') || null
}

/**
 * 检查是否已登录
 * @returns {boolean}
 */
function isLoggedIn() {
  return app.checkLogin()
}

/**
 * 保存登录凭证和用户信息
 * @param {string} token - JWT 令牌
 * @param {object} userInfo - 用户信息对象
 */
function setLoginState(token, userInfo) {
  app.setLoginState(token, userInfo)
}

/**
 * 清除登录凭证（退出登录）
 */
function clearLoginState() {
  app.clearLoginState()
}

module.exports = {
  getToken,
  getUserInfo,
  isLoggedIn,
  setLoginState,
  clearLoginState
}
