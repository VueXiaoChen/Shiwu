/**
 * 假数据源（开发阶段用，后端接口完成后替换）
 *
 * 说明：
 * - items 是模块级数组，发布页新增的数据会 unshift 进来，
 *   首页 onShow 时重新读取，就能看到"发布成功"的效果
 * - 图片用 picsum.photos 占位图（开发者工具需勾选"不校验合法域名"）
 */

// ========== 首页轮播 ==========
const banners = [
  {
    id: 1,
    image: 'https://picsum.photos/seed/campus1/750/340',
    title: '拾金不昧 传递温暖',
    sub: '捡到物品请及时发布招领信息'
  },
  {
    id: 2,
    image: 'https://picsum.photos/seed/campus2/750/340',
    title: '失物招领服务中心',
    sub: '贵重物品可移交学生服务中心保管'
  },
  {
    id: 3,
    image: 'https://picsum.photos/seed/campus3/750/340',
    title: '文明校园 你我共建',
    sub: '已帮助 1200+ 位同学找回失物'
  }
]

// ========== 热门搜索词 ==========
const hotKeywords = ['校园卡', 'AirPods', '钥匙', '保温杯', '身份证', '雨伞', '充电宝', '课本']

// ========== 物品数据 ==========
// type: lost=寻物启事(我丢了东西)  found=失物招领(我捡到东西)
// status: open=进行中  done=已完成
const items = [
  {
    id: 1,
    type: 'found',
    title: 'AirPods Pro 耳机（白色充电盒）',
    desc: '在图书馆三楼自习区靠窗座位捡到，盒子上有轻微划痕，可描述特征认领。',
    categoryId: 1,
    categoryName: '电子产品',
    images: ['https://picsum.photos/seed/airpods/700/500', 'https://picsum.photos/seed/airpods2/700/500'],
    location: '图书馆三楼自习区',
    happenTime: '2026-07-28',
    publishTime: '10分钟前',
    status: 'open',
    urgent: false,
    reward: '',
    publisher: { name: '晨曦', avatar: 'https://picsum.photos/seed/u1/100/100', campus: '东校区' },
    views: 56,
    contact: '微信：chenxi_2026'
  },
  {
    id: 2,
    type: 'lost',
    title: '校园一卡通（张同学）',
    desc: '中午在第二食堂二楼吃饭时丢失，卡套是蓝色的，里面还有一张照片，很着急！',
    categoryId: 2,
    categoryName: '证件卡片',
    images: ['https://picsum.photos/seed/card/700/500'],
    location: '第二食堂二楼',
    happenTime: '2026-07-29',
    publishTime: '32分钟前',
    status: 'open',
    urgent: true,
    reward: '',
    publisher: { name: '小张同学', avatar: 'https://picsum.photos/seed/u2/100/100', campus: '本部' },
    views: 89,
    contact: '电话：138****6621'
  },
  {
    id: 3,
    type: 'found',
    title: '黑色短款钱包',
    desc: '篮球场边长椅上捡到一个黑色钱包，内有现金和几张卡，请失主描述内部物品认领。',
    categoryId: 7,
    categoryName: '生活用品',
    images: ['https://picsum.photos/seed/wallet/700/500'],
    location: '东区篮球场',
    happenTime: '2026-07-29',
    publishTime: '1小时前',
    status: 'open',
    urgent: false,
    reward: '',
    publisher: { name: '阿泽', avatar: 'https://picsum.photos/seed/u3/100/100', campus: '东校区' },
    views: 134,
    contact: 'QQ：81234567'
  },
  {
    id: 4,
    type: 'lost',
    title: '蓝色保温杯（500ml）',
    desc: '上午上课落在教学楼B201教室，杯身有一圈白色条纹，杯底贴了姓名贴。',
    categoryId: 7,
    categoryName: '生活用品',
    images: ['https://picsum.photos/seed/cup/700/500'],
    location: '教学楼 B201',
    happenTime: '2026-07-29',
    publishTime: '2小时前',
    status: 'open',
    urgent: false,
    reward: '',
    publisher: { name: 'Momo', avatar: 'https://picsum.photos/seed/u4/100/100', campus: '南校区' },
    views: 47,
    contact: '微信：momo_cup'
  },
  {
    id: 5,
    type: 'found',
    title: '一串钥匙（带小熊挂件）',
    desc: '晚上跑步时在操场东侧跑道捡到，共4把钥匙，挂着一只棕色小熊玩偶。',
    categoryId: 3,
    categoryName: '钥匙门卡',
    images: ['https://picsum.photos/seed/keys/700/500'],
    location: '操场东侧跑道',
    happenTime: '2026-07-28',
    publishTime: '5小时前',
    status: 'open',
    urgent: false,
    reward: '',
    publisher: { name: '夜跑侠', avatar: 'https://picsum.photos/seed/u5/100/100', campus: '本部' },
    views: 73,
    contact: '微信：runner_night'
  },
  {
    id: 6,
    type: 'lost',
    title: 'iPad（灰色保护套）+ Apple Pencil',
    desc: '在自习室复习时短暂离开，回来发现 iPad 不见了，里面有重要的复习资料，急寻！',
    categoryId: 1,
    categoryName: '电子产品',
    images: ['https://picsum.photos/seed/ipad/700/500', 'https://picsum.photos/seed/ipad2/700/500'],
    location: '第四教学楼自习室',
    happenTime: '2026-07-28',
    publishTime: '昨天 21:40',
    status: 'open',
    urgent: true,
    reward: '50元',
    publisher: { name: '柚子', avatar: 'https://picsum.photos/seed/u6/100/100', campus: '本部' },
    views: 312,
    contact: '电话：150****3308'
  },
  {
    id: 7,
    type: 'found',
    title: '高等数学课本（下册）',
    desc: '3教101捡到一本高数下册，扉页写了名字但看不清，书里夹着不少笔记。',
    categoryId: 4,
    categoryName: '书籍资料',
    images: ['https://picsum.photos/seed/book/700/500'],
    location: '第三教学楼 101',
    happenTime: '2026-07-27',
    publishTime: '昨天 15:20',
    status: 'open',
    urgent: false,
    reward: '',
    publisher: { name: '学委', avatar: 'https://picsum.photos/seed/u7/100/100', campus: '西校区' },
    views: 28,
    contact: 'QQ：66889900'
  },
  {
    id: 8,
    type: 'lost',
    title: '白色无线耳机充电盒',
    desc: '只丢了充电盒，耳机还在……大概率掉在宿舍到食堂的路上，求好心人捡到联系。',
    categoryId: 1,
    categoryName: '电子产品',
    images: ['https://picsum.photos/seed/earbud/700/500'],
    location: '梅园宿舍—一食堂沿路',
    happenTime: '2026-07-27',
    publishTime: '昨天 12:05',
    status: 'open',
    urgent: false,
    reward: '',
    publisher: { name: '大熊', avatar: 'https://picsum.photos/seed/u8/100/100', campus: '本部' },
    views: 61,
    contact: '微信：bigbear_66'
  },
  {
    id: 9,
    type: 'found',
    title: '身份证（李**）',
    desc: '快递站门口捡到一张身份证，姓李，请失主携带能证明身份的材料来认领。',
    categoryId: 2,
    categoryName: '证件卡片',
    images: ['https://picsum.photos/seed/idcard/700/500'],
    location: '菜鸟驿站门口',
    happenTime: '2026-07-27',
    publishTime: '2天前',
    status: 'open',
    urgent: false,
    reward: '',
    publisher: { name: '驿站小哥', avatar: 'https://picsum.photos/seed/u9/100/100', campus: '南校区' },
    views: 205,
    contact: '驿站前台直接认领',
  },
  {
    id: 10,
    type: 'lost',
    title: '黑色羽绒服上的校牌',
    desc: '校牌从羽绒服上掉了，上面刻着学号，对我很有纪念意义，捡到必有感谢！',
    categoryId: 5,
    categoryName: '衣物饰品',
    images: ['https://picsum.photos/seed/badge/700/500'],
    location: '大学生活动中心',
    happenTime: '2026-07-26',
    publishTime: '2天前',
    status: 'open',
    urgent: false,
    reward: '奶茶一杯',
    publisher: { name: '栗子', avatar: 'https://picsum.photos/seed/u10/100/100', campus: '东校区' },
    views: 44,
    contact: '微信：lizi_2333'
  },
  {
    id: 11,
    type: 'found',
    title: '羽毛球拍（尤尼克斯）',
    desc: '体育馆羽毛球场3号场地捡到球拍一支，拍柄缠了蓝色手胶。',
    categoryId: 6,
    categoryName: '运动器材',
    images: ['https://picsum.photos/seed/racket/700/500'],
    location: '体育馆羽毛球场',
    happenTime: '2026-07-26',
    publishTime: '3天前',
    status: 'open',
    urgent: false,
    reward: '',
    publisher: { name: '球场管理员', avatar: 'https://picsum.photos/seed/u11/100/100', campus: '本部' },
    views: 39,
    contact: '体育馆前台认领'
  },
  {
    id: 12,
    type: 'lost',
    title: '银色手链（已找回）',
    desc: '在西门公交站丢失的手链已经找回，感谢捡到并联系我的好心同学！',
    categoryId: 5,
    categoryName: '衣物饰品',
    images: ['https://picsum.photos/seed/chain/700/500'],
    location: '西门公交站',
    happenTime: '2026-07-24',
    publishTime: '4天前',
    status: 'done',
    urgent: false,
    reward: '',
    publisher: { name: '桃桃', avatar: 'https://picsum.photos/seed/u12/100/100', campus: '西校区' },
    views: 156,
    contact: '微信：taotao_ss'
  },
  {
    id: 13,
    type: 'found',
    title: '蓝白格子雨伞（已归还）',
    desc: '教学楼门口伞架上多出来的一把伞，已由失主成功认领。',
    categoryId: 7,
    categoryName: '生活用品',
    images: ['https://picsum.photos/seed/umbrella/700/500'],
    location: '第一教学楼门口',
    happenTime: '2026-07-23',
    publishTime: '5天前',
    status: 'done',
    urgent: false,
    reward: '',
    publisher: { name: '热心楼长', avatar: 'https://picsum.photos/seed/u13/100/100', campus: '本部' },
    views: 98,
    contact: 'QQ：10203040'
  }
]

