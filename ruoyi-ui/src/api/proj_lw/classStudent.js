import request from '@/utils/request'

// 获取当前登录用户的学生信息
export function getCurrentStudent() {
  return request({
    url: '/proj_lw/student/class/current-student',
    method: 'get'
  })
}


