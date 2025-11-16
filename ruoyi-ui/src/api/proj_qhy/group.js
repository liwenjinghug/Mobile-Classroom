import request from '@/utils/request'

// 封装一个通用的 FormData 上传函数
function uploadWithFormData(url, method, data, file) {
  const formData = new FormData()
  for (const key in data) {
    if (data[key] !== null && data[key] !== undefined) {
      formData.append(key, data[key])
    }
  }
  if (file) {
    formData.append('image', file.raw || file)
  }
  return request({
    url: url,
    method: method,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

const api = {
  // 获取我的小组列表
  getGroupList: () => request({
    url: '/proj_qhy/group/list',
    method: 'get',
    params: {
      '_t': new Date().getTime()
    }
  }),

  // 创建小组
  createGroup: (data) => request({
    url: '/proj_qhy/group/create',
    method: 'post',
    data: data // { groupName: "xxx", memberUserIds: [1, 2, 3] }
  }),

  // 获取小组详情 (信息+成员)
  getGroupDetails: (groupId) => request({
    url: `/proj_qhy/group/${groupId}`,
    method: 'get'
  }),

  // 更新小组信息
  updateGroupInfo: (data) => request({
    url: '/proj_qhy/group/update',
    method: 'put',
    data: data // { groupId: 1, groupName: "new"}
  }),



  // 获取聊天记录
  getChatMessages: (groupId) => request({
    url: `/proj_qhy/group/${groupId}/messages`,
    method: 'get'
  }),

  // 发送消息
  sendMessage: (groupId, data, imageFile) => {
    // data 包含 { content: "..." }
    return uploadWithFormData(`/proj_qhy/group/${groupId}/send`, 'post', data, imageFile)
  },

  // 移除成员
  removeMember: (groupId, memberUserId) => request({
    url: `/proj_qhy/group/${groupId}/member/${memberUserId}`,
    method: 'delete'
  }),

  // (头像上传 API)
  uploadGroupAvatar: (groupId, file) => {
    const formData = new FormData()
    // 'avatarfile' 必须与后端 @RequestParam("avatarfile") 一致
    formData.append('avatarfile', file.raw || file)
    return request({
      url: `/proj_qhy/group/${groupId}/avatar`,
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 搜索小组
  searchGroups: (params) => request({
    url: '/proj_qhy/group/search',
    method: 'get',
    params: params // { query: 'xxx', type: 'name' }
  }),

  // 加入小组
  joinGroup: (groupId) => request({
    url: `/proj_qhy/group/join/${groupId}`,
    method: 'post'
  }),

  disbandGroup: (groupId) => request({
    url: `/proj_qhy/group/${groupId}/disband`,
    method: 'delete'
  }),

  exitGroup: (groupId) => request({
    url: `/proj_qhy/group/${groupId}/exit`,
    method: 'post'
  }),

  recallMessage: (messageId) => request({
    url: `/proj_qhy/group/message/recall/${messageId}`,
    method: 'post'
  }),

  shareArticle: (payload) => request({
    url: '/proj_qhy/group/share/article',
    method: 'post',
    data: payload // { articleId: 1, groupIds: [100, 101] }
  })
}

export default api

