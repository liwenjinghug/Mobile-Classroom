<template>
  <div class="exam-take" v-loading="loading">
    <el-card shadow="never" class="exam-header">
      <div class="exam-title">{{ exam ? exam.examName : '考试' }} (ID: {{ examId }})</div>
      <div class="exam-meta">
        <span>状态: <el-tag size="mini" :type="statusTagType">{{ statusLabel }}</el-tag></span>
        <!-- 仅在开始后显示剩余时间；未开始不显示倒计时，也不显示“已超时” -->
        <span v-if="started && timeRemaining !== null && timeRemaining >= 0">剩余时间: <strong>{{ formatDuration(timeRemaining) }}</strong></span>
        <span v-else-if="started && timeRemaining !== null && timeRemaining < 0">已超时</span>
        <!-- 移除头部的当前得分显示，避免考试过程中展示总分进度 -->
        <!-- <span>当前得分: {{ (participant && participant.totalScore) || 0 }} / {{ (exam && exam.totalScore) || 0 }}</span> -->
      </div>
      <div class="exam-actions">
        <el-button size="mini" type="primary" :disabled="!canStart || started" @click="start">开始考试</el-button>
        <el-button size="mini" type="warning" :disabled="!started || submitted" @click="submitPaper">交卷</el-button>
        <el-button size="mini" type="info" @click="refreshAll">刷新</el-button>
      </div>
    </el-card>

    <!-- 将题目区域改为：未开始不显示题目列表，仅提示 -->
    <el-row v-if="started" :gutter="12" class="exam-body">
      <el-col :span="6" class="question-list-pane">
        <el-card shadow="never" class="question-list">
          <div slot="header" class="clearfix">
            <span>题目 ({{ questions.length }})</span>
          </div>
          <el-scrollbar style="height:60vh">
            <ul class="questions-ul">
              <li v-for="(q,i) in questions" :key="q.id" :class="['q-item', currentIndex===i?'active':'']" @click="selectIndex(i)">
                <span class="idx">{{ i+1 }}.</span>
                <span class="type">{{ typeLabel(q.questionType) }}</span>
                <span class="score">[{{ q.score }}分]</span>
                <span class="state" v-if="answerMap[q.id]">✔</span>
              </li>
            </ul>
          </el-scrollbar>
        </el-card>
      </el-col>
      <el-col :span="18" class="question-detail-pane">
        <el-card v-if="currentQuestion" shadow="never" class="question-detail">
          <div slot="header" class="clearfix">
            <span>第 {{ currentIndex+1 }} 题 / {{ questions.length }} | {{ typeLabel(currentQuestion.questionType) }} | 分值 {{ currentQuestion.score }}</span>
          </div>
          <div class="q-content" v-html="currentQuestion.questionContent"></div>

          <div v-if="currentQuestion.questionType===1" class="single-choice">
            <el-radio-group v-model="workingAnswer" @change="onAnswerChanged">
              <el-radio v-for="opt in parsedOptions" :label="opt" :key="opt">{{ opt }}</el-radio>
            </el-radio-group>
          </div>
          <div v-else-if="currentQuestion.questionType===3" class="judge-choice">
            <el-radio-group v-model="workingAnswer" @change="onAnswerChanged">
              <el-radio label="true">正确</el-radio>
              <el-radio label="false">错误</el-radio>
            </el-radio-group>
          </div>
          <div v-else-if="currentQuestion.questionType===5" class="short-answer">
            <el-input type="textarea" :rows="6" v-model="workingAnswer" placeholder="请输入答案" @input="onAnswerInput"></el-input>
          </div>
          <div v-else>
            <el-alert type="warning" title="题型暂未实现此处作答面板" show-icon></el-alert>
          </div>

          <div class="q-ops">
            <!-- 自动保存提示，去除手动保存按钮 -->
            <el-tag size="mini" type="info">答题自动保存</el-tag>
            <el-button size="mini" @click="prev" :disabled="currentIndex<=0" style="margin-left:8px">上一题</el-button>
            <el-button size="mini" @click="next" :disabled="currentIndex>=questions.length-1">下一题</el-button>
          </div>
          <!-- 调整题目下方反馈：答题过程中不显示“得分 x / y”，只在交卷或解析模式显示 -->
          <div v-if="answerResultMap[currentQuestion.id]" class="answer-feedback">
            <template v-if="showImmediateAnswer || (canShowPerQuestionScore && allowShowCorrect)">
              <el-tag :type="answerResultMap[currentQuestion.id].correct?'success':'danger'">
                {{ answerResultMap[currentQuestion.id].correct ? '正确' : '错误' }}
              </el-tag>
            </template>
            <el-tag v-if="canShowPerQuestionScore && (answerResultMap[currentQuestion.id].score !== undefined)" type="info" style="margin-left:6px">
              得分: {{ formatScore(answerResultMap[currentQuestion.id].score) }} / {{ currentQuestion.score }}
            </el-tag>
            <el-tag v-else-if="canShowPerQuestionScore" type="warning" style="margin-left:6px">待批改</el-tag>
            <span v-if="currentQuestion.analysis && (showImmediateAnswer || (canShowPerQuestionScore && allowShowCorrect))" style="margin-left:8px">解析：{{ currentQuestion.analysis }}</span>
          </div>
        </el-card>
        <el-empty v-else description="请选择题目"></el-empty>
      </el-col>
    </el-row>
    <div v-else class="not-started-tip">
      <el-empty description="尚未开始考试，开始后才可查看题目" />
    </div>

    <!-- 成绩汇总弹窗 -->
    <el-dialog title="考试成绩" :visible.sync="summaryVisible" width="800px" v-if="exam">
      <div v-if="submitted">
        <!-- 未批改完主观题前，不显示及格与否，仅提示客观题得分 -->
        <el-alert v-if="canJudgePass"
                  :title="passStatus ? '恭喜，考试及格' : '未及格，继续加油'"
                  :type="passStatus ? 'success' : 'warning'" show-icon style="margin-bottom:12px" />
        <el-alert v-else type="info" title="部分题目待批改，当前显示客观题得分" show-icon style="margin-bottom:12px"/>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="考试名称">{{ exam.examName }}</el-descriptions-item>
          <el-descriptions-item label="考试类型">{{ examTypeLabel(exam.examType) }}</el-descriptions-item>
          <el-descriptions-item label="用时(估算)">{{ usedDurationDisplay }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ exam.totalScore }}</el-descriptions-item>
          <el-descriptions-item :label="scoreLabel">{{ finalScoreDisplay }}</el-descriptions-item>
          <el-descriptions-item label="及格线">{{ exam.passScore }}</el-descriptions-item>
          <el-descriptions-item label="题目数量">{{ questions.length }}</el-descriptions-item>
          <el-descriptions-item label="答案策略">{{ showAnswerPolicyLabel }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="summaryRows" size="mini" style="margin-top:16px" height="360">
          <el-table-column prop="index" label="#" width="50" />
          <el-table-column prop="type" label="题型" width="80" />
          <el-table-column prop="score" label="得分" width="100">
            <template slot-scope="scope">{{ scope.row.scoreDisplay }}</template>
          </el-table-column>
          <el-table-column prop="maxScore" label="满分" width="80" />
          <el-table-column v-if="showResultColumn" prop="correct" label="结果" width="80">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.correct === true" type="success" size="mini">正确</el-tag>
              <el-tag v-else-if="scope.row.correct === false" type="danger" size="mini">错误</el-tag>
              <el-tag v-else type="info" size="mini">待批改</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="answer" label="我的答案" min-width="160" show-overflow-tooltip />
          <el-table-column v-if="allowShowCorrect" prop="correctAnswer" label="正确答案" min-width="160" show-overflow-tooltip />
        </el-table>
      </div>
      <div v-else><el-empty description="尚未交卷" /></div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="summaryVisible=false">关闭</el-button>
        <el-button v-if="canShowAnalysis" @click="openAnalysis" type="info">查看解析</el-button>
        <el-button type="primary" @click="goBackPortal">返回考试入口</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getExam, listQuestions, startExamParticipant, saveAnswer, submitExam, listAnswers } from '@/api/proj_lwj/exam' // eslint-disable-line

