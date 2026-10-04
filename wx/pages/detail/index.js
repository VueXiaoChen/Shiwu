/**
 * 物品详情页
 *
 * 功能：
 *  - 图片轮播 + 当前页码
 *  - 物品完整信息（类型、状态、分类、地点、时间、酬谢）
 *  - 发布人信息卡
 *  - 相关推荐（同分类的其他物品）
 *  - 底部操作栏：查看联系方式/认领
 *
 * 数据说明：详情、相关推荐均已对接后端接口，打开时浏览量+1
 */
const toast = require('../../utils/toast')
const itemApi = require('../../api/item')
const { formatItem, formatItemList } = require('../../utils/format')

Page({
  data: {
    item: null,
    currentSwiper: 0,   // 当前轮播页码
    related: [],        // 相关推荐
    isMine: false       // 是不是我自己发布的（自己发的不能自己联系自己）
  },

  onLoad(options) {
    this.loadDetail(options.id)
  },

  /** 从后端加载物品详情 */
  loadDetail(id) {
    itemApi.selectItemByItemId(id).then(res => {
      const item = formatItem(res.data)
      if (!item) {
        toast.error('信息不存在')
        setTimeout(() => wx.navigateBack(), 1200)
        return
      }

      // 对比发布人ID和当前登录用户ID，是自己发的就不展示沟通入口
      const app = getApp()
      const myId = app.globalData.userInfo && app.globalData.userInfo.userId
      const isMine = app.checkLogin() && myId && Number(myId) === Number(item.userId)

      this.setData({ item, isMine })
      // 浏览量+1，悄悄调一下就行，失败也不影响看详情
      itemApi.increaseViews(id).catch(() => {})
      // 加载同分类的相关推荐
      this.loadRelated(item)
    }).catch(() => {
      toast.error('信息不存在')
      setTimeout(() => wx.navigateBack(), 1200)
    })
  },

  /** 加载相关推荐：同分类、进行中的其他物品，最多3条 */
  loadRelated(item) {
    itemApi.selectItemList({
      pageNum: 1,
      pageSize: 4,
      categoryId: item.categoryId,
      status: 'open'
    }).then(res => {
      // 把自己这条去掉，最多留3条
      const related = formatItemList(res.rows)
        .filter(it => it.id !== item.id)
        .slice(0, 3)
      this.setData({ related })
    }).catch(() => {
      // 推荐加载失败就不展示，不影响详情
    })
  },

  /** 轮播切换 */
  onSwiperChange(e) {
    this.setData({ currentSwiper: e.detail.current })
  },

  /** 预览大图 */
  previewImage(e) {
    const current = e.currentTarget.dataset.src
    wx.previewImage({
      current,
      urls: this.data.item.images
    })
  },

  /** 联系认领/联系失主 → 进入对话页（未登录先去登录） */
  goChat() {
    const app = getApp()
    if (!app.checkLogin()) {
      toast.info('请先登录后联系')
      setTimeout(() => {
        wx.switchTab({ url: '/pages/mine/index' })
      }, 600)
      return
    }

    // 自己发的信息没必要自己跟自己聊，拦一道兜底
    if (this.data.isMine) {
      toast.info('这是你自己发布的信息')
      return
    }

    wx.navigateTo({ url: '/pages/chat/index?itemId=' + this.data.item.id })
  },

  /** 跳转相关推荐 */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.redirectTo({ url: '/pages/detail/index?id=' + id })
  }
})
