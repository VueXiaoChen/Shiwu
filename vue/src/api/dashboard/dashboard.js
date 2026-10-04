import request from '@/utils/request'

// 查询数据大屏统计数据(管理端首页用)
export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}
