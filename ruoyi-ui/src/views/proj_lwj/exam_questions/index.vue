<template>
  <div class="app-container">
    <!-- 考试信息和分数状态提示 -->
    <el-alert
      :title="'题目配置（课程：' + (courseName || '—') + (courseId ? '，ID：' + courseId : '') + '）'"
      type="info"
      show-icon
      style="margin-bottom:12px"
    >
      <div slot="default" style="line-height: 1.8;">
        <div><strong>考试设定总分：</strong>{{ (examInfo && examInfo.totalScore != null) ? examInfo.totalScore : '未设置' }}分</div>
        <div><strong>当前题目总分：</strong>{{ currentExamTotalScore }}分</div>
        <div v-if="examInfo && examInfo.totalScore != null && currentExamTotalScore !== examInfo.totalScore" style="color: #E6A23C; font-weight: bold;">
          ⚠️ 分数不匹配！发布前需调整题目分值，使总分等于{{ examInfo.totalScore }}分
        </div>
        <div v-else-if="examInfo && examInfo.totalScore != null && currentExamTotalScore === examInfo.totalScore" style="color: #67C23A; font-weight: bold;">
          ✓ 分数匹配，可以发布
        </div>
      </div>
    </el-alert>

    <div style="margin-bottom:12px; display:flex; gap:8px; align-items:center; flex-wrap: wrap;">
      <el-select v-model="filter.type" placeholder="题型" clearable style="width:140px" @change="onFilterChange">
        <el-option :value="1" label="单选题"/>
        <el-option :value="3" label="判断题"/>
        <el-option :value="5" label="简答题"/>
      </el-select>
      <el-button @click="reload">查询</el-button>
      <el-button type="primary" @click="openEditor()">新增题目</el-button>
      <el-button type="info" @click="openApiImport">从题库导入</el-button>
      <el-button type="danger" @click="removeSelected" :disabled="!selection.length">批量删除</el-button>
      <el-button @click="reload">刷新</el-button>
      <el-button type="success" @click="saveDraft" :disabled="!realExamId || savingMeta">保存草稿</el-button>
      <el-button type="warning" @click="doPublish" :disabled="!canPublish" :loading="publishing">
        <i class="el-icon-upload2"></i> 发布考试
      </el-button>
      <el-button type="info" @click="goBackPublish">返回发布页面</el-button>
      <span style="color:#909399" v-if="!canPublish">（{{ publishReason }}）</span>
      <span style="color:#909399" v-if="filter.type">（已按题型筛选，已临时禁用拖拽排序）</span>
    </div>

    <el-table :data="list" row-key="id" style="width:100%" @selection-change="onSel" ref="table">
      <el-table-column type="selection" width="45" />
      <el-table-column label="拖拽" width="60">
        <template>
          <span class="drag-handle" title="拖拽排序" style="cursor: move;">≡</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="#" width="60" />
      <el-table-column prop="questionType" label="题型" width="90">
        <template slot-scope="scope">{{ typeText(scope.row.questionType) }}</template>
      </el-table-column>
      <el-table-column prop="questionContent" label="题目" />
      <el-table-column prop="score" label="分值" width="80" />
      <el-table-column label="操作" width="260">
        <template slot-scope="scope">
          <el-button size="mini" @click="openEditor(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="removeOne(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 题目编辑对话框（可视化表单） -->
    <el-dialog :title="editMode ? '编辑题目' : '新增题目'" :visible.sync="dialogVisible" width="720px" :modal="false" :lock-scroll="false" :append-to-body="true">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程">
          <el-input :value="(courseName || '—') + (courseId ? '（ID：' + courseId + '）' : '')" disabled />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.questionType" style="width: 200px" @change="onTypeChange">
            <el-option :value="1" label="判断题"/>
            <el-option :value="2" label="选择题"/>
            <el-option :value="3" label="简答题"/>
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input type="textarea" :rows="3" v-model="form.questionContent" />
        </el-form-item>

        <!-- 选择题 选项编辑器 -->
        <div v-if="form.questionType === 2">
          <el-form-item label="选项">
            <div v-for="(opt, idx) in optionList" :key="idx" style="display:flex;align-items:center;gap:8px;margin-bottom:6px">
              <el-radio v-model="singleCorrect" :label="idx">正确</el-radio>
              <el-input v-model="opt.text" placeholder="选项内容" />
              <el-button type="text" @click="removeOption(idx)">移除</el-button>
            </div>
            <el-button size="mini" @click="addOption">添加选项</el-button>
          </el-form-item>
        </div>

        <!-- 判断题 答案选择 -->
        <div v-if="form.questionType === 1">
          <el-form-item label="正确答案">
            <el-radio-group v-model="judgeAnswer">
              <el-radio :label="true">正确</el-radio>
              <el-radio :label="false">错误</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <!-- 简答题 参考答案（可选） -->
        <div v-if="form.questionType === 3">
          <el-form-item label="参考答案">
            <el-input type="textarea" :rows="3" v-model="shortAnswer" placeholder="可不填，供教师参考" />
          </el-form-item>
        </div>

        <el-form-item label="分值">
          <el-input-number v-model="form.score" :min="0.5" :step="0.5" :max="1000" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" style="width:200px">
            <el-option :value="1" label="简单"/>
            <el-option :value="2" label="一般"/>
            <el-option :value="3" label="困难"/>
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input type="textarea" :rows="3" v-model="form.analysis" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible=false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="saving">保 存</el-button>
      </div>
    </el-dialog>

    <!-- API题库导入对话框 -->
    <el-dialog title="从题库导入题目" :visible.sync="apiImportVisible" width="900px" :modal="false" :lock-scroll="false" :append-to-body="true">
      <div style="margin-bottom: 16px; display: flex; gap: 8px; align-items: center; flex-wrap: wrap;">
        <el-select v-model="apiFilter.type" placeholder="题型" clearable style="width: 120px" @change="onApiFilterChange">
          <el-option :value="2" label="选择题"/>
          <el-option :value="1" label="判断题"/>
          <el-option :value="3" label="简答题"/>
        </el-select>
        <el-select v-model="apiFilter.difficulty" placeholder="难度" clearable style="width: 120px" @change="onApiFilterChange">
          <el-option :value="1" label="简单"/>
          <el-option :value="2" label="一般"/>
          <el-option :value="3" label="困难"/>
        </el-select>
        <el-input v-model="apiFilter.keyword" placeholder="关键词搜索" style="width: 200px" clearable @input="onApiKeywordInput" />
        <!-- 移除搜索按钮，自动检索 -->
        <div style="margin-left: auto; color: #666;">
          已选择：{{ selectedAllCount }} 题，
          总分值：{{ selectedTotalScore }}分，
          当前考试总分：{{ currentExamTotalScore }}分
        </div>
      </div>

      <el-table :data="apiQuestions" ref="apiTable" row-key="id" style="width: 100%" @selection-change="onApiSelectionChange" max-height="400" :empty-text="apiLoading ? '加载中…' : '暂无题目'">
        <el-table-column type="selection" width="45" :reserve-selection="true" />
        <el-table-column prop="type" label="题型" width="90">
          <template slot-scope="scope">{{ typeText(scope.row.type) }}</template>
        </el-table-column>
        <el-table-column prop="question" label="题目内容" min-width="300" />
        <el-table-column prop="difficulty" label="难度" width="80">
          <template slot-scope="scope">{{ difficultyText(scope.row.difficulty) }}</template>
        </el-table-column>
        <el-table-column label="分值" width="120">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.score"
              :min="0.5"
              :step="0.5"
              :max="100"
              size="mini"
              style="width: 90px"
              @change="onRowScoreChange(scope.row)"
            />
          </template>
        </el-table-column>
      </el-table>

      <div slot="footer" class="dialog-footer">
        <el-button @click="apiImportVisible = false">取 消</el-button>
        <el-button type="primary" @click="importSelectedQuestions" :loading="importing" :disabled="selectedAllCount === 0">
          导入选中题目 ({{ selectedAllCount }})
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listExamQuestion, addExamQuestion, updateExamQuestion, delExamQuestion, delExamQuestionBatch, reorderExamQuestions } from '@/api/proj_lwj/examQuestion'
import { updateExam, publishExam, getExam } from '@/api/proj_lwj/exam'
import { getCourse } from '@/api/proj_lw/course'
import request from '@/utils/request'
import Sortable from 'sortablejs'

