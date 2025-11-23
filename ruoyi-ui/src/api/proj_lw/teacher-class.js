import request from '@/utils/request'

export function getMyTeachingClasses(query) {
  console.log('API调用: 获取教师课堂, 参数:', query)
  return request({
    url: '/proj_lw/teacher/class/my',
    method: 'get',
    params: query
  }).then(response => {
    console.log('教师课堂API原始响应:', response)

    // 直接使用 response.data，不进行额外处理
    // 让前端组件自己处理数据结构
    return response
  }).catch(error => {
    console.error('教师课堂API错误:', error)
    throw error
  })
}

export function getPendingApplications(query) {
  console.log('API调用: 获取待审核申请, 参数:', query)
  return request({
    url: '/proj_lw/teacher/class/applications/pending',
    method: 'get',
    params: query
  }).then(response => {
    console.log('待审核申请API原始响应:', response)

    // 直接返回原始响应
    return response
  }).catch(error => {
    console.error('待审核申请API错误:', error)
    throw error
  })
}

export function auditApplication(applicationId, status, remark) {
  return request({
    url: '/proj_lw/teacher/class/application/audit',
    method: 'post',
    params: {
      applicationId,
      status,
      remark
    }
  })
}

export function batchAuditApplications(applicationIds, status) {
  return request({
    url: '/proj_lw/teacher/class/application/batchAudit',
    method: 'post',
    params: {
      applicationIds: applicationIds.join(','),
      status
    }
  })
}
