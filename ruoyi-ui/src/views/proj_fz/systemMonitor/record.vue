<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="监控类型" prop="monitorType">
        <el-select v-model="queryParams.monitorType" placeholder="请选择监控类型" clearable>
          <el-option label="服务器监控" value="1" />
          <el-option label="数据库监控" value="2" />
          <el-option label="用户行为监控" value="3" />
          <el-option label="功能模块监控" value="4" />
          <el-option label="接口性能监控" value="5" />
          <el-option label="异常监控" value="6" />
        </el-select>
      </el-form-item>
      <el-form-item label="监控项名称" prop="monitorName">
        <el-input
          v-model="queryParams.monitorName"
          placeholder="请输入监控项名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="告警级别" prop="alertLevel">
        <el-select v-model="queryParams.alertLevel" placeholder="请选择告警级别" clearable>
          <el-option label="正常" value="0" />
          <el-option label="警告" value="1" />
          <el-option label="严重" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="异常" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态" prop="handled">
        <el-select v-model="queryParams.handled" placeholder="请选择处理状态" clearable>
          <el-option label="未处理" value="0" />
          <el-option label="已处理" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="监控时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-if="hasRole(['teacher','student']) || hasPermi(['proj_fz:monitor:add'])"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-if="hasRole(['teacher','student']) || hasPermi(['proj_fz:monitor:remove'])"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-if="hasRole(['teacher','student']) || hasPermi(['proj_fz:monitor:export'])"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-printer"
          size="mini"
          @click="handlePrintTable"
          v-if="hasRole(['teacher','student']) || hasPermi(['proj_fz:monitor:query'])"
        >打印</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-refresh"
          size="mini"
          @click="handleCollect"
          v-if="hasRole(['teacher','student']) || hasPermi(['proj_fz:monitor:collect'])"
        >立即采集</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="monitorList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="监控ID" align="center" prop="monitorId" width="80" />
      <el-table-column label="监控类型" align="center" prop="monitorType" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.monitorType == 1" type="primary">服务器监控</el-tag>
          <el-tag v-else-if="scope.row.monitorType == 2" type="success">数据库监控</el-tag>
          <el-tag v-else-if="scope.row.monitorType == 3" type="info">用户行为</el-tag>
          <el-tag v-else-if="scope.row.monitorType == 4" type="warning">功能模块</el-tag>
          <el-tag v-else-if="scope.row.monitorType == 5">接口性能</el-tag>
          <el-tag v-else type="danger">异常监控</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="监控项名称" align="center" prop="monitorName" :show-overflow-tooltip="true" />
      <el-table-column label="告警级别" align="center" prop="alertLevel" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.alertLevel == 0" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.alertLevel == 1" type="warning">警告</el-tag>
          <el-tag v-else type="danger">严重</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="告警描述" align="center" prop="alertDesc" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status == 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">异常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理状态" align="center" prop="handled" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.handled == 0" type="warning">未处理</el-tag>
          <el-tag v-else type="success">已处理</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="监控时间" align="center" prop="monitorTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.monitorTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-if="hasRole(['teacher','student']) || hasPermi(['proj_fz:monitor:query'])"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-if="hasRole(['teacher','student']) || hasPermi(['proj_fz:monitor:edit'])"
          >修改</el-button>
          <el-button
            v-if="(scope.row.handled == 0 && (scope.row.status == 1 || scope.row.alertLevel > 0)) && (hasRole(['teacher','student']) || hasPermi(['proj_fz:monitor:edit']))"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleHandle(scope.row)"
          >处理</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-if="hasRole(['teacher','student']) || hasPermi(['proj_fz:monitor:remove'])"
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

    <!-- 详情对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="监控ID">{{ form.monitorId }}</el-descriptions-item>
        <el-descriptions-item label="监控类型">
          <span v-if="form.monitorType == 1">服务器监控</span>
          <span v-else-if="form.monitorType == 2">数据库监控</span>
          <span v-else-if="form.monitorType == 3">用户行为监控</span>
          <span v-else-if="form.monitorType == 4">功能模块监控</span>
          <span v-else-if="form.monitorType == 5">接口性能监控</span>
          <span v-else>异常监控</span>
        </el-descriptions-item>
        <el-descriptions-item label="监控项名称">{{ form.monitorName }}</el-descriptions-item>
        <el-descriptions-item label="告警级别">
          <el-tag v-if="form.alertLevel == 0" type="success">正常</el-tag>
          <el-tag v-else-if="form.alertLevel == 1" type="warning">警告</el-tag>
          <el-tag v-else type="danger">严重</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="form.status == 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">异常</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="监控时间">{{ parseTime(form.monitorTime) }}</el-descriptions-item>
        <el-descriptions-item label="告警描述" :span="2">{{ form.alertDesc || '无' }}</el-descriptions-item>
        <el-descriptions-item label="监控指标" :span="2">
          <pre style="max-height: 300px; overflow-y: auto;">{{ formatMetrics(form.metrics) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag v-if="form.handled == 0" type="warning">未处理</el-tag>
          <el-tag v-else type="success">已处理</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ form.handler || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" :span="2">{{ parseTime(form.handleTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2">{{ form.handleRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleNavigation('first')" :disabled="currentIndex <= 0">首条</el-button>
        <el-button @click="handleNavigation('prev')" :disabled="currentIndex <= 0">上条</el-button>
        <el-button @click="handleNavigation('next')" :disabled="currentIndex >= total - 1">下条</el-button>
        <el-button @click="handleNavigation('last')" :disabled="currentIndex >= total - 1">末尾</el-button>
        <el-button type="primary" @click="handlePrint">打印</el-button>
        <el-button @click="open = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog title="处理告警" :visible.sync="handleOpen" width="600px" append-to-body>
      <el-form ref="handleForm" :model="handleForm" label-width="100px">
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="4" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitHandle">确 定</el-button>
        <el-button @click="handleOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" :visible.sync="addEditOpen" width="700px" append-to-body>
      <el-form ref="addEditFormRef" :model="addEditForm" :rules="addEditRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="监控类型" prop="monitorType">
              <el-select v-model="addEditForm.monitorType" placeholder="请选择监控类型" style="width: 100%">
                <el-option label="服务器监控" :value="1" />
                <el-option label="数据库监控" :value="2" />
                <el-option label="用户行为监控" :value="3" />
                <el-option label="功能模块监控" :value="4" />
                <el-option label="接口性能监控" :value="5" />
                <el-option label="异常监控" :value="6" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监控项名称" prop="monitorName">
              <el-input v-model="addEditForm.monitorName" placeholder="请输入监控项名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="告警级别" prop="alertLevel">
              <el-select v-model="addEditForm.alertLevel" placeholder="请选择告警级别" style="width: 100%">
                <el-option label="正常" :value="0" />
                <el-option label="警告" :value="1" />
                <el-option label="严重" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="addEditForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="正常" :value="0" />
                <el-option label="异常" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="处理状态" prop="handled">
              <el-select v-model="addEditForm.handled" placeholder="请选择处理状态" style="width: 100%">
                <el-option label="未处理" :value="0" />
                <el-option label="已处理" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="告警描述" prop="alertDesc">
          <el-input v-model="addEditForm.alertDesc" type="textarea" :rows="3" placeholder="请输入告警描述" />
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input v-model="addEditForm.handleRemark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAddEdit">确 定</el-button>
        <el-button @click="cancelAddEdit">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMonitor, getMonitor, addMonitor, updateMonitor, delMonitor, handleAlert, exportMonitor, collectMetrics } from '@/api/proj_fz/systemMonitor'

export default {
  name: 'MonitorRecord',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      monitorList: [],
      title: '',
      open: false,
      handleOpen: false,
      addEditOpen: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        monitorType: null,
        monitorName: null,
        alertLevel: null,
        status: null,
        handled: null
      },
      form: {},
      handleForm: {},
      addEditForm: {
        monitorId: null,
        monitorType: null,
        monitorName: '',
        alertLevel: 0,
        alertDesc: '',
        status: 0,
        handled: 0,
        handleRemark: ''
      },
      addEditRules: {
        monitorType: [
          { required: true, message: '请选择监控类型', trigger: 'change' }
        ],
        monitorName: [
          { required: true, message: '请输入监控项名称', trigger: 'blur' }
        ],
        alertLevel: [
          { required: true, message: '请选择告警级别', trigger: 'change' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      },
      currentIndex: 0
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listMonitor(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.monitorList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.monitorId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleView(row) {
      this.currentIndex = this.monitorList.findIndex(item => item.monitorId === row.monitorId)
      getMonitor(row.monitorId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '监控详情'
      })
    },
    handleNavigation(action) {
      let newIndex = this.currentIndex
      if (action === 'first') {
        newIndex = 0
      } else if (action === 'prev') {
        newIndex = Math.max(0, this.currentIndex - 1)
      } else if (action === 'next') {
        newIndex = Math.min(this.monitorList.length - 1, this.currentIndex + 1)
      } else if (action === 'last') {
        newIndex = this.monitorList.length - 1
      }

      if (newIndex !== this.currentIndex) {
        this.currentIndex = newIndex
        const row = this.monitorList[newIndex]
        getMonitor(row.monitorId).then(response => {
          this.form = response.data
        })
      }
    },
    handlePrint() {
      const printContent = document.querySelector('.el-descriptions')
      const printWindow = window.open('', '_blank')
      printWindow.document.write('<html><head><title>监控详情打印</title>')
      printWindow.document.write('<style>')
      printWindow.document.write('body { font-family: Arial, sans-serif; padding: 20px; }')
      printWindow.document.write('h2 { text-align: center; }')
      printWindow.document.write('.print-header { margin-bottom: 20px; }')
      printWindow.document.write('table { border-collapse: collapse; width: 100%; }')
      printWindow.document.write('table, th, td { border: 1px solid #ddd; }')
      printWindow.document.write('th, td { padding: 8px; text-align: left; }')
      printWindow.document.write('</style>')
      printWindow.document.write('</head><body>')
      printWindow.document.write('<h2>系统监控报表</h2>')
      printWindow.document.write('<div class="print-header">')
      printWindow.document.write('<p>监控时间：' + this.parseTime(this.form.monitorTime) + '</p>')
      printWindow.document.write('<p>打印时间：' + this.parseTime(new Date()) + '</p>')
      printWindow.document.write('</div>')
      printWindow.document.write(printContent.innerHTML)
      printWindow.document.write('</body></html>')
      printWindow.document.close()
      printWindow.print()
    },
    handleHandle(row) {
      this.handleForm = {
        monitorId: row.monitorId,
        handleRemark: ''
      }
      this.handleOpen = true
    },
    submitHandle() {
      handleAlert(this.handleForm).then(response => {
        this.$modal.msgSuccess('处理成功')
        this.handleOpen = false
        this.getList()
      })
    },
    handleDelete(row) {
      const monitorIds = row.monitorId || this.ids
      this.$modal.confirm('是否确认删除监控记录编号为"' + monitorIds + '"的数据项？').then(() => {
        return delMonitor(monitorIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('proj_fz/monitor/export', {
        ...this.queryParams
      }, `monitor_${new Date().getTime()}.xlsx`)
    },
    handleCollect() {
      this.$modal.confirm('是否立即执行监控数据采集？').then(() => {
        return collectMetrics()
      }).then(() => {
        this.$modal.msgSuccess('采集成功')
        this.getList()
      }).catch(() => {})
    },
    formatMetrics(metrics) {
      if (!metrics) return '无'
      try {
        return JSON.stringify(JSON.parse(metrics), null, 2)
      } catch (e) {
        return metrics
      }
    },
    // 判断用户角色（返回 true 如果包含 admin 或 传入列表内任一角色）
    hasRole(roleArr) {
      try {
        const roles = this.$store.getters && this.$store.getters.roles ? this.$store.getters.roles : []
        if (!Array.isArray(roles)) return false
        if (roles.includes('admin')) return true
        return roleArr.some(r => roles.includes(r))
      } catch (e) {
        return false
      }
    },
    // 判断用户权限（返回 true 如果包含 *:*:* 或 传入列表内任一权限）
    hasPermi(perms) {
      try {
        const permissions = this.$store.getters && this.$store.getters.permissions ? this.$store.getters.permissions : []
        if (!Array.isArray(permissions)) return false
        if (permissions.includes('*:*:*')) return true
        return perms.some(p => permissions.includes(p))
      } catch (e) {
        return false
      }
    },
    // 重置新增/修改表单
    resetAddEditForm() {
      this.addEditForm = {
        monitorId: null,
        monitorType: null,
        monitorName: '',
        alertLevel: 0,
        alertDesc: '',
        status: 0,
        handled: 0,
        handleRemark: ''
      }
      if (this.$refs.addEditFormRef) {
        this.$refs.addEditFormRef.resetFields()
      }
    },
    // 新增按钮操作
    handleAdd() {
      this.resetAddEditForm()
      this.title = '新增监控记录'
      this.addEditOpen = true
    },
    // 修改按钮操作
    handleUpdate(row) {
      this.resetAddEditForm()
      const monitorId = row.monitorId || this.ids[0]
      getMonitor(monitorId).then(response => {
        this.addEditForm = {
          monitorId: response.data.monitorId,
          monitorType: response.data.monitorType,
          monitorName: response.data.monitorName,
          alertLevel: response.data.alertLevel,
          alertDesc: response.data.alertDesc,
          status: response.data.status,
          handled: response.data.handled,
          handleRemark: response.data.handleRemark
        }
        this.title = '修改监控记录'
        this.addEditOpen = true
      })
    },
    // 提交新增/修改
    submitAddEdit() {
      this.$refs.addEditFormRef.validate(valid => {
        if (valid) {
          if (this.addEditForm.monitorId) {
            // 修改
            updateMonitor(this.addEditForm).then(response => {
              this.$modal.msgSuccess('修改成功')
              this.addEditOpen = false
              this.getList()
            })
          } else {
            // 新增
            addMonitor(this.addEditForm).then(response => {
              this.$modal.msgSuccess('新增成功')
              this.addEditOpen = false
              this.getList()
            })
          }
        }
      })
    },
    // 取消新增/修改
    cancelAddEdit() {
      this.addEditOpen = false
      this.resetAddEditForm()
    },
    // 获取监控类型名称
    getMonitorTypeName(type) {
      const typeMap = {
        1: '服务器监控',
        2: '数据库监控',
        3: '用户行为监控',
        4: '功能模块监控',
        5: '接口性能监控',
        6: '异常监控'
      }
      return typeMap[type] || '未知'
    },
    // 获取告警级别名称
    getAlertLevelName(level) {
      const levelMap = { 0: '正常', 1: '警告', 2: '严重' }
      return levelMap[level] || '未知'
    },
    // 获取状态名称
    getStatusName(status) {
      return status == 0 ? '正常' : '异常'
    },
    // 获取处理状态名称
    getHandledName(handled) {
      return handled == 0 ? '未处理' : '已处理'
    },
    // 打印表格（根据筛选结果）
    handlePrintTable() {
      const printWindow = window.open('', '_blank')
      printWindow.document.write('<html><head><title>监控记录打印</title>')
      printWindow.document.write('<style>')
      printWindow.document.write('body { font-family: Arial, sans-serif; padding: 20px; }')
      printWindow.document.write('h2 { text-align: center; margin-bottom: 20px; }')
      printWindow.document.write('.print-info { margin-bottom: 15px; color: #666; }')
      printWindow.document.write('table { border-collapse: collapse; width: 100%; }')
      printWindow.document.write('th, td { border: 1px solid #333; padding: 8px; text-align: center; }')
      printWindow.document.write('th { background-color: #f0f0f0; font-weight: bold; }')
      printWindow.document.write('tr:nth-child(even) { background-color: #fafafa; }')
      printWindow.document.write('.status-normal { color: #67c23a; }')
      printWindow.document.write('.status-warning { color: #e6a23c; }')
      printWindow.document.write('.status-danger { color: #f56c6c; }')
      printWindow.document.write('</style>')
      printWindow.document.write('</head><body>')
      printWindow.document.write('<h2>系统监控记录报表</h2>')
      printWindow.document.write('<div class="print-info">')
      printWindow.document.write('<p>打印时间：' + this.parseTime(new Date()) + '</p>')
      printWindow.document.write('<p>记录总数：' + this.monitorList.length + ' 条</p>')
      printWindow.document.write('</div>')
      printWindow.document.write('<table>')
      printWindow.document.write('<thead><tr>')
      printWindow.document.write('<th>监控ID</th>')
      printWindow.document.write('<th>监控类型</th>')
      printWindow.document.write('<th>监控项名称</th>')
      printWindow.document.write('<th>告警级别</th>')
      printWindow.document.write('<th>告警描述</th>')
      printWindow.document.write('<th>状态</th>')
      printWindow.document.write('<th>处理状态</th>')
      printWindow.document.write('<th>监控时间</th>')
      printWindow.document.write('</tr></thead>')
      printWindow.document.write('<tbody>')
      
      this.monitorList.forEach(item => {
        printWindow.document.write('<tr>')
        printWindow.document.write('<td>' + item.monitorId + '</td>')
        printWindow.document.write('<td>' + this.getMonitorTypeName(item.monitorType) + '</td>')
        printWindow.document.write('<td>' + (item.monitorName || '-') + '</td>')
        const alertClass = item.alertLevel == 0 ? 'status-normal' : (item.alertLevel == 1 ? 'status-warning' : 'status-danger')
        printWindow.document.write('<td class="' + alertClass + '">' + this.getAlertLevelName(item.alertLevel) + '</td>')
        printWindow.document.write('<td>' + (item.alertDesc || '-') + '</td>')
        const statusClass = item.status == 0 ? 'status-normal' : 'status-danger'
        printWindow.document.write('<td class="' + statusClass + '">' + this.getStatusName(item.status) + '</td>')
        const handledClass = item.handled == 1 ? 'status-normal' : 'status-warning'
        printWindow.document.write('<td class="' + handledClass + '">' + this.getHandledName(item.handled) + '</td>')
        printWindow.document.write('<td>' + this.parseTime(item.monitorTime) + '</td>')
        printWindow.document.write('</tr>')
      })
      
      printWindow.document.write('</tbody></table>')
      printWindow.document.write('</body></html>')
      printWindow.document.close()
      printWindow.print()
    }
  }
}
</script>
