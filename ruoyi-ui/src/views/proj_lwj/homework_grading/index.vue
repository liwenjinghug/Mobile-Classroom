<template>
  <div class="app-container">
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;flex-wrap:wrap;gap:12px">
      <div style="font-weight:600;font-size:20px">作业批改</div>
      <div style="display:flex;gap:12px;flex-wrap:wrap">
        <el-select v-model="form.courseId" placeholder="选择课程" filterable style="min-width:220px;flex:1" @change="onCourseChange">
          <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
        </el-select>
        <el-select v-model="form.sessionId" placeholder="选择课堂" filterable style="min-width:220px;flex:1" @change="loadHomeworks">
          <el-option v-for="s in sessions" :key="s.sessionId" :label="(s.className ? `${s.className} (ID:${s.sessionId})` : String(s.sessionId))" :value="s.sessionId" />
        </el-select>
      </div>
    </div>

    <div>
      <template v-if="!isListView">
        <div style="display:flex;gap:16px;flex-wrap:wrap">
          <div style="flex:1;min-width:300px">
            <el-table :data="homeworkList" style="width:100%" v-loading="loading" class="responsive-table">
              <el-table-column prop="title" label="标题" min-width="200" />
              <el-table-column label="截止时间" width="160">
                <template #default="{ row }">{{ formatTime(row.deadline) || '—' }}</template>
              </el-table-column>
              <el-table-column prop="totalScore" label="分值" width="80" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="mini" type="primary" style="min-width:80px" @click="openSubmissions(row)">查看提交</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Submissions panel on the right when a homework is selected or loading -->
          <div class="grading-right-panel" style="flex:2;min-width:400px;border-left:1px solid #eee;padding-left:16px" v-show="submissionsPanelVisible">
            <div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:8px;flex-wrap:wrap;gap:8px">
              <div style="font-weight:600;font-size:16px">批改 - {{ (selectedHomework && (selectedHomework.title || ('作业#' + (selectedHomework.homeworkId || selectedHomework.id)))) || '批改' }}</div>
              <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
                <el-button size="mini" @click="goBack" style="min-width:60px">返回</el-button>
                <el-button size="mini" type="primary" @click="exportSubmissions" :disabled="!submissions || submissions.length===0" style="min-width:60px">导出</el-button>
                <el-button size="mini" type="info" @click="printSubmissions" :disabled="!submissions || submissions.length===0" style="min-width:60px">打印</el-button>
                <el-button size="mini" type="primary" @click="refreshSubmissions" style="min-width:60px">刷新</el-button>
              </div>
            </div>
            <div v-if="submissions && submissions.length" class="stats-bar">
              <el-alert :closable="false" type="info" :title="`统计：应到 ${gradingStats.total}；已提交 ${gradingStats.submitted}（${formatPercent(gradingStats.submitRate)}）; 已批改 ${gradingStats.graded}（${formatPercent(gradingStats.gradedRate)}）; 未提交 ${gradingStats.unsubmitted}；平均分 ${gradingStats.avgScore}${gradingStats.ext}`" />
            </div>
            <div class="table-container">
              <el-table :data="sortedSubmissions" style="width:100%" v-loading="submissionsLoading" class="responsive-table">
                <el-table-column label="序号ID" width="100">
                  <template #default="{ row }">{{ row.student_id || row.studentId || row.studentId || row.id || '' }}</template>
                </el-table-column>
                <el-table-column label="学号" width="120">
                  <template #default="{ row }">{{ row.student_no || row.studentNo || '' }}</template>
                </el-table-column>
                <el-table-column prop="studentName" label="姓名" width="100" />
                <el-table-column label="提交状态" width="100">
                  <template #default="{ row }">
                    {{ statusLabel(row) }}
                  </template>
                </el-table-column>
                <el-table-column prop="submissionFiles" label="附件" min-width="120">
                  <template #default="{ row }">
                    <div v-if="row.submissionFiles" style="display:flex;flex-wrap:wrap;gap:4px">
                      <el-tag v-for="(f, idx) in parseAttachments(row.submissionFiles)" :key="idx" size="mini" class="tag-link" @click="previewFile(f)">{{ String(f).split('/').pop() }}</el-tag>
                    </div>
                    <div v-else>—</div>
                  </template>
                </el-table-column>
                <el-table-column prop="submitTime" label="提交时间" width="140">
                  <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
                </el-table-column>
                <!-- 新增批改时间列：支持后端不同字段名 -->
                <el-table-column label="批改时间" width="140">
                  <template #default="{ row }">{{ formatTime(row.corrected_time || row.correctedTime || row.gradedAt || row.gradeTime) || '—' }}</template>
                </el-table-column>
                <el-table-column label="成绩/评语" min-width="150">
                  <template #default="{ row }">
                    <div v-if="row.score !== null && row.score !== undefined" style="font-weight:600;color:#67c23a">{{ row.score }} 分</div>
                    <div v-if="row.remark" style="color:#666;font-size:12px">评语：{{ row.remark }}</div>
                    <div v-if="!(row.score !== null && row.score !== undefined) && !row.remark" style="color:#888">未批改</div>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="{ row }">
                    <el-button size="mini" :type="rowIsGraded(row) ? 'success' : 'primary'" @click="startGrade(row)" :disabled="!canGrade(row)" style="min-width:60px">
                      {{ gradeButtonLabel(row) }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-if="submissionsLoading" style="padding:20px;color:#666;text-align:center">正在加载提交列表，请稍候...</div>
            <div v-else-if="!submissions || submissions.length===0" style="padding:20px;color:#999;text-align:center">暂无提交（或后端未返回学生列表）</div>
          </div>
        </div>
      </template>

      <!-- Full-page submissions list when navigated to the list route -->
      <template v-else>
        <div style="margin-bottom:12px;display:flex;align-items:flex-start;justify-content:space-between;flex-wrap:wrap;gap:12px">
          <div style="font-weight:600;font-size:18px">提交列表 - {{ selectedHomework && (selectedHomework.title || ('作业#' + (selectedHomework.homeworkId || selectedHomework.id))) }}</div>
          <div style="display:flex;gap:8px;flex-wrap:wrap">
            <el-button size="mini" @click="goBack" style="min-width:60px">返回</el-button>
            <el-button size="mini" type="primary" @click="exportSubmissions" :disabled="!submissions || submissions.length===0" style="min-width:60px">导出</el-button>
            <el-button size="mini" type="info" @click="printSubmissions" :disabled="!submissions || submissions.length===0" style="min-width:60px">打印</el-button>
            <el-button size="mini" type="primary" @click="refreshSubmissions" style="min-width:60px">刷新</el-button>
          </div>
        </div>
        <div v-if="submissions && submissions.length" class="stats-bar">
          <el-alert :closable="false" type="info" :title="`统计：应到 ${gradingStats.total}；已提交 ${gradingStats.submitted}（${formatPercent(gradingStats.submitRate)}）; 已批改 ${gradingStats.graded}（${formatPercent(gradingStats.gradedRate)}）; 未提交 ${gradingStats.unsubmitted}；平均分 ${gradingStats.avgScore}${gradingStats.ext}`" />
        </div>
        <div class="table-container">
          <el-table :data="sortedSubmissions" style="width:100%" v-loading="submissionsLoading" class="responsive-table">
            <el-table-column label="序号ID" width="100">
              <template #default="{ row }">{{ row.student_id || row.studentId || row.id || '' }}</template>
            </el-table-column>
            <el-table-column label="学号" width="120">
              <template #default="{ row }">{{ row.student_no || row.studentNo || '' }}</template>
            </el-table-column>
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column label="提交状态" width="100">
              <template #default="{ row }">
                {{ statusLabel(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="submissionFiles" label="附件" min-width="120">
              <template #default="{ row }">
                <div v-if="row.submissionFiles" style="display:flex;flex-wrap:wrap;gap:4px">
                  <el-tag v-for="(f, idx) in parseAttachments(row.submissionFiles)" :key="idx" size="mini" class="tag-link" @click="previewFile(f)">{{ String(f).split('/').pop() }}</el-tag>
                </div>
                <div v-else>—</div>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="140">
              <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
            </el-table-column>
            <!-- 新增批改时间列：支持后端不同字段名 -->
            <el-table-column label="批改时间" width="140">
              <template #default="{ row }">{{ formatTime(row.corrected_time || row.correctedTime || row.gradedAt || row.gradeTime) || '—' }}</template>
            </el-table-column>
            <el-table-column label="成绩/评语" min-width="150">
              <template #default="{ row }">
                <div v-if="row.score !== null && row.score !== undefined" style="font-weight:600;color:#67c23a">{{ row.score }} 分</div>
                <div v-if="row.remark" style="color:#666;font-size:12px">评语：{{ row.remark }}</div>
                <div v-if="!(row.score !== null && row.score !== undefined) && !row.remark" style="color:#888">未批改</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="mini" :type="rowIsGraded(row) ? 'success' : 'primary'" @click="startGrade(row)" :disabled="!canGrade(row)" style="min-width:60px">
                  {{ gradeButtonLabel(row) }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-if="submissionsLoading" style="padding:20px;color:#666;text-align:center">正在加载提交列表，请稍候...</div>
        <div v-else-if="!submissions || submissions.length===0" style="padding:20px;color:#999;text-align:center">暂无提交（或后端未返回学生列表）</div>
      </template>
    </div>

    <!-- Grade dialog -->
    <el-dialog title="批改学生作业" :visible.sync="gradeDialogVisible" width="90%" :max-width="600" class="responsive-dialog">
      <div>
        <div style="margin-bottom:8px"><strong>学生：</strong>{{ gradingRow.studentName || gradingRow.studentId }}</div>
        <div style="margin-bottom:8px"><strong>提交时间：</strong>{{ formatTime(gradingRow.submitTime) || '—' }}</div>
        <div v-if="gradingRow.corrected_time || gradingRow.correctedTime || gradingRow.gradedAt || gradingRow.gradeTime" style="margin-bottom:8px"><strong>批改时间：</strong>{{ formatTime(gradingRow.corrected_time || gradingRow.correctedTime || gradingRow.gradedAt || gradingRow.gradeTime) || '—' }}</div>
        <div style="margin-bottom:8px"><strong>附件：</strong>
          <div v-if="gradingRow.submissionFiles" style="display:flex;flex-wrap:wrap;gap:4px">
            <el-tag v-for="(f, idx) in parseAttachments(gradingRow.submissionFiles)" :key="idx" size="mini" class="tag-link" @click="previewFile(f)">{{ String(f).split('/').pop() }}</el-tag>
          </div>
          <div v-else>—</div>
        </div>

        <el-form :model="gradeForm" label-width="80px">
          <el-form-item label="分数">
            <el-input v-model.number="gradeForm.score" type="number" placeholder="请输入分数" style="width:100%" />
          </el-form-item>
          <el-form-item label="评语">
            <el-input type="textarea" v-model="gradeForm.remark" placeholder="请输入评语（可选）" style="width:100%" />
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="gradeDialogVisible = false" style="min-width:80px">取消</el-button>
        <el-button type="primary" :loading="gradeSubmitting" @click="submitGrade" style="min-width:80px">确定</el-button>
      </span>
    </el-dialog>

    <!-- Submissions modal dialog for narrow viewports -->
    <el-dialog title="作业提交列表" :visible.sync="submissionsModalVisible" width="95%" class="responsive-dialog">
      <div>
        <div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:8px;flex-wrap:wrap;gap:12px">
          <div style="font-weight:600;font-size:16px">提交列表 - {{ (selectedHomework && (selectedHomework.title || ('作业#' + (selectedHomework.homeworkId || selectedHomework.id)))) || '提交列表' }}</div>
          <div style="display:flex;gap:8px;flex-wrap:wrap">
            <el-button size="mini" @click="goBack" style="min-width:60px">返回</el-button>
            <el-button size="mini" type="primary" @click="exportSubmissions" :disabled="!submissions || submissions.length===0" style="min-width:60px">导出</el-button>
            <el-button size="mini" type="info" @click="printSubmissions" :disabled="!submissions || submissions.length===0" style="min-width:60px">打印</el-button>
            <el-button size="mini" type="primary" @click="refreshSubmissions" style="min-width:60px">刷新</el-button>
          </div>
        </div>
        <div v-if="submissions && submissions.length" class="stats-bar">
          <el-alert :closable="false" type="info" :title="`统计：应到 ${gradingStats.total}；已提交 ${gradingStats.submitted}（${formatPercent(gradingStats.submitRate)}）; 已批改 ${gradingStats.graded}（${formatPercent(gradingStats.gradedRate)}）; 未提交 ${gradingStats.unsubmitted}；平均分 ${gradingStats.avgScore}${gradingStats.ext}`" />
        </div>
        <div class="table-container">
          <el-table :data="sortedSubmissions" style="width:100%" v-loading="submissionsLoading" class="responsive-table">
            <el-table-column label="序号ID" width="100">
              <template #default="{ row }">{{ row.student_id || row.studentId || row.id || '' }}</template>
            </el-table-column>
            <el-table-column label="学号" width="120">
              <template #default="{ row }">{{ row.student_no || row.studentNo || '' }}</template>
            </el-table-column>
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column label="提交状态" width="100">
              <template #default="{ row }">
                {{ statusLabel(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="submissionFiles" label="附件" min-width="120">
              <template #default="{ row }">
                <div v-if="row.submissionFiles" style="display:flex;flex-wrap:wrap;gap:4px">
                  <el-tag v-for="(f, idx) in parseAttachments(row.submissionFiles)" :key="idx" size="mini" class="tag-link" @click="previewFile(f)">{{ String(f).split('/').pop() }}</el-tag>
                </div>
                <div v-else>—</div>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="140">
              <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
            </el-table-column>
            <!-- 新增批改时间列：支持后端不同字段名 -->
            <el-table-column label="批改时间" width="140">
              <template #default="{ row }">{{ formatTime(row.corrected_time || row.correctedTime || row.gradedAt || row.gradeTime) || '—' }}</template>
            </el-table-column>
            <el-table-column label="成绩/评语" min-width="150">
              <template #default="{ row }">
                <div v-if="row.score !== null && row.score !== undefined" style="font-weight:600;color:#67c23a">{{ row.score }} 分</div>
                <div v-if="row.remark" style="color:#666;font-size:12px">评语：{{ row.remark }}</div>
                <div v-if="!(row.score !== null && row.score !== undefined) && !row.remark" style="color:#888">未批改</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="mini" :type="rowIsGraded(row) ? 'success' : 'primary'" @click="startGrade(row)" :disabled="!canGrade(row)" style="min-width:60px">
                  {{ gradeButtonLabel(row) }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-if="submissionsLoading" style="padding:20px;color:#666;text-align:center">正在加载提交列表，请稍候...</div>
        <div v-else-if="!submissions || submissions.length===0" style="padding:20px;color:#999;text-align:center">暂无提交（或后端未返回学生列表）</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// 保持原有的script逻辑完全不变
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
    },
    gradingStats() {
      const list = Array.isArray(this.submissions) ? this.submissions : []
      const total = list.length
      if (!total) return { total: 0, submitted: 0, unsubmitted: 0, graded: 0, ungraded: 0, submitRate: 0, gradedRate: 0, avgScore: 0, maxScore: null, minScore: null, ext: '' }
      let submitted = 0, graded = 0
      let scoreSum = 0, scoreCnt = 0
      let maxScore = null, minScore = null
      list.forEach(r => {
        const hasSubmit = !!(r.submitTime || (r.submissionFiles && String(r.submissionFiles).trim() !== '') || r.status === '1' || r.status === '2')
        if (hasSubmit) submitted += 1
        if (this.rowIsGraded(r)) graded += 1
        const s = (r.score !== null && r.score !== undefined) ? Number(r.score) : (r.grade !== null && r.grade !== undefined ? Number(r.grade) : NaN)
        if (!isNaN(s)) { scoreSum += s; scoreCnt++; maxScore = (maxScore==null? s : Math.max(maxScore, s)); minScore = (minScore==null? s : Math.min(minScore, s)) }
      })
      const unsubmitted = total - submitted
      const ungraded = submitted - graded
      const submitRate = total ? submitted / total : 0
      const gradedRate = submitted ? graded / submitted : 0
      const avgScore = scoreCnt ? (scoreSum / scoreCnt).toFixed(1) : 0
      const ext = (scoreCnt ? `；最高 ${maxScore}；最低 ${minScore}` : '')
      return { total, submitted, unsubmitted, graded, ungraded, submitRate, gradedRate, avgScore, maxScore, minScore, ext }
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

    // 新增：当没有路由查询参数时，尝试恢复本地上次选择
    try {
      const hasQuery = !!(this.$route && this.$route.query && (this.$route.query.courseId || this.$route.query.sessionId))
      if (!hasQuery) {
        const lastCid = localStorage.getItem('hwGradingLastCourseId')
        const lastSid = localStorage.getItem('hwGradingLastSessionId')
        if (lastCid) this.form.courseId = isNaN(Number(lastCid)) ? lastCid : Number(lastCid)
        if (lastSid) this.form.sessionId = isNaN(Number(lastSid)) ? lastSid : Number(lastSid)
      }
    } catch (e) { /* ignore */ }
    // 如果已恢复了课程，加载课堂列表
    if (this.form.courseId && (!this.sessions || !this.sessions.length)) {
      const api = require('@/api/proj_lw/session')
      api.getSessionsByCourseId(this.form.courseId).then(res => {
        this.sessions = res && res.rows ? res.rows : (res && res.data ? res.data : [])
        // 如果已有课堂选择，则加载作业列表
        if (this.form.sessionId) {
          this.loadHomeworks(this.form.sessionId)
        }
      }).catch(() => {})
    } else if (this.form.sessionId) {
      // 如果只有课堂（无课程），也尝试加载作业列表
      this.loadHomeworks(this.form.sessionId)
    }
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
      // 新增：持久化课程选择
      try { localStorage.setItem('hwGradingLastCourseId', this.form.courseId ? String(this.form.courseId) : '') } catch(e){}
    },
    loadHomeworks() {
      if (!this.form.sessionId) { this.homeworkList = []; return }
      this.loading = true
      listHomework({ sessionId: this.form.sessionId, pageNum: 1, pageSize: 1000 }).then(res => {
        this.homeworkList = res && (res.rows || res.data) ? (res.rows || res.data) : (res || [])
        this.loading = false
      }).catch(() => { this.loading = false; this.homeworkList = [] })
      // 新增：持久化课堂选择
      try { localStorage.setItem('hwGradingLastSessionId', this.form.sessionId ? String(this.form.sessionId) : '') } catch(e){}
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
    formatTime(val){
      if (val === null || val === undefined || val === '') return null
      // If numeric (number or numeric string), coerce to number and treat as seconds when value looks like seconds
      let ts = val
      if (typeof val === 'string' && /^\d+$/.test(val)) ts = Number(val)
      if (typeof ts === 'number') {
        // if timestamp is seconds (e.g. 10-digit), convert to ms
        if (ts > 0 && ts < 1e12) ts = ts * 1000
        const dnum = new Date(ts)
        if (!isNaN(dnum.getTime())) {
          return `${dnum.getFullYear()}-${String(dnum.getMonth()+1).padStart(2,'0')}-${String(dnum.getDate()).padStart(2,'0')} ${String(dnum.getHours()).padStart(2,'0')}:${String(dnum.getMinutes()).padStart(2,'0')}:${String(dnum.getSeconds()).padStart(2,'0')}`
        }
      }
      // Fallback: try Date parsing for ISO strings
      const d = new Date(val)
      if (isNaN(d.getTime())) return val
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}`
    },
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
      // 新增：返回前也记录当前选择，便于下次进入自动恢复
      try {
        const cid = this.form.courseId || (this.selectedHomework && this.selectedHomework.courseId)
        const sid = this.form.sessionId || (this.selectedHomework && this.selectedHomework.sessionId)
        localStorage.setItem('hwGradingLastCourseId', cid ? String(cid) : '')
        localStorage.setItem('hwGradingLastSessionId', sid ? String(sid) : '')
      } catch(e){}
      this.$router.push({ path: '/proj_lwj/homework_publish', query: { courseId: cid, sessionId: sid } })
    },
    exportSubmissions() {
      if (!this.submissions || !this.submissions.length) { this.$message.info('没有可导出的数据'); return }
      const headers = ['序号ID','学号','姓名','附件','提交时间','批改时间','状态','成绩','评语']
      const rows = (this.sortedSubmissions || []).map(r => [r.student_id || r.studentId || r.id || '', r.student_no || r.studentNo || '', r.studentName || '', (r.submissionFiles || '').replace(/,/g, ';'), this.formatTime(r.submitTime) || '', this.formatTime(r.corrected_time || r.correctedTime || r.gradedAt || r.gradeTime) || '', r.status || '', (r.score !== null && r.score !== undefined) ? r.score : '', r.remark || ''])
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
    formatPercent(n) { if (!n) return '0%'; return (n*100).toFixed(1) + '%' },
    printSubmissions() {
      if (!this.submissions || !this.submissions.length) { this.$message.info('没有可打印的数据'); return }
      const title = this.selectedHomework && (this.selectedHomework.title || this.selectedHomework.homeworkTitle) ? (this.selectedHomework.title || this.selectedHomework.homeworkTitle) : ('作业提交')
      // derive course/session names if available
      const course = (this.courses || []).find(c => String(c.courseId) === String(this.form.courseId))
      const courseName = course ? (course.courseName || '') : (this.selectedHomework && this.selectedHomework.courseName ? this.selectedHomework.courseName : '')
      const session = (this.sessions || []).find(s => String(s.sessionId) === String(this.form.sessionId))
      const sessionName = session ? (session.className || '') : (this.selectedHomework && this.selectedHomework.className ? this.selectedHomework.className : '')

      const stats = this.gradingStats

      // build HTML with print-friendly CSS (header, footer, page breaks)
      let html = `<!doctype html><html><head><meta charset="utf-8"><title>${title}</title><style>
        body{font-family:Arial,Helvetica,sans-serif;color:#222}
        .print-header{position:fixed;top:0;left:0;right:0;padding:10px 20px;border-bottom:1px solid #ddd;background:#fff}
        .print-footer{position:fixed;bottom:0;left:0;right:0;padding:6px 20px;border-top:1px solid #ddd;background:#fff;font-size:12px;color:#666}
        .print-content{margin:140px 20px 60px 20px}
        table{border-collapse:collapse;width:100%;font-size:13px}
        th,td{border:1px solid #ddd;padding:8px;text-align:left}
        th{background:#f5f7fa}
        .meta{margin-top:6px;color:#666}
        @media print {
          .print-header, .print-footer { position: fixed }
          .no-print { display: none }
          table th { background-color: #f5f7fa !important; -webkit-print-color-adjust: exact; }
        }
      </style></head><body>`

      html += `<div class="print-header"><div style="font-size:16px;font-weight:600">${title}</div><div class="meta">${courseName ? '课程：' + courseName : ''}${courseName && sessionName ? ' / ' : ''}${sessionName ? '课堂：' + sessionName : ''}</div><div class="meta">统计：应到 ${stats.total}；已提交 ${stats.submitted}（${this.formatPercent(stats.submitRate)}）; 已批改 ${stats.graded}（${this.formatPercent(stats.gradedRate)}）; 未提交 ${stats.unsubmitted}；平均分 ${stats.avgScore}${stats.ext}</div></div>`
      html += `<div class="print-footer">打印时间：${new Date().toLocaleString()}</div>`
      html += `<div class="print-content"><table><thead><tr><th style="width:90px">序号ID</th><th style="width:90px">学号</th><th style="width:140px">姓名</th><th>附件</th><th style="width:150px">提交时间</th><th style="width:150px">批改时间</th><th style="width:90px">状态</th><th style="width:90px">成绩</th><th style="width:200px">评语</th></tr></thead><tbody>`

      ;(this.sortedSubmissions || []).forEach(r => {
        const files = (r.submissionFiles || '').replace(/,/g, ';')
        const idVal = r.student_id || r.studentId || r.id || ''
        const no = r.student_no || r.studentNo || ''
        html += `<tr><td>${idVal}</td><td>${no}</td><td>${r.studentName || ''}</td><td>${files}</td><td>${this.formatTime(r.submitTime) || ''}</td><td>${this.formatTime(r.corrected_time || r.correctedTime || r.gradedAt || r.gradeTime) || ''}</td><td>${this.statusLabel(r)}</td><td>${(r.score !== null && r.score !== undefined) ? r.score : ''}</td><td>${(r.remark || '')}</td></tr>`
      })

      html += `</tbody></table></div>`
      html += `</body></html>`

      const w = window.open('', '_blank')
      if (w) {
        w.document.open()
        w.document.write(html)
        w.document.close()
        setTimeout(() => { try { w.focus(); w.print(); } catch (e) { console.error('print failed', e) } }, 300)
      } else {
        this.$message.error('弹出窗口被拦截，请允许弹窗或使用导出功能')
      }
    },
    previewFile(path) {
      if (!path) return
      const request = require('@/utils/request').default
      const { getToken } = require('@/utils/auth')
      const token = getToken && getToken()
      const headers = token ? { Authorization: 'Bearer ' + token, isToken: true } : {}
      const norm = String(path).replace(/\\/g, '/').trim()
      const filename = decodeURIComponent(norm.split('/').pop())
      const downloadBlob = url => request({ url, method: 'get', responseType: 'blob', headers, silent: true, timeout: 30000 }).then(resp => {
        const data = resp && resp instanceof Blob ? resp : (resp && resp.data instanceof Blob ? resp.data : new Blob([resp]))
        const href = URL.createObjectURL(data)
        const a = document.createElement('a')
        a.href = href
        a.download = filename
        a.click()
        URL.revokeObjectURL(href)
      })
      const tryResource = () => downloadBlob(`/common/download/resource?resource=${encodeURIComponent(norm)}`).catch(() => {
        const rel = norm.replace(/^\/?profile\/upload\//, '')
        if (!rel) throw new Error('missing path')
        return downloadBlob(`/common/download?fileName=${encodeURIComponent(rel)}&delete=false`)
      })
      if (/^https?:\/\//i.test(norm)) {
        window.open(norm, '_blank')
        return
      }
      if (norm.startsWith('/profile')) {
        tryResource().catch(() => this.$message && this.$message.error && this.$message.error('下载失败'))
        return
      }
      // 其他情况统一规范化到 /profile/upload
      let s = norm
      if (s.startsWith('/upload/')) s = '/profile' + s
      else if (s.startsWith('upload/')) s = '/profile/' + s
      else if (/^\d{4}\//.test(s)) s = '/profile/upload/' + s
      else s = '/profile/upload/' + s
      downloadBlob(`/common/download/resource?resource=${encodeURIComponent(s)}`).catch(() => this.$message && this.$message.error && this.$message.error('下载失败'))
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

<style scoped>
/* 基础样式优化 */
.app-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 表格容器 - 添加水平滚动支持 */
.table-container {
  width: 100%;
  overflow-x: auto;
}

/* 响应式表格 - 设置最小宽度确保内容显示 */
.responsive-table {
  min-width: 1000px;
}

/* 统计栏样式优化 */
.stats-bar {
  margin: 12px 0;
}

/* 右侧批改面板样式优化 */
.grading-right-panel {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 响应式对话框 */
.responsive-dialog {
  border-radius: 8px;
}

/* 标签链接样式 */
.tag-link {
  cursor: pointer;
  margin: 2px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 对话框底部按钮布局优化 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
}

/* 响应式设计 - 小屏幕适配 */
@media (max-width: 768px) {
  .app-container {
    padding: 12px;
  }

  /* 头部区域在小屏幕下垂直排列 */
  .app-container > div:first-child {
    flex-direction: column;
    align-items: stretch;
  }

  /* 筛选区域在小屏幕下垂直排列 */
  .app-container > div:first-child > div:last-child {
    flex-direction: column;
  }

  .filter-select {
    min-width: 100% !important;
    margin-bottom: 8px;
  }

  /* 分割布局在小屏幕下改为垂直 */
  .split-layout {
    flex-direction: column;
  }

  .grading-right-panel {
    border-left: none !important;
    border-top: 1px solid #eee;
    padding-left: 0 !important;
    padding-top: 16px;
    margin-top: 16px;
  }

  /* 操作按钮在小屏幕下换行 */
  .action-buttons {
    flex-wrap: wrap;
  }

  /* 表格在小屏幕下缩小字体 */
  .responsive-table {
    font-size: 12px;
    min-width: 800px;
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  /* 按钮组在小屏幕下垂直排列 */
  .action-buttons {
    flex-direction: column;
    width: 100%;
  }

  .action-buttons .el-button {
    width: 100%;
    margin-bottom: 8px;
  }

  /* 对话框在小屏幕下全宽 */
  .responsive-dialog {
    width: 95% !important;
    margin: 20px auto;
  }
}

/* 确保按钮在缩放时保持最小宽度 */
.el-button {
  box-sizing: border-box;
}

/* 滚动条样式优化 */
.table-container::-webkit-scrollbar {
  height: 6px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.table-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 保持原有的Mac风格样式 */
.app-container {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
}

.app-container >>> .el-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  background-color: #ffffff;
}

.app-container >>> .el-table th {
  background-color: #fbfbfd;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #f5f5f7;
}

.app-container >>> .el-table td {
  border-bottom: 1px solid #f5f5f7;
}

.app-container >>> .el-button--primary {
  background-color: #0071e3;
  border: none;
}

.app-container >>> .el-button--success {
  background-color: #34c759;
  border: none;
}

.app-container >>> .el-dialog {
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}
</style>
