<template>
  <div class="app-container dashboard-container">
    <!-- 顶部天气信息 -->
    <el-row :gutter="20" class="weather-section">
      <el-col :span="24">
        <el-card class="weather-card" shadow="hover">
          <div class="weather-content">
            <div class="weather-main" @click="toggleWeatherDetail">
              <i :class="getWeatherIcon(weather.weather)" class="weather-icon"></i>
              <div class="weather-info">
                <div class="temperature">{{ weather.temperature }}</div>
                <div class="city">{{ weather.city }}</div>
                <div class="current-weather">{{ weather.weather }}</div>
              </div>
              <div class="weather-actions">
                <el-button type="text" @click.stop="refreshWeather">
                  <i class="el-icon-refresh"></i> 刷新
                </el-button>
                <el-button type="text" @click.stop="showWeatherManagement">
                  <i class="el-icon-setting"></i> 管理
                </el-button>
              </div>
            </div>

            <el-collapse-transition>
              <div v-show="weatherDetailVisible" class="weather-detail">
                <div class="detail-grid">
                  <div class="detail-item">
                    <span class="label">湿度:</span>
                    <span class="value">{{ weather.humidity }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">风力:</span>
                    <span class="value">{{ weather.wind }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">更新时间:</span>
                    <span class="value">{{ new Date().toLocaleTimeString() }}</span>
                  </div>
                </div>

                <div class="weather-forecast">
                  <div class="forecast-title">未来3天预报</div>
                  <div class="forecast-list">
                    <div v-for="(day, index) in weather.forecast" :key="index" class="forecast-item">
                      <div class="forecast-date">{{ formatForecastDate(day.date) }}</div>
                      <i :class="getWeatherIcon(day.weather)" class="forecast-icon"></i>
                      <div class="forecast-temp">{{ day.tempMin }}°C / {{ day.tempMax }}°C</div>
                      <div class="forecast-weather">{{ day.weather }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </el-collapse-transition>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 核心指标卡片区 -->
    <el-row :gutter="20" class="metrics-section">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" class="metric-col" v-for="metric in coreMetricsList" :key="metric.key">
        <el-card class="metric-card core-metric" @click.native="navigateTo(metric.route)" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon-wrapper" :style="{ background: metric.color }">
              <i :class="metric.icon" class="metric-icon"></i>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ metric.value }}</div>
              <div class="metric-title">{{ metric.title }}</div>
              <div v-if="metric.subTitle" class="metric-subtitle">{{ metric.subTitle }}</div>
              <div class="metric-trend" :class="getTrendClass(metric.trend)">
                <i :class="getTrendIcon(metric.trend)"></i>
                {{ Math.abs(metric.trend) }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 可视化图表区 -->
    <el-row :gutter="20" class="chart-section">
      <!-- 左侧大图表区域 -->
      <el-col :xs="24" :lg="16" class="chart-col">
        <el-card class="chart-card main-chart" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">作业提交趋势</span>
              <div class="chart-actions">
                <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
                  <el-radio-button label="day">日</el-radio-button>
                  <el-radio-button label="week">周</el-radio-button>
                  <el-radio-button label="month">月</el-radio-button>
                </el-radio-group>
                <el-dropdown @command="handleChartAction">
                  <el-button size="small" icon="el-icon-more"></el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="download">下载图表</el-dropdown-item>
                      <el-dropdown-item command="print">打印图表</el-dropdown-item>
                      <el-dropdown-item command="export">导出数据</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </template>
          <div ref="trendChart" class="chart-container"></div>
        </el-card>

        <el-row :gutter="20" class="sub-charts-row">
          <el-col :xs="24" :sm="12" class="sub-chart-col">
            <el-card class="chart-card sub-chart" shadow="hover">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">课程平均分对比</span>
                  <el-button size="small" icon="el-icon-download" @click="downloadChart('courseChart')">
                    下载
                  </el-button>
                </div>
              </template>
              <div ref="courseChart" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" class="sub-chart-col">
            <el-card class="chart-card sub-chart" shadow="hover">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">课堂签到率</span>
                  <el-button size="small" icon="el-icon-download" @click="downloadChart('attendanceChart')">
                    下载
                  </el-button>
                </div>
              </template>
              <div ref="attendanceChart" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>

      <!-- 右侧小图表区域 -->
      <el-col :xs="24" :lg="8" class="chart-col">
        <el-card class="chart-card side-chart" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">提交状态分布</span>
              <el-dropdown @command="handleChartTypeChange">
                <el-button size="small" icon="el-icon-s-operation"></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="pie">饼图</el-dropdown-item>
                    <el-dropdown-item command="ring">环形图</el-dropdown-item>
                    <el-dropdown-item command="bar">柱状图</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          <div ref="statusChart" class="chart-container"></div>
          <div class="chart-summary">
            <div class="summary-item" v-for="item in statusSummary" :key="item.name">
              <span class="summary-label">{{ item.name }}:</span>
              <span class="summary-value">{{ item.value }} ({{ item.percentage }}%)</span>
            </div>
          </div>
        </el-card>

        <el-card class="chart-card side-chart" shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">成绩分布</span>
              <el-button size="small" icon="el-icon-download" @click="downloadChart('scoreChart')">
                下载
              </el-button>
            </div>
          </template>
          <div ref="scoreChart" class="chart-container"></div>
          <div class="chart-summary">
            <div class="summary-item">
              <span class="summary-label">平均分:</span>
              <span class="summary-value">{{ chartData.scoreStats.average }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">最高分:</span>
              <span class="summary-value">{{ chartData.scoreStats.max }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">及格率:</span>
              <span class="summary-value">{{ chartData.scoreStats.passRate }}%</span>
            </div>
          </div>
        </el-card>

        <el-card class="chart-card side-chart" shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">学生参与热力</span>
              <el-button size="small" icon="el-icon-download" @click="downloadChart('heatChart')">
                下载
              </el-button>
            </div>
          </template>
          <div ref="heatChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 核心功能分区 -->
    <el-row :gutter="20" class="function-section">
      <!-- 左侧消息与待办区 -->
      <el-col :xs="24" :lg="8" class="function-col">
        <el-card class="function-card" shadow="hover">
          <template #header>
            <div class="function-header">
              <span class="function-title">待办事项</span>
              <el-button type="text" @click="navigateTo('todo')">更多</el-button>
            </div>
          </template>
          <div class="todo-list">
            <div v-for="todo in todos" :key="todo.id" class="todo-item" :class="{
              urgent: isUrgent(todo.endTime),
              completed: todo.status === '1'
            }">
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-desc">{{ todo.content }}</div>
                <div class="todo-meta">
                  <span class="todo-time">{{ formatTime(todo.endTime) }}</span>
                  <el-tag :type="getTodoPriorityType(todo.priority)" size="mini">
                    {{ getTodoPriorityText(todo.priority) }}
                  </el-tag>
                </div>
              </div>
              <div class="todo-actions">
                <el-tag :type="getTodoStatusType(todo.status)" size="small">
                  {{ getTodoStatusText(todo.status) }}
                </el-tag>
              </div>
            </div>
            <div v-if="todos.length === 0" class="empty-state">
              <i class="el-icon-check"></i>
              <p>暂无待办事项</p>
            </div>
          </div>
        </el-card>

        <el-card class="function-card" shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="function-header">
              <span class="function-title">最新消息</span>
              <el-button type="text" @click="navigateTo('message')">更多</el-button>
            </div>
          </template>
          <div class="message-list">
            <div v-for="message in messages" :key="message.id" class="message-item" :class="{ unread: message.status === '未读' }">
              <div class="message-avatar">
                <i class="el-icon-message"></i>
              </div>
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.createTime) }}</div>
              </div>
              <div class="message-actions">
                <el-tag v-if="message.status === '未读'" type="primary" size="small">未读</el-tag>
                <el-button type="text" icon="el-icon-delete" size="mini" @click="deleteMessage(message.id)"></el-button>
              </div>
            </div>
            <div v-if="messages.length === 0" class="empty-state">
              <i class="el-icon-chat-dot-round"></i>
              <p>暂无新消息</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧数据明细与公告区 -->
      <el-col :xs="24" :lg="16" class="function-col">
        <!-- 作业明细 -->
        <el-card class="function-card" shadow="hover">
          <template #header>
            <div class="function-header">
              <span class="function-title">作业明细</span>
              <div class="function-actions">
                <el-button size="small" icon="el-icon-refresh" @click="loadHomeworkDetails">刷新</el-button>
                <el-button size="small" icon="el-icon-download" @click="exportHomework">导出</el-button>
                <el-button size="small" icon="el-icon-printer" @click="printHomework">打印</el-button>
                <el-button size="small" icon="el-icon-document-add" @click="showHomeworkDetail">详情</el-button>
              </div>
            </div>
          </template>

          <!-- 作业筛选表单 -->
          <el-form :model="homeworkFilter" :inline="true" class="filter-form">
            <el-form-item label="作业名称">
              <el-input v-model="homeworkFilter.title" placeholder="输入作业名称" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="课程">
              <el-input v-model="homeworkFilter.course" placeholder="输入课程名称" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="发布时间">
              <el-date-picker
                v-model="homeworkFilter.publishTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item label="截止时间">
              <el-date-picker
                v-model="homeworkFilter.deadlineRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item label="过期状态">
              <el-select v-model="homeworkFilter.expireStatus" placeholder="选择状态" clearable style="width: 120px">
                <el-option label="进行中" value="进行中" />
                <el-option label="已过期" value="已过期" />
              </el-select>
            </el-form-item>
            <el-form-item label="批改状态">
              <el-select v-model="homeworkFilter.gradingStatus" placeholder="选择状态" clearable style="width: 120px">
                <el-option label="已批改" value="已批改" />
                <el-option label="未批改" value="未批改" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="handleHomeworkSearch">筛选</el-button>
              <el-button icon="el-icon-refresh" @click="handleHomeworkReset">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="table-container">
            <el-table
              :data="homeworkDetails"
              style="width: 100%"
              @sort-change="handleHomeworkSort"
              :default-sort="{prop: 'deadline', order: 'ascending'}"
              v-loading="homeworkLoading"
            >
              <el-table-column prop="title" label="作业名称" min-width="150" sortable="custom"></el-table-column>
              <el-table-column prop="course" label="课程" width="120" sortable="custom"></el-table-column>
              <el-table-column prop="publishTime" label="发布时间" width="120" sortable="custom">
                <template #default="scope">
                  {{ formatTime(scope.row.publishTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="deadline" label="截止时间" width="120" sortable="custom">
                <template #default="scope">
                  {{ formatTime(scope.row.deadline) }}
                </template>
              </el-table-column>
              <el-table-column label="提交情况" width="100" sortable="custom" :sort-by="['submittedCount', 'pendingCount']">
                <template #default="scope">
                  <el-progress
                    :percentage="getSubmissionPercentage(scope.row)"
                    :show-text="false"
                    :color="getProgressColor(getSubmissionPercentage(scope.row))"
                  />
                  <div class="submission-text">
                    {{ scope.row.submittedCount }}/{{ scope.row.submittedCount + scope.row.pendingCount }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80" sortable="custom">
                <template #default="scope">
                  <el-tag :type="scope.row.status === '进行中' ? 'success' : 'danger'" size="small">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="scope">
                  <el-button type="text" @click="showHomeworkItemDetail(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>

        <!-- 最新公告 -->
        <el-card class="function-card" shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="function-header">
              <span class="function-title">最新公告</span>
              <div class="function-actions">
                <el-button size="small" icon="el-icon-refresh" @click="loadNotices">刷新</el-button>
                <el-button size="small" icon="el-icon-download" @click="exportNotices">导出</el-button>
                <el-button size="small" icon="el-icon-printer" @click="printNotices">打印</el-button>
              </div>
            </div>
          </template>

          <!-- 公告筛选表单 -->
          <el-form :model="noticeFilter" :inline="true" class="filter-form">
            <el-form-item label="标题">
              <el-input v-model="noticeFilter.title" placeholder="输入标题关键词" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="发布人">
              <el-input v-model="noticeFilter.author" placeholder="输入发布人" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="发布时间">
              <el-date-picker
                v-model="noticeFilter.publishTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="handleNoticeSearch">筛选</el-button>
              <el-button icon="el-icon-refresh" @click="handleNoticeReset">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="notice-list">
            <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="showNoticeDetail(notice)">
              <div class="notice-badge" v-if="isNewNotice(notice.createTime)">NEW</div>
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-meta">
                  <span class="notice-author">{{ notice.author }}</span>
                  <span class="notice-time">{{ formatTime(notice.createTime) }}</span>
                </div>
                <div class="notice-preview">{{ getNoticePreview(notice.content) }}</div>
              </div>
              <div class="notice-arrow">
                <i class="el-icon-arrow-right"></i>
              </div>
            </div>
            <div v-if="notices.length === 0" class="empty-state">
              <i class="el-icon-document"></i>
              <p>暂无公告</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志查阅区 -->
    <el-row class="log-section">
      <el-col :span="24">
        <el-card class="log-card" shadow="hover">
          <template #header>
            <div class="log-header">
              <span class="log-title">操作日志</span>
              <div class="log-actions">
                <el-button size="small" icon="el-icon-refresh" @click="loadOperationLogs">刷新</el-button>
                <el-button size="small" icon="el-icon-download" @click="exportLogs">导出</el-button>
                <el-button size="small" icon="el-icon-printer" @click="printLogs">打印</el-button>
              </div>
            </div>
          </template>

          <!-- 日志筛选表单 -->
          <el-form :model="logFilter" :inline="true" class="filter-form">
            <el-form-item label="操作类型">
              <el-select v-model="logFilter.businessType" placeholder="选择操作类型" clearable style="width: 120px">
                <el-option label="新增" value="0" />
                <el-option label="修改" value="1" />
                <el-option label="删除" value="2" />
                <el-option label="登录" value="10" />
              </el-select>
            </el-form-item>
            <el-form-item label="操作人">
              <el-input v-model="logFilter.operator" placeholder="输入操作人" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="操作时间">
              <el-date-picker
                v-model="logFilter.operateTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="handleLogSearch">筛选</el-button>
              <el-button icon="el-icon-refresh" @click="handleLogReset">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="log-container">
            <el-table :data="operationLogs" style="width: 100%" v-loading="logLoading">
              <el-table-column prop="title" label="操作内容" min-width="200"></el-table-column>
              <el-table-column prop="operator" label="操作人" width="120"></el-table-column>
              <el-table-column prop="operateTime" label="操作时间" width="160">
                <template #default="scope">
                  {{ formatTime(scope.row.operateTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="ip" label="IP地址" width="120"></el-table-column>
              <el-table-column prop="businessType" label="操作类型" width="100">
                <template #default="scope">
                  <el-tag :type="getBusinessTypeTag(scope.row.businessType)" size="small">
                    {{ getBusinessTypeText(scope.row.businessType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button type="text" @click="showLogDetail(scope.row)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 各种对话框 -->
    <!-- 公告详情对话框 -->
    <el-dialog
      title="公告详情"
      v-model="noticeDialogVisible"
      width="50%"
      center
    >
      <div v-if="currentNotice" class="notice-detail-content">
        <h3 style="text-align: center; margin-bottom: 20px;">{{ currentNotice.title }}</h3>
        <div style="text-align: center; color: #909399; margin-bottom: 20px;">
          <span>发布人：{{ currentNotice.author }}</span>
          <span style="margin-left: 20px;">发布时间：{{ formatTime(currentNotice.createTime) }}</span>
        </div>
        <div class="notice-content" v-html="currentNotice.content"></div>
      </div>
    </el-dialog>

    <!-- 日志详情对话框 -->
    <el-dialog
      title="操作日志详情"
      v-model="logDialogVisible"
      width="60%"
      center
    >
      <div v-if="currentLog" class="log-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="操作内容">{{ currentLog.title }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ getBusinessTypeText(currentLog.businessType) }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ formatTime(currentLog.operateTime) }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
          <el-descriptions-item label="请求方法">-</el-descriptions-item>
          <el-descriptions-item label="请求参数" :span="2">-</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 天气管理对话框 -->
    <el-dialog
      title="天气管理"
      v-model="weatherManagementVisible"
      width="40%"
    >
      <div class="weather-management">
        <el-form :model="weatherConfig" label-width="100px">
          <el-form-item label="城市">
            <el-input v-model="weatherConfig.city" placeholder="请输入城市名称"></el-input>
          </el-form-item>
          <el-form-item label="API密钥">
            <el-input v-model="weatherConfig.apiKey" placeholder="请输入天气API密钥"></el-input>
          </el-form-item>
          <el-form-item label="自动刷新">
            <el-switch v-model="weatherConfig.autoRefresh"></el-switch>
            <span style="margin-left: 10px; color: #909399;">每30分钟自动刷新</span>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="weatherManagementVisible = false">取消</el-button>
          <el-button type="primary" @click="saveWeatherConfig">保存</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getWeatherData,
  getCoreMetrics,
  getChartData,
  getTodos,
  getMessages,
  getHomeworkDetails,
  getNotices,
  getOperationLogs,
  getHomeworkByStatus
} from '@/api/proj_fz/dashboard'

export default {
  name: 'Dashboard',
  data() {
    return {
      weather: {
        city: '',
        temperature: '',
        weather: '',
        humidity: '',
        wind: '',
        forecast: []
      },
      coreMetrics: {
        totalCourses: 0,
        activeClasses: 0,
        todayDeadline: 0,
        ongoingExams: 0,
        pendingTodos: 0,
        gradedCount: 0,
        gradingRate: 0,
        courseTrend: 0,
        classTrend: 0,
        homeworkTrend: 0,
        examTrend: 0,
        todoTrend: 0,
        gradingTrend: 0
      },
      chartData: {
        trendData: [],
        submissionStatus: {
          submitted: 0,
          pending: 0,
          graded: 0
        },
        scoreStats: {
          average: 0,
          max: 0,
          passRate: 0
        },
        scoreDistribution: [],
        courseScores: [],
        attendanceRates: [],
        participationHeat: []
      },
      todos: [],
      messages: [],
      homeworkDetails: [],
      notices: [],
      operationLogs: [],

      // 筛选条件
      homeworkFilter: {
        title: '',
        course: '',
        publishTimeRange: [],
        deadlineRange: [],
        expireStatus: '',
        gradingStatus: '',
        sortField: 'deadline',
        sortOrder: 'asc'
      },
      noticeFilter: {
        title: '',
        author: '',
        publishTimeRange: []
      },
      logFilter: {
        businessType: '',
        operator: '',
        operateTimeRange: []
      },

      // 对话框控制
      noticeDialogVisible: false,
      logDialogVisible: false,
      weatherManagementVisible: false,
      currentNotice: null,
      currentLog: null,

      // 状态控制
      weatherDetailVisible: false,
      timeRange: 'week',
      homeworkLoading: false,
      logLoading: false,
      statusChartType: 'pie',

      // 配置
      weatherConfig: {
        city: '成都',
        apiKey: '3ae6faeef4eb83bb9c4881d9ec2d12cf',
        autoRefresh: true
      },

      charts: {},
      resizeTimer: null,
      refreshInterval: null,
      weatherRefreshInterval: null
    }
  },
  computed: {
    coreMetricsList() {
      return [
        {
          key: 'courses',
          title: '总课程数',
          value: this.coreMetrics.totalCourses,
          trend: this.coreMetrics.courseTrend,
          icon: 'el-icon-notebook-2',
          color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          route: 'course'
        },
        {
          key: 'classes',
          title: '进行中课堂',
          value: this.coreMetrics.activeClasses,
          trend: this.coreMetrics.classTrend,
          icon: 'el-icon-video-camera',
          color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
          route: 'classroom'
        },
        {
          key: 'deadline',
          title: '今日截止作业',
          value: this.coreMetrics.todayDeadline,
          trend: this.coreMetrics.homeworkTrend,
          icon: 'el-icon-time',
          color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
          route: 'homework'
        },
        {
          key: 'exams',
          title: '进行中考试',
          value: this.coreMetrics.ongoingExams,
          trend: this.coreMetrics.examTrend,
          icon: 'el-icon-document',
          color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
          route: 'exam'
        },
        {
          key: 'todos',
          title: '待办事项',
          value: this.coreMetrics.pendingTodos,
          trend: this.coreMetrics.todoTrend,
          icon: 'el-icon-list',
          color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
          route: 'todo'
        },
        {
          key: 'grading',
          title: '已批改数',
          value: this.coreMetrics.gradedCount,
          trend: this.coreMetrics.gradingTrend,
          icon: 'el-icon-edit',
          color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
          route: 'grading',
          subTitle: `批改率: ${this.coreMetrics.gradingRate}%`
        }
      ]
    },
    statusSummary() {
      const total = this.chartData.submissionStatus.submitted +
        this.chartData.submissionStatus.pending +
        this.chartData.submissionStatus.graded
      if (total === 0) return []

      return [
        {
          name: '已提交',
          value: this.chartData.submissionStatus.submitted,
          percentage: Math.round((this.chartData.submissionStatus.submitted / total) * 100)
        },
        {
          name: '未提交',
          value: this.chartData.submissionStatus.pending,
          percentage: Math.round((this.chartData.submissionStatus.pending / total) * 100)
        },
        {
          name: '已批改',
          value: this.chartData.submissionStatus.graded,
          percentage: Math.round((this.chartData.submissionStatus.graded / total) * 100)
        }
      ]
    }
  },
  mounted() {
    this.loadAllData()
    this.initResizeHandler()

    // 每5分钟自动刷新数据
    this.refreshInterval = setInterval(() => {
      this.loadAllData()
    }, 300000)

    // 天气自动刷新
    if (this.weatherConfig.autoRefresh) {
      this.weatherRefreshInterval = setInterval(() => {
        this.refreshWeather()
      }, 1800000) // 30分钟
    }
  },
  beforeDestroy() {
    this.destroyCharts()
    this.removeResizeHandler()
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
    }
    if (this.weatherRefreshInterval) {
      clearInterval(this.weatherRefreshInterval)
    }
  },
  methods: {
    async loadAllData() {
      try {
        const [
          weatherRes,
          metricsRes,
          chartRes,
          todosRes,
          messagesRes
        ] = await Promise.all([
          getWeatherData(),
          getCoreMetrics(),
          getChartData(),
          getTodos(),
          getMessages()
        ])

        this.weather = weatherRes.data
        this.coreMetrics = metricsRes.data
        this.chartData = chartRes.data
        this.todos = todosRes.data
        this.messages = messagesRes.data

        // 加载带筛选的数据
        await this.loadHomeworkDetails()
        await this.loadNotices()
        await this.loadOperationLogs()

        this.$nextTick(() => {
          this.initCharts()
        })
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('数据加载失败')
      }
    },

    async loadHomeworkDetails() {
      this.homeworkLoading = true
      try {
        const params = this.buildHomeworkParams()
        const res = await getHomeworkDetails(params)
        this.homeworkDetails = res.data
      } catch (error) {
        console.error('加载作业明细失败:', error)
        this.$message.error('加载作业明细失败')
      } finally {
        this.homeworkLoading = false
      }
    },

    async loadNotices() {
      try {
        const params = this.buildNoticeParams()
        const res = await getNotices(params)
        this.notices = res.data
      } catch (error) {
        console.error('加载公告失败:', error)
        this.$message.error('加载公告失败')
      }
    },

    async loadOperationLogs() {
      this.logLoading = true
      try {
        const params = this.buildLogParams()
        const res = await getOperationLogs(params)
        this.operationLogs = res.data
      } catch (error) {
        console.error('加载操作日志失败:', error)
        this.$message.error('加载操作日志失败')
      } finally {
        this.logLoading = false
      }
    },

    buildHomeworkParams() {
      const params = {}
      if (this.homeworkFilter.title) {
        params.title = this.homeworkFilter.title
      }
      if (this.homeworkFilter.course) {
        params.course = this.homeworkFilter.course
      }
      if (this.homeworkFilter.publishTimeRange && this.homeworkFilter.publishTimeRange.length === 2) {
        params.startTime = this.homeworkFilter.publishTimeRange[0]
        params.endTime = this.homeworkFilter.publishTimeRange[1]
      }
      if (this.homeworkFilter.deadlineRange && this.homeworkFilter.deadlineRange.length === 2) {
        params.deadlineStart = this.homeworkFilter.deadlineRange[0]
        params.deadlineEnd = this.homeworkFilter.deadlineRange[1]
      }
      if (this.homeworkFilter.expireStatus) {
        params.expireStatus = this.homeworkFilter.expireStatus
      }
      if (this.homeworkFilter.gradingStatus) {
        params.gradingStatus = this.homeworkFilter.gradingStatus
      }
      if (this.homeworkFilter.sortField) {
        params.sortField = this.homeworkFilter.sortField
        params.sortOrder = this.homeworkFilter.sortOrder
      }
      return params
    },

    buildNoticeParams() {
      const params = {}
      if (this.noticeFilter.title) {
        params.title = this.noticeFilter.title
      }
      if (this.noticeFilter.author) {
        params.author = this.noticeFilter.author
      }
      if (this.noticeFilter.publishTimeRange && this.noticeFilter.publishTimeRange.length === 2) {
        params.startTime = this.noticeFilter.publishTimeRange[0]
        params.endTime = this.noticeFilter.publishTimeRange[1]
      }
      return params
    },

    buildLogParams() {
      const params = {}
      if (this.logFilter.businessType) {
        params.businessType = this.logFilter.businessType
      }
      if (this.logFilter.operator) {
        params.operator = this.logFilter.operator
      }
      if (this.logFilter.operateTimeRange && this.logFilter.operateTimeRange.length === 2) {
        params.startTime = this.logFilter.operateTimeRange[0]
        params.endTime = this.logFilter.operateTimeRange[1]
      }
      return params
    },

    // 筛选处理
    handleHomeworkSearch() {
      this.loadHomeworkDetails()
    },

    handleHomeworkReset() {
      this.homeworkFilter = {
        title: '',
        course: '',
        publishTimeRange: [],
        deadlineRange: [],
        expireStatus: '',
        gradingStatus: '',
        sortField: 'deadline',
        sortOrder: 'asc'
      }
      this.loadHomeworkDetails()
    },

    handleNoticeSearch() {
      this.loadNotices()
    },

    handleNoticeReset() {
      this.noticeFilter = {
        title: '',
        author: '',
        publishTimeRange: []
      }
      this.loadNotices()
    },

    handleLogSearch() {
      this.loadOperationLogs()
    },

    handleLogReset() {
      this.logFilter = {
        businessType: '',
        operator: '',
        operateTimeRange: []
      }
      this.loadOperationLogs()
    },

    handleHomeworkSort({ column, prop, order }) {
      if (prop && order) {
        this.homeworkFilter.sortField = prop
        this.homeworkFilter.sortOrder = order === 'ascending' ? 'asc' : 'desc'
        this.loadHomeworkDetails()
      }
    },

    // 图表交互
    initCharts() {
      this.initTrendChart()
      this.initStatusChart()
      this.initScoreChart()
      this.initCourseChart()
      this.initAttendanceChart()
      this.initHeatChart()
    },

    initTrendChart() {
      const chartDom = this.$refs.trendChart
      if (!chartDom) return

      const chart = echarts.init(chartDom)
      this.charts.trendChart = chart

      const data = this.chartData.trendData
      const dates = data.map(item => item.date)
      const submissions = data.map(item => item.submissions)

      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const date = params[0].axisValue
            const value = params[0].data
            return `${date}<br/>提交数: ${value}`
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLine: {
            lineStyle: {
              color: '#E4E7ED'
            }
          },
          axisLabel: {
            color: '#606266'
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            lineStyle: {
              color: '#E4E7ED'
            }
          },
          axisLabel: {
            color: '#606266'
          },
          splitLine: {
            lineStyle: {
              color: '#F0F2F5'
            }
          }
        },
        series: [
          {
            name: '提交数',
            type: 'line',
            data: submissions,
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              width: 3,
              color: '#409EFF'
            },
            itemStyle: {
              color: '#409EFF'
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ])
            }
          }
        ]
      }

      chart.setOption(option)
    },

    initStatusChart() {
      const chartDom = this.$refs.statusChart
      if (!chartDom) return

      const chart = echarts.init(chartDom)
      this.charts.statusChart = chart

      const data = [
        { name: '已提交', value: this.chartData.submissionStatus.submitted },
        { name: '未提交', value: this.chartData.submissionStatus.pending },
        { name: '已批改', value: this.chartData.submissionStatus.graded }
      ]

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'center',
          textStyle: {
            color: '#606266'
          }
        },
        series: [
          {
            name: '提交状态',
            type: this.statusChartType,
            radius: this.statusChartType === 'ring' ? ['40%', '70%'] : '70%',
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
                fontSize: '12',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: data
          }
        ]
      }

      chart.setOption(option)

      // 添加点击事件
      chart.on('click', (params) => {
        this.handleChartClick('status', params.name)
      })
    },

    initScoreChart() {
      const chartDom = this.$refs.scoreChart
      if (!chartDom) return

      const chart = echarts.init(chartDom)
      this.charts.scoreChart = chart

      const data = this.chartData.scoreDistribution

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.range),
          axisLine: {
            lineStyle: {
              color: '#E4E7ED'
            }
          },
          axisLabel: {
            color: '#606266'
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            lineStyle: {
              color: '#E4E7ED'
            }
          },
          axisLabel: {
            color: '#606266'
          },
          splitLine: {
            lineStyle: {
              color: '#F0F2F5'
            }
          }
        },
        series: [
          {
            name: '人数',
            type: 'bar',
            data: data.map(item => item.count),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            }
          }
        ]
      }

      chart.setOption(option)
    },

    initCourseChart() {
      const chartDom = this.$refs.courseChart
      if (!chartDom) return

      const chart = echarts.init(chartDom)
      this.charts.courseChart = chart

      const data = this.chartData.courseScores

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          axisLine: {
            lineStyle: {
              color: '#E4E7ED'
            }
          },
          axisLabel: {
            color: '#606266'
          }
        },
        yAxis: {
          type: 'category',
          data: data.map(item => item.courseName),
          axisLine: {
            lineStyle: {
              color: '#E4E7ED'
            }
          },
          axisLabel: {
            color: '#606266'
          }
        },
        series: [
          {
            name: '平均分',
            type: 'bar',
            data: data.map(item => item.averageScore),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#67C23A' },
                { offset: 1, color: '#85CE61' }
              ])
            }
          }
        ]
      }

      chart.setOption(option)
    },

    initAttendanceChart() {
      const chartDom = this.$refs.attendanceChart
      if (!chartDom) return

      const chart = echarts.init(chartDom)
      this.charts.attendanceChart = chart

      const data = this.chartData.attendanceRates

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          max: 100,
          axisLine: {
            lineStyle: {
              color: '#E4E7ED'
            }
          },
          axisLabel: {
            color: '#606266',
            formatter: '{value}%'
          }
        },
        yAxis: {
          type: 'category',
          data: data.map(item => item.className),
          axisLine: {
            lineStyle: {
              color: '#E4E7ED'
            }
          },
          axisLabel: {
            color: '#606266'
          }
        },
        series: [
          {
            name: '签到率',
            type: 'bar',
            data: data.map(item => item.attendanceRate),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#E6A23C' },
                { offset: 1, color: '#EBC78E' }
              ])
            }
          }
        ]
      }

      chart.setOption(option)
    },

    initHeatChart() {
      const chartDom = this.$refs.heatChart
      if (!chartDom) return

      const chart = echarts.init(chartDom)
      this.charts.heatChart = chart

      const data = this.chartData.participationHeat

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['作业参与', '签到参与', '论坛参与']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: data.map(item => item.studentName)
        },
        series: [
          {
            name: '作业参与',
            type: 'bar',
            stack: 'total',
            data: data.map(item => item.homeworkCount)
          },
          {
            name: '签到参与',
            type: 'bar',
            stack: 'total',
            data: data.map(item => item.attendanceCount)
          },
          {
            name: '论坛参与',
            type: 'bar',
            stack: 'total',
            data: data.map(item => item.forumCount)
          }
        ]
      }

      chart.setOption(option)
    },

    // 图表点击事件
    handleChartClick(chartType, name) {
      if (chartType === 'status') {
        let status = ''
        if (name === '未提交') {
          status = 'pending'
        } else if (name === '已提交') {
          status = 'submitted'
        } else if (name === '已批改') {
          status = 'graded'
        }

        if (status) {
          this.homeworkFilter = {
            ...this.homeworkFilter,
            gradingStatus: status === 'graded' ? '已批改' : '未批改'
          }
          this.loadHomeworkDetails()
          this.$message.success(`已筛选${name}的作业`)
        }
      }
    },

    handleChartTypeChange(command) {
      this.statusChartType = command
      this.$nextTick(() => {
        this.initStatusChart()
      })
    },

    handleChartAction(command) {
      switch (command) {
        case 'download':
          this.downloadChart('trendChart')
          break
        case 'print':
          this.printChart('trendChart')
          break
        case 'export':
          this.exportChartData()
          break
      }
    },

    // 其他方法
    getWeatherIcon(weather) {
      const iconMap = {
        '晴': 'el-icon-sunny',
        '多云': 'el-icon-cloudy',
        '阴': 'el-icon-overcast',
        '雨': 'el-icon-rain',
        '雪': 'el-icon-snow',
        '雷阵雨': 'el-icon-thunderstorm'
      }
      return iconMap[weather] || 'el-icon-sunny'
    },

    getTrendClass(trend) {
      return trend > 0 ? 'positive' : trend < 0 ? 'negative' : 'neutral'
    },

    getTrendIcon(trend) {
      return trend > 0 ? 'el-icon-top' : trend < 0 ? 'el-icon-bottom' : 'el-icon-minus'
    },

    getSubmissionPercentage(row) {
      const total = row.submittedCount + row.pendingCount
      if (total === 0) return 0
      return Math.round((row.submittedCount / total) * 100)
    },

    getProgressColor(percentage) {
      if (percentage >= 80) return '#67C23A'
      if (percentage >= 60) return '#E6A23C'
      return '#F56C6C'
    },

    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    formatForecastDate(date) {
      if (!date) return ''
      const today = new Date()
      const targetDate = new Date(date)
      const diff = targetDate.getDate() - today.getDate()

      if (diff === 0) return '今天'
      if (diff === 1) return '明天'
      if (diff === 2) return '后天'

      return date
    },

    isUrgent(endTime) {
      if (!endTime) return false
      const now = new Date()
      const end = new Date(endTime)
      const diff = end - now
      return diff > 0 && diff < 24 * 60 * 60 * 1000 // 1天内
    },

    getTodoStatusType(status) {
      const typeMap = {
        '0': 'warning',
        '1': 'success',
        '2': 'danger'
      }
      return typeMap[status] || 'info'
    },

    getTodoStatusText(status) {
      const textMap = {
        '0': '未完成',
        '1': '已完成',
        '2': '过期'
      }
      return textMap[status] || '未知'
    },

    getTodoPriorityType(priority) {
      const typeMap = {
        '0': 'info',
        '1': 'warning',
        '2': 'danger'
      }
      return typeMap[priority] || 'info'
    },

    getTodoPriorityText(priority) {
      const textMap = {
        '0': '低',
        '1': '中',
        '2': '高'
      }
      return textMap[priority] || '未知'
    },

    getBusinessTypeTag(businessType) {
      const typeMap = {
        '0': 'success', // 新增
        '1': 'primary', // 修改
        '2': 'danger',  // 删除
        '10': 'info'    // 登录
      }
      return typeMap[businessType] || 'info'
    },

    getBusinessTypeText(businessType) {
      const textMap = {
        '0': '新增',
        '1': '修改',
        '2': '删除',
        '10': '登录'
      }
      return textMap[businessType] || '其他'
    },

    isNewNotice(createTime) {
      if (!createTime) return false
      const noticeTime = new Date(createTime)
      const now = new Date()
      const diff = now - noticeTime
      return diff < 24 * 60 * 60 * 1000 // 1天内
    },

    getNoticePreview(content) {
      if (!content) return ''
      // 移除HTML标签
      const text = content.replace(/<[^>]*>/g, '')
      return text.length > 50 ? text.substring(0, 50) + '...' : text
    },

    showNoticeDetail(notice) {
      this.currentNotice = notice
      this.noticeDialogVisible = true
    },

    showLogDetail(log) {
      this.currentLog = log
      this.logDialogVisible = true
    },

    toggleWeatherDetail() {
      this.weatherDetailVisible = !this.weatherDetailVisible
    },

    async refreshWeather() {
      try {
        const res = await getWeatherData()
        this.weather = res.data
        this.$message.success('天气信息已刷新')
      } catch (error) {
        console.error('刷新天气失败:', error)
        this.$message.error('刷新天气失败')
      }
    },

    showWeatherManagement() {
      this.weatherManagementVisible = true
    },

    saveWeatherConfig() {
      localStorage.setItem('weatherConfig', JSON.stringify(this.weatherConfig))
      this.weatherManagementVisible = false
      this.$message.success('天气配置已保存')
      this.refreshWeather()
    },

    handleTimeRangeChange() {
      this.loadAllData()
    },

    downloadChart(chartName) {
      const chart = this.charts[chartName]
      if (chart) {
        const url = chart.getDataURL({
          pixelRatio: 2,
          backgroundColor: '#fff'
        })
        const link = document.createElement('a')
        link.href = url
        link.download = `${chartName}.png`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        this.$message.success('图表下载成功')
      }
    },

    printChart(chartName) {
      const chart = this.charts[chartName]
      if (chart) {
        const url = chart.getDataURL({
          pixelRatio: 2,
          backgroundColor: '#fff'
        })
        const printWindow = window.open('', '_blank')
        printWindow.document.write(`
          <html>
            <head>
              <title>打印图表</title>
              <style>
                body { text-align: center; margin: 20px; }
                img { max-width: 100%; height: auto; }
              </style>
            </head>
            <body>
              <img src="${url}" alt="图表">
              <script>
                window.onload = function() {
                  window.print();
                  setTimeout(function() {
                    window.close();
                  }, 1000);
                }
              <\/script>
            </body>
          </html>
        `)
        printWindow.document.close()
      }
    },

    exportChartData() {
      this.$message.info('导出图表数据功能开发中')
    },

    navigateTo(module) {
      const routes = {
        course: '/proj_lw/course',
        classroom: '/proj_lw/classroom',
        homework: '/proj_lwj/homework_publish',
        exam: '/proj_lwj_exam/exam_portal',
        todo: '/proj_cyq/todo',
        grading: '/proj_lwj/homework_grading',
        message: '/proj_cyq/message'
      }
      if (routes[module]) {
        this.$router.push(routes[module])
      }
    },

    deleteMessage(messageId) {
      this.$confirm('确定删除这条消息吗？', '提示', {
        type: 'warning'
      }).then(() => {
        this.messages = this.messages.filter(msg => msg.id !== messageId)
        this.$message.success('消息删除成功')
      })
    },

    showHomeworkDetail() {
      this.$message.info('作业详情功能开发中')
    },

    showHomeworkItemDetail(item) {
      this.$message.info(`查看作业详情: ${item.title}`)
    },

    exportHomework() {
      this.$message.info('导出作业明细功能开发中')
    },

    exportNotices() {
      this.$message.info('导出公告功能开发中')
    },

    exportLogs() {
      this.$message.info('导出日志功能开发中')
    },

    printHomework() {
      this.$message.info('打印作业明细功能开发中')
    },

    printNotices() {
      this.$message.info('打印公告功能开发中')
    },

    printLogs() {
      this.$message.info('打印日志功能开发中')
    },

    initResizeHandler() {
      this.resizeTimer = null
      window.addEventListener('resize', this.handleResize)
    },

    handleResize() {
      if (this.resizeTimer) {
        clearTimeout(this.resizeTimer)
      }
      this.resizeTimer = setTimeout(() => {
        Object.values(this.charts).forEach(chart => {
          if (chart) {
            chart.resize()
          }
        })
      }, 300)
    },

    removeResizeHandler() {
      window.removeEventListener('resize', this.handleResize)
      if (this.resizeTimer) {
        clearTimeout(this.resizeTimer)
      }
    },

    destroyCharts() {
      Object.values(this.charts).forEach(chart => {
        if (chart) {
          chart.dispose()
        }
      })
      this.charts = {}
    }
  }
}
</script>

