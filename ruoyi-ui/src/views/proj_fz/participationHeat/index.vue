<template>
  <div class="app-container">
    <!-- 打印区域 - 始终隐藏，只在打印时显示 -->
    <div ref="printContent" class="print-content" style="display: none;">
      <div class="print-header">
        <h2>学生参与热力数据报表</h2>
        <p>生成时间：{{ currentTime }}</p>
        <p>筛选条件：{{ getPrintFilterText() }}</p>
      </div>

      <!-- 统计卡片 -->
      <div class="print-stats">
        <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px;">
          <tr>
            <td style="width: 25%; text-align: center; padding: 10px; border: 1px solid #ddd;">
              <strong>平均签到率</strong><br>{{ stats.avg_attendance_rate || 0 }}%
            </td>
            <td style="width: 25%; text-align: center; padding: 10px; border: 1px solid #ddd;">
              <strong>平均作业提交率</strong><br>{{ stats.avg_homework_rate || 0 }}%
            </td>
            <td style="width: 25%; text-align: center; padding: 10px; border: 1px solid #ddd;">
              <strong>活跃学生</strong><br>{{ stats.active_students || 0 }}人
            </td>
            <td style="width: 25%; text-align: center; padding: 10px; border: 1px solid #ddd;">
              <strong>论坛发帖</strong><br>{{ stats.total_posts || 0 }}条
            </td>
          </tr>
        </table>
      </div>

      <!-- 图表区域 -->
      <div class="print-charts" style="margin-bottom: 20px;">
        <div style="width: 48%; display: inline-block; vertical-align: top;">
          <h3 style="text-align: center;">参与度分布</h3>
          <div id="printDistributionChart" style="height: 300px;"></div>
        </div>
        <div style="width: 48%; display: inline-block; vertical-align: top; margin-left: 4%;">
          <h3 style="text-align: center;">参与热力对比</h3>
          <div id="printComparisonChart" style="height: 300px;"></div>
        </div>
      </div>

      <!-- 数据表格 -->
      <h3>学生参与热力详情</h3>
      <table class="print-table" border="1" cellspacing="0" cellpadding="8" style="width: 100%; border-collapse: collapse;">
        <thead>
        <tr>
          <th>学号</th>
          <th>姓名</th>
          <th>签到率(%)</th>
          <th>作业提交率(%)</th>
          <th>论坛活跃度(分)</th>
          <th>综合参与度(分)</th>
          <th>参与等级</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in printData" :key="item.student_id">
          <td>{{ item.student_no }}</td>
          <td>{{ item.student_name }}</td>
          <td>{{ item.attendance_rate }}</td>
          <td>{{ item.homework_rate }}</td>
          <td>{{ item.forum_score }}</td>
          <td>{{ item.participation_score }}</td>
          <td>{{ item.participation_level }}</td>
        </tr>
        </tbody>
      </table>
      <div class="print-footer">
        <p>总记录数：{{ printData.length }} 条</p>
      </div>
    </div>

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

      <!-- 筛选条件 -->
      <el-col :span="24">
        <el-card shadow="hover" class="filter-card">
          <template #header>
            <div class="card-header">
              <span>筛选条件</span>
              <div>
                <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
                <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
                <el-button type="success" icon="el-icon-download" @click="handleExport">导出</el-button>
                <el-button type="warning" icon="el-icon-printer" @click="handlePrint">打印</el-button>
              </div>
            </div>
          </template>

          <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px">
            <el-form-item label="学号" prop="studentNo">
              <el-input
                v-model="queryParams.studentNo"
                placeholder="请输入学号"
                clearable
                style="width: 200px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="姓名" prop="studentName">
              <el-input
                v-model="queryParams.studentName"
                placeholder="请输入姓名"
                clearable
                style="width: 200px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="参与等级" prop="participationLevel">
              <el-select v-model="queryParams.participationLevel" placeholder="请选择参与等级" clearable style="width: 200px">
                <el-option
                  v-for="item in filterOptions.participationLevels"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="分数范围" prop="scoreRange">
              <el-select v-model="queryParams.scoreRange" placeholder="请选择分数范围" clearable style="width: 200px">
                <el-option
                  v-for="item in filterOptions.scoreRanges"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="排序字段" prop="sortField">
              <el-select v-model="queryParams.sortField" placeholder="请选择排序字段" clearable style="width: 200px">
                <el-option label="签到率" value="attendance_rate" />
                <el-option label="作业提交率" value="homework_rate" />
                <el-option label="论坛活跃度" value="forum_score" />
                <el-option label="综合参与度" value="participation_score" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序方式" prop="sortOrder">
              <el-select v-model="queryParams.sortOrder" placeholder="请选择排序方式" clearable style="width: 200px">
                <el-option label="升序" value="ASC" />
                <el-option label="降序" value="DESC" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
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

          <!-- 可滚动表格容器 -->
          <div class="table-container">
            <el-table
              v-loading="loading"
              :data="participationHeatList"
              style="width: 100%"
              :max-height="tableHeight"
              stripe
            >
              <el-table-column label="学号" align="center" prop="student_no" width="120" fixed="left" />
              <el-table-column label="姓名" align="center" prop="student_name" width="100" fixed="left" />
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
              <el-table-column label="参与等级" align="center" prop="participation_level" width="100" fixed="right">
                <template #default="scope">
                  <el-tag :type="getLevelType(scope.row.participation_level)">
                    {{ scope.row.participation_level }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 分页 -->
          <pagination
            v-show="total>0"
            :total="total"
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="getList"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  listParticipationHeat,
  getParticipationStats,
  listParticipationHeatByFilter,
  exportParticipationHeat,
  getFilterOptions,
  getParticipationDistribution
} from "@/api/proj_fz/participationHeat";