export default {
  name: 'ExamTake',
  props: { examId: { type: [String, Number], required: true } },
  data() {
    return {
      loading: false,
      exam: null,
      questions: [],
      currentIndex: 0,
      participant: null,
      workingAnswer: '',
      answerMap: {},
      answerResultMap: {},
      timer: null,
      timeRemaining: null,
      submitted: false,
      started: false,
      showImmediateAnswer: false,
      studentNo: '',
      summaryVisible: false,
      analysisMode: false,
      autoSaveTimer: null,
      // 新增：答题自动保存去抖定时器
      answerSaveTimer: null
    }
  },
  computed: {
    currentQuestion() { return this.questions[this.currentIndex] },
    statusLabel() { const s = this.exam ? this.exam.status : undefined; return s===0?'草稿':s===1?'已发布':s===2?'进行中':s===3?'已结束':'未知' },
    statusTagType() { const s = this.exam ? this.exam.status : undefined; return s===2?'success':s===1?'info':s===3?'warning':'default' },
    canStart() { if (!this.exam) return false; const now=Date.now(); const start=new Date(this.exam.startTime).getTime(); const end=new Date(this.exam.endTime).getTime(); return this.exam.status >= 1 && now >= start && now <= end && !this.submitted },
    parsedOptions() { if (!this.currentQuestion || !this.currentQuestion.questionOptions) return []; try { return JSON.parse(this.currentQuestion.questionOptions) } catch(e){ return [] } },
    displayedTotal() { if (this.participant && this.participant.totalScore != null) return this.participant.totalScore; let sum=0; Object.keys(this.answerResultMap).forEach(k=>{ const v=this.answerResultMap[k]; if(v && v.score!==undefined && v.score!==null && !isNaN(v.score)) sum+=Number(v.score) }); return sum },
    // 是否存在未批改的主观题
    hasUnscoredSubjective(){
      return this.questions.some(q => this.isSubjective(q.questionType) && (!this.answerResultMap[q.id] || this.answerResultMap[q.id].score === undefined))
    },
    // 客观题总分（选择/判断/多选等）
    objectiveTotal(){
      let total = 0
      this.questions.forEach(q => {
        if (this.isObjective(q.questionType)) {
          const res = this.answerResultMap[q.id]
          if (res && res.score !== undefined && !isNaN(res.score)) {
            total += Number(res.score)
          } else {
            // 兜底：前端自行比对（单选/判断）
            const ans = this.answerMap[q.id]
            const correct = this.quickJudge(q.questionType, ans, q.correctAnswer)
            total += correct ? Number(q.score) : 0
          }
        }
      })
      return total
    },
    // 新增：更严格的未批改主观题判定（结合后端 correctStatus 与前端本地缺分情况）
    hasUngradedSubjective(){
      // 存在主观题，并且：
      // 1) 后端标记未完成批改 (participant.correctStatus !== 1)
      // 或 2) 前端检测到至少一个主观题分数缺失
      const hasSub = this.hasSubjectiveQuestions
      if (!hasSub) return false
      const backendUnfinished = this.participant && this.participant.correctStatus !== 1
      return backendUnfinished || this.hasUnscoredSubjective
    },
    // 总分隐藏标志：存在未批改主观题时隐藏
    hideTotalScore(){
      return this.hasUngradedSubjective
    },
    // 能否判断及格：未批改主观题直接 false
    canJudgePass(){
      if (this.hasUngradedSubjective) return false
      // 后端 correctStatus=1 时即可；否则再以本地检测兜底
      if (this.participant && this.participant.correctStatus !== undefined) {
        return this.participant.correctStatus === 1
      }
      return !this.hasUnscoredSubjective
    },
    passStatus(){
      if (!this.canJudgePass) return false
      if (this.participant && this.participant.passStatus !== undefined) {
        return this.participant.passStatus === 1
      }
      const t=this.finalScoreNumeric
      return this.exam ? Number(t) >= Number(this.exam.passScore) : false
    },
    // 最终用于展示的分值：未批改主观题时仅展示客观题得分
    finalScoreNumeric(){
      if (this.hideTotalScore) {
        // 优先用后端 objectiveScore，其次前端计算的 objectiveTotal
        if (this.participant && this.participant.objectiveScore != null) return Number(this.participant.objectiveScore)
        return this.objectiveTotal
      }
      // 已完成批改：优先后端 totalScore
      if (this.participant && this.participant.totalScore != null) return Number(this.participant.totalScore)
      return this.displayedTotal
    },
    finalScoreDisplay(){ return this.hideTotalScore ? '待批改' : this.formatScore(this.finalScoreNumeric) },
    scoreLabel(){ return this.hideTotalScore ? '客观题得分' : (this.canJudgePass ? '我的得分' : '客观题得分') },
    summaryRows(){ return this.questions.map((q,i)=>{ const res=this.answerResultMap[q.id]||{}; const sc=res.score; const subj=this.isSubjective(q.questionType); let correct=res.correct; if(subj && (sc===undefined || sc===null)) { correct=undefined } return { index:i+1, type:this.typeLabel(q.questionType), score:sc, maxScore:q.score, correct, answer:this.answerMap[q.id]||'', correctAnswer:this.allowShowCorrect?(q.correctAnswer||''):'', scoreDisplay: sc===undefined? (subj? '待批改':(res.correct===undefined?'—':(res.correct? this.formatScore(q.score):'0'))): this.formatScore(sc) } }) },
    // 新增：结果列是否显示（提交后且允许显示答案才展示）
    showResultColumn(){
      if(!this.exam) return true
      if(!this.submitted) return false
      // showAnswer: 0=不显示答案,1=即时显示,2=仅提交后显示
      const mode = this.exam.showAnswer
      if(mode === 0) return false
      return true
    },
    canShowAnalysis() { if (!this.exam) return false; return (this.exam.showAnswer === 1) || (this.exam.showAnswer === 2 && this.submitted) },
    showAnalysisInQuestion() { if (!this.submitted) return this.exam && this.exam.showAnswer === 1; return this.canShowAnalysis && this.analysisMode },
    // 新增：仅在交卷或解析模式下显示每题得分/待批改
    canShowPerQuestionScore(){ return this.submitted || this.analysisMode },
    usedDurationDisplay(){
      // 显示考试用时：如果已提交，用提交时间 - 开始时间；否则实时 now - 开始时间
      if(!this.participant || !this.participant.startTime) return '—'
      const startMs = new Date(this.participant.startTime).getTime()
      if(isNaN(startMs)) return '—'
      const endMs = this.participant.submitTime ? new Date(this.participant.submitTime).getTime() : Date.now()
      if(isNaN(endMs) || endMs < startMs) return '—'
      const diffSec = Math.floor((endMs - startMs)/1000)
      const h = Math.floor(diffSec/3600), m = Math.floor((diffSec%3600)/60), s = diffSec%60
      const pad = n=>String(n).padStart(2,'0')
      return (h>0? pad(h)+':':'') + pad(m)+':'+pad(s)
    },
    showAnswerPolicyLabel(){
      if(!this.exam) return '—'
      const map = {0:'不显示',1:'立即显示',2:'考试结束后'}
      return map[this.exam.showAnswer] || '—'
    },
    allowShowCorrect(){
      if(!this.exam) return false
      const mode = this.exam.showAnswer
      if(mode === 1) return true
      if(mode === 2) return this.submitted
      return false
    }
  },
  created() {
    this.studentNo = (this.$route.query && this.$route.query.studentNo) ? String(this.$route.query.studentNo) : ''
    try { const key = `exam_submitted_${this.examId}_${this.studentNo}`; if (localStorage.getItem(key) === '1') { this.$message.warning('该考试已完成，不能重复进入'); this.$router.push({ name:'ExamPortal', path:'/proj_lwj/exam_portal' }).catch(()=>{}); return } } catch(e){}
    this.init()
  },
  watch: {
    '$route.query.autoStart': { handler(val){ /* 禁用自动开始 */ }, immediate:false },
    // 移除自动开始考试逻辑，用户需手动点击“开始考试”
    // canStart 变化不再触发自动开始
    summaryVisible(val){
      if(val){
        // 打开弹窗时移除可能残留在题目选项上的焦点，避免 aria-hidden 焦点警告
        this.$nextTick(()=>{ try { const ae=document.activeElement; if(ae && typeof ae.blur==='function') ae.blur() } catch(e){} })
      }
    }
  },
  beforeDestroy(){ if (this.timer) clearInterval(this.timer); if (this.autoSaveTimer) clearInterval(this.autoSaveTimer); if (this.answerSaveTimer) clearTimeout(this.answerSaveTimer) },
  methods: {
    // 考试类型标签映射
    examTypeLabel(t) {
      const n = Number(t);
      switch (n) {
        case 1: return '期中';
        case 2: return '期末';
        case 3: return '测验';
        case 4: return '模拟考';
        case 5: return '课堂测验';
        default: return '其它';
      }
    },
    // 题型判断
    isObjective(t){ return [1,2,3].includes(Number(t)) },
    isSubjective(t){ return [4,5,6].includes(Number(t)) },
    hasSubjectiveQuestions(){ return this.questions.some(q => this.isSubjective(q.questionType)) },
    // 简单兜底判分：仅支持单选(1)/判断(3)
    quickJudge(t, ans, correct){
      const tp=Number(t)
      if (tp===1) {
        return String(ans||'').trim() === String(correct||'').trim()
      }
      if (tp===3) {
        const norm = v => String(v||'').toString().trim().toLowerCase()
        const mapTrue = ['true','1','t','y','yes','对','正确']
        const mapFalse = ['false','0','f','n','no','错','错误']
        const a = norm(ans), c = norm(correct)
        return (mapTrue.includes(a) && mapTrue.includes(c)) || (mapFalse.includes(a) && mapFalse.includes(c))
      }
      return false
    },
    async init(){ this.loading=true; try { await this.loadExam(); await this.loadQuestions(); this.showImmediateAnswer = (this.exam && this.exam.showAnswer === 1); if (this.autoSaveTimer) clearInterval(this.autoSaveTimer); this.autoSaveTimer=setInterval(this.autoSaveTick,30000) } catch(e){ console.error(e) } finally { this.loading=false } },
    async loadExam(){ const res=await getExam(this.examId); this.exam=res.data; this.computeRemaining() },
    async loadQuestions(){ const res=await listQuestions({ examId: this.examId }); this.questions=res.data||[] },
    async refreshAll(){ await this.init(); if (this.started) this.computeRemaining() },
    computeRemaining(){
      if (this.timer) { clearInterval(this.timer); this.timer = null }
      if(!this.exam){ this.timeRemaining = null; return }
      // 只有开始后才启动倒计时；剩余时间 = min(考试窗口结束, 开始时间 + 时长) - now
      if(!this.started){ this.timeRemaining = null; return }
      const now = Date.now()
      const endByWindow = new Date(this.exam.endTime).getTime()
      const durationMin = Number(this.exam.examDuration || this.exam.exam_duration || 0)
      const durationSec = isNaN(durationMin) ? 0 : durationMin * 60
      let endByDuration = null
      if (this.participant && (this.participant.startTime || this.participant.start_time)){
        const st = new Date(this.participant.startTime || this.participant.start_time).getTime()
        if (!isNaN(st) && durationSec > 0){ endByDuration = st + durationSec * 1000 }
      }
      let finalEnd = endByWindow
      if (endByDuration){ finalEnd = Math.min(finalEnd, endByDuration) }
      this.timeRemaining = Math.floor((finalEnd - now)/1000)
      this.timer = setInterval(()=>{
        this.timeRemaining -= 1
        if (this.timeRemaining < 0){
          clearInterval(this.timer); this.timer = null
          this.autoSubmitIfNeeded()
        }
      },1000)
    },
    selectIndex(i){ if(!this.started){ this.$message.info('请先点击“开始考试”'); return } this.currentIndex=i; this.workingAnswer=this.answerMap[this.currentQuestion.id]||'' },
    prev(){ if(this.currentIndex>0) this.selectIndex(this.currentIndex-1) },
    next(){ if(this.currentIndex < this.questions.length-1) this.selectIndex(this.currentIndex+1) },
    typeLabel(t){ return t===1?'单选':t===3?'判断':t===5?'简答':t===4?'填空':'其它' },
    async start(){ if(!this.canStart) return this.$message.error('尚未到可开始时间或考试未发布'); try { const res=await startExamParticipant({ examId:Number(this.examId), studentNo:this.studentNo }); this.participant=res.data; this.started=true; this.computeRemaining(); this.$message.success('考试已开始') } catch(e){ console.error(e); this.$message.error('开始考试失败') } },
    formatScore(s){ return (s===undefined||s===null)?'—': (Number(s)%1===0?Number(s):Number(s).toFixed(2)) },
    recalculateTotal(){ if(!this.participant) this.participant={}; if(this.participant.totalScore==null) { this.participant.totalScore=this.displayedTotal } },
    // 新增：答题变化时的自动保存（去抖）
    onAnswerChanged(){ this.scheduleSave(500) },
    onAnswerInput(){ this.scheduleSave(800) },
    scheduleSave(delay=600){
      if(!this.started || !this.currentQuestion) return
      // 捕获快照，避免切题后保存到错误题目
      const qid = this.currentQuestion.id
      const value = this.workingAnswer
      if (this.answerSaveTimer) clearTimeout(this.answerSaveTimer)
      this.answerSaveTimer = setTimeout(()=>{ this.saveAnswerFor(qid, value).catch(()=>{}) }, delay)
    },
    async saveAnswerFor(qid, value){
      if(!this.started) { this.$message.error('请先开始考试'); return }
      const saved = this.answerMap[qid]
      if (value === saved) return
      const q = this.questions.find(x => String(x.id) === String(qid)) || this.currentQuestion
      if(!q) return
      const subj = this.isSubjective(q.questionType)
      const basePayload = { examId:Number(this.examId), questionId:qid, studentNo:this.studentNo, studentAnswer:value, correctAnswer:q.correctAnswer }
      // 客观题附带分值，主观题不发送分值避免后端误记 0 分
      const payload = subj ? basePayload : { ...basePayload, score:q.score }
      const res=await saveAnswer(payload)
      this.answerMap[qid]=value
      if(!this.answerResultMap[qid]) this.answerResultMap[qid]={}
      const qType = Number(q.questionType)
      if(res && res.data){
        if(res.data.isCorrect!==undefined){ this.answerResultMap[qid].correct = (res.data.isCorrect===1 || res.data.isCorrect===true) }
        if(res.data.score!==undefined){ this.answerResultMap[qid].score=Number(res.data.score) }
      }
      if(this.answerResultMap[qid].score===undefined && this.isObjective(qType)){
        const correct = this.quickJudge(qType, value, q.correctAnswer)
        this.answerResultMap[qid].correct = correct
        this.answerResultMap[qid].score = correct ? Number(q.score) : 0
      }
      this.recalculateTotal()
    },
    async saveCurrent(){
      // 兼容保留：直接保存当前题目
      if(!this.started || !this.currentQuestion) return
      return this.saveAnswerFor(this.currentQuestion.id, this.workingAnswer)
    },
    async submitPaper(){
      if(!this.started) return this.$message.error('还未开始考试')
      try {
        const res = await submitExam({ examId: Number(this.examId), studentNo: this.studentNo })
        this.participant = res.data
        this.submitted = true
        this.started = false

        // 重新加载答案数据以获取后端的自动判分结果
        try {
          const answerRes = await listAnswers({ examId: this.examId, studentNo: this.studentNo })
          if (answerRes && answerRes.rows) {
            answerRes.rows.forEach(ans => {
              const qid = ans.questionId
              if (!this.answerResultMap[qid]) this.answerResultMap[qid] = {}
              if (ans.isCorrect !== undefined) {
                this.answerResultMap[qid].correct = (ans.isCorrect === 1 || ans.isCorrect === true)
              }
              if (ans.score !== undefined && ans.score !== null) {
                this.answerResultMap[qid].score = Number(ans.score)
              }
            })
          }
        } catch(e) {
          console.error('加载答案数据失败', e)
        }

        try {
          localStorage.setItem(`exam_submitted_${this.examId}_${this.studentNo}`, '1')
          if(this.finalScoreNumeric != null){
            localStorage.setItem(`exam_score_${this.examId}_${this.studentNo}`, String(this.finalScoreNumeric))
          }
          const summary = {
            id: Number(this.examId),
            examName: this.exam?.examName || '',
            startTime: this.exam?.startTime || '',
            endTime: this.exam?.endTime || '',
            status: 3,
            courseId: this.exam?.courseId,
            sessionId: this.exam?.sessionId,
            courseName: this.exam?.courseName || '',
            className: this.exam?.className || '',
            myScore: this.finalScoreNumeric,
            passScore: this.exam?.passScore,
            totalScore: this.exam?.totalScore
          }
          localStorage.setItem(`exam_summary_${this.examId}_${this.studentNo}`, JSON.stringify(summary))
        } catch(e){}

        if(this.exam && this.exam.showAnswer === 2){
          try {
            await this.loadQuestions()
          } catch(e){}
        }

        this.summaryVisible = true
        this.$message.success('已交卷')
      } catch(e){
        console.error(e)
        this.$message.error('交卷失败')
      }
    },
    openAnalysis(){ this.summaryVisible=false; this.analysisMode=true; if(!this.started && this.questions && this.questions.length>0){ this.currentIndex=0 } },
    autoSubmitIfNeeded(){ if(this.exam && this.exam.autoSubmit===1 && this.started && !this.submitted){ this.$message.warning('时间到，自动交卷'); this.submitPaper() } },
    autoSaveTick(){ if(!this.started || !this.currentQuestion) return; const qid=this.currentQuestion.id; const current=this.workingAnswer; const saved=this.answerMap[qid]; if(current && current!==saved){ this.saveCurrent().catch(()=>{}) } },
    formatDuration(sec){ if(sec==null) return '--:--'; if(sec<0) return '00:00'; const h=Math.floor(sec/3600), m=Math.floor((sec%3600)/60), s=sec%60; return (h>0?String(h).padStart(2,'0')+':':'')+String(m).padStart(2,'0')+':'+String(s).padStart(2,'0') },
    goBackPortal(){
      // 先关闭弹窗，避免 Element UI 遮罩在路由切换后短暂残留
      this.summaryVisible = false
      // 确保任何 loading 状态关闭
      this.loading = false
      // 使用 replace 避免产生历史记录并提升跳转体验
      this.$nextTick(()=>{
        this.$router.replace({ name:'ExamPortal', path:'/proj_lwj_exam/exam_portal'}).catch(()=>{})
      })
    }
  }
}
</script>

