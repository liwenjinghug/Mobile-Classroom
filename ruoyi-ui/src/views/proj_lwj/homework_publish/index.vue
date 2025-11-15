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
          <el-option v-for="s in sessions" :key="s.sessionId" :label="(s.className ? `${s.className} (ID:${s.sessionId})` : String(s.sessionId))" :value="s.sessionId" />
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

    <!-- 工具栏：查询/导出/打印/统计 -->
    <el-card class="box-card" style="margin-top: 10px" v-if="form.sessionId">
      <div slot="header" class="clearfix">
        <span>作业发布列表工具栏</span>
      </div>
      <div class="toolbar-row">
        <el-input v-model="filters.keyword" placeholder="按标题/内容搜索" clearable style="width:240px" @keyup.enter.native="applyFilters" />
        <el-date-picker v-model="filters.deadlineRange" type="datetimerange" start-placeholder="截止开始" end-placeholder="截止结束" style="margin-left:8px" @change="applyFilters" />
        <el-select v-model="filters.withAttachments" placeholder="附件筛选" clearable style="width: 140px; margin-left:8px" @change="applyFilters">
          <el-option :value="true" label="仅有附件" />
          <el-option :value="false" label="仅无附件" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" style="margin-left:8px" @click="applyFilters">查询</el-button>
        <el-button @click="resetFilters" style="margin-left:6px">重置</el-button>
        <el-button type="success" icon="el-icon-download" style="margin-left:12px" @click="exportCSV">导出CSV</el-button>
        <el-button type="info" icon="el-icon-printer" style="margin-left:6px" @click="printList">打印</el-button>
      </div>
      <div class="stats-row" v-if="stats">
        <el-alert :closable="false" type="info" :title="`统计：共 ${stats.total} 条；已过期 ${stats.overdue}；含附件 ${stats.withAttach}；平均分值 ${stats.avgScore}`" />
      </div>
    </el-card>

    <!-- 已发布列表 -->
    <div style="margin-top:16px">
      <el-card>
        <div slot="header" style="display:flex;align-items:center;justify-content:space-between">
          <span>已发布作业（当前课堂）</span>
          <div>
            <el-button size="small" type="primary" @click="resetForm">发布新作业</el-button>
          </div>
        </div>

        <div v-if="!form.sessionId" style="padding:16px">请选择课堂以查看已发布作业</div>
        <div v-else>
          <el-table :data="sortedAndFilteredList" style="width:100%" v-loading="listLoading" @sort-change="onSortChange" :default-sort="defaultSort" show-summary :summary-method="summaryMethod">
            <el-table-column prop="title" label="标题" sortable="custom" />
            <el-table-column label="截止时间" sortable="custom" prop="deadline">
              <template slot-scope="scope">
                {{ formatTime(scope.row.deadline) || '—' }}
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="分值" width="100" sortable="custom" />
            <el-table-column label="附件">
              <template slot-scope="scope">
                <div v-if="scope.row.attachments">
                  <a v-for="(f, idx) in parseAttachments(scope.row.attachments)" :key="idx" :href="downloadUrl(f)" target="_blank" style="margin-right:8px">{{ shortName(f) }}</a>
                </div>
                <div v-else>—</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="320">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="startEdit(scope.row)" :disabled="scope.row.homeworkDeleted">修改</el-button>
                <span v-if="scope.row.homeworkDeleted" style="color:#999;margin-left:8px">已被老师删除</span>
                <el-button size="mini" type="danger" @click="confirmDelete(scope.row)" style="margin-left:6px">删除</el-button>
                <el-button size="mini" @click="viewSubmissions(scope.row)" style="margin-left:6px">查看提交</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="(!sortedAndFilteredList || sortedAndFilteredList.length === 0) && !listLoading" style="padding:12px">当前课堂暂无已发布作业</div>
        </div>
      </el-card>
    </div>

    <el-dialog title="提交列表" :visible.sync="submissionsDialogVisible" width="800px">
      <div>
        <div style="margin-bottom:8px"><strong>作业：</strong>{{ pubSelectedHomework && (pubSelectedHomework.title || pubSelectedHomework.homeworkTitle) }}</div>
        <el-table :data="pubSubmissions" style="width:100%">
          <el-table-column label="学号" width="160">
            <template #default="{ row }">{{ row.student_no || row.studentNo || row.studentId || row.student_id || '' }}</template>
          </el-table-column>
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="submissionFiles" label="附件">
            <template #default="{ row }">
              <div v-if="row.submissionFiles">
                <a v-for="(f, idx) in parseAttachments(row.submissionFiles)" :key="idx" :href="downloadUrl(f)" target="_blank" style="margin-right:8px">{{ shortName(f) }}</a>
              </div>
              <div v-else>—</div>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="160">
            <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">{{ row.status === '2' ? '已批改' : (row.status === '3' ? '逾期' : (row.submitTime ? '已提交' : '未提交')) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="mini" type="danger" @click.prevent="onDeleteSubmission(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="openInGrading">在批改页面打开</el-button>
        <el-button @click="submissionsDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listCourse } from '@/api/proj_lw/course'
import { addHomework, listHomework, updateHomework, delHomework, getSubmissions, deleteSubmission } from '@/api/proj_lwj/homework'

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
      // publish page submissions dialog
      submissionsDialogVisible: false,
      pubSubmissions: [],
      pubSelectedHomework: null,
      // 过滤与排序
      filters: {
        keyword: '',
        deadlineRange: [],
        withAttachments: null
      },
      sort: { prop: 'deadline', order: 'descending' }
    }
  },
  created() {
    // fetch courses and then apply any route query preselection (courseId/sessionId)
    this.fetchCourses().then(() => {
      const q = (this.$route && this.$route.query) ? this.$route.query : {}
      if (q.courseId) {
        // prefer number when possible
        this.form.courseId = isNaN(Number(q.courseId)) ? q.courseId : Number(q.courseId)
        // load sessions for the course then optionally set sessionId
        this.fetchSessionsByCourseId(this.form.courseId).then(() => {
          if (q.sessionId) {
            this.form.sessionId = isNaN(Number(q.sessionId)) ? q.sessionId : Number(q.sessionId)
            // load homeworks for that session
            this.loadHomeworks(this.form.sessionId)
          }
        })
      } else if (q.sessionId) {
        // if only sessionId provided, try to set it and load homeworks (course will be empty)
        this.form.sessionId = isNaN(Number(q.sessionId)) ? q.sessionId : Number(q.sessionId)
        this.loadHomeworks(this.form.sessionId)
      }
    }).catch(() => {})
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
  computed: {
    defaultSort() {
      return { prop: this.sort.prop, order: this.sort.order }
    },
    // 过滤后的列表
    filteredList() {
      let list = Array.isArray(this.homeworkList) ? this.homeworkList.slice() : []
      const kw = (this.filters.keyword || '').toString().trim().toLowerCase()
      if (kw) {
        list = list.filter(h => {
          const t = (h.title || '').toString().toLowerCase()
          const c = (h.content || '').toString().toLowerCase()
          return t.includes(kw) || c.includes(kw)
        })
      }
      // 截止时间范围
      if (this.filters.deadlineRange && this.filters.deadlineRange.length === 2) {
        const [start, end] = this.filters.deadlineRange
        const st = start ? new Date(start).getTime() : NaN
        const et = end ? new Date(end).getTime() : NaN
        list = list.filter(h => {
          const ht = h.deadline ? new Date(h.deadline).getTime() : NaN
          if (isNaN(ht)) return false
          if (!isNaN(st) && ht < st) return false
          if (!isNaN(et) && ht > et) return false
          return true
        })
      }
      // 附件有无
      if (this.filters.withAttachments === true) {
        list = list.filter(h => !!(h.attachments && String(h.attachments).trim()))
      } else if (this.filters.withAttachments === false) {
        list = list.filter(h => !(h.attachments && String(h.attachments).trim()))
      }
      return list
    },
    // 排序 + 过滤后的最终数据
    sortedAndFilteredList() {
      const list = this.filteredList.slice()
      const { prop, order } = this.sort || {}
      if (!prop || !order || order === 'normal') return list
      const desc = order === 'descending'
      return list.sort((a, b) => {
        let av = a[prop]
        let bv = b[prop]
        if (prop === 'deadline') {
          av = a.deadline ? new Date(a.deadline).getTime() : 0
          bv = b.deadline ? new Date(b.deadline).getTime() : 0
        }
        if (typeof av === 'string') av = av.toLowerCase()
        if (typeof bv === 'string') bv = bv.toLowerCase()
        if (av === bv) return 0
        return desc ? (av > bv ? -1 : 1) : (av > bv ? 1 : -1)
      })
    },
    // 简单统计
    stats() {
      const list = this.filteredList
      const total = list.length
      if (total === 0) return { total: 0, overdue: 0, withAttach: 0, avgScore: 0 }
      const now = Date.now()
      let overdue = 0
      let withAttach = 0
      let scoreSum = 0
      list.forEach(h => {
        const t = h.deadline ? new Date(h.deadline).getTime() : NaN
        if (!isNaN(t) && t < now) overdue += 1
        if (h.attachments && String(h.attachments).trim()) withAttach += 1
        const s = Number(h.totalScore)
        if (!isNaN(s)) scoreSum += s
      })
      const avgScore = (scoreSum / total).toFixed(1)
      return { total, overdue, withAttach, avgScore }
    }
  },
  methods: {
    fetchCourses() {
      return listCourse({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.courses = response && response.rows ? response.rows : (response && response.data ? response.data : [])
        if (!this.courses || this.courses.length === 0) {
          this.$message.info('未查询到课程，请先在课程管理中添加课程')
        }
        return this.courses
      }).catch(err => {
        console.error('fetchCourses error', err)
        this.courses = []
        this.$message.error('获取课程失败请检查后端接口或权限')
        return []
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

    async publishOrSave() {
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
        // Check for duplicate title in the same session
        const isDuplicate = await this.checkDuplicateTitle(this.form.sessionId, this.form.title)
        if (isDuplicate) {
          this.publishLoading = false
          this.$message.error('发布失败：当前课堂已存在相同标题的作业，请修改标题后重试')
          return
        }
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
        // ask whether to also delete student submissions
        const also = window.confirm('是否同时删除该作业的学生提交记录？确定将一并删除学生提交，否则仅删除发布记录（学生历史提交将保留）。')
        delHomework(id, also).then(res => {
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

    viewSubmissions(scopeRow) {
      // Keep original behavior: open a read-only submissions dialog inside the publish page
      const id = scopeRow.homeworkId || scopeRow.id
      this.pubSelectedHomework = scopeRow
      this.loadSubmissionsForPublish(id)
    },

    loadSubmissionsForPublish(homeworkId) {
      if (!homeworkId) { this.pubSubmissions = []; this.submissionsDialogVisible = true; return }
      getSubmissions(homeworkId).then(res => {
        const raw = res && (res.data || res.rows) ? (res.data || res.rows) : (res || [])
        this.pubSubmissions = this.sortPubSubmissions(raw)
        this.submissionsDialogVisible = true
      }).catch(err => {
        console.error('loadSubmissionsForPublish error', err)
        this.pubSubmissions = []
        this.submissionsDialogVisible = true
        try { this.$message.error('加载提交失败：' + (err && err.message ? err.message : '服务器错误')) } catch (e) {}
      })
    },

    // Sort helper: put submitted students first (by submitTime desc), then others ordered by name
    sortPubSubmissions(list) {
      if (!Array.isArray(list)) return []
      return list.slice().sort((a, b) => {
        const aSubmitted = Boolean((a.submitTime && String(a.submitTime).trim() !== '') || (a.status && String(a.status) !== '0'))
        const bSubmitted = Boolean((b.submitTime && String(b.submitTime).trim() !== '') || (b.status && String(b.status) !== '0'))
        if (aSubmitted !== bSubmitted) return aSubmitted ? -1 : 1
        if (aSubmitted && bSubmitted) {
          const at = a.submitTime ? new Date(a.submitTime).getTime() : 0
          const bt = b.submitTime ? new Date(b.submitTime).getTime() : 0
          if (!isNaN(at) && !isNaN(bt) && at !== bt) return bt - at
        }
        // For non-submitted (or tie) sort by student_no if available, otherwise by name
        const aNo = (a.student_no || a.studentNo || a.studentId || a.student_id || '').toString()
        const bNo = (b.student_no || b.studentNo || b.studentId || b.student_id || '').toString()
        if (aNo && bNo && aNo !== bNo) {
          // numeric-like compare if both look numeric
          const aNum = Number(aNo.replace(/[^0-9]/g, ''))
          const bNum = Number(bNo.replace(/[^0-9]/g, ''))
          if (!isNaN(aNum) && !isNaN(bNum)) return aNum - bNum
          return aNo.localeCompare(bNo, 'zh-CN')
        }
        const an = (a.studentName || a.student_name || '').toString()
        const bn = (b.studentName || b.student_name || '').toString()
        return an.localeCompare(bn, 'zh-CN')
      })
    },

    openInGrading() {
      // navigate to the grading page for the selected homework; close dialog
      const hw = this.pubSelectedHomework || {}
      const id = hw.homeworkId || hw.id
      if (!id) {
        this.$message.error('无法打开：未识别作业ID')
        return
      }
      const query = { title: hw.title || hw.homeworkTitle, courseId: this.form.courseId, sessionId: this.form.sessionId }
      const named = { name: 'HomeworkGradingList', params: { homeworkId: id }, query }
      const path = `/proj_lwj/homework_grading/list/${id}`
      const tryPush = opts => this.$router && this.$router.push ? this.$router.push(opts).then(() => true).catch(() => false) : Promise.resolve(false)
      tryPush(named).then(success => {
        if (!success) {
          // fallback to path-based navigation
          tryPush({ path, query }).then(s2 => {
            if (!s2) {
              // final fallback: use full reload
              window.location.href = path + (Object.keys(query).length ? ('?' + Object.keys(query).map(k => `${k}=${encodeURIComponent(query[k])}`).join('&')) : '')
            }
          })
        }
      }).finally(() => {
        this.submissionsDialogVisible = false
      })
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
    // reuse parseAttachments already present
    parseAttachments(str) {
      if (!str) return []
      return String(str).split(',').map(s => s.trim()).filter(Boolean)
    },
    downloadUrl(fileName) {
      const base = process.env.VUE_APP_BASE_API || ''
      if (!fileName) return ''
      const f = String(fileName)
      // if resource path exposed by backend (starts with resource prefix), call resource download
      if (f.startsWith('/profile') || f.startsWith('profile')) {
        return base + '/common/download/resource?resource=' + encodeURIComponent(f)
      }
      const token = require('@/utils/auth').getToken()
      const baseQuery = base + '/common/download?fileName=' + encodeURIComponent(f)
      return token ? (baseQuery + '&token=' + token) : baseQuery
    },
    shortName(path) {
      const p = (path || '').toString()
      const idx = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'))
      return idx >= 0 ? p.slice(idx + 1) : p
    },

    async onDeleteSubmission(row) {
      if (!row || !(row.studentHomeworkId || row.id)) return
      const id = row.studentHomeworkId || row.id
      const ok = await this.$confirm('确认删除该学生的提交记录？此操作不可恢复。', '确认删除', { type: 'warning' }).then(() => true).catch(() => false)
      if (!ok) return
      try {
        const res = await deleteSubmission(id)
        if (res && (res.code === 200 || res.code === 0)) {
          this.$message.success('删除成功')
          // reload submissions for current selected homework
          const hwId = this.pubSelectedHomework && (this.pubSelectedHomework.homeworkId || this.pubSelectedHomework.id)
          this.loadSubmissionsForPublish(hwId)
        } else {
          this.$message.error((res && (res.msg || res.message)) || '删除失败')
        }
      } catch (e) {
       console.error('删除提交失败', e)
      this.$message.error('删除失败')
     }
    },

    // Check whether a title duplicates an existing non-deleted homework in the same session.
    // Returns true if duplicate exists (excluding optional excludeId), false otherwise.
    async checkDuplicateTitle(sessionId, title, excludeId = null) {
      if (!sessionId || !title) return false
      try {
        const resp = await listHomework({ sessionId: sessionId, pageNum: 1, pageSize: 1000 })
        const list = (resp && (resp.rows || resp.data)) ? (resp.rows || resp.data) : (resp || [])
        const normalized = (list || []).map(h => ({
          id: h.homeworkId || h.id,
          title: (h.title || '').toString().trim(),
          deleted: !!(h.homeworkDeleted || h.homework_deleted)
        }))
        const t = title.toString().trim().toLowerCase()
        for (const h of normalized) {
          if (!h || h.deleted) continue
          if (excludeId && String(h.id) === String(excludeId)) continue
          if (h.title.toLowerCase() === t) return true
        }
        return false
      } catch (e) {
        console.warn('checkDuplicateTitle failed, falling back to client-side check', e)
        // fallback: check the currently loaded homeworkList if available
        const t = title.toString().trim().toLowerCase()
        const list = this.homeworkList || []
        for (const h of list) {
          const id = h.homeworkId || h.id
          const deleted = !!(h.homeworkDeleted || h.homework_deleted)
          if (deleted) continue
          if (excludeId && String(id) === String(excludeId)) continue
          if ((h.title || '').toString().trim().toLowerCase() === t) return true
        }
        return false
      }
    },

    // 查询/筛选/排序交互
    applyFilters() {
      // 仅依赖 computed，触发视图更新
      this.$forceUpdate()
    },
    resetFilters() {
      this.filters.keyword = ''
      this.filters.deadlineRange = []
      this.filters.withAttachments = null
      this.sort = { prop: 'deadline', order: 'descending' }
    },
    onSortChange({ prop, order }) {
      // element 自定义排序回调
      this.sort = { prop, order }
    },

    // 导出 CSV（客户端）
    exportCSV() {
      const rows = this.sortedAndFilteredList
      if (!rows || rows.length === 0) {
        this.$message.info('没有可导出的数据')
        return
      }
      const headers = ['作业ID','课程ID','课堂ID','标题','分值','截止时间','附件']
      const data = rows.map(h => [
        h.homeworkId || h.id || '',
        h.courseId || '',
        h.sessionId || '',
        (h.title || '').toString().replace(/\n/g,' '),
        h.totalScore || '',
        this.formatTime(h.deadline) || '',
        (h.attachments || '').toString()
      ])
      const csv = this.buildCSV([headers, ...data])
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const fileName = `作业发布列表_${this.todayString()}.csv`
      this.saveBlob(blob, fileName)
    },
    buildCSV(rows) {
      return rows.map(r => r.map(cell => this.csvCell(cell)).join(',')).join('\r\n')
    },
    csvCell(val) {
      if (val == null) return ''
      const s = String(val)
      // 如果包含逗号/引号/换行，加双引号并转义引号
      if (/[",\n]/.test(s)) return '"' + s.replace(/"/g,'""') + '"'
      return s
    },
    todayString() {
      const d = new Date()
      const Y = d.getFullYear()
      const M = String(d.getMonth() + 1).padStart(2, '0')
      const D = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const m = String(d.getMinutes()).padStart(2, '0')
      return `${Y}${M}${D}_${h}${m}`
    },
    saveBlob(blob, filename) {
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      document.body.appendChild(a)
      a.click()
      setTimeout(() => {
        document.body.removeChild(a)
        window.URL.revokeObjectURL(url)
      }, 0)
    },

    // 打印列表（简单打印当前过滤后的表格数据）
    printList() {
      const rows = this.sortedAndFilteredList
      const htmlRows = rows.map(h => `
        <tr>
          <td>${h.homeworkId || h.id || ''}</td>
          <td>${h.courseId || ''}</td>
          <td>${h.sessionId || ''}</td>
          <td>${(h.title || '').toString().replace(/</g,'&lt;')}</td>
          <td>${h.totalScore || ''}</td>
          <td>${this.formatTime(h.deadline) || ''}</td>
          <td>${(h.attachments || '').toString().replace(/</g,'&lt;')}</td>
        </tr>
      `).join('')
      const win = window.open('', '_blank')
      if (!win) { this.$message.error('浏览器拦截了打印窗口'); return }
      win.document.write(`
        <html><head><title>作业发布列表</title>
        <style>body{font-family:Arial,Helvetica,'Microsoft YaHei';padding:12px} table{border-collapse:collapse;width:100%} th,td{border:1px solid #999;padding:6px;text-align:left} h2{margin:0 0 12px}</style>
        </head><body>
        <h2>作业发布列表（${this.formatTime(new Date())}）</h2>
        <table>
          <thead><tr><th>作业ID</th><th>课程ID</th><th>课堂ID</th><th>标题</th><th>分值</th><th>截止时间</th><th>附件</th></tr></thead>
          <tbody>${htmlRows}</tbody>
        </table>
        </body></html>
      `)
      win.document.close()
      win.focus()
      win.print()
    },

    // 表尾合计
    summaryMethod({ columns, data }) {
      const sums = []
      columns.forEach((col, index) => {
        if (index === 0) { sums[index] = '合计'; return }
        if (col.property === 'totalScore') {
          const total = data.reduce((acc, item) => {
            const v = Number(item.totalScore)
            return acc + (isNaN(v) ? 0 : v)
          }, 0)
          sums[index] = total
        } else {
          sums[index] = ''
        }
      })
      return sums
    }
  }
}
</script>

<style scoped>
.toolbar-row { display:flex; align-items:center; flex-wrap: wrap; }
.stats-row { margin-top: 10px; }
</style>
