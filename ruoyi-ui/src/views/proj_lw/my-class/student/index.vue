<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 我的课堂 -->
      <el-tab-pane label="我的课堂" name="joined">
        <div class="filter-container">
          <el-input
            v-model="joinedQuery.className"
            placeholder="课堂名称"
            style="width: 200px;"
            class="filter-item"
            @keyup.enter="handleJoinedFilter"
          />
          <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleJoinedFilter">
            搜索
          </el-button>
        </div>

        <el-table
          v-loading="joinedLoading"
          :data="joinedList"
          border
          fit
          highlight-current-row
          style="width: 100%;"
        >
          <el-table-column label="课堂ID" prop="sessionId" align="center" width="80" />
          <el-table-column label="课堂名称" prop="className" align="center" />
          <el-table-column label="教师" prop="teacher" align="center" />
          <el-table-column label="上课时间" align="center" width="180">
            <template slot-scope="{row}">
              <span>{{ formatSessionTime(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="加入时间" prop="assignedAt" align="center" width="180">
            <template slot-scope="{row}">
              <span>{{ row.assignedAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
            <template slot-scope="{row}">
              <el-button type="danger" size="mini" @click="handleQuit(row)">
                退出
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="joinedTotal>0"
          :total="joinedTotal"
          :page.sync="joinedQuery.page"
          :limit.sync="joinedQuery.limit"
          @pagination="getJoinedList"
        />
      </el-tab-pane>

      <!-- 发现课堂 -->
      <el-tab-pane label="发现课堂" name="available">
        <div class="filter-container">
          <el-input
            v-model="availableQuery.className"
            placeholder="课堂名称"
            style="width: 200px;"
            class="filter-item"
            @keyup.enter="handleAvailableFilter"
          />
          <el-input
            v-model="availableQuery.teacher"
            placeholder="教师姓名"
            style="width: 200px;"
            class="filter-item"
            @keyup.enter="handleAvailableFilter"
          />
          <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleAvailableFilter">
            搜索
          </el-button>
        </div>

        <el-table
          v-loading="availableLoading"
          :data="availableList"
          border
          fit
          highlight-current-row
          style="width: 100%;"
        >
          <el-table-column label="课堂ID" prop="sessionId" align="center" width="80" />
          <el-table-column label="课堂名称" prop="className" align="center" />
          <el-table-column label="教师" prop="teacher" align="center" />
          <el-table-column label="总人数" prop="totalStudents" align="center" width="80" />
          <el-table-column label="上课时间" align="center" width="180">
            <template slot-scope="{row}">
              <span>{{ formatSessionTime(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
            <template slot-scope="{row}">
              <el-button
                type="primary"
                size="mini"
                :loading="row.applying"
                :disabled="row.applied"
                @click="handleApply(row)"
              >
                {{ row.applied ? '已申请' : '申请加入' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="availableTotal>0"
          :total="availableTotal"
          :page.sync="availableQuery.page"
          :limit.sync="availableQuery.limit"
          @pagination="getAvailableList"
        />
      </el-tab-pane>

      <!-- 我的申请 -->
      <el-tab-pane label="我的申请" name="applications">
        <el-table
          v-loading="applicationsLoading"
          :data="applicationsList"
          border
          fit
          highlight-current-row
          style="width: 100%;"
        >
          <el-table-column label="申请ID" prop="applicationId" align="center" width="80" />
          <el-table-column label="课堂名称" prop="className" align="center" />
          <el-table-column label="教师" prop="teacherName" align="center" />
          <el-table-column label="申请时间" prop="applyTime" align="center" width="180">
            <template slot-scope="{row}">
              <span>{{ row.applyTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" align="center" width="100">
            <template slot-scope="{row}">
              <el-tag :type="row.status | applicationStatusFilter">
                {{ row.status | applicationStatusTextFilter }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核时间" prop="auditTime" align="center" width="180">
            <template slot-scope="{row}">
              <span v-if="row.auditTime">{{ row.auditTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" align="center" />
          <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
            <template slot-scope="{row}">
              <el-button
                v-if="row.status === '0'"
                type="danger"
                size="mini"
                @click="handleCancelApplication(row)"
              >
                取消申请
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="applicationsTotal>0"
          :total="applicationsTotal"
          :page.sync="applicationsQuery.page"
          :limit.sync="applicationsQuery.limit"
          @pagination="getApplicationsList"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getJoinedClasses, getAvailableClasses, applyJoinClass, getMyApplications, cancelApplication, quitClass } from '@/api/proj_lw/student-class'
import Pagination from '@/components/Pagination'

export default {
  name: 'StudentClass',
  components: { Pagination },
  filters: {
    statusFilter(status) {
      const statusMap = {
        '0': 'info',
        '1': 'success',
        '2': 'danger'
      }
      return statusMap[status]
    },
    statusTextFilter(status) {
      const statusMap = {
        '0': '未开始',
        '1': '进行中',
        '2': '已结束'
      }
      return statusMap[status]
    },
    applicationStatusFilter(status) {
      const statusMap = {
        '0': 'warning',
        '1': 'success',
        '2': 'danger'
      }
      return statusMap[status]
    },
    applicationStatusTextFilter(status) {
      const statusMap = {
        '0': '待审核',
        '1': '已通过',
        '2': '已拒绝'
      }
      return statusMap[status]
    },
    // 添加 parseTime 过滤器
    parseTime(time, format) {
      if (!time) return ''

      // 简单的日期格式化
      const date = new Date(time)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')
      const second = date.getSeconds().toString().padStart(2, '0')

      if (format) {
        return format
          .replace('{y}', year)
          .replace('{m}', month)
          .replace('{d}', day)
          .replace('{h}', hour)
          .replace('{i}', minute)
          .replace('{s}', second)
      }

      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    }
  },
  data() {
    return {
      activeTab: 'joined',

      // 我的课堂
      joinedList: [],
      joinedTotal: 0,
      joinedLoading: true,
      joinedQuery: {
        page: 1,
        limit: 10,
        className: undefined
      },

      // 发现课堂
      availableList: [],
      availableTotal: 0,
      availableLoading: true,
      availableQuery: {
        page: 1,
        limit: 10,
        className: undefined,
        teacher: undefined
      },

      // 我的申请
      applicationsList: [],
      applicationsTotal: 0,
      applicationsLoading: true,
      applicationsQuery: {
        page: 1,
        limit: 10
      }
    }
  },
  created() {
    this.getJoinedList()
  },
  methods: {
    // 我的课堂相关
    async getJoinedList() {
      this.joinedLoading = true
      try {
        const response = await getJoinedClasses(this.joinedQuery)
        console.log('我的课堂完整响应:', JSON.stringify(response, null, 2))
        console.log('我的课堂rows数据:', response.rows)
        console.log('我的课堂total数据:', response.total)

        this.joinedList = response.rows || []
        this.joinedTotal = response.total || 0

        console.log('最终我的课堂列表:', this.joinedList)
      } catch (error) {
        console.error('获取我的课堂失败:', error)
        this.joinedList = []
        this.joinedTotal = 0
        this.$message.error('获取课堂数据失败')
      } finally {
        this.joinedLoading = false
      }
    },
    handleJoinedFilter() {
      this.joinedQuery.page = 1
      this.getJoinedList()
    },

    // 发现课堂相关
    async getAvailableList() {
      this.availableLoading = true
      try {
        const response = await getAvailableClasses(this.availableQuery)
        console.log('发现课堂详细响应:', JSON.stringify(response, null, 2))
        console.log('响应数据rows:', response.rows)
        console.log('响应数据total:', response.total)

        this.availableList = response.rows || []
        this.availableTotal = response.total || 0

        // 为每个课堂添加申请状态
        this.availableList.forEach(item => {
          item.applying = false
          item.applied = false
        })

        console.log('最终显示的课堂列表:', this.availableList)
      } catch (error) {
        console.error('获取发现课堂失败:', error)
        this.availableList = []
        this.availableTotal = 0
        this.$message.error('获取发现课堂失败')
      } finally {
        this.availableLoading = false
      }
    },
    handleAvailableFilter() {
      this.availableQuery.page = 1
      this.getAvailableList()
    },
    async handleApply(row) {
      console.log('=== 开始申请加入课堂 ===');
      console.log('申请的课堂:', row);

      this.$set(row, 'applying', true)
      try {
        console.log('调用API申请...');
        await applyJoinClass(row.sessionId)
        console.log('申请API调用成功');

        this.$message.success('申请成功，等待教师审核')
        this.$set(row, 'applied', true)

        console.log('刷新申请列表...');
        this.getApplicationsList()
      } catch (error) {
        console.error('申请失败:', error);
        this.$message.error(error.message || '申请失败')
      } finally {
        this.$set(row, 'applying', false)
      }
    },

    // 我的申请相关
    // 我的申请相关
    async getApplicationsList() {
      this.applicationsLoading = true
      try {
        const response = await getMyApplications(this.applicationsQuery)
        console.log('我的申请完整响应:', JSON.stringify(response, null, 2))
        console.log('响应数据rows:', response.rows)
        console.log('响应数据total:', response.total)

        this.applicationsList = response.rows || []
        this.applicationsTotal = response.total || 0

        console.log('最终显示的申请列表:', this.applicationsList)
      } catch (error) {
        console.error('获取我的申请失败:', error)
        this.applicationsList = []
        this.applicationsTotal = 0
        this.$message.error('获取申请数据失败')
      } finally {
        this.applicationsLoading = false
      }
    },
    async handleCancelApplication(row) {
      try {
        await this.$confirm('确定要取消申请吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await cancelApplication(row.applicationId)
        this.$message.success('取消申请成功')
        this.getApplicationsList()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '取消申请失败')
        }
      }
    },

    // 退出课堂
    async handleQuit(row) {
      try {
        await this.$confirm('确定要退出该课堂吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await quitClass(row.sessionId)
        this.$message.success('退出课堂成功')
        this.getJoinedList()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '退出课堂失败')
        }
      }
    },

    // 进入课堂
    handleEnterClass(row) {
      this.$router.push(`/proj_lw/classroom?sessionId=${row.sessionId}`)
    },

    // 格式化课堂时间显示
    formatSessionTime(session) {
      const weekDayMap = {
        '1': '周一',
        '2': '周二',
        '3': '周三',
        '4': '周四',
        '5': '周五',
        '6': '周六',
        '7': '周日'
      };

      let weekDay = weekDayMap[session.weekDay] || '';
      let startTime = session.startTime || '';
      let endTime = session.endTime || '';

      if (startTime.includes(' ')) {
        startTime = startTime.split(' ')[1].substring(0, 5);
      }
      if (endTime.includes(' ')) {
        endTime = endTime.split(' ')[1].substring(0, 5);
      }

      return `${weekDay} ${startTime}-${endTime}`;
    },

    // 标签切换
    handleTabClick(tab) {
      if (tab.name === 'available') {
        this.getAvailableList()
      } else if (tab.name === 'applications') {
        this.getApplicationsList()
      } else if (tab.name === 'joined') {
        this.getJoinedList()
      }
    }
  }
}
</script>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}
.filter-item {
  margin-right: 10px;
}
</style>
