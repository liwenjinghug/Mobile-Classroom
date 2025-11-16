<template>
  <div class="article-detail-container">
    <div class="article-header">
      <el-page-header @back="goBack" content="文章详情"></el-page-header>
    </div>

    <div class="article-content" v-loading="loading">
      <div class="article-meta">
        <h1 class="article-title">{{ article.title }}</h1>

        <div class="article-info">
          <div class="author-info">
            <el-avatar :size="40" :src="authorAvatar" class="author-avatar"></el-avatar>
            <div class="author-details">
              <span class="author-name">{{ article.author || '未知作者' }}</span>
              <div class="publish-info">
                <span class="publish-time">发布时间：{{ parseTime(article.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
                <span class="article-type">分类：{{ article.articleType || '未分类' }}</span>
              </div>
            </div>
          </div>

          <div class="article-stats">
            <div class="stat-item">
              <i class="el-icon-view"></i>
              <span>{{ article.viewCount || 0 }} 阅读</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-thumb"></i>
              <span>{{ article.likeCount || 0 }} 点赞</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-thumb"></i>
              <span>{{ article.hateCount || 0 }} 点踩</span>
            </div>
          </div>
        </div>
      </div>

      <div class="article-cover" v-if="article.cover">
        <el-image
          :src="getFullImageUrl(article.cover)"
          fit="cover"
          class="cover-image"
          :preview-src-list="[getFullImageUrl(article.cover)]"
        >
          <div slot="error" class="image-slot">
            <i class="el-icon-picture-outline"></i>
          </div>
        </el-image>
      </div>

      <div class="article-digest" v-if="article.digest">
        <el-card shadow="never" class="digest-card">
          <div class="digest-content">
            <i class="el-icon-info digest-icon"></i>
            <span>{{ article.digest }}</span>
          </div>
        </el-card>
      </div>

      <div class="article-body">
        <el-card shadow="never" class="content-card">
          <div class="content-html" v-html="article.content"></div>
        </el-card>
      </div>

      <div class="article-actions">
        <el-card shadow="never" class="actions-card">
          <div class="action-buttons">
            <el-button
              type="primary"
              :class="{ 'liked': article.userLikeStatus === 1 }"
              @click="handleLike"
              :loading="likeLoading"
              size="large"
            >
              赞 {{ article.likeCount || 0 }}
            </el-button>

            <el-button
              type="info"
              :class="{ 'hated': article.userLikeStatus === -1 }"
              @click="handleHate"
              :loading="hateLoading"
              size="large"
            >
              踩 {{ article.hateCount || 0 }}
            </el-button>

            <el-button
              type="success"
              @click="handleShare"
              :loading="shareLoading"
              size="large"
            >
              <i class="el-icon-share"></i>
              分享
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <el-dialog
      title="分享到小组"
      :visible.sync="shareModalVisible"
      width="400px"
      append-to-body
    >
      <div class="group-share-list">
        <el-checkbox-group v-model="selectedGroupIds">
          <el-checkbox
            v-for="group in groupList"
            :key="group.groupId"
            :label="group.groupId"
            class="group-share-item"
          >
            <el-avatar :size="30" :src="getFullImageUrl(group.avatar)" />
            <span class="group-share-name">{{ group.groupName }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="shareModalVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmitShare">分享</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { getArticle, increaseViewCount, likeArticle, hateArticle } from "@/api/proj_qhy/article";
import groupApi from '@/api/proj_qhy/group' // <-- (引入 groupApi)

export default {
  name: "ArticleDetail",
  data() {
    return {
      loading: true,
      article: {},
      likeLoading: false,
      hateLoading: false,
      authorAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',

      // (分享弹窗所需数据)
      shareModalVisible: false,
      shareLoading: false,
      groupList: [],
      selectedGroupIds: []
    };
  },
  computed: {
    articleId() {
      return this.$route.params.id;
    }
  },
  created() {
    this.loadArticle();
  },
  methods: {
    // 加载文章详情
    async loadArticle() {
      this.loading = true;
      try {
        const response = await getArticle(this.articleId);
        this.article = response.data;
        console.log('文章详情数据:', this.article);

        // 增加阅读数
        await increaseViewCount(this.articleId);

        // 更新阅读数显示
        this.article.viewCount = (this.article.viewCount || 0) + 1;

        // 设置页面标题
        document.title = `${this.article.title} - 学习社区`;
      } catch (error) {
        console.error('加载文章失败:', error);
        this.$modal.msgError('加载文章失败');
      } finally {
        this.loading = false;
      }
    },

    // 获取完整图片URL
    getFullImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http')) return url;
      // 如果是相对路径，添加基础URL
      return process.env.VUE_APP_BASE_API + url;
    },

    // 返回文章列表
    goBack() {
      this.$router.push({
        path: '/proj_qhy/article',
        query: { refresh: true }
      });
    },

    // 点赞处理
    async handleLike() {
      // ... (点赞逻辑不变)
    },

    // 点踩处理
    async handleHate() {
      // ... (点踩逻辑不变)
    },

    // (修改) 分享处理
    async handleShare() {
      this.shareLoading = true;
      this.groupList = [];
      this.selectedGroupIds = [];
      try {
        const res = await groupApi.getGroupList();
        this.groupList = res.data.filter(g => g.memberStatus === '0'); // 只显示活跃的小组
        if (this.groupList.length === 0) {
          this.$modal.msgWarning("您还没有加入任何活跃的小组");
          return;
        }
        this.shareModalVisible = true;
      } catch (error) {
        this.$modal.msgError("获取小组列表失败");
      } finally {
        this.shareLoading = false;
      }
    },

    // (新增) 提交分享
    async handleSubmitShare() {
      if (this.selectedGroupIds.length === 0) {
        this.$modal.msgWarning("请至少选择一个小组");
        return;
      }

      const payload = {
        articleId: this.articleId,
        groupIds: this.selectedGroupIds
      };

      try {
        await groupApi.shareArticle(payload);
        this.shareModalVisible = false;

        // 跳转到第一个选择的小组，并带上 "分享成功" 的标记
        const firstGroupId = this.selectedGroupIds[0];
        this.$router.push({
          path: `/proj_qhy/group/chat/${firstGroupId}`,
          query: {
            shareSuccess: 'true',
            returnToPath: this.$route.path // 告诉聊天室返回到哪
          }
        });

      } catch (error) {
        this.$modal.msgError("分享失败");
      }
    }
  }
};
</script>

<style scoped>
.article-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.article-header {
  margin-bottom: 20px;
}

.article-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.article-meta {
  padding: 30px;
  border-bottom: 1px solid #f0f0f0;
}

.article-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 20px;
  line-height: 1.4;
  text-align: center;
}

.article-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  background: #409eff;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 16px;
  font-weight: 500;
  color: #1f2d3d;
  margin-bottom: 4px;
}

.publish-info {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.article-type {
  color: #409eff;
}

.article-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.article-cover {
  padding: 20px 30px;
}

.cover-image {
  width: 100%;
  max-height: 400px;
  border-radius: 8px;
  overflow: hidden;
}

.image-slot {
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 32px;
}

.article-digest {
  padding: 0 30px 20px;
}

.digest-card {
  border-left: 4px solid #409eff;
  background: #f8f9fa;
}

.digest-content {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.digest-icon {
  color: #409eff;
  font-size: 16px;
  margin-top: 2px;
  flex-shrink: 0;
}

.article-body {
  padding: 0 30px 30px;
}

.content-card {
  min-height: 200px;
  border: none;
}

.content-html {
  line-height: 1.8;
  font-size: 16px;
  color: #2c3e50;
  padding: 10px;
}

/* (v-html 样式不变) */
.content-html ::v-deep h1,
.content-html ::v-deep h2,
.content-html ::v-deep h3,
.content-html ::v-deep p,
.content-html ::v-deep img {
  /* (保留您已有的 v-html 样式) */
}


.article-actions {
  padding: 0 30px 30px;
}

.actions-card {
  text-align: center;
  border: none;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.action-buttons .el-button {
  min-width: 120px;
  height: 44px;
  font-size: 16px;
}

.liked {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.hated {
  background-color: #909399;
  border-color: #909399;
  color: white;
}

/* (新增) 分享弹窗样式 */
.group-share-list {
  max-height: 300px;
  overflow-y: auto;
}
.group-share-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 8px 0;
}
.group-share-name {
  margin-left: 10px;
}
.group-share-item ::v-deep .el-checkbox__label {
  display: flex;
  align-items: center;
}

/* (响应式样式不变) */
@media (max-width: 768px) {
  /* (保留您已有的响应式样式) */
}
</style>
