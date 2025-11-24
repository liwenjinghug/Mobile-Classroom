<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix notice-header no-print">
            <span class="header-title">操作日志</span>
            <div class="header-btn-group">
              <el-button
                type="primary"
                icon="el-icon-s-data"
                size="mini"
                @click="handleStats"
                class="mac-btn"
              >
                统计
              </el-button>
              <el-button
                style="float: right;"
                type="info"
                plain
                icon="el-icon-arrow-left"
                size="mini"
                @click="handleBack"
                class="mac-btn"
              >
                返回
              </el-button>
            </div>
          </div>

          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px" class="no-print">
            <el-form-item label="系统模块" prop="title">
              <el-input v-model="queryParams.title" placeholder="请输入系统模块" clearable size="small" style="width: 240px" @keyup.enter.native="handleQuery"/>
            </el-form-item>
            <el-form-item label="操作人员" prop="operName">
              <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable size="small" style="width: 240px" @keyup.enter.native="handleQuery"/>
            </el-form-item>
            <el-form-item label="类型" prop="businessType">
              <el-select v-model="queryParams.businessType" placeholder="操作类型" clearable size="small" style="width: 240px">
                <el-option v-for="dict in businessTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="操作状态" clearable size="small" style="width: 240px">
                <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
            <el-form-item label="操作时间">
              <el-date-picker v-model="dateRange" size="small" style="width: 240px" value-format="yyyy-MM-dd" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery" class="mac-btn">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuery" class="mac-btn">重置</el-button>
            </el-form-item>
          </el-form>

          <el-row :gutter="10" class="mb8 no-print">
            <el-col :span="1.5">
              <el-button type="danger" icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" class="mac-btn">删除</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="danger" icon="el-icon-delete" size="mini" @click="handleClean" class="mac-btn">清空</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading" class="mac-btn">导出</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="primary" icon="el-icon-printer" size="mini" @click="handlePrint" class="mac-btn">打印</el-button>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </el-row>

          <h2 class="print-title" style="display: none;">操作日志列表</h2>

          <el-table v-loading="loading" :data="operlogList" @selection-change="handleSelectionChange" class="print-table">
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
                <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">详细</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="no-print"/>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="操作日志统计" :visible.sync="statsOpen" width="800px" append-to-body class="no-print">
      <div v-loading="statsLoading">
        <el-row :gutter="20" class="stats-overview">
          <el-col :span="24" style="text-align: center; margin-bottom: 20px;">
            <div class="stat-value" style="font-size: 36px; color: #409EFF;">{{ stats.totalCount }}</div>
            <div class="stat-label">总操作次数</div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <h4 class="stats-subtitle">按模块统计</h4>
            <el-table :data="stats.moduleStats" border size="mini" height="250">
              <el-table-column prop="name" label="模块名称" align="center"/>
              <el-table-column prop="value" label="次数" align="center" width="80"/>
            </el-table>
          </el-col>
          <el-col :span="12">
            <h4 class="stats-subtitle">按类型统计</h4>
            <el-table :data="stats.typeStats" border size="mini" height="250">
              <el-table-column prop="name" label="操作类型" align="center"/>
              <el-table-column prop="value" label="次数" align="center" width="80"/>
            </el-table>
          </el-col>
        </el-row>

        <el-row style="margin-top: 20px;">
          <el-col :span="24">
            <h4 class="stats-subtitle">状态分布</h4>
            <div style="display: flex; justify-content: center; gap: 40px;">
              <div v-for="item in stats.statusStats" :key="item.name" class="stat-box" :class="item.name === '成功' ? 'success' : 'danger'">
                <div class="stat-name">{{ item.name }}</div>
                <div class="stat-num">{{ item.value }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="statsOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

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
// 请确保在 operlog.js 中添加了 getOperLogStats 方法
import {listOperlog, delOperlog, cleanOperlog, exportOperlog, getOperLogStats} from "@/api/proj_cyq/operlog";

export default {
  name: "Operlog",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      operlogList: [],
      open: false,
      businessTypeOptions: [
        {value: '0', label: '其它'}, {value: '1', label: '新增'}, {value: '2', label: '修改'},
        {value: '3', label: '删除'}, {value: '4', label: '授权'}, {value: '5', label: '导出'},
        {value: '6', label: '导入'}, {value: '7', label: '强退'}, {value: '8', label: '生成代码'},
        {value: '9', label: '清空数据'}
      ],
      statusOptions: [
        {value: '0', label: '成功'}, {value: '1', label: '失败'}
      ],
      dateRange: [],
      form: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        operName: undefined,
        businessType: undefined,
        status: undefined
      },
      exportLoading: false,

      // 统计相关
      statsOpen: false,
      statsLoading: false,
      stats: {
        totalCount: 0,
        moduleStats: [],
        typeStats: [],
        statusStats: []
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listOperlog(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.operlogList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(error => {
        this.loading = false;
      });
    },
    statusFormat(row, column) {
      const status = row.status;
      const dict = this.statusOptions.find(item => item.value == status);
      return dict ? dict.label : '未知';
    },
    businessTypeFormat(row, column) {
      const businessType = row.businessType;
      const dict = this.businessTypeOptions.find(item => item.value == businessType);
      return dict ? dict.label : '未知';
    },
    businessTypeFilter(businessType) {
      const dict = this.businessTypeOptions.find(item => item.value == businessType);
      return dict ? dict.label : '未知';
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.operId)
      this.multiple = !selection.length
    },
    handleView(row) {
      this.open = true;
      this.form = row;
    },
    handleDelete(row) {
      const operIds = row.operId || this.ids;
      this.$modal.confirm('是否确认删除操作日志编号为"' + operIds + '"的数据项？').then(() => {
        return delOperlog(operIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleClean() {
      this.$modal.confirm('是否确认清空所有操作日志数据项？').then(() => {
        return cleanOperlog();
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("清空成功");
      }).catch(() => {});
    },
    handleExport() {
      this.$modal.confirm('是否确认导出所有操作日志数据项？').then(() => {
        this.exportLoading = true;
        return exportOperlog();
      }).then(response => {
        this.handleExportResponse(response);
      }).catch((error) => {
        this.exportLoading = false;
      });
    },
    handlePrint() {
      window.print();
    },

    // 【新增】处理统计
    handleStats() {
      this.statsOpen = true;
      this.statsLoading = true;
      getOperLogStats().then(response => {
        if (response.code === 200) {
          this.stats = response.data;
        }
        this.statsLoading = false;
      }).catch(() => {
        this.statsLoading = false;
        this.$modal.msgError("获取统计数据失败");
      });
    },

    handleExportResponse(response) {
      try {
        const blob = new Blob([response], { type: 'application/vnd.ms-excel;charset=utf-8' });
        const downloadElement = document.createElement('a');
        const href = window.URL.createObjectURL(blob);
        downloadElement.href = href;
        downloadElement.download = '操作日志记录数据.xlsx';
        document.body.appendChild(downloadElement);
        downloadElement.click();
        document.body.removeChild(downloadElement);
        window.URL.revokeObjectURL(href);
        this.exportLoading = false;
        this.$modal.msgSuccess("导出成功");
      } catch (error) {
        this.exportLoading = false;
        this.$modal.msgError("处理导出文件失败");
      }
    },
    handleBack() {
      this.$router.push('/proj_cyq/index');
    }
  }
};
</script>

<style scoped>
/* 统一的 Mac 风格样式 */
.app-container {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
}

.header-btn-group {
  display: flex;
  gap: 10px;
}

/* 卡片 */
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

/* 输入框 */
.app-container >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
}
.app-container >>> .el-input__inner:focus {
  border-color: #0071e3;
}

/* 按钮通用样式 (Mac Style) */
.mac-btn {
  border-radius: 20px;
  font-weight: 500;
  border: 1px solid transparent;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 1. 主操作 (搜索/打印/新增/统计) - 深蓝实心 */
.app-container >>> .el-button--primary:not(.is-plain) {
  background-color: #0071e3; border-color: #0071e3; color: #fff !important;
  box-shadow: 0 2px 6px rgba(0, 113, 227, 0.3);
}
.app-container >>> .el-button--primary:not(.is-plain):hover {
  background-color: #0077ed; transform: translateY(-1px);
}

/* 2. 危险操作 (删除/清空) - 红色实心 */
.app-container >>> .el-button--danger:not(.is-plain) {
  background-color: #ff3b30; border-color: #ff3b30; color: #fff !important;
  box-shadow: 0 2px 6px rgba(255, 59, 48, 0.3);
}
.app-container >>> .el-button--danger:not(.is-plain):hover {
  background-color: #ff453a; transform: translateY(-1px);
}

/* 3. 警告操作 (导出) - 橙色实心 */
.app-container >>> .el-button--warning:not(.is-plain) {
  background-color: #ff9f0a; border-color: #ff9f0a; color: #fff !important;
  box-shadow: 0 2px 6px rgba(255, 159, 10, 0.3);
}
.app-container >>> .el-button--warning:not(.is-plain):hover {
  background-color: #ffb340; transform: translateY(-1px);
}

/* 4. 辅助操作 (返回/重置) - 白底灰边 */
.app-container >>> .el-button--default,
.app-container >>> .el-button--info.is-plain {
  background-color: #fff; border: 1px solid #dcdfe6; color: #606266 !important;
}
.app-container >>> .el-button--default:hover,
.app-container >>> .el-button--info.is-plain:hover {
  border-color: #c6e2ff; color: #409eff !important; background-color: #ecf5ff;
}

/* 表格 */
.app-container >>> .el-table { border-radius: 8px; overflow: hidden; margin-top: 15px; }
.app-container >>> .el-table th { background-color: #fbfbfd; color: #86868b; font-weight: 600; height: 50px; }
.app-container >>> .el-tag { border-radius: 6px; border: none; font-weight: 500; }

/* 统计样式 */
.stats-subtitle {
  font-size: 16px; font-weight: 600; color: #303133; margin: 15px 0 10px; text-align: center;
}
.stat-box {
  padding: 15px 25px; border-radius: 8px; text-align: center; min-width: 100px;
}
.stat-box.success { background-color: #f0f9eb; color: #67c23a; }
.stat-box.danger { background-color: #fef0f0; color: #f56c6c; }
.stat-name { font-size: 14px; margin-bottom: 5px; }
.stat-num { font-size: 24px; font-weight: bold; }

/* 打印样式 */
@media print {
  .no-print, .navbar, .sidebar-container, .tags-view-container, .el-dialog__wrapper, .v-modal, .el-pagination { display: none !important; }
  .no-print-col, .el-table__fixed-right { display: none !important; }
  .print-title { display: block !important; text-align: center; font-size: 24px; margin-bottom: 20px; font-weight: bold; }
  .app-container { padding: 0; margin: 0; width: 100% !important; background-color: white; }
  .app-container >>> .el-card { box-shadow: none; border: none; }
  .app-container >>> .el-card__body { padding: 0; }
  .print-table { border: 1px solid #000 !important; font-size: 12px; width: 100% !important; }
  .print-table td, .print-table th { border: 1px solid #000 !important; color: #000 !important; padding: 8px 5px !important; }
  tr { page-break-inside: avoid; }
  .app-container >>> .el-tag { border: 1px solid #000 !important; background: none !important; color: #000 !important; padding: 0 5px; }
}
</style>
