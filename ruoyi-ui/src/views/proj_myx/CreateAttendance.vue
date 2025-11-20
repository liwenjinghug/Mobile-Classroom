<template>
  <div class="create-form-container">
    <el-form :model="form" label-position="top" class="apple-form">
      <el-form-item label="签到方式">
        <el-radio-group v-model="form.type" class="apple-radio-group">
          <el-radio label="location" border>位置签到</el-radio>
          <el-radio label="qr" border>二维码签到</el-radio>
        </el-radio-group>
      </el-form-item>

      <div v-if="form.type === 'location'" class="form-section">
        <div class="form-row">
          <el-form-item label="中心纬度" class="half-width">
            <el-input v-model.number="form.centerLat" placeholder="例如：39.9042" />
          </el-form-item>
          <el-form-item label="中心经度" class="half-width">
            <el-input v-model.number="form.centerLng" placeholder="例如：116.4074" />
          </el-form-item>
        </div>
        <el-form-item label="半径（米）">
          <el-input v-model.number="form.radius" placeholder="例如：500" />
        </el-form-item>
      </div>

      <div v-else class="form-section">
        <el-form-item label="二维码有效时长(分钟)">
          <el-input v-model.number="form.qrTtl" placeholder="例如：10" />
        </el-form-item>
      </div>

      <div class="form-row">
        <el-form-item label="开始时间" class="half-width">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" class="half-width">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
      </div>

      <el-form-item class="form-actions">
        <el-button type="primary" @click="create" class="submit-btn">创建签到</el-button>
      </el-form-item>
    </el-form>

    <el-dialog title="二维码" :visible.sync="showQr" width="360px" @close="onQrClose" custom-class="qr-dialog" append-to-body>
      <div v-if="qrData" class="qr-content">
        <img :src="qrData.qrUrl" alt="qr" class="qr-image" />
        <div class="qr-info">
          <p class="qr-token">Token: {{ qrData.token }}</p>
          <p class="qr-expire">过期时间: {{ qrData.expireTime }}</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" round @click="showQr = false">查看签到详情</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { createTask, generateQr } from '@/api/proj_myx/attendance'

export default {
  name: 'CreateAttendance',
  props: { sessionId: { type: Number, required: true } },
  data() {
    return {
      form: { type: 'location', centerLat: null, centerLng: null, radius: 500, qrTtl: 10, startTime: null, endTime: null },
      showQr: false,
      qrData: null,
      pendingCreated: null // store created payload until QR dialog is closed
    }
  },
  methods: {
    async create() {
      const payload = Object.assign({}, this.form, { sessionId: this.sessionId })
      try {
        const res = await createTask(payload)
        // adapt to response wrapper or direct data
        const wrapper = (res && res.code !== undefined) ? res : (res && res.data !== undefined ? res : res)
        if (res && res.code === 200) {
          const created = res.data
          this.$message.success('签到创建成功!')
          // if qr type, request backend to generate token and show QR
          if (this.form.type === 'qr' && created && created.taskId) {
            try {
              const gen = await generateQr(created.taskId, this.form.qrTtl)
              if (gen && gen.code === 200) {
                this.qrData = gen.data
                this.showQr = true
                // do NOT emit 'created' yet — wait until user closes the QR dialog
                this.pendingCreated = created
              } else {
                this.$message.error('生成二维码失败: ' + (gen && gen.msg))
                // emit so parent can open details even if QR generation failed
                this.$emit('created', created)
              }
            } catch (e) {
              console.error('生成二维码出错', e)
              const serverMsg = e && e.response && e.response.data && (e.response.data.msg || e.response.data.message)
              this.$message.error('生成二维码出错: ' + (serverMsg || (e && e.message ? e.message : '请检查后端')))
              // emit so parent can open details even if QR generation failed
              this.$emit('created', created)
            }
          } else {
            this.$emit('created', created)
          }
        } else {
          this.$message.error('签到创建失败! ' + (res && res.msg ? res.msg : ''))
        }
      } catch (err) {
        console.error('创建签到失败', err)
        this.$message.error('创建签到失败: ' + (err && err.message ? err.message : '请检查后端或权限'))
      }
    },

    // called when user closes the QR dialog
    onQrClose() {
      try {
        if (this.pendingCreated) {
          this.$emit('created', this.pendingCreated)
          this.pendingCreated = null
        }
      } catch (e) {
        console.error('onQrClose error', e)
      }
    }
  }
}
</script>

<style scoped>
.create-form-container {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  padding: 10px;
}

.apple-form >>> .el-form-item__label {
  font-weight: 500;
  color: #1d1d1f;
  padding-bottom: 8px;
}

.apple-form >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  height: 40px;
  transition: all 0.2s ease;
}

.apple-form >>> .el-input__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

.apple-radio-group {
  display: flex;
  gap: 12px;
}

.apple-radio-group >>> .el-radio {
  margin-right: 0;
}

.apple-radio-group >>> .el-radio.is-bordered {
  border-radius: 10px;
  border-color: #d2d2d7;
  padding: 10px 20px;
  height: auto;
}

.apple-radio-group >>> .el-radio.is-bordered.is-checked {
  border-color: #0071e3;
  background-color: rgba(0, 113, 227, 0.05);
}

.apple-radio-group >>> .el-radio__label {
  font-weight: 500;
}

.form-section {
  background-color: #fbfbfd;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.half-width {
  flex: 1;
}

.form-actions {
  margin-top: 32px;
  text-align: center;
}

.submit-btn {
  width: 100%;
  max-width: 200px;
  height: 44px;
  border-radius: 22px;
  font-size: 16px;
  font-weight: 600;
  background-color: #0071e3;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.3);
  transition: all 0.2s ease;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 113, 227, 0.4);
}

.submit-btn:active {
  transform: scale(0.98);
}

/* QR Dialog Styles */
.qr-content {
  text-align: center;
  padding: 10px;
}

.qr-image {
  width: 260px;
  height: 260px;
  display: block;
  margin: 0 auto 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.qr-info {
  background-color: #f5f5f7;
  padding: 16px;
  border-radius: 12px;
}

.qr-token {
  font-family: monospace;
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
  word-break: break-all;
}

.qr-expire {
  font-size: 13px;
  color: #86868b;
}
</style>
