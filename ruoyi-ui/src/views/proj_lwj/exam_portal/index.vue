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
          <el-button type="success" @click="loadMyList" :disabled="!studentNo" style="margin-left:8px">查询我的考试记录</el-button>
          <el-button v-if="studentNoStored" type="text" @click="clearStored" style="margin-left:4px">清除记住的学号</el-button>
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
      <el-alert v-if="!studentNo" type="info" :closable="false" title="请输入学号，查询您可参加的考试列表或您的历史记录" />
    </el-card>

    <!-- 考试列表，仅在点击查询后显示 -->
    <el-card v-if="examsLoaded && displayedExams.length" shadow="never" class="list-pane">
      <div slot="header">考试列表 ({{ displayedExams.length }})</div>
      <el-table :data="displayedExams" size="small" style="width:100%">
        <el-table-column prop="examName" label="考试名称" min-width="200" />
        <el-table-column prop="courseName" label="课程" width="160" />
        <el-table-column prop="className" label="课堂" width="160" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="我的分数" width="120">
          <template slot-scope="scope">
            {{ myScore(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row)">{{ statusLabel(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" :disabled="!canTake(scope.row) || submittedLocal(scope.row)" @click="goTake(scope.row)">
              {{ submittedLocal(scope.row) ? '已完成' : '进入考试' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card v-else-if="examsLoaded" shadow="never" class="list-pane"><el-empty description="暂无符合条件的考试" /></el-card>

    <!-- 我的考试记录：仅在点击查询后显示 -->
    <el-card v-if="myLoaded" shadow="never" class="my-pane" style="margin-top:16px">
      <div slot="header" class="clearfix">
        <span>我的考试记录</span>
        <div style="float:right">
          <el-input v-model.trim="myQuery.keyword" placeholder="搜索考试/课程" size="mini" clearable style="width:220px;margin-right:8px" />
          <el-button size="mini" @click="exportMyCSV" :disabled="!myDisplayed.length">导出</el-button>
          <el-button size="mini" @click="printMy" :disabled="!myDisplayed.length">打印</el-button>
        </div>
      </div>
      <el-descriptions v-if="myStats.total>0" :column="4" size="small" border style="margin-bottom:8px">
        <el-descriptions-item label="总记录">{{ myStats.total }}</el-descriptions-item>
        <el-descriptions-item label="已完成">{{ myStats.finished }}</el-descriptions-item>
        <el-descriptions-item label="平均分">{{ myStats.avgScore }}</el-descriptions-item>
        <el-descriptions-item label="及格率">{{ myStats.passRate }}%</el-descriptions-item>
      </el-descriptions>
      <el-table :data="myDisplayed" size="small" :row-key="row=>row.id || (row.examId+'-'+row.studentNo)" ref="myTable">
        <el-table-column type="index" width="50" />
        <el-table-column prop="examName" label="考试名称" min-width="200" />
        <el-table-column prop="courseName" label="课程" width="160" />
        <!-- 删除课堂列（不需要展示课堂） -->
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="totalScore" label="成绩" width="100">
          <template slot-scope="scope">{{ displayScoreRow(scope.row) }}</template>
        </el-table-column>
        <el-table-column prop="passStatus" label="评定" width="100">
          <template slot-scope="scope">
            <el-tag :type="passTagType(scope.row)">{{ passStatusText(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="participantStatus" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.participantStatus===2?'success':'info'">{{ scope.row.participantStatus===2?'已提交':'进行中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
      </el-table>
      <div v-if="!myDisplayed.length" style="padding:12px 0"><el-empty description="暂无记录" /></div>
    </el-card>
  </div>
</template>

<script>
import { listAvailableExams, startExamParticipant, listMyParticipants, getExam } from '@/api/proj_lwj/exam'

export default {
  name: 'ExamPortal',
  data() {
    return {
      studentNo: '', exams: [], loading: false, statusFilter: 'ongoing', studentNoStored: false,
      // 新增：是否已点击查询标记
      examsLoaded: false,
      myLoaded: false,
      myList: [], myLoading: false,
      myQuery: { keyword: '' },
      examCache: {},
      myStats: { total: 0, finished: 0, avgScore: '—', passRate: 0 }
    }
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
      if (this.statusFilter === 'all') return (this.exams || []).slice()
      return (this.exams || []).filter(r => classify(r) === this.statusFilter)
    },
    myDisplayed(){
      const kw = (this.myQuery.keyword||'').toLowerCase()
      if (!kw) return this.myList
      return this.myList.filter(r=>
        String(r.examName||'').toLowerCase().includes(kw) ||
        String(r.courseName||'').toLowerCase().includes(kw)
      )
    }
  },
  watch: {
    studentNo(val) {
      const v = (val||'').trim()
      if (v) {
        try { localStorage.setItem('exam_portal_studentNo', v); this.studentNoStored = true } catch(e){}
      } else {
        this.studentNoStored = !!localStorage.getItem('exam_portal_studentNo')
      }
    }
  },
  created() {
    // 从localStorage恢复学号，但不自动加载列表，避免未就绪接口报错
    try {
      const saved = localStorage.getItem('exam_portal_studentNo')
      if (saved) {
        this.studentNo = saved
        this.studentNoStored = true
      }
    } catch(e) { /* ignore */ }
  },
  methods: {
    clearStored() {
      try { localStorage.removeItem('exam_portal_studentNo') } catch(e){}
      this.studentNoStored = false
      this.$message.success('已清除记住的学号')
    },
    async loadExams() {
      if (!this.studentNo) { this.$message.warning('请先输入学号'); return }
      this.loading = true
      this.examsLoaded = false
      try {
        const res = await listAvailableExams(this.studentNo)
        const data = res && (res.data || res.rows || res.list || res.exams)
        this.exams = Array.isArray(data) ? data : (Array.isArray(res) ? res : [])
        // 合并本地已结束考试摘要
        this.mergeLocalFinished()
      } catch (e) {
        console.error(e)
        this.$message.error('加载考试列表失败')
        this.exams = []
        this.mergeLocalFinished()
      } finally { this.loading = false; this.examsLoaded = true }
    },
    mergeLocalFinished() {
      try {
        const prefix = `exam_summary_`
        const added = []
        for (let i=0;i<localStorage.length;i++) {
          const k = localStorage.key(i)
          if (k && k.startsWith(prefix) && k.endsWith(`_${this.studentNo}`)) {
            const raw = localStorage.getItem(k)
            if (!raw) continue
            let obj
            try { obj = JSON.parse(raw) } catch(e){ continue }
            if (!obj || !obj.id) continue
            if (this.exams.some(e => Number(e.id) === Number(obj.id))) continue
            added.push(obj)
          }
        }
        if (added.length) {
          added.sort((a,b)=> new Date(b.endTime||0)-new Date(a.endTime||0))
          this.exams = this.exams.concat(added)
        }
      } catch(e){ /* ignore */ }
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
        await startExamParticipant({ examId: Number(row.id), studentNo: this.studentNo })
        this.$router.push({ name: 'ExamTake', params: { examId: String(row.id) }, query: { studentNo: this.studentNo, autoStart: '1' } })
      } catch (e) {
        console.error(e)
        this.$message.error('开考失败，请稍后重试')
      }
    },
    submittedLocal(row){
      try { return localStorage.getItem(`exam_submitted_${row.id}_${this.studentNo}`) === '1' } catch(e){ return false }
    },
    myScore(row){
      if (row && row.myScore != null) return row.myScore
      try {
        const v = localStorage.getItem(`exam_score_${row.id}_${this.studentNo}`)
        return v!=null ? v : '——'
      } catch(e){ return '——' }
    },
    displayScoreRow(row){
      // 成绩展示：优先 totalScore，若无则回退 objectiveScore，再无显示 双横线
      if (!row) return '——'
      const t = row.totalScore
      if (t !== undefined && t !== null && !isNaN(Number(t))) {
        const n = Number(t); return (n%1===0)? n : n.toFixed(2)
      }
      const o = row.objectiveScore
      if (o !== undefined && o !== null && !isNaN(Number(o))) {
        const n = Number(o); return (n%1===0)? n : n.toFixed(2)
      }
      return '——'
    },
    passStatusText(row){
      const v = Number(row && row.passStatus)
      if (v === 1) return '及格'
      if (v === 0) return '不及格'
      return '——'
    },
    passTagType(row){
      const v = Number(row && row.passStatus)
      if (v === 1) return 'success'
      if (v === 0) return 'danger'
      return 'info'
    },

    // ===== 我的考试记录 =====
    scoreDisplay(s){ return (s===undefined||s===null)?'——': (Number(s)%1===0?Number(s):Number(s).toFixed(2)) },
    async loadMyList(){
      if (!this.studentNo) { this.$message.info('请先输入学号'); return }
      this.myLoading = true
      this.myLoaded = true
      try {
        const res = await listMyParticipants(this.studentNo)
        const list = res && (res.data || res.rows || res.list || res)
        let arr = Array.isArray(list) ? list : []
        await this.enrichExamInfo(arr)
        // 过滤掉已删除的考试（后端 getExam 不存在或返回失败时不会写入 examCache）
        arr = arr.filter(r => !!this.examCache[r.examId])
        arr.sort((a,b)=> new Date(b.submitTime||0)-new Date(a.submitTime||0) || new Date(b.startTime||0)-new Date(a.startTime||0))
        this.myList = arr
        this.calcStats()
      } catch(e){
        console.error(e); this.$message.error('加载考试记录失败'); this.myList=[]; this.calcStats()
      } finally { this.myLoading=false }
    },
    async enrichExamInfo(list){
      const needIds = []
      list.forEach(r=>{ if (!this.examCache[r.examId]) needIds.push(r.examId) })
      const uniq = Array.from(new Set(needIds.filter(Boolean)))
      for (const id of uniq) {
        try {
          const res = await getExam(id)
          const ex = res && res.data
          if (ex) this.$set(this.examCache, id, ex)
        } catch(e) { /* ignore */ }
      }
      list.forEach(r=>{
        const ex = this.examCache[r.examId] || {}
        if (!r.examName) r.examName = ex.examName
        if (!r.courseName) r.courseName = ex.courseName
        // 不设置课堂到记录列表
        if (!r.startTime && ex.startTime) r.startTime = ex.startTime
        if (!r.endTime && ex.endTime) r.endTime = ex.endTime
      })
    },
    calcStats(){
      const total = this.myList.length
      let finished = 0, pass = 0, sum = 0, cnt = 0
      this.myList.forEach(r=>{
        if (r.participantStatus === 2) finished++
        if (r.passStatus === 1) pass++
        if (r.totalScore != null && !isNaN(r.totalScore)) { sum += Number(r.totalScore); cnt++ }
      })
      const avg = cnt>0 ? (Math.round((sum/cnt)*100)/100) : '—'
      const rate = total>0 ? Math.round((pass*10000/total))/100 : 0
      this.myStats = { total, finished, avgScore: avg, passRate: rate }
    },
    exportMyCSV(){
      if (!this.myDisplayed.length) return
      // 去掉课堂列，新增评定
      const headers = ['考试名称','课程','学号','成绩','评定','状态','开始时间','提交时间']
      const rows = this.myDisplayed.map(r=>[
        (r.examName||''),(r.courseName||''),(r.studentNo||''),
        this.displayScoreRow(r), this.passStatusText(r), (r.participantStatus===2?'已提交':'进行中'),
        (r.startTime||''),(r.submitTime||'')
      ])
      const csv = [headers].concat(rows).map(line => line.map(v=>`"${String(v).replace(/"/g,'""')}"`).join(',')).join('\r\n')
      const blob = new Blob(["\uFEFF"+csv], { type: 'text/csv;charset=utf-8;' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `我的考试记录_${this.studentNo||''}.csv`
      document.body.appendChild(a); a.click(); document.body.removeChild(a)
    },
    printMy(){
      if (!this.myDisplayed.length) return
      // 去掉课堂列，新增评定
      const html = `<!DOCTYPE html><html><head><meta charset='utf-8'><title>我的考试记录</title>
        <style>table{border-collapse:collapse;width:100%;font-size:12px}th,td{border:1px solid #ccc;padding:6px;text-align:left}</style>
        </head><body>
        <h3>我的考试记录（学号：${this.studentNo}）</h3>
        <p>总记录：${this.myStats.total}；已完成：${this.myStats.finished}；平均分：${this.myStats.avgScore}；及格率：${this.myStats.passRate}%</p>
        <table><thead><tr><th>#</th><th>考试名称</th><th>课程</th><th>学号</th><th>成绩</th><th>评定</th><th>状态</th><th>开始时间</th><th>提交时间</th></tr></thead><tbody>
        ${this.myDisplayed.map((r,i)=>`<tr><td>${i+1}</td><td>${r.examName||''}</td><td>${r.courseName||''}</td><td>${r.studentNo||''}</td><td>${this.displayScoreRow(r)}</td><td>${this.passStatusText(r)}</td><td>${r.participantStatus===2?'已提交':'进行中'}</td><td>${r.startTime||''}</td><td>${r.submitTime||''}</td></tr>`).join('')}
        </tbody></table></body></html>`
      const w = window.open('', '_blank')
      if (w) { w.document.write(html); w.document.close(); w.focus(); w.print(); setTimeout(()=>w.close(), 500) }
    }
  }
}
</script>

<style scoped>
/* Mac Style for Exam Portal */
.exam-portal {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
}

/* Card Styling */
.exam-portal >>> .el-card {
  border-radius: 18px;
  border: none;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  background-color: #ffffff;
  margin-bottom: 24px;
}

.exam-portal >>> .el-card__header {
  border-bottom: 1px solid #f5f5f7;
  padding: 20px 24px;
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

/* Form Styling */
.exam-portal >>> .el-form-item__label {
  font-weight: 500;
  color: #1d1d1f;
}

.exam-portal >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  height: 36px;
  transition: all 0.2s ease;
}

.exam-portal >>> .el-input__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

/* Button Styling */
.exam-portal >>> .el-button {
  border-radius: 980px;
  font-weight: 500;
  border: none;
  padding: 9px 20px;
  transition: all 0.2s ease;
}

.exam-portal >>> .el-button--primary {
  background-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}

.exam-portal >>> .el-button--primary:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

.exam-portal >>> .el-button--success {
  background-color: #34c759;
  box-shadow: 0 2px 8px rgba(52, 199, 89, 0.2);
}

.exam-portal >>> .el-button--text {
  color: #0071e3;
  background: none;
  padding: 0 5px;
  box-shadow: none;
}

.exam-portal >>> .el-button--text:hover {
  color: #0077ed;
  background: none;
  transform: none;
}

/* Table Styling */
.exam-portal >>> .el-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.exam-portal >>> .el-table th {
  background-color: #fbfbfd;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #f5f5f7;
  padding: 12px 0;
}

.exam-portal >>> .el-table td {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
}

/* Radio Button Styling */
.exam-portal >>> .el-radio-button__inner {
  border-radius: 0;
  border: 1px solid #dcdfe6;
  box-shadow: none !important;
}

.exam-portal >>> .el-radio-button:first-child .el-radio-button__inner {
  border-radius: 10px 0 0 10px;
}

.exam-portal >>> .el-radio-button:last-child .el-radio-button__inner {
  border-radius: 0 10px 10px 0;
}

.exam-portal >>> .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background-color: #0071e3;
  border-color: #0071e3;
  box-shadow: -1px 0 0 0 #0071e3;
}

/* Alert Styling */
.exam-portal >>> .el-alert {
  border-radius: 10px;
  margin-top: 16px;
}

/* Descriptions Styling */
.exam-portal >>> .el-descriptions {
  margin-bottom: 16px;
}

.exam-portal >>> .el-descriptions__header {
  margin-bottom: 12px;
}

.exam-portal >>> .el-descriptions__title {
  font-weight: 600;
  color: #1d1d1f;
}

.exam-portal >>> .el-descriptions-item__label {
  color: #86868b;
  font-weight: 500;
}

.exam-portal >>> .el-descriptions-item__content {
  color: #1d1d1f;
  font-weight: 600;
}

/* Tags */
.exam-portal >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

.search-pane { margin-bottom: 24px; }
</style>
