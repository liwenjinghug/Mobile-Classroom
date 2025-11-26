<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 实时指标卡片 -->
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-cpu"></i>
            <span>CPU使用率</span>
          </div>
          <div class="metric-value" :class="getStatusClass(serverMetrics.cpuUsage, 80, 90)">
            {{ serverMetrics.cpuUsage || 0 }}%
          </div>
          <div class="metric-detail">&nbsp;</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-s-data"></i>
            <span>内存使用率</span>
          </div>
          <div class="metric-value" :class="getStatusClass(serverMetrics.memoryUsage, 80, 90)">
            {{ serverMetrics.memoryUsage || 0 }}%
          </div>
          <div class="metric-detail">
            {{ serverMetrics.usedMemory || 0 }}GB / {{ serverMetrics.totalMemory || 0 }}GB
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-folder-opened"></i>
            <span>磁盘使用率</span>
          </div>
          <div class="metric-value" :class="getStatusClass(serverMetrics.diskUsage, 85, 95)">
            {{ serverMetrics.diskUsage || 0 }}%
          </div>
          <div class="metric-detail">
            {{ serverMetrics.usedDisk || 0 }}GB / {{ serverMetrics.totalDisk || 0 }}GB
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-connection"></i>
            <span>网络速率</span>
          </div>
          <div class="metric-value normal" style="font-size: 24px;">
            ↑{{ (serverMetrics.uploadSpeed || 0).toFixed(2) }}KB/s
          </div>
          <div class="metric-detail">
            ↓{{ (serverMetrics.downloadSpeed || 0).toFixed(2) }}KB/s
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- JVM信息 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-odometer"></i>
            <span>JVM堆内存</span>
          </div>
          <div class="metric-value" :class="getStatusClass(serverMetrics.jvmHeapUsage, 80, 90)">
            {{ serverMetrics.jvmHeapUsage || 0 }}%
          </div>
          <div class="metric-detail">
            {{ serverMetrics.jvmHeapUsed || 0 }}MB / {{ serverMetrics.jvmHeapTotal || 0 }}MB
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-share"></i>
            <span>线程数</span>
          </div>
          <div class="metric-value normal">
            {{ serverMetrics.threadCount || 0 }}
          </div>
          <div class="metric-detail">&nbsp;</div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="metric-card">
          <div class="metric-header">
            <i class="el-icon-time"></i>
            <span>更新时间</span>
          </div>
          <div class="metric-value" style="font-size: 14px;">
            {{ updateTime }}
          </div>
          <div class="metric-detail">
            <el-button type="primary" size="mini" @click="refreshMetrics">刷新数据</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史趋势图表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <div slot="header">
            <span>资源使用趋势</span>
            <el-radio-group v-model="historyHours" size="small" style="float: right;" @change="loadHistory">
              <el-radio-button :label="6">6小时</el-radio-button>
              <el-radio-button :label="12">12小时</el-radio-button>
              <el-radio-button :label="24">24小时</el-radio-button>
            </el-radio-group>
          </div>
          <div id="serverTrendChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header">CPU & 内存使用率</div>
          <div id="cpuMemoryChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div slot="header">磁盘使用情况</div>
          <div id="diskChart" style="height: 300px;"></div>
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
        <el-checkbox label="serverTrendChart">资源使用趋势</el-checkbox><br/>
        <el-checkbox label="cpuMemoryChart">CPU & 内存使用率</el-checkbox><br/>
        <el-checkbox label="diskChart">磁盘使用情况</el-checkbox>
      </el-checkbox-group>
      <div slot="footer" class="dialog-footer">
        <el-button @click="exportDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="exportCharts">确定导出</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getServerMetrics, getServerHistory } from '@/api/proj_fz/systemMonitor'
import * as echarts from 'echarts'

