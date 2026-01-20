import request from '@/utils/request'

// 查询系统监控列表
export function listMonitor(query) {
  return request({
    url: '/proj_fz/monitor/list',
    method: 'get',
    params: query
  })
}

// 查询系统监控列表（不分页，用于打印所有筛选结果）
export function listAllMonitor(query) {
  return request({
    url: '/proj_fz/monitor/listAll',
    method: 'get',
    params: query
  })
}

// 查询系统监控详细
export function getMonitor(monitorId) {
  return request({
    url: '/proj_fz/monitor/' + monitorId,
    method: 'get'
  })
}

// 新增系统监控
export function addMonitor(data) {
  return request({
    url: '/proj_fz/monitor',
    method: 'post',
    data: data
  })
}

// 修改系统监控
export function updateMonitor(data) {
  return request({
    url: '/proj_fz/monitor',
    method: 'put',
    data: data
  })
}

// 删除系统监控
export function delMonitor(monitorIds) {
  return request({
    url: '/proj_fz/monitor/' + monitorIds,
    method: 'delete'
  })
}

// 处理告警
export function handleAlert(data) {
  return request({
    url: '/proj_fz/monitor/handle',
    method: 'put',
    data: data
  })
}

// 导出系统监控
export function exportMonitor(query) {
  return request({
    url: '/proj_fz/monitor/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

// 获取实时服务器指标
export function getServerMetrics() {
  return request({
    url: '/proj_fz/monitor/server/realtime',
    method: 'get'
  })
}

// 获取服务器历史指标
export function getServerHistory(hours) {
  return request({
    url: '/proj_fz/monitor/server/history',
    method: 'get',
    params: { hours }
  })
}

// 获取实时数据库指标
export function getDatabaseMetrics() {
  return request({
    url: '/proj_fz/monitor/database/realtime',
    method: 'get'
  })
}

// 获取数据库历史指标
export function getDatabaseHistory(hours) {
  return request({
    url: '/proj_fz/monitor/database/history',
    method: 'get',
    params: { hours }
  })
}

// 获取监控统计数据
export function getStatistics(days) {
  return request({
    url: '/proj_fz/monitor/statistics',
    method: 'get',
    params: { days }
  })
}

// 获取未处理告警数量
export function getUnhandledAlertCount() {
  return request({
    url: '/proj_fz/monitor/alert/count',
    method: 'get'
  })
}

// 手动执行监控采集
export function collectMetrics() {
  return request({
    url: '/proj_fz/monitor/collect',
    method: 'post'
  })
}

