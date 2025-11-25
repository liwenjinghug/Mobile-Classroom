import request from '@/utils/request'

export function listHomework(query) {
  return request({
    url: '/proj_lwj/homework/list',
    method: 'get',
    params: query,
    silent: true
  })
}

export function getHomework(id) {
  return request({
    url: '/proj_lwj/homework/' + id,
    method: 'get',
    silent: true
  })
}

export function addHomework(data) {
  return request({
    url: '/proj_lwj/homework',
    method: 'post',
    data
  })
}

export function updateHomework(data) {
  return request({
    url: '/proj_lwj/homework',
    method: 'put',
    data
  })
}

export function delHomework(id, cascade) {
  return request({
    url: '/proj_lwj/homework/' + id,
    method: 'delete',
    params: cascade ? { cascade: true } : {}
  })
}

export function submitHomework(data, params) {
  return request({
    url: '/proj_lwj/homework/submit',
    method: 'post',
    data,
    params: params || {},
    silent: true,
    timeout: 20000
  })
}

export function updateSubmission(data, params) {
  return request({
    url: '/proj_lwj/homework/submit',
    method: 'put',
    data,
    params: params || {},
    silent: true,
    timeout: 20000
  })
}

export function getSubmissions(homeworkId) {
  return request({
    url: '/proj_lwj/homework/submissions/' + homeworkId,
    method: 'get',
    silent: true
  })
}

export function getStudentSubmissions(studentNo) {
  return request({
    url: '/proj_lwj/homework/studentSubmissions/public',
    method: 'get',
    // send both common param names to be tolerant to backend naming
    params: { studentNo, student_no: studentNo },
    silent: true
  })
}

// Convenience alias for grading (uses same backend endpoint as updateSubmission)
export function gradeSubmission(data) {
  return request({
    url: '/proj_lwj/homework/grade',
    method: 'put',
    data,
    silent: true,
    timeout: 20000
  })
}

export function deleteSubmission(id) {
  return request({
    url: '/proj_lwj/homework/submission/' + id,
    method: 'delete',
    silent: true
  })
}
