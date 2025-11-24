import request from '@/utils/request'

export function listExamQuestion(query) {
  return request({ url: '/proj_lwj/exam/question/list', method: 'get', params: query })
}
export function getExamQuestion(id) {
  return request({ url: '/proj_lwj/exam/question/' + id, method: 'get' })
}
export function addExamQuestion(data) {
  return request({ url: '/proj_lwj/exam/question', method: 'post', data })
}
export function updateExamQuestion(data) {
  return request({ url: '/proj_lwj/exam/question', method: 'put', data })
}
export function delExamQuestion(id) {
  return request({ url: '/proj_lwj/exam/question/' + id, method: 'delete' })
}
export function delExamQuestionBatch(ids) {
  return request({ url: '/proj_lwj/exam/question/batch', method: 'delete', data: ids })
}
export function reorderExamQuestions(items) {
  return request({ url: '/proj_lwj/exam/question/reorder', method: 'post', data: items })
}
