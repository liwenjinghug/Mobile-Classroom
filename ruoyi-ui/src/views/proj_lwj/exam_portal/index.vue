<template>
  <div class="exam-portal" v-loading="loading">
    <el-card shadow="never" class="search-pane">
      <div slot="header">考试入口</div>
      <el-form inline @submit.native.prevent>
        <el-form-item label="学号">
          <el-input v-model.trim="studentNo" placeholder="请输入学号" clearable style="width:240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadExams">查询可参加考试</el-button>
        </el-form-item>
        <el-form-item label="筛选">
          <el-radio-group v-model="statusFilter" size="small">
            <el-radio-button label="ongoing">进行中</el-radio-button>
            <el-radio-button label="notStarted">未开始</el-radio-button>
            <el-radio-button label="ended">已结束</el-radio-button>
            <el-radio-button label="all">全部</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <el-alert v-if="!studentNo" type="info" :closable="false" title="请输入学号，查询您可参加的考试列表" />
    </el-card>

    <el-card v-if="displayedExams.length" shadow="never" class="list-pane">
      <div slot="header">考试列表 ({{ displayedExams.length }})</div>
      <el-table :data="displayedExams" size="small" style="width:100%">
        <el-table-column prop="examName" label="考试名称" min-width="200" />
        <el-table-column prop="courseName" label="课程" width="160" />
        <el-table-column prop="className" label="课堂" width="160" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row)">{{ statusLabel(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" :disabled="!canTake(scope.row)" @click="goTake(scope.row)">进入考试</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-empty v-else description="暂无符合条件的考试" />
  </div>
</template>

<script>
import { listAvailableExams, startExam } from '@/api/proj_lwj/exam'

export default {
  name: 'ExamPortal',
  data() {
    return { studentNo: '', exams: [], loading: false, statusFilter: 'ongoing' }
  },
  computed: {
    displayedExams() {
      const now = Date.now()
      const classify = (row) => {
        const st = new Date(row.startTime).getTime()
        const ed = new Date(row.endTime).getTime()
        if (now < st) return 'notStarted'
        if (now > ed) return 'ended'
        return 'ongoing'
      }
      if (this.statusFilter === 'all') return this.exams.slice()
      return (this.exams || []).filter(r => classify(r) === this.statusFilter)
    }
  },
  methods: {
    async loadExams() {
      if (!this.studentNo) { this.$message.warning('请先输入学号'); return }
      this.loading = true
      try {
        const res = await listAvailableExams({ studentNo: this.studentNo })
        this.exams = Array.isArray(res.data) ? res.data : []
      } catch (e) {
        console.error(e)
        this.$message.error('加载考试列表失败')
        this.exams = []
      } finally { this.loading = false }
    },
    statusLabel(row) {
      const now = Date.now(); const st = new Date(row.startTime).getTime(); const ed = new Date(row.endTime).getTime()
      if (now < st) return '未开始'
      if (now > ed) return '已结束'
      return '进行中'
    },
    statusTagType(row) {
      const l = this.statusLabel(row)
      return l==='进行中'?'success':l==='未开始'?'info':'warning'
    },
    canTake(row) {
      const now = Date.now(); const st = new Date(row.startTime).getTime(); const ed = new Date(row.endTime).getTime()
      return now>=st && now<=ed && (row.status===1 || row.status===2)
    },
    async goTake(row) {
      if (!row || !row.id) return
      if (!this.studentNo) { this.$message.warning('请先输入学号'); return }
      if (!this.canTake(row)) { this.$message.warning('当前不在考试开放时间'); return }
      try {
        await startExam({ examId: Number(row.id), studentNo: this.studentNo })
        // 一键开始并进入，携带 autoStart=1
        this.$router.push({ name: 'ExamTake', params: { examId: String(row.id) }, query: { studentNo: this.studentNo, autoStart: '1' } })
      } catch (e) {
        console.error(e)
        this.$message.error('开考失败，请稍后重试')
      }
    }
  }
}
</script>

<style scoped>
.search-pane { margin-bottom: 12px; }
</style>
