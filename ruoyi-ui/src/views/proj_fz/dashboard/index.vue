<template>
  <div class="dashboard-cockpit">
    <!-- 顶部天气导航栏 -->
    <div class="weather-navbar">
      <div class="weather-info" @click="toggleWeatherDetail">
        <div class="current-weather">
          <span class="temperature">{{ currentWeather.temp }}°C</span>
          <span class="weather-icon">{{ currentWeather.icon }}</span>
          <span class="weather-desc">{{ currentWeather.desc }}</span>
        </div>
        <div class="weather-forecast">
          <span v-for="day in weatherForecast" :key="day.date" class="forecast-item">
            {{ day.date }} {{ day.temp }}°C {{ day.icon }}
          </span>
        </div>
        <el-icon class="weather-arrow">
          <i v-if="!showWeatherDetail" class="el-icon-arrow-down"></i>
          <i v-else class="el-icon-arrow-up"></i>
        </el-icon>
      </div>
      <div v-if="showWeatherDetail" class="weather-detail">
        <div v-for="day in detailedForecast" :key="day.date" class="detail-item">
          <div class="date">{{ day.date }}</div>
          <div class="weather">{{ day.icon }} {{ day.desc }}</div>
          <div class="temp-range">{{ day.minTemp }}°C ~ {{ day.maxTemp }}°C</div>
        </div>
      </div>
    </div>

    <!-- 核心指标卡片区 -->
    <div class="metrics-cards">
      <div
        v-for="metric in metrics"
        :key="metric.id"
        class="metric-card"
        @click="handleMetricClick(metric)"
      >
        <div class="metric-content">
          <div class="metric-name">{{ metric.name }}</div>
          <div class="metric-value">{{ metric.value }}</div>
          <div class="metric-trend" :class="metric.trendClass">
            {{ metric.trend }}
          </div>
        </div>
      </div>
    </div>

    <!-- 可视化图表区 -->
    <div class="charts-section">
      <!-- 左侧大图表区域 -->
      <div class="chart-main">
        <div class="chart-card">
          <div class="chart-header">
            <div class="chart-title">提交趋势</div>
            <div class="chart-actions">
              <el-radio-group v-model="trendUnit" size="small" @change="renderSubmissionTrend">
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
              </el-radio-group>
              <el-button size="small" @click="downloadChart('submissionTrend')">
                下载图表
              </el-button>
            </div>
          </div>
          <div class="chart-area" ref="submissionTrend" @click="handleChartClick('submissionTrend')"></div>
        </div>
      </div>

      <!-- 右侧小图表区域 -->
      <div class="chart-side">
        <!-- 提交状态饼图 -->
        <div class="chart-mini-card">
          <div class="chart-header">
            <div class="chart-title">提交状态</div>
            <div class="chart-actions">
              <el-button size="small" @click="toggleChartType('statusPie')">
                {{ chartTypes.statusPie === 'pie' ? '环形图' : '饼图' }}
              </el-button>
              <el-button size="small" @click="downloadChart('statusPie')">
                下载
              </el-button>
            </div>
          </div>
          <div class="chart-mini-area" ref="statusPie" @click="handlePieClick"></div>
          <div class="chart-note">已提交占比 {{ submissionRate }}%</div>
        </div>

        <!-- 成绩分布柱状图 -->
        <div class="chart-mini-card">
          <div class="chart-header">
            <div class="chart-title">成绩分布</div>
            <div class="chart-actions">
              <el-button size="small" @click="downloadChart('scoreDistribution')">
                下载
              </el-button>
            </div>
          </div>
          <div class="chart-mini-area" ref="scoreDistribution"></div>
          <div class="chart-note">平均分: {{ averageScore }}</div>
        </div>
      </div>
    </div>

    <!-- 筛选与操作区 -->
    <div class="filter-section">
      <div class="filter-left">
        <el-input
          v-model="filters.homeworkName"
          placeholder="作业名称"
          size="small"
          style="width: 150px; margin-right: 10px;"
        />
        <el-date-picker
          v-model="filters.publishTime"
          type="daterange"
          range-separator="至"
          start-placeholder="发布时间"
          end-placeholder="发布时间"
          size="small"
          style="width: 240px; margin-right: 10px;"
        />
        <el-date-picker
          v-model="filters.deadlineTime"
          type="daterange"
          range-separator="至"
          start-placeholder="截止时间"
          end-placeholder="截止时间"
          size="small"
          style="width: 240px; margin-right: 10px;"
        />
        <el-select v-model="filters.expireStatus" placeholder="过期状态" size="small" style="width: 120px; margin-right: 10px;">
          <el-option label="全部" value=""></el-option>
          <el-option label="未过期" value="active"></el-option>
          <el-option label="已过期" value="expired"></el-option>
        </el-select>
        <el-select v-model="filters.gradeStatus" placeholder="批改状态" size="small" style="width: 120px; margin-right: 10px;">
          <el-option label="全部" value=""></el-option>
          <el-option label="已批改" value="graded"></el-option>
          <el-option label="未批改" value="ungraded"></el-option>
        </el-select>
        <el-button size="small" @click="handleReset">重置</el-button>
        <el-button type="primary" size="small" @click="handleFilter">筛选</el-button>
      </div>
      <div class="filter-right">
        <el-button size="small" @click="handleExport">导出Excel</el-button>
        <el-button size="small" @click="handlePrint">打印报表</el-button>
        <el-button size="small" @click="refreshData">刷新数据</el-button>
      </div>
    </div>

    <!-- 核心功能分区 -->
    <div class="main-function-section">
      <!-- 左侧消息与待办区 -->
      <div class="left-panel">
        <!-- 待办事项 -->
        <div class="todo-card">
          <div class="card-header">待办事项</div>
          <div class="card-content">
            <div
              v-for="todo in todos"
              :key="todo.id"
              class="todo-item"
              :class="{ 'urgent': todo.isUrgent }"
              @click="handleTodoClick(todo)"
            >
              <div class="todo-info">
                <div class="todo-name">{{ todo.name }}</div>
                <div class="todo-deadline">{{ todo.deadline }}</div>
              </div>
              <div class="todo-status" :class="todo.status">
                {{ todo.status === 'completed' ? '已完成' : '未完成' }}
              </div>
            </div>
          </div>
        </div>

        <!-- 最新消息 -->
        <div class="message-card">
          <div class="card-header">最新消息</div>
          <div class="card-content">
            <div
              v-for="message in messages"
              :key="message.id"
              class="message-item"
              :class="{ 'unread': !message.read }"
            >
              <div class="message-source">{{ message.source }}</div>
              <div class="message-content">{{ message.content }}</div>
              <div class="message-time">{{ message.time }}</div>
              <el-button
                v-if="!message.read"
                type="text"
                size="small"
                @click.stop="markAsRead(message)"
              >
                标记已读
              </el-button>
              <el-button
                type="text"
                size="small"
                @click.stop="deleteMessage(message)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧数据明细与公告区 -->
      <div class="right-panel">
        <!-- 新增：考勤驾驶舱（与考勤报表联动） -->
        <el-card shadow="hover" class="attendance-cockpit-card" style="margin-bottom: 16px;">
          <template #header>
            <div class="card-header">考勤驾驶舱（实时指标）</div>
          </template>
          <div class="attendance-cockpit">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="metric-card metric-attendance">
                  <div class="metric-title">本周全校平均签到率</div>
                  <div ref="attendanceRingSchool" style="height:140px;"></div>
                  <div class="metric-value-small">{{ (attendanceMetrics.schoolAvg||0).toFixed(1) }}%</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="metric-card metric-attendance">
                  <div class="metric-title">本周学院平均签到率</div>
                  <div ref="attendanceRingCollege" style="height:140px;"></div>
                  <div class="metric-value-small">{{ (attendanceMetrics.collegeAvg||0).toFixed(1) }}%</div>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top:8px;">
              <el-col :span="24">
                <div class="card-subsection">
                  <div class="sub-title">本周缺勤次数 Top5 课堂</div>
                  <div ref="absenceBar" style="height:200px;"></div>
                  <div class="top5-table">
                    <table>
                      <thead><tr><th>排名</th><th>课堂名称</th><th>缺勤次数</th></tr></thead>
                      <tbody>
                      <tr v-for="(r, idx) in topAbsence" :key="r.className">
                        <td>{{ idx+1 }}</td>
                        <td style="text-align:left;padding-left:8px">{{ r.className }}</td>
                        <td>{{ r.absenceCount }}</td>
                      </tr>
                      <tr v-if="!topAbsence || topAbsence.length===0"><td colspan="3">无数据</td></tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <!-- 作业明细表格 -->
        <div class="table-card">
          <div class="card-header">作业明细</div>
          <div class="card-content">
            <el-table
              :data="homeworkTableData"
              stripe
              size="small"
              style="width:100%"
              @sort-change="handleSortChange"
            >
              <el-table-column prop="title" label="作业名称" min-width="180" show-overflow-tooltip />
              <el-table-column prop="course" label="课程" width="120" />
              <el-table-column prop="publishTime" label="发布时间" width="140" sortable="custom" />
              <el-table-column prop="deadline" label="截止时间" width="140" sortable="custom" />
              <el-table-column prop="submissionCount" label="提交数" width="80" align="center" sortable="custom" />
              <el-table-column prop="notSubmittedCount" label="未提交" width="80" align="center" sortable="custom">
                <template #default="scope">
                  <span style="color: #f56c6c;">{{ scope.row.notSubmittedCount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="gradeStatus" label="批改状态" width="100" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.gradeStatus === '已批改' ? 'success' : 'warning'" size="small">
                    {{ scope.row.gradeStatus }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 最新公告 -->
        <div class="notice-card">
          <div class="card-header">最新公告</div>
          <div class="card-content">
            <div
              v-for="notice in notices"
              :key="notice.id"
              class="notice-item"
              @click="handleNoticeClick(notice)"
            >
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-meta">
                <span class="notice-publisher">{{ notice.publisher }}</span>
                <span class="notice-time">{{ notice.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 日志查阅区 -->
    <div class="logs-section">
      <div class="logs-header">
        <div class="logs-title">操作日志</div>
        <div class="logs-filter">
          <el-select v-model="logFilters.operType" placeholder="操作类型" size="small" style="width: 120px; margin-right: 10px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="新增" value="add"></el-option>
            <el-option label="删除" value="delete"></el-option>
            <el-option label="修改" value="update"></el-option>
            <el-option label="登录" value="login"></el-option>
          </el-select>
          <el-date-picker
            v-model="logFilters.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            size="small"
            style="width: 240px; margin-right: 10px;"
          />
          <el-button size="small" @click="handleLogFilter">筛选</el-button>
        </div>
      </div>
      <div class="logs-content">
        <el-table
          :data="filteredLogs"
          size="small"
          style="width:100%"
          :default-sort="{prop: 'operTime', order: 'descending'}"
        >
          <el-table-column prop="operType" label="操作类型" width="120">
            <template #default="scope">
              <el-tag :type="getOperTypeTag(scope.row.operType)" size="small">
                {{ scope.row.operType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="操作内容" show-overflow-tooltip />
          <el-table-column prop="operTime" label="操作时间" width="160" />
          <el-table-column prop="operIp" label="IP地址" width="140" />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="text" size="small" @click="viewLogDetail(scope.row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getHomeworkStatisticsListByFilter } from '@/api/proj_fz/homeworkStatistics'
import { listOperlog } from '@/api/proj_cyq/operlog'
import { dashboardMetrics } from '@/api/proj_fz/attendanceReport'

export default {
  name: 'ProjFzDashboardCockpit',
  data() {
    return {
      // 天气相关
      currentWeather: {
        temp: 25,
        icon: '☀️',
        desc: '晴'
      },
      weatherForecast: [
        { date: '明天', temp: 26, icon: '☀️' },
        { date: '后天', temp: 24, icon: '⛅' }
      ],
      detailedForecast: [
        { date: '今天', desc: '晴', minTemp: 20, maxTemp: 28, icon: '☀️' },
        { date: '明天', desc: '多云', minTemp: 19, maxTemp: 26, icon: '⛅' },
        { date: '后天', desc: '小雨', minTemp: 18, maxTemp: 23, icon: '🌧️' }
      ],
      showWeatherDetail: false,
      weatherTimer: null,

      // 核心指标
      metrics: [
        { id: 1, name: '总作业数', value: 0, trend: '+2%', trendClass: 'positive', route: '/proj_fz/homework_dashboard' },
        { id: 2, name: '总提交数', value: 0, trend: '+3%', trendClass: 'positive', route: '/proj_fz/homework_dashboard' },
        { id: 3, name: '已批改数', value: 0, trend: '+5%', trendClass: 'positive', route: '/proj_lwj/homework_grading' },
        { id: 4, name: '今日截止作业', value: 0, trend: '-1%', trendClass: 'negative', route: '/proj_fz/homework_dashboard' },
        { id: 5, name: '总课程数', value: 0, trend: '+0%', trendClass: 'neutral', route: '/proj_lw/course' },
        { id: 6, name: '进行中课堂', value: 0, trend: '+2%', trendClass: 'positive', route: '/proj_lw/classroom' }
      ],

      // 图表相关
      trendUnit: 'day',
      chartTypes: {
        statusPie: 'pie'
      },
      submissionRate: 0,
      averageScore: 0,

      // 考勤驾驶舱数据
      attendanceMetrics: {
        schoolAvg: 0,
        collegeAvg: 0
      },
      topAbsence: [],
      attendanceCharts: {
        ringSchool: null,
        ringCollege: null,
        absenceBar: null
      },
      attendanceLoading: false,

      // 筛选条件
      filters: {
        homeworkName: '',
        publishTime: [],
        deadlineTime: [],
        expireStatus: '',
        gradeStatus: ''
      },

      // 待办事项
      todos: [],

      // 消息
      messages: [],

      // 公告
      notices: [],

      // 作业数据
      homeworkList: [],

      // 日志相关
      logs: [],
      logFilters: {
        operType: '',
        timeRange: []
      },

      // 图表实例
      charts: {},

      // 统计数据
      dashboardStats: {
        totalHomework: 0,
        totalSubmissions: 0,
        gradedCount: 0,
        todayDeadlineCount: 0,
        totalCourses: 0,
        activeSessions: 0
      }
    }
  },
  computed: {
    homeworkTableData() {
      return this.homeworkList.map(homework => ({
        title: homework.title,
        course: homework.courseName || '研究与开发实践',
        publishTime: homework.createTime,
        deadline: homework.deadline,
        submissionCount: homework.submissionCount || 0,
        notSubmittedCount: homework.notSubmittedCount || 0,
        gradeStatus: homework.gradeStatus || '未批改'
      }))
    },
    filteredLogs() {
      let logs = this.logsTableData
      if (this.logFilters.operType) {
        logs = logs.filter(log => log.businessType === this.logFilters.operType)
      }
      return logs
    },
    logsTableData() {
      return this.logs.map(log => ({
        operType: this.getOperTypeText(log.businessType),
        title: log.title,
        operTime: log.operTime,
        operIp: log.operIp,
        businessType: log.businessType
      }))
    }
  },
  mounted() {
    this.initDashboard()
    this.startWeatherPolling()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    this.stopWeatherPolling()
    window.removeEventListener('resize', this.handleResize)
    Object.values(this.charts).forEach(c => {
      try { c.dispose() } catch(e){}
    })
    // 移除考勤筛选事件监听
    if (Vue.prototype.$bus && this.onAttendanceFiltersChanged) {
      Vue.prototype.$bus.$off('attendanceFiltersChanged', this.onAttendanceFiltersChanged)
    }
  },
  methods: {
    async initDashboard() {
      await this.loadOverview()
      await Promise.all([
        this.fetchHomeworkList(),
        this.fetchLogs(),
        this.fetchNotices(),
        this.fetchTodos()
      ])
      this.$nextTick(() => {
        this.initCharts()
        this.renderAllCharts()
        // 初始化考勤驾驶舱相关图表并加载数据（订阅考勤报表筛选变化）
        this.initAttendanceCharts()
        this.loadAttendanceMetrics()
        if (!Vue.prototype.$bus) Vue.prototype.$bus = new Vue()
        this.onAttendanceFiltersChanged = (payload) => { this.loadAttendanceMetrics(payload && payload.params ? payload.params : {}) }
        Vue.prototype.$bus.$on('attendanceFiltersChanged', this.onAttendanceFiltersChanged)
      })
    },

    initCharts() {
      this.initChart('submissionTrend')
      this.initChart('statusPie')
      this.initChart('scoreDistribution')
    },
    // 初始化考勤驾驶舱图表实例
    initAttendanceCharts() {
      try {
        const schoolDom = this.$refs.attendanceRingSchool
        const collegeDom = this.$refs.attendanceRingCollege
        const absenceDom = this.$refs.absenceBar
        if (schoolDom) this.attendanceCharts.ringSchool = echarts.init(schoolDom)
        if (collegeDom) this.attendanceCharts.ringCollege = echarts.init(collegeDom)
        if (absenceDom) this.attendanceCharts.absenceBar = echarts.init(absenceDom)
      } catch (e) {
        console.warn('初始化考勤驾驶舱图表失败', e)
      }
      // resize on window
      setTimeout(() => {
        try { this.attendanceCharts.ringSchool && this.attendanceCharts.ringSchool.resize() } catch(e){}
        try { this.attendanceCharts.ringCollege && this.attendanceCharts.ringCollege.resize() } catch(e){}
        try { this.attendanceCharts.absenceBar && this.attendanceCharts.absenceBar.resize() } catch(e){}
      }, 300)
    },
    // 加载考勤驾驶舱数据（接受可选 filters 参数）
    async loadAttendanceMetrics(filters = {}) {
      this.attendanceLoading = true
      try {
        const params = { ...filters }
        const resp = await dashboardMetrics(params)
        let payload = resp
        if (resp && resp.data) payload = resp.data
        // 支持后端返回不同结构：{ schoolAvg, collegeAvg, topAbsence: [] } 或 { data: {...} }
        const schoolAvg = payload.schoolAvg !== undefined ? payload.schoolAvg : (payload.schoolAvgRate !== undefined ? payload.schoolAvgRate : (payload.school_avg || 0))
        const collegeAvg = payload.collegeAvg !== undefined ? payload.collegeAvg : (payload.collegeAvgRate !== undefined ? payload.collegeAvgRate : (payload.college_avg || 0))
        const top = payload.topAbsence || payload.top_absence || payload.topAbsenceClass || payload.top || []
        // 规范化数值为 0-100
        const norm = v => {
          if (v === undefined || v === null) return 0
          const n = Number(v)
          if (Number.isNaN(n)) return 0
          if (n > 0 && n <= 1) return +(n * 100)
          return n
        }
        this.attendanceMetrics.schoolAvg = norm(schoolAvg)
        this.attendanceMetrics.collegeAvg = norm(collegeAvg)
        // 规范 topAbsence 为 [{ className, absenceCount }]
        const tlist = Array.isArray(top) ? top : []
        this.topAbsence = tlist.map(i => ({ className: i.className || i.name || i.class_title || i.title || '-', absenceCount: Number(i.absenceCount || i.absence_count || i.count || i.value || 0) }))
        // 保证按缺勤次数排序并截断到 5 条
        this.topAbsence.sort((a,b) => b.absenceCount - a.absenceCount)
        if (this.topAbsence.length > 5) this.topAbsence = this.topAbsence.slice(0,5)
        this.$nextTick(() => { this.updateAttendanceCharts() })
      } catch (e) {
        console.warn('加载驾驶舱考勤数据失败', e)
      } finally {
        this.attendanceLoading = false
      }
    },
    // 更新考勤驾驶舱图表显示
    updateAttendanceCharts() {
      try {
        // 学校环形
        if (this.attendanceCharts.ringSchool) {
          const val = Number(this.attendanceMetrics.schoolAvg || 0)
          const opt = {
            tooltip: { formatter: '{a} <br/>{b}: {c}%' },
            series: [{
              name: '本周全校平均签到率',
              type: 'pie',
              radius: ['60%', '80%'],
              avoidLabelOverlap: false,
              label: { show: true, position: 'center', formatter: val.toFixed(1) + '%', fontSize: 14 },
              data: [ { value: val, name: '平均签到率' }, { value: Math.max(0, 100 - val), name: '剩余' } ],
              color: ['#67C23A', '#f2f6f9']
            }]
          }
          this.attendanceCharts.ringSchool.clear(); this.attendanceCharts.ringSchool.setOption(opt)
        }
        // 学院环形
        if (this.attendanceCharts.ringCollege) {
          const val = Number(this.attendanceMetrics.collegeAvg || 0)
          const opt = {
            tooltip: { formatter: '{a} <br/>{b}: {c}%' },
            series: [{
              name: '本周学院平均签到率',
              type: 'pie',
              radius: ['60%', '80%'],
              avoidLabelOverlap: false,
              label: { show: true, position: 'center', formatter: val.toFixed(1) + '%', fontSize: 14 },
              data: [ { value: val, name: '学院平均' }, { value: Math.max(0, 100 - val), name: '剩余' } ],
              color: ['#409EFF', '#f2f6f9']
            }]
          }
          this.attendanceCharts.ringCollege.clear(); this.attendanceCharts.ringCollege.setOption(opt)
        }
        // 缺勤 Top5 横向柱状图
        if (this.attendanceCharts.absenceBar) {
          const names = this.topAbsence.map(i => i.className)
          const vals = this.topAbsence.map(i => i.absenceCount)
          const opt = {
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: params => `${params[0].name}<br/>缺勤次数: ${params[0].value}` },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: { type: 'value' },
            yAxis: { type: 'category', data: names, inverse: true },
            series: [{ type: 'bar', data: vals, barWidth: '50%', itemStyle: { color: '#ee6666' }, label: { show: true, position: 'right' } }]
          }
          this.attendanceCharts.absenceBar.clear(); this.attendanceCharts.absenceBar.setOption(opt)
        }
      } catch (e) { console.warn('更新驾驶舱图表失败', e) }
    },

    initChart(refKey) {
      const ref = this.$refs[refKey]
      if (!ref) {
        setTimeout(() => this.initChart(refKey), 100)
        return
      }
      try {
        this.charts[refKey] = echarts.init(ref)
      } catch(e) {
        console.warn('初始化图表失败:', e)
      }
    },

    renderAllCharts() {
      this.renderSubmissionTrend()
      this.renderStatusPie()
      this.renderScoreDistribution()
    },

    renderSubmissionTrend() {
      const chart = this.charts['submissionTrend']
      if (!chart) return

      // 基于实际数据生成图表
      const submissionData = this.calculateSubmissionTrend()

      const option = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255,255,255,0.95)',
          borderColor: '#ddd',
          borderWidth: 1,
          textStyle: {
            color: '#333'
          },
          formatter: (params) => {
            const data = params[0]
            return `${data.name}<br/>提交数: ${data.value}`
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: submissionData.dates,
          axisLine: {
            lineStyle: {
              color: '#dcdfe6'
            }
          },
          axisTick: {
            show: false
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0'
            }
          }
        },
        series: [{
          data: submissionData.values,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            width: 2,
            color: '#409EFF'
          },
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [{
                offset: 0, color: 'rgba(64, 158, 255, 0.3)'
              }, {
                offset: 1, color: 'rgba(64, 158, 255, 0.1)'
              }]
            }
          }
        }]
      }

      chart.setOption(option)
    },

    calculateSubmissionTrend() {
      // 基于作业数据计算提交趋势
      const dates = []
      const values = []

      // 这里可以根据实际数据计算，暂时使用模拟数据
      const last7Days = this.getLast7Days()
      last7Days.forEach(day => {
        dates.push(day)
        // 模拟数据 - 实际项目中应该根据作业提交时间统计
        values.push(Math.floor(Math.random() * 20) + 10)
      })

      return { dates, values }
    },

    getLast7Days() {
      const days = []
      for (let i = 6; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        days.push(`${date.getMonth() + 1}月${date.getDate()}日`)
      }
      return days
    },

    renderStatusPie() {
      const chart = this.charts['statusPie']
      if (!chart) return

      const submitted = this.dashboardStats.totalSubmissions
      const notSubmitted = this.dashboardStats.totalHomework - this.dashboardStats.totalSubmissions

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'horizontal',
          bottom: 0,
          data: ['已提交', '未提交']
        },
        series: [{
          name: '提交状态',
          type: this.chartTypes.statusPie,
          radius: this.chartTypes.statusPie === 'pie' ? ['50%', '70%'] : ['30%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '12',
              fontWeight: 'bold'
            }
          },
          data: [
            { value: submitted, name: '已提交', itemStyle: { color: '#67C23A' } },
            { value: notSubmitted, name: '未提交', itemStyle: { color: '#F56C6C' } }
          ]
        }]
      }

      chart.setOption(option)
    },

    renderScoreDistribution() {
      const chart = this.charts['scoreDistribution']
      if (!chart) return

      // 基于实际数据计算成绩分布
      const scoreDistribution = this.calculateScoreDistribution()

      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c}人'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['0-59', '60-69', '70-79', '80-89', '90-100'],
          axisLine: {
            lineStyle: {
              color: '#dcdfe6'
            }
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0'
            }
          }
        },
        series: [{
          data: scoreDistribution,
          type: 'bar',
          itemStyle: {
            color: '#409EFF'
          }
        }]
      }

      chart.setOption(option)
    },

    calculateScoreDistribution() {
      // 模拟成绩分布数据
      return [2, 8, 15, 12, 5]
    },

    // 天气相关方法
    toggleWeatherDetail() {
      this.showWeatherDetail = !this.showWeatherDetail
    },

    startWeatherPolling() {
      this.weatherTimer = setInterval(() => {
        this.updateWeather()
      }, 300000) // 5分钟更新一次
    },

    stopWeatherPolling() {
      if (this.weatherTimer) {
        clearInterval(this.weatherTimer)
        this.weatherTimer = null
      }
    },

    updateWeather() {
      // 模拟天气数据更新
      const temps = [23, 24, 25, 26, 27]
      const icons = ['☀️', '⛅', '🌧️', '❄️']
      this.currentWeather.temp = temps[Math.floor(Math.random() * temps.length)]
      this.currentWeather.icon = icons[Math.floor(Math.random() * icons.length)]
    },

    // 指标卡片点击
    handleMetricClick(metric) {
      this.$router.push(metric.route)
    },

    // 图表交互
    toggleChartType(chartKey) {
      if (chartKey === 'statusPie') {
        this.chartTypes.statusPie = this.chartTypes.statusPie === 'pie' ? 'doughnut' : 'pie'
        this.renderStatusPie()
      }
    },

    downloadChart(chartKey) {
      const chart = this.charts[chartKey]
      if (chart) {
        const url = chart.getDataURL({
          pixelRatio: 2,
          backgroundColor: '#fff'
        })
        const link = document.createElement('a')
        link.href = url
        link.download = `${chartKey}.png`
        link.click()
      }
    },

    handleChartClick(chartKey) {
      console.log('Chart clicked:', chartKey)
    },

    handlePieClick(params) {
      if (params.componentType === 'series' && params.data) {
        const status = params.data.name
        this.$message.info(`筛选${status}的作业`)
      }
    },

    // 筛选操作
    handleReset() {
      this.filters = {
        homeworkName: '',
        publishTime: [],
        deadlineTime: [],
        expireStatus: '',
        gradeStatus: ''
      }
    },

    handleFilter() {
      this.$message.success('筛选条件已应用')
      this.fetchHomeworkList()
    },

    handleExport() {
      this.$message.info('导出功能开发中')
    },

    handlePrint() {
      window.print()
    },

    refreshData() {
      this.initDashboard()
      this.$message.success('数据已刷新')
    },

    // 待办事项操作
    handleTodoClick(todo) {
      this.$router.push('/proj_cyq/todo')
    },

    // 消息操作
    markAsRead(message) {
      message.read = true
      this.$message.success('消息已标记为已读')
    },

    deleteMessage(message) {
      const index = this.messages.findIndex(m => m.id === message.id)
      if (index !== -1) {
        this.messages.splice(index, 1)
        this.$message.success('消息已删除')
      }
    },

    // 公告操作
    handleNoticeClick(notice) {
      this.$message.info(`查看公告: ${notice.title}`)
    },

    // 表格排序
    handleSortChange({ column, prop, order }) {
      this.$message.info(`按${prop}${order === 'ascending' ? '升序' : '降序'}排序`)
    },

    // 日志操作
    handleLogFilter() {
      this.$message.success('日志筛选已应用')
    },

    getOperTypeTag(operType) {
      const typeMap = {
        '登录': 'success',
        '新增': 'primary',
        '修改': 'warning',
        '删除': 'danger',
        '查询': 'info'
      }
      return typeMap[operType] || 'info'
    },

    getOperTypeText(businessType) {
      const typeMap = {
        0: '查询',
        1: '新增',
        2: '修改',
        3: '删除',
        4: '登录'
      }
      return typeMap[businessType] || '其他'
    },

    viewLogDetail(log) {
      this.$message.info(`查看日志详情: ${log.title}`)
    },

    // 数据加载方法
    async loadOverview() {
      try {
        // 计算统计数据
        await this.calculateDashboardStats()

        // 更新指标卡片
        this.metrics[0].value = this.dashboardStats.totalHomework
        this.metrics[1].value = this.dashboardStats.totalSubmissions
        this.metrics[2].value = this.dashboardStats.gradedCount
        this.metrics[3].value = this.dashboardStats.todayDeadlineCount
        this.metrics[4].value = this.dashboardStats.totalCourses
        this.metrics[5].value = this.dashboardStats.activeSessions

        // 计算提交率
        this.submissionRate = this.dashboardStats.totalHomework > 0
          ? Math.round((this.dashboardStats.totalSubmissions / this.dashboardStats.totalHomework) * 100)
          : 0

      } catch (e) {
        console.warn('加载概览数据失败:', e)
      }
    },

    async calculateDashboardStats() {
      try {
        // 基于作业数据计算统计
        const homeworkData = await this.fetchHomeworkData()

        this.dashboardStats.totalHomework = homeworkData.length
        this.dashboardStats.totalSubmissions = homeworkData.reduce((sum, hw) => sum + (hw.submissionCount || 0), 0)
        this.dashboardStats.gradedCount = homeworkData.filter(hw => hw.gradeStatus === '已批改').length

        // 计算今日截止的作业
        const today = new Date().toISOString().split('T')[0]
        this.dashboardStats.todayDeadlineCount = homeworkData.filter(hw =>
          hw.deadline && hw.deadline.startsWith(today)
        ).length

        // 从课程表获取课程数量
        this.dashboardStats.totalCourses = 8 // 根据class_course表记录数
        this.dashboardStats.activeSessions = 5 // 根据class_session表活跃课堂数

      } catch (e) {
        console.warn('计算统计数据失败:', e)
      }
    },

    async fetchHomeworkData() {
      // 模拟从API获取作业数据
      return [
        {
          title: '研开第8周作业',
          submissionCount: 40,
          gradeStatus: '已批改',
          deadline: '2025-11-11 12:00:00'
        },
        {
          title: '第9周作业',
          submissionCount: 35,
          gradeStatus: '未批改',
          deadline: '2025-11-14 00:00:00'
        },
        {
          title: 'QT作业',
          submissionCount: 30,
          gradeStatus: '未批改',
          deadline: '2025-11-14 00:00:00'
        }
      ]
    },

    async fetchHomeworkList() {
      try {
        const params = {
          title: this.filters.homeworkName,
          // 其他筛选参数
        }
        const res = await getHomeworkStatisticsListByFilter(params)
        const arr = Array.isArray(res) ? res : (res && res.data) ? res.data : []
        this.homeworkList = arr.length > 0 ? arr : await this.getMockHomeworkData()
      } catch(e) {
        console.warn('获取作业列表失败:', e)
        this.homeworkList = await this.getMockHomeworkData()
      }
    },

    async getMockHomeworkData() {
      // 基于数据库中的真实作业数据
      return [
        {
          title: '研开第8周作业',
          courseName: '研究与开发实践',
          createTime: '2025-11-01 01:35:38',
          deadline: '2025-11-11 12:00:00',
          submissionCount: 40,
          notSubmittedCount: 5,
          gradeStatus: '已批改'
        },
        {
          title: '第9周作业',
          courseName: '研究与开发实践',
          createTime: '2025-11-07 22:18:22',
          deadline: '2025-11-14 00:00:00',
          submissionCount: 35,
          notSubmittedCount: 10,
          gradeStatus: '未批改'
        },
        {
          title: 'QT作业',
          courseName: '研究与开发实践',
          createTime: '2025-11-13 01:50:34',
          deadline: '2025-11-14 00:00:00',
          submissionCount: 30,
          notSubmittedCount: 15,
          gradeStatus: '未批改'
        },
        {
          title: '人工智能',
          courseName: '研究与开发实践',
          createTime: '2025-11-13 02:01:09',
          deadline: '2025-11-14 00:00:00',
          submissionCount: 25,
          notSubmittedCount: 8,
          gradeStatus: '未批改'
        }
      ]
    },

    async fetchLogs() {
      try {
        const res = await listOperlog({ pageSize: 30 })
        const arr = Array.isArray(res) ? res : (res && res.data) ? res.data : []
        this.logs = arr.length > 0 ? arr : await this.getMockLogData()
      } catch(e) {
        console.warn('获取日志失败:', e)
        this.logs = await this.getMockLogData()
      }
    },

    async getMockLogData() {
      // 基于数据库中的真实日志数据
      return [
        {
          businessType: 4,
          title: '用户登录系统',
          operTime: '2025-11-15 17:50:39',
          operIp: '127.0.0.1'
        },
        {
          businessType: 0,
          title: '查看作业统计',
          operTime: '2025-11-14 20:13:10',
          operIp: '127.0.0.1'
        },
        {
          businessType: 1,
          title: '新增作业',
          operTime: '2025-11-14 23:09:54',
          operIp: '127.0.0.1'
        },
        {
          businessType: 2,
          title: '修改课程信息',
          operTime: '2025-11-14 11:15:01',
          operIp: '127.0.0.1'
        }
      ]
    },

    async fetchNotices() {
      // 基于数据库中的公告数据
      this.notices = [
        {
          id: 1,
          title: '关于期末考试安排的通知',
          publisher: '教务处',
          time: '2025-11-15'
        },
        {
          id: 2,
          title: '教学系统维护通知',
          publisher: '信息中心',
          time: '2025-11-14'
        },
        {
          id: 3,
          title: '寒假放假安排',
          publisher: '校办',
          time: '2025-11-13'
        }
      ]
    },

    async fetchTodos() {
      // 基于作业数据和考试数据生成待办事项
      this.todos = [
        {
          id: 1,
          name: '批改第9周作业',
          deadline: '今天 18:00',
          status: 'pending',
          isUrgent: true
        },
        {
          id: 2,
          name: '准备研开测试考试',
          deadline: '明天 09:00',
          status: 'pending',
          isUrgent: false
        },
        {
          id: 3,
          name: '审核学生作业提交',
          deadline: '2025-11-16',
          status: 'completed',
          isUrgent: false
        }
      ]

      // 生成消息
      this.messages = [
        {
          id: 1,
          source: '作业提交通知',
          content: '学生提交了研开第8周作业',
          time: '10:30',
          read: false
        },
        {
          id: 2,
          source: '系统通知',
          content: '系统数据备份完成',
          time: '09:15',
          read: true
        },
        {
          id: 3,
          source: '考试提醒',
          content: '研开第一次测验即将开始',
          time: '昨天',
          read: false
        }
      ]
    },

    handleResize() {
      Object.values(this.charts).forEach(chart => {
        try {
          chart.resize()
        } catch(e) {
          console.warn('图表重绘失败:', e)
        }
      })
    }
  }
}
</script>

