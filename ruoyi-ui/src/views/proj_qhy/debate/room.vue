<template>
  <div class="room-container" v-loading="loading">
    <div class="room-header">
      <div class="header-left">
        <h2 class="debate-title">{{ debate.title }}</h2>
        <div class="status-badge" :class="statusClass">{{ statusText }}</div>
      </div>

      <div class="header-center" v-if="debate.status === '1'">
        <div class="turn-info">当前第 <span class="highlight-num">{{ debate.currentTurn }}</span> / 10 回合</div>

        <div class="timer" :class="{'urgent': remainingTime < 10}">
          <i class="el-icon-time"></i> {{ remainingTime }}s
        </div>

        <div class="turn-tips">
          当前发言：
          <span v-if="debate.currentRole === '1'" class="role-tag pro-tag">正方</span>
          <span v-else class="role-tag con-tag">反方</span>
        </div>
      </div>

      <div class="room-actions" v-if="isCreator">
        <el-button v-if="debate.status === '0'" icon="el-icon-setting" plain size="small" @click="handleEdit">设置</el-button>
        <el-button v-if="debate.status === '0'" type="primary" size="small" @click="handleStart">开始辩论</el-button>
        <el-button v-if="debate.status === '1'" type="danger" size="small" @click="handleStop">结束辩论</el-button>
      </div>
    </div>

    <div class="result-banner" v-if="debate.status === '2'">
      <div class="winner-trophy">
        <i class="el-icon-trophy"></i>
        获胜方：{{ debate.winner === '1' ? '正方' : (debate.winner === '2' ? '反方' : '平局') }}
      </div>
      <div class="final-score">
        <span class="score pro">{{ proVotes }}</span> : <span class="score con">{{ conVotes }}</span>
      </div>
    </div>

    <div class="debate-arena">
      <div class="side-panel pro-panel" :class="{'active-turn': isTurn('1')}">
        <div class="side-header pro-header">
          <div class="header-content-wrapper">
            <h3 class="viewpoint">正方: {{ debate.proViewpoint }}</h3>
            <div class="player-list">
              <span v-for="(name, index) in debate.proPlayerNames" :key="index" class="player-tag">
                <i class="el-icon-user"></i> {{ name }}
              </span>
              <span v-if="(!debate.proPlayerNames || debate.proPlayerNames.length === 0)" class="no-player">暂无选手</span>
            </div>
          </div>
          <div class="vote-count">👍 {{ proVotes }}</div>
        </div>

        <div class="msg-list" ref="proList">
          <div v-for="msg in proMsgs" :key="msg.id" class="msg-item pro-msg">
            <div class="msg-meta">{{ msg.nickName }} ({{ parseTime(msg.createTime, '{h}:{i}:{s}') }})</div>
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
        </div>
      </div>

      <div class="side-panel con-panel" :class="{'active-turn': isTurn('2')}">
        <div class="side-header con-header">
          <div class="header-content-wrapper">
            <h3 class="viewpoint">反方: {{ debate.conViewpoint }}</h3>
            <div class="player-list">
              <span v-for="(name, index) in debate.conPlayerNames" :key="index" class="player-tag">
                <i class="el-icon-user"></i> {{ name }}
              </span>
              <span v-if="(!debate.conPlayerNames || debate.conPlayerNames.length === 0)" class="no-player">暂无选手</span>
            </div>
          </div>
          <div class="vote-count">👍 {{ conVotes }}</div>
        </div>

        <div class="msg-list" ref="conList">
          <div v-for="msg in conMsgs" :key="msg.id" class="msg-item con-msg">
            <div class="msg-meta">{{ msg.nickName }} ({{ parseTime(msg.createTime, '{h}:{i}:{s}') }})</div>
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="room-footer">
      <div v-if="isPlayer" class="chat-input-area">
        <el-input
          v-model="msgContent"
          :placeholder="inputPlaceholder"
          @keyup.enter.native="handleSend"
          :disabled="!isMyTurn"
        >
          <el-button slot="append" icon="el-icon-s-promotion" @click="handleSend" :disabled="!isMyTurn">发送</el-button>
        </el-input>
      </div>

      <div v-if="isAudience && debate.status === '1'" class="vote-area">
        <span class="vote-label">请选择支持的一方：</span>
        <el-button
          :type="myVote === '1' ? 'primary' : ''"
          class="vote-btn pro-btn"
          @click="handleVote('1')"
          :disabled="myVote === '2'"
        >支持正方</el-button>
        <el-button
          :type="myVote === '2' ? 'danger' : ''"
          class="vote-btn con-btn"
          @click="handleVote('2')"
          :disabled="myVote === '1'"
        >支持反方</el-button>
      </div>

      <div v-if="debate.status !== '1' && !isPlayer" class="waiting-text">
        {{ debate.status === '0' ? '等待辩论开始...' : '辩论已结束' }}
      </div>
      <div v-if="debate.status === '1' && !isPlayer" class="waiting-text">
        正在发言：{{ debate.currentRole === '1' ? '正方' : '反方' }} (观众模式)
      </div>
    </div>

    <el-dialog title="修改辩论设置" :visible.sync="editOpen" width="500px" append-to-body>
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="辩题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="正方观点">
          <el-input v-model="editForm.proViewpoint" />
        </el-form-item>
        <el-form-item label="反方观点">
          <el-input v-model="editForm.conViewpoint" />
        </el-form-item>
        <el-form-item label="每轮时长">
          <el-input-number v-model="editForm.speechLimit" :min="10" :step="10" /> 秒
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEdit">保存</el-button>
        <el-button @click="editOpen = false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getRoomInfo,
  getMsgList,
  sendMsg,
  voteDebate,
  startDebate,
  stopDebate,
  updateDebate // 确保在 api/debate.js 中导出了 updateDebate
} from "@/api/proj_qhy/debate";

