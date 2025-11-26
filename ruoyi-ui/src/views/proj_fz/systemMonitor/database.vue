<template>
  <div class="app-container">
    <!-- 可折叠的指标说明 -->
    <el-collapse v-model="activeCollapse" style="margin-bottom: 20px;">
      <el-collapse-item name="1">
        <template slot="title">
          <i class="el-icon-info"></i>
          <span style="margin-left: 10px; font-weight: bold;">数据库监控指标说明</span>
        </template>
        <div style="line-height: 1.8; padding: 10px;">
          <p><strong>连接数：</strong>当前数据库的总连接数（包括活跃和空闲连接）- 通过 SHOW STATUS 查询</p>
          <p><strong>活跃连接：</strong>正在执行查询的连接数 - 通过 SHOW STATUS 查询</p>
          <p><strong>空闲连接：</strong>总连接数减去活跃连接数</p>
          <p><strong>数据库大小：</strong>当前数据库所有表的数据和索引大小总和，单位MB - 查询 information_schema.TABLES</p>
          <p><strong>累计查询数：</strong>数据库启动以来执行的查询总数 - 通过 SHOW STATUS 查询</p>
          <p style="color: #909399; font-size: 12px; margin-top: 10px;"><em>注意：所有显示的数据均为真实数据，通过MySQL系统表查询获取</em></p>
        </div>
      </el-collapse-item>
    </el-collapse>

    <el-row :gutter="20">
      <!-- 实时指标卡片 - 只显示真实数据 -->
      <el-col :span="8">
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

      <el-col :span="8">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-folder"></i>
            <span>数据库大小</span>
          </div>
          <div class="metric-value normal">
            {{ (dbMetrics.databaseSize || 0).toFixed(2) }}MB
          </div>
          <div class="metric-detail">&nbsp;</div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-s-data"></i>
            <span>累计查询数</span>
          </div>
          <div class="metric-value normal">
            {{ (dbMetrics.qps || 0).toLocaleString() }}
          </div>
          <div class="metric-detail">数据库启动以来</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 更新时间 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="metric-card" style="min-height: auto;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="color: #909399;">
              <i class="el-icon-time"></i> 最后更新: {{ updateTime }}
            </span>
            <el-button type="primary" size="small" icon="el-icon-refresh" @click="refreshMetrics">刷新数据</el-button>
          </div>
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

    <!-- 导出按钮 -->
    <el-row style="margin-top: 20px;">
      <el-col :span="24" style="text-align: center;">
        <el-button type="primary" icon="el-icon-download" @click="showExportDialog">导出图表</el-button>
      </el-col>
    </el-row>

    <!-- 导出图表对话框 -->
    <el-dialog title="导出图表" :visible.sync="exportDialogVisible" width="500px">
      <el-checkbox-group v-model="selectedCharts">
        <el-checkbox label="dbTrendChart">数据库性能趋势</el-checkbox><br/>
        <el-checkbox label="connectionChart">连接数趋势</el-checkbox><br/>
        <el-checkbox label="slowQueryChart">慢查询统计</el-checkbox>
      </el-checkbox-group>
      <div slot="footer" class="dialog-footer">
        <el-button @click="exportDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="exportCharts">确定导出</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getDatabaseMetrics, getDatabaseHistory } from '@/api/proj_fz/systemMonitor'
import * as echarts from 'echarts'

export default {
  name: 'DatabaseMonitor',
  data() {
    return {
      activeCollapse: [], // 默认折叠
      dbMetrics: {},
      updateTime: '',
      historyHours: 24,
      dbTrendChart: null,
      connectionChart: null,
      slowQueryChart: null,
      timer: null,
      exportDialogVisible: false,
      selectedCharts: []
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
    },
    showExportDialog() {
      this.selectedCharts = ['dbTrendChart', 'connectionChart', 'slowQueryChart'] // 默认全选
      this.exportDialogVisible = true
    },
    exportCharts() {
      if (this.selectedCharts.length === 0) {
        this.$modal.msgWarning('请至少选择一个图表')
        return
      }

      const chartMap = {
        dbTrendChart: { instance: this.dbTrendChart, name: '数据库性能趋势' },
        connectionChart: { instance: this.connectionChart, name: '连接数趋势' },
        slowQueryChart: { instance: this.slowQueryChart, name: '慢查询统计' }
      }

      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const padding = 20
      const chartWidth = 800
      const chartHeight = 500
      const totalHeight = this.selectedCharts.length * (chartHeight + padding) + padding

      canvas.width = chartWidth + 2 * padding
      canvas.height = totalHeight

      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvas.width, canvas.height)

      let yOffset = padding
      let loadedCount = 0

      this.selectedCharts.forEach((chartKey) => {
        const chart = chartMap[chartKey]
        if (chart && chart.instance) {
          const url = chart.instance.getDataURL({
            type: 'png',
            pixelRatio: 2,
            backgroundColor: '#fff'
          })

          const img = new Image()
          img.onload = () => {
            ctx.fillStyle = '#000000'
            ctx.font = 'bold 20px Arial'
            ctx.fillText(chart.name, padding, yOffset + 25)

            ctx.drawImage(img, padding, yOffset + 40, chartWidth, chartHeight - 60)
            yOffset += chartHeight + padding

            loadedCount++
            if (loadedCount === this.selectedCharts.length) {
              const link = document.createElement('a')
              link.download = `数据库监控图表_${this.parseTime(new Date(), '{y}{m}{d}{h}{i}{s}')}.png`
              link.href = canvas.toDataURL('image/png')
              link.click()
              this.$modal.msgSuccess('导出成功')
              this.exportDialogVisible = false
            }
          }
          img.src = url
        }
      })
    }
  }
}
</script>

<style scoped>
.metric-card {
  text-align: center;
  margin-bottom: 20px;
  min-height: 150px;
}

.metric-card >>> .el-card__body {
  min-height: 110px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-header {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  height: 24px;
  line-height: 24px;
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
  height: 40px;
  line-height: 40px;
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
  height: 20px;
  line-height: 20px;
}
</style>

