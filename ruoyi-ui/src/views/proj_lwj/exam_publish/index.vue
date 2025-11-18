<template>
  <div class="app-container">
    <!-- 顶部配置表单 -->
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
      <!-- 将原来的保存草稿/发布考试改为“题目配置”入口 -->
      <el-form-item>
        <!-- 主入口：题目配置（保存草稿并跳转） -->
        <el-button type="primary" @click="goToQuestionsFromForm" :loading="btnLoading">题目配置</el-button>
        <el-tooltip effect="dark" content="先完成题目配置再发布考试" placement="right">
          <i class="el-icon-info" style="margin-left:8px;color:#909399"></i>
        </el-tooltip>
      </el-form-item>
    </el-form>

    <!-- 已创建考试列表 -->
    <el-card style="margin-top:24px">
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center">
        <span>已创建考试（当前课堂）</span>
        <el-input v-model="query.examName" placeholder="按名称过滤" size="small" style="width:240px" @input="loadList" />
      </div>
      <el-alert
        title="发布流程：1) 题目配置 → 2) 列表中点击“发布考试” → 3) 开始考试"
        type="info" show-icon style="margin-bottom:12px"/>
      <el-table :data="list" v-loading="listLoading" style="width:100%" :row-key="rowKey" :row-class-name="rowClassName">
        <el-table-column prop="examName" label="名称" />
        <el-table-column label="时间">
          <template slot-scope="scope">{{ fmt(scope.row.startTime) }} ~ {{ fmt(scope.row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="examDuration" label="时长" width="80" />
        <el-table-column prop="totalScore" label="总分" width="90" />
        <!-- 新增题目数量列 -->
        <el-table-column prop="questionCount" label="题目数" width="90">
          <template slot-scope="scope">
            <span :style="{color: scope.row.questionCount ? '#409EFF' : '#F56C6C'}">{{ scope.row.questionCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="statusType(scope.row)">{{ statusDisplay(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="560">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="openExamEdit(scope.row)">编辑配置</el-button>
            <el-button size="mini" @click="gotoQuestions(scope.row)">题目配置</el-button>
            <el-tooltip
              v-if="publishDisabledReason(scope.row)"
              :content="publishDisabledReason(scope.row)"
              placement="top"
              effect="dark"
            >
              <el-button
                size="mini"
                type="success"
                :disabled="!!publishDisabledReason(scope.row)"
                @click="publish(scope.row)"
              >发布考试</el-button>
            </el-tooltip>
            <el-button
              v-else
              size="mini"
              type="success"
              @click="publish(scope.row)"
            >发布考试</el-button>
            <el-button size="mini" type="warning" @click="start(scope.row)" :disabled="Number(scope.row.status) !== 1 || !canStart(scope.row)">开始考试</el-button>
            <el-button size="mini" type="info" @click="end(scope.row)" :disabled="Number(scope.row.status) !== 2">结束考试</el-button>
            <el-tooltip v-if="removeDisabled(scope.row)" :content="removeDisabled(scope.row)" placement="top">
              <el-button size="mini" type="danger" :disabled="!!removeDisabled(scope.row)" @click="remove(scope.row)">删除</el-button>
            </el-tooltip>
            <el-button v-else size="mini" type="danger" @click="remove(scope.row)">删除</el-button>
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

    <!-- 编辑考试配置弹窗 -->
    <el-dialog title="编辑考试配置" :visible.sync="examEditVisible" width="760px">
      <el-form :model="examEditForm" label-width="110px" size="small">
        <el-form-item label="考试名称">
          <el-input v-model="examEditForm.examName" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="考试类型">
          <el-select v-model="examEditForm.examType" style="width:200px">
            <el-option :value="1" label="期中" />
            <el-option :value="2" label="期末" />
            <el-option :value="3" label="测验" />
            <el-option :value="4" label="模拟考" />
            <el-option :value="5" label="课堂测验" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="examEditTimeRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="examEditForm.examDuration" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="总分/及格">
          <div style="display:flex;align-items:center;gap:6px">
            <el-input-number v-model="examEditForm.totalScore" :min="0" :max="10000" :step="1" :precision="2" />
            <span>/</span>
            <el-input-number v-model="examEditForm.passScore" :min="0" :max="10000" :step="1" :precision="2" />
          </div>
        </el-form-item>
        <el-form-item label="考试模式">
          <el-radio-group v-model="examEditForm.examMode">
            <el-radio :label="1">定时考试</el-radio>
            <el-radio :label="2">随到随考</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目顺序">
          <el-radio-group v-model="examEditForm.questionOrder">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">随机</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="答案显示">
          <el-select v-model="examEditForm.showAnswer" style="width:200px">
            <el-option :value="0" label="不显示" />
            <el-option :value="1" label="立即显示" />
            <el-option :value="2" label="结束后显示" />
          </el-select>
        </el-form-item>
        <el-form-item label="附加设置">
          <el-checkbox v-model="examEditBool.antiCheat">防作弊</el-checkbox>
          <el-checkbox v-model="examEditBool.autoSubmit">自动交卷</el-checkbox>
          <el-checkbox v-model="examEditBool.lateSubmit">允许迟交</el-checkbox>
          <el-input-number v-model="examEditForm.lateTime" :min="0" :max="1440" style="margin-left:8px" /> 分钟
        </el-form-item>
        <el-form-item label="题目数量">
          <el-tag :type="examEditForm.questionCount? 'success':'info'">{{ examEditForm.questionCount || 0 }}</el-tag>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="editStatusTagType(examEditForm.status)">{{ editStatusText(examEditForm.status) }}</el-tag>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="examEditVisible=false">取 消</el-button>
        <el-button type="primary" :loading="examEditLoading" @click="confirmExamEdit">确 定</el-button>
      </div>
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
      },
      _pendingRestoreSessionId: null,
      rowKey: 'id',
      examEditVisible: false,
      examEditLoading: false,
      examEditForm: { id:null, examName:'', examType:3, examDuration:60, totalScore:100, passScore:60, examMode:1, questionOrder:0, showAnswer:0, lateTime:0, courseId:null, sessionId:null, questionCount:0, status:0, startTime:null, endTime:null },
      examEditTimeRange: [],
      examEditBool: { antiCheat:false, autoSubmit:true, lateSubmit:false },
      highlightedExamId: null // 最近更新的考试行ID
    }
  },
  watch: {
    'form.courseId'(val) {
      if (val) {
        // 记住上次选择的课程
        try { localStorage.setItem('exam_publish_lastCourseId', String(val)) } catch (e) {}
        this.fetchSessionsByCourseId(val)
      } else {
        this.sessions = []
        this.form.sessionId = null
        try { localStorage.removeItem('exam_publish_lastCourseId') } catch (e) {}
      }
    },
    'form.sessionId'(val) {
      if (val) {
        try { localStorage.setItem('exam_publish_lastSessionId', String(val)) } catch (e) {}
        this.loadList()
      } else {
        this.list = []
        try { localStorage.removeItem('exam_publish_lastSessionId') } catch (e) {}
      }
    }
  },
  created() {
    // 加载课程后尝试恢复上次选择
    this.fetchCourses().then(() => {
      this.restoreSelections()
    })
  },
  methods: {
    fetchCourses() {
      return listCourse({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.courses = res && res.rows ? res.rows : (res.data || [])
      })
    },
    // 恢复上次的课程/课堂选择
    restoreSelections() {
      try {
        const lastCourseId = localStorage.getItem('exam_publish_lastCourseId')
        const lastSessionId = localStorage.getItem('exam_publish_lastSessionId')
        if (lastCourseId && this.courses && this.courses.some(c => String(c.courseId) === String(lastCourseId))) {
          // 设置 courseId 会触发 watcher，进而加载 sessions
          this.form.courseId = Number(lastCourseId)
          // 暂存期望恢复的 sessionId，待 sessions 加载后再设置
          this._pendingRestoreSessionId = lastSessionId ? Number(lastSessionId) : null
        }
      } catch (e) {
        // 忽略本地存储错误
      }
    },
    fetchSessionsByCourseId(courseId) {
      const api = require('@/api/proj_lw/session')
      return api.getSessionsByCourseId(courseId).then(res => {
        this.sessions = res && res.rows ? res.rows : (res.data || [])
        // 优先恢复上次 sessionId（需属于当前课程的课堂列表）
        const sid = this._pendingRestoreSessionId
        if (sid && this.sessions.some(s => String(s.sessionId) === String(sid))) {
          this.form.sessionId = Number(sid)
        } else {
          // 如果当前未选择则置空
          if (!this.form.sessionId || !this.sessions.some(s => String(s.sessionId) === String(this.form.sessionId))) {
            this.form.sessionId = null
          }
        }
        // 清除待恢复标记
        this._pendingRestoreSessionId = null
        // 不在这里调用 loadList，交给 sessionId 的 watcher 统一处理
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
    // 新增：根据当前时间动态计算状态文案
    statusDisplay(row) {
      const now = Date.now()
      const start = row.startTime ? new Date(row.startTime).getTime() : null
      const end = row.endTime ? new Date(row.endTime).getTime() : null
      const status = Number(row.status || 0)

      // 如果后台已经标记为已结束，则直接显示已结束
      if (status === 3) return '已结束'

      // 如果有结束时间且当前时间已超过结束时间，则视为已截止/已结束
      if (end && now > end) {
        return '已截止'
      }

      // 开始时间、结束时间未到之前
      if (start && now < start) {
        return '未开始'
      }

      // 在时间范围内并且状态为已发布或进行中
      if (start && (!end || now >= start && now <= end)) {
        if (status === 2) return '进行中'
        if (status === 1) return '已发布'
      }

      // 默认回退：根据原始 status 字段
      const map = { 0: '草稿', 1: '已发布', 2: '进行中', 3: '已结束' }
      return map[status] || '未知'
    },
    // 调整：statusType 现在接收 row，根据动态状态返回类型
    statusType(row) {
      const now = Date.now()
      const end = row.endTime ? new Date(row.endTime).getTime() : null
      const status = Number(row.status || 0)

      if (end && now > end && status !== 3) {
        return 'warning' // 已截止
      }
      if (status === 2) return 'success' // 进行中
      if (status === 1) return ''        // 已发布
      if (status === 0) return 'info'    // 草稿
      if (status === 3) return 'danger'  // 已结束
      return 'info'
    },
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
    // 新增: 简单日期时间格式化(编辑弹窗使用)
    formatDateTimeSimple(d) {
      if (!d) return null
      if (!(d instanceof Date)) d = new Date(d)
      if (isNaN(d.getTime())) return null
      const Y=d.getFullYear(),M=String(d.getMonth()+1).padStart(2,'0'),D=String(d.getDate()).padStart(2,'0'),h=String(d.getHours()).padStart(2,'0'),m=String(d.getMinutes()).padStart(2,'0'),s=String(d.getSeconds()).padStart(2,'0')
      return `${Y}-${M}-${D} ${h}:${m}:${s}`
    },
    validate() {
      if (!this.form.courseId) { this.$message.error('请选择课程'); return false }
      if (!this.form.sessionId) { this.$message.error('请选择课堂'); return false }
      if (!this.form.examName) { this.$message.error('请输入考试名称'); return false }
      if (!this.timeRange || this.timeRange.length !== 2) { this.$message.error('请选择考试时间段'); return false }
      return true
    },
    // 新增：从基本配置直接进入题目配置
    async goToQuestionsFromForm() {
      if (!this.validate()) return
      this.btnLoading = true
      try {
        let examId = this.form.id
        // 如果还没有保存过考试，先插入一条草稿记录
        if (!examId) {
          const payload = this.buildPayload(false)
          payload.status = 0 // 强制草稿
          const res = await addExam(payload)
          if (!res || !(res.code === 200 || res.code === 0)) {
            this.$message.error((res && (res.msg || res.message)) || '创建考试失败')
            this.btnLoading = false
            return
          }
          // 尝试多种方式从响应中获取新建考试ID
          examId = (res.data && (res.data.id || res.data.examId)) || res.id || res.examId || payload.id

          // 如果仍然没拿到ID，则尝试通过重新加载列表、按名称匹配找到最新一条考试
          if (!examId) {
            try {
              const listRes = await listExam({
                courseId: this.form.courseId,
                sessionId: this.form.sessionId,
                examName: this.form.examName,
                pageNum: 1,
                pageSize: 20
              })
              const rows = listRes && (listRes.rows || listRes.data) ? (listRes.rows || listRes.data) : []
              const candidates = rows
                .filter(e => e.examName === this.form.examName)
                .sort((a, b) => new Date(b.startTime || 0) - new Date(a.startTime || 0))
              if (candidates.length > 0) {
                examId = candidates[0].id
              }
            } catch (e) {
              console.warn('fallback listExam to resolve examId failed', e)
            }
          }

          if (!examId) {
            this.$message.error('未获取到考试ID，无法进入题目配置')
            this.btnLoading = false
            return
          }
          this.form.id = examId
        }
        this.btnLoading = false
        const path = `/proj_lwj_exam/exam_questions/${examId}`
        this.$router.push({ path }).catch(() => {})
      } catch (e) {
        console.error('goToQuestionsFromForm error', e)
        this.btnLoading = false
        this.$message.error('进入题目配置失败，请重试')
      }
    },
    // 保留 onSave 以兼容后续可能在别处复用，但当前表单不再直接调用 onSave
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
    publishDisabledReason(row) {
      const status = Number(row.status || 0)
      if (status > 0) return '已发布/进行中/已结束'
      if (!row.questionCount || row.questionCount === 0) return '尚未配置题目'
      if (!row.startTime || !row.endTime) return '缺少时间范围'
      return ''
    },
    canStart(row) {
      // 已发布且在时间范围内可开始
      const now = Date.now()
      const start = row.startTime ? new Date(row.startTime).getTime() : 0
      const end = row.endTime ? new Date(row.endTime).getTime() : 0
      return now >= start && (!end || now <= end)
    },
    // 新增：删除禁用原因计算
    removeDisabled(row) {
      const status = Number(row.status || 0)
      if (status === 2) { // 进行中
        const end = row.endTime ? new Date(row.endTime).getTime() : 0
        if (!end || Date.now() < end) {
          return '进行中的考试尚未到截止时间，禁止删除'
        }
      }
      return ''
    },
    remove(row) {
      const reason = this.removeDisabled(row)
      if (reason) { this.$message.warning(reason); return }
      this.$confirm('确定删除该考试吗？此操作不可恢复','提示',{type:'warning'}).then(()=>{
        delExam(row.id).then(()=>{ this.$message.success('删除成功'); this.loadList() })
      }).catch(()=>{})
    },
    publish(row) {
      if (this.publishDisabledReason(row)) {
        this.$message.warning(this.publishDisabledReason(row))
        return
      }
      // 安全兜底：没有题目禁止发布
      if (!row.questionCount || row.questionCount === 0) {
        this.$message.warning('该考试还没有配置题目，请先点击“题目配置”添加题目后再发布')
        return
      }
      this.$confirm('确定发布该考试吗？','提示',{type:'warning'}).then(()=>{
        publishExam(row.id).then(() => {
          this.$message.success('已发布考试')
          this.loadList()
        })
      }).catch(()=>{})
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
    },
    openExamEdit(row) {
      // 深拷贝行数据到弹窗表单
      this.examEditForm = {
        id: row.id,
        examName: row.examName,
        examType: Number(row.examType)||3,
        examDuration: row.examDuration,
        totalScore: row.totalScore,
        passScore: row.passScore,
        examMode: Number(row.examMode)||1,
        questionOrder: Number(row.questionOrder)||0,
        showAnswer: Number(row.showAnswer)||0,
        lateTime: Number(row.lateTime)||0,
        courseId: row.courseId,
        sessionId: row.sessionId,
        questionCount: row.questionCount||0,
        status: Number(row.status)||0,
        startTime: row.startTime,
        endTime: row.endTime
      }
      this.examEditTimeRange = [row.startTime? new Date(row.startTime): null, row.endTime? new Date(row.endTime): null].filter(Boolean)
      this.examEditBool = {
        antiCheat: (row.antiCheat|0) === 1,
        autoSubmit: (row.autoSubmit|0) === 1,
        lateSubmit: (row.lateSubmit|0) === 1
      }
      this.examEditVisible = true
    },
    rowClassName({ row }) {
      if (this.highlightedExamId && Number(row.id) === Number(this.highlightedExamId)) {
        return 'row-updated'
      }
      return ''
    },
    validateExamEdit() {
      const f = this.examEditForm
      if (!f.examName || !String(f.examName).trim()) { this.$message.error('请输入考试名称'); return false }
      if (!this.examEditTimeRange || this.examEditTimeRange.length !== 2) { this.$message.error('请选择时间范围'); return false }
      const start = this.examEditTimeRange[0]
      const end = this.examEditTimeRange[1]
      const startMs = start instanceof Date ? start.getTime() : new Date(start).getTime()
      const endMs = end instanceof Date ? end.getTime() : new Date(end).getTime()
      const now = Date.now()
      if (isNaN(startMs) || isNaN(endMs)) { this.$message.error('时间格式不正确'); return false }
      if (startMs >= endMs) { this.$message.error('开始时间必须早于结束时间'); return false }
      const status = Number(f.status || 0)
      // 状态相关约束：进行中 / 已结束 时间不可与当前实际冲突
      if (status === 2) { // 进行中
        if (!(startMs <= now && now < endMs)) {
          this.$message.error('进行中考试的时间范围必须包含现在时间'); return false }
      } else if (status === 3) { // 已结束
        if (!(endMs <= now)) { this.$message.error('已结束考试的结束时间必须早于当前时间'); return false }
      }
      // 可选：防止草稿/已发布设置过期结束时间（提示但不阻止）
      if ((status === 0 || status === 1) && endMs < now) {
        this.$message.warning('注意：结束时间已在过去，发布后将立即视为已截止')
      }
      if (!f.totalScore || Number(f.totalScore) <= 0) { this.$message.error('总分需大于0'); return false }
      if (Number(f.passScore) > Number(f.totalScore)) { this.$message.error('及格分不能大于总分'); return false }
      return true
    },
    confirmExamEdit() {
      if (!this.validateExamEdit()) return
      this.examEditLoading = true
      const f = this.examEditForm
      const [start, end] = this.examEditTimeRange
      const payload = Object.assign({}, f, {
        startTime: this.formatDateTimeSimple(start),
        endTime: this.formatDateTimeSimple(end),
        antiCheat: this.examEditBool.antiCheat ? 1 : 0,
        autoSubmit: this.examEditBool.autoSubmit ? 1 : 0,
        lateSubmit: this.examEditBool.lateSubmit ? 1 : 0
      })
      updateExam(payload).then(res => {
        this.examEditLoading = false
        if (res && (res.code === 200 || res.code === 0)) {
          this.$message.success('已更新考试配置')
          this.examEditVisible = false
          this.highlightedExamId = payload.id
          this.loadList()
          // 5 秒后取消高亮
          setTimeout(()=>{ this.highlightedExamId = null }, 5000)
        } else {
          this.$message.error((res && (res.msg || res.message)) || '更新失败')
        }
      }).catch(err => { this.examEditLoading = false; console.error(err); this.$message.error('更新失败') })
    },
    editStatusText(s) {
      const map = {0:'草稿',1:'已发布',2:'进行中',3:'已结束'}; return map[Number(s)||0]||'未知'
    },
    editStatusTagType(s) {
      const n = Number(s)||0; if (n===0) return 'info'; if (n===1) return ''; if (n===2) return 'success'; if (n===3) return 'danger'; return 'info'
    }
  }
}
</script>

<style scoped>
/* 高亮更新行样式 */
.row-updated td { background: #fff7e6 !important; transition: background 0.6s; }
</style>