<style scoped>
/* Mac Style for Dashboard */
.app-container {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
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

/* Weather Section */
.weather-section {
  margin-bottom: 20px;
}

.weather-card {
  background: linear-gradient(135deg, #0071e3 0%, #40a9ff 100%) !important;
  color: white;
  border: none;
}

.weather-content {
  padding: 0;
}

.weather-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.weather-main:hover {
  background: rgba(255, 255, 255, 0.1);
}

.weather-icon {
  font-size: 48px;
  margin-right: 24px;
  opacity: 0.95;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.weather-info {
  flex: 1;
}

.temperature {
  font-size: 42px;
  font-weight: 600;
  margin-bottom: 4px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.city {
  font-size: 20px;
  opacity: 0.95;
  margin-bottom: 4px;
  font-weight: 500;
}

.current-weather {
  font-size: 16px;
  opacity: 0.9;
}

.weather-actions {
  display: flex;
  gap: 12px;
}

.weather-actions .el-button {
  color: white;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  padding: 8px 16px;
  border-radius: 980px;
}

.weather-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.weather-detail {
  padding: 24px;
  background: rgba(255, 255, 255, 0.1);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.detail-item .label {
  opacity: 0.9;
  font-size: 14px;
}

.detail-item .value {
  font-weight: 600;
  font-size: 16px;
}

.weather-forecast {
  text-align: center;
}

.forecast-title {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 20px;
  font-weight: 600;
}

.forecast-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
}

.forecast-item {
  background: rgba(255, 255, 255, 0.15);
  padding: 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.forecast-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.forecast-date {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.forecast-icon {
  font-size: 28px;
  margin-bottom: 10px;
  display: block;
}

.forecast-temp {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}

.forecast-weather {
  font-size: 13px;
  opacity: 0.9;
}

/* Metrics Section */
.metrics-section {
  margin-bottom: 24px;
}

.metric-col {
  margin-bottom: 24px;
}

.core-metric {
  height: 140px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  border-radius: 18px;
  overflow: hidden;
  position: relative;
}

.core-metric:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.metric-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 24px;
}

.metric-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.metric-icon {
  font-size: 28px;
  color: white;
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 4px;
  line-height: 1.1;
  letter-spacing: -0.5px;
}

.metric-title {
  font-size: 15px;
  color: #86868b;
  margin-bottom: 6px;
  font-weight: 500;
}

.metric-subtitle {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 8px;
}

.metric-trend {
  display: inline-flex;
  align-items: center;
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 980px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.metric-trend.positive {
  background-color: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.metric-trend.negative {
  background-color: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.metric-trend.neutral {
  background-color: rgba(142, 142, 147, 0.1);
  color: #8e8e93;
}

.metric-trend i {
  margin-right: 4px;
  font-size: 11px;
}

/* Chart Section */
.chart-section {
  margin-bottom: 24px;
}

.chart-col {
  margin-bottom: 24px;
}

.chart-card {
  border: none;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.main-chart {
  min-height: 420px;
}

.sub-chart {
  min-height: 320px;
}

.side-chart {
  min-height: 300px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0; /* Reset padding as it's inside el-card__header */
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-container {
  height: 320px;
  width: 100%;
  padding: 10px;
}

.main-chart .chart-container {
  height: 340px;
}

.sub-chart .chart-container {
  height: 260px;
}

.side-chart .chart-container {
  height: 220px;
}

.chart-summary {
  margin-top: 20px;
  padding: 20px;
  background-color: #f5f5f7;
  border-radius: 12px;
  border-left: 4px solid #0071e3;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 4px 0;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-label {
  font-size: 14px;
  color: #86868b;
  font-weight: 500;
}

.summary-value {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.sub-charts-row {
  margin-top: 24px;
}

.sub-chart-col {
  margin-bottom: 24px;
}

/* Function Section */
.function-section {
  margin-bottom: 24px;
}

.function-col {
  margin-bottom: 24px;
}

.function-card {
  border: none;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.function-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.function-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.function-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

.function-actions {
  display: flex;
  gap: 10px;
}

/* Empty State */
.empty-state {
  text-align: center;
  color: #86868b;
  padding: 60px 20px;
  font-size: 15px;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
}

/* Dialog Content */
.notice-detail-content, .log-detail-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 10px;
}

.notice-content {
  line-height: 1.6;
  font-size: 15px;
  color: #1d1d1f;
}

.notice-content >>> img {
  max-width: 100%;
  height: auto;
  border-radius: 12px;
  margin: 16px 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.weather-management {
  padding: 20px 0;
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
  .dashboard-container {
    padding: 20px;
  }
  .metric-value {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }
  .weather-content, .weather-main, .metric-content {
    flex-direction: column;
    text-align: center;
  }
  .weather-icon {
    margin-right: 0;
    margin-bottom: 16px;
  }
  .metric-icon-wrapper {
    margin-right: 0;
    margin-bottom: 16px;
  }
  .chart-container {
    height: 250px;
  }
}
</style>
