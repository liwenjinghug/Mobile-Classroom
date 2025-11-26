<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 实时指标卡片 -->
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-connection"></i>
            <span>数据库连接</span>
          </div>
          <div class="metric-value normal">
            {{ dbMetrics.connectionCount || 0 }}
          </div>
          <div class="metric-detail">
            活跃: {{ dbMetrics.activeConnections || 0 }} / 空闲: {{ dbMetrics.idleConnections || 0 }}
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-time"></i>
            <span>查询响应时间</span>
          </div>
          <div class="metric-value" :class="getResponseTimeClass(dbMetrics.queryResponseTime)">
            {{ dbMetrics.queryResponseTime || 0 }}ms
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-warning"></i>
            <span>慢查询数</span>
          </div>
          <div class="metric-value" :class="getSlowQueryClass(dbMetrics.slowQueryCount)">
            {{ dbMetrics.slowQueryCount || 0 }}
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-folder"></i>
            <span>数据库大小</span>
          </div>
          <div class="metric-value normal">
            {{ (dbMetrics.databaseSize || 0).toFixed(2) }}MB
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 性能指标 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-s-data"></i>
            <span>QPS</span>
          </div>
          <div class="metric-value normal">
            {{ dbMetrics.qps || 0 }}
          </div>
          <div class="metric-detail">每秒查询数</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-s-operation"></i>
            <span>TPS</span>
          </div>
          <div class="metric-value normal">
            {{ dbMetrics.tps || 0 }}
          </div>
          <div class="metric-detail">每秒事务数</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-pie-chart"></i>
            <span>缓存命中率</span>
          </div>
          <div class="metric-value normal">
            {{ (dbMetrics.cacheHitRate || 0).toFixed(2) }}%
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-refresh"></i>
            <span>更新时间</span>
          </div>
          <div class="metric-detail" style="margin-top: 20px;">
            {{ updateTime }}
          </div>
          <el-button type="primary" size="small" @click="refreshMetrics" style="margin-top: 10px;">
            刷新数据
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史趋势图表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <div slot="header">
            <span>数据库性能趋势</span>
            <el-radio-group v-model="historyHours" size="small" style="float: right;" @change="loadHistory">
              <el-radio-button :label="6">6小时</el-radio-button>
              <el-radio-button :label="12">12小时</el-radio-button>
              <el-radio-button :label="24">24小时</el-radio-button>
            </el-radio-group>
          </div>
          <div id="dbTrendChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header">连接数趋势</div>
          <div id="connectionChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div slot="header">慢查询统计</div>
          <div id="slowQueryChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDatabaseMetrics, getDatabaseHistory } from '@/api/proj_fz/systemMonitor'
import * as echarts from 'echarts'

export default {
  name: 'DatabaseMonitor',
  data() {
    return {
      dbMetrics: {},
      updateTime: '',
      historyHours: 24,
      dbTrendChart: null,
      connectionChart: null,
      slowQueryChart: null,
      timer: null
    }
  },
  mounted() {
    this.refreshMetrics()
    this.loadHistory()
    // 每30秒自动刷新
    this.timer = setInterval(() => {
      this.refreshMetrics()
    }, 30000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
    if (this.dbTrendChart) {
      this.dbTrendChart.dispose()
    }
    if (this.connectionChart) {
      this.connectionChart.dispose()
    }
    if (this.slowQueryChart) {
      this.slowQueryChart.dispose()
    }
  },
  methods: {
    refreshMetrics() {
      getDatabaseMetrics().then(response => {
        this.dbMetrics = response.data
        this.updateTime = this.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')
      })
    },
    loadHistory() {
      getDatabaseHistory(this.historyHours).then(response => {
        this.drawTrendChart(response.data)
        this.drawConnectionChart(response.data)
        this.drawSlowQueryChart(response.data)
      })
    },
    getResponseTimeClass(time) {
      if (!time) return 'normal'
      if (time >= 3000) return 'danger'
      if (time >= 1000) return 'warning'
      return 'normal'
    },
    getSlowQueryClass(count) {
      if (!count) return 'normal'
      if (count >= 10) return 'danger'
      if (count >= 5) return 'warning'
      return 'normal'
    },
    drawTrendChart(historyData) {
      const chartDom = document.getElementById('dbTrendChart')
      if (!this.dbTrendChart) {
        this.dbTrendChart = echarts.init(chartDom)
      }

      const times = historyData.map(item => this.parseTime(item.monitorTime, '{m}-{d} {h}:{i}'))
      const connData = historyData.map(item => item.connectionCount || 0)
      const responseData = historyData.map(item => item.queryResponseTime || 0)

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['连接数', '响应时间(ms)']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: times
        },
        yAxis: [
          {
            type: 'value',
            name: '连接数',
            position: 'left'
          },
          {
            type: 'value',
            name: '响应时间(ms)',
            position: 'right'
          }
        ],
        series: [
          {
            name: '连接数',
            type: 'line',
            data: connData,
            smooth: true,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '响应时间(ms)',
            type: 'line',
            yAxisIndex: 1,
            data: responseData,
            smooth: true,
            itemStyle: { color: '#F56C6C' }
          }
        ]
      }

      this.dbTrendChart.setOption(option)
    },
    drawConnectionChart(historyData) {
      const chartDom = document.getElementById('connectionChart')
      if (!this.connectionChart) {
        this.connectionChart = echarts.init(chartDom)
      }

      const times = historyData.map(item => this.parseTime(item.monitorTime, '{m}-{d} {h}:{i}'))
      const connData = historyData.map(item => item.connectionCount || 0)

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: times
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '连接数',
            type: 'bar',
            data: connData,
            itemStyle: { color: '#67C23A' }
          }
        ]
      }

      this.connectionChart.setOption(option)
    },
    drawSlowQueryChart(historyData) {
      const chartDom = document.getElementById('slowQueryChart')
      if (!this.slowQueryChart) {
        this.slowQueryChart = echarts.init(chartDom)
      }

      const times = historyData.map(item => this.parseTime(item.monitorTime, '{m}-{d} {h}:{i}'))
      const slowData = historyData.map(item => item.slowQueryCount || 0)

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: times
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '慢查询数',
            type: 'bar',
            data: slowData,
            itemStyle: { color: '#E6A23C' }
          }
        ]
      }

      this.slowQueryChart.setOption(option)
    }
  }
}
</script>

<style scoped>
.metric-card {
  text-align: center;
  margin-bottom: 20px;
}

.metric-header {
  font-size: 14px;
  color: #606266;
  margin-bottom: 15px;
}

.metric-header i {
  font-size: 20px;
  margin-right: 5px;
  vertical-align: middle;
}

.metric-value {
  font-size: 32px;
  font-weight: bold;
  margin: 10px 0;
}

.metric-value.normal {
  color: #67C23A;
}

.metric-value.warning {
  color: #E6A23C;
}

.metric-value.danger {
  color: #F56C6C;
}

.metric-detail {
  font-size: 12px;
  color: #909399;
}
</style>

