<template>
  <div class="app-container homework-upload-page">
      <div slot="header" class="clearfix">
        <span style="line-height:36px;font-weight:600">作业提交</span>
        <el-button v-if="homeworkId" style="float:right;margin-top:4px" size="small" type="text" @click="chooseAnother">选择其他作业</el-button>
      </div>

      <!-- If no homeworkId provided, let user pick one -->
      <div v-if="!homeworkId && !loading" style="margin-bottom:12px">
        <el-alert title="未选择作业，请先选择课程和课堂，然后从下拉列表选择要提交的作业" type="warning" show-icon />
        <div style="display:flex;gap:8px;align-items:center;margin-top:8px">
          <el-select v-model="formCourseId" placeholder="选择课程" filterable @change="onCourseChange" style="min-width:260px">
            <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
          </el-select>
          <el-select v-model="formSessionId" placeholder="选择课堂" filterable @change="onSessionChange" style="min-width:260px">
            <el-option v-for="s in sessions" :key="s.sessionId" :label="s.className" :value="s.sessionId" />
          </el-select>
          <el-select v-model="selectedHomeworkId" placeholder="选择作业" style="min-width:340px">
            <el-option v-for="h in homeworkList" :key="h.homeworkId" :label="h.title" :value="h.homeworkId" />
          </el-select>
          <el-button type="primary" @click="loadSelectedHomework" :disabled="!selectedHomeworkId">加载作业</el-button>
          <el-button @click="fetchHomeworkList(formSessionId)">刷新列表</el-button>
        </div>
      </div>

      <div v-if="loading" style="text-align:center;padding:40px 0">
        <div style="display:flex;flex-direction:column;align-items:center;">
          <i class="el-icon-loading" style="font-size:28px;margin-bottom:8px;color:#409EFF"></i>
          <div>加载中...</div>
        </div>
      </div>

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
          <!-- 学号输入 -->
          <div style="margin-bottom:12px; display:flex; gap:8px; align-items:center">
            <el-input v-model="studentName" placeholder="请输入姓名" style="width:200px"></el-input>
            <el-input v-model="studentNo" placeholder="请输入学号" style="width:260px"></el-input>
            <el-button type="primary" size="mini" @click="confirmStudentId">确认学号</el-button>
            <div style="color:#888">(请先填写姓名和学号；确认后将显示您已提交的作业，并允许修改自己的提交)</div>
          </div>

          <el-alert v-if="submitted" title="您已提交作业" type="success" show-icon></el-alert>
          <el-alert v-else title="尚未提交" type="info" show-icon></el-alert>

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

            <!-- 已选/已上传的文件列表（可预览） -->
            <div v-if="uploadedFiles.length" style="margin-top:8px">
              <div style="font-weight:600;margin-bottom:6px">已选择文件：</div>
              <div>
                <el-tag v-for="(f, i) in uploadedFiles" :key="i" @click.native.prevent="previewFile(f)" style="cursor:pointer;margin-right:8px">{{ f }}</el-tag>
              </div>
            </div>
          </div>

          <div style="margin-bottom:12px">
            <el-button type="primary" :disabled="submitDisabled" @click="submit" :loading="submitLoading">提交作业</el-button>
            <el-button @click="resetUpload">清空</el-button>
            <el-button type="warning" @click="chooseAnother" style="margin-left:8px">选择其他作业</el-button>
            <el-button @click="$router.back()">返回</el-button>
          </div>

          <el-card shadow="never">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px">
              <div style="font-weight:600">提交记录</div>
              <div style="font-size:12px;color:#888">您的姓名：{{ studentName || '未填写' }} &nbsp; 学号：{{ studentNo || '未填写' }}</div>
            </div>
            <el-table :data="submissions" stripe style="width:100%">
              <el-table-column label="作业" width="220">
                <template #default="{ row }">
                  {{ row.homeworkTitle ? row.homeworkTitle : (row.homeworkId ? '作业#' + row.homeworkId : '—') }}
                </template>
              </el-table-column>

              <el-table-column label="学生" width="160">
                <template #default="{ row }">
                  {{ row.studentName ? row.studentName : (row.studentId ? row.studentId : '—') }}
                </template>
              </el-table-column>
              <el-table-column prop="attachments" label="附件" >
                <template #default="{ row }">
                  <div v-if="row.submissionFiles || row.attachments || row.files">
                    <el-tag v-for="(f, i) in parseAttachmentString(row.submissionFiles || row.attachments || row.files)" :key="i" style="margin-right:6px;cursor:pointer">
                      <a href="javascript:void(0)" @click.prevent="previewFile(f)">{{ f }}</a>
                    </el-tag>
                  </div>
                  <div v-else>无</div>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="提交时间" width="200">
                <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
              </el-table-column>
              <!-- 状态列已移除：学生端提交记录不再显示状态 -->

              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="mini" type="primary" v-if="studentConfirmed && String(row.studentId) === String(resolvedStudentId || studentId || '')" @click="editSubmission(row)">修改</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
          <div style="margin-top:8px">
            <el-button size="small" type="primary" @click="loadMySubmissions" :disabled="!studentNo">刷新我的提交</el-button>
          </div>
        </div>
      </div>

      <!-- Edit submission dialog -->
      <el-dialog title="修改提交附件" :visible.sync="editDialogVisible" width="640px">
        <div>
          <div style="margin-bottom:8px">当前附件：</div>
          <div v-if="editUploadedFiles.length">
            <el-tag v-for="(f, i) in editUploadedFiles" :key="i" style="margin-right:8px;cursor:pointer">
              <a href="javascript:void(0)" @click.prevent="previewFile(f)">{{ f }}</a>
            </el-tag>
          </div>
          <div v-else style="color:#888;margin-bottom:8px">无附件</div>

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
            <el-button size="small" type="primary">选择文件（修改）</el-button>
            <div slot="tip" class="el-upload__tip">可添加或替换附件，上传文件将追加到当前附件列表。</div>
          </el-upload>
        </div>

        <span slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitLoading" @click="saveEditSubmission">保存并重新提交</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { getHomework, submitHomework, getSubmissions, listHomework, getStudentSubmissions, updateSubmission } from '@/api/proj_lwj/homework'
