/**
 * 我的发布页面
 *
 * 展示当前用户发布的寻物启事 / 失物招领信息，
 * 支持按状态筛选（全部 / 进行中 / 已完成），
 * 支持标记完成（已找回 / 已归还）和删除。
 *
 * 数据说明：已对接后端接口，按当前登录用户查询
 */
const toast = require('../../utils/toast')
const itemApi = require('../../api/item')
const { formatItemList } = require('../../utils/format')

Page({
  data: {
    // 状态筛选：all=全部 open=进行中 done=已完成
    activeStatus: 'all',
    // 各状态数量（Tab 上的角标）
    counts: { all: 0, open: 0, done: 0 },
    // 当前展示的列表
    list: []
  },

  onShow() {
    // 没登录就看不了自己的发布，引导回"我的"页登录
    const app = getApp()
    if (!app.checkLogin()) {
      toast.info('请先登录')
      setTimeout(() => {
        wx.switchTab({ url: '/pages/mine/index' })
      }, 600)
      return
    }
    this.refreshList()
  },

  /** 从后端拉取我发布的全部信息，再按筛选条件展示 */
  refreshList() {
    itemApi.selectMyItemList({ pageNum: 1, pageSize: 100 }).then(res => {
      const myItems = formatItemList(res.rows)
      const { activeStatus } = this.data

      const list = activeStatus === 'all'
        ? myItems
        : myItems.filter(it => it.status === activeStatus)

      this.setData({
        list,
        counts: {
          all: myItems.length,
          open: myItems.filter(it => it.status === 'open').length,
          done: myItems.filter(it => it.status === 'done').length
        }
      })
    }).catch(() => {
      // 加载失败时展示空状态
      this.setData({ list: [], counts: { all: 0, open: 0, done: 0 } })
    })
  },

  /** 切换状态筛选 */
  onStatusChange(e) {
    const status = e.currentTarget.dataset.status
    this.setData({ activeStatus: status }, () => this.refreshList())
  },

  /** 点击卡片 → 详情页 */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/detail/index?id=' + id })
  },

  /** 去发布（空状态引导按钮） */
  goPublish() {
    wx.switchTab({ url: '/pages/publish/index' })
  },

  /**
   * 标记完成：寻物 → 已找回，招领 → 已归还
   * catchtap 阻止冒泡，避免触发卡片跳详情
   */
  onFinishTap(e) {
    const { id, type } = e.currentTarget.dataset
    const doneText = type === 'found' ? '已归还失主' : '已找回物品'

    toast.confirm(`确认标记为「${doneText}」吗？`, '提示').then(() => {
      itemApi.finishItem(id).then(() => {
        this.refreshList()
        toast.success('已标记完成')
      }).catch(() => {})
    }).catch(() => {})
  },

  /** 删除发布 */
  onDeleteTap(e) {
    const id = e.currentTarget.dataset.id

    toast.confirm('删除后无法恢复，确定删除这条发布吗？', '提示').then(() => {
      itemApi.deleteMyItem(id).then(() => {
        this.refreshList()
        toast.success('已删除')
      }).catch(() => {})
    }).catch(() => {})
  }
})
