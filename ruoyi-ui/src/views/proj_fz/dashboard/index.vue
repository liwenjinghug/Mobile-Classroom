<template>
  <div class="app-container dashboard-container">
    <el-row :gutter="20" type="flex" justify="center">
      <el-col :span="8">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-icon">
            <i class="el-icon-data-analysis" style="color: #409EFF;"></i>
          </div>
          <div class="metric-title">本周全校平均签到率</div>
          <div class="metric-value">{{ dashboardData.schoolAvgRate !== undefined ? (Number(dashboardData.schoolAvgRate).toFixed(1) + '%') : '—' }}</div>
          <div ref="rateChart" style="height:220px;margin-top:12px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import Vue from 'vue'
import * as echarts from 'echarts'
import { dashboardMetrics } from "@/api/proj_fz/attendanceReport";

export default {
  name: 'Dashboard',
  data() {
    return {
      dashboardData: {
        schoolAvgRate: 0
      },
      rateChart: null
    }
  },
  mounted() {
    if (!Vue.prototype.$bus) Vue.prototype.$bus = new Vue()
    Vue.prototype.$bus.$on('attendanceFiltersChanged', this.onAttendanceFiltersChanged)
    this.initChart();
    this.loadDashboardData();
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    if (this.rateChart) this.rateChart.dispose();
    window.removeEventListener('resize', this.handleResize)
    if (Vue.prototype.$bus) Vue.prototype.$bus.$off('attendanceFiltersChanged', this.onAttendanceFiltersChanged)
  },
  methods: {
    initChart() {
      this.$nextTick(() => {
        if (this.$refs.rateChart) this.rateChart = echarts.init(this.$refs.rateChart)
      })
    },
    loadDashboardData(params) {
      dashboardMetrics(params).then(res => {
        const data = (res && res.data) ? res.data : res
        this.dashboardData = data || this.dashboardData
        this.updateChart()
      }).catch(() => {
        // 默认占位数据（若后端未就绪）
        this.dashboardData = { schoolAvgRate: 0 }
        this.updateChart()
      })
    },
    onAttendanceFiltersChanged(payload) {
      this.loadDashboardData(payload.params)
    },
    updateChart() {
      if (!this.rateChart) return
      const rate = Number(this.dashboardData.schoolAvgRate || 0)
      const option = {
        tooltip: { formatter: '{b}: {c}%' },
        series: [{
          type: 'pie',
          radius: ['60%','80%'],
          center: ['50%','50%'],
          label: { show: true, position: 'center', formatter: rate + '%' },
          data: [ { value: rate, name: '签到率', itemStyle: { color: '#409EFF' } }, { value: Math.max(0,100-rate), name: '未签到', itemStyle: { color: '#FF9900' } } ],
          silent: true
        }]
      }
      this.rateChart.clear();
      this.rateChart.setOption(option);
      try { this.rateChart.resize(); } catch(e){}
    },
    refreshData() { this.loadDashboardData(); this.$message && this.$message.success && this.$message.success('已刷新') },
    handleResize() { if (this.rateChart) this.rateChart.resize(); }
  }
}
</script>

<style scoped>
.dashboard-container { padding: 20px }
.metric-card { text-align:center; height: 360px }
.metric-title { font-size: 14px; color: #909399 }
.metric-value { font-size: 28px; font-weight: bold }
</style>
