<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <span class="page-title">课堂详情</span>
        <span class="course-name">{{ courseName }}</span>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['projlw:session:add']">
          新增课堂
        </el-button>
        <el-button icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>
    </div>

    <!-- 课堂列表 -->
    <div v-loading="loading" class="session-list">
      <el-card v-for="session in sessionList" :key="session.sessionId" class="session-card">
        <div class="session-header">
          <div class="session-title">
            <h3 class="session-name">{{ courseName }}</h3>
            <div class="session-id">课堂ID: {{ session.sessionId }}</div>
          </div>
          <div class="session-actions">
            <el-tag :type="getStatusType(session.calculatedStatus)">
              {{ getStatusText(session.calculatedStatus) }}
            </el-tag>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-video-play"
              @click="handleEnterClass(session)"
              :disabled="session.calculatedStatus !== 1"
              v-hasPermi="['projlw:session:enter']"
            >进入课堂</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(session)"
              v-hasPermi="['projlw:session:edit']"
            >编辑</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(session)"
              v-hasPermi="['projlw:session:remove']"
            >删除</el-button>
          </div>
        </div>

        <div class="session-info">
          <div class="info-item">
            <span class="label">授课老师：</span>
            <span class="value">{{ session.teacher || '未分配' }}</span>
          </div>
          <div class="info-item">
            <span class="label">老师ID：</span>
            <span class="value">{{ session.teacherId }}</span>
          </div>
          <div class="info-item">
            <span class="label">上课时间：</span>
            <span class="value">{{ formatSessionTime(session) }}</span>
          </div>
          <div class="info-item">
            <span class="label">每堂课时长：</span>
            <span class="value">{{ session.classDuration || 45 }}分钟</span>
          </div>
          <div class="info-item">
            <span class="label">课堂人数：</span>
            <span class="value">{{ session.totalStudents || 0 }}人</span>
          </div>
        </div>
      </el-card>

      <!-- 空状态 -->
      <div v-if="sessionList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无课堂数据">
          <el-button type="primary" @click="handleAdd" v-hasPermi="['projlw:session:add']">新增课堂</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 添加或修改课堂对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程名称">
          <el-input :value="courseName" disabled />
        </el-form-item>
        <el-form-item v-if="form.sessionId" label="课堂ID">
          <el-input :value="form.sessionId" disabled />
        </el-form-item>
        <el-form-item label="授课老师" prop="teacher">
          <el-input v-model="form.teacher" placeholder="请输入授课老师姓名" />
        </el-form-item>
        <el-form-item label="老师ID" prop="teacherId">
          <el-input-number v-model="form.teacherId" :min="1" placeholder="请输入老师ID" style="width: 100%" />
        </el-form-item>

        <!-- 上课时间设置 -->
        <el-form-item label="星期" prop="weekDay">
          <el-select v-model="form.weekDay" placeholder="请选择星期" style="width: 100%">
            <el-option label="周一" value="1" />
            <el-option label="周二" value="2" />
            <el-option label="周三" value="3" />
            <el-option label="周四" value="4" />
            <el-option label="周五" value="5" />
            <el-option label="周六" value="6" />
            <el-option label="周日" value="7" />
          </el-select>
        </el-form-item>

        <el-form-item label="上课时间" prop="startTime">
          <el-time-picker
            v-model="form.startTime"
            placeholder="选择上课时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="下课时间" prop="endTime">
          <el-time-picker
            v-model="form.endTime"
            placeholder="选择下课时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="每堂课时长" prop="classDuration">
          <el-input-number
            v-model="form.classDuration"
            :min="30"
            :max="180"
            :step="5"
            placeholder="请输入每堂课时长（分钟）"
            style="width: 100%"
          />
          <div class="form-tip">单位：分钟</div>
        </el-form-item>

        <el-form-item label="课堂人数" prop="totalStudents">
          <el-input-number v-model="form.totalStudents" :min="1" :max="200" placeholder="请输入课堂人数" style="width: 100%" />
        </el-form-item>
        <!-- 移除状态选择 -->
        <el-form-item label="课程ID">
          <el-input :value="courseId" disabled />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listSession, getSession, addSession, updateSession, delSession } from "@/api/proj_lw/session";

