<template>
  <div class="app-container homework-dashboard">
    <!-- 顶部概览卡片和图表部分保持不变 -->
    <el-row :gutter="20">
      <!-- 顶部概览卡片 -->
      <el-col :span="6" v-for="(card, index) in overviewCards" :key="index">
        <el-card shadow="hover" class="dashboard-card">
          <div class="card-content">
            <div class="card-icon" :style="{ backgroundColor: card.color }">
              <i :class="card.icon"></i>
            </div>
            <div class="card-info">
              <div class="card-value">{{ card.value }}</div>
              <div class="card-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧统计图表 -->
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>作业提交统计</span>
              <el-select v-model="selectedHomework" placeholder="选择作业" @change="handleHomeworkChange" style="width: 200px;">
                <el-option
                  v-for="item in homeworkList"
                  :key="item.homeworkId"
                  :label="item.homeworkTitle"
                  :value="item.homeworkId">
                  <span>{{ item.homeworkTitle }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ item.courseName }}</span>
                </el-option>
              </el-select>
            </div>
          </template>

          <div v-if="selectedHomeworkData">
            <el-tabs v-model="activeChartTab" @tab-click="handleTabClick">
              <!-- 提交状态饼图 -->
              <el-tab-pane label="提交状态" name="submission">
                <div class="chart-container">
                  <div ref="submissionChart" class="chart" style="width: 100%; height: 400px;"></div>
                </div>
              </el-tab-pane>

              <!-- 成绩分布柱状图 -->
              <el-tab-pane label="成绩分布" name="score">
                <div class="chart-container">
                  <div ref="scoreChart" class="chart" style="width: 100%; height: 400px;"></div>
                </div>
              </el-tab-pane>

              <!-- 提交趋势折线图 -->
              <el-tab-pane label="提交趋势" name="trend">
                <div class="chart-container">
                  <div ref="trendChart" class="chart" style="width: 100%; height: 400px;"></div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
          <div v-else class="no-data">
            <el-empty description="请选择一个作业查看统计图表" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧课堂概览 -->
      <el-col :span="8">
        <el-card shadow="hover" class="overview-card">
          <template #header>
            <div class="card-header">
              <span>课堂作业概览</span>
            </div>
          </template>
          <div class="session-list">
            <div v-for="session in sessionOverview" :key="session.sessionId" class="session-item">
              <div class="session-info">
                <div class="session-name">{{ session.className }}</div>
                <div class="session-course">{{ session.courseName }}</div>
                <div class="session-stats">
                  <span>作业数: {{ session.homeworkCount || 0 }}</span>
                  <span>提交率: {{ session.avgSubmissionRate || 0 }}%</span>
                </div>
              </div>
              <el-progress
                :percentage="parseFloat(session.avgSubmissionRate) || 0"
                :show-text="false"
                :color="getProgressColor(session.avgSubmissionRate)">
              </el-progress>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 作业数据表格 -->
    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>作业数据列表 ({{ homeworkList.length }})</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleExport">
              导出作业数据
            </el-button>
            <el-button type="success" @click="handlePrint">
              打印统计报表
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 - 优化布局 -->
      <div class="filter-container">
        <div class="filter-row">
          <!-- 第一行筛选条件 -->
          <div class="filter-group">
            <div class="filter-item">
              <span class="filter-label">作业名称：</span>
              <el-input
                v-model="filterForm.homeworkTitle"
                placeholder="输入作业名称"
                clearable
                style="width: 200px;"
                @input="handleFilterChange"
                @clear="handleFilterChange">
              </el-input>
            </div>

            <div class="filter-item">
              <span class="filter-label">课程：</span>
              <el-select
                v-model="filterForm.courseId"
                placeholder="选择课程"
                clearable
                style="width: 160px;"
                @change="handleFilterChange">
                <el-option
                  v-for="course in courseList"
                  :key="course.courseId"
                  :label="course.courseName"
                  :value="course.courseId">
                </el-option>
              </el-select>
            </div>

            <div class="filter-item">
              <span class="filter-label">课堂：</span>
              <el-select
                v-model="filterForm.sessionId"
                placeholder="选择课堂"
                clearable
                style="width: 160px;"
                @change="handleFilterChange">
                <el-option
                  v-for="session in sessionList"
                  :key="session.sessionId"
                  :label="session.className"
                  :value="session.sessionId">
                </el-option>
              </el-select>
            </div>
          </div>
        </div>

        <div class="filter-row">
          <!-- 第二行筛选条件 -->
          <div class="filter-group">
            <div class="filter-item">
              <span class="filter-label">发布时间：</span>
              <el-date-picker
                v-model="filterForm.createTimeStart"
                type="date"
                placeholder="开始日期"
                value-format="yyyy-MM-dd"
                style="width: 150px;"
                @change="handleFilterChange">
              </el-date-picker>
              <span class="date-separator">至</span>
              <el-date-picker
                v-model="filterForm.createTimeEnd"
                type="date"
                placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 150px;"
                @change="handleFilterChange">
              </el-date-picker>
            </div>

            <div class="filter-item">
              <span class="filter-label">截止时间：</span>
              <el-date-picker
                v-model="filterForm.deadlineStart"
                type="date"
                placeholder="开始日期"
                value-format="yyyy-MM-dd"
                style="width: 150px;"
                @change="handleFilterChange">
              </el-date-picker>
              <span class="date-separator">至</span>
              <el-date-picker
                v-model="filterForm.deadlineEnd"
                type="date"
                placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 150px;"
                @change="handleFilterChange">
              </el-date-picker>
            </div>
          </div>
        </div>

        <div class="filter-row">
          <!-- 第三行筛选条件 -->
          <div class="filter-group">
            <div class="filter-item">
              <span class="filter-label">过期状态：</span>
              <el-select
                v-model="filterForm.expireStatus"
                placeholder="选择状态"
                clearable
                style="width: 140px;"
                @change="handleFilterChange">
                <el-option
                  v-for="option in expireStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value">
                </el-option>
              </el-select>
            </div>

            <div class="filter-item">
              <span class="filter-label">批改状态：</span>
              <el-select
                v-model="filterForm.gradeStatus"
                placeholder="选择状态"
                clearable
                style="width: 140px;"
                @change="handleFilterChange">
                <el-option
                  v-for="option in gradeStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value">
                </el-option>
              </el-select>
            </div>

            <div class="filter-item">
              <span class="filter-label">完成状态：</span>
              <el-select
                v-model="filterForm.completionStatus"
                placeholder="选择状态"
                clearable
                style="width: 140px;"
                @change="handleFilterChange">
                <el-option
                  v-for="option in completionStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value">
                </el-option>
              </el-select>
            </div>

            <div class="filter-item">
              <el-button @click="resetFilter" type="default" icon="el-icon-refresh">
                重置
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <el-table
        :data="homeworkList"
        v-loading="loading"
        style="width: 100%"
        :default-sort="{ prop: 'homeworkId', order: 'descending' }"
        id="printTable">
        <el-table-column
          prop="homeworkId"
          label="作业ID"
          width="80"
          sortable>
        </el-table-column>
        <el-table-column
          prop="homeworkTitle"
          label="作业名称"
          min-width="200"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="courseName"
          label="课程"
          width="120"
          sortable>
        </el-table-column>
        <el-table-column
          prop="className"
          label="课堂"
          width="120"
          sortable>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="发布时间"
          width="160"
          sortable>
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="deadline"
          label="截止时间"
          width="160"
          sortable>
          <template #default="scope">
            <span>{{ formatDate(scope.row.deadline) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="submissionRate"
          label="提交率"
          width="100"
          sortable>
          <template #default="scope">
            <el-tag :type="getSubmissionRateType(scope.row.submissionRate)">
              {{ scope.row.submissionRate || 0 }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="submittedCount"
          label="已提交"
          width="80"
          sortable>
        </el-table-column>
        <el-table-column
          prop="notSubmittedCount"
          label="未提交"
          width="80"
          sortable>
        </el-table-column>
        <el-table-column
          prop="overdueCount"
          label="逾期"
          width="80"
          sortable>
        </el-table-column>
        <el-table-column
          prop="averageScore"
          label="平均分"
          width="100"
          sortable>
          <template #default="scope">
            <span v-if="scope.row.averageScore">{{ scope.row.averageScore.toFixed(1) }}</span>
            <span v-else style="color: #909399;">未批改</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="createBy"
          label="发布者"
          width="100">
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getHomeworkStatisticsList,
  getHomeworkStatistics,
  getScoreDistribution,
  getSubmissionTrend,
  getDashboardOverview,
  getCourseList,
  getSessionList,
  exportHomeworkData,
  getHomeworkStatisticsListByFilter,
  getSessionOverview,
  getTodayDeadlineCount,
  getHomeworkStatisticsListByAdvancedFilter
} from '@/api/proj_fz/homeworkStatistics'

export default {
  name: 'HomeworkStatistics',
  data() {
    return {
      loading: false,
      homeworkList: [],
      sessionOverview: [],
      courseList: [],
      sessionList: [],
      selectedHomework: null,
      selectedHomeworkData: null,
      activeChartTab: 'submission',
      submissionChart: null,
      scoreChart: null,
      trendChart: null,

      // 整合所有筛选条件到一个表单中
      filterForm: {
        homeworkTitle: '',
        courseId: null,
        sessionId: null,
        createTimeStart: null,
        createTimeEnd: null,
        deadlineStart: null,
        deadlineEnd: null,
        expireStatus: '',
        gradeStatus: '',
        completionStatus: ''
      },

      expireStatusOptions: [
        { label: '全部', value: '' },
        { label: '已过期', value: 'expired' },
        { label: '未过期', value: 'notExpired' }
      ],
      gradeStatusOptions: [
        { label: '全部', value: '' },
        { label: '已批改', value: 'graded' },
        { label: '未批改', value: 'notGraded' }
      ],
      completionStatusOptions: [
        { label: '全部', value: '' },
        { label: '已完成', value: 'completed' },
        { label: '按期完成', value: 'onTime' },
        { label: '逾期完成', value: 'overdue' },
        { label: '未完成', value: 'notCompleted' }
      ],

      overviewCards: [
        {
          label: '今日截止作业',
          value: 0,
          icon: 'el-icon-alarm-clock',
          color: '#FF6B6B'
        },
        {
          label: '总作业数',
          value: 0,
          icon: 'el-icon-document',
          color: '#409EFF'
        },
        {
          label: '总提交数',
          value: 0,
          icon: 'el-icon-upload',
          color: '#67C23A'
        },
        {
          label: '总课程数',
          value: 0,
          icon: 'el-icon-notebook-2',
          color: '#E6A23C'
        },
        {
          label: '已批改数',
          value: 0,
          icon: 'el-icon-check',
          color: '#F56C6C'
        }
      ]
    }
  },
  mounted() {
    this.loadData()
    this.loadCourseList()
    this.loadSessionList()
  },
  beforeUnmount() {
    this.destroyCharts()
  },
  methods: {
    async loadTodayDeadlineCount() {
      try {
        const response = await getTodayDeadlineCount()
        this.overviewCards[0].value = response.data || 0
      } catch (error) {
        console.error('加载今日截止作业数失败:', error)
        this.overviewCards[0].value = 0
      }
    },

    async loadHomeworkList() {
      try {
        let response

        // 判断是否有任何筛选条件
        if (this.hasAnyFilter()) {
          // 使用高级筛选，支持所有条件
          response = await getHomeworkStatisticsListByAdvancedFilter(this.filterForm)
        } else {
          // 没有筛选条件，获取全部数据
          response = await getHomeworkStatisticsList()
        }

        this.homeworkList = response.data || []

        if (this.homeworkList.length > 0 && !this.selectedHomework) {
          this.selectedHomework = this.homeworkList[0].homeworkId
          await this.loadHomeworkDetail(this.selectedHomework)
        }
      } catch (error) {
        console.error('加载作业列表失败:', error)
        this.homeworkList = []
      }
    },

    // 检查是否有任何筛选条件
    hasAnyFilter() {
      const form = this.filterForm
      return form.homeworkTitle || form.courseId || form.sessionId ||
        form.createTimeStart || form.createTimeEnd ||
        form.deadlineStart || form.deadlineEnd ||
        form.expireStatus || form.gradeStatus ||
        form.completionStatus
    },

    // 其他方法保持不变...
    async loadData() {
      this.loading = true
      try {
        await this.loadTodayDeadlineCount()
        await this.loadHomeworkList()
        await this.loadOverviewData()
        await this.loadSessionOverview()
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('数据加载失败')
      } finally {
        this.loading = false
      }
    },

    async loadOverviewData() {
      try {
        const response = await getDashboardOverview()
        const data = response.data || {}
        this.overviewCards[1].value = data.totalHomeworkCount || 0
        this.overviewCards[2].value = data.totalSubmissionCount || 0
        this.overviewCards[3].value = data.totalCourseCount || 0
        this.overviewCards[4].value = data.gradedCount || 0
      } catch (error) {
        console.error('加载概览数据失败:', error)
      }
    },

    async loadSessionOverview() {
      try {
        const response = await getSessionOverview()
        this.sessionOverview = response.data || []
      } catch (error) {
        console.error('加载课堂概览失败:', error)
        this.sessionOverview = []
      }
    },

    async loadCourseList() {
      try {
        const response = await getCourseList()
        this.courseList = response.data || []
      } catch (error) {
        console.error('加载课程列表失败:', error)
        this.courseList = []
      }
    },

    async loadSessionList() {
      try {
        const response = await getSessionList()
        this.sessionList = response.data || []
      } catch (error) {
        console.error('加载课堂列表失败:', error)
        this.sessionList = []
      }
    },

    async loadHomeworkDetail(homeworkId) {
      try {
        const response = await getHomeworkStatistics(homeworkId)
        this.selectedHomeworkData = response.data
        this.$nextTick(() => {
          this.renderCharts()
        })
      } catch (error) {
        console.error('加载作业详情失败:', error)
        this.selectedHomeworkData = null
      }
    },

    renderCharts() {
      if (!this.selectedHomeworkData) return
      this.destroyCharts()
      this.renderSubmissionChart()

      if (this.activeChartTab === 'score') {
        this.renderScoreChart()
      } else if (this.activeChartTab === 'trend') {
        this.renderTrendChart()
      }
    },

    async renderSubmissionChart() {
      if (!this.selectedHomeworkData || !this.$refs.submissionChart) return

      if (this.submissionChart) {
        this.submissionChart.dispose()
      }

      this.submissionChart = echarts.init(this.$refs.submissionChart)
      const data = [
        { value: this.selectedHomeworkData.submittedCount || 0, name: '已提交' },
        { value: this.selectedHomeworkData.notSubmittedCount || 0, name: '未提交' },
        { value: this.selectedHomeworkData.overdueCount || 0, name: '逾期提交' }
      ].filter(item => item.value > 0)

      const option = {
        title: {
          text: '提交状态分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: data.map(item => item.name)
        },
        series: [{
          name: '提交状态',
          type: 'pie',
          radius: '50%',
          data: data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      this.submissionChart.setOption(option)
    },

    async renderScoreChart() {
      if (!this.selectedHomeworkData || !this.$refs.scoreChart) return

      if (this.scoreChart) {
        this.scoreChart.dispose()
      }

      this.scoreChart = echarts.init(this.$refs.scoreChart)
      try {
        const response = await getScoreDistribution(this.selectedHomeworkData.homeworkId)
        const distributionData = response.data || []

        const xAxisData = []
        const seriesData = []
        const scoreRanges = ['0-59', '60-69', '70-79', '80-89', '90-100', '未批改']

        scoreRanges.forEach(range => {
          const item = distributionData.find(d => d.scoreRange === range)
          xAxisData.push(range)
          seriesData.push(item ? item.count : 0)
        })

        const option = {
          title: {
            text: '成绩分布',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'category',
            data: xAxisData
          },
          yAxis: {
            type: 'value',
            name: '人数'
          },
          series: [{
            name: '人数',
            type: 'bar',
            data: seriesData,
            itemStyle: {
              color: '#5470c6'
            }
          }]
        }
        this.scoreChart.setOption(option)
      } catch (error) {
        console.error('渲染成绩分布图失败:', error)
      }
    },

    async renderTrendChart() {
      if (!this.selectedHomeworkData || !this.$refs.trendChart) return

      if (this.trendChart) {
        this.trendChart.dispose()
      }

      this.trendChart = echarts.init(this.$refs.trendChart)
      try {
        const response = await getSubmissionTrend(this.selectedHomeworkData.homeworkId)
        const trendData = response.data || []

        const xAxisData = trendData.map(item => item.hour)
        const seriesData = trendData.map(item => item.count)

        const option = {
          title: {
            text: '提交趋势',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'category',
            data: xAxisData,
            axisLabel: {
              rotate: 45
            }
          },
          yAxis: {
            type: 'value',
            name: '提交人数'
          },
          series: [{
            name: '提交人数',
            type: 'line',
            data: seriesData,
            smooth: true,
            itemStyle: {
              color: '#91cc75'
            }
          }]
        }
        this.trendChart.setOption(option)
      } catch (error) {
        console.error('渲染提交趋势图失败:', error)
      }
    },

    destroyCharts() {
      [this.submissionChart, this.scoreChart, this.trendChart].forEach(chart => {
        if (chart) {
          chart.dispose()
        }
      })
    },

    async handleHomeworkChange(homeworkId) {
      await this.loadHomeworkDetail(homeworkId)
    },

    handleTabClick(tab) {
      this.activeChartTab = tab.name
      this.$nextTick(() => {
        if (this.selectedHomeworkData) {
          if (tab.name === 'submission') {
            this.renderSubmissionChart()
          } else if (tab.name === 'score') {
            this.renderScoreChart()
          } else if (tab.name === 'trend') {
            this.renderTrendChart()
          }
        }
      })
    },

    async handleExport() {
      try {
        this.loading = true

        // 构建导出参数 - 使用当前筛选条件
        const exportParams = { ...this.filterForm }

        console.log('导出参数:', exportParams)
        const response = await exportHomeworkData(exportParams)

        // 创建下载链接
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '作业数据统计.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)

        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      } finally {
        this.loading = false
      }
    },

    // 打印功能
    handlePrint() {
      this.$nextTick(() => {
        const printContent = document.getElementById('printTable').outerHTML
        const charts = []

        // 获取图表数据
        if (this.submissionChart) {
          charts.push({
            title: '提交状态分布',
            dataUrl: this.submissionChart.getDataURL({ type: 'png', pixelRatio: 2 })
          })
        }
        if (this.scoreChart) {
          charts.push({
            title: '成绩分布',
            dataUrl: this.scoreChart.getDataURL({ type: 'png', pixelRatio: 2 })
          })
        }
        if (this.trendChart) {
          charts.push({
            title: '提交趋势',
            dataUrl: this.trendChart.getDataURL({ type: 'png', pixelRatio: 2 })
          })
        }

        const printWindow = window.open('', '_blank')
        printWindow.document.write(`
          <html>
            <head>
              <title>作业统计报表</title>
              <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                .print-header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 10px; }
                .print-header h1 { margin: 0; color: #333; }
                .print-time { color: #666; font-size: 14px; }
                .chart-container { margin: 20px 0; text-align: center; }
                .chart-container img { max-width: 100%; height: auto; }
                .chart-title { font-size: 16px; font-weight: bold; margin-bottom: 10px; }
                table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                th { background-color: #f5f5f5; font-weight: bold; }
                @media print {
                  body { margin: 0; }
                  .no-print { display: none; }
                }
              </style>
            </head>
            <body>
              <div class="print-header">
                <h1>作业统计报表</h1>
                <div class="print-time">打印时间: ${new Date().toLocaleString()}</div>
              </div>

              ${charts.map(chart => `
                <div class="chart-container">
                  <div class="chart-title">${chart.title}</div>
                  <img src="${chart.dataUrl}" alt="${chart.title}">
                </div>
              `).join('')}

              <div class="table-container">
                <h3>作业数据列表 (${this.homeworkList.length} 条记录)</h3>
                ${printContent}
              </div>
            </body>
          </html>
        `)
        printWindow.document.close()

        printWindow.onload = function() {
          printWindow.print()
          printWindow.onafterprint = function() {
            printWindow.close()
          }
        }
      })
    },

    handleFilterChange() {
      // 防抖处理，避免频繁请求
      clearTimeout(this.filterTimer)
      this.filterTimer = setTimeout(() => {
        this.loadHomeworkList()
      }, 500)
    },

    resetFilter() {
      this.filterForm = {
        homeworkTitle: '',
        courseId: null,
        sessionId: null,
        createTimeStart: null,
        createTimeEnd: null,
        deadlineStart: null,
        deadlineEnd: null,
        expireStatus: '',
        gradeStatus: '',
        completionStatus: ''
      }
      this.loadHomeworkList()
    },

    getSubmissionRateType(rate) {
      const numRate = parseFloat(rate) || 0
      if (numRate >= 80) return 'success'
      if (numRate >= 60) return 'warning'
      return 'danger'
    },

    getProgressColor(rate) {
      const numRate = parseFloat(rate) || 0
      if (numRate >= 80) return '#67C23A'
      if (numRate >= 60) return '#E6A23C'
      return '#F56C6C'
    },

    // 格式化日期显示
    formatDate(dateString) {
      if (!dateString) return '-'
      try {
        const date = new Date(dateString)
        if (isNaN(date.getTime())) return '-'
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }).replace(/\//g, '-')
      } catch (error) {
        console.error('日期格式化错误:', error)
        return '-'
      }
    }
  }
}
</script>

<style scoped>
.homework-dashboard {
  padding: 20px;
}

.dashboard-card {
  margin-bottom: 20px;
}

.card-content {
  display: flex;
  align-items: center;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.card-icon i {
  font-size: 24px;
  color: white;
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

.chart-card,
.overview-card {
  height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 400px;
}

.no-data {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.session-list {
  max-height: 400px;
  overflow-y: auto;
}

.session-item {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.session-item:last-child {
  border-bottom: none;
}

.session-info {
  margin-bottom: 10px;
}

.session-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.session-course {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.session-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
}

/* 筛选条件样式优化 */
.filter-container {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.filter-row {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  min-width: 60px;
  text-align: right;
}

.date-separator {
  color: #909399;
  font-size: 14px;
  margin: 0 8px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .el-col-16, .el-col-8 {
    width: 100%;
  }

  .filter-group {
    gap: 12px;
  }

  .filter-item {
    flex: 1;
    min-width: 200px;
  }
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group {
    flex-direction: column;
    gap: 12px;
  }

  .filter-item {
    flex: none;
  }
}
</style>
