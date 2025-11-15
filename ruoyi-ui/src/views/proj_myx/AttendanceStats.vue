<template>
  <div>
    <div v-if="!task">请选择签到任务</div>
    <div v-else>
      <h3>签到统计 - {{ task.taskId }}</h3>
      <p>出勤率: {{ stats.attendanceRate }}%</p>
      <el-table :data="students" style="width:100%">
        <el-table-column prop="studentName" label="姓名" align="center"/>
        <el-table-column prop="studentNo" label="学号" align="center"/>
        <el-table-column label="状态" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="statusTagType(row.attendanceStatus)">{{ statusText(row.attendanceStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="attendanceTime" label="签到时间" align="center">
          <template slot-scope="{ row }">
            <span>{{ formatDate(row.attendanceTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px">
        <el-button @click="exportExcel">导出Excel</el-button>
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
