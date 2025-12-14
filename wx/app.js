// app.js - 全局入口，负责登录态检查
App({
  onLaunch() {
    const token = wx.getStorageSync('token');
    // 如果没有登录令牌，跳转到登录页
    // 开发提示：若后端未就绪或域名不在小程序合法域名中，可以在开发者工具的 Storage 中手动设置 key=`token` 的值以跳过登录（仅用于本地调试）。
    if (!token) {
      wx.reLaunch({ url: '/pages/login/login' });
    }

    // Dev helper: require a non-executing placeholder so devtools marks exam pages as used
    try {
      if (this.globalData && this.globalData.mockMode) {
        require('pages/exam/_entry');
      }
    } catch (e) {
      console.warn('dev placeholder require failed', e);
    }
  },
  globalData: {
    // 本地开发默认后端地址，若使用 ngrok/https 请替换为 https://xxxx.ngrok.io
    baseUrl: 'http://localhost:8080',
    // mockMode 开关：默认关闭，使用真实后端数据；仅在后端未就绪时临时打开
    mockMode: false
  }
});