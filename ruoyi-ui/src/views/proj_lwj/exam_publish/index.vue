<template>
  <div class="app-container">
    <el-form :model="form" label-width="100px">
      <el-form-item label="课程">
        <el-select v-model="form.courseId" placeholder="请选择课程" filterable>
          <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
        </el-select>
      </el-form-item>
      <el-form-item label="课堂">
        <el-select v-model="form.sessionId" placeholder="请选择课堂" @change="onSessionChange">
          <el-option v-for="s in sessions" :key="s.sessionId" :label="(s.className ? `${s.className} (ID:${s.sessionId})` : String(s.sessionId))" :value="s.sessionId" />
        </el-select>
      </el-form-item>
      <el-form-item label="考试名称">
        <el-input v-model="form.examName" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.examType">
          <el-option :value="1" label="期中"/>
          <el-option :value="2" label="期末"/>
          <el-option :value="3" label="测验"/>
          <el-option :value="4" label="模拟考"/>
          <el-option :value="5" label="课堂测验"/>
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="timeRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" style="width:100%" />
      </el-form-item>
      <el-form-item label="时长(分钟)">
        <el-input-number v-model="form.examDuration" :min="1" :max="1000" />
      </el-form-item>
      <el-form-item label="总分/及格">
        <div style="display:flex;gap:8px;align-items:center">
          <el-input-number v-model="form.totalScore" :precision="2" :step="1" :min="0" :max="1000" />
          <span>/</span>
          <el-input-number v-model="form.passScore" :precision="2" :step="1" :min="0" :max="1000" />
        </div>
      </el-form-item>
      <el-form-item label="考试模式">
        <el-radio-group v-model="form.examMode">
          <el-radio :label="1">定时考试</el-radio>
          <el-radio :label="2">随到随考</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="防作弊">
        <el-switch v-model="bool.antiCheat" />
      </el-form-item>
      <el-form-item label="题目顺序">
        <el-radio-group v-model="form.questionOrder">
          <el-radio :label="0">正常顺序</el-radio>
          <el-radio :label="1">随机排序</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="答案显示">
        <el-select v-model="form.showAnswer">
          <el-option :label="'不显示'" :value="0"/>
          <el-option :label="'立即显示'" :value="1"/>
          <el-option :label="'考试结束后'" :value="2"/>
        </el-select>
      </el-form-item>
      <el-form-item label="其他">
        <el-checkbox v-model="bool.autoSubmit">自动交卷</el-checkbox>
        <el-checkbox v-model="bool.lateSubmit">允许迟交</el-checkbox>
        <el-input-number v-model="form.lateTime" :min="0" :max="1440" label="迟交时间" style="margin-left:8px" /> 分钟
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSave(false)" :loading="btnLoading">保存草稿</el-button>
        <el-button type="success" @click="onSave(true)" :loading="btnLoading">发布考试</el-button>
      </el-form-item>
    </el-form>

    <el-card style="margin-top:24px">
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center">
        <span>已创建考试（当前课堂）</span>
        <el-input v-model="query.examName" placeholder="按名称过滤" size="small" style="width:240px" @input="loadList" />
      </div>
      <el-table :data="list" v-loading="listLoading" style="width:100%">
        <el-table-column prop="examName" label="名称" />
        <el-table-column label="时间">
          <template slot-scope="scope">{{ fmt(scope.row.startTime) }} ~ {{ fmt(scope.row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="examDuration" label="时长" width="80" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="520">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="edit(scope.row)">编辑</el-button>
            <el-button size="mini" @click="gotoQuestions(scope.row)">题目配置</el-button>
            <el-button size="mini" type="danger" @click="remove(scope.row)">删除</el-button>
            <el-button size="mini" type="success" @click="publish(scope.row)" :disabled="Number(scope.row.status) > 0">发布</el-button>
            <el-button size="mini" type="warning" @click="start(scope.row)" :disabled="Number(scope.row.status) !== 1">开始</el-button>
            <el-button size="mini" type="info" @click="end(scope.row)" :disabled="Number(scope.row.status) !== 2">结束</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 批量发布对话框 -->
    <el-dialog title="批量发布考试" :visible.sync="batchVisible" width="680px">
      <div>
        <el-alert title="在同一课程下选择多个课堂，一次性创建多个考试" type="info" show-icon style="margin-bottom:12px" />
        <el-form :model="batch" label-width="100px">
          <el-form-item label="课程">
            <el-select v-model="form.courseId" disabled>
              <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
            </el-select>
          </el-form-item>
          <el-form-item label="选择课堂">
            <el-select v-model="batch.sessionIds" multiple placeholder="请选择课堂" filterable style="width:100%">
              <el-option v-for="s in sessions" :key="s.sessionId" :label="(s.className ? `${s.className} (ID:${s.sessionId})` : String(s.sessionId))" :value="s.sessionId" />
            </el-select>
          </el-form-item>
          <el-form-item label="考试名称"><el-input v-model="batch.exam.examName" /></el-form-item>
          <el-form-item label="类型">
            <el-select v-model="batch.exam.examType">
              <el-option :value="1" label="期中"/>
              <el-option :value="2" label="期末"/>
              <el-option :value="3" label="测验"/>
              <el-option :value="4" label="模拟考"/>
              <el-option :value="5" label="课堂测验"/>
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker v-model="batch.timeRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" style="width:100%" />
          </el-form-item>
          <el-form-item label="时长(分钟)"><el-input-number v-model="batch.exam.examDuration" :min="1" :max="1000" /></el-form-item>
          <el-form-item label="总分/及格">
            <div style="display:flex;gap:8px;align-items:center">
              <el-input-number v-model="batch.exam.totalScore" :precision="2" :step="1" :min="0" :max="1000" />
              <span>/</span>
              <el-input-number v-model="batch.exam.passScore" :precision="2" :step="1" :min="0" :max="1000" />
            </div>
          </el-form-item>
          <el-form-item label="模式">
            <el-radio-group v-model="batch.exam.examMode">
              <el-radio :label="1">定时</el-radio>
              <el-radio :label="2">随到随考</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="发布状态">
            <el-radio-group v-model="batch.exam.status">
              <el-radio :label="0">草稿</el-radio>
              <el-radio :label="1">立即发布</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchVisible=false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="submitBatch">批量创建</el-button>
      </span>
    </el-dialog>

    <!-- 右上角入口按钮 -->
    <el-button type="primary" icon="el-icon-s-operation" style="position:fixed;right:24px;bottom:24px" @click="openBatch" :disabled="!form.courseId || !sessions.length">批量发布</el-button>
  </div>
</template>

<script>
import { listExam, addExam, updateExam, publishExam, delExam, startExam, endExam, batchAddExam } from '@/api/proj_lwj/exam'
import { listCourse } from '@/api/proj_lw/course'

export default {
  name: 'ExamPublish',
  data() {
    return {
      courses: [],
      sessions: [],
      form: {
        id: null,
        courseId: null,
        sessionId: null,
        examName: '',
        examType: 3,
        startTime: null,
        endTime: null,
        examDuration: 60,
        totalScore: 100,
        passScore: 60,
        examMode: 1,
        questionOrder: 0,
        showAnswer: 0,
        lateTime: 0
      },
      bool: { antiCheat: false, autoSubmit: true, lateSubmit: false },
      timeRange: [],
      btnLoading: false,
      list: [],
      listLoading: false,
      query: { examName: '' },
      batchVisible: false,
      batchLoading: false,
      batch: {
        sessionIds: [],
        timeRange: [],
        exam: {
          courseId: null,
          examName: '',
          examType: 3,
          examDuration: 60,
          totalScore: 100,
          passScore: 60,
          examMode: 1,
          status: 0
        }
      }
    }
  },
  watch: {
    'form.courseId'(val) {
      if (val) this.fetchSessionsByCourseId(val); else { this.sessions = []; this.form.sessionId = null; this.list = [] }
    },
    'form.sessionId'(val) {
      if (val) {
        this.loadList()
      } else {
        this.list = []
      }
    }
  },
  created() {
    this.fetchCourses()
  },
  methods: {
    fetchCourses() {
      return listCourse({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.courses = res && res.rows ? res.rows : (res.data || [])
      })
    },
    fetchSessionsByCourseId(courseId) {
      const api = require('@/api/proj_lw/session')
      return api.getSessionsByCourseId(courseId).then(res => {
        this.sessions = res && res.rows ? res.rows : (res.data || [])
        this.form.sessionId = null
        // 不立即加载列表，等选择具体课堂后自动加载
      })
    },
    onSessionChange(val) {
      this.form.sessionId = val
      // 选择课堂后，自动加载该课堂的考试列表
      this.loadList()
    },
    fmt(v) {
      if (!v) return '—'
      const d = new Date(v)
      if (isNaN(d.getTime())) return v
      const pad = n => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    },
    statusText(s) { return ['草稿','已发布','进行中','已结束'][Number(s)||0] || '未知' },
    statusType(s) { return {0:'info',1:'',2:'success',3:'warning'}[Number(s)||0] || 'info' },
    buildPayload(publishNow) {
      const payload = Object.assign({}, this.form)
      payload.antiCheat = this.bool.antiCheat ? 1 : 0
      payload.autoSubmit = this.bool.autoSubmit ? 1 : 0
      payload.lateSubmit = this.bool.lateSubmit ? 1 : 0
      if (Array.isArray(this.timeRange) && this.timeRange.length === 2) {
        payload.startTime = this.formatDateTime(this.timeRange[0])
        payload.endTime = this.formatDateTime(this.timeRange[1])
      }
      if (publishNow) payload.status = 1
      return payload
    },
    formatDateTime(value) {
      const d = value instanceof Date ? value : new Date(value)
      if (isNaN(d.getTime())) return null
      const Y = d.getFullYear(), M = String(d.getMonth()+1).padStart(2,'0'), D = String(d.getDate()).padStart(2,'0')
      const h = String(d.getHours()).padStart(2,'0'), m = String(d.getMinutes()).padStart(2,'0'), s = String(d.getSeconds()).padStart(2,'0')
      return `${Y}-${M}-${D} ${h}:${m}:${s}`
    },
    validate() {
      if (!this.form.courseId) { this.$message.error('请选择课程'); return false }
      if (!this.form.sessionId) { this.$message.error('请选择课堂'); return false }
      if (!this.form.examName) { this.$message.error('请输入考试名称'); return false }
      if (!this.timeRange || this.timeRange.length !== 2) { this.$message.error('请选择考试时间段'); return false }
      return true
    },
    onSave(publishNow) {
      if (!this.validate()) return
      const payload = this.buildPayload(publishNow)
      this.btnLoading = true
      const req = !payload.id ? addExam(payload) : updateExam(payload)
      req.then(res => {
        this.btnLoading = false
        if (res && (res.code === 200 || res.code === 0)) {
          if (publishNow && payload.id) {
            publishExam(payload.id).then(() => this.loadList())
          } else {
            this.loadList()
          }
          this.$message.success(publishNow ? '发布成功' : '保存成功')
          if (!payload.id) this.resetForm()
        } else {
          this.$message.error((res && (res.msg || res.message)) || '保存失败')
        }
      }).catch(err => { this.btnLoading = false; console.error(err); this.$message.error('操作失败') })
    },
    resetForm() {
      this.form = { id:null, courseId:this.form.courseId, sessionId:this.form.sessionId, examName:'', examType:3, examDuration:60, totalScore:100, passScore:60, examMode:1, questionOrder:0, showAnswer:0, lateTime:0 }
      this.bool = { antiCheat:false, autoSubmit:true, lateSubmit:false }
      this.timeRange = []
    },
    edit(row) {
      this.form = {
        id: row.id, courseId: row.courseId, sessionId: row.sessionId, examName: row.examName,
        examType: row.examType, startTime: row.startTime, endTime: row.endTime, examDuration: row.examDuration,
        totalScore: row.totalScore, passScore: row.passScore, examMode: row.examMode, questionOrder: row.questionOrder,
        showAnswer: row.showAnswer, lateTime: row.lateTime
      }
      this.bool = { antiCheat: (row.antiCheat|0) === 1, autoSubmit: (row.autoSubmit|0) === 1, lateSubmit: (row.lateSubmit|0) === 1 }
      this.timeRange = [row.startTime ? new Date(row.startTime) : null, row.endTime ? new Date(row.endTime) : null].filter(Boolean)
    },
    remove(row) {
      this.$confirm('确定删除该考试吗？','提示',{type:'warning'}).then(()=>{
        delExam(row.id).then(()=>{ this.$message.success('删除成功'); this.loadList() })
      }).catch(()=>{})
    },
    publish(row) {
      publishExam(row.id).then(()=>{ this.$message.success('已发布'); this.loadList() })
    },
    start(row) {
      startExam(row.id).then(()=>{ this.$message.success('已开始'); this.loadList() })
    },
    end(row) {
      endExam(row.id).then(()=>{ this.$message.success('已结束'); this.loadList() })
    },
    loadList() {
      if (!this.form.sessionId) { this.list = []; return }
      this.listLoading = true
      listExam({ sessionId: this.form.sessionId, examName: this.query.examName, pageNum:1, pageSize:1000 }).then(res=>{
        this.listLoading = false
        this.list = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
      }).catch(e=>{ this.listLoading = false; console.error(e) })
    },
    gotoQuestions(row){
      const id = row.id
      if (!id) { this.$message.error('未识别考试ID'); return }
      // 统一使用路径跳转，避免命名路由在某些情况下未携带 params 导致出现 :examId
      const path = `/proj_lwj_exam/exam_questions/${id}`
      if (this.$router && this.$router.push) {
        this.$router.push({ path }).catch(() => {})
      }
    },
    openBatch(){
      if (!this.form.courseId) { this.$message.error('请先选择课程'); return }
      if (!this.sessions || this.sessions.length === 0) { this.$message.error('该课程暂无课堂'); return }
      // 预填当前课程
      this.batch.exam = Object.assign({}, this.batch.exam, { courseId: this.form.courseId })
      // 清空上次选择
      this.batch.sessionIds = []
      this.batch.timeRange = []
      this.batchVisible = true
    },
    submitBatch(){
      if (!this.form.courseId) { this.$message.error('请先选择课程'); return }
      if (!this.batch.sessionIds || this.batch.sessionIds.length === 0) { this.$message.error('请选择至少一个课堂'); return }
      if (!this.batch.exam.examName) { this.$message.error('请输入考试名称'); return }
      if (!this.batch.timeRange || this.batch.timeRange.length !== 2) { this.$message.error('请选择考试时间段'); return }
      const [start, end] = this.batch.timeRange
      const payloadExam = Object.assign({}, this.batch.exam, {
        courseId: this.form.courseId,
        startTime: this.formatDateTime(start),
        endTime: this.formatDateTime(end)
      })
      this.batchLoading = true
      batchAddExam(payloadExam, this.batch.sessionIds).then(res => {
        this.batchLoading = false
        const ok = res && (res.code === 200 || res.code === 0 || res.success)
        if (ok) {
          this.$message.success((res.msg || '批量创建成功'))
          this.batchVisible = false
          this.loadList()
        } else {
          this.$message.error((res && res.msg) || '批量创建失败')
        }
      }).catch(err => { this.batchLoading = false; console.error(err); this.$message.error('批量创建失败') })
    }
  }
}
</script>
