/**
 * 首页 — 校园失物招领
 *
 * 设计：沉浸式纯色品牌头（自定义导航栏）+ 双发布入口卡
 *       + 分类横滑条 + 下划线Tab + 双列瀑布卡片
 *
 * 数据说明：公告、分类、物品列表、统计数字均已对接后端接口
 */
const toast = require('../../utils/toast')
const noticeApi = require('../../api/notice')
const categoryApi = require('../../api/category')
const itemApi = require('../../api/item')
const { formatItemList } = require('../../utils/format')

Page({
  data: {
    // 状态栏高度（自定义导航栏用）
    statusBarHeight: 20,

    notices: [],
    categories: [],

    // 筛选条件
    activeTab: 'all',        // all=全部 found=失物招领 lost=寻物启事
    activeCategoryId: 0,     // 0=全部分类
    onlyUrgent: false,       // 只看急寻

    // 双列瀑布流数据（左右两列）
    leftList: [],
    rightList: [],
    itemCount: 0,

    // 统计数字
    stats: { total: 0, found: 0, lost: 0 }
  },

  onLoad() {
    // 获取状态栏高度，撑起自定义导航栏
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.setData({ statusBarHeight: windowInfo.statusBarHeight || 20 })
    // 加载公告（只需加载一次）
    this.loadNotices()
    // 加载物品分类
    this.loadCategories()
  },

  onShow() {
    // 每次回到首页都重新拉一遍（发布页可能新增了数据）
    this.refreshList()
    this.loadStats()
  },

  /** 下拉刷新 */
  onPullDownRefresh() {
    this.loadNotices()
    this.loadCategories()
    this.loadStats()
    this.refreshList()
    wx.stopPullDownRefresh()
    toast.info('已刷新')
  },

  /** 从后端加载公告列表（取最新5条滚动展示） */
  loadNotices() {
    noticeApi.selectNoticeList({ pageNum: 1, pageSize: 5 }).then(res => {
      const notices = (res.rows || []).map(n => ({
        id: n.noticeId,
        title: n.title
      }))
      this.setData({ notices })
    }).catch(() => {
      // 加载失败时公告条留空，不阻塞首页其他内容
    })
  },

  /** 从后端加载物品分类（分类横滑条筛选用） */
  loadCategories() {
    const app = getApp()
    categoryApi.selectCategoryAll().then(res => {
      const categories = (res.data || []).map(c => ({
        id: c.categoryId,
        name: c.categoryName,
        // 相对路径图标需拼接 baseUrl 才能渲染
        icon: c.icon
          ? (/^https?:\/\//.test(c.icon) ? c.icon : app.globalData.baseUrl + c.icon)
          : ''
      }))
      this.setData({ categories })
    }).catch(() => {
      // 加载失败时分类条留空，不阻塞首页其他内容
    })
  },

  /** 从后端加载统计数字（全部/招领/寻物条数） */
  loadStats() {
    itemApi.selectItemStats().then(res => {
      this.setData({
        stats: {
          total: res.total || 0,
          found: res.found || 0,
          lost: res.lost || 0
        }
      })
    }).catch(() => {
      // 统计加载失败就保持原样，不影响列表
    })
  },

  /** 根据当前筛选条件从后端拉列表（拆成左右两列） */
  refreshList() {
    const { activeTab, activeCategoryId, onlyUrgent } = this.data

    // 组装查询参数，没选的条件就不传
    const params = { pageNum: 1, pageSize: 50 }
    if (activeTab !== 'all') params.type = activeTab
    if (activeCategoryId !== 0) params.categoryId = activeCategoryId
    if (onlyUrgent) params.urgent = 1

    itemApi.selectItemList(params).then(res => {
      const list = formatItemList(res.rows)

      // 按下标奇偶拆列，模拟瀑布流
      const leftList = []
      const rightList = []
      list.forEach((it, i) => {
        (i % 2 === 0 ? leftList : rightList).push(it)
      })

      this.setData({
        leftList,
        rightList,
        itemCount: list.length
      })
    }).catch(() => {
      // 加载失败时列表清空，展示空状态
      this.setData({ leftList: [], rightList: [], itemCount: 0 })
    })
  },

  /** 点击搜索栏 → 搜索页 */
  goSearch() {
    wx.navigateTo({ url: '/pages/search/index' })
  },

  /** 点击 AI 找物入口 → AI 找物助手页（tabBar 页需用 switchTab） */
  goAI() {
    wx.switchTab({ url: '/pages/ai/index' })
  },

  /** 点击公告条 → 公告详情页 */
  goNotice(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/notice/index?id=' + id })
  },

  /** 切换 Tab */
  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab }, () => this.refreshList())
  },

  /** 点击分类（再点一次取消筛选） */
  onCategoryTap(e) {
    const id = Number(e.currentTarget.dataset.id)
    const next = this.data.activeCategoryId === id ? 0 : id
    this.setData({ activeCategoryId: next }, () => this.refreshList())
  },

  /** 切换"只看急寻" */
  toggleUrgent() {
    this.setData({ onlyUrgent: !this.data.onlyUrgent }, () => this.refreshList())
  },

  /** 点击物品卡片 → 详情页 */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/detail/index?id=' + id })
  },

  /** 发布入口（未登录先去"我的"页登录），data-type 指定寻物/招领 */
  goPublish(e) {
    const app = getApp()
    if (!app.checkLogin()) {
      toast.info('请先登录后再发布')
      setTimeout(() => {
        wx.switchTab({ url: '/pages/mine/index' })
      }, 600)
      return
    }
    // 发布页已升级为 tabBar 页，switchTab 无法带参，通过 globalData 传递类型
    app.globalData.publishType = (e && e.currentTarget.dataset.type) || 'lost'
    wx.switchTab({ url: '/pages/publish/index' })
  }
})
