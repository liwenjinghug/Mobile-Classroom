<template>
  <div class="forum-container">
    <div class="top-buttons">
      <el-button type="primary" @click="showNoticeDialog" icon="el-icon-bell">通知</el-button>
      <el-button type="success" @click="showPublishDialog" icon="el-icon-edit">发帖</el-button>
      <el-button type="info" @click="refreshPosts" icon="el-icon-refresh" :loading="refreshLoading">刷新</el-button>
    </div>

    <div class="post-list">
      <div v-for="post in posts" :key="post.postId" class="post-item">
        <div class="post-content">
          <div class="avatar-container">
            <img
              :src="post.avatar ? getImageUrl(post.avatar) : defaultAvatar"
              alt="用户头像"
              class="avatar"
              @error="handleImageError"
            >
          </div>

          <div class="post-main">
            <div class="username">{{ post.nickName }}</div>

            <div class="post-text">{{ post.content }}</div>

            <div v-if="post.imageUrls && post.imageUrls.trim()" class="post-images">
              <div
                v-for="(img, index) in post.imageUrls.split(',')"
                :key="index"
                class="image-item"
                v-if="img.trim()"
              >
                <el-image
                  :src="getImageUrl(img)"
                  alt="帖子图片"
                  class="post-img"
                  fit="cover"
                  :preview-src-list="getPreviewList(post.imageUrls, img)"
                  @error="handleImageError"
                >
                  <div slot="error" class="image-slot">
                    <i class="el-icon-picture-outline"></i>
                  </div>
                </el-image>
              </div>
            </div>

            <div class="post-footer">
              <span class="post-time">{{ formatTime(post.createTime) }}</span>
              <div class="post-actions">
                <el-button
                  v-if="Number(post.userId) === Number(currentUserId)"
                  type="text"
                  class="action-btn edit-btn"
                  @click="showEditDialog(post)"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="Number(post.userId) === Number(currentUserId)"
                  type="text"
                  class="action-btn delete-btn"
                  @click="handleDelete(post)"
                >
                  删除
                </el-button>

                <el-button
                  :class="post.isLiked ? 'liked-btn' : 'like-btn'"
                  @click="handleLike(post)"
                  :loading="likeLoading[post.postId]"
                >
                  {{ post.isLiked ? '已赞' : '点赞' }}
                </el-button>

                <el-button
                  class="comment-btn"
                  @click="showCommentDialog(post)"
                >
                  评论
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="post-interactions" v-if="post.likeCount > 0 || post.commentCount > 0">
          <div v-if="post.likeCount > 0" class="likes-list">
            <span class="heart-icon">♡</span>
            <span class="like-names">
              {{ getLikeNames(post.postId) }}
            </span>
          </div>

          <div v-if="post.commentCount > 0" class="comments-list">
            <div
              v-for="comment in getComments(post.postId)"
              :key="comment.commentId"
              class="comment-item"
              @click="showReplyDialog(post, comment)"
            >
              <span class="comment-username">{{ comment.nickName }}</span>
              <span v-if="comment.replyToNickName" class="reply-to"> 回复 {{ comment.replyToNickName }}：</span>
              <span v-else class="comment-colon">：</span>
              <span class="comment-content">{{ comment.content }}</span>
            </div>
          </div>
        </div>

        <div class="divider"></div>
      </div>
    </div>

    <el-dialog
      title="发布帖子"
      :visible.sync="publishDialogVisible"
      width="600px"
      :close-on-click-modal="false"
      @close="resetPublishForm"
    >
      <el-form :model="publishForm">
        <el-input
          type="textarea"
          v-model="publishForm.content"
          placeholder="分享新鲜事..."
          rows="6"
          class="publish-textarea"
        ></el-input>
        <el-upload
          ref="publishUpload"
          action="#"
          :auto-upload="false"
          :on-change="handlePublishImageChange"
          :file-list="publishForm.imageFileList"
          list-type="picture-card"
          :on-remove="handlePublishImageChange"
          accept="image/*"
        >
          <i class="el-icon-plus"></i>
        </el-upload>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button
          type="success"
          @click="submitPost"
          :disabled="!publishForm.content.trim()"
          :loading="publishLoading"
        >
          发布
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="修改帖子"
      :visible.sync="editDialogVisible"
      width="600px"
      :close-on-click-modal="false"
      @close="resetEditForm"
    >
      <el-form :model="editForm">
        <el-input
          type="textarea"
          v-model="editForm.content"
          placeholder="请输入帖子内容"
          rows="6"
          class="publish-textarea"
        ></el-input>
        <el-upload
          ref="editUpload"
          action="#"
          :auto-upload="false"
          :on-change="handleEditImageChange"
          :file-list="editForm.imageFileList"
          list-type="picture-card"
          :on-remove="handleEditImageChange"
          accept="image/*"
        >
          <i class="el-icon-plus"></i>
        </el-upload>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button
          type="success"
          @click="submitEdit"
          :disabled="!editForm.content.trim()"
          :loading="editLoading"
        >
          保存
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      :title="currentCommentParentId ? '回复评论' : '发表评论'"
      :visible.sync="commentDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-input
        type="textarea"
        v-model="commentContent"
        placeholder="请输入评论内容"
        rows="4"
      ></el-input>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeCommentDialog">取消</el-button>
        <el-button
          type="success"
          @click="submitComment"
          :disabled="!commentContent.trim()"
        >
          发送
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="通知"
      :visible.sync="noticeDialogVisible"
      width="500px"
      :close-on-click-modal="false"
      class="notice-dialog"
    >
      <div v-if="notices.length === 0" class="no-notice">暂无通知</div>
      <div v-else class="notice-list">
        <div
          v-for="(notice, index) in notices"
          :key="index"
          class="notice-item"
        >
          <div class="notice-avatar">
            <img
              :src="notice.operatorAvatar ? getImageUrl(notice.operatorAvatar) : defaultAvatar"
              alt="用户头像"
              class="avatar"
              @error="handleImageError"
            >
          </div>
          <div class="notice-content">
            <div class="notice-username">{{ notice.operatorNickName }}</div>
            <div class="notice-text">
              <template v-if="notice.noticeType === 1">
                点赞了你的帖子
              </template>
              <template v-else-if="notice.noticeType === 2">
                评论了你的帖子：{{ notice.commentContent }}
              </template>
            </div>
            <div class="notice-time">{{ formatTime(notice.createTime) }}</div>
          </div>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import forumApi from '@/api/proj_qhy/forum'
