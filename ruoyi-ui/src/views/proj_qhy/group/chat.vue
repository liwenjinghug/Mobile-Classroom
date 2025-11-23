<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="back-link" @click="$router.back()">
        <i class="el-icon-arrow-left"></i>
        <span>小组讨论</span>
      </div>
      <div class="group-name">{{ groupInfo.groupName }} ({{ members.length }})</div>
      <div
        class="details-btn"
        @click="!isReadOnly && (detailsVisible = true)"
        :style="{ visibility: isReadOnly ? 'hidden' : 'visible' }"
      >
        <i class="el-icon-more"></i>
      </div>
    </div>

    <div class="message-list" ref="messageList">
      <div v-for="(msg, index) in messages" :key="msg.messageId">
        <div class="message-time" v-if="shouldShowTime(msg, index)">
          {{ formatTime(msg.createTime, 'yyyy/MM/dd HH:mm') }}
        </div>

        <div :class="getMessageClass(msg)">
          <el-avatar class="avatar" :size="35" :src="getImageUrl(msg.senderAvatar)" />
          <div class="message-content">
            <div class="sender-name">{{ msg.senderNickName }}</div>
            <div
              class="bubble"
              @contextmenu.prevent="handleMessageRightClick(msg, $event)"
            >
              <span v-if="msg.messageType === '0'">{{ msg.content }}</span>
              <el-image
                v-if="msg.messageType === '1'"
                class="chat-image"
                :src="getImageUrl(msg.content)"
                :preview-src-list="[getImageUrl(msg.content)]"
                fit="cover"
              />
              <span v-if="msg.messageType === '9'" class="system-message">{{ msg.content }}</span>

              <div
                v-if="msg.messageType === '2'"
                class="article-card"
                @click="goToArticle(parseArticleCard(msg.content).id)"
              >
                <div class="article-card-title">{{ parseArticleCard(msg.content).title }}</div>
                <div class="article-card-digest">{{ parseArticleCard(msg.content).digest || '点击查看详情' }}</div>
                <div class="article-card-cover" v-if="parseArticleCard(msg.content).cover">
                  <el-image
                    :src="getImageUrl(parseArticleCard(msg.content).cover)"
                    fit="cover"
                  />
                </div>
                <div class="article-card-footer">学习社区文章</div>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="input-area" v-if="!isReadOnly">
      <el-upload
        action="#"
        :auto-upload="false"
        :on-change="handleImageChange"
        :show-file-list="false"
        accept="image/*"
        class="upload-btn"
      >
        <i class="el-icon-picture-outline"></i>
      </el-upload>
      <el-input
        v-model="newMessage"
        placeholder="输入消息..."
        @keyup.enter.native="handleSend"
      />
      <el-button type="primary" @click="handleSend" :disabled="!newMessage && !imageFile">发送</el-button>
    </div>

    <div class="read-only-banner" v-else>
      你已不是小组成员
    </div>


    <el-dialog
      :visible.sync="detailsVisible"
      width="400px"
      append-to-body
      v-if="!isReadOnly"
    >
      <div slot="title" class="dialog-title">小组信息</div>
      <div class="group-details">
        <el-form label-position="top">
          <el-form-item label="小组名称">
            <el-input v-model="groupInfo.groupName" :disabled="!isOwner" />
            <el-button v-if="isOwner" size="mini" @click="updateGroupName" style="margin-top: 5px;">保存名称</el-button>
          </el-form-item>

          <el-form-item label="小组头像">
            <el-image
              style="width: 100px; height: 100px; border-radius: 6px;"
              :src="getImageUrl(groupInfo.avatar)"
              :preview-src-list="[getImageUrl(groupInfo.avatar)]"
              fit="cover"
            >
              <div slot="error" class="image-slot">
                <i style="font-size: 30px;" class="el-icon-picture-outline"></i>
              </div>
            </el-image>
            <el-upload
              v-if="isOwner"
              action="#"
              :auto-upload="false"
              :on-change="handleAvatarChange"
              :show-file-list="false"
              accept="image/*"
              style="margin-top: 10px;"
            >
              <el-button size="small" type="primary">点击更换头像</el-button>
            </el-upload>
          </el-form-item>

          <el-form-item label="小组号">
            <el-input :value="groupInfo.groupNumber" disabled />
          </el-form-item>
          <el-form-item label="创建人">
            <el-input :value="groupInfo.ownerNickName" disabled />
          </el-form-item>
          <el-form-item label="创建时间">
            <el-input :value="formatTime(groupInfo.createTime, 'yyyy/MM/dd HH:mm')" disabled />
          </el-form-item>
        </el-form>

        <div class="dialog-actions">
          <el-button type="danger" @click="handleExitGroup" style="width: 100%;">退出小组</el-button>
          <el-button
            type="danger"
            plain
            @click="handleDisbandGroup"
            v-if="isOwner"
            style="width: 100%; margin-top: 10px; margin-left: 0;"
          >
            解散小组
          </el-button>
        </div>

        <div class="member-list">
          <div class="member-list-title">小组成员 ({{ members.length }})</div>
          <div
            v-for="member in members"
            :key="member.userId"
            class="member-item"
            @contextmenu.prevent="isOwner && member.userId !== currentUserId ? showRemoveMenu(member, $event) : null"
          >
            <el-avatar :size="40" :src="getImageUrl(member.avatar)" />
            <span class="member-name">{{ member.nickName }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      title="分享成功"
      :visible.sync="shareSuccessVisible"
      width="300px"
      :close-on-click-modal="false"
      :show-close="false"
      append-to-body
    >
      <span>已成功分享到小组！</span>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleStayInChat">留在聊天室</el-button>
        <el-button type="primary" @click="handleReturnToArticle">返回文章</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import groupApi from '@/api/proj_qhy/group'
import store from '@/store'

// (辅助函数) 时间格式化
function formatTime(date, fmt = 'yyyy/MM/dd HH:mm') {
  if (!date) return ''
  let dateObj
  if (typeof date === 'string') {
    dateObj = new Date(date.replace(/-/g, '/'))
  } else if (date instanceof Date) {
    dateObj = date
  } else {
    dateObj = new Date(date)
  }
  if (isNaN(dateObj.getTime())) {
    return ''
  }
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (dateObj.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  const o = {
    'M+': dateObj.getMonth() + 1,
    'd+': dateObj.getDate(),
    'H+': dateObj.getHours(),
    'm+': dateObj.getMinutes(),
    's+': dateObj.getSeconds()
  }
  for (const k in o) {
    if (new RegExp(`(${k})`).test(fmt)) {
      const str = o[k] + ''
      fmt = fmt.replace(RegExp.$1, RegExp.$1.length === 1 ? str : ('00' + str).substr(str.length))
    }
  }
  return fmt
}

export default {
  name: 'GroupChat',
  data() {
    return {
      groupId: null,
      groupInfo: {},
      members: [],
      messages: [],
      newMessage: '',
      imageFile: null,
      detailsVisible: false,
      currentUserId: store.getters.userId,
      defaultAvatar: require('@/assets/images/profile.jpg'),
      isReadOnly: false, // 是否为只读 (已退出)

      // (分享相关)
      shareSuccessVisible: false,
      returnToPath: null
    }
  },
  computed: {
    isOwner() {
      return this.groupInfo.ownerUserId && this.groupInfo.ownerUserId === this.currentUserId
    }
  },
  created() {
    this.groupId = this.$route.params.groupId
    this.loadChat()
    this.checkShareSuccess(); // (检查分享)
  },
  activated() {
    this.loadChat()
    this.checkShareSuccess(); // (检查分享)
  },
  methods: {
    // (辅助函数) 获取图片 URL
    getImageUrl(url) {
      if (!url) return this.defaultAvatar
      if (url.startsWith('/profile')) {
        return process.env.VUE_APP_BASE_API + url
      }
      if (url.startsWith('http')) {
        return url
      }
      return url
    },
    // 引用顶层函数
    formatTime,

    // --- 核心业务 ---
    loadChat() {
      // 1. 获取详情
      groupApi.getGroupDetails(this.groupId).then(res => {
        this.groupInfo = res.data
        this.members = res.data.members
        this.isReadOnly = this.groupInfo.currentUserStatus === '2'
      }).catch(err => {
        this.$message.error("无法加载小组信息，您可能已非小组成员。")
        this.$router.push('/proj_qhy/group')
      })
      // 2. 获取消息
      this.loadMessages()
    },
    loadMessages() {
      groupApi.getChatMessages(this.groupId).then(res => {
        this.messages = res.data
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      })
    },
    handleSend() {
      if (!this.newMessage && !this.imageFile) return
      const data = { content: this.newMessage }
      if(this.imageFile) { data.content = '' }
      groupApi.sendMessage(this.groupId, data, this.imageFile).then(() => {
        this.newMessage = ''
        this.imageFile = null
        this.loadMessages()
      }).catch(err => {
        this.$message.error(err.msg || "发送失败，您可能已被移出小组。")
      })
    },
    handleImageChange(file) {
      this.imageFile = file
      this.newMessage = `[图片]`
      this.handleSend()
    },

    // --- (新增) 分享相关方法 ---
    checkShareSuccess() {
      const { shareSuccess, returnToPath } = this.$route.query;
      if (shareSuccess === 'true') {
        this.shareSuccessVisible = true;
        this.returnToPath = returnToPath || '/proj_qhy/article';
        // 清除 query 标记
        this.$router.replace({ path: this.$route.path, query: null });
      }
    },
    handleStayInChat() {
      this.shareSuccessVisible = false;
      this.returnToPath = null;
    },
    handleReturnToArticle() {
      this.shareSuccessVisible = false;
      if (this.returnToPath) {
        this.$router.push(this.returnToPath);
      }
    },
    parseArticleCard(content) {
      try {
        return JSON.parse(content);
      } catch (e) {
        return { title: '文章加载失败', digest: '请检查内容', id: null };
      }
    },
    goToArticle(id) {
      if (id) {
        this.$router.push('/proj_qhy/article/detail/' + id);
      }
    },

    // --- 详情弹窗方法 ---
    updateGroupName() {
      const data = {
        groupId: this.groupInfo.groupId,
        groupName: this.groupInfo.groupName
      }
      groupApi.updateGroupInfo(data).then(() => {
        this.$message.success('小组名称修改成功')
      })
    },
    handleAvatarChange(file) {
      if (!file) return
      this.$confirm('确定要上传并更换这个头像吗?', '提示', {
        type: 'info'
      }).then(() => {
        groupApi.uploadGroupAvatar(this.groupInfo.groupId, file).then(res => {
          this.$message.success('头像更换成功')
          this.groupInfo.avatar = res.data
        }).catch(() => {
          this.$message.error('上传失败')
        })
      }).catch(() => {});
    },
    showRemoveMenu(member, event) {
      this.$confirm(`确定要将 ${member.nickName} 移除小组吗?`, '提示', {
        confirmButtonText: '确定移除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.handleRemoveMember(member)
      }).catch(() => {});
    },
    handleRemoveMember(member) {
      groupApi.removeMember(this.groupId, member.userId).then(() => {
        this.$message.success('移除成功')
        this.detailsVisible = false
        this.loadChat()
      })
    },
    handleExitGroup() {
      const confirmText = this.isOwner
        ? "您是组长，退出后组长将自动转让。确定退出吗？"
        : "确定要退出该小组吗？"
      this.$confirm(confirmText, '提示', {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        groupApi.exitGroup(this.groupId).then(() => {
          this.$message.success("已退出小组")
          this.detailsVisible = false
          this.$router.push('/proj_qhy/group')
        })
      }).catch(() => {});
    },
    handleDisbandGroup() {
      this.$confirm('解散小组后，所有聊天记录将无法查看，且无法恢复。', '警告：确定解散小组？', {
        confirmButtonText: '确定解散',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => {
        groupApi.disbandGroup(this.groupId).then(() => {
          this.$message.success("小组已解散")
          this.detailsVisible = false
          this.$router.push('/proj_qhy/group')
        })
      }).catch(() => {});
    },

    // --- 消息列表方法 ---
    handleMessageRightClick(msg) {
      if (msg.senderUserId !== this.currentUserId) return
      if (msg.messageType !== '0' && msg.messageType !== '1') return
      const msgTime = new Date(msg.createTime.replace(/-/g, '/')).getTime()
      const now = new Date().getTime()
      if (now - msgTime > (2 * 60 * 1000)) {
        this.$message.warning("已超过2分钟，无法撤回")
        return
      }
      this.$confirm('确定要撤回这条消息吗?', '提示', {
        type: 'warning'
      }).then(() => {
        groupApi.recallMessage(msg.messageId).then(() => {
          this.$message.success("撤回成功")
          this.loadMessages()
        }).catch(err => {
          this.$message.error(err.msg || '撤回失败')
        });
      }).catch(() => {});
    },
    getMessageClass(msg) {
      if (msg.messageType === '9') return 'message-item system'
      return msg.senderUserId === this.currentUserId ? 'message-item sent' : 'message-item received'
    },
    shouldShowTime(msg, index) {
      if (index === 0) return true
      const prevMsg = this.messages[index - 1]
      if (!prevMsg || !prevMsg.createTime || !msg.createTime) return true
      const currentTimeStr = formatTime(msg.createTime, 'yyyy/MM/dd HH:mm')
      const prevTimeStr = formatTime(prevMsg.createTime, 'yyyy/MM/dd HH:mm')
      return currentTimeStr !== prevTimeStr
    },
    scrollToBottom() {
      const el = this.$refs.messageList
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    }
  }
}
</script>

<style scoped>
/* Mac Style for Group Chat */
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  background-color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

/* Header */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
  z-index: 10;
}

.back-link {
  cursor: pointer;
  font-size: 15px;
  color: #0071e3;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.group-name {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
}

.details-btn {
  cursor: pointer;
  font-size: 20px;
  color: #0071e3;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.details-btn:hover {
  background-color: rgba(0, 113, 227, 0.1);
}

.details-btn[style*="visibility: hidden"] {
  cursor: default;
}

/* Message List */
.message-list {
  flex-grow: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-time {
  text-align: center;
  font-size: 12px;
  color: #86868b;
  margin: 8px 0;
  font-weight: 500;
}

.message-item {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 80%;
}

.message-item .avatar {
  flex-shrink: 0;
  border: 1px solid rgba(0,0,0,0.05);
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sender-name {
  font-size: 12px;
  color: #86868b;
  margin-left: 4px;
}

.bubble {
  padding: 0;
  border-radius: 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  overflow: hidden;
}

.bubble span {
  padding: 10px 16px;
  display: inline-block;
  background-color: #ffffff;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.5;
  color: #1d1d1f;
}

.chat-image {
  width: 240px;
  border-radius: 12px;
  display: block;
}

/* System Message */
.message-item.system {
  justify-content: center;
  max-width: 100%;
}

.message-item.system .bubble {
  box-shadow: none;
  background: none;
}

.message-item.system .bubble span.system-message {
  background-color: rgba(0,0,0,0.05);
  color: #86868b;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 980px;
}

.message-item.system .avatar, .message-item.system .sender-name {
  display: none;
}

/* Sent Message */
.message-item.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.sent .message-content {
  align-items: flex-end;
}

.message-item.sent .sender-name {
  display: none;
}

.message-item.sent .bubble span {
  background-color: #0071e3;
  color: #ffffff;
}

.message-item.sent .bubble {
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}

/* Input Area */
.input-area {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
  gap: 12px;
}

.upload-btn {
  font-size: 24px;
  cursor: pointer;
  color: #86868b;
  transition: color 0.2s;
  display: flex;
  align-items: center;
}

.upload-btn:hover {
  color: #0071e3;
}

.input-area >>> .el-input__inner {
  border-radius: 20px;
  border: 1px solid #d2d2d7;
  background-color: #f5f5f7;
  height: 40px;
  transition: all 0.2s ease;
}

.input-area >>> .el-input__inner:focus {
  background-color: #ffffff;
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

.input-area >>> .el-button {
  border-radius: 980px;
  padding: 10px 20px;
  font-weight: 500;
  background-color: #0071e3;
  border: none;
  transition: all 0.2s ease;
}

.input-area >>> .el-button:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

.input-area >>> .el-button.is-disabled {
  background-color: #e5e5ea;
  color: #86868b;
}

/* Read Only Banner */
.read-only-banner {
  text-align: center;
  padding: 16px;
  background-color: #f5f5f7;
  color: #86868b;
  font-size: 14px;
  flex-shrink: 0;
  border-top: 1px solid rgba(0,0,0,0.05);
}

/* Dialog Styling */
.chat-container >>> .el-dialog {
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}

.chat-container >>> .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
}

.chat-container >>> .el-dialog__title {
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

.chat-container >>> .el-dialog__body {
  padding: 24px;
}

.chat-container >>> .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
}

.dialog-title {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f5f7;
  color: #86868b;
}

.dialog-actions {
  margin-top: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f5f5f7;
  margin-bottom: 24px;
}

.member-list {
  max-height: 240px;
  overflow-y: auto;
}

.member-list-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: #1d1d1f;
  font-size: 15px;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  cursor: pointer;
  transition: background-color 0.2s;
  border-radius: 8px;
}

.member-item:hover {
  background-color: #f5f5f7;
}

.member-name {
  margin-left: 12px;
  font-size: 14px;
  color: #1d1d1f;
}

/* Article Card */
.article-card {
  width: 260px;
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid rgba(0,0,0,0.05);
}

.message-item.sent .article-card {
  background-color: rgba(255,255,255,0.9);
  border: none;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.article-card-title {
  font-size: 15px;
  font-weight: 600;
  padding: 12px 12px 4px;
  color: #1d1d1f;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.article-card-digest {
  font-size: 13px;
  color: #86868b;
  padding: 0 12px 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-card-cover {
  width: 100%;
  height: 140px;
}

.article-card-cover .el-image {
  width: 100%;
  height: 100%;
}

.article-card-footer {
  font-size: 12px;
  color: #86868b;
  padding: 8px 12px;
  border-top: 1px solid rgba(0,0,0,0.05);
  background-color: #fbfbfd;
}
</style>
