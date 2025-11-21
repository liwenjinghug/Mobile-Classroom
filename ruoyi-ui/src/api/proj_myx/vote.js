import request from '@/utils/request'

// 查询投票列表
export function listVotes(sessionId) {
  return request({
    url: '/proj_myx/vote/list',
    method: 'get',
    params: { sessionId }
  })
}

// 创建投票
export function createVote(data) {
  return request({
    url: '/proj_myx/vote/create',
    method: 'post',
    data: data
  })
}

// 开始投票
export function startVote(voteId) {
  return request({
    url: `/proj_myx/vote/${voteId}/start`,
    method: 'post'
  })
}

// 结束投票
export function closeVote(voteId) {
  return request({
    url: `/proj_myx/vote/${voteId}/close`,
    method: 'post'
  })
}

// 删除投票
export function deleteVote(voteId) {
  return request({
    url: `/proj_myx/vote/${voteId}`,
    method: 'delete'
  })
}

// 获取投票详情
export function getVote(voteId) {
  return request({
    url: `/proj_myx/vote/${voteId}`,
    method: 'get'
  })
}

// 获取统计
export function getVoteStats(voteId) {
  return request({
    url: `/proj_myx/vote/${voteId}/stats`,
    method: 'get'
  })
}

// 提交投票
export function submitVote(data) {
  return request({
    url: '/proj_myx/vote/submit',
    method: 'post',
    data: data
  })
}

// 检查是否已投
export function checkVoted(voteId, studentId) {
  return request({
    url: `/proj_myx/vote/${voteId}/check`,
    method: 'get',
    params: { studentId }
  })
}
