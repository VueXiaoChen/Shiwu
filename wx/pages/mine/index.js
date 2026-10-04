/**
 * 个人中心页面
 * 
 * 三种状态切换：
 *   状态 1：未登录 → 显示「微信一键登录」按钮
 *   状态 2：已登录但无头像昵称 → 显示头像选择器 + 昵称输入框
 *   状态 3：已登录且有头像昵称 → 显示完整个人资料
 * 
 * 适配微信新版基础库（3.x）：
 *   - getUserProfile 已废弃，用 wx.login 做登录
 *   - 头像用 <button open-type="chooseAvatar">
 *   - 昵称用 <input type="nickname">
 */

const { wxLogin, logout } = require('../../api/login')
const { updateProfile } = require('../../api/user')
const toast = require('../../utils/toast')

Page({
  data: {
    // 是否已登录
    isLoggedIn: false,
    // 是否已设置头像和昵称（用于决定显示状态 2 还是状态 3）
    hasProfile: false,
    // 登录按钮 loading
    loginLoading: false,
    // 保存按钮 loading
    saving: false,
    // 微信用户信息
    userInfo: {
      avatarUrl: '',
      nickName: ''
    },
    // 头像选择器临时数据
    tempAvatar: '',
    // 昵称输入框临时数据
    tempNickName: '',
    // 昵称输入框是否自动聚焦（登录后设为 true，弹出微信昵称建议）
    nicknameFocus: false,
    // 后端用户数据
    userId: '',
    userName: '',
    genderText: ''
  },

  onShow() {
    this.checkLoginState()
  },

  /**
   * 头像 URL 补全
   * 后端存的可能是相对路径（/profile/avatar/xxx.jpg），
   * 小程序需要拼上 baseUrl 才能显示
   */
  fixAvatarUrl(url) {
    if (!url) return ''
    // 已经是完整 URL 就直接用
    if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('wxfile://')) {
      return url
    }
    // 相对路径补上后端地址
    return getApp().globalData.baseUrl + url
  },

  /**
   * 检查登录状态，决定显示哪个状态
   */
  checkLoginState() {
    const app = getApp()

    if (app.checkLogin()) {
      const cachedInfo = app.globalData.userInfo
      const hasAvatar = cachedInfo && cachedInfo.avatarUrl
      const hasNickName = cachedInfo && cachedInfo.nickName && cachedInfo.nickName !== '微信用户'

      this.setData({
        isLoggedIn: true,
        hasProfile: !!(hasAvatar && hasNickName),
        userInfo: {
          avatarUrl: this.fixAvatarUrl(cachedInfo?.avatarUrl || ''),
          nickName: cachedInfo?.nickName || ''
        },
        userId: cachedInfo?.userId || '',
        userName: cachedInfo?.userName || '',
        genderText: cachedInfo?.sex !== undefined
          ? (cachedInfo.sex === 0 ? '男' : '女')
          : '',
        // 初始化临时数据
        tempAvatar: cachedInfo?.avatarUrl || '',
        tempNickName: cachedInfo?.nickName || ''
      })


    } else {
      this.setData({
        isLoggedIn: false,
        hasProfile: false,
        userInfo: { avatarUrl: '', nickName: '' },
        userId: '',
        userName: '',
        genderText: '',
        tempAvatar: '',
        tempNickName: '',
        nicknameFocus: false
      })
    }
  },



  // ========================
  //  状态 1：微信一键登录
  // ========================

  /**
   * 微信一键登录
   * 只做一件事：wx.login() 拿 code → 发后端换 token
   * 新版微信不再允许一键获取头像昵称，改为登录后再设置
   */
  handleWxLogin() {
    const that = this

    if (this.data.loginLoading) return

    this.setData({ loginLoading: true })

    wx.login({
      success(loginRes) {
        if (!loginRes.code) {
          toast.error('获取微信登录凭证失败')
          that.setData({ loginLoading: false })
          return
        }

        // 把 code 发给后端（不再附带 userInfo，因为 getUserProfile 已废弃）
        wxLogin({
          code: loginRes.code,
          userInfo: null  // 新版不再传用户信息
        }).then(res => {
          const token = res.token
          const serverUser = res.user || res.data || {}
          const isNewUser = res.isNewUser !== false  // 后端标记：是否首次注册

          // 判断老用户是否已有头像（有头像 = 之前设置过个人信息）
          const alreadyHasProfile = !isNewUser && serverUser.avatar

          // 构建本地用户信息（头像补全为完整URL再存，下次就不用再补了）
          const localUserInfo = {
            nickName: alreadyHasProfile ? (serverUser.userName || '') : '',
            avatarUrl: alreadyHasProfile ? that.fixAvatarUrl(serverUser.avatar || '') : '',
            userId: serverUser.userId || '',
            userName: serverUser.userName || '',
            sex: serverUser.sex
          }

          // 保存登录状态
          getApp().setLoginState(token, localUserInfo)

          if (alreadyHasProfile) {
            // 老用户且已设置过头像 → 直接进个人主页，不用重新设置
            that.setData({
              isLoggedIn: true,
              hasProfile: true,
              loginLoading: false,
              nicknameFocus: false,
              userInfo: {
                avatarUrl: that.fixAvatarUrl(serverUser.avatar || ''),
                nickName: serverUser.userName || ''
              },
              userId: serverUser.userId || '',
              userName: serverUser.userName || '',
              genderText: serverUser.sex !== undefined
                ? (serverUser.sex === 0 ? '男' : '女')
                : ''
            })
            toast.success('欢迎回来')
          } else {
            // 新用户或未设置过头像 → 进设置页（自动聚焦昵称框，弹出微信昵称建议）
            that.setData({
              isLoggedIn: true,
              hasProfile: false,
              loginLoading: false,
              nicknameFocus: true,
              tempAvatar: '',
              tempNickName: '',
              userId: serverUser.userId || '',
              userName: serverUser.userName || ''
            })
            toast.success('登录成功')
          }

        }).catch(err => {
          console.error('登录失败:', err)
          that.setData({ loginLoading: false })
        })
      },

      fail(err) {
        console.error('wx.login 失败:', err)
        toast.error('微信登录失败，请重试')
        that.setData({ loginLoading: false })
      }
    })
  },

  // ========================
  //  状态 2：设置头像和昵称
  // ========================

  /**
   * 选择头像
   * 由 <button open-type="chooseAvatar"> 触发
   */
  onChooseAvatar(e) {
    const avatarUrl = e.detail.avatarUrl
    if (avatarUrl) {
      this.setData({ tempAvatar: avatarUrl })
    }
  },

  /**
   * 昵称输入（实时更新）
   */
  onNickInput(e) {
    this.setData({ tempNickName: e.detail.value })
  },

  /**
   * 昵称输入失焦（微信 type="nickname" 会在失焦时回填微信昵称）
   */
  onNickBlur(e) {
    this.setData({ tempNickName: e.detail.value })
  },

  /**
   * 保存头像和昵称到后端
   * 
   * 头像流程：chooseAvatar 拿到的只是本地临时路径（wxfile://...），
   * 必须先 wx.uploadFile 上传到后端，拿到永久访问 URL 再存进数据库
   */
  saveProfile() {
    const that = this
    const { tempAvatar, tempNickName } = this.data

    if (!tempNickName || !tempNickName.trim()) {
      toast.info('请输入昵称')
      return
    }

    this.setData({ saving: true })

    // 内层函数：真正调 updateProfile 保存昵称和头像URL
    const doUpdateProfile = (avatarUrl) => {
      // 上传返回的是相对路径，补全为完整URL再存
      const fullAvatarUrl = that.fixAvatarUrl(avatarUrl)
      updateProfile({
        userName: tempNickName.trim(),
        avatar: avatarUrl || ''  // 数据库存相对路径，后端负责解析
      }).then(() => {
        // 更新本地缓存（存完整URL，显示直接用）
        const app = getApp()
        const cachedInfo = app.globalData.userInfo || {}
        cachedInfo.nickName = tempNickName.trim()
        cachedInfo.avatarUrl = fullAvatarUrl || tempAvatar
        cachedInfo.userName = tempNickName.trim()
        app.setLoginState(app.globalData.token, cachedInfo)

        that.setData({
          hasProfile: true,
          saving: false,
          userInfo: {
            avatarUrl: fullAvatarUrl || tempAvatar,
            nickName: tempNickName.trim()
          },
          userName: tempNickName.trim()
        })

        toast.success('设置完成')
      }).catch(() => {
        that.setData({ saving: false })
      })
    }

    // 如果选了头像，先上传文件到后端
    if (tempAvatar) {
      const app = getApp()
      wx.uploadFile({
        url: app.globalData.baseUrl + '/system/user/profile/avatar',
        filePath: tempAvatar,
        name: 'file',
        header: {
          'Authorization': 'Bearer ' + app.globalData.token
        },
        success(uploadRes) {
          try {
            const data = JSON.parse(uploadRes.data)
            if (data.code === 200 && data.imgUrl) {
              // 上传成功，拿返回的永久URL去保存
              doUpdateProfile(data.imgUrl)
            } else {
              // 上传失败，直接用临时路径保存（降级方案）
              doUpdateProfile(tempAvatar)
            }
          } catch (e) {
            doUpdateProfile(tempAvatar)
          }
        },
        fail() {
          // 上传失败，降级：直接保存临时路径（至少昵称能存上）
          doUpdateProfile(tempAvatar)
        }
      })
    } else {
      // 没选头像，只更新昵称
      doUpdateProfile('')
    }
  },

  /**
   * 跳过设置头像昵称（直接进入状态 3）
   */
  skipProfile() {
    this.setData({ hasProfile: true })
  },

  // ========================
  //  状态 3：个人资料
  // ========================

  /**
   * 修改性别
   * 弹出微信原生选择器，选完后直接调后端更新
   */
  onChangeGender() {
    const that = this
    wx.showActionSheet({
      itemList: ['男', '女'],
      success(res) {
        const sex = res.tapIndex  // 0 → 男, 1 → 女
        const genderText = sex === 0 ? '男' : '女'

        updateProfile({ sex }).then(() => {
          // 同步更新本地缓存
          const app = getApp()
          const cachedInfo = app.globalData.userInfo || {}
          cachedInfo.sex = sex
          app.setLoginState(app.globalData.token, cachedInfo)

          that.setData({ genderText })
          toast.success('性别已更新')
        })
      }
    })
  },

  /** 我的发布 */
  goMyPublish() {
    wx.navigateTo({ url: '/pages/my-publish/index' })
  },

  /** 我的消息（tabBar 页需用 switchTab） */
  goMessage() {
    wx.switchTab({ url: '/pages/message/index' })
  },

  handleLogout() {
    toast.confirm('确定要退出登录吗？', '提示').then(() => {
      logout().then(() => {
        this.doLogout()
      }).catch(() => {
        this.doLogout()
      })
    }).catch(() => {})
  },

  doLogout() {
    getApp().clearLoginState()
    this.setData({
      isLoggedIn: false,
      hasProfile: false,
      userInfo: { avatarUrl: '', nickName: '' },
      userId: '',
      userName: '',
      genderText: '',
      tempAvatar: '',
      tempNickName: '',
      nicknameFocus: false
    })
    toast.success('已退出登录')
  }
})
