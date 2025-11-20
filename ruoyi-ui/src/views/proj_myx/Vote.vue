<template>
  <div class="vote-page">
    <h2>课堂投票</h2>

    <div class="actions-bar">
      <el-button type="primary" @click="loadPolls">加载投票列表</el-button>
      <el-button v-if="isAdmin" type="success" @click="openCreate">创建投票</el-button>
    </div>

    <el-table :data="polls" style="width:100%">
      <el-table-column prop="pollId" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="type" label="类型" width="120">
        <template slot-scope="{ row }">
          <span>{{ row.type === 'single' ? '单选' : '多选' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : (row.status===2 ? 'info' : 'warning')">
            {{ row.status === 1 ? '进行中' : (row.status===2 ? '已结束' : '未开始') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template slot-scope="{ row }">
          <el-button size="mini" type="primary" @click="openVote(row)">投票</el-button>
          <el-button size="mini" @click="viewResults(row)" style="margin-left:6px">查看结果</el-button>
          <el-button size="mini" v-if="isAdmin" type="danger" @click="endPoll(row)" style="margin-left:6px">结束投票</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建投票弹窗（本地前端模拟） -->
    <el-dialog title="创建投票（仅前端模拟）" :visible.sync="showCreateDialog" width="600px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="createForm.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="createForm.type">
            <el-radio label="single">单选</el-radio>
            <el-radio label="multi">多选</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选项">
          <div v-for="(opt, idx) in createForm.options" :key="idx" style="display:flex;align-items:center;margin-bottom:6px">
            <el-input v-model="opt.text" placeholder="选项文本" />
            <el-button type="text" @click="removeOption(idx)">删除</el-button>
          </div>
          <el-button size="mini" @click="addOption">添加选项</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showCreateDialog = false">取 消</el-button>
        <el-button type="primary" @click="createPoll">创 建</el-button>
      </div>
    </el-dialog>

    <!-- 投票弹窗 -->
    <el-dialog :title="currentPoll ? currentPoll.title : '投票'" :visible.sync="showVoteDialog" width="520px">
      <div v-if="currentPoll">
        <div style="margin-bottom:8px">请选择你的选项：</div>
        <div v-if="currentPoll.type === 'single'">
          <el-radio-group v-model="selectedOption">
            <div v-for="opt in currentPoll.options" :key="opt.id" style="margin-bottom:6px">
              <el-radio :label="opt.id">{{ opt.text }}</el-radio>
            </div>
          </el-radio-group>
        </div>
        <div v-else>
          <el-checkbox-group v-model="selectedOptions">
            <div v-for="opt in currentPoll.options" :key="opt.id" style="margin-bottom:6px">
              <el-checkbox :label="opt.id">{{ opt.text }}</el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showVoteDialog = false">取 消</el-button>
        <el-button type="primary" @click="submitVote">提 交</el-button>
      </div>
    </el-dialog>

    <!-- 结果弹窗 -->
    <el-dialog title="投票结果（前端模拟）" :visible.sync="showResultsDialog" width="520px">
      <div v-if="currentPoll">
        <div v-for="opt in currentPoll.options" :key="opt.id" class="vote-result-item">
          <div class="vote-option-text">{{ opt.text }}</div>
          <div class="vote-count">{{ opt.votes }} 票</div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showResultsDialog = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
export default {
  name: 'Vote',
  data() {
    return {
      polls: [],
      showCreateDialog: false,
      createForm: {
        title: '',
        type: 'single',
        options: [ { text: '选项 A' }, { text: '选项 B' } ]
      },
      showVoteDialog: false,
      currentPoll: null,
      selectedOption: null,
      selectedOptions: [],
      showResultsDialog: false
    }
  },
  computed: {
    isAdmin() {
      const roles = this.$store && this.$store.getters && this.$store.getters.roles
      return Array.isArray(roles) && roles.includes('admin')
    }
  },
  methods: {
    // 加载示例投票（本地模拟）
    loadPolls() {
      // 示例数据：前端模拟，实际应从后端加载
      this.polls = [
        {
          pollId: 1,
          title: '本周课程满意度调查',
          type: 'single',
          status: 1,
          options: [ { id: 11, text: '非常满意', votes: 5 }, { id: 12, text: '一般', votes: 2 }, { id: 13, text: '不满意', votes: 0 } ]
        },
        {
          pollId: 2,
          title: '下次想学的主题（可多选）',
          type: 'multi',
          status: 1,
          options: [ { id: 21, text: '算法', votes: 3 }, { id: 22, text: '前端', votes: 4 }, { id: 23, text: '数据库', votes: 1 } ]
        }
      ]
    },
    openCreate() {
      this.showCreateDialog = true
      // reset form
      this.createForm = { title: '', type: 'single', options: [ { text: '' }, { text: '' } ] }
    },
    addOption() {
      this.createForm.options.push({ text: '' })
    },
    removeOption(idx) {
      this.createForm.options.splice(idx, 1)
    },
    createPoll() {
      // 前端模拟：将表单内容加入 polls（生成简单 id）
      const nextId = this.polls.length ? Math.max(...this.polls.map(p => p.pollId)) + 1 : 1
      const baseOptId = nextId * 10
      const poll = {
        pollId: nextId,
        title: this.createForm.title || '未命名投票',
        type: this.createForm.type,
        status: 1,
        options: this.createForm.options.map((o, i) => ({ id: baseOptId + i + 1, text: o.text || ('选项 ' + (i+1)), votes: 0 }))
      }
      this.polls.unshift(poll)
      this.showCreateDialog = false
      this.$message.success('投票已在前端创建（模拟）')
    },
    openVote(poll) {
      if (!poll || poll.status !== 1) {
        this.$message.warning('该投票不可投票（未开始或已结束）')
        return
      }
      this.currentPoll = poll
      // 重置选择
      this.selectedOption = null
      this.selectedOptions = []
      this.showVoteDialog = true
    },
    submitVote() {
      if (!this.currentPoll) return
      if (this.currentPoll.type === 'single') {
        if (this.selectedOption == null) return this.$message.warning('请选择一个选项')
        const opt = this.currentPoll.options.find(o => o.id === this.selectedOption)
        if (opt) opt.votes += 1
      } else {
        if (!this.selectedOptions || !this.selectedOptions.length) return this.$message.warning('请选择至少一个选项')
        this.selectedOptions.forEach(id => {
          const opt = this.currentPoll.options.find(o => o.id === id)
          if (opt) opt.votes += 1
        })
      }
      this.showVoteDialog = false
      this.$message.success('投票成功（仅前端模拟）')
      this.showResults(this.currentPoll)
    },
    viewResults(poll) {
      this.currentPoll = poll
      this.showResultsDialog = true
    },
    showResults(poll) {
      this.currentPoll = poll
      this.showResultsDialog = true
    },
    endPoll(poll) {
      if (!poll) return
      poll.status = 2
      this.$message.info('投票已结束（仅前端模拟）')
    }
  }
}
</script>

<style scoped>
.vote-page {
  padding: 40px 20px;
  max-width: 1000px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
  background-color: #f5f5f7;
  min-height: 100vh;
}

h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 24px;
  text-align: center;
}

.actions-bar {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.actions-bar >>> .el-button {
  border-radius: 980px;
  padding: 10px 24px;
  font-weight: 500;
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.actions-bar >>> .el-button--primary {
  background-color: #0071e3;
}

.actions-bar >>> .el-button--success {
  background-color: #34c759;
}

/* Table Styling */
.vote-page >>> .el-table {
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.vote-page >>> .el-table th {
  background-color: #ffffff;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #f5f5f7;
  padding: 16px 0;
}

.vote-page >>> .el-table td {
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f7;
}

.vote-page >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

.vote-page >>> .el-tag--success {
  background-color: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.vote-page >>> .el-tag--warning {
  background-color: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.vote-page >>> .el-tag--info {
  background-color: rgba(142, 142, 147, 0.1);
  color: #8e8e93;
}

/* Dialog Styling */
.vote-page >>> .el-dialog {
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}

.vote-page >>> .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
}

.vote-page >>> .el-dialog__title {
  font-weight: 600;
  font-size: 18px;
}

.vote-page >>> .el-dialog__body {
  padding: 24px;
}

.vote-page >>> .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
}

/* Form Items */
.vote-page >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  transition: all 0.2s;
}

.vote-page >>> .el-input__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

/* Option List in Dialog */
.vote-option-item {
  background: #fbfbfd;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  transition: background 0.2s;
}

.vote-option-item:hover {
  background: #f5f5f7;
}

.vote-result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
}

.vote-result-item:last-child {
  border-bottom: none;
}

.vote-count {
  font-weight: 600;
  color: #0071e3;
  background: rgba(0, 113, 227, 0.1);
  padding: 4px 10px;
  border-radius: 980px;
  font-size: 13px;
}
</style>

