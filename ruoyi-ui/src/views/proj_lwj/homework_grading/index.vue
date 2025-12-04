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
      <div class="submissions-header">
        <div class="left">
          <h3>{{ selectedHomework ? selectedHomework.title : '作业' }} — 提交列表</h3>
          <el-tag size="mini" type="info" style="margin-left:8px">课堂：{{ courseInfo.sessionName || '—' }}</el-tag>
        </div>
        <div class="right">
          <el-select v-model="subSortField" size="mini" placeholder="排序字段" style="width:140px;margin-right:8px">
            <el-option label="成绩" value="score" />
            <el-option label="提交时间" value="submitTime" />
            <el-option label="批改时间" value="correctedTime" />
            <el-option label="已批改" value="graded" />
            <el-option label="学号" value="studentNo" />
            <el-option label="姓名" value="studentName" />
          </el-select>
          <el-select v-model="subSortOrder" size="mini" placeholder="顺序" style="width:120px;margin-right:8px">
            <el-option label="降序" value="desc" />
            <el-option label="升序" value="asc" />
          </el-select>
          <el-button size="mini" type="primary" icon="el-icon-refresh" @click="refreshSubmissions(true)" :loading="submissionsLoading">刷新</el-button>
          <el-button size="mini" type="success" icon="el-icon-download" @click="exportSubmissionsCsv">导出</el-button>
          <el-button size="mini" type="info" icon="el-icon-printer" @click="printSubmissions">打印</el-button>
          <el-button size="mini" @click="backToList">返回列表</el-button>
        </div>
      </div>

      <!-- 统计信息 -->
      <div v-if="submissions && submissions.length" class="stats-section">
        <el-alert type="info" :title="statsSummary" :closable="false" />
      </div>

      <!-- 提交列表 -->
      <el-card class="submissions-card">
        <el-table :data="sortedSubmissions" v-loading="submissionsLoading" stripe style="width: 100%">
          <el-table-column type="index" label="#" width="60" align="center" />
          <el-table-column label="学号" width="120" fixed="left">
            <template #default="{ row }">{{ row.student_no || row.studentNo || '—' }}</template>
          </el-table-column>
          <el-table-column prop="studentName" label="姓名" width="100" fixed="left" />
          <el-table-column label="提交状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row)" size="small">
                {{ getStatusText(row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="160">
            <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
          </el-table-column>
          <el-table-column label="批改时间" width="160">
            <template #default="{ row }">{{ formatTime(row.corrected_time || row.correctedTime) || '—' }}</template>
          </el-table-column>
          <el-table-column label="成绩" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.score !== null && row.score !== undefined"
                    :style="{ color: row.score >= 60 ? '#67c23a' : '#f56c6c', fontWeight: 'bold', fontSize: '16px' }">
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
          <el-table-column label="附件" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <div v-if="row.submissionFiles" style="display: flex; flex-wrap: wrap; gap: 4px;">
                <el-tag
                  v-for="(file, index) in parseAttachments(row.submissionFiles)"
                  :key="index"
                  size="mini"
                  type="success"
                  class="attachment-download"
                  style="cursor: pointer; margin: 2px;"
                  :title="`点击下载文件：${getFileName(file)}`"
                  @click="previewFile(file)">
                  <i class="el-icon-download" style="margin-right: 4px;"></i>
                  {{ getFileName(file) }}
                </el-tag>
              </div>
              <span v-else style="color: #999">无附件</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                size="mini"
                :type="rowIsGraded(row) ? 'success' : 'primary'"
                @click="startGrade(row)"
                :disabled="!canGrade(row)">
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
              style="width: 100%" />
            <div class="form-tip">满分: {{ selectedHomework ? selectedHomework.totalScore : 100 }} 分</div>
          </el-form-item>
          <el-form-item label="评语">
            <el-input
              type="textarea"
              v-model="gradeForm.remark"
              :rows="4"
              placeholder="请输入评语（可选）"
              maxlength="200"
              show-word-limit />
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

      // 批改相关
      gradeDialogVisible: false,
      gradingRow: {},
      gradeForm: { score: null, remark: '' },
      gradeSubmitting: false,

      // 批量下载相关
      batchDownloadLoading: false,

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

      // 判断提交状态：0=未提交, 1=已提交未批改, 2=已批改
      const getSubmitStatus = r => {
        if (r.corrected_time || r.correctedTime || r.score != null) return 2 // 已批改
        if (r.submitTime || (r.submissionFiles && r.submissionFiles.length)) return 1 // 已提交
        return 0 // 未提交
      }

      const val = r => {
        if(field === 'graded') return getSubmitStatus(r)
        if(field === 'score') return r.score == null ? (desc?-Infinity:Infinity) : Number(r.score)
        if(field === 'submitTime') return r.submitTime ? new Date(r.submitTime).getTime() : 0
        if(field === 'correctedTime') return (r.corrected_time || r.correctedTime) ? new Date(r.corrected_time || r.correctedTime).getTime() : 0
        if(field === 'studentNo') {
          const s = (r.student_no || r.studentNo || '').toString()
          const n = Number(s.replace(/[^0-9]/g,''))
          return isNaN(n) ? s.toLowerCase() : n
        }
        if(field === 'studentName') return (r.studentName || r.student_name || '').toString().toLowerCase()
        return 0
      }

      return rows.sort((a,b)=>{
        const av = val(a), bv = val(b)
        if(av===bv) {
          // 相同状态时，按提交时间倒序排序（最新的在前）
          const timeA = a.submitTime ? new Date(a.submitTime).getTime() : 0
          const timeB = b.submitTime ? new Date(b.submitTime).getTime() : 0
          return timeB - timeA
        }
        return desc ? (av>bv?-1:1) : (av>bv?1:-1)
      })
    },
    statsSummary() {
      const total = (this.submissions || []).length
      const submitted = (this.submissions || []).filter(r =>
        r.submitTime || (r.submissionFiles && r.submissionFiles.length)
      ).length
      const graded = (this.submissions || []).filter(r => this.rowIsGraded(r)).length
      const avgScore = total > 0 ?
        ((this.submissions || []).reduce((sum, r) => sum + (Number(r.score) || 0), 0) / total).toFixed(1) : 0

      return `统计：共 ${total} 人，已提交 ${submitted} 人，已批改 ${graded} 人，平均分 ${avgScore}`
    },
    hasAttachments() {
      // 检查是否有提交的附件可以下载
      if (!this.submissions || !this.submissions.length) return false
      return this.submissions.some(submission =>
        submission.submissionFiles && submission.submissionFiles.trim()
      )
    }
  },
  created() {
    console.log('作业批改页面初始化')
    this.fetchCourses().then(() => {
      // 尝试恢复上次选择的课程和课堂
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

      // 保存用户选择的课程
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

      // 保存用户选择的课堂
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

      // 切换到提交列表视图
      this.isSubmissionsView = true
      this.loadSubmissions(id)

      this.$message.success(`正在加载【${this.selectedHomework.title}】的提交列表`)
    },

    // 关闭提交列表视图
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

    async refreshSubmissions(force=false) {
      if(!this.selectedHomework) return
      this.submissionsLoading = true
      try {
        const res = await getSubmissions(this.selectedHomework.homeworkId || this.selectedHomework.id)
        const raw = res && (res.data || res.rows) ? (res.data || res.rows) : (res || [])
        this.submissions = Array.isArray(raw) ? raw : []
      } catch(e){ console.error(e); this.$message.error('加载提交失败') }
      finally { this.submissionsLoading=false }
    },

    startGrade(row) {
      console.log('开始批改:', row)
      this.gradingRow = { ...row }
      this.gradeForm.score = row.score || null
      this.gradeForm.remark = row.remark || ''
      this.gradeDialogVisible = true
    },

    async submitGrade() {
      // 提交批改成绩
      if (!this.selectedHomework || !this.gradingRow || !this.gradingRow.studentHomeworkId) {
        const sid = this.gradingRow.student_homework_id || this.gradingRow.id
        if (!sid) {
          this.$message.error('无法识别提交记录ID')
          return
        }
        // 兼容不同字段命名
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
        // 刷新提交列表以展示最新成绩
        await this.refreshSubmissions(true)
      } catch (error) {
        console.error('批改提交失败:', error)
        this.$message.error(error.message || '批改提交失败')
      } finally {
        this.gradeSubmitting = false
      }
    },

    rowIsGraded(row) {
      return (row.is_graded === 1) ||
        String(row.status) === '2' ||
        (row.grade !== null && row.grade !== undefined) ||
        (row.score !== null && row.score !== undefined)
    },

    canGrade(row) {
      return !!(row.submitTime || (row.submissionFiles && row.submissionFiles.length))
    },

    gradeButtonLabel(row) {
      return this.rowIsGraded(row) ? '修改' : '批改'
    },

    getStatusType(row) {
      if (this.rowIsGraded(row)) return 'success'
      if (row.submitTime) return 'warning'
      return 'info'
    },

    getStatusText(row) {
      if (this.rowIsGraded(row)) return '已批改'
      if (row.submitTime) return '已提交'
      return '未提交'
    },

    parseAttachments(files) {
      if (!files) return []
      return String(files).split(',').map(f => f.trim()).filter(Boolean)
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
        }).then(blob => {
          console.log('下载成功，创建下载链接')
          const data = blob && blob.data instanceof Blob ? blob.data : (blob instanceof Blob ? blob : new Blob([blob]))
          const href = URL.createObjectURL(data)
          const a = document.createElement('a')
          a.href = href
          a.download = filename
          a.click()
          URL.revokeObjectURL(href)
          this.$message.success('文件下载完成')
        }).catch(error => {
          console.error('下载失败:', error)
          throw error
        })
      }

      const tryProfile = () => downloadBlob(`/common/download/resource?resource=${encodeURIComponent(norm)}`).catch(() => {
        console.log('profile资源下载失败，尝试fileName方式')
        const rel = norm.replace(/^\/?profile\/upload\//, '')
        if (!rel) throw new Error('missing path')
        return downloadBlob(`/common/download?fileName=${encodeURIComponent(rel)}&delete=false`)
      })

      if (norm.startsWith('/profile')) {
        console.log('使用profile路径下载')
        tryProfile().catch(() => this.$message.error('文件下载失败'))
      } else if (/^https?:\/\//i.test(norm)) {
        console.log('外部链接，直接打开')
        window.open(norm, '_blank')
      } else {
        console.log('使用fileName方式下载')
        downloadBlob(`/common/download?fileName=${encodeURIComponent(norm)}&delete=false`).catch(() => {
          this.$message.error('文件下载失败')
        })
      }
      console.log('=== 调试结束 ===')
    },

    // 预览图片
    previewImage(filePath) {
      const imageUrl = this.downloadUrl(filePath)
      const fileName = this.getFileName(filePath)

      // 解码文件名以正确显示中文
      const decodedFileName = decodeURIComponent(fileName)

      // 调试信息
      console.log('图片预览 - 原始文件路径:', filePath)
      console.log('图片预览 - 生成的URL:', imageUrl)
      console.log('图片预览 - 文件名:', fileName)
      console.log('图片预览 - 解码后文件名:', decodedFileName)

      // 创建一个测试图片来检查URL是否有效
      const testImg = new Image()
      testImg.onload = () => {
        console.log('图片加载成功，可以显示预览')
        this.showImagePreview(imageUrl, decodedFileName)
      }
      testImg.onerror = (error) => {
        console.error('图片加载失败:', error)
        console.log('尝试备用下载方式')
        this.showImagePreviewWithFallback(imageUrl, filePath, decodedFileName)
      }
      testImg.src = imageUrl
    },

    // 显示图片预览（成功加载时）
    showImagePreview(blobUrl, fileName, originalUrl) {
      this.$alert(`
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
      `, '图片预览', {
        dangerouslyUseHTMLString: true,
        showConfirmButton: true,
        confirmButtonText: '关闭',
        customClass: 'image-preview-dialog',
        callback: () => {
          // 清理blob URL资源
          if (blobUrl && blobUrl.startsWith('blob:')) {
            URL.revokeObjectURL(blobUrl)
            console.log('已清理blob URL资源:', blobUrl)
          }
        }
      })
    },

    // 显示图片预览（加载失败时的备用方案）
    showImagePreviewWithFallback(imageUrl, filePath, fileName) {
      this.$alert(`
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
            <button onclick="document.createElement('a').href='${imageUrl}'; document.querySelector('a').download='${this.escapeHtml(fileName)}'; document.querySelector('a').click();" style="color: #67c23a; background: none; border: 1px solid #67c23a; border-radius: 4px; padding: 8px 16px; cursor: pointer; font-size: 12px;">
              直接下载
            </button>
          </div>
          <div style="margin-top: 12px; font-size: 11px; color: #999;">
            原始路径: ${this.escapeHtml(filePath)}
          </div>
        </div>
      `, '图片预览', {
        dangerouslyUseHTMLString: true,
        showConfirmButton: true,
        confirmButtonText: '关闭',
        customClass: 'image-preview-dialog'
      })
    },

    // HTML转义函数，防止XSS攻击
    escapeHtml(text) {
      if (!text) return ''
      return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
    },

    // 下载文件
    downloadFile(file) {
      const downloadLink = this.downloadUrl(file)
      const a = document.createElement('a')
      a.href = downloadLink
      a.download = this.getFileName(file)
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
    },

    // 生成下载URL
    downloadUrl(fileName) {
      const base = process.env.VUE_APP_BASE_API || ''
      if (!fileName) return ''

      let f = String(fileName).trim()

      // 调试信息
      console.log('downloadUrl - 输入文件名:', f)

      // 处理完整的HTTP URL
      if (f.startsWith('http://') || f.startsWith('https://')) {
        console.log('downloadUrl - 识别为完整URL')
        return f
      }

      let finalUrl = ''

      // 处理 /profile 开头的资源路径
      if (f.startsWith('/profile') || f.startsWith('profile')) {
        console.log('downloadUrl - 识别为profile路径')
        finalUrl = base + '/common/download/resource?resource=' + encodeURIComponent(f)
      }
      // 处理 /upload 开头的路径 - 转换为 /profile/upload
      else if (f.startsWith('/upload/')) {
        console.log('downloadUrl - 识别为/upload路径，转换为/profile/upload')
        const profilePath = '/profile' + f
        finalUrl = base + '/common/download/resource?resource=' + encodeURIComponent(profilePath)
      }
      // 处理 upload/ 开头的路径
      else if (f.startsWith('upload/')) {
        console.log('downloadUrl - 识别为upload路径，转换为/profile/upload')
        const profilePath = '/profile/' + f
        finalUrl = base + '/common/download/resource?resource=' + encodeURIComponent(profilePath)
      }
      // 处理年份格式的路径 (如 2024/12/03/file.txt)
      else if (/^\d{4}\//.test(f)) {
        console.log('downloadUrl - 识别为年份格式路径，转换为/profile/upload/')
        const profilePath = '/profile/upload/' + f
        finalUrl = base + '/common/download/resource?resource=' + encodeURIComponent(profilePath)
      }
      // 默认使用fileName下载接口
      else {
        console.log('downloadUrl - 使用默认fileName下载接口')
        const token = require('@/utils/auth').getToken()
        const baseQuery = base + '/common/download?fileName=' + encodeURIComponent(f)
        finalUrl = token ? (baseQuery + '&token=' + token) : baseQuery
      }

      console.log('downloadUrl - 最终生成的URL:', finalUrl)
      return finalUrl
    },

    exportSubmissions() {
      // 导出逻辑
      this.$message.info('导出功能开发中...')
    },

    async exportSubmissionsCsv() {
      const list = this.sortedSubmissions || []
      if(!list.length){ this.$message.warning('暂无可导出数据'); return }
      const headers = ['学号','姓名','提交时间','批改时间','成绩','评语','文件']
      const lines = [headers.join(',')]
      list.forEach(r=>{
        const no = r.student_no || r.studentNo || ''
        const name = r.studentName || r.student_name || ''
        const submit = this.formatTime(r.submitTime) || ''
        const corrected = this.formatTime(r.corrected_time || r.correctedTime) || ''
        const score = r.score==null?'':r.score
        const remark = r.remark || ''
        const files = this.parseFiles(r.submissionFiles || r.files || '')
        const row = [no,name,submit,corrected,score,remark,files.join(' | ')]
        lines.push(row.map(v=> '"'+String(v).replace(/"/g,'""')+'"').join(','))
      })
      const blob = new Blob(['\ufeff'+lines.join('\n')], { type:'text/csv;charset=utf-8;' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `作业提交_${(this.selectedHomework && this.selectedHomework.title)||'作业'}_${this.tsStr()}.csv`
      a.click(); URL.revokeObjectURL(a.href)
    },

    printSubmissions() {
      const list = this.sortedSubmissions || []
      const title = `${(this.selectedHomework && this.selectedHomework.title)||'作业'} - 提交列表（${this.courseInfo.sessionName||'课堂'}）`
      const cols = ['学号','姓名','提交时间','批改时间','成绩','评语']
      const rowsHtml = list.map(r=>{
        const no = r.student_no || r.studentNo || ''
        const name = r.studentName || r.student_name || ''
        const submit = this.formatTime(r.submitTime) || '—'
        const corrected = this.formatTime(r.corrected_time || r.correctedTime) || '—'
        const score = r.score==null?'—':(r.score+'分')
        const remark = r.remark || ''
        return `<tr><td>${no}</td><td>${name}</td><td>${submit}</td><td>${corrected}</td><td>${score}</td><td>${remark}</td></tr>`
      }).join('')
      const html = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>${title}</title>
      <style>body{font-family:Segoe UI,Arial,Helvetica,sans-serif;padding:20px;color:#303133}h1{font-size:20px;margin:0 0 12px}.table{width:100%;border-collapse:collapse}.table th,.table td{border:1px solid #ddd;padding:6px 8px;font-size:12px}.table th{background:#f5f7fa;text-align:left}@media print{button{display:none}}</style>
      </head><body><h1>${title}</h1><table class="table"><thead><tr>${cols.map(c=>`<th>${c}</th>`).join('')}</tr></thead><tbody>${rowsHtml}</tbody></table><button onclick="window.print()" style="margin-top:12px">打印</button></body></html>`
      const win = window.open('','_blank'); if(win){ win.document.open(); win.document.write(html); win.document.close(); win.focus(); }
    },

    tsStr() {
      const d=new Date();
      const p=n=>String(n).padStart(2,'0');
      return `${d.getFullYear()}${p(d.getMonth()+1)}${p(d.getDate())}_${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}`
    },

    parseFiles(files) {
      if(!files) return [];
      return String(files).split(',').map(s=>s.trim()).filter(Boolean)
    },

    backToList() {
      this.isSubmissionsView = false
      this.selectedHomework = null
    },

    formatTime(val) {
      if (!val) return ''
      try {
        // ISO string or common date string
        if (typeof val === 'string') {
          const s = val.trim()
          // pure digits: timestamp
          if (/^\d+$/.test(s)) {
            let n = Number(s)
            if (s.length === 10) n *= 1000
            const d = new Date(n)
            return isNaN(d.getTime()) ? '' : `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
          }
          const d1 = new Date(s)
          if (!isNaN(d1.getTime())) return `${d1.getFullYear()}-${String(d1.getMonth() + 1).padStart(2, '0')}-${String(d1.getDate()).padStart(2, '0')} ${String(d1.getHours()).padStart(2, '0')}:${String(d1.getMinutes()).padStart(2, '0')}`
          const d2 = new Date(s.replace(/-/g, '/'))
          if (!isNaN(d2.getTime())) return `${d2.getFullYear()}-${String(d2.getMonth() + 1).padStart(2, '0')}-${String(d2.getDate()).padStart(2, '0')} ${String(d2.getHours()).padStart(2, '0')}:${String(d2.getMinutes()).padStart(2, '0')}`
          return s
        }
        // Date object
        if (val instanceof Date) {
          const d = val
          return isNaN(d.getTime()) ? '' : `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
        }
        // number timestamp (ms or s)
        if (typeof val === 'number') {
          let n = val
          if (String(val).length === 10) n *= 1000
          const d = new Date(n)
          return isNaN(d.getTime()) ? '' : `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
        }
      } catch(e) { /* ignore */ }
      return ''
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
  0%, 20%, 50%, 80%, 100% {
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
