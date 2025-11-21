<template>
  <div class="app-container">
    <el-alert :title="'题目配置（课程：' + (courseName || '—') + (courseId ? '，ID：' + courseId : '') + '）'" type="info" show-icon style="margin-bottom:12px" />
    <div style="margin-bottom:12px; display:flex; gap:8px; align-items:center; flex-wrap: wrap;">
      <el-select v-model="filter.type" placeholder="题型" clearable style="width:140px" @change="onFilterChange">
        <el-option :value="1" label="单选题"/>
        <el-option :value="3" label="判断题"/>
        <el-option :value="5" label="简答题"/>
      </el-select>
      <el-button @click="reload">查询</el-button>
      <el-button type="primary" @click="openEditor()">新增题目</el-button>
      <el-button type="danger" @click="removeSelected" :disabled="!selection.length">批量删除</el-button>
      <el-button @click="reload">刷新</el-button>
      <el-button type="success" @click="saveDraft" :disabled="!realExamId || savingMeta">保存草稿</el-button>
      <el-button type="warning" @click="doPublish" :disabled="!canPublish" :loading="publishing">发布考试</el-button>
      <span style="color:#909399" v-if="!canPublish">（需至少1题且考试ID有效才能发布）</span>
      <span style="color:#909399" v-if="filter.type">（已按题型筛选，已临时禁用拖拽排序）</span>
    </div>

    <el-table :data="list" row-key="id" style="width:100%" @selection-change="onSel" v-loading="loading" ref="table">
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
    <el-dialog :title="editMode ? '编辑题目' : '新增题目'" :visible.sync="dialogVisible" width="720px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程">
          <el-input :value="(courseName || '—') + (courseId ? '（ID：' + courseId + '）' : '')" disabled />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.questionType" style="width: 200px" @change="onTypeChange">
            <el-option :value="1" label="单选题"/>
            <el-option :value="3" label="判断题"/>
            <el-option :value="5" label="简答题"/>
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input type="textarea" :rows="3" v-model="form.questionContent" />
        </el-form-item>

        <!-- 单选题 选项编辑器 -->
        <div v-if="form.questionType === 1">
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
        <div v-if="form.questionType === 3">
          <el-form-item label="正确答案">
            <el-radio-group v-model="judgeAnswer">
              <el-radio :label="true">正确</el-radio>
              <el-radio :label="false">错误</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <!-- 简答题 参考答案（可选） -->
        <div v-if="form.questionType === 5">
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
  </div>
</template>

<script>
import { listExamQuestion, addExamQuestion, updateExamQuestion, delExamQuestion, delExamQuestionBatch, reorderExamQuestions } from '@/api/proj_lwj/examQuestion'
import { updateExam, publishExam, getExam } from '@/api/proj_lwj/exam'
import { getCourse } from '@/api/proj_lw/course'
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
      courseType: ''
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
      return this.realExamId > 0 && Array.isArray(this.list) && this.list.length > 0
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
  },
  methods: {
    async fetchCourseType(){
      try {
        const id = this.realExamId
        const ex = await getExam(id)
        const exData = ex && (ex.data || ex)
        const examObj = exData && (exData.data || exData)
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
    typeText(t){ return {1:'单选',3:'判断',5:'简答'}[Number(t)||0] || t },
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
      if (this.form.questionType === 1) { // 单选初始化
        if (!this.optionList || this.optionList.length < 2) this.optionList = [{ text: '' }, { text: '' }]
        this.singleCorrect = 0
      } else if (this.form.questionType === 3) {
        this.judgeAnswer = true
      } else if (this.form.questionType === 5) {
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
        if (this.form.questionType === 1) {
          try {
            const opts = Array.isArray(row.questionOptions) ? row.questionOptions : JSON.parse(row.questionOptions || '[]')
            this.optionList = (opts || []).map(x => ({ text: String(x) }))
          } catch(e) { this.optionList = [ { text: '' }, { text: '' } ] }
          // 选中正确答案
          const ans = String(row.correctAnswer || '')
          let found = this.optionList.findIndex(o => o.text === ans)
          this.singleCorrect = found >= 0 ? found : 0
        } else if (this.form.questionType === 3) {
          const v = String(row.correctAnswer||'').toLowerCase()
          this.judgeAnswer = (v === 'true' || v === '1')
        } else if (this.form.questionType === 5) {
          this.shortAnswer = row.correctAnswer || ''
        }
      } else {
        // 新建
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
      if (![1,3,5].includes(t)) { this.$message.error('题型仅支持：单选、判断、简答'); return false }
      if (t === 1) {
        const texts = (this.optionList || []).map(x => (x.text||'').trim()).filter(Boolean)
        if (texts.length < 2) { this.$message.error('单选题至少2个非空选项'); return false }
        if (this.singleCorrect < 0 || this.singleCorrect >= texts.length) { this.$message.error('请选择正确答案'); return false }
      }
      return true
    },
    buildPayload(){
      const t = Number(this.form.questionType)
      const payload = Object.assign({}, this.form)
      if (t === 1) {
        const texts = (this.optionList || []).map(x => (x.text||'').trim()).filter(Boolean)
        payload.questionOptions = JSON.stringify(texts)
        const idx = this.singleCorrect
        payload.correctAnswer = texts[idx] || ''
      } else if (t === 3) {
        payload.questionOptions = ''
        payload.correctAnswer = this.judgeAnswer ? 'true' : 'false'
      } else if (t === 5) {
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
    async doPublish() {
      if (!this.canPublish) { this.$message.error('至少添加1个题目后才能发布'); return }
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
        this.$message.success('考试已发布')
      } catch (e) {
        console.error('publish error', e)
        this.$message.error('发布失败')
      } finally { this.publishing = false }
    }
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

/* Dialog Styling */
.app-container >>> .el-dialog {
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}

.app-container >>> .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
}

.app-container >>> .el-dialog__title {
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

.app-container >>> .el-dialog__body {
  padding: 24px;
}

.app-container >>> .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
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
