<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-row :gutter="20" class="stats-cards">
          <el-col :xs="24" :sm="12" :lg="6">
            <el-card shadow="hover" class="stats-card">
              <div class="stats-content">
                <div class="stats-icon attendance">
                  <i class="el-icon-user-solid"></i>
                </div>
                <div class="stats-info">
                  <div class="stats-value">{{ stats.avg_attendance_rate || 0 }}%</div>
                  <div class="stats-label">平均签到率</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="6">
            <el-card shadow="hover" class="stats-card">
              <div class="stats-content">
                <div class="stats-icon homework">
                  <i class="el-icon-document"></i>
                </div>
                <div class="stats-info">
                  <div class="stats-value">{{ stats.avg_homework_rate || 0 }}%</div>
                  <div class="stats-label">平均作业提交率</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="6">
            <el-card shadow="hover" class="stats-card">
              <div class="stats-content">
                <div class="stats-icon forum">
                  <i class="el-icon-chat-dot-round"></i>
                </div>
                <div class="stats-info">
                  <div class="stats-value">{{ stats.active_students || 0 }}</div>
                  <div class="stats-label">活跃学生</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="6">
            <el-card shadow="hover" class="stats-card">
              <div class="stats-content">
                <div class="stats-icon activity">
                  <i class="el-icon-data-analysis"></i>
                </div>
                <div class="stats-info">
                  <div class="stats-value">{{ stats.total_posts || 0 }}</div>
                  <div class="stats-label">论坛发帖</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>参与度分布</span>
            </div>
          </template>
          <div ref="distributionChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>参与热力对比</span>
            </div>
          </template>
          <div ref="comparisonChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>学生参与热力详情</span>
              <el-button type="primary" icon="el-icon-refresh" @click="handleQuery">刷新</el-button>
            </div>
          </template>

          <el-table v-loading="loading" :data="participationHeatList" style="width: 100%">
            <el-table-column label="学号" align="center" prop="student_no" width="120" />
            <el-table-column label="姓名" align="center" prop="student_name" width="100" />
            <el-table-column label="签到率" align="center" prop="attendance_rate" width="100">
              <template #default="scope">
                <el-tag :type="getScoreType(scope.row.attendance_rate)">
                  {{ scope.row.attendance_rate }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="作业提交率" align="center" prop="homework_rate" width="120">
              <template #default="scope">
                <el-tag :type="getScoreType(scope.row.homework_rate)">
                  {{ scope.row.homework_rate }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="论坛活跃度" align="center" prop="forum_score" width="120">
              <template #default="scope">
                <div class="forum-score">
                  <span class="score-text">{{ scope.row.forum_score }}分</span>
                  <div class="score-bar">
                    <div
                      class="score-fill"
                      :style="{ width: Math.min(scope.row.forum_score / 2, 100) + '%' }"
                      :class="getForumScoreClass(scope.row.forum_score)"
                    ></div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="综合参与度" align="center" prop="participation_score" width="120">
              <template #default="scope">
                <div class="participation-progress">
                  <el-progress
                    :percentage="scope.row.participation_score"
                    :color="getProgressColor(scope.row.participation_score)"
                    :show-text="false">
                  </el-progress>
                  <span class="score-text">{{ scope.row.participation_score }}分</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="参与等级" align="center" prop="participation_level" width="100">
              <template #default="scope">
                <el-tag :type="getLevelType(scope.row.participation_level)">
                  {{ scope.row.participation_level }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { listParticipationHeat, getParticipationStats } from "@/api/proj_fz/participationHeat";

export default {
  name: "ParticipationHeat",
  data() {
    return {
      loading: true,
      participationHeatList: [],
      stats: {},
      distributionChart: null,
      comparisonChart: null
    };
  },
  mounted() {
    this.getList();
    this.getStats();
  },
  beforeUnmount() {
    if (this.distributionChart) {
      this.distributionChart.dispose();
    }
    if (this.comparisonChart) {
      this.comparisonChart.dispose();
    }
  },
  methods: {
    getList() {
      this.loading = true;
      listParticipationHeat().then(response => {
        this.participationHeatList = response.data;
        this.loading = false;
        this.$nextTick(() => {
          this.initCharts();
        });
      }).catch(() => {
        this.loading = false;
      });
    },
    getStats() {
      getParticipationStats().then(response => {
        this.stats = response.data;
      });
    },
    handleQuery() {
      this.getList();
      this.getStats();
    },
    initCharts() {
      this.initDistributionChart();
      this.initComparisonChart();
    },
    initDistributionChart() {
      const chartDom = this.$refs.distributionChart;
      this.distributionChart = echarts.init(chartDom);

      const distribution = this.calculateDistribution();

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'center'
        },
        series: [
          {
            name: '参与度分布',
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
            data: distribution
          }
        ],
        color: ['#67C23A', '#E6A23C', '#409EFF', '#909399', '#F56C6C']
      };

      this.distributionChart.setOption(option);
    },
    calculateDistribution() {
      const levels = {
        '极高': 0,
        '高': 0,
        '中等': 0,
        '一般': 0,
        '低': 0
      };

      this.participationHeatList.forEach(item => {
        levels[item.participation_level]++;
      });

      return [
        { value: levels['极高'], name: '90-100 (极高)' },
        { value: levels['高'], name: '80-89 (高)' },
        { value: levels['中等'], name: '70-79 (中等)' },
        { value: levels['一般'], name: '60-69 (一般)' },
        { value: levels['低'], name: '0-59 (低)' }
      ];
    },
    initComparisonChart() {
      const chartDom = this.$refs.comparisonChart;
      this.comparisonChart = echarts.init(chartDom);

      const studentNames = this.participationHeatList.slice(0, 10).map(item => item.student_name);
      const attendanceRates = this.participationHeatList.slice(0, 10).map(item => item.attendance_rate);
      const homeworkRates = this.participationHeatList.slice(0, 10).map(item => item.homework_rate);
      const participationScores = this.participationHeatList.slice(0, 10).map(item => item.participation_score);

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['签到率', '作业提交率', '综合参与度']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: studentNames
        },
        yAxis: {
          type: 'value',
          name: '百分比/分数',
          max: 100,
          axisLabel: {
            formatter: '{value}'
          }
        },
        series: [
          {
            name: '签到率',
            type: 'bar',
            barWidth: '20%',
            data: attendanceRates,
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '作业提交率',
            type: 'bar',
            barWidth: '20%',
            data: homeworkRates,
            itemStyle: {
              color: '#E6A23C'
            }
          },
          {
            name: '综合参与度',
            type: 'line',
            data: participationScores,
            itemStyle: {
              color: '#67C23A'
            },
            lineStyle: {
              width: 3
            },
            symbol: 'circle',
            symbolSize: 8
          }
        ]
      };

      this.comparisonChart.setOption(option);
    },
    getScoreType(score) {
      if (score >= 90) return 'success';
      if (score >= 80) return 'primary';
      if (score >= 70) return 'warning';
      if (score >= 60) return 'info';
      return 'danger';
    },
    getLevelType(level) {
      const levelMap = {
        '极高': 'success',
        '高': 'primary',
        '中等': 'warning',
        '一般': 'info',
        '低': 'danger'
      };
      return levelMap[level] || 'info';
    },
    getProgressColor(score) {
      if (score >= 90) return '#67C23A';
      if (score >= 80) return '#409EFF';
      if (score >= 70) return '#E6A23C';
      if (score >= 60) return '#909399';
      return '#F56C6C';
    },
    getForumScoreClass(score) {
      if (score >= 80) return 'score-high';
      if (score >= 60) return 'score-medium';
      return 'score-low';
    }
  }
};
</script>

