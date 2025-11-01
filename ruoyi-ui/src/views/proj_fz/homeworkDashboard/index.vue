<template>
  <div class="app-container homework-dashboard">
    <!-- 调试信息 -->
    <el-alert
      v-if="debugInfo"
      :title="'调试信息: ' + debugInfo"
      type="info"
      :closable="false"
      style="margin-bottom: 20px;"
    />

    <!-- 调试按钮 -->
    <el-button type="warning" @click="loadDebugInfo" style="margin-bottom: 20px;">
      加载调试信息
    </el-button>

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
                <div v-if="chartsReady" ref="submissionChart" style="height: 400px;"></div>
                <div v-else style="height: 400px; display: flex; align-items: center; justify-content: center;">
                  <span>图表加载中...</span>
                </div>
              </el-tab-pane>

              <!-- 成绩分布柱状图 -->
              <el-tab-pane label="成绩分布" name="score">
                <div v-if="chartsReady" ref="scoreChart" style="height: 400px;"></div>
                <div v-else style="height: 400px; display: flex; align-items: center; justify-content: center;">
                  <span>图表加载中...</span>
                </div>
              </el-tab-pane>

              <!-- 提交趋势折线图 -->
              <el-tab-pane label="提交趋势" name="trend">
                <div v-if="chartsReady" ref="trendChart" style="height: 400px;"></div>
                <div v-else style="height: 400px; display: flex; align-items: center; justify-content: center;">
                  <span>图表加载中...</span>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
          <div v-else style="text-align: center; padding: 50px;">
            <el-empty description="请选择作业查看统计图表" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧作业列表 -->
      <el-col :span="8">
        <el-card shadow="hover" class="homework-list-card">
          <template #header>
            <span>作业列表 ({{ homeworkList.length }})</span>
          </template>

          <div class="homework-list">
            <div
              v-for="homework in homeworkList"
              :key="homework.homeworkId"
              class="homework-item"
              :class="{ active: selectedHomework === homework.homeworkId }"
              @click="selectHomework(homework.homeworkId)"
            >
              <div class="homework-title">{{ homework.homeworkTitle }}</div>
              <div class="homework-meta">
                <span class="course">{{ homework.courseName }}</span>
                <span class="submission-rate">提交率: {{ homework.submissionRate || 0 }}%</span>
              </div>
              <div class="homework-stats">
                <el-progress
                  :percentage="Math.round(homework.submissionRate || 0)"
                  :color="getProgressColor(homework.submissionRate || 0)"
                  :show-text="false">
                </el-progress>
                <div class="stats-numbers">
                  <span class="submitted">{{ homework.submittedCount || 0 }}/{{ homework.totalStudents || 0 }}</span>
                  <span class="average" v-if="homework.averageScore">均分: {{ homework.averageScore }}</span>
                  <span class="create-by" v-else>发布者: {{ homework.createBy }}</span>
                </div>
              </div>
            </div>
            <div v-if="homeworkList.length === 0" style="text-align: center; padding: 20px;">
              <el-empty description="暂无作业数据" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span>作业详细数据 ({{ homeworkList.length }})</span>
        <el-button type="primary" @click="exportData" style="float: right;">导出Excel</el-button>
      </template>

      <el-table :data="homeworkList" v-loading="loading">
        <el-table-column prop="homeworkTitle" label="作业名称" min-width="200">
          <template #default="scope">
            <el-button type="text" @click="viewHomeworkDetail(scope.row.homeworkId)">{{ scope.row.homeworkTitle }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程" width="120"></el-table-column>
        <el-table-column prop="className" label="课堂" width="120"></el-table-column>
        <el-table-column prop="submissionRate" label="提交率" width="100">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.submissionRate || 0)">
              {{ (scope.row.submissionRate || 0).toFixed(1) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submittedCount" label="已提交" width="80">
          <template #default="scope">
            {{ scope.row.submittedCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="notSubmittedCount" label="未提交" width="80">
          <template #default="scope">
            {{ scope.row.notSubmittedCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="overdueCount" label="逾期" width="80">
          <template #default="scope">
            {{ scope.row.overdueCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="averageScore" label="平均分" width="80">
          <template #default="scope">
            <span v-if="scope.row.averageScore">{{ scope.row.averageScore.toFixed(1) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="发布者" width="100"></el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="text" @click="viewHomeworkDetail(scope.row.homeworkId)">查看详情</el-button>
          </template>
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
  getDashboardOverview,
  getDebugHomeworkInfo
} from '@/api/proj_fz/homeworkStatistics'

export default {
  name: 'HomeworkDashboard',
  data() {
    return {
      loading: false,
      homeworkList: [],
      selectedHomework: null,
      selectedHomeworkData: null,
      activeChartTab: 'submission',
      overviewCards: [
        { label: '总作业数', value: 0, icon: 'el-icon-document', color: '#409EFF' },
        { label: '总提交数', value: 0, icon: 'el-icon-success', color: '#67C23A' },
        { label: '平均提交率', value: '0%', icon: 'el-icon-edit', color: '#E6A23C' },
        { label: '涉及课程', value: 0, icon: 'el-icon-collection', color: '#F56C6C' }
      ],
      submissionChart: null,
      scoreChart: null,
      trendChart: null,
      debugInfo: '',
      chartsReady: false,
      chartsInitialized: false
    }
  },
  mounted() {
    // 使用 nextTick 确保 DOM 已经渲染
    this.$nextTick(() => {
      this.initCharts()
      this.loadDashboardData()
      this.loadHomeworkList()
    })
  },
  beforeUnmount() {
    this.disposeCharts()
  },
  methods: {
    // 初始化图表
    initCharts() {
      // 检查DOM元素是否存在
      if (!this.$refs.submissionChart || !this.$refs.scoreChart || !this.$refs.trendChart) {
        console.warn('图表DOM元素未找到，延迟初始化')
        setTimeout(() => {
          this.initCharts()
        }, 100)
        return
      }

      try {
        this.submissionChart = echarts.init(this.$refs.submissionChart)
        this.scoreChart = echarts.init(this.$refs.scoreChart)
        this.trendChart = echarts.init(this.$refs.trendChart)

        this.chartsInitialized = true
        this.chartsReady = true

        // 设置默认图表
        this.renderSubmissionChart([])
        this.renderScoreChart([])
        this.renderTrendChart([])

        console.log('图表初始化成功')
      } catch (error) {
        console.error('图表初始化失败:', error)
        this.chartsReady = false
      }
    },

    // 销毁图表
    disposeCharts() {
      if (this.submissionChart) {
        this.submissionChart.dispose()
        this.submissionChart = null
      }
      if (this.scoreChart) {
        this.scoreChart.dispose()
        this.scoreChart = null
      }
      if (this.trendChart) {
        this.trendChart.dispose()
        this.trendChart = null
      }
      this.chartsInitialized = false
      this.chartsReady = false
    },

    // 标签点击事件
    handleTabClick(tab) {
      if (this.chartsInitialized) {
        // 确保图表重新渲染
        this.$nextTick(() => {
          this.refreshCharts()
        })
      }
    },

    // 刷新图表
    refreshCharts() {
      if (!this.chartsInitialized) {
        this.initCharts()
        return
      }

      if (this.selectedHomeworkData) {
        this.updateChartsWithData()
      }
    },

    // 使用数据更新图表
    updateChartsWithData() {
      // 更新提交状态饼图
      const submissionData = [
        { value: this.selectedHomeworkData.submittedCount || 0, name: '已提交' },
        { value: this.selectedHomeworkData.notSubmittedCount || 0, name: '未提交' },
        { value: this.selectedHomeworkData.overdueCount || 0, name: '逾期提交' }
      ].filter(item => item.value > 0)

      this.renderSubmissionChart(submissionData)

      // 更新成绩分布柱状图
      if (this.selectedHomeworkData.scoreDistribution && Object.keys(this.selectedHomeworkData.scoreDistribution).length > 0) {
        const scoreData = Object.entries(this.selectedHomeworkData.scoreDistribution)
          .map(([name, value]) => ({ name, value }))
          .filter(item => item.value > 0)
        this.renderScoreChart(scoreData)
      } else {
        this.renderScoreChart([])
      }

      // 更新提交趋势折线图
      if (this.selectedHomeworkData.chartData && this.selectedHomeworkData.chartData.length > 0) {
        this.renderTrendChart(this.selectedHomeworkData.chartData)
      } else {
        this.renderTrendChart([])
      }
    },

    // 加载看板数据
    async loadDashboardData() {
      try {
        this.debugInfo = '正在加载看板数据...'
        const response = await getDashboardOverview()
        console.log('看板概览数据:', response)

        if (response.code === 200) {
          const data = response.data
          // 更新概览卡片
          this.overviewCards[0].value = data.totalHomeworkCount || 0
          this.overviewCards[1].value = data.totalSubmissionCount || 0
          this.overviewCards[2].value = '计算中...'
          this.overviewCards[3].value = data.totalCourseCount || 0

          this.debugInfo = `看板数据加载成功: ${data.totalHomeworkCount}个作业, ${data.totalSubmissionCount}次提交`
        } else {
          this.debugInfo = `看板数据加载失败: ${response.msg}`
        }
      } catch (error) {
        console.error('加载看板数据失败:', error)
        this.debugInfo = `看板数据加载异常: ${error.message}`
      }
    },

    // 加载作业列表
    async loadHomeworkList() {
      this.loading = true
      try {
        this.debugInfo = '正在加载作业列表...'
        const response = await getHomeworkStatisticsList()
        console.log('作业列表数据:', response)

        if (response.code === 200) {
          this.homeworkList = response.data || []
          this.debugInfo = `作业列表加载成功: ${this.homeworkList.length}条记录`

          // 计算平均提交率
          if (this.homeworkList.length > 0) {
            const totalRate = this.homeworkList.reduce((sum, item) => sum + (item.submissionRate || 0), 0)
            const avgRate = (totalRate / this.homeworkList.length).toFixed(1)
            this.overviewCards[2].value = avgRate + '%'
          } else {
            this.overviewCards[2].value = '0%'
          }

          // 默认选择第一个作业
          if (this.homeworkList.length > 0) {
            this.selectedHomework = this.homeworkList[0].homeworkId
            await this.loadHomeworkDetail(this.homeworkList[0].homeworkId)
          }
        } else {
          this.debugInfo = `作业列表加载失败: ${response.msg}`
        }
      } catch (error) {
        console.error('加载作业列表失败:', error)
        this.debugInfo = `作业列表加载异常: ${error.message}`
      } finally {
        this.loading = false
      }
    },

    // 加载作业详情
    async loadHomeworkDetail(homeworkId) {
      try {
        this.debugInfo = `正在加载作业 ${homeworkId} 的详情...`
        const response = await getHomeworkStatistics(homeworkId)
        console.log('作业详情数据:', response)

        if (response.code === 200) {
          this.selectedHomeworkData = response.data

          // 确保图表已经初始化
          if (!this.chartsInitialized) {
            this.initCharts()
          }

          // 更新图表数据
          if (this.chartsInitialized) {
            this.updateChartsWithData()
          }

          this.debugInfo = `作业 ${homeworkId} 详情加载成功`
        } else {
          this.debugInfo = `作业详情加载失败: ${response.msg}`
        }
      } catch (error) {
        console.error('加载作业详情失败:', error)
        this.debugInfo = `作业详情加载异常: ${error.message}`
      }
    },

    // 渲染提交状态饼图
    renderSubmissionChart(data) {
      if (!this.submissionChart) return

      const option = {
        title: {
          text: data.length > 0 ? '提交状态分布' : '暂无提交数据',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
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
          }
        ]
      }
      this.submissionChart.setOption(option, true)
    },

    // 渲染成绩分布柱状图
    renderScoreChart(data) {
      if (!this.scoreChart) return

      const option = {
        title: {
          text: data.length > 0 ? '成绩分布' : '暂无成绩数据',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.name)
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '人数',
            type: 'bar',
            data: data.map(item => item.value),
            itemStyle: {
              color: '#409EFF'
            }
          }
        ]
      }
      this.scoreChart.setOption(option, true)
    },

    // 渲染提交趋势折线图
    renderTrendChart(data) {
      if (!this.trendChart) return

      const option = {
        title: {
          text: data.length > 0 ? '提交时间趋势' : '暂无提交趋势数据',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.hour)
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '提交人数',
            type: 'line',
            data: data.map(item => item.count),
            smooth: true,
            lineStyle: {
              color: '#67C23A'
            },
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      }
      this.trendChart.setOption(option, true)
    },

    // 选择作业
    selectHomework(homeworkId) {
      this.selectedHomework = homeworkId
      this.loadHomeworkDetail(homeworkId)
    },

    // 作业选择变更
    handleHomeworkChange(homeworkId) {
      this.loadHomeworkDetail(homeworkId)
    },

    // 查看作业详情
    viewHomeworkDetail(homeworkId) {
      this.$message.info(`查看作业详情: ${homeworkId}`)
    },

    // 导出数据
    exportData() {
      this.$message.info('导出功能开发中...')
    },

    // 加载调试信息
    async loadDebugInfo() {
      try {
        const response = await getDebugHomeworkInfo()
        console.log('调试信息:', response)
        if (response.code === 200) {
          this.$message.success('调试信息加载成功，请查看控制台')
        }
      } catch (error) {
        console.error('加载调试信息失败:', error)
        this.$message.error('加载调试信息失败')
      }
    },

    // 获取进度条颜色
    getProgressColor(rate) {
      if (rate >= 80) return '#67C23A'
      if (rate >= 60) return '#E6A23C'
      return '#F56C6C'
    },

    // 获取标签类型
    getTagType(rate) {
      if (rate >= 80) return 'success'
      if (rate >= 60) return 'warning'
      return 'danger'
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
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.card-icon i {
  font-size: 24px;
  color: white;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.card-label {
  color: #909399;
  font-size: 14px;
}

.chart-card, .homework-list-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.homework-list {
  max-height: 400px;
  overflow-y: auto;
}

.homework-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.homework-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.homework-item.active {
  border-color: #409EFF;
  background-color: #f0f9ff;
}

.homework-title {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 14px;
}

.homework-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
}

.homework-stats {
  margin-top: 8px;
}

.stats-numbers {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
  font-size: 12px;
}

.submitted {
  color: #409EFF;
}

.average {
  color: #67C23A;
}

.create-by {
  color: #909399;
}
</style>
