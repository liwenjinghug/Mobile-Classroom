<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix notice-header no-print">
            <span class="header-title">登录日志</span>
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

    <el-dialog title="登录日志统计" :visible.sync="statsOpen" width="800px" append-to-body class="no-print">
      <div v-loading="statsLoading">
        <el-row :gutter="20" class="stats-overview">
          <el-col :span="24" style="text-align: center; margin-bottom: 20px;">
            <div class="stat-value" style="font-size: 36px; color: #409EFF;">{{ stats.totalCount }}</div>
            <div class="stat-label">总登录次数</div>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="24">
            <h4 class="stats-subtitle">登录状态分布</h4>
            <div style="display: flex; justify-content: center; gap: 40px;">
              <div v-for="item in stats.statusStats" :key="item.name" class="stat-box" :class="item.name === '成功' ? 'success' : 'danger'">
                <div class="stat-name">{{ item.name }}</div>
                <div class="stat-num">{{ item.value }}</div>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <h4 class="stats-subtitle">浏览器统计</h4>
            <el-table :data="stats.browserStats" border size="mini" height="250">
              <el-table-column prop="name" label="浏览器" align="center"/>
              <el-table-column prop="value" label="次数" align="center" width="80"/>
            </el-table>
          </el-col>
          <el-col :span="12">
            <h4 class="stats-subtitle">操作系统统计</h4>
            <el-table :data="stats.osStats" border size="mini" height="250">
              <el-table-column prop="name" label="操作系统" align="center"/>
              <el-table-column prop="value" label="次数" align="center" width="80"/>
            </el-table>
          </el-col>
        </el-row>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="statsOpen = false" class="mac-btn">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listLoginlog, delLoginlog, cleanLoginlog, exportLoginlog, getLoginLogStats } from "@/api/proj_cyq/loginlog";

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
      },

      // 【新增】统计相关数据
      statsOpen: false,
      statsLoading: false,
      stats: {
        totalCount: 0,
        statusStats: [],
        browserStats: [],
        osStats: []
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

    /** 【新增】处理统计 */
    handleStats() {
      this.statsOpen = true;
      this.statsLoading = true;
      getLoginLogStats().then(response => {
        if (response.code === 200) {
          this.stats = response.data;
        }
        this.statsLoading = false;
      }).catch(() => {
        this.statsLoading = false;
        this.$modal.msgError("获取统计数据失败");
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
      this.ids = selection.map(item => item.loginId)
      this.multiple = !selection.length
    },

    handleDelete(row) {
      const loginIds = row.loginId || this.ids;
      this.$modal.confirm('是否确认删除登录日志编号为"' + loginIds + '"的数据项？').then(() => {
        return delLoginlog(loginIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    handleClean() {
      this.$modal.confirm('是否确认清空所有登录日志数据项？').then(() => {
        return cleanLoginlog();
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("清空成功");
      }).catch(() => {});
    },

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

<style>
.debug-modal .el-message-box__message {
  white-space: pre-wrap;
  font-family: monospace;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}
</style>
