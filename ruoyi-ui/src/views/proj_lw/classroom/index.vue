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

.classroom-title {
  display: flex;
  align-items: center;
}

.animated-title {
  display: flex;
  align-items: center;
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: bold;
  position: relative;
}

.animated-title .text {
  color: #409EFF;
  text-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
  animation: textGlow 2s ease-in-out infinite alternate;
}

.animated-title .dot {
  color: #409EFF;
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
    text-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
  }
  100% {
    text-shadow: 0 2px 8px rgba(64, 158, 255, 0.6), 0 4px 16px rgba(64, 158, 255, 0.4);
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
