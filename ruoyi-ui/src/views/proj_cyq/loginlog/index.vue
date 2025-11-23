<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix notice-header no-print">
            <span class="header-title">登录日志</span>
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

          <el-alert
            v-if="debugInfo.show"
            :title="debugInfo.title"
            :type="debugInfo.type"
            :description="debugInfo.description"
            show-icon
            closable
            @close="debugInfo.show = false"
            style="margin-bottom: 15px;"
            class="no-print"
          />

          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px" class="no-print">
            <el-form-item label="用户账号" prop="userName">
              <el-input
                v-model="queryParams.userName"
                placeholder="请输入用户账号"
                clearable
                size="small"
                style="width: 240px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="登录地址" prop="ipaddr">
              <el-input
                v-model="queryParams.ipaddr"
                placeholder="请输入登录地址"
                clearable
                size="small"
                style="width: 240px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="登录状态" prop="status">
              <el-select
                v-model="queryParams.status"
                placeholder="登录状态"
                clearable
                size="small"
                style="width: 240px"
              >
                <el-option
                  v-for="dict in statusOptions"
                  :key="dict.dictValue"
                  :label="dict.dictLabel"
                  :value="dict.dictValue"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="操作信息" prop="msg">
              <el-input
                v-model="queryParams.msg"
                placeholder="请输入操作信息"
                clearable
                size="small"
                style="width: 240px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="登录时间">
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
              <el-button type="info" icon="el-icon-info" size="mini" @click="showDebugInfo" class="mac-btn">调试</el-button>
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

          <h2 class="print-title" style="display: none;">登录日志列表</h2>

          <el-table
            v-loading="loading"
            :data="loginlogList"
            @selection-change="handleSelectionChange"
            class="print-table"
          >
            <el-table-column type="selection" width="55" align="center" class-name="no-print-col" />
            <el-table-column label="访问编号" align="center" prop="loginId" />
            <el-table-column label="用户账号" align="center" prop="userName" />
            <el-table-column label="登录地址" align="center" prop="ipaddr" width="130" :show-overflow-tooltip="true" />
            <el-table-column label="登录地点" align="center" prop="loginLocation" :show-overflow-tooltip="true" />
            <el-table-column label="浏览器" align="center" prop="browser" />
            <el-table-column label="操作系统" align="center" prop="os" />
            <el-table-column label="登录状态" align="center" prop="status" :formatter="statusFormat">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
                  {{ statusFormat(scope.row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作信息" align="center" prop="msg">
              <template slot-scope="scope">
                <span :class="getMsgClass(scope.row.msg)">{{ scope.row.msg }}</span>
              </template>
            </el-table-column>
            <el-table-column label="登录日期" align="center" prop="loginTime" width="180">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.loginTime) }}</span>
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

          <div v-if="!loading && loginlogList.length === 0" class="empty-data no-print">
            <el-empty description="暂无登录日志数据">
              <div slot="description">
                <p>暂无登录日志数据，可能的原因：</p>
                <p>1. 还没有用户登录过系统</p>
                <p>2. 登录日志记录功能未正常工作</p>
                <p>3. 数据库连接异常</p>
                <el-button type="primary" @click="getList" class="mac-btn">重新加载</el-button>
              </div>
            </el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listLoginlog, delLoginlog, cleanLoginlog, exportLoginlog } from "@/api/proj_cyq/loginlog";

