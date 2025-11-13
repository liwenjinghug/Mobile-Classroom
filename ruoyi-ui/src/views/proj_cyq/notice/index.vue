<!-- src/views/proj_cyq/notice/index.vue -->
<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="notice-header">
          <span class="notice-title">通告管理</span>
          <el-button
            v-hasPermi="['proj_cyq:notice:add']"
            type="primary"
            plain
            icon="el-icon-plus"
            size="mini"
            @click="handleAdd"
            style="float: right; margin-left: 10px;"
          >
            新增
          </el-button>
          <el-button
            type="info"
            plain
            icon="el-icon-arrow-left"
            size="mini"
            @click="handleBack"
            style="float: right;"
          >
            返回
          </el-button>
        </div>

        <el-card class="box-card">
          <!-- 搜索区域 -->
          <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
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
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 操作按钮 -->
          <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button
                type="warning"
                plain
                icon="el-icon-download"
                size="mini"
                @click="handleExport"
                v-hasPermi="['proj_cyq:notice:export']"
                :loading="exportLoading"
              >导出</el-button>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </el-row>

          <!-- 公告表格 -->
          <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="公告标题" align="center" prop="title" :show-overflow-tooltip="true" />
            <el-table-column label="创建人" align="center" prop="createBy" width="120" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="180">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
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
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加或修改公告对话框 -->
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
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 公告详情对话框 -->
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
        <el-button @click="viewOpen = false">关 闭</el-button>
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
    /** 导出按钮操作 - 参考操作日志的方式 */
    handleExport() {
      this.$modal.confirm('是否确认导出所有公告数据项？').then(() => {
        this.exportLoading = true;
        // 不传递任何参数，使用 GET 请求
        return exportNotice();
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
        downloadElement.download = '通知公告数据.xlsx';
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
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.noticeId);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    /** 返回按钮操作 */
    handleBack() {
      this.$router.push('/index');
    }
  }
};
</script>

<style scoped>
.notice-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.notice-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.notice-content {
  border: 1px solid #ddd;
  padding: 15px;
  border-radius: 4px;
  background-color: #f9f9f9;
  min-height: 100px;
}
</style>
