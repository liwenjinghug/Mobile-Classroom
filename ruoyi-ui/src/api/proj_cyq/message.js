import request from '@/utils/request'

// 查询个人消息列表
export function listMessage() {
  return request({
    url: '/proj_cyq/message/list',
    method: 'get'
  })
}

// 标记待办消息已读
export function markTodoAsRead(todoId) {
  return request({
    url: '/proj_cyq/message/read/todo/' + todoId,
    method: 'put'
  })
}

// 标记作业消息已读
export function markHomeworkAsRead(homeworkId) {
  return request({
    url: '/proj_cyq/message/read/homework/' + homeworkId,
    method: 'put'
  })
}

// 【新增】标记考试消息已读
export function markExamAsRead(examId) {
  return request({
    url: '/proj_cyq/message/read/exam/' + examId,
    method: 'put'
  })
}

// 批量标记所有消息已读
export function markAllAsRead() {
  return request({
    url: '/proj_cyq/message/read/all',
    method: 'put'
  })
}

// 获取未读消息数量
export function getUnreadCount() {
  return request({
    url: '/proj_cyq/message/unreadCount',
    method: 'get'
  })
}

// 删除消息
export function deleteMessage(messageId) {
  return request({
    url: '/proj_cyq/message/' + messageId,
    method: 'delete'
  })
}

// 导出消息
export function exportMessage() {
  return request({
    url: '/proj_cyq/message/export',
    method: 'post',
    responseType: 'blob'
  })
}

// 获取消息统计
export function getMessageStats() {
  return request({
    url: '/proj_cyq/message/stats',
    method: 'get'
  })
}
