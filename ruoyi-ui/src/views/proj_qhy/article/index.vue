<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
      <el-form-item label="文章标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入文章标题"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文章分类" prop="articleType">
        <el-select v-model="queryParams.articleType" placeholder="文章分类" clearable style="width: 240px">
          <el-option label="技术" value="技术" />
          <el-option label="生活" value="生活" />
          <el-option label="思考" value="思考" />
          <el-option label="读书" value="读书" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="文章状态" clearable style="width: 240px">
          <el-option label="编辑中" value="editting" />
          <el-option label="已发布" value="published" />
          <el-option label="草稿" value="draft" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <template v-if="!isSelectionMode">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="el-icon-plus"
            @click="handleAdd"
            v-hasPermi="['proj_qhy:article:add']"
          >新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="warning"
            plain
            icon="el-icon-download"
            @click="handleExport"
            v-hasPermi="['proj_qhy:article:export']"
          >导出Excel</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="info"
            plain
            icon="el-icon-check"
            @click="toggleSelectionMode"
          >选择</el-button>
        </el-col>
      </template>

      <template v-else>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="el-icon-delete"
            :disabled="selectedIds.length === 0"
            @click="handleDelete"
            v-hasPermi="['proj_qhy:article:remove']"
          >批量删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="warning"
            plain
            icon="el-icon-document"
            :disabled="selectedIds.length === 0"
            @click="handleExportPdf"
            v-hasPermi="['proj_qhy:article:export']"
          >导出PDF</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="info"
            plain
            icon="el-icon-close"
            @click="toggleSelectionMode"
          >取消选择</el-button>
        </el-col>
      </template>
    </el-row>

    <el-checkbox-group v-model="selectedIds">
      <div class="article-list" v-loading="loading">
        <div
          class="article-item"
          v-for="article in articleList"
          :key="article.id"
          @click="handleArticleClick(article)"
        >
          <el-checkbox
            v-if="isSelectionMode"
            :label="article.id"
            @click.native.stop
            class="article-checkbox"
          >&nbsp;</el-checkbox> <div class="article-cover">
          <el-image
            :src="getFullImageUrl(article.cover)"
            fit="cover"
            class="cover-image"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </div>

          <div class="article-content">
            <div class="article-header">
              <div class="article-title-section">
                <h3 class="article-title">{{ article.title }}</h3>
                <div class="article-tags">
                  <el-tag
                    size="mini"
                    :type="getArticleTypeTagType(article.articleType)"
                  >
                    {{ article.articleType || '未分类' }}
                  </el-tag>
                  <el-tag
                    size="mini"
                    :type="article.status === 'published' ? 'success' : 'warning'"
                  >
                    {{ article.status === 'published' ? '已发布' : article.status === 'draft' ? '草稿' : '编辑中' }}
                  </el-tag>
                </div>
              </div>
              <div class="action-buttons">
                <el-button
                  type="text"
                  class="like-btn"
                  :class="{ 'liked': article.userLikeStatus === 1 }"
                  @click.stop="handleLike(article)"
                >
                  赞 {{ article.likeCount || 0 }}
                </el-button>
                <el-button
                  type="text"
                  class="hate-btn"
                  :class="{ 'hated': article.userLikeStatus === -1 }"
                  @click.stop="handleHate(article)"
                >
                  踩 {{ article.hateCount || 0 }}
                </el-button>
              </div>
            </div>

            <p class="article-digest">{{ article.digest || '暂无摘要' }}</p>

            <div class="article-meta">
              <span class="article-author">{{ article.author || '未知作者' }}</span>

              <div class="article-stats">
                <span class="view-count">{{ article.viewCount || 0 }} 观看</span>
                <span class="publish-time">{{ parseTime(article.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
              </div>
            </div>

            <div class="article-actions">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click.stop="handleUpdate(article)"
                v-hasPermi="['proj_qhy:article:edit']"
              >修改</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click.stop="handleDelete(article)"
                v-hasPermi="['proj_qhy:article:remove']"
              >删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-checkbox-group>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="文章分类" prop="articleType">
          <el-select v-model="form.articleType" placeholder="请选择文章分类">
            <el-option label="技术" value="技术" />
            <el-option label="生活" value="生活" />
            <el-option label="思考" value="思考" />
            <el-option label="读书" value="读书" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="editting">编辑中</el-radio>
            <el-radio label="published">已发布</el-radio>
            <el-radio label="draft">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文章摘要" prop="digest">
          <el-input v-model="form.digest" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="文章内容" prop="content">
          <editor v-model="form.content" :min-height="192"/>
        </el-form-item>
        <el-form-item label="封面图片" prop="cover">
          <imageUpload v-model="form.cover"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// (引入了新的 exportPdf)
import {
  listArticle,
  getArticle,
  delArticle,
  addArticle,
  updateArticle,
  exportArticle,
  likeArticle,
  hateArticle,
  exportPdf
} from "@/api/proj_qhy/article";

export default {
  name: "Article",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 文章管理表格数据
      articleList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        articleType: null,
        status: null,
        author: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "文章标题不能为空", trigger: "blur" }
        ],
        articleType: [
          { required: true, message: "文章分类不能为空", trigger: "change" }
        ]
      },
      // (选择模式)
      isSelectionMode: false,
      selectedIds: [], // 选中的ID数组
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询文章管理列表 */
    getList() {
      this.loading = true;
      listArticle(this.queryParams).then(response => {
        this.articleList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(error => {
        console.error('获取文章列表失败:', error);
        this.loading = false;
      });
    },

    // 获取完整图片URL
    getFullImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http')) return url;
      return process.env.VUE_APP_BASE_API + url;
    },

    // 文章点击事件 - 跳转或选择
    handleArticleClick(article) {
      if (this.isSelectionMode) {
        // 在选择模式下，点击卡片切换选中状态
        const index = this.selectedIds.indexOf(article.id);
        if (index > -1) {
          this.selectedIds.splice(index, 1);
        } else {
          this.selectedIds.push(article.id);
        }
      } else {
        // 正常模式下跳转
        this.$router.push(`/proj_qhy/article/detail/${article.id}`);
      }
    },

    // (点赞/点踩 不变)
    async handleLike(article) {
      // ... (保留)
    },
    async handleHate(article) {
      // ... (保留)
    },

    // 文章分类标签类型
    getArticleTypeTagType(type) {
      const typeMap = {
        '技术': '',
        '生活': 'success',
        '思考': 'info',
        '读书': 'warning'
      };
      return typeMap[type] || 'info';
    },

    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },

    // 表单重置
    reset() {
      this.form = {
        id: null,
        title: null,
        digest: null,
        content: null,
        cover: null,
        articleType: "技术",
        status: "editting",
        viewCount: 0,
        commentCount: 0,
        likeCount: 0,
        hateCount: 0,
        bookmarkCount: 0,
        author: this.$store.getters.name, // 默认作者设为当前登录用户昵称
        userId: this.$store.getters.userId
      };
      this.resetForm("form");
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },

    // (新增) 切换选择模式
    toggleSelectionMode() {
      this.isSelectionMode = !this.isSelectionMode;
      this.selectedIds = []; // 切换模式时清空选项
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加文章";
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      // (row.id 是卡片上的修改按钮传来的)
      const id = row.id;
      getArticle(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改文章";
      });
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateArticle(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            // 新增时自动填充作者信息
            this.form.author = this.$store.getters.name;
            this.form.userId = this.$store.getters.userId;
            addArticle(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },

    /** 删除按钮操作 (已适配批量) */
    handleDelete(row) {
      // row.id 存在表示是卡片按钮；否则使用 selectedIds
      const ids = row.id ? [row.id] : this.selectedIds;
      const idsString = ids.join(',');

      this.$modal.confirm('是否确认删除文章编号为"' + idsString + '"的数据项？').then(function() {
        return delArticle(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
        this.selectedIds = []; // 清空选项
        if (this.isSelectionMode) {
          this.isSelectionMode = false; // 退出选择模式
        }
      }).catch(() => {});
    },

    /** 导出Excel按钮操作 */
    handleExport() {
      const queryParams = this.queryParams;
      this.$modal.confirm('是否确认导出所有文章数据项？').then(() => {
        this.exportLoading = true;
        return exportArticle(queryParams);
      }).then(response => {
        this.$download.name(response.msg);
        this.exportLoading = false;
      }).catch(() => {});
    },

    /** (新增) 导出PDF按钮操作 */
    handleExportPdf() {
      if (this.selectedIds.length === 0) {
        this.$modal.msgWarning("请至少选择一篇文章");
        return;
      }

      const ids = this.selectedIds;
      this.$modal.confirm('是否确认导出选中的 ' + ids.length + ' 篇文章为PDF？').then(() => {
        this.loading = true; // 开启遮罩
        return exportPdf(ids);
      }).then(response => {
        this.loading = false;
        // response.data 是一个 URL 列表
        // 因为是多个文件，我们不能一次性下载
        this.$modal.alert({
          title: '导出成功',
          message: '导出成功！ ' + response.data.length + ' 个PDF文件已保存在服务器的 /profile 目录 (即 ' + response.msg + ' 文件夹)。',
          type: 'success'
        });
        this.isSelectionMode = false;
        this.selectedIds = [];
      }).catch(() => {
        this.loading = false;
      });
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.mb8 {
  margin-bottom: 8px;
}

/* B站风格文章列表样式 */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-item {
  display: flex;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e8e8e8;
  position: relative; /* 为了定位复选框 */
}

.article-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.article-cover {
  flex-shrink: 0;
  margin-right: 16px;
}

.cover-image {
  width: 160px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
}

.image-slot {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 24px;
}

.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.article-title-section {
  flex: 1;
  margin-right: 12px;
}

.article-title {
  font-size: 16px;
  font-weight: 600;
  color: #18191c;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-tags {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.like-btn,
.hate-btn {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  transition: all 0.3s;
  font-size: 12px;
  color: #61666d;
}

.like-btn:hover,
.like-btn.liked {
  border-color: #f56c6c;
  color: #f56c6c;
  background-color: #fff5f5;
}

.hate-btn:hover,
.hate-btn.hated {
  border-color: #909399;
  color: #909399;
  background-color: #f4f4f5;
}

.article-digest {
  font-size: 14px;
  color: #61666d;
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.article-author {
  font-size: 12px;
  color: #9499a0;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #9499a0;
}

.view-count {
  color: #61666d;
}

.publish-time {
  color: #9499a0;
}

.article-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 选择模式样式 */
.article-checkbox {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}
/* 当处于选择模式时，卡片增加一个遮罩提示 */
.article-item:hover {
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-item {
    flex-direction: column;
  }

  .article-cover {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .cover-image {
    width: 100%;
    height: 180px;
  }

  .article-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .action-buttons {
    margin-top: 8px;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
