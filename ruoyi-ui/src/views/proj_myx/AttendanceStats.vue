<template>
  <div>
    <div v-if="!task">请选择签到任务</div>
    <div v-else>
      <h3>签到统计 - {{ task.taskId }}</h3>
      <p>出勤率: {{ stats.attendanceRate }}%</p>
      <el-table :data="students" style="width:100%">
        <el-table-column prop="studentName" label="姓名"/>
        <el-table-column prop="studentNo" label="学号"/>
        <el-table-column label="状态">
          <template slot-scope="{ row }">
            <el-tag :type="row.attendanceStatus===1 ? 'success' : 'warning'">{{ row.attendanceStatus===1? '已签到':'未签到' }}</el-tag>
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

export default {
  name: 'AttendanceStats',
  props: { task: Object },
  data() {
    return { students: [], stats: { attendanceRate: 0 } }
  },
  watch: {
    task: { immediate: true, handler(v) { if (v) this.load() } }
  },
  methods: {
    async load() {
      if (!this.task) return
      const res = await taskRecords(this.task.taskId)
      this.students = (res && res.data) || res || []
      const total = this.students.length
      const present = this.students.filter(s => s.attendanceStatus === 1).length
      this.stats.attendanceRate = total ? Math.round((present/total)*100) : 0
    },
    exportExcel() {
      // 简易导出：生成 CSV 并触发下载
      const rows = [['姓名','学号','状态','签到时间']]
      this.students.forEach(s => rows.push([s.studentName||'', s.studentNo||'', s.attendanceStatus===1?'已签到':'未签到', s.attendanceTime||'']))
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

