<template>
  <div class="app-container dashboard-container">
    <!-- 顶部天气信息 -->
    <el-row :gutter="15" class="weather-section">
      <el-col :span="24">
        <el-card class="weather-card" shadow="hover">
          <div class="weather-content">
            <!-- 天气数据显示 -->
            <div v-if="weather.city" key="weather-data">
              <div class="weather-main" @click="toggleWeatherDetail">
                <i :class="getWeatherIcon(weather.weather)" class="weather-icon"></i>
                <div class="weather-info">
                  <div class="temperature">{{ weather.temperature }}°C</div>
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
            <!-- 天气加载失败提示 -->
            <div v-else key="weather-error" class="weather-error-state">
              <i class="el-icon-warning-outline"></i>
              <span>天气信息加载失败</span>
              <el-button type="text" @click.stop="refreshWeather" class="retry-button">
                <i class="el-icon-refresh"></i> 重试
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 核心指标卡片区 -->
    <el-row :gutter="15" class="metrics-section">
      <el-col :xs="24" :sm="12" :md="8" :lg="4" class="metric-col" v-for="metric in coreMetricsList" :key="metric.key">
        <el-card class="metric-card core-metric" @click.native="navigateTo(metric.route)" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon-wrapper" :style="{ background: metric.color }">
              <i :class="metric.icon" class="metric-icon"></i>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ metric.value }}</div>
              <div class="metric-title">{{ metric.title }}</div>
              <div v-if="metric.subTitle" class="metric-subtitle">{{ metric.subTitle }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要图表区域 -->
    <el-row :gutter="15" class="main-chart-section">
      <!-- 左侧图表区域 -->
      <el-col :xs="24" :lg="16" class="chart-col">
        <!-- 作业提交趋势 -->
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

        <!-- 底部两个小图表 -->
        <el-row :gutter="15" class="sub-charts-row">
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

      <!-- 右侧图表区域 -->
      <el-col :xs="24" :lg="8" class="chart-col">
        <!-- 提交状态分布 -->
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

        <!-- 成绩分布 -->
        <el-card class="chart-card side-chart" shadow="hover" style="margin-top: 15px;">
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
      </el-col>
    </el-row>

    <!-- 功能与数据区域 -->
    <el-row :gutter="15" class="function-section">
      <!-- 左侧消息与热力图区域 -->
      <el-col :xs="24" :lg="8" class="function-col">
        <!-- 学生参与热力图 -->
        <el-card class="function-card" shadow="hover">
          <template #header>
            <div class="function-header">
              <span class="function-title">学生参与热力</span>
              <el-button size="small" icon="el-icon-download" @click="downloadChart('heatChart')">
                下载
              </el-button>
            </div>
          </template>
          <div ref="heatChart" class="chart-container"></div>
        </el-card>

        <!-- 最新消息 -->
        <el-card class="function-card" shadow="hover" style="margin-top: 15px;">
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

      <!-- 右侧数据明细区域 -->
      <el-col :xs="24" :lg="16" class="function-col">
        <!-- 待办事项 -->
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

        <!-- 作业明细 -->
        <el-card class="function-card" shadow="hover" style="margin-top: 15px;">
          <template #header>
            <div class="function-header">
              <span class="function-title">作业明细</span>
              <div class="function-actions">
                <el-button size="small" icon="el-icon-refresh" @click="loadHomeworkDetails">刷新</el-button>
                <el-button size="small" icon="el-icon-download" @click="exportHomework">导出</el-button>
                <el-button size="small" icon="el-icon-printer" @click="printHomework">打印</el-button>
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
              height="300"
              :fit="true"
            >
              <el-table-column prop="title" label="作业名称" min-width="140" sortable="custom" show-overflow-tooltip></el-table-column>
              <el-table-column prop="course" label="课程" min-width="90" sortable="custom" show-overflow-tooltip></el-table-column>
              <el-table-column prop="publishTime" label="发布时间" min-width="110" sortable="custom">
                <template #default="scope">
                  {{ formatTime(scope.row.publishTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="deadline" label="截止时间" min-width="110" sortable="custom">
                <template #default="scope">
                  {{ formatTime(scope.row.deadline) }}
                </template>
              </el-table-column>
              <el-table-column label="提交" min-width="85" sortable="custom" :sort-by="['submittedCount', 'pendingCount']">
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
              <el-table-column prop="status" label="状态" width="75" sortable="custom">
                <template #default="scope">
                  <el-tag :type="scope.row.status === '进行中' ? 'success' : 'danger'" size="small">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="65" fixed="right" align="center">
                <template #default="scope">
                  <el-button
                    size="mini"
                    type="text"
                    icon="el-icon-view"
                    @click="showHomeworkItemDetail(scope.row)"
                  >详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告与日志区域 -->
    <el-row :gutter="15" class="bottom-section">
      <!-- 最新公告 -->
      <el-col :xs="24" :lg="12" class="bottom-col">
        <el-card class="function-card" shadow="hover">
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

          <div class="notice-list" style="height: 300px; overflow-y: auto;">
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

      <!-- 操作日志 -->
      <el-col :xs="24" :lg="12" class="bottom-col">
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

          <div class="log-container" style="height: 300px; overflow-y: auto;">
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

    <!-- 作业明细详情对话框 -->
    <el-dialog :title="homeworkDetailTitle" :visible.sync="homeworkDetailDialogVisible" width="900px" append-to-body>
      <el-descriptions v-if="currentHomeworkDetail" :column="2" border>
        <el-descriptions-item label="作业ID">{{ currentHomeworkDetail.homeworkId }}</el-descriptions-item>
        <el-descriptions-item label="作业名称">{{ currentHomeworkDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ currentHomeworkDetail.course }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatTime(currentHomeworkDetail.publishTime) }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ formatTime(currentHomeworkDetail.deadline) }}</el-descriptions-item>
        <el-descriptions-item label="作业状态">
          <el-tag :type="currentHomeworkDetail.status === '进行中' ? 'success' : 'danger'" size="small">
            {{ currentHomeworkDetail.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交人数">{{ currentHomeworkDetail.submittedCount }}</el-descriptions-item>
        <el-descriptions-item label="未提交人数">{{ currentHomeworkDetail.pendingCount }}</el-descriptions-item>
        <el-descriptions-item label="提交率">{{ getSubmissionPercentage(currentHomeworkDetail) }}%</el-descriptions-item>
        <el-descriptions-item label="批改人数">{{ currentHomeworkDetail.gradedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="批改率">
          {{ currentHomeworkDetail.submittedCount > 0 ?
          Math.round((currentHomeworkDetail.gradedCount || 0) / currentHomeworkDetail.submittedCount * 100) : 0 }}%
        </el-descriptions-item>
        <el-descriptions-item label="平均分">{{ currentHomeworkDetail.averageScore || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最高分">{{ currentHomeworkDetail.maxScore || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最低分">{{ currentHomeworkDetail.minScore || '-' }}</el-descriptions-item>
        <el-descriptions-item label="作业内容" :span="2">
          <div style="max-height: 200px; overflow-y: auto;">
            {{ currentHomeworkDetail.content || '无' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="附件" :span="2">
          <div v-if="currentHomeworkDetail.attachments && currentHomeworkDetail.attachments.length > 0">
            <el-tag v-for="(file, index) in currentHomeworkDetail.attachments" :key="index" style="margin-right: 5px;">
              {{ file.fileName }}
            </el-tag>
          </div>
          <span v-else>无附件</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentHomeworkDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleHomeworkNavigation('first')" :disabled="currentHomeworkIndex <= 0">首条</el-button>
        <el-button @click="handleHomeworkNavigation('prev')" :disabled="currentHomeworkIndex <= 0">上一条</el-button>
        <el-button @click="handleHomeworkNavigation('next')" :disabled="currentHomeworkIndex >= homeworkDetails.length - 1">下一条</el-button>
        <el-button @click="handleHomeworkNavigation('last')" :disabled="currentHomeworkIndex >= homeworkDetails.length - 1">末尾</el-button>
        <el-button type="primary" @click="handlePrintHomeworkDetail">打印</el-button>
        <el-button type="success" @click="handleExportHomeworkDetail">导出</el-button>
        <el-button @click="homeworkDetailDialogVisible = false">关闭</el-button>
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
            <el-input v-model="weatherConfig.apiKey" placeholder="请输入天气API密钥" disabled></el-input>
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
  exportHomeworkDetails,
  exportNotices,
  exportOperationLogs,
  getWeatherConfig,
  updateWeatherConfig,
  getHomeworkDetail,
  exportSingleHomework
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
      homeworkDetailDialogVisible: false,
      currentNotice: null,
      currentLog: null,
      currentHomeworkDetail: null,
      currentHomeworkIndex: 0,
      homeworkDetailTitle: '作业详情',

      // 状态控制
      weatherDetailVisible: false,
      timeRange: 'week',
      homeworkLoading: false,
      logLoading: false,
      statusChartType: 'pie',

      // 配置
      weatherConfig: {
        city: '成都',
        apiKey: '3ae6faeef4eb83bb9c4881d9ec2d12cf', // 此处仅为显示，实际请求的Key在api文件中
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
    this.initWeatherConfig();
    this.loadAllData()
    this.initResizeHandler()

    // 每5分钟自动刷新核心数据
    this.refreshInterval = setInterval(() => {
      this.loadCoreData()
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
    /**
     * @description 主加载函数，隔离了天气API，确保核心数据能正常显示
     */
    async loadAllData() {
      // 1. 先加载天气（非阻塞）
      this.refreshWeather(); // 调用独立的刷新方法，内部处理UI和错误

      // 2. 加载所有核心数据
      this.loadCoreData();
    },

    /**
     * @description 加载除天气外的所有核心数据
     */
    async loadCoreData() {
      try {
        const [
          metricsRes,
          chartRes,
          todosRes,
          messagesRes
        ] = await Promise.all([
          getCoreMetrics(),
          getChartData(),
          getTodos(),
          getMessages()
        ]);

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
        console.error('加载核心数据失败:', error)
        this.$message.error('驾驶舱核心数据加载失败，请刷新页面或联系管理员');
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

    handleHomeworkSort({ prop, order }) {
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

      let option = {}

      if (this.statusChartType === 'bar') {
        // 柱状图配置
        option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          legend: {
            orient: 'horizontal',
            left: 'center',
            top: 'top',
            textStyle: {
              color: '#606266'
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '15%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: data.map(item => item.name),
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
              name: '提交状态',
              type: 'bar',
              data: data.map(item => item.value),
              itemStyle: {
                color: function(params) {
                  const colors = ['#409EFF', '#F56C6C', '#67C23A']
                  return colors[params.dataIndex]
                },
                borderRadius: [8, 8, 0, 0]
              },
              barWidth: '40%'
            }
          ]
        }
      } else {
        // 饼图/环形图配置
        option = {
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
              type: 'pie',
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
      }

      chart.setOption(option)

      // 添加点击事件
      chart.off('click') // 先移除旧的事件监听
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
      this.$message.success(`已切换为${command === 'pie' ? '饼图' : command === 'ring' ? '环形图' : '柱状图'}`)
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
      if (!weather) return 'el-icon-sunny';
      const iconMap = {
        '晴': 'el-icon-sunny',
        '多云': 'el-icon-cloudy',
        '阴': 'el-icon-cloudy', // 阴天也用多云图标
        '雨': 'el-icon-heavy-rain',
        '雪': 'el-icon-light-rain', // 没有雪的图标，用雨代替
        '雷阵雨': 'el-icon-lightning'
      }
      for (const key in iconMap) {
        if (weather.includes(key)) {
          return iconMap[key];
        }
      }
      return 'el-icon-sunny';
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

      today.setHours(0, 0, 0, 0);
      targetDate.setHours(0, 0, 0, 0);

      const diffTime = targetDate - today;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

      if (diffDays === 0) return '今天'
      if (diffDays === 1) return '明天'
      if (diffDays === 2) return '后天'

      return date.substring(5); // 返回 MM-dd
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

    async initWeatherConfig() {
      try {
        const res = await getWeatherConfig();
        if (res.data) {
          if (res.data.city) this.weatherConfig.city = res.data.city;
          if (res.data.apiKey) this.weatherConfig.apiKey = res.data.apiKey; // 仅显示，不直接使用前端调用高德
        }
      } catch (e) {
        console.warn('获取天气配置失败', e);
      }
    },
    async refreshWeather() {
      try {
        const res = await getWeatherData();
        if (res.code === 200 && res.data && res.data.city) {
          this.weather = res.data;
          this.$message.success('天气信息已刷新');
        } else {
          const msg = res.msg || '天气接口调用失败';
          this.$message.error(msg);
          this.weather = { city: '', temperature: '', weather: '', humidity: '', wind: '', forecast: [] };
        }
      } catch (error) {
        console.error('刷新天气失败:', error);
        const errorMessage = (error && error.message) ? error.message : '请检查API Key或网络。';
        this.$message.error(`刷新天气失败: ${errorMessage}`);
        this.weather = { city: '', temperature: '', weather: '', humidity: '', wind: '', forecast: [] };
      }
    },
    saveWeatherConfig() {
      updateWeatherConfig({ apiKey: this.weatherConfig.apiKey, city: this.weatherConfig.city })
        .then(() => {
          this.weatherManagementVisible = false;
          this.$message.success('天气配置已更新，重新获取中...');
          this.refreshWeather();
        })
        .catch(err => {
          console.error('更新天气配置失败', err);
          this.$message.error('更新天气配置失败');
        });
    },

    handleTimeRangeChange() {
      this.loadCoreData();
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


    showHomeworkItemDetail(item) {
      // 获取作业ID，兼容不同的字段名
      const homeworkId = item.homeworkId || item.id
      if (!homeworkId) {
        this.$message.error('无法获取作业ID')
        return
      }

      this.currentHomeworkIndex = this.homeworkDetails.findIndex(h => (h.homeworkId || h.id) === homeworkId)
      getHomeworkDetail(homeworkId).then(response => {
        this.currentHomeworkDetail = response.data
        // 确保homeworkId字段存在
        if (!this.currentHomeworkDetail.homeworkId && this.currentHomeworkDetail.id) {
          this.currentHomeworkDetail.homeworkId = this.currentHomeworkDetail.id
        }
        this.homeworkDetailDialogVisible = true
        this.homeworkDetailTitle = '作业详情'
      }).catch(() => {
        // 如果后端API还没实现，使用列表数据
        this.currentHomeworkDetail = { ...item }
        // 确保homeworkId字段存在
        if (!this.currentHomeworkDetail.homeworkId && this.currentHomeworkDetail.id) {
          this.currentHomeworkDetail.homeworkId = this.currentHomeworkDetail.id
        }
        this.homeworkDetailDialogVisible = true
        this.homeworkDetailTitle = '作业详情'
      })
    },

    // 作业详情导航
    handleHomeworkNavigation(action) {
      let newIndex = this.currentHomeworkIndex
      if (action === 'first') {
        newIndex = 0
      } else if (action === 'prev') {
        newIndex = Math.max(0, this.currentHomeworkIndex - 1)
      } else if (action === 'next') {
        newIndex = Math.min(this.homeworkDetails.length - 1, this.currentHomeworkIndex + 1)
      } else if (action === 'last') {
        newIndex = this.homeworkDetails.length - 1
      }

      if (newIndex !== this.currentHomeworkIndex) {
        this.currentHomeworkIndex = newIndex
        const row = this.homeworkDetails[newIndex]
        const homeworkId = row.homeworkId || row.id

        getHomeworkDetail(homeworkId).then(response => {
          this.currentHomeworkDetail = response.data
          // 确保homeworkId字段存在
          if (!this.currentHomeworkDetail.homeworkId && this.currentHomeworkDetail.id) {
            this.currentHomeworkDetail.homeworkId = this.currentHomeworkDetail.id
          }
        }).catch(() => {
          // 如果后端API还没实现，使用列表数据
          this.currentHomeworkDetail = { ...row }
          // 确保homeworkId字段存在
          if (!this.currentHomeworkDetail.homeworkId && this.currentHomeworkDetail.id) {
            this.currentHomeworkDetail.homeworkId = this.currentHomeworkDetail.id
          }
        })
      }
    },

    // 打印作业详情
    handlePrintHomeworkDetail() {
      const printContent = document.querySelector('.el-descriptions')
      const printWindow = window.open('', '_blank')
      printWindow.document.write('<html><head><title>作业详情打印</title>')
      printWindow.document.write('<style>')
      printWindow.document.write('body { font-family: Arial, sans-serif; padding: 20px; }')
      printWindow.document.write('h2 { text-align: center; }')
      printWindow.document.write('.print-header { margin-bottom: 20px; }')
      printWindow.document.write('table { border-collapse: collapse; width: 100%; }')
      printWindow.document.write('table, th, td { border: 1px solid #ddd; }')
      printWindow.document.write('th, td { padding: 8px; text-align: left; }')
      printWindow.document.write('</style>')
      printWindow.document.write('</head><body>')
      printWindow.document.write('<h2>作业明细详情报表</h2>')
      printWindow.document.write('<div class="print-header">')
      printWindow.document.write('<p>作业名称：' + this.currentHomeworkDetail.title + '</p>')
      printWindow.document.write('<p>打印时间：' + this.parseTime(new Date()) + '</p>')
      printWindow.document.write('</div>')
      printWindow.document.write(printContent.innerHTML)
      printWindow.document.write('</body></html>')
      printWindow.document.close()
      printWindow.print()
    },

    // 导出单个作业详情
    handleExportHomeworkDetail() {
      // 获取作业ID，兼容不同的字段名
      const homeworkId = this.currentHomeworkDetail.homeworkId || this.currentHomeworkDetail.id

      if (!homeworkId) {
        this.$message.error('无法获取作业ID，请重新打开详情')
        return
      }

      this.$confirm('是否确认导出该作业详情?', "提示", { type: "warning" }).then(() => {
        this.$modal.loading("正在导出数据，请稍后...")
        return exportSingleHomework(homeworkId)
      }).then((response) => {
        const fileName = `作业详情_${this.currentHomeworkDetail.title}_${new Date().getTime()}.xlsx`
        this.handleBlobDownload(response, fileName)
        this.$modal.closeLoading()
        this.$modal.msgSuccess("导出成功")
      }).catch((error) => {
        this.$modal.closeLoading()
        if (error !== 'cancel') {
          console.error('导出失败:', error)
        }
      })
    },

    // ===============================================
    // =========== 打印/导出 方法 (已修复) ============
    // ===============================================

    // 辅助方法: 处理Blob下载
    handleBlobDownload(blob, fileName) {
      const link = document.createElement('a')
      const blobUrl = window.URL.createObjectURL(blob)
      link.href = blobUrl
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(blobUrl)
    },

    // 辅助方法: 打印数据 (前端实现)
    printData(title, headers, data) {
      if (!data || data.length === 0) {
        return this.$message.warning('没有可打印的数据');
      }
      let printContent = `
        <html>
          <head>
            <title>${title}</title>
            <style>
              body { font-family: sans-serif; }
              table { width: 100%; border-collapse: collapse; table-layout: fixed;}
              th, td { border: 1px solid #ddd; padding: 8px; text-align: left; word-wrap: break-word; }
              th { background-color: #f2f2f2; }
              h2 { text-align: center; }
            </style>
          </head>
          <body>
            <h2>${title}</h2>
            <table>
              <thead>
                <tr>
      `;
      headers.forEach(header => {
        printContent += `<th>${header.label}</th>`;
      });
      printContent += `
                </tr>
              </thead>
              <tbody>
      `;
      data.forEach(row => {
        printContent += '<tr>';
        headers.forEach(header => {
          let value = row[header.prop] || '';
          if (header.format && typeof header.format === 'function') {
            value = header.format(row); // 传入整行数据以支持复杂格式化
          }
          printContent += `<td>${value}</td>`;
        });
        printContent += '</tr>';
      });
      printContent += `
              </tbody>
            </table>
            <script>
              window.onload = function() {
                window.print();
                setTimeout(function() { window.close(); }, 100);
              }
            <\/script>
          </body>
        </html>
      `;

      const printWindow = window.open('', '_blank', 'height=800,width=1000');
      printWindow.document.write(printContent);
      printWindow.document.close();
      printWindow.focus();
    },

    // 作业明细导出
    exportHomework() {
      this.$confirm('是否确认导出作业明细数据?', "警告", { type: "warning" }).then(() => {
        this.$modal.loading("正在导出数据，请稍后...");
        return exportHomeworkDetails(this.buildHomeworkParams());
      }).then((response) => {
        const fileName = `作业明细_${new Date().getTime()}.xlsx`;
        this.handleBlobDownload(response, fileName);
        this.$modal.closeLoading();
        this.$modal.msgSuccess("导出成功");
      }).catch(() => {
        this.$modal.closeLoading();
      });
    },

    // 公告导出
    exportNotices() {
      this.$confirm('是否确认导出最新公告数据?', "警告", { type: "warning" }).then(() => {
        this.$modal.loading("正在导出数据，请稍后...");
        return exportNotices(this.buildNoticeParams());
      }).then((response) => {
        const fileName = `最新公告_${new Date().getTime()}.xlsx`;
        this.handleBlobDownload(response, fileName);
        this.$modal.closeLoading();
        this.$modal.msgSuccess("导出成功");
      }).catch(() => {
        this.$modal.closeLoading();
      });
    },

    // 日志导出
    exportLogs() {
      this.$confirm('是否确认导出操作日志数据?', "警告", { type: "warning" }).then(() => {
        this.$modal.loading("正在导出数据，请稍后...");
        return exportOperationLogs(this.buildLogParams());
      }).then((response) => {
        const fileName = `操作日志_${new Date().getTime()}.xlsx`;
        this.handleBlobDownload(response, fileName);
        this.$modal.closeLoading();
        this.$modal.msgSuccess("导出成功");
      }).catch(() => {
        this.$modal.closeLoading();
      });
    },

    // 作业明细打印
    printHomework() {
      const headers = [
        { prop: 'title', label: '作业名称' },
        { prop: 'course', label: '课程' },
        { prop: 'publishTime', label: '发布时间', format: (row) => this.formatTime(row.publishTime) },
        { prop: 'deadline', label: '截止时间', format: (row) => this.formatTime(row.deadline) },
        { prop: 'submittedCount', label: '已提交' },
        { prop: 'status', label: '状态' }
      ];
      this.printData('作业明细', headers, this.homeworkDetails);
    },

    // 公告打印
    printNotices() {
      const headers = [
        { prop: 'title', label: '标题' },
        { prop: 'author', label: '发布人' },
        { prop: 'createTime', label: '发布时间', format: (row) => this.formatTime(row.createTime) }
      ];
      this.printData('最新公告', headers, this.notices);
    },

    // 日志打印
    printLogs() {
      const headers = [
        { prop: 'title', label: '操作内容' },
        { prop: 'operator', label: '操作人' },
        { prop: 'operateTime', label: '操作时间', format: (row) => this.formatTime(row.operateTime) },
        { prop: 'ip', label: 'IP地址' },
        { prop: 'businessType', label: '操作类型', format: (row) => this.getBusinessTypeText(row.businessType) }
      ];
      this.printData('操作日志', headers, this.operationLogs);
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
/* 天气加载失败状态样式 */
.weather-error-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-size: 16px;
  color: white;
  opacity: 0.9;
}
.weather-error-state i {
  font-size: 24px;
  margin-right: 10px;
}
.weather-error-state .retry-button {
  margin-left: 15px;
  color: white;
  font-size: 16px;
}
.weather-error-state .retry-button:hover {
  text-decoration: underline;
}

/* 基础样式 */
.dashboard-container {
  padding: 15px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 84px);
}

/* 天气区域样式 */
.weather-section {
  margin-bottom: 15px;
}

.weather-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  min-height: 90px;
}

.weather-content {
  padding: 0;
}

.weather-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.weather-main:hover {
  background: rgba(255, 255, 255, 0.1);
}

.weather-icon {
  font-size: 48px;
  margin-right: 20px;
  opacity: 0.9;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.weather-info {
  flex: 1;
}

.temperature {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.city {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.current-weather {
  font-size: 16px;
  opacity: 0.8;
}

.weather-actions {
  display: flex;
  gap: 10px;
}

.weather-detail {
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 15px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
}

.detail-item .label {
  opacity: 0.8;
  font-size: 14px;
}

.detail-item .value {
  font-weight: bold;
  font-size: 16px;
}

.weather-forecast {
  text-align: center;
}

.forecast-title {
  font-size: 16px;
  opacity: 0.8;
  margin-bottom: 15px;
  font-weight: bold;
}

.forecast-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 15px;
}

.forecast-item {
  background: rgba(255, 255, 255, 0.1);
  padding: 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.forecast-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.forecast-date {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.forecast-icon {
  font-size: 24px;
  margin-bottom: 8px;
  display: block;
}

.forecast-temp {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 4px;
}

.forecast-weather {
  font-size: 12px;
  opacity: 0.8;
}

/* 核心指标卡片样式 */
.metrics-section {
  margin-bottom: 15px;
}

.metric-col {
  margin-bottom: 15px;
}

.core-metric {
  height: 120px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.core-metric::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.8) 50%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.core-metric:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.core-metric:hover::before {
  opacity: 1;
}

.core-metric >>> .el-card__body {
  height: 100%;
  padding: 0;
}

.metric-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 15px;
  box-sizing: border-box;
}

.metric-icon-wrapper {
  width: 60px;
  height: 60px;
  min-width: 60px;
  min-height: 60px;
  max-width: 60px;
  max-height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.metric-icon {
  font-size: 24px;
  color: white;
  line-height: 1;
}

.metric-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
  height: 60px;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
  height: 34px;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metric-title {
  font-size: 14px;
  color: #909399;
  line-height: 1.2;
  height: 18px;
  display: flex;
  align-items: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metric-subtitle {
  font-size: 12px;
  color: #C0C4CC;
  line-height: 1;
  height: 14px;
  display: flex;
  align-items: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}


/* 主要图表区域样式 */
.main-chart-section {
  margin-bottom: 15px;
}

.chart-col {
  margin-bottom: 15px;
}

.chart-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.main-chart {
  min-height: 350px;
}

.sub-chart {
  min-height: 280px;
}

.side-chart {
  min-height: 250px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-container {
  height: 280px;
  width: 100%;
  padding: 10px;
}

.main-chart .chart-container {
  height: 300px;
}

.sub-chart .chart-container {
  height: 220px;
}

.side-chart .chart-container {
  height: 180px;
}

.chart-summary {
  margin-top: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding: 4px 0;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-label {
  font-size: 14px;
  color: #606266;
}

.summary-value {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.sub-charts-row {
  margin-top: 15px;
}

.sub-chart-col {
  margin-bottom: 15px;
}

/* 功能区域样式 */
.function-section {
  margin-bottom: 15px;
}

.function-col {
  margin-bottom: 15px;
}

.function-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.function-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.function-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
}

.function-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.function-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 待办事项样式 */
.todo-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 12px;
  margin-bottom: 6px;
  border-radius: 8px;
  border-left: 4px solid #e4e7ed;
  transition: all 0.3s ease;
  background: #fafafa;
}

.todo-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.todo-item.urgent {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  border-left-color: #F56C6C;
}

.todo-item.urgent:hover {
  background: linear-gradient(135deg, #fde2e2 0%, #fcd3d3 100%);
}

.todo-item.completed {
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border-left-color: #409EFF;
  opacity: 0.7;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  font-weight: 500;
}

.todo-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
  line-height: 1.4;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-time {
  font-size: 12px;
  color: #C0C4CC;
}

.todo-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 消息列表样式 */
.message-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 12px;
  margin-bottom: 6px;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s ease;
  border-left: 4px solid #e4e7ed;
}

.message-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.message-item.unread {
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border-left-color: #409EFF;
}

.message-item.unread:hover {
  background: linear-gradient(135deg, #e6f7ff 0%, #d9ecff 100%);
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.message-avatar i {
  color: white;
  font-size: 16px;
}

.message-content {
  flex: 1;
}

.message-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* 公告列表样式 */
.notice-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
}

.notice-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 6px;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  border: 1px solid transparent;
}

.notice-item:hover {
  background: #f0f2f5;
  border-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.notice-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #F56C6C;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 8px;
  font-weight: bold;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  font-weight: 500;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 4px;
}

.notice-author {
  font-size: 12px;
  color: #409EFF;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.notice-preview {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-arrow {
  color: #C0C4CC;
  transition: all 0.3s ease;
}

.notice-item:hover .notice-arrow {
  color: #409EFF;
  transform: translateX(4px);
}

/* 底部区域样式 */
.bottom-section {
  margin-bottom: 15px;
}

.bottom-col {
  margin-bottom: 15px;
}

/* 日志区域样式 */
.log-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
}

.log-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.log-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
}

/* 筛选表单样式 */
.filter-form {
  margin-bottom: 15px;
  padding: 0 15px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
}

.filter-form .el-form-item {
  margin-bottom: 12px;
  margin-right: 12px;
}

/* 表格容器样式 */
.table-container {
  padding: 0 15px 15px;
}

/* 去除表格单元格之间的空隙，让表项平铺占满 */
.table-container >>> .el-table {
  border-spacing: 0 !important;
  border-collapse: collapse !important;
  table-layout: auto !important;
}

.table-container >>> .el-table td,
.table-container >>> .el-table th {
  padding: 8px 5px !important;
  border-bottom: 1px solid #ebeef5;
}

.table-container >>> .el-table--border td,
.table-container >>> .el-table--border th {
  border-right: 1px solid #ebeef5;
}

.table-container >>> .el-table th.is-leaf {
  border-bottom: 1px solid #ebeef5;
}

.table-container >>> .el-table__body tr:hover > td {
  background-color: #f5f7fa;
}

.table-container >>> .el-table__body-wrapper {
  overflow-x: hidden !important;
}

.table-container >>> .el-table colgroup col[name="gutter"] {
  display: none;
  width: 0 !important;
}

.submission-text {
  font-size: 12px;
  color: #909399;
  text-align: center;
  margin-top: 4px;
}

/* 确保详情按钮不会出现省略号 */
.table-container .el-button--mini {
  padding: 5px 0;
  font-size: 12px;
  white-space: nowrap;
}

.table-container .el-button--text {
  padding: 5px 0;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  color: #909399;
  padding: 40px 15px;
  font-size: 14px;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 对话框内容样式 */
.notice-detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.notice-content {
  line-height: 1.8;
  font-size: 14px;
  color: #606266;
}

.notice-content >>> img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 10px 0;
}

.log-detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.weather-management {
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .dashboard-container {
    padding: 12px;
  }

  .metric-value {
    font-size: 24px;
    height: 30px;
  }

  .metric-icon-wrapper {
    width: 50px;
    height: 50px;
    min-width: 50px;
    min-height: 50px;
    max-width: 50px;
    max-height: 50px;
    margin-right: 12px;
  }

  .metric-icon {
    font-size: 20px;
  }

  .metric-info {
    height: 50px;
  }

  .metric-title {
    height: 16px;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 10px;
  }

  .weather-content {
    flex-direction: column;
    text-align: center;
  }

  .weather-main {
    flex-direction: column;
    text-align: center;
    padding: 12px;
  }

  .weather-icon {
    margin-right: 0;
    margin-bottom: 10px;
    font-size: 36px;
  }

  .weather-actions {
    margin-top: 10px;
    justify-content: center;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .forecast-list {
    grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  }

  .metric-content {
    flex-direction: column;
    text-align: center;
    padding: 12px;
    justify-content: center;
    align-items: center;
  }

  .metric-icon-wrapper {
    margin-right: 0;
    margin-bottom: 10px;
    width: 50px;
    height: 50px;
    min-width: 50px;
    min-height: 50px;
    max-width: 50px;
    max-height: 50px;
  }

  .metric-info {
    height: auto;
    align-items: center;
  }

  .metric-value {
    height: auto;
    justify-content: center;
  }

  .metric-title {
    height: auto;
    justify-content: center;
  }

  .metric-subtitle {
    height: auto;
    justify-content: center;
  }

  .chart-container {
    height: 250px;
  }

  .main-chart .chart-container {
    height: 280px;
  }

  .function-actions {
    flex-direction: column;
    gap: 5px;
  }

  .filter-form .el-form-item {
    margin-right: 10px;
    margin-bottom: 10px;
    width: 100%;
  }

  .filter-form .el-form-item >>> .el-input,
  .filter-form .el-form-item >>> .el-select {
    width: 100% !important;
  }
}

@media (max-width: 480px) {
  .temperature {
    font-size: 28px;
  }

  .city {
    font-size: 16px;
  }

  .metric-value {
    font-size: 20px;
    height: auto;
  }

  .metric-icon-wrapper {
    width: 45px;
    height: 45px;
    min-width: 45px;
    min-height: 45px;
    max-width: 45px;
    max-height: 45px;
  }

  .metric-icon {
    font-size: 18px;
  }

  .metric-title {
    font-size: 12px;
  }

  .chart-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .chart-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .filter-form {
    padding: 12px;
  }

  .filter-form .el-form-item {
    width: 100%;
    margin-right: 0;
  }
}

/* 滚动条样式 */
.todo-list::-webkit-scrollbar,
.message-list::-webkit-scrollbar,
.notice-list::-webkit-scrollbar,
.log-container::-webkit-scrollbar {
  width: 6px;
}

.todo-list::-webkit-scrollbar-track,
.message-list::-webkit-scrollbar-track,
.notice-list::-webkit-scrollbar-track,
.log-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.todo-list::-webkit-scrollbar-thumb,
.message-list::-webkit-scrollbar-thumb,
.notice-list::-webkit-scrollbar-thumb,
.log-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.todo-list::-webkit-scrollbar-thumb:hover,
.message-list::-webkit-scrollbar-thumb:hover,
.notice-list::-webkit-scrollbar-thumb:hover,
.log-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
