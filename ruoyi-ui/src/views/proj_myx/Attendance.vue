<template>
  <div class="attendance-page">
    <h2>课堂签到</h2>
    <div style="margin-bottom:8px;color:#999;font-size:12px">
      当前 sessionId: {{ sessionId }} | 当前角色: {{ rolesDisplay.join(', ') || '未登录/无角色' }}
      <!-- 已移除测试用的切换按钮 -->
    </div>
    <div style="margin-bottom:12px">
      <el-input v-model.number="sessionId" placeholder="课堂 sessionId（数字）" style="width:220px; margin-right:8px"></el-input>
      <el-button type="primary" @click="loadTasks">加载签到列表</el-button>
      <el-button v-if="isAdmin" type="success" @click="openCreate" style="margin-left:8px">创建签到</el-button>
      <el-button v-if="isAdmin" type="info" @click="goToStats" style="margin-left:8px">数据统计</el-button>
    </div>

    <el-table :data="tasks" style="width:100%">
      <el-table-column prop="taskId" label="ID" width="80"/>
      <el-table-column prop="type" label="方式" width="120">
        <template slot-scope="{ row }">
          <span>{{ row.type === 'location' ? '位置签到' : '二维码签到' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180"/>
      <el-table-column prop="endTime" label="结束时间" width="180"/>
      <el-table-column prop="status" label="状态" width="120">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : (row.status===2? 'info':'warning')">{{ row.status===1? '进行中':(row.status===2? '已结束':'未开始') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template slot-scope="{ row }">
          <el-button size="mini" @click="openRecords(row)">查看签到详情</el-button>
          <el-button size="mini" type="primary" @click="openStats(row)">统计</el-button>
          <!-- 已移除调试打印按钮 -->
        </template>
      </el-table-column>
    </el-table>

    <!-- 已移除调试 JSON 面板 -->

    <el-dialog title="创建签到" :visible.sync="showCreate" width="600px">
      <CreateAttendance @created="onCreated" :sessionId="sessionId" />
    </el-dialog>

    <el-dialog title="签到详情" :visible.sync="showRecords" width="700px">
      <el-table :data="records" style="width:100%">
        <el-table-column prop="studentName" label="姓名"/>
        <el-table-column prop="studentNo" label="学号"/>
        <el-table-column prop="attendanceStatus" label="状态">
          <template slot-scope="{ row }">
            <div :style="row.attendanceStatus === 1 ? 'background:#e6f7ff;padding:6px;border-radius:4px' : ''">{{ row.attendanceStatus === 1 ? '已签到' : '未签到' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="attendanceTime" label="签到时间"/>
      </el-table>
    </el-dialog>

    <el-dialog title="统计" :visible.sync="showStats" width="800px">
      <AttendanceStats :task="selectedTask" />
    </el-dialog>
  </div>
</template>

<script>
import CreateAttendance from '@/views/proj_myx/CreateAttendance.vue'
import AttendanceStats from '@/views/proj_myx/AttendanceStats.vue'
import { listTasks, taskRecords } from '@/api/proj_myx/attendance'

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
      selectedTask: null
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
    async loadTasks() {
      if (!this.sessionId) return this.$message.warning('请输入 sessionId')
      try {
        const res = await listTasks(this.sessionId)
        this.tasks = (res && res.data) || res || []
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
    goToStats() { this.$router.push('/proj_myx/AttendanceStats') }
  }
}
</script>

<style scoped>
.attendance-page { padding:16px }
</style>
