import request from '@/utils/request'

//查询物品列表
export function selectItemList(query) {
  return request({
    url: '/item/item/selectItemList',
    method: 'get',
    params: query
  })
}

//根据物品ID查询物品信息
export function selectItemByItemId(itemId) {
  return request({
    url: '/item/item/selectItemByItemId/' + itemId,
    method: 'get'
  })
}

//修改物品信息
export function updateItem(data) {
  return request({
    url: '/item/item/updateItem',
    method: 'put',
    data: data
  })
}

//删除物品信息
export function deleteItemByItemIds(itemIds) {
  return request({
    url: '/item/item/deleteItemByItemIds/' + itemIds,
    method: 'delete'
  })
}
