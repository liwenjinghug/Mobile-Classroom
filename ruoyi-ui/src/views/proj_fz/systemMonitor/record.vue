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
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['proj_fz:monitor:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['proj_fz:monitor:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-refresh"
          size="mini"
          @click="handleCollect"
          v-hasPermi="['proj_fz:monitor:collect']"
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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['proj_fz:monitor:query']"
          >详情</el-button>
          <el-button
            v-if="scope.row.handled == 0 && scope.row.alertLevel > 0"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleHandle(scope.row)"
            v-hasPermi="['proj_fz:monitor:edit']"
          >处理</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['proj_fz:monitor:remove']"
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
  </div>
</template>

<script>
import { listMonitor, getMonitor, delMonitor, handleAlert, exportMonitor, collectMetrics } from '@/api/proj_fz/systemMonitor'

export default {
  name: 'MonitorRecord',
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      monitorList: [],
      title: '',
      open: false,
      handleOpen: false,
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
    }
  }
}
</script>

