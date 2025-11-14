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
          <el-radio-button label="session">课堂维度</el-radio-button>
          <el-radio-button label="time">时间维度</el-radio-button>
          <el-radio-button label="details">明细展示</el-radio-button>
          <el-radio-button label="weekly">周报表</el-radio-button>
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
      <!-- 签到率趋势图 -->
      <el-row :gutter="20" style="margin-bottom: 20px;" v-if="statDimension === 'time'">
        <el-col :span="24">
          <div class="chart-wrapper">
            <div class="chart-title">签到率趋势图</div>
            <div ref="trendChart" style="height: 400px;"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 课堂签到分布和趋势图 -->
      <el-row :gutter="20" style="margin-bottom: 20px;" v-if="statDimension === 'session' && dataList.length > 0">
        <el-col :span="12">
          <div class="chart-wrapper">
            <div class="chart-title">课堂签到分布</div>
            <div ref="distributionChart" style="height: 400px;"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-wrapper">
            <div class="chart-title">签到状态统计</div>
            <div ref="statusChart" style="height: 400px;"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 周考勤汇总图 -->
      <div v-if="statDimension === 'weekly'" class="chart-wrapper">
        <div class="chart-title">周考勤汇总</div>
        <div ref="weeklyChart" style="height: 400px;"></div>
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

      <!-- 课堂维度列 -->
      <el-table-column
        v-if="statDimension !== 'details'"
        label="课堂名称"
        align="center"
        min-width="120"
        show-overflow-tooltip
      >
        <template slot-scope="scope">
          {{ scope.row.className || scope.row.class_name || (scope.row.session && (scope.row.session.className || scope.row.session.class_name)) || scope.row.classTitle || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="statDimension !== 'details'"
        label="总人数"
        align="center"
        width="90"
      >
        <template slot-scope="scope">
          {{ getVal(scope.row, ['totalStudents','total','studentCount','total_students']) || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="statDimension !== 'details'"
        label="已签到"
        align="center"
        width="90"
      >
        <template slot-scope="scope">
          {{ getVal(scope.row, ['signedCount','signed','attended','signCount']) || 0 }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="statDimension !== 'details'"
        label="缺勤人数"
        align="center"
        width="90"
      >
        <template slot-scope="scope">
          {{ getVal(scope.row, ['absentCount','absent','missing']) || 0 }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="statDimension !== 'details'"
        label="迟到人数"
        align="center"
        width="90"
      >
        <template slot-scope="scope">
          {{ getVal(scope.row, ['lateCount','late','tardy']) || 0 }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="statDimension !== 'details'"
        label="签到率"
        align="center"
        sortable
        width="100"
      >
        <template slot-scope="scope">
          <el-tag :type="getRateType(Number(getVal(scope.row, ['attendanceRate','avgAttendanceRate','rate']) || 0))" effect="dark">
            {{ formatRate(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 时间维度列 -->
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
      <el-table-column v-if="statDimension === 'time'" label="当日签到" align="center" width="100">
        <template slot-scope="scope">
          {{ getVal(scope.row, ['dailySigned','signed','attended','signCount']) || 0 }}
        </template>
      </el-table-column>
      <el-table-column v-if="statDimension === 'time'" label="当日缺勤" align="center" width="100">
        <template slot-scope="scope">
          {{ getVal(scope.row, ['dailyAbsent','absent','missing']) || 0 }}
        </template>
      </el-table-column>

      <!-- 明细展示列 -->
      <el-table-column
        v-if="statDimension === 'details'"
        label="学生姓名"
        align="center"
        width="140"
      >
        <template slot-scope="scope">
          {{ scope.row.studentName || scope.row.student_name || scope.row.name || '-' }}
        </template>
      </el-table-column>
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
      >
        <template slot-scope="scope">
          {{ scope.row.className || scope.row.class_name || (scope.row.session && (scope.row.session.className || scope.row.session.class_name)) || scope.row.classTitle || '-' }}
        </template>
      </el-table-column>
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

      <!-- 周报列 -->
      <el-table-column
        v-if="statDimension === 'weekly'
        "
        label="平均签到率"
        align="center"
        prop="avgAttendanceRate"
        sortable
        width="120"
      >
        <template slot-scope="scope">
          <el-tag :type="getRateType(Number(normalizeRate(scope.row.avgAttendanceRate || scope.row.attendanceRate || 0)))" effect="dark">
            {{ (Number(normalizeRate(scope.row.avgAttendanceRate || scope.row.attendanceRate || 0))).toFixed(1) + '%' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        v-if="statDimension === 'weekly'"
        label="缺勤排名"
        align="center"
        prop="absenceRank"
        width="100"
        v-show="showAbsenceRank"
      >
        <template slot-scope="scope">
          <span v-if="scope.row.absenceRank">第{{ scope.row.absenceRank }}名</span>
          <span v-else>-</span>
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
  weeklyReport,
  exportAttendanceReport,
  exportAttendanceReportForm
} from "@/api/proj_fz/attendanceReport";

export default {
  name: "AttendanceReport",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 总条数
      total: 0,
      // 表格数据
      dataList: [],
      // 课堂列表
      sessionList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        sessionId: undefined,
        attendanceStatus: undefined,
        groupBy: 'day'
      },
      // 时间范围
      dateRange: [],
      // 统计维度
      statDimension: 'session',
      // 图表实例
      trendChart: null,
      distributionChart: null,
      statusChart: null,
      weeklyChart: null,
      // 是否显示图表
      showCharts: true,
      // 显示缺勤排名（周报时）
      showAbsenceRank: false,
      // 日期选择器配置
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
    // 初始化事件总线
    if (!Vue.prototype.$bus) Vue.prototype.$bus = new Vue()
    this.getSessionList();
    // 设置默认时间范围为最近一周
    const end = new Date();
    const start = new Date();
    start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
    this.dateRange = [this.parseTime(start, '{y}-{m}-{d}'), this.parseTime(end, '{y}-{m}-{d}')];
    // 如果周一，自动尝试生成上周周报（前端触发查询）
    this.autoGenerateWeeklyIfMonday();
    this.getList();
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts();
    });
  },
  beforeDestroy() {
    [this.trendChart, this.distributionChart, this.statusChart, this.weeklyChart].forEach(chart => {
      if (chart) {
        chart.dispose();
      }
    });
  },
  methods: {
    // 将 0-1 比例或 0-100 值规范为 0-100
    normalizeRate(v) {
      if (v === undefined || v === null) return 0;
      const n = Number(v);
      if (Number.isNaN(n)) return 0;
      if (n > 0 && n <= 1) return +(n * 100);
      return n;
    },
    /** 查询课堂列表 */
    getSessionList() {
      listSession().then(response => {
        let payload = response;
        if (response && response.data) payload = response.data;
        let rows = payload && payload.rows ? payload.rows : (Array.isArray(payload) ? payload : []);
        // 标准化 sessionId/className
        this.sessionList = (rows || []).map(r => ({ sessionId: r.sessionId || r.id || r.classId, className: r.className || r.name || r.title || r.class_name }));
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
          params.groupBy = 'day'; // 默认按日统计
          apiMethod = timeStatistics;
          break;
        case 'details':
          apiMethod = attendanceDetails;
          break;
        case 'weekly':
          apiMethod = weeklyReport;
          break;
        default:
          apiMethod = sessionStatistics;
      }

      apiMethod(params).then(response => {
        // 处理后端可能的 wrapper：response 可能是 { code, msg, data } 或 data 本身
        let payload = response;
        if (response && response.data) payload = response.data;
        // 进一步解包一次（有些后端会返回 { data: { rows: ... } }）
        if (payload && payload.data) payload = payload.data;
         // 规范化 payload 为统一 dataList 结构
         const normalized = this.normalizePayload(payload);
         this.total = normalized.total || (Array.isArray(normalized.list) ? normalized.list.length : 0);
         this.dataList = normalized.list || [];

        if (this.statDimension === 'time') {
          // 构造 chartData：dateList/rateList/signedList/absentList
          const dateList = (normalized.list || []).map(i => i.statDate || '');
          const rateList = (normalized.list || []).map(i => Number(i.attendanceRate || 0));
          const signedList = (normalized.list || []).map(i => Number(i.dailySigned || 0));
          const absentList = (normalized.list || []).map(i => Number(i.dailyAbsent || 0));
          const chartData = { dateList, rateList, signedList, absentList };
          this.$nextTick(() => { this.initCharts(); this.updateCharts(chartData); });
        } else if (this.statDimension === 'weekly') {
          this.showAbsenceRank = (payload && payload.topAbsenceStudents && payload.topAbsenceStudents.length > 0) || false;
          // 构造 weeklyData 结构以供 updateWeeklyCharts 使用
          const weeklyData = { weeklyData: (normalized.list || []).map(i => ({ className: i.className, avgAttendanceRate: i.avgAttendanceRate || 0 })) };
          this.$nextTick(() => { this.initCharts(); this.updateWeeklyCharts(weeklyData); });
        } else if (this.statDimension === 'details') {
          // nothing chart-related
        } else {
          this.$nextTick(() => { this.initCharts(); this.updateSessionCharts(); });
        }

        // 广播筛选条件
        if (Vue.prototype.$bus) {
          Vue.prototype.$bus.$emit('attendanceFiltersChanged', { params: params, statDimension: this.statDimension })
        }
        this.loading = false;
      }).catch(err => {
        console.error('查询失败', err);
        this.loading = false;
      });
    },

    // 规范化后端各种字段为前端统一字段结构
    normalizePayload(payload) {
      if (!payload) return { list: [], total: 0 };
      const normRate = v => {
        if (v === undefined || v === null) return 0;
        const n = Number(v);
        if (Number.isNaN(n)) return 0;
        if (n > 0 && n <= 1) return +(n * 100);
        return n;
      };

      // 如果调用上下文有 statDimension，优先使用它来决定解析策略
      const dim = (this && this.statDimension) ? this.statDimension : null;

      // 明细优先判定（当用户选择明细时）
      if (dim === 'details' || (payload.rows && (payload.rows.length && (payload.rows[0].studentName || payload.rows[0].studentNo || payload.rows[0].name)))) {
        const rows = payload.rows || (Array.isArray(payload) ? payload : []);
        const list = rows.map(item => ({
          sessionId: item.sessionId || item.classId || (item.session && (item.session.sessionId || item.session.id)) || item.id || '',
          className: item.className || item.class_name || (item.session && (item.session.className || item.session.class_name)) || item.classTitle || item.title || '',
          statDate: item.statDate || item.attendanceDate || item.date || item.day || '',
          studentName: item.studentName || item.student_name || item.name || item.student || '',
          studentNo: item.studentNo || item.student_no || item.no || item.sno || '',
          attendanceMethod: item.attendanceMethod || item.method || item.channel || item.signMethod || '',
          attendanceTime: item.attendanceTime || item.signTime || item.time || item.timestamp || '',
          attendanceStatus: item.attendanceStatus !== undefined ? item.attendanceStatus : (item.status !== undefined ? item.status : null),
          statusText: item.statusText || item.status_name || item.statusLabel || (item.attendanceStatus === 0 ? '缺勤' : (item.attendanceStatus === 1 ? '已签到' : '')) || ''
        }));
        return { list, total: payload.total || rows.length };
      }

      // 时间维度（按日/周）
      if (dim === 'time' || payload.statistics || (Array.isArray(payload) && payload.length && (payload[0].statDate || payload[0].date))) {
        const arr = payload.statistics || payload.rows || payload;
        const list = (Array.isArray(arr) ? arr : []).map(item => ({
          statDate: item.statDate || item.date || item.day || item.label || '',
          dailySigned: item.dailySigned || item.signed || item.attended || item.signCount || 0,
          dailyAbsent: item.dailyAbsent || item.absent || item.missing || 0,
          attendanceRate: normRate(item.attendanceRate !== undefined ? item.attendanceRate : (item.rate !== undefined ? item.rate : 0))
        }));
        return { list, total: list.length };
      }

      // 周报
      if (dim === 'weekly' || payload.weeklyData || (Array.isArray(payload) && payload.length && payload[0].avgAttendanceRate !== undefined && payload[0].className)) {
        const arr = payload.weeklyData || payload.rows || payload;
        const list = (Array.isArray(arr) ? arr : []).map(item => ({
          sessionId: item.sessionId || item.id || item.classId || '',
          className: item.className || item.name || item.class_title || item.title || '',
          avgAttendanceRate: normRate(item.avgAttendanceRate !== undefined ? item.avgAttendanceRate : (item.attendanceRate !== undefined ? item.attendanceRate : 0)),
          absenceRank: item.absenceRank || item.rank || item.absenceRank || item.rankNo || null
        }));
        return { list, total: payload.total || list.length };
      }

      // 课堂维度
      if (dim === 'session' || payload.rows || (Array.isArray(payload) && payload.length && (payload[0].signedCount !== undefined || payload[0].totalStudents !== undefined || payload[0].signed !== undefined))) {
        const rows = payload.rows || payload;
        const list = (Array.isArray(rows) ? rows : []).map(item => ({
           sessionId: item.sessionId || item.id || item.classId || '',
           className: item.className || item.class_name || item.name || item.title || item.classTitle || '',
           totalStudents: item.totalStudents || item.total || item.studentCount || item.total_students || item.capacity || 0,
           signedCount: item.signedCount || item.signed || item.attended || item.signCount || item.present || 0,
           absentCount: item.absentCount || item.absent || item.missing || (item.totalStudents ? (item.totalStudents - (item.signedCount || item.signed || item.attended || 0)) : 0) || 0,
           lateCount: item.lateCount || item.late || item.tardy || item.late_num || 0,
           attendanceRate: normRate(item.attendanceRate !== undefined ? item.attendanceRate : (item.rate !== undefined ? item.rate : 0))
         }));
         return { list, total: payload.total || list.length };
       }

      // 默认回退：如果 payload 是数组
      if (Array.isArray(payload)) {
        return { list: payload, total: payload.length };
      }
      return { list: [], total: 0 };
    },

    /** 初始化图表 */
    initCharts() {
      // 使用 getInstanceByDom 避免重复初始化导致控制台警告
      try {
        if (this.$refs.trendChart) {
          const dom = this.$refs.trendChart;
          const inst = echarts.getInstanceByDom(dom);
          if (inst) {
            this.trendChart = inst;
          } else {
            this.trendChart = echarts.init(dom);
          }
          try { this.trendChart.resize(); } catch (e) {}
        }
        if (this.$refs.distributionChart) {
          const dom = this.$refs.distributionChart;
          const inst = echarts.getInstanceByDom(dom);
          if (inst) {
            this.distributionChart = inst;
          } else {
            this.distributionChart = echarts.init(dom);
          }
          try { this.distributionChart.resize(); } catch (e) {}
        }
        if (this.$refs.statusChart) {
          const dom = this.$refs.statusChart;
          const inst = echarts.getInstanceByDom(dom);
          if (inst) {
            this.statusChart = inst;
          } else {
            this.statusChart = echarts.init(dom);
          }
          try { this.statusChart.resize(); } catch (e) {}
        }
        if (this.$refs.weeklyChart) {
          const dom = this.$refs.weeklyChart;
          const inst = echarts.getInstanceByDom(dom);
          if (inst) {
            this.weeklyChart = inst;
          } else {
            this.weeklyChart = echarts.init(dom);
          }
          try { this.weeklyChart.resize(); } catch (e) {}
        }
      } catch (e) {
        // ignore
      }
      return Promise.resolve();
    },
    /** 更新图表数据 */
    updateCharts(chartData) {
      this.$nextTick(() => {
        // 更新趋势图
        if (chartData.dateList && chartData.rateList && this.trendChart) {
          const trendOption = {
            color: ['#409EFF'],
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
              formatter: function(params) {
                const data = params[0];
                const signed = chartData.signedList[data.dataIndex] || 0;
                const absent = chartData.absentList[data.dataIndex] || 0;
                const total = signed + absent;
                return `${data.name}<br/>签到率: ${data.value}%<br/>签到人数: ${signed}/${total}`;
              }
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'category',
              data: chartData.dateList,
              axisLabel: {
                rotate: 45
              }
            },
            yAxis: {
              type: 'value',
              axisLabel: {
                formatter: '{value}%'
              },
              min: 0,
              max: 100
            },
            series: [{
              name: '签到率',
              data: chartData.rateList,
              type: 'line',
              smooth: true,
              itemStyle: {
                color: '#5470c6'
              },
              lineStyle: {
                width: 3
              },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(64,158,255,0.45)' },
                  { offset: 1, color: 'rgba(64,158,255,0.08)' }
                ])
              },
              markPoint: {
                data: [
                  { type: 'max', name: '最大值' },
                  { type: 'min', name: '最小值' }
                ]
              }
            }]
          };
          this.trendChart.clear();
          this.trendChart.setOption(trendOption);
          try { this.trendChart.resize(); } catch (e) {}
        }
      });
    },
    /** 更新课堂维度图表 */
    updateSessionCharts() {
      this.$nextTick(() => {
        if (this.dataList && this.dataList.length > 0 && this.distributionChart && this.statusChart) {
          // 优先使用用户选择的课堂
          let sessionData = null;
          if (this.queryParams && this.queryParams.sessionId) {
            sessionData = this.dataList.find(it => String(it.sessionId) === String(this.queryParams.sessionId) || String(it.id) === String(this.queryParams.sessionId));
          }
          if (!sessionData) sessionData = this.dataList[0];

           // 更新分布图
           const distributionOption = {
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
                { value: sessionData.absentCount || 0, name: '缺勤' },
                { value: sessionData.lateCount || 0, name: '迟到' }
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
            color: ['#409EFF', '#FF9900', '#909399']
           };
           this.distributionChart.setOption(distributionOption);
           try { this.distributionChart.resize(); } catch (e) {}

           // 更新状态统计图
           const statusData = [
             { value: sessionData.signedCount || 0, name: '已签到' },
             { value: sessionData.absentCount || 0, name: '缺勤' },
             { value: sessionData.lateCount || 0, name: '迟到' }
           ];
           const statusOption = {
             title: {
               text: '签到状态统计',
               left: 'center',
               textStyle: {
                 fontSize: 16,
                 fontWeight: 'bold'
               }
             },
             tooltip: {
               trigger: 'axis',
               axisPointer: {
                 type: 'shadow'
               }
             },
             xAxis: {
               type: 'category',
               data: statusData.map(item => item.name)
             },
             yAxis: {
               type: 'value'
             },
             series: [{
               data: statusData.map(item => ({ value: item.value })),
               type: 'bar',
               barWidth: '60%'
             }]
           };
           this.statusChart.setOption(statusOption);
           try { this.statusChart.resize(); } catch (e) {}
         }
       });
     },
    /** 更新周报图表 */
    updateWeeklyCharts(weeklyData) {
      this.$nextTick(() => {
        if (weeklyData.weeklyData && this.weeklyChart) {
          const classNames = weeklyData.weeklyData.map(item => item.className);
          const rates = weeklyData.weeklyData.map(item => item.avgAttendanceRate || 0);

          const weeklyOption = {
            title: {
              text: '各课堂平均签到率',
              left: 'center',
              textStyle: {
                fontSize: 16,
                fontWeight: 'bold'
              }
            },
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              },
              formatter: function(params) {
                return `${params[0].name}<br/>平均签到率: ${params[0].value}%`;
              }
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
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
              axisLabel: {
                formatter: '{value}%'
              },
              min: 0,
              max: 100
            },
            series: [{
              data: rates,
              type: 'bar',
              itemStyle: {
                color: function(params) {
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
          this.weeklyChart.setOption(weeklyOption);
          try { this.weeklyChart.resize(); } catch (e) {}
        }
      });
    },
    /** 统计维度变化 */
    handleDimensionChange() {
      this.showCharts = this.statDimension !== 'details';
      this.queryParams.pageNum = 1;
      this.getList();
      // 等待 DOM 更新后确保图表已初始化（切换到某个维度时可能需要初始化新 ref）
      this.$nextTick(() => {
        this.initCharts();
      });
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
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.attendanceId);
    },
    /** 导出按钮操作：尝试使用后端返回的文件 blob 导出；若后端未返回文件则拉取全部数据并前端导出 */
    async handleExport() {
      const params = {
        sessionId: this.queryParams.sessionId,
        startDate: this.dateRange && this.dateRange.length ? this.dateRange[0] : undefined,
        endDate: this.dateRange && this.dateRange.length ? this.dateRange[1] : undefined,
        exportType: this.statDimension === 'details' ? 'details' : (this.statDimension === 'time' ? 'time' : (this.statDimension === 'weekly' ? 'weekly' : 'session')),
        attendanceStatus: this.queryParams.attendanceStatus
      };
      if (!params.exportType) params.exportType = 'session';
      const className = this.queryParams.sessionId ? (this.sessionList.find(s => s.sessionId === this.queryParams.sessionId) || {}).className : '全部课堂';
      const dateRangeText = params.startDate && params.endDate ? `${params.startDate.replace(/-/g,'')}-${params.endDate.replace(/-/g,'')}` : this.getCurrentTime();
      const filename = `考勤报表_${className}_${dateRangeText}.xlsx`;

      // 优先尝试后端 download helper（会携带 axios 的 header/认证信息）
      try {
        await exportAttendanceReport(params, filename);
        this.$message.success('已开始导出，下载将在浏览器中触发');
        return;
      } catch (e) {
        console.warn('download helper 导出失败，尝试其它方式', e);
      }

      // 然后尝试 fetch POST 获取 blob 并保存（稳健回退）
      try {
        await this.postExportFetch(params, filename);
        return;
      } catch (e) {
        console.warn('fetch 导出失败，尝试表单提交回退', e);
      }

      // 尝试在新窗口提交表单（兼容不支持直接带 header 的环境）
      try {
        this.submitFormDownload('/proj_fz/attendanceReport/export', params);
        this.$message.info('已通过表单方式在新窗口触发下载');
        return;
      } catch (e) {
        console.warn('表单下载尝试失败，继续其它方式', e);
      }

      // 回退：尝试浏览器 GET 导出（部分后端仅支持 GET/Query）
      try {
        const buildQuery = (obj) => {
          const parts = [];
          for (const k in obj) {
            if (obj[k] === undefined || obj[k] === null) continue;
            parts.push(encodeURIComponent(k) + '=' + encodeURIComponent(obj[k]));
          }
          return parts.join('&');
        };
        const base = (process && process.env && process.env.VUE_APP_BASE_API) ? process.env.VUE_APP_BASE_API : '';
        const url = `${base}/proj_fz/attendanceReport/export?${buildQuery(params)}`;
        window.open(url, '_blank');
        this.$message.info('已尝试通过浏览器直接下载（如未自动开始，请检查后端接口是否支持 GET 导出）');
        return;
      } catch (e) {
        console.warn('浏览器直接下载回退失败', e);
      }

      // 最后回退：前端构建表格并导出（xls）
      try {
        const fetchParams = { ...this.queryParams, startDate: params.startDate, endDate: params.endDate };
        let fullDetails = [];
        try {
          const resp = await attendanceDetails({ ...fetchParams, pageNum: 1, pageSize: 100000 });
          fullDetails = resp.rows || resp.data || resp;
          if (resp.data && resp.data.rows) fullDetails = resp.data.rows;
        } catch (e) { fullDetails = this.dataList || []; }

        let fullSummary = [];
        try {
          if (this.statDimension === 'time') {
            const resp = await timeStatistics({ ...fetchParams, groupBy: 'day', pageNum: 1, pageSize: 100000 });
            fullSummary = resp.data ? (resp.data.statistics || resp.data || resp.statistics) : (resp.rows || resp);
            if (resp.data && resp.data.statistics) fullSummary = resp.data.statistics;
          } else if (this.statDimension === 'weekly') {
            const resp = await weeklyReport({ startDate: params.startDate, endDate: params.endDate });
            fullSummary = resp.data ? (resp.data.weeklyData || resp.weeklyData || resp.rows || []) : (resp.weeklyData || resp.rows || []);
          } else {
            const resp = await sessionStatistics({ ...fetchParams, pageNum: 1, pageSize: 100000 });
            fullSummary = resp.rows || resp.data || resp;
            if (resp.data && resp.data.rows) fullSummary = resp.data.rows;
          }
        } catch (e) { fullSummary = this.dataList || []; }

        if ((!fullSummary || fullSummary.length === 0) && (!fullDetails || fullDetails.length === 0)) fullSummary = this.dataList || [];

        const summaryHtml = this.buildSummaryTableHtmlFrom(fullSummary);
        const detailHtml = this.buildDetailTableHtmlFrom(fullDetails);

        const html = `<!DOCTYPE html><html><head><meta charset="utf-8" /><style>table{border-collapse:collapse;width:100%;font-size:12px}th,td{border:1px solid #ddd;padding:6px;text-align:center}th{background:#f5f7fa}</style></head><body><h2>${filename.replace(/\.xlsx?$|\.xls?$/,'')}</h2><h3>统计汇总</h3>${summaryHtml}<div style="height:16px"></div><h3>明细数据</h3>${detailHtml}</body></html>`;
        const blob = new Blob([html], { type: 'application/vnd.ms-excel' });
        saveAs(blob, filename.replace(/xlsx?$/, 'xls'));
        this.$message.success('已生成导出文件（xls 格式）');
      } catch (e) {
        console.error('前端导出失败', e);
        this.$message.error('导出失败：' + (e && e.message));
      }
    },

    // 改进：更完整、更好看的表格构建器
    buildSummaryTableHtmlFrom(list) {
      if (!list || list.length === 0) return '<div>无统计数据</div>';
      // 根据维度选择列
      if (this.statDimension === 'session' || this.statDimension === 'weekly') {
        const headers = ['课堂ID','课堂名称','总人数','已签到','缺勤','迟到','签到率'];
        const rows = list.map(r => {
          const rate = this.normalizeRate(r.attendanceRate !== undefined ? r.attendanceRate : (r.avgAttendanceRate !== undefined ? r.avgAttendanceRate : 0));
          const total = r.totalStudents || r.total || 0;
          const signed = r.signedCount || r.signed || 0;
          const absent = r.absentCount || r.absent || (total ? (total - signed) : 0);
          const late = r.lateCount || r.late || 0;
          return `<tr><td>${r.sessionId||r.id||''}</td><td style="text-align:left">${r.className||r.class_name||r.classTitle||r.title||''}</td><td>${total}</td><td>${signed}</td><td>${absent}</td><td>${late}</td><td>${Number(rate).toFixed(1)}%</td></tr>`
        }).join('');
        return `<table><thead><tr>${headers.map(h=>`<th>${h}</th>`).join('')}</tr></thead><tbody>${rows}</tbody></table>`;
      }
      if (this.statDimension === 'time') {
        const headers = ['日期','签到人数','缺勤','签到率'];
        const rows = list.map(r => {
          const rate = this.normalizeRate(r.attendanceRate || 0);
          const signed = r.dailySigned || r.signed || 0;
          const absent = r.dailyAbsent || r.absent || 0;
          return `<tr><td>${r.statDate||r.date||''}</td><td>${signed}</td><td>${absent}</td><td>${Number(rate).toFixed(1)}%</td></tr>`
        }).join('');
        return `<table><thead><tr>${headers.map(h=>`<th>${h}</th>`).join('')}</tr></thead><tbody>${rows}</tbody></table>`;
      }
      return '<div>无可用统计数据</div>';
    },

    buildDetailTableHtmlFrom(list) {
      if (!list || list.length === 0) return '<div>无明细数据</div>';
      const headers = ['课堂ID','课堂名称','日期','学生姓名','学号','签到方式','签到时间','状态'];
      const rows = list.map(r => {
        const timeText = r.attendanceTime ? (typeof r.attendanceTime === 'number' || /^[0-9]+$/.test(String(r.attendanceTime)) ? this.parseTime(r.attendanceTime) : r.attendanceTime) : '';
        return `<tr><td>${r.sessionId||r.classId||''}</td><td style="text-align:left">${r.className||r.class_name||''}</td><td>${r.statDate||r.attendanceDate||r.date||''}</td><td>${r.studentName||r.student_name||r.name||''}</td><td>${r.studentNo||r.student_no||''}</td><td>${r.attendanceMethod||r.method||''}</td><td>${timeText || '-'}</td><td>${r.statusText||r.status||''}</td></tr>`
      }).join('');
      return `<table><thead><tr>${headers.map(h=>`<th>${h}</th>`).join('')}</tr></thead><tbody>${rows}</tbody></table>`;
    },

    // 打印使用的包装函数：从当前 dataList 构建表格 HTML
    buildSummaryTableHtml() {
      return this.buildSummaryTableHtmlFrom(this.dataList || []);
    },

    buildDetailTableHtml() {
      return this.buildDetailTableHtmlFrom(this.dataList || []);
    },

    // 将签到率和已签到/总人数整合成友好展示文本，如 "92.0% (36/39)"
    formatRate(row) {
      const raw = row.attendanceRate !== undefined ? row.attendanceRate : (row.avgAttendanceRate !== undefined ? row.avgAttendanceRate : 0);
      const rateNum = this.normalizeRate(raw);
      const signed = row.signedCount || row.signed || row.signedStudents || row.signed_num || 0;
      let total = row.totalStudents || row.total || row.studentTotal || row.total_students || 0;
      // 如果没有 total，但有 signed 和 rate，尝试推算 total
      if ((!total || total === 0) && signed && rateNum) {
        total = Math.round((signed * 100) / (rateNum || 1));
      }
      const rateText = Number.isFinite(rateNum) ? Number(rateNum).toFixed(1) + '%' : '-';
      if (signed || total) {
        const s = signed || 0;
        const t = total || s;
        return `${rateText} (${s}/${t})`;
      }
      return rateText;
    },

    /** 获取当前时间用于导出文件名 */
    getCurrentTime() {
      const now = new Date();
      return this.parseTime(now, '{y}{m}{d}{h}{i}{s}');
    },
    /** 获取签到率类型 */
    getRateType(rate) {
      // 将签到率高的显示为绿色(success)
      if (rate >= 90) return 'success';
      if (rate >= 70) return 'warning';
      return 'danger';
    },
    /** 获取状态类型 */
    getStatusType(status) {
      // 避免绿色，已签到用 info(蓝)
      const statusMap = {
        0: 'danger',   // 缺勤
        1: 'info',     // 已签到
        2: 'warning',  // 迟到
        3: 'info',     // 请假
        4: 'info'      // 早退
      };
      return statusMap[status] || 'info';
    },
    /** 时间格式化 */
    parseTime(time, pattern) {
      if (!time) return '';
      if (arguments.length === 0) {
        return null;
      }
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

    /** 周一自动生成上周周报（前端触发查询，尽量不影响后端） */
    autoGenerateWeeklyIfMonday() {
      try {
        const today = new Date();
        const day = today.getDay();
        if (day === 1) { // Monday
          const end = new Date(today.getTime() - 24 * 3600 * 1000);
          const start = new Date(end.getTime() - 6 * 24 * 3600 * 1000);
          const startDate = this.parseTime(start, '{y}-{m}-{d}');
          const endDate = this.parseTime(end, '{y}-{m}-{d}');
          // 仅发起查询让后端/前端缓存周报数据
          weeklyReport({ startDate, endDate, auto: true }).then(() => {
            // 无需强提示，但记录日志
            console.info('已触发周报生成（前端触发）', startDate, endDate);
          }).catch(() => {
            // 忽略错误
          });
        }
      } catch (e) {
        // ignore
      }
    },
    /** 打印入口（绑定按钮），等待图表图片并打开打印页 */
    async handlePrint(orientation = 'portrait') {
      try {
        await this.$nextTick();
        await this.initCharts();
        if (this.statDimension === 'session') {
          this.updateSessionCharts();
        } else if (this.statDimension === 'time') {
          const dateList = (this.dataList || []).map(i => i.statDate || '');
          const rateList = (this.dataList || []).map(i => Number(i.attendanceRate || 0));
          const signedList = (this.dataList || []).map(i => Number(i.dailySigned || 0));
          const absentList = (this.dataList || []).map(i => Number(i.dailyAbsent || 0));
          this.updateCharts({ dateList, rateList, signedList, absentList });
        } else if (this.statDimension === 'weekly') {
          const weeklyData = { weeklyData: (this.dataList || []).map(i => ({ className: i.className, avgAttendanceRate: i.avgAttendanceRate || i.attendanceRate || 0 })) };
          this.updateWeeklyCharts(weeklyData);
        }

        // 重试等待（最多 5 次）确保 charts 完成渲染
        let attempts = 0;
        let images = null;
        while (attempts < 5) {
          try {
            if (this.trendChart) try { this.trendChart.resize(); } catch(e){}
            if (this.distributionChart) try { this.distributionChart.resize(); } catch(e){}
            if (this.statusChart) try { this.statusChart.resize(); } catch(e){}
            if (this.weeklyChart) try { this.weeklyChart.resize(); } catch(e){}
            await new Promise(r => setTimeout(r, 400));
            images = await this.getChartImagesForPrint();
            // 若至少有一张图存在则跳出
            if (images && Object.values(images).some(v => !!v)) break;
          } catch (err) {
            // ignore
          }
          attempts++;
        }
        images = images || {};
        const html = this.buildPrintHtml(images, orientation);
        const w = window.open('', '_blank');
        w.document.write(html);
        w.document.close();
        setTimeout(() => { try { w.focus(); w.print(); } catch(e) {} }, 800);
      } catch (e) {
        console.error('打印失败', e);
        this.$message.error('打印失败，请稍后重试');
      }
    },

    /** 获取图表图片用于打印 */
    async getChartImagesForPrint() {
      const images = {};
      const chartRefs = {
        trend: this.trendChart,
        distribution: this.distributionChart,
        status: this.statusChart,
        weekly: this.weeklyChart
      };
      for (const key in chartRefs) {
        const chart = chartRefs[key];
        if (chart) {
          try {
            // 确保 resize 以便渲染正确尺寸，然后短延迟等待 DOM/Canvas 变化
            try { chart.resize(); } catch (e) { /* ignore */ }
            await new Promise(resolve => setTimeout(resolve, 200));
            images[key] = chart.getDataURL({ pixelRatio: 3, backgroundColor: '#fff' });
          } catch (e) {
            images[key] = null;
          }
        }
      }
      return images;
    },

    /** 打印 HTML 构建 */
    buildPrintHtml(images, orientation) {
      const title = '考勤报表';
      const now = this.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}');
      const userName = (this.$store && this.$store.state && this.$store.state.user && this.$store.state.user.userName) || '当前用户';
      const style = `
        <style>
        @page { size: ${orientation === 'landscape' ? 'landscape' : 'portrait'}; }
        body { font-family: Arial, Helvetica, sans-serif; padding: 20px; }
        .header { text-align: center; margin-bottom: 10px }
        .meta { margin-bottom: 10px; }
        .chart-img { max-width: 100%; display: block; margin: 10px auto; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        table, th, td { border: 1px solid #ccc; }
        th, td { padding: 6px; text-align: center; }
        </style>
      `;
      const meta = `<div class="meta">统计时间：${this.dateRange && this.dateRange.length ? this.dateRange[0] + ' 至 ' + this.dateRange[1] : '全部'} &nbsp;&nbsp; 生成人：${userName} &nbsp;&nbsp; 生成时间：${now}</div>`;

      const chartHtmlParts = [];
      if (this.statDimension === 'time' && images.trend) chartHtmlParts.push(`<div><h4>签到率趋势图</h4><img class="chart-img" src="${images.trend}"/></div>`);
      if (this.statDimension === 'session' && images.distribution) chartHtmlParts.push(`<div><h4>课堂签到分布</h4><img class="chart-img" src="${images.distribution}"/></div>`);
      if (this.statDimension === 'session' && images.status) chartHtmlParts.push(`<div><h4>签到状态统计</h4><img class="chart-img" src="${images.status}"/></div>`);
      // 始终包含周报图（如果存在），使得周报版面可打印
      if (images.weekly) chartHtmlParts.push(`<div><h4>周考勤汇总</h4><img class="chart-img" src="${images.weekly}"/></div>`);

      // 插入表格（明细或汇总）
      const tableHtml = this.statDimension === 'details' ? this.buildDetailTableHtml() : this.buildSummaryTableHtml();

      return `<!DOCTYPE html><html><head><meta charset="utf-8">${style}</head><body><div class="header"><h2>${title}</h2></div>${meta}${chartHtmlParts.join('')}<div>${tableHtml}</div></body></html>`;
    },
    // 通用字段读取器：按 candidates 数组尝试多个字段名
    getVal(row, candidates) {
      if (!row) return undefined;
      for (const k of candidates) {
        if (row[k] !== undefined && row[k] !== null) return row[k];
      }
      return undefined;
    },
    // 动态创建表单并提交到后端以触发浏览器下载（在新窗口中）
    submitFormDownload(action, params) {
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = (process && process.env && process.env.VUE_APP_BASE_API ? process.env.VUE_APP_BASE_API : '') + action;
      form.target = '_blank';
      form.style.display = 'none';
      for (const key in params) {
        if (params[key] === undefined || params[key] === null) continue;
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = key;
        input.value = params[key];
        form.appendChild(input);
      }
      document.body.appendChild(form);
      form.submit();
      setTimeout(() => { try { document.body.removeChild(form); } catch (e) {} }, 1000);
    },
    // 使用 fetch POST 拉取导出文件（作为 download helper 之外的稳健回退）
    async postExportFetch(params, filename) {
      try {
        const url = (process && process.env && process.env.VUE_APP_BASE_API ? process.env.VUE_APP_BASE_API : '') + '/proj_fz/attendanceReport/export';
        const body = new URLSearchParams();
        for (const k in params) {
          if (params[k] === undefined || params[k] === null) continue;
          body.append(k, params[k]);
        }
        const resp = await fetch(url, {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: body.toString()
        });
        const contentType = resp.headers.get('content-type') || '';
        const blob = await resp.blob();
        // 如果返回 JSON，解析并抛错
        if (contentType.indexOf('application/json') !== -1) {
          const txt = await (new Response(blob)).text();
          let j = null;
          try { j = JSON.parse(txt); } catch (e) { j = { msg: txt }; }
          throw new Error((j && j.msg) ? (j.msg) : '导出接口返回错误');
        }
        // 否则认为是文件流，保存
        const finalName = filename && filename.length ? filename : '考勤报表.xlsx';
        saveAs(blob, finalName);
        this.$message.success('已成功导出文件');
        return true;
      } catch (e) {
        console.warn('fetch 导出失败：', e);
        throw e;
      }
    },
    // el-table 行 key
    rowKey(row) {
      return row.sessionId || row.studentNo || row.id || row._index || JSON.stringify(row);
    },
    // 表格行样式：交替浅灰背景
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

.metric-card {
  text-align: center;
  height: 200px;
}
.metric-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}
.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

/* 表格美化 */
.el-table th {
  background: #f7f9fb !important;
  color: #333;
  font-weight: 600;
}
.striped-row {
  background: #fafafa;
}
.el-button[icon="el-icon-download"] {
  background: #409EFF; color: #fff; border-color: #409EFF;
}
.el-button[icon="el-icon-printer"] {
  background: #606266; color: #fff; border-color: #606266;
}

@media print {
  .el-form-item, .el-button {
    display: none !important;
  }
  .chart-wrapper {
    page-break-inside: avoid;
  }
  .app-container {
    padding: 0 !important;
  }
}
</style>
