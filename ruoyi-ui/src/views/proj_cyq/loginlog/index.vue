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
                v-hasPermi="['proj_cyq:loginlog:remove']"
              >删除</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="danger"
                plain
                icon="el-icon-delete"
                size="small"
                @click="handleClean"
                v-hasPermi="['proj_cyq:loginlog:remove']"
              >清空</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="warning"
                plain
                icon="el-icon-download"
                size="small"
                @click="handleExport"
                v-hasPermi="['proj_cyq:loginlog:export']"
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
            <el-table-column label="登录状态" align="center" prop="status" :formatter="statusFormat" />
            <el-table-column label="操作信息" align="center" prop="msg" />
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
        status: undefined
      }
    };
  },
  created() {
    this.getList();
    this.getDicts("sys_common_status").then(response => {
      this.statusOptions = response.data;
    });
  },
  methods: {
    /** 查询系统登录日志列表 */
    getList() {
      this.loading = true;
      listLoginlog(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.loginlogList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 登录状态字典翻译
    statusFormat(row, column) {
      return this.selectDictLabel(this.statusOptions, row.status);
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
      this.$modal.confirm('是否确认删除登录日志编号为"' + loginIds + '"的数据项？').then(function() {
        return delLoginlog(loginIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 清空按钮操作 */
    handleClean() {
      this.$modal.confirm('是否确认清空所有登录日志数据项？').then(function() {
        return cleanLoginlog();
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("清空成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      const queryParams = this.queryParams;
      this.$modal.confirm('是否确认导出所有登录日志数据项？').then(() => {
        this.exportLoading = true;
        return exportLoginlog(queryParams);
      }).then(response => {
        this.$download.name(response.msg);
        this.exportLoading = false;
      }).catch(() => {});
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
</style>
