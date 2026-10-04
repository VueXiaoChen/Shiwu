/**
 * 用户相关 API
 */
const { get, put } = require('../utils/api')

/**
 * 获取用户详细信息
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
function selectUserByUserId(userId) {
  return get('/system/user/selectUserByUserId/' + userId)
}

/**
 * 修改用户个人资料
 * @param {object} data - 用户资料
 * @returns {Promise}
 */
function updateProfile(data) {
  return put('/system/user/profile', data, { showLoading: true })
}

module.exports = {
  selectUserByUserId,
  updateProfile
}
