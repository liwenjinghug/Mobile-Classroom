import request from '@/utils/request'

// 获取作业统计列表
export function getHomeworkStatisticsList() {
  return request({
    url: '/proj_fz/homeworkStatistics/list',
    method: 'get'
  })
}

// 根据条件获取作业统计列表
export function getHomeworkStatisticsListByFilter(params) {
  return request({
    url: '/proj_fz/homeworkStatistics/listByFilter',
    method: 'get',
    params: params
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
    url: '/proj_fz/homeworkStatistics/teacherOverview',
    method: 'get'
  })
}

// 获取学生提交详情
export function getStudentSubmissionDetails(homeworkId) {
  return request({
    url: `/proj_fz/homeworkStatistics/studentSubmissions/${homeworkId}`,
    method: 'get'
  })
}

// 获取看板概览
export function getDashboardOverview() {
  return request({
    url: '/proj_fz/homeworkStatistics/dashboardOverview',
    method: 'get'
  })
}

// 获取课程列表
export function getCourseList() {
  return request({
    url: '/proj_fz/homeworkStatistics/courseList',
    method: 'get'
  })
}

// 获取课堂列表
export function getSessionList() {
  return request({
    url: '/proj_fz/homeworkStatistics/sessionList',
    method: 'get'
  })
}

// 获取课堂作业概览
export function getSessionOverview() {
  return request({
    url: '/proj_fz/homeworkStatistics/sessionOverview',
    method: 'get'
  })
}

// 导出作业数据
export function exportHomeworkData(params) {
  return request({
    url: '/proj_fz/homeworkStatistics/export',
    method: 'get',
    params: params,
    responseType: 'blob'
  })
}

// 调试接口：获取作业基本信息
export function getDebugHomeworkInfo() {
  return request({
    url: '/proj_fz/homeworkStatistics/debug/homeworkInfo',
    method: 'get'
  })
}

// 获取今日截止作业数
export function getTodayDeadlineCount() {
  return request({
    url: '/proj_fz/homeworkStatistics/todayDeadlineCount',
    method: 'get'
  })
}

// 根据高级筛选条件获取作业统计列表
export function getHomeworkStatisticsListByAdvancedFilter(params) {
  return request({
    url: '/proj_fz/homeworkStatistics/listByAdvancedFilter',
    method: 'get',
    params: params
  })
}
