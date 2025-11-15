<template>
  <div>
    <el-form :model="form" label-width="120px">
      <el-form-item label="签到方式">
        <el-radio-group v-model="form.type">
          <el-radio label="location">位置签到</el-radio>
          <el-radio label="qr">二维码签到</el-radio>
        </el-radio-group>
      </el-form-item>

      <div v-if="form.type === 'location'">
        <el-form-item label="中心纬度">
          <el-input v-model.number="form.centerLat" />
        </el-form-item>
        <el-form-item label="中心经度">
          <el-input v-model.number="form.centerLng" />
        </el-form-item>
        <el-form-item label="半径（米）">
          <el-input v-model.number="form.radius" />
        </el-form-item>
      </div>

      <div v-else>
        <el-form-item label="二维码有效时长(分钟)">
          <el-input v-model.number="form.qrTtl" />
        </el-form-item>
      </div>

      <el-form-item label="开始时间">
        <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="create">创建</el-button>
      </el-form-item>
    </el-form>

    <el-dialog title="二维码" :visible.sync="showQr" width="360px" @close="onQrClose">
      <div v-if="qrData">
        <img :src="qrData.qrUrl" alt="qr" style="width:300px;height:300px;display:block;margin:0 auto" />
        <p style="word-break:break-all;">Token: {{ qrData.token }}</p>
        <p>过期时间: {{ qrData.expireTime }}</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="onQrClose">查看签到详情</el-button>
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
