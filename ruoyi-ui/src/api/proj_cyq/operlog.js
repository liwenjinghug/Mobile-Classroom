import request from '@/utils/request'

// 查询操作日志记录列表
export function listOperlog(query) {
  return request({
    url: '/proj_cyq/operlog/list',
    method: 'get',
    params: query
  })
}

// 查询操作日志记录详细
export function getOperlog(operId) {
  return request({
    url: '/proj_cyq/operlog/' + operId,
    method: 'get'
  })
}

// 删除操作日志记录
export function delOperlog(operId) {
  return request({
    url: '/proj_cyq/operlog/' + operId,
    method: 'delete'
  })
}

// 清空操作日志
export function cleanOperlog() {
  return request({
    url: '/proj_cyq/operlog/clean',
    method: 'delete'
  })
}

// 导出操作日志记录
export function exportOperlog(query) {
  return request({
    url: '/proj_cyq/operlog/export',
    method: 'get',
    params: query, // 添加参数支持，以便按条件导出
    responseType: 'blob'
  })
}

// 【新增】获取操作日志统计信息
export function getOperLogStats() {
  return request({
    url: '/proj_cyq/operlog/stats',
    method: 'get'
  })
}