export default {
  name: 'ExamQuestions',
  props: { examId: { type: [Number, String], required: false } },
  data() {
    return {
      list: [],
      loading: false,
      filter: { type: null },
      selection: [],
      sortable: null,
      savingOrder: false,
      dialogVisible: false,
      editMode: false,
      saving: false,
      savingMeta: false,
      publishing: false,
      form: { id: null, examId: null, questionType: 1, questionContent: '', score: 1, difficulty: 1, sortOrder: 1, analysis: '' },
      optionList: [ { text: '' }, { text: '' } ],
      singleCorrect: 0,
      judgeAnswer: true,
      shortAnswer: '',
      // 课程信息（来自考试 -> 课程）
      courseId: null,
      courseName: '',
      courseType: '',
      // API题库导入相关
      apiImportVisible: false,
      apiLoading: false,
      importing: false,
      apiQuestions: [],
      selectedApiQuestions: [],
      apiFilter: {
        type: null,
        difficulty: null,
        keyword: ''
      },
      apiPrefetched: false,
      selectedIdMap: {},
      selectedScoreMap: {},
      keywordDebounceTimer: null,
      examInfo: null,
    }
  },
  computed: {
    realExamId() {
      // 优先从 props 获取
      if (this.examId && this.examId !== '') {
        const n = Number(this.examId)
        if (Number.isFinite(n)) return n
      }
      // 从路由参数获取
      if (this.$route && this.$route.params) {
        const { examId, id } = this.$route.params
        if (examId && examId !== '') {
          const n = Number(examId)
          if (Number.isFinite(n)) return n
        }
        if (id && id !== '') {
          const n = Number(id)
          if (Number.isFinite(n)) return n
        }
      }
      // 从查询参数获取
      if (this.$route && this.$route.query) {
        const { examId, id } = this.$route.query
        if (examId && examId !== '') {
          const n = Number(examId)
          if (Number.isFinite(n)) return n
        }
        if (id && id !== '') {
          const n = Number(id)
          if (Number.isFinite(n)) return n
        }
      }
      // 从路径中解析（兜底方案）
      if (this.$route) {
        const path = this.$route.path || this.$route.fullPath || ''
        // 匹配 /exam_questions/123 或 /proj_lwj_exam/exam_questions/123 等模式
        const match = path.match(/\/exam_questions\/(\d+)/)
        if (match && match[1]) {
          const n = Number(match[1])
          if (Number.isFinite(n)) return n
        }
      }
      return NaN
    },
    canPublish() {
      const hasExam = this.realExamId > 0
      const hasQuestions = Array.isArray(this.list) && this.list.length > 0
      const expected = Number(this.examInfo && this.examInfo.totalScore)
      const configured = Number(this.currentExamTotalScore)
      const totalsOk = Number.isFinite(expected) && expected > 0 && configured === expected
      return hasExam && hasQuestions && totalsOk
    },
    publishReason() {
      if (!(this.realExamId > 0)) return '考试ID无效'
      if (!Array.isArray(this.list) || this.list.length === 0) return '至少添加1道题目'
      const expected = Number(this.examInfo && this.examInfo.totalScore)
      const configured = Number(this.currentExamTotalScore)
      if (!Number.isFinite(expected) || expected <= 0) return '考试总分未设置或不合法'
      if (configured !== expected) return `当前配置总分(${configured})需等于考试总分(${expected})`
      return ''
    },
    selectedAllCount() {
      return Object.keys(this.selectedIdMap || {}).length
    },
    selectedTotalScore() {
      // 统计全局已选题目的总分（跨筛选）
      const m = this.selectedScoreMap || {}
      return Object.keys(m).reduce((sum, id) => sum + (Number(m[id]) || 0), 0)
    },
    currentExamTotalScore() {
      return (this.list || []).reduce((sum, q) => sum + (Number(q.score) || 0), 0)
    }
  },
  mounted() {
    // 兼容未通过 props 传入的情况，从路由参数兜底；最终依赖 realExamId
    if (!(this.realExamId > 0)) {
      this.$message.error('无效的考试ID');
      // 尝试回到发布页
      try { this.$router && this.$router.replace && this.$router.replace({ name: 'ExamPublish' }) } catch (e) {}
      return
    }
    this.fetchCourseType().then(()=> this.reload())
    this.prefetchApiQuestions()
  },
  methods: {
    async fetchCourseType(){
      try {
        const id = this.realExamId
        const ex = await getExam(id)
        const exData = ex && (ex.data || ex)
        const examObj = exData && (exData.data || exData)

        // 保存考试信息
        if (examObj) {
          this.examInfo = examObj
        }

        const courseId = examObj && (examObj.courseId || examObj.course_id)
        if (courseId) {
          const c = await getCourse(courseId)
          const cData = c && (c.data || c)
          const courseObj = cData && (cData.data || cData)
          // 存储课程ID、名称，并保留原 courseType 作为兜底
          this.courseId = courseId
          this.courseName = courseObj && (courseObj.courseName || courseObj.course_name) ? (courseObj.courseName || courseObj.course_name) : ''
          this.courseType = courseObj && (courseObj.courseType || courseObj.course_type) ? (courseObj.courseType || courseObj.course_type) : ''
        }
      } catch(e) { console.warn('fetch course info failed', e) }
    },
    typeText(t){ return {1:'判断',2:'选择',3:'简答'}[Number(t)||0] || t },
    difficultyText(d){ const map = {1:'简单', 2:'一般', 3:'困难'}; const n = Number(d); return map[n] || '未知' },
    reload(){
      this.loading = true
      const eid = this.realExamId
      const query = { examId: eid }
      if (this.filter.type) query.questionType = this.filter.type
      listExamQuestion(query).then(res => {
        this.loading = false
        this.list = (res && (res.rows || res.data)) ? (res.rows || res.data) : []
        this.$nextTick(() => this.initSortable())
      }).catch(()=>{ this.loading = false })
    },
    onFilterChange(){
      // 重新加载并重建拖拽（当筛选时禁用拖拽）
      this.reload()
    },
    initSortable(){
      // 当存在筛选条件时，不开启拖拽，避免局部重排引发全局顺序混乱
      const disabled = !!this.filter.type
      const tbody = this.$refs.table && this.$refs.table.$el && this.$refs.table.$el.querySelector('.el-table__body-wrapper tbody')
      if (!tbody) return
      if (this.sortable) { this.sortable.destroy(); this.sortable = null }
      if (disabled || !this.list || this.list.length === 0) return
      this.sortable = Sortable.create(tbody, {
        handle: '.drag-handle',
        animation: 150,
        onEnd: ({ oldIndex, newIndex }) => {
          if (oldIndex === newIndex) return
          const moved = this.list.splice(oldIndex, 1)[0]
          this.list.splice(newIndex, 0, moved)
          // 重排 sortOrder 并持久化
          const items = this.list.map((it, idx) => ({ id: it.id, sortOrder: idx + 1, updateBy: undefined }))
          // 同步更新本地 sortOrder 以便 UI 立即反映
          this.list.forEach((it, idx) => { it.sortOrder = idx + 1 })
          this.savingOrder = true
          reorderExamQuestions(items).then(() => {
            this.savingOrder = false
            this.$message.success('已保存排序')
          }).catch(err => {
            this.savingOrder = false
            console.error('reorder failed', err)
            this.$message.error('保存排序失败，已还原')
            // 失败则还原到按 sortOrder 排序
            this.list.sort((a,b) => (a.sortOrder||0) - (b.sortOrder||0))
          })
        }
      })
    },
    onTypeChange(){
      if (this.form.questionType === 2) { // 选择题初始化
        if (!this.optionList || this.optionList.length < 2) this.optionList = [{ text: '' }, { text: '' }]
        this.singleCorrect = 0
      } else if (this.form.questionType === 1) {
        this.judgeAnswer = true
      } else if (this.form.questionType === 3) {
        this.shortAnswer = ''
      }
    },
    openEditor(row){
      this.editMode = !!row
      this.dialogVisible = true
      if (row) {
        // 初始化表单
        this.form = {
          id: row.id, examId: row.examId, questionType: Number(row.questionType)||1, questionContent: row.questionContent||'',
          score: row.score || 1, difficulty: row.difficulty || 1, sortOrder: row.sortOrder || 1, analysis: row.analysis || ''
        }
        // 解析不同题型的数据
        if (this.form.questionType === 2) {
          // 选择题
          try {
            const opts = Array.isArray(row.questionOptions) ? row.questionOptions : JSON.parse(row.questionOptions || '[]')
            this.optionList = (opts || []).map(x => ({ text: String(x) }))
          } catch(e) { this.optionList = [ { text: '' }, { text: '' } ] }
          // 选中正确答案
          const ans = String(row.correctAnswer || '')
          let found = this.optionList.findIndex(o => o.text === ans)
          this.singleCorrect = found >= 0 ? found : 0
        } else if (this.form.questionType === 1) {
          // 判断题：支持多种格式转换为布尔值
          const v = String(row.correctAnswer||'').toLowerCase()
          this.judgeAnswer = (v === '正确' || v === 'true' || v === '1' || v === 't' || v === 'yes')
        } else if (this.form.questionType === 3) {
          this.shortAnswer = row.correctAnswer || ''
        }
      } else {
        // 新建：默认判断题
        this.form = { id:null, examId: Number(this.realExamId), questionType: 1, questionContent: '', score: 1, difficulty: 1, sortOrder: (this.list.length+1), analysis: '' }
        this.optionList = [ { text: '' }, { text: '' } ]
        this.singleCorrect = 0
        this.judgeAnswer = true
        this.shortAnswer = ''
      }
      this.onTypeChange()
    },
    validateForm(){
      if (!this.form.questionContent || !String(this.form.questionContent).trim()) { this.$message.error('题目内容必填'); return false }
      if (!this.form.score || Number(this.form.score) <= 0) { this.$message.error('分值需大于0'); return false }
      const t = Number(this.form.questionType)
      if (![1,2,3].includes(t)) { this.$message.error('题型仅支持：判断、选择、简答'); return false }
      // 选择题需要验证选项
      if (t === 2) {
        const texts = (this.optionList || []).map(x => (x.text||'').trim()).filter(Boolean)
        if (texts.length < 2) { this.$message.error('选择题至少2个非空选项'); return false }
        if (this.singleCorrect < 0 || this.singleCorrect >= texts.length) { this.$message.error('请选择正确答案'); return false }
      }
      // 判断题和简答题不需要额外验证
      return true
    },
    buildPayload(){
      const t = Number(this.form.questionType)
      const payload = Object.assign({}, this.form)
      if (t === 1) {
        // 判断题：标准化答案为"正确"或"错误"
        payload.questionOptions = JSON.stringify(['正确', '错误'])
        payload.correctAnswer = this.judgeAnswer ? '正确' : '错误'
      } else if (t === 2) {
        // 选择题
        const texts = (this.optionList || []).map(x => (x.text||'').trim()).filter(Boolean)
        payload.questionOptions = JSON.stringify(texts)
        const idx = this.singleCorrect
        payload.correctAnswer = texts[idx] || ''
      } else if (t === 3) {
        // 简答题
        payload.questionOptions = ''
        payload.correctAnswer = this.shortAnswer || ''
      }
      if (!payload.examId) payload.examId = Number(this.realExamId)
      // 将题目分类对齐为所属课程名称（作为 subject），若无则回退到 courseType
      payload.subject = (this.courseName && String(this.courseName).trim()) ? this.courseName : (this.courseType || '')
      return payload
    },
    submitForm(){
      if (!this.validateForm()) return
      const payload = this.buildPayload()
      this.saving = true
      const req = this.editMode ? updateExamQuestion : addExamQuestion
      req(payload).then(()=>{
        this.saving = false
        this.$message.success('已保存')
        this.dialogVisible = false
        this.reload()
      }).catch(err=>{ this.saving = false; console.error(err); this.$message.error('保存失败') })
    },
    addOption(){ this.optionList.push({ text: '' }) },
    removeOption(idx){ if (this.optionList.length <= 2) { this.$message.warning('至少保留2个选项'); return } this.optionList.splice(idx, 1); if (this.singleCorrect >= this.optionList.length) this.singleCorrect = this.optionList.length - 1 },
    onSel(rows){ this.selection = rows || [] },
    removeOne(row){ delExamQuestion(row.id).then(()=>{ this.$message.success('已删除'); this.reload() }) },
    removeSelected(){
      if (!this.selection.length) return
      const ids = this.selection.map(x=>x.id)
      delExamQuestionBatch(ids).then(()=>{ this.$message.success('已删除'); this.reload() })
    },
    async saveDraft() {
      if (!(this.realExamId > 0)) { this.$message.error('无效考试ID'); return }
      this.savingMeta = true
      try {
        // 获取考试详情，补充题目数量
        const ex = await getExam(this.realExamId)
        const examData = (ex && (ex.data || ex))
        const examObj = examData && (examData.data || examData)
        if (!examObj) throw new Error('未获取到考试详情')
        const payload = {
          id: examObj.id || examObj.examId,
          examName: examObj.examName,
          courseId: examObj.courseId,
          sessionId: examObj.sessionId,
          examType: examObj.examType,
          startTime: examObj.startTime,
          endTime: examObj.endTime,
          examDuration: examObj.examDuration,
          totalScore: examObj.totalScore,
          passScore: examObj.passScore,
          examMode: examObj.examMode,
          questionOrder: examObj.questionOrder,
          showAnswer: examObj.showAnswer,
          lateTime: examObj.lateTime,
          antiCheat: examObj.antiCheat,
          autoSubmit: examObj.autoSubmit,
          lateSubmit: examObj.lateSubmit,
          status: 0, // 强制草稿
          questionCount: this.list.length
        }
        await updateExam(payload)
        this.$message.success('草稿已保存')
      } catch (e) {
        console.error('saveDraft error', e)
        this.$message.error('保存草稿失败')
      } finally { this.savingMeta = false }
    },
    // 题库相关方法
    async openTriviaDialog() {
      this.triviaDialogVisible = true
      if (this.triviaCategories.length === 0) {
        try {
          const res = await getTriviaCategories()
          this.triviaCategories = res.data || []
        } catch (error) {
          this.$message.error('获取题库分类失败: ' + error.message)
        }
      }
    },

    async fetchTriviaQuestions() {
      if (this.triviaParams.amount < 1 || this.triviaParams.amount > 50) {
        this.$message.error('题目数量必须在1-50之间')
        return
      }

      this.loadingTrivia = true
      try {
        const res = await getTriviaQuestions(this.triviaParams)
        this.triviaQuestions = res.data || []
        this.selectedTrivia = []
        if (this.triviaQuestions.length === 0) {
          this.$message.warning('未获取到题目，请尝试调整筛选条件')
        } else {
          this.$message.success(`成功获取${this.triviaQuestions.length}道题目`)
        }
      } catch (error) {
        this.$message.error('获取题目失败: ' + error.message)
        this.triviaQuestions = []
      } finally {
        this.loadingTrivia = false
      }
    },

    onTriviaSelectionChange(selection) {
      this.selectedTrivia = selection
    },

    updateTriviaScore() {
      // 更新选中题目的总分值显示
      this.$forceUpdate()
    },

    async addSelectedTriviaQuestions() {
      if (this.selectedTrivia.length === 0) {
        this.$message.warning('请先选择要添加的题目')
        return
      }

      const duplicateCount = this.selectedTrivia.filter(question =>
        checkQuestionDuplicate(this.list, question)
      ).length

      if (duplicateCount > 0) {
        const confirm = await this.$confirm(
          `检测到${duplicateCount}道重复题目，是否仍要添加？`,
          '重复题目提醒',
          { type: 'warning' }
        ).catch(() => false)
        if (!confirm) return
      }

      // 添加题目到列表
      const newQuestions = []
      for (const question of this.selectedTrivia) {
        // 跳过重复题目
        if (checkQuestionDuplicate(this.list, question)) continue

        const newQuestion = {
          ...question,
          examId: this.realExamId,
          sortOrder: this.list.length + newQuestions.length + 1,
          id: null // 新题目ID为空
        }
        newQuestions.push(newQuestion)
      }

      if (newQuestions.length === 0) {
        this.$message.warning('所选题目均为重复题目，未添加')
        return
      }

      try {
        // 逐个保存题目
        for (const question of newQuestions) {
          await addExamQuestion(question)
        }

        this.$message.success(`成功添加${newQuestions.length}道题目`)
        this.triviaDialogVisible = false
        this.reload()
      } catch (error) {
        console.error('添加题目失败:', error)
        this.$message.error('添加题目失败: ' + (error.message || '未知错误'))
      }
    },

    getQuestionTypeText(type) {
      const types = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题' }
      return types[type] || '未知'
    },

    getDifficultyText(difficulty) {
      const difficulties = { 1: '简单', 2: '中等', 3: '困难' }
      return difficulties[difficulty] || '未知'
    },

    // API题库导入相关方法（切换为本地题库）
    openApiImport() {
      // 重新进入题库时清零已选
      this.selectedIdMap = {}
      this.selectedScoreMap = {}
      // 不清空数据，保持已预取题目，进入即可选择
      if (!this.apiPrefetched) {
        this.searchApiQuestions().then(()=>{ this.apiPrefetched = true })
      }
      this.apiImportVisible = true
      this.$nextTick(() => {
        try {
          const table = this.$refs.apiTable
          if (table && table.clearSelection) table.clearSelection()
        } catch(e) {}
        // 打开弹窗后移除可能残留的焦点，避免 aria-hidden 焦点阻塞警告
        try { const ae = document.activeElement; if (ae && typeof ae.blur === 'function') ae.blur() } catch (e) {}
      })
    },
    prefetchApiQuestions(){
      if (this.apiPrefetched) return
      this.searchApiQuestions().then(()=>{ this.apiPrefetched = true })
    },
    onApiFilterChange(){
      // 选择题型或难度后自动搜索
      this.autoSearchApi()
    },
    onApiKeywordInput(){
      // 关键词输入去抖自动搜索
      if (this.keywordDebounceTimer) clearTimeout(this.keywordDebounceTimer)
      this.keywordDebounceTimer = setTimeout(()=>{ this.autoSearchApi() }, 400)
    },
    autoSearchApi(){
      // 避免重复搜索阻塞；不显示遮罩，仅刷新数据
      if (this.apiLoading) return
      this.searchApiQuestions()
    },
    async searchApiQuestions() {
      this.apiLoading = true
      try {
        const params = {
          keyword: this.apiFilter.keyword || undefined,
          questionType: this.apiFilter.type ? this.examTypeToLocalType(this.apiFilter.type) : undefined,
          difficulty: this.apiFilter.difficulty || undefined,
          pageNum: 1,
          pageSize: 50
        }
        const response = await request({ url: '/proj_lwj/local-question/list', method: 'get', params })
        const raw = (response && (response.rows || response.data)) ? (response.rows || response.data) : []
        this.apiQuestions = raw.map(q => {
          let options = []
          try {
            if (q.optionsJson) {
              const parsed = typeof q.optionsJson === 'string' ? JSON.parse(q.optionsJson) : q.optionsJson
              if (Array.isArray(parsed)) options = parsed
            }
          } catch (e) {}
          return {
            id: q.id,
            type: this.localTypeToExamType(q.questionType),
            question: q.questionContent,
            options,
            correctAnswer: q.correctAnswer,
            difficulty: Number(q.difficulty) || 1,
            score: Number(q.score || 1)
          }
        })
        // 重新应用已选勾选状态，并同步分值
        this.$nextTick(() => {
          try {
            const table = this.$refs.apiTable
            if (table && table.clearSelection) {
              table.clearSelection()
              (this.apiQuestions || []).forEach(row => {
                if (this.selectedIdMap[row.id]) {
                  if (this.selectedScoreMap[row.id] != null) {
                    row.score = Number(this.selectedScoreMap[row.id]) || Number(row.score) || 1
                  } else {
                    this.$set(this.selectedScoreMap, row.id, Number(row.score) || 1)
                  }
                  table.toggleRowSelection(row, true)
                }
              })
            }
          } catch (e) {}
        })
        if (this.apiQuestions.length === 0) {
          this.$message.warning('未找到符合条件的题目')
        }
      } catch (error) {
        this.$message.error('搜索题目失败: ' + (error.message || '网络错误'))
        this.apiQuestions = []
      } finally {
        this.apiLoading = false
      }
    },

    onApiSelectionChange(selection) {
      // 仅更新当前可见数据对应的选择，在全局集合中保留其他筛选下已选的ID
      const currentIds = new Set((this.apiQuestions || []).map(q => q.id))
      // 清除当前可见中的记录
      currentIds.forEach(id => {
        if (this.selectedIdMap[id]) this.$delete(this.selectedIdMap, id)
        if (this.selectedScoreMap[id] != null) this.$delete(this.selectedScoreMap, id)
      })
      // 写入当前选择
      ;(selection || []).forEach(row => {
        if (row && row.id != null) {
          this.$set(this.selectedIdMap, row.id, true)
          this.$set(this.selectedScoreMap, row.id, Number(row.score) || 0)
        }
      })
    },

    onRowScoreChange(row) {
      // 若该题已被选中，同步更新其分值
      if (row && row.id != null && this.selectedIdMap[row.id]) {
        this.$set(this.selectedScoreMap, row.id, Number(row.score) || 0)
      }
    },

    async importSelectedQuestions() {
      const ids = Object.keys(this.selectedIdMap || {}).map(id => Number(id)).filter(Boolean)
      if (ids.length === 0) {
        this.$message.warning('请选择要导入的题目')
        return
      }
      this.importing = true
      try {
        const mapNow = {}
        ;(this.apiQuestions || []).forEach(q => { mapNow[q.id] = q })
        const needFetch = ids.filter(id => !mapNow[id])
        const fetched = []
        for (const id of needFetch) {
          try {
            const res = await request({ url: `/proj_lwj/local-question/${id}`, method: 'get' })
            const q = (res && (res.data || res)) ? (res.data || res) : null
            if (q && q.id) {
              let options = []
              try {
                if (q.optionsJson) {
                  const parsed = typeof q.optionsJson === 'string' ? JSON.parse(q.optionsJson) : q.optionsJson
                  if (Array.isArray(parsed)) options = parsed
                }
              } catch(e){}
              fetched.push({
                id: q.id,
                type: this.localTypeToExamType(q.questionType),
                question: q.questionContent,
                options,
                correctAnswer: q.correctAnswer,
                difficulty: Number(q.difficulty) || 1,
                score: Number(q.score || 1)
              })
            }
          } catch (e) { /* 忽略单个失败 */ }
        }
        const allSelected = ids.map(id => mapNow[id] || fetched.find(x => x.id === id)).filter(Boolean)

        const duplicates = allSelected.filter(apiQ =>
          this.list.some(localQ => localQ.questionContent && apiQ.question && localQ.questionContent.trim() === apiQ.question.trim())
        )
        if (duplicates.length > 0) {
          try {
            await this.$confirm(`检测到 ${duplicates.length} 道重复题目，是否继续导入？`, '重复题目提醒', { type: 'warning' })
          } catch { this.importing = false; return }
        }

        const maxOrder = Math.max(...this.list.map(q => q.sortOrder || 0), 0)
        for (let i = 0; i < allSelected.length; i++) {
          const apiQ = allSelected[i]
          const id = apiQ.id
          const selScore = this.selectedScoreMap && this.selectedScoreMap[id] != null ? Number(this.selectedScoreMap[id]) : undefined
          const payload = {
            examId: this.realExamId,
            questionType: apiQ.type,
            questionContent: apiQ.question,
            questionOptions: Array.isArray(apiQ.options) ? JSON.stringify(apiQ.options) : (apiQ.options || ''),
            correctAnswer: apiQ.correctAnswer,
            score: selScore != null ? selScore : (apiQ.score || 1),
            difficulty: apiQ.difficulty || 1,
            sortOrder: maxOrder + i + 1,
            analysis: '',
            subject: this.courseName || this.courseType || ''
          }
          await addExamQuestion(payload)
        }
        this.$message.success(`成功导入 ${allSelected.length} 道题目`)
        this.apiImportVisible = false
        this.reload()
      } catch (error) {
        this.$message.error('导入失败: ' + (error.message || '未知错误'))
      } finally {
        this.importing = false
      }
    },

    async doPublish() {
      // 前置验证
      if (!this.canPublish) {
        this.$message.error(this.publishReason || '发布条件不满足')
        return
      }

      // 二次确认（特别是当分数刚好匹配时）
      const expected = Number(this.examInfo && this.examInfo.totalScore)
      const configured = Number(this.currentExamTotalScore)

      try {
        await this.$confirm(
          `确认发布考试？\n考试总分: ${expected}分\n题目总分: ${configured}分`,
          '发布确认',
          {
            confirmButtonText: '确定发布',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
      } catch {
        return // 用户取消
      }

      this.publishing = true
      try {
        // 先更新题目数量再发布
        const ex = await getExam(this.realExamId)
        const examData = (ex && (ex.data || ex))
        const examObj = examData && (examData.data || examData)
        if (!examObj) throw new Error('未获取到考试详情')
        const payload = {
          id: examObj.id || examObj.examId,
          examName: examObj.examName,
          courseId: examObj.courseId,
          sessionId: examObj.sessionId,
          examType: examObj.examType,
          startTime: examObj.startTime,
          endTime: examObj.endTime,
          examDuration: examObj.examDuration,
          totalScore: examObj.totalScore,
          passScore: examObj.passScore,
          examMode: examObj.examMode,
          questionOrder: examObj.questionOrder,
          showAnswer: examObj.showAnswer,
          lateTime: examObj.lateTime,
          antiCheat: examObj.antiCheat,
          autoSubmit: examObj.autoSubmit,
          lateSubmit: examObj.lateSubmit,
          status: 1, // 准备发布
          questionCount: this.list.length
        }
        await updateExam(payload)
        await publishExam(this.realExamId)
        this.$message.success('考试已发布成功！')
        // 发布成功后可以选择跳转回发布页
        setTimeout(() => {
          this.$confirm('考试已发布，是否返回发布页面？', '提示', {
            confirmButtonText: '返回',
            cancelButtonText: '留在此页',
            type: 'success'
          }).then(() => {
            this.goBackPublish()
          }).catch(() => {})
        }, 800)
      } catch (e) {
        console.error('publish error', e)
        // 提取详细错误信息
        let errorMsg = '发布失败'
        if (e && e.response && e.response.data) {
          const data = e.response.data
          errorMsg = data.msg || data.message || errorMsg
        } else if (e && e.message) {
          errorMsg = e.message
        }
        // 使用 MessageBox 显示更详细的错误信息
        this.$alert(errorMsg, '发布失败', {
          type: 'error',
          confirmButtonText: '知道了'
        })
      } finally {
        this.publishing = false
      }
    },
    goBackPublish(){
      this.$router.push({ path:'/proj_lwj_exam/exam_publish' }).catch(()=>{})
    },
    // 数据库已统一：1=判断 2=选择 3=简答
    // 考试题型和本地题库题型使用相同编码，无需转换
    examTypeToLocalType(examType) {
      return examType
    },
    // 将本地题库题型映射为考试题型（已统一，直接返回）
    localTypeToExamType(localType) {
      return localType
    },
  }
}
</script>

<style scoped>
/* Mac Style for Exam Questions */
.app-container {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
}

/* Card Styling */
.app-container >>> .el-card {
  border-radius: 18px;
  border: none;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  background-color: #ffffff;
  margin-bottom: 24px;
}

.app-container >>> .el-card__header {
  border-bottom: 1px solid #f5f5f7;
  padding: 20px 24px;
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

/* Form Styling */
.app-container >>> .el-form-item__label {
  font-weight: 500;
  color: #1d1d1f;
}

.app-container >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  height: 36px;
  transition: all 0.2s ease;
}

.app-container >>> .el-input__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

.app-container >>> .el-textarea__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  transition: all 0.2s ease;
}

.app-container >>> .el-textarea__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

/* Button Styling */
.app-container >>> .el-button {
  border-radius: 980px;
  font-weight: 500;
  border: none;
  padding: 9px 20px;
  transition: all 0.2s ease;
}

.app-container >>> .el-button--primary {
  background-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}

.app-container >>> .el-button--primary:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

.app-container >>> .el-button--success {
  background-color: #34c759;
  box-shadow: 0 2px 8px rgba(52, 199, 89, 0.2);
}

.app-container >>> .el-button--warning {
  background-color: #ff9500;
  box-shadow: 0 2px 8px rgba(255, 149, 0, 0.2);
}

.app-container >>> .el-button--danger {
  background-color: #ff3b30;
  box-shadow: 0 2px 8px rgba(255, 59, 48, 0.2);
}

.app-container >>> .el-button--text {
  color: #0071e3;
  background: none;
  padding: 0 5px;
  box-shadow: none;
}

.app-container >>> .el-button--text:hover {
  color: #0077ed;
  background: none;
  transform: none;
}

/* Table Styling */
.app-container >>> .el-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.app-container >>> .el-table th {
  background-color: #fbfbfd;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #f5f5f7;
  padding: 12px 0;
}

.app-container >>> .el-table td {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
}

/* Dialog Styling - 使用 fixed + transform 完美居中 */
.app-container >>> .el-dialog__wrapper {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  overflow: auto !important;
}

.app-container >>> .el-dialog {
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  margin: 0 !important;
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
  max-height: 90vh;
  max-width: 95vw;
  display: flex;
  flex-direction: column;
}


.app-container >>> .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
  flex-shrink: 0;
}

.app-container >>> .el-dialog__title {
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

.app-container >>> .el-dialog__body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.app-container >>> .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
  flex-shrink: 0;
}

/* Tags */
.app-container >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

/* Alert Styling */
.app-container >>> .el-alert {
  border-radius: 10px;
  margin-bottom: 16px;
}

/* Drag Handle */
.drag-handle {
  display: inline-block;
  width: 100%;
  height: 100%;
  text-align: center;
  line-height: 32px;
  cursor: move;
  color: #0071e3;
  font-size: 18px;
}
</style>
