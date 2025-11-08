import request from '@/utils/request'

// 查询系统登录日志列表
export function listLoginlog(query) {
  return request({
    url: '/proj_cyq/loginlog/list',
    method: 'get',
    params: query
  })
}

// 查询系统登录日志详细
export function getLoginlog(loginId) {
  return request({
    url: '/proj_cyq/loginlog/' + loginId,
    method: 'get'
  })
}

// 删除系统登录日志
export function delLoginlog(loginId) {
  return request({
    url: '/proj_cyq/loginlog/' + loginId,
    method: 'delete'
  })
}

// 清空登录日志
export function cleanLoginlog() {
  return request({
    url: '/proj_cyq/loginlog/clean',
    method: 'delete'
  })
}

// 导出系统登录日志
export function exportLoginlog(query) {
  return request({
    url: '/proj_cyq/loginlog/export',
    method: 'post',
    data: query,
    responseType: 'blob'  // 重要：确保设置响应类型为 blob
  })
}
