<template>
  <div class="attendance-page">
    <div class="attendance-container">
      <h2 class="attendance-title">课堂签到</h2>
      <div class="attendance-info">当前 sessionId: {{ sessionId }} | 当前角色: {{ rolesDisplay.join(', ') || '未登录/无角色' }}</div>
      <div class="attendance-controls">
        <el-input v-model.number="sessionId" placeholder="课堂 sessionId（数字）" style="width:220px;"></el-input>
        <el-button type="primary" @click="loadTasks">加载签到列表</el-button>

        <el-button
          v-hasPermi="['proj_myx:attendance:add']"
          type="success"
          @click="openCreate"
        >创建签到</el-button>

        <el-button
          v-hasPermi="['proj_myx:attendance:export']"
          icon="el-icon-download"
          @click="handleExport"
        >导出列表</el-button>

        <el-button
          v-hasPermi="['proj_myx:attendance:print']"
          icon="el-icon-printer"
          @click="handlePrint"
        >打印</el-button>
      </div>

      <el-table :data="tasks" style="width:100%">
        <el-table-column prop="taskId" label="ID" width="80" align="center"/>
        <el-table-column prop="type" label="方式" width="120" align="center">
          <template slot-scope="{ row }">
            <span>{{ row.type === 'location' ? '位置签到' : '二维码签到' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" align="center">
          <template slot-scope="{ row }">
            <span>{{ formatDate(row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180" align="center">
          <template slot-scope="{ row }">
            <span>{{ formatDate(row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : (row.status===2? 'info':'warning')">{{ row.status===1? '进行中':(row.status===2? '已结束':'未开始') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="440" class-name="ops-col" align="center">
          <template slot-scope="{ row }">
            <div style="display: flex; align-items: center;">
              <div style="flex: 1; text-align: center;">
                <el-button size="mini" @click="openRecords(row)">详情</el-button>
                <el-button size="mini" type="primary" style="background-color: #0071e3; border-color: #0071e3;" @click="openStats(row)">统计</el-button>

                <el-button
                  v-if="row.status === 0"
                  v-hasPermi="['proj_myx:attendance:edit']"
                  size="mini"
                  type="success"
                  @click="handleStart(row)"
                >开始</el-button>
                <el-button
                  v-if="row.status === 1"
                  v-hasPermi="['proj_myx:attendance:close']"
                  size="mini"
                  type="warning"
                  @click="handleClose(row)"
                >结束</el-button>

                <el-button
                  v-if="row.type === 'qr' && row.status === 1"
                  v-hasPermi="['proj_myx:attendance:qr:create']"
                  size="mini"
                  type="warning"
                  plain
                  @click="showQrForTask(row)"
                >二维码</el-button>
              </div>

              <el-button
                v-hasPermi="['proj_myx:attendance:remove']"
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDelete(row)"
                style="margin-left: 10px; color: #C0C4CC; font-size: 14px;"
                title="删除"
              ></el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog title="创建签到" :visible.sync="showCreate" width="600px">
        <CreateAttendance @created="onCreated" :sessionId="sessionId" />
      </el-dialog>

      <el-dialog title="签到详情" :visible.sync="showRecords" width="80%">
        <div class="legend">
          <span class="legend-item"><i class="status-dot status-1"></i> 已签到</span>
          <span class="legend-item"><i class="status-dot status-2"></i> 迟到</span>
          <span class="legend-item"><i class="status-dot status-3"></i> 请假</span>
          <span class="legend-item"><i class="status-dot status-4"></i> 早退</span>
          <span class="legend-item"><i class="status-dot status-0"></i> 未签到</span>
          <span class="legend-hint">(点击姓名可切换状态)</span>
        </div>
        <div class="student-grid">
          <div
            v-for="student in records"
            :key="student.studentId"
            class="student-cell"
            :class="`status-${student.attendanceStatus}`"
            @click="toggleStudentStatus(student)"
          >
            {{ student.studentName }}
          </div>
        </div>
      </el-dialog>

      <el-dialog title="统计" :visible.sync="showStats" width="800px">
        <AttendanceStats :task="selectedTask" :studentsProp="records" />
      </el-dialog>

      <el-dialog title="二维码（任务）" :visible.sync="showQrDialog" width="360px" @close="showQrDialog=false">
        <div v-if="qrData">
          <img :src="qrData.qrUrl" alt="qr" style="width:300px;height:300px;display:block;margin:0 auto" />
          <p style="word-break:break-all;">Token: {{ qrData.token }}</p>
          <p>过期时间: {{ formatDate(qrData.expireTime) }}</p>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="showQrDialog=false">关闭</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import CreateAttendance from '@/views/proj_myx/CreateAttendance.vue'
import AttendanceStats from '@/views/proj_myx/AttendanceStats.vue'
import { listTasks, taskRecords, generateQr, updateStudentStatus, closeTask, startTask, deleteTask } from '@/api/proj_myx/attendance'
import { formatDate } from '@/utils/format'

export default {
  name: 'Attendance',
  components: { CreateAttendance, AttendanceStats },
  data() {
    return {
      sessionId: null,
      tasks: [],
      showCreate: false,
      showRecords: false,
      showStats: false,
      records: [],
      selectedTask: null,
      // QR dialog state
      showQrDialog: false,
      qrData: null,
      qrTtl: 10
    }
  },
  computed: {
    isAdmin() {
      const roles = this.$store && this.$store.getters && this.$store.getters.roles
      return Array.isArray(roles) && roles.includes('admin')
    },
    rolesDisplay() {
      return (this.$store && this.$store.getters && this.$store.getters.roles) || []
    }
  },
  methods: {
    formatDate,
    async loadTasks() {
      if (!this.sessionId) return this.$message.warning('请输入 sessionId')
      try {
        const res = await listTasks(this.sessionId)
        const raw = (res && res.data) || res || []

        // 1. 处理数据
        let list = (Array.isArray(raw) ? raw : []).map(task => {
          // 确保 status 是数字
          if (task.status === undefined || task.status === null) task.status = 1
          return task
        })

        // 2. 排序逻辑
        list.sort((a, b) => {
          const statusOrder = { 1: 0, 0: 1, 2: 2 }
          const sa = statusOrder[a.status] !== undefined ? statusOrder[a.status] : 99
          const sb = statusOrder[b.status] !== undefined ? statusOrder[b.status] : 99

          if (sa !== sb) {
            return sa - sb
          }
          const ta = a.endTime ? new Date(a.endTime).getTime() : (a.createTime ? new Date(a.createTime).getTime() : 0)
          const tb = b.endTime ? new Date(b.endTime).getTime() : (b.createTime ? new Date(b.createTime).getTime() : 0)
          return tb - ta
        })

        this.tasks = list
      } catch (err) {
        console.error('加载签到列表失败', err)
        this.$message.error('加载签到列表失败: ' + (err && err.message ? err.message : '请检查后端'))
      }
    },

    // 状态变更操作
    async handleStart(task) {
      try {
        await startTask(task.taskId)
        this.$message.success('任务已开始')
        this.loadTasks()
      } catch (e) {
        console.error(e)
        this.$message.error('操作失败')
      }
    },
    async handleClose(task) {
      try {
        await closeTask(task.taskId)
        this.$message.success('任务已结束')
        this.loadTasks()
      } catch (e) {
        console.error(e)
        this.$message.error('操作失败')
      }
    },
    handleDelete(task) {
      this.$confirm('确认删除该签到任务吗？删除后相关签到记录也将被清除。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteTask(task.taskId)
          this.$message.success('删除成功')
          this.loadTasks()
        } catch (e) {
          console.error(e)
          this.$message.error('删除失败')
        }
      })
    },

    // 导出任务列表
    handleExport() {
      const tHeader = ['ID', '类型', '开始时间', '结束时间', '状态']
      const filterVal = ['taskId', 'type', 'startTime', 'endTime', 'status']

      const list = this.tasks.map(item => ({
        taskId: item.taskId,
        type: item.type === 'location' ? '位置签到' : '二维码签到',
        status: item.status === 1 ? '进行中' : (item.status === 2 ? '已结束' : '未开始'),
        startTime: this.formatDate(item.startTime),
        endTime: this.formatDate(item.endTime)
      }))

      const data = list.map(v => filterVal.map(j => v[j]))
      data.unshift(tHeader)

      const csvContent = data.map(row =>
        row.map(item => {
          let str = String(item === null || item === undefined ? '' : item);
          if (str.includes(',') || str.includes('"') || str.includes('\n')) {
            str = '"' + str.replace(/"/g, '""') + '"';
          }
          return str;
        }).join(',')
      ).join('\n');

      const blob = new Blob(["\ufeff" + csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement("a");
      const url = URL.createObjectURL(blob);
      link.setAttribute("href", url);
      link.setAttribute("download", '签到任务列表_' + new Date().getTime() + '.csv');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },

    handlePrint() {
      window.print()
    },

    openCreate() {
      if (!this.sessionId) {
        this.$message.warning('请先输入课堂 sessionId 后再创建签到')
        return
      }
      this.showCreate = true
    },
    async onCreated(created) {
      this.showCreate = false
      await this.loadTasks()
      const maybeId = created && (typeof created.taskId === 'number' ? created.taskId : (created.taskId ? Number(created.taskId) : NaN))
      if (Number.isFinite(maybeId)) {
        try {
          const res = await taskRecords(maybeId)
          this.records = (res && res.data) || res || []
          this.showRecords = true
          this.selectedTask = created
        } catch (e) {
          console.error('加载新任务签到详情失败', e)
          this.$message.error('加载新任务签到详情失败')
        }
      }
    },
    async openRecords(task) {
      try {
        if (!task) return this.$message.error('无效的任务')
        const raw = task.taskId
        const id = (typeof raw === 'number') ? raw : (raw ? Number(raw) : NaN)
        if (!Number.isFinite(id)) {
          return this.$message.error('无效的任务 ID（请检查任务列表或重新加载）')
        }
        this.selectedTask = task
        try {
          const res = await taskRecords(id)
          this.records = (res && res.data) || res || []
          this.showRecords = true
        } catch (err) {
          console.error('请求签到详情失败', err)
          this.$message.error('请求签到详情失败：' + (err && err.message ? err.message : '请检查后端或权限'))
        }
      } catch (e) {
        console.error('openRecords error', e)
        this.$message.error('打开签到详情时出错')
      }
    },

    async openStats(task) {
      if (!task) return
      this.selectedTask = task
      try {
        const raw = task.taskId
        const id = (typeof raw === 'number') ? raw : (raw ? Number(raw) : NaN)
        if (!Number.isFinite(id)) {
          return this.$message.error('无效的任务 ID')
        }
        const res = await taskRecords(id)
        this.records = (res && res.data) || res || []
        this.showStats = true
      } catch (e) {
        console.error('加载统计数据失败', e)
        this.$message.error('加载统计数据失败')
      }
    },
    goToStats() { this.$router.push('/proj_myx/AttendanceStats') },
    async showQrForTask(task) {
      if (!task || !task.taskId) return this.$message.error('无效的任务')
      const status = typeof task.status === 'number' ? task.status : (task.status ? Number(task.status) : NaN)
      if (Number.isFinite(status) && status === 2) {
        this.$message.info('签到已结束！')
        return
      }
      const id = typeof task.taskId === 'number' ? task.taskId : Number(task.taskId)
      if (!Number.isFinite(id)) return this.$message.error('无效的任务 ID')
      try {
        const gen = await generateQr(id, this.qrTtl)
        if (gen && gen.code === 200) {
          this.qrData = gen.data
          this.showQrDialog = true
        } else {
          this.$message.error('生成二维码失败: ' + (gen && gen.msg))
        }
      } catch (e) {
        console.error('生成/显示二维码出错', e)
        const serverMsg = e && e.response && e.response.data && (e.response.data.msg || e.response.data.message)
        this.$message.error('生成二维码出错: ' + (serverMsg || (e && e.message ? e.message : '请检查后端')))
      }
    },
    async toggleStudentStatus(student) {
      if (!this.selectedTask || !student) return;
      const currentStatus = student.attendanceStatus;
      const newStatus = (currentStatus + 1) % 5;
      try {
        await updateStudentStatus(this.selectedTask.taskId, student.studentId, newStatus);
        if ((newStatus === 1 || newStatus === 2) && !student.attendanceTime) {
          student.attendanceTime = new Date();
        }
        if (newStatus === 0) {
          student.attendanceTime = null;
        }
        student.attendanceStatus = newStatus;
        const statusText = ['未签到', '已签到', '迟到', '请假', '早退'];
        this.$message.success(`已将 ${student.studentName} 状态更新为 ${statusText[newStatus]}`);
      } catch (error) {
        console.error('更新状态失败', error);
        this.$message.error('更新状态失败');
      }
    }
  }
}
</script>

<style scoped>
/* Apple-style Global Container */
.attendance-page {
  padding: 20px;
  background-color: #f5f5f7;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
}

.attendance-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #ffffff;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  padding: 30px;
}

.attendance-title {
  text-align: center;
  margin: 0 0 10px 0;
  font-weight: 600;
  font-size: 28px;
  letter-spacing: -0.015em;
}

.attendance-info {
  text-align: center;
  margin-bottom: 24px;
  color: #86868b;
  font-size: 14px;
}

.attendance-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  padding: 20px;
  background-color: #fbfbfd;
  border-radius: 14px;
}

.attendance-controls >>> .el-input__inner {
  border-radius: 12px;
  border: 1px solid #d2d2d7;
  height: 40px;
  line-height: 40px;
  transition: all 0.2s ease;
}

.attendance-controls >>> .el-input__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

.attendance-controls >>> .el-button {
  border-radius: 980px;
  font-weight: 500;
  padding: 10px 20px;
  border: none;
  transition: transform 0.1s ease, opacity 0.2s ease;
}

.attendance-controls >>> .el-button:active {
  transform: scale(0.98);
}

.attendance-controls >>> .el-button--primary {
  background-color: #0071e3;
}

.attendance-controls >>> .el-button--success {
  background-color: #34c759;
}

.attendance-controls >>> .el-button--info {
  background-color: #86868b;
}

.attendance-container >>> .el-table {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.attendance-container >>> .el-table th {
  background-color: #f5f5f7;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #e5e5e5;
}

.attendance-container >>> .el-table td {
  border-bottom: 1px solid #f5f5f7;
}

.attendance-container >>> .el-table .cell {
  padding: 12px;
}

.attendance-container >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

.attendance-container >>> .el-tag--success {
  background-color: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.attendance-container >>> .el-tag--warning {
  background-color: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.attendance-container >>> .el-tag--info {
  background-color: rgba(142, 142, 147, 0.1);
  color: #8e8e93;
}

.ops-col >>> .el-button {
  margin-left: 6px;
  border-radius: 8px;
  font-size: 12px;
  padding: 6px 12px;
}

.ops-col >>> .el-button:first-child {
  margin-left: 0;
}

.ops-col >>> .cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  white-space: nowrap;
}

.student-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 16px;
  padding: 10px;
}

.student-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  font-size: 16px;
  font-weight: 500;
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 16px;
  text-align: center;
  cursor: pointer;
  background-color: #ffffff;
  color: #1d1d1f;
  transition: all 0.2s cubic-bezier(0.25, 0.1, 0.25, 1);
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  word-break: break-word;
}

.student-cell:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.student-cell:active {
  transform: scale(0.96);
}

.student-cell.status-0 {
  background-color: #ffffff;
  color: #86868b;
}

.student-cell.status-1 {
  background-color: #0071e3;
  color: #ffffff;
  border-color: #0071e3;
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.3);
}

.student-cell.status-2 {
  background-color: #ff9500;
  color: #ffffff;
  border-color: #ff9500;
  box-shadow: 0 4px 12px rgba(255, 149, 0, 0.3);
}

.student-cell.status-3 {
  background-color: #8e8e93;
  color: #ffffff;
  border-color: #8e8e93;
}

.student-cell.status-4 {
  background-color: #ff3b30;
  color: #ffffff;
  border-color: #ff3b30;
  box-shadow: 0 4px 12px rgba(255, 59, 48, 0.3);
}

.legend {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px;
  background-color: #f5f5f7;
  border-radius: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #1d1d1f;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
  display: inline-block;
}

.status-dot.status-0 { background-color: #d2d2d7; }
.status-dot.status-1 { background-color: #0071e3; }
.status-dot.status-2 { background-color: #ff9500; }
.status-dot.status-3 { background-color: #8e8e93; }
.status-dot.status-4 { background-color: #ff3b30; }

.legend-hint {
  color: #86868b;
  font-size: 12px;
  margin-left: auto;
}

.attendance-page >>> .el-dialog {
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}

.attendance-page >>> .el-dialog__header {
  padding: 20px;
  border-bottom: 1px solid #f5f5f7;
}

.attendance-page >>> .el-dialog__title {
  font-weight: 600;
  color: #1d1d1f;
}

.attendance-page >>> .el-dialog__body {
  padding: 24px;
}
</style>
