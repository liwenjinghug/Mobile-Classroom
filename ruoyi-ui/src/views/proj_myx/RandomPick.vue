<template>
  <div class="random-pick-page">
    <h2 class="title">随机抽人</h2>

    <div class="board">
      <div v-if="!validSessionId" class="session-chooser">
        <p>未检测到有效的课堂 sessionId，请输入课堂 ID（数字）：</p>
        <input v-model="sessionInput" placeholder="例如：1" />
        <button class="btn" @click="applySessionId">加载课堂</button>
        <p style="color:#999;margin-top:8px">提示：请在后台课堂管理或课程安排中查看当前 session 的 numeric id，菜单直接访问时可能未携带该参数。</p>
      </div>

      <div v-else>
        <RandomWheel 
          :students="students" 
          @picked="onPicked" 
          @start-spin="onStartSpin"
          ref="wheel" 
        />

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="!students.length" class="empty">暂无可抽取的学生（请检查签到或是否已全部抽取）</div>
      </div>
    </div>

    <div v-if="result" class="result">
      <h3>被抽中：</h3>
      <p class="picked">{{ result.studentName }} ({{ result.studentNo }})</p>
      <p class="time">抽取时间：{{ result.pickTime ? formatTime(result.pickTime) : '-' }}</p>
    </div>

    <div v-if="history && history.length" class="history-section">
      <div class="section-header">
        <h4>本节课抽人历史 ({{ stats.total }})</h4>
        <div class="tools">
          <el-button size="small" icon="el-icon-download" @click="handleExport">导出</el-button>
          <el-button size="small" icon="el-icon-printer" @click="handlePrint">打印</el-button>
        </div>
      </div>
      
      <div class="history-list">
        <div v-for="item in history" :key="item.rpickId" class="history-item">
          <div class="info">
            <span class="name">{{ item.studentName }}</span>
            <span class="no">({{ item.studentNo }})</span>
            <span class="time">{{ formatTime(item.pickTime) }}</span>
            <span v-if="item.remark" class="remark-tag">{{ item.remark }}</span>
          </div>
          <div class="actions">
            <el-button type="text" size="small" @click="handleEdit(item)">备注</el-button>
            <el-button type="text" size="small" class="delete-btn" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑备注弹窗 -->
    <el-dialog title="编辑备注" :visible.sync="editDialogVisible" width="400px">
      <el-form :model="editForm">
        <el-form-item label="备注内容">
          <el-input v-model="editForm.remark" autocomplete="off" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import RandomWheel from '@/components/proj_myx/RandomWheel.vue'
import { fetchEligible, savePick, fetchHistory, delPick, updatePick, exportPick } from '@/api/proj_myx/randomPick'

export default {
  name: 'RandomPick',
  components: { RandomWheel },
  data() {
    const route = this.$route || {}
    const q = route.query || {}
    const p = route.params || {}
    // 尝试把传入的 sessionId 转为数字（Long），若无法转换则留空并提示用户输入正确的数字 ID
    const raw = q.sessionId || p.sessionId || ''
    const parsed = raw !== '' ? Number(raw) : NaN
    return {
      sessionId: isNaN(parsed) ? null : parsed,
      sessionInput: '',
      students: [],
      result: null,
      history: [],
      loading: false,
      // 编辑备注相关
      editDialogVisible: false,
      editForm: {
        rpickId: null,
        remark: ''
      }
    }
  },
  computed: {
    validSessionId() {
      return this.sessionId !== null && !isNaN(this.sessionId)
    },
    stats() {
      return {
        total: this.history.length,
        lastPick: this.history.length > 0 ? this.history[0].pickTime : null
      }
    }
  },
  methods: {
    unwrap(res) {
      return res && typeof res === 'object' && 'data' in res ? res.data : res
    },
    formatTime(t) {
      if (!t) return ''
      const d = new Date(t)
      return d.toLocaleString()
    },
    applySessionId() {
      const v = Number(this.sessionInput)
      if (!v || isNaN(v) || v <= 0) {
        this.$message && this.$message.warning('请输入合法的数字 sessionId')
        return
      }
      this.sessionId = v
      // 初始化加载
      this.loadEligible()
      this.loadHistory()
    },
    async loadEligible() {
      if (!this.validSessionId) return
      this.loading = true
      try {
        const res = await fetchEligible(this.sessionId)
        this.students = this.unwrap(res) || []
      } catch (e) {
        console.error('加载可抽学生失败', e)
        this.students = []
      } finally {
        this.loading = false
      }
    },
    async loadHistory() {
      if (!this.validSessionId) return
      try {
        const res = await fetchHistory(this.sessionId)
        this.history = this.unwrap(res) || []
      } catch (e) {
        console.error('加载历史失败', e)
        this.history = []
      }
    },
    async onPicked(student) {
      if (!student) return
      const now = new Date().toISOString()
      const picked = Object.assign({}, student, { pickTime: now })
      this.result = picked

      const record = {
        sessionId: this.sessionId,
        teacherId: 0,
        studentId: student.studentId
      }

      try {
        await savePick(record)
        await this.loadHistory()
      } catch (e) {
        console.error('保存抽取记录失败', e)
      }
    },
    async onStartSpin() {
      await this.loadEligible()
      if (this.students.length > 0) {
        this.$refs.wheel.spin()
      }
    },
    // 新增功能方法
    handleDelete(item) {
      this.$confirm('确认删除该条抽取记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await delPick(item.rpickId)
          this.$message.success('删除成功')
          this.loadHistory()
          this.loadEligible() // 删除记录后，该学生可能重新变为可抽取状态（取决于业务逻辑，这里假设需要刷新）
        } catch (e) {
          console.error(e)
        }
      })
    },
    handleEdit(item) {
      this.editForm = {
        rpickId: item.rpickId,
        remark: item.remark || ''
      }
      this.editDialogVisible = true
    },
    async submitEdit() {
      try {
        await updatePick(this.editForm)
        this.$message.success('修改成功')
        this.editDialogVisible = false
        this.loadHistory()
      } catch (e) {
        console.error(e)
      }
    },
    handleExport() {
      this.$confirm('确认导出所有记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        exportPick(this.sessionId).then(response => {
          // 处理文件下载
          const blob = new Blob([response]) // response is blob
          // RuoYi 的 request 拦截器可能会处理 blob，如果返回的是 data
          // 这里假设 request 配置了 responseType: 'blob'
          // 如果 request 拦截器统一处理了，可能需要调整。
          // 通常 RuoYi 的 download 方法封装了这些，但这里我们直接用 exportPick 返回 blob
          // 简单处理：
          const link = document.createElement('a')
          link.href = window.URL.createObjectURL(blob)
          link.download = `随机抽人记录_${new Date().getTime()}.xlsx`
          link.click()
        })
      })
    },
    handlePrint() {
      window.print()
    }
  },
  mounted() {
    if (this.validSessionId) {
      this.loadEligible()
      this.loadHistory()
    }
  }
}
</script>

