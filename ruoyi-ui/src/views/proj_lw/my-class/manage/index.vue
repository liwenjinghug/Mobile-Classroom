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
        <el-button type="success" icon="el-icon-printer" @click="handlePrint">
          打印学生列表
        </el-button>
        <el-button icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>
    </div>

    <!-- 学生列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生列表（共 {{ studentList.length }} 人）</span>
          <el-input
            v-model="studentQuery.studentName"
            placeholder="搜索学生姓名"
            style="width: 200px;"
            @input="handleSearch"
          >
            <template #append>
              <el-button icon="el-icon-search" @click="getAllStudents" />
            </template>
          </el-input>
        </div>
      </template>

      <el-table
        v-loading="studentLoading"
        :data="filteredStudents"
        border
        fit
        highlight-current-row
        style="width: 100%"
      >
        <el-table-column label="序号" align="center" width="60">
          <template #default="{ $index }">
            {{ $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="学生ID" prop="studentId" align="center" width="80" />
        <el-table-column label="学号" prop="studentNo" align="center" />
        <el-table-column label="姓名" prop="studentName" align="center" />
        <el-table-column label="性别" prop="gender" align="center" width="80">
          <template #default="{row}">
            <span>{{ row.gender === 'M' ? '男' : row.gender === 'F' ? '女' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" align="center" width="100">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '在读' : '退学' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加入时间" prop="assignedAt" align="center" width="180">
          <template #default="{row}">
            <span>{{ row.assignedAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
          <template #default="{row}">
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
            <template #default="{row}">
              <span>{{ row.gender === 'M' ? '男' : row.gender === 'F' ? '女' : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" align="center" width="100">
            <template #default="{row}">
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

    <!-- 打印区域（隐藏） -->
    <div v-show="false" ref="printContent" class="print-content">
      <div class="print-header">
        <h2>{{ classInfo.className }} - 学生名单</h2>
        <p>课堂ID: {{ classInfo.sessionId }} | 生成时间: {{ currentTime }}</p>
      </div>
      <table class="print-table">
        <thead>
        <tr>
          <th>序号</th>
          <th>学号</th>
          <th>姓名</th>
          <th>性别</th>
          <th>状态</th>
          <th>加入时间</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(student, index) in studentList" :key="student.studentId">
          <td>{{ index + 1 }}</td>
          <td>{{ student.studentNo }}</td>
          <td>{{ student.studentName }}</td>
          <td>{{ student.gender === 'M' ? '男' : student.gender === 'F' ? '女' : '-' }}</td>
          <td>{{ student.status === 1 ? '在读' : '退学' }}</td>
          <td>{{ student.assignedAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</td>
        </tr>
        </tbody>
      </table>
      <div class="print-footer">
        <p>共 {{ studentList.length }} 名学生</p>
      </div>
    </div>
  </div>
</template>

<script>
import { getClassStudents, addStudentsToClass, removeStudentFromClass, searchAllStudents } from '@/api/proj_lw/class-management'

export default {
  name: 'ClassManagement',
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

      // 学生列表（不再需要分页相关数据）
      studentList: [],  // 所有学生数据
      studentLoading: false,
      studentQuery: {
        studentName: ''  // 仅保留搜索字段
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
      },

      // 打印相关
      currentTime: new Date().toLocaleString(),
      userName: '管理员'  // 可以改为从store获取当前用户
    }
  },
  computed: {
    // 计算属性：实现前端搜索
    filteredStudents() {
      if (!this.studentQuery.studentName.trim()) {
        return this.studentList
      }
      const keyword = this.studentQuery.studentName.toLowerCase()
      return this.studentList.filter(student => {
        return (
          (student.studentName && student.studentName.toLowerCase().includes(keyword)) ||
          (student.studentNo && student.studentNo.toLowerCase().includes(keyword))
        )
      })
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

    this.getAllStudents()
  },
  methods: {
    // 获取所有学生（不分页）
    async getAllStudents() {
      this.studentLoading = true
      try {
        console.log('=== 开始获取课堂所有学生 ===')

        // 创建一个不分页的查询参数
        const params = {
          studentName: this.studentQuery.studentName,
          noPagination: true  // 添加不分页标识
        }

        const response = await getClassStudents(this.classInfo.sessionId, params)
        console.log('课堂学生完整响应:', JSON.stringify(response, null, 2))

        // 直接使用rows作为完整列表
        this.studentList = response.rows || []

        console.log(`获取到 ${this.studentList.length} 名学生`)

      } catch (error) {
        console.error('获取学生列表失败:', error)
        this.studentList = []
        this.$message.error('获取学生列表失败')
      } finally {
        this.studentLoading = false
      }
    },

    // 实时搜索处理
    handleSearch() {
      // 如果搜索框为空，重新获取所有数据
      if (!this.studentQuery.studentName.trim()) {
        this.getAllStudents()
      }
    },

    // 打印学生列表
    handlePrint() {
      if (this.studentList.length === 0) {
        this.$message.warning('没有学生数据可打印')
        return
      }

      // 更新当前时间
      this.currentTime = new Date().toLocaleString()

      // 等待DOM更新
      this.$nextTick(() => {
        // 创建打印窗口
        const printWindow = window.open('', '_blank')

        // 获取打印内容
        const printContent = this.$refs.printContent.innerHTML

        // 构建完整的打印页面
        printWindow.document.write(`
          <!DOCTYPE html>
          <html>
          <head>
            <title>${this.classInfo.className} - 学生名单</title>
            <meta charset="UTF-8">
            <style>
              body {
                font-family: 'Microsoft YaHei', 'SimSun', sans-serif;
                margin: 20px;
                color: #333;
              }
              .print-content {
                max-width: 1000px;
                margin: 0 auto;
              }
              .print-header {
                text-align: center;
                margin-bottom: 30px;
                border-bottom: 2px solid #333;
                padding-bottom: 15px;
              }
              .print-header h2 {
                margin: 0 0 10px 0;
                font-size: 24px;
                color: #333;
              }
              .print-header p {
                margin: 0;
                font-size: 14px;
                color: #666;
              }
              .print-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 30px;
                font-size: 14px;
              }
              .print-table th {
                background-color: #f5f5f5;
                font-weight: bold;
                text-align: center;
                padding: 10px;
                border: 1px solid #ddd;
              }
              .print-table td {
                text-align: center;
                padding: 8px;
                border: 1px solid #ddd;
              }
              .print-table tr:nth-child(even) {
                background-color: #f9f9f9;
              }
              .print-footer {
                margin-top: 30px;
                text-align: right;
                font-size: 14px;
                color: #666;
                border-top: 1px solid #ddd;
                padding-top: 15px;
              }
              .print-footer p {
                margin: 5px 0;
              }
              @media print {
                body {
                  margin: 0;
                  padding: 10mm;
                }
                .no-print {
                  display: none !important;
                }
                .print-table {
                  page-break-inside: avoid;
                }
              }
              @page {
                size: A4 portrait;
                margin: 20mm;
              }
            </style>
          </head>
          <body>
            <div class="print-content">
              ${printContent}
              <div class="no-print" style="margin-top: 30px; text-align: center;">
                <button onclick="window.print()" style="padding: 10px 20px; background: #409EFF; color: white; border: none; border-radius: 4px; cursor: pointer; margin-right: 10px;">
                  打印
                </button>
                <button onclick="window.close()" style="padding: 10px 20px; background: #909399; color: white; border: none; border-radius: 4px; cursor: pointer;">
                  关闭
                </button>
              </div>
            </div>
            <script>
              // 自动触发打印对话框
              setTimeout(function() {
                window.print();
              }, 500);
            <\/script>
          </body>
          </html>
        `)

        printWindow.document.close()
      })
    },

    // 打开添加学生对话框
    handleAddStudent() {
      this.addStudentDialog.visible = true
      this.studentSearch.keyword = ''
      this.studentSearch.results = []
      this.studentSearch.selected = []
    },

    // 搜索学生（用于添加学生对话框）
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

        // 添加不分页参数
        const response = await searchAllStudents(
          this.studentSearch.keyword,
          this.classInfo.sessionId
        )

        this.studentSearch.results = response.rows || []

        console.log('最终显示结果数量:', this.studentSearch.results.length)

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
        this.getAllStudents() // 刷新列表
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
        this.getAllStudents() // 刷新列表
      } catch (error) {
        if (error !== 'cancel') {
          console.error('移除学生失败:', error)
          this.$message.error(error.message || '移除学生失败')
        }
      }
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

/* 打印相关样式 */
.print-content {
  font-family: 'Microsoft YaHei', 'SimSun', sans-serif;
}

.print-header {
  text-align: center;
  margin-bottom: 30px;
  border-bottom: 2px solid #333;
  padding-bottom: 15px;
}

.print-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #333;
}

.print-header p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.print-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 30px;
  font-size: 14px;
}

.print-table th {
  background-color: #f5f5f5;
  font-weight: bold;
  text-align: center;
  padding: 10px;
  border: 1px solid #ddd;
}

.print-table td {
  text-align: center;
  padding: 8px;
  border: 1px solid #ddd;
}

.print-table tr:nth-child(even) {
  background-color: #f9f9f9;
}

.print-footer {
  margin-top: 30px;
  text-align: right;
  font-size: 14px;
  color: #666;
  border-top: 1px solid #ddd;
  padding-top: 15px;
}

.print-footer p {
  margin: 5px 0;
}
</style>
