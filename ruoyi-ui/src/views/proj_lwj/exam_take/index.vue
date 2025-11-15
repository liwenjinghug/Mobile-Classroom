<template>
  <div class="exam-take" v-loading="loading">
    <el-card shadow="never" class="exam-header">
      <div class="exam-title">{{ exam ? exam.examName : '考试' }} (ID: {{ examId }})</div>
      <div class="exam-meta">
        <span>状态: <el-tag size="mini" :type="statusTagType">{{ statusLabel }}</el-tag></span>
        <span v-if="timeRemaining >= 0">剩余时间: <strong>{{ formatDuration(timeRemaining) }}</strong></span>
        <span v-else>已超时</span>
        <span>当前得分: {{ (participant && participant.totalScore) || 0 }} / {{ (exam && exam.totalScore) || 0 }}</span>
      </div>
      <div class="exam-actions">
        <el-button size="mini" type="primary" :disabled="!canStart || started" @click="start">开始考试</el-button>
        <el-button size="mini" type="warning" :disabled="!started || submitted" @click="submitPaper">交卷</el-button>
        <el-button size="mini" type="info" @click="refreshAll">刷新</el-button>
      </div>
    </el-card>

    <el-row :gutter="12" class="exam-body">
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
            <el-radio-group v-model="workingAnswer">
              <el-radio v-for="opt in parsedOptions" :label="opt" :key="opt">{{ opt }}</el-radio>
            </el-radio-group>
          </div>
          <div v-else-if="currentQuestion.questionType===3" class="judge-choice">
            <el-radio-group v-model="workingAnswer">
              <el-radio label="true">正确</el-radio>
              <el-radio label="false">错误</el-radio>
            </el-radio-group>
          </div>
          <div v-else-if="currentQuestion.questionType===5" class="short-answer">
            <el-input type="textarea" :rows="6" v-model="workingAnswer" placeholder="请输入答案"></el-input>
          </div>
          <div v-else>
            <el-alert type="warning" title="题型暂未实现此处作答面板" show-icon></el-alert>
          </div>

          <div class="q-ops">
            <el-button size="mini" type="primary" :disabled="!started" @click="saveCurrent">保存答案</el-button>
            <el-button size="mini" @click="prev" :disabled="currentIndex<=0">上一题</el-button>
            <el-button size="mini" @click="next" :disabled="currentIndex>=questions.length-1">下一题</el-button>
          </div>
          <div v-if="showImmediateAnswer && answerResultMap[currentQuestion.id]" class="answer-feedback">
            <el-tag :type="answerResultMap[currentQuestion.id].correct?'success':'danger'">
              {{ answerResultMap[currentQuestion.id].correct ? '正确' : '错误' }}
            </el-tag>
            <span v-if="currentQuestion.analysis">解析：{{ currentQuestion.analysis }}</span>
          </div>
        </el-card>
        <el-empty v-else description="请选择题目"></el-empty>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getExam, listQuestions, startExam, saveAnswer, submitExam } from '@/api/proj_lwj/exam'

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
      answerMap: {}, // questionId -> answer string
      answerResultMap: {}, // correctness info
      timer: null,
      timeRemaining: -1,
      submitted: false,
      started: false,
      showImmediateAnswer: false,
      studentNo: ''
    }
  },
  computed: {
    currentQuestion() { return this.questions[this.currentIndex] },
    statusLabel() {
      const s = this.exam ? this.exam.status : undefined
      return s===0?'草稿':s===1?'已发布':s===2?'进行中':s===3?'已结束':'未知'
    },
    statusTagType() {
      const s = this.exam ? this.exam.status : undefined
      return s===2?'success':s===1?'info':s===3?'warning':'default'
    },
    canStart() {
      if (!this.exam) return false
      const now = Date.now()
      const start = new Date(this.exam.startTime).getTime()
      const end = new Date(this.exam.endTime).getTime()
      return this.exam.status >= 1 && now >= start && now <= end && !this.submitted
    },
    parsedOptions() {
      if (!this.currentQuestion || !this.currentQuestion.questionOptions) return []
      try { return JSON.parse(this.currentQuestion.questionOptions) } catch(e){ return [] }
    }
  },
  created() { this.studentNo = (this.$route.query && this.$route.query.studentNo) ? String(this.$route.query.studentNo) : ''; this.init() },
  watch: {
    '$route.query.autoStart': {
      handler(val) {
        if (String(val) === '1' && !this.started && this.canStart) {
          this.start()
        }
      }, immediate: true
    }
  },
  beforeDestroy() { if (this.timer) clearInterval(this.timer); if (this.autoSaveTimer) clearInterval(this.autoSaveTimer) },
  methods: {
    async init() {
      this.loading = true
      try {
        await this.loadExam()
        await this.loadQuestions()
        this.showImmediateAnswer = (this.exam && this.exam.showAnswer === 1)
        // 启动轻量自动保存
        if (this.autoSaveTimer) clearInterval(this.autoSaveTimer)
        this.autoSaveTimer = setInterval(this.autoSaveTick, 30000)
      } catch (e) { console.error(e) } finally { this.loading=false }
    },
    async loadExam() {
      const res = await getExam(this.examId)
      this.exam = res.data
      this.computeRemaining()
    },
    async loadQuestions() {
      const res = await listQuestions({ examId: this.examId })
      this.questions = res.data || []
    },
    async refreshAll() { await this.init(); if (this.started) this.computeRemaining() },
    computeRemaining() {
      if (!this.exam) return
      const end = new Date(this.exam.endTime).getTime()
      const now = Date.now()
      this.timeRemaining = Math.floor((end - now)/1000)
      if (this.timer) clearInterval(this.timer)
      this.timer = setInterval(()=>{
        this.timeRemaining -= 1
        if (this.timeRemaining < 0) { clearInterval(this.timer); this.autoSubmitIfNeeded() }
      }, 1000)
    },
    selectIndex(i) { this.currentIndex = i; this.workingAnswer = this.answerMap[this.currentQuestion.id] || '' },
    prev() { if (this.currentIndex>0) this.selectIndex(this.currentIndex-1) },
    next() { if (this.currentIndex < this.questions.length-1) this.selectIndex(this.currentIndex+1) },
    typeLabel(t) { return t===1?'单选':t===3?'判断':t===5?'简答':'其它' },
    async start() {
      if (!this.canStart) return this.$message.error('尚未到可开始时间或考试未发布')
      const res = await startExam({ examId: Number(this.examId), studentNo: this.studentNo })
      this.participant = res.data
      this.started = true
      this.$message.success('考试已开始')
    },
    async saveCurrent() {
      if (!this.started) return this.$message.error('请先开始考试')
      if (!this.currentQuestion) return
      const payload = {
        examId: Number(this.examId),
        questionId: this.currentQuestion.id,
        studentAnswer: this.workingAnswer,
        correctAnswer: this.currentQuestion.correctAnswer,
        score: this.currentQuestion.score
      }
      const res = await saveAnswer(payload)
      this.answerMap[this.currentQuestion.id] = this.workingAnswer
      if (this.showImmediateAnswer && res.data && res.data.isCorrect !== undefined) {
        this.answerResultMap[this.currentQuestion.id] = { correct: res.data.isCorrect === 1 }
      }
      this.$message.success('已保存')
    },
    async submitPaper() {
      if (!this.started) return this.$message.error('还未开始考试')
      const res = await submitExam({ examId: Number(this.examId), studentNo: this.studentNo })
      this.participant = res.data
      this.submitted = true
      this.started = false
      this.$message.success('已交卷')
    },
    autoSubmitIfNeeded() {
      if (this.exam && this.exam.autoSubmit === 1 && this.started && !this.submitted) {
        this.$message.warning('时间到，自动交卷')
        this.submitPaper()
      }
    },
    autoSaveTick() {
      if (!this.started || !this.currentQuestion) return
      const qid = this.currentQuestion.id
      const current = this.workingAnswer
      const saved = this.answerMap[qid]
      if (current && current !== saved) {
        this.saveCurrent().catch(()=>{})
      }
    },
    formatDuration(sec) {
      if (sec < 0) return '00:00'
      const h = Math.floor(sec/3600)
      const m = Math.floor((sec%3600)/60)
      const s = sec%60
      return (h>0?String(h).padStart(2,'0')+':':'') + String(m).padStart(2,'0') + ':' + String(s).padStart(2,'0')
    }
  }
}
</script>

<style scoped>
.exam-header { margin-bottom: 12px; }
.exam-title { font-size: 18px; font-weight: 600; margin-bottom: 6px; }
.exam-meta span { margin-right: 16px; font-size: 13px; }
.question-list-pane { }
.questions-ul { list-style:none; padding:0; margin:0; }
.q-item { cursor:pointer; padding:6px 8px; display:flex; align-items:center; border-bottom:1px solid #eee; }
.q-item.active { background:#f0f9ff; }
.q-item .idx { width:32px; font-weight:bold; }
.q-item .type { margin-right:6px; color:#888; font-size:12px; }
.q-item .score { margin-left:auto; color:#666; font-size:12px; }
.q-content { margin: 10px 0 16px; line-height:1.6; }
.q-ops { margin-top:16px; }
.answer-feedback { margin-top:12px; }
</style>
