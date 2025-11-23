<template>
  <div class="app-container">
    <div class="no-print">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
        <el-form-item label="待办标题" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入待办标题"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="待办类型" prop="todoType">
          <el-select v-model="queryParams.todoType" placeholder="待办类型" clearable style="width: 240px">
            <el-option label="学习" value="study" />
            <el-option label="工作" value="work" />
            <el-option label="生活" value="life" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 240px">
            <el-option label="未完成" value="0" />
            <el-option label="完成" value="1" />
            <el-option label="过期" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          <el-button v-if="hasInvalidData" type="warning" icon="el-icon-warning" @click="showDataDiagnosis">
            数据诊断 ({{ invalidDataCount }})
          </el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" icon="el-icon-edit" :disabled="single" @click="handleUpdate">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete" :disabled="multiple" @click="handleDelete">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" icon="el-icon-download" @click="handleExport">导出</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="primary" plain icon="el-icon-printer" @click="handlePrint">打印</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="info" icon="el-icon-s-data" @click="handleStats">统计</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button icon="el-icon-back" @click="handleBack">返回</el-button>
        </el-col>
      </el-row>
    </div>

    <h2 class="print-title" style="display: none;">待办事项列表</h2>

    <el-table
      v-loading="loading"
      :data="todoList"
      @selection-change="handleSelectionChange"
      row-key="todoId"
      class="print-table"
    >
      <el-table-column type="selection" width="55" align="center" class-name="no-print-col" />
      <el-table-column label="待办ID" align="center" prop="todoId" width="100">
        <template slot-scope="scope">
          <div class="todo-id-cell">
            <span :class="{ 'invalid-id': !scope.row.todoId }">
              {{ scope.row.todoId || '无ID' }}
            </span>
            <el-tag v-if="!scope.row.todoId" size="mini" type="danger" class="no-print">ID异常</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="todoType" :formatter="typeFormat" width="100" />
      <el-table-column label="优先级" align="center" prop="priority" :formatter="priorityFormat" width="100" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" :formatter="statusFormat" width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width no-print-col" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleRowUpdate(scope.$index, scope.row)"
            :disabled="!isRowValid(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleRowDelete(scope.$index, scope.row)"
            :disabled="!isRowValid(scope.row)"
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

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="待办标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入待办标题" />
        </el-form-item>
        <el-form-item label="待办内容" prop="content">
          <el-input v-model="form.content" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="待办类型" prop="todoType">
          <el-select v-model="form.todoType" placeholder="请选择待办类型">
            <el-option label="学习" value="study" />
            <el-option label="工作" value="work" />
            <el-option label="生活" value="life" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级">
            <el-option label="低" value="0" />
            <el-option label="中" value="1" />
            <el-option label="高" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">未完成</el-radio>
            <el-radio label="1">完成</el-radio>
            <el-radio label="2">过期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="数据诊断" :visible.sync="diagnosisOpen" width="800px" append-to-body>
      <el-alert
        title="数据异常诊断报告"
        type="warning"
        description="发现部分数据缺少ID，这可能是后端字段映射问题。请检查后端TodoMapper中的字段映射配置。"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-table :data="invalidData" border v-loading="loading">
        <el-table-column label="索引" align="center" width="60">
          <template slot-scope="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="标题" align="center" prop="title" />
        <el-table-column label="类型" align="center" prop="todoType" :formatter="typeFormat" />
        <el-table-column label="状态" align="center" prop="status" :formatter="statusFormat" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120">
          <template slot-scope="scope">
            <el-button type="text" @click="showRawData(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; padding: 10px; background: #f5f5f5; border-radius: 4px;">
        <h4>修复建议：</h4>
        <p>1. 检查后端 TodoMapper 中的字段映射，确保 @Result(column = "todo_id", property = "todoId") 存在</p>
        <p>2. 检查数据库 class_todo 表中的数据完整性</p>
        <p>3. 重启后端服务确保映射生效</p>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="diagnosisOpen = false">关闭</el-button>
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
      </div>
    </el-dialog>

    <el-dialog title="原始数据详情" :visible.sync="rawDataOpen" width="600px" append-to-body>
      <pre style="background: #f5f5f5; padding: 15px; border-radius: 4px; max-height: 400px; overflow: auto; font-size: 12px;">{{ JSON.stringify(currentRawData, null, 2) }}</pre>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rawDataOpen = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="待办事项统计" :visible.sync="statsOpen" width="700px" append-to-body>
      <el-form :model="statsQueryParams" ref="statsQueryForm" :inline="true" label-width="80px">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="statsQueryParams.startTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择开始时间"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="statsQueryParams.endTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleStatsQuery">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetStatsQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="stats-cards">
        <el-row :gutter="20">
          <el-col :span="8" v-for="(stat, index) in statsData" :key="index">
            <el-card class="stats-card" shadow="hover">
              <div class="stats-value">{{ stat.value }}</div>
              <div class="stats-label">{{ stat.label }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <el-table :data="statsTableData" v-loading="statsLoading" border style="margin-top: 20px;">
        <el-table-column label="分类" align="center" prop="category" />
        <el-table-column label="项目" align="center" prop="name" />
        <el-table-column label="数量" align="center" prop="value" />
        <el-table-column label="占比" align="center">
          <template slot-scope="scope">
            <span>{{ ((scope.row.value / statsTotal) * 100).toFixed(2) }}%</span>
          </template>
        </el-table-column>
      </el-table>

      <div slot="footer" class="dialog-footer">
        <el-button @click="statsOpen = false">取 消</el-button>
        <el-button type="primary" @click="handleExportStats">导出统计</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTodo, getTodo, delTodo, delTodos, addTodo, updateTodo, exportTodo, getTodoStatsDetail, exportStats } from "@/api/proj_cyq/todo";

