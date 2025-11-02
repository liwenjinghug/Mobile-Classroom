<template>
  <div class="app-container personal-center">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :span="24">
        <el-card class="welcome-card" shadow="hover">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎来到个人中心</h2>
              <p>在这里管理您的待办事项和消息通知</p>
            </div>
            <div class="welcome-actions">
              <el-button type="primary" icon="el-icon-list" @click="$router.push('/proj_cyq/todo')">待办事项</el-button>
              <el-button type="success" icon="el-icon-message" @click="$router.push('/proj_cyq/message')">消息中心</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 快速入口 -->
      <el-col :span="12">
        <el-card class="quick-access" shadow="hover">
          <div slot="header" class="clearfix">
            <span>快速入口</span>
          </div>
          <div class="access-items">
            <el-button
              type="primary"
              icon="el-icon-plus"
              class="access-item"
              @click="$router.push('/proj_cyq/todo')"
            >
              新建待办
            </el-button>
            <el-button
              type="success"
              icon="el-icon-message"
              class="access-item"
              @click="$router.push('/proj_cyq/message')"
            >
              查看消息
            </el-button>
            <el-button
              type="info"
              icon="el-icon-s-data"
              class="access-item"
              @click="$router.push('/proj_cyq/todo')"
            >
              数据统计
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 统计概览 -->
      <el-col :span="12">
        <el-card class="stats-overview" shadow="hover">
          <div slot="header" class="clearfix">
            <span>统计概览</span>
          </div>
          <div class="stats-items">
            <div class="stat-item">
              <div class="stat-value">{{ todoStats.total || 0 }}</div>
              <div class="stat-label">总待办数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ todoStats.uncompleted || 0 }}</div>
              <div class="stat-label">未完成</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ messageStats.unread || 0 }}</div>
              <div class="stat-label">未读消息</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listTodo } from "@/api/proj_cyq/todo";
import { getUnreadCount } from "@/api/proj_cyq/message";

export default {
  name: "PersonalCenter",
  data() {
    return {
      todoStats: {
        total: 0,
        uncompleted: 0
      },
      messageStats: {
        unread: 0
      }
    };
  },
  created() {
    this.loadStats();
  },
  methods: {
    async loadStats() {
      try {
        // 加载待办统计
        const todoResponse = await listTodo();
        const todos = todoResponse.rows || [];
        this.todoStats.total = todos.length;
        this.todoStats.uncompleted = todos.filter(todo => todo.status === '0').length;

        // 加载消息统计
        const messageResponse = await getUnreadCount();
        this.messageStats.unread = messageResponse.data || 0;
      } catch (error) {
        console.error('加载统计信息失败:', error);
      }
    }
  }
};
</script>

<style scoped>
.personal-center {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.welcome-text p {
  margin: 0;
  color: #606266;
}

.access-items {
  display: flex;
  gap: 15px;
}

.access-item {
  flex: 1;
}

.stats-items {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item {
  padding: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.quick-access,
.stats-overview {
  height: 200px;
}
</style>
