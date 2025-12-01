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
        <div class="header-left">
          <el-button icon="el-icon-arrow-left" @click="closeSubmissionsView">返回作业列表</el-button>
          <div class="title-info">
            <h2>{{ selectedHomework.title }}</h2>
            <div class="sub-info">
              <el-tag size="small">{{ courseInfo.courseName }}</el-tag>
              <el-tag size="small">{{ courseInfo.sessionName }}</el-tag>
              <span class="total-score">满分: {{ selectedHomework.totalScore }} 分</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-button @click="exportSubmissions" :disabled="!submissions || submissions.length===0">导出</el-button>
          <el-button type="primary" @click="refreshSubmissions" :loading="submissionsLoading">
            {{ submissionsLoading ? '加载中...' : '刷新' }}
          </el-button>
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
                  type="info"
                  style="cursor: pointer;"
                  @click="previewFile(file)">
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
      gradeSubmitting: false
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
      if (!Array.isArray(this.submissions)) return []
      return this.submissions.slice().sort((a, b) => {
        // 已批改的排在前面，然后按提交时间倒序
        const aGraded = this.rowIsGraded(a) ? 1 : 0
        const bGraded = this.rowIsGraded(b) ? 1 : 0
        if (aGraded !== bGraded) return bGraded - aGraded

        const ta = a.submitTime ? new Date(a.submitTime).getTime() : 0
        const tb = b.submitTime ? new Date(b.submitTime).getTime() : 0
        return tb - ta
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
    }
  },
  created() {
    console.log('作业批改页面初始化')
    this.fetchCourses()
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

    refreshSubmissions() {
      if (this.selectedHomework && this.selectedHomework.homeworkId) {
        this.loadSubmissions(this.selectedHomework.homeworkId)
      }
    },

    startGrade(row) {
      console.log('开始批改:', row)
      this.gradingRow = { ...row }
      this.gradeForm.score = row.score || null
      this.gradeForm.remark = row.remark || ''
      this.gradeDialogVisible = true
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

    previewFile(file) {
      // 文件预览逻辑
      window.open(file, '_blank')
    },

    exportSubmissions() {
      // 导出逻辑
      this.$message.info('导出功能开发中...')
    },

    async submitGrade() {
      if (this.gradeForm.score === null || this.gradeForm.score === undefined) {
        this.$message.error('请输入成绩')
        return
      }

      this.gradeSubmitting = true
      try {
        const payload = {
          studentHomeworkId: this.gradingRow.studentHomeworkId || this.gradingRow.id,
          homeworkId: this.gradingRow.homeworkId || (this.selectedHomework && this.selectedHomework.homeworkId),
          studentId: this.gradingRow.studentId,
          score: this.gradeForm.score,
          remark: this.gradeForm.remark,
          status: 2
        }

        console.log('提交批改数据:', payload)
        await gradeSubmission(payload)

        // 更新本地数据，而不是重新加载
        const index = this.submissions.findIndex(s =>
          (s.studentHomeworkId === this.gradingRow.studentHomeworkId) ||
          (s.id === this.gradingRow.id) ||
          (s.studentId === this.gradingRow.studentId && s.homeworkId === this.gradingRow.homeworkId)
        )

        if (index !== -1) {
          // 更新该记录的批改信息
          this.$set(this.submissions, index, {
            ...this.submissions[index],
            score: this.gradeForm.score,
            remark: this.gradeForm.remark,
            status: 2,
            is_graded: 1,
            correctedTime: new Date().toISOString(),
            corrected_time: new Date().toISOString()
          })
        }

        this.$message.success('批改已保存')
        this.gradeDialogVisible = false

        // 可选：在后台静默刷新数据
        setTimeout(() => {
          this.refreshSubmissions()
        }, 500)
      } catch (error) {
        console.error('批改保存失败:', error)
        this.$message.error('保存失败: ' + (error.message || '网络错误'))
      } finally {
        this.gradeSubmitting = false
      }
    },

    formatTime(val) {
      if (!val) return null
      let ts = val
      if (typeof val === 'string' && /^\d+$/.test(val)) ts = Number(val)
      if (typeof ts === 'number') {
        if (ts > 0 && ts < 1e12) ts = ts * 1000
        const d = new Date(ts)
        if (!isNaN(d.getTime())) {
          return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
        }
      }
      const d = new Date(val)
      if (isNaN(d.getTime())) return val
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
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

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
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

.header-actions {
  display: flex;
  gap: 8px;
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

