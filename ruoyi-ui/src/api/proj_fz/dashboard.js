import request from '@/utils/request'

// 获取驾驶舱指标数据
export function getDashboardMetrics() {
  return request({
    url: '/proj_fz/dashboard/metrics',
    method: 'get'
  })
}

// 获取天气数据
export function getWeatherData() {
  return request({
    url: '/proj_fz/dashboard/weather',
    method: 'get'
  })
}

// 获取核心指标数据
export function getCoreMetrics() {
  return request({
    url: '/proj_fz/dashboard/core-metrics',
    method: 'get'
  })
}

// 获取图表数据
export function getChartData() {
  return request({
    url: '/proj_fz/dashboard/chart-data',
    method: 'get'
  })
}

// 获取待办事项
export function getTodos() {
  return request({
    url: '/proj_fz/dashboard/todos',
    method: 'get'
  })
}

// 获取消息列表
export function getMessages() {
  return request({
    url: '/proj_fz/dashboard/messages',
    method: 'get'
  })
}

// 获取作业明细（带筛选）
export function getHomeworkDetails(params) {
  return request({
    url: '/proj_fz/dashboard/homework-details',
    method: 'post',
    data: params
  })
}

// 获取公告列表（带筛选）
export function getNotices(params) {
  return request({
    url: '/proj_fz/dashboard/notices',
    method: 'post',
    data: params
  })
}

// 获取操作日志（带筛选）
export function getOperationLogs(params) {
  return request({
    url: '/proj_fz/dashboard/operation-logs',
    method: 'post',
    data: params
  })
}

// 根据状态筛选作业
export function getHomeworkByStatus(status) {
  return request({
    url: `/proj_fz/dashboard/homework-by-status/${status}`,
    method: 'get'
  })
}