<style scoped>
/* Mac Style for Exam Take */
.exam-take {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
}

/* Card Styling */
.exam-take >>> .el-card {
  border-radius: 18px;
  border: none;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  background-color: #ffffff;
  margin-bottom: 24px;
}

.exam-take >>> .el-card__header {
  border-bottom: 1px solid #f5f5f7;
  padding: 20px 24px;
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

/* Exam Header */
.exam-header {
  margin-bottom: 24px;
}

.exam-title {
  font-size: 24px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.exam-meta {
  margin-bottom: 16px;
  display: flex;
  gap: 16px;
  align-items: center;
  font-size: 14px;
  color: #86868b;
}

.exam-meta strong {
  color: #1d1d1f;
  font-weight: 600;
}

.exam-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

/* Button Styling */
.exam-take >>> .el-button {
  border-radius: 980px;
  font-weight: 500;
  border: none;
  padding: 9px 20px;
  transition: all 0.2s ease;
}

.exam-take >>> .el-button--primary {
  background-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}

.exam-take >>> .el-button--primary:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

.exam-take >>> .el-button--warning {
  background-color: #ff9500;
  box-shadow: 0 2px 8px rgba(255, 149, 0, 0.2);
}

.exam-take >>> .el-button--info {
  background-color: #8e8e93;
}

/* Question List */
.question-list-pane {
  padding-right: 12px;
}

.question-list {
  width: 100%;
}

.questions-ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.q-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-radius: 10px;
  transition: background 0.2s;
  margin-bottom: 4px;
}

.q-item:hover {
  background: #f5f5f7;
}

.q-item.active {
  background: #e6f7ff;
  color: #0071e3;
}

.idx {
  width: 32px;
  text-align: center;
  color: #86868b;
  font-weight: 500;
}

.q-item.active .idx {
  color: #0071e3;
}

.type {
  width: 80px;
  text-align: center;
  font-size: 13px;
  color: #86868b;
}

.score {
  width: 80px;
  text-align: center;
  font-size: 13px;
  color: #86868b;
}

.state {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #34c759;
  color: white;
  font-size: 12px;
  margin-left: auto;
}

/* Question Detail */
.question-detail-pane {
  padding-left: 12px;
}

.question-detail {
  width: 100%;
}

.q-content {
  margin: 20px 0;
  font-size: 16px;
  line-height: 1.6;
  color: #1d1d1f;
}

.single-choice, .judge-choice, .short-answer {
  margin-bottom: 24px;
}

.q-ops {
  margin-top: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-top: 1px solid #f5f5f7;
  padding-top: 20px;
}

.answer-feedback {
  margin-top: 16px;
  padding: 16px;
  background: #f5f5f7;
  border-radius: 10px;
}

/* Form Elements */
.exam-take >>> .el-radio {
  display: block;
  margin-bottom: 12px;
  margin-left: 0 !important;
}

.exam-take >>> .el-radio__inner {
  width: 18px;
  height: 18px;
}

.exam-take >>> .el-radio__label {
  font-size: 15px;
  padding-left: 12px;
}

.exam-take >>> .el-textarea__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  padding: 12px;
  font-size: 15px;
  transition: all 0.2s ease;
}

.exam-take >>> .el-textarea__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

/* Tags */
.exam-take >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

/* Dialog Styling */
.exam-take >>> .el-dialog {
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}

.exam-take >>> .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
}

.exam-take >>> .el-dialog__title {
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

.exam-take >>> .el-dialog__body {
  padding: 24px;
}

.exam-take >>> .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
}

.dialog-footer {
  text-align: right;
}

.not-started-tip {
  padding: 40px;
  text-align: center;
}

/* Scrollbar */
.exam-take >>> .el-scrollbar__wrap {
  overflow-x: hidden;
}
</style>
