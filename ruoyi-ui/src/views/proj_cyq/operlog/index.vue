<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix notice-header no-print">
            <span class="header-title">操作日志</span>
            <el-button style="float: right;" type="info" plain icon="el-icon-arrow-left" size="mini" @click="handleBack" class="mac-btn">
              返回
            </el-button>
          </div>

          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px" class="no-print">
            <el-form-item label="系统模块" prop="title">
              <el-input
                v-model="queryParams.title"
                placeholder="请输入系统模块"
                clearable
                size="small"
                style="width: 240px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="操作人员" prop="operName">
              <el-input
                v-model="queryParams.operName"
                placeholder="请输入操作人员"
                clearable
                size="small"
                style="width: 240px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="类型" prop="businessType">
              <el-select
                v-model="queryParams.businessType"
                placeholder="操作类型"
                clearable
                size="small"
                style="width: 240px"
              >
                <el-option
                  v-for="dict in businessTypeOptions"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select
                v-model="queryParams.status"
                placeholder="操作状态"
                clearable
                size="small"
                style="width: 240px"
              >
                <el-option
                  v-for="dict in statusOptions"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="操作时间">
              <el-date-picker
                v-model="dateRange"
                size="small"
                style="width: 240px"
                value-format="yyyy-MM-dd"
                type="daterange"
                range-separator="-"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery" class="mac-btn">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuery" class="mac-btn">重置</el-button>
            </el-form-item>
          </el-form>

          <el-row :gutter="10" class="mb8 no-print">
            <el-col :span="1.5">
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                :disabled="multiple"
                @click="handleDelete"
                class="mac-btn"
              >删除</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                @click="handleClean"
                class="mac-btn"
              >清空</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="warning"
                icon="el-icon-download"
                size="mini"
                @click="handleExport"
                :loading="exportLoading"
                class="mac-btn"
              >导出</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="primary"
                icon="el-icon-printer"
                size="mini"
                @click="handlePrint"
                class="mac-btn"
              >打印</el-button>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </el-row>

          <h2 class="print-title" style="display: none;">操作日志列表</h2>

          <el-table
            v-loading="loading"
            :data="operlogList"
            @selection-change="handleSelectionChange"
            class="print-table"
          >
            <el-table-column type="selection" width="55" align="center" class-name="no-print-col" />
            <el-table-column label="日志编号" align="center" prop="operId" />
            <el-table-column label="系统模块" align="center" prop="title" />
            <el-table-column label="操作类型" align="center" prop="businessType" :formatter="businessTypeFormat" />
            <el-table-column label="请求方式" align="center" prop="requestMethod" />
            <el-table-column label="操作人员" align="center" prop="operName" />
            <el-table-column label="操作地址" align="center" prop="operIp" width="130" :show-overflow-tooltip="true" />
            <el-table-column label="操作地点" align="center" prop="operLocation" :show-overflow-tooltip="true" />
            <el-table-column label="操作状态" align="center" prop="status" :formatter="statusFormat">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
                  {{ statusFormat(scope.row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作日期" align="center" prop="operTime" width="180">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.operTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width no-print-col">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-view"
                  @click="handleView(scope.row)"
                >详细</el-button>
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

    <el-dialog title="操作日志详细" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="100px" size="mini">
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作模块：">{{ form.title }} / {{ businessTypeFilter(form.businessType) }}</el-form-item>
            <el-form-item label="登录信息：">{{ form.operName }} / {{ form.operIp }} / {{ form.operLocation }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请求地址：">{{ form.operUrl }}</el-form-item>
            <el-form-item label="请求方式：">{{ form.requestMethod }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="操作方法：">{{ form.method }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="请求参数：">{{ form.operParam }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="返回参数：">{{ form.jsonResult }}</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false" class="mac-btn">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {listOperlog, delOperlog, cleanOperlog, exportOperlog} from "@/api/proj_cyq/operlog";

export default {
  name: "Operlog",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      operlogList: [],
      // 是否显示弹出层
      open: false,
      // 类型数据字典
      businessTypeOptions: [
        {value: '0', label: '其它'},
        {value: '1', label: '新增'},
        {value: '2', label: '修改'},
        {value: '3', label: '删除'},
        {value: '4', label: '授权'},
        {value: '5', label: '导出'},
        {value: '6', label: '导入'},
        {value: '7', label: '强退'},
        {value: '8', label: '生成代码'},
        {value: '9', label: '清空数据'}
      ],
      // 状态数据字典
      statusOptions: [
        {value: '0', label: '成功'},
        {value: '1', label: '失败'}
      ],
      // 日期范围
      dateRange: [],
      // 表单参数
      form: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        operName: undefined,
        businessType: undefined,
        status: undefined
      },
      // 导出加载状态
      exportLoading: false
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询操作日志记录列表 */
    getList() {
      this.loading = true;
      listOperlog(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.operlogList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(error => {
        console.error('获取列表失败:', error);
        this.loading = false;
        this.$modal.msgError('获取数据失败');
      });
    },

    // 操作日志状态字典翻译
    statusFormat(row, column) {
      const status = row.status;
      const dict = this.statusOptions.find(item => item.value == status);
      return dict ? dict.label : '未知';
    },

    // 操作日志类型字典翻译
    businessTypeFormat(row, column) {
      const businessType = row.businessType;
      const dict = this.businessTypeOptions.find(item => item.value == businessType);
      return dict ? dict.label : '未知';
    },

    // 业务类型过滤器（用于对话框显示）
    businessTypeFilter(businessType) {
      const dict = this.businessTypeOptions.find(item => item.value == businessType);
      return dict ? dict.label : '未知';
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },

    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.operId)
      this.multiple = !selection.length
    },

    /** 详细按钮操作 */
    handleView(row) {
      this.open = true;
      this.form = row;
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const operIds = row.operId || this.ids;
      this.$modal.confirm('是否确认删除操作日志编号为"' + operIds + '"的数据项？').then(() => {
        return delOperlog(operIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },

    /** 清空按钮操作 */
    handleClean() {
      this.$modal.confirm('是否确认清空所有操作日志数据项？').then(() => {
        return cleanOperlog();
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("清空成功");
      }).catch(() => {
      });
    },

    /** 导出按钮操作 */
    handleExport() {
      this.$modal.confirm('是否确认导出所有操作日志数据项？').then(() => {
        this.exportLoading = true;
        return exportOperlog();
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
        // 创建 blob 对象
        const blob = new Blob([response], {
          type: 'application/vnd.ms-excel;charset=utf-8'
        });

        // 创建下载链接
        const downloadElement = document.createElement('a');
        const href = window.URL.createObjectURL(blob);

        downloadElement.href = href;
        downloadElement.download = '操作日志记录数据.xlsx';
        document.body.appendChild(downloadElement);
        downloadElement.click();

        // 清理
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

    /** 返回按钮操作 */
    handleBack() {
      this.$router.push('/proj_cyq/index');
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
}

/* Card Styling */
.app-container >>> .el-card {
  border-radius: 18px;
  border: none;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  background-color: #ffffff;
}

.app-container >>> .el-card__header {
  border-bottom: 1px solid #f5f5f7;
  padding: 20px 24px;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
}

/* Form Styling */
.app-container >>> .el-form-item__label {
  font-weight: 500;
  color: #1d1d1f;
}

.app-container >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  height: 36px;
  transition: all 0.2s ease;
}

.app-container >>> .el-input__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

/* ============================================
   【核心修改】按钮样式优化 (高对比度 & 圆润风格)
   ============================================ */
.mac-btn {
  border-radius: 20px;
  font-weight: 500;
  border: 1px solid transparent;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 1. 主操作按钮 (搜索、重新加载、打印)：深蓝色实心，白字 */
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

/* 2. 危险操作按钮 (删除/清空)：红色实心，白字 */
.app-container >>> .el-button--danger {
  background-color: #ff3b30;
  border-color: #ff3b30;
  box-shadow: 0 2px 6px rgba(255, 59, 48, 0.3);
  color: #ffffff !important; /* 强制白字 */
}
.app-container >>> .el-button--danger:hover {
  background-color: #ff453a;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 59, 48, 0.4);
}

/* 3. 警告操作按钮 (导出)：橙色实心，白字 */
.app-container >>> .el-button--warning {
  background-color: #ff9f0a;
  border-color: #ff9f0a;
  color: #ffffff !important; /* 强制白字 */
  box-shadow: 0 2px 6px rgba(255, 159, 10, 0.3);
}
.app-container >>> .el-button--warning:hover {
  background-color: #ffb340;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 159, 10, 0.4);
}

/* 4. 辅助操作按钮 (返回/重置)：白底灰边，黑字 */
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

/* Table Styling */
.app-container >>> .el-table {
  border-radius: 8px;
  overflow: hidden;
  margin-top: 15px;
}

.app-container >>> .el-table th {
  background-color: #fbfbfd;
  color: #86868b;
  font-weight: 600;
  height: 50px;
}

.app-container >>> .el-table td {
  padding: 12px 0;
}

/* Dialog Styling */
.app-container >>> .el-dialog {
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}

.app-container >>> .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
}

.app-container >>> .el-dialog__title {
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

.app-container >>> .el-dialog__body {
  padding: 24px;
}

.app-container >>> .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
}

/* Tags */
.app-container >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

/* ==============================
   【打印样式】
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

  /* 打印时移除el-tag的背景，只显示文字 */
  .app-container >>> .el-tag {
    border: 1px solid #000 !important;
    background: none !important;
    color: #000 !important;
    padding: 0 5px;
  }
}
</style>
