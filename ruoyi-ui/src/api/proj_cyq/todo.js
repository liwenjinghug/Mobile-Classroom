import request from '@/utils/request'

// 查询待办事项列表
export function listTodo(query) {
  return request({
    url: '/proj_cyq/todo/list',
    method: 'get',
    params: query
  })
}

// 查询待办事项详细
export function getTodo(todoId) {
  return request({
    url: '/proj_cyq/todo/' + todoId,
    method: 'get'
  })
}

// 新增待办事项
export function addTodo(data) {
  return request({
    url: '/proj_cyq/todo',
    method: 'post',
    data: data
  })
}

// 修改待办事项
export function updateTodo(data) {
  return request({
    url: '/proj_cyq/todo',
    method: 'put',
    data: data
  })
}

// 删除待办事项（单个）
export function delTodo(todoId) {
  return request({
    url: '/proj_cyq/todo/' + todoId,
    method: 'delete'
  })
}

// 批量删除待办事项
export function delTodos(todoIds) {
  return request({
    url: '/proj_cyq/todo/batch/' + todoIds.join(','),
    method: 'delete'
  })
}

// 导出待办事项
export function exportTodo(query) {
  return request({
    url: '/proj_cyq/todo/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

// 获取统计信息
export function getTodoStats(params) {
  return request({
    url: '/proj_cyq/todo/stats',
    method: 'get',
    params: params
  })
}

// 获取统计详情
export function getTodoStatsDetail(params) {
  return request({
    url: '/proj_cyq/todo/stats/detail',
    method: 'get',
    params: params
  })
}

// 导出统计结果
export function exportStats(params) {
  return request({
    url: '/proj_cyq/todo/exportStats',
    method: 'post',
    params: params,
    responseType: 'blob'
  })
}
