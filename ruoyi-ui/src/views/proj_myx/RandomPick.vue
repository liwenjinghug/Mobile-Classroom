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
        <RandomWheel :students="students" @picked="onPicked" ref="wheel" />

        <div class="controls">
          <button class="btn" @click="loadEligible" :disabled="loading">刷新名单</button>
          <button class="btn primary" @click="serverPick" :disabled="loading || !students.length">服务器随机抽一位</button>
        </div>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="!students.length" class="empty">暂无可抽取的学生（请检查签到或是否已全部抽取）</div>
      </div>
    </div>

    <div v-if="result" class="result">
      <h3>被抽中：</h3>
      <p class="picked">{{ result.studentName }} ({{ result.studentNo }})</p>
      <p class="time">抽取时间：{{ result.pickTime ? formatTime(result.pickTime) : '-' }}</p>
    </div>

    <div v-if="history && history.length" class="history">
      <h4>本节课抽人历史</h4>
      <ul>
        <li v-for="item in history" :key="item.rpickId">{{ item.studentName }} ({{ item.studentNo }}) - {{ formatTime(item.pickTime) }}</li>
      </ul>
    </div>
  </div>
</template>

<script>
import RandomWheel from '@/components/proj_myx/RandomWheel.vue'
import { fetchEligible, savePick, serverRandomPick, fetchHistory } from '@/api/proj_myx/randomPick'

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
      loading: false
    }
  },
  computed: {
    validSessionId() {
      return this.sessionId !== null && !isNaN(this.sessionId)
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
        await this.loadEligible()
        await this.loadHistory()
      } catch (e) {
        console.error('保存抽取记录失败', e)
      }
    },
    async serverPick() {
      if (this.loading || !this.validSessionId) return
      this.loading = true
      try {
        const res = await serverRandomPick(this.sessionId, 0)
        const picked = this.unwrap(res)
        if (picked) {
          this.result = picked
          await this.loadEligible()
          await this.loadHistory()
        }
      } catch (e) {
        console.error('服务器抽人失败', e)
      } finally {
        this.loading = false
      }
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
.random-pick-page { padding: 16px; max-width: 900px; margin: 0 auto; }
.title { font-size: 20px; margin-bottom: 12px }
.board { display:flex; flex-direction:column; align-items:center; }
.controls { margin-top: 12px; display:flex; gap:8px }
.btn { padding:8px 12px; border-radius:6px; border:1px solid #dcdfe6; cursor:pointer; background:#fff }
.btn.primary { background:#409EFF; color:#fff; border-color:#409EFF }
.loading { margin-top:12px; color:#999 }
.empty { margin-top:12px; color:#999 }
.result { margin-top:20px; text-align:center }
.picked { font-size:18px; font-weight:700 }
.time { color:#666 }
.history { margin-top:20px }
.session-chooser { text-align:center; padding: 12px; border: 1px dashed #e6e6e6; border-radius:6px; margin-bottom:12px }
.session-chooser input { padding:6px 8px; width:120px; margin-right:8px }
</style>
