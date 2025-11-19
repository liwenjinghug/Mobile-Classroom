import request from '@/utils/request'

export function listParticipationHeat(query) {
  return request({
    url: '/proj_fz/participationHeat/list',
    method: 'get',
    params: query
  })
}

export function getParticipationStats() {
  return request({
    url: '/proj_fz/participationHeat/stats',
    method: 'get'
  })
}
