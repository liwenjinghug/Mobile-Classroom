<template>
  <div class="attendance-page">
    <div class="attendance-container">
      <h2 class="attendance-title">课堂签到</h2>
      <div class="attendance-info">当前 sessionId: {{ sessionId }} | 当前角色: {{ rolesDisplay.join(', ') || '未登录/无角色' }}</div>
      <div class="attendance-controls">
        <el-input v-model.number="sessionId" placeholder="课堂 sessionId（数字）" style="width:220px;"></el-input>
        <el-button type="primary" @click="loadTasks">加载签到列表</el-button>
        <el-button v-if="isAdmin" type="success" @click="openCreate">创建签到</el-button>
        <el-button v-if="isAdmin" type="info" @click="goToStats">数据统计</el-button>
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
            <el-button size="mini" @click="openRecords(row)">查看签到详情</el-button>
            <el-button size="mini" type="primary" @click="openStats(row)">统计</el-button>
            <el-button v-show="isAdmin && row.type === 'qr' && row.status !== 2"
                       size="mini"
                       type="warning"
                       @click="showQrForTask(row)">二维码</el-button>
            <!-- Add a placeholder button for alignment when the QR button is hidden -->
            <span v-show="!isAdmin || row.type !== 'qr' || row.status === 2" style="display: inline-block; width: 60px;"></span>
            <!-- 已移除调试打印按钮 -->
          </template>
        </el-table-column>
      </el-table>

      <!-- 已移除调试 JSON 面板 -->

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
import { listTasks, taskRecords, generateQr, updateStudentStatus } from '@/api/proj_myx/attendance'
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
      // 已移除 devShowButtons 测试字段
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
        // compute status from start/end times to reflect real-time state
        const now = Date.now()
        this.tasks = (Array.isArray(raw) ? raw : []).map(task => {
          try {
            // If backend explicitly closed the task keep it closed
            const backendStatus = (task && (typeof task.status === 'number' ? task.status : (task.status ? Number(task.status) : NaN)))
            if (Number.isFinite(backendStatus) && backendStatus === 2) return task

            const startTs = task && task.startTime ? (typeof task.startTime === 'number' ? task.startTime : new Date(task.startTime).getTime()) : NaN
            const endTs = task && task.endTime ? (typeof task.endTime === 'number' ? task.endTime : new Date(task.endTime).getTime()) : NaN

            let computed = backendStatus
            if (!Number.isFinite(computed)) computed = 1 // default to running when unknown

            if (Number.isFinite(startTs) && now < startTs) {
              computed = 0 // not started
            } else if (Number.isFinite(endTs) && now > endTs) {
              computed = 2 // ended
            } else {
              // only set to running if it's between start and end or if times are missing
              computed = 1
            }

            // set the status so existing template continues to work
            task.status = computed
            return task
          } catch (e) {
            console.error('计算任务状态失败', e)
            return task
          }
        })
      } catch (err) {
        console.error('加载签到列表失败', err)
        this.$message.error('加载签到列表失败: ' + (err && err.message ? err.message : '请检查后端'))
      }
    },
    openCreate() {
      if (!this.sessionId) {
        this.$message.warning('请先输入课堂 sessionId 后再创建签到')
        return
      }
      this.showCreate = true
    },
    // onCreated 现在接收后端创建的对象（可能含 taskId），若有则自动打开详情
    async onCreated(created) {
      this.showCreate = false
      await this.loadTasks()
      // 安全地解析 taskId，防止字符串 'null' 导致后端抛错
      const maybeId = created && (typeof created.taskId === 'number' ? created.taskId : (created.taskId ? Number(created.taskId) : NaN))
      if (Number.isFinite(maybeId)) {
        // 自动打开签到详情
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
      // 严格校验 taskId，避免把字符串 'null' 发送到后端
      try {
        if (!task) return this.$message.error('无效的任务')
        console.log('openRecords called with task:', task)
        const raw = task.taskId
        const id = (typeof raw === 'number') ? raw : (raw ? Number(raw) : NaN)
        if (!Number.isFinite(id)) {
          console.warn('openRecords: invalid taskId', raw)
          return this.$message.error('无效的任务 ID（请检查任务列表或重新加载）')
        }
        this.selectedTask = task
        // 请求并捕获异常，避免未捕获错误导致控制台和后端日志困惑
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

    openStats(task) { this.selectedTask = task; this.showStats = true },
    goToStats() { this.$router.push('/proj_myx/AttendanceStats') },
    async showQrForTask(task) {
      if (!task || !task.taskId) return this.$message.error('无效的任务')
      // 如果任务已经结束，提示并返回
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
      const newStatus = (currentStatus + 1) % 5; // 0->1->2->3->4->0
      try {
        await updateStudentStatus(this.selectedTask.taskId, student.studentId, newStatus);
        // 前端本地状态：
        // 1 已签到、2 迟到 都视为“有签到时间”，如果之前是未签到则本地也补一个当前时间方便展示
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
.attendance-page { padding:10px 8px 8px 8px }
.attendance-container { max-width:1200px; margin: 0 auto; }
.attendance-title { text-align:center; margin: 6px 0 10px 0; font-weight:600 }
.attendance-info { text-align:center; margin-bottom:8px; color:#999; font-size:13px }
.attendance-controls { display:flex; justify-content:center; align-items:center; gap:8px; margin-bottom:12px; flex-wrap:wrap }

/* make table occupy available width within the centered container */
.attendance-container >>> .el-table { width:100% }

/* make operation buttons inline with consistent spacing */
.ops-col >>> .el-button { margin-left:6px }
.ops-col >>> .el-button:first-child { margin-left:0 }
.ops-col >>> .cell { display:flex; align-items:center; gap:8px; white-space:nowrap }

/* a bit more padding in table cells for a roomier layout */
.attendance-container >>> .el-table .cell { padding: 14px 14px }

.student-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr)); /* 放大到210% */
  gap: 15px;
  width: 76vw;
  /* min-height: 20vh; */
}
.student-cell {
  padding: 21px; /* 放大到210% */
  font-size: 25px; /* 放大到210% */
  font-weight: bold;
  border: 2.1px solid #dcdfe6; /* 放大到210% */
  border-radius: 12px; /* 放大到210% */
  text-align: center;
  cursor: pointer;
  background-color: #f5f7fa;
  color: #909399;
  transition: background-color 0.3s, color 0.3s;
  box-sizing: border-box;
}
.student-cell.signed-in {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
}
.legend {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}
.legend-item {
  display: flex;
  align-items: center;
}
.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 5px;
  display: inline-block;
}
.legend-hint {
  color: #999;
  font-size: 12px;
}

/* Status Colors */
.status-0, .student-cell.status-0 { background-color: #f5f7fa; color: #909399; border-color: #dcdfe6; }
.status-1, .student-cell.status-1 { background-color: #409eff; color: #fff; border-color: #409eff; }
.status-2, .student-cell.status-2 { background-color: #e6a23c; color: #fff; border-color: #e6a23c; }
.status-3, .student-cell.status-3 { background-color: #909399; color: #fff; border-color: #909399; }
.status-4, .student-cell.status-4 { background-color: #f56c6c; color: #fff; border-color: #f56c6c; }

.student-cell {
  padding: 10px;
  border: 1px solid;
  border-radius: 4px;
  text-align: center;
  cursor: pointer;
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;
}
</style>
