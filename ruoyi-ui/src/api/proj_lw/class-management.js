import request from '@/utils/request'

// 获取课堂学生列表
export function getClassStudents(sessionId, query) {
  console.log('API调用: 获取课堂学生, sessionId:', sessionId, 'query:', query)
  return request({
    url: `/proj_lw/class/management/${sessionId}/students`,
    method: 'get',
    params: query
  }).then(response => {
    console.log('课堂学生API原始响应:', response)

    // 直接返回响应，让组件处理
    return response
  }).catch(error => {
    console.error('课堂学生API错误:', error)
    throw error
  })
}

// 搜索所有学生
export function searchAllStudents(keyword, sessionId) {
  console.log('=== API调用调试 ===')
  console.log('keyword:', keyword)
  console.log('sessionId:', sessionId)

  return request({
    url: '/proj_lw/class/management/students/search',
    method: 'get',
    params: {
      keyword: keyword,
      sessionId: sessionId  // 确保这里传递了sessionId
    }
  }).then(response => {
    console.log('API响应数据:', response)
    return response
  }).catch(error => {
    console.error('搜索学生API错误:', error)
    throw error
  })
}

//添加学生到课堂
export function addStudentsToClass(sessionId, studentIds) {
  console.log('API调用: 添加学生, sessionId:', sessionId, 'studentIds:', studentIds)
  return request({
    url: `/proj_lw/class/management/${sessionId}/students/add`,
    method: 'post',
    data: studentIds  // 直接发送数组
  })
}

// 从课堂移除学生
export function removeStudentFromClass(sessionId, studentId) {
  console.log('API调用: 移除学生, sessionId:', sessionId, 'studentId:', studentId)
  return request({
    url: `/proj_lw/class/management/${sessionId}/students/${studentId}/remove`,
    method: 'post'
  })
}
