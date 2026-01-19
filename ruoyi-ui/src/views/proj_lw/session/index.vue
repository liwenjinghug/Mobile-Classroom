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

    <!-- 对话框部分保持不变 -->
    <!-- 添加对话框部分到template中 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="授课老师" prop="teacher">
          <el-input v-model="form.teacher" placeholder="请输入授课老师" />
        </el-form-item>
        <el-form-item label="老师ID" prop="teacherId">
          <el-input v-model="form.teacherId" placeholder="请输入老师ID" type="number" />
        </el-form-item>
        <el-form-item label="星期" prop="weekDay">
          <el-select v-model="form.weekDay" placeholder="请选择星期" style="width: 100%">
            <el-option
              v-for="item in [
            { label: '周一', value: '1' },
            { label: '周二', value: '2' },
            { label: '周三', value: '3' },
            { label: '周四', value: '4' },
            { label: '周五', value: '5' },
            { label: '周六', value: '6' },
            { label: '周日', value: '7' }
          ]"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上课时间" prop="startTime">
          <el-time-select
            v-model="form.startTime"
            placeholder="请选择上课时间"
            :picker-options="{ start: '08:00', step: '00:15', end: '22:00' }"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="下课时间" prop="endTime">
          <el-time-select
            v-model="form.endTime"
            placeholder="请选择下课时间"
            :picker-options="{ start: '08:00', step: '00:15', end: '22:00' }"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="课时长" prop="classDuration">
          <el-input-number
            v-model="form.classDuration"
            placeholder="请输入每堂课时长"
            :min="30"
            :max="180"
            :step="15"
            style="width: 100%"
          />
          <div class="form-tip">单位：分钟</div>
        </el-form-item>
        <el-form-item label="课堂人数" prop="totalStudents">
          <el-input-number
            v-model="form.totalStudents"
            placeholder="请输入课堂人数"
            :min="1"
            :max="100"
            style="width: 100%"
          />
          <div class="form-tip">单位：人</div>
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

      // 直接跳转到课堂页面
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

          const submitData = {...this.form};

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
      }).catch(() => {
      });
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
/* Mac Style for Session Page */
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
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  background-color: #ffffff;
  transition: all 0.3s ease;
}

.app-container >>> .el-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.app-container >>> .el-card__header {
  border-bottom: 1px solid #f5f5f7;
  padding: 20px 24px;
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
}

.course-name {
  font-size: 18px;
  color: #86868b;
  font-weight: 500;
  margin-top: 6px;
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

.app-container >>> .el-button--text {
  color: #0071e3;
  background: none;
  padding: 0 5px;
  box-shadow: none;
}

.app-container >>> .el-button--text:hover {
  color: #0077ed;
  background: none;
  transform: none;
}

.app-container >>> .el-button--text[disabled] {
  color: #c0c4cc !important;
  cursor: not-allowed !important;
}

/* Session List */
.session-list {
  min-height: 200px;
}

.session-card {
  margin-bottom: 24px;
  transition: all 0.3s;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5f5f7;
}

.session-title {
  flex: 1;
}

.session-name {
  margin: 0 0 6px 0;
  color: #1d1d1f;
  font-size: 20px;
  font-weight: 600;
}

.session-id {
  font-size: 14px;
  color: #86868b;
}

.session-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  color: #86868b;
  min-width: 100px;
  font-weight: 500;
  font-size: 14px;
}

.value {
  color: #1d1d1f;
  flex: 1;
  font-size: 14px;
}

/* Dialog Styling */
.app-container >>> .el-dialog {
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.app-container >>> .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
}

.app-container >>> .el-dialog__title {
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

.app-container >>> .el-dialog__body {
  padding: 24px;
}

.app-container >>> .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
}

/* Form Styling */
.app-container >>> .el-form-item__label {
  font-weight: 500;
  color: #1d1d1f;
}

.app-container >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  height: 36px;
  transition: all 0.2s ease;
}

.app-container >>> .el-input__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

/* Tags */
.app-container >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.form-tip {
  font-size: 12px;
  color: #86868b;
  margin-top: 6px;
}
</style>
