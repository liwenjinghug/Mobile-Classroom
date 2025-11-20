import request from '@/utils/request'

export function listTasks(sessionId) {
  return request({
    url: '/proj_myx/attendance/tasks',
    method: 'get',
    params: { sessionId }
  })
}

export function createTask(data) {
  return request({
    url: '/proj_myx/attendance/task/create',
    method: 'post',
    data
  })
}

export function getTask(taskId) {
  return request({ url: '/proj_myx/attendance/task/' + taskId, method: 'get' })
}

export function taskRecords(taskId) {
  return request({ url: '/proj_myx/attendance/task/' + taskId + '/records', method: 'get' })
}

export function closeTask(taskId) {
  return request({ url: '/proj_myx/attendance/task/' + taskId + '/close', method: 'post' })
}

export function startTask(taskId) {
  return request({ url: '/proj_myx/attendance/task/' + taskId + '/start', method: 'post' })
}

export function deleteTask(taskId) {
  return request({ url: '/proj_myx/attendance/task/' + taskId, method: 'delete' })
}

export function generateQr(taskId, ttlMinutes) {
  return request({ url: '/proj_myx/attendance/task/generateQr', method: 'post', data: { taskId, ttlMinutes } })
}

export function signByQr(payload) {
  return request({ url: '/proj_myx/attendance/sign/qr', method: 'post', data: payload })
}

export function signByLocation(payload) {
  return request({ url: '/proj_myx/attendance/sign/location', method: 'post', data: payload })
}

export function updateStudentStatus(taskId, studentId, status) {
  return request({
    url: '/proj_myx/attendance/task/status',
    method: 'post',
    data: { taskId, studentId, status }
  })
}
