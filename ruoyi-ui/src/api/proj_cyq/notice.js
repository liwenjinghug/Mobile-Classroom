import request from '@/utils/request'

// ... (保留 listNotice, getNotice, addNotice, updateNotice, delNotice, exportNotice, getNoticeStats) ...
export function listNotice(query) {
  return request({ url: '/proj_cyq/notice/list', method: 'get', params: query })
}
export function getNotice(noticeId) {
  return request({ url: '/proj_cyq/notice/' + noticeId, method: 'get' })
}
export function addNotice(data) {
  return request({ url: '/proj_cyq/notice', method: 'post', data: data })
}
export function updateNotice(data) {
  return request({ url: '/proj_cyq/notice', method: 'put', data: data })
}
export function delNotice(noticeIds) {
  return request({ url: '/proj_cyq/notice/' + noticeIds, method: 'delete' })
}
export function exportNotice() {
  return request({ url: '/proj_cyq/notice/export', method: 'get', responseType: 'blob' })
}
export function getNoticeStats() {
  return request({ url: '/proj_cyq/notice/stats', method: 'get' })
}

// 【修改】导出通告 Word
export function exportNoticeWord(noticeId) {
  return request({
    url: '/proj_cyq/notice/exportWord/' + noticeId,
    method: 'get',
    responseType: 'blob' // 必须保留，Word文件也是二进制流
  })
}
