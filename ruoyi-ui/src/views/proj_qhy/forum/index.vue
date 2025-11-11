<template>
  <div class="forum-container">
    <!-- 顶部按钮区 -->
    <div class="top-buttons">
      <el-button type="primary" @click="showNoticeDialog" icon="el-icon-bell">通知</el-button>
      <el-button type="success" @click="showPublishDialog" icon="el-icon-edit">发帖</el-button>
      <el-button type="info" @click="refreshPosts" icon="el-icon-refresh" :loading="refreshLoading">刷新</el-button>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list">
      <div v-for="post in posts" :key="post.postId" class="post-item">
        <!-- 帖子内容 -->
        <div class="post-content">
          <!-- 头像 -->
          <div class="avatar-container">
            <img
              :src="post.avatar || defaultAvatar"
              alt="用户头像"
              class="avatar"
            >
          </div>

          <!-- 帖子主体 -->
          <div class="post-main">
            <!-- 用户名 -->
            <div class="username">{{ post.nickName }}</div>

            <!-- 帖子文字内容 -->
            <div class="post-text">{{ post.content }}</div>

            <!-- 帖子图片 -->
            <div v-if="post.imageUrls && post.imageUrls.trim()" class="post-images">
              <div
                v-for="(img, index) in post.imageUrls.split(',')"
                :key="index"
                class="image-item"
                v-if="img.trim()"
              >
                <img :src="img" alt="帖子图片" class="post-img" @error="handleImageError($event, img)">
              </div>
            </div>

            <!-- 发布时间和操作按钮 -->
            <div class="post-footer">
              <span class="post-time">{{ formatTime(post.createTime) }}</span>
              <div class="post-actions">
                <!-- 点赞按钮 -->
                <el-button
                  :class="post.isLiked ? 'liked-btn' : 'like-btn'"
                  @click="handleLike(post)"
                  :loading="likeLoading"
                >
                  {{ post.isLiked ? '已赞' : '点赞' }}
                </el-button>

                <!-- 评论按钮 -->
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

        <!-- 点赞和评论区 -->
        <div class="post-interactions">
          <!-- 点赞列表 -->
          <div v-if="post.likeCount > 0" class="likes-list">
            <span class="heart-icon">♡</span>
            <span class="like-names">
              {{ getLikeNames(post.postId) }}
            </span>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
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

        <!-- 分隔线 -->
        <div class="divider"></div>
      </div>
    </div>

    <!-- 发帖弹窗 -->
    <el-dialog
      title="发布帖子"
      :visible.sync="publishDialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-input
        type="textarea"
        v-model="publishContent"
        placeholder="请输入帖子内容"
        rows="6"
        class="publish-textarea"
      ></el-input>
      <el-upload
        class="upload-images"
        action=""
        :auto-upload="false"
        :on-change="handleImageChange"
        :file-list="imageFileList"
        list-type="picture-preview"
      >
        <el-button size="small" type="primary">选择图片</el-button>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button
          type="success"
          @click="submitPost"
          :disabled="!publishContent.trim()"
        >
          发布
        </el-button>
      </div>
    </el-dialog>

    <!-- 评论弹窗 -->
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

    <!-- 通知弹窗 -->
    <el-dialog
      title="通知"
      :visible.sync="noticeDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <div v-if="notices.length === 0" class="no-notice">暂无通知</div>
      <div v-else class="notice-list">
        <div
          v-for="notice in notices"
          :key="notice.noticeId"
          class="notice-item"
        >
          <div class="notice-avatar">
            <img
              :src="notice.operatorAvatar || defaultAvatar"
              alt="用户头像"
              class="avatar"
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
import { Loading } from 'element-ui'

