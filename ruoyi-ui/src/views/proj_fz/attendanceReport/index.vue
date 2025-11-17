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
          <el-radio-button label="session">课堂统计</el-radio-button>
          <el-radio-button label="time">时间趋势</el-radio-button>
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
        <el-button type="warning" icon="el-icon-download" @click="handleExport">导出</el-button>
        <el-button type="info" icon="el-icon-printer" @click="handlePrint">打印</el-button>
      </el-form-item>
    </el-form>

    <!-- 图表展示区域 -->
    <div v-if="showCharts" class="chart-container">
      <!-- 课堂统计维度图表 -->
      <div v-if="statDimension === 'session'">
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="12">
            <div class="chart-wrapper">
              <div class="chart-title">课堂签到分布</div>
              <div ref="distributionChart" style="height: 400px;"></div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-wrapper">
              <div class="chart-title">课堂签到率排名</div>
              <div ref="sessionRankChart" style="height: 400px;"></div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 时间趋势维度图表 -->
      <div v-if="statDimension === 'time'">
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="24">
            <div class="chart-wrapper">
              <div class="chart-title">签到率趋势图</div>
              <div ref="trendChart" style="height: 400px;"></div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="24">
            <div class="chart-wrapper">
              <div class="chart-title">每日签到统计</div>
              <div ref="timeStackChart" style="height: 320px;"></div>
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
      :row-class-name="rowClassName"
      @selection-change="handleSelectionChange"
      :default-sort="{prop: 'attendanceRate', order: 'descending'}"
      style="width: 100%"
    >
      <el-table-column type="selection" width="55" align="center" />

      <!-- 课堂统计维度列 -->
      <el-table-column
        v-if="statDimension === 'session'"
        label="课堂名称"
        align="center"
        min-width="120"
        show-overflow-tooltip
        prop="className"
      />
      <el-table-column
        v-if="statDimension === 'session'"
        label="总人数"
        align="center"
        width="90"
        prop="totalStudents"
      />
      <el-table-column
        v-if="statDimension === 'session'"
        label="已签到"
        align="center"
        width="90"
        prop="signedCount"
      />
      <el-table-column
        v-if="statDimension === 'session'"
        label="缺勤人数"
        align="center"
        width="90"
        prop="absentCount"
      />
      <el-table-column
        v-if="statDimension === 'session'"
        label="迟到人数"
        align="center"
        width="90"
        prop="lateCount"
      />
      <el-table-column
        v-if="statDimension === 'session'"
        label="请假人数"
        align="center"
        width="90"
        prop="leaveCount"
      />
      <el-table-column
        v-if="statDimension === 'session'"
        label="签到率"
        align="center"
        sortable
        width="100"
      >
        <template slot-scope="scope">
          <el-tag :type="getRateType(scope.row.attendanceRate)" effect="dark">
            {{ formatRate(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 时间趋势维度列 -->
      <el-table-column
        v-if="statDimension === 'time'"
        label="统计日期"
        align="center"
        prop="statDate"
        width="120"
      >
        <template slot-scope="scope">
          {{ scope.row.statDate ? parseTime(scope.row.statDate, '{y}-{m}-{d}') : scope.row.statWeek }}
        </template>
      </el-table-column>
      <el-table-column v-if="statDimension === 'time'" label="当日签到" align="center" width="100" prop="dailySigned" />
      <el-table-column v-if="statDimension === 'time'" label="当日缺勤" align="center" width="100" prop="dailyAbsent" />
      <el-table-column v-if="statDimension === 'time'" label="当日迟到" align="center" width="100" prop="dailyLate" />
      <el-table-column v-if="statDimension === 'time'" label="签到率" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="getRateType(scope.row.attendanceRate)" effect="dark">
            {{ scope.row.attendanceRate.toFixed(1) }}%
          </el-tag>
        </template>
      </el-table-column>

      <!-- 明细查询维度列 -->
      <el-table-column
        v-if="statDimension === 'details'"
        label="学生姓名"
        align="center"
        width="140"
        prop="studentName"
      />
      <el-table-column
        v-if="statDimension === 'details'"
        label="学号"
        align="center"
        prop="studentNo"
        width="120"
      />
      <el-table-column
        v-if="statDimension === 'details'"
        label="课堂名称"
        align="center"
        min-width="120"
        show-overflow-tooltip
        prop="className"
      />
      <el-table-column
        v-if="statDimension === 'details'"
        label="签到时间"
        align="center"
        prop="attendanceTime"
        width="160"
      >
        <template slot-scope="scope">
          {{ scope.row.attendanceTime ? parseTime(scope.row.attendanceTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="statDimension === 'details'"
        label="签到方式"
        align="center"
        prop="attendanceMethod"
        width="100"
      />
      <el-table-column
        v-if="statDimension === 'details'"
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
import Vue from 'vue'
import * as echarts from 'echarts'
import { saveAs } from 'file-saver'
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
      loading: true,
      ids: [],
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
      statDimension: 'session',
      trendChart: null,
      distributionChart: null,
      sessionRankChart: null,
      timeStackChart: null,
      showCharts: true,
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
      },
      chartImages: {
        distribution: '',
        sessionRank: '',
        trend: '',
        timeStack: ''
      }
    };
  },
  created() {
    if (!Vue.prototype.$bus) Vue.prototype.$bus = new Vue()
    this.getSessionList();
    const end = new Date();
    const start = new Date();
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
    this.dateRange = [this.parseTime(start, '{y}-{m}-{d}'), this.parseTime(end, '{y}-{m}-{d}')];
    this.getList();
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts();
    });
  },
  beforeDestroy() {
    [this.trendChart, this.distributionChart, this.sessionRankChart, this.timeStackChart].forEach(chart => {
      if (chart) {
        try { chart.dispose() } catch(e){}
      }
    });
  },
  methods: {
    /** 查询课堂列表 */
    getSessionList() {
      listSession().then(response => {
        let payload = response;
        if (response && response.data) payload = response.data;
        let rows = payload && payload.rows ? payload.rows : (Array.isArray(payload) ? payload : []);
        this.sessionList = (rows || []).map(r => ({
          sessionId: r.sessionId || r.id || r.classId,
          className: r.className || r.name || r.title || r.class_name
        }));
      }).catch(err => {
        console.warn('加载课堂列表失败', err);
        this.sessionList = [];
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

      let apiMethod;
      switch (this.statDimension) {
        case 'session':
          apiMethod = sessionStatistics;
          break;
        case 'time':
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
        let payload = response;
        if (response && response.data) payload = response.data;

        // 处理不同的响应结构
        if (payload && payload.data) {
          payload = payload.data;
        } else if (payload && payload.rows) {
          payload = payload;
        }

        const normalized = this.normalizePayload(payload);
        this.total = normalized.total || (Array.isArray(normalized.list) ? normalized.list.length : 0);
        this.dataList = normalized.list || [];

        // 更新图表
        this.$nextTick(() => {
          this.initCharts();
          if (this.statDimension === 'time') {
            this.updateTimeCharts(normalized.list);
          } else if (this.statDimension === 'session') {
            this.updateSessionCharts(normalized.list);
          }

          // 生成图表图片用于打印
          this.generateChartImages();
        });

        // 广播筛选条件到驾驶舱
        if (Vue.prototype.$bus) {
          Vue.prototype.$bus.$emit('attendanceFiltersChanged', {
            params: params,
            statDimension: this.statDimension
          })
        }
        this.loading = false;
      }).catch(err => {
        console.error('查询失败', err);
        this.loading = false;
      });
    },

    /** 规范化后端数据 */
    normalizePayload(payload) {
      if (!payload) return { list: [], total: 0 };

      // 处理不同的响应结构
      let dataArray = [];
      if (Array.isArray(payload)) {
        dataArray = payload;
      } else if (payload.rows && Array.isArray(payload.rows)) {
        dataArray = payload.rows;
      } else if (payload.data && Array.isArray(payload.data)) {
        dataArray = payload.data;
      } else if (payload.statistics && Array.isArray(payload.statistics)) {
        dataArray = payload.statistics;
      }

      if (this.statDimension === 'details') {
        const list = dataArray.map(item => ({
          sessionId: item.sessionId || item.classId || '',
          className: item.className || item.class_name || '',
          studentName: item.studentName || item.student_name || item.name || '',
          studentNo: item.studentNo || item.student_no || '',
          attendanceMethod: item.attendanceMethod || item.method || (item.device_type || '未知'),
          attendanceTime: item.attendanceTime || item.signTime || '',
          attendanceStatus: item.attendanceStatus !== undefined ? item.attendanceStatus : (item.status !== undefined ? item.status : null),
          statusText: item.statusText || item.status_name ||
            (item.attendanceStatus === 0 ? '缺勤' :
              item.attendanceStatus === 1 ? '已签到' :
                item.attendanceStatus === 2 ? '迟到' :
                  item.attendanceStatus === 3 ? '请假' :
                    item.attendanceStatus === 4 ? '早退' : '未知')
        }));
        return { list, total: payload.total || list.length };
      }

      if (this.statDimension === 'time') {
        const list = dataArray.map(item => ({
          statDate: item.statDate || item.date || item.day || '',
          statWeek: item.statWeek || item.week || '',
          dailySigned: item.dailySigned || item.signed || item.attended || 0,
          dailyAbsent: item.dailyAbsent || item.absent || item.missing || 0,
          dailyLate: item.dailyLate || item.late || 0,
          attendanceRate: Number(item.attendanceRate || item.rate || 0)
        }));
        return { list, total: list.length };
      }

      // 课堂维度
      const list = dataArray.map(item => ({
        sessionId: item.sessionId || item.id || item.classId || '',
        className: item.className || item.class_name || item.name || item.title || '',
        totalStudents: item.totalStudents || item.total || item.studentCount || 0,
        signedCount: item.signedCount || item.signed || item.attended || 0,
        absentCount: item.absentCount || item.absent || item.missing || 0,
        lateCount: item.lateCount || item.late || item.tardy || 0,
        leaveCount: item.leaveCount || item.leave || 0,
        attendanceRate: Number(item.attendanceRate || item.rate || 0)
      }));
      return { list, total: payload.total || list.length };
    },

    /** 初始化图表 */
    initCharts() {
      try {
        const initChart = (refName, chartVar) => {
          if (this.$refs[refName]) {
            const dom = this.$refs[refName];
            // 先销毁现有实例
            const inst = echarts.getInstanceByDom(dom);
            if (inst) {
              echarts.dispose(dom);
            }
            this[chartVar] = echarts.init(dom);
            // 添加窗口大小变化监听
            window.addEventListener('resize', () => {
              try { this[chartVar].resize(); } catch (e) {}
            });
          }
        };

        initChart('trendChart', 'trendChart');
        initChart('distributionChart', 'distributionChart');
        initChart('sessionRankChart', 'sessionRankChart');
        initChart('timeStackChart', 'timeStackChart');
      } catch (e) {
        console.warn('初始化图表失败', e);
      }
    },

    /** 生成图表图片用于打印 */
    generateChartImages() {
      this.$nextTick(() => {
        setTimeout(() => {
          try {
            if (this.distributionChart) {
              this.chartImages.distribution = this.distributionChart.getDataURL({
                pixelRatio: 2,
                backgroundColor: '#fff'
              });
            }
            if (this.sessionRankChart) {
              this.chartImages.sessionRank = this.sessionRankChart.getDataURL({
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
            if (this.timeStackChart) {
              this.chartImages.timeStack = this.timeStackChart.getDataURL({
                pixelRatio: 2,
                backgroundColor: '#fff'
              });
            }
          } catch (e) {
            console.warn('生成图表图片失败', e);
          }
        }, 1000);
      });
    },

    /** 更新时间趋势维度图表 */
    updateTimeCharts(dataList) {
      if (!dataList || dataList.length === 0) {
        this.setEmptyChart(this.trendChart, '签到率趋势图');
        this.setEmptyChart(this.timeStackChart, '每日签到统计');
        return;
      }

      const dateList = dataList.map(item => item.statDate || item.statWeek);
      const rateList = dataList.map(item => item.attendanceRate || 0);
      const signedList = dataList.map(item => item.dailySigned || 0);
      const absentList = dataList.map(item => item.dailyAbsent || 0);
      const lateList = dataList.map(item => item.dailyLate || 0);

      // 更新趋势图
      if (this.trendChart) {
        const option = {
          title: {
            text: '签到率趋势图',
            left: 'center',
            textStyle: {
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          tooltip: {
            trigger: 'axis',
            formatter: (params) => {
              const data = params[0];
              const index = data.dataIndex;
              return `${data.name}<br/>签到率: ${data.value}%<br/>签到人数: ${signedList[index]}/${signedList[index] + absentList[index] + lateList[index]}`;
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
            data: dateList,
            axisLabel: {
              rotate: 45,
              interval: 0
            }
          },
          yAxis: {
            type: 'value',
            axisLabel: { formatter: '{value}%' },
            min: 0,
            max: 100
          },
          series: [{
            data: rateList,
            type: 'line',
            smooth: true,
            itemStyle: { color: '#5470c6' },
            lineStyle: { width: 3 },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(84, 112, 198, 0.3)' },
                { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
              ])
            }
          }]
        };
        this.trendChart.setOption(option, true);
      }

      // 更新时间堆叠图
      if (this.timeStackChart) {
        const option = {
          title: {
            text: '每日签到统计',
            left: 'center',
            textStyle: {
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' }
          },
          legend: {
            data: ['已签到', '迟到', '缺勤'],
            bottom: 0
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '10%',
            top: '15%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: dateList,
            axisLabel: {
              rotate: 45,
              interval: 0
            }
          },
          yAxis: { type: 'value' },
          series: [
            {
              name: '已签到',
              type: 'bar',
              stack: 'total',
              data: signedList,
              itemStyle: { color: '#67C23A' }
            },
            {
              name: '迟到',
              type: 'bar',
              stack: 'total',
              data: lateList,
              itemStyle: { color: '#E6A23C' }
            },
            {
              name: '缺勤',
              type: 'bar',
              stack: 'total',
              data: absentList,
              itemStyle: { color: '#F56C6C' }
            }
          ]
        };
        this.timeStackChart.setOption(option, true);
      }
    },

    /** 更新课堂统计维度图表 */
    updateSessionCharts(dataList) {
      if (!dataList || dataList.length === 0) {
        this.setEmptyChart(this.distributionChart, '课堂签到分布');
        this.setEmptyChart(this.sessionRankChart, '课堂签到率排名');
        return;
      }

      // 更新分布图（使用第一个课堂的数据）
      const sessionData = dataList[0];
      if (this.distributionChart && sessionData) {
        const option = {
          title: {
            text: '课堂签到分布',
            left: 'center',
            textStyle: {
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            top: 'middle'
          },
          series: [{
            name: '签到状态',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            data: [
              { value: sessionData.signedCount || 0, name: '已签到' },
              { value: sessionData.lateCount || 0, name: '迟到' },
              { value: sessionData.leaveCount || 0, name: '请假' },
              { value: sessionData.absentCount || 0, name: '缺勤' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            }
          }],
          color: ['#67C23A', '#E6A23C', '#409EFF', '#F56C6C']
        };
        this.distributionChart.setOption(option, true);
      }

      // 更新排名图
      if (this.sessionRankChart) {
        const classNames = dataList.map(item => item.className);
        const rates = dataList.map(item => item.attendanceRate || 0);

        const option = {
          title: {
            text: '课堂签到率排名',
            left: 'center',
            textStyle: {
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter: (params) => {
              const data = params[0];
              const session = dataList[data.dataIndex];
              return `${data.name}<br/>签到率: ${data.value}%<br/>总人数: ${session.totalStudents}<br/>已签到: ${session.signedCount}`;
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
            data: classNames,
            axisLabel: {
              interval: 0,
              rotate: 45
            }
          },
          yAxis: {
            type: 'value',
            axisLabel: { formatter: '{value}%' },
            min: 0,
            max: 100
          },
          series: [{
            data: rates,
            type: 'bar',
            itemStyle: {
              color: (params) => {
                const colorList = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'];
                return colorList[params.dataIndex % colorList.length];
              }
            },
            label: {
              show: true,
              position: 'top',
              formatter: '{c}%'
            }
          }]
        };
        this.sessionRankChart.setOption(option, true);
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
            textStyle: {
              color: '#999',
              fontSize: 16,
              fontWeight: 'normal'
            }
          },
          graphic: {
            type: 'text',
            left: 'center',
            top: '45%',
            style: {
              text: '暂无数据',
              fontSize: 14,
              fill: '#999'
            }
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
      this.resetForm("queryForm");
      this.handleQuery();
    },

    /** 维度变更 */
    handleDimensionChange() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.sessionId || item.id);
    },

    /** 导出按钮操作 */
    handleExport() {
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
      const filename = `考勤报表_${className}_${dateRangeText}.xlsx`;

      exportAttendanceReport(params, filename).then(() => {
        this.$message.success('导出任务已开始');
      }).catch(err => {
        console.error('导出失败', err);
        this.$message.error('导出失败: ' + (err.message || '请检查数据'));
      });
    },

    /** 打印功能 */
    handlePrint() {
      // 重新生成图表图片确保最新
      this.generateChartImages();

      setTimeout(() => {
        const printContent = this.generatePrintContent();
        const printWindow = window.open('', '_blank');
        printWindow.document.write(printContent);
        printWindow.document.close();
        printWindow.print();
      }, 500);
    },

    /** 生成打印内容 */
    generatePrintContent() {
      const title = '考勤报表';
      const now = this.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}');
      const userName = (this.$store && this.$store.state.user && this.$store.state.user.userName) || '当前用户';

      let tableContent = '';
      let chartContent = '';

      if (this.statDimension === 'session') {
        tableContent = this.generateSessionTable();
        chartContent = this.generateSessionChartsForPrint();
      } else if (this.statDimension === 'time') {
        tableContent = this.generateTimeTable();
        chartContent = this.generateTimeChartsForPrint();
      } else {
        tableContent = this.generateDetailsTable();
      }

      return `
        <!DOCTYPE html>
        <html>
        <head>
          <title>${title}</title>
          <style>
            body { font-family: Arial, sans-serif; margin: 20px; }
            .header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 10px; }
            .meta { margin-bottom: 15px; color: #666; }
            .chart-section { margin: 20px 0; }
            .chart-container { text-align: center; margin: 15px 0; }
            .chart-title { font-weight: bold; margin-bottom: 10px; font-size: 16px; }
            .chart-image { max-width: 100%; height: auto; border: 1px solid #ddd; }
            table { width: 100%; border-collapse: collapse; margin-top: 10px; }
            table, th, td { border: 1px solid #ddd; }
            th, td { padding: 8px; text-align: center; }
            th { background-color: #f5f7fa; font-weight: bold; }
            .no-data { text-align: center; color: #999; padding: 20px; }
            @media print {
              body { margin: 0; }
              .no-print { display: none; }
              .chart-image { max-width: 90% !important; }
            }
          </style>
        </head>
        <body>
          <div class="header">
            <h2>${title}</h2>
          </div>
          <div class="meta">
            <div><strong>统计时间:</strong> ${this.dateRange && this.dateRange.length ? this.dateRange[0] + ' 至 ' + this.dateRange[1] : '全部'}</div>
            <div><strong>生成人:</strong> ${userName} | <strong>生成时间:</strong> ${now}</div>
            <div><strong>统计维度:</strong> ${this.getDimensionText()}</div>
            ${this.queryParams.sessionId ? `<div><strong>课堂:</strong> ${(this.sessionList.find(s => s.sessionId === this.queryParams.sessionId) || {}).className || '未知'}</div>` : ''}
          </div>

          ${chartContent}

          <div class="table-section">
            <h3>数据表格</h3>
            ${tableContent}
          </div>
        </body>
        </html>
      `;
    },

    /** 生成课堂统计图表打印内容 */
    generateSessionChartsForPrint() {
      if (!this.dataList || this.dataList.length === 0) {
        return '<div class="no-data">暂无图表数据</div>';
      }

      return `
        <div class="chart-section">
          <h3>统计图表</h3>
          <div class="chart-container">
            <div class="chart-title">课堂签到分布</div>
            <img src="${this.chartImages.distribution}" class="chart-image" alt="课堂签到分布图">
          </div>
          <div class="chart-container">
            <div class="chart-title">课堂签到率排名</div>
            <img src="${this.chartImages.sessionRank}" class="chart-image" alt="课堂签到率排名图">
          </div>
        </div>
      `;
    },

    /** 生成时间趋势图表打印内容 */
    generateTimeChartsForPrint() {
      if (!this.dataList || this.dataList.length === 0) {
        return '<div class="no-data">暂无图表数据</div>';
      }

      return `
        <div class="chart-section">
          <h3>统计图表</h3>
          <div class="chart-container">
            <div class="chart-title">签到率趋势图</div>
            <img src="${this.chartImages.trend}" class="chart-image" alt="签到率趋势图">
          </div>
          <div class="chart-container">
            <div class="chart-title">每日签到统计</div>
            <img src="${this.chartImages.timeStack}" class="chart-image" alt="每日签到统计图">
          </div>
        </div>
      `;
    },

    /** 生成课堂统计表格 */
    generateSessionTable() {
      if (!this.dataList || this.dataList.length === 0) {
        return '<div class="no-data">暂无数据</div>';
      }

      return `
        <table>
          <thead>
            <tr>
              <th>课堂名称</th>
              <th>总人数</th>
              <th>已签到</th>
              <th>缺勤</th>
              <th>迟到</th>
              <th>请假</th>
              <th>签到率</th>
            </tr>
          </thead>
          <tbody>
            ${this.dataList.map(item => `
              <tr>
                <td>${item.className}</td>
                <td>${item.totalStudents}</td>
                <td>${item.signedCount}</td>
                <td>${item.absentCount}</td>
                <td>${item.lateCount}</td>
                <td>${item.leaveCount}</td>
                <td>${item.attendanceRate.toFixed(1)}%</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    },

    /** 生成时间统计表格 */
    generateTimeTable() {
      if (!this.dataList || this.dataList.length === 0) {
        return '<div class="no-data">暂无数据</div>';
      }

      return `
        <table>
          <thead>
            <tr>
              <th>统计日期</th>
              <th>当日签到</th>
              <th>当日缺勤</th>
              <th>当日迟到</th>
              <th>签到率</th>
            </tr>
          </thead>
          <tbody>
            ${this.dataList.map(item => `
              <tr>
                <td>${item.statDate || item.statWeek}</td>
                <td>${item.dailySigned}</td>
                <td>${item.dailyAbsent}</td>
                <td>${item.dailyLate}</td>
                <td>${item.attendanceRate.toFixed(1)}%</td>
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
                <td>${item.studentName}</td>
                <td>${item.studentNo}</td>
                <td>${item.className}</td>
                <td>${item.attendanceTime ? this.parseTime(item.attendanceTime) : '-'}</td>
                <td>${item.attendanceMethod}</td>
                <td>${item.statusText}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    },

    /** 获取维度文本 */
    getDimensionText() {
      const dimensionMap = {
        'session': '课堂统计',
        'time': '时间趋势',
        'details': '明细查询'
      };
      return dimensionMap[this.statDimension] || '课堂统计';
    },

    /** 格式化签到率显示 */
    formatRate(row) {
      return `${row.attendanceRate.toFixed(1)}% (${row.signedCount}/${row.totalStudents})`;
    },

    /** 获取签到率类型 */
    getRateType(rate) {
      if (rate >= 90) return 'success';
      if (rate >= 70) return 'warning';
      return 'danger';
    },

    /** 获取状态类型 */
    getStatusType(status) {
      const statusMap = {
        0: 'danger',   // 缺勤
        1: 'success',  // 已签到
        2: 'warning',  // 迟到
        3: 'info',     // 请假
        4: 'info'      // 早退
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
      return row.sessionId || row.studentNo || row.id || JSON.stringify(row);
    },

    rowClassName({ rowIndex }) {
      return rowIndex % 2 === 1 ? 'striped-row' : '';
    },
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
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.chart-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: center;
  color: #333;
}

.el-table th {
  background: #f7f9fb !important;
  color: #333;
  font-weight: 600;
}
.striped-row {
  background: #fafafa;
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
