import request from '@/utils/request'

export function listStudentsByCourseAndSession(params) {
  // params: { courseCode, sessionId }
  return request({
    url: '/proj_lw/class_student/listByCourseAndSession',
    method: 'get',
    params
  })
}