export default {
  name: "ClassSession",
  data() {
    return {
      loading: false,
      submitLoading: false,
      sessionList: [],
      courseName: '',
      courseId: null,
      title: "",
      open: false,
      statusTimer: null,
      form: {},
      rules: {
        teacher: [
          { required: true, message: "授课老师不能为空", trigger: "blur" }
        ],
        teacherId: [
          { required: true, message: "老师ID不能为空", trigger: "blur" }
        ],
        weekDay: [
          { required: true, message: "请选择星期", trigger: "change" }
        ],
        startTime: [
          { required: true, message: "请选择上课时间", trigger: "change" }
        ],
        endTime: [
          { required: true, message: "请选择下课时间", trigger: "change" }
        ],
        classDuration: [
          { required: true, message: "请输入每堂课时长", trigger: "blur" }
        ],
        totalStudents: [
          { required: true, message: "请输入课堂人数", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.courseId = this.$route.query.courseId;
    this.courseName = this.$route.query.courseName;
    this.getList();
  },
  mounted() {
    // 每分钟检查一次状态
    this.statusTimer = setInterval(() => {
      this.updateAllSessionStatus();
    }, 60000);
  },
  beforeDestroy() {
    if (this.statusTimer) {
      clearInterval(this.statusTimer);
    }
  },
  methods: {
    /** 查询课堂列表 */
    getList() {
      this.loading = true;
      const queryParams = {
        courseId: this.courseId
      };
      listSession(queryParams).then(response => {
        this.sessionList = response.rows || [];
        this.updateAllSessionStatus();
        this.loading = false;
      }).catch(error => {
        console.error("获取课堂列表失败:", error);
        this.sessionList = [];
        this.loading = false;
      });
    },

    /** 更新所有课堂状态 */
    updateAllSessionStatus() {
      this.sessionList.forEach(session => {
        this.calculateSessionStatus(session);
      });
    },

    /** 计算课堂状态 */
    calculateSessionStatus(session) {
      const now = new Date();
      const currentWeekDay = now.getDay(); // 0-6, 0是周日
      const currentTime = now.getHours() * 60 + now.getMinutes();

      // 转换星期格式
      let systemWeekDay;
      if (currentWeekDay === 0) {
        systemWeekDay = '7'; // 周日
      } else {
        systemWeekDay = currentWeekDay.toString();
      }

      const sessionWeekDay = session.weekDay;

      // 处理时间格式
      let startTime = session.startTime || '';
      let endTime = session.endTime || '';

      if (startTime.includes(' ')) {
        startTime = startTime.split(' ')[1].substring(0, 5);
      }
      if (endTime.includes(' ')) {
        endTime = endTime.split(' ')[1].substring(0, 5);
      }

      const startMinutes = this.timeToMinutes(startTime);
      const endMinutes = this.timeToMinutes(endTime);

      // 状态判断
      if (sessionWeekDay === systemWeekDay) {
        if (currentTime >= startMinutes && currentTime <= endMinutes) {
          session.calculatedStatus = 1; // 进行中
        } else if (currentTime < startMinutes) {
          session.calculatedStatus = 0; // 未开始
        } else {
          session.calculatedStatus = 2; // 已结束
        }
      } else {
        const sessionDayNum = parseInt(sessionWeekDay);
        const currentDayNum = parseInt(systemWeekDay);

        if (sessionDayNum < currentDayNum) {
          session.calculatedStatus = 2; // 已结束
        } else {
          session.calculatedStatus = 0; // 未开始
        }
      }
    },

    /** 时间字符串转换为分钟数 */
    timeToMinutes(timeStr) {
      if (!timeStr) return 0;
      const [hours, minutes] = timeStr.split(':').map(Number);
      return hours * 60 + minutes;
    },

    /** 返回按钮操作 */
    handleBack() {
      this.$router.push('/proj_lw/course');
    },

    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },

    // 表单重置
    reset() {
      this.form = {
        sessionId: null,
        className: this.courseName,
        teacherId: 1,
        teacher: null,
        weekDay: null,
        startTime: null,
        endTime: null,
        classDuration: 45,
        totalStudents: 30,
        courseId: this.courseId,
        status: 0
      };
      this.resetForm("form");
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加课堂";
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const sessionId = row.sessionId;
      getSession(sessionId).then(response => {
        this.form = response.data;
        this.form.totalStudents = this.form.totalStudents || 30;
        this.form.teacherId = this.form.teacherId || 1;
        this.form.classDuration = this.form.classDuration || 45;

        if (this.form.startTime && this.form.startTime.includes(' ')) {
          this.form.startTime = this.form.startTime.split(' ')[1].substring(0, 5);
        }
        if (this.form.endTime && this.form.endTime.includes(' ')) {
          this.form.endTime = this.form.endTime.split(' ')[1].substring(0, 5);
        }

        this.open = true;
        this.title = "修改课堂";
      }).catch(error => {
        console.error("获取课堂详情失败:", error);
        this.$modal.msgError("获取课堂详情失败");
      });
    },

    /** 进入课堂按钮操作 */
    handleEnterClass(row) {
      if (row.calculatedStatus !== 1) {
        this.$modal.msgWarning("只有进行中的课堂才能进入");
        return;
      }

      this.$router.push({
        path: '/proj_lw/classroom',
        query: {
          sessionId: row.sessionId,
          courseId: this.courseId,
          courseName: this.courseName,
          teacher: row.teacher,
          teacherId: row.teacherId
        }
      });
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.submitLoading = true;

          this.form.totalStudents = this.form.totalStudents || 30;
          this.form.teacherId = this.form.teacherId || 1;
          this.form.classDuration = this.form.classDuration || 45;
          this.form.courseId = this.courseId;
          this.form.className = this.courseName;
          this.form.status = 0; // 后端存储固定为0

          const submitData = { ...this.form };

          const request = submitData.sessionId != null
            ? updateSession(submitData)
            : addSession(submitData);

          request.then(response => {
            this.$modal.msgSuccess(submitData.sessionId != null ? "修改成功" : "新增成功");
            this.open = false;
            this.getList();
          }).catch(error => {
            console.error("操作失败:", error);
            this.$modal.msgError("操作失败: " + (error.message || "请检查数据格式"));
          }).finally(() => {
            this.submitLoading = false;
          });
        }
      });
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const sessionId = row.sessionId;
      this.$modal.confirm('是否确认删除"' + this.courseName + '"的课堂(ID:' + sessionId + ')？').then(() => {
        return delSession(sessionId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    /** 获取状态文本 */
    getStatusText(status) {
      const statusMap = {
        0: '未开始',
        1: '进行中',
        2: '已结束'
      };
      return statusMap[status] || '未知';
    },

    /** 获取状态类型 */
    getStatusType(status) {
      const typeMap = {
        0: 'info',
        1: 'success',
        2: 'warning'
      };
      return typeMap[status] || 'info';
    },

    /** 格式化课堂时间显示 */
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
    }
  }
};
</script>

<style scoped>
/* 保持原有样式不变 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e6e6e6;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.course-name {
  font-size: 16px;
  color: #606266;
  margin-top: 5px;
}

.session-list {
  min-height: 200px;
}

.session-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.session-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.session-title {
  flex: 1;
}

.session-name {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 18px;
}

.session-id {
  font-size: 14px;
  color: #909399;
}

.session-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
}

.label {
  color: #909399;
  min-width: 100px;
  font-weight: 500;
}

.value {
  color: #606266;
  flex: 1;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.el-button--text[disabled] {
  color: #c0c4cc !important;
  cursor: not-allowed !important;
}
</style>
