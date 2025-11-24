<template>
  <div class="exam-portal" v-loading="loading">
    <el-card shadow="never" class="search-pane">
      <div slot="header">考试入口</div>
      <el-form inline @submit.native.prevent>
        <el-form-item label="学号">
          <el-input v-model.trim="studentNo" placeholder="请输入学号" clearable style="width:240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadExams">查询我参加的考试</el-button>
          <el-button type="success" @click="loadMyList" :disabled="!studentNo" style="margin-left:8px">查询我的考试记录</el-button>
          <el-button v-if="studentNoStored" type="text" @click="clearStored" style="margin-left:4px">清除记住的学号</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="!studentNo" type="info" :closable="false" title="请输入学号，查询您可参加的考试列表或您的历史记录" />
    </el-card>

    <!-- 考试列表，仅在点击查询后显示 -->
    <el-card v-if="examsLoaded && displayedExams.length" shadow="never" class="list-pane">
      <div slot="header" class="clearfix">
        <span>考试列表 ({{ displayedExams.length }})</span>
        <el-button
          size="mini"
          type="primary"
          :loading="loading"
          @click="loadExams"
          style="float:right; margin-left:8px"
        >刷新</el-button>
      </div>
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
        <div style="float:right; display:flex; align-items:center; gap:8px">
          <el-input v-model.trim="myQuery.keyword" placeholder="搜索考试/课程" size="mini" clearable style="width:220px;margin-right:8px" />
          <el-button size="mini" type="primary" :loading="myLoading" @click="loadMyList">刷新</el-button>
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
      <div v-if="!myDisplayed.length" style="padding:12px 0">
        <el-empty description="暂无记录">
          <template #description>
            <div>
              暂无考试记录。
              <div v-if="studentNo">如果刚刚提交了含主观题的考试，成绩可能暂未显示，请稍后刷新或点击下方“诊断”。</div>
            </div>
          </template>
          <el-button type="primary" size="mini" @click="loadMyList">刷新</el-button>
          <el-button size="mini" @click="diagnoseMy" :loading="diagnosing">诊断</el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script>
import { listAvailableExams, startExamParticipant, listMyParticipants, getExam, listMyExams, getQuestionCorrectSummary } from '@/api/proj_lwj/exam'