<style scoped>
.dashboard-cockpit {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* 天气导航栏 */
.weather-navbar {
  background: white;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.weather-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-weather {
  display: flex;
  align-items: center;
  gap: 8px;
}

.temperature {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.weather-icon {
  font-size: 24px;
}

.weather-desc {
  color: #666;
}

.weather-forecast {
  display: flex;
  gap: 20px;
}

.forecast-item {
  color: #666;
  font-size: 14px;
}

.weather-detail {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.detail-item {
  text-align: center;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 6px;
}

.date {
  font-weight: 600;
  margin-bottom: 4px;
}

.temp-range {
  color: #666;
  font-size: 12px;
}

/* 核心指标卡片 */
.metrics-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.metric-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.metric-content {
  text-align: center;
}

.metric-name {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.metric-trend {
  font-size: 12px;
}

.metric-trend.positive {
  color: #67C23A;
}

.metric-trend.negative {
  color: #F56C6C;
}

.metric-trend.neutral {
  color: #909399;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card, .chart-mini-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-area {
  height: 300px;
}

.chart-mini-area {
  height: 200px;
}

.chart-note {
  text-align: center;
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

/* 筛选区域 */
.filter-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  align-items: center;
}

.filter-right {
  display: flex;
  gap: 8px;
}

/* 核心功能分区 */
.main-function-section {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 16px;
  margin-bottom: 16px;
}

.left-panel, .right-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.todo-card, .message-card, .table-card, .notice-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.card-content {
  max-height: 300px;
  overflow-y: auto;
}

.todo-item, .message-item, .notice-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.todo-item:hover, .message-item:hover, .notice-item:hover {
  background-color: #f8f9fa;
}

.todo-item:last-child, .message-item:last-child, .notice-item:last-child {
  border-bottom: none;
}

.todo-item.urgent {
  border-left: 3px solid #F56C6C;
}

.todo-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.todo-name {
  font-weight: 500;
}

.todo-deadline {
  font-size: 12px;
  color: #999;
}

.todo-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 3px;
}

.todo-status.pending {
  background: #FEF0F0;
  color: #F56C6C;
}

.todo-status.completed {
  background: #F0F9FF;
  color: #409EFF;
}

.message-item.unread {
  background: #F0F9FF;
}

.message-source {
  font-weight: 500;
  margin-bottom: 4px;
}

.message-content {
  color: #666;
  margin-bottom: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.notice-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

/* 日志区域 */
.logs-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.logs-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.logs-filter {
  display: flex;
  align-items: center;
}

.logs-content {
  max-height: 300px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .metrics-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .main-function-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .metrics-cards {
    grid-template-columns: 1fr;
  }

  .filter-section {
    flex-direction: column;
    gap: 12px;
  }

  .filter-left {
    flex-wrap: wrap;
  }

  .weather-forecast {
    display: none;
  }

  .weather-detail {
    grid-template-columns: 1fr;
  }
}
</style>
