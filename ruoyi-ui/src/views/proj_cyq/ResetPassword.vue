<template>
  <div class="reset-container">
    <el-form ref="form" :model="form" :rules="rules" class="reset-form">
      <h3 class="title">{{ title }}</h3>

      <div v-if="step === 1">
        <p class="tips">请输入您要找回密码的账号：</p>
        <el-form-item prop="userName">
          <el-input
            v-model="form.userName"
            type="text"
            auto-complete="off"
            placeholder="账号"
          >
            <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
          </el-input>
        </el-form-item>
        <el-form-item style="width:100%;">
          <el-button
            :loading="loading"
            size="medium"
            type="primary"
            style="width:100%;"
            @click.native.prevent="handleRequestReset"
          >
            <span v-if="!loading">发送重置邮件</span>
            <span v-else>发送中...</span>
          </el-button>
        </el-form-item>
      </div>

      <div v-if="step === 2">
        <p class="tips">请输入您的新密码：</p>
        <el-form-item prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            auto-complete="off"
            placeholder="新密码"
          >
            <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            auto-complete="off"
            placeholder="确认新密码"
          >
            <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
          </el-input>
        </el-form-item>
        <el-form-item style="width:100%;">
          <el-button
            :loading="loading"
            size="medium"
            type="primary"
            style="width:100%;"
            @click.native.prevent="handleSubmitReset"
          >
            <span v-if="!loading">确认重置密码</span>
            <span v-else>重置中...</span>
          </el-button>
        </el-form-item>
      </div>

      <div v-if="step === 3">
        <p class="tips" v-html="message"></p>
      </div>

      <div style="text-align: center; margin-top: 20px;">
        <router-link class="link-type" :to="'/login'">返回登录</router-link>
      </div>

    </el-form>

    <div class="el-login-footer"> <span>Copyright © 2018-2025 ruoyi.vip All Rights Reserved.</span> </div>
  </div>
</template>

<script>
// 引入若依的 request 工具
import request from '@/utils/request'

// 后端API路径 (与 Controller 对应)
const api = {
  request: '/proj_cyq/password/request-reset',
  verify: '/proj_cyq/password/verify-token',
  reset: '/proj_cyq/password/reset-password'
}

export default {
  name: "ResetPassword",
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (value !== this.form.newPassword) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };
    return {
      title: "找回密码",
      step: 1, // 1:请求, 2:重置, 3:提示
      loading: false,
      message: "", // 用于步骤3的提示信息
      form: {
        userName: "",
        newPassword: "",
        confirmPassword: "",
        token: ""
      },
      rules: {
        userName: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        newPassword: [
          { required: true, trigger: "blur", message: "请输入新密码" },
          { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, trigger: "blur", message: "请再次输入新密码" },
          { required: true, validator: equalToPassword, trigger: "blur" }
        ]
      }
    };
  },
  created() {
    // 检查 URL 中是否带有 token
    const token = this.$route.query.token;
    if (token) {
      this.verifyToken(token);
    } else {
      this.step = 1;
    }
  },
  methods: {
    // 验证Token有效性
    verifyToken(token) {
      this.loading = true;
      this.title = "验证链接...";
      request({
        url: api.verify,
        method: 'get',
        params: { token: token }
      }).then(response => {
        this.form.token = token;
        this.step = 2; // 切换到重置密码步骤
        this.title = "设置新密码";
        this.loading = false;
      }).catch(err => {
        this.step = 3;
        this.message = "链接无效或已过期，请重新发起请求。";
        this.title = "验证失败";
        this.loading = false;
      });
    },

    // 步骤1: 提交“请求重置” (已修正)
    handleRequestReset() {
      // 使用 .validate() 替换 .validateField()
      this.$refs.form.validate((valid) => {
        // 检查 (valid === true)
        if (valid) {
          console.log("验证通过，正在发送 'request-reset'..."); // 供F12调试
          this.loading = true;
          request({
            url: api.request,
            method: 'post',
            data: { userName: this.form.userName }
          }).then(response => {
            console.log("请求成功:", response);
            this.step = 3;
            this.title = "邮件已发送";
            this.message = response.msg || "如果账号存在且绑定了邮箱，重置邮件将很快发送，请注意查收。";
            this.loading = false;
          }).catch(err => {
            console.error("请求失败:", err);
            this.step = 3;
            this.title = "发送失败";
            this.message = err.message || "请求失败，请稍后再试";
            this.loading = false;
          });
        }
        // (如果 valid === false, Element UI 会自动显示表单错误, F12会打印 "验证失败")
        else {
          console.log("验证失败，请求未发送。");
          return false;
        }
      });
    },

    // 步骤2: 提交“新密码” (此方法原本就是正确的)
    handleSubmitReset() {
      this.$refs.form.validate(valid => {
        if (valid) {
          console.log("验证通过，正在发送 'reset-password'..."); // 供F12调试
          this.loading = true;
          request({
            url: api.reset,
            method: 'post',
            data: {
              token: this.form.token,
              newPassword: this.form.newPassword
            }
          }).then(response => {
            console.log("重置成功:", response);
            this.step = 3;
            this.title = "操作成功";
            this.message = "密码重置成功！<br>请返回登录页使用新密码登录。";
            this.loading = false;
          }).catch(err => {
            console.error("重置失败:", err);
            this.step = 3;
            this.title = "操作失败";
            this.message = err.message || "密码重置失败，链接可能已失效，请重试。";
            this.loading = false;
          });
        } else {
          console.log("验证失败，请求未发送。");
          return false;
        }
      });
    }
  },

    // 步骤2: 提交“新密码”
    handleSubmitReset() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.loading = true;
          request({
            url: api.reset,
            method: 'post',
            data: {
              token: this.form.token,
              newPassword: this.form.newPassword
            }
          }).then(response => {
            this.step = 3;
            this.title = "操作成功";
            this.message = "密码重置成功！<br>请返回登录页使用新密码登录。";
            this.loading = false;
          }).catch(err => {
            this.step = 3;
            this.title = "操作失败";
            this.message = err.message || "密码重置失败，链接可能已失效，请重试。";
            this.loading = false;
          });
        }
      });
    }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
/* 复用 login.vue 的样式 */
.reset-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("~@/assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}
.tips {
  font-size: 14px;
  color: #606266;
  margin-bottom: 20px;
  text-align: center;
  line-height: 1.5;
}
.reset-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 25px 25px;
  .el-input {
    height: 38px;
    input {
      height: 38px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}
.link-type {
  color: #409eff;
  text-decoration: none;
  &:hover {
    text-decoration: underline;
  }
}
.el-login-footer { /* */
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
</style>
