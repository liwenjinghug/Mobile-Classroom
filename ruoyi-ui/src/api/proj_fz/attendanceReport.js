import request, { download } from '@/utils/request'

// 查询课堂维度统计
export function sessionStatistics(params) {
  return request({
    url: '/proj_fz/attendanceReport/sessionStatistics',
    method: 'get',
    params: params
  })
}

// 查询时间维度统计
export function timeStatistics(params) {
  return request({
    url: '/proj_fz/attendanceReport/timeStatistics',
    method: 'get',
    params: params
  })
}

// 查询签到明细
export function attendanceDetails(params) {
  return request({
    url: '/proj_fz/attendanceReport/attendanceDetails',
    method: 'get',
    params: params
  })
}

// 查询周报表
export function weeklyReport(params) {
  return request({
    url: '/proj_fz/attendanceReport/weeklyReport',
    method: 'get',
    params: params
  })
}

// 查询驾驶舱指标
export function dashboardMetrics(params) {
  return request({
    url: '/proj_fz/attendanceReport/dashboardMetrics',
    method: 'get',
    params: params
  })
}

// 导出考勤报表（使用 download helper 确保 form-urlencoded 兼容）
export function exportAttendanceReport(params, filename) {
  // download helper 会 POST 表单并保存 blob
  return download('/proj_fz/attendanceReport/export', params, filename, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
}

// 导出（表单方式，回退用）
export function exportAttendanceReportForm(params, filename) {
  return download('/proj_fz/attendanceReport/export', params, filename, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
}
