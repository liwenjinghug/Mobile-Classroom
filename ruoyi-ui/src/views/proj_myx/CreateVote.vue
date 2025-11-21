<template>
  <div class="create-vote-container">
    <el-form :model="form" ref="form" label-position="top" class="apple-form">
      <el-form-item label="投票标题/问题" prop="title" :rules="[{required:true, message:'请输入标题', trigger:'blur'}]">
        <el-input v-model="form.title" placeholder="请输入问题内容" />
      </el-form-item>

      <div class="form-row">
        <el-form-item label="类型" class="half-width">
          <el-radio-group v-model="form.type">
            <el-radio label="1">单选</el-radio>
            <el-radio label="2">多选</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="匿名投票" class="half-width">
          <el-switch v-model="form.isAnonymous" active-value="1" inactive-value="0" />
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

      <div class="options-section">
        <div class="section-title">选项设置</div>
        <div v-for="(opt, index) in form.options" :key="index" class="option-row">
          <div class="option-label">{{ getOptionLabel(index) }}</div>
          <el-input v-model="opt.content" placeholder="选项内容" class="option-input" />
          <el-button type="text" icon="el-icon-delete" class="delete-btn" @click="removeOption(index)" v-if="form.options.length > 2"></el-button>
        </div>
        <el-button type="text" icon="el-icon-plus" @click="addOption">添加选项</el-button>
      </div>

      <el-form-item class="form-actions">
        <el-button type="primary" @click="submit" class="submit-btn">创建投票</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { createVote } from '@/api/proj_myx/vote'

export default {
  name: 'CreateVote',
  props: { sessionId: { type: Number, required: true } },
  data() {
    return {
      form: {
        title: '',
        type: '1',
        isAnonymous: '0',
        startTime: null,
        endTime: null,
        options: [
          { content: '' },
          { content: '' }
        ]
      }
    }
  },
  methods: {
    getOptionLabel(index) {
      return String.fromCharCode(65 + index); // A, B, C...
    },
    addOption() {
      this.form.options.push({ content: '' });
    },
    removeOption(index) {
      this.form.options.splice(index, 1);
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          // Validate options
          if (this.form.options.some(o => !o.content.trim())) {
            return this.$message.warning('请填写所有选项内容');
          }
          
          const payload = {
            ...this.form,
            sessionId: this.sessionId,
            options: this.form.options.map((o, i) => ({
              label: this.getOptionLabel(i),
              content: o.content
            }))
          };
          
          try {
            const res = await createVote(payload);
            if (res.code === 200) {
              this.$message.success('创建成功');
              this.$emit('created');
            } else {
              this.$message.error(res.msg);
            }
          } catch (e) {
            this.$message.error('创建失败');
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.create-vote-container {
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

.form-row {
  display: flex;
  gap: 20px;
}

.half-width {
  flex: 1;
}

.options-section {
  background: #f5f5f7;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 16px;
  font-size: 14px;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.option-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.option-label {
  width: 36px;
  height: 36px;
  line-height: 36px;
  text-align: center;
  font-weight: 600;
  color: #1d1d1f;
  background: #ffffff;
  border-radius: 50%;
  margin-right: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.option-input {
  flex: 1;
}

.delete-btn {
  margin-left: 12px;
  color: #86868b;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.2s;
}

.delete-btn:hover {
  color: #ff3b30;
  background: rgba(255, 59, 48, 0.1);
}

.form-actions {
  text-align: center;
  margin-top: 32px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  background-color: #0071e3;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.3);
  transition: all 0.2s;
}

.submit-btn:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 113, 227, 0.35);
}
</style>