export default {
  name: "DebateRoom",
  data() {
    return {
      debateId: null,
      loading: true,
      debate: {
        title: '',
        status: '0',
        currentTurn: 0,
        currentRole: '',
        speechLimit: 60,
        proPlayerNames: [],
        conPlayerNames: []
      },
      myRole: '', // 1, 2, 3
      myVote: null,
      proVotes: 0,
      conVotes: 0,
      msgList: [],
      msgContent: '',

      // 轮询与倒计时
      timer: null,           // 轮询请求Timer
      countDownTimer: null,  // 本地倒计时Timer
      remainingTime: 0,

      // 编辑弹窗
      editOpen: false,
      editForm: {}
    };
  },
  computed: {
    isCreator() {
      return this.debate.createBy === this.$store.getters.name;
    },
    isPlayer() {
      return this.myRole === '1' || this.myRole === '2';
    },
    isAudience() {
      return this.myRole === '3';
    },
    // 是否轮到我发言
    isMyTurn() {
      if (this.debate.status !== '1') return false;
      return this.myRole === this.debate.currentRole;
    },
    inputPlaceholder() {
      if (!this.isPlayer) return "";
      if (this.debate.status === '0') return "等待开始...";
      if (this.debate.status === '2') return "辩论已结束";
      if (this.isMyTurn) return `轮到你了！剩余 ${this.remainingTime}秒`;
      return "对方正在发言...";
    },
    statusClass() {
      if (this.debate.status === '1') return 'ongoing';
      if (this.debate.status === '2') return 'finished';
      return 'waiting';
    },
    statusText() {
      const map = {'0': '未开始', '1': '进行中', '2': '已结束'};
      return map[this.debate.status] || '未知';
    },
    proMsgs() {
      return this.msgList.filter(m => m.role === '1');
    },
    conMsgs() {
      return this.msgList.filter(m => m.role === '2');
    }
  },
  created() {
    this.debateId = this.$route.params.id;
    this.initData();
    // 启动轮询 (3秒一次)
    this.timer = setInterval(this.refreshData, 3000);
    // 启动本地倒计时 (1秒一次)
    this.startLocalTimer();
  },
  destroyed() {
    if (this.timer) clearInterval(this.timer);
    if (this.countDownTimer) clearInterval(this.countDownTimer);
  },
  methods: {
    // 判断是否是某方的回合 (用于UI高亮)
    isTurn(role) {
      return this.debate.status === '1' && this.debate.currentRole === role;
    },

    initData() {
      getRoomInfo(this.debateId).then(res => {
        this.updateRoomState(res.data);
        this.loading = false;
        // 初次加载滚动到底部
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      });
      // 单独获取一次消息列表
      getMsgList(this.debateId).then(res => {
        this.msgList = res.data;
      });
    },

    refreshData() {
      // 1. 刷新房间信息
      getRoomInfo(this.debateId).then(res => {
        this.updateRoomState(res.data);
      });
      // 2. 刷新消息
      getMsgList(this.debateId).then(res => {
        const oldLen = this.msgList.length;
        this.msgList = res.data;
        // 有新消息时滚动
        if (this.msgList.length > oldLen) {
          this.$nextTick(() => this.scrollToBottom());
        }
      });
    },

    updateRoomState(data) {
      const oldTurn = this.debate.currentTurn;
      this.debate = data.debate;
      this.myRole = data.currentUserRole;
      this.myVote = data.currentVote;
      this.proVotes = data.proVotes;
      this.conVotes = data.conVotes;

      // --- 倒计时校准逻辑 ---
      if (this.debate.status === '1' && this.debate.turnStartTime) {
        // 计算逻辑：回合限时 - (当前时间 - 回合开始时间)
        const now = new Date().getTime();
        const startTime = new Date(this.debate.turnStartTime).getTime();
        const limit = (this.debate.speechLimit || 60) * 1000;

        const passed = now - startTime;
        const left = Math.max(0, Math.floor((limit - passed) / 1000));

        // 修正本地时间
        this.remainingTime = left;

        // 检测回合切换，提示用户
        if (oldTurn && oldTurn !== this.debate.currentTurn) {
          this.$notify.info({
            title: '回合切换',
            message: `第 ${this.debate.currentTurn} 回合开始，请 ${this.debate.currentRole === '1' ? '正方' : '反方'} 发言`
          });
        }
      } else {
        this.remainingTime = 0;
      }
    },

    startLocalTimer() {
      if (this.countDownTimer) clearInterval(this.countDownTimer);
      this.countDownTimer = setInterval(() => {
        if (this.debate.status === '1' && this.remainingTime > 0) {
          this.remainingTime--;
        }
      }, 1000);
    },

    handleStart() {
      this.$modal.confirm('确定开始辩论吗？').then(() => {
        startDebate(this.debateId).then(() => {
          this.$modal.msgSuccess("辩论开始");
          this.refreshData();
        });
      });
    },

    handleStop() {
      this.$modal.confirm('确定结束辩论并统计结果吗？').then(() => {
        stopDebate(this.debateId).then(() => {
          this.$modal.msgSuccess("辩论结束");
          this.refreshData();
        });
      });
    },

    // 打开编辑弹窗
    handleEdit() {
      this.editForm = {
        id: this.debate.id,
        title: this.debate.title,
        proViewpoint: this.debate.proViewpoint,
        conViewpoint: this.debate.conViewpoint,
        speechLimit: this.debate.speechLimit || 60
      };
      this.editOpen = true;
    },

    submitEdit() {
      updateDebate(this.editForm).then(() => {
        this.$modal.msgSuccess("设置修改成功");
        this.editOpen = false;
        this.refreshData();
      });
    },

    handleVote(side) {
      voteDebate({ debateId: this.debateId, side: side }).then(() => {
        this.myVote = side;
        this.$modal.msgSuccess("投票成功");
        this.refreshData();
      });
    },

    handleSend() {
      if (!this.msgContent.trim()) return;
      if (!this.isMyTurn) {
        this.$modal.msgError("还没轮到你发言");
        return;
      }
      sendMsg({ debateId: this.debateId, content: this.msgContent }).then(() => {
        this.msgContent = '';
        this.refreshData();
      });
    },

    scrollToBottom() {
      const proEl = this.$refs.proList;
      const conEl = this.$refs.conList;
      if (proEl) proEl.scrollTop = proEl.scrollHeight;
      if (conEl) conEl.scrollTop = conEl.scrollHeight;
    }
  }
};
</script>

