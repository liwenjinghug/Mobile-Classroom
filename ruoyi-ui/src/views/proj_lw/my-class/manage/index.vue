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
        <el-button type="info" icon="el-icon-download" @click="handleExport" :loading="exportLoading">
          导出名单
        </el-button>
        <el-button icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>
    </div>

    <!-- 新增：统计卡片区域 -->
    <div class="statistics-section">
      <el-row :gutter="20">
        <!-- 学生总数统计 -->
        <el-col :span="6">
          <el-card class="stat-card total-card">
            <div class="stat-content">
              <div class="stat-icon">
                <i class="el-icon-user-solid" style="color: #409EFF;"></i>
              </div>
              <div class="stat-info">
                <div class="stat-title">学生总数</div>
                <div class="stat-value">{{ studentList.length }}人</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 男生统计 -->
        <el-col :span="6">
          <el-card class="stat-card male-card">
            <div class="stat-content">
              <div class="stat-icon">
                <i class="el-icon-male" style="color: #1890FF;"></i>
              </div>
              <div class="stat-info">
                <div class="stat-title">男生人数</div>
                <div class="stat-value">{{ genderStats.male }}人</div>
                <div class="stat-percent">{{ genderStats.malePercent }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 女生统计 -->
        <el-col :span="6">
          <el-card class="stat-card female-card">
            <div class="stat-content">
              <div class="stat-icon">
                <i class="el-icon-female" style="color: #F56C6C;"></i>
              </div>
              <div class="stat-info">
                <div class="stat-title">女生人数</div>
                <div class="stat-value">{{ genderStats.female }}人</div>
                <div class="stat-percent">{{ genderStats.femalePercent }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 未设置性别 -->
        <el-col :span="6">
          <el-card class="stat-card unknown-card">
            <div class="stat-content">
              <div class="stat-icon">
                <i class="el-icon-question" style="color: #909399;"></i>
              </div>
              <div class="stat-info">
                <div class="stat-title">未设置性别</div>
                <div class="stat-value">{{ genderStats.unknown }}人</div>
                <div class="stat-percent">{{ genderStats.unknownPercent }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 新增：饼图区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>性别分布饼图</span>
              <el-tooltip content="显示男生、女生和未设置性别的比例" placement="top">
                <i class="el-icon-info" style="margin-left: 5px; color: #909399;"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="chart-container">
            <div v-if="studentList.length > 0" id="genderPieChart" style="width: 100%; height: 300px;"></div>
            <div v-else class="chart-empty">
              <el-empty description="暂无学生数据" :image-size="80"></el-empty>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>状态分布</span>
              <el-tooltip content="显示在读和退学学生的比例" placement="top">
                <i class="el-icon-info" style="margin-left: 5px; color: #909399;"></i>
              </el-tooltip>
            </div>
          </template>
          <div class="chart-container">
            <div v-if="studentList.length > 0" id="statusPieChart" style="width: 100%; height: 300px;"></div>
            <div v-else class="chart-empty">
              <el-empty description="暂无学生数据" :image-size="80"></el-empty>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学生列表卡片 -->
    <el-card class="student-list-card">
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
            <span :class="{'male': row.gender === 'M', 'female': row.gender === 'F'}">
              {{ row.gender === 'M' ? '男' : row.gender === 'F' ? '女' : '-' }}
            </span>
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
// 引入ECharts
import * as echarts from 'echarts'

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

      // 学生列表
      studentList: [],
      studentLoading: false,
      studentQuery: {
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
      },

      // 打印相关
      currentTime: new Date().toLocaleString(),
      userName: '管理员',

      // 导出相关状态
      exportLoading: false,

      // 新增：图表实例
      genderChart: null,
      statusChart: null,

      // 新增：性别统计数据
      genderStats: {
        male: 0,
        female: 0,
        unknown: 0,
        malePercent: 0,
        femalePercent: 0,
        unknownPercent: 0
      },

      // 新增：图表初始化标志
      chartsInitialized: false
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
    },

    // 新增：计算状态统计数据
    statusStats() {
      const total = this.studentList.length
      const studying = this.studentList.filter(s => s.status === 1).length
      const dropped = total - studying

      return {
        studying,
        dropped,
        studyingPercent: total > 0 ? ((studying / total) * 100).toFixed(1) : 0,
        droppedPercent: total > 0 ? ((dropped / total) * 100).toFixed(1) : 0
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

    this.getAllStudents()
  },
  mounted() {
    // 延迟初始化图表，确保DOM已渲染
    this.$nextTick(() => {
      setTimeout(() => {
        this.initCharts()
        // 如果有数据，立即更新图表
        if (this.studentList.length > 0) {
          this.updateCharts()
        }
      }, 300)
    })
  },
  beforeDestroy() {
    // 销毁图表实例
    if (this.genderChart) {
      this.genderChart.dispose()
      this.genderChart = null
    }
    if (this.statusChart) {
      this.statusChart.dispose()
      this.statusChart = null
    }
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
          noPagination: true
        }

        const response = await getClassStudents(this.classInfo.sessionId, params)
        console.log('课堂学生完整响应:', JSON.stringify(response, null, 2))

        // 直接使用rows作为完整列表
        this.studentList = response.rows || []

        console.log(`获取到 ${this.studentList.length} 名学生`)

        // 新增：更新统计数据
        this.updateStatistics()

        // 新增：确保图表已初始化后更新图表
        this.$nextTick(() => {
          if (!this.chartsInitialized) {
            this.initCharts()
          }
          setTimeout(() => {
            this.updateCharts()
          }, 100)
        })

      } catch (error) {
        console.error('获取学生列表失败:', error)
        this.studentList = []
        this.$message.error('获取学生列表失败')
      } finally {
        this.studentLoading = false
      }
    },

    // 新增：更新统计数据
    updateStatistics() {
      const total = this.studentList.length

      // 统计性别
      const male = this.studentList.filter(s => s.gender === 'M').length
      const female = this.studentList.filter(s => s.gender === 'F').length
      const unknown = total - male - female

      // 计算百分比
      this.genderStats = {
        male,
        female,
        unknown,
        malePercent: total > 0 ? ((male / total) * 100).toFixed(1) : 0,
        femalePercent: total > 0 ? ((female / total) * 100).toFixed(1) : 0,
        unknownPercent: total > 0 ? ((unknown / total) * 100).toFixed(1) : 0
      }

      console.log('性别统计:', this.genderStats)
    },

    // 新增：初始化图表
    initCharts() {
      console.log('开始初始化图表...')

      try {
        // 检查ECharts是否可用
        if (typeof echarts === 'undefined') {
          console.error('ECharts 未加载')
          return
        }

        // 检查DOM元素是否存在
        const genderChartDom = document.getElementById('genderPieChart')
        const statusChartDom = document.getElementById('statusPieChart')

        if (genderChartDom) {
          // 先销毁旧的实例
          if (this.genderChart) {
            this.genderChart.dispose()
          }
          this.genderChart = echarts.init(genderChartDom)
          console.log('性别图表初始化成功')
        } else {
          console.warn('性别图表DOM元素未找到')
        }

        if (statusChartDom) {
          // 先销毁旧的实例
          if (this.statusChart) {
            this.statusChart.dispose()
          }
          this.statusChart = echarts.init(statusChartDom)
          console.log('状态图表初始化成功')
        } else {
          console.warn('状态图表DOM元素未找到')
        }

        this.chartsInitialized = true

        // 监听窗口大小变化
        const resizeHandler = () => {
          if (this.genderChart) {
            this.genderChart.resize()
          }
          if (this.statusChart) {
            this.statusChart.resize()
          }
        }

        window.addEventListener('resize', resizeHandler)

        // 组件销毁时清理
        this.$once('hook:beforeDestroy', () => {
          window.removeEventListener('resize', resizeHandler)
        })

      } catch (error) {
        console.error('初始化图表失败:', error)
      }
    },

    // 新增：更新图表
    updateCharts() {
      console.log('开始更新图表，学生数量:', this.studentList.length)

      if (this.studentList.length === 0) {
        // 清空图表
        if (this.genderChart) {
          this.genderChart.clear()
        }
        if (this.statusChart) {
          this.statusChart.clear()
        }
        return
      }

      // 确保图表已初始化
      if (!this.genderChart || !this.statusChart) {
        console.log('图表未初始化，重新初始化...')
        this.initCharts()
      }

      // 更新性别饼图
      this.updateGenderChart()

      // 更新状态饼图
      this.updateStatusChart()
    },

    // 新增：更新性别饼图
    updateGenderChart() {
      if (!this.genderChart) {
        console.log('性别图表实例不存在，尝试重新初始化...')
        this.initCharts()
        if (!this.genderChart) {
          console.error('无法获取性别图表实例')
          return
        }
      }

      const {male, female, unknown} = this.genderStats
      console.log(`性别饼图数据 - 男:${male}, 女:${female}, 未知:${unknown}`)

      // 如果没有数据，显示空状态
      if (male === 0 && female === 0 && unknown === 0) {
        const option = {
          title: {
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: {
              fontSize: 14,
              color: '#999'
            }
          }
        }
        this.genderChart.setOption(option)
        return
      }

      const option = {
        title: {
          text: '性别分布',
          left: 'center',
          top: 10,
          textStyle: {
            fontSize: 14,
            fontWeight: 'normal'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle',
          data: ['男生', '女生', '未设置性别']
        },
        series: [
          {
            name: '性别分布',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '14',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              {
                value: male,
                name: '男生',
                itemStyle: {color: '#1890FF'}
              },
              {
                value: female,
                name: '女生',
                itemStyle: {color: '#F56C6C'}
              },
              {
                value: unknown,
                name: '未设置性别',
                itemStyle: {color: '#909399'}
              }
            ]
          }
        ]
      }

      try {
        this.genderChart.setOption(option)
        console.log('性别图表更新成功')
      } catch (error) {
        console.error('性别图表更新失败:', error)
      }
    },

    // 新增：更新状态饼图
    updateStatusChart() {
      if (!this.statusChart) {
        console.log('状态图表实例不存在，尝试重新初始化...')
        this.initCharts()
        if (!this.statusChart) {
          console.error('无法获取状态图表实例')
          return
        }
      }

      const {studying, dropped} = this.statusStats
      console.log(`状态饼图数据 - 在读:${studying}, 退学:${dropped}`)

      // 如果没有数据，显示空状态
      if (studying === 0 && dropped === 0) {
        const option = {
          title: {
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: {
              fontSize: 14,
              color: '#999'
            }
          }
        }
        this.statusChart.setOption(option)
        return
      }

      const option = {
        title: {
          text: '状态分布',
          left: 'center',
          top: 10,
          textStyle: {
            fontSize: 14,
            fontWeight: 'normal'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle',
          data: ['在读', '退学']
        },
        series: [
          {
            name: '状态分布',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '14',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              {
                value: studying,
                name: '在读',
                itemStyle: {color: '#67C23A'}
              },
              {
                value: dropped,
                name: '退学',
                itemStyle: {color: '#F56C6C'}
              }
            ]
          }
        ]
      }

      try {
        this.statusChart.setOption(option)
        console.log('状态图表更新成功')
      } catch (error) {
        console.error('状态图表更新失败:', error)
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

    // 导出学生名单
    async handleExport() {
      if (this.studentList.length === 0) {
        this.$message.warning('没有学生数据可导出')
        return
      }

      try {
        this.exportLoading = true

        // 确认导出
        await this.$confirm('确定要导出学生名单吗？', '确认导出', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })

        // 生成TXT文件
        this.generateTxtFile()

        this.$message.success('导出成功，文件已开始下载')

      } catch (error) {
        if (error !== 'cancel') {
          console.error('导出失败:', error)
          this.$message.error(error.message || '导出失败')
        }
      } finally {
        this.exportLoading = false
      }
    },

    // 生成TXT文件
    generateTxtFile() {
      // 构建TXT文件内容
      let txtContent = ''

      // 添加标题
      txtContent += '='.repeat(50) + '\n'
      txtContent += `课堂名称：${this.classInfo.className}\n`
      txtContent += `课堂ID：${this.classInfo.sessionId}\n`
      txtContent += `教师：${this.classInfo.teacher}\n`
      txtContent += `导出时间：${new Date().toLocaleString()}\n`
      txtContent += `学生总数：${this.studentList.length}人\n`
      txtContent += '='.repeat(50) + '\n\n'

      // 添加性别统计
      txtContent += '性别统计：\n'
      txtContent += `男生：${this.genderStats.male}人 (${this.genderStats.malePercent}%)\n`
      txtContent += `女生：${this.genderStats.female}人 (${this.genderStats.femalePercent}%)\n`
      txtContent += `未设置性别：${this.genderStats.unknown}人 (${this.genderStats.unknownPercent}%)\n\n`

      // 添加状态统计
      txtContent += '状态统计：\n'
      txtContent += `在读：${this.statusStats.studying}人 (${this.statusStats.studyingPercent}%)\n`
      txtContent += `退学：${this.statusStats.dropped}人 (${this.statusStats.droppedPercent}%)\n\n`

      // 添加表头
      txtContent += '学生列表：\n'
      txtContent += '序号\t学号\t姓名\t性别\t状态\t加入时间\n'
      txtContent += '-'.repeat(60) + '\n'

      // 添加学生数据
      this.studentList.forEach((student, index) => {
        const gender = student.gender === 'M' ? '男' : student.gender === 'F' ? '女' : '-'
        const status = student.status === 1 ? '在读' : '退学'
        const assignedAt = student.assignedAt ? this.$options.filters.parseTime(student.assignedAt, '{y}-{m}-{d} {h}:{i}') : ''

        txtContent += `${index + 1}\t${student.studentNo || ''}\t${student.studentName || ''}\t${gender}\t${status}\t${assignedAt}\n`
      })

      // 创建Blob对象
      const blob = new Blob([txtContent], {type: 'text/plain;charset=utf-8'})

      // 创建下载链接
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url

      // 生成文件名：课堂名称_学生名单_日期.txt
      const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '')
      const fileName = `${this.classInfo.className}_学生名单_${dateStr}.txt`
      link.download = fileName

      // 触发下载
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)

      // 释放URL对象
      URL.revokeObjectURL(url)
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

/* 新增：统计卡片样式 */
.statistics-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-percent {
  font-size: 12px;
  color: #909399;
}

/* 卡片颜色 */
.total-card .stat-icon {
  background: rgba(64, 158, 255, 0.1);
}

.male-card .stat-icon {
  background: rgba(24, 144, 255, 0.1);
}

.female-card .stat-icon {
  background: rgba(245, 108, 108, 0.1);
}

.unknown-card .stat-icon {
  background: rgba(144, 147, 153, 0.1);
}

/* 新增：图表区域样式 */
.chart-section {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart-container {
  width: 100%;
  height: 300px;
  position: relative;
}

.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
}

/* 确保图表容器有明确的尺寸 */
#genderPieChart, #statusPieChart {
  width: 100% !important;
  height: 300px !important;
}

/* 表格中的性别样式 */
.male {
  color: #1890FF;
  font-weight: bold;
}

.female {
  color: #F56C6C;
  font-weight: bold;
}

/* 学生列表卡片 */
.student-list-card {
  margin-top: 20px;
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
