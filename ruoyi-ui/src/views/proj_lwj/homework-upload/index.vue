<template>
  <div class="homework-upload-page">

    <!-- 1. 选择课程 / 课堂 / 作业 -->
    <el-card class="selection-card">
      <div slot="header" class="card-header-with-icon">
        <i class="el-icon-notebook-2"></i>
        <span>选择课程 / 课堂 / 作业</span>
      </div>
      <el-form :model="selectionForm" label-width="80px">
        <el-form-item label="课程">
          <el-select v-model="selectionForm.courseId" placeholder="选择课程" filterable style="width:260px" @change="onCourseChange">
            <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="课堂">
          <el-select v-model="selectionForm.sessionId" placeholder="选择课堂" filterable style="width:260px" @change="onSessionChange" :disabled="!selectionForm.courseId">
            <el-option v-for="s in sessions" :key="s.sessionId" :label="s.className || ('课堂'+s.sessionId)" :value="s.sessionId" />
          </el-select>
        </el-form-item>
        <el-form-item label="作业">
          <div class="homework-select-wrapper">
            <el-select v-model="selectionForm.homeworkId" placeholder="选择作业" filterable style="width:260px" @change="onHomeworkChange" :disabled="!selectionForm.sessionId">
              <el-option v-for="h in homeworkList" :key="h.homeworkId" :label="h.title" :value="h.homeworkId" />
            </el-select>
            <el-button type="text" icon="el-icon-refresh" :disabled="!selectionForm.sessionId" @click="refreshHomeworkList" class="refresh-btn">刷新</el-button>
          </div>
        </el-form-item>
      </el-form>
      <div v-if="!homeworkId" class="hint-box">
        <i class="el-icon-info"></i>
        <span>请选择作业后继续下面的身份确认与提交。</span>
      </div>
    </el-card>

    <!-- 2. 身份确认 -->
    <el-card class="identity-card">
      <div slot="header" class="card-header-with-icon">
        <i class="el-icon-user"></i>
        <span>身份确认</span>
      </div>
      <div class="identity-content">
        <div class="identity-input-group">
          <el-input
            v-model.trim="studentNo"
            placeholder="请输入学号"
            style="max-width:260px"
            prefix-icon="el-icon-s-custom"
          />
          <div class="identity-actions">
            <el-button
              type="primary"
              size="mini"
              :disabled="confirming || !studentNo"
              @click="confirmIdentity"
              class="confirm-btn"
            >
              {{ confirming ? '确认中...' : '确认学号' }}
            </el-button>
            <el-button
              size="mini"
              @click="resetIdentity"
              :disabled="confirming"
              class="reset-btn"
            >
              重置
            </el-button>
          </div>
          <div class="identity-status-indicator">
            <div v-if="studentConfirmed" class="status-badge success">
              <i class="el-icon-success"></i>
              <span>已确认</span>
            </div>
            <div v-else class="status-badge warning">
              <i class="el-icon-warning"></i>
              <span>未确认</span>
            </div>
          </div>
        </div>
        <p class="identity-hint">输入学号后点击"确认学号"以加载您的历史提交记录</p>

        <el-alert
          v-if="!studentConfirmed"
          type="warning"
          :closable="false"
          show-icon
          title="尚未确认学号"
          description="请先输入学号并点击'确认学号'按钮以启用下面的功能"
          class="identity-alert"
        />
      </div>
    </el-card>

    <!-- 3. 作业详情与上传 -->
    <el-card v-if="homework && studentConfirmed" class="work-card">
      <div slot="header" class="card-header-with-icon">
        <i class="el-icon-document"></i>
        <span>作业详情</span>
      </div>

      <!-- 状态提示 -->
      <el-alert
        v-if="isDeadlinePassed || isSubmissionGraded"
        :type="isSubmissionGraded ? 'success' : 'warning'"
        :closable="false"
        show-icon
        :title="isSubmissionGraded ? '该作业已批改，不能再次修改或提交' : '已过截止时间，不能再提交或修改'"
        style="margin-bottom:12px"
      />

      <el-descriptions :column="1" size="small" border>
        <el-descriptions-item label="标题">{{ homework.title || '—' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">
          <span :class="{ overdue: isDeadlinePassed }">{{ formatTime(homework.deadline) || '—' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="教师附件">
          <span v-if="!parsedHomeworkAttachments.length" class="text-muted">无</span>
          <el-tag v-else v-for="(f,i) in parsedHomeworkAttachments" :key="i" size="mini" @click="previewFile(f)" class="tag-link">{{ f }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="作业内容">
          <div class="content" v-html="homework.content || '—'" />
        </el-descriptions-item>
      </el-descriptions>

      <h4 class="upload-title">
        <i class="el-icon-upload2"></i>
        上传附件
      </h4>
      <el-upload
        ref="upload"
        class="upload-block"
        :action="uploadUrl"
        :headers="headers"
        :on-success="handleUploadSuccess"
        :on-remove="handleRemove"
        :before-upload="beforeUpload"
        :on-progress="handleUploadProgress"
        :on-error="handleUploadError"
        :file-list="fileList"
        multiple
        list-type="text"
        :disabled="isDeadlinePassed || isSubmissionGraded"
      >
        <el-button size="small" type="primary" :disabled="isDeadlinePassed || isSubmissionGraded">选择文件</el-button>
        <div slot="tip" class="upload-tip">
          支持多文件，单个文件≤50MB
          <span v-if="isDeadlinePassed" style="color:#f56c6c;margin-left:8px">已过截止</span>
          <span v-else-if="isSubmissionGraded" style="color:#67c23a;margin-left:8px">已批改</span>
        </div>
      </el-upload>
      <div v-if="uploadedFiles.length" class="selected-files">
        <span>已选择：</span>
        <div class="file-chip" v-for="f in fileList" :key="f.uid">
          <el-tag size="mini" :type="failedFilesMap[f.uid] ? 'danger' : 'info'" class="tag-link" @click="previewFile(f.name)">{{ f.name }}</el-tag>
          <el-progress v-if="uploadingMap[f.uid] && !failedFilesMap[f.uid]" :percentage="uploadingMap[f.uid]" :status="uploadingMap[f.uid]===100?'success':undefined" style="width:120px; margin:0 6px" />
          <el-button v-if="failedFilesMap[f.uid]" type="text" size="mini" @click="retryUpload(f)">重试</el-button>
        </div>
      </div>
      <div class="submit-row">
        <el-button type="primary" :disabled="submitDisabled" :loading="submitLoading" @click="submitHomework">{{ hasExistingSubmission ? '更新提交' : '提交作业' }}</el-button>
        <el-button @click="resetUpload" :disabled="submitLoading || isDeadlinePassed || isSubmissionGraded">清空文件</el-button>
      </div>
    </el-card>

    <!-- 4. 我的提交记录 -->
    <el-card class="submissions-card" ref="submissionsCard">
      <div slot="header" class="card-header-with-actions">
        <div class="header-left">
          <i class="el-icon-tickets"></i>
          <span>我的提交记录</span>
        </div>
        <div class="header-actions">
          <el-button type="text" @click="loadMySubmissions" :disabled="!studentConfirmed">刷新</el-button>
          <el-button v-if="studentConfirmed" type="text" @click="toggleSelectAll" :disabled="mySubmissionsLoading || !filteredSubmissions.length">{{ allSelected ? '取消全选' : '全选' }}</el-button>
          <el-button v-if="studentConfirmed" type="danger" size="mini" :disabled="batchDeleteDisabled" :loading="batchDeleteLoading" @click="batchDeleteSelected">批量删除</el-button>
          <el-dropdown v-if="studentConfirmed" trigger="click">
            <el-button size="mini">导出 <i class="el-icon-arrow-down el-icon--right" /></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="exportSubmissions('csv')">导出 CSV</el-dropdown-item>
              <el-dropdown-item @click.native="exportSubmissions('excel')">导出 Excel</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>

      <!-- 过滤器条 -->
      <div v-if="studentConfirmed" class="filter-bar">
        <el-form :inline="true" size="mini">
          <el-form-item label="课程">
            <el-select v-model="filterCourseId" placeholder="全部" clearable style="width:140px">
              <el-option v-for="c in distinctCourses" :key="c.courseId" :value="c.courseId" :label="c.courseName || ('课程'+c.courseId)" />
            </el-select>
          </el-form-item>
          <el-form-item label="批改">
            <el-select v-model="filterGraded" placeholder="全部" style="width:120px">
              <el-option label="全部" value="" />
              <el-option label="已批改" value="graded" />
              <el-option label="未批改" value="ungraded" />
            </el-select>
          </el-form-item>
          <el-form-item label="截止">
            <el-select v-model="filterDeadline" placeholder="全部" style="width:120px">
              <el-option label="全部" value="" />
              <el-option label="已过期" value="expired" />
              <el-option label="未过期" value="active" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button size="mini" type="primary" @click="resetFilters">重置筛选</el-button>
          </el-form-item>
          <el-form-item>
            <span class="filter-count">匹配 {{ filteredSubmissions.length }} / 总 {{ mySubmissions.length }}</span>
          </el-form-item>
          <el-form-item>
            <el-button size="mini" icon="el-icon-printer" @click="printSubmissions" :disabled="!filteredSubmissions.length">打印</el-button>
          </el-form-item>
        </el-form>
        <div class="stats-row" v-if="submissionStats">
          <el-alert :closable="false" type="info" :title="`统计：共 ${submissionStats.total} 条；已批改 ${submissionStats.graded}；已过期 ${submissionStats.expired}；平均成绩 ${submissionStats.avgScore}`" />
        </div>
      </div>

      <div v-if="!studentConfirmed" class="blocked-panel">
        <el-alert type="info" :closable="false" show-icon title="请先确认学号" description="确认后将显示您所有相关的提交记录。" />
      </div>

      <el-table
        v-else
        :data="filteredSubmissions"
        size="small"
        stripe
        v-loading="mySubmissionsLoading"
        @selection-change="onSelectionChange"
        @sort-change="onSortChange"
        :row-key="submissionRowKey"
        class="submissions-table"
      >
        <el-table-column type="selection" width="42" reserve-selection />
        <el-table-column prop="courseName" label="课程" min-width="140" :sortable="true" />
        <el-table-column prop="homeworkTitle" label="作业" min-width="200" :sortable="true" />
        <el-table-column label="提交文件" min-width="240">
          <template #default="{ row }">
            <span v-if="!row.submissionFiles" class="text-muted">无</span>
            <el-tag v-else v-for="(f,i) in parseAttachmentString(row.submissionFiles)" :key="i" size="mini" @click="previewFile(f)" class="tag-link">{{ f.split('/').pop() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" :sortable="true">
          <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" width="90" :sortable="true">
          <template #default="{ row }">{{ row.score == null ? '—' : row.score + '分' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.homeworkDeleted" type="danger" size="mini">已删除</el-tag>
            <el-tag v-else-if="isRowGraded(row)" type="success" size="mini">已批改</el-tag>
            <el-tag v-else-if="isRowExpired(row)" type="warning" size="mini">已过期</el-tag>
            <el-tag v-else type="info" size="mini">可修改</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="mini" :disabled="!rowEditable(row)" @click="openEdit(row)">修改</el-button>
            <el-button type="danger" size="mini" :disabled="!canDeleteRow(row)" @click="deleteRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑附件弹窗 -->
    <el-dialog :visible.sync="editDialogVisible" title="修改附件" width="640px" append-to-body>
      <div v-if="editingSubmissionRow">
        <p class="dialog-tip">作业：{{ editingSubmissionRow.homeworkTitle || ('#'+editingSubmissionRow.homeworkId) }} | 学号：{{ editingSubmissionRow.studentNo }}</p>
        <el-alert v-if="!rowEditable(editingSubmissionRow)" type="warning" :closable="false" show-icon title="该提交不可修改" style="margin-bottom:12px" />
        <h4>原始附件</h4>
        <div v-if="editExistingFiles.length" class="file-tags">
          <el-tag v-for="(f,i) in editExistingFiles" :key="i" size="mini" closable @close="removeExistingFile(i)">{{ f.split('/').pop() }}</el-tag>
        </div>
        <div v-else class="text-muted" style="margin-bottom:8px">无</div>
        <h4 style="margin-top:12px">新增附件</h4>
        <el-upload
          ref="editUpload"
          class="upload-block"
          :action="uploadUrl"
          :headers="headers"
          :on-success="handleEditUploadSuccess"
          :before-upload="beforeEditUpload"
          :file-list="editFileList"
          multiple
          list-type="text"
          :disabled="!rowEditable(editingSubmissionRow)"
        >
          <el-button size="small" type="primary" :disabled="!rowEditable(editingSubmissionRow)">选择文件</el-button>
          <div slot="tip" class="upload-tip">支持多文件，单个文件≤50MB</div>
        </el-upload>
        <div v-if="editNewFiles.length" class="file-tags">
          <el-tag v-for="(f,i) in editNewFiles" :key="'new-'+i" size="mini" type="info" closable @close="removeNewFile(i)">{{ f.split('/').pop() }}</el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeEditDialog">取消</el-button>
        <el-button type="primary" :disabled="saveEditDisabled" :loading="saveEditLoading" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
// 保持原有的script部分完全不变
import { listCourse } from '@/api/proj_lw/course'
import { getHomework, submitHomework, updateSubmission, listHomework, getStudentSubmissions, deleteSubmission } from '@/api/proj_lwj/homework'
import { getToken } from '@/utils/auth'

export default {
  name: 'HomeworkUpload',
  data() {
    return {
      courses: [],
      sessions: [],
      homeworkList: [],
      selectionForm: { courseId: null, sessionId: null, homeworkId: null },
      homework: null,
      homeworkId: null,
      studentNo: '',
      studentConfirmed: false,
      confirming: false,
      mySubmissions: [],
      mySubmissionsLoading: false,
      allSubmissions: [],
      fileList: [],
      uploadedFiles: [],
      submitLoading: false,
      submitResult: null,
      submissionsLoaded: false,
      headers: { Authorization: 'Bearer ' + getToken(), isToken: true },
      uploadUrl: ((process.env.VUE_APP_BASE_API || '/dev-api').replace(/\/+$/,'') + '/common/upload'),
      uploadingMap: {}, // uid -> progress (0-100)
      failedFilesMap: {}, // uid -> true if failed
      homeworkMetaMap: {}, // homeworkId -> { deadline: Date }
      editDialogVisible: false,
      editingSubmissionRow: null,
      editExistingFiles: [],
      editNewFiles: [],
      editFileList: [],
      saveEditLoading: false,
      selectedRows: [],
      batchDeleteLoading: false,
      filterCourseId: '',
      filterGraded: '',
      filterDeadline: '',
      sortProp: '',
      sortOrder: '', // ascending | descending
    }
  },
  computed: {
    parsedHomeworkAttachments() {
      return this.parseAttachmentString(this.homework && this.homework.attachments)
    },
    hasExistingSubmission() {
      return this.submissionsLoaded && this.allSubmissions.some(r => r.homeworkId === this.homeworkId)
    },
    isDeadlinePassed() {
      if (!this.homework || !this.homework.deadline) return false
      const d = new Date(this.homework.deadline)
      return !isNaN(d) && d.getTime() < Date.now()
    },
    isSubmissionGraded() {
      if (!this.homeworkId) return false;
      const entry = this.allSubmissions.find(r => r.homeworkId === this.homeworkId);
      if (!entry) return false;
      return (entry.score != null && entry.score !== '')
        || (entry.gradedTime != null && entry.gradedTime !== '')
        || (entry.isGraded === 1)
        || (entry.status && String(entry.status) === '2');
    },
    submitDisabled() {
      if (!this.studentConfirmed) return true;
      if (!this.homeworkId) return true;
      if (this.submitLoading) return true;
      if (!this.uploadedFiles.length) return true;
      if (this.isDeadlinePassed) return true;
      if (this.isSubmissionGraded) return true;
      return false;
    },
    saveEditDisabled() {
      if (!this.editDialogVisible) return true
      if (!this.editingSubmissionRow) return true
      if (!this.rowEditable(this.editingSubmissionRow)) return true
      return (this.editExistingFiles.length + this.editNewFiles.length) === 0 || this.saveEditLoading
    },
    allSelected() { return this.selectedRows.length && this.selectedRows.length === this.mySubmissions.length },
    deletableSelectedIds() {
      return this.selectedRows.filter(r => this.canDeleteRow(r)).map(r => r.studentHomeworkId).filter(Boolean)
    },
    batchDeleteDisabled() {
      if (!this.studentConfirmed) return true
      if (!this.selectedRows.length) return true
      if (!this.deletableSelectedIds.length) return true
      return this.batchDeleteLoading
    },
    distinctCourses() {
      const map = {};
      this.mySubmissions.forEach(r => {
        if (r.courseId && !map[r.courseId]) map[r.courseId] = { courseId: r.courseId, courseName: r.courseName };
      });
      return Object.values(map).sort((a,b)=>String(a.courseName).localeCompare(String(b.courseName)));
    },
    filteredSubmissions() {
      let arr = this.mySubmissions.slice();
      if (this.filterCourseId) arr = arr.filter(r => String(r.courseId) === String(this.filterCourseId));
      if (this.filterGraded === 'graded') arr = arr.filter(r => this.isRowGraded(r));
      else if (this.filterGraded === 'ungraded') arr = arr.filter(r => !this.isRowGraded(r));
      if (this.filterDeadline === 'expired') arr = arr.filter(r => this.isRowExpired(r));
      else if (this.filterDeadline === 'active') arr = arr.filter(r => !this.isRowExpired(r));
      if (this.sortProp) {
        const prop = this.sortProp;
        const order = this.sortOrder === 'descending' ? -1 : 1;
        arr.sort((a,b)=>{
          let va = a[prop]; let vb = b[prop];
          if (prop === 'submitTime') { va = va ? new Date(va).getTime() : 0; vb = vb ? new Date(vb).getTime() : 0; }
          if (prop === 'score') { va = va == null ? -Infinity : Number(va); vb = vb == null ? -Infinity : Number(vb); }
          if (va == null && vb == null) return 0; if (va == null) return -order; if (vb == null) return order;
          if (typeof va === 'number' && typeof vb === 'number') return (va - vb) * order;
          return String(va).localeCompare(String(vb), 'zh-CN') * order;
        });
      }
      return arr;
    },
    submissionStats() {
      const list = this.filteredSubmissions || []
      const total = list.length
      if (!total) return { total: 0, graded: 0, expired: 0, avgScore: 0 }
      let graded = 0
      let expired = 0
      let scoreSum = 0
      let scoreCnt = 0
      list.forEach(r => {
        if (this.isRowGraded(r)) graded += 1
        if (this.isRowExpired(r)) expired += 1
        const s = (r.score == null || r.score === '') ? NaN : Number(r.score)
        if (!isNaN(s)) { scoreSum += s; scoreCnt += 1 }
      })
      const avgScore = scoreCnt ? (scoreSum / scoreCnt).toFixed(1) : 0
      return { total, graded, expired, avgScore }
    }
  },
  created() {
    try {
      const savedNo = localStorage.getItem('hwUploadStudentNo')
      if (savedNo) this.studentNo = savedNo
    } catch (e) { /* ignore */ }
    this.fetchCourses()
  },
  methods: {
    formatTime(ts) {
      return (!ts && ts !== 0) ? '' : (()=>{try{let n=typeof ts==='number'?ts:Number(ts);if(isNaN(n)){const dObj=new Date(ts);return isNaN(dObj.getTime())?'':dObj.toLocaleString('zh-CN')}if(String(n).length===10)n*=1000;const d=new Date(n);return isNaN(d.getTime())?'':d.toLocaleString('zh-CN')}catch{return ''}})()
    },
    getUploadedFileName(resp, file) {
      try {
        if (!resp) return file && file.name;
        const candidates = [resp.fileName, resp.filename, resp.name, resp.originalFilename, resp.originalName, resp.url];
        for (const c of candidates) { if (c && typeof c === 'string') return c; }
        if (resp.data && typeof resp.data === 'object') {
          const d = resp.data;
          const inner = [d.fileName, d.filename, d.name];
          for (const c of inner) { if (c && typeof c === 'string') return c; }
        }
        return file && file.name;
      } catch { return file && file.name; }
    },
    async fetchCourses() {
      try {
        const res = await listCourse({ pageNum: 1, pageSize: 1000 })
        this.courses = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
      } catch (e) {
        console.error('获取课程失败', e)
      }
    },
    async onCourseChange(courseId) {
      console.log('[HW] course change', courseId)
      this.selectionForm.sessionId = null
      this.selectionForm.homeworkId = null
      this.homework = null
      this.homeworkId = null
      this.sessions = []
      this.homeworkList = []
      if (!courseId) return
      try {
        const api = require('@/api/proj_lw/session')
        const res = await api.getSessionsByCourseId(courseId)
        this.sessions = res && res.rows ? res.rows : (res && res.data ? res.data : [])
      } catch (e) {
        console.error('获取课堂失败', e)
      }
    },
    async onSessionChange(sessionId) {
      console.log('[HW] session change', sessionId)
      this.selectionForm.homeworkId = null
      this.homework = null
      this.homeworkId = null
      this.homeworkList = []
      if (!sessionId) return
      await this.refreshHomeworkList()
    },
    async refreshHomeworkList() {
      const sessionId = this.selectionForm.sessionId
      if (!sessionId) return
      try {
        const res = await listHomework({ sessionId, pageNum: 1, pageSize: 100 })
        this.homeworkList = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
      } catch (e) {
        console.error('获取作业失败', e)
        this.homeworkList = []
      }
    },
    async onHomeworkChange(homeworkId) {
      console.log('[HW] homework change', homeworkId)
      this.homeworkId = homeworkId
      this.homework = null
      this.uploadedFiles = []
      this.fileList = []
      if (!homeworkId) return
      try {
        const res = await getHomework(homeworkId)
        this.homework = (res && res.data) || res
      } catch (e) {
        console.error('加载作业详情失败', e)
        this.homework = null
      }
    },
    async confirmIdentity() {
      console.log('[HW] confirmIdentity triggered studentNo=', this.studentNo)
      if (!this.studentNo) return
      this.confirming = true
      try {
        this.mySubmissionsLoading = true
        const res = await getStudentSubmissions(this.studentNo)
        let list = []
        if (Array.isArray(res)) list = res
        else if (res && Array.isArray(res.rows)) list = res.rows
        else if (res && Array.isArray(res.data)) list = res.data
        const normalized = list.map(this.normalizeSubmission)
        this.mySubmissions = normalized
        this.allSubmissions = normalized
        this.studentConfirmed = true
        this.submissionsLoaded = true
        try { localStorage.setItem('hwUploadStudentNo', this.studentNo) } catch (e) {}
        this.scrollToSubmissions()
      } catch (e) {
        console.error('加载提交记录失败', e)
        this.mySubmissions = []
        this.studentConfirmed = false
      } finally {
        this.mySubmissionsLoading = false
        this.confirming = false
      }
    },
    async loadMySubmissions() {
      if (!this.studentConfirmed || !this.studentNo) return
      await this.confirmIdentity()
    },
    normalizeSubmission(sub = {}) {
      return {
        studentHomeworkId: sub.studentHomeworkId || sub.id,
        homeworkId: sub.homeworkId,
        courseId: sub.courseId,
        courseName: sub.courseName || '',
        homeworkTitle: sub.homeworkTitle || sub.title || '',
        submissionFiles: sub.submissionFiles || '',
        submitTime: sub.submitTime,
        score: sub.score,
        gradedTime: sub.gradedTime,
        homeworkDeleted: sub.homeworkDeleted
      }
    },
    beforeUpload(file) {
      const max = 50 * 1024 * 1024
      if (file.size > max) { this.$message.error('文件不能超过50MB'); return false }
      if (!this.studentConfirmed) { this.$message.warning('请先确认学号'); return false }
      if (this.isDeadlinePassed) { this.$message.error('已过截止，不能再上传'); return false }
      if (this.isSubmissionGraded) { this.$message.error('作业已批改，不能再上传'); return false }
      return true
    },
    handleUploadProgress(event, file, fileList) {
      this.$set(this.uploadingMap, file.uid, Math.min(99, Math.floor(event.percent)))
    },
    handleUploadError(err, file, fileList) {
      console.error('[HW] upload error', file.name, err)
      this.$set(this.failedFilesMap, file.uid, true)
      this.$delete(this.uploadingMap, file.uid)
      this.$message.error('上传失败: ' + (file.name || '文件'))
    },
    handleUploadSuccess(resp, file) {
      if (this.isDeadlinePassed || this.isSubmissionGraded) return;
      const rawName = this.getUploadedFileName(resp, file);
      if (!rawName) { this.$message.error('上传失败：未返回文件名'); return; }
      const fullPath = this.buildFullPath(rawName);
      if (!this.uploadedFiles.includes(fullPath)) this.uploadedFiles.push(fullPath);
      const found = this.fileList.find(f => f.uid === file.uid);
      if (found) {
        found.name = fullPath;
      } else {
        this.fileList.push({ ...file, name: fullPath });
      }
      this.$set(this.uploadingMap, file.uid, 100);
      this.$delete(this.failedFilesMap, file.uid);
      console.log('[HW] upload success -> raw:', rawName, 'full:', fullPath);
    },
    retryUpload(file) {
      if (!file || !file.raw) { this.$message.error('无法重试：原始文件数据缺失'); return }
      this.$delete(this.failedFilesMap, file.uid)
      this.$set(this.uploadingMap, file.uid, 0)
      const request = require('@/utils/request').default
      const form = new FormData()
      form.append('file', file.raw)
      request({ url: this.uploadUrl, method: 'post', data: form, headers: this.headers }).then(resp => {
        this.handleUploadSuccess(resp, file)
      }).catch(err => {
        console.error('[HW] retry failed', err)
        this.handleUploadError(err, file)
      })
    },
    handleRemove(file) {
      const name = file && file.name
      this.uploadedFiles = this.uploadedFiles.filter(n => n !== name)
      this.fileList = this.fileList.filter(f => f.uid !== file.uid)
      this.$delete(this.uploadingMap, file.uid)
      this.$delete(this.failedFilesMap, file.uid)
      console.log('[HW] removed file', name)
    },
    submitHomework: async function() {
      if (this.submitDisabled) return
      if (this.isDeadlinePassed) { this.$message.error('已过截止时间，禁止提交'); return }
      if (this.isSubmissionGraded) { this.$message.error('该作业已批改，禁止修改'); return }
      try {
        const existing = this.hasExistingSubmission ? this.allSubmissions.find(r => r.homeworkId === this.homeworkId) : null
        const submissionFiles = this.uploadedFiles.map(f => this.buildFullPath(f)).join(',')
        const basePayload = { homeworkId: this.homeworkId, studentNo: this.studentNo, submissionFiles }
        let res
        if (existing && existing.studentHomeworkId) {
          console.log('[HW] update payload', basePayload)
          res = await updateSubmission({ ...basePayload, studentHomeworkId: existing.studentHomeworkId, id: existing.studentHomeworkId })
        } else if (this.hasExistingSubmission) {
          console.warn('[HW] fallback create (missing id)')
          res = await submitHomework(basePayload)
        } else {
          console.log('[HW] create payload', basePayload)
          res = await submitHomework(basePayload)
        }
        const ok = res && (res.code === 0 || res.code === 200 || res.success === true)
        if (!ok) throw new Error((res && res.msg) || '提交失败')
        this.submitResult = { type: 'success', message: existing ? '更新成功' : '提交成功' }
        await this.loadMySubmissions()
        this.resetUpload()
        this.scrollToSubmissions()
      } catch (e) {
        console.error('[HW] submit error', e)
        this.submitResult = { type: 'error', message: e.message || '提交失败' }
      } finally { this.submitLoading = false }
    },
    resetUpload() {
      this.uploadedFiles = []
      this.fileList = []
      this.uploadingMap = {}
      this.failedFilesMap = {}
      if (this.$refs.upload) this.$refs.upload.clearFiles()
    },
    scrollToSubmissions() {
      this.$nextTick(() => {
        const card = this.$refs.submissionsCard && this.$refs.submissionsCard.$el
        if (card) {
          card.scrollIntoView({ behavior: 'smooth', block: 'start' })
        } else {
          window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' })
        }
      })
    },
    resetIdentity() {
      this.studentConfirmed = false
      this.mySubmissions = []
      this.allSubmissions = []
      this.submissionsLoaded = false
      this.uploadedFiles = []
      this.fileList = []
      this.selectionForm = { courseId: null, sessionId: null, homeworkId: null }
      this.homework = null
      this.homeworkId = null
    },
    parseAttachmentString(str) {
      if (!str) return []
      return String(str).split(',').map(s => s.trim()).filter(Boolean)
    },
    previewFile(path) {
      if (!path) return
      const request = require('@/utils/request').default
      const token = getToken()
      const headers = { Authorization: 'Bearer ' + token, isToken: true }
      const norm = String(path).replace(/\\/g, '/').trim()
      const filename = decodeURIComponent(norm.split('/').pop())
      const downloadBlob = url => request({ url, method: 'get', responseType: 'blob', headers }).then(blob => {
        const data = blob && blob.data instanceof Blob ? blob.data : (blob instanceof Blob ? blob : new Blob([blob]))
        const href = URL.createObjectURL(data)
        const a = document.createElement('a')
        a.href = href
        a.download = filename
        a.click()
        URL.revokeObjectURL(href)
      })
      const tryProfile = () => downloadBlob(`/common/download/resource?resource=${encodeURIComponent(norm)}`).catch(() => {
        const rel = norm.replace(/^\/?profile\/upload\//, '')
        if (!rel) throw new Error('missing path')
        return downloadBlob(`/common/download?fileName=${encodeURIComponent(rel)}&delete=false`)
      })
      if (norm.startsWith('/profile')) tryProfile().catch(() => this.$message.error('下载失败'))
      else if (/^https?:\/\//i.test(norm)) window.open(norm, '_blank')
      else downloadBlob(`/common/download?fileName=${encodeURIComponent(norm)}&delete=false`).catch(() => this.$message.error('下载失败'))
    },
    ensureHomeworkMeta(homeworkId) {
      if (!homeworkId) return
      if (this.homeworkMetaMap[homeworkId]) return
      getHomework(homeworkId).then(res => {
        const hw = (res && res.data) || res
        if (hw && hw.deadline) {
          this.$set(this.homeworkMetaMap, homeworkId, { deadline: hw.deadline })
        } else {
          this.$set(this.homeworkMetaMap, homeworkId, { deadline: null })
        }
      }).catch(() => {
        this.$set(this.homeworkMetaMap, homeworkId, { deadline: null })
      })
    },
    isRowGraded(row) {
      if (!row) return false
      return (row.score != null && row.score !== '') || (row.gradedTime != null && row.gradedTime !== '') || (row.status && String(row.status) === '2') || row.isGraded === 1
    },
    isRowExpired(row) {
      if (!row || !row.homeworkId) return false
      this.ensureHomeworkMeta(row.homeworkId)
      const meta = this.homeworkMetaMap[row.homeworkId]
      if (!meta || !meta.deadline) return false
      const d = new Date(meta.deadline)
      return !isNaN(d.getTime()) && d.getTime() < Date.now()
    },
    rowEditable(row) {
      if (!this.studentConfirmed) return false
      if (!row || !row.homeworkId) return false
      if (row.homeworkDeleted) return false
      if (this.isRowGraded(row)) return false
      if (this.isRowExpired(row)) return false
      return true
    },
    canDeleteRow(row) {
      if (!row) return false
      if (row.homeworkDeleted) return true
      if (this.isRowGraded(row)) return false
      return true
    },
    openEdit(row) {
      if (!this.rowEditable(row)) return
      this.editingSubmissionRow = row
      this.editExistingFiles = this.parseAttachmentString(row.submissionFiles)
      this.editNewFiles = []
      this.editFileList = []
      this.editDialogVisible = true
    },
    closeEditDialog() {
      this.editDialogVisible = false
      this.editingSubmissionRow = null
      this.editExistingFiles = []
      this.editNewFiles = []
      this.editFileList = []
      this.saveEditLoading = false
    },
    beforeEditUpload(file) {
      if (!this.rowEditable(this.editingSubmissionRow)) return false
      const max = 50 * 1024 * 1024
      if (file.size > max) { this.$message.error('文件不能超过50MB'); return false }
      return true
    },
    handleEditUploadSuccess(resp, file) {
      if (!this.rowEditable(this.editingSubmissionRow)) return;
      const rawName = this.getUploadedFileName(resp, file);
      if (!rawName) { this.$message.error('上传失败：未返回文件名'); return; }
      const fullPath = this.buildFullPath(rawName);
      if (!this.editNewFiles.includes(fullPath)) this.editNewFiles.push(fullPath);
      const found = this.editFileList.find(f => f.uid === file.uid);
      if (found) {
        found.name = fullPath;
      } else {
        this.editFileList.push({ ...file, name: fullPath });
      }
    },
    removeExistingFile(i) { this.editExistingFiles.splice(i,1) },
    removeNewFile(i) { this.editNewFiles.splice(i,1) },
    async saveEdit() {
      if (this.saveEditDisabled) return
      this.saveEditLoading = true
      try {
        const row = this.editingSubmissionRow
        const files = [...this.editExistingFiles, ...this.editNewFiles]
        const payload = {
          studentHomeworkId: row.studentHomeworkId,
          homeworkId: row.homeworkId,
          studentNo: row.studentNo,
          submissionFiles: files.join(',')
        }
        const res = await updateSubmission(payload)
        const ok = res && (res.code === 0 || res.code === 200 || res.success === true)
        if (!ok) throw new Error(res && res.msg || '保存失败')
        this.$message.success('保存成功')
        await this.loadMySubmissions()
        this.closeEditDialog()
      } catch (e) {
        console.error('saveEdit error', e)
        this.$message.error(e.message || '保存失败')
      } finally { this.saveEditLoading = false }
    },
    async deleteRow(row) {
      if (!this.canDeleteRow(row)) return
      try {
        await this.$confirm('确认删除该提交记录及其附件引用？', '提示', { type: 'warning' })
      } catch { return }
      try {
        const res = await deleteSubmission(row.studentHomeworkId)
        const ok = res && (res.code === 0 || res.code === 200 || res.success === true)
        if (!ok) throw new Error(res && res.msg || '删除失败')
        this.$message.success('删除成功')
        await this.loadMySubmissions()
      } catch (e) {
        console.error('deleteRow error', e)
        this.$message.error(e.message || '删除失败')
      }
    },
    onSelectionChange(rows) {
      this.selectedRows = rows || []
    },
    toggleSelectAll() {
      if (!this.mySubmissions.length) return
      const table = this.$refs.submissionsCard && this.$refs.submissionsCard.$el.querySelector('.el-table')
      if (!this.allSelected) {
        this.$nextTick(() => { this.selectedRows = [...this.mySubmissions] })
      } else {
        this.selectedRows = []
      }
    },
    async batchDeleteSelected() {
      if (this.batchDeleteDisabled) return
      const deletable = this.deletableSelectedIds
      const blocked = this.selectedRows.filter(r => !this.canDeleteRow(r)).map(r => r.studentHomeworkId)
      let msg = `可删除 ${deletable.length} 条记录` + (blocked.length ? `，已自动排除 ${blocked.length} 条不可删除（已批改或受限）` : '')
      try {
        await this.$confirm(msg + '，是否继续？', '批量删除', { type: 'warning' })
      } catch { return }
      if (!deletable.length) return
      this.batchDeleteLoading = true
      const results = []
      for (const id of deletable) {
        try {
          const res = await require('@/api/proj_lwj/homework').deleteSubmission(id)
          const ok = res && (res.code === 0 || res.code === 200 || res.success === true)
          results.push({ id, ok })
        } catch (e) { results.push({ id, ok: false, err: e }) }
      }
      this.batchDeleteLoading = false
      const failed = results.filter(r => !r.ok)
      if (failed.length) {
        this.$message.error(`删除完成，但 ${failed.length} 条失败`)
      } else {
        this.$message.success('批量删除成功')
      }
      await this.loadMySubmissions()
      this.selectedRows = []
    },
    onSortChange({ prop, order }) {
      this.sortProp = prop;
      this.sortOrder = order;
    },
    resetFilters() {
      this.filterCourseId = '';
      this.filterGraded = '';
      this.filterDeadline = '';
      this.sortProp = '';
      this.sortOrder = '';
    },
    exportSubmissions(type) {
      const target = this.selectedRows.length ? this.selectedRows : this.filteredSubmissions;
      if (!target.length) { this.$message.warning('没有可导出的记录'); return; }
      if (type === 'csv') {
        const bom = '\ufeff';
        const headers = ['学号','课程','作业','提交时间','成绩','批改状态','是否过期','附件'];
        const lines = [headers.join(',')];
        target.forEach(r => {
          const files = (r.submissionFiles ? r.submissionFiles.split(',').map(f=>f.split('/').pop()).join(';') : '');
          const graded = this.isRowGraded(r) ? '已批改' : '未批改';
          const expired = this.isRowExpired(r) ? '已过期' : '未过期';
          const row = [r.studentNo || '', (r.courseName || ''), (r.homeworkTitle || ''), (this.formatTime(r.submitTime) || ''), (r.score==null?'':r.score), graded, expired, files];
          lines.push(row.map(v=>('"'+String(v).replace(/"/g,'""')+'"')).join(','));
        });
        const blob = new Blob([bom + lines.join('\n')], { type: 'text/csv;charset=utf-8;' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'homework_submissions_' + this.timestampString() + '.csv';
        a.click();
        URL.revokeObjectURL(a.href);
      } else if (type === 'excel') {
        const headers = ['学号','课程','作业','提交时间','成绩','批改状态','是否过期','附件'];
        let html = '<table><tr>' + headers.map(h=>'<th>'+h+'</th>').join('') + '</tr>';
        target.forEach(r => {
          const files = (r.submissionFiles ? r.submissionFiles.split(',').map(f=>f.split('/').pop()).join(';') : '');
          const graded = this.isRowGraded(r) ? '已批改' : '未批改';
          const expired = this.isRowExpired(r) ? '已过期' : '未过期';
          const row = [r.studentNo || '', (r.courseName || ''), (r.homeworkTitle || ''), (this.formatTime(r.submitTime) || ''), (r.score==null?'':r.score), graded, expired, files];
          html += '<tr>' + row.map(v=>'<td style="mso-number-format:\@">'+String(v).replace(/</g,'&lt;')+'</td>').join('') + '</tr>';
        });
        html += '</table>';
        const blob = new Blob(['\ufeff' + html], { type: 'application/vnd.ms-excel' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'homework_submissions_' + this.timestampString() + '.xls';
        a.click();
        URL.revokeObjectURL(a.href);
      } else {
        this.$message.error('未知导出类型');
      }
    },
    timestampString() {
      const d = new Date();
      const pad = n => String(n).padStart(2,'0');
      return d.getFullYear()+pad(d.getMonth()+1)+pad(d.getDate())+pad(d.getHours())+pad(d.getMinutes())+pad(d.getSeconds());
    },
    buildFullPath(raw) {
      try {
        if (!raw) return ''
        let s = String(raw).trim()
        if (/^https?:\/\//i.test(s)) return s
        if (s.startsWith('/profile/')) return s
        if (s.startsWith('/upload/')) return '/profile' + s
        if (/^\d{4}\//.test(s)) return '/profile/upload/' + s
        if (s.startsWith('profile/upload/')) return '/' + s
        if (s.startsWith('upload/')) return '/profile/' + s
        return '/profile/upload/' + s
      } catch { return String(raw || '') }
    },
    submissionRowKey(row) {
      return row && (row.studentHomeworkId || row.id || `${row.homeworkId || 'hw'}-${row.studentNo || this.studentNo || ''}-${row.submitTime || ''}`)
    },
    printSubmissions() {
      const rows = this.selectedRows.length ? this.selectedRows : this.filteredSubmissions
      if (!rows || !rows.length) { this.$message.info('没有可打印的数据'); return }
      const esc = s => String(s == null ? '' : s).replace(/</g,'&lt;')
      const tr = rows.map(r => {
        const files = (r.submissionFiles ? r.submissionFiles.split(',').map(f=>f.split('/').pop()).join('; ') : '')
        const graded = this.isRowGraded(r) ? '已批改' : '未批改'
        const expired = this.isRowExpired(r) ? '已过期' : '未过期'
        return `<tr>
          <td>${esc(r.studentNo || '')}</td>
          <td>${esc(r.courseName || '')}</td>
          <td>${esc(r.homeworkTitle || '')}</td>
          <td>${esc(this.formatTime(r.submitTime) || '')}</td>
          <td>${esc(r.score == null ? '' : r.score + '分')}</td>
          <td>${graded}</td>
          <td>${expired}</td>
          <td>${esc(files)}</td>
        </tr>`
      }).join('')
      const stats = this.submissionStats
      const win = window.open('', '_blank')
      if (!win) { this.$message.error('打印窗口被拦截'); return }
      win.document.write(`
        <html><head><title>我的提交记录</title>
        <meta charset="utf-8" />
        <style>
          body{font-family:Arial,Helvetica,'Microsoft YaHei';padding:12px}
          table{border-collapse:collapse;width:100%}
          th,td{border:1px solid #999;padding:6px;text-align:left}
          h2{margin:0 0 12px}
          .meta{margin:6px 0 12px;color:#666}
        </style>
        </head><body>
        <h2>我的提交记录</h2>
        <div class="meta">统计：共 ${stats.total} 条；已批改 ${stats.graded}；已过期 ${stats.expired}；平均成绩 ${stats.avgScore}</div>
        <table>
          <thead><tr>
            <th>学号</th><th>课程</th><th>作业</th><th>提交时间</th><th>成绩</th><th>批改状态</th><th>是否过期</th><th>附件</th>
          </tr></thead>
          <tbody>${tr}</tbody>
        </table>
        </body></html>
      `)
      win.document.close()
      win.focus()
      win.print()
    }
  }
}
</script>

<style scoped>
/* 现代化UI样式 - 只修改样式，不改变原有逻辑 */

.homework-upload-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  margin-bottom: 32px;
  text-align: center;
  padding: 20px 0;
}

.header-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  display: block;
  margin-bottom: 8px;
}

.header-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  font-weight: 400;
}

/* 卡片通用样式 */
.homework-upload-page >>> .el-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  background: #ffffff;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.homework-upload-page >>> .el-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.homework-upload-page >>> .el-card__header {
  border-bottom: 1px solid #f1f2f6;
  padding: 16px 20px;
  background: #fafbfc;
}

/* 卡片头部带图标 */
.card-header-with-icon {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
}

.card-header-with-icon i {
  margin-right: 8px;
  font-size: 18px;
  color: #409eff;
}

.card-header-with-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
}

.header-left i {
  margin-right: 8px;
  font-size: 18px;
  color: #409eff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 表单样式 */
.homework-upload-page >>> .el-form-item__label {
  font-weight: 600;
  color: #2c3e50;
}

.homework-upload-page >>> .el-input__inner {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.homework-upload-page >>> .el-input__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.homework-select-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.refresh-btn {
  color: #409eff;
}

/* 按钮样式 */
.homework-upload-page >>> .el-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.homework-upload-page >>> .el-button--primary {
  background: #409eff;
  border-color: #409eff;
}

.homework-upload-page >>> .el-button--primary:hover {
  background: #66b1ff;
  border-color: #66b1ff;
  transform: translateY(-1px);
}

.homework-upload-page >>> .el-button--text {
  color: #409eff;
}

/* 提示框 */
.hint-box {
  background: #f0f9ff;
  border: 1px solid #bee5eb;
  border-radius: 6px;
  padding: 12px 16px;
  margin-top: 16px;
  display: flex;
  align-items: center;
  color: #0c5460;
}

.hint-box i {
  margin-right: 8px;
  color: #409eff;
}

/* 身份确认区域 */
.identity-content {
  padding: 8px 0;
}

.identity-input-group {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.identity-actions {
  display: flex;
  gap: 8px;
}

.identity-status-indicator {
  margin-left: auto;
}

.status-badge {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge.success {
  background: #f0f9ff;
  color: #409eff;
}

.status-badge.warning {
  background: #fff3cd;
  color: #856404;
}

.status-badge i {
  margin-right: 6px;
}

.identity-hint {
  color: #7f8c8d;
  font-size: 13px;
  margin: 8px 0 16px 0;
}

.identity-alert {
  margin-top: 16px;
}

/* 作业详情 */
.upload-title {
  color: #2c3e50;
  font-size: 16px;
  margin: 20px 0 16px 0;
  display: flex;
  align-items: center;
}

.upload-title i {
  margin-right: 8px;
  color: #409eff;
}

.homework-upload-page >>> .el-descriptions__label {
  font-weight: 600;
  color: #2c3e50;
}

.homework-upload-page >>> .el-descriptions__content {
  color: #34495e;
}

.content {
  max-height: 200px;
  overflow: auto;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.overdue {
  color: #f56c6c;
  font-weight: 500;
}

/* 上传区域 */
.upload-tip {
  color: #7f8c8d;
  font-size: 13px;
  margin-top: 8px;
}

.selected-files {
  margin: 16px 0;
}

.file-chip {
  display: flex;
  align-items: center;
  margin: 8px 0;
  flex-wrap: wrap;
  gap: 8px;
}

.submit-row {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

/* 提交记录表格 */
.filter-bar {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 16px;
}

.filter-count {
  color: #409eff;
  font-weight: 500;
  font-size: 13px;
}

.stats-row {
  margin-top: 12px;
}

.submissions-table {
  border-radius: 6px;
  overflow: hidden;
}

.homework-upload-page >>> .el-table th {
  background: #f5f7fa;
  color: #2c3e50;
  font-weight: 600;
}

.homework-upload-page >>> .el-table td {
  border-bottom: 1px solid #f1f2f6;
}

.blocked-panel {
  padding: 20px;
  text-align: center;
}

/* 标签样式 */
.tag-link {
  cursor: pointer;
  margin: 2px;
  transition: all 0.3s ease;
}

.tag-link:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.text-muted {
  color: #7f8c8d;
}

/* 文件标签 */
.file-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 8px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .homework-upload-page {
    padding: 16px;
  }

  .header-title {
    font-size: 24px;
  }

  .identity-input-group {
    flex-direction: column;
    align-items: stretch;
  }

  .identity-actions {
    justify-content: center;
  }

  .identity-status-indicator {
    margin-left: 0;
    justify-content: center;
  }
}
</style>
