import request from '@/utils/request'

// 查询文章管理列表
export function listArticle(query) {
  return request({
    url: '/proj_qhy/article/list',
    method: 'get',
    params: query
  })
}

// 查询文章管理详细
export function getArticle(id) {
  return request({
    url: '/proj_qhy/article/' + id,
    method: 'get'
  })
}

// 新增文章管理
export function addArticle(data) {
  return request({
    url: '/proj_qhy/article',
    method: 'post',
    data: data
  })
}

// 修改文章管理
export function updateArticle(data) {
  return request({
    url: '/proj_qhy/article',
    method: 'put',
    data: data
  })
}

// 删除文章管理
export function delArticle(ids) {
  return request({
    url: '/proj_qhy/article/' + ids,
    method: 'delete'
  })
}

// 导出文章管理
export function exportArticle(query) {
  return request({
    url: '/proj_qhy/article/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

// 获取热门文章
export function getHotArticles(query) {
  return request({
    url: '/proj_qhy/article/hot',
    method: 'get',
    params: query
  })
}

// 增加阅读数
export function increaseViewCount(id) {
  return request({
    url: '/proj_qhy/article/view/' + id,
    method: 'post'
  })
}

// 点赞文章
export function likeArticle(id) {
  return request({
    url: '/proj_qhy/article/like/' + id,
    method: 'post'
  })
}

// 点踩文章
export function hateArticle(id) {
  return request({
    url: '/proj_qhy/article/hate/' + id,
    method: 'post'
  })
}
/**
 * (新增) 批量导出文章为PDF
 * @param {Array} ids 文章ID数组
 */
export function exportPdf(ids) {
  return request({
    url: '/proj_qhy/article/export-pdf',
    method: 'post',
    data: ids ,
    responseType: 'blob'
  })
}
// (新增) 导出 Word
export function exportWord(ids) {
  return request({
    url: '/proj_qhy/article/export-word',
    method: 'post',
    data: ids,
    responseType: 'blob' // 必须是 blob
  })
}
