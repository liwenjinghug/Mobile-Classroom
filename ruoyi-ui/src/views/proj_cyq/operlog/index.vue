<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span class="header-title">操作日志</span>
            <el-button style="float: right;" type="primary" icon="el-icon-arrow-left" @click="handleBack">
              返回
            </el-button>
          </div>

          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
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

          <el-table v-loading="loading" :data="operlogList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="日志编号" align="center" prop="operId" />
            <el-table-column label="系统模块" align="center" prop="title" />
            <el-table-column label="操作类型" align="center" prop="businessType" :formatter="businessTypeFormat" />
            <el-table-column label="请求方式" align="center" prop="requestMethod" />
            <el-table-column label="操作人员" align="center" prop="operName" />
            <el-table-column label="操作地址" align="center" prop="operIp" width="130" :show-overflow-tooltip="true" />
            <el-table-column label="操作地点" align="center" prop="operLocation" :show-overflow-tooltip="true" />
            <el-table-column label="操作状态" align="center" prop="status" :formatter="statusFormat" />
            <el-table-column label="操作日期" align="center" prop="operTime" width="180">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.operTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
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
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作日志详细 -->
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
        <el-button @click="open = false">关 闭</el-button>
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
      // 类型数据字典 - 使用硬编码
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
      // 状态数据字典 - 使用硬编码
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

    /** 导出按钮操作 - 修复版本 */
    handleExport() {
      this.$modal.confirm('是否确认导出所有操作日志数据项？').then(() => {
        this.exportLoading = true;
        // 不传递任何参数，使用 GET 请求
        return exportOperlog();
      }).then(response => {
        // 直接使用响应数据创建下载
        this.handleExportResponse(response);
      }).catch((error) => {
        console.error('导出失败:', error);
        this.exportLoading = false;
        this.$modal.msgError("导出失败：" + (error.message || '未知错误'));
      });
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
/* Mac Style for Operlog Page */
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

/* Button Styling */
.app-container >>> .el-button {
  border-radius: 980px;
  font-weight: 500;
  border: none;
  padding: 9px 20px;
  transition: all 0.2s ease;
}

.app-container >>> .el-button--primary {
  background-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}
.app-container >>> .el-button--primary:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

.app-container >>> .el-button--success {
  background-color: #34c759;
  box-shadow: 0 2px 8px rgba(52, 199, 89, 0.2);
}

.app-container >>> .el-button--warning {
  background-color: #ff9500;
  box-shadow: 0 2px 8px rgba(255, 149, 0, 0.2);
}

.app-container >>> .el-button--danger {
  background-color: #ff3b30;
  box-shadow: 0 2px 8px rgba(255, 59, 48, 0.2);
}

.app-container >>> .el-button--info {
  background-color: #8e8e93;
}

/* Table Styling */
.app-container >>> .el-table {
  border-radius: 12px;
  overflow: hidden;
}

.app-container >>> .el-table th {
  background-color: #fbfbfd;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #f5f5f7;
  padding: 12px 0;
}

.app-container >>> .el-table td {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
}

/* Dialog Styling */
.app-container >>> .el-dialog {
  border-radius: 18px;
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
</style>
