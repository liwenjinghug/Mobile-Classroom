<template>
  <div class="wheel-container">
    <div class="wheel-wrapper">
      <div 
        class="wheel" 
        ref="wheel"
        :style="wheelStyle"
      >
        <div 
          v-for="(item, index) in students" 
          :key="item.studentId || index"
          class="label-container"
          :style="getLabelStyle(index)"
        >
          <span class="label-text">{{ item.studentName }}</span>
        </div>
      </div>
      <div class="pointer"></div>
    </div>
    
    <div class="controls">
      <button 
        class="spin-btn" 
        @click="handleSpinClick" 
        :disabled="isSpinning || students.length === 0"
      >
        {{ isSpinning ? '抽取中...' : '开始抽取' }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RandomWheel',
  props: {
    students: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      rotation: 0,
      isSpinning: false,
      duration: 4, // seconds
      colors: ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEEAD', '#D4A5A5', '#9B59B6', '#3498DB']
    }
  },
  computed: {
    sectorAngle() {
      return 360 / Math.max(this.students.length, 1)
    },
    wheelStyle() {
      // Create conic-gradient for sectors
      const len = this.students.length
      if (len === 0) return {}
      
      let gradient = 'conic-gradient('
      const angle = 360 / len
      
      for (let i = 0; i < len; i++) {
        const color = this.colors[i % this.colors.length]
        const start = i * angle
        const end = (i + 1) * angle
        gradient += `${color} ${start}deg ${end}deg`
        if (i < len - 1) gradient += ', '
      }
      gradient += ')'
      
      return {
        background: gradient,
        transform: `rotate(${this.rotation}deg)`,
        transition: this.isSpinning ? `transform ${this.duration}s cubic-bezier(0.25, 0.1, 0.25, 1)` : 'none'
      }
    }
  },
  methods: {
    getLabelStyle(index) {
      const angle = 360 / this.students.length
      // Position the label in the middle of the sector
      const rotate = index * angle + angle / 2
      
      return {
        transform: `rotate(${rotate}deg) translateY(-240px)`, // Move text out from center
      }
    },
    
    handleSpinClick() {
      this.$emit('start-spin')
    },

    spin() {
      if (this.isSpinning || this.students.length === 0) return
      
      this.isSpinning = true
      
      // Calculate a random landing spot
      const minSpins = 5
      const extraSpins = Math.floor(Math.random() * 5)
      const randomDegree = Math.floor(Math.random() * 360)
      
      const totalDegrees = (minSpins + extraSpins) * 360 + randomDegree
      
      this.rotation += totalDegrees
      
      setTimeout(() => {
        this.isSpinning = false
        this.announceWinner()
      }, this.duration * 1000)
    },
    
    announceWinner() {
      const len = this.students.length
      if (len === 0) return

      const actualRotation = this.rotation % 360
      const angle = 360 / len
      const target = (360 - (actualRotation % 360)) % 360
      const index = Math.floor(target / angle)
      
      const winnerIndex = Math.min(Math.max(index, 0), len - 1)
      const winner = this.students[winnerIndex]
      
      if (winner) {
        this.$emit('picked', winner)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.wheel-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.wheel-wrapper {
  position: relative;
  width: 600px;
  height: 600px;
  margin-bottom: 30px;
}

.wheel {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: relative;
  overflow: hidden;
  border: 8px solid #fff;
  box-shadow: 0 0 20px rgba(0,0,0,0.1);
}

.label-container {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
}

.label-text {
  display: block;
  width: 160px;
  margin-left: -80px;
  text-align: center;
  transform: rotate(-90deg); 
  color: #fff;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pointer {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 15px solid transparent;
  border-right: 15px solid transparent;
  border-top: 30px solid #ff4757;
  z-index: 10;
  filter: drop-shadow(0 2px 2px rgba(0,0,0,0.2));
}

.spin-btn {
  padding: 12px 40px;
  font-size: 18px;
  background: linear-gradient(135deg, #0071e3, #00c7be);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  font-weight: 600;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,113,227,0.3);
  }
  
  &:active:not(:disabled) {
    transform: translateY(0);
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    background: #ccc;
  }
}
</style>
