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

// 导出系统登录日志 - 修改为GET请求，参考操作日志的方式
export function exportLoginlog(query) {
  return request({
    url: '/proj_cyq/loginlog/export',
    method: 'get', // 改为GET请求
    params: query, // 使用params传递查询参数
    responseType: 'blob'
  })
}
