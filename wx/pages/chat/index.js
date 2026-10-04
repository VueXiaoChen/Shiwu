/**
 * 对话页 — 与物品发布人（失主/拾主）的一对一留言对话
 *
 * 入口：
 *  - 详情页底部"联系认领/联系失主"按钮（对方=物品发布人，不带 peerUserId）
 *  - 消息中心"留言互动"列表（带 peerUserId，我是发布人时对方是来认领的同学）
 *
 * 数据说明：已对接后端留言接口，页面停留期间每5秒轮询一次新消息
 */
const toast = require('../../utils/toast')
const itemApi = require('../../api/item')
const messageApi = require('../../api/message')
const { formatItem, formatRelativeTime, fullImageUrl } = require('../../utils/format')

Page({
  data: {
    item: null,          // 关联物品
    peer: null,          // 对方（昵称+头像）
    peerUserId: null,    // 对方用户ID
    myAvatar: '',        // 我的头像
    messages: [],        // 对话消息列表
    inputText: '',       // 输入框内容
    scrollIntoId: ''     // 滚动锚点，保持列表滚到底部
  },

  onLoad(options) {
    const app = getApp()
    if (!app.checkLogin()) {
      toast.info('请先登录')
      setTimeout(() => wx.navigateBack(), 1200)
      return
    }

    const userInfo = app.globalData.userInfo || {}
    this.myId = Number(userInfo.userId)
    // 消息中心进来会带 peerUserId，详情页进来不带（对方就是发布人）
    this.optionPeerId = options.peerUserId ? Number(options.peerUserId) : null
    this.sending = false      // 防止连点重复发送
    this.lastMsgId = 0        // 记住最后一条消息ID，轮询时判断有没有新消息

    this.setData({ myAvatar: userInfo.avatarUrl || '' })
    this.loadItem(options.itemId)
  },

  onShow() {
    // 从详情页返回或小程序切回前台时，补拉一次并恢复轮询
    if (this.data.item) {
      this.refreshMessages()
      this.startPolling()
    }
  },

  onHide() {
    // 页面看不见了就别浪费流量轮询了
    this.stopPolling()
  },

  onUnload() {
    this.stopPolling()
  },

  /** 从后端加载关联物品，确定对方是谁 */
  loadItem(id) {
    itemApi.selectItemByItemId(id).then(res => {
      const item = formatItem(res.data)
      if (!item) {
        toast.error('信息不存在')
        setTimeout(() => wx.navigateBack(), 1200)
        return
      }

      // 没带 peerUserId 就默认和发布人聊；带了就用带的
      const peerUserId = this.optionPeerId || Number(item.userId)
      // 对方是发布人的话，昵称头像直接用物品上的；不是的话等会话拉回来再补
      const peer = peerUserId === Number(item.userId)
        ? { name: item.publisher.name, avatar: item.publisher.avatar }
        : { name: '', avatar: '' }

      this.setData({ item, peerUserId, peer })
      if (peer.name) {
        wx.setNavigationBarTitle({ title: peer.name })
      }

      this.refreshMessages()
      this.startPolling()
    }).catch(() => {
      toast.error('信息不存在')
      setTimeout(() => wx.navigateBack(), 1200)
    })
  },

  /** 从后端拉取会话消息（打开会话时后端顺带把对方发来的标记已读） */
  refreshMessages() {
    if (!this.data.item) return

    messageApi.selectConversation(this.data.item.id, this.data.peerUserId).then(res => {
      const rows = res.data || []
      const messages = rows.map(m => ({
        id: m.messageId,
        from: Number(m.fromUserId) === this.myId ? 'me' : 'peer',
        text: m.content,
        time: formatRelativeTime(m.createTime)
      }))

      // 对方昵称头像还是空的话，从对方发的消息里补一份（我是发布人的场景）
      if (!this.data.peer.name) {
        const peerMsg = rows.find(m => Number(m.fromUserId) !== this.myId)
        if (peerMsg) {
          const peer = {
            name: peerMsg.fromUserName || '同学',
            avatar: fullImageUrl(peerMsg.fromUserAvatar)
          }
          this.setData({ peer })
          wx.setNavigationBarTitle({ title: peer.name })
        }
      }

      // 消息没变化就不重新渲染，轮询时避免列表闪烁
      const lastId = messages.length ? messages[messages.length - 1].id : 0
      if (messages.length === this.data.messages.length && lastId === this.lastMsgId) {
        return
      }
      this.lastMsgId = lastId

      this.setData({ messages }, () => {
        if (lastId) {
          this.setData({ scrollIntoId: 'msg-' + lastId })
        }
      })
    }).catch(() => {
      // 轮询拉失败就等下一轮，不打扰用户
    })
  },

  /** 每5秒问一次后端有没有新消息（简易轮询，代替推送） */
  startPolling() {
    this.stopPolling()
    this.pollTimer = setInterval(() => this.refreshMessages(), 5000)
  },

  /** 停止轮询 */
  stopPolling() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = null
    }
  },

  onInput(e) {
    this.setData({ inputText: e.detail.value })
  },

  /** 发送消息 */
  send() {
    const text = this.data.inputText.trim()
    if (!text || this.sending) return

    this.sending = true
    messageApi.sendMessage({
      itemId: this.data.item.id,
      toUserId: this.data.peerUserId,
      content: text
    }).then(() => {
      // 发成功再清空输入框，然后拉一次最新会话把自己这条显示出来
      this.setData({ inputText: '' })
      this.refreshMessages()
    }).catch(() => {
      // 发送失败输入内容留着，改改还能再发
    }).finally(() => {
      this.sending = false
    })
  },

  /** 查看/复制对方留下的联系方式 */
  showContact() {
    const { item } = this.data
    wx.showModal({
      title: item.type === 'found' ? '拾主联系方式' : '失主联系方式',
      content: item.contact,
      confirmText: '复制',
      cancelText: '关闭',
      success(res) {
        if (res.confirm) {
          wx.setClipboardData({ data: item.contact })
        }
      }
    })
  },

  /** 点击顶部物品卡 → 回到物品详情（上一页就是该详情时直接返回，避免页面栈叠加） */
  goDetail() {
    const pages = getCurrentPages()
    const prev = pages[pages.length - 2]
    if (prev && prev.route === 'pages/detail/index' &&
        prev.data.item && prev.data.item.id === this.data.item.id) {
      wx.navigateBack()
      return
    }
    wx.navigateTo({ url: '/pages/detail/index?id=' + this.data.item.id })
  }
})
