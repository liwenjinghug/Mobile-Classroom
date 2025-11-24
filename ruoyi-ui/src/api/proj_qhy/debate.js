import request from '@/utils/request'

// 查询辩论列表
export function listDebate(query) {
  return request({
    url: '/proj_qhy/debate/list',
    method: 'get',
    params: query
  })
}

// 查询辩论详细
export function getDebate(id) {
  return request({
    url: '/proj_qhy/debate/' + id,
    method: 'get'
  })
}

// 新增辩论
export function addDebate(data) {
  return request({
    url: '/proj_qhy/debate',
    method: 'post',
    data: data
  })
}

// 修改辩论
export function updateDebate(data) {
  return request({
    url: '/proj_qhy/debate',
    method: 'put',
    data: data
  })
}

// 删除辩论
export function delDebate(id) {
  return request({
    url: '/proj_qhy/debate/' + id,
    method: 'delete'
  })
}

// 加入辩论
export function joinDebate(data) {
  return request({
    url: '/proj_qhy/debate/join',
    method: 'post',
    data: data
  })
}

// 获取房间实时信息
export function getRoomInfo(id) {
  return request({
    url: '/proj_qhy/debate/room/' + id,
    method: 'get'
  })
}

// 发送消息
export function sendMsg(data) {
  return request({
    url: '/proj_qhy/debate/msg',
    method: 'post',
    data: data
  })
}

// 获取消息列表
export function getMsgList(debateId) {
  return request({
    url: '/proj_qhy/debate/msg/list/' + debateId,
    method: 'get'
  })
}

// 投票
export function voteDebate(data) {
  return request({
    url: '/proj_qhy/debate/vote',
    method: 'post',
    data: data
  })
}

// 开始
export function startDebate(id) {
  return request({
    url: '/proj_qhy/debate/start/' + id,
    method: 'put'
  })
}

// 结束
export function stopDebate(id) {
  return request({
    url: '/proj_qhy/debate/stop/' + id,
    method: 'put'
  })
}