// 引入 store 获取当前用户信息
import store from '@/store'

// 时间格式化函数 (若您已有全局方法，请替换)
function formatDate(date, fmt) {
  // 1. 安全检查：如果date是null或undefined，返回空字符串
  if (!date) {
    return ''
  }

  // 2. 确保date是Date对象
  let dateObj
  if (typeof date === 'string') {
    // 替换'-'为'/'以兼容iOS/Safari
    dateObj = new Date(date.replace(/-/g, '/'))
  } else if (date instanceof Date) {
    dateObj = date
  } else {
    // 如果是数字时间戳等，也尝试转换
    dateObj = new Date(date)
  }

  // 3. 检查转换结果是否有效
  if (isNaN(dateObj.getTime())) {
    return '' // 返回空字符串，而不是 "Invalid Date"
  }

  // 4. 开始格式化
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (dateObj.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  const o = {
    'M+': dateObj.getMonth() + 1,
    'd+': dateObj.getDate(),
    'H+': dateObj.getHours(),
    'm+': dateObj.getMinutes(),
    's+': dateObj.getSeconds()
  }
  for (const k in o) {
    if (new RegExp(`(${k})`).test(fmt)) {
      const str = o[k] + ''
      fmt = fmt.replace(RegExp.$1, RegExp.$1.length === 1 ? str : ('00' + str).substr(str.length))
    }
  }
  return fmt
}

export default {
  name: 'Forum',
  data() {
    return {
      // 当前登录用户ID
      //currentUserId: store.getters.userId,

      // 帖子列表
      posts: [],
      // 点赞列表缓存
      likesMap: {},
      // 评论列表缓存
      commentsMap: {},
      // 默认头像
      defaultAvatar: require('@/assets/images/profile.jpg'),

      // 发帖弹窗相关
      publishDialogVisible: false,
      publishForm: {
        content: '',
        imageFileList: []
      },
      publishLoading: false,

      // 新增：修改弹窗相关
      editDialogVisible: false,
      editForm: {
        postId: null,
        content: '',
        imageFileList: []
      },
      editLoading: false,

      // 评论弹窗相关
      commentDialogVisible: false,
      commentContent: '',
      currentPostId: null,
      currentCommentParentId: null,
      currentReplyToUserId: null,

      // 通知弹窗相关
      noticeDialogVisible: false,
      notices: [],

      // 加载状态
      likeLoading: {},
      refreshLoading: false
    }
  },
  created() {
    this.loadPosts()
  },
  // --- 新增这个 computed 块 ---
  computed: {
    currentUserId() {
      // 这样写，currentUserId 就会在 store.getters.userId 变化时自动更新
      return store.getters.userId
    }
  },
  methods: {
    // 基础路径处理
    getImageUrl(url) {
      if (!url) return this.defaultAvatar
      // 若依的头像/profile路径已经是完整的，不需要再加
      if (url.startsWith('/profile')) {
        return process.env.VUE_APP_BASE_API + url
      }
      // 如果是http/https开头，直接返回
      if (url.startsWith('http')) {
        return url
      }
      // 兜底（如果用户头像是相对路径）
      if (url.startsWith('/')) {
        return process.env.VUE_APP_BASE_API + url
      }
      return url
    },

    // 加载帖子列表
    loadPosts() {
      forumApi.getPostList().then(response => {
        this.posts = response.data || []
        // 自动加载每个帖子的点赞和评论
        this.posts.forEach(post => {
          if (post.likeCount > 0) {
            this.loadLikes(post.postId)
          }
          if (post.commentCount > 0) {
            this.loadComments(post.postId)
          }
        })
      }).catch(error => {
        this.$message.error('加载帖子失败')
      })
    },

    // 加载点赞列表
    loadLikes(postId) {
      forumApi.getLikesByPostId(postId).then(response => {
        this.$set(this.likesMap, postId, response.data || [])
      }).catch(error => {
        console.warn(`加载点赞失败 (PostID: ${postId}):`, error)
      })
    },

    // 加载评论列表
    loadComments(postId) {
      forumApi.getCommentsByPostId(postId).then(response => {
        this.$set(this.commentsMap, postId, response.data || [])
      }).catch(error => {
        console.warn(`加载评论失败 (PostID: ${postId}):`, error)
      })
    },

    // 刷新帖子列表
    refreshPosts() {
      this.refreshLoading = true
      forumApi.refreshPosts().then(response => {
        this.posts = response.data || []
        this.posts.forEach(post => {
          if (post.likeCount > 0) {
            this.loadLikes(post.postId)
          }
          if (post.commentCount > 0) {
            this.loadComments(post.postId)
          }
        })
        this.$message.success('刷新成功')
      }).catch(error => {
        this.$message.error('刷新失败')
      }).finally(() => {
        this.refreshLoading = false
      })
    },

    // 处理点赞/取消点赞
    handleLike(post) {
      this.$set(this.likeLoading, post.postId, true)

      const action = post.isLiked ? forumApi.cancelLike : forumApi.likePost

      action(post.postId).then(response => {
        if (response.data) {
          // 更新UI
          post.isLiked = !post.isLiked
          post.likeCount += post.isLiked ? 1 : -1
          // 重新加载点赞列表
          this.loadLikes(post.postId)
        } else {
          this.$message.warning(post.isLiked ? '取消点赞失败' : '点赞失败')
        }
      }).catch(error => {
        this.$message.error('操作失败')
      }).finally(() => {
        this.$set(this.likeLoading, post.postId, false)
      })
    },

    // 显示评论弹窗
    showCommentDialog(post) {
      this.currentPostId = post.postId
      this.currentCommentParentId = 0
      this.currentReplyToUserId = null
      this.commentContent = ''
      this.commentDialogVisible = true
    },

    // 显示回复弹窗
    showReplyDialog(post, comment) {
      this.currentPostId = post.postId
      this.currentCommentParentId = comment.commentId
      this.currentReplyToUserId = comment.userId
      this.commentContent = ''
      this.commentDialogVisible = true
    },

    // 关闭评论弹窗
    closeCommentDialog() {
      this.commentDialogVisible = false
      this.commentContent = ''
    },

    // 提交评论
    submitComment() {
      const content = this.commentContent.trim()
      if (!content) return

      const comment = {
        postId: this.currentPostId,
        parentId: this.currentCommentParentId,
        replyToUserId: this.currentReplyToUserId,
        content: content
      }

      forumApi.addComment(comment).then(response => {
        if (response.data) {
          this.closeCommentDialog()
          // 重新加载评论并更新评论数
          this.loadComments(this.currentPostId)
          const post = this.posts.find(p => p.postId === this.currentPostId)
          if (post) {
            post.commentCount += 1
          }
          this.$message.success('评论成功')
        } else {
          this.$message.warning('评论失败')
        }
      }).catch(error => {
        this.$message.error('评论失败')
      })
    },

    // ---------------------------------
    // 发帖相关
    // ---------------------------------
    showPublishDialog() {
      this.publishDialogVisible = true
    },
    resetPublishForm() {
      this.publishForm.content = ''
      this.publishForm.imageFileList = []
      if (this.$refs.publishUpload) {
        this.$refs.publishUpload.clearFiles()
      }
    },
    handlePublishImageChange(file, fileList) {
      this.publishForm.imageFileList = fileList
    },
    submitPost() {
      this.publishLoading = true

      const postData = {
        content: this.publishForm.content
      }
      // files 列表
      const files = this.publishForm.imageFileList.map(f => f.raw)

      forumApi.publishPost(postData, files).then(response => {
        if (response.data) {
          this.$message.success('发布成功')
          this.publishDialogVisible = false
          this.refreshPosts() // 刷新列表
        } else {
          this.$message.warning('发布失败')
        }
      }).catch(error => {
        this.$message.error('发布失败')
      }).finally(() => {
        this.publishLoading = false
      })
    },

    // ---------------------------------
    // 新增：修改帖子相关
    // ---------------------------------
    showEditDialog(post) {
      this.editForm.postId = post.postId
      this.editForm.content = post.content
      // 将已有的图片URL转换为 el-upload 需要的格式
      this.editForm.imageFileList = (post.imageUrls || '').split(',')
        .filter(Boolean)
        .map(url => ({
          name: url, // 存储原始相对路径
          url: this.getImageUrl(url), // 完整的显示路径
          status: 'success', // 标记为已上传
          uid: url // 唯一标识
        }))
      this.editDialogVisible = true
    },
    resetEditForm() {
      this.editForm.postId = null
      this.editForm.content = ''
      this.editForm.imageFileList = []
      if (this.$refs.editUpload) {
        this.$refs.editUpload.clearFiles()
      }
    },
    handleEditImageChange(file, fileList) {
      // 当on-change 和 on-remove 触发时，都用 fileList 更新
      this.editForm.imageFileList = fileList
    },
    submitEdit() {
      this.editLoading = true

      // 1. 分离出需要保留的旧图片URL和新上传的文件
      const oldUrlsToKeep = []
      const newFilesToUpload = []

      this.editForm.imageFileList.forEach(file => {
        if (file.status === 'success') {
          // 'success' 状态的是我们初始化的旧图片
          oldUrlsToKeep.push(file.name) // name 存的是原始相对路径
        } else if (file.raw) {
          // 带有 raw 属性的是新选择的文件
          newFilesToUpload.push(file.raw)
        }
      })

      // 2. 准备提交的数据
      const postData = {
        postId: this.editForm.postId,
        content: this.editForm.content,
        imageUrls: oldUrlsToKeep.join(',') // 需要保留的旧图片URL列表
      }

      // 3. 调用API
      forumApi.updatePost(postData, newFilesToUpload).then(response => {
        if (response.data) {
          this.$message.success('修改成功')
          this.editDialogVisible = false
          // 刷新帖子列表（或只更新当前帖子）
          this.refreshPosts()
        } else {
          this.$message.warning('修改失败')
        }
      }).catch(error => {
        this.$message.error('修改失败')
      }).finally(() => {
        this.editLoading = false
      })
    },

    // ---------------------------------
    // 新增：删除帖子相关
    // ---------------------------------
    handleDelete(post) {
      this.$confirm('此操作将永久删除该帖子及其所有评论和点赞, 是否继续?', '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 执行删除
        forumApi.deletePost(post.postId).then(() => {
          this.$message.success('删除成功')
          // 从列表中移除
          const index = this.posts.findIndex(p => p.postId === post.postId)
          if (index > -1) {
            this.posts.splice(index, 1)
          }
        }).catch(error => {
          this.$message.error('删除失败')
        })
      }).catch(() => {
        // 取消
        this.$message.info('已取消删除')
      })
    },

    // ---------------------------------
    // 通知相关
    // ---------------------------------
    showNoticeDialog() {
      forumApi.getUserNotices().then(response => {
        this.notices = response.data || []
        this.noticeDialogVisible = true
      }).catch(error => {
        this.$message.error('加载通知失败')
      })
    },

    // ---------------------------------
    // 工具方法
    // ---------------------------------
    formatTime(time) {
      return formatDate(time, 'yyyy/MM/dd HH:mm')
    },
    handleImageError(event) {
      event.target.src = this.defaultAvatar
    },
    getLikeNames(postId) {
      const likes = this.likesMap[postId] || []
      return likes.map(like => like.nickName).join('，')
    },
    getComments(postId) {
      return this.commentsMap[postId] || []
    },
    // 为 el-image 预览生成列表
    getPreviewList(imageUrls, currentImg) {
      const fullList = (imageUrls || '').split(',')
        .filter(Boolean)
        .map(url => this.getImageUrl(url))

      // 切换当前预览的图片到第一张（体验更好）
      const currentIndex = fullList.indexOf(this.getImageUrl(currentImg))
      if (currentIndex > 0) {
        const current = fullList.splice(currentIndex, 1)
        return [...current, ...fullList]
      }
      return fullList
    }
  }
}
</script>