import { listCourse } from '@/api/proj_lw/course'
import { getToken } from '@/utils/auth'

export default {
  name: 'HomeworkUpload',
  data() {
    return {
      editingSubmissionId: null,
      studentConfirmed: false,
      courses: [],
      sessions: [],
      loading: true,
      homeworkId: null,
      homeworkList: [],
      selectedHomeworkId: null,
      homework: {},
      fileList: // el-upload fileList
      [],
      uploadedFiles: // array of filenames returned by server
      [],
      studentName: '',
      studentNo: '', // 学号，提交时必填
      submitLoading: false,
      submissions: [],
      submitted: false,
      // upload config
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      headers: { Authorization: 'Bearer ' + getToken() },
      formCourseId: null,
      formSessionId: null,

      // --- new fields for edit dialog ---
      editDialogVisible: false,
      editFileList: [],
      editUploadedFiles: [],
      editSubmitLoading: false,
      resolvedStudentId: null, // student_id returned from backend after resolving studentNo
      resolvedStudentName: null,
    }
  },
  computed: {
    submitDisabled() {
      // require studentNo and at least one uploaded file
      return this.submitted || this.uploadedFiles.length === 0 || this.submitLoading || !this.studentNo || !this.homeworkId
    },
    parsedHomeworkAttachments() {
      return this.parseAttachmentString(this.homework.attachments)
    }
  },
  watch: {
    // Keep uploadedFiles in sync with el-upload's fileList (covers cases where upload response doesn't return fileName)
    fileList(newList) {
      this.uploadedFiles = (newList || []).map(f => this.getNameFromFileObj(f)).filter(Boolean)
    },
    // keep editUploadedFiles in sync when editFileList changes
    editFileList(newList) {
      this.editUploadedFiles = (newList || []).map(f => this.getNameFromFileObj(f)).filter(Boolean)
    }
  },
  created() {
    this.homeworkId = this.$route.query.homeworkId || null
    // always fetch courses first; user must choose course -> session -> homework
    this.fetchCourses().then(() => {
      if (!this.homeworkId) {
        this.loading = false
      } else {
        this.init()
      }
    })
  },
  methods: {
    async fetchCourses() {
      try {
        const res = await listCourse({ pageNum: 1, pageSize: 1000 })
        this.courses = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
      } catch (e) {
        console.error('fetchCourses error', e)
        this.courses = []
      }
    },

    // reuse session API to get sessions by classNumber
    $apiGetSessionsByClassNumber(classNumber) {
      const api = require('@/api/proj_lw/session')
      // Try the direct byClassNumber endpoint first
      try {
        const num = Number(classNumber)
        const param = Number.isNaN(num) ? classNumber : num
        // debug
        // console.debug('[HomeworkUpload] fetching sessions by classNumber:', param)
        return api.getSessionsByClassNumber(param).then(res => {
           this.sessions = res && res.data ? res.data : []
          // If empty, try the list endpoint with classNumber as param (fallback)
          if ((!this.sessions || this.sessions.length === 0) && api.listSession) {
            return api.listSession({ classNumber }).then(r2 => {
              this.sessions = r2 && r2.rows ? r2.rows : (r2 && r2.data ? r2.data : (r2 || []))
              if (!this.sessions || this.sessions.length === 0) {
                this.$message.info('未查询到对应课堂')
              }
              return this.sessions
            }).catch(e2 => {
              console.error('fallback listSession error', e2)
              // keep sessions as []
              this.sessions = this.sessions || []
              return this.sessions
            })
          }
          if (!this.sessions || this.sessions.length === 0) {
            this.$message.info('未查询到对应课堂')
          }
          return this.sessions
        }).catch(err => {
          console.error('getSessionsByClassNumber error', err)
          // fallback to listSession if available
          if (api.listSession) {
            return api.listSession({ classNumber }).then(r2 => {
              this.sessions = r2 && r2.rows ? r2.rows : (r2 && r2.data ? r2.data : (r2 || []))
              if (!this.sessions || this.sessions.length === 0) this.$message.info('未查询到对应课堂')
              return this.sessions
            }).catch(e2 => {
              console.error('fallback listSession error', e2)
              this.sessions = []
              this.$message.error('获取课堂失败，请检查后端接口或权限')
              return []
            })
          }
          this.sessions = []
          this.$message.error('获取课堂失败，请检查后端接口或权限')
          return []
        })
      } catch (e) {
        console.error('unexpected error in $apiGetSessionsByClassNumber', e)
        this.sessions = []
        return Promise.resolve([])
      }
    },

    onCourseChange() {
      // When user selects a course, fetch sessions by its classNumber
      if (!this.formCourseId) {
        this.sessions = []
        this.formSessionId = null
        this.homeworkList = []
        return
      }
      const selected = this.courses.find(c => c.courseId === this.formCourseId)
      // require classNumber (班级编号) to query classrooms
      const classNumber = selected ? selected.classNumber : null
      if (classNumber == null) {
        this.sessions = []
        this.formSessionId = null
        this.homeworkList = []
        this.$message.error('所选课程没有班级编号(classNumber)，无法查询课堂，请在课程管理中设置班级编号')
        return
      }
      const num = Number(classNumber)
      if (Number.isNaN(num)) {
        this.$message.error('课程的班级编号(classNumber)不是有效数字，无法查询课堂')
        this.sessions = []
        this.formSessionId = null
        this.homeworkList = []
        return
      }
       if (classNumber != null) {
         this.sessions = []
         this.$apiGetSessionsByClassNumber(num).then(() => {
           // clear session selection after loading
           this.formSessionId = null
           this.homeworkList = []
         })
       } else {
         this.sessions = []
         this.formSessionId = null
         this.homeworkList = []
       }
     },

    // Called when user selects a session in the student upload page
    onSessionChange(sessionId) {
      // set formSessionId and load homeworks for this session
      this.formSessionId = sessionId || null
      if (!sessionId) {
        this.homeworkList = []
        this.selectedHomeworkId = null
        return
      }
      // load homework list filtered by sessionId
      this.fetchHomeworkList(sessionId)
    },

    // fetch homeworks filtered by sessionId
    async fetchHomeworkList(sessionId) {
      if (!sessionId) {
        this.$message.info('请选择课堂以获取对应的作业列表')
        return
      }
      this.loading = true
      try {
        const res = await listHomework({ sessionId: sessionId, pageNum: 1, pageSize: 50 })
        this.homeworkList = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
        if (!this.homeworkList || this.homeworkList.length === 0) {
          this.$message.info('当前课堂暂无可提交的作业，请等待教师发布')
        }
      } catch (err) {
        console.error('获取作业列表失败，error object:', err)
        let userMsg = '获取作业列表失败'
        try {
          if (err && err.response) {
            const data = err.response.data
            if (data && (data.msg || data.message)) userMsg = `获取作业列表失败：${data.msg || data.message}`
            else if (typeof data === 'string') userMsg = `获取作业列表失败：${data}`
            else userMsg = `获取作业列表失败（HTTP ${err.response.status}）`
          } else if (err && err.message) {
            userMsg = `获取作业列表失败：${err.message}`
          }
        } catch (e) { console.error('解析错误信息失败', e) }
        this.$message.error(userMsg)
        this.homeworkList = []
      } finally {
        this.loading = false
      }
    },

    chooseAnother() {
      // clear current homework and show the selection list
      this.homeworkId = null
      this.homework = {}
      this.selectedHomeworkId = null
      this.submissions = []
      this.submitted = false
      this.fileList = []
      this.uploadedFiles = []
      // fetch list again
      this.fetchHomeworkList()
    },

    loadSelectedHomework() {
      if (!this.selectedHomeworkId) return
      this.homeworkId = this.selectedHomeworkId
      this.selectedHomeworkId = null
      this.init()
    },

    async init() {
      this.loading = true
      try {
        await this.fetchHomework()
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
        // res may be { code, data } or the data object itself depending on interceptor
        this.homework = (res && (res.data || res.data === 0)) ? res.data : (res && res.data === undefined && res.title ? res : res)
      } catch (err) {
        console.error('获取作业失败', err)
        this.$message.error('获取作业失败')
      }
    },
    async fetchSubmissions() {
      try {
        const res = await getSubmissions(this.homeworkId)
        this.submissions = (res && (res.data || res.rows)) ? (res.data || res.rows) : (res || [])
        const currentUserId = this.$store && this.$store.state && this.$store.state.user ? this.$store.state.user.userId : null
        this.submitted = this.submissions.some(s => s.isCurrentUser || s.status === 'submitted' || (s.studentId && currentUserId && s.studentId === currentUserId))
      } catch (err) {
        console.error('获取提交记录失败', err)
        this.$message.error('获取提交记录失败')
      }
    },
    uploadSuccess(response, file) {
      // response expected to contain fileName or url; fallback to file.name
      if (!response && !file) return
      const nameFromResponse = response && (response.fileName || response.data || response.filename || response.name || (response.data && response.data.fileName))
      const name = nameFromResponse || (file && file.name)
      if (name) {
        // dedupe
        if (this.uploadedFiles.indexOf(name) === -1) this.uploadedFiles.push(name)
      }
      // Ensure fileList contains the file object so UI shows currently uploaded files and watcher works
      try {
        const exists = this.fileList && this.fileList.some(f => (f.name === (file && file.name)) || (f.uid && file && file.uid && f.uid === file.uid))
        if (!exists && file) {
          // push the file object returned by el-upload to fileList
          this.fileList = (this.fileList || []).slice() // clone
          this.fileList.push(file)
        }
      } catch (e) {
        // ignore
      }
    },
    getNameFromFileObj(f) {
      if (!f) return null
      if (typeof f === 'string') return f
      // el-upload file object
      if (f.name) return f.name
      if (f.response) {
        return f.response.fileName || f.response.filename || (f.response.data && f.response.data.fileName) || f.response.name || null
      }
      if (f.url) {
        // try to extract filename from url
        try {
          const u = decodeURIComponent(f.url)
          return u.split('/').pop().split('?')[0]
        } catch (e) {
          return f.url
        }
      }
      return null
    },
    handleRemove(file, fileList) {
      // sync local fileList and uploadedFiles
      this.fileList = fileList || []
      // uploadedFiles will be synced by watcher on fileList
    },
    beforeUpload(file) {
      // small client-side checks (optional): file size limit 50MB
      const max = 50 * 1024 * 1024
      if (file.size > max) {
        this.$message.error('单个文件不能超过50MB')
        return false
      }
      return true
    },
    parseAttachmentString(str) {
      if (!str) return []
      return String(str).split(',').map(s => s.trim()).filter(Boolean)
    },
    previewFile(fileName) {
      // Try to fetch the file via the app's request instance so Authorization header is attached
      // If the file is previewable (image/pdf) open as blob URL in new tab; otherwise trigger download
      const prefix = (process.env && process.env.VUE_APP_BASE_API) ? process.env.VUE_APP_BASE_API : ''
      const url = `${prefix}/common/download?fileName=${encodeURIComponent(fileName)}`
      // Use the project's request util to include token and proper baseURL
      // import on-demand to avoid circular issues
      const request = require('@/utils/request').default
      request({ url: url, method: 'get', responseType: 'blob', headers: { 'isToken': true } }).then(blobData => {
        try {
          // blobData might be a raw blob or an object depending on axios interceptor
          const blob = blobData instanceof Blob ? blobData : new Blob([blobData])
          const mime = blob.type || ''
          const objectUrl = URL.createObjectURL(blob)
          // if it's an image or pdf, open in new tab for preview
          if (mime.startsWith('image/') || mime === 'application/pdf') {
            window.open(objectUrl, '_blank')
          } else {
            // force download
            const a = document.createElement('a')
            a.href = objectUrl
            a.download = fileName
            document.body.appendChild(a)
            a.click()
            document.body.removeChild(a)
          }
        } catch (e) {
          console.error('预览失败', e)
          this.$message.error('预览出错，请下载后查看')
        }
      }).catch(err => {
        console.error('下载/预览失败', err)
        // show specific 401 message returned by backend
        if (err && err.response && err.response.data) {
          try {
            // try to parse ajaxResult JSON from blob
            const reader = new FileReader()
            reader.onload = () => {
              try {
                const txt = reader.result
                const json = JSON.parse(txt)
                if (json && json.code === 401) {
                  this.$message.error(json.msg || '认证失败，无法访问资源，请重新登录')
                  return
                }
              } catch (e) {
                // ignore parse errors
              }
              this.$message.error('下载失败')
            }
            reader.readAsText(err.response.data)
          } catch (e) {
            this.$message.error('下载失败')
          }
        } else if (err && err.status === 401) {
          this.$message.error('认证失败，无法访问系统资源，请先登录')
        } else {
          this.$message.error('下载/预览失败')
        }
      })
    },
    submissionStatusText(status) {
      if (status === 1 || status === 'submitted') return '已提交'
      if (status === 2 || status === 'graded') return '已批改'
      return '未提交'
    },
    resetUpload() {
      this.$refs.upload && this.$refs.upload.clearFiles && this.$refs.upload.clearFiles()
      this.fileList = []
      this.uploadedFiles = []
    },
    formatTime(val) {
      if (!val) return ''
      try {
        const d = new Date(val)
        if (isNaN(d.getTime())) return String(val)
        return d.toLocaleString()
      } catch (e) {
        return String(val)
      }
    },
    async submit() {
      // validations
      if (!this.homeworkId) {
        this.$message.error('未选择作业，无法提交')
        return
      }
      // If editing an existing submission, allow update; otherwise prevent duplicate submission for same homework
      // consider either resolvedStudentId (numeric) or studentNo (string) in submissions
      if (!this.editingSubmissionId && this.submissions && this.submissions.some(s => Number(s.homeworkId) === Number(this.homeworkId) && ((this.resolvedStudentId && String(s.studentId) === String(this.resolvedStudentId)) || (!this.resolvedStudentId && s.studentNo && String(s.studentNo) === String(this.studentNo))))) {
        this.$message.info('检测到您已提交过本作业，若要修改请选择左侧列表的“修改”按钮')
        return
      }
      if (!this.uploadedFiles || this.uploadedFiles.length === 0) {
        this.$message.error('请先上传至少一个文件')
        return
      }
      if (!this.studentNo) {
        this.$message.error('请输入学号')
        return
      }
      this.submitLoading = true
      try {
        // backend expects ClassStudentHomework: homeworkId, studentNo (we send studentNo), submissionFiles
        const payload = {
          studentHomeworkId: this.editingSubmissionId,
           homeworkId: this.homeworkId,
           // send studentNo (学号) so backend can resolve to studentId
           studentNo: this.studentNo,
           studentName: this.studentName,
           submissionFiles: this.uploadedFiles.join(','),
           remark: ''
         }
        let res
        if (this.editingSubmissionId) {
          res = await updateSubmission(payload)
        } else {
          res = await submitHomework(payload)
        }
         const ok = (res && (res.code === 200 || res.code === 0 || res.success)) || res === 200
         if (ok) {
           this.$message.success('提交成功')
           this.resetUpload()
           // reload submissions for current homework and for this student
           await this.fetchSubmissions()
           await this.loadMySubmissions()
           this.submitted = true
           // clear editing state after successful update
           this.editingSubmissionId = null
         } else {
           this.$message.error((res && res.msg) || '提交失败')
         }
       } catch (err) {
         console.error('提交失败', err)
         this.$message.error('提交失败')
       } finally {
         this.submitLoading = false
       }
     },
     onSessionChange(sessionId) {
       // set formSessionId and load homeworks for this session
       this.formSessionId = sessionId || null
       if (!sessionId) {
         this.homeworkList = []
         this.selectedHomeworkId = null
         return
       }
       // load homework list filtered by sessionId
       this.fetchHomeworkList(sessionId)
     },

     // Confirm the entered studentId and load only that student's submissions
     async confirmStudentId() {
       if (!this.studentName) {
         this.$message.error('请输入姓名后再确认')
         return
       }
       if (!this.studentNo) {
         this.$message.error('请输入学号后再确认')
         return
       }
       this.studentConfirmed = true
       await this.loadMySubmissions()
     },

     // Load submissions for the current entered studentId
     async loadMySubmissions() {
       if (!this.studentNo) {
         this.$message.info('请输入学号以查看提交记录')
         return
       }
       try {
         const res = await getStudentSubmissions(this.studentNo)
         const list = (res && (res.data || res.rows)) ? (res.data || res.rows) : (res || [])
         // Only show this student's submissions after confirmation
         this.submissions = list
        // backend may include resolved studentId in each row or return associated studentId
        // record resolvedStudentId if available (first row)
        if (this.submissions && this.submissions.length > 0) {
          this.resolvedStudentId = this.submissions[0].studentId || null
          this.resolvedStudentName = this.submissions[0].studentName || null
          // if studentName input is empty, prefill it with resolved name
          if (!this.studentName && this.resolvedStudentName) this.studentName = this.resolvedStudentName
        }
         // If current homework has a submission by this student, mark submitted
         if (this.homeworkId) {
           const mine = this.submissions.find(s => String(s.homeworkId) === String(this.homeworkId) && ((this.resolvedStudentId && String(s.studentId) === String(this.resolvedStudentId)) || (!this.resolvedStudentId && s.studentNo && String(s.studentNo) === String(this.studentNo))))
           if (mine) {
             this.submitted = true
             this.editingSubmissionId = mine.studentHomeworkId || null
           } else {
             this.submitted = false
             this.editingSubmissionId = null
           }
         }
       } catch (e) {
         console.error('加载学生提交失败', e)
         this.$message.error('加载学生提交失败')
         this.submissions = []
       }
     },

     // Load a submission into the editing area so the student can modify it
     editSubmission(row) {
       if (!row) return
       // Only allow editing if the row belongs to current confirmed student
       if (!this.studentConfirmed || (this.resolvedStudentId ? String(row.studentId) !== String(this.resolvedStudentId) : (row.studentNo && String(row.studentNo) !== String(this.studentNo)))) {
         this.$message.error('只能编辑您自己的提交')
         return
       }
       // Open dialog to edit attachments
       this.editingSubmissionId = row.studentHomeworkId
       this.homeworkId = row.homeworkId
       // load homework details
       this.fetchHomework()
       // populate editUploadedFiles from submissionFiles
       this.uploadedFiles = this.parseAttachmentString(row.submissionFiles || row.attachments || row.files)
       this.editFileList = []
       this.editDialogVisible = true
       this.$nextTick(() => {
         this.$message.success('已打开修改窗口，您可以添加或删除附件，然后保存')
       })
     },

     // Handlers for edit dialog upload
     editUploadSuccess(response, file) {
      if (!response && !file) return
      const nameFromResponse = response && (response.fileName || response.data || response.filename || response.name || (response.data && response.data.fileName))
      const name = nameFromResponse || (file && file.name)
      if (name) {
        if (this.editUploadedFiles.indexOf(name) === -1) this.editUploadedFiles.push(name)
      }
      try {
        const exists = this.editFileList && this.editFileList.some(f => (f.name === (file && file.name)) || (f.uid && file && file.uid && f.uid === file.uid))
        if (!exists && file) {
          this.editFileList = (this.editFileList || []).slice()
          this.editFileList.push(file)
        }
      } catch (e) { }
    },
    editHandleRemove(file, fileList) {
      this.editFileList = fileList || []
      // editUploadedFiles will be synced by watcher on editFileList
    },

    // Save edited submission (update)
    async saveEditSubmission() {
      if (!this.editingSubmissionId) {
        this.$message.error('编辑的提交ID缺失')
        return
      }
      if (!this.studentNo) {
        this.$message.error('请输入学号后再保存')
        return
      }
      if (!this.editUploadedFiles || this.editUploadedFiles.length === 0) {
        this.$message.error('请至少保留或上传一个附件')
        return
      }
      this.editSubmitLoading = true
      try {
        const payload = {
          studentHomeworkId: this.editingSubmissionId,
          homeworkId: this.homeworkId,
          // send studentNo and studentName so backend can resolve/update
          studentNo: this.studentNo,
          studentName: this.studentName,
          submissionFiles: this.editUploadedFiles.join(',')
        }
        const res = await updateSubmission(payload)
        const ok = (res && (res.code === 200 || res.code === 0 || res.success)) || res === 200
        if (ok) {
          this.$message.success('保存并重新提交成功')
          this.editDialogVisible = false
          // refresh lists
          await this.fetchSubmissions()
          await this.loadMySubmissions()
          // clear editing state
          this.editingSubmissionId = null
          this.editFileList = []
          this.editUploadedFiles = []
        } else {
          this.$message.error((res && res.msg) || '保存失败')
        }
      } catch (e) {
        console.error('保存提交失败', e)
        this.$message.error('保存失败')
      } finally {
        this.editSubmitLoading = false
      }
    },
   }
 }
 </script>
