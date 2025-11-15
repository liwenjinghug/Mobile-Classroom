import request from '@/utils/request'

// 考试列表（分页，支持 examName/courseId/sessionId/status 等查询）
export function listExam(params) {
  return request({ url: '/proj_lwj/exam/list', method: 'get', params })
}

export function addExam(data) {
  return request({ url: '/proj_lwj/exam', method: 'post', data })
}

export function updateExam(data) {
  return request({ url: '/proj_lwj/exam', method: 'put', data })
}

export function changeExamStatus(id, status) {
  return request({ url: `/proj_lwj/exam/status/${id}/${status}`, method: 'put' })
}

// 考试详情
export function getExam(id) {
  return request({ url: `/proj_lwj/exam/${id}`, method: 'get' })
}

// 查询考试题目列表（后端在 /proj_lwj/exam/question/list）
export function listQuestions(params) {
  return request({ url: '/proj_lwj/exam/question/list', method: 'get', params })
}

// 学生可参加的考试（按学号）
export function listAvailableExams(params) {
  return request({ url: '/proj_lwj/exam/available', method: 'get', params })
}

// 开始考试（后端兼容 /participant/start 与 /start）
export function startExam(data) {
  return request({ url: '/proj_lwj/exam/start', method: 'post', data })
}

// 保存答案
export function saveAnswer(data) {
  return request({ url: '/proj_lwj/exam/answer/save', method: 'post', data })
}

// 提交试卷（后端兼容 /participant/submit 与 /submit）
export function submitExam(data) {
  return request({ url: '/proj_lwj/exam/submit', method: 'post', data })
}

// 其它统计/监控相关（预留）
export function examProgress(params) {
  return request({ url: `/proj_lwj/exam/${params.examId}/progress`, method: 'get' })
}
export function monitorEvents(params) {
  return request({ url: '/proj_lwj/exam/monitor/list', method: 'get', params })
}
export function pushMonitorEvent(data) {
  return request({ url: '/proj_lwj/exam/monitor/event', method: 'post', data })
}