export default {
  name: 'ServerMonitor',
  data() {
    return {
      serverMetrics: {},
      updateTime: '',
      historyHours: 24,
      serverTrendChart: null,
      cpuMemoryChart: null,
      diskChart: null,
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
    if (this.serverTrendChart) {
      this.serverTrendChart.dispose()
    }
    if (this.cpuMemoryChart) {
      this.cpuMemoryChart.dispose()
    }
    if (this.diskChart) {
      this.diskChart.dispose()
    }
  },
  methods: {
    refreshMetrics() {
      getServerMetrics().then(response => {
        this.serverMetrics = response.data
        this.updateTime = this.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')
        this.updateCharts()
      })
    },
    loadHistory() {
      getServerHistory(this.historyHours).then(response => {
        this.drawTrendChart(response.data)
      })
    },
    getStatusClass(value, warning, danger) {
      if (!value) return 'normal'
      if (value >= danger) return 'danger'
      if (value >= warning) return 'warning'
      return 'normal'
    },
    updateCharts() {
      this.drawCpuMemoryChart()
      this.drawDiskChart()
    },
    drawTrendChart(historyData) {
      const chartDom = document.getElementById('serverTrendChart')
      if (!this.serverTrendChart) {
        this.serverTrendChart = echarts.init(chartDom)
      }

      const times = historyData.map(item => this.parseTime(item.monitorTime, '{m}-{d} {h}:{i}'))
      const cpuData = historyData.map(item => (item.cpuUsage || 0).toFixed(2))
      const memData = historyData.map(item => (item.memoryUsage || 0).toFixed(2))
      const diskData = historyData.map(item => (item.diskUsage || 0).toFixed(2))

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['CPU使用率', '内存使用率', '磁盘使用率']
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
        yAxis: {
          type: 'value',
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: 'CPU使用率',
            type: 'line',
            data: cpuData,
            smooth: true,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '内存使用率',
            type: 'line',
            data: memData,
            smooth: true,
            itemStyle: { color: '#67C23A' }
          },
          {
            name: '磁盘使用率',
            type: 'line',
            data: diskData,
            smooth: true,
            itemStyle: { color: '#E6A23C' }
          }
        ]
      }

      this.serverTrendChart.setOption(option)
    },
    drawCpuMemoryChart() {
      const chartDom = document.getElementById('cpuMemoryChart')
      if (!this.cpuMemoryChart) {
        this.cpuMemoryChart = echarts.init(chartDom)
      }

      const option = {
        tooltip: {
          formatter: '{b}: {c}%'
        },
        series: [
          {
            name: 'CPU',
            type: 'gauge',
            center: ['25%', '50%'],
            radius: '80%',
            min: 0,
            max: 100,
            detail: {
              formatter: '{value}%',
              fontSize: 16
            },
            data: [{ value: (this.serverMetrics.cpuUsage || 0).toFixed(2), name: 'CPU' }],
            title: {
              offsetCenter: [0, '80%']
            },
            axisLine: {
              lineStyle: {
                width: 10,
                color: [
                  [0.8, '#67C23A'],
                  [0.9, '#E6A23C'],
                  [1, '#F56C6C']
                ]
              }
            }
          },
          {
            name: 'Memory',
            type: 'gauge',
            center: ['75%', '50%'],
            radius: '80%',
            min: 0,
            max: 100,
            detail: {
              formatter: '{value}%',
              fontSize: 16
            },
            data: [{ value: (this.serverMetrics.memoryUsage || 0).toFixed(2), name: '内存' }],
            title: {
              offsetCenter: [0, '80%']
            },
            axisLine: {
              lineStyle: {
                width: 10,
                color: [
                  [0.8, '#67C23A'],
                  [0.9, '#E6A23C'],
                  [1, '#F56C6C']
                ]
              }
            }
          }
        ]
      }

      this.cpuMemoryChart.setOption(option)
    },
    drawDiskChart() {
      const chartDom = document.getElementById('diskChart')
      if (!this.diskChart) {
        this.diskChart = echarts.init(chartDom)
      }

      const used = this.serverMetrics.usedDisk || 0
      const free = (this.serverMetrics.totalDisk || 0) - used

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}GB ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '磁盘使用',
            type: 'pie',
            radius: '70%',
            data: [
              { value: used.toFixed(2), name: '已使用', itemStyle: { color: '#409EFF' } },
              { value: free.toFixed(2), name: '剩余空间', itemStyle: { color: '#E4E7ED' } }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            label: {
              formatter: '{b}: {c}GB'
            }
          }
        ]
      }

      this.diskChart.setOption(option)
    },
    showExportDialog() {
      this.selectedCharts = ['serverTrendChart', 'cpuMemoryChart', 'diskChart'] // 默认全选
      this.exportDialogVisible = true
    },
    exportCharts() {
      if (this.selectedCharts.length === 0) {
        this.$modal.msgWarning('请至少选择一个图表')
        return
      }

      const chartMap = {
        serverTrendChart: { instance: this.serverTrendChart, name: '资源使用趋势' },
        cpuMemoryChart: { instance: this.cpuMemoryChart, name: 'CPU & 内存使用率' },
        diskChart: { instance: this.diskChart, name: '磁盘使用情况' }
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
              link.download = `服务器监控图表_${this.parseTime(new Date(), '{y}{m}{d}{h}{i}{s}')}.png`
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

