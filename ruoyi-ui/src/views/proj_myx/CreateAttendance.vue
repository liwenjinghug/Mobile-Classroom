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

    <el-dialog title="二维码" :visible.sync="showQr" width="360px">
      <div v-if="qrData">
        <img :src="qrData.qrUrl" alt="qr" style="width:300px;height:300px;display:block;margin:0 auto" />
        <p style="word-break:break-all;">Token: {{ qrData.token }}</p>
        <p>过期时间: {{ qrData.expireTime }}</p>
      </div>
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
      qrData: null
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
          // emit created object to parent so it can open details (contains taskId)
          // if qr type, request backend to generate token and show QR
          if (this.form.type === 'qr' && created && created.taskId) {
            try {
              const gen = await generateQr(created.taskId, this.form.qrTtl)
              if (gen && gen.code === 200) {
                this.qrData = gen.data
                this.showQr = true
                this.$emit('created', created)
              } else {
                this.$message.error('生成二维码失败: ' + (gen && gen.msg))
                this.$emit('created', created)
              }
            } catch (e) {
              console.error('生成二维码出错', e)
              this.$message.error('生成二维码出错: ' + (e && e.message ? e.message : '请检查后端'))
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
        // request 拦截器会在响应非200时 reject 错误，Message 已在 request 拦截里处理，但这里也显示更明确信息
        this.$message.error('创建签到失败: ' + (err && err.message ? err.message : '请检查后端或权限'))
      }
    }
  }
}
</script>
