<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409EFF;">
            <i class="el-icon-monitor"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日监控次数</div>
            <div class="stat-value">{{ todayCount }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #F56C6C;">
            <i class="el-icon-warning"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">未处理告警</div>
            <div class="stat-value">{{ statistics.unhandledAlertCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67C23A;">
            <i class="el-icon-success"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">系统正常率</div>
            <div class="stat-value">{{ normalRate }}%</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #E6A23C;">
            <i class="el-icon-pie-chart"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">本周监控总数</div>
            <div class="stat-value">{{ weekCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>监控类型分布</span>
            <el-radio-group v-model="statDays" size="small" style="float: right;" @change="loadStatistics">
              <el-radio-button :label="7">近7天</el-radio-button>
              <el-radio-button :label="30">近30天</el-radio-button>
            </el-radio-group>
          </div>
          <div id="typeChart" style="height: 350px;"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>告警级别分布</span>
          </div>
          <div id="alertChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <div slot="header">
            <span>监控趋势（近24小时）</span>
          </div>
          <div id="trendChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <div slot="header">快捷操作</div>
          <el-button type="primary" icon="el-icon-monitor" @click="gotoServerMonitor">服务器监控</el-button>
          <el-button type="success" icon="el-icon-data-line" @click="gotoDatabaseMonitor">数据库监控</el-button>
          <el-button type="warning" icon="el-icon-document" @click="gotoMonitorRecord">监控记录</el-button>
          <el-button type="danger" icon="el-icon-refresh" @click="handleCollect">立即采集</el-button>
          <el-button type="info" icon="el-icon-download" @click="showExportDialog">导出图表</el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- 导出图表对话框 -->
    <el-dialog title="导出图表" :visible.sync="exportDialogVisible" width="500px">
      <el-checkbox-group v-model="selectedCharts">
        <el-checkbox label="typeChart">监控类型分布</el-checkbox><br/>
        <el-checkbox label="alertChart">告警级别分布</el-checkbox><br/>
        <el-checkbox label="trendChart">监控趋势</el-checkbox>
      </el-checkbox-group>
      <div slot="footer" class="dialog-footer">
        <el-button @click="exportDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="exportCharts">确定导出</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStatistics, collectMetrics } from '@/api/proj_fz/systemMonitor'
import * as echarts from 'echarts'

export default {
  name: 'MonitorStatistics',
  data() {
    return {
      statistics: {},
      statDays: 7,
      todayCount: 0,
      weekCount: 0,
      normalRate: 0,
      typeChart: null,
      alertChart: null,
      trendChart: null,
      timer: null,
      exportDialogVisible: false,
      selectedCharts: []
    }
  },
  mounted() {
    this.loadStatistics()
    // 每分钟自动刷新
    this.timer = setInterval(() => {
      this.loadStatistics()
    }, 60000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
    if (this.typeChart) {
      this.typeChart.dispose()
    }
    if (this.alertChart) {
      this.alertChart.dispose()
    }
    if (this.trendChart) {
      this.trendChart.dispose()
    }
  },
  methods: {
    loadStatistics() {
      getStatistics(this.statDays).then(response => {
        this.statistics = response.data
        this.calculateStats()
        this.drawTypeChart()
        this.drawAlertChart()
        this.drawTrendChart()
      })
    },
    calculateStats() {
      // 计算今日监控次数
      if (this.statistics.trend && this.statistics.trend.length > 0) {
        const today = this.parseTime(new Date(), '{y}-{m}-{d}')
        const todayData = this.statistics.trend.filter(item => {
          if (item.time) {
            // 兼容不同格式的时间
            const itemDate = item.time.substring(0, 10) // 取 YYYY-MM-DD 部分
            return itemDate === today
          }
          return false
        })
        this.todayCount = todayData.reduce((sum, item) => sum + (item.total || 0), 0)
      } else {
        this.todayCount = 0
      }

      // 计算本周监控总数
      if (this.statistics.trend) {
        this.weekCount = this.statistics.trend.reduce((sum, item) => sum + (item.total || 0), 0)
      } else {
        this.weekCount = 0
      }

      // 计算正常率
      if (this.statistics.trend && this.statistics.trend.length > 0) {
        const totalCount = this.statistics.trend.reduce((sum, item) => sum + (item.total || 0), 0)
        const alertCount = this.statistics.trend.reduce((sum, item) => sum + (item.alertCount || 0), 0)
        this.normalRate = totalCount > 0 ? ((totalCount - alertCount) / totalCount * 100).toFixed(2) : 100
      } else {
        this.normalRate = 100
      }
    },
    drawTypeChart() {
      const chartDom = document.getElementById('typeChart')
      if (!this.typeChart) {
        this.typeChart = echarts.init(chartDom)
      }

      const typeNames = {
        '1': '服务器监控',
        '2': '数据库监控',
        '3': '用户行为监控',
        '4': '功能模块监控',
        '5': '接口性能监控',
        '6': '异常监控'
      }

      const data = (this.statistics.typeStats || []).map(item => ({
        name: typeNames[String(item.type)] || '未知类型',
        value: item.count || 0
      }))

      if (data.length === 0) {
        data.push({ name: '暂无数据', value: 0 })
      }

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '监控类型',
            type: 'pie',
            radius: ['40%', '70%'],
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
                fontSize: '18',
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

      this.typeChart.setOption(option)
    },
    drawAlertChart() {
      const chartDom = document.getElementById('alertChart')
      if (!this.alertChart) {
        this.alertChart = echarts.init(chartDom)
      }

      const alertNames = {
        '0': '正常',
        '1': '警告',
        '2': '严重'
      }

      const data = (this.statistics.alertStats || []).map(item => ({
        name: alertNames[String(item.level)] || '未知',
        value: item.count || 0
      }))

      if (data.length === 0) {
        data.push({ name: '暂无数据', value: 0 })
      }

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '告警级别',
            type: 'pie',
            radius: '70%',
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ],
        color: ['#E6A23C', '#F56C6C']
      }

      this.alertChart.setOption(option)
    },
    drawTrendChart() {
      const chartDom = document.getElementById('trendChart')
      if (!this.trendChart) {
        this.trendChart = echarts.init(chartDom)
      }

      const trendData = this.statistics.trend || []
      const times = trendData.map(item => {
        if (!item.time) return ''
        const date = new Date(item.time)
        return this.parseTime(date, '{m}-{d} {h}:00')
      })
      const totals = trendData.map(item => item.total || 0)
      const alertCounts = trendData.map(item => item.alertCount || 0)

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['监控次数', '告警次数']
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
          type: 'value'
        },
        series: [
          {
            name: '监控次数',
            type: 'line',
            stack: 'Total',
            data: totals,
            areaStyle: {},
            smooth: true,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '告警次数',
            type: 'line',
            stack: 'Total',
            data: alertCounts,
            areaStyle: {},
            smooth: true,
            itemStyle: { color: '#F56C6C' }
          }
        ]
      }

      this.trendChart.setOption(option)
    },
    gotoServerMonitor() {
      this.$router.push('/proj_fz/monitor/server')
    },
    gotoDatabaseMonitor() {
      this.$router.push('/proj_fz/monitor/database')
    },
    gotoMonitorRecord() {
      this.$router.push('/proj_fz/monitor/record')
    },
    handleCollect() {
      this.$modal.confirm('是否立即执行监控数据采集？').then(() => {
        return collectMetrics()
      }).then(() => {
        this.$modal.msgSuccess('采集成功')
        this.loadStatistics()
      }).catch(() => {})
    },
    showExportDialog() {
      this.selectedCharts = ['typeChart', 'alertChart', 'trendChart'] // 默认全选
      this.exportDialogVisible = true
    },
    exportCharts() {
      if (this.selectedCharts.length === 0) {
        this.$modal.msgWarning('请至少选择一个图表')
        return
      }

      const chartMap = {
        typeChart: { instance: this.typeChart, name: '监控类型分布' },
        alertChart: { instance: this.alertChart, name: '告警级别分布' },
        trendChart: { instance: this.trendChart, name: '监控趋势' }
      }

      // 使用canvas合并多个图表
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const padding = 20
      const chartWidth = 800
      const chartHeight = 500
      const totalHeight = this.selectedCharts.length * (chartHeight + padding) + padding

      canvas.width = chartWidth + 2 * padding
      canvas.height = totalHeight

      // 白色背景
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvas.width, canvas.height)

      let yOffset = padding
      let loadedCount = 0

      this.selectedCharts.forEach((chartKey, index) => {
        const chart = chartMap[chartKey]
        if (chart && chart.instance) {
          const url = chart.instance.getDataURL({
            type: 'png',
            pixelRatio: 2,
            backgroundColor: '#fff'
          })

          const img = new Image()
          img.onload = () => {
            // 绘制图表标题
            ctx.fillStyle = '#000000'
            ctx.font = 'bold 20px Arial'
            ctx.fillText(chart.name, padding, yOffset + 25)

            // 绘制图表
            ctx.drawImage(img, padding, yOffset + 40, chartWidth, chartHeight - 60)
            yOffset += chartHeight + padding

            loadedCount++
            if (loadedCount === this.selectedCharts.length) {
              // 所有图表加载完成，导出
              const link = document.createElement('a')
              link.download = `监控统计图表_${this.parseTime(new Date(), '{y}{m}{d}{h}{i}{s}')}.png`
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
.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card >>> .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.stat-icon i {
  font-size: 28px;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
</style>