// 时间格式化函数
function formatDate(date, fmt) {
  if (typeof date === 'string') {
    date = new Date(date.replace(/-/g, '/'))
  }
  if (isNaN(date.getTime())) {
    return ''
  }

  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }

  const o = {
    'M+': date.getMonth() + 1,
    'd+': date.getDate(),
    'H+': date.getHours(),
    'm+': date.getMinutes(),
    's+': date.getSeconds()
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
      publishContent: '',
      imageFileList: [],

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
      likeLoading: false,
      refreshLoading: false
    }
  },
  created() {
    this.loadPosts()
  },
  methods: {
    // 加载帖子列表
    loadPosts() {
      forumApi.getPostList().then(response => {
        this.posts = response.data || []
        this.posts.forEach(post => {
          this.loadLikes(post.postId)
          this.loadComments(post.postId)
        })
      }).catch(error => {
        this.$message.error('加载帖子失败：' + (error.message || '网络错误'))
      })
    },

    // 加载点赞列表
    loadLikes(postId) {
      forumApi.getLikesByPostId(postId).then(response => {
        this.$set(this.likesMap, postId, response.data || [])
      }).catch(error => {
        console.error('加载点赞失败：', error)
      })
    },

    // 加载评论列表
    loadComments(postId) {
      forumApi.getCommentsByPostId(postId).then(response => {
        this.$set(this.commentsMap, postId, response.data || [])
      }).catch(error => {
        console.error('加载评论失败：', error)
      })
    },

    // 刷新帖子列表
    refreshPosts() {
      this.refreshLoading = true
      forumApi.refreshPosts().then(response => {
        this.posts = response.data || []
        this.posts.forEach(post => {
          this.loadLikes(post.postId)
          this.loadComments(post.postId)
        })
        this.$message.success('刷新成功')
      }).catch(error => {
        this.$message.error('刷新失败：' + (error.message || '网络错误'))
      }).finally(() => {
        this.refreshLoading = false
      })
    },

    // 处理点赞/取消点赞
    handleLike(post) {
      if (this.likeLoading) return
      this.likeLoading = true

      if (post.isLiked) {
        // 取消点赞
        forumApi.cancelLike(post.postId).then(response => {
          if (response.data) {
            post.isLiked = false
            post.likeCount = Math.max(0, post.likeCount - 1)
            this.loadLikes(post.postId)
          } else {
            this.$message.warning('取消点赞失败')
          }
        }).catch(error => {
          this.$message.error('操作失败：' + (error.message || '网络错误'))
        }).finally(() => {
          this.likeLoading = false
        })
      } else {
        // 点赞
        forumApi.likePost(post.postId).then(response => {
          if (response.data) {
            post.isLiked = true
            post.likeCount += 1
            this.loadLikes(post.postId)
          } else {
            this.$message.warning('点赞失败，可能已点赞')
          }
        }).catch(error => {
          this.$message.error('操作失败：' + (error.message || '网络错误'))
        }).finally(() => {
          this.likeLoading = false
        })
      }
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
      if (!content) {
        this.$message.warning('评论内容不能为空')
        return
      }

      const comment = {
        postId: this.currentPostId,
        parentId: this.currentCommentParentId,
        replyToUserId: this.currentReplyToUserId,
        content: content
      }

      forumApi.addComment(comment).then(response => {
        if (response.data) {
          this.closeCommentDialog()
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
        this.$message.error('评论失败：' + (error.message || '网络错误'))
      })
    },

    // 显示发布弹窗
    showPublishDialog() {
      this.publishContent = ''
      this.imageFileList = []
      this.publishDialogVisible = true
    },

    // 处理图片选择
    handleImageChange(file, fileList) {
      this.imageFileList = fileList.filter(f => !f.removed)
    },

    // 提交发布帖子
    submitPost() {
      const content = this.publishContent.trim()
      if (!content) {
        this.$message.warning('帖子内容不能为空')
        return
      }

      const files = this.imageFileList.map(file => file.raw)

      forumApi.publishPost({ content: content }, files).then(response => {
        if (response.data) {
          this.publishDialogVisible = false
          this.refreshPosts()
          this.$message.success('发布成功')
        } else {
          this.$message.warning('发布失败')
        }
      }).catch(error => {
        this.$message.error('发布失败：' + (error.message || '网络错误'))
      })
    },

    // 显示通知弹窗
    showNoticeDialog() {
      forumApi.getUserNotices().then(response => {
        this.notices = response.data || []
        this.noticeDialogVisible = true
      }).catch(error => {
        this.$message.error('加载通知失败：' + (error.message || '网络错误'))
      })
    },

    // 格式化时间
    formatTime(time) {
      return formatDate(time, 'yyyy/MM/dd HH:mm')
    },

    // 图片加载失败处理
    handleImageError(event, imgUrl) {
      console.error(`图片加载失败: ${imgUrl}`)
      // 可以设置一张默认错误图片
      event.target.src = this.defaultAvatar
    },

    // 获取点赞用户名称
    getLikeNames(postId) {
      const likes = this.likesMap[postId] || []
      return likes.map(like => like.nickName).join('，')
    },

    // 获取评论列表
    getComments(postId) {
      return this.commentsMap[postId] || []
    }
  }
}
</script>

