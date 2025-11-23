<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <span class="page-title">课堂人员管理</span>
        <span class="class-name">{{ classInfo.className }}</span>
        <span class="class-id">课堂ID: {{ classInfo.sessionId }}</span>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="el-icon-plus" @click="handleAddStudent">
          添加学生
        </el-button>
        <el-button icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>
    </div>

    <!-- 学生列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生列表</span>
          <el-input
            v-model="studentQuery.studentName"
            placeholder="搜索学生姓名"
            style="width: 200px;"
            @keyup.enter="getStudentList"
          >
            <template #append>
              <el-button icon="el-icon-search" @click="getStudentList" />
            </template>
          </el-input>
        </div>
      </template>

      <el-table
        v-loading="studentLoading"
        :data="studentList"
        border
        fit
        highlight-current-row
      >
        <el-table-column label="学生ID" prop="studentId" align="center" width="80" />
        <el-table-column label="学号" prop="studentNo" align="center" />
        <el-table-column label="姓名" prop="studentName" align="center" />
        <el-table-column label="性别" prop="gender" align="center" width="80">
          <template slot-scope="{row}">
            <span>{{ row.gender === 'M' ? '男' : row.gender === 'F' ? '女' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" align="center" width="100">
          <template slot-scope="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '在读' : '退学' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加入时间" prop="assignedAt" align="center" width="180">
          <template slot-scope="{row}">
            <span>{{ row.assignedAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
          <template slot-scope="{row}">
            <el-button
              type="danger"
              size="mini"
              @click="handleRemoveStudent(row)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="studentTotal>0"
        :total="studentTotal"
        :page.sync="studentQuery.page"
        :limit.sync="studentQuery.limit"
        @pagination="getStudentList"
      />
    </el-card>

    <!-- 添加学生对话框 -->
    <el-dialog
      title="添加学生到课堂"
      :visible.sync="addStudentDialog.visible"
      width="800px"
    >
      <div class="dialog-content">
        <!-- 搜索学生 -->
        <div class="search-section">
          <el-input
            v-model="studentSearch.keyword"
            placeholder="输入学号或姓名搜索学生"
            style="width: 300px;"
            @keyup.enter="searchStudents"
          >
            <template #append>
              <el-button icon="el-icon-search" @click="searchStudents" />
            </template>
          </el-input>
        </div>

        <!-- 学生列表 -->
        <el-table
          v-loading="studentSearch.loading"
          :data="studentSearch.results"
          border
          fit
          style="width: 100%; margin-top: 15px;"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="学号" prop="studentNo" align="center" />
          <el-table-column label="姓名" prop="studentName" align="center" />
          <el-table-column label="性别" prop="gender" align="center" width="80">
            <template slot-scope="{row}">
              <span>{{ row.gender === 'M' ? '男' : row.gender === 'F' ? '女' : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" align="center" width="100">
            <template slot-scope="{row}">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '在读' : '退学' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="studentSearch.selected.length > 0" class="selected-info">
          已选择 {{ studentSearch.selected.length }} 名学生
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addStudentDialog.visible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="addStudentDialog.loading"
            :disabled="studentSearch.selected.length === 0"
            @click="handleAddStudentsSubmit"
          >
            添加选中学生
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getClassStudents, addStudentsToClass, removeStudentFromClass, searchAllStudents } from '@/api/proj_lw/class-management'
import Pagination from '@/components/Pagination'

export default {
  name: 'ClassManagement',
  components: { Pagination },
  filters: {
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
    return {
      // 课堂信息
      classInfo: {
        sessionId: null,
        className: '',
        teacher: ''
      },

      // 学生列表
      studentList: [],
      studentTotal: 0,
      studentLoading: false,
      studentQuery: {
        page: 1,
        limit: 10,
        studentName: ''
      },

      // 添加学生对话框
      addStudentDialog: {
        visible: false,
        loading: false
      },

      // 学生搜索
      studentSearch: {
        keyword: '',
        loading: false,
        results: [],
        selected: []
      }
    }
  },
  created() {
    this.classInfo.sessionId = this.$route.query.sessionId
    this.classInfo.className = this.$route.query.className
    this.classInfo.teacher = this.$route.query.teacher

    if (!this.classInfo.sessionId) {
      this.$message.error('缺少课堂信息')
      this.handleBack()
      return
    }

    this.getStudentList()
  },
  methods: {
    // 在获取学生列表的方法中添加状态日志
    async getStudentList() {
      this.studentLoading = true
      try {
        console.log('=== 开始获取课堂学生列表 ===')
        const response = await getClassStudents(this.classInfo.sessionId, this.studentQuery)
        console.log('课堂学生完整响应:', JSON.stringify(response, null, 2))

        this.studentList = response.rows || []
        this.studentTotal = response.total || 0

        // 添加状态值检查
        if (this.studentList.length > 0) {
          console.log('学生状态值检查:')
          this.studentList.forEach(student => {
            console.log(`学生: ${student.studentName}, status: ${student.status}, type: ${typeof student.status}`)
          })
        }

      } catch (error) {
        console.error('获取学生列表失败:', error)
        this.studentList = []
        this.studentTotal = 0
        this.$message.error('获取学生列表失败')
      } finally {
        this.studentLoading = false
      }
    },

    // 打开添加学生对话框
    handleAddStudent() {
      this.addStudentDialog.visible = true
      this.studentSearch.keyword = ''
      this.studentSearch.results = []
      this.studentSearch.selected = []
    },

    // 搜索学生
    async searchStudents() {
      if (!this.studentSearch.keyword.trim()) {
        this.$message.warning('请输入搜索关键词')
        return
      }

      this.studentSearch.loading = true
      try {
        console.log('=== 前端搜索调试 ===')
        console.log('搜索关键词:', this.studentSearch.keyword)
        console.log('当前课堂ID:', this.classInfo.sessionId)
        console.log('课堂信息:', this.classInfo)

        // 检查API调用参数
        const response = await searchAllStudents(this.studentSearch.keyword, this.classInfo.sessionId)
        console.log('API响应:', response)
        console.log('搜索结果:', response.rows)

        this.studentSearch.results = response.rows || []

        console.log('最终显示结果数量:', this.studentSearch.results.length)
        console.log('最终显示结果:', this.studentSearch.results)

        // 检查是否有已经在课堂中的学生被显示
        const currentStudentIds = this.studentList.map(student => student.studentId)
        const duplicateStudents = this.studentSearch.results.filter(student =>
          currentStudentIds.includes(student.studentId)
        )
        console.log('重复的学生（应该为空）:', duplicateStudents)

        if (this.studentSearch.results.length === 0) {
          this.$message.info('未找到可添加的学生')
        }
      } catch (error) {
        console.error('搜索学生失败:', error)
        this.$message.error('搜索学生失败: ' + (error.message || '未知错误'))
      } finally {
        this.studentSearch.loading = false
      }
    },

    // 表格选择变化
    handleSelectionChange(selection) {
      this.studentSearch.selected = selection
    },

    // 添加学生提交
    async handleAddStudentsSubmit() {
      if (this.studentSearch.selected.length === 0) {
        this.$message.warning('请选择要添加的学生')
        return
      }

      this.addStudentDialog.loading = true
      try {
        const studentIds = this.studentSearch.selected.map(item => item.studentId)
        await addStudentsToClass(this.classInfo.sessionId, studentIds)

        this.$message.success(`成功添加 ${studentIds.length} 名学生`)
        this.addStudentDialog.visible = false
        this.getStudentList() // 刷新列表
      } catch (error) {
        console.error('添加学生失败:', error)
        this.$message.error(error.message || '添加学生失败')
      } finally {
        this.addStudentDialog.loading = false
      }
    },

    // 移除学生
    async handleRemoveStudent(row) {
      try {
        await this.$confirm(`确定要将学生 "${row.studentName}" 从课堂中移除吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await removeStudentFromClass(this.classInfo.sessionId, row.studentId)
        this.$message.success('移除学生成功')
        this.getStudentList() // 刷新列表
      } catch (error) {
        if (error !== 'cancel') {
          console.error('移除学生失败:', error)
          this.$message.error(error.message || '移除学生失败')
        }
      }
    },

    // 获取状态显示文本
    getStatusText(status) {
      // 多种可能的"在读"状态值判断
      if (status === 1 || status === '1' || status === true || status === 'true') {
        return '在读'
      }
      return '退学'
    },

    // 获取状态标签类型
    getStatusType(status) {
      if (status === 1 || status === '1' || status === true || status === 'true') {
        return 'success'
      }
      return 'danger'
    },



    // 返回
    handleBack() {
      this.$router.push('/proj_lw/teacher-class')
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e6e6e6;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.class-name {
  font-size: 18px;
  color: #606266;
}

.class-id {
  font-size: 14px;
  color: #909399;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-section {
  margin-bottom: 15px;
}

.selected-info {
  margin-top: 15px;
  padding: 10px;
  background: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  color: #1890ff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
