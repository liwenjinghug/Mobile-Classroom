<template>
  <div class="forum-container">

    <!-- 1. 查询栏 (打印时隐藏) -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" class="search-form no-print">
      <el-form-item label="发帖人">
        <el-input
          v-model="queryParams.nickName"
          placeholder="请输入发帖人昵称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="帖子内容">
        <el-input
          v-model="queryParams.content"
          placeholder="请输入内容关键词"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 2. 工具栏 (打印时隐藏) -->
    <div class="toolbar no-print">
      <!-- 左侧：功能按钮 -->
      <div class="left-tools">
        <el-button type="warning" plain icon="el-icon-download" size="small" @click="handleExport">导出</el-button>
        <el-button type="danger" plain icon="el-icon-printer" size="small" @click="handlePrint">打印</el-button>
        <el-button type="success" plain icon="el-icon-s-data" size="small" @click="handleStats">统计</el-button>
      </div>
      <!-- 右侧：原有操作 -->
      <div class="right-tools">
        <el-button type="primary" size="small" @click="showNoticeDialog" icon="el-icon-bell">通知</el-button>
        <el-button type="success" size="small" @click="showPublishDialog" icon="el-icon-edit">发帖</el-button>
        <el-button type="info" size="small" @click="refreshPosts" icon="el-icon-refresh" :loading="refreshLoading">刷新</el-button>
      </div>
    </div>

    <!-- 3. 帖子列表 (显示分页后的数据) -->
    <div class="post-list no-print" v-loading="loading">
      <div v-if="filteredPosts.length === 0" class="no-data">
        <i class="el-icon-search"></i>
        <p>无匹配结果</p>
      </div>

      <div v-for="post in pagedPosts" :key="post.postId" class="post-item">
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

    <!-- 4. 打印专用表格 (平时隐藏) -->
    <div class="print-section">
      <h2 class="print-title">论坛帖子列表</h2>
      <table class="print-table">
        <thead>
        <tr>
          <th>发帖人</th>
          <th>内容</th>
          <th>发布时间</th>
          <th>点赞数</th>
          <th>评论数</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="post in filteredPosts" :key="post.postId">
          <td>{{ post.nickName }}</td>
          <td>{{ post.content }}</td>
          <td>{{ formatTime(post.createTime) }}</td>
          <td>{{ post.likeCount }}</td>
          <td>{{ post.commentCount }}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- 5. 分页组件 (打印时隐藏) -->
    <div class="pagination-wrapper no-print" v-if="filteredPosts.length > 0">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.currentPage"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredPosts.length"
        background
      >
      </el-pagination>
    </div>

    <!-- 弹窗组件保持不变 -->
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

    <!-- 6. 统计图表弹窗 -->
    <el-dialog title="发帖趋势统计" :visible.sync="statsVisible" width="800px" append-to-body>
      <div id="forum-chart" style="width: 100%; height: 400px;"></div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="statsVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import forumApi from '@/api/proj_qhy/forum'
import store from '@/store'
// 引入 ECharts
import * as echarts from 'echarts';

