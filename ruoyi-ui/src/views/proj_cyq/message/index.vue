<template>
  <div class="app-container">
    <div class="message-header">
      <h3>消息提示</h3>
      <div class="header-actions">
        <el-button
          @click="handleExport"
          icon="el-icon-download"
          :loading="exportLoading"
        >导出</el-button>

        <el-button @click="handleRefresh" icon="el-icon-refresh">刷新</el-button>
        <el-button @click="handleMarkAllRead" type="primary" :loading="markingAllRead">全部标记已读</el-button>
        <el-button icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>
    </div>

    <h4 class="stats-title">统计概览</h4>

    <el-row :gutter="20" class="stats-overview" v-if="stats.totalCount > 0">
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-label">消息总数</div>
          <div class="stat-value">{{ stats.totalCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item unread">
          <div class="stat-label">未读消息</div>
          <div class="stat-value">{{ stats.unreadCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item todo">
          <div class="stat-label">待办事项</div>
          <div class="stat-value">{{ getStatValue('typeStats', '待办事项') }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item homework">
          <div class="stat-label">作业消息</div>
          <div class="stat-value">{{ getStatValue('typeStats', '作业消息') }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="message-list">
      <div
        v-for="(message, index) in messageList"
        :key="generateKey(message, index)"
        class="message-item"
        :class="{ unread: message.isRead === '0' }"
      >
        <div class="message-avatar">
          <el-avatar
            :style="getAvatarStyle(message.type)"
            :size="40"
          >
            <i :class="getAvatarIcon(message.type)"></i>
          </el-avatar>
        </div>
        <div class="message-content">
          <div class="message-title">
            <span class="title-text">{{ message.title }}</span>
            <el-tag
              :type="getMessageType(message.type)"
              size="mini"
            >
              {{ getTypeText(message.type) }}
            </el-tag>
            <el-tag v-if="message.isRead === '0'" type="danger" size="mini">未读</el-tag>
          </div>

          <div v-if="message.type === 'homework'" class="message-body">
            <div class="homework-info">
              <p><strong>作业名称：</strong>{{ message.homeworkName }}</p>
              <p><strong>作业内容：</strong>{{ message.content }}</p>
              <p><strong>截止时间：</strong>{{ parseTime(message.deadline) }}</p>
            </div>
          </div>

          <div v-else-if="message.type === 'todo'" class="message-body">
            <div class="todo-info">
              <p><strong>待办内容：</strong>{{ message.content }}</p>
              <p><strong>创建时间：</strong>{{ parseTime(message.sendTime) }}</p>
            </div>
          </div>

          <div class="message-meta">
            <span class="sender">发送者：{{ message.sender }}</span>
            <span class="time">发送时间：{{ parseTime(message.sendTime) }}</span>
          </div>
        </div>
        <div class="message-actions">
          <el-button
            v-if="message.isRead === '0'"
            type="text"
            @click="handleMarkRead(message)"
            :loading="message.markingRead"
          >{{ message.markingRead ? '标记中...' : '标记已读' }}</el-button>
          <span v-else class="read-text">已读</span>

          <el-button
            type="text"
            @click="handleDelete(message, index)"
            style="color: #f56c6c; margin-top: 5px;"
            :loading="message.deleting"
          >{{ message.deleting ? '删除中...' : '删除' }}</el-button>
        </div>
      </div>

      <div v-if="messageList.length === 0" class="empty-state">
        <el-empty description="暂无消息"></el-empty>
      </div>
    </div>
  </div>
</template>

<script>
// 【修改】引入 getMessageStats
import {
  listMessage,
  markTodoAsRead,
  markHomeworkAsRead,
  markAllAsRead,
  deleteMessage,
  getMessageStats // <-- 引入
} from "@/api/proj_cyq/message";

export default {
  name: "Message",
  data() {
    return {
      // 消息列表
      messageList: [],
      // 全部标记已读加载状态
      markingAllRead: false,

      // 【新增】数据
      exportLoading: false, // 导出加载状态
      stats: { // 统计数据
        totalCount: 0,
        unreadCount: 0,
        typeStats: [],
        readStats: []
      }
    };
  },
  created() {
    this.getList();
    this.getStats(); // 【新增】页面加载时获取统计
  },
  methods: {
    // 获取消息列表
    getList() {
      listMessage().then(response => {
        // 处理可能存在的重复或空ID，并添加删除状态
        this.messageList = this.processMessageList(response.data || []);
        console.log('消息列表:', this.messageList);
      });
    },

    // 【新增】获取统计数据
    getStats() {
      getMessageStats().then(response => {
        if (response.code === 200) {
          this.stats = response.data;
          console.log("统计数据已更新:", this.stats);
        } else {
          this.$modal.msgError("获取统计数据失败: " + response.msg);
        }
      }).catch(err => {
        this.$modal.msgError("获取统计数据失败，请检查网络或联系管理员");
        console.error("getStats 失败:", err);
      });
    },

    // 【新增】处理导出
    handleExport() {
      this.exportLoading = true;
      // this.download 是若依的全局混入(mixin)
      // 它直接接收 URL、参数和文件名
      this.download(
        '/proj_cyq/message/export', // 后端 Controller 的 URL
        {},                         // 没有参数，传空对象
        '消息中心数据.xlsx'
      ).then(() => {
        this.exportLoading = false;
      }).catch(() => {
        this.exportLoading = false;
      });
    },

    // 处理消息列表，确保没有重复的key
    processMessageList(messages) {
      const seenKeys = new Set();

      return messages.map(message => {
        // 确保每个消息都有唯一的messageId
        let messageId = message.messageId;
        if (!messageId || messageId === 'todo_null' || messageId === 'homework_null') {
          // 生成新的唯一ID
          if (message.type === 'todo' && message.todoId) {
            messageId = `todo_${message.todoId}`;
          } else if (message.type === 'homework' && message.homeworkId) {
            messageId = `homework_${message.homeworkId}`;
          } else {
            // 如果还是没有有效ID，使用时间戳和随机数
            messageId = `${message.type}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
          }
          message.messageId = messageId;
        }

        // 检查是否重复，如果重复则重新生成
        let finalMessageId = messageId;
        let counter = 1;
        while (seenKeys.has(finalMessageId)) {
          finalMessageId = `${messageId}_${counter}`;
          counter++;
        }
        seenKeys.add(finalMessageId);

        return {
          ...message,
          messageId: finalMessageId,
          deleting: false, // 添加删除状态
          markingRead: false // 添加标记已读状态
        };
      });
    },
    // 生成唯一的key
    generateKey(message, index) {
      if (message.messageId && message.messageId !== 'todo_null' && message.messageId !== 'homework_null') {
        return message.messageId;
      }
      // 如果messageId无效，使用索引和其他属性组合
      return `${message.type}_${index}_${Date.now()}`;
    },
    // 【修改】刷新
    handleRefresh() {
      this.getList();
      this.getStats(); // 刷新时也刷新统计
    },
    // 返回个人中心
    handleBack() {
      this.$router.push("/proj_cyq");
    },
    // 【修改】标记单个消息已读
    handleMarkRead(message) {
      this.$set(message, 'markingRead', true);

      let promise;
      if (message.type === 'todo') {
        console.log('标记待办消息已读:', message.todoId);
        promise = markTodoAsRead(message.todoId);
      } else if (message.type === 'homework') {
        console.log('标记作业消息已读:', message.homeworkId);
        promise = markHomeworkAsRead(message.homeworkId);
      } else {
        promise = Promise.reject('未知的消息类型');
      }

      promise.then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess("标记已读成功");
          message.isRead = '1';
          this.getStats(); // <-- 刷新统计
          console.log('标记已读成功，消息ID:', message.messageId);
        } else {
          this.$modal.msgError(response.msg || "标记已读失败");
        }
      }).catch(error => {
        console.error('标记已读失败:', error);
        this.$modal.msgError("标记已读失败");
      }).finally(() => {
        this.$set(message, 'markingRead', false);
      });
    },
    // 【修改】标记全部已读
    handleMarkAllRead() {
      this.markingAllRead = true;
      markAllAsRead().then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess("全部标记已读成功");
          // 更新前端状态
          this.messageList.forEach(message => {
            message.isRead = '1';
          });
          this.getStats(); // <-- 刷新统计
          console.log('全部标记已读成功');
        } else {
          this.$modal.msgError(response.msg || "全部标记已读失败");
        }
      }).catch(error => {
        console.error('全部标记已读失败:', error);
        this.$modal.msgError("全部标记已读失败");
      }).finally(() => {
        this.markingAllRead = false;
      });
    },
    // 【修改】删除消息
    handleDelete(message, index) {
      this.$modal.confirm('是否确认删除该消息？').then(() => {
        // 设置删除状态
        this.$set(message, 'deleting', true);

        return deleteMessage(message.messageId);
      }).then(response => {
        if (response.code === 200) {
          // 从列表中移除消息
          this.messageList.splice(index, 1);
          this.$modal.msgSuccess("删除成功");
          this.getStats(); // <-- 刷新统计
        } else {
          this.$modal.msgError(response.msg || "删除失败");
        }
      }).catch((error) => {
        console.error('删除失败:', error);
        this.$modal.msgError("删除失败");
      }).finally(() => {
        // 重置删除状态
        this.$set(message, 'deleting', false);
      });
    },

    // (辅助方法)
    getStatValue(statType, name) {
      if (!this.stats[statType]) return 0;
      const item = this.stats[statType].find(s => s.name === name);
      return item ? item.value : 0;
    },

    // 获取消息类型标签样式
    getMessageType(type) {
      const typeMap = {
        'homework': 'success',
        'todo': 'warning'
      };
      return typeMap[type] || 'info';
    },
    // 获取类型文本
    getTypeText(type) {
      const textMap = {
        'homework': '作业',
        'todo': '待办'
      };
      return textMap[type] || '消息';
    },
    // 获取头像样式
    getAvatarStyle(type) {
      const styleMap = {
        'homework': { backgroundColor: '#67c23a' },
        'todo': { backgroundColor: '#e6a23c' }
      };
      return styleMap[type] || { backgroundColor: '#909399' };
    },
    // 获取头像图标
    getAvatarIcon(type) {
      const iconMap = {
        'homework': 'el-icon-document',
        'todo': 'el-icon-alarm-clock'
      };
      return iconMap[type] || 'el-icon-message';
    },
    // 时间格式化 - 手动实现
    parseTime(time) {
      if (!time) return '';

      try {
        const date = new Date(time);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');

        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
      } catch (e) {
        console.error('时间格式化错误:', e);
        return String(time);
      }
    }
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e6e6e6;
}

.message-list {
  border: 1px solid #e6e6e6;
  border-radius: 4px;
}

.message-item {
  display: flex;
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
  transition: background-color 0.3s;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.message-item.unread {
  background-color: #f0f9ff;
}

.message-item:last-child {
  border-bottom: none;
}

.message-avatar {
  margin-right: 15px;
  display: flex;
  align-items: center;
}

.message-content {
  flex: 1;
}

.message-title {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.title-text {
  font-weight: bold;
  margin-right: 10px;
}

.message-title .el-tag {
  margin-right: 8px;
}

.message-body {
  color: #606266;
  margin-bottom: 12px;
  line-height: 1.6;
}

.homework-info p,
.todo-info p {
  margin: 4px 0;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.message-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 80px;
}

.read-text {
  font-size: 12px;
  color: #67c23a;
}

.empty-state {
  padding: 40px 0;
}

/* 【新增】统计概览标题样式 */
.stats-title {
  font-size: 18px; /* 调整了字体大小 */
  color: #303133;
  margin-bottom: 15px; /* 增加了和下方卡片的间距 */
  font-weight: 600; /* 加粗一点 */
}

/* 统计概览样式 */
.stats-overview {
  margin-bottom: 20px;
}
.stat-item {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  text-align: center;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
}
.stat-item.unread .stat-value {
  color: #f56c6c;
}
.stat-item.todo .stat-value {
  color: #e6a23c;
}
.stat-item.homework .stat-value {
  color: #67c23a;
}
</style>
