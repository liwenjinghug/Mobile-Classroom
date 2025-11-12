import request from '@/utils/request'

// 查询资料列表
export function listMaterial(query) {
  return request({
    url: '/proj_lw/material/list',
    method: 'get',
    params: query
  })
}

// 查询资料详细
export function getMaterial(materialId) {
  return request({
    url: '/proj_lw/material/' + materialId,
    method: 'get'
  })
}

// 新增资料
export function addMaterial(data) {
  return request({
    url: '/proj_lw/material',
    method: 'post',
    data: data
  })
}

// 修改资料
export function updateMaterial(data) {
  return request({
    url: '/proj_lw/material',
    method: 'put',
    data: data
  })
}

// 删除资料
export function delMaterial(materialId) {
  return request({
    url: '/proj_lw/material/' + materialId,
    method: 'delete'
  })
}

// 推送资料
export function pushMaterial(materialId) {
  return request({
    url: '/proj_lw/material/push/' + materialId,
    method: 'post'
  })
}
