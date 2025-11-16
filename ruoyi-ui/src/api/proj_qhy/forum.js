import request from '@/utils/request'

// 封装一个通用的 FormData 上传函数
// (用于发布和修改时，同时上传文本和文件)
function uploadWithFormData(url, method, postData, files) {
  const formData = new FormData()

  // 1. 添加非文件数据
  for (const key in postData) {
    if (postData[key] !== null && postData[key] !== undefined) {
      formData.append(key, postData[key])
    }
  }

  // 2. 添加文件数据
  if (files && files.length > 0) {
    files.forEach(file => {
      // 确保我们只添加原始文件对象
      if (file.raw) {
        formData.append('files', file.raw)
      } else if (file instanceof File) {
        formData.append('files', file)
      }
    })
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


// 论坛相关API
const api = {
  // 获取帖子列表
  getPostList() {
    return request({
      url: '/proj_qhy/forum/posts',
      method: 'get'
    })
  },

  // 发布帖子
  publishPost(data, files) {
    // data 包含 { content: '...' }
    // files 是 el-upload 的文件列表
    return uploadWithFormData('/proj_qhy/forum/post/publish', 'post', data, files)
  },

  // 刷新帖子列表
  refreshPosts() {
    return request({
      url: '/proj_qhy/forum/posts/refresh',
      method: 'get'
    })
  },

  // --- 新增：修改帖子 ---
  // data 包含 { postId: '...', content: '...', imageUrls: '...' }
  // files 是新上传的文件
  updatePost(data, files) {
    return uploadWithFormData('/proj_qhy/forum/post/update', 'put', data, files)
  },

  // --- 新增：删除帖子 ---
  deletePost(postId) {
    return request({
      url: `/proj_qhy/forum/post/delete/${postId}`,
      method: 'delete'
    })
  },

  // 获取帖子的点赞列表
  getLikesByPostId(postId) {
    return request({
      url: `/proj_qhy/forum/likes/${postId}`,
      method: 'get'
    })
  },

  // 点赞帖子
  likePost(postId) {
    return request({
      url: `/proj_qhy/forum/like/${postId}`,
      method: 'post'
    })
  },

  // 取消点赞
  cancelLike(postId) {
    return request({
      url: `/proj_qhy/forum/like/cancel/${postId}`,
      method: 'post'
    })
  },

  // 获取帖子的评论列表
  getCommentsByPostId(postId) {
    return request({
      url: `/proj_qhy/forum/comments/${postId}`,
      method: 'get'
    })
  },

  // 添加评论/回复
  addComment(comment) {
    return request({
      url: '/proj_qhy/forum/comment/add',
      method: 'post',
      data: comment
    })
  },

  // 获取用户通知
  getUserNotices() {
    return request({
      url: '/proj_qhy/forum/notices',
      method: 'get'
    })
  }
}

export default api
