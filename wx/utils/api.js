/**
 * API 请求封装
 * 
 * 基于 wx.request 封装的网络请求工具
 * 功能：自动添加 Token、统一错误处理、请求/响应拦截
 */

// 获取全局 app 实例
const app = getApp()

/**
 * 基础请求方法
 * @param {object} options - 请求配置
 * @param {string} options.url - 接口路径（相对路径，如 '/login'）
 * @param {string} options.method - 请求方法 GET/POST/PUT/DELETE
 * @param {object} options.data - 请求参数
 * @param {boolean} options.isToken - 是否需要携带 Token，默认 true
 * @param {boolean} options.showLoading - 是否显示加载中，默认 false
 * @returns {Promise}
 */
function request(options) {
  return new Promise((resolve, reject) => {
    const {
      url,
      method = 'GET',
      data = {},
      isToken = true,
      showLoading = false
    } = options

    // 显示加载提示
    if (showLoading) {
      wx.showLoading({ title: '加载中...', mask: true })
    }

    // 构建请求头
    const header = {
      'Content-Type': 'application/json;charset=utf-8'
    }

    // 需要 Token 且已登录时，自动携带 Authorization
    if (isToken) {
      const token = app.globalData.token || wx.getStorageSync('token')
      if (token) {
        header['Authorization'] = 'Bearer ' + token
      }
    }

    // 发起请求
    wx.request({
      url: app.globalData.baseUrl + url,
      method: method,
      data: data,
      header: header,
      success(res) {
        // HTTP 状态码异常处理
        if (res.statusCode === 401) {
          // Token 过期，清除登录状态
          app.clearLoginState()
          wx.showModal({
            title: '提示',
            content: '登录已过期，请重新登录',
            showCancel: false,
            success() {
              // 跳转到个人页面重新登录
              wx.switchTab({ url: '/pages/mine/index' })
            }
          })
          reject(new Error('登录已过期'))
          return
        }

        if (res.statusCode !== 200) {
          wx.showToast({ title: '请求失败: ' + res.statusCode, icon: 'none' })
          reject(new Error('HTTP ' + res.statusCode))
          return
        }

        // 解析业务状态码
        const bizData = res.data
        if (bizData.code === 401) {
          app.clearLoginState()
          wx.showModal({
            title: '提示',
            content: '登录已过期，请重新登录',
            showCancel: false,
            success() {
              wx.switchTab({ url: '/pages/mine/index' })
            }
          })
          reject(new Error('登录已过期'))
          return
        }

        if (bizData.code !== 200) {
          wx.showToast({ title: bizData.msg || '操作失败', icon: 'none' })
          reject(new Error(bizData.msg || '操作失败'))
          return
        }

        // 请求成功，返回业务数据
        resolve(bizData)
      },
      fail(err) {
        // 网络异常处理
        const errMsg = err.errMsg || ''
        if (errMsg.includes('timeout')) {
          wx.showToast({ title: '请求超时，请重试', icon: 'none' })
        } else if (errMsg.includes('fail')) {
          wx.showToast({ title: '网络连接异常', icon: 'none' })
        } else {
          wx.showToast({ title: '请求失败', icon: 'none' })
        }
        reject(err)
      },
      complete() {
        // 隐藏加载提示
        if (showLoading) {
          wx.hideLoading()
        }
      }
    })
  })
}

/**
 * GET 请求
 */
function get(url, params = {}, options = {}) {
  return request({
    url,
    method: 'GET',
    data: params,
    ...options
  })
}

/**
 * POST 请求
 */
function post(url, data = {}, options = {}) {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

/**
 * PUT 请求
 */
function put(url, data = {}, options = {}) {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

/**
 * DELETE 请求
 */
function del(url, data = {}, options = {}) {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}

/**
 * 文件上传
 * 把本地临时文件传给后端 /file/upload，拿到服务器上的访问地址
 * @param {string} filePath - 本地临时文件路径（wx.chooseMedia 拿到的）
 * @returns {Promise<string>} 服务器返回的文件访问URL（相对路径，如 /profile/upload/xxx.jpg）
 */
function upload(filePath) {
  return new Promise((resolve, reject) => {
    const token = app.globalData.token || wx.getStorageSync('token')

    wx.uploadFile({
      url: app.globalData.baseUrl + '/file/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success(res) {
        // uploadFile 返回的是字符串，得自己解析成对象
        let bizData
        try {
          bizData = JSON.parse(res.data)
        } catch (e) {
          reject(new Error('上传返回数据异常'))
          return
        }

        if (bizData.code !== 200) {
          wx.showToast({ title: bizData.msg || '图片上传失败', icon: 'none' })
          reject(new Error(bizData.msg || '图片上传失败'))
          return
        }

        resolve(bizData.data.url)
      },
      fail(err) {
        wx.showToast({ title: '图片上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = {
  request,
  get,
  post,
  put,
  del,
  upload
}
