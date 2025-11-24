import request from '@/utils/request'

export function listSession(query) {
  return request({
    url: '/proj_lw/session/list',
    method: 'get',
    params: query
  })
}

// 修改这个方法：使用查询参数方式
export function getSessionsByClassNumber(classNumber) {
  return request({
    url: '/proj_lw/session/list',
    method: 'get',
    params: { classNumber: classNumber }
  })
}

// 修改这个方法：使用查询参数方式
export function getSessionsByCourseId(courseId) {
  return request({
    url: '/proj_lw/session/list',
    method: 'get',
    params: { courseId: courseId }
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

// 检查进入课堂权限
export function checkEnterPermission(sessionId) {
  return request({
    url: '/proj_lw/session/enter/' + sessionId,
    method: 'get'
  })
}
