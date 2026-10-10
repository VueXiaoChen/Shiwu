/**
 * 发布页 — 寻物启事 / 失物招领
 *
 * 功能：
 *  - 类型切换（寻物/招领）
 *  - 本地选图（最多3张，可删除）
 *  - 标题、描述、分类、地点、时间、联系方式表单
 *  - 寻物模式支持"急寻"开关和悬赏
 *  - 提交时先把图片逐张传给后端，再调发布接口入库
 */
const toast = require('../../utils/toast')
const categoryApi = require('../../api/category')
const itemApi = require('../../api/item')
const { upload } = require('../../utils/api')

const MAX_IMAGES = 3

Page({
  data: {
    type: 'lost',             // lost=寻物启事 found=失物招领
    categories: [],
    categoryIndex: -1,        // picker 选中的分类下标
    images: [],               // 本地临时图片路径
    maxImages: MAX_IMAGES,

    // 表单字段
    title: '',
    desc: '',
    location: '',
    happenTime: '',
    contact: '',
    urgent: false,
    reward: '',

    // 今天的日期（date picker 上限）
    today: ''
  },

  onLoad(options) {
    // 计算今天日期 yyyy-MM-dd
    const d = new Date()
    const today = d.getFullYear() + '-' +
      String(d.getMonth() + 1).padStart(2, '0') + '-' +
      String(d.getDate()).padStart(2, '0')

    this.setData({ today, happenTime: this.data.happenTime || today })
    // 加载物品分类（picker 选择用）
    this.loadCategories()
  },

  /** 从后端加载物品分类 */
  loadCategories() {
    categoryApi.selectCategoryAll().then(res => {
      const categories = (res.data || []).map(c => ({
        id: c.categoryId,
        name: c.categoryName
      }))

      // 分类加载完成后，默认选中第一个（用户尚未手动选择时）
      const categoryIndex = this.data.categoryIndex < 0 && categories.length
        ? 0
        : this.data.categoryIndex

      this.setData({ categories, categoryIndex })
    }).catch(() => {
      // 加载失败时分类列表留空，提交校验会提示重新选择
    })
  },

  onShow() {
    const app = getApp()

    // tabBar 页无法带 URL 参数，首页入口通过 globalData.publishType 指定类型
    if (app.globalData.publishType) {
      this.setData({ type: app.globalData.publishType === 'found' ? 'found' : 'lost' })
      app.globalData.publishType = ''
    }

    // 未登录不允许发布，引导去"我的"页登录
    if (!app.checkLogin()) {
      toast.info('请先登录后再发布')
      setTimeout(() => {
        wx.switchTab({ url: '/pages/mine/index' })
      }, 600)
    }
  },

  /** 切换类型 */
  onTypeChange(e) {
    this.setData({ type: e.currentTarget.dataset.type })
  },

  /** 选择图片 */
  chooseImage() {
    const remain = MAX_IMAGES - this.data.images.length
    if (remain <= 0) return

    wx.chooseMedia({
      count: remain,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const paths = res.tempFiles.map(f => f.tempFilePath)
        this.setData({ images: this.data.images.concat(paths) })
      }
    })
  },

  /** 删除图片 */
  removeImage(e) {
    const index = e.currentTarget.dataset.index
    const images = this.data.images.slice()
    images.splice(index, 1)
    this.setData({ images })
  },

  /** 预览图片 */
  previewImage(e) {
    wx.previewImage({
      current: e.currentTarget.dataset.src,
      urls: this.data.images
    })
  },

  // ========== 表单输入 ==========
  onTitleInput(e) {
    this.setData({ title: e.detail.value })
  },

  onDescInput(e) {
    this.setData({ desc: e.detail.value })
  },

  onLocationInput(e) {
    this.setData({ location: e.detail.value })
  },

  onContactInput(e) {
    this.setData({ contact: e.detail.value })
  },

  onRewardInput(e) {
    this.setData({ reward: e.detail.value })
  },

  onCategoryChange(e) {
    this.setData({ categoryIndex: Number(e.detail.value) })
  },

  onDateChange(e) {
    this.setData({ happenTime: e.detail.value })
  },

  toggleUrgent() {
    this.setData({ urgent: !this.data.urgent })
  },

  /** 提交发布 */
  submit() {
    const { type, title, desc, categoryIndex, categories, location, happenTime, contact, urgent, reward, images } = this.data

    // 逐项校验
    if (!title.trim()) return toast.info('请填写标题')
    if (categoryIndex < 0) return toast.info('请选择物品分类')
    if (!desc.trim()) return toast.info('请填写详细描述')
    if (!location.trim()) return toast.info('请填写地点')
    if (!contact.trim()) return toast.info('请填写联系方式')

    // 防止手快连点两次重复发布
    if (this.submitting) return
    this.submitting = true

    const category = categories[categoryIndex]

    wx.showLoading({ title: '发布中...', mask: true })

    // 第一步：把本地图片逐张传到服务器，换成服务器地址
    Promise.all(images.map(path => upload(path))).then(urls => {
      // 第二步：组装后端要的字段格式提交发布
      return itemApi.insertItem({
        type,
        title: title.trim(),
        description: desc.trim(),
        categoryId: category.id,
        images: urls.join(','),
        location: location.trim(),
        happenTime,
        contact: contact.trim(),
        urgent: type === 'lost' && urgent ? 1 : 0,
        reward: type === 'lost' ? reward.trim() : ''
      })
    }).then(() => {
      wx.hideLoading()
      this.submitting = false
      toast.success('发布成功')
      // tabBar 页没有返回栈：清空表单后回到首页查看新发布的信息
      setTimeout(() => {
        this.resetForm()
        wx.switchTab({ url: '/pages/index/index' })
      }, 1200)
    }).catch(() => {
      wx.hideLoading()
      this.submitting = false
      // 失败提示由请求封装统一弹出，这里只负责解锁按钮
    })
  },

  /** 发布成功后清空表单，方便下次进入重新填写 */
  resetForm() {
    this.setData({
      type: 'lost',
      // 分类已加载时默认选中第一个，否则保持未选中
      categoryIndex: this.data.categories.length ? 0 : -1,
      images: [],
      title: '',
      desc: '',
      location: '',
      happenTime: this.data.today,
      contact: '',
      urgent: false,
      reward: ''
    })
  }
})