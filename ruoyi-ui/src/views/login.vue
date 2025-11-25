<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{title}}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          auto-complete="off"
          placeholder="账号"
        >
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
<!--      开始修改-->
      <div class="login-options">
        <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>

        <router-link v-if="register" class="link-type" :to="'/register'">立即注册</router-link>

        <router-link class="link-type" :to="'/reset-password'">忘记密码？</router-link>
      </div>
<!--修改结束-->
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
<!--        <div style="float: right;" v-if="register">-->
<!--          <router-link class="link-type" :to="'/register'">立即注册</router-link>-->
<!--        </div>-->
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
<!--      <span>Copyright © 2018-2025 ruoyi.vip All Rights Reserved.</span>-->
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关
      captchaEnabled: true,
      // 注册开关
      register: true,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
        // ========== ↓↓↓ 添加这一行 ↓↓↓ ==========
        // 读取后端返回的注册开关状态
        this.register = res.register === undefined ? true : res.register
        // ========== ↑↑↑ 添加结束 ↑↑↑ ==========
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #F2F6FC;
  font-size: 30px;     /* 文字大小，按需调整 */
  font-weight: 600;    /* 加粗 */
  letter-spacing: 1px; /* 可选：字间距 */
}

.login-form {
  border-radius: 6px;
  /* 使用 rgba 调整透明度（0.0 - 1.0），示例为 0.85 */
  background: rgba(0, 0, 0, 0.35);
  /* 可选：给玻璃效果 */
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  width: 400px;
  padding: 25px 25px 5px 25px;
  z-index: 1;
  .el-input {
    height: 38px;

    input {
      height: 38px;
      //新增
      background: transparent; // 背景透明
      border: none; // 移除默认边框
      border-bottom: 3px solid #FFFFFF; // 只保留深色底线
      border-radius: 0; // 移除圆角
      color: #ffffff; // 输入文字颜色
      // 修改 placeholder 颜色
      &::placeholder {
        color: #ffffff;
        //新增结束
      }
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
    color: #ffffff; // 图标颜色
  }

  /* ========== 新增：登录选项容器样式 ========== */
  .login-options {
    display: flex;            /* 启用 Flex 布局 */
    justify-content: space-between; /* 两端对齐，中间元素自动居中 */
    align-items: center;      /* 垂直居中对齐 */
    margin-bottom: 25px;      /* 底部留出间距，替换掉原先 el-checkbox 的 margin */
    font-size: 14px;          /* 统一字体大小 */
  }

  /* 调整复选框的样式，去掉它自带的 margin */
  .el-checkbox {
    margin: 0 !important;
  }
  /* ========== 新增结束 ========== */

  //新增
  // --- 关键修改 2: 登录按钮样式 (参考图1) ---
  .el-form-item .el-button--primary {
    width: 100% !important;
    background: linear-gradient(90deg, #f05d49, #4285f4) !important;
    border: none !important;
    border-radius: 18px !important;

    &:hover {
      opacity: 0.9;
    }
  }

  // --- 关键修改 3: 统一其他元素颜色 ---
  .el-checkbox {
    // (A) "记住密码" 文字的颜色
    .el-checkbox__label {
      color: #ffffff;
    }

    // (B) 未勾选时，方框的边框颜色
    .el-checkbox__inner {
      border-color: #ffffff;
      background: transparent; // 确保方框也是透明的
    }

    // (C) 勾选后，方框的背景色和边框色
    .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: #c3d770; // 改为按钮的红色
      border-color: #c3d770; // 边框也改为红色
    }

    // (D) 勾选后，那个 "√" 勾的颜色
    .el-checkbox__input.is-checked .el-checkbox__inner::after {
      border-color: #ffffff; // 勾改为白色
    }
    // --- (E) 【本次新增】勾选后，"记住密码" 文字的颜色 ---
    // .is-checked + .el-checkbox__label 意思是:
    // 找到 "is-checked" 的输入框，然后改变它后面紧跟着的 label 文字
    .el-checkbox__input.is-checked + .el-checkbox__label {
      color: #c3d770; // 勾选后文字也变为红色 (你也可以改成 #FFFFFF 或其他颜色)
    }
  }
  .link-type {
    color: #ffffff; // "忘记密码"
    &:hover {
      color: #c3d770; // 悬浮时变为按钮的蓝色
    }
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
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
.login-code-img {
  height: 38px;
}
</style>
