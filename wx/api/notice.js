/**
 * 公告相关 API
 */
const { get } = require('../utils/api')

/**
 * 分页查询公告列表
 * @param {object} params - 查询参数（pageNum、pageSize、title）
 * @returns {Promise}
 */
function selectNoticeList(params = {}) {
  return get('/content/notice/selectNoticeList', params, { isToken: false })
}

/**
 * 根据公告ID查询公告详情
 * @param {number} noticeId - 公告ID
 * @returns {Promise}
 */
function selectNoticeByNoticeId(noticeId) {
  return get('/content/notice/selectNoticeByNoticeId/' + noticeId, {}, { isToken: false })
}

module.exports = {
  selectNoticeList,
  selectNoticeByNoticeId
}
