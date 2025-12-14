<template>
  <div class="app-container">
    <!-- 作业列表模式 -->
    <div v-if="!isSubmissionsView">
      <div class="header-row">
        <div class="title">作业批改</div>
        <div class="controls">
          <el-select v-model="form.courseId" placeholder="选择课程" filterable @change="onCourseChange" style="min-width:220px">
            <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
          </el-select>
          <el-select v-model="form.sessionId" placeholder="选择课堂" filterable @change="loadHomeworks" style="min-width:220px">
            <el-option v-for="s in sessions" :key="s.sessionId" :label="s.className || s.sessionId" :value="s.sessionId" />
          </el-select>
        </div>
      </div>

      <div class="content">
        <el-card class="homework-card">
          <template #header>
            <div class="card-header">
              <span>作业列表</span>
              <el-button size="mini" @click="refreshHomeworks" :loading="loading">刷新</el-button>
            </div>
          </template>

          <el-table :data="homeworkList" v-loading="loading" stripe style="width: 100%">
            <el-table-column prop="title" label="作业标题" min-width="200" show-overflow-tooltip />
            <el-table-column label="截止时间" width="160">
              <template #default="{ row }">{{ formatTime(row.deadline) || '—' }}</template>
            </el-table-column>
            <el-table-column prop="totalScore" label="分值" width="80" align="center" />
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button size="mini" type="primary" @click="openSubmissionsView(row)">
                  查看提交
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="!loading && homeworkList.length === 0" class="empty-state">
            <el-empty description="暂无作业数据" :image-size="100" />
          </div>
        </el-card>
      </div>
    </div>

    <!-- 提交列表模式（全屏） -->
    <div v-else class="submissions-full-view">
      <div class="header-row" v-if="isSubmissionsView">
        <div class="title">
          {{ (selectedHomework && selectedHomework.title) || '作业' }} - 提交列表
        </div>
        <div class="controls">
          <el-button size="mini" type="primary" icon="el-icon-refresh" @click="refreshSubmissions(true)" :loading="submissionsLoading">刷新</el-button>
          <el-button size="mini" type="success" icon="el-icon-download" @click="exportSubmissionsCsv">导出</el-button>
          <el-button size="mini" type="info" icon="el-icon-printer" @click="printSubmissions">打印</el-button>
          <el-button size="mini" @click="backToList">返回列表</el-button>
        </div>
      </div>

      <!-- 统计信息 -->
      <div v-if="submissions && submissions.length" class="stats-section">
        <div class="section-header">
          <h3>作业提交与批改</h3>
          <div class="actions">
            <!-- 这里可以加额外操作按钮 -->
          </div>
        </div>

        <el-alert type="info" :title="statsSummary" :closable="false" />
      </div>

      <!-- 提交列表 -->
      <el-card class="submissions-card">
        <el-table :data="sortedSubmissions" v-loading="submissionsLoading" stripe style="width: 100%" @sort-change="handleSortChange">
          <el-table-column type="index" label="#" width="60" align="center" />
          <el-table-column label="学号" width="120" fixed="left" sortable="custom" prop="studentNo">
            <template #default="{ row }">{{ row.student_no || row.studentNo || '—' }}</template>
          </el-table-column>
          <el-table-column prop="studentName" label="姓名" width="100" fixed="left" sortable="custom" />
          <el-table-column label="提交状态" width="100" align="center" sortable="custom" prop="status">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row)" size="small">
                {{ getStatusText(row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="160" sortable="custom" prop="submitTime">
            <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
          </el-table-column>
          <el-table-column label="批改时间" width="160" sortable="custom" prop="correctedTime">
            <template #default="{ row }">{{ formatTime(row.corrected_time || row.correctedTime) || '—' }}</template>
          </el-table-column>
          <el-table-column label="成绩" width="100" align="center" sortable="custom" prop="score">
            <template #default="{ row }">
              <span
                v-if="row.score !== null && row.score !== undefined"
                :style="{
                  color: row.score >= 60 ? '#67c23a' : '#f56c6c',
                  fontWeight: 'bold',
                  fontSize: '16px'
                }"
              >
                {{ row.score }}
              </span>
              <span v-else style="color: #909399">—</span>
            </template>
          </el-table-column>
          <el-table-column label="评语" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.remark" style="color: #666">{{ row.remark }}</span>
              <span v-else style="color: #999">无评语</span>
            </template>
          </el-table-column>
          <el-table-column label="附件" width="200" fixed="right">
            <template #default="{ row }">
              <div v-if="parseAttachments(row.submissionFiles).length">
                <el-tag
                  v-for="(f, idx) in parseAttachments(row.submissionFiles)"
                  :key="idx + '-' + f"
                  class="file-chip"
                  type="info"
                  @click="downloadFile(f)"
                  style="cursor: pointer; margin-right: 6px; margin-bottom: 4px;"
                >
                  {{ getFileName(f) }}
                </el-tag>
              </div>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                size="mini"
                :type="rowIsGraded(row) ? 'success' : 'primary'"
                @click="startGrade(row)"
                :disabled="!canGrade(row)"
              >
                {{ gradeButtonLabel(row) }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!submissionsLoading && (!submissions || submissions.length === 0)" class="empty-state">
          <el-empty description="暂无提交记录" :image-size="120" />
        </div>
      </el-card>
    </div>

    <!-- 批改对话框 -->
    <el-dialog
      :title="`批改作业 - ${gradingRow.studentName || gradingRow.studentId}`"
      :visible.sync="gradeDialogVisible"
      width="500px"
      :close-on-click-modal="false"
      :modal="false"
      :lock-scroll="false"
      custom-class="centered-homework-dialog"
    >
      <div class="grade-dialog-content">
        <div class="student-info">
          <div><strong>学号：</strong> {{ gradingRow.student_no || gradingRow.studentNo || '—' }}</div>
          <div><strong>提交时间：</strong> {{ formatTime(gradingRow.submitTime) || '—' }}</div>
        </div>

        <el-form :model="gradeForm" label-width="80px" style="margin-top: 16px;">
          <el-form-item label="分数" required>
            <el-input-number
              v-model.number="gradeForm.score"
              :min="0"
              :max="selectedHomework ? selectedHomework.totalScore : 100"
              :precision="1"
              :step="0.5"
              controls-position="right"
              style="width: 100%"
            />
            <div class="form-tip">满分: {{ selectedHomework ? selectedHomework.totalScore : 100 }} 分</div>
          </el-form-item>
          <el-form-item label="评语">
            <el-input
              type="textarea"
              v-model="gradeForm.remark"
              :rows="4"
              placeholder="请输入评语（可选）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="gradeDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="gradeSubmitting" @click="submitGrade">
            {{ gradeSubmitting ? '提交中...' : '确定' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listCourse } from '@/api/proj_lw/course'
import { listHomework, getSubmissions, gradeSubmission } from '@/api/proj_lwj/homework'

export default {
  name: 'HomeworkGrading',
  data() {
    return {
      courses: [],
      sessions: [],
      form: { courseId: null, sessionId: null },
      homeworkList: [],
      loading: false,

      // 提交列表相关
      isSubmissionsView: false,
      selectedHomework: null,
      submissions: [],
      submissionsLoading: false,
      selectedRows: [],

      // 批改相关
      gradeDialogVisible: false,
      gradingRow: {},
      gradeForm: { score: null, remark: '' },
      gradeSubmitting: false,

      // 排序相关
      subSortField: 'graded',
      subSortOrder: 'desc'
    }
  },
  computed: {
    courseInfo() {
      const course = this.courses.find(c => c.courseId === this.form.courseId)
      const session = this.sessions.find(s => s.sessionId === this.form.sessionId)
      return {
        courseName: course ? course.courseName : '',
        sessionName: session ? session.className : ''
      }
    },
    sortedSubmissions() {
      const rows = Array.isArray(this.submissions) ? this.submissions.slice() : []
      const field = this.subSortField || 'graded'
      const order = (this.subSortOrder || 'desc').toLowerCase()
      const desc = order !== 'asc'

      // 统一用 status 来代表状态：0未提交、1已提交、2已批改、3已逾期
      const getSubmitStatus = r => {
        const sRaw = r.status
        if (sRaw !== undefined && sRaw !== null) {
          const s = String(sRaw)
          // 排序优先级：已批改(2) > 已提交(1) > 已逾期(3) > 未提交(0)
          if (s === '2') return 3
          if (s === '1') return 2
          if (s === '3') return 1
          return 0
        }
        // 没有 status 时的兜底逻辑
        if (this.rowIsGraded(r)) return 3
        if (r.submitTime || (r.submissionFiles && r.submissionFiles.length)) return 2
        return 0
      }

      const val = r => {
        if (field === 'graded') return getSubmitStatus(r)
        if (field === 'score') return r.score == null ? (desc ? -Infinity : Infinity) : Number(r.score)
        if (field === 'submitTime') return r.submitTime ? new Date(r.submitTime).getTime() : 0
        if (field === 'correctedTime') {
          const t = r.corrected_time || r.correctedTime
          return t ? new Date(t).getTime() : 0
        }
        if (field === 'studentNo') {
          const s = (r.student_no || r.studentNo || '').toString()
          const n = Number(s.replace(/[^0-9]/g, ''))
          return isNaN(n) ? s.toLowerCase() : n
        }
        if (field === 'studentName') return (r.studentName || r.student_name || '').toString().toLowerCase()
        return 0
      }

      return rows.sort((a, b) => {
        const av = val(a)
        const bv = val(b)
        if (av === bv) {
          const timeA = a.submitTime ? new Date(a.submitTime).getTime() : 0
          const timeB = b.submitTime ? new Date(b.submitTime).getTime() : 0
          return timeB - timeA
        }
        return desc ? (av > bv ? -1 : 1) : (av > bv ? 1 : -1)
      })
    },
    statsSummary() {
      const list = this.submissions || []
      const total = list.length

      // 统一用 status 来统计已提交人数（1、2、3 都认为是已提交，只是状态不同）
      const submitted = list.filter(r => {
        const sRaw = r.status
        if (sRaw !== undefined && sRaw !== null) {
          const s = String(sRaw)
          if (s === '1' || s === '2' || s === '3') return true
        }
        return !!(r.submitTime || (r.submissionFiles && String(r.submissionFiles).trim()))
      }).length

      const graded = list.filter(r => this.rowIsGraded(r)).length

      // 平均分只统计有分数的记录
      const scoredList = list.filter(r => r.score !== null && r.score !== undefined && r.score !== '')
      const avgScore =
        scoredList.length > 0
          ? (
            scoredList.reduce((sum, r) => sum + (Number(r.score) || 0), 0) / scoredList.length
          ).toFixed(1)
          : 0

      return `统计：共 ${total} 人，已提交 ${submitted} 人，已批改 ${graded} 人，平均分 ${avgScore}`
    },
    hasAttachments() {
      if (!this.submissions || !this.submissions.length) return false
      return this.submissions.some(
        submission => submission.submissionFiles && submission.submissionFiles.trim()
      )
    },
    hasAnyAttachment() {
      return (this.submissions || []).some(r => this.attachmentsOf(r).length > 0)
    }
  },
  created() {
    console.log('作业批改页面初始化')
    this.fetchCourses().then(() => {
      const lastCourseId = localStorage.getItem('homework_grading_last_courseId')
      const lastSessionId = localStorage.getItem('homework_grading_last_sessionId')

      if (lastCourseId && this.courses.some(c => c.courseId == lastCourseId)) {
        this.form.courseId = isNaN(Number(lastCourseId)) ? lastCourseId : Number(lastCourseId)
        this.onCourseChange().then(() => {
          if (lastSessionId && this.sessions.some(s => s.sessionId == lastSessionId)) {
            this.form.sessionId = isNaN(Number(lastSessionId)) ? lastSessionId : Number(lastSessionId)
            this.loadHomeworks()
          }
        })
      }
    })
  },
  methods: {
    // 处理表格排序变化
    handleSortChange({ column, prop, order }) {
      console.log('排序变化:', prop, order)
      if (!order) {
        // 取消排序时，恢复默认排序（按状态）
        this.subSortField = 'graded'
        this.subSortOrder = 'desc'
      } else {
        // 将 prop 映射到 sortedSubmissions 中使用的字段名
        const fieldMap = {
          'studentNo': 'studentNo',
          'studentName': 'studentName',
          'status': 'graded',
          'submitTime': 'submitTime',
          'correctedTime': 'correctedTime',
          'score': 'score'
        }
        this.subSortField = fieldMap[prop] || prop
        this.subSortOrder = order === 'ascending' ? 'asc' : 'desc'
      }
    },

    async fetchCourses() {
      try {
        console.log('开始获取课程列表')
        const res = await listCourse({ pageNum: 1, pageSize: 1000 })
        this.courses = res && (res.rows || res.data) ? (res.rows || res.data) : []
      } catch (error) {
        console.error('获取课程失败:', error)
        this.courses = []
        this.$message.error('课程加载失败')
      }
    },

    async onCourseChange() {
      console.log('课程变更:', this.form.courseId)
      if (!this.form.courseId) {
        this.sessions = []
        this.form.sessionId = null
        this.homeworkList = []
        return
      }

      localStorage.setItem('homework_grading_last_courseId', this.form.courseId)

      try {
        const api = require('@/api/proj_lw/session')
        const res = await api.getSessionsByCourseId(this.form.courseId)
        this.sessions = res && (res.rows || res.data) ? (res.rows || res.data) : []
        this.form.sessionId = null
        this.homeworkList = []
      } catch (error) {
        console.error('获取课堂失败:', error)
        this.sessions = []
        this.$message.error('课堂加载失败')
      }
    },

    async loadHomeworks() {
      console.log('加载作业，课堂ID:', this.form.sessionId)
      if (!this.form.sessionId) {
        this.homeworkList = []
        return
      }

      localStorage.setItem('homework_grading_last_sessionId', this.form.sessionId)

      this.loading = true
      try {
        const res = await listHomework({ sessionId: this.form.sessionId, pageNum: 1, pageSize: 1000 })
        this.homeworkList = res && (res.rows || res.data) ? (res.rows || res.data) : []

        if (this.homeworkList.length === 0) {
          this.$message.info('该课堂暂无作业')
        }
      } catch (error) {
        console.error('加载作业失败:', error)
        this.homeworkList = []
        this.$message.error('作业加载失败')
      } finally {
        this.loading = false
      }
    },

    refreshHomeworks() {
      if (this.form.sessionId) {
        this.loadHomeworks()
      }
    },

    // 打开提交列表视图（全屏）
    openSubmissionsView(row) {
      console.log('打开提交列表:', row)
      const id = row.homeworkId || row.id
      if (!id) {
        this.$message.error('无法识别作业ID')
        return
      }

      this.selectedHomework = {
        homeworkId: id,
        title: row.title || row.name || '',
        totalScore: row.totalScore
      }

      this.isSubmissionsView = true
      this.loadSubmissions(id)

      this.$message.success(`正在加载【${this.selectedHomework.title}】的提交列表`)
    },

    closeSubmissionsView() {
      this.isSubmissionsView = false
      this.selectedHomework = null
      this.submissions = []
      this.$message.info('已返回作业列表')
    },

    async loadSubmissions(homeworkId) {
      console.log('加载提交记录，作业ID:', homeworkId)
      if (!homeworkId) {
        this.submissions = []
        return
      }

      this.submissionsLoading = true
      try {
        const res = await getSubmissions(homeworkId)
        this.submissions = res && (res.data || res.rows) ? (res.data || res.rows) : []

        if (this.submissions.length === 0) {
          this.$message.info('该作业暂无提交记录')
        } else {
          this.$message.success(`加载了 ${this.submissions.length} 条提交记录`)
        }
      } catch (error) {
        console.error('加载提交记录失败:', error)
        this.submissions = []
        this.$message.error('提交记录加载失败')
      } finally {
        this.submissionsLoading = false
      }
    },

    async refreshSubmissions(force = false) {
      if (!this.selectedHomework) return
      this.submissionsLoading = true
      try {
        const res = await getSubmissions(this.selectedHomework.homeworkId || this.selectedHomework.id)
        const raw = res && (res.data || res.rows) ? (res.data || res.rows) : res || []
        this.submissions = Array.isArray(raw) ? raw : []
      } catch (e) {
        console.error(e)
        this.$message.error('加载提交失败')
      } finally {
        this.submissionsLoading = false
      }
    },

    startGrade(row) {
      console.log('开始批改:', row)
      this.gradingRow = { ...row }
      this.gradeForm.score = row.score || null
      this.gradeForm.remark = row.remark || ''
      this.gradeDialogVisible = true
    },

    async submitGrade() {
      if (!this.selectedHomework || !this.gradingRow || !this.gradingRow.studentHomeworkId) {
        const sid = this.gradingRow.student_homework_id || this.gradingRow.id
        if (!sid) {
          this.$message.error('无法识别提交记录ID')
          return
        }
        this.gradingRow.studentHomeworkId = sid
      }

      const score = this.gradeForm.score
      if (score === null || score === undefined || isNaN(Number(score))) {
        this.$message.warning('请填写有效的分数')
        return
      }

      const payload = {
        studentHomeworkId: this.gradingRow.studentHomeworkId,
        homeworkId: this.selectedHomework.homeworkId || this.selectedHomework.id,
        score: Number(score),
        remark: this.gradeForm.remark || ''
      }

      this.gradeSubmitting = true
      try {
        const res = await gradeSubmission(payload)
        const ok = res && (res.code === 0 || res.code === 200 || res.success === true)
        if (!ok) {
          throw new Error((res && res.msg) || '提交批改失败')
        }
        this.$message.success('批改成绩已提交')
        this.gradeDialogVisible = false
        await this.refreshSubmissions(true)
      } catch (error) {
        console.error('批改提交失败:', error)
        this.$message.error(error.message || '批改提交失败')
      } finally {
        this.gradeSubmitting = false
      }
    },

    // 统一“已批改”判断逻辑：status == 2 或 is_graded == 1 或 有成绩/批改时间
    rowIsGraded(row) {
      const sRaw = row.status
      if (sRaw !== undefined && sRaw !== null && String(sRaw) === '2') return true
      if (row.is_graded === 1 || row.isGraded === 1) return true
      if (row.corrected_time || row.correctedTime) return true
      if (row.grade !== null && row.grade !== undefined) return true
      if (row.score !== null && row.score !== undefined) return true
      return false
    },

    canGrade(row) {
      // 只要有提交记录就可以批改（包括已逾期的提交）
      return !!(row.submitTime || (row.submissionFiles && row.submissionFiles.length))
    },

    gradeButtonLabel(row) {
      return this.rowIsGraded(row) ? '修改' : '批改'
    },

    // 根据 status 返回标签类型
    getStatusType(row) {
      const sRaw = row.status
      const s = sRaw !== undefined && sRaw !== null ? String(sRaw) : null

      // 2 = 已批改
      if (s === '2' || this.rowIsGraded(row)) return 'success'
      // 3 = 已逾期
      if (s === '3') return 'danger'
      // 1 = 已提交未批改
      if (s === '1') return 'warning'
      // 兜底：通过 submitTime 判断是否已提交
      if (row.submitTime || (row.submissionFiles && row.submissionFiles.length)) return 'warning'
      // 0 或未定义 = 未提交
      return 'info'
    },

    // 根据 status 返回显示文本
    getStatusText(row) {
      const sRaw = row.status
      const s = sRaw !== undefined && sRaw !== null ? String(sRaw) : null

      if (s === '2' || this.rowIsGraded(row)) return '已批改'
      if (s === '3') return '已逾期'
      if (s === '1') return '已提交'
      if (row.submitTime || (row.submissionFiles && row.submissionFiles.length)) return '已提交'
      return '未提交'
    },

    parseAttachments(files) {
      if (!files) return []
      return String(files)
        .split(',')
        .map(f => f.trim())
        .filter(Boolean)
    },

    getFileName(filePath) {
      return filePath.split('/').pop() || filePath
    },

    previewFile(path) {
      if (!path) return

      const request = require('@/utils/request').default
      const getToken = require('@/utils/auth').getToken
      const token = getToken()
      const headers = { Authorization: 'Bearer ' + token, isToken: true }
      const norm = String(path).replace(/\\/g, '/').trim()
      const filename = decodeURIComponent(norm.split('/').pop())

      console.log('=== 批改页面文件下载调试 ===')
      console.log('原始路径:', path)
      console.log('标准化路径:', norm)
      console.log('文件名:', filename)
      console.log('token存在:', !!token)

      const downloadBlob = url => {
        console.log('尝试下载URL:', url)
        return request({
          url,
          method: 'get',
          responseType: 'blob',
          headers
        })
          .then(blob => {
            console.log('下载成功，创建下载链接')
            const data =
              blob && blob.data instanceof Blob
                ? blob.data
                : blob instanceof Blob
                  ? blob
                  : new Blob([blob])
            const href = URL.createObjectURL(data)
            const a = document.createElement('a')
            a.href = href
            a.download = filename
            a.click()
            URL.revokeObjectURL(href)
            this.$message.success('文件下载完成')
          })
          .catch(error => {
            console.error('下载失败:', error)
            throw error
          })
      }

      const tryProfile = () =>
        downloadBlob(`/common/download/resource?resource=${encodeURIComponent(norm)}`).catch(
          () => {
            console.log('profile资源下载失败，尝试fileName方式')
            const rel = norm.replace(/^\/?profile\/upload\//, '')
            if (!rel) throw new Error('missing path')
            return downloadBlob(`/common/download?fileName=${encodeURIComponent(rel)}&delete=false`)
          }
        )

      if (norm.startsWith('/profile')) {
        console.log('使用profile路径下载')
        tryProfile().catch(() => this.$message.error('文件下载失败'))
      } else if (/^https?:\/\//i.test(norm)) {
        console.log('外部链接，直接打开')
        window.open(norm, '_blank')
      } else {
        console.log('使用fileName方式下载')
        downloadBlob(`/common/download?fileName=${encodeURIComponent(norm)}&delete=false`).catch(
          () => {
            this.$message.error('文件下载失败')
          }
        )
      }
      console.log('=== 调试结束 ===')
    },

    // 预览图片
    previewImage(filePath) {
      const imageUrl = this.downloadUrl(filePath)
      const fileName = this.getFileName(filePath)
      const decodedFileName = decodeURIComponent(fileName)

      console.log('图片预览 - 原始文件路径:', filePath)
      console.log('图片预览 - 生成的URL:', imageUrl)
      console.log('图片预览 - 文件名:', fileName)
      console.log('图片预览 - 解码后文件名:', decodedFileName)

      const testImg = new Image()
      testImg.onload = () => {
        console.log('图片加载成功，可以显示预览')
        this.showImagePreview(imageUrl, decodedFileName)
      }
      testImg.onerror = error => {
        console.error('图片加载失败:', error)
        console.log('尝试备用下载方式')
        this.showImagePreviewWithFallback(imageUrl, filePath, decodedFileName)
      }
      testImg.src = imageUrl
    },

    showImagePreview(blobUrl, fileName, originalUrl) {
      this.$alert(
        `
        <div style="text-align: center; padding: 10px;">
          <div style="margin-bottom: 12px; font-size: 14px; color: #666; font-weight: 500;">
            ${this.escapeHtml(fileName)}
          </div>
          <div style="position: relative; display: inline-block; max-width: 100%;">
            <img
              src="${blobUrl}"
              alt="${this.escapeHtml(fileName)}"
              style="max-width: 100%; max-height: 500px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.2); display: block;"
            />
          </div>
          <div style="margin-top: 12px; font-size: 12px; color: #999;">
            <a href="${originalUrl || blobUrl}" target="_blank" style="color: #409eff; text-decoration: none;">在新窗口中打开</a>
          </div>
        </div>
      `,
        '图片预览',
        {
          dangerouslyUseHTMLString: true,
          showConfirmButton: true,
          confirmButtonText: '关闭',
          customClass: 'image-preview-dialog',
          callback: () => {
            if (blobUrl && blobUrl.startsWith('blob:')) {
              URL.revokeObjectURL(blobUrl)
              console.log('已清理blob URL资源:', blobUrl)
            }
          }
        }
      )
    },

    showImagePreviewWithFallback(imageUrl, filePath, fileName) {
      this.$alert(
        `
        <div style="text-align: center; padding: 20px;">
          <div style="margin-bottom: 16px;">
            <i class="el-icon-picture" style="font-size: 48px; color: #ddd;"></i>
          </div>
          <div style="margin-bottom: 12px; font-size: 14px; color: #666;">
            ${this.escapeHtml(fileName)}
          </div>
          <div style="color: #f56c6c; margin-bottom: 16px; font-size: 13px;">
            图片预览失败，可能是网络问题或文件格式不支持
          </div>
          <div style="display: flex; gap: 8px; justify-content: center; flex-wrap: wrap;">
            <a href="${imageUrl}" target="_blank" style="color: #409eff; text-decoration: none; padding: 8px 16px; border: 1px solid #409eff; border-radius: 4px; font-size: 12px;">
              在新窗口中打开
            </a>
            <button onclick="document.createElement('a').href='${imageUrl}'; document.querySelector('a').download='${this.escapeHtml(
          fileName
        )}'; document.querySelector('a').click();" style="color: #67c23a; background: none; border: 1px solid #67c23a; border-radius: 4px; padding: 8px 16px; cursor: pointer; font-size: 12px;">
              直接下载
            </button>
          </div>
          <div style="margin-top: 12px; font-size: 11px; color: #999;">
            原始路径: ${this.escapeHtml(filePath)}
          </div>
        </div>
      `,
        '图片预览',
        {
          dangerouslyUseHTMLString: true,
          showConfirmButton: true,
          confirmButtonText: '关闭',
          customClass: 'image-preview-dialog'
        }
      )
    },

    escapeHtml(text) {
      if (!text) return ''
      return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
    },

    downloadFile(file) {
      if (!file) return

      const request = require('@/utils/request').default
      const getToken = require('@/utils/auth').getToken
      const token = getToken()
      const headers = { Authorization: 'Bearer ' + token, isToken: true }
      const norm = String(file).replace(/\\/g, '/').trim()
      const filename = decodeURIComponent(norm.split('/').pop())

      console.log('downloadFile - 输入文件路径:', file)
      console.log('downloadFile - 标准化路径:', norm)
      console.log('downloadFile - 文件名:', filename)

      const downloadBlob = url => {
        console.log('downloadFile - 尝试下载URL:', url)
        return request({
          url,
          method: 'get',
          responseType: 'blob',
          headers
        }).then(blob => {
          console.log('downloadFile - 下载成功，创建下载链接')
          const data = blob && blob.data instanceof Blob ? blob.data : (blob instanceof Blob ? blob : new Blob([blob]))
          const href = URL.createObjectURL(data)
          const a = document.createElement('a')
          a.href = href
          a.download = filename
          document.body.appendChild(a)
          a.click()
          document.body.removeChild(a)
          URL.revokeObjectURL(href)
          this.$message.success('文件下载完成')
        }).catch(error => {
          console.error('downloadFile - 下载失败:', error)
          throw error
        })
      }

      const tryProfile = () => downloadBlob(`/common/download/resource?resource=${encodeURIComponent(norm)}`).catch(() => {
        console.log('downloadFile - profile资源下载失败，尝试fileName方式')
        const rel = norm.replace(/^\/?profile\/upload\//, '')
        if (!rel) throw new Error('missing path')
        return downloadBlob(`/common/download?fileName=${encodeURIComponent(rel)}&delete=false`)
      })

      if (norm.startsWith('/profile') || norm.startsWith('profile')) {
        console.log('downloadFile - 使用profile路径下载')
        tryProfile().catch(() => this.$message.error('文件下载失败'))
      } else if (/^https?:\/\//i.test(norm)) {
        console.log('downloadFile - 外部链接，直接打开')
        window.open(norm, '_blank')
      } else if (norm.startsWith('/upload/') || norm.startsWith('upload/')) {
        console.log('downloadFile - 使用upload路径下载，转换为profile路径')
        const profilePath = norm.startsWith('/') ? '/profile' + norm : '/profile/' + norm
        downloadBlob(`/common/download/resource?resource=${encodeURIComponent(profilePath)}`).catch(() => {
          this.$message.error('文件下载失败')
        })
      } else if (/^\d{4}\//.test(norm)) {
        console.log('downloadFile - 识别为年份格式路径，转换为/profile/upload/')
        const profilePath = '/profile/upload/' + norm
        downloadBlob(`/common/download/resource?resource=${encodeURIComponent(profilePath)}`).catch(() => {
          this.$message.error('文件下载失败')
        })
      } else {
        console.log('downloadFile - 使用fileName方式下载')
        downloadBlob(`/common/download?fileName=${encodeURIComponent(norm)}&delete=false`).catch(() => {
          this.$message.error('文件下载失败')
        })
      }
    },

    downloadUrl(fileName) {
      const base = process.env.VUE_APP_BASE_API || ''
      if (!fileName) return ''

      let f = String(fileName).trim()

      console.log('downloadUrl - 输入文件名:', f)

      if (f.startsWith('http://') || f.startsWith('https://')) {
        console.log('downloadUrl - 识别为完整URL')
        return f
      }

      let finalUrl = ''

      if (f.startsWith('/profile') || f.startsWith('profile')) {
        console.log('downloadUrl - 识别为profile路径')
        finalUrl = base + '/common/download/resource?resource=' + encodeURIComponent(f)
      } else if (f.startsWith('/upload/')) {
        console.log('downloadUrl - 识别为/upload路径，转换为/profile/upload')
        const profilePath = '/profile' + f
        finalUrl = base + '/common/download/resource?resource=' + encodeURIComponent(profilePath)
      } else if (f.startsWith('upload/')) {
        console.log('downloadUrl - 识别为upload路径，转换为/profile/upload')
        const profilePath = '/profile/' + f
        finalUrl = base + '/common/download/resource?resource=' + encodeURIComponent(profilePath)
      } else if (/^\d{4}\//.test(f)) {
        console.log('downloadUrl - 识别为年份格式路径，转换为/profile/upload/')
        const profilePath = '/profile/upload/' + f
        finalUrl = base + '/common/download/resource?resource=' + encodeURIComponent(profilePath)
      } else {
        console.log('downloadUrl - 使用默认fileName下载接口')
        const token = require('@/utils/auth').getToken()
        const baseQuery = base + '/common/download?fileName=' + encodeURIComponent(f)
        finalUrl = token ? baseQuery + '&token=' + token : baseQuery
      }

      console.log('downloadUrl - 最终生成的URL:', finalUrl)
      return finalUrl
    },

    exportSubmissions() {
      this.$message.info('导出功能开发中...')
    },

    async exportSubmissionsCsv() {
      const list = this.sortedSubmissions || []
      if (!list.length) {
        this.$message.warning('暂无可导出数据')
        return
      }
      const headers = ['学号', '姓名', '提交时间', '批改时间', '成绩', '评语', '文件']
      const lines = [headers.join(',')]
      list.forEach(r => {
        const no = r.student_no || r.studentNo || ''
        const name = r.studentName || r.student_name || ''
        const submit = this.formatTime(r.submitTime) || ''
        const corrected = this.formatTime(r.corrected_time || r.correctedTime) || ''
        const score = r.score == null ? '' : r.score
        const remark = r.remark || ''
        const files = this.parseFiles(r.submissionFiles || r.files || '')
        const row = [no, name, submit, corrected, score, remark, files.join(' | ')]
        lines.push(
          row
            .map(v => '"' + String(v).replace(/"/g, '""') + '"')
            .join(',')
        )
      })
      const blob = new Blob(['\ufeff' + lines.join('\n')], {
        type: 'text/csv;charset=utf-8;'
      })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `作业提交_${(this.selectedHomework && this.selectedHomework.title) || '作业'}_${this.tsStr()}.csv`
      a.click()
      URL.revokeObjectURL(a.href)
    },

    printSubmissions() {
      const list = this.sortedSubmissions || []
      const title = `${(this.selectedHomework && this.selectedHomework.title) || '作业'} - 提交列表（${
        this.courseInfo.sessionName || '课堂'
      }）`
      const cols = ['学号', '姓名', '提交时间', '批改时间', '成绩', '评语']
      const rowsHtml = list
        .map(r => {
          const no = r.student_no || r.studentNo || ''
          const name = r.studentName || r.student_name || ''
          const submit = this.formatTime(r.submitTime) || '—'
          const corrected = this.formatTime(r.corrected_time || r.correctedTime) || '—'
          const score = r.score == null ? '—' : r.score + '分'
          const remark = r.remark || ''
          return `<tr><td>${no}</td><td>${name}</td><td>${submit}</td><td>${corrected}</td><td>${score}</td><td>${remark}</td></tr>`
        })
        .join('')
      const html = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>${title}</title>
      <style>body{font-family:Segoe UI,Arial,Helvetica,sans-serif;padding:20px;color:#303133}h1{font-size:20px;margin:0 0 12px}.table{width:100%;border-collapse:collapse}.table th,.table td{border:1px solid #ddd;padding:6px 8px;font-size:12px}.table th{background:#f5f7fa;text-align:left}@media print{button{display:none}}</style>
      </head><body><h1>${title}</h1><table class="table"><thead><tr>${cols
        .map(c => `<th>${c}</th>`)
        .join('')}</tr></thead><tbody>${rowsHtml}</tbody></table><button onclick="window.print()" style="margin-top:12px">打印</button></body></html>`
      const win = window.open('', '_blank')
      if (win) {
        win.document.open()
        win.document.write(html)
        win.document.close()
        win.focus()
      }
    },

    tsStr() {
      const d = new Date()
      const p = n => String(n).padStart(2, '0')
      return `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}_${p(d.getHours())}${p(
        d.getMinutes()
      )}${p(d.getSeconds())}`
    },

    parseFiles(files) {
      if (!files) return []
      return String(files)
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
    },

    backToList() {
      this.isSubmissionsView = false
      this.selectedHomework = null
    },

    formatTime(val) {
      if (!val) return ''
      try {
        if (typeof val === 'string') {
          const s = val.trim()
          if (/^\d+$/.test(s)) {
            let n = Number(s)
            if (s.length === 10) n *= 1000
            const d = new Date(n)
            return isNaN(d.getTime())
              ? ''
              : `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
                d.getDate()
              ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(
                d.getMinutes()
              ).padStart(2, '0')}`
          }
          const d1 = new Date(s)
          if (!isNaN(d1.getTime())) {
            return `${d1.getFullYear()}-${String(d1.getMonth() + 1).padStart(2, '0')}-${String(
              d1.getDate()
            ).padStart(2, '0')} ${String(d1.getHours()).padStart(2, '0')}:${String(
              d1.getMinutes()
            ).padStart(2, '0')}`
          }
          const d2 = new Date(s.replace(/-/g, '/'))
          if (!isNaN(d2.getTime())) {
            return `${d2.getFullYear()}-${String(d2.getMonth() + 1).padStart(2, '0')}-${String(
              d2.getDate()
            ).padStart(2, '0')} ${String(d2.getHours()).padStart(2, '0')}:${String(
              d2.getMinutes()
            ).padStart(2, '0')}`
          }
          return s
        }
        if (val instanceof Date) {
          const d = val
          return isNaN(d.getTime())
            ? ''
            : `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
              d.getDate()
            ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(
              d.getMinutes()
            ).padStart(2, '0')}`
        }
        if (typeof val === 'number') {
          let n = val
          if (String(val).length === 10) n *= 1000
          const d = new Date(n)
          return isNaN(d.getTime())
            ? ''
            : `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
              d.getDate()
            ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(
              d.getMinutes()
            ).padStart(2, '0')}`
        }
      } catch (e) {}
      return ''
    },

    onSelectionChange(rows) {
      this.selectedRows = rows || []
    },

    attachmentsOf(row) {
      if (!row) return []
      const tryFields = []
      if (row.attachments) tryFields.push(row.attachments)
      if (row.attachmentList) tryFields.push(row.attachmentList)
      if (row.files) tryFields.push(row.files)
      if (row.fileList) tryFields.push(row.fileList)
      if (row.attachmentUrls) tryFields.push(row.attachmentUrls)
      if (row.attachmentUrl) tryFields.push([row.attachmentUrl])
      if (row.filePath) tryFields.push([row.filePath])
      if (row.originalPath) tryFields.push([row.originalPath])
      let arr = []
      for (const v of tryFields) {
        if (!v) continue
        if (Array.isArray(v)) arr = arr.concat(v)
        else arr.push(v)
      }
      const norm = []
      const seen = new Set()
      for (const it of arr) {
        let url = null
        let name = null
        if (typeof it === 'string') {
          url = it
        } else if (typeof it === 'object' && it) {
          url = it.url || it.path || it.filePath || it.originalPath || it.src || it.href || null
          name = it.name || it.fileName || it.title || null
        }
        if (!url) continue
        const key = url
        if (seen.has(key)) continue
        seen.add(key)
        norm.push({ url, name })
      }
      return norm
    },

    fileNameFromPath(p) {
      if (!p) return '附件'
      try {
        const decoded = decodeURIComponent(p)
        const parts = decoded.split('/')
        return parts[parts.length - 1] || decoded
      } catch (e) {
        return String(p)
      }
    },

    buildDownloadUrl(raw) {
      let resource = raw
      if (typeof raw === 'object' && raw) resource = raw.url || raw.path || raw.filePath || raw.originalPath
      if (!resource) return null
      if (/^https?:\/\//i.test(resource)) return resource
      const base = process.env.VUE_APP_BASE_API || ''
      const api =
        base.replace(/\/$/, '') +
        '/common/download/resource?resource=' +
        encodeURIComponent(resource)
      return api
    },

    downloadAttachment(row, file) {
      const url = this.buildDownloadUrl(file)
      if (!url) {
        this.$message.error('无法解析附件下载地址')
        return
      }
      window.open(url, '_blank')
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* 作业列表样式 */
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.title {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.controls {
  display: flex;
  gap: 12px;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
}

.homework-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

/* 提交列表全屏样式 */
.submissions-full-view {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
  margin: -20px;
  padding: 20px;
}

.submissions-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.submissions-header .left {
  display: flex;
  align-items: center;
}

.submissions-header .right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-info h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.sub-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.total-score {
  color: #606266;
  font-size: 14px;
}

.stats-section {
  margin-bottom: 16px;
}

.submissions-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

/* 批改对话框样式 */
.grade-dialog-content {
  padding: 8px 0;
}

.student-info {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.student-info div {
  margin-bottom: 4px;
  font-size: 14px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 附件下载样式 */
.attachment-download {
  transition: all 0.3s ease;
  border: 1px solid #67c23a;
  background-color: #f0f9ff;
  color: #67c23a;
  font-weight: 500;
}

.attachment-download:hover {
  background-color: #67c23a;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
}

.attachment-download i {
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-2px);
  }
  60% {
    transform: translateY(-1px);
  }
}

/* 图片预览对话框样式 */
.image-preview-dialog .el-message-box {
  max-width: 90vw;
  max-height: 90vh;
}

.image-preview-dialog .el-message-box__content {
  padding: 10px 20px;
}

.image-preview_dialog img {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.attach-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.attach-item {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.submissions-table .muted {
  color: #909399;
}
.file-chip {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>

<style>
/* 全局样式 - 确保作业批改弹窗居中在用户屏幕中间（无 scoped） */
.centered-homework-dialog .el-dialog__wrapper {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  overflow: auto !important;
}

.centered-homework-dialog .el-dialog {
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  margin: 0 !important;
  max-height: 90vh;
  max-width: 95vw;
}
</style>
