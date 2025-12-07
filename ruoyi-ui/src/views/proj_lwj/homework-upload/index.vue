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
            placeholder="自动获取学号"
            style="max-width:260px"
            prefix-icon="el-icon-s-custom"
            disabled
          />
          <div class="identity-status-indicator">
            <div v-if="studentConfirmed" class="status-badge success">
              <i class="el-icon-success"></i>
              <span>已确认: {{ studentNo }}</span>
            </div>
            <div v-else class="status-badge warning">
              <i class="el-icon-warning"></i>
              <span>{{ confirming ? '正在确认...' : '未确认' }}</span>
            </div>
          </div>
        </div>
        <p class="identity-hint">
          学号已从您的登录账号自动获取
        </p>

        <el-alert
          v-if="!studentConfirmed && !confirming"
          type="warning"
          :closable="false"
          show-icon
          title="未获取到学号"
          description="请确保您已登录且已绑定学生身份"
          class="identity-alert"
        />
      </div>
    </el-card>

    <!-- 3. 作业详情与上传 -->
    <el-card v-if="homework && studentConfirmed" class="work-card" :body-style="{ padding: '16px 20px' }">
      <div slot="header" class="work-header">
        <div class="work-title">
          <i class="el-icon-document"></i>
          <span>{{ homework.title || '作业详情' }}</span>
        </div>
        <div class="work-actions">
          <el-button size="mini" type="primary" icon="el-icon-refresh" @click="refreshHomework" :loading="homeworkLoading">刷新详情</el-button>
        </div>
      </div>

      <!-- 状态提示 -->
      <div class="status-alerts">
        <el-alert
          v-if="isSubmissionGraded"
          type="success"
          :closable="false"
          show-icon
          title="该作业已批改"
          :description="`您的成绩: ${currentSubmission.score}分 ${currentSubmission.remark ? '评语: ' + currentSubmission.remark : ''}`"
        />
        <el-alert
          v-else-if="isDeadlinePassed"
          type="warning"
          :closable="false"
          show-icon
          title="已过截止时间"
          description="不能再提交或修改作业"
        />
        <el-alert
          v-else-if="hasExistingSubmission"
          type="info"
          :closable="false"
          show-icon
          title="您已提交过该作业"
          description="可以更新提交或查看当前提交状态"
        />
        <el-alert
          v-else
          type="info"
          :closable="false"
          show-icon
          title="待提交"
          description="请上传作业文件并提交"
        />
      </div>

      <!-- 作业信息 -->
      <div class="homework-info-section">
        <el-descriptions :column="1" size="small" border class="homework-info">
          <el-descriptions-item label="作业标题">{{ homework.title || '—' }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">
            <span :class="{ overdue: isDeadlinePassed }">
              {{ formatTime(homework.deadline) || '—' }}
              <el-tag v-if="isDeadlinePassed" size="mini" type="danger" style="margin-left:8px">已过期</el-tag>
              <el-tag v-else size="mini" type="success" style="margin-left:8px">进行中</el-tag>
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="作业分值">
            <span>{{ homework.totalScore || 100 }} 分</span>
          </el-descriptions-item>
          <el-descriptions-item label="教师附件">
            <span v-if="!parsedHomeworkAttachments.length" class="text-muted">无附件</span>
            <div v-else class="attachment-list">
              <el-tag v-for="(f,i) in parsedHomeworkAttachments" :key="i" size="mini" @click="previewFile(f)" class="tag-link attachment-tag">
                <i class="el-icon-document"></i>
                {{ getFileName(f) }}
              </el-tag>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="作业内容">
            <div class="content-preview" v-html="homework.content || '暂无内容描述'" />
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 上传区域 -->
      <div class="upload-section">
        <h4 class="upload-title">
          <i class="el-icon-upload2"></i>
          上传作业文件
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
          :limit="10"
          :on-exceed="handleExceed"
          list-type="text"
          :disabled="isDeadlinePassed || isSubmissionGraded"
        >
          <el-button size="small" type="primary" :disabled="isDeadlinePassed || isSubmissionGraded">
            <i class="el-icon-upload"></i>
            选择文件
          </el-button>
          <div slot="tip" class="upload-tip">
            支持多文件上传，最多10个文件，单个文件≤50MB
            <span v-if="isDeadlinePassed" style="color:#f56c6c;margin-left:8px">❌ 已过截止时间</span>
            <span v-else-if="isSubmissionGraded" style="color:#67c23a;margin-left:8px">✅ 已批改完成</span>
          </div>
        </el-upload>

        <!-- 上传进度和状态 -->
        <div v-if="fileList.length > 0" class="upload-status">
          <div class="status-header">
            <span>已选择 {{ fileList.length }} 个文件</span>
            <el-button type="text" size="mini" @click="clearAllFiles">清空全部</el-button>
          </div>
          <div class="file-list">
            <div v-for="file in fileList" :key="file.uid" class="file-item">
              <div class="file-info">
                <i class="el-icon-document" style="margin-right:8px;"></i>
                <span class="file-name">{{ file.name }}</span>
                <span class="file-size">({{ formatFileSize(file.size) }})</span>
              </div>
              <div class="file-actions">
                <el-progress
                  v-if="uploadingMap[file.uid] && !failedFilesMap[file.uid]"
                  :percentage="uploadingMap[file.uid]"
                  :status="uploadingMap[file.uid]===100?'success':undefined"
                  style="width:120px; margin:0 12px"
                />
                <el-tag v-if="failedFilesMap[file.uid]" type="danger" size="mini">上传失败</el-tag>
                <el-button v-if="failedFilesMap[file.uid]" type="text" size="mini" @click="retryUpload(file)">重试</el-button>
                <el-button v-else type="text" size="mini" @click="removeFile(file)">移除</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 提交说明 -->
        <div v-if="uploadedFiles.length > 0 && !submitDisabled" class="submit-notice">
          <el-alert
            type="warning"
            :closable="false"
            show-icon
            title="请注意"
            :description='"文件上传完成后，需要手动点击下方\"提交作业\"按钮才能正式提交作业。"'
          />
        </div>

        <!-- 提交按钮 -->
        <div class="submit-actions">
          <el-button
            type="primary"
            :disabled="submitDisabled"
            :loading="submitLoading"
            @click="submitHomework"
            class="submit-btn"
            size="medium"
          >
            <i class="el-icon-check"></i>
            {{ hasExistingSubmission ? '更新提交' : '提交作业' }}
          </el-button>
          <el-button @click="resetUpload" :disabled="submitLoading">
            <i class="el-icon-refresh-left"></i>
            重置
          </el-button>

          <!-- 提交结果提示 -->
          <div v-if="submitResult" class="submit-result" :class="submitResult.type">
            <i :class="submitResult.type === 'success' ? 'el-icon-success' : 'el-icon-error'"></i>
            {{ submitResult.message }}
          </div>
        </div>
      </div>
    </el-card>

    <!-- 作业详情加载提示 -->
    <el-card v-else-if="studentConfirmed && selectionForm.homeworkId && !homework" class="work-card">
      <div slot="header" class="card-header-with-icon">
        <i class="el-icon-document"></i>
        <span>作业详情</span>
      </div>
      <div class="loading-homework">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="正在加载作业详情..."
          description="请稍候，正在获取选中的作业信息"
        />
        <div style="text-align: center; padding: 20px;">
          <i class="el-icon-loading" style="font-size: 24px; color: #409eff;"></i>
          <p style="margin-top: 12px; color: #666;">加载中...</p>
        </div>
      </div>
    </el-card>

    <!-- 4. 我的提交记录 -->
    <el-card class="submissions-card" ref="submissionsCard">
      <div slot="header" class="card-header-with-actions">
        <div class="header-left">
          <i class="el-icon-tickets"></i>
          <span>我的提交记录</span>
          <el-tag v-if="studentConfirmed" size="small" type="info">{{ mySubmissions.length }} 条记录</el-tag>
        </div>
        <div class="header-actions">
          <el-button type="text" @click="loadMySubmissions" :disabled="!studentConfirmed" :loading="mySubmissionsLoading">
            <i class="el-icon-refresh"></i>
            刷新
          </el-button>
          <el-button v-if="studentConfirmed" type="text" @click="toggleSelectAll" :disabled="mySubmissionsLoading || !filteredSubmissions.length">
            {{ allSelected ? '取消全选' : '全选' }}
          </el-button>
          <el-button v-if="studentConfirmed" type="danger" size="mini" :disabled="batchDeleteDisabled" :loading="batchDeleteLoading" @click="batchDeleteSelected">
            <i class="el-icon-delete"></i>
            批量删除
          </el-button>
          <el-dropdown v-if="studentConfirmed" trigger="click" class="export-dropdown">
            <el-button size="mini">
              <i class="el-icon-download"></i>
              导出/打印
              <i class="el-icon-arrow-down el-icon--right" />
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="exportSubmissions('csv')">
                <i class="el-icon-document"></i>
                导出 CSV
              </el-dropdown-item>
              <el-dropdown-item @click.native="exportSubmissions('excel')">
                <i class="el-icon-document"></i>
                导出 Excel
              </el-dropdown-item>
              <el-dropdown-item divided @click.native="printSubmissions">
                <i class="el-icon-printer"></i>
                打印列表
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>

      <!-- 过滤器条 -->
      <div v-if="studentConfirmed" class="filter-bar">
        <el-form :inline="true" size="mini" class="filter-form">
          <el-form-item label="课程">
            <el-select v-model="filterCourseId" placeholder="全部课程" clearable style="width:140px">
              <el-option v-for="c in distinctCourses" :key="c.courseId" :value="c.courseId" :label="c.courseName || ('课程'+c.courseId)" />
            </el-select>
          </el-form-item>
          <el-form-item label="批改状态">
            <el-select v-model="filterGraded" placeholder="全部状态" style="width:120px">
              <el-option label="全部" value="" />
              <el-option label="已批改" value="graded" />
              <el-option label="未批改" value="ungraded" />
            </el-select>
          </el-form-item>
          <el-form-item label="截止状态">
            <el-select v-model="filterDeadline" placeholder="全部状态" style="width:120px">
              <el-option label="全部" value="" />
              <el-option label="已过期" value="expired" />
              <el-option label="未过期" value="active" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button size="mini" type="primary" @click="resetFilters">
              <i class="el-icon-refresh"></i>
              重置
            </el-button>
          </el-form-item>
          <el-form-item>
            <span class="filter-count">匹配 {{ filteredSubmissions.length }} / 总 {{ mySubmissions.length }}</span>
          </el-form-item>
        </el-form>
        <div class="stats-row" v-if="submissionStats">
          <el-alert :closable="false" type="info"
                    :title="`统计：共 ${submissionStats.total} 条记录；已批改 ${submissionStats.graded} 条；已过期 ${submissionStats.expired} 条；平均成绩 ${submissionStats.avgScore} 分`" />
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
        empty-text="暂无提交记录"
      >
        <el-table-column type="selection" width="42" reserve-selection />
        <el-table-column prop="courseName" label="课程" min-width="140" :sortable="true" />
        <el-table-column prop="homeworkTitle" label="作业标题" min-width="200" :sortable="true" show-overflow-tooltip />
        <el-table-column label="提交文件" min-width="240">
          <template #default="{ row }">
            <span v-if="!row.submissionFiles" class="text-muted">无文件</span>
            <div v-else class="file-tags-small">
              <el-tag v-for="(f,i) in parseAttachmentString(row.submissionFiles)" :key="i" size="mini" @click="previewFile(f)" class="tag-link file-tag">
                <i class="el-icon-document"></i>
                {{ getFileName(f) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" :sortable="true">
          <template #default="{ row }">{{ formatTime(row.submitTime) || '—' }}</template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" width="90" :sortable="true" align="center">
          <template #default="{ row }">
            <span v-if="row.score == null" class="text-muted">—</span>
            <el-tag v-else :type="row.score >= 60 ? 'success' : 'danger'" size="small">
              {{ row.score }}分
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.homeworkDeleted" type="danger" size="mini">作业已删除</el-tag>
            <el-tag v-else-if="isRowGraded(row)" type="success" size="mini">已批改</el-tag>
            <el-tag v-else-if="isRowExpired(row)" type="warning" size="mini">已过期</el-tag>
            <el-tag v-else type="info" size="mini">待批改</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center" class-name="action-column">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="mini" :disabled="!rowEditable(row)" @click="openEdit(row)">
                <i class="el-icon-edit"></i>
                修改
              </el-button>
              <el-button type="danger" size="mini" :disabled="!canDeleteRow(row)" @click="deleteRow(row)">
                <i class="el-icon-delete"></i>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑附件弹窗 -->
    <el-dialog
      :visible.sync="editDialogVisible"
      title="修改提交文件"
      width="640px"
      append-to-body
      :modal="false"
      :lock-scroll="false"
      :close-on-click-modal="false"
      custom-class="centered-homework-dialog"
    >
      <div v-if="editingSubmissionRow">
        <div class="edit-dialog-info">
          <p><strong>作业：</strong>{{ editingSubmissionRow.homeworkTitle || ('#'+editingSubmissionRow.homeworkId) }}</p>
          <p><strong>学号：</strong>{{ editingSubmissionRow.studentNo }}</p>
          <p><strong>提交时间：</strong>{{ formatTime(editingSubmissionRow.submitTime) || '—' }}</p>
        </div>

        <el-alert v-if="!rowEditable(editingSubmissionRow)" type="warning" :closable="false" show-icon title="该提交不可修改" style="margin-bottom:12px" />

        <div class="edit-section">
          <h4>当前文件</h4>
          <div v-if="editExistingFiles.length" class="file-tags">
            <el-tag v-for="(f,i) in editExistingFiles" :key="i" size="small" closable @close="removeExistingFile(i)" class="tag-link file-tag" @click="previewFile(f)">
              <i class="el-icon-document"></i>
              {{ getFileName(f) }}
            </el-tag>
          </div>
          <div v-else class="text-muted" style="margin-bottom:8px">暂无文件</div>
        </div>

        <div class="edit-section" v-if="rowEditable(editingSubmissionRow)">
          <h4>新增文件</h4>
          <el-upload
            ref="editUpload"
            class="upload-block"
            :action="uploadUrl"
            :headers="headers"
            :on-success="handleEditUploadSuccess"
            :before-upload="beforeEditUpload"
            :file-list="editFileList"
            multiple
            :limit="5"
            list-type="text"
          >
            <el-button size="small" type="primary">
              <i class="el-icon-upload"></i>
              选择文件
            </el-button>
            <div slot="tip" class="upload-tip">支持多文件上传，最多5个文件，单个文件≤50MB</div>
          </el-upload>
          <div v-if="editNewFiles.length" class="file-tags">
            <el-tag v-for="(f,i) in editNewFiles" :key="i" size="small" closable @close="removeNewFile(i)" class="tag-link file-tag" @click="previewFile(f)">
              <i class="el-icon-document"></i>
              {{ getFileName(f) }}
            </el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeEditDialog">取消</el-button>
        <el-button type="primary" :disabled="saveEditDisabled" :loading="saveEditLoading" @click="saveEdit">
          {{ saveEditLoading ? '保存中...' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listCourse } from '@/api/proj_lw/course'
import { getHomework, submitHomework, updateSubmission, listHomework, getStudentSubmissions, deleteSubmission } from '@/api/proj_lwj/homework'
import { getCurrentStudent } from '@/api/proj_lw/classStudent'
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
      uploadingMap: {},
      failedFilesMap: {},
      homeworkMetaMap: {},
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
      sortOrder: '',
      currentSubmission: null,
      lastConfirmedData: null,
      homeworkLoading: false
    }
  },
  computed: {
    parsedHomeworkAttachments() {
      return this.parseAttachmentString(this.homework && this.homework.attachments)
    },
    hasExistingSubmission() {
      return this.currentSubmission !== null
    },
    isDeadlinePassed() {
      if (!this.homework || !this.homework.deadline) return false
      const d = new Date(this.homework.deadline)
      return !isNaN(d) && d.getTime() < Date.now()
    },
    isSubmissionGraded() {
      return this.currentSubmission && this.isRowGraded(this.currentSubmission)
    },
    submitDisabled() {
      if (!this.studentConfirmed) return true
      if (!this.homeworkId) return true
      if (this.submitLoading) return true
      if (!this.uploadedFiles.length) return true
      if (this.isDeadlinePassed) return true
      if (this.isSubmissionGraded) return true
      const hasUploading = Object.values(this.uploadingMap).some(progress => progress < 100)
      if (hasUploading) return true
      const hasFailed = Object.keys(this.failedFilesMap).length > 0
      if (hasFailed) return true
      return false
    },
    saveEditDisabled() {
      if (!this.editDialogVisible) return true
      if (!this.editingSubmissionRow) return true
      if (!this.rowEditable(this.editingSubmissionRow)) return true
      if (this.saveEditLoading) return true
      return (this.editExistingFiles.length + this.editNewFiles.length) === 0
    },
    allSelected() {
      return this.selectedRows.length && this.selectedRows.length === this.filteredSubmissions.length
    },
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
      const map = {}
      this.mySubmissions.forEach(r => {
        if (r.courseId && !map[r.courseId]) {
          map[r.courseId] = { courseId: r.courseId, courseName: r.courseName }
        }
      })
      return Object.values(map).sort((a,b) => String(a.courseName).localeCompare(String(b.courseName)))
    },
    filteredSubmissions() {
      let arr = this.mySubmissions.slice()
      if (this.filterCourseId) {
        arr = arr.filter(r => String(r.courseId) === String(this.filterCourseId))
      }
      if (this.filterGraded === 'graded') {
        arr = arr.filter(r => this.isRowGraded(r))
      } else if (this.filterGraded === 'ungraded') {
        arr = arr.filter(r => !this.isRowGraded(r))
      }
      if (this.filterDeadline === 'expired') {
        arr = arr.filter(r => this.isRowExpired(r))
      } else if (this.filterDeadline === 'active') {
        arr = arr.filter(r => !this.isRowExpired(r))
      }
      if (this.sortProp) {
        const prop = this.sortProp
        const order = this.sortOrder === 'descending' ? -1 : 1
        arr.sort((a,b) => {
          let va = a[prop]
          let vb = b[prop]
          if (prop === 'submitTime') {
            va = va ? new Date(va).getTime() : 0
            vb = vb ? new Date(vb).getTime() : 0
          }
          if (prop === 'score') {
            va = va == null ? -Infinity : Number(va)
            vb = vb == null ? -Infinity : Number(vb)
          }
          if (va == null && vb == null) return 0
          if (va == null) return -order
          if (vb == null) return order
          if (typeof va === 'number' && typeof vb === 'number') return (va - vb) * order
          return String(va).localeCompare(String(vb), 'zh-CN') * order
        })
      }
      return arr
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
  async created() {
    // 加载课程列表
    await this.loadCourses()
    // 从当前登录用户自动获取学号并确认身份
    await this.fetchAndConfirmCurrentStudent()
  },
  watch: {
    'selectionForm.homeworkId': {
      handler(newHomeworkId, oldHomeworkId) {
        if (newHomeworkId && newHomeworkId !== oldHomeworkId) {
          console.log('作业ID变化，重新加载详情:', newHomeworkId)
          this.loadHomeworkDetail(newHomeworkId)
        }
      },
      immediate: false
    }
  },
  methods: {
    async fetchAndConfirmCurrentStudent() {
      try {
        const res = await getCurrentStudent()
        if (res && res.data && res.data.studentNo) {
          this.studentNo = res.data.studentNo
          console.log('[HW] 自动获取学号:', this.studentNo)

          // 自动确认身份并加载提交记录
          await this.autoConfirmIdentity()
        } else {
          this.$message.warning('未获取到学号信息，请确保已绑定学生身份')
        }
      } catch(e) {
        console.error('获取学号失败:', e)
        this.$message.error('获取学号失败，请确保已登录')
      }
    },

    async autoConfirmIdentity() {
      if (!this.studentNo) return

      this.confirming = true
      try {
        this.mySubmissionsLoading = true
        const res = await getStudentSubmissions(this.studentNo)

        let list = []
        if (Array.isArray(res)) {
          list = res
        } else if (res && Array.isArray(res.rows)) {
          list = res.rows
        } else if (res && Array.isArray(res.data)) {
          list = res.data
        }

        const normalized = list.map(this.normalizeSubmission)
        this.mySubmissions = normalized
        this.allSubmissions = normalized
        this.studentConfirmed = true
        this.submissionsLoaded = true

        console.log('[HW] 学号自动确认成功，提交记录数:', list.length)
      } catch (e) {
        console.error('加载提交记录失败', e)
        this.mySubmissions = []
        this.studentConfirmed = false
      } finally {
        this.mySubmissionsLoading = false
        this.confirming = false
      }
    },

    async loadCourses() {
      try {
        const res = await listCourse()
        this.courses = res.rows || []
      } catch (e) {
        console.error('加载课程失败', e)
        this.$message.error('课程列表加载失败')
      }
    },

    async loadHomeworkDetail(homeworkId) {
      if (!homeworkId) {
        this.homework = null
        return
      }

      this.homeworkLoading = true
      try {
        const res = await getHomework(homeworkId)
        this.homework = res.data || res || null
        this.checkCurrentSubmission()
      } catch (e) {
        console.error('加载作业详情失败', e)
        this.homework = null
        this.$message.error('作业详情加载失败')
      } finally {
        this.homeworkLoading = false
      }
    },

    async refreshHomeworkDetail() {
      if (this.homeworkId) {
        await this.loadHomeworkDetail(this.homeworkId)
        this.$message.success('作业详情已刷新')
      }
    },

    // Alias for template compatibility
    refreshHomework() {
      return this.refreshHomeworkDetail()
    },

    async confirmIdentity() {
      console.log('[HW] confirmIdentity triggered studentNo=', this.studentNo)
      if (!this.studentNo) {
        this.$message.warning('请输入学号')
        return
      }

      this.confirming = true
      try {
        this.mySubmissionsLoading = true
        const res = await getStudentSubmissions(this.studentNo)

        let list = []
        if (Array.isArray(res)) {
          list = res
        } else if (res && Array.isArray(res.rows)) {
          list = res.rows
        } else if (res && Array.isArray(res.data)) {
          list = res.data
        }

        const normalized = list.map(this.normalizeSubmission)
        this.mySubmissions = normalized
        this.allSubmissions = normalized
        this.studentConfirmed = true
        this.submissionsLoaded = true

        this.checkCurrentSubmission()

        this.saveRememberedData()

        this.$message.success('学号确认成功')
        this.scrollToSubmissions()
      } catch (e) {
        console.error('加载提交记录失败', e)
        this.mySubmissions = []
        this.studentConfirmed = false
        this.$message.error('学号确认失败: ' + (e.message || '网络错误'))
      } finally {
        this.mySubmissionsLoading = false
        this.confirming = false
      }
    },

    loadRememberedData() {
      try {
        // 只恢复 studentNo 到输入框，不做自动确认
        const savedNo = localStorage.getItem('hwUploadStudentNo')
        this.studentNo = savedNo ? String(savedNo) : ''

        // 不恢复 courseId / sessionId / homeworkId
        this.selectionForm = { courseId: null, sessionId: null, homeworkId: null }

        // 不设置 studentConfirmed，不恢复历史提交
        this.lastConfirmedData = null
      } catch (e) {
        console.warn('加载记忆数据失败:', e)
      }
    },

    async restoreConfirmedState() {
      // 禁用自动恢复确认状态
      return
    },

    checkCurrentSubmission() {
      if (!this.homeworkId || !this.allSubmissions.length) {
        this.currentSubmission = null
        return
      }

      console.log('检查当前作业提交状态，作业ID:', this.homeworkId)
      console.log('所有提交记录:', this.allSubmissions)

      const submission = this.allSubmissions.find(r =>
        r.homeworkId && String(r.homeworkId) === String(this.homeworkId)
      )

      console.log('找到的提交记录:', submission)
      this.currentSubmission = submission || null
    },

    resetIdentity() {
      this.studentConfirmed = false
      this.mySubmissions = []
      this.allSubmissions = []
      this.submissionsLoaded = false
      this.uploadedFiles = []
      this.fileList = []
      this.currentSubmission = null
      this.studentNo = ''
      this.submitResult = null
      this.lastConfirmedData = null

      try {
        localStorage.removeItem('hwUploadStudentNo')
        localStorage.removeItem('hwUploadConfirmedData')
      } catch (e) {}

      this.$message.info('已重置学号信息')
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
        remark: sub.remark || '',
        gradedTime: sub.gradedTime,
        homeworkDeleted: sub.homeworkDeleted,
        studentNo: sub.studentNo || this.studentNo,
        status: sub.status != null ? Number(sub.status) : null
      }
    },

    async fetchCourses() {
      try {
        const res = await listCourse({ pageNum: 1, pageSize: 1000 })
        this.courses = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
      } catch (e) {
        console.error('获取课程失败', e)
        this.$message.error('课程加载失败')
      }
    },

    async loadSessions(courseId) {
      try {
        const api = require('@/api/proj_lw/session')
        const res = await api.getSessionsByCourseId(courseId)
        this.sessions = res && res.rows ? res.rows : (res && res.data ? res.data : [])
      } catch (e) {
        console.error('获取课堂失败', e)
        this.sessions = []
        this.$message.error('课堂加载失败')
      }
    },

    async onCourseChange(courseId) {
      console.log('[HW] course change', courseId)
      this.saveRememberedData()

      this.selectionForm.sessionId = null
      this.selectionForm.homeworkId = null
      this.homework = null
      this.homeworkId = null
      this.sessions = []
      this.homeworkList = []
      this.currentSubmission = null

      if (!courseId) return
      await this.loadSessions(courseId)
    },

    async onSessionChange(sessionId) {
      console.log('[HW] session change', sessionId)
      this.saveRememberedData()

      this.selectionForm.homeworkId = null
      this.homework = null
      this.homeworkId = null
      this.homeworkList = []
      this.currentSubmission = null

      if (!sessionId) return
      await this.refreshHomeworkList()
    },

    async refreshHomeworkList() {
      const sessionId = this.selectionForm.sessionId
      if (!sessionId) return

      try {
        const res = await listHomework({ sessionId, pageNum: 1, pageSize: 100 })
        this.homeworkList = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])

        if (this.homeworkList.length === 0) {
          this.$message.info('该课堂暂无作业')
        }
      } catch (e) {
        console.error('获取作业失败', e)
        this.homeworkList = []
        this.$message.error('作业列表加载失败')
      }
    },

    onHomeworkChange(homeworkId) {
      console.log('[HW] homework change', homeworkId)
      this.saveRememberedData()

      // Set the homeworkId for loading
      this.homeworkId = homeworkId

      // The watcher will automatically trigger loadHomeworkDetail
      // No need to call it here since the watch handler will do it
    },

    saveRememberedData() {
      try {
        if (this.studentNo) {
          localStorage.setItem('hwUploadStudentNo', this.studentNo)
        }

        if (this.selectionForm.courseId) {
          localStorage.setItem('hwUploadLastCourseId', String(this.selectionForm.courseId))
        }
        if (this.selectionForm.sessionId) {
          localStorage.setItem('hwUploadLastSessionId', String(this.selectionForm.sessionId))
        }
        if (this.selectionForm.homeworkId) {
          localStorage.setItem('hwUploadLastHomeworkId', String(this.selectionForm.homeworkId))
        }

        if (this.studentConfirmed && this.studentNo) {
          const confirmedData = {
            studentNo: this.studentNo,
            timestamp: Date.now(),
            submissionsCount: this.mySubmissions.length
          }
          localStorage.setItem('hwUploadConfirmedData', JSON.stringify(confirmedData))
          this.lastConfirmedData = confirmedData
        }
      } catch (e) {
        console.warn('保存记忆数据失败:', e)
      }
    },

    beforeUpload(file) {
      const maxSize = 50 * 1024 * 1024
      if (file.size > maxSize) {
        this.$message.error(`文件 "${file.name}" 不能超过50MB`)
        return false
      }
      if (!this.studentConfirmed) {
        this.$message.warning('请先确认学号')
        return false
      }
      if (this.isDeadlinePassed) {
        this.$message.error('已过截止时间，不能再上传')
        return false
      }
      if (this.isSubmissionGraded) {
        this.$message.error('作业已批改，不能再上传')
        return false
      }
      return true
    },

    handleExceed(files, fileList) {
      this.$message.warning(`最多只能上传 10 个文件，当前选择了 ${files.length} 个文件，共 ${files.length + fileList.length} 个文件`)
    },

    handleUploadProgress(event, file, fileList) {
      this.$set(this.uploadingMap, file.uid, Math.min(99, Math.floor(event.percent)))
    },

    handleUploadError(err, file, fileList) {
      console.error('[HW] upload error', file.name, err)
      this.$set(this.failedFilesMap, file.uid, true)
      this.$delete(this.uploadingMap, file.uid)
      this.$message.error(`文件 "${file.name}" 上传失败`)
    },

    handleUploadSuccess(resp, file) {
      if (this.isDeadlinePassed || this.isSubmissionGraded) return

      const rawName = this.getUploadedFileName(resp, file)
      if (!rawName) {
        this.$message.error('上传失败：未返回文件名')
        return
      }

      const fullPath = this.buildFullPath(rawName)
      if (!this.uploadedFiles.includes(fullPath)) {
        this.uploadedFiles.push(fullPath)
      }

      const found = this.fileList.find(f => f.uid === file.uid)
      if (found) {
        found.name = fullPath
      } else {
        this.fileList.push({ ...file, name: fullPath })
      }

      this.$set(this.uploadingMap, file.uid, 100)
      this.$delete(this.failedFilesMap, file.uid)

      console.log('[HW] upload success -> raw:', rawName, 'full:', fullPath)
      this.$message.success(`文件 "${file.name}" 上传成功`)
    },

    getUploadedFileName(resp, file) {
      try {
        if (!resp) return file && file.name
        const candidates = [resp.fileName, resp.filename, resp.name, resp.originalFilename, resp.originalName, resp.url]
        for (const c of candidates) {
          if (c && typeof c === 'string') return c
        }
        if (resp.data && typeof resp.data === 'object') {
          const d = resp.data
          const inner = [d.fileName, d.filename, d.name]
          for (const c of inner) {
            if (c && typeof c === 'string') return c
          }
        }
        return file && file.name
      } catch {
        return file && file.name
      }
    },

    removeFile(file) {
      this.handleRemove(file)
    },

    clearAllFiles() {
      this.resetUpload()
      this.$message.info('已清空所有文件')
    },

    handleRemove(file) {
      const name = file && file.name
      this.uploadedFiles = this.uploadedFiles.filter(n => n !== name)
      this.fileList = this.fileList.filter(f => f.uid !== file.uid)
      this.$delete(this.uploadingMap, file.uid)
      this.$delete(this.failedFilesMap, file.uid)
      console.log('[HW] removed file', name)
    },

    retryUpload(file) {
      if (!file || !file.raw) {
        this.$message.error('无法重试：原始文件数据缺失')
        return
      }
      this.$delete(this.failedFilesMap, file.uid)
      this.$set(this.uploadingMap, file.uid, 0)

      const request = require('@/utils/request').default
      const form = new FormData()
      form.append('file', file.raw)

      request({
        url: this.uploadUrl,
        method: 'post',
        data: form,
        headers: this.headers
      }).then(resp => {
        this.handleUploadSuccess(resp, file)
      }).catch(err => {
        console.error('[HW] retry failed', err)
        this.handleUploadError(err, file)
      })
    },

    async submitHomework() {
      if (this.submitDisabled) return

      if (this.isDeadlinePassed) {
        this.$message.error('已过截止时间，禁止提交')
        return
      }

      if (this.isSubmissionGraded) {
        this.$message.error('该作业已批改，禁止修改')
        return
      }

      // 添加确认对话框
      const action = this.hasExistingSubmission ? '重新提交' : '提交'
      const confirmResult = await this.$confirm(
        `确认要${action}作业吗？\n\n已上传文件数量：${this.uploadedFiles.length} 个\n作业：${this.homework ? this.homework.title : ''}`,
        `确认${action}`,
        {
          confirmButtonText: `确认${action}`,
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).catch(() => false)

      if (!confirmResult) {
        return
      }

      const failedFiles = Object.keys(this.failedFilesMap)
      if (failedFiles.length > 0) {
        this.$message.error('有文件上传失败，请先解决上传问题')
        return
      }

      const uploading = Object.values(this.uploadingMap).some(progress => progress < 100)
      if (uploading) {
        this.$message.warning('有文件正在上传，请等待上传完成')
        return
      }

      this.submitLoading = true
      this.submitResult = null

      try {
        const submissionFiles = this.uploadedFiles.map(f => this.buildFullPath(f)).join(',')
        const basePayload = {
          homeworkId: this.homeworkId,
          studentNo: this.studentNo,
          submissionFiles
        }

        let res
        if (this.hasExistingSubmission && this.currentSubmission.studentHomeworkId) {
          res = await updateSubmission({
            ...basePayload,
            studentHomeworkId: this.currentSubmission.studentHomeworkId,
            id: this.currentSubmission.studentHomeworkId
          })
        } else {
          res = await submitHomework(basePayload)
        }

        const ok = res && (res.code === 0 || res.code === 200 || res.success === true)
        if (!ok) {
          throw new Error((res && res.msg) || '提交失败')
        }

        this.submitResult = {
          type: 'success',
          message: this.hasExistingSubmission ? '作业更新成功' : '作业提交成功'
        }

        await this.loadMySubmissions()
        this.resetUpload()
        this.scrollToSubmissions()

        this.$message.success(this.submitResult.message)
      } catch (e) {
        console.error('[HW] submit error', e)
        this.submitResult = {
          type: 'error',
          message: e.message || '提交失败，请重试'
        }
        this.$message.error(this.submitResult.message)
      } finally {
        this.submitLoading = false
      }
    },

    resetUpload() {
      this.uploadedFiles = []
      this.fileList = []
      this.uploadingMap = {}
      this.failedFilesMap = {}
      if (this.$refs.upload) {
        this.$refs.upload.clearFiles()
      }
    },

    scrollToSubmissions() {
      this.$nextTick(() => {
        const card = this.$refs.submissionsCard && this.$refs.submissionsCard.$el
        if (card) {
          card.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      })
    },

    async loadMySubmissions() {
      if (!this.studentConfirmed || !this.studentNo) return
      await this.confirmIdentity()
    },

    parseAttachmentString(str) {
      if (!str) return []
      return String(str).split(',').map(s => s.trim()).filter(Boolean)
    },

    formatTime(ts) {
      if (!ts && ts !== 0) return ''
      try {
        let n = typeof ts === 'number' ? ts : Number(ts)
        if (isNaN(n)) {
          const dObj = new Date(ts)
          return isNaN(dObj.getTime()) ? '' : dObj.toLocaleString('zh-CN')
        }
        if (String(n).length === 10) n *= 1000
        const d = new Date(n)
        return isNaN(d.getTime()) ? '' : d.toLocaleString('zh-CN')
      } catch {
        return ''
      }
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    getFileName(filePath) {
      return filePath.split('/').pop() || filePath
    },

    previewFile(path) {
      if (!path) return

      const request = require('@/utils/request').default
      const token = getToken()
      const headers = { Authorization: 'Bearer ' + token, isToken: true }
      const norm = String(path).replace(/\\/g, '/').trim()
      const filename = decodeURIComponent(norm.split('/').pop())

      const downloadBlob = url => request({
        url,
        method: 'get',
        responseType: 'blob',
        headers
      }).then(blob => {
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

      if (norm.startsWith('/profile')) {
        tryProfile().catch(() => this.$message.error('文件下载失败'))
      } else if (/^https?:\/\//i.test(norm)) {
        window.open(norm, '_blank')
      } else {
        downloadBlob(`/common/download?fileName=${encodeURIComponent(norm)}&delete=false`).catch(() => {
          this.$message.error('文件下载失败')
        })
      }
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
      return row && Number(row.status) === 2
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
      if (!this.rowEditable(row)) {
        this.$message.warning('该提交记录不可修改')
        return
      }

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
      const maxSize = 50 * 1024 * 1024
      if (file.size > maxSize) {
        this.$message.error('文件不能超过50MB')
        return false
      }
      return true
    },

    handleEditUploadSuccess(resp, file) {
      if (!this.rowEditable(this.editingSubmissionRow)) return

      const rawName = this.getUploadedFileName(resp, file)
      if (!rawName) {
        this.$message.error('上传失败：未返回文件名')
        return
      }

      const fullPath = this.buildFullPath(rawName)
      if (!this.editNewFiles.includes(fullPath)) {
        this.editNewFiles.push(fullPath)
      }

      const found = this.editFileList.find(f => f.uid === file.uid)
      if (found) {
        found.name = fullPath
      } else {
        this.editFileList.push({ ...file, name: fullPath })
      }

      this.$message.success(`文件 "${file.name}" 上传成功`)
    },

    removeExistingFile(i) {
      this.editExistingFiles.splice(i, 1)
    },

    removeNewFile(i) {
      this.editNewFiles.splice(i, 1)
    },

    async saveEdit() {
      if (this.saveEditDisabled) return

      this.saveEditLoading = true
      try {
        const row = this.editingSubmissionRow
        const files = [...this.editExistingFiles, ...this.editNewFiles]

        if (files.length === 0) {
          this.$message.warning('请至少选择一个文件')
          return
        }

        const payload = {
          studentHomeworkId: row.studentHomeworkId,
          homeworkId: row.homeworkId,
          studentNo: row.studentNo,
          submissionFiles: files.join(',')
        }

        const res = await updateSubmission(payload)
        const ok = res && (res.code === 0 || res.code === 200 || res.success === true)
        if (!ok) {
          throw new Error((res && res.msg) || '保存失败')
        }

        this.$message.success('修改保存成功')
        await this.loadMySubmissions()
        this.closeEditDialog()
      } catch (e) {
        console.error('saveEdit error', e)
        this.$message.error(e.message || '保存失败')
      } finally {
        this.saveEditLoading = false
      }
    },

    async deleteRow(row) {
      if (!this.canDeleteRow(row)) {
        this.$message.warning('该提交记录不可删除')
        return
      }

      try {
        await this.$confirm('确认删除该提交记录及其附件引用？', '提示', {
          type: 'warning',
          confirmButtonText: '确认删除',
          cancelButtonText: '取消'
        })
      } catch {
        return
      }

      try {
        const res = await deleteSubmission(row.studentHomeworkId)
        const ok = res && (res.code === 0 || res.code === 200 || res.success === true)
        if (!ok) {
          throw new Error((res && res.msg) || '删除失败')
        }

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

      if (!this.allSelected) {
        this.selectedRows = [...this.filteredSubmissions]
      } else {
        this.selectedRows = []
      }
    },

    async batchDeleteSelected() {
      if (this.batchDeleteDisabled) return

      const deletable = this.deletableSelectedIds
      const blocked = this.selectedRows.filter(r => !this.canDeleteRow(r)).map(r => r.studentHomeworkId)

      let msg = `确认删除 ${deletable.length} 条记录？`
      if (blocked.length) {
        msg += ` (已自动排除 ${blocked.length} 条不可删除的记录)`
      }

      try {
        await this.$confirm(msg, '批量删除', {
          type: 'warning',
          confirmButtonText: '确认删除',
          cancelButtonText: '取消'
        })
      } catch {
        return
      }

      if (!deletable.length) return

      this.batchDeleteLoading = true
      const results = []

      for (const id of deletable) {
        try {
          const res = await require('@/api/proj_lwj/homework').deleteSubmission(id)
          const ok = res && (res.code === 0 || res.code === 200 || res.success === true)
          results.push({ id, ok })
        } catch (e) {
          results.push({ id, ok: false, err: e })
        }
      }

      this.batchDeleteLoading = false
      const failed = results.filter(r => !r.ok)

      if (failed.length) {
        this.$message.error(`删除完成，但 ${failed.length} 条记录删除失败`)
      } else {
        this.$message.success('批量删除成功')
      }

      await this.loadMySubmissions()
      this.selectedRows = []
    },

    onSortChange({ prop, order }) {
      this.sortProp = prop
      this.sortOrder = order
    },

    resetFilters() {
      this.filterCourseId = ''
      this.filterGraded = ''
      this.filterDeadline = ''
      this.sortProp = ''
      this.sortOrder = ''
      this.selectedRows = []
    },

    exportSubmissions(type) {
      const target = this.selectedRows.length ? this.selectedRows : this.filteredSubmissions
      if (!target.length) {
        this.$message.warning('没有可导出的记录')
        return
      }

      if (type === 'csv') {
        this.exportToCSV(target)
      } else if (type === 'excel') {
        this.exportToExcel(target)
      } else {
        this.$message.error('未知导出类型')
      }
    },

    exportToCSV(target) {
      const bom = '\ufeff'
      const headers = ['学号', '课程', '作业', '提交时间', '成绩', '批改状态', '是否过期', '附件']
      const lines = [headers.join(',')]

      target.forEach(r => {
        const files = r.submissionFiles ?
          r.submissionFiles.split(',').map(f => this.getFileName(f)).join(';') : ''
        const graded = this.isRowGraded(r) ? '已批改' : '未批改'
        const expired = this.isRowExpired(r) ? '已过期' : '未过期'
        const row = [
          r.studentNo || '',
          r.courseName || '',
          r.homeworkTitle || '',
          this.formatTime(r.submitTime) || '',
          r.score == null ? '' : r.score,
          graded,
          expired,
          files
        ]
        lines.push(row.map(v => ('"' + String(v).replace(/"/g, '""') + '"')).join(','))
      })

      const blob = new Blob([bom + lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `作业提交记录_${this.timestampString()}.csv`
      a.click()
      URL.revokeObjectURL(a.href)
    },

    exportToExcel(target) {
      const headers = ['学号', '课程', '作业', '提交时间', '成绩', '批改状态', '是否过期', '附件']
      let html = '<table><tr>' + headers.map(h => '<th>' + h + '</th>').join('') + '</tr>'

      target.forEach(r => {
        const files = r.submissionFiles ?
          r.submissionFiles.split(',').map(f => this.getFileName(f)).join('; ') : ''
        const graded = this.isRowGraded(r) ? '已批改' : '未批改'
        const expired = this.isRowExpired(r) ? '已过期' : '未过期'
        const row = [
          r.studentNo || '',
          r.courseName || '',
          r.homeworkTitle || '',
          this.formatTime(r.submitTime) || '',
          r.score == null ? '' : r.score,
          graded,
          expired,
          files
        ]
        html += '<tr>' + row.map(v => '<td style="mso-number-format:\\@">' + String(v).replace(/</g, '&lt;') + '</td>').join('') + '</tr>'
      })

      html += '</table>'
      const blob = new Blob(['\ufeff' + html], { type: 'application/vnd.ms-excel' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `作业提交记录_${this.timestampString()}.xls`
      a.click()
      URL.revokeObjectURL(a.href)
    },

    timestampString() {
      const d = new Date()
      const pad = n => String(n).padStart(2, '0')
      return d.getFullYear() + pad(d.getMonth() + 1) + pad(d.getDate()) + pad(d.getHours()) + pad(d.getMinutes()) + pad(d.getSeconds())
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
      } catch {
        return String(raw || '')
      }
    },

    submissionRowKey(row) {
      return row && (row.studentHomeworkId || row.id || `${row.homeworkId || 'hw'}-${row.studentNo || this.studentNo || ''}-${row.submitTime || ''}`)
    },

    printSubmissions() {
      const rows = this.selectedRows.length ? this.selectedRows : this.filteredSubmissions
      if (!rows || !rows.length) {
        this.$message.info('没有可打印的数据')
        return
      }

      const esc = s => String(s == null ? '' : s).replace(/</g, '&lt;')
      const tr = rows.map(r => {
        const files = r.submissionFiles ?
          r.submissionFiles.split(',').map(f => this.getFileName(f)).join('; ') : ''
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
      if (!win) {
        this.$message.error('打印窗口被拦截')
        return
      }

      win.document.write(`
        <html><head><title>我的提交记录</title>
        <meta charset="utf-8" />
        <style>
          body{font-family:Arial,Helvetica,'Microsoft YaHei';padding:20px}
          table{border-collapse:collapse;width:100%;margin-top:12px}
          th,td{border:1px solid #999;padding:8px;text-align:left}
          th{background:#f5f5f5}
          h2{margin:0 0 12px}
          .meta{margin:6px 0 16px;color:#666;font-size:14px}
          @media print {
            body{padding:0}
            .no-print{display:none}
          }
        </style>
        </head><body>
        <h2>我的作业提交记录</h2>
        <div class="meta">统计：共 ${stats.total} 条记录；已批改 ${stats.graded} 条；已过期 ${stats.expired} 条；平均成绩 ${stats.avgScore} 分</div>
        <table>
          <thead><tr>
            <th>学号</th><th>课程</th><th>作业</th><th>提交时间</th><th>成绩</th><th>批改状态</th><th>是否过期</th><th>附件</th>
          </tr></thead>
          <tbody>${tr}</tbody>
        </table>
        <div class="no-print" style="margin-top:20px;text-align:center">
          <button onclick="window.print()" style="padding:8px 16px">打印</button>
          <button onclick="window.close()" style="padding:8px 16px;margin-left:12px">关闭</button>
        </div>
        </body></html>
      `)
      win.document.close()
      win.focus()
    }
  }
}
</script>

<style scoped>
/* ============ 页面整体布局 ============ */
.homework-upload-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  background: #f5f7fa;
  min-height: 100vh;
}

/* ============ 卡片样式 ============ */
.selection-card,
.identity-card,
.work-card,
.submissions-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.selection-card:hover,
.identity-card:hover,
.work-card:hover,
.submissions-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 卡片头部图标 */
.card-header-with-icon {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-header-with-icon i {
  font-size: 22px;
  margin-right: 10px;
  color: #409EFF;
}

/* ============ 提示框样式 ============ */
.hint-box {
  padding: 16px;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f9ff 100%);
  border-left: 4px solid #409EFF;
  border-radius: 8px;
  display: flex;
  align-items: center;
  margin-top: 16px;
}

.hint-box i {
  font-size: 20px;
  color: #409EFF;
  margin-right: 12px;
}

.hint-box span {
  color: #606266;
  font-size: 14px;
}

/* ============ 身份确认区域 ============ */
.identity-content {
  padding: 12px 0;
}

.identity-input-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.identity-actions {
  display: flex;
  gap: 8px;
}

.confirm-btn,
.reset-btn {
  min-width: 100px;
  font-weight: 500;
}

.identity-status-indicator {
  flex: 1;
  display: flex;
  justify-content: flex-start;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.status-badge.success {
  background: linear-gradient(135deg, #e7f9f0 0%, #d4f4e3 100%);
  color: #67C23A;
  border: 1px solid #c2e7b0;
}

.status-badge.warning {
  background: linear-gradient(135deg, #fff4e6 0%, #ffe7cc 100%);
  color: #E6A23C;
  border: 1px solid #f5d9a8;
}

.status-badge i {
  margin-right: 6px;
  font-size: 16px;
}

.identity-hint {
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}

.identity-alert {
  margin-top: 16px;
}

/* ============ 作业详情区域 ============ */
.work-card {
  background: linear-gradient(135deg, #ffffff 0%, #f9fbff 100%);
}

.work-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.work-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.work-title i {
  color: #67C23A;
}

.work-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-alerts {
  margin: 8px 0 12px;
}

.homework-info-section {
  padding: 8px 0;
}

.homework-info >>> .el-descriptions__header {
  font-weight: 600;
}

.homework-info >>> .el-descriptions__body {
  background: #fafafa;
}

/* ============ 上传区域样式 ============ */
.homework-select-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.refresh-btn {
  padding: 8px 12px;
  color: #409EFF;
  transition: all 0.3s;
}

.refresh-btn:hover {
  background: #ecf5ff;
  color: #67b1ff;
}

.upload-section {
  margin-top: 8px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
}

.upload-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
}

.upload-block {
  border: 1px dashed #c0c4cc;
  border-radius: 8px;
  padding: 12px;
  background: #fcfdff;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.upload-status {
  margin-top: 12px;
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 8px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  font-weight: 500;
  color: #606266;
}

.file-size {
  color: #909399;
  font-size: 12px;
}

.file-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.file-tag {
  cursor: pointer;
}

.tag-link {
  border: 1px solid #409EFF;
  color: #409EFF;
  background: #f0f9ff;
}

.tag-link:hover {
  background: #409EFF;
  color: #fff;
}

/* ============ 提交记录区域 ============ */
.submissions-card {
  background: white;
}

.submission-stats {
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f4fd 100%);
  border-radius: 8px;
  margin-bottom: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-top: 12px;
}

.stat-item {
  text-align: center;
  padding: 12px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #409EFF;
  display: block;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

/* ============ 按钮美化 ============ */
.el-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s;
}

.el-button--primary {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409EFF 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.el-button--success {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
  border: none;
}

.el-button--success:hover {
  background: linear-gradient(135deg, #85ce61 0%, #67C23A 100%);
}

.el-button--danger {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  border: none;
}

.el-button--danger:hover {
  background: linear-gradient(135deg, #f78989 0%, #F56C6C 100%);
}

/* ============ 加载样式 ============ */
.loading-homework {
  padding: 40px 20px;
  text-align: center;
}

.loading-homework .el-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

/* ============ 响应式设计 ============ */
@media (max-width: 768px) {
  .homework-upload-page {
    padding: 12px;
  }

  .identity-input-group {
    flex-direction: column;
    align-items: stretch;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

<style>
/* 全局样式 - 确保作业上传弹窗居中在用户屏幕中间（无 scoped） */
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
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.centered-homework-dialog .el-dialog__header {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  color: white;
  padding: 20px 24px;
  border-radius: 12px 12px 0 0;
}

.centered-homework-dialog .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.centered-homework-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white;
  font-size: 20px;
}

.centered-homework-dialog .el-dialog__body {
  padding: 24px;
}

.centered-homework-dialog .el-dialog__footer {
  padding: 16px 24px;
  background: #f5f7fa;
  border-radius: 0 0 12px 12px;
}
</style>

