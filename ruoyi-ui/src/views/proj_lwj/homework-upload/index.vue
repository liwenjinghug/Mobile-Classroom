<template>
  <div class="app-container homework-upload-page">
    <el-card>
      <div slot="header" class="clearfix">
        <span style="line-height:36px;font-weight:600">作业提交</span>
        <el-button v-if="homeworkId" style="float:right;margin-top:4px" size="small" type="text" @click="chooseAnother">选择其他作业</el-button>
      </div>

      <!-- If no homeworkId provided, let user pick one -->
      <div v-if="!homeworkId && !loading" style="margin-bottom:12px">
        <el-alert title="未选择作业，请从下拉列表选择要提交的作业" type="warning" show-icon />
        <div style="display:flex;gap:8px;align-items:center;margin-top:8px">
          <el-select v-model="selectedHomeworkId" placeholder="选择作业" style="min-width:340px">
            <el-option v-for="h in homeworkList" :key="h.homeworkId" :label="h.title" :value="h.homeworkId" />
          </el-select>
          <el-button type="primary" @click="loadSelectedHomework" :disabled="!selectedHomeworkId">加载作业</el-button>
          <el-button @click="fetchHomeworkList">刷新列表</el-button>
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
            <el-input v-model="studentId" placeholder="请输入学号 (studentId)" style="width:260px"></el-input>
            <div style="color:#888">(提交时需要填写学号)</div>
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
            <div style="font-weight:600;margin-bottom:6px">提交记录</div>
            <el-table :data="submissions" stripe style="width:100%">
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
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">{{ submissionStatusText(row.status) }}</template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getHomework, submitHomework, getSubmissions, listHomework } from '@/api/proj_lwj/homework'
import { getToken } from '@/utils/auth'

export default {
  name: 'HomeworkUpload',
  data() {
    return {
      loading: true,
      homeworkId: null,
      homeworkList: [],
      selectedHomeworkId: null,
      homework: {},
      fileList: [], // el-upload fileList
      uploadedFiles: [], // array of filenames returned by server
      studentId: '', // 学号，提交时必填
      submitLoading: false,
      submissions: [],
      submitted: false,
      // upload config
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      headers: { Authorization: 'Bearer ' + getToken() },
    }
  },
  computed: {
    submitDisabled() {
      // require studentId and at least one uploaded file
      return this.submitted || this.uploadedFiles.length === 0 || this.submitLoading || !this.studentId
    },
    parsedHomeworkAttachments() {
      return this.parseAttachmentString(this.homework.attachments)
    }
  },
  watch: {
    // Keep uploadedFiles in sync with el-upload's fileList (covers cases where upload response doesn't return fileName)
    fileList(newList) {
      this.uploadedFiles = (newList || []).map(f => this.getNameFromFileObj(f)).filter(Boolean)
    }
  },
  created() {
    this.homeworkId = this.$route.query.homeworkId || null
    if (!this.homeworkId) {
      // fetch a list the user can pick from
      this.fetchHomeworkList().then(() => {
        this.loading = false
      })
      return
    }
    this.init()
  },
  methods: {
    async fetchHomeworkList() {
      this.loading = true
      try {
        const res = await listHomework({ pageNum: 1, pageSize: 50 })
        this.homeworkList = (res && (res.rows || res.data)) ? (res.rows || res.data) : (res || [])
        if (!this.homeworkList || this.homeworkList.length === 0) {
          this.$message.info('暂无可提交的作业，请等待教师发布')
        }
      } catch (err) {
        // log full error for debugging
        console.error('获取作业列表失败，error object:', err)
        let userMsg = '获取作业列表失败'
        try {
          // axios error shape: err.response.data or err.message
          if (err && err.response) {
            // if backend sent AjaxResult with msg
            const data = err.response.data
            if (data && (data.msg || data.message)) {
              userMsg = `获取作业列表失败：${data.msg || data.message}`
            } else if (typeof data === 'string') {
              userMsg = `获���作业列表失败：${data}`
            } else {
              userMsg = `获取作业列表失败（HTTP ${err.response.status}）`
            }
          } else if (err && err.message) {
            userMsg = `获取作业列表失败：${err.message}`
          }
        } catch (e) {
          console.error('解析错误信息失败', e)
        }
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
      if (this.submitted) {
        this.$message.info('您已提交过作业，无法重复提交')
        return
      }
      if (!this.uploadedFiles || this.uploadedFiles.length === 0) {
        this.$message.error('请先上传至少一个文件')
        return
      }
      if (!this.studentId) {
        this.$message.error('请输入学号 studentId')
        return
      }
      this.submitLoading = true
      try {
        // backend expects ClassStudentHomework: homeworkId, studentId, submissionFiles
        const payload = {
          homeworkId: this.homeworkId,
          studentId: this.studentId,
          submissionFiles: this.uploadedFiles.join(','),
          remark: ''
        }
        const res = await submitHomework(payload)
        const ok = (res && (res.code === 200 || res.code === 0 || res.success)) || res === 200
        if (ok) {
          this.$message.success('提交成功')
          this.resetUpload()
          await this.fetchSubmissions()
          this.submitted = true
        } else {
          this.$message.error((res && res.msg) || '提交失败')
        }
      } catch (err) {
        console.error('提交失败', err)
        this.$message.error('提交失败')
      } finally {
        this.submitLoading = false
      }
    }
   }
 }
 </script>
