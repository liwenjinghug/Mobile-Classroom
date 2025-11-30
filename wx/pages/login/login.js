const api = require('../../utils/api');
Page({
  data: {
    studentNo: '',
    password: '',
    captchaEnabled: true,
    captchaImg: '',
    captchaUuid: '',
    captchaCode: '',
    // always attempt captcha by default; remove auto-skip in local environments so dev can test captcha flows
    localDebugSkipCaptcha: false,
    captchaError: '',
    lastLoginResponse: ''
  },
  onLoad() {
    // 尝试加载验证码（始终尝试加载以便测试验证码相关逻辑）
    this.refreshCaptcha();
  },
  onStudentNoInput(e) {
    this.setData({ studentNo: e.detail.value });
  },
  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },
  onCaptchaInput(e) {
    this.setData({ captchaCode: e.detail.value, captchaError: '' });
  },
  openWebCaptcha() {
    // open a web-view page showing the captchaImage endpoint
    try {
      const base = getApp().globalData && getApp().globalData.baseUrl ? getApp().globalData.baseUrl : '';
      // Use the direct captcha image endpoint which most backends expose
      const url = (base ? base.replace(/\/$/, '') : '') + '/captchaImage'
      wx.navigateTo({ url: '/pages/login/webcaptcha?url=' + encodeURIComponent(url) })
    } catch (e) { console.error('openWebCaptcha failed', e) }
  },
  refreshCaptcha() {
    const that = this;
    try { console.log('refreshCaptcha triggered'); } catch (e) {}
    api.getCaptcha().then(res => {
      // 后端返回可能是 { img: 'base64...', uuid: 'xxx', captchaEnabled: true }
      try {
        const img = (res && res.img) ? res.img : (res && res.data && res.data.img ? res.data.img : null);
        const uuid = (res && res.uuid) ? res.uuid : (res && res.data && res.data.uuid ? res.data.uuid : null);
        const enabled = (res && typeof res.captchaEnabled !== 'undefined') ? res.captchaEnabled : (res && res.data && typeof res.data.captchaEnabled !== 'undefined' ? res.data.captchaEnabled : true);
        that.setData({ captchaImg: img, captchaUuid: uuid, captchaEnabled: enabled, captchaError: '', lastLoginResponse: '' });
      } catch (e) {
        console.warn('parse captcha response failed', e, res);
      }
    }).catch(err => {
      console.error('getCaptcha error', err);
      // 仍然保证页面可用
      that.setData({ captchaEnabled: false, captchaError: '获取验证码失败，请稍后重试' });
    });
  },
  onSubmit() {
    // 调试提示：确认点击事件是否触发
    try {
      console.log('login.onSubmit triggered', this.data);
      wx.showToast({ title: '开始登录...', icon: 'none', duration: 600 });
    } catch (e) {
      // 即使 showToast 失败也继续
      console.error('showToast error', e);
    }

    const { studentNo, password, captchaCode, captchaUuid, localDebugSkipCaptcha } = this.data;
    if (!studentNo || studentNo.trim() === '') {
      wx.showToast({ title: '请输入学号/账号', icon: 'none' });
      return;
    }

    if (!api || typeof api.login !== 'function') {
      wx.showModal({ title: '调试错误', content: '无法找到 login 方法：请检查 wx/utils/api.js', showCancel: false });
      return;
    }

    wx.showLoading({ title: '登录中...' });
    try {
      // 本地调试时跳过验证码参数，避免后端验证码导致的登录失败
      const payload = { studentNo: studentNo.trim(), password: password || '', code: (captchaCode || ''), uuid: (captchaUuid || '') };
      // If captcha is enabled on UI, require the user to input a code
      if (this.data.captchaEnabled && (!captchaCode || String(captchaCode).trim() === '')) {
        wx.hideLoading();
        wx.showToast({ title: '请输入验证码', icon: 'none' });
        return;
      }
      api.login(payload)
        .then(res => {
          wx.hideLoading();
          // Defensive: if login resolved but token missing, show the raw response for debugging
          if (!res || !res.token) {
            try {
              const raw = res && res.raw ? res.raw : res
              const detail = typeof raw === 'string' ? raw : JSON.stringify(raw, null, 2)
              this.setData({ lastLoginResponse: detail })
            } catch (e) {}
            wx.showModal({ title: '登录未成功（无 token）', content: '后端未返回 token，请查看下方的调试信息', showCancel: false })
            return
          }
          // clear last response debug
          this.setData({ lastLoginResponse: '' });
          wx.setStorageSync('token', res.token);
          // 保存学号和用户信息
          wx.setStorageSync('studentNo', studentNo.trim());
          if (res.raw && res.raw.user) {
            wx.setStorageSync('userInfo', res.raw.user);
          }
          wx.showToast({ title: '登录成功', icon: 'success' });
          // 登录后切换到课程 tab
          wx.switchTab({ url: '/pages/course/list' });
        })
        .catch(err => {
          wx.hideLoading();
          // show raw response in debug area for easier troubleshooting
          try { const raw = (err && err.raw) ? err.raw : err; this.setData({ lastLoginResponse: typeof raw === 'string' ? raw : JSON.stringify(raw, null, 2) }); } catch(e){}
          // 如果后端返回了结构化的 NO_TOKEN 错误，弹窗显示原始响应并提供复制功能
          if (err && err.code === 'NO_TOKEN') {
            const raw = err.raw || err;
            const detail = typeof raw === 'string' ? raw : JSON.stringify(raw, null, 2);
            // 如果 msg 是 验证码已失效，自动刷新验证码
            try {
              const rawObj = typeof raw === 'string' ? JSON.parse(raw) : raw;
              if (rawObj && rawObj.msg && rawObj.msg.indexOf('验证码') !== -1) {
                // show inline error and refresh the captcha image
                this.setData({ captchaError: '验证码已失效请刷新' });
                // 仅在非本地调试下刷新验证码
                if (!this.data.localDebugSkipCaptcha) this.refreshCaptcha();
              }
            } catch (e) {}
            wx.showModal({
              title: '登录失败：后端未返回 token',
              content: '后端响应（可复制）：\n' + (detail.length > 800 ? detail.substring(0, 800) + '... (截断)' : detail),
              showCancel: true,
              cancelText: '关闭',
              confirmText: '复制调试信息',
              success(modalRes) {
                if (modalRes.confirm) {
                  wx.setClipboardData({ data: detail, success() { wx.showToast({ title: '已复制到剪贴板', icon: 'none' }); } });
                }
              }
            });
            return;
          }

          wx.showToast({ title: err && err.message ? err.message : '登录失败，请检查后端或网络', icon: 'none' });
        });
    } catch (ex) {
      wx.hideLoading();
      console.error('login exception', ex);
      wx.showModal({ title: '异常', content: String(ex), showCancel: false });
    }
  }
});