<style scoped>
/* Mac Style for Participation Heat */
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

/* Stats Cards */
.stats-cards {
  margin-bottom: 24px;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-content {
  display: flex;
  align-items: center;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 28px;
  color: #fff;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.stats-icon.attendance {
  background: #0071e3;
}

.stats-icon.homework {
  background: #ff9500;
}

.stats-icon.forum {
  background: #34c759;
}

.stats-icon.activity {
  background: #8e8e93;
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 14px;
  color: #86868b;
}

/* Chart Card */
.chart-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

/* Table Styling */
.app-container >>> .el-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.app-container >>> .el-table th {
  background-color: #fbfbfd;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #f5f5f7;
  padding: 12px 0;
}

.app-container >>> .el-table td {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
}

/* Button Styling */
.app-container >>> .el-button {
  border-radius: 980px;
  font-weight: 500;
  border: none;
  padding: 9px 20px;
  transition: all 0.2s ease;
}

.app-container >>> .el-button--primary {
  background-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}

.app-container >>> .el-button--primary:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

/* Custom Progress Bars */
.forum-score {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-text {
  font-size: 12px;
  color: #86868b;
  margin-bottom: 4px;
}

.score-bar {
  width: 80px;
  height: 6px;
  background-color: #f5f5f7;
  border-radius: 3px;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.score-high {
  background-color: #34c759;
}

.score-medium {
  background-color: #ff9500;
}

.score-low {
  background-color: #ff3b30;
}

.participation-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.participation-progress .el-progress {
  margin-bottom: 4px;
}

/* Tags */
.app-container >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}
</style>
