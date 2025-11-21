<template>
  <div class="vote-stats">
    <div v-if="!voteData" class="loading">加载中...</div>
    <div v-else>
      <div class="stats-header">
        <h3>{{ voteData.vote.title }}</h3>
        <div class="meta">
          <el-tag size="small" :type="voteData.vote.status==='1'?'success':(voteData.vote.status==='2'?'info':'warning')">
            {{ voteData.vote.status==='1'?'进行中':(voteData.vote.status==='2'?'已结束':'未开始') }}
          </el-tag>
          <span class="total">总票数: {{ voteData.total }}</span>
        </div>
      </div>

      <div class="options-list" v-if="!showChart">
        <div v-for="opt in voteData.vote.options" :key="opt.optionId" class="stat-row">
          <div class="row-header">
            <span class="opt-label">{{ opt.label }}. {{ opt.content }}</span>
            <span class="opt-count">{{ opt.count || 0 }} 票</span>
          </div>
          <el-progress 
            :percentage="calculatePercent(opt.count)" 
            :color="customColors"
            :stroke-width="18"
            :text-inside="true"
          ></el-progress>
          
          <!-- 显示投票人姓名 -->
          <div v-if="opt.voterNames && opt.voterNames.length > 0" class="voter-names">
            <span v-for="(name, idx) in opt.voterNames" :key="idx" class="voter-tag">{{ name }}</span>
          </div>
        </div>
      </div>

      <div v-show="showChart" class="chart-container" ref="chart" style="height: 300px; width: 100%;"></div>
      
      <div class="footer-actions">
        <el-button icon="el-icon-refresh" size="small" @click="loadStats">刷新</el-button>
        <el-button icon="el-icon-pie-chart" size="small" type="primary" @click="toggleChart">统计图表</el-button>
        <el-button icon="el-icon-download" size="small" @click="handleExport">导出</el-button>
        <el-button icon="el-icon-printer" size="small" @click="handlePrint">打印</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getVoteStats } from '@/api/proj_myx/vote'
import * as echarts from 'echarts'

export default {
  name: 'VoteStats',
  props: { voteId: { type: Number, required: true } },
  data() {
    return {
      voteData: null,
      showChart: false,
      chartInstance: null,
      customColors: [
        {color: '#f56c6c', percentage: 20},
        {color: '#e6a23c', percentage: 40},
        {color: '#5cb87a', percentage: 60},
        {color: '#1989fa', percentage: 80},
        {color: '#6f7ad3', percentage: 100}
      ]
    }
  },
  watch: {
    voteId: {
      immediate: true,
      handler(val) {
        if (val) this.loadStats();
      }
    }
  },
  methods: {
    async loadStats() {
      try {
        const res = await getVoteStats(this.voteId);
        if (res.code === 200) {
          this.voteData = res.data;
          if (this.showChart) {
            this.$nextTick(() => this.initChart());
          }
        }
      } catch (e) {
        this.$message.error('加载统计失败');
      }
    },
    calculatePercent(count) {
      if (!this.voteData || !this.voteData.total) return 0;
      return Math.round(((count || 0) / this.voteData.total) * 100);
    },
    toggleChart() {
      this.showChart = !this.showChart;
      if (this.showChart) {
        this.$nextTick(() => {
          this.initChart();
        });
      }
    },
    initChart() {
      if (!this.voteData || !this.$refs.chart) return;
      
      if (this.chartInstance) {
        this.chartInstance.dispose();
      }
      
      this.chartInstance = echarts.init(this.$refs.chart);
      
      const data = this.voteData.vote.options.map(opt => ({
        name: opt.label + '. ' + opt.content,
        value: opt.count || 0
      }));
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          bottom: '0%',
          left: 'center'
        },
        series: [
          {
            name: '投票统计',
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
      };
      
      this.chartInstance.setOption(option);
    },
    handleExport() {
      if (!this.voteData) return;
      
      const tHeader = ['选项', '内容', '票数', '占比', '投票人'];
      const list = this.voteData.vote.options.map(opt => {
        const percent = this.calculatePercent(opt.count) + '%';
        const voters = opt.voterNames ? opt.voterNames.join('; ') : '';
        return [
          opt.label,
          opt.content,
          opt.count || 0,
          percent,
          voters
        ];
      });
      
      list.unshift(tHeader);
      
      const csvContent = list.map(row => 
        row.map(item => {
          let str = String(item === null || item === undefined ? '' : item);
          if (str.includes(',') || str.includes('"') || str.includes('\n')) {
            str = '"' + str.replace(/"/g, '""') + '"';
          }
          return str;
        }).join(',')
      ).join('\n');
      
      const blob = new Blob(["\ufeff" + csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement("a");
      const url = URL.createObjectURL(blob);
      link.setAttribute("href", url);
      link.setAttribute("download", `投票统计_${this.voteData.vote.title}_${new Date().getTime()}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
    handlePrint() {
      // Simple print implementation: print the current window
      // Ideally, we would print just the dialog content, but window.print() is standard.
      // We can use CSS @media print to hide other elements.
      window.print();
    }
  }
}
</script>

<style scoped>
.vote-stats {
  padding: 10px;
}

.stats-header {
  margin-bottom: 32px;
  text-align: center;
}

.stats-header h3 {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 12px 0;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #86868b;
  font-size: 13px;
}

.total {
  background: #f5f5f7;
  padding: 4px 12px;
  border-radius: 980px;
  font-weight: 500;
  color: #1d1d1f;
}

.options-list {
  background: #ffffff;
  border-radius: 12px;
  padding: 8px;
}

.stat-row {
  margin-bottom: 24px;
  padding: 0 8px;
}

.row-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
}

.opt-count {
  color: #0071e3;
  font-weight: 600;
}

/* Progress Bar Customization */
.vote-stats >>> .el-progress-bar__outer {
  background-color: #f5f5f7 !important;
  border-radius: 6px !important;
}

.vote-stats >>> .el-progress-bar__inner {
  border-radius: 6px !important;
  transition: width 1s ease;
}

.footer-actions {
  text-align: center;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #f5f5f7;
}

.footer-actions >>> .el-button {
  border-radius: 980px;
  padding: 8px 20px;
}

.voter-names {
  margin-top: 12px;
  padding: 12px;
  background: #fbfbfd;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.6;
}

.voter-tag {
  display: inline-block;
  background-color: #ffffff;
  border: 1px solid #e5e5ea;
  border-radius: 4px;
  padding: 2px 8px;
  margin-right: 6px;
  margin-bottom: 6px;
  color: #1d1d1f;
  font-size: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}
</style>
