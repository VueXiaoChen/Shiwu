import request from '@/utils/request'

//查询公告列表
export function selectNoticeList(query) {
  return request({
    url: '/content/notice/selectNoticeList',
    method: 'get',
    params: query
  })
}

//根据公告ID查询公告信息
export function selectNoticeByNoticeId(noticeId) {
  return request({
    url: '/content/notice/selectNoticeByNoticeId/' + noticeId,
    method: 'get'
  })
}

//新增公告
export function insertNotice(data) {
  return request({
    url: '/content/notice/insertNotice',
    method: 'post',
    data: data
  })
}

//修改公告
export function updateNotice(data) {
  return request({
    url: '/content/notice/updateNotice',
    method: 'put',
    data: data
  })
}

//删除公告
export function deleteNoticeByNoticeIds(noticeIds) {
  return request({
    url: '/content/notice/deleteNoticeByNoticeIds/' + noticeIds,
    method: 'delete'
  })
}
