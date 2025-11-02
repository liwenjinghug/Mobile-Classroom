<template>
  <div class="app-container">
    <div class="message-header">
      <h3>消息提示</h3>
      <div class="header-actions">
        <el-button @click="handleRefresh" icon="el-icon-refresh">刷新</el-button>
        <el-button @click="handleMarkAllRead" type="primary" :loading="markingAllRead">全部标记已读</el-button>
        <el-button icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>
    </div>

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

          <!-- 作业消息内容 -->
          <div v-if="message.type === 'homework'" class="message-body">
            <div class="homework-info">
              <p><strong>作业名称：</strong>{{ message.homeworkName }}</p>
              <p><strong>作业内容：</strong>{{ message.content }}</p>
              <p><strong>截止时间：</strong>{{ parseTime(message.deadline) }}</p>
            </div>
          </div>

          <!-- 待办消息内容 -->
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
          <!-- 标记已读按钮 -->
          <el-button
            v-if="message.isRead === '0'"
            type="text"
            @click="handleMarkRead(message)"
            :loading="message.markingRead"
          >{{ message.markingRead ? '标记中...' : '标记已读' }}</el-button>
          <span v-else class="read-text">已读</span>

          <!-- 删除按钮 -->
          <el-button
            type="text"
            @click="handleDelete(message, index)"
            style="color: #f56c6c; margin-top: 5px;"
            :loading="message.deleting"
          >{{ message.deleting ? '删除中...' : '删除' }}</el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="messageList.length === 0" class="empty-state">
        <el-empty description="暂无消息"></el-empty>
      </div>
    </div>
  </div>
</template>

<script>
import { listMessage, markTodoAsRead, markHomeworkAsRead, markAllAsRead, deleteMessage } from "@/api/proj_cyq/message";

export default {
  name: "Message",
  data() {
    return {
      // 消息列表
      messageList: [],
      // 全部标记已读加载状态
      markingAllRead: false
    };
  },
  created() {
    this.getList();
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
    // 刷新
    handleRefresh() {
      this.getList();
    },
    // 返回个人中心
    handleBack() {
      this.$router.push("/proj_cyq");
    },
    // 标记单个消息已读
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
    // 标记全部已读
    handleMarkAllRead() {
      this.markingAllRead = true;
      markAllAsRead().then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess("全部标记已读成功");
          // 更新前端状态
          this.messageList.forEach(message => {
            message.isRead = '1';
          });
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
    // 删除消息
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
</style>