<style scoped>
.forum-container {
  padding: 20px;
  background-color: #fff;
  min-height: calc(100vh - 140px);
}

/* 顶部按钮 */
.top-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

/* 帖子列表 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-item {
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  background-color: #fff;
}

/* 帖子内容 */
.post-content {
  display: flex;
  gap: 10px;
}

.avatar-container {
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 50%;
  border: 1px solid #eee;
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
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 5px;
  font-size: 14px;
}

.post-text {
  margin-bottom: 10px;
  line-height: 1.6;
  color: #333;
  font-size: 14px;
}

/* 帖子图片 */
.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 10px;
}

.image-item {
  width: calc(20% - 4px);
  aspect-ratio: 1/1;
  overflow: hidden;
  border-radius: 4px;
}

.post-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s;
}

.post-img:hover {
  transform: scale(1.05);
}

/* 帖子底部 */
.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #888;
  font-size: 12px;
}

.post-actions {
  display: flex;
  gap: 10px;
}

.like-btn {
  background-color: #f5f5f5;
  color: #333;
  border: none;
  padding: 4px 12px;
  border-radius: 4px;
}

.liked-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 4px;
}

.comment-btn {
  background-color: #f5f5f5;
  color: #333;
  border: none;
  padding: 4px 12px;
  border-radius: 4px;
}

/* 互动区域 */
.post-interactions {
  background-color: #f0f7ff;
  border-radius: 4px;
  padding: 10px;
  margin-top: 10px;
  margin-left: 60px;
}

/* 点赞列表 */
.likes-list {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
}

.heart-icon {
  color: #f56c6c;
  font-size: 14px;
}

.like-names {
  color: #666;
  font-size: 13px;
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-item {
  padding: 5px 0;
  border-bottom: 1px solid #e6f7ff;
  cursor: pointer;
  font-size: 13px;
}

.comment-item:hover {
  background-color: rgba(255,255,255,0.5);
}

.comment-username {
  color: #1890ff;
  font-weight: 500;
}

.reply-to {
  color: #666;
}

.comment-colon {
  color: #666;
}

.comment-content {
  color: #333;
}

/* 分隔线 */
.divider {
  height: 1px;
  background-color: #eee;
  margin: 15px 0;
}

/* 弹窗样式 */
.publish-textarea {
  margin-bottom: 15px;
  width: 100%;
}

.upload-images {
  margin-bottom: 10px;
}

/* 通知样式 */
.notice-list {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}

.notice-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.notice-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.notice-content {
  flex-grow: 1;
}

.notice-username {
  font-weight: 500;
  margin-bottom: 3px;
  font-size: 14px;
}

.notice-text {
  color: #333;
  margin-bottom: 3px;
  font-size: 13px;
}

.notice-time {
  font-size: 12px;
  color: #888;
}

.no-notice {
  text-align: center;
  padding: 20px;
  color: #888;
}

/* 适配小屏幕 */
@media (max-width: 768px) {
  .image-item {
    width: calc(33.33% - 4px);
  }
}
</style>