<style scoped>
/* 1. 容器与整体布局 */
.room-container {
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  background-color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 2. 头部区域 */
.room-header {
  padding: 12px 24px;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  z-index: 10;
  height: 80px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.debate-title {
  margin: 0;
  font-size: 18px;
  color: #1d1d1f;
}
.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}
.ongoing { background-color: #34c759; }
.finished { background-color: #86868b; }
.waiting { background-color: #ff9500; }

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.turn-info {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 2px;
}
.highlight-num {
  font-weight: bold;
  color: #1d1d1f;
  font-size: 16px;
}
.timer {
  font-size: 26px;
  font-weight: 700;
  color: #1d1d1f;
  font-family: monospace;
  line-height: 1;
}
.timer.urgent {
  color: #ff3b30;
  animation: pulse 1s infinite;
}
.turn-tips {
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
}
.role-tag {
  font-weight: bold;
  padding: 1px 4px;
  border-radius: 4px;
}
.pro-tag { color: #0071e3; background: #e3f1fc; }
.con-tag { color: #ff3b30; background: #fcebeb; }

/* 3. 结果 Banner */
.result-banner {
  background: linear-gradient(90deg, #0071e3 0%, #ff3b30 100%);
  color: white;
  text-align: center;
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}
.winner-trophy { font-size: 18px; font-weight: bold; }
.final-score .pro { font-size: 22px; font-weight: bold; }
.final-score .con { font-size: 22px; font-weight: bold; }

/* 4. 辩论主区域 (Flexbox Fix) */
.debate-arena {
  flex: 1;
  display: flex;
  overflow: hidden;
  padding: 20px;
  gap: 20px;
}

.side-panel {
  flex: 1;
  background: white;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: all 0.5s ease;
  border: 3px solid transparent;
  opacity: 0.6; /* 默认变暗，非激活状态 */
}

/* 激活状态 */
.side-panel.active-turn {
  opacity: 1;
  transform: scale(1.005);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}
.pro-panel.active-turn { border-color: #0071e3; }
.con-panel.active-turn { border-color: #ff3b30; }
/* 结束状态下两边都亮 */
.room-container:not(:has(.ongoing)) .side-panel { opacity: 1; border: none; }

/* 面板头部 */
.side-header {
  padding: 16px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.header-content-wrapper { flex: 1; }
.viewpoint { margin: 0 0 8px 0; font-size: 16px; font-weight: 600; line-height: 1.4; }
.player-list { display: flex; flex-wrap: wrap; gap: 6px; }
.player-tag {
  background: rgba(0,0,0,0.2);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
}
.no-player { font-size: 11px; opacity: 0.7; font-style: italic; }
.vote-count {
  font-size: 18px;
  font-weight: bold;
  background: rgba(255,255,255,0.2);
  padding: 5px 12px;
  border-radius: 12px;
  margin-left: 12px;
  flex-shrink: 0;
}
.pro-header { background: #0071e3; }
.con-header { background: #ff3b30; }

/* 5. 消息列表 (Flexbox Fix - Critical) */
.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background: #f9f9fa;
  display: flex;
  flex-direction: column; /* 保证消息垂直排列 */
  gap: 16px; /* 消息间距 */
}

/* 消息项容器 */
.msg-item {
  display: flex;
  flex-direction: column;
  max-width: 100%;
}

.msg-meta { font-size: 11px; color: #86868b; margin-bottom: 4px; }
.msg-bubble {
  padding: 10px 16px;
  border-radius: 16px;
  max-width: 85%;
  line-height: 1.5;
  font-size: 14px;
  word-break: break-all; /* 强制换行修复 */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

/* 正方消息 (左对齐) */
.pro-msg { align-items: flex-start; }
.pro-msg .msg-meta { padding-left: 4px; }
.pro-msg .msg-bubble { background: #fff; color: #1d1d1f; border: 1px solid #e3f1fc; border-top-left-radius: 4px; }

/* 反方消息 (右对齐) */
.con-msg { align-items: flex-end; }
.con-msg .msg-meta { padding-right: 4px; }
.con-msg .msg-bubble { background: #fff; color: #1d1d1f; border: 1px solid #fcebeb; border-top-right-radius: 4px; }
/* 若需要彩色气泡可恢复:
   Pro: bg: #0071e3, color: white
   Con: bg: #ff3b30, color: white
   但Mac风格通常是气泡白底，边框或文字带色，这里为了清晰度使用白底。
*/

/* 6. 底部 Footer */
.room-footer {
  padding: 15px 30px;
  background: white;
  border-top: 1px solid #ebedf0;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80px;
}
.chat-input-area { width: 60%; }
.vote-area { display: flex; align-items: center; gap: 16px; }
.vote-label { color: #86868b; font-size: 14px; }
.vote-btn { width: 120px; font-weight: 500; }
.waiting-text { color: #86868b; font-style: italic; font-size: 14px; }

/* 动画 */
@keyframes pulse {
  0% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.1); }
  100% { opacity: 1; transform: scale(1); }
}
</style>
