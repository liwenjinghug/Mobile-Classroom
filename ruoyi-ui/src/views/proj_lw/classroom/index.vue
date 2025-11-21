<template>
  <div class="app-container classroom-container">
    <div class="classroom-header">
      <div class="classroom-title">
        <h1 class="animated-title">
          <span class="text">正在上课</span>
          <span class="dot dot-1">.</span>
          <span class="dot dot-2">.</span>
          <span class="dot dot-3">.</span>
        </h1>
      </div>
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
      // 跳转到在线课堂的课堂签到
      this.$router.push({
        path: '/proj_myx/AttendanceCreate',
        query: {
          courseId: this.courseId,
          courseName: this.courseName,
          sessionId: this.sessionId
        }
      });
    },

    handleRandomPick() {
      // 跳转到在线课堂的随机抽人
      this.$router.push({
        path: '/proj_myx/RandomPick',
        query: {
          courseId: this.courseId,
          courseName: this.courseName,
          sessionId: this.sessionId
        }
      });
    },

    handleVote() {
      // 跳转到在线课堂的课堂投票
      this.$router.push({
        path: '/proj_myx/Vote',
        query: {
          courseId: this.courseId,
          courseName: this.courseName,
          sessionId: this.sessionId
        }
      });
    },

    handleInteraction() {
      this.$message.success('开始课堂互动');
      // 这里可以添加课堂互动的具体逻辑
    },

    formatTime(time) {
      return time || new Date().toLocaleString();
    }
  }
};
</script>

<style scoped>
/* Mac Style for Classroom Page */
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
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
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

.app-container >>> .el-button--success {
  background-color: #34c759;
  box-shadow: 0 2px 8px rgba(52, 199, 89, 0.2);
}

.app-container >>> .el-button--warning {
  background-color: #ff9500;
  box-shadow: 0 2px 8px rgba(255, 149, 0, 0.2);
}

.app-container >>> .el-button--info {
  background-color: #8e8e93;
}

/* Classroom Header */
.classroom-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  padding: 24px;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  margin-bottom: 24px;
}

.classroom-title {
  display: flex;
  align-items: center;
}

.animated-title {
  display: flex;
  align-items: center;
  margin: 0;
  color: #1d1d1f;
  font-size: 28px;
  font-weight: 700;
  position: relative;
}

.animated-title .text {
  color: #0071e3;
  text-shadow: 0 2px 4px rgba(0, 113, 227, 0.2);
  animation: textGlow 2s ease-in-out infinite alternate;
}

.animated-title .dot {
  color: #0071e3;
  opacity: 0;
  animation: dotBlink 1.5s infinite;
}

.animated-title .dot-1 {
  animation-delay: 0s;
}

.animated-title .dot-2 {
  animation-delay: 0.3s;
}

.animated-title .dot-3 {
  animation-delay: 0.6s;
}

@keyframes textGlow {
  0% {
    text-shadow: 0 2px 4px rgba(0, 113, 227, 0.2);
  }
  100% {
    text-shadow: 0 2px 8px rgba(0, 113, 227, 0.4), 0 4px 16px rgba(0, 113, 227, 0.2);
  }
}

@keyframes dotBlink {
  0%, 20% {
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  80%, 100% {
    opacity: 0;
  }
}

.classroom-info {
  flex: 1;
  margin-left: 40px;
}

.classroom-info p {
  margin: 8px 0;
  color: #86868b;
  font-size: 15px;
}

.classroom-info p strong {
  color: #1d1d1f;
  font-weight: 600;
}

/* Classroom Content */
.classroom-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.classroom-features {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.classroom-features .el-button {
  margin: 0;
  height: 44px;
  padding: 0 24px;
  font-size: 15px;
}

.classroom-status p {
  margin: 12px 0;
  padding: 16px;
  background: #f5f5f7;
  border-radius: 12px;
  color: #1d1d1f;
  font-size: 15px;
  font-weight: 500;
}
</style>
