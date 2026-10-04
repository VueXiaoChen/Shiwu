/**
 * 消息中心 — 留言互动
 *
 * 数据说明：已对接后端会话列表接口，
 * 每条 = 我和某人围绕某件物品的最新一条留言（附带未读数）
 */
const messageApi = require('../../api/message')
const { formatRelativeTime, fullImageUrl } = require('../../utils/format')

Page({
  data: {
    isLoggedIn: false,

    interactList: []
  },

  onShow() {
    const app = getApp()
    this.setData({ isLoggedIn: app.checkLogin() })
    if (app.checkLogin()) {
      this.refreshList()
    }
  },

  /** 从后端拉会话列表，翻译成页面渲染用的字段 */
  refreshList() {
    const app = getApp()
    const myId = Number(app.globalData.userInfo && app.globalData.userInfo.userId)

    messageApi.selectMyConversationList().then(res => {
      const interactList = (res.data || []).map(m => {
        // 最新这条留言是我发的还是对方发的？对方才是会话里"那个人"
        const iAmSender = Number(m.fromUserId) === myId
        return {
          id: m.messageId,
          itemId: m.itemId,
          itemTitle: m.itemTitle || '',
          peerUserId: iAmSender ? m.toUserId : m.fromUserId,
          user: {
            name: (iAmSender ? m.toUserName : m.fromUserName) || '同学',
            avatar: fullImageUrl(iAmSender ? m.toUserAvatar : m.fromUserAvatar)
          },
          // 最新一条是我发的就加个"我："前缀，一眼看出谁说的
          content: (iAmSender ? '我：' : '') + m.content,
          unread: (m.unreadCount || 0) > 0,
          time: formatRelativeTime(m.createTime)
        }
      })
      this.setData({ interactList })
    }).catch(() => {
      // 拉失败就先空着，下次 onShow 会再试
    })
  },

  /** 点击消息进入对话页（后端打开会话时会把未读标成已读，返回时 onShow 再刷新） */
  onMessageTap(e) {
    const id = Number(e.currentTarget.dataset.id)
    const msg = this.data.interactList.find(m => m.id === id)
    if (!msg || !msg.itemId) return

    wx.navigateTo({
      url: '/pages/chat/index?itemId=' + msg.itemId + '&peerUserId=' + msg.peerUserId
    })
  },

  /** 未登录时去"我的"页登录 */
  goLogin() {
    wx.switchTab({ url: '/pages/mine/index' })
  }
})
