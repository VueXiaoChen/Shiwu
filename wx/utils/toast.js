/**
 * Toast 提示封装
 * 
 * 统一的消息提示工具，替代原生 wx.showToast / wx.showModal
 * 提供更友好的交互反馈
 */

/**
 * 成功提示
 * @param {string} title - 提示文字
 * @param {number} duration - 显示时长（毫秒）
 */
function success(title = '操作成功', duration = 2000) {
  wx.showToast({
    title,
    icon: 'success',
    duration
  })
}

/**
 * 错误提示
 * @param {string} title - 提示文字
 * @param {number} duration - 显示时长（毫秒）
 */
function error(title = '操作失败', duration = 2000) {
  wx.showToast({
    title,
    icon: 'error',
    duration
  })
}

/**
 * 普通提示（无图标）
 * @param {string} title - 提示文字
 * @param {number} duration - 显示时长（毫秒）
 */
function info(title, duration = 2000) {
  wx.showToast({
    title,
    icon: 'none',
    duration
  })
}

/**
 * 加载中提示
 * @param {string} title - 提示文字
 */
function loading(title = '加载中...') {
  wx.showLoading({
    title,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
function hideLoading() {
  wx.hideLoading()
}

/**
 * 确认弹窗
 * @param {string} content - 提示内容
 * @param {string} title - 弹窗标题
 * @returns {Promise} - 确认 resolve，取消 reject
 */
function confirm(content, title = '提示') {
  return new Promise((resolve, reject) => {
    wx.showModal({
      title,
      content,
      confirmText: '确定',
      cancelText: '取消',
      success(res) {
        if (res.confirm) {
          resolve()
        } else {
          reject()
        }
      },
      fail() {
        reject()
      }
    })
  })
}

module.exports = {
  success,
  error,
  info,
  loading,
  hideLoading,
  confirm
}