<style scoped>
.random-pick-page {
  padding: 40px 20px;
  max-width: 800px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
}

.title {
  font-size: 32px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 40px;
  letter-spacing: -0.02em;
}

.board {
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.08);
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: transform 0.3s ease;
}

.session-chooser {
  text-align: center;
  padding: 30px;
  background-color: #f5f5f7;
  border-radius: 16px;
  width: 100%;
  box-sizing: border-box;
}

.session-chooser p {
  margin-bottom: 16px;
  font-size: 16px;
  color: #1d1d1f;
}

.session-chooser input {
  padding: 12px 16px;
  width: 200px;
  margin-right: 12px;
  border: 1px solid #d2d2d7;
  border-radius: 12px;
  font-size: 16px;
  outline: none;
  transition: all 0.2s;
}

.session-chooser input:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

.controls {
  margin-top: 32px;
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn {
  padding: 12px 24px;
  border-radius: 980px;
  border: none;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.2s cubic-bezier(0.25, 0.1, 0.25, 1);
  background-color: #f5f5f7;
  color: #1d1d1f;
}

.btn:hover {
  background-color: #e5e5ea;
  transform: translateY(-1px);
}

.btn:active {
  transform: scale(0.96);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn.primary {
  background-color: #0071e3;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.3);
}

.btn.primary:hover {
  background-color: #0077ed;
  box-shadow: 0 6px 16px rgba(0, 113, 227, 0.4);
}

.loading {
  margin-top: 20px;
  color: #86868b;
  font-weight: 500;
}

.empty {
  margin-top: 20px;
  color: #86868b;
  background: #f5f5f7;
  padding: 12px 20px;
  border-radius: 12px;
}

.result {
  margin-top: 40px;
  text-align: center;
  background: #ffffff;
  padding: 30px;
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.06);
  animation: popIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes popIn {
  from { opacity: 0; transform: scale(0.9) translateY(20px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.result h3 {
  margin: 0 0 16px 0;
  color: #86868b;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.picked {
  font-size: 36px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #0071e3, #00c7be);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.time {
  color: #86868b;
  font-size: 14px;
}

.history-section {
  margin-top: 40px;
  background: #ffffff;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h4 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.history-list {
  display: flex;
  flex-direction: column;
}

.history-item {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
  color: #1d1d1f;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
}

.history-item:last-child {
  border-bottom: none;
}

.info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-weight: 500;
}

.no {
  color: #86868b;
}

.remark-tag {
  background: #f5f5f7;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #0071e3;
}

.actions {
  display: flex;
  gap: 8px;
}

.delete-btn {
  color: #ff3b30;
}

.delete-btn:hover {
  color: #d70015;
}

@media print {
  .random-pick-page {
    padding: 0;
    max-width: none;
  }
  .board, .controls, .tools, .actions, .session-chooser, .title {
    display: none !important;
  }
  .history-section {
    box-shadow: none;
    padding: 0;
  }
  .history-item {
    border-bottom: 1px solid #000;
  }
}
</style>
