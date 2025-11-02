<template>
  <div class="vote-page">
    <h2>课堂投票</h2>

    <div style="margin-bottom:12px">
      <el-button type="primary" @click="loadPolls">加载投票列表</el-button>
      <el-button v-if="isAdmin" type="success" style="margin-left:8px" @click="openCreate">创建投票</el-button>
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
        <div v-for="opt in currentPoll.options" :key="opt.id" style="display:flex;align-items:center;justify-content:space-between;padding:6px 0;border-bottom:1px solid #eee">
          <div>{{ opt.text }}</div>
          <div style="min-width:120px;text-align:right">{{ opt.votes }} 票</div>
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
.vote-page { padding: 16px }
</style>

