import request from '@/utils/request'

//查询物品分类列表
export function selectCategoryList(query) {
  return request({
    url: '/content/category/selectCategoryList',
    method: 'get',
    params: query
  })
}

//查询所有物品分类(供下拉选择使用)
export function selectCategoryAll() {
  return request({
    url: '/content/category/selectCategoryAll',
    method: 'get'
  })
}

//根据分类ID查询物品分类信息
export function selectCategoryByCategoryId(categoryId) {
  return request({
    url: '/content/category/selectCategoryByCategoryId/' + categoryId,
    method: 'get'
  })
}

//新增物品分类
export function insertCategory(data) {
  return request({
    url: '/content/category/insertCategory',
    method: 'post',
    data: data
  })
}

//修改物品分类
export function updateCategory(data) {
  return request({
    url: '/content/category/updateCategory',
    method: 'put',
    data: data
  })
}

//删除物品分类
export function deleteCategoryByCategoryIds(categoryIds) {
  return request({
    url: '/content/category/deleteCategoryByCategoryIds/' + categoryIds,
    method: 'delete'
  })
}
