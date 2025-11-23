<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="notice-header no-print">
          <span class="notice-title">通告管理</span>
          <div class="header-btn-group">
            <el-button
              v-hasPermi="['proj_cyq:notice:add']"
              type="primary"
              icon="el-icon-plus"
              size="small"
              @click="handleAdd"
              class="mac-btn"
            >
              新增
            </el-button>
            <el-button
              type="info"
              plain
              icon="el-icon-arrow-left"
              size="small"
              @click="handleBack"
              class="mac-btn"
            >
              返回
            </el-button>
          </div>
        </div>

        <el-card class="box-card">
          <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px" class="no-print">
            <el-form-item label="公告标题" prop="title">
              <el-input
                v-model="queryParams.title"
                placeholder="请输入公告标题"
                clearable
                size="small"
                style="width: 240px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery" class="mac-btn">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuery" class="mac-btn">重置</el-button>
            </el-form-item>
          </el-form>

          <el-row :gutter="10" class="mb8 no-print">
            <el-col :span="1.5">
              <el-button
                type="warning"
                icon="el-icon-download"
                size="mini"
                @click="handleExport"
                v-hasPermi="['proj_cyq:notice:export']"
                :loading="exportLoading"
                class="mac-btn"
              >导出</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="primary"
                plain
                icon="el-icon-printer"
                size="mini"
                @click="handlePrint"
                class="mac-btn"
              >打印</el-button>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </el-row>

          <h2 class="print-title" style="display: none;">通告列表</h2>

          <el-table
            v-loading="loading"
            :data="noticeList"
            @selection-change="handleSelectionChange"
            class="print-table"
          >
            <el-table-column type="selection" width="55" align="center" class-name="no-print-col" />
            <el-table-column label="公告标题" align="center" prop="title" :show-overflow-tooltip="true" />
            <el-table-column label="创建人" align="center" prop="createBy" width="120" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="180">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width no-print-col">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-view"
                  @click="handleView(scope.row)"
                  v-hasPermi="['proj_cyq:notice:query']"
                >查看</el-button>
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-edit"
                  @click="handleUpdate(scope.row)"
                  v-hasPermi="['proj_cyq:notice:edit']"
                >修改</el-button>
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleDelete(scope.row)"
                  v-hasPermi="['proj_cyq:notice:remove']"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="total>0"
            :total="total"
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="getList"
            class="no-print"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <editor v-model="form.content" :min-height="192"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm" class="mac-btn">确 定</el-button>
        <el-button @click="cancel" class="mac-btn">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="公告详情" :visible.sync="viewOpen" width="800px" append-to-body>
      <el-form ref="viewForm" :model="viewForm" label-width="80px">
        <el-form-item label="公告标题">{{ viewForm.title }}</el-form-item>
        <el-form-item label="公告内容">
          <div class="notice-content" v-html="viewForm.content"></div>
        </el-form-item>
        <el-form-item label="创建时间">{{ viewForm.createTime }}</el-form-item>
        <el-form-item label="创建人">{{ viewForm.createBy }}</el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false" class="mac-btn">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listNotice, getNotice, delNotice, addNotice, updateNotice, exportNotice } from "@/api/proj_cyq/notice";

