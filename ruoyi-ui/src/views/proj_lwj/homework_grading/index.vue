<template>
  <div class="app-container">
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px">
      <div style="font-weight:600">作业批改</div>
      <div>
        <el-select v-model="form.courseId" placeholder="选择课程" filterable style="min-width:220px" @change="onCourseChange">
          <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
        </el-select>
        <el-select v-model="form.sessionId" placeholder="选择课堂" filterable style="min-width:220px" @change="loadHomeworks">
          <el-option v-for="s in sessions" :key="s.sessionId" :label="s.className" :value="s.sessionId" />
        </el-select>
      </div>
    </div>

    <div>
      <template v-if="!isListView">
        <div style="display:flex;gap:16px">
          <div style="flex:1">
            <el-table :data="homeworkList" style="width:100%" v-loading="loading">
              <el-table-column prop="title" label="标题" />
              <el-table-column label="截止时间" width="180">
                <template #default="{ row }">{{ formatTime(row.deadline) || '—' }}</template>
              </el-table-column>
              <el-table-column prop="totalScore" label="分值" width="80" />
              <el-table-column label="操作" width="160">
                <template #default="{ row }">
                  <el-button size="mini" type="primary" @click="openSubmissions(row)">查看提交</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Submissions panel on the right when a homework is selected or loading -->
          <div class="grading-right-panel" style="width:560px;border-left:1px solid #eee;padding-left:16px" v-show="submissionsPanelVisible">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
              <div style="font-weight:600">批改 - {{ (selectedHomework && (selectedHomework.title || ('作业#' + (selectedHomework.homeworkId || selectedHomework.id)))) || '批改' }}</div>
              <div style="display:flex;gap:8px;align-items:center">
                <el-button size="mini" @click="goBack">返回</el-button>
                <el-button size="mini" type="primary" @click="exportSubmissions" :disabled="!submissions || submissions.length===0">导出</el-button>
                <el-button size="mini" type="info" @click="printSubmissions" :disabled="!submissions || submissions.length===0">打印</el-button>
                <el-button size="mini" type="primary" @click="refreshSubmissions">刷新</el-button>
              </div>
            </div>
            <el-table :data="sortedSubmissions" style="width:100%" v-loading="submissionsLoading">
              <el-table-column label="序号ID" width="110">
                <template #default="{ row }">{{ row.student_id || row.studentId || row.studentId || row.id || '' }}</template>
              </el-table-column>
              <el-table-column label="学号" width="140">
                <template #default="{ row }">{{ row.student_no || row.studentNo || '' }}</template>
              </el-table-column>
              <el-table-column prop="studentName" label="姓名" width="120" />
              <el-table-column label="提交状态" width="120">
                <template #default="{ row }">
                  {{ statusLabel(row) }}
                </template>
              </el-table-column>
              <el-table-column prop="submissionFiles" label="附件">
                <template #default="{ row }">
                  <div v-if="row.submissionFiles">
                    <a v-for="(f, idx) in parseAttachments(row.submissionFiles)" :key="idx" :href="downloadUrl(f)" target="_blank" style="margin-right:8px">{{ f }}</a>
                  </div>
                  <div v-else>—</div>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="提交时间" width="160">
                <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
              </el-table-column>
              <el-table-column label="成绩/评语" width="200">
                <template #default="{ row }">
                  <div v-if="row.score !== null && row.score !== undefined">{{ row.score }} 分</div>
                  <div v-if="row.remark" style="color:#666">评语：{{ row.remark }}</div>
                  <div v-if="!(row.score !== null && row.score !== undefined) && !row.remark" style="color:#888">未批改</div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button size="mini" :type="rowIsGraded(row) ? 'success' : 'primary'" @click="startGrade(row)" :disabled="!canGrade(row)">{{ gradeButtonLabel(row) }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="submissionsLoading" style="padding:20px;color:#666">正在加载提交列表，请稍候...</div>
            <div v-else-if="!submissions || submissions.length===0" style="padding:20px;color:#999">暂无提交（或后端未返回学生列表）</div>
          </div>
        </div>
      </template>

      <!-- Full-page submissions list when navigated to the list route -->
      <template v-else>
        <div style="margin-bottom:12px;display:flex;align-items:center;justify-content:space-between">
          <div style="font-weight:600">提交列表 - {{ selectedHomework && (selectedHomework.title || ('作业#' + (selectedHomework.homeworkId || selectedHomework.id))) }}</div>
          <div style="display:flex;gap:8px">
            <el-button size="mini" @click="goBack">返回</el-button>
            <el-button size="mini" type="primary" @click="exportSubmissions" :disabled="!submissions || submissions.length===0">导出</el-button>
            <el-button size="mini" type="info" @click="printSubmissions" :disabled="!submissions || submissions.length===0">打印</el-button>
            <el-button size="mini" type="primary" @click="refreshSubmissions">刷新</el-button>
          </div>
        </div>
        <el-table :data="sortedSubmissions" style="width:100%" v-loading="submissionsLoading">
          <el-table-column label="序号ID" width="110">
            <template #default="{ row }">{{ row.student_id || row.studentId || row.id || '' }}</template>
          </el-table-column>
          <el-table-column label="学号" width="140">
            <template #default="{ row }">{{ row.student_no || row.studentNo || '' }}</template>
          </el-table-column>
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column label="提交状态" width="120">
            <template #default="{ row }">
              {{ statusLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="submissionFiles" label="附件">
            <template #default="{ row }">
              <div v-if="row.submissionFiles">
                <a v-for="(f, idx) in parseAttachments(row.submissionFiles)" :key="idx" :href="downloadUrl(f)" target="_blank" style="margin-right:8px">{{ f }}</a>
              </div>
              <div v-else>—</div>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="160">
            <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
          </el-table-column>
          <el-table-column label="成绩/评语" width="200">
            <template #default="{ row }">
              <div v-if="row.score !== null && row.score !== undefined">{{ row.score }} 分</div>
              <div v-if="row.remark" style="color:#666">评语：{{ row.remark }}</div>
              <div v-if="!(row.score !== null && row.score !== undefined) && !row.remark" style="color:#888">未批改</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button size="mini" :type="rowIsGraded(row) ? 'success' : 'primary'" @click="startGrade(row)" :disabled="!canGrade(row)">{{ gradeButtonLabel(row) }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="submissionsLoading" style="padding:20px;color:#666">正在加载提交列表，请稍候...</div>
        <div v-else-if="!submissions || submissions.length===0" style="padding:20px;color:#999">暂无提交（或后端未返回学生列表）</div>
      </template>
    </div>

    <!-- Grade dialog -->
    <el-dialog title="批改学生作业" :visible.sync="gradeDialogVisible" width="600px">
      <div>
        <div style="margin-bottom:8px"><strong>学生：</strong>{{ gradingRow.studentName || gradingRow.studentId }}</div>
        <div style="margin-bottom:8px"><strong>提交时间：</strong>{{ formatTime(gradingRow.submitTime) || '—' }}</div>
        <div style="margin-bottom:8px"><strong>附件：</strong>
          <div v-if="gradingRow.submissionFiles">
            <a v-for="(f, idx) in parseAttachments(gradingRow.submissionFiles)" :key="idx" :href="downloadUrl(f)" target="_blank" style="margin-right:8px">{{ f }}</a>
          </div>
          <div v-else>—</div>
        </div>

        <el-form :model="gradeForm" label-width="80px">
          <el-form-item label="分数">
            <el-input v-model.number="gradeForm.score" type="number" placeholder="请输入分数" />
          </el-form-item>
          <el-form-item label="评语">
            <el-input type="textarea" v-model="gradeForm.remark" placeholder="请输入评语（可选）" />
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="gradeSubmitting" @click="submitGrade">确定</el-button>
      </span>
    </el-dialog>

    <!-- Submissions modal dialog for narrow viewports -->
    <el-dialog title="作业提交列表" :visible.sync="submissionsModalVisible" width="80%" :fullscreen="true">
      <div>
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
          <div style="font-weight:600">提交列表 - {{ (selectedHomework && (selectedHomework.title || ('作业#' + (selectedHomework.homeworkId || selectedHomework.id)))) || '提交列表' }}</div>
          <div>
            <el-button size="mini" @click="goBack">返回</el-button>
            <el-button size="mini" type="primary" @click="exportSubmissions" :disabled="!submissions || submissions.length===0">导出</el-button>
            <el-button size="mini" type="info" @click="printSubmissions" :disabled="!submissions || submissions.length===0">打印</el-button>
            <el-button size="mini" type="primary" @click="refreshSubmissions">刷新</el-button>
          </div>
        </div>
        <el-table :data="sortedSubmissions" style="width:100%" v-loading="submissionsLoading">
          <el-table-column label="序号ID" width="110">
            <template #default="{ row }">{{ row.student_id || row.studentId || row.id || '' }}</template>
          </el-table-column>
          <el-table-column label="学号" width="140">
            <template #default="{ row }">{{ row.student_no || row.studentNo || '' }}</template>
          </el-table-column>
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column label="提交状态" width="120">
            <template #default="{ row }">
              {{ statusLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="submissionFiles" label="附件">
            <template #default="{ row }">
              <div v-if="row.submissionFiles">
                <a v-for="(f, idx) in parseAttachments(row.submissionFiles)" :key="idx" :href="downloadUrl(f)" target="_blank" style="margin-right:8px">{{ f }}</a>
              </div>
              <div v-else>—</div>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="160">
            <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
          </el-table-column>
          <el-table-column label="成绩/评语" width="200">
            <template #default="{ row }">
              <div v-if="row.score !== null && row.score !== undefined">{{ row.score }} 分</div>
              <div v-if="row.remark" style="color:#666">评语：{{ row.remark }}</div>
              <div v-if="!(row.score !== null && row.score !== undefined) && !row.remark" style="color:#888">未批改</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button size="mini" :type="rowIsGraded(row) ? 'success' : 'primary'" @click="startGrade(row)" :disabled="!canGrade(row)">{{ gradeButtonLabel(row) }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="submissionsLoading" style="padding:20px;color:#666">正在加载提交列表，请稍候...</div>
        <div v-else-if="!submissions || submissions.length===0" style="padding:20px;color:#999">暂无提交（或后端未返回学生列表）</div>
      </div>
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
      subDialogVisible: false,
      subDialogTitle: '',
      submissions: [],
      submissionsLoading: false,
      submissionsModalVisible: false,
       // controls visibility of the right-side submissions panel independently so UI appears immediately
      submissionsPanelVisible: false,
      // grading dialog
      gradeDialogVisible: false,
      gradingRow: {},
      gradeForm: { score: null, remark: '' },
      gradeSubmitting: false,
      selectedHomework: null,
      // when true we display the submissions as a full page (route: HomeworkGradingList)
      isListView: false,
      // When navigated from homework_publish (mode=view) we show read-only submissions
      readOnlyView: false
    }
  },
  computed: {
    // Return submissions sorted: submitted students first (by submitTime desc), then others by student number asc
    sortedSubmissions() {
      if (!this.submissions || !Array.isArray(this.submissions)) return []
      return this.submissions.slice().sort((a, b) => {
        const aSubmitted = (a.submitTime || (a.submissionFiles && String(a.submissionFiles).trim() !== '') || a.status === '1' || a.status === '2') ? 1 : 0
        const bSubmitted = (b.submitTime || (b.submissionFiles && String(b.submissionFiles).trim() !== '') || b.status === '1' || b.status === '2') ? 1 : 0
        if (aSubmitted !== bSubmitted) return bSubmitted - aSubmitted // submitted first

        // If both submitted, put newer submissions first
        if (aSubmitted && bSubmitted) {
          const ta = a.submitTime ? new Date(a.submitTime).getTime() : 0
          const tb = b.submitTime ? new Date(b.submitTime).getTime() : 0
          if (tb !== ta) return tb - ta
        }

        // Fallback: sort by student number (学号) numeric asc when possible
        const aNo = Number(a.student_no || a.studentNo || a.student_id || a.studentId || 0)
        const bNo = Number(b.student_no || b.studentNo || b.student_id || b.studentId || 0)
        if (!isNaN(aNo) && !isNaN(bNo) && aNo !== bNo) return aNo - bNo

        // final fallback: string compare of name or id
        const as = String(a.student_no || a.studentNo || a.studentName || a.student_id || a.studentId || '')
        const bs = String(b.student_no || b.studentNo || b.studentName || b.student_id || b.studentId || '')
        return as.localeCompare(bs, undefined, { numeric: true })
      })
    }
  },
  created() {
    // Fetch courses first. If route query contains courseId/sessionId/title/homeworkId, prefill and load appropriately
    const params = (this.$route && this.$route.params) ? this.$route.params : {}
    const q = (this.$route && this.$route.query) ? this.$route.query : {}
    // First load courses, then possibly sessions and submissions
    // set readOnlyView if route query has mode=view
    this.readOnlyView = (q && q.mode === 'view')
    this.fetchCourses().then(() => {
      // if query contains course/session, set them so UI shows context
      if (q.courseId) {
        this.form.courseId = isNaN(Number(q.courseId)) ? q.courseId : Number(q.courseId)
        // load sessions for that course
        const api = require('@/api/proj_lw/session')
        api.getSessionsByCourseId(this.form.courseId).then(res => {
          this.sessions = res && res.rows ? res.rows : (res && res.data ? res.data : [])
          if (q.sessionId) {
            this.form.sessionId = isNaN(Number(q.sessionId)) ? q.sessionId : Number(q.sessionId)
            // optionally set selectedHomework.title from query
            if (q.title) {
              this.selectedHomework = { homeworkId: params.homeworkId ? Number(params.homeworkId) : (q.homeworkId ? Number(q.homeworkId) : null), title: q.title, courseId: this.form.courseId, sessionId: this.form.sessionId }
            }
            // load homeworks for the preselected session
            this.loadHomeworks(this.form.sessionId)
          }
        }).catch(() => {})
      }

      // If no courseId but sessionId provided, just load homeworks for that session
      if (!q.courseId && q.sessionId) {
        this.form.sessionId = isNaN(Number(q.sessionId)) ? q.sessionId : Number(q.sessionId)
        this.loadHomeworks(this.form.sessionId)
      }

      // If homeworkId/title provided (either via params or query), load submissions directly
      const hwParam = params.homeworkId || q.homeworkId
      if (hwParam) {
        const hwId = Number(hwParam)
        this.isListView = true
        // If we already set selectedHomework from title above, preserve title; otherwise pick from query
        if (!this.selectedHomework) this.selectedHomework = { homeworkId: hwId, title: q.title }
        this.loadSubmissions(hwId)
      }
    }).catch(() => {
      // fallback: still attempt to load submissions if homeworkId present
      const hwParam = params.homeworkId || q.homeworkId
      if (hwParam) {
        const hwId = Number(hwParam)
        this.selectedHomework = { homeworkId: hwId, title: q.title }
        this.loadSubmissions(hwId)
      }
    })
  },
  methods: {
    fetchCourses() {
      return listCourse({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.courses = res && res.rows ? res.rows : (res && res.data ? res.data : [])
        return this.courses
      }).catch(() => { this.courses = []; return [] })
    },
    onCourseChange() {
      if (!this.form.courseId) { this.sessions = []; this.form.sessionId = null; this.homeworkList = []; return }
      const api = require('@/api/proj_lw/session')
      api.getSessionsByCourseId(this.form.courseId).then(res => {
        this.sessions = res && res.rows ? res.rows : (res && res.data ? res.data : [])
        this.form.sessionId = null
      }).catch(() => { this.sessions = [] })
    },
    loadHomeworks() {
      if (!this.form.sessionId) { this.homeworkList = []; return }
      this.loading = true
      listHomework({ sessionId: this.form.sessionId, pageNum: 1, pageSize: 1000 }).then(res => {
        this.homeworkList = res && (res.rows || res.data) ? (res.rows || res.data) : (res || [])
        this.loading = false
      }).catch(() => { this.loading = false; this.homeworkList = [] })
    },
    openSubmissions(row) {
      // Navigate to the dedicated submissions list route so the submissions show on a new page.
      const idRaw = row.homeworkId || row.homework_id || row.id || row.homework || null
      const id = (typeof idRaw === 'string' && idRaw.trim() !== '') ? Number(idRaw) : (typeof idRaw === 'number' ? idRaw : (idRaw ? Number(idRaw) : null))
      const title = row.title || row.homeworkTitle || row.name || ''
      const query = { title, courseId: this.form.courseId, sessionId: this.form.sessionId }
      if (!id || isNaN(id)) { this.$message && this.$message.error && this.$message.error('无法识别作业ID'); return }
      // Set UI state immediately so the full-page list appears without waiting for route navigation
      this.selectedHomework = { homeworkId: id, title, courseId: this.form.courseId, sessionId: this.form.sessionId }
      this.isListView = true
      this.submissionsLoading = true
      // start loading submissions right away so the user sees content
      this.loadSubmissions(id)
      // Push an explicit path to avoid name-based ambiguities and ensure full navigation
      const path = `/proj_lwj/homework_grading/list/${id}`
      if (this.$router && this.$router.push) {
        this.$router.push({ path, query }).catch(() => {})
      }
       // leave actual loading to the created()/watcher which reacts to route params
    },
    // Centralized loader so created()/watcher/navigation reuse same logic
    loadSubmissions(homeworkId) {
      const hwId = (typeof homeworkId === 'string' && homeworkId.trim() !== '') ? Number(homeworkId) : Number(homeworkId)
      if (!hwId || isNaN(hwId)) { this.submissions = []; console.warn('loadSubmissions: invalid homeworkId', homeworkId); return }
      // set selectedHomework homeworkId if not set
      if (!this.selectedHomework) this.selectedHomework = { homeworkId: hwId }
      else this.selectedHomework.homeworkId = hwId
      console.debug('loadSubmissions: calling getSubmissions', hwId)
      this.submissionsLoading = true
      getSubmissions(hwId).then(res => {
        this.submissions = res && (res.data || res.rows) ? (res.data || res.rows) : (res || [])
        console.debug('loadSubmissions: loaded', this.submissions && this.submissions.length)
      }).catch(err => { console.error('loadSubmissions error', err); this.submissions = [] }).finally(() => { this.submissionsLoading = false })
    },
    refreshSubmissions() {
      if (this.selectedHomework && this.selectedHomework.homeworkId) {
        getSubmissions(this.selectedHomework.homeworkId).then(res => {
          this.submissions = res && (res.data || res.rows) ? (res.data || res.rows) : (res || [])
        }).catch(() => { this.submissions = [] })
      }
    },
    parseAttachments(str) { if (!str) return []; return String(str).split(',').map(s=>s.trim()).filter(Boolean) },
    downloadUrl(fileName) { return process.env.VUE_APP_BASE_API + '/common/download?fileName=' + encodeURIComponent(fileName) },
    startGrade(row) {
      if (this.readOnlyView) return
      this.gradingRow = Object.assign({}, row)
      this.gradeForm.score = row.score || null
      this.gradeForm.remark = row.remark || ''
      this.gradeDialogVisible = true
    },
    rowIsGraded(row) {
      return (row.is_graded === 1) || String(row.status) === '2' || (row.grade !== null && row.grade !== undefined) || (row.score !== null && row.score !== undefined)
    },
    gradeButtonLabel(row) {
      return this.rowIsGraded(row) ? '修改' : '批改'
    },
    submitGrade() {
      if (this.gradeForm.score === null || this.gradeForm.score === undefined) { this.$message.error('请输入成绩'); return }
      this.gradeSubmitting = true
      const payload = {
        studentHomeworkId: this.gradingRow.studentHomeworkId || this.gradingRow.id,
        homeworkId: this.gradingRow.homeworkId,
        studentId: this.gradingRow.studentId,
        score: this.gradeForm.score,
        remark: this.gradeForm.remark,
        status: 2
      }
      gradeSubmission(payload).then(() => {
        this.gradeSubmitting = false
        this.gradeDialogVisible = false
        this.$message.success('批改保存成功')
        // Update the local submissions row immediately so button/label/state update without waiting
        try {
          const idKey = this.gradingRow.id || this.gradingRow.studentHomeworkId
          const idx = this.submissions.findIndex(s => (s.id && idKey && s.id === idKey) || (s.studentId && this.gradingRow.studentId && s.studentId === this.gradingRow.studentId))
          if (idx !== -1) {
            const updated = Object.assign({}, this.submissions[idx], { score: this.gradeForm.score, remark: this.gradeForm.remark, status: 2, is_graded: 1, corrected_time: new Date().toISOString() })
            this.$set(this.submissions, idx, updated)
          }
        } catch (e) { console.debug('local update failed', e) }
        this.refreshSubmissions()
      }).catch(err => { console.error(err); this.gradeSubmitting = false; this.$message.error('保存失败') })
    },
    formatTime(val){ if(!val) return null; const d = new Date(val); if(isNaN(d.getTime())) return val; return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}` },
    statusLabel(row) {
      // Prefer status field if present
      const s = row.status !== null && row.status !== undefined ? String(row.status) : null
      if (!row.submitTime && (!row.submissionFiles || row.submissionFiles.length === 0)) {
        // no submission
        // if homework deadline passed, backend can mark overdue; client doesn't know deadline here so show '未提交'
        return '未提交'
      }
      if (s === '2') return '已批改'
      if (s === '3') return '逾期提交'
      return '已提交'
    },
    canGrade(row) {
      // Disable grading when in read-only view (from homework_publish)
      if (this.readOnlyView) return false
      return true
    },
    goBack() {
      // Navigate back to the HomeworkPublish page and preserve last selected course/session if available
      const cid = this.form.courseId || (this.selectedHomework && this.selectedHomework.courseId) || null
      const sid = this.form.sessionId || (this.selectedHomework && this.selectedHomework.sessionId) || null
      // hide panel first
      this.submissionsPanelVisible = false
      this.submissionsModalVisible = false
      this.$router.push({ path: '/proj_lwj/homework_publish', query: { courseId: cid, sessionId: sid } })
    },
    exportSubmissions() {
      if (!this.submissions || !this.submissions.length) { this.$message.info('没有可导出的数据'); return }
      const headers = ['序号ID','学号','姓名','附件','提交时间','状态','成绩','评语']
      const rows = (this.sortedSubmissions || []).map(r => [r.student_id || r.studentId || r.id || '', r.student_no || r.studentNo || '', r.studentName || '', (r.submissionFiles || '').replace(/,/g, ';'), this.formatTime(r.submitTime) || '', r.status || '', (r.score !== null && r.score !== undefined) ? r.score : '', r.remark || ''])
      const csv = [headers.join(',')].concat(rows.map(r => r.map(c => '"' + String(c).replace(/"/g,'""') + '"').join(','))).join('\n')
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      const title = this.selectedHomework && (this.selectedHomework.title || this.selectedHomework.homeworkTitle) ? (this.selectedHomework.title || this.selectedHomework.homeworkTitle) : ('submissions')
      link.setAttribute('download', title + '.csv')
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      this.$message.success('导出已开始')
    },
    printSubmissions() {
      if (!this.submissions || !this.submissions.length) { this.$message.info('没有可打印的数据'); return }
      const title = this.selectedHomework && (this.selectedHomework.title || this.selectedHomework.homeworkTitle) ? (this.selectedHomework.title || this.selectedHomework.homeworkTitle) : ('作业提交')
      // derive course/session names if available
      const course = (this.courses || []).find(c => String(c.courseId) === String(this.form.courseId))
      const courseName = course ? (course.courseName || '') : (this.selectedHomework && this.selectedHomework.courseName ? this.selectedHomework.courseName : '')
      const session = (this.sessions || []).find(s => String(s.sessionId) === String(this.form.sessionId))
      const sessionName = session ? (session.className || '') : (this.selectedHomework && this.selectedHomework.className ? this.selectedHomework.className : '')

      // build HTML with print-friendly CSS (header, footer, page breaks)
      let html = `<!doctype html><html><head><meta charset="utf-8"><title>${title}</title><style>
        body{font-family:Arial,Helvetica,sans-serif;color:#222}
        .print-header{position:fixed;top:0;left:0;right:0;padding:10px 20px;border-bottom:1px solid #ddd;background:#fff}
        .print-footer{position:fixed;bottom:0;left:0;right:0;padding:6px 20px;border-top:1px solid #ddd;background:#fff;font-size:12px;color:#666}
        .print-content{margin:120px 20px 60px 20px}
        table{border-collapse:collapse;width:100%;font-size:13px}
        th,td{border:1px solid #ddd;padding:8px;text-align:left}
        th{background:#f5f7fa}
        @media print {
          .print-header, .print-footer { position: fixed }
          .no-print { display: none }
          table th { background-color: #f5f7fa !important; -webkit-print-color-adjust: exact; }
        }
      </style></head><body>`

      html += `<div class="print-header"><div style="font-size:16px;font-weight:600">${title}</div><div style="margin-top:6px;color:#666">${courseName ? '课程：' + courseName : ''}${courseName && sessionName ? ' / ' : ''}${sessionName ? '课堂：' + sessionName : ''}</div></div>`
      html += `<div class="print-footer">打印时间：${new Date().toLocaleString()}</div>`
      html += `<div class="print-content"><table><thead><tr><th style="width:90px">序号ID</th><th style="width:90px">学号</th><th style="width:140px">姓名</th><th>附件</th><th style="width:150px">提交时间</th><th style="width:90px">状态</th><th style="width:90px">成绩</th><th style="width:200px">评语</th></tr></thead><tbody>`

      (this.sortedSubmissions || []).forEach(r => {
        const files = (r.submissionFiles || '').replace(/,/g, ';')
        const idVal = r.student_id || r.studentId || r.id || ''
        const no = r.student_no || r.studentNo || ''
        html += `<tr><td>${idVal}</td><td>${no}</td><td>${r.studentName || ''}</td><td>${files}</td><td>${this.formatTime(r.submitTime) || ''}</td><td>${this.statusLabel(r)}</td><td>${(r.score !== null && r.score !== undefined) ? r.score : ''}</td><td>${(r.remark || '')}</td></tr>`
      })

      html += `</tbody></table></div>`
      html += `</body></html>`

      const w = window.open('', '_blank')
      if (w) {
        w.document.open()
        w.document.write(html)
        w.document.close()
        // trigger print from opener context to avoid embedding scripts in the written HTML
        setTimeout(() => { try { w.focus(); w.print(); } catch (e) { console.error('print failed', e) } }, 300)
      } else {
        this.$message.error('弹出窗口被拦截，请允许弹窗或使用导出功能')
      }
    }
  },
  watch: {
    // reload when route param homeworkId changes (covers navigation from other pages)
    '$route.params.homeworkId'(newId, oldId) {
      const id = newId || (this.$route && this.$route.query && this.$route.query.homeworkId)
      if (id) {
        const hwId = Number(id)
        this.selectedHomework = this.selectedHomework || { homeworkId: hwId }
        // when route param present, show panel
        // switch to list view (full page)
        this.isListView = true
        // hide side panel/modal when using list view
        this.submissionsPanelVisible = false
        this.submissionsModalVisible = false
        if (this.$route && this.$route.query && this.$route.query.title) {
          this.selectedHomework.title = this.$route.query.title
        }
        // update readOnlyView flag from query
        this.readOnlyView = (this.$route && this.$route.query && this.$route.query.mode === 'view')
        this.loadSubmissions(hwId)
      } else {
        // no homeworkId -> exit list view
        this.isListView = false
        this.submissions = []
        this.selectedHomework = null
        this.submissionsPanelVisible = false
        this.submissionsModalVisible = false
      }
    }
  }
}
</script>

<style>
.grading-right-panel {
  transition: transform 0.3s ease;
  /* Optional: Add a transition for smoothness */
}

/* Add any additional styles for the right panel here */
</style>
