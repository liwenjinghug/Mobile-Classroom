import request from '@/utils/request'

// 获取所有驾驶舱数据
export function getDashboardData() {
  return request({
    url: '/proj_fz/dashboard/data',
    method: 'get'
  })
}

/**
 * 获取天气数据 (最终方案：恢复为请求后端API)
 * @description 后端将负责调用高德API，前端只与后端交互。
 * 请确保已将后端服务器的公网IP地址添加至高德Key的IP白名单中。
 */
export function getWeatherData() {
  return request({
    url: '/proj_fz/dashboard/weather',
    method: 'get'
  })
}

// 获取天气配置
export function getWeatherConfig() {
  return request({
    url: '/proj_fz/dashboard/weather/config',
    method: 'get'
  })
}

// 更新天气配置
export function updateWeatherConfig(data) {
  return request({
    url: '/proj_fz/dashboard/weather/config',
    method: 'post',
    data
  })
}

// 获取核心指标
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

// 导出作业明细
export function exportHomeworkDetails(params) {
  return request({
    url: '/proj_fz/dashboard/export-homework',
    method: 'post',
    data: params,
    responseType: 'blob',
    headers: { 'Content-Type': 'application/json' }
  })
}

// 导出最新公告
export function exportNotices(params) {
  return request({
    url: '/proj_fz/dashboard/export-notices',
    method: 'post',
    data: params,
    responseType: 'blob',
    headers: { 'Content-Type': 'application/json' }
  })
}

// 导出操作日志
export function exportOperationLogs(params) {
  return request({
    url: '/proj_fz/dashboard/export-logs',
    method: 'post',
    data: params,
    responseType: 'blob',
    headers: { 'Content-Type': 'application/json' }
  })
}
// 获取单个作业详情
export function getHomeworkDetail(homeworkId) {
  return request({
    url: `/proj_fz/dashboard/homework-detail/${homeworkId}`,
    method: 'get'
  })
}

// 导出单个作业详情
export function exportSingleHomework(homeworkId) {
  return request({
    url: `/proj_fz/dashboard/export-single-homework/${homeworkId}`,
    method: 'get',
    responseType: 'blob'
  })
}

