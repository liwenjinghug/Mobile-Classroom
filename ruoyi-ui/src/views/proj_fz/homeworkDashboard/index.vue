<template>
  <div class="app-container homework-dashboard">
    <!-- 顶部概览卡片 - 一行占满显示 -->
    <el-row :gutter="20" class="top-overview-row">
      <el-col :span="4.8" v-for="(card, index) in overviewCards" :key="index">
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

      <!-- 筛选条件 -->
      <div class="filter-container">
        <div class="filter-row">
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
              <el-button @click="resetFilter" icon="el-icon-refresh">
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
          width="70"
          sortable
          align="center">
        </el-table-column>
        <el-table-column
          prop="homeworkTitle"
          label="作业名称"
          width="180"
          show-overflow-tooltip
          sortable>
        </el-table-column>
        <el-table-column
          prop="courseName"
          label="课程"
          width="100"
          sortable
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="className"
          label="课堂"
          width="100"
          sortable
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="发布时间"
          width="140"
          sortable>
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="deadline"
          label="截止时间"
          width="140"
          sortable>
          <template #default="scope">
            <span>{{ formatDate(scope.row.deadline) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="submissionRate"
          label="提交率"
          width="80"
          sortable
          align="center">
          <template #default="scope">
            <el-tag :type="getSubmissionRateType(scope.row.submissionRate)" size="small">
              {{ scope.row.submissionRate || 0 }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="submittedCount"
          label="已提交"
          width="70"
          sortable
          align="center">
        </el-table-column>
        <el-table-column
          prop="notSubmittedCount"
          label="未提交"
          width="70"
          sortable
          align="center">
        </el-table-column>
        <el-table-column
          prop="overdueCount"
          label="逾期"
          width="60"
          sortable
          align="center">
        </el-table-column>
        <el-table-column
          prop="averageScore"
          label="平均分"
          width="80"
          sortable
          align="center">
          <template #default="scope">
            <span v-if="scope.row.averageScore">{{ scope.row.averageScore.toFixed(1) }}</span>
            <span v-else style="color: #909399;">未批改</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="createBy"
          label="发布者"
          width="90"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column label="操作" align="center" width="60" fixed="right">
          <template #default="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-view"
              @click="handleViewDetail(scope.row)"
            >详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 作业详情对话框 -->
    <el-dialog :title="detailDialogTitle" :visible.sync="detailDialogVisible" width="900px" append-to-body>
      <el-descriptions v-if="currentDetailRow" :column="2" border>
        <el-descriptions-item label="作业ID">{{ currentDetailRow.homeworkId }}</el-descriptions-item>
        <el-descriptions-item label="作业名称">{{ currentDetailRow.homeworkTitle }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ currentDetailRow.courseName }}</el-descriptions-item>
        <el-descriptions-item label="课堂名称">{{ currentDetailRow.className }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatDate(currentDetailRow.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ formatDate(currentDetailRow.deadline) }}</el-descriptions-item>
        <el-descriptions-item label="作业状态">
          <el-tag :type="isExpired(currentDetailRow.deadline) ? 'danger' : 'success'" size="small">
            {{ isExpired(currentDetailRow.deadline) ? '已过期' : '进行中' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发布者">{{ currentDetailRow.createBy }}</el-descriptions-item>
        <el-descriptions-item label="已提交人数">{{ currentDetailRow.submittedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="未提交人数">{{ currentDetailRow.notSubmittedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="逾期提交人数">{{ currentDetailRow.overdueCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="提交率">
          <el-tag :type="getSubmissionRateType(currentDetailRow.submissionRate)">
            {{ currentDetailRow.submissionRate || 0 }}%
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已批改人数">{{ currentDetailRow.gradedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="批改率">
          {{ currentDetailRow.submittedCount > 0 ?
            Math.round((currentDetailRow.gradedCount || 0) / currentDetailRow.submittedCount * 100) : 0 }}%
        </el-descriptions-item>
        <el-descriptions-item label="平均分">
          <span v-if="currentDetailRow.averageScore">{{ currentDetailRow.averageScore.toFixed(1) }}</span>
          <span v-else style="color: #909399;">未批改</span>
        </el-descriptions-item>
        <el-descriptions-item label="最高分">
          <span v-if="currentDetailRow.maxScore">{{ currentDetailRow.maxScore }}</span>
          <span v-else style="color: #909399;">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="最低分">
          <span v-if="currentDetailRow.minScore">{{ currentDetailRow.minScore }}</span>
          <span v-else style="color: #909399;">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="及格率">
          <span v-if="currentDetailRow.passRate !== undefined">{{ currentDetailRow.passRate }}%</span>
          <span v-else style="color: #909399;">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="作业内容" :span="2">
          <div style="max-height: 200px; overflow-y: auto;">
            {{ currentDetailRow.homeworkContent || '无' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="附件" :span="2">
          <div v-if="currentDetailRow.attachments && currentDetailRow.attachments.length > 0">
            <el-tag v-for="(file, index) in currentDetailRow.attachments" :key="index" style="margin-right: 5px;">
              {{ file.fileName }}
            </el-tag>
          </div>
          <span v-else>无附件</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentDetailRow.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleDetailNavigation('first')" :disabled="currentDetailIndex <= 0">首条</el-button>
        <el-button @click="handleDetailNavigation('prev')" :disabled="currentDetailIndex <= 0">上一条</el-button>
        <el-button @click="handleDetailNavigation('next')" :disabled="currentDetailIndex >= homeworkList.length - 1">下一条</el-button>
        <el-button @click="handleDetailNavigation('last')" :disabled="currentDetailIndex >= homeworkList.length - 1">末尾</el-button>
        <el-button type="primary" @click="handlePrintDetail">打印</el-button>
        <el-button type="success" @click="handleExportDetail">导出</el-button>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
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
      ],

      // 详情对话框相关
      detailDialogVisible: false,
      detailDialogTitle: '作业详情',
      currentDetailRow: null,
      currentDetailIndex: 0
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

        if (this.hasAnyFilter()) {
          response = await getHomeworkStatisticsListByAdvancedFilter(this.filterForm)
        } else {
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

    hasAnyFilter() {
      const form = this.filterForm
      return form.homeworkTitle || form.courseId || form.sessionId ||
        form.createTimeStart || form.createTimeEnd ||
        form.deadlineStart || form.deadlineEnd ||
        form.expireStatus || form.gradeStatus ||
        form.completionStatus
    },

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
        const exportParams = { ...this.filterForm }
        const response = await exportHomeworkData(exportParams)

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

    handlePrint() {
      this.$nextTick(() => {
        // 收集图表数据 - 使用较小的图片尺寸和缩放比例
        const charts = []

        if (this.submissionChart) {
          charts.push({
            title: '提交状态分布',
            dataUrl: this.submissionChart.getDataURL({
              type: 'png',
              pixelRatio: 1.5,
              backgroundColor: '#fff'
            })
          })
        }
        if (this.scoreChart) {
          charts.push({
            title: '成绩分布',
            dataUrl: this.scoreChart.getDataURL({
              type: 'png',
              pixelRatio: 1.5,
              backgroundColor: '#fff'
            })
          })
        }
        if (this.trendChart) {
          charts.push({
            title: '提交趋势',
            dataUrl: this.trendChart.getDataURL({
              type: 'png',
              pixelRatio: 1.5,
              backgroundColor: '#fff'
            })
          })
        }

        // 手动构建表格内容
        const tableRows = this.homeworkList.map((row, index) => `
          <tr>
            <td style="text-align: center;">${row.homeworkId}</td>
            <td>${row.homeworkTitle || '-'}</td>
            <td>${row.courseName || '-'}</td>
            <td>${row.className || '-'}</td>
            <td>${this.formatDate(row.createTime)}</td>
            <td>${this.formatDate(row.deadline)}</td>
            <td style="text-align: center;">${row.submissionRate || 0}%</td>
            <td style="text-align: center;">${row.submittedCount || 0}</td>
            <td style="text-align: center;">${row.notSubmittedCount || 0}</td>
            <td style="text-align: center;">${row.overdueCount || 0}</td>
            <td style="text-align: center;">${row.averageScore ? row.averageScore.toFixed(1) : '未批改'}</td>
            <td>${row.createBy || '-'}</td>
          </tr>
        `).join('')

        const printWindow = window.open('', '_blank')
        printWindow.document.write(`
          <html>
            <head>
              <title>作业统计报表</title>
              <meta charset="utf-8">
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                  font-family: "Microsoft YaHei", Arial, sans-serif;
                  padding: 15px;
                  background: #fff;
                }
                .print-header {
                  text-align: center;
                  margin-bottom: 15px;
                  border-bottom: 2px solid #333;
                  padding-bottom: 8px;
                }
                .print-header h1 {
                  margin: 0;
                  color: #333;
                  font-size: 22px;
                  line-height: 1.4;
                }
                .print-time {
                  color: #666;
                  font-size: 12px;
                  margin-top: 5px;
                }
                .charts-section {
                  margin-bottom: 20px;
                }
                .chart-container {
                  margin: 15px 0;
                  text-align: center;
                  page-break-inside: avoid;
                }
                .chart-title {
                  font-size: 14px;
                  font-weight: bold;
                  margin-bottom: 8px;
                  color: #333;
                }
                .chart-container img {
                  max-width: 90%;
                  width: 600px;
                  height: auto;
                  display: block;
                  margin: 0 auto;
                }
                .table-container {
                  margin-top: 20px;
                  page-break-before: auto;
                }
                .table-container h3 {
                  margin-bottom: 8px;
                  color: #333;
                  font-size: 16px;
                }
                table {
                  width: 100%;
                  border-collapse: collapse;
                  margin: 10px 0;
                  font-size: 11px;
                  page-break-inside: auto;
                }
                th, td {
                  border: 1px solid #ddd;
                  padding: 6px 4px;
                  text-align: left;
                }
                th {
                  background-color: #f5f5f5;
                  font-weight: bold;
                  white-space: nowrap;
                  font-size: 11px;
                }
                tr { page-break-inside: avoid; }
                @media print {
                  body {
                    margin: 0;
                    padding: 10px;
                  }
                  .chart-container {
                    page-break-inside: avoid;
                    margin: 10px 0;
                  }
                  .chart-container img {
                    max-width: 85%;
                    width: 550px;
                  }
                  .table-container {
                    page-break-before: auto;
                  }
                }
              </style>
            </head>
            <body>
              <div class="print-header">
                <h1>作业统计报表</h1>
                <div class="print-time">打印时间: ${new Date().toLocaleString('zh-CN')}</div>
              </div>

              <div class="charts-section">
                ${charts.map(chart => `
                  <div class="chart-container">
                    <div class="chart-title">${chart.title}</div>
                    <img src="${chart.dataUrl}" alt="${chart.title}" />
                  </div>
                `).join('')}
              </div>

              <div class="table-container">
                <h3>作业数据列表 (${this.homeworkList.length} 条记录)</h3>
                <table>
                  <thead>
                    <tr>
                      <th style="text-align: center;">作业ID</th>
                      <th>作业名称</th>
                      <th>课程</th>
                      <th>课堂</th>
                      <th>发布时间</th>
                      <th>截止时间</th>
                      <th style="text-align: center;">提交率</th>
                      <th style="text-align: center;">已提交</th>
                      <th style="text-align: center;">未提交</th>
                      <th style="text-align: center;">逾期</th>
                      <th style="text-align: center;">平均分</th>
                      <th>发布者</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${tableRows}
                  </tbody>
                </table>
              </div>
            </body>
          </html>
        `)
        printWindow.document.close()

        // 等待图片加载完成后再打印
        const images = printWindow.document.getElementsByTagName('img')
        let loadedCount = 0
        const totalImages = images.length

        if (totalImages === 0) {
          // 如果没有图片,直接打印
          setTimeout(() => {
            printWindow.print()
          }, 500)
        } else {
          // 等待所有图片加载完成
          const checkAllImagesLoaded = () => {
            loadedCount++
            if (loadedCount === totalImages) {
              setTimeout(() => {
                printWindow.print()
              }, 300)
            }
          }

          for (let i = 0; i < images.length; i++) {
            if (images[i].complete) {
              checkAllImagesLoaded()
            } else {
              images[i].onload = checkAllImagesLoaded
              images[i].onerror = checkAllImagesLoaded
            }
          }
        }
      })
    },

    handleFilterChange() {
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
    },

    // 详情相关方法
    handleViewDetail(row) {
      this.currentDetailRow = row
      this.currentDetailIndex = this.homeworkList.indexOf(row)
      this.detailDialogTitle = `作业详情 - ${row.homeworkTitle}`
      this.detailDialogVisible = true
    },

    handleDetailNavigation(action) {
      let newIndex = this.currentDetailIndex
      switch (action) {
        case 'first':
          newIndex = 0
          break
        case 'prev':
          newIndex = Math.max(0, this.currentDetailIndex - 1)
          break
        case 'next':
          newIndex = Math.min(this.homeworkList.length - 1, this.currentDetailIndex + 1)
          break
        case 'last':
          newIndex = this.homeworkList.length - 1
          break
      }

      if (newIndex !== this.currentDetailIndex) {
        this.currentDetailIndex = newIndex
        this.currentDetailRow = this.homeworkList[newIndex]
        this.detailDialogTitle = `作业详情 - ${this.currentDetailRow.homeworkTitle}`
      }
    },

    handlePrintDetail() {
      this.$nextTick(() => {
        const printWindow = window.open('', '_blank')
        const detailContent = `
          <html>
            <head>
              <title>作业详情 - ${this.currentDetailRow.homeworkTitle}</title>
              <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                .print-header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 10px; }
                .print-header h1 { margin: 0; color: #333; }
                .print-time { color: #666; font-size: 14px; }
                table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                th { background-color: #f5f5f5; font-weight: bold; width: 150px; }
                @media print {
                  body { margin: 0; }
                  .no-print { display: none; }
                }
              </style>
            </head>
            <body>
              <div class="print-header">
                <h1>作业详情</h1>
                <div class="print-time">打印时间: ${new Date().toLocaleString()}</div>
              </div>
              <table>
                <tr><th>作业ID</th><td>${this.currentDetailRow.homeworkId}</td></tr>
                <tr><th>作业名称</th><td>${this.currentDetailRow.homeworkTitle}</td></tr>
                <tr><th>课程名称</th><td>${this.currentDetailRow.courseName}</td></tr>
                <tr><th>课堂名称</th><td>${this.currentDetailRow.className}</td></tr>
                <tr><th>发布时间</th><td>${this.formatDate(this.currentDetailRow.createTime)}</td></tr>
                <tr><th>截止时间</th><td>${this.formatDate(this.currentDetailRow.deadline)}</td></tr>
                <tr><th>发布者</th><td>${this.currentDetailRow.createBy}</td></tr>
                <tr><th>已提交人数</th><td>${this.currentDetailRow.submittedCount || 0}</td></tr>
                <tr><th>未提交人数</th><td>${this.currentDetailRow.notSubmittedCount || 0}</td></tr>
                <tr><th>逾期提交人数</th><td>${this.currentDetailRow.overdueCount || 0}</td></tr>
                <tr><th>提交率</th><td>${this.currentDetailRow.submissionRate || 0}%</td></tr>
                <tr><th>已批改人数</th><td>${this.currentDetailRow.gradedCount || 0}</td></tr>
                <tr><th>平均分</th><td>${this.currentDetailRow.averageScore ? this.currentDetailRow.averageScore.toFixed(1) : '未批改'}</td></tr>
                <tr><th>作业内容</th><td>${this.currentDetailRow.homeworkContent || '无'}</td></tr>
                <tr><th>备注</th><td>${this.currentDetailRow.remark || '无'}</td></tr>
              </table>
            </body>
          </html>
        `
        printWindow.document.write(detailContent)
        printWindow.document.close()
        printWindow.onload = function() {
          printWindow.print()
          printWindow.onafterprint = function() {
            printWindow.close()
          }
        }
      })
    },

    handleExportDetail() {
      try {
        const row = this.currentDetailRow
        const csvContent = [
          ['作业ID', row.homeworkId],
          ['作业名称', row.homeworkTitle],
          ['课程名称', row.courseName],
          ['课堂名称', row.className],
          ['发布时间', this.formatDate(row.createTime)],
          ['截止时间', this.formatDate(row.deadline)],
          ['发布者', row.createBy],
          ['已提交人数', row.submittedCount || 0],
          ['未提交人数', row.notSubmittedCount || 0],
          ['逾期提交人数', row.overdueCount || 0],
          ['提交率', `${row.submissionRate || 0}%`],
          ['已批改人数', row.gradedCount || 0],
          ['平均分', row.averageScore ? row.averageScore.toFixed(1) : '未批改'],
          ['作业内容', row.homeworkContent || '无'],
          ['备注', row.remark || '无']
        ].map(e => e.join(',')).join('\n')

        const BOM = '\uFEFF'
        const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `作业详情_${row.homeworkTitle}_${new Date().getTime()}.csv`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)

        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    },

    isExpired(deadline) {
      if (!deadline) return false
      return new Date(deadline) < new Date()
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
}

/* 顶部概览卡片 - 一行占满 */
.top-overview-row {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

/* Card Styling */
.app-container >>> .el-card {
  border-radius: 18px;
  border: none;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  background-color: #ffffff;
  transition: all 0.3s ease;
}

.app-container >>> .el-card:hover {
  box-shadow: 0 8px 32px rgba(0,0,0,0.08);
  transform: translateY(-2px);
}

.app-container >>> .el-card__header {
  border-bottom: 1px solid #f5f5f7;
  padding: 20px 24px;
}

/* Dashboard Cards */
.dashboard-card {
  height: 120px;
  display: flex;
  align-items: center;
  flex: 1;
  margin: 0 10px;
}

.dashboard-card:first-child {
  margin-left: 0;
}

.dashboard-card:last-child {
  margin-right: 0;
}

.card-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: white;
  font-size: 28px;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #86868b;
}

/* Chart Card */
.chart-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

.chart-container {
  padding: 20px;
}

/* Overview Card */
.overview-card {
  height: 500px;
  overflow: hidden;
}

.session-list {
  height: 420px;
  overflow-y: auto;
  padding-right: 10px;
}

.session-item {
  padding: 16px;
  border-bottom: 1px solid #f5f5f7;
  transition: background-color 0.2s;
}

.session-item:last-child {
  border-bottom: none;
}

.session-item:hover {
  background-color: #f5f5f7;
  border-radius: 12px;
}

.session-info {
  margin-bottom: 12px;
}

.session-name {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.session-course {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 8px;
}

.session-stats {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #1d1d1f;
}

/* 筛选条件样式 */
.filter-container {
  margin-bottom: 24px;
  padding: 24px;
  background-color: #ffffff;
  border-radius: 18px;
  border: none;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.filter-row {
  margin-bottom: 20px;
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
  gap: 20px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #1d1d1f;
  font-weight: 500;
  white-space: nowrap;
  min-width: 60px;
  text-align: right;
}

.date-separator {
  margin: 0 8px;
  color: #86868b;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* Form Elements */
.app-container >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  height: 36px;
  transition: all 0.2s ease;
}

.app-container >>> .el-input__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

.app-container >>> .el-select {
  width: 100%;
}

.app-container >>> .el-date-editor {
  width: 100%;
}

.app-container >>> .el-date-editor .el-input__inner {
  padding-left: 30px;
}

.app-container >>> .el-button {
  border-radius: 980px;
  font-weight: 500;
  border: none;
  padding: 9px 20px;
  transition: all 0.2s ease;
}

.app-container >>> .el-button--primary {
  background-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}

.app-container >>> .el-button--primary:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

.app-container >>> .el-button--success {
  background-color: #67C23A;
  color: white;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
}

.app-container >>> .el-button--success:hover {
  background-color: #85ce61;
  transform: translateY(-1px);
}

.app-container >>> .el-button--default {
  background-color: #f5f5f7;
  color: #1d1d1f;
  border: 1px solid #d2d2d7;
}

.app-container >>> .el-button--default:hover {
  background-color: #e8e8ed;
  border-color: #b8b8bd;
}

/* Table Styling */
.app-container >>> .el-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.app-container >>> .el-table th {
  background-color: #fbfbfd;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #f5f5f7;
  padding: 12px 0;
}

/* 确保详情按钮不会出现省略号 */
.app-container >>> .el-table .el-button--mini {
  padding: 5px 8px;
  font-size: 12px;
  white-space: nowrap;
}

.app-container >>> .el-table .el-button--text {
  padding: 5px 0;
}

.app-container >>> .el-table td {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
}

/* Scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #d2d2d7;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #86868b;
}

/* Responsive */
@media (max-width: 1200px) {
  .el-col-16, .el-col-8 {
    width: 100%;
    margin-bottom: 20px;
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
