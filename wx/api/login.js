/**
 * 登录相关 API
 */
const { post, get } = require('../utils/api')

/**
 * 微信小程序登录
 * 将微信登录凭证发送给后端，换取 JWT Token
 * 
 * @param {object} params
 * @param {string} params.code - wx.login() 获取的临时凭证
 * @param {object} params.userInfo - wx.getUserProfile() 获取的用户信息
 * @returns {Promise}
 */
function wxLogin(params) {
  return post('/wx-login', params, { isToken: false, showLoading: true })
}

/**
 * 获取当前登录用户信息
 * @returns {Promise}
 */
function getInfo() {
  return get('/getInfo')
}

/**
 * 退出登录
 * @returns {Promise}
 */
function logout() {
  return post('/logout')
}

module.exports = {
  wxLogin,
  getInfo,
  logout
}
