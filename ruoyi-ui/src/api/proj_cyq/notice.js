// src/api/proj_cyq/notice.js
import request from '@/utils/request'

// 查询公告列表
export function listNotice(query) {
  return request({
    url: '/proj_cyq/notice/list',
    method: 'get',
    params: query
  })
}

// 查询公告详细
export function getNotice(noticeId) {
  return request({
    url: '/proj_cyq/notice/' + noticeId,
    method: 'get'
  })
}

// 新增公告
export function addNotice(data) {
  return request({
    url: '/proj_cyq/notice',
    method: 'post',
    data: data
  })
}

// 修改公告
export function updateNotice(data) {
  return request({
    url: '/proj_cyq/notice',
    method: 'put',
    data: data
  })
}

// 删除公告
export function delNotice(noticeIds) {
  return request({
    url: '/proj_cyq/notice/' + noticeIds,
    method: 'delete'
  })
}

// 导出公告 - 参考操作日志的方式，使用GET请求，不传递参数
export function exportNotice() {
  return request({
    url: '/proj_cyq/notice/export',
    method: 'get',
    responseType: 'blob'
  })
}