/**
 * 发布新物品（假数据版：直接插入到列表最前面）
 * @param {object} item - 表单收集的物品信息
 * @returns {object} 带 id 的完整物品对象
 */
function addItem(item) {
  const maxId = items.reduce((max, it) => Math.max(max, it.id), 0)
  const newItem = {
    id: maxId + 1,
    status: 'open',
    publishTime: '刚刚',
    views: 0,
    urgent: false,
    reward: '',
    publisher: { name: '我', avatar: 'https://picsum.photos/seed/me/100/100', campus: '本部' },
    ...item
  }
  // 没传图就给一张默认占位图
  if (!newItem.images || newItem.images.length === 0) {
    newItem.images = ['https://picsum.photos/seed/default' + newItem.id + '/700/500']
  }
  items.unshift(newItem)
  return newItem
}

/**
 * 根据 ID 查询物品
 * @param {number} id - 物品ID
 */
function getItemById(id) {
  return items.find(it => it.id === Number(id)) || null
}

/**
 * 获取"我"发布的物品列表（假数据版：按发布人名字过滤）
 * addItem 时 publisher.name 固定为 '我'，以此识别自己发布的数据
 */
function getMyItems() {
  return items.filter(it => it.publisher.name === '我')
}

/**
 * 标记物品为已完成（寻物→已找回 / 招领→已归还）
 * @param {number} id - 物品ID
 */
function finishItem(id) {
  const item = getItemById(id)
  if (item) {
    item.status = 'done'
  }
  return item
}

/**
 * 删除物品
 * @param {number} id - 物品ID
 */
function removeItem(id) {
  const index = items.findIndex(it => it.id === Number(id))
  if (index > -1) {
    items.splice(index, 1)
  }
}

/**
 * 关键词搜索（标题/描述/分类/地点 模糊匹配）
 * @param {string} keyword - 搜索词
 */
function searchItems(keyword) {
  const kw = (keyword || '').trim().toLowerCase()
  if (!kw) return []
  return items.filter(it =>
    it.title.toLowerCase().includes(kw) ||
    it.desc.toLowerCase().includes(kw) ||
    it.categoryName.toLowerCase().includes(kw) ||
    it.location.toLowerCase().includes(kw)
  )
}

module.exports = {
  banners,
  hotKeywords,
  items,
  addItem,
  getItemById,
  getMyItems,
  finishItem,
  removeItem,
  searchItems
}
