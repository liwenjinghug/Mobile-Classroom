import request from '@/utils/request'

export function listSession(query) {
  return request({
    url: '/proj_lw/session/list',
    method: 'get',
    params: query
  })
}

export function getSessionsByClassNumber(classNumber) {
  return request({
    url: '/proj_lw/session/byClassNumber/' + classNumber,
    method: 'get'
  })
}

export function getSession(sessionId) {
  return request({
    url: '/proj_lw/session/' + sessionId,
    method: 'get'
  })
}

export function addSession(data) {
  return request({
    url: '/proj_lw/session',
    method: 'post',
    data: data
  })
}

export function updateSession(data) {
  return request({
    url: '/proj_lw/session',
    method: 'put',
    data: data
  })
}

export function delSession(sessionId) {
  return request({
    url: '/proj_lw/session/' + sessionId,
    method: 'delete'
  })
}