export default {
  name: "ClassNotice",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出加载状态
      exportLoading: false,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 公告表格数据
      noticeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示查看弹出层
      viewOpen: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined
      },
      // 表单参数
      form: {},
      // 查看表单参数
      viewForm: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "公告标题不能为空", trigger: "blur" }
        ],
        content: [
          { required: true, message: "公告内容不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询公告列表 */
    getList() {
      this.loading = true;
      listNotice(this.queryParams).then(response => {
        this.noticeList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        noticeId: undefined,
        title: undefined,
        content: undefined
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
      this.title = "添加公告";
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.viewForm = row;
      this.viewOpen = true;
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const noticeId = row.noticeId || this.ids;
      getNotice(noticeId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改公告";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.noticeId != null) {
            updateNotice(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            }).catch(() => {
              this.$modal.msgError("修改失败");
            });
          } else {
            addNotice(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            }).catch(() => {
              this.$modal.msgError("新增失败");
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const noticeIds = row.noticeId || this.ids;
      this.$modal.confirm('是否确认删除公告编号为"' + noticeIds + '"的数据项?').then(() => {
        return delNotice(noticeIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.$modal.confirm('是否确认导出所有公告数据项？').then(() => {
        this.exportLoading = true;
        return exportNotice();
      }).then(response => {
        this.handleExportResponse(response);
      }).catch((error) => {
        console.error('导出失败:', error);
        this.exportLoading = false;
        this.$modal.msgError("导出失败：" + (error.message || '未知错误'));
      });
    },
    /** 【新增】打印功能 */
    handlePrint() {
      window.print();
    },
    /** 处理导出响应 */
    handleExportResponse(response) {
      try {
        const blob = new Blob([response], {
          type: 'application/vnd.ms-excel;charset=utf-8'
        });
        const downloadElement = document.createElement('a');
        const href = window.URL.createObjectURL(blob);
        downloadElement.href = href;
        downloadElement.download = '通知公告数据.xlsx';
        document.body.appendChild(downloadElement);
        downloadElement.click();
        document.body.removeChild(downloadElement);
        window.URL.revokeObjectURL(href);
        this.exportLoading = false;
        this.$modal.msgSuccess("导出成功");
      } catch (error) {
        console.error('处理导出文件失败:', error);
        this.exportLoading = false;
        this.$modal.msgError("处理导出文件失败");
      }
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.noticeId);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    /** 返回按钮操作 */
    handleBack() {
      this.$router.push('/proj_cyq');
    }
  }
};
</script>

<style scoped>
/* 整体容器：浅灰背景，舒适间距 */
.app-container {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 头部布局 */
.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notice-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.header-btn-group {
  display: flex;
  gap: 10px;
}

/* 卡片容器 */
.app-container >>> .el-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  background-color: #ffffff;
}

.app-container >>> .el-card__body {
  padding: 24px;
}

/* ============================================
   【核心修改】按钮样式优化 (高对比度，防止文字隐身)
   ============================================ */
.mac-btn {
  border-radius: 20px;
  font-weight: 500;
  border: 1px solid transparent;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 1. 主操作按钮 (新增、搜索、确定)：深蓝色实心，白字 */
.app-container >>> .el-button--primary:not(.is-plain) {
  background-color: #0071e3;
  border-color: #0071e3;
  color: #ffffff !important; /* 强制白字 */
  box-shadow: 0 2px 6px rgba(0, 113, 227, 0.3);
}
.app-container >>> .el-button--primary:not(.is-plain):hover {
  background-color: #0077ed;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.4);
}

/* 2. 警告操作按钮 (导出)：橙色实心，白字 */
.app-container >>> .el-button--warning {
  background-color: #ff9f0a;
  border-color: #ff9f0a;
  color: #ffffff !important; /* 强制白字 */
  box-shadow: 0 2px 6px rgba(255, 159, 10, 0.3);
}
.app-container >>> .el-button--warning:hover {
  background-color: #ffb340;
  transform: translateY(-1px);
}

/* 3. 辅助操作按钮 (打印)：浅蓝背景，深蓝字 */
/* 【修复】移除了 border-color (变透明)，保留了悬停上浮 */
.app-container >>> .el-button--primary.is-plain {
  background-color: #f0f7ff !important; /* 浅蓝背景 */
  border-color: transparent !important;  /* 【关键】去掉边框 */
  color: #0071e3 !important;            /* 深蓝文字 */
}
.app-container >>> .el-button--primary.is-plain:hover {
  background-color: #0071e3 !important;
  border-color: #0071e3 !important;
  color: #ffffff !important;
  transform: translateY(-1px); /* 悬停上浮 */
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2); /* 悬停阴影 */
}

/* 4. 通用按钮 (返回、取消、重置)：白底灰边，黑字 */
.app-container >>> .el-button--default,
.app-container >>> .el-button--info.is-plain {
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
  color: #606266 !important; /* 强制深灰字 */
}
.app-container >>> .el-button--default:hover,
.app-container >>> .el-button--info.is-plain:hover {
  border-color: #c6e2ff;
  color: #409eff !important;
  background-color: #ecf5ff;
}

/* 表格样式优化 */
.app-container >>> .el-table {
  border-radius: 8px;
  overflow: hidden;
  margin-top: 15px;
}

.app-container >>> .el-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
  height: 50px;
}

.app-container >>> .el-table td {
  padding: 12px 0;
}

/* 弹窗优化 */
.app-container >>> .el-dialog {
  border-radius: 12px;
}
.app-container >>> .el-dialog__header {
  border-bottom: 1px solid #eee;
  padding: 20px;
}
.app-container >>> .el-dialog__footer {
  border-top: 1px solid #eee;
  padding: 15px 20px;
}

.notice-content {
  border: 1px solid #dcdfe6;
  padding: 20px;
  border-radius: 4px;
  background-color: #fafafa;
  min-height: 120px;
  color: #303133;
  line-height: 1.6;
}

/* ==============================
   【打印样式】保持不变
   ============================== */
@media print {
  .no-print,
  .navbar,
  .sidebar-container,
  .tags-view-container,
  .el-dialog__wrapper,
  .v-modal,
  .el-pagination {
    display: none !important;
  }

  .no-print-col {
    display: none !important;
  }
  .el-table__fixed-right {
    display: none !important;
  }

  .print-title {
    display: block !important;
    text-align: center;
    font-size: 24px;
    margin-bottom: 20px;
    font-weight: bold;
  }

  .app-container {
    padding: 0;
    margin: 0;
    width: 100% !important;
    background-color: white;
  }

  .app-container >>> .el-card {
    box-shadow: none;
    border: none;
  }
  .app-container >>> .el-card__body {
    padding: 0;
  }

  .print-table {
    border: 1px solid #000 !important;
    font-size: 12px;
    width: 100% !important;
  }

  .print-table td,
  .print-table th {
    border: 1px solid #000 !important;
    color: #000 !important;
    padding: 8px 5px !important;
  }

  tr {
    page-break-inside: avoid;
  }
}
</style>
