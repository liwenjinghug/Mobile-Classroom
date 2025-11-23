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

export function listParticipationHeatByFilter(data) {
  return request({
    url: '/proj_fz/participationHeat/listByFilter',
    method: 'post',
    data: data
  })
}

export function exportParticipationHeat(data) {
  return request({
    url: '/proj_fz/participationHeat/export',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

export function getFilterOptions() {
  return request({
    url: '/proj_fz/participationHeat/filterOptions',
    method: 'get'
  })
}

export function getParticipationDistribution(data) {
  return request({
    url: '/proj_fz/participationHeat/distribution',
    method: 'post',
    data: data
  })
}
