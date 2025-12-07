<template>
  <div class="exam-portal" v-loading="loading">
    <el-card shadow="never" class="search-pane">
      <div slot="header">考试入口</div>
      <el-form inline @submit.native.prevent>
        <el-form-item label="学号">
          <el-input v-model.trim="studentNo" placeholder="自动获取学号" disabled style="width:240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadExams">查询可参加的考试</el-button>
          <el-button type="success" @click="loadMyList" :disabled="!studentNo" style="margin-left:8px">查询我的考试记录</el-button>
          <el-button v-if="studentNoStored" type="text" @click="clearStored" style="margin-left:4px">清除记住的学号</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="!studentNo" type="warning" :closable="false" title="未获取到学号信息，请确保您已登录且已绑定学生身份" />
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

    <!-- 未完成的考试弹窗 -->
    <el-card v-if="myLoaded" shadow="never" class="my-pane" style="margin-top:16px">
      <div slot="header" class="clearfix">
        <span>我的考试记录</span>
        <div style="float:right; display:flex; align-items:center; gap:8px">
          <el-select v-model="myQuery.filterCourse" placeholder="全部课程" size="mini" clearable style="width:150px">
            <el-option label="全部课程" value="" />
            <el-option
              v-for="course in distinctCourses"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId"
            />
          </el-select>
          <el-input v-model.trim="myQuery.keyword" placeholder="搜索考试/课程" size="mini" clearable style="width:220px" />
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
        <el-table-column prop="courseName" label="课程" width="140" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="totalScore" label="成绩" width="100">
          <template slot-scope="scope">{{ displayScoreRow(scope.row) }}</template>
        </el-table-column>
        <el-table-column prop="passStatus" label="评定" width="100">
          <template slot-scope="scope">
            <el-tag :type="passTagType(scope.row)">{{ passStatusText(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row)">{{ getStatusText(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="canEnterExam(scope.row)"
              type="primary"
              size="mini"
              @click="goToExam(scope.row)"
            >
              {{ scope.row.participantStatus === null || scope.row.participantStatus === undefined ? '开始考试' : '进入考试' }}
            </el-button>
            <span v-else-if="isExpired(scope.row)" style="color: #F56C6C;">已截止</span>
            <span v-else-if="scope.row.participantStatus === 2" style="color: #67C23A;">{{ displayScoreRow(scope.row) }}</span>
          </template>
        </el-table-column>
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
import { getCurrentStudent } from '@/api/proj_lw/classStudent'
import { getExam, listMyAvailableExams, listMyParticipants, startExamParticipant, getQuestionCorrectSummary } from '@/api/proj_lwj/exam'

export default {
  name: 'ExamPortal',
  data() {
    return {
      studentNo: '',
      studentNoStored: false,
      exams: [],
      loading: false,
      // 新增：是否已点击查询标记
      examsLoaded: false,
      myLoaded: false,
      myList: [], myLoading: false,
      myQuery: { keyword: '', filterCourse: '' }, // 只保留关键字和课程筛选
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
    // 获取所有不同的课堂（用于筛选）
    distinctSessions() {
      const sessions = new Map()
      this.myList.forEach(r => {
        const sid = r.sessionId
        const sname = r.className || r.sessionName || `课堂${sid}`
        if (sid && !sessions.has(sid)) {
          sessions.set(sid, { sessionId: sid, sessionName: sname })
        }
      })
      return Array.from(sessions.values()).sort((a, b) => a.sessionId - b.sessionId)
    },
    // 获取所有不同的课程（用于筛选）
    distinctCourses() {
      const courses = new Map()
      console.log('[distinctCourses] myList 长度:', this.myList.length)
      this.myList.forEach(r => {
        const cid = r.courseId
        const cname = r.courseName || `课程${cid}`
        console.log('[distinctCourses] 记录:', { courseId: cid, courseName: cname })
        if (cid && !courses.has(cid)) {
          courses.set(cid, { courseId: cid, courseName: cname })
        }
      })
      const result = Array.from(courses.values()).sort((a, b) => a.courseId - b.courseId)
      console.log('[distinctCourses] 最终课程列表:', result)
      return result
    },
    myDisplayed(){
      const kw = (this.myQuery.keyword||'').toLowerCase()
      const filterCourse = this.myQuery.filterCourse

      let filtered = this.myList

      // 关键字筛选
      if (kw) {
        filtered = filtered.filter(r=>
          String(r.examName||'').toLowerCase().includes(kw) ||
          String(r.courseName||'').toLowerCase().includes(kw)
        )
      }

      // 课程筛选
      if (filterCourse) {
        filtered = filtered.filter(r => r.courseId === Number(filterCourse))
      }

      return filtered
    }
  },
  watch: {
    studentNo(v) {
      if (v) {
        try { localStorage.setItem('exam_portal_studentNo', v); this.studentNoStored = true } catch(e){}
      } else {
        try { localStorage.removeItem('exam_portal_studentNo'); this.studentNoStored = false } catch(e){}
      }
    }
  },
  async mounted() {
    // 从当前登录用户自动获取学号
    await this.fetchCurrentStudent()
  },
  methods: {
    async fetchCurrentStudent() {
      this.loading = true
      try {
        const res = await getCurrentStudent()
        if (res && res.data && res.data.studentNo) {
          this.studentNo = res.data.studentNo
          this.$message.success('已自动获取学号: ' + this.studentNo)
        } else {
          this.$message.warning('未获取到学号信息，请确保已绑定学生身份')
        }
      } catch(e) {
        console.error('获取学号失败:', e)
        this.$message.error('获取学号失败，请确保已登录')
      } finally {
        this.loading = false
      }
    },
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
        const res = await listMyAvailableExams()
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
    displayScoreRow(row) {
      if (!row) return '——'
      if (row.participantStatus !== 2) return '——' // 未提交
      if (row.hasSubjective && row.correctStatus !== 1) return '批改中'
      return this.scoreDisplay(row.totalScore)
    },
    isExpired(row) {
      if (!row || !row.endTime) return false
      const now = Date.now()
      const endTime = new Date(row.endTime).getTime()
      return endTime < now
    },
    canEnterExam(row) {
      if (!row) return false

      const notExpired = !this.isExpired(row)

      // 未参加过或未提交的都可以进入（只要未截止）
      if (row.participantStatus === null || row.participantStatus === undefined) {
        return notExpired
      }

      const notFinished = row.participantStatus !== 2
      return notFinished && notExpired
    },
    getStatusText(row) {
      if (!row) return '——'

      // 已删除的考试
      if (row.isDeleted) {
        return '已删除'
      }

      // 未参加
      if (row.participantStatus === null || row.participantStatus === undefined) {
        const now = Date.now()
        const endTime = row.endTime ? new Date(row.endTime).getTime() : null
        const isExpired = endTime && endTime < now
        return isExpired ? '已截止(未参加)' : '未参加'
      }

      const isExpired = this.isExpired(row)
      const isFinished = row.participantStatus === 2

      if (isFinished) return '已完成'
      if (isExpired) return '已截止'
      return '待完成'
    },
    getStatusTagType(row) {
      if (!row) return 'info'

      // 已删除的考试
      if (row.isDeleted) {
        return 'danger'
      }

      // 未参加
      if (row.participantStatus === null || row.participantStatus === undefined) {
        const now = Date.now()
        const endTime = row.endTime ? new Date(row.endTime).getTime() : null
        const isExpired = endTime && endTime < now
        return isExpired ? 'danger' : 'info'
      }

      const isExpired = this.isExpired(row)
      const isFinished = row.participantStatus === 2

      if (isFinished) return 'success'
      if (isExpired) return 'danger'
      return 'warning'
    },
    async goToExam(row) {
      if (!row || !row.examId) return
      if (!this.studentNo) {
        this.$message.warning('请先输入学号')
        return
      }

      // 检查是否可以进入考试
      if (!this.canEnterExam(row)) {
        this.$message.warning('当前考试已截止或已完成')
        return
      }

      try {
        this.loading = true
        // 调用开考接口创建参与记录
        await startExamParticipant({
          examId: Number(row.examId),
          studentNo: this.studentNo
        })

        // 跳转到考试页面
        this.$router.push({
          path: '/proj_lwj/exam_take',
          query: {
            examId: row.examId,
            studentNo: this.studentNo,
            autoStart: '1'
          }
        })
      } catch (e) {
        console.error('开考失败:', e)
        const errMsg = e.message || e.msg || '开考失败，请稍后重试'
        this.$message.error(errMsg)
      } finally {
        this.loading = false
      }
    },
    async loadMyList(){
      if (!this.studentNo) { this.$message.info('请先输入学号'); return }
      this.myLoading = true
      this.myLoaded = true
      try {
        console.log('[ExamPortal] 开始加载我的考试记录（仅已参与的）...')
        console.log('[ExamPortal] 使用学号:', this.studentNo)

        // 获取用户的参与记录 - 使用 listMyParticipants 接口
        const participantsRes = await listMyParticipants(this.studentNo)
        console.log('[ExamPortal] API 返回:', participantsRes)

        const participants = (participantsRes && (participantsRes.data || participantsRes.rows || participantsRes.list)) || []
        console.log('[ExamPortal] 获取到参与记录:', participants.length, '个')
        if (participants.length > 0) {
          console.log('[ExamPortal] 第一条记录示例:', participants[0])
          console.log('[ExamPortal] 第一条记录的所有字段:', Object.keys(participants[0]))
        }

        // 直接使用参与记录作为列表
        let list = []
        if (Array.isArray(participants) && participants.length > 0) {
          list = participants.map(p => {
            if (!p) return null
            const examId = p.examId != null ? p.examId : (p.id != null ? p.id : p.exam_id)

            // 检查是否已删除（如果有 delFlag 字段）
            const isDeleted = p.delFlag === '1' || p.delFlag === 1 || p.status === 0

            return {
              examId,
              examName: p.examName || p.exam_name || '未命名考试',
              courseName: p.courseName || p.course_name || '未知课程',
              className: p.className || p.class_name || '',
              courseId: p.courseId || p.course_id,
              sessionId: p.sessionId || p.session_id,
              startTime: p.startTime || p.start_time,
              endTime: p.endTime || p.end_time,
              status: p.status,
              studentNo: this.studentNo,
              totalScore: p.totalScore != null ? p.totalScore : p.total_score,
              objectiveScore: p.objectiveScore != null ? p.objectiveScore : p.objective_score,
              passStatus: p.passStatus != null ? p.passStatus : p.pass_status,
              correctStatus: p.correctStatus != null ? p.correctStatus : p.correct_status,
              hasSubjective: p.hasSubjective != null ? p.hasSubjective : p.has_subjective,
              participantStatus: p.participantStatus != null ? p.participantStatus : p.participant_status,
              submitTime: p.submitTime || p.submit_time,
              isDeleted: isDeleted  // 标记已删除的考试
            }
          }).filter(item => item !== null)
        }

        console.log('[ExamPortal] 我的考试记录数:', list.length)
        if (list.length > 0) {
          const deletedCount = list.filter(r => r.isDeleted).length
          if (deletedCount > 0) {
            console.log('[ExamPortal] 其中已删除的考试:', deletedCount, '个')
          }
          // 调试：检查课程信息
          console.log('[ExamPortal] 第一条记录的课程信息:', {
            courseId: list[0].courseId,
            courseName: list[0].courseName
          })
          const withCourseId = list.filter(r => r.courseId).length
          console.log('[ExamPortal] 有 courseId 的记录:', withCourseId, '/', list.length)
        }

        // 推断主观题及 correctStatus 缺失的记录
        console.log('[ExamPortal] 其中已删除的考试:', list.filter(r => r.isDeleted).length, '个')

        // 推断主观题及 correctStatus 缺失的记录（仅有参与记录时）
        try {
          const needInfer = list.filter(r => r && r.participantStatus !== null && r.examId && (r.hasSubjective === undefined || r.correctStatus === undefined))
          if (needInfer.length > 0) {
            console.log('[ExamPortal] 需要推断主观题标记的记录数:', needInfer.length)
            await this.inferSubjectiveFlags(needInfer)
          }
        } catch (e) {
          console.error('[ExamPortal] 推断主观题标记失败:', e)
          // 不中断流程，继续执行
        }

        // 排序：有参与记录的优先，然后按提交时间或开始时间排序
        list.sort((a, b) => {
          // 有参与记录的排在前面
          const aHas = a.participantStatus !== null
          const bHas = b.participantStatus !== null
          if (aHas && !bHas) return -1
          if (!aHas && bHas) return 1

          // 都有或都没有参与记录，按时间排序
          const aTime = new Date(a.submitTime || a.startTime || 0).getTime()
          const bTime = new Date(b.submitTime || b.startTime || 0).getTime()
          return bTime - aTime
        })

        this.myList = list
        this.calcStats()

        if (!list.length) {
          console.log('[ExamPortal] ℹ️ 未获取到考试记录（participants:', participants.length, '个）')
          this.$message.info('当前没有考试记录。可能原因：您还未参加任何考试')
        } else {
          console.log('[ExamPortal] ✅ 成功加载', list.length, '条记录')
        }
      } catch(e){
        console.error('[ExamPortal] 加载考试记录失败 - 完整错误:', e)
        console.error('[ExamPortal] 错误类型:', typeof e)
        console.error('[ExamPortal] 错误信息:', e.message || '无')
        console.error('[ExamPortal] 错误堆栈:', e.stack || '无')

        const errorMsg = e.message || e.msg || (typeof e === 'string' ? e : '未知错误')
        this.$message.error('加载考试记录失败: ' + errorMsg)

        this.myList = []

        try {
          this.calcStats()
        } catch (statsError) {
          console.error('[ExamPortal] calcStats 也失败了:', statsError)
          // 手动设置默认统计值
          this.myStats = { total: 0, finished: 0, avgScore: '—', passRate: 0 }
        }
      } finally {
        this.myLoading = false
      }
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
    // ===== 未完成的考试 =====
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
