import request from '@/utils/request'

// 获取作业统计列表
export function getHomeworkStatisticsList() {
  return request({
    url: '/proj_fz/homeworkStatistics/list',
    method: 'get'
  })
}

// 获取作业详细统计
export function getHomeworkStatistics(homeworkId) {
  return request({
    url: `/proj_fz/homeworkStatistics/${homeworkId}`,
    method: 'get'
  })
}

// 获取成绩分布
export function getScoreDistribution(homeworkId) {
  return request({
    url: `/proj_fz/homeworkStatistics/scoreDistribution/${homeworkId}`,
    method: 'get'
  })
}

// 获取提交趋势
export function getSubmissionTrend(homeworkId) {
  return request({
    url: `/proj_fz/homeworkStatistics/submissionTrend/${homeworkId}`,
    method: 'get'
  })
}

// 获取教师作业概览
export function getTeacherOverview() {
  return request({
    url: '/proj_fz/homeworkStatistics/overview',
    method: 'get'
  })
}

// 获取看板数据
export function getDashboardData() {
  return request({
    url: '/proj_fz/homeworkStatistics/dashboard',
    method: 'get'
  })
}

// 获取学生提交详情
export function getStudentSubmissionDetails(homeworkId) {
  return request({
    url: `/proj_fz/homeworkStatistics/submissionDetails/${homeworkId}`,
    method: 'get'
  })
}

// 获取看板概览
export function getDashboardOverview() {
  return request({
    url: '/proj_fz/homeworkStatistics/dashboard/overview',
    method: 'get'
  })
}

// 调试接口：获取作业基本信息
export function getDebugHomeworkInfo() {
  return request({
    url: '/proj_fz/homeworkStatistics/debug/homeworkInfo',
    method: 'get'
  })
}