export default {
  name: "Todo",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 选中的行数据
      selection: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 待办表格数据
      todoList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 诊断对话框
      diagnosisOpen: false,
      // 原始数据对话框
      rawDataOpen: false,
      // 当前查看的原始数据
      currentRawData: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        todoType: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "待办标题不能为空", trigger: "blur" }
        ],
        endTime: [
          { required: true, message: "结束时间不能为空", trigger: "change" }
        ]
      },

      // 统计相关
      statsOpen: false,
      statsLoading: false,
      statsQueryParams: {
        startTime: undefined,
        endTime: undefined
      },
      statsTableData: [],
      statsTotal: 0,
      statsData: [],

      // 导出加载状态
      exportLoading: false
    };
  },
  computed: {
    // 无效数据（没有todoId的数据）
    invalidData() {
      return this.todoList.filter(item => !item.todoId);
    },
    // 无效数据数量
    invalidDataCount() {
      return this.invalidData.length;
    },
    // 是否有无效数据
    hasInvalidData() {
      return this.invalidDataCount > 0;
    }
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询待办列表 */
    getList() {
      this.loading = true;
      listTodo(this.queryParams).then(response => {
        this.todoList = response.rows || [];
        this.total = response.total;
        this.loading = false;

        console.log('待办列表数据:', this.todoList);

        // 检查数据异常
        if (this.hasInvalidData) {
          this.$message.warning(`发现 ${this.invalidDataCount} 条数据异常（无ID），请点击"数据诊断"查看详情`);
        }
      }).catch(error => {
        console.error('获取待办列表失败:', error);
        this.loading = false;
        this.$modal.msgError("获取待办列表失败");
      });
    },
    /** 检查行数据是否有效 */
    isRowValid(row) {
      return row && row.todoId;
    },
    /** 显示数据诊断 */
    showDataDiagnosis() {
      this.diagnosisOpen = true;
    },
    /** 显示原始数据 */
    showRawData(row) {
      this.currentRawData = row;
      this.rawDataOpen = true;
    },
    /** 刷新数据 */
    refreshData() {
      this.getList();
      this.diagnosisOpen = false;
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        todoId: undefined,
        title: undefined,
        content: undefined,
        todoType: undefined,
        priority: "0",
        startTime: undefined,
        endTime: undefined,
        status: "0",
        remark: undefined
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      // 只选择有效的数据
      this.selection = selection.filter(item => this.isRowValid(item));
      this.ids = this.selection.map(item => item.todoId);
      this.single = this.selection.length !== 1;
      this.multiple = !this.selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加待办事项";
    },
    /** 行修改按钮操作 */
    handleRowUpdate(index, row) {
      if (!this.isRowValid(row)) {
        this.$modal.msgError("该条数据ID异常，无法修改");
        return;
      }

      const todoId = row.todoId;

      this.reset();
      this.open = true;
      this.title = "修改待办事项";

      // 从后端获取数据
      getTodo(todoId).then(response => {
        if (response.data) {
          this.form = response.data;
        } else {
          this.$modal.msgError("获取待办详情失败");
          this.open = false;
        }
      }).catch(error => {
        console.error('获取待办详情失败:', error);
        this.$modal.msgError("获取待办详情失败");
        this.open = false;
      });
    },
    /** 顶部修改按钮操作 */
    handleUpdate() {
      if (this.single) {
        this.$modal.msgError("请选择一条要修改的数据");
        return;
      }

      if (this.selection.length === 1) {
        const row = this.selection[0];
        this.handleRowUpdate(0, row);
      }
    },
    /** 返回个人中心 */
    handleBack() {
      this.$router.push("/proj_cyq");
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.todoId != undefined) {
            updateTodo(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            }).catch(error => {
              console.error('修改失败:', error);
              this.$modal.msgError("修改失败: " + (error.message || '未知错误'));
            });
          } else {
            addTodo(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            }).catch(error => {
              console.error('新增失败:', error);
              this.$modal.msgError("新增失败: " + (error.message || '未知错误'));
            });
          }
        }
      });
    },
    /** 行删除按钮操作 */
    handleRowDelete(index, row) {
      if (!this.isRowValid(row)) {
        this.$modal.msgError("该条数据ID异常，无法删除");
        return;
      }

      const todoId = row.todoId;
      this.$modal.confirm('是否确认删除待办事项"' + (row.title || '未知') + '"？').then(() => {
        return delTodo(todoId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch((error) => {
        console.error('删除失败:', error);
        this.$modal.msgError("删除失败: " + (error.message || '未知错误'));
      });
    },
    /** 顶部删除按钮操作 */
    handleDelete() {
      if (this.multiple) {
        this.$modal.msgError("请选择要删除的数据");
        return;
      }

      const validIds = this.ids;
      if (validIds.length === 0) {
        this.$modal.msgError("请选择要删除的待办事项");
        return;
      }

      if (validIds.length === 1) {
        const row = this.selection[0];
        this.handleRowDelete(0, row);
      } else {
        this.$modal.confirm('是否确认删除选中的' + validIds.length + '个待办事项？').then(() => {
          return delTodos(validIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch((error) => {
          console.error('批量删除失败:', error);
          this.$modal.msgError("批量删除失败: " + (error.message || '未知错误'));
        });
      }
    },
    /** 导出按钮操作 */
    handleExport() {
      const queryParams = this.queryParams;
      this.$modal.confirm('是否确认导出所有待办事项数据项？').then(() => {
        this.exportLoading = true;
        return exportTodo(queryParams);
      }).then(response => {
        if (response instanceof Blob) {
          const blob = new Blob([response]);
          const downloadElement = document.createElement('a');
          const href = window.URL.createObjectURL(blob);
          downloadElement.href = href;
          downloadElement.download = '待办事项数据.xlsx';
          document.body.appendChild(downloadElement);
          downloadElement.click();
          document.body.removeChild(downloadElement);
          window.URL.revokeObjectURL(href);
        }
        this.exportLoading = false;
        this.$modal.msgSuccess("导出成功");
      }).catch(() => {
        this.exportLoading = false;
      });
    },
    /** 【新增】打印预览功能 */
    handlePrint() {
      window.print();
    },
    /** 统计按钮操作 */
    handleStats() {
      this.statsOpen = true;
      this.handleStatsQuery();
    },
    /** 统计查询 */
    handleStatsQuery() {
      this.statsLoading = true;

      const queryParams = {
        ...this.statsQueryParams
      };

      Object.keys(queryParams).forEach(key => {
        if (queryParams[key] === undefined || queryParams[key] === null || queryParams[key] === '') {
          delete queryParams[key];
        }
      });

      getTodoStatsDetail(queryParams).then(response => {
        // 添加数据验证
        if (response.data && Array.isArray(response.data)) {
          this.statsTableData = response.data.map(item => {
            // 确保每个item都有必要的字段
            return {
              category: item.category || '未知',
              name: item.name || '未知',
              value: item.value || 0
            };
          });
          this.statsTotal = this.statsTableData.reduce((sum, item) => sum + parseInt(item.value || 0), 0);
          this.generateStatsCards();
          this.$modal.msgSuccess("统计查询成功");
        } else {
          this.statsTableData = [];
          this.statsTotal = 0;
          this.statsData = [];
          this.$modal.msgWarning("暂无统计数据");
        }
        this.statsLoading = false;
      }).catch(error => {
        console.error('统计查询失败:', error);
        this.statsLoading = false;
        this.statsTableData = [];
        this.statsTotal = 0;
        this.statsData = [];
        this.$modal.msgError("统计查询失败: " + (error.message || '未知错误'));
      });
    },
    /** 生成统计卡片数据 */
    generateStatsCards() {
      this.statsData = [];
      const typeStats = this.statsTableData.filter(item => item.category === '类型');
      typeStats.forEach(stat => {
        this.statsData.push({
          label: stat.name,
          value: stat.value
        });
      });
      const statusStats = this.statsTableData.filter(item => item.category === '状态');
      statusStats.forEach(stat => {
        this.statsData.push({
          label: stat.name,
          value: stat.value
        });
      });
      const priorityStats = this.statsTableData.filter(item => item.category === '优先级');
      priorityStats.forEach(stat => {
        this.statsData.push({
          label: stat.name,
          value: stat.value
        });
      });
    },
    /** 重置统计查询 */
    resetStatsQuery() {
      this.resetForm("statsQueryForm");
      this.handleStatsQuery();
    },
    /** 导出统计结果 */
    handleExportStats() {
      this.$modal.confirm('是否确认导出统计结果？').then(() => {
        return exportStats(this.statsQueryParams);
      }).then(response => {
        if (response instanceof Blob) {
          const blob = new Blob([response]);
          const downloadElement = document.createElement('a');
          const href = window.URL.createObjectURL(blob);
          downloadElement.href = href;
          downloadElement.download = '待办事项统计.xlsx';
          document.body.appendChild(downloadElement);
          downloadElement.click();
          document.body.removeChild(downloadElement);
          window.URL.revokeObjectURL(href);
        }
        this.$modal.msgSuccess("导出成功");
      }).catch(() => {});
    },
    // 类型字典翻译
    typeFormat(row, column) {
      if (row.todoType == 'study') {
        return '学习';
      } else if (row.todoType == 'work') {
        return '工作';
      } else if (row.todoType == 'life') {
        return '生活';
      } else if (row.todoType == 'other') {
        return '其他';
      } else {
        return row.todoType;
      }
    },
    // 优先级字典翻译
    priorityFormat(row, column) {
      if (row.priority == '0') {
        return '低';
      } else if (row.priority == '1') {
        return '中';
      } else if (row.priority == '2') {
        return '高';
      } else {
        return row.priority;
      }
    },
    // 状态字典翻译
    statusFormat(row, column) {
      if (row.status == '0') {
        return '未完成';
      } else if (row.status == '1') {
        return '完成';
      } else if (row.status == '2') {
        return '过期';
      } else {
        return row.status;
      }
    },
    // 时间格式化
    parseTime(time) {
      if (!time) return '';
      try {
        const date = new Date(time);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
      } catch (e) {
        console.error('时间格式化错误:', e);
        return String(time);
      }
    }
  }
};
</script>

<style scoped>
.stats-cards {
  margin: 20px 0;
}

.stats-card {
  text-align: center;
  margin-bottom: 20px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.todo-id-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.invalid-id {
  color: #f56c6c;
  font-style: italic;
}

/* ==============================
   【新增】打印样式专用设置
   ============================== */
@media print {
  /* 1. 隐藏所有不需要打印的元素 */
  .no-print,
  .navbar,
  .sidebar-container,
  .tags-view-container,
  .el-dialog__wrapper,
  .v-modal,
  .el-pagination {
    display: none !important;
  }

  /* 隐藏表格中的操作列和选择列 */
  .no-print-col {
    display: none !important;
  }
  .el-table__fixed-right {
    display: none !important;
  }

  /* 2. 显示打印标题 */
  .print-title {
    display: block !important;
    text-align: center;
    font-size: 24px;
    margin-bottom: 20px;
    font-weight: bold;
  }

  /* 3. 调整容器宽度，利用整个纸张 */
  .app-container {
    padding: 0;
    margin: 0;
    width: 100% !important;
  }

  /* 移除Element UI表格的固定高度和滚动条 */
  .el-table {
    height: auto !important;
    width: 100% !important;
  }
  .el-table__body-wrapper,
  .el-table__header-wrapper {
    height: auto !important;
    overflow: visible !important;
  }

  /* 4. 表格样式优化：黑白、边框 */
  .print-table {
    border: 1px solid #000 !important;
    font-size: 12px; /* 调整字号以容纳更多内容 */
  }

  /* 强制显示边框 */
  .print-table td,
  .print-table th {
    border: 1px solid #000 !important;
    color: #000 !important; /* 纯黑文字 */
    padding: 8px 5px !important; /* 减少内边距 */
  }

  /* 隐藏Element默认的浅色边框 */
  .el-table::before,
  .el-table::after {
    display: none;
  }

  /* 5. 避免分页截断表格行 */
  tr {
    page-break-inside: avoid;
  }

  /* 隐藏标签的背景色，只打印文字 */
  .el-tag {
    border: none !important;
    background: none !important;
    padding: 0 !important;
    color: #000 !important;
  }
}
</style>
