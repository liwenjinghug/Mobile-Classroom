<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 我管理的课堂 -->
      <el-tab-pane label="我管理的课堂" name="teaching">
        <el-table
          v-loading="teachingLoading"
          :data="teachingList"
          border
          fit
          highlight-current-row
          style="width: 100%;"
        >
          <el-table-column label="课堂ID" prop="sessionId" align="center" width="80" />
          <el-table-column label="课堂名称" prop="className" align="center" />
          <el-table-column label="教师" prop="teacher" align="center" />
          <el-table-column label="上课时间" align="center" width="180">
            <template slot-scope="{row}">
              <span>{{ formatSessionTime(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="学生人数" prop="totalStudents" align="center" width="100" />
          <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
            <template slot-scope="{row}">
              <el-button type="primary" size="mini" @click="handleManageClass(row)">
                管理
              </el-button>
              <el-button type="warning" size="mini" @click="handleViewApplications(row)">
                审核申请
              </el-button>
              <!-- 新增：修改学生人数按钮 -->
              <el-button type="success" size="mini" @click="handleUpdateStudents(row)">
                修改人数
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="teachingTotal>0"
          :total="teachingTotal"
          :page.sync="teachingQuery.page"
          :limit.sync="teachingQuery.limit"
          @pagination="getTeachingList"
        />
      </el-tab-pane>

      <!-- 待审核申请 -->
      <el-tab-pane label="待审核申请" name="pending">
        <div class="batch-actions" style="margin-bottom: 15px;">
          <el-button
            type="success"
            size="small"
            :disabled="selectedApplications.length === 0"
            @click="handleBatchApprove"
          >
            批量通过
          </el-button>
          <el-button
            type="danger"
            size="small"
            :disabled="selectedApplications.length === 0"
            @click="handleBatchReject"
          >
            批量拒绝
          </el-button>
          <span style="margin-left: 10px; color: #909399;">
            已选择 {{ selectedApplications.length }} 个申请
          </span>
        </div>

        <el-table
          v-loading="pendingLoading"
          :data="pendingList"
          border
          fit
          highlight-current-row
          style="width: 100%;"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="申请ID" prop="applicationId" align="center" width="80" />
          <el-table-column label="课堂名称" prop="className" align="center" />
          <el-table-column label="学生姓名" prop="studentName" align="center" />
          <el-table-column label="学号" prop="studentNo" align="center" />
          <el-table-column label="申请时间" prop="applyTime" align="center" width="180">
            <template slot-scope="{row}">
              <span>{{ row.applyTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
            <template slot-scope="{row}">
              <el-button type="success" size="mini" @click="handleApprove(row)">
                通过
              </el-button>
              <el-button type="danger" size="mini" @click="handleReject(row)">
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="pendingTotal>0"
          :total="pendingTotal"
          :page.sync="pendingQuery.page"
          :limit.sync="pendingQuery.limit"
          @pagination="getPendingList"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 审核对话框 -->
    <el-dialog
      :title="auditDialog.title"
      :visible.sync="auditDialog.visible"
      width="500px"
    >
      <el-form ref="auditForm" :model="auditDialog.form" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditDialog.form.status">
            <el-radio label="1">通过</el-radio>
            <el-radio label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="auditDialog.form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入审核备注（可选）"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="auditDialog.loading" @click="handleAuditSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 新增：修改学生人数对话框 -->
    <el-dialog
      :title="updateStudentsDialog.title"
      :visible.sync="updateStudentsDialog.visible"
      width="400px"
      @close="handleUpdateStudentsClose"
    >
      <el-form
        ref="updateStudentsForm"
        :model="updateStudentsDialog.form"
        :rules="updateStudentsDialog.rules"
        label-width="100px"
      >
        <el-form-item label="课堂名称">
          <el-input v-model="updateStudentsDialog.form.className" :disabled="true" />
        </el-form-item>
        <el-form-item label="当前人数">
          <el-input v-model="updateStudentsDialog.form.currentStudents" :disabled="true" />
        </el-form-item>
        <el-form-item label="新人数" prop="totalStudents" required>
          <el-input-number
            v-model="updateStudentsDialog.form.totalStudents"
            :min="0"
            :max="999"
            controls-position="right"
            style="width: 100%;"
            placeholder="请输入新的人数"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="updateStudentsDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="updateStudentsDialog.loading"
          @click="handleUpdateStudentsSubmit"
        >
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyTeachingClasses, getPendingApplications, auditApplication, batchAuditApplications } from '@/api/proj_lw/teacher-class'
// 新增API导入
import { updateSessionStudents } from '@/api/proj_lw/teacher-class'
import Pagination from '@/components/Pagination'

export default {
  name: 'TeacherClass',
  components: { Pagination },
  filters: {
    statusFilter(status) {
      const statusMap = {
        '0': 'info',
        '1': 'success',
        '2': 'danger'
      }
      return statusMap[status]
    },
    statusTextFilter(status) {
      const statusMap = {
        '0': '未开始',
        '1': '进行中',
        '2': '已结束'
      }
      return statusMap[status]
    },
    parseTime(time, format) {
      if (!time) return ''

      const date = new Date(time)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')

      if (format) {
        return format
          .replace('{y}', year)
          .replace('{m}', month)
          .replace('{d}', day)
          .replace('{h}', hour)
          .replace('{i}', minute)
      }

      return `${year}-${month}-${day} ${hour}:${minute}`
    }
  },
  data() {
    // 自定义验证规则：学生人数不能为负数
    const validateStudents = (rule, value, callback) => {
      if (value === null || value === undefined || value === '') {
        callback(new Error('请输入学生人数'))
      } else if (value < 0) {
        callback(new Error('学生人数不能小于0'))
      } else if (value > 999) {
        callback(new Error('学生人数不能超过999'))
      } else {
        callback()
      }
    }

    return {
      activeTab: 'teaching',

      // 我管理的课堂
      teachingList: [],
      teachingTotal: 0,
      teachingLoading: true,
      teachingQuery: {
        page: 1,
        limit: 10
      },

      // 待审核申请
      pendingList: [],
      pendingTotal: 0,
      pendingLoading: true,
      pendingQuery: {
        page: 1,
        limit: 10
      },
      selectedApplications: [],

      // 审核对话框
      auditDialog: {
        visible: false,
        loading: false,
        title: '审核申请',
        type: 'single', // 'single' 或 'batch'
        form: {
          applicationId: null,
          applicationIds: [],
          status: '1',
          remark: ''
        }
      },

      // 新增：修改学生人数对话框
      updateStudentsDialog: {
        visible: false,
        loading: false,
        title: '修改学生人数',
        form: {
          sessionId: null,
          className: '',
          currentStudents: '',
          totalStudents: null
        },
        rules: {
          totalStudents: [
            { required: true, message: '请输入学生人数', trigger: 'blur' },
            { validator: validateStudents, trigger: 'blur' }
          ]
        }
      }
    }
  },
  created() {
    this.getTeachingList()
  },
  methods: {
    // 我管理的课堂
    async getTeachingList() {
      this.teachingLoading = true
      try {
        console.log('=== 开始获取教师课堂 ===')
        console.log('查询参数:', this.teachingQuery)

        const response = await getMyTeachingClasses(this.teachingQuery)
        console.log('教师课堂完整响应:', response)

        // 直接使用响应数据，不通过 .rows
        let data = response.data || response
        console.log('响应数据:', data)

        // 兼容不同的数据结构
        if (data && data.rows) {
          // 标准结构: { rows: [], total: 0 }
          this.teachingList = data.rows || []
          this.teachingTotal = data.total || 0
        } else if (Array.isArray(data)) {
          // 直接是数组
          this.teachingList = data
          this.teachingTotal = data.length
        } else if (data && data.data) {
          // 嵌套结构: { data: { rows: [], total: 0 } }
          this.teachingList = data.data.rows || []
          this.teachingTotal = data.data.total || 0
        } else {
          // 默认空数组
          this.teachingList = []
          this.teachingTotal = 0
        }

        console.log('最终教师课堂列表:', this.teachingList)
        console.log('最终教师课堂总数:', this.teachingTotal)
      } catch (error) {
        console.error('获取教师课堂失败:', error)
        this.teachingList = []
        this.teachingTotal = 0
        this.$message.error('获取课堂数据失败')
      } finally {
        this.teachingLoading = false
      }
    },

    // 待审核申请
    async getPendingList() {
      this.pendingLoading = true
      try {
        console.log('=== 开始获取待审核申请 ===')
        console.log('查询参数:', this.pendingQuery)

        const response = await getPendingApplications(this.pendingQuery)
        console.log('待审核申请完整响应:', response)

        // 直接使用响应数据，不通过 .rows
        let data = response.data || response
        console.log('响应数据:', data)

        // 兼容不同的数据结构
        if (data && data.rows) {
          this.pendingList = data.rows || []
          this.pendingTotal = data.total || 0
        } else if (Array.isArray(data)) {
          this.pendingList = data
          this.pendingTotal = data.length
        } else if (data && data.data) {
          this.pendingList = data.data.rows || []
          this.pendingTotal = data.data.total || 0
        } else {
          this.pendingList = []
          this.pendingTotal = 0
        }

        this.selectedApplications = [] // 清空选择

        console.log('最终待审核申请列表:', this.pendingList)
        console.log('最终待审核申请总数:', this.pendingTotal)
      } catch (error) {
        console.error('获取待审核申请失败:', error)
        this.pendingList = []
        this.pendingTotal = 0
        this.$message.error('获取申请数据失败')
      } finally {
        this.pendingLoading = false
      }
    },

    // 表格选择变化
    handleSelectionChange(selection) {
      this.selectedApplications = selection
    },

    // 管理课堂
    handleManageClass(row) {
      this.$router.push({
        path: '/proj_lw/class-management',
        query: {
          sessionId: row.sessionId,
          className: row.className,
          teacher: row.teacher
        }
      })
    },

    // 查看申请
    handleViewApplications(row) {
      this.activeTab = 'pending'
      this.getPendingList()
    },

    // 通过申请
    handleApprove(row) {
      this.auditDialog.type = 'single'
      this.auditDialog.form.applicationId = row.applicationId
      this.auditDialog.form.applicationIds = []
      this.auditDialog.form.status = '1'
      this.auditDialog.form.remark = ''
      this.auditDialog.visible = true
    },

    // 拒绝申请
    handleReject(row) {
      this.auditDialog.type = 'single'
      this.auditDialog.form.applicationId = row.applicationId
      this.auditDialog.form.applicationIds = []
      this.auditDialog.form.status = '2'
      this.auditDialog.form.remark = ''
      this.auditDialog.visible = true
    },

    // 批量通过
    handleBatchApprove() {
      if (this.selectedApplications.length === 0) {
        this.$message.warning('请选择要审核的申请')
        return
      }
      this.auditDialog.type = 'batch'
      this.auditDialog.form.applicationId = null
      this.auditDialog.form.applicationIds = this.selectedApplications.map(item => item.applicationId)
      this.auditDialog.form.status = '1'
      this.auditDialog.form.remark = ''
      this.auditDialog.visible = true
    },

    // 批量拒绝
    handleBatchReject() {
      if (this.selectedApplications.length === 0) {
        this.$message.warning('请选择要审核的申请')
        return
      }
      this.auditDialog.type = 'batch'
      this.auditDialog.form.applicationId = null
      this.auditDialog.form.applicationIds = this.selectedApplications.map(item => item.applicationId)
      this.auditDialog.form.status = '2'
      this.auditDialog.form.remark = ''
      this.auditDialog.visible = true
    },

    // 提交审核
    async handleAuditSubmit() {
      this.auditDialog.loading = true
      try {
        if (this.auditDialog.type === 'single') {
          await auditApplication(
            this.auditDialog.form.applicationId,
            this.auditDialog.form.status,
            this.auditDialog.form.remark
          )
          this.$message.success('审核成功')
        } else {
          await batchAuditApplications(
            this.auditDialog.form.applicationIds,
            this.auditDialog.form.status
          )
          this.$message.success(`批量审核成功，共处理 ${this.auditDialog.form.applicationIds.length} 个申请`)
        }

        this.auditDialog.visible = false
        this.getPendingList() // 刷新申请列表
      } catch (error) {
        this.$message.error(error.message || '审核失败')
      } finally {
        this.auditDialog.loading = false
      }
    },

    // 格式化课堂时间显示
    formatSessionTime(session) {
      const weekDayMap = {
        '1': '周一',
        '2': '周二',
        '3': '周三',
        '4': '周四',
        '5': '周五',
        '6': '周六',
        '7': '周日'
      };

      let weekDay = weekDayMap[session.weekDay] || '';
      let startTime = session.startTime || '';
      let endTime = session.endTime || '';

      if (startTime.includes(' ')) {
        startTime = startTime.split(' ')[1].substring(0, 5);
      }
      if (endTime.includes(' ')) {
        endTime = endTime.split(' ')[1].substring(0, 5);
      }

      return `${weekDay} ${startTime}-${endTime}`;
    },

    // 标签切换
    handleTabClick(tab) {
      if (tab.name === 'pending') {
        this.getPendingList()
      } else if (tab.name === 'teaching') {
        this.getTeachingList()
      }
    },

    // ============ 新增：修改学生人数相关方法 ============

    // 点击修改学生人数按钮
    handleUpdateStudents(row) {
      console.log('修改学生人数:', row)

      this.updateStudentsDialog.form = {
        sessionId: row.sessionId,
        className: row.className,
        currentStudents: row.totalStudents || 0,
        totalStudents: row.totalStudents || 0
      }

      this.updateStudentsDialog.title = `修改学生人数 - ${row.className}`
      this.updateStudentsDialog.visible = true
    },

    // 关闭修改学生人数对话框
    handleUpdateStudentsClose() {
      this.$refs.updateStudentsForm.clearValidate()
      this.updateStudentsDialog.form = {
        sessionId: null,
        className: '',
        currentStudents: '',
        totalStudents: null
      }
    },

    // 提交修改学生人数
    async handleUpdateStudentsSubmit() {
      this.$refs.updateStudentsForm.validate(async (valid) => {
        if (!valid) {
          return
        }

        this.updateStudentsDialog.loading = true
        try {
          const form = this.updateStudentsDialog.form

          // 如果人数没有变化，直接关闭
          if (form.totalStudents == form.currentStudents) {
            this.$message.info('学生人数没有变化')
            this.updateStudentsDialog.visible = false
            return
          }

          console.log('提交修改学生人数:', form)

          await updateSessionStudents(form.sessionId, form.totalStudents)

          this.$message.success('学生人数修改成功')
          this.updateStudentsDialog.visible = false

          // 刷新课堂列表
          this.getTeachingList()
        } catch (error) {
          console.error('修改学生人数失败:', error)
          this.$message.error(error.message || '修改失败')
        } finally {
          this.updateStudentsDialog.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}
.filter-item {
  margin-right: 10px;
}
.batch-actions {
  padding: 10px 0;
}
</style>
