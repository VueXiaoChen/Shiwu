import request from '@/utils/request'

//查询消息列表
export function selectMessageList(query) {
  return request({
    url: '/message/message/selectMessageList',
    method: 'get',
    params: query
  })
}

//查询全站会话列表(管理端对话记录左侧栏)
export function selectConversationList(query) {
  return request({
    url: '/message/message/selectConversationList',
    method: 'get',
    params: query
  })
}

//查询某个会话的完整对话记录(管理端对话记录右侧栏)
export function selectConversationDetail(query) {
  return request({
    url: '/message/message/selectConversationDetail',
    method: 'get',
    params: query
  })
}

//根据消息ID查询消息信息
export function selectMessageByMessageId(messageId) {
  return request({
    url: '/message/message/selectMessageByMessageId/' + messageId,
    method: 'get'
  })
}

//删除消息
export function deleteMessageByMessageIds(messageIds) {
  return request({
    url: '/message/message/deleteMessageByMessageIds/' + messageIds,
    method: 'delete'
  })
}
