<template>
  <div class="stats-container">
    <div v-if="!task" class="empty-state">请选择签到任务</div>
    <div v-else>
      <div class="stats-header">
        <h3 class="stats-title">签到统计 #{{ task.taskId }}</h3>
        <div class="stats-card">
          <div class="stats-label">出勤率</div>
          <div class="stats-value">{{ stats.attendanceRate }}%</div>
        </div>
      </div>
      
      <div class="table-wrapper">
        <el-table :data="students" style="width:100%">
          <el-table-column prop="studentName" label="姓名" align="center"/>
          <el-table-column prop="studentNo" label="学号" align="center"/>
          <el-table-column label="状态" align="center">
            <template slot-scope="{ row }">
              <el-tag :type="statusTagType(row.attendanceStatus)" effect="plain" class="status-tag">
                {{ statusText(row.attendanceStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="attendanceTime" label="签到时间" align="center">
            <template slot-scope="{ row }">
              <span class="time-text">{{ formatDate(row.attendanceTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="actions-footer">
        <el-button type="primary" plain round @click="exportExcel">导出 Excel</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { taskRecords } from '@/api/proj_myx/attendance'
import { formatDate } from '@/utils/format'

export default {
  name: 'AttendanceStats',
  props: {
    task: Object,
    studentsProp: { type: Array, default: () => [] }
  },
  data() {
    return { students: [], stats: { attendanceRate: 0 } }
  },
  watch: {
    task: { immediate: true, handler(v) { this.init(); } },
    studentsProp: { immediate: true, handler() { this.init(); } }
  },
  methods: {
    formatDate,
    async init() {
      // 优先使用父组件传入的学生列表
      if (this.studentsProp && this.studentsProp.length) {
        this.students = this.studentsProp;
        this.computeStats();
        return;
      }
      // 否则按原逻辑，通过 taskId 去请求一次
      if (!this.task) return;
      const res = await taskRecords(this.task.taskId);
      this.students = (res && res.data) || res || [];
      this.computeStats();
    },
    computeStats() {
      const total = this.students.length;
      const present = this.students.filter(s => s.attendanceStatus === 1 || s.attendanceStatus === 2).length;
      this.stats.attendanceRate = total ? Math.round((present / total) * 100) : 0;
    },
    statusText(status) {
      const map = { 0: '未签到', 1: '已签到', 2: '迟到', 3: '请假', 4: '早退' };
      return map[status] || '未知';
    },
    statusTagType(status) {
      const map = { 0: 'danger', 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' };
      return map[status] || 'primary';
    },
    exportExcel() {
      // 简易导出：生成 CSV 并触发下载
      const rows = [['姓名','学号','状态','签到时间']]
      this.students.forEach(s => rows.push([s.studentName||'', s.studentNo||'', this.statusText(s.attendanceStatus), s.attendanceTime||'']))
      const csv = rows.map(r => r.map(c => '"'+String(c).replace(/"/g,'""')+'"').join(',')).join('\n')
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'attendance_'+(this.task.taskId||'')+'.csv'
      a.click()
      URL.revokeObjectURL(url)
    }
  }
}
</script>

<style scoped>
.stats-container {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #86868b;
  font-size: 16px;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.stats-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.stats-card {
  background: #f5f5f7;
  padding: 12px 24px;
  border-radius: 12px;
  text-align: center;
}

.stats-label {
  font-size: 12px;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.stats-value {
  font-size: 24px;
  font-weight: 700;
  color: #0071e3;
}

.table-wrapper {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e5e5;
}

.status-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

.time-text {
  color: #86868b;
  font-size: 13px;
}

.actions-footer {
  margin-top: 24px;
  text-align: right;
}

/* Table Overrides */
.stats-container >>> .el-table th {
  background-color: #fbfbfd;
  color: #86868b;
  font-weight: 600;
}

.stats-container >>> .el-table td {
  border-bottom: 1px solid #f5f5f7;
}
</style>
