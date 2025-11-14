import request from '@/utils/request'

export function listExam(query) {
  return request({
    url: '/proj_lwj/exam/list',
    method: 'get',
    params: query
  })
}

export function getExam(id) {
  return request({
    url: '/proj_lwj/exam/' + id,
    method: 'get'
  })
}

export function addExam(data) {
  return request({
    url: '/proj_lwj/exam',
    method: 'post',
    data
  })
}

export function updateExam(data) {
  return request({
    url: '/proj_lwj/exam',
    method: 'put',
    data
  })
}

export function delExam(id) {
  return request({
    url: '/proj_lwj/exam/' + id,
    method: 'delete'
  })
}

export function publishExam(id) {
  return request({
    url: `/proj_lwj/exam/${id}/publish`,
    method: 'put'
  })
}

export function startExam(id) {
  return request({
    url: `/proj_lwj/exam/${id}/start`,
    method: 'put'
  })
}

export function endExam(id) {
  return request({
    url: `/proj_lwj/exam/${id}/end`,
    method: 'put'
  })
}

export function batchAddExam(exam, sessionIds) {
  return request({
    url: '/proj_lwj/exam/batch',
    method: 'post',
    data: { exam, sessionIds }
  })
}
