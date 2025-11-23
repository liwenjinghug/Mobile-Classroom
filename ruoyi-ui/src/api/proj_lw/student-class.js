import request from '@/utils/request'

// 学生视角API
export function getJoinedClasses(query) {
  return request({
    url: '/proj_lw/student/class/joined',
    method: 'get',
    params: query
  }).then(response => {
    console.log('我的课堂原始响应:', response)
    console.log('我的课堂响应数据:', response.data)

    let rows = [];
    let total = 0;

    if (response && response.data) {
      rows = response.data.rows || [];
      total = response.data.total || 0;
    } else if (response) {
      rows = response.rows || [];
      total = response.total || 0;
    }

    console.log('我的课堂解析结果:', { rows, total })
    return {
      rows: rows,
      total: total
    }
  })
}

export function getAvailableClasses(query) {
  return request({
    url: '/proj_lw/student/class/available',
    method: 'get',
    params: query
  }).then(response => {
    console.log('原始响应:', response)

    // 尝试不同的数据结构解析
    let rows = [];
    let total = 0;

    if (response && response.data) {
      // 方式1：直接使用response.data
      rows = response.data.rows || response.data.data || [];
      total = response.data.total || 0;
    } else if (response && Array.isArray(response)) {
      // 方式2：响应本身就是数组
      rows = response;
      total = response.length;
    } else if (response) {
      // 方式3：响应是对象但有rows字段
      rows = response.rows || [];
      total = response.total || 0;
    }

    console.log('解析后的数据 - rows:', rows, 'total:', total)

    return {
      rows: rows,
      total: total
    }
  })
}

export function applyJoinClass(sessionId) {
  return request({
    url: `/proj_lw/student/class/apply/${sessionId}`,
    method: 'post'
  })
}

export function getMyApplications(query) {
  return request({
    url: '/proj_lw/student/class/applications',
    method: 'get',
    params: query
  }).then(response => {
    console.log('我的申请原始响应:', response)

    let rows = [];
    let total = 0;

    if (response && response.data) {
      rows = response.data.rows || response.data.data || [];
      total = response.data.total || 0;
    } else if (response) {
      rows = response.rows || [];
      total = response.total || 0;
    }

    console.log('我的申请解析结果 - rows:', rows, 'total:', total)

    return {
      rows: rows,
      total: total
    }
  })
}

export function cancelApplication(applicationId) {
  return request({
    url: `/proj_lw/student/class/application/cancel/${applicationId}`,
    method: 'post'
  })
}

export function quitClass(sessionId) {
  return request({
    url: `/proj_lw/student/class/quit/${sessionId}`,
    method: 'post'
  })
}
