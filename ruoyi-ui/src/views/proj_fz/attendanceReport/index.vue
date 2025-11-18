<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="100px">
      <el-form-item label="课堂选择" prop="sessionId">
        <el-select v-model="queryParams.sessionId" placeholder="请选择课堂" clearable filterable>
          <el-option
            v-for="session in sessionList"
            :key="session.sessionId"
            :label="session.className"
            :value="session.sessionId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          :picker-options="pickerOptions"
        />
      </el-form-item>
      <el-form-item label="统计维度" prop="statDimension">
        <el-radio-group v-model="statDimension" @change="handleDimensionChange">
          <el-radio-button label="summary">汇总统计</el-radio-button>
          <el-radio-button label="trend">趋势分析</el-radio-button>
          <el-radio-button label="details">明细查询</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="statDimension === 'details'" label="签到状态" prop="attendanceStatus">
        <el-select v-model="queryParams.attendanceStatus" placeholder="请选择状态" clearable>
          <el-option label="全部" :value="null" />
          <el-option label="已签到" :value="1" />
          <el-option label="缺勤" :value="0" />
          <el-option label="迟到" :value="2" />
          <el-option label="请假" :value="3" />
          <el-option label="早退" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery" :loading="loading">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        <el-button type="warning" icon="el-icon-download" @click="handleExport" :loading="exportLoading">导出</el-button>
        <el-button type="info" icon="el-icon-printer" @click="handlePrint">打印</el-button>
      </el-form-item>
    </el-form>

    <!-- 图表展示区域 -->
    <div v-if="showCharts" class="chart-container">
      <!-- 汇总统计维度图表 -->
      <div v-if="statDimension === 'summary'">
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="12">
            <div class="chart-wrapper">
              <div class="chart-title">课堂签到率对比</div>
              <div ref="summaryChart" style="height: 400px;"></div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-wrapper">
              <div class="chart-title">考勤状态分布</div>
              <div ref="distributionChart" style="height: 400px;"></div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 趋势分析维度图表 -->
      <div v-if="statDimension === 'trend'">
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="24">
            <div class="chart-wrapper">
              <div class="chart-title">签到率趋势分析</div>
              <div ref="trendChart" style="height: 400px;"></div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="24">
            <div class="chart-wrapper">
              <div class="chart-title">每次签到统计</div>
              <div ref="dailyChart" style="height: 400px;"></div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="dataList"
      :row-key="rowKey"
      style="width: 100%"
      stripe
    >
      <!-- 汇总统计维度列 -->
      <template v-if="statDimension === 'summary'">
        <el-table-column
          label="课堂名称"
          align="center"
          min-width="150"
          show-overflow-tooltip
          prop="className"
        />
        <el-table-column
          label="总人数"
          align="center"
          width="90"
          prop="totalStudents"
        />
        <el-table-column
          label="平均已签到"
          align="center"
          width="100"
          prop="signedCount"
        />
        <el-table-column
          label="平均缺勤"
          align="center"
          width="100"
          prop="absentCount"
        />
        <el-table-column
          label="平均迟到"
          align="center"
          width="100"
          prop="lateCount"
        />
        <el-table-column
          label="平均请假"
          align="center"
          width="100"
          prop="leaveCount"
        />
        <el-table-column
          label="平均早退"
          align="center"
          width="100"
          prop="earlyLeaveCount"
        />
        <el-table-column
          label="平均签到率"
          align="center"
          sortable
          width="120"
        >
          <template slot-scope="scope">
            <el-tag :type="getRateType(scope.row.attendanceRate)" effect="dark">
              {{ formatRate(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
      </template>

      <!-- 趋势分析维度列 -->
      <template v-if="statDimension === 'trend'">
        <el-table-column
          label="签到时间"
          align="center"
          prop="statDate"
          width="160"
        >
          <template slot-scope="scope">
            {{ scope.row.statDate ? parseTime(scope.row.statDate, '{y}-{m}-{d} {h}:{i}') : '未知时间' }}
          </template>
        </el-table-column>
        <el-table-column label="签到人数" align="center" width="100" prop="dailySigned" />
        <el-table-column label="缺勤人数" align="center" width="100" prop="dailyAbsent" />
        <el-table-column label="迟到人数" align="center" width="100" prop="dailyLate" />
        <el-table-column label="请假人数" align="center" width="100" prop="dailyLeave" />
        <el-table-column label="早退人数" align="center" width="100" prop="dailyEarlyLeave" />
        <el-table-column label="签到率" align="center" width="100">
          <template slot-scope="scope">
            <el-tag :type="getRateType(scope.row.attendanceRate)" effect="dark">
              {{ (scope.row.attendanceRate || 0).toFixed(1) }}%
            </el-tag>
          </template>
        </el-table-column>
      </template>

      <!-- 明细查询维度列 -->
      <template v-if="statDimension === 'details'">
        <el-table-column
          label="学生姓名"
          align="center"
          width="120"
          prop="studentName"
        />
        <el-table-column
          label="学号"
          align="center"
          prop="studentNo"
          width="120"
        />
        <el-table-column
          label="课堂名称"
          align="center"
          min-width="150"
          show-overflow-tooltip
          prop="className"
        />
        <el-table-column
          label="签到时间"
          align="center"
          prop="attendanceTime"
          width="160"
        >
          <template slot-scope="scope">
            {{ scope.row.attendanceTime ? parseTime(scope.row.attendanceTime) : '未签到' }}
          </template>
        </el-table-column>
        <el-table-column
          label="签到方式"
          align="center"
          prop="attendanceMethod"
          width="100"
        />
        <el-table-column
          label="状态"
          align="center"
          prop="statusText"
          width="100"
        >
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.attendanceStatus)" effect="light">
              {{ scope.row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
      </template>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { listSession } from "@/api/proj_lw/session";
import {
  sessionStatistics,
  timeStatistics,
  attendanceDetails,
  exportAttendanceReport
} from "@/api/proj_fz/attendanceReport";

export default {
  name: "AttendanceReport",
  data() {
    return {
      loading: false,
      exportLoading: false,
      total: 0,
      dataList: [],
      sessionList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        sessionId: undefined,
        attendanceStatus: undefined,
        groupBy: 'day'
      },
      dateRange: [],
      statDimension: 'summary',
      summaryChart: null,
      distributionChart: null,
      trendChart: null,
      dailyChart: null,
      showCharts: true,
      chartImages: {},
      pickerOptions: {
        shortcuts: [{
          text: '最近一周',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: '最近一个月',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: '最近三个月',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
            picker.$emit('pick', [start, end]);
          }
        }]
      }
    };
  },
  created() {
    this.getSessionList();
    // 设置默认时间范围为最近一周
    const end = new Date();
    const start = new Date();
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
    this.dateRange = [this.parseTime(start, '{y}-{m}-{d}'), this.parseTime(end, '{y}-{m}-{d}')];
  },
  mounted() {
    // 延迟加载数据，确保图表容器已渲染
    setTimeout(() => {
      this.getList();
    }, 300);
  },
  beforeDestroy() {
    [this.summaryChart, this.distributionChart, this.trendChart, this.dailyChart].forEach(chart => {
      if (chart) {
        try { chart.dispose() } catch(e){}
      }
    });
  },
  methods: {
    /** 查询课堂列表 */
    getSessionList() {
      this.loading = true;
      listSession().then(response => {
        console.log('课堂列表响应:', response);

        // 修复：直接使用响应数据，不进行复杂的结构处理
        let rows = [];
        if (response && response.code === 200) {
          // 直接使用 response.rows
          rows = response.rows || [];
        }

        // 修复：简化映射逻辑
        this.sessionList = rows.map(r => ({
          sessionId: r.sessionId,
          className: r.className || '未知课堂'
        })).filter(item => item.sessionId); // 过滤掉无效数据

        console.log('处理后的课堂列表:', this.sessionList);
        this.loading = false;
      }).catch(err => {
        console.error('加载课堂列表失败', err);
        this.sessionList = [];
        this.loading = false;
        this.$message.error('加载课堂列表失败: ' + (err.message || '请检查网络连接'));
      });
    },

    /** 查询数据 */
    getList() {
      this.loading = true;
      const params = {
        ...this.queryParams,
        startDate: this.dateRange && this.dateRange.length ? this.dateRange[0] : undefined,
        endDate: this.dateRange && this.dateRange.length ? this.dateRange[1] : undefined
      };

      console.log('查询参数:', params);

      let apiMethod;
      switch (this.statDimension) {
        case 'summary':
          apiMethod = sessionStatistics;
          break;
        case 'trend':
          params.groupBy = 'day';
          apiMethod = timeStatistics;
          break;
        case 'details':
          apiMethod = attendanceDetails;
          break;
        default:
          apiMethod = sessionStatistics;
      }

      apiMethod(params).then(response => {
        console.log('API响应:', response);

        // 修复：简化数据解析逻辑
        let data = [];
        let totalCount = 0;

        if (response && response.code === 200) {
          if (this.statDimension === 'trend') {
            // 趋势分析的特殊结构
            data = response.data?.statistics || [];
            totalCount = data.length;
          } else {
            // 汇总统计和明细查询的标准结构
            data = response.rows || [];
            totalCount = response.total || data.length;
          }
        }

        this.total = totalCount;
        this.dataList = data;

        console.log('处理后的数据:', this.dataList);

        // 更新图表 - 确保在数据加载完成后
        this.$nextTick(() => {
          setTimeout(() => {
            this.initCharts();
            if (this.statDimension === 'summary') {
              this.updateSummaryCharts(this.dataList);
            } else if (this.statDimension === 'trend') {
              this.updateTrendCharts(this.dataList);
            }
          }, 100);
        });

        this.loading = false;
      }).catch(err => {
        console.error('查询失败', err);
        this.loading = false;
        this.$message.error('数据加载失败: ' + (err.message || '请检查网络连接'));
      });
    },

    /** 初始化图表 */
    initCharts() {
      try {
        // 只在图表容器存在时初始化
        const initChart = (refName, chartVar) => {
          if (this.$refs[refName] && this.$refs[refName].offsetHeight > 0) {
            const dom = this.$refs[refName];
            // 先销毁现有实例
            const inst = echarts.getInstanceByDom(dom);
            if (inst) {
              echarts.dispose(dom);
            }
            this[chartVar] = echarts.init(dom);
            console.log(`初始化图表: ${chartVar}`);
          }
        };

        if (this.statDimension === 'summary') {
          initChart('summaryChart', 'summaryChart');
          initChart('distributionChart', 'distributionChart');
        } else if (this.statDimension === 'trend') {
          initChart('trendChart', 'trendChart');
          initChart('dailyChart', 'dailyChart');
        }
      } catch (e) {
        console.warn('初始化图表失败', e);
      }
    },

    /** 生成图表图片用于打印 */
    generateChartImages() {
      try {
        if (this.summaryChart) {
          this.chartImages.summary = this.summaryChart.getDataURL({
            pixelRatio: 2,
            backgroundColor: '#fff'
          });
        }
        if (this.distributionChart) {
          this.chartImages.distribution = this.distributionChart.getDataURL({
            pixelRatio: 2,
            backgroundColor: '#fff'
          });
        }
        if (this.trendChart) {
          this.chartImages.trend = this.trendChart.getDataURL({
            pixelRatio: 2,
            backgroundColor: '#fff'
          });
        }
        if (this.dailyChart) {
          this.chartImages.daily = this.dailyChart.getDataURL({
            pixelRatio: 2,
            backgroundColor: '#fff'
          });
        }
      } catch (e) {
        console.warn('生成图表图片失败', e);
      }
    },

    /** 更新汇总统计图表 */
    updateSummaryCharts(dataList) {
      if (!dataList || dataList.length === 0) {
        console.log('汇总统计无数据，显示空图表');
        this.setEmptyChart(this.summaryChart, '课堂签到率对比');
        this.setEmptyChart(this.distributionChart, '考勤状态分布');
        return;
      }

      console.log('更新汇总统计图表，数据:', dataList);

      // 课堂签到率对比图
      if (this.summaryChart) {
        const classNames = dataList.map(item => item.className || '未知课堂');
        const rates = dataList.map(item => item.attendanceRate || 0);

        const option = {
          title: {
            text: '课堂签到率对比',
            left: 'center',
            textStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' }
          },
          tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(255,255,255,0.9)',
            borderColor: '#ddd',
            borderWidth: 1,
            textStyle: { color: '#333' },
            formatter: (params) => {
              const data = params[0];
              const session = dataList[data.dataIndex];
              return `
                <div style="font-weight: bold; margin-bottom: 5px;">${data.name}</div>
                <div>平均签到率: <span style="color: #5470c6; font-weight: bold">${data.value}%</span></div>
                <div>总人数: ${session.totalStudents || 0}</div>
                <div>平均已签到: ${session.signedCount || 0}</div>
              `;
            }
          },
          grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
          xAxis: {
            type: 'category',
            data: classNames,
            axisLabel: {
              interval: 0,
              rotate: 45,
              color: '#666'
            },
            axisLine: { lineStyle: { color: '#ddd' } }
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              formatter: '{value}%',
              color: '#666'
            },
            axisLine: { show: false },
            splitLine: { lineStyle: { color: '#f0f0f0' } },
            min: 0,
            max: 100
          },
          series: [{
            data: rates,
            type: 'bar',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ]),
              borderRadius: [4, 4, 0, 0]
            },
            emphasis: {
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#2378f7' },
                  { offset: 0.7, color: '#2378f7' },
                  { offset: 1, color: '#83bff6' }
                ])
              }
            },
            label: {
              show: true,
              position: 'top',
              formatter: '{c}%',
              color: '#333',
              fontWeight: 'bold'
            }
          }]
        };
        this.summaryChart.setOption(option, true);
      }

      // 考勤状态分布图（使用第一个课堂的数据）
      if (this.distributionChart && dataList[0]) {
        const sessionData = dataList[0];
        const pieData = [
          { value: sessionData.signedCount || 0, name: '已签到' },
          { value: sessionData.lateCount || 0, name: '迟到' },
          { value: sessionData.leaveCount || 0, name: '请假' },
          { value: sessionData.earlyLeaveCount || 0, name: '早退' },
          { value: sessionData.absentCount || 0, name: '缺勤' }
        ].filter(item => item.value > 0);

        const option = {
          title: {
            text: '平均考勤状态分布',
            left: 'center',
            textStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' }
          },
          tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(255,255,255,0.9)',
            borderColor: '#ddd',
            borderWidth: 1,
            textStyle: { color: '#333' },
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            top: 'middle',
            textStyle: { color: '#666' }
          },
          series: [{
            name: '考勤状态',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            data: pieData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            itemStyle: {
              borderRadius: 8,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              formatter: '{b}: {d}%',
              color: '#333'
            }
          }],
          color: ['#67C23A', '#E6A23C', '#409EFF', '#F56C6C', '#909399']
        };
        this.distributionChart.setOption(option, true);
      }
    },

    /** 更新趋势分析图表 */
    updateTrendCharts(dataList) {
      if (!dataList || dataList.length === 0) {
        console.log('趋势分析无数据，显示空图表');
        this.setEmptyChart(this.trendChart, '签到率趋势分析');
        this.setEmptyChart(this.dailyChart, '每次签到统计');
        return;
      }

      console.log('更新趋势分析图表，数据:', dataList);

      const dateList = dataList.map(item => {
        if (item.statDate) {
          return this.parseTime(item.statDate, '{m}-{d} {h}:{i}');
        }
        return '未知时间';
      });
      const rateList = dataList.map(item => item.attendanceRate || 0);
      const signedList = dataList.map(item => item.dailySigned || 0);
      const absentList = dataList.map(item => item.dailyAbsent || 0);
      const lateList = dataList.map(item => item.dailyLate || 0);
      const leaveList = dataList.map(item => item.dailyLeave || 0);
      const earlyLeaveList = dataList.map(item => item.dailyEarlyLeave || 0);

      // 签到率趋势图
      if (this.trendChart) {
        const option = {
          title: {
            text: '签到率趋势分析',
            left: 'center',
            textStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' }
          },
          tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(255,255,255,0.9)',
            borderColor: '#ddd',
            borderWidth: 1,
            textStyle: { color: '#333' }
          },
          grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
          xAxis: {
            type: 'category',
            data: dateList,
            axisLabel: {
              rotate: 45,
              interval: 0,
              color: '#666'
            },
            axisLine: { lineStyle: { color: '#ddd' } }
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              formatter: '{value}%',
              color: '#666'
            },
            axisLine: { show: false },
            splitLine: { lineStyle: { color: '#f0f0f0' } },
            min: 0,
            max: 100
          },
          series: [{
            data: rateList,
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            itemStyle: {
              color: '#5470c6',
              borderColor: '#fff',
              borderWidth: 2
            },
            lineStyle: {
              width: 4,
              shadowColor: 'rgba(84, 112, 198, 0.3)',
              shadowBlur: 10,
              shadowOffsetY: 5
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(84, 112, 198, 0.6)' },
                { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
              ])
            },
            label: {
              show: true,
              position: 'top',
              formatter: '{c}%',
              color: '#5470c6',
              fontWeight: 'bold'
            }
          }]
        };
        this.trendChart.setOption(option, true);
      }

      // 每次签到统计图 - 改为柱状图
      if (this.dailyChart) {
        const option = {
          title: {
            text: '每次签到统计',
            left: 'center',
            textStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' }
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            backgroundColor: 'rgba(255,255,255,0.9)',
            borderColor: '#ddd',
            borderWidth: 1,
            textStyle: { color: '#333' }
          },
          legend: {
            data: ['已签到', '迟到', '请假', '早退', '缺勤'],
            bottom: 0,
            textStyle: { color: '#666' }
          },
          grid: { left: '3%', right: '4%', bottom: '12%', top: '15%', containLabel: true },
          xAxis: {
            type: 'category',
            data: dateList,
            axisLabel: {
              rotate: 45,
              interval: 0,
              color: '#666'
            },
            axisLine: { lineStyle: { color: '#ddd' } }
          },
          yAxis: {
            type: 'value',
            axisLabel: { color: '#666' },
            axisLine: { show: false },
            splitLine: { lineStyle: { color: '#f0f0f0' } }
          },
          series: [
            {
              name: '已签到',
              type: 'bar',
              data: signedList,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#9EE6A1' },
                  { offset: 1, color: '#67C23A' }
                ]),
                borderRadius: [2, 2, 0, 0]
              }
            },
            {
              name: '迟到',
              type: 'bar',
              data: lateList,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#F8D9A0' },
                  { offset: 1, color: '#E6A23C' }
                ]),
                borderRadius: [2, 2, 0, 0]
              }
            },
            {
              name: '请假',
              type: 'bar',
              data: leaveList,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#A0C8FF' },
                  { offset: 1, color: '#409EFF' }
                ]),
                borderRadius: [2, 2, 0, 0]
              }
            },
            {
              name: '早退',
              type: 'bar',
              data: earlyLeaveList,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#F9A7A7' },
                  { offset: 1, color: '#F56C6C' }
                ]),
                borderRadius: [2, 2, 0, 0]
              }
            },
            {
              name: '缺勤',
              type: 'bar',
              data: absentList,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#C0C4CC' },
                  { offset: 1, color: '#909399' }
                ]),
                borderRadius: [2, 2, 0, 0]
              }
            }
          ]
        };
        this.dailyChart.setOption(option, true);
      }
    },

    /** 设置空图表 */
    setEmptyChart(chart, title) {
      if (chart) {
        const option = {
          title: {
            text: title,
            left: 'center',
            top: 'center',
            textStyle: { color: '#999', fontSize: 16, fontWeight: 'normal' }
          },
          graphic: {
            type: 'text',
            left: 'center',
            top: '45%',
            style: { text: '暂无数据', fontSize: 14, fill: '#999' }
          },
          xAxis: { show: false },
          yAxis: { show: false }
        };
        chart.setOption(option, true);
      }
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.queryParams.sessionId = undefined;
      this.queryParams.attendanceStatus = undefined;
      this.queryParams.pageNum = 1;
      // 重置后重新设置默认时间范围
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
      this.dateRange = [this.parseTime(start, '{y}-{m}-{d}'), this.parseTime(end, '{y}-{m}-{d}')];
      this.getList();
    },

    /** 维度变更 */
    handleDimensionChange() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 导出按钮操作 */
    handleExport() {
      if (this.dataList.length === 0) {
        this.$message.warning('没有数据可以导出');
        return;
      }

      this.exportLoading = true;
      const params = {
        sessionId: this.queryParams.sessionId,
        startDate: this.dateRange && this.dateRange.length ? this.dateRange[0] : undefined,
        endDate: this.dateRange && this.dateRange.length ? this.dateRange[1] : undefined,
        exportType: this.statDimension,
        attendanceStatus: this.queryParams.attendanceStatus
      };

      const className = this.queryParams.sessionId ?
        (this.sessionList.find(s => s.sessionId === this.queryParams.sessionId) || {}).className : '全部课堂';
      const dateRangeText = params.startDate && params.endDate ?
        `${params.startDate.replace(/-/g,'')}-${params.endDate.replace(/-/g,'')}` : this.getCurrentTime();
      const filename = `考勤报表_${this.getDimensionText()}_${className}_${dateRangeText}.xlsx`;

      exportAttendanceReport(params, filename).then(() => {
        this.$message.success('导出成功');
        this.exportLoading = false;
      }).catch(err => {
        console.error('导出失败', err);
        this.$message.error('导出失败: ' + (err.message || '请检查数据'));
        this.exportLoading = false;
      });
    },

    /** 打印功能 */
    handlePrint() {
      if (this.dataList.length === 0) {
        this.$message.warning('没有数据可以打印');
        return;
      }

      // 确保图表图片已生成
      this.generateChartImages();

      setTimeout(() => {
        const printContent = this.generatePrintContent();
        const printWindow = window.open('', '_blank');
        if (printWindow) {
          printWindow.document.write(printContent);
          printWindow.document.close();
          printWindow.onload = () => {
            printWindow.print();
          };
        }
      }, 1000);
    },

    /** 生成打印内容 */
    generatePrintContent() {
      const title = '考勤报表';
      const now = this.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}');
      const userName = (this.$store && this.$store.state.user && this.$store.state.user.userName) || '当前用户';

      let tableContent = '';
      let chartContent = '';

      if (this.statDimension === 'summary') {
        tableContent = this.generateSummaryTable();
        chartContent = this.generateSummaryChartsForPrint();
      } else if (this.statDimension === 'trend') {
        tableContent = this.generateTrendTable();
        chartContent = this.generateTrendChartsForPrint();
      } else {
        tableContent = this.generateDetailsTable();
      }

      return `
        <!DOCTYPE html>
        <html>
        <head>
          <title>${title}</title>
          <meta charset="UTF-8">
          <style>
            body { font-family: "Microsoft YaHei", Arial, sans-serif; margin: 20px; line-height: 1.6; }
            .header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 10px; }
            .meta { margin-bottom: 20px; color: #666; background: #f9f9f9; padding: 10px; border-radius: 4px; }
            .chart-section { margin: 25px 0; }
            .chart-container { text-align: center; margin: 20px 0; page-break-inside: avoid; }
            .chart-title { font-weight: bold; margin-bottom: 15px; font-size: 16px; color: #333; }
            .chart-image { max-width: 90%; height: auto; border: 1px solid #ddd; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .table-section { margin-top: 25px; }
            table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 12px; }
            table, th, td { border: 1px solid #ddd; }
            th, td { padding: 8px 12px; text-align: center; }
            th { background-color: #f5f7fa; font-weight: bold; color: #333; }
            .no-data { text-align: center; color: #999; padding: 40px; font-size: 14px; }
            .print-info { background: #e8f4ff; padding: 10px; margin: 10px 0; border-radius: 4px; font-size: 12px; }
            @media print {
              body { margin: 0.5cm; font-size: 12pt; }
              .no-print { display: none; }
              .chart-image { max-width: 95% !important; }
              .chart-container { page-break-inside: avoid; }
              table { page-break-inside: auto; }
              tr { page-break-inside: avoid; page-break-after: auto; }
            }
          </style>
        </head>
        <body>
          <div class="header">
            <h1 style="margin: 0; color: #333;">${title}</h1>
          </div>

          <div class="meta">
            <div class="print-info">
              <strong>统计时间:</strong> ${this.dateRange && this.dateRange.length ? this.dateRange[0] + ' 至 ' + this.dateRange[1] : '全部时间'} |
              <strong>统计维度:</strong> ${this.getDimensionText()} |
              <strong>课堂:</strong> ${this.queryParams.sessionId ? ((this.sessionList.find(s => s.sessionId === this.queryParams.sessionId) || {}).className || '未知') : '全部课堂'}
            </div>
            <div class="print-info">
              <strong>生成人:</strong> ${userName} |
              <strong>生成时间:</strong> ${now} |
              <strong>数据条数:</strong> ${this.dataList.length} 条
            </div>
          </div>

          ${chartContent}

          <div class="table-section">
            <h2 style="color: #333; border-bottom: 1px solid #eee; padding-bottom: 8px;">数据明细</h2>
            ${tableContent}
          </div>
        </body>
        </html>
      `;
    },

    /** 生成汇总统计图表打印内容 */
    generateSummaryChartsForPrint() {
      if (!this.dataList || this.dataList.length === 0 || !this.chartImages.summary) {
        return '<div class="no-data">暂无图表数据</div>';
      }

      return `
        <div class="chart-section">
          <h2 style="color: #333; border-bottom: 1px solid #eee; padding-bottom: 8px;">统计图表</h2>
          <div class="chart-container">
            <div class="chart-title">课堂签到率对比</div>
            <img src="${this.chartImages.summary}" class="chart-image" alt="课堂签到率对比图" onerror="this.style.display='none'">
          </div>
          <div class="chart-container">
            <div class="chart-title">平均考勤状态分布</div>
            <img src="${this.chartImages.distribution}" class="chart-image" alt="考勤状态分布图" onerror="this.style.display='none'">
          </div>
        </div>
      `;
    },

    /** 生成趋势分析图表打印内容 */
    generateTrendChartsForPrint() {
      if (!this.dataList || this.dataList.length === 0 || !this.chartImages.trend) {
        return '<div class="no-data">暂无图表数据</div>';
      }

      return `
        <div class="chart-section">
          <h2 style="color: #333; border-bottom: 1px solid #eee; padding-bottom: 8px;">统计图表</h2>
          <div class="chart-container">
            <div class="chart-title">签到率趋势分析</div>
            <img src="${this.chartImages.trend}" class="chart-image" alt="签到率趋势分析图" onerror="this.style.display='none'">
          </div>
          <div class="chart-container">
            <div class="chart-title">每次签到统计</div>
            <img src="${this.chartImages.daily}" class="chart-image" alt="每次签到统计图" onerror="this.style.display='none'">
          </div>
        </div>
      `;
    },

    /** 生成汇总统计表格 */
    generateSummaryTable() {
      if (!this.dataList || this.dataList.length === 0) {
        return '<div class="no-data">暂无数据</div>';
      }

      return `
        <table>
          <thead>
            <tr>
              <th>课堂名称</th>
              <th>总人数</th>
              <th>平均已签到</th>
              <th>平均缺勤</th>
              <th>平均迟到</th>
              <th>平均请假</th>
              <th>平均早退</th>
              <th>平均签到率</th>
            </tr>
          </thead>
          <tbody>
            ${this.dataList.map(item => `
              <tr>
                <td>${item.className || '未知'}</td>
                <td>${item.totalStudents || 0}</td>
                <td>${item.signedCount || 0}</td>
                <td>${item.absentCount || 0}</td>
                <td>${item.lateCount || 0}</td>
                <td>${item.leaveCount || 0}</td>
                <td>${item.earlyLeaveCount || 0}</td>
                <td>${(item.attendanceRate || 0).toFixed(1)}%</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    },

    /** 生成趋势分析表格 */
    generateTrendTable() {
      if (!this.dataList || this.dataList.length === 0) {
        return '<div class="no-data">暂无数据</div>';
      }

      return `
        <table>
          <thead>
            <tr>
              <th>签到时间</th>
              <th>签到人数</th>
              <th>缺勤人数</th>
              <th>迟到人数</th>
              <th>请假人数</th>
              <th>早退人数</th>
              <th>签到率</th>
            </tr>
          </thead>
          <tbody>
            ${this.dataList.map(item => `
              <tr>
                <td>${item.statDate ? this.parseTime(item.statDate, '{y}-{m}-{d} {h}:{i}') : '未知时间'}</td>
                <td>${item.dailySigned || 0}</td>
                <td>${item.dailyAbsent || 0}</td>
                <td>${item.dailyLate || 0}</td>
                <td>${item.dailyLeave || 0}</td>
                <td>${item.dailyEarlyLeave || 0}</td>
                <td>${(item.attendanceRate || 0).toFixed(1)}%</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    },

    /** 生成明细表格 */
    generateDetailsTable() {
      if (!this.dataList || this.dataList.length === 0) {
        return '<div class="no-data">暂无数据</div>';
      }

      return `
        <table>
          <thead>
            <tr>
              <th>学生姓名</th>
              <th>学号</th>
              <th>课堂名称</th>
              <th>签到时间</th>
              <th>签到方式</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            ${this.dataList.map(item => `
              <tr>
                <td>${item.studentName || '未知'}</td>
                <td>${item.studentNo || '未知'}</td>
                <td>${item.className || '未知'}</td>
                <td>${item.attendanceTime ? this.parseTime(item.attendanceTime) : '未签到'}</td>
                <td>${item.attendanceMethod || '未知'}</td>
                <td>${item.statusText || '未知'}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    },

    /** 获取维度文本 */
    getDimensionText() {
      const dimensionMap = {
        'summary': '汇总统计',
        'trend': '趋势分析',
        'details': '明细查询'
      };
      return dimensionMap[this.statDimension] || '汇总统计';
    },

    /** 格式化签到率显示 */
    formatRate(row) {
      return `${(row.attendanceRate || 0).toFixed(1)}%`;
    },

    /** 获取签到率类型 */
    getRateType(rate) {
      const value = rate || 0;
      if (value >= 90) return 'success';
      if (value >= 70) return 'warning';
      return 'danger';
    },

    /** 获取状态类型 */
    getStatusType(status) {
      const statusMap = {
        0: 'danger',   // 缺勤
        1: 'success',  // 已签到
        2: 'warning',  // 迟到
        3: 'info',     // 请假
        4: 'warning'   // 早退
      };
      return statusMap[status] || 'info';
    },

    /** 时间格式化 */
    parseTime(time, pattern) {
      if (!time) return '';
      const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}';
      let date;
      if (typeof time === 'object') {
        date = time;
      } else {
        if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
          time = parseInt(time);
        }
        if ((typeof time === 'number') && (time.toString().length === 10)) {
          time = time * 1000;
        }
        date = new Date(time);
      }
      const formatObj = {
        y: date.getFullYear(),
        m: date.getMonth() + 1,
        d: date.getDate(),
        h: date.getHours(),
        i: date.getMinutes(),
        s: date.getSeconds(),
        a: date.getDay()
      };
      const time_str = format.replace(/{([ymdhisa])+}/g, (result, key) => {
        let value = formatObj[key];
        if (key === 'a') { return ['日', '一', '二', '三', '四', '五', '六'][value]; }
        if (result.length > 0 && value < 10) {
          value = '0' + value;
        }
        return value || 0;
      });
      return time_str;
    },

    /** 获取当前时间 */
    getCurrentTime() {
      return this.parseTime(new Date(), '{y}{m}{d}{h}{i}{s}');
    },

    rowKey(row) {
      // 修复重复key问题
      if (this.statDimension === 'summary') {
        return `summary_${row.sessionId || Math.random()}`;
      } else if (this.statDimension === 'trend') {
        return `trend_${row.taskId || row.statDate || Math.random()}`;
      } else {
        return `details_${row.studentNo}_${row.attendanceTime || Math.random()}`;
      }
    }
  }
};
</script>

<style scoped>
.chart-container {
  margin-bottom: 20px;
}
.chart-wrapper {
  background: #fff;
  padding: 16px 16px 0;
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #ebeef5;
}
.chart-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: center;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.el-table th {
  background: #f7f9fb !important;
  color: #333;
  font-weight: 600;
}

@media print {
  .el-form-item, .el-button {
    display: none !important;
  }
  .app-container {
    padding: 0 !important;
  }
}
</style>
