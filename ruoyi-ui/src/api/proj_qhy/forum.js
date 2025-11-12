import request from '@/utils/request'

// 论坛相关API
export default {
  // 获取帖子列表
  getPostList() {
    return request({
      url: '/proj_qhy/forum/posts',
      method: 'get'
    })
  },

  // 发布帖子
  publishPost(data, files) {
    const formData = new FormData()
    formData.append('content', data.content)
    if (files && files.length > 0) {
      files.forEach(file => {
        formData.append('files', file)
      })
    }
    return request({
      url: '/proj_qhy/forum/post/publish',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 刷新帖子列表
  refreshPosts() {
    return request({
      url: '/proj_qhy/forum/posts/refresh',
      method: 'get'
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
