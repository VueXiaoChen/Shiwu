/**
 * 用户留言消息相关 API
 */
const { get, post } = require('../utils/api')

/**
 * 我的会话列表（消息中心用）
 * 每个"物品+对方"组合返回最新一条留言，附带未读数
 * @returns {Promise}
 */
function selectMyConversationList() {
  return get('/message/message/selectMyConversationList')
}

/**
 * 查询我和对方围绕某件物品的完整对话（对话页用）
 * 打开会话时后端会顺带把对方发给我的未读消息标记为已读
 * @param {number} itemId - 关联物品ID
 * @param {number} peerUserId - 对方用户ID
 * @returns {Promise}
 */
function selectConversation(itemId, peerUserId) {
  return get('/message/message/selectConversation', { itemId, peerUserId })
}

/**
 * 发送一条留言
 * @param {object} data - { itemId, toUserId, content }
 * @returns {Promise}
 */
function sendMessage(data) {
  return post('/message/message/sendMessage', data)
}

module.exports = {
  selectMyConversationList,
  selectConversation,
  sendMessage
}
