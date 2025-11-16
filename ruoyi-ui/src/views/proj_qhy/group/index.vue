<template>
  <div class="group-container">
    <div class="top-bar">
      <el-button type="success" icon="el-icon-plus" @click="handleCreateGroup">创建小组</el-button>
      <el-button type="primary" icon="el-icon-search" @click="handleShowJoinDialog">加入小组</el-button>
    </div>

    <div class="group-list">
      <div
        v-for="group in groupList"
        :key="group.groupId"
        class="group-item"
        @click="goToChat(group.groupId)"
      >
        <div class="avatar-container">
          <el-avatar :size="50" :src="getImageUrl(group.avatar)" />
          <span class="unread-dot" v-if="group.unreadCount > 0">{{ group.unreadCount }}</span>
        </div>

        <div class="group-info">
          <div class="header">
            <span class="group-name">{{ group.groupName }}</span>
            <el-tag type="info" size="mini" v-if="group.memberStatus === '2'">已退出</el-tag>
            <span class="time">{{ formatTime(group.latestMessage.createTime) }}</span>
          </div>
          <div class="latest-message">
            <span class="sender">{{ group.latestMessage.senderNickName }}: </span>
            <span class="content" v-if="group.latestMessage.messageType === '0'">
              {{ group.latestMessage.content }}
            </span>
            <span class="content" v-else-if="group.latestMessage.messageType === '1'">
              [图片]
            </span>
            <span class="content" v-else>
              [系统消息]
            </span>
          </div>
        </div>
      </div>

      <el-empty v-if="!groupList.length" description="您还没有加入任何小组"></el-empty>
    </div>

    <el-dialog title="创建小组" :visible.sync="createDialogVisible" width="400px">
      <el-form :model="createForm" ref="createForm">
        <el-form-item label="小组名称" prop="groupName" required>
          <el-input v-model="createForm.groupName" placeholder="请输入小组名称"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateGroup">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="加入小组" :visible.sync="joinDialogVisible" width="500px">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-radio-group v-model="searchForm.type">
            <el-radio label="number">按小组号</el-radio>
            <el-radio label="name">按名称</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item style="flex-grow: 1;">
          <el-input v-model="searchForm.query" placeholder="输入小组号或名称"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearchGroup">搜索</el-button>
        </el-form-item>
      </el-form>

      <div class="search-results">
        <el-empty v-if="!searchResults.length" description="暂无结果"></el-empty>
        <div v-for="group in searchResults" :key="group.groupId" class="result-item">
          <el-avatar :size="40" :src="getImageUrl(group.avatar)" />
          <div class="result-info">
            <div class="result-name">{{ group.groupName }}</div>
            <div class="result-number">小组号: {{ group.groupNumber }}</div>
          </div>

          <template v-if="group.isMember">
            <el-tag type="info" size="mini">已加入</el-tag>
          </template>
          <template v-else>
            <el-button type="success" size="mini" @click="handleJoinGroup(group)">加入</el-button>
          </template>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import groupApi from '@/api/proj_qhy/group'

// 2. --- 添加了 formatTime 函数 (同 forum/index.vue) ---
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
  name: 'Group',
  data() {
    return {
      groupList: [],
      loading: false,
      defaultAvatar: require('@/assets/images/profile.jpg'),
      createDialogVisible: false,
      createForm: {
        groupName: ''
      },
      joinDialogVisible: false,
      searchForm: {
        type: 'number', // 默认按小组号
        query: ''
      },
      searchResults: []
    }
  },
  created() {
    // 应对刷新网页 (F5)
    this.loadGroupList()
  },
  activated() {
    this.loadGroupList()
  },
  methods: {
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
    formatTime,

    loadGroupList() {
      this.loading = true
      groupApi.getGroupList().then(res => {
        this.groupList = res.data.map(g => {
          if (!g.latestMessage) {
            g.latestMessage = { content: '暂无消息', senderNickName: '系统' }
          }
          return g
        })
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    goToChat(groupId) {
      this.$router.push(`/proj_qhy/group/chat/${groupId}`)
    },
    handleCreateGroup() {
      this.createDialogVisible = true
    },
    submitCreateGroup() {
      this.$refs.createForm.validate(valid => {
        if (valid) {
          const data = {
            groupName: this.createForm.groupName,
            memberUserIds: []
          }
          groupApi.createGroup(data).then(() => {
            this.$message.success('创建成功')
            this.createDialogVisible = false
            this.loadGroupList()
          })
        }
      })
    },
    handleShowJoinDialog() {
      this.joinDialogVisible = true
      this.searchResults = []
      this.searchForm.query = ''
    },
    handleSearchGroup() {
      if (!this.searchForm.query) return
      groupApi.searchGroups(this.searchForm).then(res => {
        this.searchResults = res.data
      })
    },
    handleJoinGroup(group) {
      this.$confirm(`确定要加入小组 "${group.groupName}" 吗?`, '提示', {
        type: 'info'
      }).then(() => {
        groupApi.joinGroup(group.groupId).then(() => {
          this.$message.success('加入成功！')
          this.joinDialogVisible = false
          this.loadGroupList() // 刷新主列表
        })
        // --- (修正点) ---
        // 这里的箭头函数是 `() => {}`
      }).catch(() => {});
    }
  }
}
</script>

<style scoped>
.group-list {
  display: flex;
  flex-direction: column;
}
.group-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  background-color: #fff;
}
.group-item:hover {
  background-color: #f9f9f9;
}
.avatar-container {
  position: relative;
  margin-right: 12px;
}
.unread-dot {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 12px;
  height: 18px;
  line-height: 18px;
  transform: translate(50%, -50%);
  z-index: 1;
}
.group-info {
  flex-grow: 1;
  overflow: hidden;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}
.group-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}
.time {
  font-size: 12px;
  color: #999;
}
.latest-message {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.latest-message .sender {
  color: #888;
}
.top-bar {
  padding: 10px 15px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.search-results {
  min-height: 150px;
  max-height: 300px;
  overflow-y: auto;
}
.result-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #f5f5f5;
}
.result-info {
  flex-grow: 1;
  margin-left: 10px;
}
.result-name {
  font-weight: 500;
}
.result-number {
  font-size: 12px;
  color: #888;
}
</style>
