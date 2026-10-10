/**
 * 物品数据格式化工具
 *
 * 后端返回的字段和页面用的字段长得不太一样，
 * 这里统一做一次"翻译"，页面拿到就能直接渲染：
 *  - itemId → id
 *  - description → desc
 *  - images 逗号串 → 数组（相对路径补上 baseUrl）
 *  - urgent 0/1 → 布尔值
 *  - createTime → publishTime（"10分钟前"这种相对时间）
 *  - userName/userAvatar → publisher 对象
 */

/**
 * 相对路径的图片补上后端地址，http 开头的直接用
 * @param {string} url - 图片地址
 * @returns {string}
 */
function fullImageUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  const app = getApp()
  return app.globalData.baseUrl + url
}

/**
 * 把精确时间变成"10分钟前"这种人话
 * @param {string} timeStr - 后端时间字符串（yyyy-MM-dd HH:mm:ss）
 * @returns {string}
 */
function formatRelativeTime(timeStr) {
  if (!timeStr) return ''
  // iOS 不认识"2026-07-30 09:20:00"这种横线格式，换成斜杠才能解析
  const time = new Date(timeStr.replace(/-/g, '/')).getTime()
  if (isNaN(time)) return timeStr

  const diff = Date.now() - time
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return Math.floor(diff / minute) + '分钟前'
  if (diff < day) return Math.floor(diff / hour) + '小时前'
  if (diff < 2 * day) return '昨天'
  if (diff < 7 * day) return Math.floor(diff / day) + '天前'
  // 超过一周就直接显示日期
  return timeStr.slice(0, 10)
}

/**
 * 把后端返回的一条物品数据转成页面渲染用的格式
 * @param {object} item - 后端返回的物品对象
 * @returns {object} 页面直接能用的物品对象
 */
function formatItem(item) {
  if (!item) return null

  // 图片是逗号分隔的一串，拆成数组再逐个补全地址
  const images = (item.images || '')
    .split(',')
    .filter(url => url.trim())
    .map(url => fullImageUrl(url.trim()))

  return {
    id: item.itemId,
    type: item.type,
    title: item.title,
    desc: item.description,
    categoryId: item.categoryId,
    categoryName: item.categoryName || '',
    images,
    location: item.location,
    happenTime: item.happenTime || '',
    publishTime: formatRelativeTime(item.createTime),
    status: item.status,
    urgent: item.urgent === 1,
    reward: item.reward || '',
    views: item.views || 0,
    contact: item.contact,
    // 发布人的用户ID（详情页判断"是不是我自己发的"要用）
    userId: item.userId,
    publisher: {
      name: item.userName || '同学',
      avatar: fullImageUrl(item.userAvatar)
    }
  }
}

/**
 * 批量转换物品列表
 * @param {Array} list - 后端返回的物品数组
 * @returns {Array}
 */
function formatItemList(list) {
  return (list || []).map(item => formatItem(item))
}

module.exports = {
  fullImageUrl,
  formatRelativeTime,
  formatItem,
  formatItemList
}