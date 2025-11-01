<template>
  <div class="article-detail-container">
    <div class="article-header">
      <el-page-header @back="goBack" content="文章详情"></el-page-header>
    </div>

    <div class="article-content" v-loading="loading">
      <!-- 文章头部信息 -->
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

      <!-- 封面图片 -->
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

      <!-- 文章摘要 -->
      <div class="article-digest" v-if="article.digest">
        <el-card shadow="never" class="digest-card">
          <div class="digest-content">
            <i class="el-icon-info digest-icon"></i>
            <span>{{ article.digest }}</span>
          </div>
        </el-card>
      </div>

      <!-- 文章内容 -->
      <div class="article-body">
        <el-card shadow="never" class="content-card">
          <div class="content-html" v-html="article.content"></div>
        </el-card>
      </div>

      <!-- 互动操作区域 -->
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
              size="large"
            >
              <i class="el-icon-share"></i>
              分享
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { getArticle, increaseViewCount, likeArticle, hateArticle } from "@/api/proj_qhy/article";

export default {
  name: "ArticleDetail",
  data() {
    return {
      loading: true,
      article: {},
      likeLoading: false,
      hateLoading: false,
      authorAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
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

    // 返回文章列表 - 刷新页面
    goBack() {
      this.$router.push({
        path: '/proj_qhy/article',
        query: { refresh: true }
      });
    },

    // 点赞处理
    async handleLike() {
      this.likeLoading = true;
      try {
        const response = await likeArticle(this.articleId);
        if (response.code === 200) {
          // 更新点赞状态和数量
          const oldStatus = this.article.userLikeStatus || 0;

          if (oldStatus === 1) {
            // 取消点赞
            this.article.userLikeStatus = 0;
            this.article.likeCount = Math.max(0, (this.article.likeCount || 0) - 1);
          } else {
            // 点赞
            this.article.userLikeStatus = 1;
            this.article.likeCount = (this.article.likeCount || 0) + 1;
            // 如果之前点踩了，取消点踩
            if (oldStatus === -1) {
              this.article.hateCount = Math.max(0, (this.article.hateCount || 0) - 1);
            }
          }
        }
      } catch (error) {
        console.error('点赞失败:', error);
      } finally {
        this.likeLoading = false;
      }
    },

    // 点踩处理
    async handleHate() {
      this.hateLoading = true;
      try {
        const response = await hateArticle(this.articleId);
        if (response.code === 200) {
          // 更新点踩状态和数量
          const oldStatus = this.article.userLikeStatus || 0;

          if (oldStatus === -1) {
            // 取消点踩
            this.article.userLikeStatus = 0;
            this.article.hateCount = Math.max(0, (this.article.hateCount || 0) - 1);
          } else {
            // 点踩
            this.article.userLikeStatus = -1;
            this.article.hateCount = (this.article.hateCount || 0) + 1;
            // 如果之前点赞了，取消点赞
            if (oldStatus === 1) {
              this.article.likeCount = Math.max(0, (this.article.likeCount || 0) - 1);
            }
          }
        }
      } catch (error) {
        console.error('点踩失败:', error);
      } finally {
        this.hateLoading = false;
      }
    },

    // 分享处理
    handleShare() {
      this.$modal.msgSuccess('分享功能开发中');
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

.content-html ::v-deep h1 {
  font-size: 24px;
  margin: 24px 0 16px;
  color: #1f2d3d;
  border-bottom: 1px solid #eaecef;
  padding-bottom: 8px;
}

.content-html ::v-deep h2 {
  font-size: 20px;
  margin: 20px 0 12px;
  color: #1f2d3d;
}

.content-html ::v-deep h3 {
  font-size: 18px;
  margin: 16px 0 8px;
  color: #1f2d3d;
}

.content-html ::v-deep p {
  margin-bottom: 16px;
}

.content-html ::v-deep img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

/* 响应式设计 */
@media (max-width: 768px) {
  .article-detail-container {
    padding: 10px;
  }

  .article-meta {
    padding: 20px;
  }

  .article-title {
    font-size: 22px;
  }

  .article-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .article-stats {
    width: 100%;
    justify-content: space-around;
  }

  .action-buttons {
    flex-wrap: wrap;
    gap: 16px;
  }

  .publish-info {
    flex-direction: column;
    gap: 4px;
  }

  .article-cover,
  .article-digest,
  .article-body,
  .article-actions {
    padding-left: 20px;
    padding-right: 20px;
  }
}
</style>
