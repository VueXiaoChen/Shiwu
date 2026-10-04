/**
 * 公告详情页
 *
 * 数据来源：后端 /content/notice/selectNoticeByNoticeId/{id} 接口
 * 正文为富文本 HTML，使用 rich-text 组件渲染
 */
const noticeApi = require('../../api/notice')
const toast = require('../../utils/toast')

Page({
  data: {
    notice: null       // 公告对象 { title, time, content }
  },

  onLoad(options) {
    this.loadNotice(options.id)
  },

  /** 从后端加载公告详情 */
  loadNotice(noticeId) {
    if (!noticeId) {
      toast.info('公告不存在')
      setTimeout(() => wx.navigateBack(), 800)
      return
    }
    noticeApi.selectNoticeByNoticeId(noticeId).then(res => {
      const data = res.data
      if (!data) {
        toast.info('公告不存在')
        setTimeout(() => wx.navigateBack(), 800)
        return
      }
      this.setData({
        notice: {
          title: data.title,
          // createTime 格式 yyyy-MM-dd HH:mm:ss，只取日期部分展示
          time: (data.createTime || '').split(' ')[0],
          // 富文本内相对路径图片拼接 baseUrl，保证能正常显示
          content: this.resolveImageUrl(data.content || '')
        }
      })
    }).catch(() => {
      setTimeout(() => wx.navigateBack(), 800)
    })
  },

  /** 富文本 HTML 中 /profile 开头的图片地址拼接服务器域名 */
  resolveImageUrl(html) {
    const baseUrl = getApp().globalData.baseUrl
    return html
      .replace(/src="\/profile/g, 'src="' + baseUrl + '/profile')
      // 限制富文本图片宽度不超出屏幕
      .replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;"')
  }
})
