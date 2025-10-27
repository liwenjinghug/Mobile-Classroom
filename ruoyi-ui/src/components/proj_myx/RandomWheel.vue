<template>
  <div class="wheel-container">
    <div class="circle" @click="start">
      <div class="name">{{ displayName }}</div>
    </div>
    <button class="stop-btn" @click="stop" :disabled="!running">停止</button>
  </div>
</template>

<script>
export default {
  name: 'RandomWheel',
  props: {
    students: {
      type: Array,
      default: function() { return [] }
    }
  },
  data() {
    return {
      running: false,
      idx: 0,
      timer: null,
      displayName: '开始'
    }
  },
  methods: {
    start() {
      if (this.running) return
      const list = this.students || []
      if (!list.length) return
      this.running = true
      this.timer = setInterval(() => {
        this.idx = (this.idx + 1) % list.length
        const s = list[this.idx]
        this.displayName = s.studentName || s.name || '匿名'
      }, 80)
    },
    stop() {
      if (!this.running) return
      clearInterval(this.timer)
      this.running = false
      const list = this.students || []
      const chosen = list[this.idx]
      this.displayName = chosen ? (chosen.studentName || chosen.name || '匿名') : '无'
      this.$emit('picked', chosen)
    }
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
  }
}
</script>

<style scoped>
.wheel-container { text-align:center; margin-top:20px; }
.circle {
  width: 220px; height: 220px; border-radius: 50%; background:#409EFF; color:#fff;
  display:flex; align-items:center; justify-content:center; margin:0 auto; cursor:pointer;
  box-shadow: 0 6px 18px rgba(64,158,255,0.35);
}
.name { font-size:20px; font-weight:700 }
.stop-btn { margin-top:12px; padding:6px 12px; border-radius:4px; border:1px solid #dcdfe6; cursor:pointer }
.stop-btn:disabled { opacity:0.5; cursor:not-allowed }
</style>