export default {
  name: 'ExamPortal',
  data() {
    return {
      studentNo: '', exams: [], loading: false, studentNoStored: false,
      // 新增：是否已点击查询标记
      examsLoaded: false,
      myLoaded: false,
      myList: [], myLoading: false,
      myQuery: { keyword: '' },
      examCache: {},
      myStats: { total: 0, finished: 0, avgScore: '—', passRate: 0 },
      diagnosing:false
    }
  },
  computed: {
    displayedExams() {
      const now = Date.now()
      // 过滤条件：能够参加、还未结束、未删除、未完成的考试
      return (this.exams || []).filter(row => {
        const st = new Date(row.startTime).getTime()
        const ed = new Date(row.endTime).getTime()

        // 检查时间范围：考试已开始且未结束
        const timeValid = now >= st && now <= ed

        // 检查考试状态：已发布(1)或进行中(2)
        const statusValid = (row.status === 1 || row.status === 2)

        // 检查是否已完成：未提交
        const notSubmitted = !this.submittedLocal(row)

        return timeValid && statusValid && notSubmitted
      })
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
            // 检查后端考试是否仍存在（避免显示已删除考试）
            if (this.examCache[obj.id] === undefined) {
              // 尝试同步获取一次
              // 由于 mergeLocalFinished 在 loadExams 后调用，此时可以异步补充但不阻塞
              this.$set(this.examCache, obj.id, null)
              getExam(obj.id).then(res => {
                if (res && res.data && !res.data.missing) {
                  this.$set(this.examCache, obj.id, res.data)
                } else {
                  // 标记 missing，避免加入
                  this.$set(this.examCache, obj.id, { missing: true })
                }
              }).catch(() => { this.$set(this.examCache, obj.id, { missing: true }) })
            }
            const ex = this.examCache[obj.id]
            if (ex && ex.missing) continue
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
      if(!row) return '——'
      // 当存在主观题且 correctStatus!=1 时显示 待批改
      if (row.hasSubjective && row.correctStatus !== 1) return '待批改'
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
      if(!row) return '——'
      // 未批改主观题不显示及格/不及格
      if (row.hasSubjective && row.correctStatus !== 1) return '待批改'
      const v = Number(row && row.passStatus)
      if (v === 1) return '及格'
      if (v === 0) return '不及格'
      return '——'
    },
    passTagType(row){
      if(!row) return 'info'
      if (row.hasSubjective && row.correctStatus !== 1) return 'info'
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
        // 同时获取新版与旧版数据，合并去重，避免任一接口结构变化导致列表空白
        const [newRes, oldRes] = await Promise.all([
          listMyExams(this.studentNo).catch(()=>({})),
          listMyParticipants(this.studentNo).catch(()=>({}))
        ])
        const newListRaw = newRes && (newRes.data || newRes.rows || newRes.list)
        const oldListRaw = oldRes && (oldRes.data || oldRes.rows || oldRes.list)
        let newList = Array.isArray(newListRaw) ? newListRaw.slice() : []
        let oldList = Array.isArray(oldListRaw) ? oldListRaw.slice() : []
        // 标准化字段: examId 兼容 id
        const normalize = (arr) => arr.map(r => ({
          ...r,
          examId: r.examId != null ? r.examId : (r.id != null ? r.id : r.exam_id),
          totalScore: r.totalScore != null ? r.totalScore : r.total_score,
          objectiveScore: r.objectiveScore != null ? r.objectiveScore : r.objective_score,
          passStatus: r.passStatus != null ? r.passStatus : r.pass_status,
          correctStatus: r.correctStatus != null ? r.correctStatus : r.correct_status,
          hasSubjective: r.hasSubjective != null ? r.hasSubjective : r.has_subjective
        }))
        newList = normalize(newList)
        oldList = normalize(oldList)
        // 合并：以 examId+studentNo 为主键，新接口优先
        const map = new Map()
        for (const item of oldList) {
          if (!item.examId) continue
          const key = item.examId + ':' + (item.studentNo || item.student_no || '')
          map.set(key, item)
        }
        for (const item of newList) {
          if (!item.examId) continue
          const key = item.examId + ':' + (item.studentNo || item.student_no || '')
          map.set(key, { ...map.get(key), ...item })
        }
        let merged = Array.from(map.values())
        // 推断主观题及 correctStatus 缺失的记录（仅缺失时）
        const needInfer = merged.filter(r => r.examId && (r.hasSubjective === undefined || r.correctStatus === undefined))
        if (needInfer.length) await this.inferSubjectiveFlags(needInfer)
        // 填充考试基本信息
        await this.enrichExamInfo(merged)
        // 排序：提交时间优先，其次开始时间
        merged.sort((a,b)=> new Date(b.submitTime||0)-new Date(a.submitTime||0) || new Date(b.startTime||0)-new Date(a.startTime||0))
        this.myList = merged
        this.calcStats()
        if (!merged.length) {
          this.$message.info('未获取到考试记录：可能尚无参与记录或接口数据为空')
        }
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
    async inferSubjectiveFlags(list){
      // 为没有 hasSubjective 或 correctStatus 字段的记录进行推断
      const needSummaryIds = []
      list.forEach(r=>{
        if (r.hasSubjective === undefined) needSummaryIds.push(r.examId)
      })
      const uniq = Array.from(new Set(needSummaryIds.filter(Boolean)))
      for(const id of uniq){
        try {
          const summary = await getQuestionCorrectSummary(id)
          const payload = summary && (summary.data || summary)
          const hasSubj = !!(payload && payload.hasSubjective)
          list.forEach(r=>{ if(r.examId===id && r.hasSubjective===undefined) r.hasSubjective = hasSubj })
          if (payload && payload.questions){
            const ungradedAny = payload.questions.some(q=> q.ungradedCount && q.ungradedCount>0)
            list.forEach(r=>{ if(r.examId===id && r.correctStatus===undefined && hasSubj) r.correctStatus = ungradedAny?0:1 })
          }
        } catch(e){ /* ignore single exam */ }
      }
    },
    async diagnoseMy(){
      if(!this.studentNo){ return this.$message.info('请先输入学号') }
      this.diagnosing=true
      try {
        await this.loadMyList()
        const hidden = this.myList.filter(r=> r.hasSubjective && r.correctStatus!==1 && (r.totalScore==null))
        if(hidden.length){
          this.$message.info(`诊断：发现 ${hidden.length} 场含主观题未批改考试，成绩已隐藏为“待批改”`)
        } else if(this.myList.length){
          this.$message.success('诊断：当前考试记录正常')
        } else {
          this.$message.warning('诊断：仍未获取到考试记录，可能学号无考试或接口限制')
        }
      } finally { this.diagnosing=false }
    },
    // 重新加入统计方法
    calcStats(){
      const total = this.myList.length
      let finished = 0, pass = 0, sum = 0, cnt = 0, gradedTotal = 0
      this.myList.forEach(r=>{
        if (r.participantStatus === 2) finished++
        const ungradedSubjective = r.hasSubjective && r.correctStatus !== 1
        // 只有已批改或无主观题的记录才计入 passRate 统计
        if (!ungradedSubjective) {
          gradedTotal++
          if (r.passStatus === 1) pass++
        }
        // 平均分只统计可见的分数
        const visibleTotal = ungradedSubjective ? null : r.totalScore
        if (visibleTotal != null && !isNaN(visibleTotal)) { sum += Number(visibleTotal); cnt++ }
      })
      const avg = cnt>0 ? (Math.round((sum/cnt)*100)/100) : '—'
      const rate = gradedTotal>0 ? Math.round((pass*10000/gradedTotal))/100 : 0
      this.myStats = { total, finished, avgScore: avg, passRate: rate }
    },
    exportMyCSV(){
      if (!this.myDisplayed.length) return this.$message.info('暂无记录可导出')
      const headers = ['考试名称','课程','学号','成绩','评定','状态','开始时间','提交时间']
      const rows = this.myDisplayed.map(r=>[
        (r.examName||''),(r.courseName||''),(r.studentNo||''),
        this.displayScoreRow(r), this.passStatusText(r), (r.participantStatus===2?'已提交':'进行中'),
        (r.startTime||''),(r.submitTime||'')
      ])
      const csv = [headers].concat(rows).map(line => line.map(v=>`"${String(v).replace(/"/g,'""')}"`).join(',')).join('\r\n')
      try {
        const blob = new Blob(['\uFEFF'+csv], { type: 'text/csv;charset=utf-8;' })
        const a = document.createElement('a')
        a.href = URL.createObjectURL(blob)
        a.download = `我的考试记录_${this.studentNo||''}.csv`
        document.body.appendChild(a); a.click(); document.body.removeChild(a)
      } catch(e){ console.error(e); this.$message.error('导出失败') }
    },
    printMy(){
      if (!this.myDisplayed.length) return this.$message.info('暂无记录可打印')
      const html = `<!DOCTYPE html><html><head><meta charset='utf-8'><title>我的考试记录</title><style>table{border-collapse:collapse;width:100%;font-size:12px}th,td{border:1px solid #ccc;padding:6px;text-align:left}</style></head><body>
        <h3>我的考试记录（学号：${this.studentNo}）</h3>
        <p>总记录：${this.myStats.total}；已完成：${this.myStats.finished}；平均分：${this.myStats.avgScore}；及格率：${this.myStats.passRate}%</p>
        <table><thead><tr><th>#</th><th>考试名称</th><th>课程</th><th>学号</th><th>成绩</th><th>评定</th><th>状态</th><th>开始时间</th><th>提交时间</th></tr></thead><tbody>
        ${this.myDisplayed.map((r,i)=>`<tr><td>${i+1}</td><td>${r.examName||''}</td><td>${r.courseName||''}</td><td>${r.studentNo||''}</td><td>${this.displayScoreRow(r)}</td><td>${this.passStatusText(r)}</td><td>${r.participantStatus===2?'已提交':'进行中'}</td><td>${r.startTime||''}</td><td>${r.submitTime||''}</td></tr>`).join('')}
        </tbody></table></body></html>`
      try {
        const w = window.open('', '_blank')
        if (w) { w.document.write(html); w.document.close(); w.focus(); w.print(); setTimeout(()=>w.close(), 500) }
      } catch(e){ console.error(e); this.$message.error('打印失败') }
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
