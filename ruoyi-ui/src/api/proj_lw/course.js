import request from '@/utils/request'

export function listCourse(query) {
  return request({
    url: '/proj_lw/course/list',  // 保持下划线
    method: 'get',
    params: query
  })
}

export function getCourse(courseId) {
  return request({
    url: '/proj_lw/course/' + courseId,
    method: 'get'
  })
}

export function addCourse(data) {
  return request({
    url: '/proj_lw/course',
    method: 'post',
    data: data
  })
}

export function updateCourse(data) {
  return request({
    url: '/proj_lw/course',
    method: 'put',
    data: data
  })
}

export function delCourse(courseIds) {
  return request({
    url: '/proj_lw/course/' + courseIds,
    method: 'delete'
  })
}