export default {
  name: "ParticipationHeat",
  data() {
    return {
      loading: true,
      participationHeatList: [],
      printData: [],
      stats: {},
      distributionData: {},
      filterOptions: {
        participationLevels: [],
        scoreRanges: []
      },
      distributionChart: null,
      comparisonChart: null,
      tableHeight: 500,
      total: 0,
      currentTime: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentNo: undefined,
        studentName: undefined,
        participationLevel: undefined,
        scoreRange: undefined,
        sortField: undefined,
        sortOrder: undefined
      }
    };
  },
  mounted() {
    this.getList();
    this.getStats();
    this.getFilterOptions();
    this.calculateTableHeight();
    this.updateCurrentTime();
    window.addEventListener('resize', this.calculateTableHeight);
  },
  beforeUnmount() {
    [this.distributionChart, this.comparisonChart].forEach(chart => {
      if (chart) {
        chart.dispose();
      }
    });
    window.removeEventListener('resize', this.calculateTableHeight);
  },
  methods: {
    getList() {
      this.loading = true;
      listParticipationHeatByFilter(this.queryParams).then(response => {
        this.participationHeatList = response.data;
        this.printData = response.data;
        this.total = response.total || this.participationHeatList.length;
        this.loading = false;
        this.$nextTick(() => {
          this.initCharts();
        });
        this.getDistribution();
      }).catch(() => {
        this.loading = false;
      });
    },
    getStats() {
      getParticipationStats().then(response => {
        this.stats = response.data;
      });
    },
    getDistribution() {
      const params = {
        studentNo: this.queryParams.studentNo,
        studentName: this.queryParams.studentName
      };
      getParticipationDistribution(params).then(response => {
        this.distributionData = response.data;
      });
    },
    getFilterOptions() {
      getFilterOptions().then(response => {
        this.filterOptions = response.data;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    handleReset() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleExport() {
      this.$confirm('是否确认导出当前筛选的参与热力数据?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.loading = true;
        return exportParticipationHeat(this.queryParams);
      }).then(response => {
        this.download(response, '学生参与热力数据.xlsx');
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    async handlePrint() {
      this.updateCurrentTime();

      // 创建新的打印窗口
      const printWindow = window.open('', '_blank');
      if (!printWindow) {
        this.$message.error('请允许弹出窗口以进行打印');
        return;
      }

      // 准备打印内容
      const printContent = this.$refs.printContent.cloneNode(true);
      printContent.style.display = 'block';

      // 获取当前图表配置
      const distributionOption = this.getDistributionChartOption();
      const comparisonOption = this.getComparisonChartOption();

      // 创建打印页面的HTML结构
      const printHtml = `
        <!DOCTYPE html>
        <html>
        <head>
          <title>学生参与热力数据报表</title>
          <meta charset="utf-8">
          <script src="https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js"><\/script>
          <style>
            @media print {
              @page {
                size: A4 portrait;
                margin: 15mm;
              }

              body {
                margin: 0;
                padding: 0;
                font-family: 'Microsoft YaHei', Arial, sans-serif;
                font-size: 12px;
                color: #333;
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
              }

              .print-header {
                text-align: center;
                margin-bottom: 15px;
                border-bottom: 2px solid #333;
                padding-bottom: 8px;
                page-break-after: avoid;
              }
              .print-header h2 {
                margin: 0;
                color: #333;
                font-size: 18px;
              }

              .print-stats {
                margin-bottom: 15px;
                page-break-after: avoid;
              }
              .print-stats table {
                width: 100%;
                border-collapse: collapse;
              }
              .print-stats td {
                text-align: center;
                padding: 8px;
                border: 1px solid #ddd;
                background-color: #f9f9f9;
              }

              .print-charts {
                margin-bottom: 15px;
                page-break-after: avoid;
                page-break-inside: avoid;
              }
              .print-charts > div {
                display: inline-block;
                vertical-align: top;
              }

              .print-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 15px;
                font-size: 10px;
                page-break-inside: auto;
              }
              .print-table thead {
                display: table-header-group;
              }
              .print-table th {
                background-color: #f5f5f5 !important;
                font-weight: bold;
                text-align: center;
                padding: 6px;
                border: 1px solid #ddd;
                page-break-inside: avoid;
              }
              .print-table td {
                text-align: center;
                padding: 6px;
                border: 1px solid #ddd;
                page-break-inside: avoid;
              }
              .print-table tbody tr {
                page-break-inside: avoid;
                page-break-after: auto;
              }

              .print-footer {
                text-align: right;
                font-size: 10px;
                color: #666;
                page-break-before: avoid;
              }

              /* 确保表格在分页时正确显示表头 */
              table { page-break-inside:auto }
              tr    { page-break-inside:avoid; page-break-after:auto }
              thead { display:table-header-group }
              tfoot { display:table-footer-group }
            }

            @media screen {
              body {
                margin: 0;
                padding: 20px;
                font-family: 'Microsoft YaHei', Arial, sans-serif;
                font-size: 12px;
                color: #333;
              }
              .print-header {
                text-align: center;
                margin-bottom: 20px;
                border-bottom: 2px solid #333;
                padding-bottom: 10px;
              }
              .print-header h2 {
                margin: 0;
                color: #333;
                font-size: 18px;
              }
              .print-stats {
                margin-bottom: 20px;
              }
              .print-stats table {
                width: 100%;
                border-collapse: collapse;
              }
              .print-stats td {
                text-align: center;
                padding: 10px;
                border: 1px solid #ddd;
                background-color: #f9f9f9;
              }
              .print-charts {
                margin-bottom: 20px;
              }
              .print-charts > div {
                display: inline-block;
                vertical-align: top;
              }
              .print-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 20px;
                font-size: 10px;
              }
              .print-table th {
                background-color: #f5f5f5;
                font-weight: bold;
                text-align: center;
                padding: 6px;
                border: 1px solid #ddd;
              }
              .print-table td {
                text-align: center;
                padding: 6px;
                border: 1px solid #ddd;
              }
              .print-footer {
                text-align: right;
                font-size: 10px;
                color: #666;
              }
            }
          </style>
        </head>
        <body>
          ${printContent.innerHTML}
          <script>
            // 等待页面完全加载后初始化图表
            function initCharts() {
              try {
                // 参与度分布图表
                const distributionChartDom = document.getElementById('printDistributionChart');
                if (distributionChartDom) {
                  const distributionChart = echarts.init(distributionChartDom);
                  distributionChart.setOption(${JSON.stringify(distributionOption)});

                  // 等待图表渲染完成
                  distributionChart.on('finished', function() {
                    checkChartsReady();
                  });
                }

                // 参与热力对比图表
                const comparisonChartDom = document.getElementById('printComparisonChart');
                if (comparisonChartDom) {
                  const comparisonChart = echarts.init(comparisonChartDom);
                  comparisonChart.setOption(${JSON.stringify(comparisonOption)});

                  // 等待图表渲染完成
                  comparisonChart.on('finished', function() {
                    checkChartsReady();
                  });
                }
              } catch (error) {
                console.error('图表初始化错误:', error);
                // 如果出错，延迟打印
                setTimeout(() => {
                  window.print();
                }, 1000);
              }
            }

            let chartsReadyCount = 0;
            function checkChartsReady() {
              chartsReadyCount++;
              // 两个图表都渲染完成后打印
              if (chartsReadyCount >= 2) {
                setTimeout(() => {
                  window.print();
                }, 500);
              }
            }

            // 页面加载完成后初始化图表
            if (document.readyState === 'complete') {
              initCharts();
            } else {
              window.addEventListener('load', initCharts);
            }

            // 备用方案：如果5秒后图表仍未准备好，强制打印
            setTimeout(() => {
              window.print();
            }, 5000);
          <\/script>
        </body>
        </html>
      `;

      // 写入打印内容
      printWindow.document.open();
      printWindow.document.write(printHtml);
      printWindow.document.close();

      // 打印后关闭窗口
      printWindow.onafterprint = () => {
        printWindow.close();
      };
    },
    getPrintFilterText() {
      const filters = [];
      if (this.queryParams.studentNo) {
        filters.push(`学号: ${this.queryParams.studentNo}`);
      }
      if (this.queryParams.studentName) {
        filters.push(`姓名: ${this.queryParams.studentName}`);
      }
      if (this.queryParams.participationLevel) {
        filters.push(`参与等级: ${this.queryParams.participationLevel}`);
      }
      if (this.queryParams.scoreRange) {
        filters.push(`分数范围: ${this.queryParams.scoreRange}`);
      }
      return filters.length > 0 ? filters.join(' | ') : '全部数据';
    },
    updateCurrentTime() {
      const now = new Date();
      this.currentTime = now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    calculateTableHeight() {
      const windowHeight = window.innerHeight;
      this.tableHeight = windowHeight - 400;
    },
    initCharts() {
      this.initDistributionChart();
      this.initComparisonChart();
    },
    initDistributionChart() {
      const chartDom = this.$refs.distributionChart;
      if (!chartDom) return;

      if (this.distributionChart) {
        this.distributionChart.dispose();
      }

      this.distributionChart = echarts.init(chartDom);
      this.distributionChart.setOption(this.getDistributionChartOption());
    },
    initComparisonChart() {
      const chartDom = this.$refs.comparisonChart;
      if (!chartDom) return;

      if (this.comparisonChart) {
        this.comparisonChart.dispose();
      }

      this.comparisonChart = echarts.init(chartDom);
      this.comparisonChart.setOption(this.getComparisonChartOption());
    },
    getDistributionChartOption() {
      const distribution = this.calculateDistribution();

      return {
        animation: true,
        animationDuration: 1000,
        animationEasing: 'cubicOut',
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
    },
    getComparisonChartOption() {
      const displayData = this.participationHeatList.slice(0, 10);
      const studentNames = displayData.map(item => item.student_name || '未知');
      const attendanceRates = displayData.map(item => Number(item.attendance_rate) || 0);
      const homeworkRates = displayData.map(item => Number(item.homework_rate) || 0);
      const forumScores = displayData.map(item => Number(item.forum_score) || 0);
      const participationScores = displayData.map(item => Number(item.participation_score) || 0);

      return {
        animation: true,
        animationDuration: 1000,
        animationEasing: 'cubicOut',
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            let result = params[0].name + '<br/>';
            params.forEach(param => {
              result += `${param.seriesName}: ${param.value}`;
              if (param.seriesName === '签到率' || param.seriesName === '作业提交率') {
                result += '%';
              } else {
                result += '分';
              }
              result += '<br/>';
            });
            return result;
          }
        },
        legend: {
          data: ['签到率', '作业提交率', '论坛活跃度', '综合参与度'],
          bottom: 10
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
          data: studentNames,
          axisLabel: {
            interval: 0,
            rotate: 45
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '百分比(%)',
            max: 100,
            axisLabel: {
              formatter: '{value}'
            }
          },
          {
            type: 'value',
            name: '分数',
            max: 100,
            axisLabel: {
              formatter: '{value}'
            }
          }
        ],
        series: [
          {
            name: '签到率',
            type: 'bar',
            barWidth: '15%',
            data: attendanceRates,
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '作业提交率',
            type: 'bar',
            barWidth: '15%',
            data: homeworkRates,
            itemStyle: {
              color: '#E6A23C'
            }
          },
          {
            name: '论坛活跃度',
            type: 'bar',
            barWidth: '15%',
            data: forumScores,
            itemStyle: {
              color: '#F56C6C'
            }
          },
          {
            name: '综合参与度',
            type: 'line',
            yAxisIndex: 1,
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
    },
    calculateDistribution() {
      if (this.distributionData && Object.keys(this.distributionData).length > 0) {
        return [
          { value: this.distributionData.level_high_count || 0, name: '90-100 (极高)' },
          { value: this.distributionData.level_good_count || 0, name: '80-89 (高)' },
          { value: this.distributionData.level_medium_count || 0, name: '70-79 (中等)' },
          { value: this.distributionData.level_normal_count || 0, name: '60-69 (一般)' },
          { value: this.distributionData.level_low_count || 0, name: '0-59 (低)' }
        ].filter(item => item.value > 0);
      }

      const levels = {
        '极高': 0,
        '高': 0,
        '中等': 0,
        '一般': 0,
        '低': 0
      };

      this.participationHeatList.forEach(item => {
        if (item.participation_level) {
          levels[item.participation_level]++;
        }
      });

      return [
        { value: levels['极高'], name: '90-100 (极高)' },
        { value: levels['高'], name: '80-89 (高)' },
        { value: levels['中等'], name: '70-79 (中等)' },
        { value: levels['一般'], name: '60-69 (一般)' },
        { value: levels['低'], name: '0-59 (低)' }
      ].filter(item => item.value > 0);
    },
    getScoreType(score) {
      const numScore = Number(score);
      if (numScore >= 90) return 'success';
      if (numScore >= 80) return 'primary';
      if (numScore >= 70) return 'warning';
      if (numScore >= 60) return 'info';
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
      const numScore = Number(score);
      if (numScore >= 90) return '#67C23A';
      if (numScore >= 80) return '#409EFF';
      if (numScore >= 70) return '#E6A23C';
      if (numScore >= 60) return '#909399';
      return '#F56C6C';
    },
    getForumScoreClass(score) {
      const numScore = Number(score);
      if (numScore >= 80) return 'score-high';
      if (numScore >= 60) return 'score-medium';
      return 'score-low';
    },
    resetForm(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields();
      }
    },
    download(data, fileName) {
      if (!data) {
        this.$message.error('文件数据为空');
        return;
      }
      const url = window.URL.createObjectURL(new Blob([data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }
  }
};
</script>

<style scoped>
.stats-cards {
  margin-bottom: 20px;
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
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stats-icon.attendance {
  background-color: #409EFF;
}

.stats-icon.homework {
  background-color: #E6A23C;
}

.stats-icon.forum {
  background-color: #F56C6C;
}

.stats-icon.activity {
  background-color: #67C23A;
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stats-label {
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-card {
  margin-bottom: 20px;
}

.table-container {
  overflow-x: auto;
}

.forum-score {
  display: flex;
  align-items: center;
}

.score-text {
  margin-right: 10px;
  min-width: 40px;
}

.score-bar {
  flex: 1;
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  transition: width 0.3s;
}

.score-high {
  background-color: #67C23A;
}

.score-medium {
  background-color: #E6A23C;
}

.score-low {
  background-color: #F56C6C;
}

.participation-progress {
  display: flex;
  align-items: center;
}

.participation-progress .el-progress {
  flex: 1;
  margin-right: 10px;
}
</style>
