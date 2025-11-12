<template>
  <div class="app-container homework-upload-page">
    <div slot="header" class="clearfix">
      <span style="line-height:36px;font-weight:600">作业提交</span>
      <div style="float:right;margin-top:4px;display:flex;align-items:center;gap:8px">
        <el-button v-if="homeworkId" size="small" type="text" @click="chooseAnother">选择其他作业</el-button>
      </div>
    </div>

    <!-- selection area (shown when no homework selected) -->
    <div v-if="!homeworkId && !loading" style="margin-bottom:12px">
      <el-alert title="未选择作业，请先选择课程和课堂，然后从下拉列表选择要提交的作业" type="warning" show-icon />
      <div style="display:flex;gap:8px;align-items:center;margin-top:8px">
        <el-select v-model="formCourseId" placeholder="选择课程" filterable @change="onCourseChange" style="min-width:260px">
          <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
        </el-select>

        <el-select v-model="formSessionId" placeholder="选择课堂" filterable @change="onSessionChange" style="min-width:260px">
          <el-option v-for="s in sessions" :key="s.sessionId" :label="(s.className ? `${s.className} (ID:${s.sessionId})` : String(s.sessionId))" :value="s.sessionId" />
        </el-select>

        <el-select v-model="selectedHomeworkId" placeholder="选择作业" style="min-width:340px">
          <el-option v-for="h in homeworkList" :key="h.homeworkId" :label="h.title" :value="h.homeworkId" />
        </el-select>

        <el-button type="primary" @click="loadSelectedHomework" :disabled="!selectedHomeworkId">加载作业</el-button>
        <el-button @click="fetchHomeworkList(formSessionId)">刷新列表</el-button>
      </div>
    </div>

    <!-- loading -->
    <div v-if="loading" style="text-align:center;padding:40px 0">
      <div style="display:flex;flex-direction:column;align-items:center;">
        <i class="el-icon-loading" style="font-size:28px;margin-bottom:8px;color:#409EFF"></i>
        <div>加载中...</div>
      </div>
    </div>

    <!-- main content -->
    <div v-else>
      <el-descriptions title="作业详情" :column="1" border>
        <el-descriptions-item label="标题">{{ homework.title || '—' }}</el-descriptions-item>
        <el-descriptions-item label="课程/课堂">{{ homework.courseName || homework.className || '—' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ formatTime(homework.deadline) || '—' }}</el-descriptions-item>
        <el-descriptions-item label="分值">{{ homework.totalScore != null ? homework.totalScore : '—' }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div v-html="homework.content || '—'" style="white-space:pre-wrap"></div>
        </el-descriptions-item>
        <el-descriptions-item label="教师附件">
          <div v-if="parsedHomeworkAttachments.length">
            <el-tag v-for="(f, idx) in parsedHomeworkAttachments" :key="idx" style="margin-right:8px">
              <a href="javascript:void(0)" @click.prevent="previewFile(f)">{{ f }}</a>
            </el-tag>
          </div>
          <div v-else>无</div>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="submission-section">
        <!-- name and studentNo -->
        <div style="margin-bottom:12px; display:flex; gap:8px; align-items:center">
          <el-input v-model="studentName" placeholder="请输入姓名" style="width:180px"></el-input>
          <el-input v-model="studentNo" placeholder="请输入学号" style="width:200px"></el-input>
          <el-button type="primary" size="mini" @click="confirmStudentId">确认学号</el-button>
          <div style="color:#888">(请先填写姓名和学号；确认后将显示您已提交的作业，并允许修改自己的提交)</div>
        </div>

        <div v-if="studentConfirmed">
          <el-alert v-if="submitted" title="您已提交作业" type="success" show-icon></el-alert>
          <el-alert v-else title="尚未提交" type="info" show-icon></el-alert>
        </div>
        <div v-else>
          <el-alert title="请先填写并确认姓名和学号，确认后将仅显示并允许修改您的个人提交记录" type="warning" show-icon></el-alert>
        </div>

        <!-- upload area -->
        <div style="margin:16px 0">
          <el-upload
            ref="upload"
            class="upload-demo"
            :action="uploadUrl"
            :headers="headers"
            :on-success="uploadSuccess"
            :on-remove="handleRemove"
            :file-list="fileList"
            :before-upload="beforeUpload"
            multiple
            list-type="text">
            <el-button size="small" type="primary">选择文件</el-button>
            <div slot="tip" class="el-upload__tip">支持多个文件上传，老师允许的格式与大小以系统为准。</div>
          </el-upload>

          <div v-if="uploadedFiles.length" style="margin-top:8px">
            <div style="font-weight:600;margin-bottom:6px">已选择文件：</div>
            <div>
              <el-tag v-for="(f, i) in uploadedFiles" :key="i" @click.native.prevent="previewFile(f)" style="cursor:pointer;margin-right:8px">{{ f }}</el-tag>
            </div>
          </div>
        </div>

        <div style="margin-bottom:12px">
          <el-button type="primary" :disabled="submitDisabled" @click="submit" :loading="submitLoading">{{ editingSubmissionId ? '保存修改' : '提交作业' }}</el-button>
          <el-button @click="resetUpload">清空</el-button>
          <el-button type="warning" @click="chooseAnother" style="margin-left:8px">选择其他作业</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </div>

        <!-- Edit dialog for modifying an existing submission -->
        <el-dialog title="修改提交" :visible.sync="editDialogVisible" width="600px" @close="closeEditDialog">
          <div>
            <div style="margin-bottom:8px;font-weight:600">当前附件</div>
            <div v-if="editUploadedFiles && editUploadedFiles.length">
              <el-tag v-for="(f, idx) in editUploadedFiles" :key="idx" style="margin-right:8px;cursor:pointer" @click.native.prevent="previewFile(f)">{{ f }}</el-tag>
            </div>
            <div v-else style="color:#888;margin-bottom:8px">当前无附件，您可以上传新文件。</div>

            <el-upload
              ref="editUpload"
              class="upload-demo"
              :action="uploadUrl"
              :headers="headers"
              :on-success="editUploadSuccess"
              :on-remove="editHandleRemove"
              :file-list="editFileList"
              :before-upload="beforeUpload"
              multiple
              list-type="text">
              <el-button size="small" type="primary">选择文件</el-button>
              <div slot="tip" class="el-upload__tip">上传后点击“保存修改”以提交变更。</div>
            </el-upload>

            <div style="margin-top:12px">
              <el-input type="textarea" v-model="remark" placeholder="评语（可选）" rows="3"></el-input>
            </div>
          </div>
          <span slot="footer" class="dialog-footer">
            <el-button @click="closeEditDialog">取消</el-button>
            <el-button type="primary" :loading="editSubmitLoading" @click="saveEditSubmission">保存修改</el-button>
          </span>
        </el-dialog>

        <!-- submissions table -->
        <el-card shadow="never" class="submissions-card">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px">
            <div style="font-weight:600">提交记录 <span style="font-weight:400;color:#888;margin-left:8px;font-size:12px">显示 {{ displayedCount }} / {{ submissionsCount }}</span></div>
            <div style="display:flex;align-items:center;gap:12px">
              <div style="font-size:12px;color:#888">您的姓名：{{ studentName || '未填写' }} &nbsp; 学号：{{ studentNo || '未填写' }}</div>
              <el-switch v-model="showOnlyGraded" active-text="仅显示已批改" inactive-text="显示全部" size="small"></el-switch>
            </div>
          </div>

          <div v-if="submissionsCount > 0 && displayedCount < submissionsCount" style="margin-bottom:8px;color:#e6a23c;font-size:12px">提示：当前显示记录被过滤（显示 {{ displayedCount }} / 共 {{ submissionsCount }} 条）。尝试关闭“仅显示已批改”或确认学号以查看完整记录。</div>

          <div class="table-wrapper">
            <el-table :data="displayedSubmissions" stripe border :key="tableKey" style="width:100%;color:#333;" row-key="studentHomeworkId">
              <el-table-column prop="studentHomeworkId" label="ID" width="80" />

              <el-table-column label="作业" width="220">
                <template #default="{ row }">
                  <div>
                    <div v-if="row.homeworkTitle || row.title">{{ row.homeworkTitle || row.title }}</div>
                    <div v-else-if="row.homeworkId" style="color:#e55353">作业已被老师删除（ID: {{ row.homeworkId }}）</div>
                    <div v-else>—</div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="学生" width="160">
                <template #default="{ row }">{{ row.studentName || row.studentNo || row.studentId || '—' }}</template>
              </el-table-column>

              <el-table-column prop="studentNo" label="学号" width="140">
                <template #default="{ row }">{{ row.student_no || '' }}</template>
              </el-table-column>

              <el-table-column prop="submissionFiles" label="附件">
                <template #default="{ row }">
                  <div v-if="row.submissionFiles || row.attachments || row.files">
                    <el-tag v-for="(f, i) in parseAttachmentString(row.submissionFiles || row.attachments || row.files)" :key="i" class="attachment-tag" style="margin-right:6px;cursor:pointer">
                      <a href="javascript:void(0)" @click.prevent="previewFile(f)" class="attachment-link">{{ f }}</a>
                    </el-tag>
                  </div>
                  <div v-else>无</div>
                </template>
              </el-table-column>

              <el-table-column prop="submitTime" label="提交时间" width="200">
                <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
              </el-table-column>

              <el-table-column label="成绩" width="160">
                <template #default="{ row }">
                  <div v-if="row.score !== null && row.score !== undefined">{{ row.score }} 分</div>
                  <div v-if="row.remark" style="color:#666;margin-top:6px">评语：{{ row.remark }}</div>
                  <div v-if="!(row.score !== null && row.score !== undefined) && !row.remark" style="color:#888">未批改</div>
                </template>
              </el-table-column>

              <el-table-column prop="gradedTime" label="批改时间" width="200">
                <template #default="{ row }">{{ row.gradedTime ? formatTime(row.gradedTime) : '—' }}</template>
              </el-table-column>

              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <div class="action-buttons">
                    <!-- Keep order fixed and use hidden placeholders to avoid layout shift -->
                    <div class="action-slot">
                      <el-tooltip v-if="row.homeworkDeleted || row.homework_deleted" content="该作业已被老师删除，无法修改" placement="top">
                        <el-button class="action-btn" size="mini" type="primary" disabled>已删除</el-button>
                      </el-tooltip>
                      <span v-else-if="false" class="action-button-placeholder"></span>
                    </div>

                    <div class="action-slot">
                      <el-tooltip v-if="row.gradedTime || row.is_graded === 1 || (row.score !== null && row.score !== undefined)" content="该提交已被批改，无法修改" placement="top">
                        <el-button class="action-btn" size="mini" type="primary" disabled>已批改</el-button>
                      </el-tooltip>
                      <span v-else class="action-button-placeholder"></span>
                    </div>

                    <div class="action-slot">
                      <el-tooltip v-if="isPastDeadline(row)" content="已过截止时间，无法修改" placement="top">
                        <el-button class="action-btn" size="mini" type="primary" disabled>已过截止时间</el-button>
                      </el-tooltip>
                      <span v-else class="action-button-placeholder"></span>
                    </div>

                    <div class="action-slot">
                      <el-button v-if="canEditSubmission(row)" class="action-btn" size="mini" type="primary" @click="editSubmission(row)">修改</el-button>
                      <span v-else class="action-button-placeholder"></span>
                    </div>

                    <div class="action-slot">
                      <el-button v-if="isRowOwn(row) && !(row.homeworkDeleted || row.homework_deleted)" class="action-btn danger" size="mini" type="danger" @click="confirmDelete(row)">删除</el-button>
                      <span v-else class="action-button-placeholder"></span>
                    </div>
                  </div>
                </template>
              </el-table-column>

            </el-table>
          </div>

        </el-card>

        <div v-if="!studentConfirmed" style="margin:12px 0;color:#666">（提示）提交记录已载入，但编辑仅在确认学号后启用。</div>

        <div style="margin-top:8px">
          <el-button size="small" type="primary" @click="loadMySubmissions" :disabled="!studentName || !studentNo">刷新我的提交</el-button>
        </div>

      </div>

    </div>
  </div>
</template>

<script>
import {
  getHomework,
  submitHomework,
  getSubmissions,
  listHomework,
  getStudentSubmissions,
  updateSubmission,
  deleteSubmission
} from '@/api/proj_lwj/homework'
import { listCourse } from '@/api/proj_lw/course'
import { getToken } from '@/utils/auth'

export default {
  name: 'HomeworkUpload',
  data() {
    return {
      // debug: capture raw API response for student submissions
      debugResp: null,
      // when true, show inline debug blocks inside the submissions card (disabled in production)
      debugEnabled: false,
      debugLogs: [],
      // key used to force el-table re-render when submissions change
      tableKey: '',
      debugDialogVisible: false,
      courses: [],
      sessions: [],
      loading: true,
      homeworkId: null,
      homeworkList: [],
      selectedHomeworkId: null,
      homework: {},

      // upload state
      fileList: [],
      uploadedFiles: [],
      formCourseId: null,
      formSessionId: null,

      // student identity
      studentName: '',
      studentNo: '',
      studentConfirmed: false,
      // guard to prevent concurrent/rapid repeated loadMySubmissions calls
      _loadingMySubmissions: false,
      // debounce timer id for auto-loading submissions when typing
      _loadMySubmissionsTimer: null,

      // submissions
      submissions: [],
      allSubmissions: [],
      submitted: false,

      // upload config
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      // include both Authorization and isToken (some backend utils expect isToken header)
      headers: { Authorization: 'Bearer ' + getToken(), isToken: true },

      // editing dialog
      editDialogVisible: false,
      editFileList: [],
      editUploadedFiles: [],
      editingSubmissionId: null,
      editSubmitLoading: false,
      // edit remark input (for modification dialog)
      remark: '',

      // meta
      resolvedStudentId: null,
      resolvedStudentName: null,
      submitLoading: false,

      // when true, only show submissions that have a score
      showOnlyGraded: false
    }
  },
  computed: {
    submitDisabled() {
      // need studentNo, at least one file, not already submitted or graded, and homework exists & not deleted & before deadline
      if (!this.homeworkId) return true
      if (!this.studentNo) return true
      if (!this.uploadedFiles || this.uploadedFiles.length === 0) return true
      if (this.submitLoading) return true
      const existing = this.submissions && this.submissions.length ? this.submissions.find(s => String(s.homeworkId) === String(this.homeworkId)) : null
      const hwDeleted = !!(this.homework && (this.homework.homeworkDeleted || this.homework.homework_deleted))
      const pastDeadline = this.homework && this.homework.deadline ? (new Date(this.homework.deadline).getTime() < Date.now()) : false

      // If homework deleted or past deadline, always disable
      if (hwDeleted || pastDeadline) return true

      // If there is an existing submission record for this student/homework, check whether it's already submitted/graded.
      if (existing) {
        const isActuallySubmitted = Boolean((existing.submitTime && String(existing.submitTime).trim() !== '') || (existing.status && String(existing.status) === '2') || (existing.score !== null && existing.score !== undefined) || (existing.gradedTime))
        // If it is already submitted/graded, disallow creating a new submission; require edit mode to modify
        if (isActuallySubmitted) {
          if (!this.editingSubmissionId) return true
        } else {
          // existing record exists but is not actually submitted: allow submission (student can submit files)
          // However, if they're in the middle of editing another submission, prevent duplicate actions
          if (this.editingSubmissionId && String(this.editingSubmissionId) !== String(existing.studentHomeworkId)) {
            return true
          }
        }
      }

      // if component-level submitted flag and not editing, disable
      if (this.submitted && !this.editingSubmissionId) return true

      return false
    },
    parsedHomeworkAttachments() {
      return this.parseAttachmentString(this.homework.attachments || this.homework.teacherFiles)
    },
    // Ensure the table always receives a plain-array of objects with the expected display fields.
    displayedSubmissions() {
      // Only show submissions after the user has entered both name and student number.
      // This prevents exposing the full class list before identifying the student.
      if (!this.studentName || !this.studentNo) return []
      const source = (this.submissions || [])
      if (!Array.isArray(source)) return []
       const mapped = source.map(s => {
          const obj = (s && typeof s === 'object') ? s : { __raw: s }
          return {
             studentHomeworkId: obj.studentHomeworkId || obj.id || obj.student_homework_id || null,
             homeworkId: obj.homeworkId || obj.homework_id || obj.homework || null,
             homeworkTitle: obj.homeworkTitle || obj.title || (obj.__raw && obj.__raw.title) || '',
             studentId: obj.studentId || obj.student_id || null,
             // keep both forms: primary displayed student_no then fallback to studentNo
             student_no: obj.student_no || obj.studentNo || obj.stu_no || '',
             studentNo: obj.studentNo || obj.student_no || obj.stu_no || '',
             studentName: obj.studentName || obj.student_name || obj.student_name || '',
             submissionFiles: obj.submissionFiles || obj.submission_files || obj.attachments || obj.files || '',
             submitTime: obj.submitTime || obj.submit_time || obj.create_time || null,
             gradedTime: obj.gradedTime || obj.gradeTime || obj.grade_time || obj.graded_at || obj.marked_at || obj.update_time || obj.updatedAt || null,
             score: obj.score != null ? obj.score : (obj.grade != null ? obj.grade : null),
             remark: obj.remark || obj.grade_comment || null,
             homeworkDeleted: !!(obj.homeworkDeleted || obj.homework_deleted)
           }
        })

      // If requested, only return submissions that have a score
      if (this.showOnlyGraded) {
        return mapped.filter(r => r.score !== null && r.score !== undefined)
      }

      return mapped
     },
     submissionsCount() {
      // Only show submission counts for the identified student (require both name and studentNo)
      if (!this.studentName || !this.studentNo) return 0
      const source = (this.submissions || [])
      return (source || []).filter(s => String(s.homeworkId) === String(this.homeworkId)).length
    },
     displayedCount() {
       // count of submissions currently displayed in the table
       return (this.displayedSubmissions || []).length
     }
  },
  watch: {
    fileList(newList) {
      this.uploadedFiles = (newList || []).map(f => this.getNameFromFileObj(f)).filter(Boolean)
    },
    editFileList(newList) {
      this.editUploadedFiles = (newList || []).map(f => this.getNameFromFileObj(f)).filter(Boolean)
    },
    // auto-load submissions when user types studentNo or studentName (debounced)
    studentNo() {
      // If user edits studentNo after confirming, invalidate the confirmed state and clear loaded submissions
      if (this._loadMySubmissionsTimer) { clearTimeout(this._loadMySubmissionsTimer); this._loadMySubmissionsTimer = null }
      if (this.studentConfirmed) {
        this.studentConfirmed = false
        this.submissions = []
        this.resolvedStudentId = null
        this.resolvedStudentName = null
        this.submitted = false
      }
    },
    studentName() {
      // If user edits studentName after confirming, invalidate the confirmed state and clear loaded submissions
      if (this._loadMySubmissionsTimer) { clearTimeout(this._loadMySubmissionsTimer); this._loadMySubmissionsTimer = null }
      if (this.studentConfirmed) {
        this.studentConfirmed = false
        this.submissions = []
        this.resolvedStudentId = null
        this.resolvedStudentName = null
        this.submitted = false
      }
    }
  },
  created() {
    // prefer homeworkId from query
    this.homeworkId = this.getValidHomeworkId(this.$route && this.$route.query && this.$route.query.homeworkId ? this.$route.query.homeworkId : null)
    // Use finally so we always clear loading or call init even if fetchCourses throws
    this.fetchCourses().finally(() => {
      if (!this.homeworkId) {
        this.loading = false
      } else {
        // init will set loading true then false around fetches
        this.init()
      }
    })

    // Safety fallback: if something hangs (network, unexpected runtime), clear the spinner after 8s
    setTimeout(() => {
      if (this.loading) {
        console.warn('homework-upload: loading fallback cleared after timeout')
        this.loading = false
      }
    }, 8000)
  },
  methods: {
    // fetch courses
    async fetchCourses() {
      try {
        const res = await listCourse({ pageNum: 1, pageSize: 1000 })
        this.courses = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
      } catch (e) {
        console.error('fetchCourses error', e)
        this.courses = []
        // store debugResp internally but do not open debug UI
        try { this.debugResp = `fetchCourses error:\n${(e && e.message) || e}` } catch (er) { this.debugResp = String(e) }
      }
    },

    // when course changes
    onCourseChange() {
      if (!this.formCourseId) {
        this.sessions = []
        this.formSessionId = null
        this.homeworkList = []
        return
      }
      this.fetchSessionsByCourseId(this.formCourseId)
    },

    fetchSessionsByCourseId(courseId) {
      const api = require('@/api/proj_lw/session')
      return api.getSessionsByCourseId(courseId).then(res => {
        this.sessions = res && res.rows ? res.rows : (res && res.data ? res.data : [])
        this.formSessionId = null
        this.homeworkList = []
        if (!this.sessions || this.sessions.length === 0) this.$message.info('该课程下暂无课堂')
        return this.sessions
      }).catch(err => {
        console.error('fetchSessionsByCourseId error', err)
        this.sessions = []
        this.formSessionId = null
        this.homeworkList = []
        this.$message.error('获取课堂失败')
        return []
      })
    },

    onSessionChange(sessionId) {
      this.formSessionId = sessionId || null
      if (!sessionId) {
        this.homeworkList = []
        this.selectedHomeworkId = null
        return
      }
      this.fetchHomeworkList(sessionId)
    },

    async fetchHomeworkList(sessionId) {
      if (!sessionId) {
        this.$message.info('请选择课堂以获取对应的作业列表')
        return
      }
      this.loading = true
      try {
        const res = await listHomework({ sessionId: sessionId, pageNum: 1, pageSize: 50 })
        this.homeworkList = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
        if (!this.homeworkList || this.homeworkList.length === 0) this.$message.info('当前课堂暂无可提交的作业，请等待教师发布')
      } catch (err) {
        console.error('获取作业列表失败', err)
        this.$message.error('获取作业列表失败')
        this.homeworkList = []
      } finally {
        this.loading = false
      }
    },

    chooseAnother() {
      this.homeworkId = null
      this.homework = {}
      this.selectedHomeworkId = null
      this.submissions = []
      this.submitted = false
      this.fileList = []
      this.uploadedFiles = []
      this.fetchHomeworkList()
    },

    loadSelectedHomework() {
      if (!this.selectedHomeworkId) return
      this.homeworkId = this.getValidHomeworkId(this.selectedHomeworkId)
      this.selectedHomeworkId = null
      this.init()
    },

    async init() {
      this.loading = true
      try {
        await this.fetchHomework()
        // ensure we also fetch current homework submissions so page shows data right away
        await this.fetchSubmissions()
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },

    async fetchHomework() {
      try {
        const res = await getHomework(this.homeworkId)
        this.homework = (res && (res.data || res.data === 0)) ? res.data : (res && res.title ? res : res)
        if (!this.homework || Object.keys(this.homework).length === 0) {
          this.homework = { homeworkDeleted: true, title: '该作业已被老师删除' }
        }
        // add debug log
        this.debugLogs.push(`fetchHomework: got homework for id=${this.homeworkId} -> ${JSON.stringify(this.homework)}`)
      } catch (err) {
        console.error('获取作业失败', err)
        this.debugLogs.push(`fetchHomework error: ${err && err.message ? err.message : err}`)
        this.$message.error('获取作业失败')
      }
    },

    // upload hooks
    uploadSuccess(response, file) {
      // robustly extract filename from a few possible response shapes
      try {
        if (!response) {
          this.$message.error('上传返回为空，请重试')
          return
        }
        // If backend returns an AjaxResult wrapper (code/msg/data)
        const payload = (response.data && (typeof response.data === 'object')) ? response.data : response
        // try multiple fields
        const nameFromResponse = payload.fileName || payload.filename || payload.name || payload.file || payload.data || null
        const name = nameFromResponse || (file && file.name)
        if (!name) {
          this.$message.error('上传后未返回文件名，可能上传失败')
          return
        }
        if (this.uploadedFiles.indexOf(name) === -1) this.uploadedFiles.push(name)
        const exists = this.fileList && this.fileList.some(f => (f.name === (file && file.name)) || (f.uid && file && file.uid && f.uid === file.uid))
        if (!exists && file) this.fileList = (this.fileList || []).concat(file)
      } catch (e) {
        console.error('uploadSuccess parse error', e)
        this.$message.error('上传解析失败')
      }
    },
    // for edit dialog uploads
    editUploadSuccess(response, file) {
      try {
        if (!response) { this.$message.error('上传返回为空，请重试'); return }
        const payload = (response.data && (typeof response.data === 'object')) ? response.data : response
        const nameFromResponse = payload.fileName || payload.filename || payload.name || payload.file || payload.data || null
        const name = nameFromResponse || (file && file.name)
        if (!name) { this.$message.error('上传后未返回文件名，可能上传失败'); return }
        if (this.editUploadedFiles.indexOf(name) === -1) this.editUploadedFiles.push(name)
        const exists = this.editFileList && this.editFileList.some(f => (f.name === (file && file.name)) || (f.uid && file && file.uid && f.uid === file.uid))
        if (!exists && file) this.editFileList = (this.editFileList || []).concat(file)
      } catch (e) {
        console.error('editUploadSuccess parse error', e)
        this.$message.error('上传解析失败')
      }
    },
    editHandleRemove(file, fileList) { this.editFileList = fileList || []; this.editUploadedFiles = (this.editFileList || []).map(f => this.getNameFromFileObj(f)).filter(Boolean) },
    handleRemove(file, fileList) { this.fileList = fileList || [] },
    beforeUpload(file) {
      const max = 50 * 1024 * 1024
      if (file.size > max) { this.$message.error('单个文件不能超过50MB'); return false }
      return true
    },

    getNameFromFileObj(f) {
      if (!f) return null
      if (typeof f === 'string') return f
      if (f.name) return f.name
      if (f.response) return f.response.fileName || f.response.filename || (f.response.data && f.response.data.fileName) || f.response.name || null
      if (f.url) { try { const u = decodeURIComponent(f.url); return u.split('/').pop().split('?')[0] } catch (e) { return f.url } }
      return null
    },

    parseAttachmentString(str) { if (!str) return []; return String(str).split(',').map(s => s.trim()).filter(Boolean) },

    // Helper: try to pull a list/rows array from various API response shapes
    extractListFromApi(resp) {
      if (!resp) return []
      // common shapes: { data: { rows: [...] } } or { rows: [...] } or { data: [...] } or []
      try {
        if (Array.isArray(resp)) return resp
        if (resp.rows && Array.isArray(resp.rows)) return resp.rows
        if (resp.data) {
          if (Array.isArray(resp.data)) return resp.data
          if (resp.data.rows && Array.isArray(resp.data.rows)) return resp.data.rows
        }
        // some backends wrap result under list or records
        if (resp.list && Array.isArray(resp.list)) return resp.list
        if (resp.records && Array.isArray(resp.records)) return resp.records
        // fallback: if resp has a 'result' field
        if (resp.result && Array.isArray(resp.result)) return resp.result
      } catch (e) {
        console.warn('extractListFromApi failed to parse response', e)
      }
      return []
    },

    // Helper: normalize/validate homeworkId (accept number or string); return null for invalid
    getValidHomeworkId(id) {
      if (id === null || id === undefined) return null
      // try to coerce to string/number
      const s = String(id).trim()
      if (s === '' || s === 'null' || s === 'undefined') return null
      return s
    },

    // Helper: format timestamps (accept number or string), return readable local string or empty
    formatTime(ts) {
      if (!ts && ts !== 0) return ''
      try {
        // if numeric string and seems seconds (10 digits), convert to ms
        let n = typeof ts === 'number' ? ts : Number(ts)
        if (Number.isNaN(n)) {
          // try Date parsing
          const d = new Date(ts)
          return isNaN(d.getTime()) ? '' : d.toLocaleString()
        }
        if (n > 0 && String(n).length === 10) n = n * 1000
        const d = new Date(n)
        if (isNaN(d.getTime())) return ''
        return d.toLocaleString()
      } catch (e) {
        return ''
      }
    },

    previewFile(fileName) {
      const prefix = (process.env && process.env.VUE_APP_BASE_API) ? process.env.VUE_APP_BASE_API : ''
      const url = `${prefix}/common/download?fileName=${encodeURIComponent(fileName)}`
      const request = require('@/utils/request').default
      request({ url, method: 'get', responseType: 'blob', headers: { isToken: true, Authorization: 'Bearer ' + getToken() } }).then(blobData => {
        try {
          const blob = blobData instanceof Blob ? blobData : new Blob([blobData])
          const mime = blob.type || ''
          const objectUrl = URL.createObjectURL(blob)
          if (mime.startsWith('image/') || mime === 'application/pdf') window.open(objectUrl, '_blank')
          else { const a = document.createElement('a'); a.href = objectUrl; a.download = fileName; document.body.appendChild(a); a.click(); document.body.removeChild(a) }
        } catch (e) { console.error('预览失败', e); this.$message.error('预览出错，请下载后查看') }
      }).catch(err => { console.error('下载/预览失败', err); this.$message.error('下载/预览失败') })
    },

    // submit flow
    async submit() {
      if (!this.homeworkId) { this.$message.error('未选择作业，无法提交'); return }
      if (this.homework && (this.homework.homeworkDeleted || this.homework.homework_deleted)) { this.$message.error('该作业已被老师删除，无法提交'); return }
      if (!this.uploadedFiles || this.uploadedFiles.length === 0) { this.$message.error('请先上传至少一个文件'); return }
      if (!this.studentNo) { this.$message.error('请输入学号'); return }

      // if student already has graded submission, disallow
      if (!this.editingSubmissionId && this.submissions && this.submissions.some(s => String(s.homeworkId) === String(this.homeworkId) && ((this.resolvedStudentId && String(s.studentId) === String(this.resolvedStudentId)) || (!this.resolvedStudentId && s.studentNo && String(s.studentNo) === String(this.studentNo))))) {
        this.$message.info('检测到您已提交过本作业，若要修改请选择左侧列表的"修改"按钮'); return
      }

      this.submitLoading = true
      try {
        const payload = {
          studentHomeworkId: this.editingSubmissionId,
          id: this.editingSubmissionId,
          homeworkId: this.homeworkId,
          homework_id: this.homeworkId,
          // student identifiers (send multiple variants to tolerate backend naming)
          studentNo: this.studentNo || '',
          student_no: this.studentNo || '',
          stuNo: this.studentNo || '',
          stu_no: this.studentNo || '',
          // include resolved student id when available
          studentId: this.resolvedStudentId || this.studentId || null,
          student_id: this.resolvedStudentId || this.studentId || null,
          studentName: this.studentName || '',
          submissionFiles: this.uploadedFiles.join(','),
          remark: ''
        }
        // debug: print outgoing payload so we can verify student_no is present in the request
        console.debug('submit payload:', payload)
        this.debugLogs.push(`submit payload: ${JSON.stringify(payload)}`)

        let res
        // Also send student identifiers as URL params to be tolerant to backends expecting form/query params
        const params = { student_no: this.studentNo || '', studentNo: this.studentNo || '', studentId: this.resolvedStudentId || this.studentId || null }
        if (this.editingSubmissionId) res = await updateSubmission(payload, params)
        else res = await submitHomework(payload, params)
        console.debug('submit response:', res)
        try { this.debugResp = `submit response:\n${JSON.stringify(res, null, 2)}` } catch (e) { this.debugResp = String(res) }
        const ok = (res && (res.code === 200 || res.code === 0 || res.success)) || res === 200
        if (ok) {
          this.$message.success('提交成功')
          this.resetUpload()
          await this.fetchSubmissions()
          await this.loadMySubmissions()
          // If backend didn't persist student_no, patch local list to show 学号 in UI
          const patched = this.patchLocalStudentNoForMySubmissions()
          if (patched) this.$message.info('注意：学号在后台未保存，已在本地显示以便查看（请联系管理员修复后台持久化）')
          this.submitted = true
          this.editingSubmissionId = null
        } else {
          this.$message.error((res && res.msg) || '提交失败')
        }
      } catch (err) {
        console.error('提交失败', err)
        this.$message.error('提交失败')
      } finally { this.submitLoading = false }
    },

    // override resetUpload to also clear edit-file lists
    resetUpload() { this.$refs.upload && this.$refs.upload.clearFiles && this.$refs.upload.clearFiles(); this.fileList = []; this.uploadedFiles = []; this.editFileList = []; this.editUploadedFiles = [] },

    // normalize a raw submission record to a consistent shape
    normalizeSubmission(raw) {
      if (!raw) return null
      const r = raw || {}
      const studentNo = (r.studentNo || r.student_no || r.stu_no || '')
      const studentName = (r.studentName || r.student_name || r.nickName || r.nick_name || '')
       const homeworkId = (r.homeworkId || r.homework_id || r.homework || null)
       const studentId = (r.studentId || r.student_id || r.student || null)
       const submissionFiles = (r.submissionFiles || r.submission_files || r.attachments || r.files || '')
       const homeworkDeleted = !!(r.homeworkDeleted || r.homework_deleted)
       const gradedTime = r.gradedTime || r.gradeTime || r.grade_time || r.graded_at || r.marked_at || r.update_time || r.updatedAt || null
       const isGraded = !!(gradedTime || r.score != null || r.grade != null || r.is_graded === 1 || String(r.status) === '2')
       return {
         ...r,
         studentHomeworkId: r.studentHomeworkId || r.student_homework_id || r.id || null,
         homeworkId: homeworkId,
         homeworkTitle: r.homeworkTitle || r.title || r.name || '',
         studentId: studentId,
         studentNo: studentNo ? String(studentNo).trim() : '',
         student_no: studentNo ? String(studentNo).trim() : '',
         studentName: studentName ? String(studentName).trim() : '',
         submissionFiles: submissionFiles || '',
         submitTime: r.submitTime || r.submit_time || r.create_time || null,
         gradedTime: gradedTime,
         is_graded: isGraded ? 1 : 0,
         score: r.score != null ? r.score : (r.grade != null ? r.grade : null),
         remark: r.remark || r.grade_comment || null,
         homeworkDeleted: homeworkDeleted,
         homework_deleted: homeworkDeleted
       }
     },

    // confirm student identity and load personal submissions
    async confirmStudentId() {
      this.studentName = (this.studentName || '').toString().trim()
      this.studentNo = (this.studentNo || '').toString().trim()
      // require both studentName and studentNo to proceed
      if (!this.studentName || !this.studentNo) { this.$message.error('请输入姓名和学号后再确认'); return }

      // Clear any previous submission/edit state: confirming identity must not auto-mark as submitted
      this.submitted = false
      this.editingSubmissionId = null
      // Keep uploadedFiles untouched so students don't lose selected files when they confirm

      // Attempt to load submissions and only mark confirmed on success
      try {
        if (this.homeworkId) await this.fetchSubmissions().catch(() => {})
        // non-silent so the user sees messages when they actively confirm
        await this.loadMySubmissions()
        // if we got any matched submissions or resolved id, mark confirmed
        if ((this.submissions && this.submissions.length > 0) || this.resolvedStudentId) {
          this.studentConfirmed = true
          this.$message.success('学号/姓名确认成功，已加载您的提交记录')
        } else {
          // even when none matched, still set confirmed so user can submit a new one if allowed
          this.studentConfirmed = true
          this.$message.info('未检索到已提交记录；您可以继续上传并提交作业')
        }
      } catch (e) {
        console.error('confirmStudentId error', e)
        if (e && e.response && e.response.data) {
          const d = e.response.data
          const serverMsg = d.msg || d.message || (typeof d === 'string' ? d : null)
          if (serverMsg) { this.$message.error(`确认失败：${serverMsg}`); return }
        }
        this.$message.error('确认学号/姓名失败，请重试')
        this.submissions = []
      }
    },

    async fetchSubmissions() {
      try {
        const hwId = this.getValidHomeworkId(this.homeworkId)
        if (!hwId) { this.allSubmissions = []; return }
        const res = await getSubmissions(hwId)
        this.debugLogs.push(`fetchSubmissions: api response for hwId=${hwId} -> ${JSON.stringify(res)}`)
        const rawAll = (res && (res.data || res.rows)) ? (res.data || res.rows) : (res || [])
        this.allSubmissions = (rawAll || []).map(s => this.normalizeSubmission(s))
        // update debugResp for visibility
        this.debugResp = this.debugLogs.concat([`allSubmissions count=${this.allSubmissions.length}`]).join('\n')
        // if the page already marked the student as confirmed, refresh their submissions silently
        if (this.studentConfirmed) await this.loadMySubmissions(true)
      } catch (err) { console.error('获取提交记录失败', err); this.debugLogs.push(`fetchSubmissions error: ${err && err.message ? err.message : err}`); this.$message.error('获取提交记录失败')
        // store debugResp internally but keep debug UI hidden
        try { this.debugResp = `fetchSubmissions error:\n${(err && err.response && (JSON.stringify(err.response.data) || err.response.statusText)) || err.message || err}` } catch (er) { this.debugResp = String(err) }
      }
    },

    async loadMySubmissions(silent = false) {
      if (!this.studentNo && !this.studentName) {
        if (!silent) this.$message.info('请输入学号或姓名以查看提交记录')
        return
      }

      // debounce short-circuit: prevent rapid repeated calls
      if (this._loadMySubmissionsTimer) { clearTimeout(this._loadMySubmissionsTimer); this._loadMySubmissionsTimer = null }
      // small debounce to collapse rapid inputs/calls
      await new Promise(resolve => { this._loadMySubmissionsTimer = setTimeout(resolve, 120) })

      // prevent concurrent execution
      if (this._loadingMySubmissions) { this.debugLogs.push('loadMySubmissions: call ignored because another load is in progress'); return }
      this._loadingMySubmissions = true

      const inputNo = (this.studentNo || '').toString().trim()
      const inputName = (this.studentName || '').toString().trim()

      // Try dedicated API first
      try {
        this.debugLogs.push(`loadMySubmissions: start - studentNo=${inputNo}, studentName=${inputName}, homeworkId=${this.homeworkId}`)
        console.log('loadMySubmissions start', { studentNo: inputNo, studentName: inputName, homeworkId: this.homeworkId })
        const res = await getStudentSubmissions(this.studentNo || this.studentName)
        this.debugLogs.push(`getStudentSubmissions response: ${JSON.stringify(res)}`)
        this.debugResp = JSON.stringify(res, null, 2)
        let list = this.extractListFromApi(res)

        // normalize all records
        list = (list || []).map(s => this.normalizeSubmission(s)).filter(Boolean)

        // fallback to allSubmissions if API returned empty
        if ((!list || list.length === 0) && this.allSubmissions && this.allSubmissions.length > 0) {
          this.debugLogs.push('getStudentSubmissions returned empty, falling back to allSubmissions')
          list = (this.allSubmissions || []).map(s => this.normalizeSubmission(s)).filter(Boolean)
        }

        // filter for current student: prefer id, then exact studentNo, then name contains
        let matched = (list || []).filter(s => {
          if (!s) return false;
          // If user provided a studentNo, require match by studentNo or resolvedStudentId only.
          if (inputNo) {
            if (this.resolvedStudentId && s.studentId && String(s.studentId) === String(this.resolvedStudentId)) return true;
            if (s.studentId && String(s.studentId) === String(inputNo)) return true;
            if (s.studentNo && String(s.studentNo) === String(inputNo)) return true;
            if (s.student_no && String(s.student_no) === String(inputNo)) return true;
            // do NOT fallback to name-match when studentNo provided (avoids false positives)
            return false;
          }
          // If no studentNo provided, allow matching by resolvedStudentId or name contains
          if (this.resolvedStudentId && s.studentId && String(s.studentId) === String(this.resolvedStudentId)) return true;
          if (inputName && s.studentName && (s.studentName === inputName || s.studentName.indexOf(inputName) !== -1)) return true;
          return false;
        })

        // Deduplicate by studentHomeworkId (or fallback composite key)
        const seen = new Set()
        const unique = []
        for (const s of matched) {
          const key = String(s.studentHomeworkId || `${s.homeworkId || ''}_${s.studentId || s.studentNo || ''}`)
          if (!seen.has(key)) { seen.add(key); unique.push(s) }
        }
        matched = unique

        this.debugLogs.push(`matched submissions count=${matched.length}`)
        console.log('loadMySubmissions matched', matched)

        this.submissions = matched
        // force table to re-render (handles some environment rendering glitches)
        this.tableKey = `submissions-${Date.now()}-${(this.submissions || []).length}`
        if (this.submissions && this.submissions.length > 0) {
          if (!silent) this.$message.success(`已加载 ${this.submissions.length} 条提交记录（仅显示您的记录）`)
          this.resolvedStudentId = this.submissions[0].studentId || null
          this.resolvedStudentName = this.submissions[0].studentName || null
          if (!this.studentName && this.resolvedStudentName) this.studentName = this.resolvedStudentName
        } else {
          // do not show debug dialog by default when auto-loading
          if (!silent) this.$message.info('未找到匹配的提交记录；如果您尚未提交，可直接上传并提交。')
        }

        // NOTE: Do NOT automatically set `submitted`/`editingSubmissionId` here.
        // The confirm action should only display the student's submissions; it must not change
        // submit state or disable the submit button. Users should upload files and click 提交 or
        // use the 列表的“修改”按钮 to edit an existing submission.

        // update debugResp with logs for easier troubleshooting
        this.debugResp = this.debugLogs.join('\n') + '\n\nAPI result:\n' + JSON.stringify(res, null, 2)

        return
      } catch (e) {
        console.warn('getStudentSubmissions failed, falling back to local filtering', e)
        this.debugLogs.push(`getStudentSubmissions failed: ${e && e.message ? e.message : e}`)
      } finally {
        this._loadingMySubmissions = false
      }

      // final fallback: fetch current homework submissions and filter locally
      try {
        const hwId = this.getValidHomeworkId(this.homeworkId)
        if (hwId) {
          const hwRes = await getSubmissions(hwId)
          let hwList = (hwRes && (hwRes.data || hwRes.rows)) ? (hwRes.data || hwRes.rows) : (hwRes || [])
          hwList = (hwList || []).map(s => this.normalizeSubmission(s)).filter(Boolean)
          const filtered = hwList.filter(s => {
            if (!s) return false
            if (inputNo && s.studentNo && String(s.studentNo) === String(inputNo)) return true
            if (inputName && s.studentName && (s.studentName === inputName || s.studentName.indexOf(inputName) !== -1)) return true
            return false
          })
          // dedupe filtered results
          const seen2 = new Set(); const unique2 = []
          for (const s of filtered) {
            const key = String(s.studentHomeworkId || `${s.homeworkId || ''}_${s.studentId || s.studentNo || ''}`)
            if (!seen2.has(key)) { seen2.add(key); unique2.push(s) }
          }
          this.submissions = unique2
          // force table refresh on fallback
          this.tableKey = `submissions-${Date.now()}-${(this.submissions || []).length}`
          if (this.submissions && this.submissions.length > 0) {
            this.resolvedStudentId = this.submissions[0].studentId || null
            this.resolvedStudentName = this.submissions[0].studentName || null
            if (!this.studentName && this.resolvedStudentName) this.studentName = this.resolvedStudentName
            // Do NOT auto-assign submitted/editingSubmissionId in fallback either.
             return
           }
        }
        this.$message.info('未找到与所填学号/姓名匹配的提交记录；若您确定已提交，请联系教师或尝试刷新页面。')
        this.submissions = []
      } catch (e2) {
        console.error('Fallback fetchSubmissions failed', e2)
        this.debugLogs.push(`Fallback fetchSubmissions failed: ${e2 && e2.message ? e2.message : e}`)
        this.$message.error('加载学生提交失败')
        this.submissions = []
      } finally {
        this._loadingMySubmissions = false
      }
     },

    editSubmission(row) {
      if (!row) return
      // Prevent editing if the homework was deleted by teacher
      if (row.homeworkDeleted || row.homework_deleted) { this.$message.error('该作业已被老师删除，无法修改'); return }
      // if student identity is confirmed, enforce own-submission restriction; otherwise allow editing unconfirmed entries (for convenience)
      if (this.studentConfirmed) {
        if (this.resolvedStudentId ? String(row.studentId) !== String(this.resolvedStudentId) : (row.studentNo && String(row.studentNo) !== String(this.studentNo))) {
          this.$message.error('只能编辑您自己的提交'); return
        }
      }

      this.editingSubmissionId = row.studentHomeworkId
      this.homeworkId = this.getValidHomeworkId(row.homeworkId)
      // Ensure the homework details are loaded (used for deadline checks); fetch asynchronously but still open dialog
      this.fetchHomework()
      // populate upload lists: show existing attachments in both main and edit contexts
      const attachments = this.parseAttachmentString(row.submissionFiles || row.attachments || row.files)
      this.uploadedFiles = attachments.slice()
      this.editUploadedFiles = attachments.slice()
      this.editFileList = []
      // populate remark from the existing row
      this.remark = row.remark || ''
      // Open edit dialog to allow modifications
      this.editDialogVisible = true
      this.$nextTick(() => this.$message.success('已打开修改窗口，您可以添加或删除附件，然后保存'))
    },

    closeEditDialog() {
      this.editDialogVisible = false
      this.editingSubmissionId = null
      this.editFileList = []
      this.editUploadedFiles = []
      this.remark = ''
    },

    canEditSubmission(row) {
      if (!row) return false
      // Disallow if homework itself was deleted
      if (row.homeworkDeleted || row.homework_deleted) return false
      // Disallow editing if the submission is already graded
      if (row.gradedTime || row.is_graded === 1 || (row.score !== null && row.score !== undefined)) return false
      // Disallow editing if past deadline
      // NOTE: allow editing even if past deadline per requirement
      return true
     },

    isPastDeadline(row) {
      if (!row) return false
      // prefer row.deadline if present
      let rowDeadline = row.deadline || null
      // fallback: if row.homeworkId matches current homeworkId, use this.homework.deadline
      if (!rowDeadline && row.homeworkId && String(row.homeworkId) === String(this.homeworkId) && this.homework && this.homework.deadline) rowDeadline = this.homework.deadline
      if (!rowDeadline) return false
      try {
        return new Date(rowDeadline).getTime() < Date.now()
      } catch (e) {
        return false
      }
    },

    async saveEditSubmission() {
      if (!this.editingSubmissionId) { this.$message.error('无效的提交记录'); return }
      // prefer files uploaded in edit dialog; fallback to main uploadedFiles
      const filesToSubmit = (this.editUploadedFiles && this.editUploadedFiles.length) ? this.editUploadedFiles : (this.uploadedFiles || [])
      if (!filesToSubmit || filesToSubmit.length === 0) { this.$message.error('请先上传至少一个文件'); return }
      this.editSubmitLoading = true
      try {
        // Build a payload shape tolerant to backend naming. Some backends expect 'id' instead of studentHomeworkId.
        const payload = {
          studentHomeworkId: this.editingSubmissionId,
          id: this.editingSubmissionId,
          homeworkId: this.homeworkId,
          homework_id: this.homeworkId,
          studentNo: this.studentNo || '',
          student_no: this.studentNo || '',
          stuNo: this.studentNo || '',
          stu_no: this.studentNo || '',
          studentId: this.resolvedStudentId || this.studentId || null,
          student_id: this.resolvedStudentId || this.studentId || null,
          studentName: this.studentName || '',
          submissionFiles: (filesToSubmit || []).join(','),
          attachments: (filesToSubmit || []).join(','),
          remark: this.remark || ''
        }

        // Log payload for debugging (will appear in browser console)
        console.debug('saveEditSubmission payload:', payload)
        const res = await updateSubmission(payload)
        console.debug('saveEditSubmission response:', res)

        // set debugResp for internal troubleshooting (hidden by default)
        try { this.debugResp = `updateSubmission response:\n${JSON.stringify(res, null, 2)}` } catch (e) { this.debugResp = String(res) }

        // success checker: adapt to multiple common shapes
        const ok = !!( (res && (res.code === 200 || res.code === 0 || res.success === true)) || res === 200 || (res && res.data && (res.data.updated || res.data.id)) )
        if (ok) {
          this.$message.success('修改提交成功')
          this.editDialogVisible = false
          this.resetUpload()
          await this.fetchSubmissions()
          await this.loadMySubmissions()
          // Patch local submissions if backend didn't persist student_no
          const patched2 = this.patchLocalStudentNoForMySubmissions()
          if (patched2) this.$message.info('注意：学号在后台未保存，已在本地显示以便查看（请联系管理员修复后台持久化）')
          this.submitted = true
          this.editingSubmissionId = null
          this.remark = ''
        } else {
          // try to surface server-provided message
          const serverMsg = (res && (res.msg || res.message)) || (res && res.data && (res.data.msg || res.data.message))
          console.warn('updateSubmission failed', res)
          if (serverMsg) this.$message.error(`修改提交失败：${serverMsg}`)
          else this.$message.error('修改提交失败，请查看控制台或联系管理员')
        }
      } catch (err) {
        console.error('修改提交失败', err)
        // if axios-style error with response, show response data
        const info = err && err.response && err.response.data ? (err.response.data.msg || err.response.data.message || JSON.stringify(err.response.data)) : (err && err.message ? err.message : String(err))
        try { this.debugResp = `updateSubmission error:\n${JSON.stringify(err && err.response ? err.response.data : err, null, 2)}` } catch (e) { this.debugResp = String(err) }
        this.$message.error(`修改提交失败：${info}`)
      } finally { this.editSubmitLoading = false }
    },

    // Determine whether the provided row belongs to the currently resolved student
    isRowOwn(row) {
      if (!row) return false
      // prefer resolved student id when available
      if (this.resolvedStudentId && row.studentId) return String(row.studentId) === String(this.resolvedStudentId)
      // fall back to comparing studentNo
      if (this.studentNo && row.studentNo) return String(row.studentNo) === String(this.studentNo)
      // sometimes studentId may be in studentId field but we only have studentNo input
      if (this.resolvedStudentId && row.studentId == null && row.studentNo) return String(row.studentNo) === String(this.studentNo)
      return false
    },

    // If backend did not persist student_no, patch local submissions so the UI shows the 学号
    patchLocalStudentNoForMySubmissions() {
      try {
        const myNo = (this.studentNo || '').toString().trim()
        if (!myNo) return false
        let updated = false
        if (!this.submissions || !Array.isArray(this.submissions)) return false
        this.submissions = (this.submissions || []).map(s => {
          if (!s) return s
          const copy = { ...s }
          // If student_no missing, but this looks like the current student's record, fill it in locally
          const isMatch = (copy.studentId && this.resolvedStudentId && String(copy.studentId) === String(this.resolvedStudentId))
            || (copy.studentNo && String(copy.studentNo) === myNo)
            || (!copy.studentNo && String(copy.homeworkId) === String(this.homeworkId))
          if ((!copy.student_no || copy.student_no === '') && isMatch) {
            copy.student_no = myNo
            copy.studentNo = myNo
            updated = true
          }
          return copy
        })
        if (updated) this.tableKey = `submissions-patched-${Date.now()}`
        return updated
      } catch (e) {
        console.warn('patchLocalStudentNoForMySubmissions failed', e)
        return false
      }
    },

    // Confirm and delete a student's submission record
    async confirmDelete(row) {
      if (!row || !row.studentHomeworkId) return
      try {
        await this.$confirm('确定要删除该提交记录吗？此操作无法撤销。', '删除确认', { type: 'warning' })
      } catch (e) { return }
      this.$loading && this.$loading({ text: '正在删除，请稍候' })
      try {
        const res = await deleteSubmission(row.studentHomeworkId)
        // handle common success shapes
        const ok = !!( (res && (res.code === 200 || res.code === 0 || res.success)) || res === 200 )
        if (ok) {
          this.$message.success('删除成功')
          await this.fetchSubmissions()
          await this.loadMySubmissions()
          return
        }
        this.$message.error((res && res.msg) || '删除失败')
      } catch (err) {
        console.error('删除提交失败', err)
        // Try to show server-provided message (common Axios error shapes)
        let info = '删除失败，请稍后重试'
        try {
          // If server returned 404, give a clearer user-facing hint
          if (err && err.response && err.response.status === 404) {
            info = '提交记录未找到（可能已被删除）'
          } else {
            if (err && err.response && err.response.data) {
              const data = err.response.data
              info = data.msg || data.message || JSON.stringify(data)
            } else if (err && err.message) {
              info = err.message
            }
          }
        } catch (e) { console.warn('confirmDelete error parse failed', e) }
        this.$message.error(`删除提交失败：${info}`)
      } finally {
        // close any global loading overlay
        try { const l = document.querySelector('.el-loading-mask'); if (l) l.remove() } catch (e) {}
      }
    },
  }
}
</script>

<style scoped>
.app-container {
  padding: 16px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.clearfix::after {
  content: '';
  display: table;
  clear: both;
}

.el-upload__tip {
  font-size: 12px;
  color: #999;
}

.submission-section {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 16px;
}

.submissions-card {
  margin-top: 16px;
}

.table-wrapper {
  overflow-x: auto;
}

.attachment-tag {
  background-color: #e1f5fe;
  border-color: #81d4fa;
  color: #01579b;
}

.attachment-link {
  text-decoration: none;
  color: inherit;
}

.el-descriptions-item label {
  color: #666;
  font-weight: 500;
}

.el-descriptions-item {
  padding: 8px 0;
}

.el-alert {
  margin-bottom: 12px;
}

.action-buttons {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
}

.action-slot {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-button-placeholder {
  flex: 1;
  height: 32px;
}
</style>