<style scoped>
/* Mac Style for Forum */
.forum-container {
  padding: 40px 20px;
  max-width: 900px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
}

/* Top Buttons */
.top-buttons {
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.top-buttons .el-button {
  border-radius: 980px;
  font-weight: 500;
  border: none;
  padding: 9px 20px;
  transition: all 0.2s ease;
}

.top-buttons .el-button--primary {
  background-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}

.top-buttons .el-button--primary:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

.top-buttons .el-button--success {
  background-color: #34c759;
  box-shadow: 0 2px 8px rgba(52, 199, 89, 0.2);
}

.top-buttons .el-button--info {
  background-color: #86868b;
  box-shadow: 0 2px 8px rgba(134, 134, 139, 0.2);
}

/* Post List */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.post-item {
  padding: 24px;
  background-color: #ffffff;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  border: none;
}

.post-content {
  display: flex;
  gap: 16px;
}

.avatar-container {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-main {
  flex-grow: 1;
}

.username {
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
  font-size: 16px;
}

.post-text {
  margin-bottom: 12px;
  line-height: 1.6;
  color: #1d1d1f;
  font-size: 15px;
  white-space: pre-wrap;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.image-item {
  width: calc(33.33% - 6px);
  aspect-ratio: 1/1;
  overflow: hidden;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.post-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f5f7;
  color: #86868b;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #86868b;
  font-size: 13px;
  margin-top: 16px;
}

.post-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.action-btn {
  color: #86868b;
  font-size: 13px;
  padding: 0;
  font-weight: 500;
}

.action-btn:hover {
  color: #0071e3;
}

.delete-btn:hover {
  color: #ff3b30;
}

.like-btn, .comment-btn {
  background-color: #f5f5f7;
  color: #1d1d1f;
  border: none;
  padding: 6px 14px;
  border-radius: 980px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.like-btn:hover, .comment-btn:hover {
  background-color: #e5e5ea;
}

.liked-btn {
  background-color: #ff2d55;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 980px;
  font-size: 13px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(255, 45, 85, 0.2);
}

.post-interactions {
  background-color: #f5f5f7;
  border-radius: 12px;
  padding: 16px;
  margin-top: 16px;
  margin-left: 64px;
}

.likes-list {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e5ea;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.likes-list:last-child {
  margin-bottom: 0;
  border-bottom: none;
}

.heart-icon {
  color: #ff2d55;
  font-size: 14px;
}

.like-names {
  color: #0071e3;
  font-size: 13px;
  font-weight: 500;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-item {
  padding: 4px 0;
  cursor: pointer;
  font-size: 14px;
  line-height: 1.5;
}

.comment-username {
  color: #0071e3;
  font-weight: 500;
}

.reply-to {
  color: #86868b;
}

.comment-colon {
  color: #86868b;
}

.comment-content {
  color: #1d1d1f;
}

.divider {
  display: none;
}

/* Dialog Styling */
.forum-container >>> .el-dialog {
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}

.forum-container >>> .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
}

.forum-container >>> .el-dialog__title {
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
  text-align: center;
  display: block;
}

.forum-container >>> .el-dialog__body {
  padding: 24px;
}

.forum-container >>> .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
}

.publish-textarea >>> .el-textarea__inner {
  border-radius: 12px;
  border: 1px solid #d2d2d7;
  padding: 12px;
  font-family: inherit;
}

.publish-textarea >>> .el-textarea__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

/* Notice List */
.notice-list {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 4px;
}

.notice-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
}

.notice-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
}

.notice-content {
  flex-grow: 1;
}

.notice-username {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 14px;
  color: #1d1d1f;
}

.notice-text {
  color: #1d1d1f;
  margin-bottom: 4px;
  font-size: 13px;
}

.notice-time {
  font-size: 12px;
  color: #86868b;
}

.no-notice {
  text-align: center;
  padding: 24px;
  color: #86868b;
}

/* Upload Styling */
.forum-container >>> .el-upload--picture-card {
  width: 100px;
  height: 100px;
  line-height: 100px;
  border-radius: 12px;
  border: 1px dashed #d2d2d7;
  background-color: #f5f5f7;
}

.forum-container >>> .el-upload--picture-card:hover {
  border-color: #0071e3;
  color: #0071e3;
}

.forum-container >>> .el-upload-list--picture-card .el-upload-list-item {
  width: 100px;
  height: 100px;
  border-radius: 12px;
}

/* Responsive */
@media (max-width: 768px) {
  .forum-container {
    padding: 20px 16px;
  }

  .post-item {
    padding: 20px;
  }

  .post-content {
    flex-direction: column;
  }

  .avatar-container {
    width: 40px;
    height: 40px;
  }

  .post-interactions {
    margin-left: 0;
  }

  .image-item {
    width: calc(50% - 4px);
  }
}
</style>
