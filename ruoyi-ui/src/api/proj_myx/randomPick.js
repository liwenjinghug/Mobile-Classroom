import request from '@/utils/request'

export function fetchEligible(sessionId) {
  return request({
    url: '/proj_myx/random/eligible',
    method: 'get',
    params: { sessionId }
  })
}

export function serverRandomPick(sessionId, teacherId) {
  return request({
    url: '/proj_myx/random/pick/random',
    method: 'post',
    params: { sessionId, teacherId }
  })
}

export function savePick(record) {
  return request({
    url: '/proj_myx/random/pick/save',
    method: 'post',
    data: record
  })
}

export function fetchHistory(sessionId) {
  return request({
    url: '/proj_myx/random/history',
    method: 'get',
    params: { sessionId }
  })
}

export function delPick(rpickId) {
  return request({
    url: '/proj_myx/random/pick/' + rpickId,
    method: 'delete'
  })
}

export function updatePick(data) {
  return request({
    url: '/proj_myx/random/pick',
    method: 'put',
    data: data
  })
}

export function exportPick(sessionId) {
  return request({
    url: '/proj_myx/random/export',
    method: 'post',
    params: { sessionId },
    responseType: 'blob'
  })
}
