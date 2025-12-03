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
            <el-avatar
              :size="40"
              :src="getFullImageUrl(article.authorAvatar) || defaultAvatar"
              class="author-avatar"
            ></el-avatar>
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

    <!-- 分享到小组弹窗 -->
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
import groupApi from '@/api/proj_qhy/group'

export default {
  name: "ArticleDetail",
  data() {
    return {
      loading: true,
      article: {},
      likeLoading: false,
      hateLoading: false,
      defaultAvatar: '[https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png](https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png)',
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
    async loadArticle() {
      this.loading = true;
      try {
        const response = await getArticle(this.articleId);
        this.article = response.data;
        await increaseViewCount(this.articleId);
        this.article.viewCount = (this.article.viewCount || 0) + 1;
        document.title = `${this.article.title} - 学习社区`;
      } catch (error) {
        console.error('加载文章失败:', error);
        this.$modal.msgError('加载文章失败');
      } finally {
        this.loading = false;
      }
    },
    getFullImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http')) return url;
      return process.env.VUE_APP_BASE_API + url;
    },
    goBack() {
      this.$router.push({
        path: '/proj_qhy/article',
        query: { refresh: true }
      });
    },
    async handleLike() {
      this.likeLoading = true;
      try {
        const response = await likeArticle(this.articleId);
        if (response.code === 200) {
          const oldStatus = this.article.userLikeStatus || 0;
          if (oldStatus === 1) {
            this.article.userLikeStatus = 0;
            this.article.likeCount = Math.max(0, (this.article.likeCount || 0) - 1);
          } else {
            this.article.userLikeStatus = 1;
            this.article.likeCount = (this.article.likeCount || 0) + 1;
            if (oldStatus === -1) {
              this.article.hateCount = Math.max(0, (this.article.hateCount || 0) - 1);
            }
          }
        }
      } catch (error) {
      } finally {
        this.likeLoading = false;
      }
    },
    async handleHate() {
      this.hateLoading = true;
      try {
        const response = await hateArticle(this.articleId);
        if (response.code === 200) {
          const oldStatus = this.article.userLikeStatus || 0;
          if (oldStatus === -1) {
            this.article.userLikeStatus = 0;
            this.article.hateCount = Math.max(0, (this.article.hateCount || 0) - 1);
          } else {
            this.article.userLikeStatus = -1;
            this.article.hateCount = (this.article.hateCount || 0) + 1;
            if (oldStatus === 1) {
              this.article.likeCount = Math.max(0, (this.article.likeCount || 0) - 1);
            }
          }
        }
      } catch (error) {
      } finally {
        this.hateLoading = false;
      }
    },
    async handleShare() {
      this.shareLoading = true;
      this.groupList = [];
      this.selectedGroupIds = [];
      try {
        const res = await groupApi.getGroupList();
        this.groupList = res.data.filter(g => g.memberStatus === '0');
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
        const firstGroupId = this.selectedGroupIds[0];
        this.$router.push({
          path: `/proj_qhy/group/chat/${firstGroupId}`,
          query: {
            shareSuccess: 'true',
            returnToPath: this.$route.path
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

/* --- (新增) 字体样式支持 --- */
.content-html ::v-deep .ql-font-SimSun {
  font-family: "SimSun";
}
.content-html ::v-deep .ql-font-SimHei {
  font-family: "SimHei";
}
.content-html ::v-deep .ql-font-Microsoft-YaHei {
  font-family: "Microsoft YaHei";
}
.content-html ::v-deep .ql-font-KaiTi {
  font-family: "KaiTi";
}
.content-html ::v-deep .ql-font-FangSong {
  font-family: "FangSong";
}

/* (原有) 字号样式 */
.content-html ::v-deep .ql-size-small {
  font-size: 0.75em;
}
.content-html ::v-deep .ql-size-large {
  font-size: 1.5em;
}
.content-html ::v-deep .ql-size-huge {
  font-size: 2.5em;
}

/* (原有) 对齐样式 */
.content-html ::v-deep .ql-align-center {
  text-align: center;
}
.content-html ::v-deep .ql-align-right {
  text-align: right;
}
.content-html ::v-deep .ql-align-justify {
  text-align: justify;
}

/* (原有) 链接样式 */
.content-html ::v-deep a {
  color: #409EFF;
  text-decoration: underline;
  font-weight: 600;
  cursor: pointer;
}
.content-html ::v-deep a:hover {
  color: #66b1ff;
}

/* (原有) 代码块样式 */
/* --- (修改) 代码块样式 --- */
.content-html ::v-deep .ql-code-block-container {
  background-color: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 6px;
  padding: 10px 15px; /* 容器内边距 */
  margin-bottom: 16px;
  font-family: Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  overflow: auto;
  color: #24292e;
}

/* 关键修复：强制去除每一行代码块的默认间距 */
.content-html ::v-deep .ql-code-block {
  margin: 0 !important;
  padding: 0 !important;
  border: none !important;
  background-color: transparent !important;
}

/* ... (保留其他样式) */
.content-html ::v-deep h1 { font-size: 24px; margin: 24px 0 16px; border-bottom: 1px solid #eaecef; padding-bottom: 8px; }
.content-html ::v-deep h2 { font-size: 20px; margin: 20px 0 12px; }
.content-html ::v-deep h3 { font-size: 18px; margin: 16px 0 8px; }
.content-html ::v-deep p { margin-bottom: 16px; }
.content-html ::v-deep img { max-width: 100%; height: auto; border-radius: 4px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }

.article-actions { padding: 0 30px 30px; }
.actions-card { text-align: center; border: none; }
.action-buttons { display: flex; justify-content: center; gap: 30px; }
.action-buttons .el-button { min-width: 120px; height: 44px; font-size: 16px; }
.liked { background-color: #f56c6c; border-color: #f56c6c; color: white; }
.hated { background-color: #909399; border-color: #909399; color: white; }
.group-share-list { max-height: 300px; overflow-y: auto; }
.group-share-item { display: flex; align-items: center; width: 100%; padding: 8px 0; }
.group-share-name { margin-left: 10px; }
.group-share-item ::v-deep .el-checkbox__label { display: flex; align-items: center; }

@media (max-width: 768px) {
  .article-detail-container { padding: 10px; }
  .article-meta { padding: 20px; }
  .article-title { font-size: 22px; }
  .article-info { flex-direction: column; align-items: flex-start; gap: 16px; }
  .article-stats { width: 100%; justify-content: space-around; }
  .action-buttons { flex-wrap: wrap; gap: 16px; }
  .publish-info { flex-direction: column; gap: 4px; }
  .article-cover, .article-digest, .article-body, .article-actions { padding-left: 20px; padding-right: 20px; }
}
</style>
