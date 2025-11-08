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

// 导出操作日志记录 - 直接使用 GET 请求避免参数问题
export function exportOperlog() {
  return request({
    url: '/proj_cyq/operlog/export',
    method: 'get', // 改为 GET 请求，不传递任何参数
    responseType: 'blob'
  })
}
