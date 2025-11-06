<template>
  <div class="app-container classroom-container">
    <div class="classroom-header">
      <h1>正在上课</h1>
      <div class="classroom-info">
        <p><strong>课程：</strong>{{ courseName }}</p>
        <p><strong>课堂ID：</strong>{{ sessionId }}</p>
        <p><strong>授课老师：</strong>{{ teacher }}</p>
      </div>
      <el-button type="primary" icon="el-icon-back" @click="handleBack">返回课堂管理</el-button>
    </div>

    <div class="classroom-content">
      <el-card class="classroom-card">
        <div slot="header">
          <span>课堂功能</span>
        </div>
        <div class="classroom-features">
          <el-button type="primary" icon="el-icon-user" @click="handleAttendance">课堂签到</el-button>
          <el-button type="success" icon="el-icon-user-solid" @click="handleRandomPick">随机抽人</el-button>
          <el-button type="warning" icon="el-icon-data-board" @click="handleVote">课堂投票</el-button>
          <el-button type="info" icon="el-icon-chat-dot-round" @click="handleInteraction">课堂互动</el-button>
        </div>
      </el-card>

      <el-card class="classroom-card">
        <div slot="header">
          <span>课堂状态</span>
        </div>
        <div class="classroom-status">
          <p>上课时间：{{ formatTime(startTime) }}</p>
          <p>课程时长：{{ classDuration }}分钟</p>
          <p>学生人数：{{ totalStudents }}人</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: "Classroom",
  data() {
    return {
      courseName: '',
      sessionId: null,
      teacher: '',
      courseId: null,
      startTime: '',
      classDuration: 0,
      totalStudents: 0
    };
  },
  created() {
    this.courseName = this.$route.query.courseName || '未知课程';
    this.sessionId = this.$route.query.sessionId || '';
    this.teacher = this.$route.query.teacher || '未知老师';
    this.courseId = this.$route.query.courseId || '';
    this.startTime = new Date().toLocaleTimeString();
    this.classDuration = 45;
    this.totalStudents = 30;
  },
  methods: {
    handleBack() {
      this.$router.push({
        path: '/proj_lw/session',
        query: {
          courseId: this.courseId,
          courseName: this.courseName
        }
      });
    },

    handleAttendance() {
      this.$message.success('开始课堂签到');
      // 这里可以跳转到签到页面或打开签到弹窗
    },

    handleRandomPick() {
      this.$message.success('开始随机抽人');
      // 这里可以跳转到随机抽人页面或打开抽人弹窗
    },

    handleVote() {
      this.$message.success('开始课堂投票');
      // 这里可以跳转到投票页面或打开投票弹窗
    },

    handleInteraction() {
      this.$message.success('开始课堂互动');
      // 这里可以跳转到互动页面或打开互动弹窗
    },

    formatTime(time) {
      return time || new Date().toLocaleString();
    }
  }
};
</script>

<style scoped>
.classroom-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.classroom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.classroom-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.classroom-info {
  flex: 1;
  margin-left: 30px;
}

.classroom-info p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}

.classroom-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.classroom-card {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.classroom-features {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.classroom-features .el-button {
  margin: 5px;
}

.classroom-status p {
  margin: 10px 0;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
  color: #495057;
}
</style>
