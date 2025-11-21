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
        <el-button type="primary" round style="background-color: #0071e3; border-color: #0071e3;" @click="exportExcel">导出 Excel</el-button>
        <el-button type="primary" round style="background-color: #0071e3; border-color: #0071e3;" icon="el-icon-printer" @click="handlePrint">打印统计</el-button>
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
      let list = [];
      // 优先使用父组件传入的学生列表
      if (this.studentsProp && this.studentsProp.length) {
        list = [...this.studentsProp];
      } else if (this.task) {
        // 否则按原逻辑，通过 taskId 去请求一次
        const res = await taskRecords(this.task.taskId);
        list = (res && res.data) || res || [];
      }

      // 排序逻辑：未签到(0) > 早退(4) > 请假(3) > 迟到(2) > 已签到(1)
      const priority = { 0: 0, 4: 1, 3: 2, 2: 3, 1: 4 };
      list.sort((a, b) => {
        const pa = priority[a.attendanceStatus] !== undefined ? priority[a.attendanceStatus] : 99;
        const pb = priority[b.attendanceStatus] !== undefined ? priority[b.attendanceStatus] : 99;
        return pa - pb;
      });

      this.students = list;
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
      const csv = rows.map(r => r.map(c => {
        let str = String(c === null || c === undefined ? '' : c);
        if (str.includes(',') || str.includes('"') || str.includes('\n')) {
          str = '"' + str.replace(/"/g, '""') + '"';
        }
        return str;
      }).join(',')).join('\n')
      const blob = new Blob(["\ufeff" + csv], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'attendance_'+(this.task.taskId||'')+'.csv'
      a.click()
      URL.revokeObjectURL(url)
    },
    handlePrint() {
      const printContent = this.$el.outerHTML;
      const iframe = document.createElement('iframe');
      iframe.setAttribute('style', 'position:fixed;width:0;height:0;top:0;left:0;border:0;z-index:9999');
      document.body.appendChild(iframe);
      
      const doc = iframe.contentWindow.document;
      
      // Copy styles
      const links = Array.from(document.querySelectorAll('link[rel="stylesheet"]')).map(link => link.outerHTML).join('');
      const styles = Array.from(document.querySelectorAll('style')).map(style => style.outerHTML).join('');
      
      doc.write(`
        <!DOCTYPE html>
        <html>
          <head>
            <title>签到统计</title>
            ${links}
            ${styles}
            <style>
              body { margin: 20px; background-color: #fff !important; }
              .actions-footer { display: none !important; }
              /* Ensure table shows all rows */
              .el-table { height: auto !important; }
              .el-table__body-wrapper { height: auto !important; overflow: visible !important; }
            </style>
          </head>
          <body>
            ${printContent}
          </body>
        </html>
      `);
      doc.close();
      
      // Wait for styles to load
      setTimeout(() => {
        iframe.contentWindow.focus();
        iframe.contentWindow.print();
        setTimeout(() => {
          document.body.removeChild(iframe);
        }, 2000);
      }, 500);
    }
  }
}
</script>

<style scoped>
.stats-container {
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.empty-state {
  text-align: center;
  color: #86868b;
  padding: 40px;
  background: #f5f5f7;
  border-radius: 18px;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #ffffff;
  padding: 20px;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.stats-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
}

.stats-card {
  text-align: right;
}

.stats-label {
  font-size: 12px;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.stats-value {
  font-size: 32px;
  font-weight: 700;
  color: #0071e3;
}

.table-wrapper {
  background: #ffffff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  margin-bottom: 24px;
}

/* Table Styling */
.stats-container >>> .el-table {
  border: none;
}

.stats-container >>> .el-table th {
  background-color: #ffffff;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #f5f5f7;
  padding: 16px 0;
}

.stats-container >>> .el-table td {
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f7;
}

.stats-container >>> .el-table::before {
  display: none;
}

/* Tags */
.status-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
  padding: 0 10px;
  height: 28px;
  line-height: 28px;
}

.stats-container >>> .el-tag--success {
  background-color: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.stats-container >>> .el-tag--warning {
  background-color: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.stats-container >>> .el-tag--info {
  background-color: rgba(142, 142, 147, 0.1);
  color: #8e8e93;
}

.stats-container >>> .el-tag--danger {
  background-color: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.time-text {
  color: #86868b;
  font-size: 13px;
}

.actions-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.actions-footer >>> .el-button {
  border-radius: 980px;
  padding: 10px 24px;
  font-weight: 500;
}

.actions-footer >>> .el-button--primary.is-plain {
  color: #0071e3;
  background: rgba(0, 113, 227, 0.1);
  border-color: transparent;
}

.actions-footer >>> .el-button--primary.is-plain:hover {
  background: #0071e3;
  color: #ffffff;
}

@media print {
  .stats-container {
    padding: 0;
  }
  .actions-footer {
    display: none;
  }
  .table-wrapper {
    box-shadow: none;
  }
}
</style>
