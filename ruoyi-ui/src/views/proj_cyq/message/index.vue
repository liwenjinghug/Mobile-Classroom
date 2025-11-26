<template>
  <div class="app-container">
    <div class="message-header no-print">
      <h3>消息提示</h3>
      <div class="header-actions">
        <el-button @click="handlePrint" icon="el-icon-printer">打印</el-button>

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

    <h2 class="print-title" style="display: none;">消息中心报表</h2>

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
      <el-col :span="4">
        <div class="stat-item todo">
          <div class="stat-label">待办事项</div>
          <div class="stat-value">{{ getStatValue('typeStats', '待办事项') }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-item homework">
          <div class="stat-label">作业消息</div>
          <div class="stat-value">{{ getStatValue('typeStats', '作业消息') }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-item exam">
          <div class="stat-label">考试通知</div>
          <div class="stat-value">{{ getStatValue('typeStats', '考试通知') }}</div>
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
        <div class="message-avatar no-print">
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
              class="no-print-border"
            >
              {{ getTypeText(message.type) }}
            </el-tag>
            <el-tag v-if="message.isRead === '0'" type="danger" size="mini" class="no-print">未读</el-tag>
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

          <div v-else-if="message.type === 'exam'" class="message-body">
            <div class="exam-info">
              <p><strong>考试名称：</strong>{{ message.examName }}</p>
              <p><strong>考试时间：</strong>{{ parseTime(message.startTime) }} ~ {{ parseTime(message.endTime) }}</p>
              <p><strong>考试时长：</strong>{{ message.duration }} 分钟</p>
              <el-button type="primary" size="mini" @click="handleTakeExam(message.examId)" style="margin-top: 5px;" class="no-print">去考试</el-button>
            </div>
          </div>

          <div class="message-meta">
            <span class="sender">发送者：{{ message.sender }}</span>
            <span class="time">发送时间：{{ parseTime(message.sendTime) }}</span>
          </div>
        </div>

        <div class="message-actions no-print">
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
import {
  listMessage,
  markTodoAsRead,
  markHomeworkAsRead,
  markExamAsRead,
  markAllAsRead,
  deleteMessage,
  getMessageStats
} from "@/api/proj_cyq/message";

export default {
  name: "Message",
  data() {
    return {
      messageList: [],
      markingAllRead: false,
      exportLoading: false,
      stats: {
        totalCount: 0,
        unreadCount: 0,
        typeStats: [],
        readStats: []
      }
    };
  },
  created() {
    this.getList();
    this.getStats();
  },
  methods: {
    /** 获取消息列表 */
    getList() {
      listMessage().then(response => {
        this.messageList = this.processMessageList(response.data || []);
      });
    },

    /** 获取统计数据 */
    getStats() {
      getMessageStats().then(response => {
        if (response.code === 200) {
          this.stats = response.data;
        } else {
          this.$modal.msgError("获取统计数据失败: " + response.msg);
        }
      }).catch(err => {
        this.$modal.msgError("获取统计数据失败，请检查网络或联系管理员");
      });
    },

    /** 导出 */
    handleExport() {
      this.exportLoading = true;
      this.download(
        '/proj_cyq/message/export',
        {},
        '消息中心数据.xlsx'
      ).then(() => {
        this.exportLoading = false;
      }).catch(() => {
        this.exportLoading = false;
      });
    },

    /** 打印 */
    handlePrint() {
      window.print();
    },

    /** 跳转去考试入口 */
    handleTakeExam(examId) {
      // 跳转到考试入口列表页
      this.$router.push("/proj_lwj_exam/exam_portal");
    },

    /** 处理消息列表，生成唯一ID */
    processMessageList(messages) {
      const seenKeys = new Set();
      return messages.map(message => {
        let messageId = message.messageId;
        // 如果ID无效，重新生成
        if (!messageId || messageId.endsWith('null')) {
          if (message.type === 'todo' && message.todoId) {
            messageId = `todo_${message.todoId}`;
          } else if (message.type === 'homework' && message.homeworkId) {
            messageId = `homework_${message.homeworkId}`;
          } else if (message.type === 'exam' && message.examId) {
            messageId = `exam_${message.examId}`;
          } else {
            messageId = `${message.type}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
          }
          message.messageId = messageId;
        }

        // 确保ID唯一
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
          deleting: false,
          markingRead: false
        };
      });
    },

    /** 生成列表 Key */
    generateKey(message, index) {
      if (message.messageId && !message.messageId.endsWith('null')) {
        return message.messageId;
      }
      return `${message.type}_${index}_${Date.now()}`;
    },

    /** 刷新 */
    handleRefresh() {
      this.getList();
      this.getStats();
    },

    /** 返回 */
    handleBack() {
      this.$router.push("/proj_cyq");
    },

    /** 标记单个已读 */
    handleMarkRead(message) {
      this.$set(message, 'markingRead', true);
      let promise;
      if (message.type === 'todo') {
        promise = markTodoAsRead(message.todoId);
      } else if (message.type === 'homework') {
        promise = markHomeworkAsRead(message.homeworkId);
      } else if (message.type === 'exam') {
        promise = markExamAsRead(message.examId);
      } else {
        promise = Promise.reject('未知的消息类型');
      }

      promise.then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess("标记已读成功");
          message.isRead = '1';
          this.getStats(); // 刷新统计
        } else {
          this.$modal.msgError(response.msg || "标记已读失败");
        }
      }).catch(error => {
        this.$modal.msgError("标记已读失败");
      }).finally(() => {
        this.$set(message, 'markingRead', false);
      });
    },

    /** 全部标记已读 */
    handleMarkAllRead() {
      this.markingAllRead = true;
      markAllAsRead().then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess("全部标记已读成功");
          this.messageList.forEach(message => {
            message.isRead = '1';
          });
          this.getStats();
        } else {
          this.$modal.msgError(response.msg || "全部标记已读失败");
        }
      }).catch(error => {
        this.$modal.msgError("全部标记已读失败");
      }).finally(() => {
        this.markingAllRead = false;
      });
    },

    /** 删除消息 */
    handleDelete(message, index) {
      this.$modal.confirm('是否确认删除该消息？').then(() => {
        this.$set(message, 'deleting', true);
        return deleteMessage(message.messageId);
      }).then(response => {
        if (response.code === 200) {
          this.messageList.splice(index, 1);
          this.$modal.msgSuccess("删除成功");
          this.getStats();
        } else {
          this.$modal.msgError(response.msg || "删除失败");
        }
      }).catch((error) => {
        this.$modal.msgError("删除失败");
      }).finally(() => {
        this.$set(message, 'deleting', false);
      });
    },

    /** 辅助：获取统计值 */
    getStatValue(statType, name) {
      if (!this.stats[statType]) return 0;
      const item = this.stats[statType].find(s => s.name === name);
      return item ? item.value : 0;
    },

    /** 辅助：获取消息类型样式 */
    getMessageType(type) {
      const typeMap = { 'homework': 'success', 'todo': 'warning', 'exam': 'danger' };
      return typeMap[type] || 'info';
    },
    getTypeText(type) {
      const textMap = { 'homework': '作业', 'todo': '待办', 'exam': '考试' };
      return textMap[type] || '消息';
    },
    getAvatarStyle(type) {
      const styleMap = {
        'homework': { backgroundColor: '#67c23a' },
        'todo': { backgroundColor: '#e6a23c' },
        'exam': { backgroundColor: '#f56c6c' }
      };
      return styleMap[type] || { backgroundColor: '#909399' };
    },
    getAvatarIcon(type) {
      const iconMap = {
        'homework': 'el-icon-document',
        'todo': 'el-icon-alarm-clock',
        'exam': 'el-icon-edit-outline'
      };
      return iconMap[type] || 'el-icon-message';
    },
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
        return String(time);
      }
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.message-header h3 {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  color: #1d1d1f;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.stats-overview {
  margin-bottom: 32px;
}

.stat-item {
  padding: 24px;
  background-color: #ffffff;
  border-radius: 18px;
  text-align: center;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  transition: transform 0.2s;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
}

.stat-item.unread .stat-value { color: #ff3b30; }
.stat-item.todo .stat-value { color: #ff9500; }
.stat-item.homework .stat-value { color: #34c759; }
.stat-item.exam .stat-value { color: #f56c6c; }

.message-list {
  background: #ffffff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.message-item {
  display: flex;
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
  transition: background-color 0.2s;
}

.message-item:hover {
  background-color: #fbfbfd;
}

.message-item.unread {
  background-color: rgba(0, 113, 227, 0.03);
}

.message-item:last-child {
  border-bottom: none;
}

.message-avatar {
  margin-right: 20px;
}

.message-content {
  flex: 1;
}

.message-title {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.title-text {
  font-weight: 600;
  font-size: 16px;
  margin-right: 12px;
  color: #1d1d1f;
}

.message-body {
  color: #424245;
  margin-bottom: 12px;
  line-height: 1.5;
  font-size: 14px;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #86868b;
}

.message-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 80px;
}

.read-text {
  font-size: 12px;
  color: #34c759;
  font-weight: 500;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
  color: #86868b;
}

.app-container >>> .el-button {
  border-radius: 980px;
  font-weight: 500;
  border: none;
  padding: 10px 20px;
}

.app-container >>> .el-button--primary {
  background-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}

.app-container >>> .el-button--default {
  background-color: #e5e5ea;
  color: #1d1d1f;
}

.app-container >>> .el-button--default:hover {
  background-color: #d1d1d6;
}

.app-container >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

/* ==============================
   【新增】打印样式专用设置
   ============================== */
@media print {
  .no-print,
  .navbar,
  .sidebar-container,
  .tags-view-container,
  .el-dialog__wrapper,
  .v-modal {
    display: none !important;
  }

  .app-container {
    padding: 0;
    margin: 0;
    background-color: white;
    width: 100%;
  }

  .print-title {
    display: block !important;
    text-align: center;
    font-size: 24px;
    margin-bottom: 30px;
    font-weight: bold;
  }

  .stats-overview {
    margin-bottom: 30px;
    border: 1px solid #ddd;
    padding: 15px;
    border-radius: 8px;
  }
  .stat-item {
    box-shadow: none;
    border: 1px solid #eee;
    padding: 15px;
  }

  .message-list {
    box-shadow: none;
    border: 1px solid #ddd;
  }
  .message-item {
    border-bottom: 1px solid #000; /* 打印时用深色分割线 */
    padding: 15px;
    page-break-inside: avoid; /* 防止消息被分页截断 */
  }

  .title-text, .message-body, .stat-value {
    color: #000 !important;
  }

  .el-tag {
    border: 1px solid #000 !important;
    background: none !important;
    color: #000 !important;
    padding: 0 5px !important;
  }

  .message-item.unread {
    background-color: transparent !important;
  }
}
</style>
