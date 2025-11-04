<template>
  <div class="app-container">
    <el-form :model="form" label-width="100px">
      <el-form-item label="课程">
        <el-select v-model="form.courseId" placeholder="请选择课程" filterable>
          <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
        </el-select>
      </el-form-item>
      <el-form-item label="课堂">
        <el-select v-model="form.sessionId" placeholder="请选择课堂">
          <el-option v-for="s in sessions" :key="s.sessionId" :label="s.className" :value="s.sessionId" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input type="textarea" v-model="form.content" />
      </el-form-item>
      <el-form-item label="分值">
        <el-input v-model.number="form.totalScore" />
      </el-form-item>
      <el-form-item label="截止时间">
        <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择日期时间" style="width: 100%" />
      </el-form-item>
      <el-form-item label="附件">
        <el-upload :action="uploadUrl" :headers="headers" name="file" :on-success="uploadSuccess" :multiple="true">
          <el-button size="small" type="primary">上传参考文件</el-button>
        </el-upload>
        <div v-if="form.attachments">已上传: {{ form.attachments }}</div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="publishOrSave" :loading="publishLoading">{{ editing ? '保存' : '发布' }}</el-button>
        <el-button v-if="editing" type="info" @click="cancelEdit" style="margin-left:8px">取消编辑</el-button>
      </el-form-item>
    </el-form>

    <!-- Published homeworks list for the selected classroom -->
    <div style="margin-top:24px">
      <el-card>
        <div slot="header" style="display:flex;align-items:center;justify-content:space-between">
          <span>已发布作业（当前课堂）</span>
          <div>
            <el-button size="small" type="primary" @click="resetForm">发布新作业</el-button>
          </div>
        </div>

        <div v-if="!form.sessionId" style="padding:16px">请选择课堂以查看已发布作业</div>
        <div v-else>
          <el-table :data="homeworkList" style="width:100%" v-loading="listLoading">
            <el-table-column prop="title" label="标题" />
            <el-table-column label="截止时间">
              <template slot-scope="scope">
                {{ formatTime(scope.row.deadline) || '—' }}
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="分值" width="80" />
            <el-table-column label="附件">
              <template slot-scope="scope">
                <div v-if="scope.row.attachments">
                  <a v-for="(f, idx) in parseAttachments(scope.row.attachments)" :key="idx" :href="downloadUrl(f)" target="_blank" style="margin-right:8px">{{ f }}</a>
                </div>
                <div v-else>—</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="startEdit(scope.row)">修改</el-button>
                <el-button size="mini" type="danger" @click="confirmDelete(scope.row)" style="margin-left:6px">删除</el-button>
                <el-button size="mini" @click="viewSubmissions(scope.row)" style="margin-left:6px">查看提交</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="(!homeworkList || homeworkList.length === 0) && !listLoading" style="padding:12px">当前课堂暂无已发布作业</div>
        </div>
      </el-card>
    </div>

    <!-- Submissions dialog -->
    <el-dialog :visible.sync="subDialogVisible" width="800px" :before-close="() => (subDialogVisible = false)">
      <span slot="title">作业提交详情 - {{ subDialogTitle }}</span>
      <div>
        <el-table :data="submissions" style="width:100%">
          <el-table-column prop="studentId" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="submissionFiles" label="附件">
            <template slot-scope="scope">
              <div v-if="scope.row.submissionFiles">
                <a v-for="(f, idx) in parseAttachments(scope.row.submissionFiles)" :key="idx" :href="downloadUrl(f)" target="_blank" style="margin-right:8px">{{ f }}</a>
              </div>
              <div v-else>—</div>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="180">
            <template slot-scope="scope">{{ formatTime(scope.row.submitTime) || '—' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" />
        </el-table>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="subDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="exportSubmissions" :disabled="!submissions || submissions.length===0">导出</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listCourse } from '@/api/proj_lw/course'
import { addHomework, listHomework, updateHomework, delHomework, getSubmissions } from '@/api/proj_lwj/homework'
import { listStudentsByCourseAndSession } from '@/api/proj_lw/classStudent'

export default {
  name: 'HomeworkPublish',
  data() {
    return {
      courses: [],
      sessions: [],
      form: {
        courseId: null,
        sessionId: null,
        title: '',
        content: '',
        totalScore: 100,
        deadline: null,
        attachments: ''
      },
      editing: false,
      editingId: null,
      // upload config for backend
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      headers: { Authorization: 'Bearer ' + (require('@/utils/auth').getToken()) },
      homeworkList: [],
      listLoading: false,
      publishLoading: false,
      subDialogVisible: false,
      subDialogTitle: '',
      submissions: []
    }
  },
  created() {
    this.fetchCourses()
  },
  watch: {
    'form.courseId'(val) {
      if (val) {
        this.fetchSessionsByCourseId(val)
      } else {
        this.sessions = []
        this.form.sessionId = null
      }
    },
    'form.sessionId'(val) {
      if (val) {
        this.loadHomeworks(val)
      } else {
        this.homeworkList = []
      }
    }
  },
  methods: {
    fetchCourses() {
      listCourse({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.courses = response && response.rows ? response.rows : (response && response.data ? response.data : [])
        if (!this.courses || this.courses.length === 0) {
          this.$message.info('未查询到课程，请先在课程管理中添加课程')
        }
      }).catch(err => {
        console.error('fetchCourses error', err)
        this.courses = []
        this.$message.error('获取课程失败请检查后端接口或权限')
      })
    },

    fetchSessionsByCourseId(courseId) {
      const api = require('@/api/proj_lw/session')
      return api.getSessionsByCourseId(courseId).then(res => {
        // 处理响应数据格式
        this.sessions = res && res.rows ? res.rows : (res && res.data ? res.data : [])
        this.form.sessionId = null // 清空课堂选择
        if (!this.sessions || this.sessions.length === 0) {
          this.$message.info('该课程下暂无课堂')
        }
        return this.sessions
      }).catch(err => {
        console.error('fetchSessionsByCourseId error', err)
        this.sessions = []
        this.$message.error('获取课堂失败')
        return []
      })
    },

    formatDateToBackend(value) {
      if (!value) return null
      const d = value instanceof Date ? value : new Date(value)
      if (isNaN(d.getTime())) return null
      const Y = d.getFullYear()
      const M = String(d.getMonth() + 1).padStart(2, '0')
      const D = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const m = String(d.getMinutes()).padStart(2, '0')
      const s = String(d.getSeconds()).padStart(2, '0')
      return `${Y}-${M}-${D} ${h}:${m}:${s}`
    },

    uploadSuccess(response) {
      if (response && response.fileName) {
        const prev = this.form.attachments ? this.form.attachments + ',' : ''
        this.form.attachments = prev + response.fileName
      }
    },

    publishOrSave() {
      if (!this.form.title) { this.$message.error('请输入标题'); return }
      if (!this.form.courseId) { this.$message.error('请选择课程'); return }
      if (!this.form.sessionId) { this.$message.error('请选择课堂'); return }
      const payload = Object.assign({}, this.form)
      if (this.form.deadline) {
        const formatted = this.formatDateToBackend(this.form.deadline)
        if (formatted) payload.deadline = formatted
      }
      this.publishLoading = true
      if (this.editing) {
        // update
        payload.homeworkId = this.editingId
        updateHomework(payload).then(res => {
          this.publishLoading = false
          if (res && (res.code === 200 || res.code === 0)) {
            this.$message.success('保存成功')
            this.loadHomeworks(this.form.sessionId)
            this.resetForm()
          } else {
            this.$message.error((res && (res.msg || res.message)) || '保存失败')
          }
        }).catch(err => {
          this.publishLoading = false
          console.error('更新作业失败', err)
          this.$message.error('保存失败')
        })
      } else {
        addHomework(payload).then(res => {
          this.publishLoading = false
          if (res && (res.code === 200 || res.code === 0)) {
            this.$message.success('发布成功')
            this.loadHomeworks(this.form.sessionId)
            this.resetForm()
          } else {
            this.$message.error((res && (res.msg || res.message)) || '发布失败')
            console.error('发布失败，server response:', res)
          }
        }).catch(err => {
          this.publishLoading = false
          console.error('发布接口调用失败：', err)
          let userMsg = '发布失败'
          try {
            if (err && err.response) {
              const d = err.response.data
              if (d && (d.msg || d.message)) userMsg = `发布失败：${d.msg || d.message}`
              else userMsg = `发布失败（HTTP ${err.response.status}）`
            } else if (err && err.message) {
              userMsg = `发布失败：${err.message}`
            }
          } catch (e) {
            console.error('解析发布错误信息失败', e)
          }
          this.$message.error(userMsg)
        })
      }
    },

    resetForm() {
      this.editing = false
      this.editingId = null
      this.form.title = ''
      this.form.content = ''
      this.form.totalScore = 100
      this.form.deadline = null
      this.form.attachments = ''
    },

    startEdit(row) {
      this.editing = true
      this.editingId = row.homeworkId || row.id || null
      // populate form fields
      this.form.courseId = row.courseId
      this.form.sessionId = row.sessionId
      this.form.title = row.title
      this.form.content = row.content
      this.form.totalScore = row.totalScore
      // try to parse deadline into Date
      this.form.deadline = row.deadline ? new Date(row.deadline) : null
      this.form.attachments = row.attachments || ''

      // 确保课堂列表包含当前课堂
      if (this.form.courseId && (!this.sessions || !this.sessions.find(s => s.sessionId === this.form.sessionId))) {
        this.fetchSessionsByCourseId(this.form.courseId)
      }
    },

    cancelEdit() { this.resetForm() },

    confirmDelete(row) {
      this.$confirm('确认删除该作业？', '提示', { type: 'warning' }).then(() => {
        const id = row.homeworkId || row.id
        delHomework(id).then(res => {
          if (res && (res.code === 200 || res.code === 0)) {
            this.$message.success('删除成功')
            this.loadHomeworks(this.form.sessionId)
          } else {
            this.$message.error((res && (res.msg || res.message)) || '删除失败')
          }
        }).catch(err => {
          console.error('删除失败', err)
          this.$message.error('删除失败')
        })
      }).catch(() => {})
    },

    loadHomeworks(sessionId) {
      if (!sessionId) { this.homeworkList = []; return }
      this.listLoading = true
      listHomework({ sessionId: sessionId, pageNum: 1, pageSize: 1000 }).then(res => {
        this.listLoading = false
        this.homeworkList = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
      }).catch(err => {
        this.listLoading = false
        console.error('loadHomeworks error', err)
        this.$message.error('获取已发布作业失败')
      })
    },

    async viewSubmissions(row) {
      const id = row.homeworkId || row.id
      if (!id) { this.$message.error('作业ID缺失'); return }
      this.subDialogTitle = row.title || '作业提交'
      this.subDialogVisible = true
      this.submissions = []
      try {
        // fetch submissions and class students in parallel
        const [subRes, studentsRes] = await Promise.all([
          getSubmissions(id).catch(e => { console.error('getSubmissions failed', e); return null }),
          // get courseCode: prefer row.courseCode, fallback to course details if needed
          (async () => {
            const courseCode = row.courseCode || (row.courseId ? (await require('@/api/proj_lw/course').getCourse(row.courseId).then(r => (r && r.data && r.data.courseCode) ? r.data.courseCode : null).catch(() => null)) : null)
            if (!courseCode) return null
            return listStudentsByCourseAndSession({ courseCode: courseCode, sessionId: row.sessionId }).catch(e => { console.error('listStudents failed', e); return null })
          })()
        ])

        const submissionsList = (subRes && (subRes.rows || subRes.data)) ? (subRes.rows || subRes.data) : (subRes || [])
        const studentsList = (studentsRes && (studentsRes.rows || studentsRes.data)) ? (studentsRes.rows || studentsRes.data) : (studentsRes || [])

        // Build a map of submissions by studentId
        const subMap = {}
        for (const s of submissionsList) {
          const sid = s.studentId || s.student_id || s.student || ''
          if (sid) subMap[String(sid)] = s
        }

        // Merge: iterate over studentsList to ensure all students present
        const merged = []
        for (const st of studentsList) {
          const sid = st.studentId || st.student_id || st.student || ''
          const name = st.studentName || st.name || st.realName || ''
          if (sid && subMap[String(sid)]) {
            const item = Object.assign({}, subMap[String(sid)])
            // ensure student fields exist
            item.studentId = sid
            item.studentName = item.studentName || name
            merged.push(item)
          } else {
            merged.push({ studentId: sid, studentName: name, submissionFiles: '', submitTime: null, status: '未提交' })
          }
        }

        // Add any submissions that are from students not in class_student list
        for (const s of submissionsList) {
          const sid = s.studentId || s.student_id || s.student || ''
          if (!studentsList.find(x => String(x.studentId || x.student_id || x.student || '') === String(sid))) {
            // ensure studentName on submission
            s.studentName = s.studentName || s.studentName || ''
            merged.push(s)
          }
        }

        // sort: submitted first (status != '未提交'), then by submitTime desc
        merged.sort((a, b) => {
          const aSubmitted = a.status && a.status !== '未提交'
          const bSubmitted = b.status && b.status !== '未提交'
          if (aSubmitted && !bSubmitted) return -1
          if (!aSubmitted && bSubmitted) return 1
          const ta = a.submitTime ? new Date(a.submitTime).getTime() : 0
          const tb = b.submitTime ? new Date(b.submitTime).getTime() : 0
          return tb - ta
        })

        this.submissions = merged
      } catch (err) {
        console.error('viewSubmissions error', err)
        this.$message.error('获取提交详情失败')
        this.submissions = []
      }
    },

    parseAttachments(str) {
      if (!str) return []
      return String(str).split(',').map(s => s.trim()).filter(Boolean)
    },

    downloadUrl(fileName) {
      const token = require('@/utils/auth').getToken()
      // include token as query param if needed
      const base = process.env.VUE_APP_BASE_API + '/common/download?fileName=' + encodeURIComponent(fileName)
      return base + (token ? ('&token=' + token) : '')
    },

    formatTime(val) {
      if (!val) return null
      const d = new Date(val)
      if (isNaN(d.getTime())) return val
      const Y = d.getFullYear()
      const M = String(d.getMonth() + 1).padStart(2, '0')
      const D = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const m = String(d.getMinutes()).padStart(2, '0')
      const s = String(d.getSeconds()).padStart(2, '0')
      return `${Y}-${M}-${D} ${h}:${m}:${s}`
    },

    exportSubmissions() {
      if (!this.submissions || this.submissions.length === 0) { this.$message.info('没有可导出的数据'); return }
      // build CSV
      const headers = ['学号', '姓名', '附件', '提交时间', '状态']
      const rows = this.submissions.map(r => [r.studentId || '', r.studentName || '', (r.submissionFiles || '').replace(/,/g, ';'), this.formatTime(r.submitTime) || '', r.status || ''])
      const csv = [headers.join(',')].concat(rows.map(r => r.map(c => '"' + String(c).replace(/"/g,'""') + '"').join(','))).join('\n')
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      link.setAttribute('download', (this.subDialogTitle || 'submissions') + '.csv')
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    }
  }
}
</script>
