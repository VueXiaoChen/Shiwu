/**
 * 物品分类相关 API
 */
const { get } = require('../utils/api')

/**
 * 查询所有物品分类（不分页，首页分类条 / 发布页分类选择用）
 * @returns {Promise}
 */
function selectCategoryAll() {
  return get('/content/category/selectCategoryAll', {}, { isToken: false })
}

module.exports = {
  selectCategoryAll
}
