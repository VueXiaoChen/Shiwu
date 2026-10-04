/**
 * 搜索页
 *
 * 功能：
 *  - 关键词搜索（后端模糊匹配标题/描述/地点）
 *  - 搜索历史（本地存储，可清空）
 *  - 热门搜索词
 */
const mock = require('../../utils/mock')
const toast = require('../../utils/toast')
const itemApi = require('../../api/item')
const { formatItemList } = require('../../utils/format')

const HISTORY_KEY = 'searchHistory'
const HISTORY_MAX = 10

Page({
  data: {
    keyword: '',
    history: [],
    hotKeywords: mock.hotKeywords,
    // 是否已执行过搜索（区分"初始态"和"无结果态"）
    searched: false,
    resultList: []
  },

  onLoad() {
    this.setData({ history: wx.getStorageSync(HISTORY_KEY) || [] })
  },

  /** 输入框内容变化 */
  onInput(e) {
    const keyword = e.detail.value
    this.setData({ keyword })
    // 清空输入时回到初始态
    if (!keyword.trim()) {
      this.setData({ searched: false, resultList: [] })
    }
  },

  /** 键盘确认搜索 */
  onConfirm() {
    this.doSearch(this.data.keyword)
  },

  /** 点击历史/热门词 */
  onKeywordTap(e) {
    const kw = e.currentTarget.dataset.kw
    this.setData({ keyword: kw })
    this.doSearch(kw)
  },

  /** 执行搜索 */
  doSearch(keyword) {
    const kw = (keyword || '').trim()
    if (!kw) {
      toast.info('请输入搜索关键词')
      return
    }

    // 写入搜索历史（去重 + 最新在前 + 限制数量）
    let history = this.data.history.filter(h => h !== kw)
    history.unshift(kw)
    history = history.slice(0, HISTORY_MAX)
    wx.setStorageSync(HISTORY_KEY, history)
    this.setData({ history })

    // 调后端接口搜索，一个词同时匹配标题/描述/地点
    itemApi.selectItemList({ pageNum: 1, pageSize: 50, keyword: kw }).then(res => {
      this.setData({
        searched: true,
        resultList: formatItemList(res.rows)
      })
    }).catch(() => {
      this.setData({ searched: true, resultList: [] })
    })
  },

  /** 清空输入 */
  clearKeyword() {
    this.setData({ keyword: '', searched: false, resultList: [] })
  },

  /** 清空搜索历史 */
  clearHistory() {
    toast.confirm('确定清空搜索历史吗？').then(() => {
      wx.removeStorageSync(HISTORY_KEY)
      this.setData({ history: [] })
    }).catch(() => {})
  },

  /** 取消返回 */
  goBack() {
    wx.navigateBack()
  },

  /** 跳转详情 */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/detail/index?id=' + id })
  }
})
