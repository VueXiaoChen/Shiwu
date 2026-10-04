/**
 * 失物/寻物信息相关 API
 */
const { get, post, put, del } = require('../utils/api')

/**
 * 分页查询物品列表（首页/搜索/详情相关推荐用）
 * @param {object} params - 查询参数（pageNum、pageSize、type、categoryId、status、urgent、keyword）
 * @returns {Promise}
 */
function selectItemList(params = {}) {
  return get('/item/item/selectItemList', params, { isToken: false })
}

/**
 * 查询我发布的物品列表（我的发布页用，需要登录）
 * @param {object} params - 查询参数（pageNum、pageSize、status）
 * @returns {Promise}
 */
function selectMyItemList(params = {}) {
  return get('/item/item/selectMyItemList', params)
}

/**
 * 首页统计数字（全部/招领/寻物的条数）
 * @returns {Promise}
 */
function selectItemStats() {
  return get('/item/item/selectItemStats', {}, { isToken: false })
}

/**
 * 根据物品ID查询物品详情
 * @param {number} itemId - 物品ID
 * @returns {Promise}
 */
function selectItemByItemId(itemId) {
  return get('/item/item/selectItemByItemId/' + itemId, {}, { isToken: false })
}

/**
 * 发布物品信息（需要登录）
 * 加载提示由发布页自己控制（要先传图再提交，全程一个loading）
 * @param {object} data - 物品表单数据
 * @returns {Promise}
 */
function insertItem(data) {
  return post('/item/item/insertItem', data)
}

/**
 * 标记物品为已完成（只能标记自己发布的）
 * @param {number} itemId - 物品ID
 * @returns {Promise}
 */
function finishItem(itemId) {
  return put('/item/item/finishItem/' + itemId, {}, { showLoading: true })
}

/**
 * 浏览量+1（详情页打开时悄悄调一下，失败也无所谓）
 * @param {number} itemId - 物品ID
 * @returns {Promise}
 */
function increaseViews(itemId) {
  return put('/item/item/increaseViews/' + itemId, {}, { isToken: false })
}

/**
 * 删除自己发布的物品
 * @param {number} itemId - 物品ID
 * @returns {Promise}
 */
function deleteMyItem(itemId) {
  return del('/item/item/deleteMyItem/' + itemId, {}, { showLoading: true })
}

module.exports = {
  selectItemList,
  selectMyItemList,
  selectItemStats,
  selectItemByItemId,
  insertItem,
  finishItem,
  increaseViews,
  deleteMyItem
}
