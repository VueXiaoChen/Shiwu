/**
 * AI 找物助手页 — 用自然语言描述失物，AI Agent 匹配平台内相关信息
 * 新增：支持上传图片进行以图搜物
 *
 * 入口：首页搜索栏旁"AI 找物"按钮
 *
 * 数据说明：调用后端 /ai/chat 流式接口（DeepSeek 大模型 Agent），
 * 回复通过 SSE 逐段推过来做打字机效果，AI 会自己决定查库、
 * 代发布、标记完成等操作，匹配到的物品随 done 事件带回来渲染卡片
 */
const { aiChatStream, aiChatStreamWithImage } = require('../../api/ai')
const { formatItemList } = require('../../utils/format')
const toast = require('../../utils/toast')

// 欢迎区快捷提问
const quickQuestions = [
  '我想寻找一个视频',
  '该视频我找到了',
  '帮我发布一条寻视频启事'
]

Page({
  data: {
    quickQuestions,
    messages: [],        // 对话消息：{ id, from: 'ai'|'me', text, imageUrl, items, action }
    inputText: '',       // 输入框内容
    selectedImage: '',   // 已选图片临时路径
    thinking: false,     // AI 思考中（显示打字动画、禁止重复发送）
    scrollIntoId: ''     // 滚动锚点，保持列表滚到底部
  },

  // 当前进行中的流式请求，页面卸载时要掐掉
  _chatTask: null,

  onLoad() {
    // 开场欢迎消息
    this.appendMessage({
      from: 'ai',
      text: '你好，我是 AI 找视频图助手～\n想要寻找视频都可以直接告诉我，也可以上传图片让我帮你找，我会帮你在平台里智能匹配相关信息，还可以帮你直接发布寻视启事或视频图招领。',
      items: [],
      action: null
    })
  },

  onUnload() {
    // 页面退了就别让流继续跑
    if (this._chatTask) {
      this._chatTask.abort()
      this._chatTask = null
    }
  },

  /** 追加一条消息并滚动到底部 */
  appendMessage(msg) {
    const messages = this.data.messages
    const id = messages.length + 1
    messages.push({ id, ...msg })
    this.setData({ messages }, () => {
      this.setData({ scrollIntoId: 'msg-' + id })
    })
  },

  onInput(e) {
    this.setData({ inputText: e.detail.value })
  },

  /** 点击快捷提问 */
  onQuickTap(e) {
    this.sendText(e.currentTarget.dataset.text)
  },

  /** ========== 图片上传相关 ========== */

  /** 选择图片 */
  chooseImage() {
    if (this.data.thinking) return
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      sizeType: ['compressed'],
      success: (res) => {
        const file = res.tempFiles[0]
        // 限制图片大小，比如 5MB
        if (file.size > 5 * 1024 * 1024) {
          toast.info('图片不能超过 5MB')
          return
        }
        this.setData({ selectedImage: file.tempFilePath })
      }
    })
  },

  /** 移除已选图片 */
  removeSelectedImage() {
    this.setData({ selectedImage: '' })
  },

  /** 预览已选图片 */
  previewSelectedImage() {
    if (!this.data.selectedImage) return
    wx.previewImage({ urls: [this.data.selectedImage] })
  },

  /** 预览消息中的图片 */
  previewImage(e) {
    const url = e.currentTarget.dataset.url
    if (!url) return
    wx.previewImage({ urls: [url] })
  },

  /** ========== 发送消息 ========== */

  /** 点击发送按钮 */
  send() {
    const { inputText, selectedImage } = this.data
    this.sendMessage(inputText, selectedImage)
  },

  /** 只发文字的快捷入口（快捷提问复用） */
  sendText(text) {
    this.sendMessage(text, '')
  },

  /**
   * 取最近10条对话作为上下文传给后端，AI 才能接住多轮追问
   * （只带文本，卡片这些结构化内容模型不需要）
   */
  buildHistory() {
    return this.data.messages
      .filter(m => m.text)
      .slice(-10)
      .map(m => ({
        role: m.from === 'me' ? 'user' : 'assistant',
        content: m.text
      }))
  },

  /**
   * 统一发送：文字 + 可选图片
   * @param {string} text 文本内容
   * @param {string} imagePath 本地图片临时路径（可选）
   */
  sendMessage(text, imagePath) {
    text = (text || '').trim()
    if ((!text && !imagePath) || this.data.thinking) return

    // 先把上下文攒好（不含本条），再上屏用户消息
    const history = this.buildHistory()
    const displayText = text || '[图片]'

    this.appendMessage({
      from: 'me',
      text: displayText,
      imageUrl: imagePath || '',
      items: [],
      action: null
    })

    this.setData({
      inputText: '',
      selectedImage: '',
      thinking: true,
      scrollIntoId: 'thinking'
    })

    // 本轮 AI 回复消息在列表里的下标（首个 delta 到达时创建）
    let aiIndex = -1

    // 根据是否有图片选择不同的请求方式
    const requestFn = imagePath ? aiChatStreamWithImage : aiChatStream

    this._chatTask = requestFn({
      question: text || '请帮我找找这个物品',
      imagePath: imagePath || '',
      history,
      onDelta: (content) => {
        if (aiIndex === -1) {
          // 首段回复：关掉思考动画，挂一条空的 AI 消息开始打字
          this.setData({ thinking: false })
          this.appendMessage({ from: 'ai', text: content, items: [], action: null })
          aiIndex = this.data.messages.length - 1
        } else {
          // 后续增量追加到同一条消息上，打字机效果
          const key = `messages[${aiIndex}].text`
          this.setData({
            [key]: this.data.messages[aiIndex].text + content,
            scrollIntoId: 'msg-' + this.data.messages[aiIndex].id
          })
        }
      },
      onDone: ({ items, action }) => {
        this._chatTask = null
        // 后端偶尔一个字都没吐就结束（理论上不会），兜一条空消息
        if (aiIndex === -1) {
          this.setData({ thinking: false })
          this.appendMessage({ from: 'ai', text: '我在的，你说～', items: [], action: null })
          aiIndex = this.data.messages.length - 1
        }
        // 匹配物品转成卡片格式（补图片地址、相对时间），连同引导按钮一起挂上
        this.setData({
          [`messages[${aiIndex}].items`]: formatItemList(items),
          [`messages[${aiIndex}].action`]: action,
          scrollIntoId: 'msg-' + this.data.messages[aiIndex].id
        })
      },
      onError: (msg) => {
        this._chatTask = null
        this.setData({ thinking: false })
        if (aiIndex === -1) {
          this.appendMessage({ from: 'ai', text: msg, items: [], action: null })
        } else {
          // 说到一半断了，在后面补个提示
          const key = `messages[${aiIndex}].text`
          this.setData({ [key]: this.data.messages[aiIndex].text + '\n(' + msg + ')' })
        }
      }
    })
  },

  /** 点击匹配物品卡 → 详情页 */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/detail/index?id=' + id })
  },

  /** 点击引导发布按钮 → 发布页（与首页发布入口逻辑一致） */
  goPublish(e) {
    const app = getApp()
    if (!app.checkLogin()) {
      toast.info('请先登录后再发布')
      setTimeout(() => {
        wx.switchTab({ url: '/pages/mine/index' })
      }, 600)
      return
    }
    app.globalData.publishType = e.currentTarget.dataset.type || 'lost'
    wx.switchTab({ url: '/pages/publish/index' })
  }
})