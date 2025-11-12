<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span class="header-title">登录日志</span>
            <el-button style="float: right;" type="primary" icon="el-icon-arrow-left" @click="handleBack">
              返回
            </el-button>
          </div>

          <!-- 调试信息 -->
          <el-alert
            v-if="debugInfo.show"
            :title="debugInfo.title"
            :type="debugInfo.type"
            :description="debugInfo.description"
            show-icon
            closable
            @close="debugInfo.show = false"
            style="margin-bottom: 15px;"
          />

          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
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
              <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
              <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
              <el-button type="info" icon="el-icon-info" size="small" @click="showDebugInfo">调试信息</el-button>
            </el-form-item>
          </el-form>

          <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button
                type="danger"
                plain
                icon="el-icon-delete"
                size="small"
                :disabled="multiple"
                @click="handleDelete"
              >删除</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="danger"
                plain
                icon="el-icon-delete"
                size="small"
                @click="handleClean"
              >清空</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="warning"
                plain
                icon="el-icon-download"
                size="small"
                @click="handleExport"
                :loading="exportLoading"
              >导出</el-button>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </el-row>

          <el-table v-loading="loading" :data="loginlogList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
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
          />

          <!-- 空数据提示 -->
          <div v-if="!loading && loginlogList.length === 0" class="empty-data">
            <el-empty description="暂无登录日志数据">
              <div slot="description">
                <p>暂无登录日志数据，可能的原因：</p>
                <p>1. 还没有用户登录过系统</p>
                <p>2. 登录日志记录功能未正常工作</p>
                <p>3. 数据库连接异常</p>
                <el-button type="primary" @click="getList">重新加载</el-button>
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
      console.log('查询参数:', this.queryParams);

      listLoginlog(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        console.log('API响应数据:', response);

        if (response && response.code === 200) {
          this.loginlogList = response.rows || [];
          this.total = response.total || 0;

          console.log('加载的登录日志数据:', this.loginlogList);
          console.log('总记录数:', this.total);

          // 检查是否有退出成功记录
          const logoutRecords = this.loginlogList.filter(item =>
            item.msg && (item.msg.includes('退出成功') || item.msg.includes('退出系统'))
          );
          console.log('退出成功记录数量:', logoutRecords.length);

          if (this.loginlogList.length === 0) {
            this.showDebugAlert('warning', '数据为空', '当前没有登录日志数据，请确认：1. 用户已登录过系统 2. 登录日志记录功能正常');
          } else {
            this.debugInfo.show = false;
          }
        } else {
          console.error('API返回错误:', response);
          this.showDebugAlert('error', 'API错误', `接口返回错误: ${response.msg || '未知错误'}`);
          this.loginlogList = [];
          this.total = 0;
        }

        this.loading = false;
      }).catch(error => {
        console.error('获取登录日志列表失败:', error);
        this.loading = false;
        this.loginlogList = [];
        this.total = 0;
        this.showDebugAlert('error', '请求失败', `获取数据失败: ${error.message || '未知错误'}`);
      });
    },

    /** 显示调试信息 */
    showDebugInfo() {
      const logoutCount = this.loginlogList.filter(item =>
        item.msg && (item.msg.includes('退出成功') || item.msg.includes('退出系统'))
      ).length;

      const info = `
当前状态：
- 数据条数: ${this.loginlogList.length}
- 退出成功记录: ${logoutCount}
- 总记录数: ${this.total}
- 加载状态: ${this.loading ? '加载中' : '完成'}
- 查询参数: ${JSON.stringify(this.queryParams)}
- 字典数据: ${this.statusOptions.length} 条

退出成功记录检查：
${this.loginlogList.filter(item => item.msg && item.msg.includes('退出')).map(item =>
        `- 用户: ${item.userName}, 消息: ${item.msg}, 状态: ${item.status}, 时间: ${this.parseTime(item.loginTime)}`
      ).join('\n') || '暂无退出记录'}

建议检查：
1. 确认用户已登录并退出过系统
2. 检查浏览器控制台网络请求
3. 查看服务器日志确认切面是否工作
4. 确认数据库表 class_login_log 存在退出记录
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
        // 如果是退出成功，显示为退出成功，否则显示登录成功
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

      // 优先判断退出成功
      if (msg.includes('退出成功')) {
        return 'logout-msg';  // 退出成功使用特殊样式
      } else if (msg.includes('成功') && !msg.includes('退出成功')) {
        return 'success-msg';
      } else if (msg.includes('错误') || msg.includes('失败')) {
        return 'error-msg';
      } else if (msg.includes('退出') || msg.includes('登出')) {
        return 'logout-msg';  // 其他退出相关消息
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

        // 构建查询参数
        const params = {
          ...this.queryParams,
          // 添加日期范围参数
          beginTime: this.dateRange && this.dateRange[0] ? this.dateRange[0] : undefined,
          endTime: this.dateRange && this.dateRange[1] ? this.dateRange[1] : undefined
        };

        // 移除空参数
        Object.keys(params).forEach(key => {
          if (params[key] === undefined || params[key] === '' || params[key] === null) {
            delete params[key];
          }
        });

        console.log('导出参数:', params);

        return exportLoginlog(params);
      }).then(response => {
        // 创建Blob对象并下载
        const blob = new Blob([response], { type: 'application/vnd.ms-excel' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;

        // 设置文件名，包含当前时间
        const now = new Date();
        const timestamp = now.toISOString().slice(0, 19).replace(/:/g, '-');
        const filename = `登录日志数据_${timestamp}.xlsx`;
        link.setAttribute('download', filename);

        document.body.appendChild(link);
        link.click();

        // 清理
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        this.exportLoading = false;
        this.$modal.msgSuccess("导出成功");
      }).catch(error => {
        console.error('导出失败:', error);
        this.exportLoading = false;
        this.$modal.msgError("导出失败: " + (error.message || '未知错误'));
      });
    },

    /** 返回按钮操作 */
    handleBack() {
      this.$router.push('/proj_cyq/index');
    }
  }
};
</script>

<style scoped>
.header-title {
  font-size: 18px;
  font-weight: bold;
}

.empty-data {
  text-align: center;
  padding: 40px 0;
}

.empty-data p {
  margin: 5px 0;
  color: #909399;
}

/* 操作信息样式 */
.success-msg {
  color: #67c23a;
  font-weight: 500;
}

.error-msg {
  color: #f56c6c;
  font-weight: 500;
}

.warning-msg {
  color: #e6a23c;
  font-weight: 500;
}

/* 退出成功消息样式 */
.logout-msg {
  color: #e6a23c;
  font-weight: 500;
  font-style: italic;
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