export default {
  name: "Loginlog",
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
      loginlogList: [],
      // 类型数据字典
      statusOptions: [],
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        ipaddr: undefined,
        status: undefined,
        msg: undefined
      },
      // 导出加载状态
      exportLoading: false,
      // 调试信息
      debugInfo: {
        show: false,
        title: '',
        type: 'info',
        description: ''
      }
    };
  },
  created() {
    console.log('登录日志页面初始化');
    this.loadData();
  },
  methods: {
    /** 加载数据 */
    loadData() {
      console.log('开始加载登录日志数据');
      this.getList();
      this.loadDictData();
    },

    /** 加载字典数据 */
    loadDictData() {
      this.getDicts("sys_common_status").then(response => {
        this.statusOptions = response.data;
        console.log('获取字典数据成功:', this.statusOptions);
      }).catch(error => {
        console.error('获取字典数据失败:', error);
        // 使用默认字典数据
        this.statusOptions = [
          { dictValue: '0', dictLabel: '成功' },
          { dictValue: '1', dictLabel: '失败' }
        ];
      });
    },

    /** 查询系统登录日志列表 */
    getList() {
      this.loading = true;
      listLoginlog(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        if (response && response.code === 200) {
          this.loginlogList = response.rows || [];
          this.total = response.total || 0;
          if (this.loginlogList.length === 0) {
            this.showDebugAlert('warning', '数据为空', '当前没有登录日志数据，请确认：1. 用户已登录过系统 2. 登录日志记录功能正常');
          } else {
            this.debugInfo.show = false;
          }
        } else {
          this.showDebugAlert('error', 'API错误', `接口返回错误: ${response.msg || '未知错误'}`);
          this.loginlogList = [];
          this.total = 0;
        }
        this.loading = false;
      }).catch(error => {
        this.loading = false;
        this.loginlogList = [];
        this.total = 0;
        this.showDebugAlert('error', '请求失败', `获取数据失败: ${error.message || '未知错误'}`);
      });
    },

    /** 显示调试信息 */
    showDebugInfo() {
      const info = `
当前状态：
- 数据条数: ${this.loginlogList.length}
- 总记录数: ${this.total}
- 加载状态: ${this.loading ? '加载中' : '完成'}
      `;
      this.$modal.alert(info, '调试信息', {
        confirmButtonText: '确定',
        customClass: 'debug-modal'
      });
    },

    /** 显示调试提示 */
    showDebugAlert(type, title, description) {
      this.debugInfo = {
        show: true,
        type: type,
        title: title,
        description: description
      };
    },

    // 登录状态字典翻译
    statusFormat(row, column) {
      const status = row.status;
      if (status === 0) {
        if (row.msg && (row.msg.includes('退出成功') || row.msg.includes('退出系统'))) {
          return '退出成功';
        }
        return '成功';
      } else if (status === 1) {
        return '失败';
      } else {
        return '未知';
      }
    },

    /** 根据操作信息设置样式 */
    getMsgClass(msg) {
      if (!msg) return '';
      if (msg.includes('退出成功')) {
        return 'logout-msg';
      } else if (msg.includes('成功') && !msg.includes('退出成功')) {
        return 'success-msg';
      } else if (msg.includes('错误') || msg.includes('失败')) {
        return 'error-msg';
      } else if (msg.includes('退出') || msg.includes('登出')) {
        return 'logout-msg';
      }
      return '';
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
      this.ids = selection.map(item => item.loginId)
      this.multiple = !selection.length
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const loginIds = row.loginId || this.ids;
      this.$modal.confirm('是否确认删除登录日志编号为"' + loginIds + '"的数据项？').then(() => {
        return delLoginlog(loginIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    /** 清空按钮操作 */
    handleClean() {
      this.$modal.confirm('是否确认清空所有登录日志数据项？').then(() => {
        return cleanLoginlog();
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("清空成功");
      }).catch(() => {});
    },

    /** 导出按钮操作 */
    handleExport() {
      this.$modal.confirm('是否确认导出所有登录日志数据项？').then(() => {
        this.exportLoading = true;
        const params = {
          ...this.queryParams,
          beginTime: this.dateRange && this.dateRange[0] ? this.dateRange[0] : undefined,
          endTime: this.dateRange && this.dateRange[1] ? this.dateRange[1] : undefined
        };
        Object.keys(params).forEach(key => {
          if (params[key] === undefined || params[key] === '' || params[key] === null) {
            delete params[key];
          }
        });
        return exportLoginlog(params);
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.ms-excel' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        const now = new Date();
        const timestamp = now.toISOString().slice(0, 19).replace(/:/g, '-');
        const filename = `登录日志数据_${timestamp}.xlsx`;
        link.setAttribute('download', filename);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.exportLoading = false;
        this.$modal.msgSuccess("导出成功");
      }).catch(error => {
        this.exportLoading = false;
        this.$modal.msgError("导出失败: " + (error.message || '未知错误'));
      });
    },

    /** 【新增】打印操作 */
    handlePrint() {
      window.print();
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
  display: flex;
  align-items: center;
  justify-content: space-between;
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

/* 4. 辅助操作按钮 (返回/重置/调试)：白底灰边，黑字 */
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

/* Tags */
.app-container >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

.empty-data {
  text-align: center;
  padding: 40px 0;
}

.empty-data p {
  margin: 5px 0;
  color: #86868b;
}

/* 操作信息样式 */
.success-msg {
  color: #34c759;
  font-weight: 500;
}

.error-msg {
  color: #ff3b30;
  font-weight: 500;
}

.warning-msg {
  color: #ff9500;
  font-weight: 500;
}

/* 退出成功消息样式 */
.logout-msg {
  color: #ff9500;
  font-weight: 500;
  font-style: italic;
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

<style>
.debug-modal .el-message-box__message {
  white-space: pre-wrap;
  font-family: monospace;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}
</style>
