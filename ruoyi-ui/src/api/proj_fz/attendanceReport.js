import request, { download } from '@/utils/request'

// 查询课堂维度统计
export function sessionStatistics(params) {
  return request({
    url: '/proj_fz/attendanceReport/sessionStatistics',
    method: 'get',
    params: params
  })
}

// 查询时间维度统计
export function timeStatistics(params) {
  return request({
    url: '/proj_fz/attendanceReport/timeStatistics',
    method: 'get',
    params: params
  })
}

// 查询签到明细
export function attendanceDetails(params) {
  return request({
    url: '/proj_fz/attendanceReport/attendanceDetails',
    method: 'get',
    params: params
  })
}

// 查询周报表
export function weeklyReport(params) {
  return request({
    url: '/proj_fz/attendanceReport/weeklyReport',
    method: 'get',
    params: params
  })
}

// 查询驾驶舱指标
export function dashboardMetrics(params) {
  return request({
    url: '/proj_fz/attendanceReport/dashboardMetrics',
    method: 'get',
    params: params
  })
}

// 添加新的API方法
export function getParticipationDistribution() {
  return request({
    url: '/proj_fz/participationHeat/distribution',
    method: 'get'
  })
}

// 导出考勤报表 - 使用新的导出接口
export function exportAttendanceReport(params, filename) {
  return request({
    url: '/proj_fz/attendanceReport/exportData',
    method: 'get',
    params: params,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  }).then(response => {
    // 处理blob响应，创建下载链接
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const downloadElement = document.createElement('a');
    const href = window.URL.createObjectURL(blob);
    downloadElement.href = href;
    downloadElement.download = filename;
    document.body.appendChild(downloadElement);
    downloadElement.click();
    document.body.removeChild(downloadElement);
    window.URL.revokeObjectURL(href);
  });
}
