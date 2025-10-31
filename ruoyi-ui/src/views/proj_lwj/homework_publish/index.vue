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
        <el-button type="primary" @click="publish">发布</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { listCourse } from '@/api/proj_lw/course'
import { addHomework } from '@/api/proj_lwj/homework'

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
      headers: { Authorization: 'Bearer ' + (require('@/utils/auth').getToken()) }
    }
  },
  created() {
    this.fetchCourses()
  },
  watch: {
    'form.courseId'(val) {
      if (val) {
        // find course to get classNumber
        const selected = this.courses.find(c => c.courseId === val)
        const classNumber = selected ? selected.classNumber : null
        if (classNumber != null) {
          // use getSessionsByClassNumber to match session page
          this.sessions = []
          this.$nextTick(() => {})
          // import the correct function
          // eslint-disable-next-line no-undef
          this.$apiGetSessionsByClassNumber(classNumber)
        } else {
          this.sessions = []
        }
      } else {
        this.sessions = []
      }
    }
  },
  methods: {
    fetchCourses() {
      // fetch many courses to populate select (use large pageSize)
      listCourse({ pageNum: 1, pageSize: 1000 }).then(response => {
        // response is TableDataInfo with .rows
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
    // format Date or date-string into backend expected format: yyyy-MM-dd HH:mm:ss
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
    // wrapper to call getSessionsByClassNumber (to avoid import cycles)
    $apiGetSessionsByClassNumber(classNumber) {
      // call the session API directly
      return this.$httpGetSessionsByClassNumber(classNumber).then(res => {
        this.sessions = res && res.data ? res.data : []
        if (!this.sessions || this.sessions.length === 0) {
          this.$message.info('未查询到对应课堂')
        }
        return this.sessions
      }).catch(err => {
        console.error('getSessionsByClassNumber error', err)
        this.sessions = []
        this.$message.error('获取课堂失败请检查后端接口或权限')
        return []
      })
    },
    // helper that actually calls the imported API function
    $httpGetSessionsByClassNumber(classNumber) {
      // import function at runtime to avoid bundler issues
      const api = require('@/api/proj_lw/session')
      return api.getSessionsByClassNumber(classNumber)
    },
    uploadSuccess(response) {
      // response contains fileName
      if (response && response.fileName) {
        const prev = this.form.attachments ? this.form.attachments + ',' : ''
        this.form.attachments = prev + response.fileName
      }
    },
    publish() {
      // basic validation
      if (!this.form.title) { this.$message.error('请输入标题'); return }
      if (!this.form.courseId) { this.$message.error('请选择课程'); return }
      if (!this.form.sessionId) { this.$message.error('请选择课堂'); return }
      // ensure deadline is string
      const payload = Object.assign({}, this.form)
      if (this.form.deadline) {
        const formatted = this.formatDateToBackend(this.form.deadline)
        if (formatted) payload.deadline = formatted
      }
      // debug: log payload
      console.info('发布作业 payload:', payload)
      addHomework(payload).then(res => {
        console.info('发布作业 response:', res)
        if (res && (res.code === 200 || res.code === 0)) {
          this.$message.success('发布成功')
          this.$router.back()
        } else {
          const msg = (res && (res.msg || res.message)) ? (res.msg || res.message) : '发布失败'
          this.$message.error(msg)
          console.error('发布失败，server response:', res)
        }
      }).catch(err => {
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
  }
}
</script>