function formatDate(date, fmt) {
  if (!date) return ''
  let dateObj
  if (typeof date === 'string') {
    dateObj = new Date(date.replace(/-/g, '/'))
  } else if (date instanceof Date) {
    dateObj = date
  } else {
    dateObj = new Date(date)
  }
  if (isNaN(dateObj.getTime())) return ''
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
      loading: false,
      // 原始所有数据（用于前端筛选和统计）
      allPosts: [],
      // 点赞/评论缓存
      likesMap: {},
      commentsMap: {},
      defaultAvatar: require('@/assets/images/profile.jpg'),

      // 查询参数
      queryParams: {
        nickName: '',
        content: ''
      },

      // 分页参数
      pagination: {
        currentPage: 1,
        pageSize: 10
      },

      // 统计图表
      statsVisible: false,
      chartInstance: null,

      // 弹窗状态
      publishDialogVisible: false,
      publishForm: { content: '', imageFileList: [] },
      publishLoading: false,
      editDialogVisible: false,
      editForm: { postId: null, content: '', imageFileList: [] },
      editLoading: false,
      commentDialogVisible: false,
      commentContent: '',
      currentPostId: null,
      currentCommentParentId: null,
      currentReplyToUserId: null,
      noticeDialogVisible: false,
      notices: [],
      likeLoading: {},
      refreshLoading: false
    }
  },
  computed: {
    currentUserId() {
      return store.getters.userId
    },
    // 根据条件筛选后的所有帖子
    filteredPosts() {
      let result = this.allPosts;
      if (this.queryParams.nickName) {
        result = result.filter(post => post.nickName && post.nickName.includes(this.queryParams.nickName));
      }
      if (this.queryParams.content) {
        result = result.filter(post => post.content && post.content.includes(this.queryParams.content));
      }
      return result;
    },
    // 当前页显示的帖子
    pagedPosts() {
      const start = (this.pagination.currentPage - 1) * this.pagination.pageSize;
      const end = start + this.pagination.pageSize;
      return this.filteredPosts.slice(start, end);
    }
  },
  created() {
    this.loadPosts()
  },
  methods: {
    // 基础路径处理
    getImageUrl(url) {
      if (!url) return this.defaultAvatar
      if (url.startsWith('/profile')) return process.env.VUE_APP_BASE_API + url
      if (url.startsWith('http')) return url
      if (url.startsWith('/')) return process.env.VUE_APP_BASE_API + url
      return url
    },

    // 加载帖子列表
    loadPosts() {
      this.loading = true;
      forumApi.getPostList().then(response => {
        this.allPosts = response.data || []
        // 自动加载交互数据
        this.allPosts.forEach(post => {
          if (post.likeCount > 0) this.loadLikes(post.postId)
          if (post.commentCount > 0) this.loadComments(post.postId)
        })
        this.loading = false;
      }).catch(error => {
        this.$message.error('加载帖子失败')
        this.loading = false;
      })
    },

    // --- 查询操作 ---
    handleQuery() {
      this.pagination.currentPage = 1; // 搜索时重置到第一页
    },
    resetQuery() {
      this.queryParams.nickName = '';
      this.queryParams.content = '';
      this.handleQuery();
    },

    // --- 分页操作 ---
    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.pagination.currentPage = 1;
    },
    handleCurrentChange(val) {
      this.pagination.currentPage = val;
      // 翻页回到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    // --- 导出功能 ---
    handleExport() {
      if (this.filteredPosts.length === 0) {
        this.$message.warning("暂无数据可导出");
        return;
      }
      this.$confirm('确定导出当前查询到的数据吗？', '导出确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 构建CSV内容
        // BOM头，防止乱码
        let csvContent = "\uFEFF";
        // 表头
        csvContent += "发帖人,帖子内容,发布时间,点赞数,评论数\n";

        this.filteredPosts.forEach(item => {
          // 处理内容中的换行和逗号，防止格式错乱
          let safeContent = (item.content || '').replace(/"/g, '""').replace(/[\r\n]+/g, ' ');
          let row = [
            item.nickName,
            `"${safeContent}"`, // 用引号包裹内容
            formatDate(item.createTime, 'yyyy-MM-dd HH:mm:ss'),
            item.likeCount,
            item.commentCount
          ];
          csvContent += row.join(",") + "\n";
        });

        // 创建下载链接
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement("a");
        const url = URL.createObjectURL(blob);
        link.setAttribute("href", url);
        link.setAttribute("download", `论坛数据_${new Date().getTime()}.csv`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        this.$message.success("导出成功");
      });
    },

    // --- 打印功能 ---
    handlePrint() {
      window.print();
    },

    // --- 统计功能 ---
    handleStats() {
      this.statsVisible = true;
      this.$nextTick(() => {
        this.initChart();
      });
    },
    initChart() {
      if (this.chartInstance) {
        this.chartInstance.dispose();
      }
      const chartDom = document.getElementById('forum-chart');
      this.chartInstance = echarts.init(chartDom);

      // 1. 数据处理：按日期分组统计
      const dateMap = {};
      this.allPosts.forEach(post => {
        if (post.createTime) {
          const dateStr = formatDate(post.createTime, 'yyyy-MM-dd');
          if (dateStr) {
            dateMap[dateStr] = (dateMap[dateStr] || 0) + 1;
          }
        }
      });

      // 2. 排序日期
      const sortedDates = Object.keys(dateMap).sort();
      const counts = sortedDates.map(date => dateMap[date]);

      // 3. 配置图表
      const option = {
        title: { text: '发帖趋势统计', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: sortedDates,
          name: '日期'
        },
        yAxis: {
          type: 'value',
          name: '发帖数'
        },
        series: [{
          data: counts,
          type: 'line',
          smooth: true,
          itemStyle: { color: '#0071e3' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0, 113, 227, 0.5)' },
              { offset: 1, color: 'rgba(0, 113, 227, 0.1)' }
            ])
          }
        }]
      };

      this.chartInstance.setOption(option);
    },

    // --- 原有逻辑保留 ---
    loadLikes(postId) {
      forumApi.getLikesByPostId(postId).then(response => {
        this.$set(this.likesMap, postId, response.data || [])
      }).catch(error => { console.warn(error) })
    },
    loadComments(postId) {
      forumApi.getCommentsByPostId(postId).then(response => {
        this.$set(this.commentsMap, postId, response.data || [])
      }).catch(error => { console.warn(error) })
    },
    refreshPosts() {
      this.refreshLoading = true
      this.queryParams.nickName = ''
      this.queryParams.content = ''
      this.pagination.currentPage = 1
      this.loadPosts();
      setTimeout(() => {
        this.refreshLoading = false;
        this.$message.success('刷新成功');
      }, 500);
    },
    handleLike(post) {
      this.$set(this.likeLoading, post.postId, true)
      const action = post.isLiked ? forumApi.cancelLike : forumApi.likePost
      action(post.postId).then(response => {
        if (response.data) {
          post.isLiked = !post.isLiked
          post.likeCount += post.isLiked ? 1 : -1
          this.loadLikes(post.postId)
        } else {
          this.$message.warning(post.isLiked ? '取消点赞失败' : '点赞失败')
        }
      }).catch(error => { this.$message.error('操作失败') })
        .finally(() => { this.$set(this.likeLoading, post.postId, false) })
    },
    showCommentDialog(post) {
      this.currentPostId = post.postId
      this.currentCommentParentId = 0
      this.currentReplyToUserId = null
      this.commentContent = ''
      this.commentDialogVisible = true
    },
    showReplyDialog(post, comment) {
      this.currentPostId = post.postId
      this.currentCommentParentId = comment.commentId
      this.currentReplyToUserId = comment.userId
      this.commentContent = ''
      this.commentDialogVisible = true
    },
    closeCommentDialog() {
      this.commentDialogVisible = false
      this.commentContent = ''
    },
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
          this.loadComments(this.currentPostId)
          const post = this.allPosts.find(p => p.postId === this.currentPostId)
          if (post) post.commentCount += 1
          this.$message.success('评论成功')
        } else {
          this.$message.warning('评论失败')
        }
      }).catch(error => { this.$message.error('评论失败') })
    },
    showPublishDialog() {
      this.publishDialogVisible = true
    },
    resetPublishForm() {
      this.publishForm.content = ''
      this.publishForm.imageFileList = []
      if (this.$refs.publishUpload) this.$refs.publishUpload.clearFiles()
    },
    handlePublishImageChange(file, fileList) {
      this.publishForm.imageFileList = fileList
    },
    submitPost() {
      this.publishLoading = true
      const postData = { content: this.publishForm.content }
      const files = this.publishForm.imageFileList.map(f => f.raw)
      forumApi.publishPost(postData, files).then(response => {
        if (response.data) {
          this.$message.success('发布成功')
          this.publishDialogVisible = false
          this.refreshPosts()
        } else {
          this.$message.warning('发布失败')
        }
      }).catch(error => { this.$message.error('发布失败') })
        .finally(() => { this.publishLoading = false })
    },
    showEditDialog(post) {
      this.editForm.postId = post.postId
      this.editForm.content = post.content
      this.editForm.imageFileList = (post.imageUrls || '').split(',')
        .filter(Boolean)
        .map(url => ({
          name: url,
          url: this.getImageUrl(url),
          status: 'success',
          uid: url
        }))
      this.editDialogVisible = true
    },
    resetEditForm() {
      this.editForm.postId = null
      this.editForm.content = ''
      this.editForm.imageFileList = []
      if (this.$refs.editUpload) this.$refs.editUpload.clearFiles()
    },
    handleEditImageChange(file, fileList) {
      this.editForm.imageFileList = fileList
    },
    submitEdit() {
      this.editLoading = true
      const oldUrlsToKeep = []
      const newFilesToUpload = []
      this.editForm.imageFileList.forEach(file => {
        if (file.status === 'success') oldUrlsToKeep.push(file.name)
        else if (file.raw) newFilesToUpload.push(file.raw)
      })
      const postData = {
        postId: this.editForm.postId,
        content: this.editForm.content,
        imageUrls: oldUrlsToKeep.join(',')
      }
      forumApi.updatePost(postData, newFilesToUpload).then(response => {
        if (response.data) {
          this.$message.success('修改成功')
          this.editDialogVisible = false
          this.refreshPosts()
        } else {
          this.$message.warning('修改失败')
        }
      }).catch(error => { this.$message.error('修改失败') })
        .finally(() => { this.editLoading = false })
    },
    handleDelete(post) {
      this.$confirm('此操作将永久删除该帖子及其所有评论和点赞, 是否继续?', '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        forumApi.deletePost(post.postId).then(() => {
          this.$message.success('删除成功')
          this.loadPosts() // 重新加载以更新列表
        }).catch(error => { this.$message.error('删除失败') })
      }).catch(() => {})
    },
    showNoticeDialog() {
      forumApi.getUserNotices().then(response => {
        this.notices = response.data || []
        this.noticeDialogVisible = true
      }).catch(error => { this.$message.error('加载通知失败') })
    },
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
    getPreviewList(imageUrls, currentImg) {
      const fullList = (imageUrls || '').split(',').filter(Boolean).map(url => this.getImageUrl(url))
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

/* 搜索表单 */
.search-form {
  background: white;
  padding: 15px 20px 0 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.left-tools {
  display: flex;
  gap: 10px;
}

.right-tools {
  display: flex;
  gap: 10px;
}

/* 通用按钮圆角 */
.el-button {
  border-radius: 980px;
}

/* 无数据提示 */
.no-data {
  text-align: center;
  padding: 40px;
  color: #86868b;
}
.no-data i {
  font-size: 48px;
  margin-bottom: 10px;
}

/* 打印表格 (默认隐藏) */
.print-section {
  display: none;
}

/* 分页组件 */
.pagination-wrapper {
  margin-top: 30px;
  text-align: center;
  padding-bottom: 30px;
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

/* 打印样式 */
@media print {
  @page {
    size: auto;
    margin: 10mm;
  }

  /* 隐藏所有无关元素 */
  .no-print,
  .navbar,
  .sidebar-container,
  .tags-view-container,
  .el-dialog__wrapper,
  .v-modal {
    display: none !important;
  }

  /* 调整容器 */
  .forum-container {
    padding: 0;
    margin: 0;
    max-width: 100%;
    background: white;
  }

  .main-container {
    margin-left: 0 !important;
    width: 100% !important;
  }

  /* 显示打印表格 */
  .print-section {
    display: block !important;
  }

  .print-title {
    text-align: center;
    font-size: 24px;
    margin-bottom: 20px;
  }

  .print-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 10px;
  }

  .print-table th,
  .print-table td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: left;
    font-size: 14px;
  }

  .print-table th {
    background-color: #f2f2f2;
    font-weight: bold;
  }
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
