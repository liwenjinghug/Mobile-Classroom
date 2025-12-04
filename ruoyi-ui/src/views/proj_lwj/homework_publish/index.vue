<template>
  <div class="app-container">
    <!-- 顶部卡片：作业发布表单 -->
    <el-card class="hero-card" shadow="hover">
      <div slot="header" class="card-header">
        <div class="title-wrap">
          <i class="el-icon-edit-outline" />
          <span class="title">作业发布</span>
          <span class="sub">请选择课程与课堂，填写标题、内容、分值与截止时间</span>
        </div>
        <div class="header-actions">
          <el-button type="primary" icon="el-icon-upload" @click="publishOrSave" :loading="publishLoading">发布作业</el-button>
          <el-button type="default" icon="el-icon-refresh" @click="resetForm">重置</el-button>
        </div>
      </div>

      <!-- 响应式两列布局的发布表单 -->
      <el-form :model="form" label-width="100px" class="form-grid">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="课程" required>
              <el-select v-model="form.courseId" placeholder="请选择课程" filterable class="w-100">
                <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="课堂" required>
              <el-select v-model="form.sessionId" placeholder="请选择课堂" class="w-100">
                <el-option v-for="s in sessions" :key="s.sessionId" :label="(s.className ? `${s.className} (ID:${s.sessionId})` : String(s.sessionId))" :value="s.sessionId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="标题" required>
              <el-input v-model="form.title" placeholder="请输入作业标题" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="分值" required>
              <el-input-number v-model="form.totalScore" :min="0" :max="1000" :precision="1" class="w-100" />
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="内容">
              <el-input type="textarea" v-model="form.content" :rows="4" placeholder="请输入作业内容或要求" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="截止时间" required>
              <el-date-picker v-model="form.deadline" type="datetime" placeholder="请选择截止时间" class="w-100" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="附件">
              <!-- 使用原有上传实现，避免未知组件导致渲染错误 -->
              <el-upload :action="uploadUrl" :headers="headers" name="file" :on-success="uploadSuccess" :multiple="true">
                <el-button size="small" type="primary">上传参考文件</el-button>
              </el-upload>
              <div v-if="form.attachments" class="attach-tip">已选择附件：{{ form.attachments }}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 表单提示与统计 -->
      <div class="form-hint">
        <el-alert type="info" :closable="false" :title="'提示：发布后学生即可在“作业上传”页面看到该作业并提交。'" />
      </div>
    </el-card>

    <!-- 工具栏：查询/导出/打印/统计 -->
    <el-card class="box-card toolbar-card" shadow="never" v-if="form.sessionId">
      <div slot="header" class="toolbar-header">
        <div class="left">
          <i class="el-icon-s-order" />
          <span>作业发布列表工具栏</span>
        </div>
        <div class="right">
          <el-button type="primary" size="small" icon="el-icon-refresh" @click="applyFilters">刷新</el-button>
          <el-dropdown trigger="click" @command="handleExportPrint" class="export-dropdown">
            <el-button size="small" type="success">
              <i class="el-icon-download"></i>
              导出/打印
              <i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="csv">
                <i class="el-icon-document"></i>
                导出 CSV
              </el-dropdown-item>
              <el-dropdown-item command="excel">
                <i class="el-icon-document"></i>
                导出 Excel
              </el-dropdown-item>
              <el-dropdown-item divided command="print">
                <i class="el-icon-printer"></i>
                打印列表
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
      <div class="toolbar-row">
        <el-input v-model="filters.keyword" placeholder="按标题/内容搜索" clearable class="search-input" @keyup.enter.native="applyFilters" />
        <el-date-picker v-model="filters.deadlineRange" type="datetimerange" start-placeholder="截止开始" end-placeholder="截止结束" class="date-range" @change="applyFilters" />
        <el-select v-model="filters.withAttachments" placeholder="是否包含附件" clearable class="select-small" @change="applyFilters">
          <el-option :value="true" label="含附件" />
          <el-option :value="false" label="不含附件" />
        </el-select>
      </div>
      <div class="stats-row" v-if="stats">
        <el-alert :closable="false" type="info" :title="`统计：共 ${stats.total} 条；已过期 ${stats.overdue}；含附件 ${stats.withAttach}；平均分值 ${stats.avgScore}`" />
      </div>
    </el-card>

    <!-- 已发布列表 -->
    <div style="margin-top:16px">
      <el-card class="list-card" shadow="hover">
        <div slot="header" class="list-header">
          <span>已发布作业（当前课堂）</span>
          <div>
            <el-button size="small" type="danger" @click="batchDelete" :disabled="selectedHomeworks.length === 0">
              <i class="el-icon-delete"></i>
              批量删除 ({{ selectedHomeworks.length }})
            </el-button>
            <el-button size="small" type="primary" @click="resetForm">发布新作业</el-button>
          </div>
        </div>

        <div v-if="!form.sessionId" class="empty-guard">请选择课堂以查看已发布作业</div>
        <div v-else>
          <el-table :data="sortedAndFilteredList" style="width:100%" v-loading="listLoading" @sort-change="onSortChange" @selection-change="handleSelectionChange" :default-sort="defaultSort" show-summary :summary-method="summaryMethod">
            <el-table-column type="selection" width="55" :selectable="checkSelectable"></el-table-column>
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
                  <el-tag
                    v-for="(f, idx) in parseAttachments(scope.row.attachments)"
                    :key="idx"
                    size="mini"
                    @click="previewFile(f)"
                    style="margin-right:8px; cursor: pointer;">
                    <i class="el-icon-document" style="margin-right: 4px;"></i>
                    {{ shortName(f) }}
                  </el-tag>
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

    <el-dialog
      title="提交列表"
      :visible.sync="submissionsDialogVisible"
      width="800px"
      :modal="false"
      :lock-scroll="false"
      :close-on-click-modal="false"
      custom-class="centered-homework-dialog"
    >
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

    <!-- 修改作业弹窗 -->
    <el-dialog
      title="修改作业"
      :visible.sync="editDialogVisible"
      width="720px"
      :modal="false"
      :lock-scroll="false"
      :close-on-click-modal="false"
      custom-class="centered-homework-dialog"
    >
      <el-form :model="editForm" label-width="100px" ref="editFormRef">
        <el-form-item label="课程">
          <el-select v-model="editForm.courseId" placeholder="请选择课程" filterable disabled>
            <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="课堂">
          <el-select v-model="editForm.sessionId" placeholder="请选择课堂" disabled>
            <el-option v-for="s in sessions" :key="s.sessionId" :label="(s.className ? `${s.className} (ID:${s.sessionId})` : String(s.sessionId))" :value="s.sessionId" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="editForm.content" :rows="4" placeholder="请输入作业内容" />
        </el-form-item>
        <el-form-item label="分值" required>
          <el-input-number v-model="editForm.totalScore" :min="0" :max="1000" :precision="1" />
        </el-form-item>
        <el-form-item label="截止时间" required>
          <el-date-picker v-model="editForm.deadline" type="datetime" placeholder="选择截止日期时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            :action="uploadUrl"
            :headers="headers"
            name="file"
            :on-success="editUploadSuccess"
            :multiple="true"
            :show-file-list="true"
          >
            <el-button size="small" type="primary">上传参考文件</el-button>
          </el-upload>
          <div v-if="editForm.attachments" style="margin-top: 8px;">
            已上传:
            <el-tag
              v-for="(f, idx) in parseAttachments(editForm.attachments)"
              :key="idx"
              closable
              @close="removeEditAttachment(idx)"
              style="margin-right: 8px;">
              {{ shortName(f) }}
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align: right;">
          <el-button @click="closeEditDialog">取消</el-button>
          <el-button type="primary" :loading="editSaving" @click="saveEdit">保存修改</el-button>
        </div>
      </template>
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
      // upload config for backend
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      headers: { Authorization: 'Bearer ' + (require('@/utils/auth').getToken()) },
      homeworkList: [],
      listLoading: false,
      publishLoading: false,
      selectedHomeworks: [],
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
      sort: { prop: 'deadline', order: 'descending' },
      // 修改作业弹窗
      editDialogVisible: false,
      editForm: {
        homeworkId: null,
        courseId: null,
        sessionId: null,
        title: '',
        content: '',
        totalScore: 100,
        deadline: null,
        attachments: ''
      },
      editSaving: false
    }
  },
  created() {
    // fetch courses and then apply any route query preselection (courseId/sessionId)
    this.fetchCourses().then(() => {
      const q = (this.$route && this.$route.query) ? this.$route.query : {}

      // 优先使用路由参数
      if (q.courseId) {
        this.form.courseId = isNaN(Number(q.courseId)) ? q.courseId : Number(q.courseId)
        this.fetchSessionsByCourseId(this.form.courseId).then(() => {
          if (q.sessionId) {
            this.form.sessionId = isNaN(Number(q.sessionId)) ? q.sessionId : Number(q.sessionId)
            this.loadHomeworks(this.form.sessionId)
          }
        })
      } else if (q.sessionId) {
        this.form.sessionId = isNaN(Number(q.sessionId)) ? q.sessionId : Number(q.sessionId)
        this.loadHomeworks(this.form.sessionId)
      } else {
        // 如果没有路由参数，尝试恢复上次选择的课程和课堂
        const lastCourseId = localStorage.getItem('homework_publish_last_courseId')
        const lastSessionId = localStorage.getItem('homework_publish_last_sessionId')

        if (lastCourseId) {
          this.form.courseId = isNaN(Number(lastCourseId)) ? lastCourseId : Number(lastCourseId)
          this.fetchSessionsByCourseId(this.form.courseId).then(() => {
            if (lastSessionId && this.sessions.some(s => s.sessionId == lastSessionId)) {
              this.form.sessionId = isNaN(Number(lastSessionId)) ? lastSessionId : Number(lastSessionId)
              this.loadHomeworks(this.form.sessionId)
            }
          })
        }
      }
    }).catch(() => {})
  },
  watch: {
    'form.courseId'(val) {
      if (val) {
        // 保存用户选择的课程
        localStorage.setItem('homework_publish_last_courseId', val)
        this.fetchSessionsByCourseId(val)
      } else {
        this.sessions = []
        this.form.sessionId = null
      }
    },
    'form.sessionId'(val) {
      if (val) {
        // 保存用户选择的课堂
        localStorage.setItem('homework_publish_last_sessionId', val)
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
    },

    resetForm() {
      this.form.title = ''
      this.form.content = ''
      this.form.totalScore = 100
      this.form.deadline = null
      this.form.attachments = ''
    },

    startEdit(row) {
      // 打开弹窗编辑，而不是在页面表单中编辑
      this.editForm.homeworkId = row.homeworkId || row.id || null
      this.editForm.courseId = row.courseId
      this.editForm.sessionId = row.sessionId
      this.editForm.title = row.title
      this.editForm.content = row.content
      this.editForm.totalScore = row.totalScore
      this.editForm.deadline = row.deadline ? new Date(row.deadline) : null
      this.editForm.attachments = row.attachments || ''

      // 确保课堂列表包含当前课堂
      if (this.editForm.courseId && (!this.sessions || !this.sessions.find(s => s.sessionId === this.editForm.sessionId))) {
        this.fetchSessionsByCourseId(this.editForm.courseId)
      }

      this.editDialogVisible = true
    },

    closeEditDialog() {
      this.editDialogVisible = false
      this.editForm = {
        homeworkId: null,
        courseId: null,
        sessionId: null,
        title: '',
        content: '',
        totalScore: 100,
        deadline: null,
        attachments: ''
      }
    },

    editUploadSuccess(res, file) {
      if (res.code === 200 || res.code === 0) {
        const url = res.url || res.data || res.fileName
        this.editForm.attachments = this.editForm.attachments ? (this.editForm.attachments + ',' + url) : url
        this.$message.success('文件上传成功')
      } else {
        this.$message.error('上传失败: ' + (res.msg || res.message || ''))
      }
    },

    removeEditAttachment(idx) {
      const arr = this.parseAttachments(this.editForm.attachments)
      arr.splice(idx, 1)
      this.editForm.attachments = arr.join(',')
    },

    async saveEdit() {
      // 验证必填字段
      if (!this.editForm.title || !this.editForm.title.trim()) {
        this.$message.warning('请输入作业标题')
        return
      }
      if (!this.editForm.totalScore || this.editForm.totalScore <= 0) {
        this.$message.warning('请输入有效的分值')
        return
      }
      if (!this.editForm.deadline) {
        this.$message.warning('请选择截止时间')
        return
      }

      this.editSaving = true
      try {
        // 格式化日期为后端期望的格式 yyyy-MM-dd HH:mm:ss
        let formattedDeadline = this.editForm.deadline
        if (this.editForm.deadline) {
          formattedDeadline = this.formatDateToBackend(this.editForm.deadline)
        }

        const payload = {
          homeworkId: this.editForm.homeworkId,
          courseId: this.editForm.courseId,
          sessionId: this.editForm.sessionId,
          title: this.editForm.title,
          content: this.editForm.content,
          totalScore: this.editForm.totalScore,
          deadline: formattedDeadline,
          attachments: this.editForm.attachments
        }

        await updateHomework(payload)

        // 立即更新本地数据，而不是重新加载
        console.log('修改成功，开始更新本地数据')
        console.log('当前 homeworkList:', this.homeworkList)
        console.log('要查找的 homeworkId:', this.editForm.homeworkId)

        // 确保 homeworkList 已初始化
        if (!Array.isArray(this.homeworkList)) {
          console.warn('homeworkList 未初始化，重新加载数据')
          this.loadHomeworks(this.editForm.sessionId)
          this.$message.success('作业修改成功')
          this.closeEditDialog()
          return
        }

        const index = this.homeworkList.findIndex(h =>
          (h.homeworkId === this.editForm.homeworkId) ||
          (h.id === this.editForm.homeworkId)
        )

        console.log('找到的索引:', index)

        if (index !== -1) {
          console.log('更新索引 ' + index + ' 的记录')
          // 更新该记录的信息
          this.$set(this.homeworkList, index, {
            ...this.homeworkList[index],
            title: this.editForm.title,
            content: this.editForm.content,
            totalScore: this.editForm.totalScore,
            deadline: this.editForm.deadline,
            attachments: this.editForm.attachments
          })
          console.log('本地数据已更新:', this.homeworkList[index])
        } else {
          console.warn('未找到对应的作业记录，索引:', index)
          console.warn('将在延迟刷新时重新加载')
        }

        this.$message.success('作业修改成功')
        this.closeEditDialog()

        // 🔧 移除自动刷新，避免覆盖本地更新的数据
        // 如果需要刷新，用户可以手动切换课堂或刷新页面
        console.log('本地数据更新完成，跳过自动刷新以保持记录可见')
      } catch (error) {
        console.error('修改作业失败:', error)
        this.$message.error('修改失败: ' + (error.message || '网络错误'))
      } finally {
        this.editSaving = false
      }
    },

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

    handleSelectionChange(selection) {
      this.selectedHomeworks = selection
    },

    checkSelectable(row) {
      // 所有作业都可以被选择删除
      return true
    },

    async batchDelete() {
      if (this.selectedHomeworks.length === 0) {
        this.$message.warning('请先选择要删除的作业')
        return
      }

      try {
        await this.$confirm(`确认删除选中的 ${this.selectedHomeworks.length} 条作业？`, '批量删除', {
          type: 'warning',
          confirmButtonText: '确认删除',
          cancelButtonText: '取消'
        })

        // 询问是否同时删除学生提交
        const also = window.confirm('是否同时删除这些作业的学生提交记录？\n\n确定：一并删除学生提交\n取消：仅删除发布记录（学生历史提交将保留）')

        const deletePromises = this.selectedHomeworks.map(row => {
          const id = row.homeworkId || row.id
          return delHomework(id, also)
            .then(res => {
              if (res && (res.code === 200 || res.code === 0)) {
                return { success: true, id, title: row.title }
              } else {
                return { success: false, id, title: row.title, error: res.msg || res.message || '删除失败' }
              }
            })
            .catch(err => {
              return { success: false, id, title: row.title, error: err.message || '网络错误' }
            })
        })

        const results = await Promise.all(deletePromises)
        const successCount = results.filter(r => r.success).length
        const failCount = results.filter(r => !r.success).length

        if (failCount === 0) {
          this.$message.success(`成功删除 ${successCount} 条作业`)
        } else {
          const failedTitles = results.filter(r => !r.success).map(r => r.title).join('、')
          this.$message.warning(`删除完成：成功 ${successCount} 条，失败 ${failCount} 条\n失败的作业：${failedTitles}`)
        }

        // 刷新列表
        this.loadHomeworks(this.form.sessionId)
        // 清空选择
        this.selectedHomeworks = []
      } catch {
        // 用户取消操作
      }
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
        this.submissionsDialogVisible = false
      }).catch(err => {
        console.error('导航到批改页面失败', err)
        this.$message.error('打开批改页面失败')
      })
    },

    checkDuplicateTitle(sessionId, title) {
      if (!sessionId || !title) return Promise.resolve(false)
      return listHomework({ sessionId, title, pageNum: 1, pageSize: 1 }).then(res => {
        const list = (res && (res.rows || res.data)) ? (res.rows || res.data) : []
        return list.length > 0
      }).catch(err => {
        console.error('检查重复标题时出错', err)
        return false
      })
    },

    onSortChange({ prop, order }) {
      // prop: 排序字段，order: asc / descending
      if (!prop) return
      this.sort.prop = prop
      this.sort.order = order === 'ascending' ? 'ascending' : 'descending'
      this.applyFilters()
    },

    applyFilters() {
      this.loadHomeworks(this.form.sessionId)
    },

    handleExportPrint(command) {
      const data = this.sortedAndFilteredList
      if (!data || data.length === 0) {
        this.$message.warning('当前没有可导出或打印的作业')
        return
      }

      switch (command) {
        case 'csv':
          this.exportToCSV(data)
          break
        case 'excel':
          this.exportToExcel(data)
          break
        case 'print':
          this.printList(data)
          break
        default:
          break
      }
    },

    exportToCSV(data) {
      const bom = '\ufeff'
      const headers = ['标题', '内容', '截止时间', '分值', '附件', '发布状态']
      const lines = [headers.join(',')]

      data.forEach(r => {
        const attachments = this.parseAttachments(r.attachments).map(f => this.shortName(f)).join(';')
        const deadline = this.formatTime(r.deadline) || ''
        const status = r.homeworkDeleted ? '已删除' : '正常'
        const content = (r.content || '').replace(/\n/g, ' ').substring(0, 50) + (r.content && r.content.length > 50 ? '...' : '')

        const row = [
          r.title || '',
          content,
          deadline,
          r.totalScore || '',
          attachments,
          status
        ]
        lines.push(row.map(v => ('"' + String(v).replace(/"/g, '""') + '"')).join(','))
      })

      const blob = new Blob([bom + lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `作业发布列表_${this.timestampString()}.csv`
      a.click()
      URL.revokeObjectURL(a.href)
      this.$message.success('CSV导出成功')
    },

    exportToExcel(data) {
      const headers = ['标题', '内容', '截止时间', '分值', '附件', '发布状态']
      let html = '<table border="1"><thead><tr>' + headers.map(h => '<th>' + h + '</th>').join('') + '</tr></thead><tbody>'

      data.forEach(r => {
        const attachments = this.parseAttachments(r.attachments).map(f => this.shortName(f)).join('; ')
        const deadline = this.formatTime(r.deadline) || ''
        const status = r.homeworkDeleted ? '已删除' : '正常'
        const content = (r.content || '').replace(/\n/g, ' ').substring(0, 100) + (r.content && r.content.length > 100 ? '...' : '')

        const row = [
          r.title || '',
          content,
          deadline,
          r.totalScore || '',
          attachments,
          status
        ]
        html += '<tr>' + row.map(v => '<td style="mso-number-format:\\@">' + String(v).replace(/</g, '&lt;').replace(/>/g, '&gt;') + '</td>').join('') + '</tr>'
      })

      html += '</tbody></table>'
      const blob = new Blob(['\ufeff' + html], { type: 'application/vnd.ms-excel' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `作业发布列表_${this.timestampString()}.xls`
      a.click()
      URL.revokeObjectURL(a.href)
      this.$message.success('Excel导出成功')
    },

    printList(data) {
      if (!data || data.length === 0) {
        this.$message.warning('没有可打印的数据')
        return
      }

      const esc = s => String(s == null ? '' : s).replace(/</g, '&lt;').replace(/>/g, '&gt;')
      const rows = data.map(r => {
        const attachments = this.parseAttachments(r.attachments).map(f => this.shortName(f)).join('; ')
        const deadline = this.formatTime(r.deadline) || '—'
        const status = r.homeworkDeleted ? '已删除' : '正常'
        const content = (r.content || '').substring(0, 50) + (r.content && r.content.length > 50 ? '...' : '')

        return `<tr>
          <td>${esc(r.title || '')}</td>
          <td>${esc(content)}</td>
          <td>${esc(deadline)}</td>
          <td>${esc(r.totalScore || '')}</td>
          <td>${esc(attachments)}</td>
          <td>${status}</td>
        </tr>`
      }).join('')

      const stats = this.stats
      const statsText = stats ? `统计：共 ${stats.total} 条；已过期 ${stats.overdue}；含附件 ${stats.withAttach}；平均分值 ${stats.avgScore}` : ''

      const win = window.open('', '_blank')
      if (!win) {
        this.$message.error('打印窗口被拦截，请允许弹出窗口')
        return
      }

      win.document.write(`
        <html>
        <head>
          <title>作业发布列表</title>
          <meta charset="utf-8" />
          <style>
            body { font-family: Arial, Helvetica, 'Microsoft YaHei'; padding: 20px; }
            table { border-collapse: collapse; width: 100%; margin-top: 12px; }
            th, td { border: 1px solid #999; padding: 8px; text-align: left; font-size: 12px; }
            th { background: #f5f5f5; font-weight: bold; }
            h2 { margin: 0 0 12px; }
            .meta { margin: 6px 0 16px; color: #666; font-size: 14px; }
            .no-print { margin-top: 20px; text-align: center; }
            @media print {
              body { padding: 0; }
              .no-print { display: none; }
            }
          </style>
        </head>
        <body>
          <h2>作业发布列表</h2>
          <div class="meta">${statsText}</div>
          <table>
            <thead>
              <tr>
                <th>标题</th>
                <th>内容</th>
                <th>截止时间</th>
                <th>分值</th>
                <th>附件</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              ${rows}
            </tbody>
          </table>
          <div class="no-print">
            <button onclick="window.print()" style="padding: 8px 20px; font-size: 14px; cursor: pointer;">打印</button>
            <button onclick="window.close()" style="padding: 8px 20px; font-size: 14px; cursor: pointer; margin-left: 10px;">关闭</button>
          </div>
        </body>
        </html>
      `)
      win.document.close()
    },

    timestampString() {
      const d = new Date()
      const pad = n => String(n).padStart(2, '0')
      return d.getFullYear() + pad(d.getMonth() + 1) + pad(d.getDate()) + '_' + pad(d.getHours()) + pad(d.getMinutes()) + pad(d.getSeconds())
    },

    exportList() {
      // 保留旧方法以防兼容性问题
      this.$message.warning('请使用"导出/打印"下拉菜单')
    },

    previewFile(file) {
      const url = this.downloadUrl(file)
      if (url) {
        window.open(url, '_blank')
      }
    },

    downloadUrl(file) {
      if (!file) return ''
      const baseUrl = process.env.VUE_APP_BASE_API + '/common/download'
      return `${baseUrl}?file=${encodeURIComponent(file)}`
    },

    shortName(file) {
      if (!file) return ''
      const parts = file.split('/')
      return parts.length > 0 ? parts[parts.length - 1] : file
    },

    parseAttachments(attachments) {
      if (!attachments) return []
      return attachments.split(',').map(f => f.trim()).filter(f => f !== '')
    },
    summaryMethod({ columns, data }) {
      const sums = []
      columns.forEach((col, index) => {
        if (index === 0) { sums[index] = '合计'; return }
        if (col.property === 'totalScore') {
          const total = (data || []).reduce((acc, item) => {
            const v = Number(item.totalScore)
            return acc + (isNaN(v) ? 0 : v)
          }, 0)
          sums[index] = total
        } else {
          sums[index] = ''
        }
      })
      return sums
    },
    formatTime(value) {
      if (!value) return ''
      try {
        if (typeof value === 'string') {
          const s = value.trim()
          // if pure digits, treat as timestamp
          if (/^\d+$/.test(s)) {
            let n = Number(s)
            if (s.length === 10) n *= 1000
            const d = new Date(n)
            return isNaN(d.getTime()) ? '' : d.toLocaleString('zh-CN')
          }
          // try ISO or common formats
          const d = new Date(s)
          if (!isNaN(d.getTime())) return d.toLocaleString('zh-CN')
          const d2 = new Date(s.replace(/-/g, '/'))
          return isNaN(d2.getTime()) ? '' : d2.toLocaleString('zh-CN')
        }
        if (value instanceof Date) {
          return isNaN(value.getTime()) ? '' : value.toLocaleString('zh-CN')
        }
        if (typeof value === 'number') {
          let n = value
          if (String(value).length === 10) n *= 1000
          const d = new Date(n)
          return isNaN(d.getTime()) ? '' : d.toLocaleString('zh-CN')
        }
      } catch (e) { /* ignore */ }
      return ''
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 16px;
  background-color: #f5f7fa;
}

.hero-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title-wrap {
  display: flex;
  align-items: center;
}

.title-wrap i {
  font-size: 24px;
  color: #409eff;
  margin-right: 8px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.sub {
  font-size: 14px;
  color: #999;
  margin-left: 4px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.header-actions el-button {
  margin-left: 8px;
}

.form-grid {
  background-color: #ffffff;
  padding: 24px;
  border-radius: 8px;
}

.form-hint {
  margin-top: 16px;
}

.empty-guard {
  padding: 24px;
  text-align: center;
  color: #999;
}

.box-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.toolbar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.left {
  display: flex;
  align-items: center;
}

.left i {
  font-size: 20px;
  color: #409eff;
  margin-right: 8px;
}

.right {
  display: flex;
  align-items: center;
}

.right el-button {
  margin-left: 8px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.search-input {
  flex: 1;
  margin-right: 8px;
}

.date-range {
  flex: 2;
  margin-right: 8px;
}

.select-small {
  width: 120px;
}

.stats-row {
  margin-top: 8px;
}

.list-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 16px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.list-header span {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.list-header el-button {
  margin-left: 8px;
}

.el-table {
  width: 100%;
}

.el-table th,
.el-table td {
  padding: 12px 16px;
  text-align: left;
}

.el-table th {
  background-color: #f5f7fa;
  color: #333;
  font-weight: 500;
}

.el-table td {
  border-bottom: 1px solid #e4e7ec;
}

.el-table .empty {
  padding: 24px;
  text-align: center;
  color: #999;
}

.el-tag {
  margin-right: 8px;
}

.dialog-footer {
  text-align: right;
}

.el-upload {
  display: inline-block;
  width: 100%;
}

.el-upload .el-button {
  width: 100%;
}

.attach-tip {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}
</style>
