<template>
  <div class="app-container">
    <!-- 搜索表单 (打印时隐藏: class="no-print") -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px" class="no-print">
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

    <!-- 操作按钮组 (打印时隐藏) -->
    <el-row :gutter="10" class="mb8 no-print">
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

        <!-- 打印按钮 -->
        <el-col :span="1.5">
          <el-button
            type="warning"
            plain
            icon="el-icon-printer"
            @click="handlePrint"
          >打印</el-button>
        </el-col>

        <!-- 统计按钮 -->
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="el-icon-s-data"
            @click="handleStatistics"
          >统计</el-button>
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
            type="primary"
            plain
            icon="el-icon-document"
            :disabled="selectedIds.length === 0"
            @click="handleExportWord"
          >导出Word</el-button>
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
          v-for="article in articleList"
          :key="article.id"
          class="article-wrapper"
          @click="handleArticleClick(article)"
        >
          <!-- 选择模式的复选框 (打印时隐藏) -->
          <div v-if="isSelectionMode" class="selection-area no-print">
            <el-checkbox
              :label="article.id"
              @click.native.stop
            >&nbsp;</el-checkbox>
          </div>

          <div class="article-item">
            <div class="article-cover">
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
                <!-- 按钮组 (打印时隐藏) -->
                <div class="action-buttons no-print">
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

              <!-- 操作按钮 (打印时隐藏) -->
              <div class="article-actions no-print">
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
      </div>
    </el-checkbox-group>

    <!-- 分页 (打印时隐藏) -->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
      class="no-print"
    />

    <!-- 编辑/新增弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" id="articleTitle" />
        </el-form-item>
        <el-form-item label="文章分类" prop="articleType">
          <el-select v-model="form.articleType" placeholder="请选择文章分类" id="articleType">
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
          <el-input v-model="form.digest" type="textarea" placeholder="请输入内容" id="articleDigest" />
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

    <!-- ⭐ 统计图表弹窗 -->
    <el-dialog title="文章分类统计" :visible.sync="statsVisible" width="800px" append-to-body>
      <div id="chart-container" style="width: 100%; height: 400px;"></div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="statsVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {
  listArticle,
  getArticle,
  delArticle,
  addArticle,
  updateArticle,
  exportArticle,
  likeArticle,
  hateArticle,
  exportPdf,
  exportWord
} from "@/api/proj_qhy/article";
// ⭐ 引入 echarts
import * as echarts from 'echarts';

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
      // 选择模式相关
      isSelectionMode: false,
      selectedIds: [], // 选中的ID数组

      // ⭐ 统计相关数据
      statsVisible: false,
      chartInstance: null
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

    // 切换选择模式
    toggleSelectionMode() {
      this.isSelectionMode = !this.isSelectionMode;
      this.selectedIds = []; // 切换模式时清空选项
    },

    // 文章点击事件
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
        // 正常模式下跳转到详情
        this.$router.push(`/proj_qhy/article/detail/${article.id}`);
      }
    },

    // 点赞处理
    async handleLike(article) {
      try {
        const response = await likeArticle(article.id);
        if (response.code === 200) {
          const oldStatus = article.userLikeStatus || 0;
          if (oldStatus === 1) {
            article.userLikeStatus = 0;
            article.likeCount = Math.max(0, (article.likeCount || 0) - 1);
          } else {
            article.userLikeStatus = 1;
            article.likeCount = (article.likeCount || 0) + 1;
            if (oldStatus === -1) article.hateCount = Math.max(0, (article.hateCount || 0) - 1);
          }
        }
      } catch (error) {}
    },

    // 点踩处理
    async handleHate(article) {
      try {
        const response = await hateArticle(article.id);
        if (response.code === 200) {
          const oldStatus = article.userLikeStatus || 0;
          if (oldStatus === -1) {
            article.userLikeStatus = 0;
            article.hateCount = Math.max(0, (article.hateCount || 0) - 1);
          } else {
            article.userLikeStatus = -1;
            article.hateCount = (article.hateCount || 0) + 1;
            if (oldStatus === 1) article.likeCount = Math.max(0, (article.likeCount || 0) - 1);
          }
        }
      } catch (error) {}
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
        author: this.$store.getters.name,
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

    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加文章";
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      // 如果从顶部按钮点，row 为 null，此时提示在列表中修改
      if (!row) {
        this.$modal.msgWarning("请在文章列表中点击'修改'按钮");
        return;
      }
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

    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id ? [row.id] : this.selectedIds;
      if (ids.length === 0) return;

      this.$modal.confirm('是否确认删除？').then(function() {
        return delArticle(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
        this.selectedIds = [];
        if (this.isSelectionMode && !row.id) {
          this.isSelectionMode = false; // 如果是批量删除完，退出选择模式
        }
      }).catch(() => {});
    },

    /** 导出Excel */
    handleExport() {
      const queryParams = this.queryParams;
      this.$modal.confirm('是否确认导出所有文章数据项？').then(() => {
        return exportArticle(queryParams);
      }).then(response => {
        this.$download.name(response.msg);
      }).catch(() => {});
    },

    /** 导出PDF */
    handleExportPdf() {
      if (this.selectedIds.length === 0) {
        this.$modal.msgWarning("请至少选择一篇文章");
        return;
      }
      const ids = this.selectedIds;
      this.$modal.confirm('是否确认导出选中的 ' + ids.length + ' 篇文章为PDF？').then(() => {
        this.$modal.msgSuccess("正在打包下载，请稍候...");
        this.loading = true;
        return exportPdf(ids);
      }).then((res) => {
        const blob = new Blob([res], { type: 'application/zip' });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = '文章批量导出_' + new Date().getTime() + '.zip';
        link.click();
        window.URL.revokeObjectURL(link.href);

        this.loading = false;
        this.isSelectionMode = false;
        this.selectedIds = [];
      }).catch((err) => {
        console.error(err);
        this.loading = false;
        this.$modal.msgError("下载失败");
      });
    },
    /** 导出Word按钮操作 */
    handleExportWord() {
      if (this.selectedIds.length === 0) {
        this.$modal.msgWarning("请至少选择一篇文章");
        return;
      }
      const ids = this.selectedIds;
      this.$modal.confirm('是否确认导出选中的 ' + ids.length + ' 篇文章为 Word？').then(() => {
        this.$modal.msgSuccess("正在打包下载，请稍候...");
        this.loading = true;
        return exportWord(ids);
      }).then((res) => {
        const blob = new Blob([res], { type: 'application/zip' });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = '文章Word导出_' + new Date().getTime() + '.zip';
        link.click();
        window.URL.revokeObjectURL(link.href);

        this.loading = false;
        this.isSelectionMode = false;
        this.selectedIds = [];
      }).catch((err) => {
        console.error(err);
        this.loading = false;
        this.$modal.msgError("下载失败");
      });
    },

    /** ⭐ 打印功能 */
    handlePrint() {
      window.print();
    },

    /** ⭐ 打开统计弹窗 (使用真实数据) */
    handleStatistics() {
      this.statsVisible = true;

      // 显示加载动画
      const loading = this.$loading({
        lock: true,
        text: '正在统计数据...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      // 获取所有文章数据 (pageSize 设置很大以获取全部)
      listArticle({ pageNum: 1, pageSize: 10000 }).then(response => {
        const list = response.rows;

        // 统计逻辑
        let statsMap = {
          '技术': 0,
          '生活': 0,
          '思考': 0,
          '读书': 0
        };

        // 遍历所有文章进行计数
        list.forEach(item => {
          if (statsMap[item.articleType] !== undefined) {
            statsMap[item.articleType]++;
          }
        });

        // 构造 ECharts 数据格式
        const chartData = [
          { value: statsMap['技术'], name: '技术' },
          { value: statsMap['生活'], name: '生活' },
          { value: statsMap['思考'], name: '思考' },
          { value: statsMap['读书'], name: '读书' }
        ];

        loading.close();

        // 渲染图表
        this.$nextTick(() => {
          this.initChart(chartData);
        });
      }).catch(() => {
        loading.close();
        this.$modal.msgError("获取统计数据失败");
      });
    },

    /** ⭐ 初始化图表 (接收真实数据) */
    initChart(data) {
      if (this.chartInstance) {
        this.chartInstance.dispose();
      }
      const chartDom = document.getElementById('chart-container');
      this.chartInstance = echarts.init(chartDom);

      const option = {
        title: { text: '文章分类统计', left: 'center' },
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', left: 'left' },
        series: [
          {
            name: '文章数量',
            type: 'pie',
            radius: '50%',
            data: data, // 使用传入的真实数据
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      };
      this.chartInstance.setOption(option);
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

/* 外层包装器：Flex布局实现复选框与卡片并排 */
.article-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s;
}

/* 左侧复选框区域 */
.selection-area {
  width: 40px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}
.selection-area ::v-deep .el-checkbox__inner {
  width: 20px;
  height: 20px;
}
.selection-area ::v-deep .el-checkbox__inner::after {
  left: 7px;
  top: 3px;
}

/* 文章列表容器 */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 文章卡片 */
.article-item {
  flex-grow: 1;
  display: flex;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e8e8e8;
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

<!-- ⭐ 修正后的打印专用样式：只在打印时生效 -->
<style>
@media print {
  @page {
    size: auto;
    margin: 10mm;
  }
  /* 隐藏不想打印的元素：侧边栏、顶部导航、标签页等 */
  .navbar,
  .sidebar-container,
  .tags-view-container,
  .el-dialog__wrapper,
  .v-modal {
    display: none !important;
  }
  /* 隐藏带有 no-print 类的元素（我们在 template 里加的） */
  .no-print {
    display: none !important;
  }
  /* 调整主容器宽度，让内容铺满打印纸 */
  .main-container {
    margin-left: 0 !important;
    width: 100% !important;
    padding: 0 !important;
  }
  /* 确保文章列表可见 */
  .article-list {
    display: block !important;
  }
  /* 优化打印时的卡片样式 */
  .article-item {
    border: 1px solid #ddd;
    margin-bottom: 15px;
    page-break-inside: avoid; /* 防止分页切断内容 */
    box-shadow: none !important;
  }
}
</style>
