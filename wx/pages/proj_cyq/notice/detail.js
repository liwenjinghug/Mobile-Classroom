const api = require('../../../utils/api.js');
const app = getApp(); // 获取全局应用实例

Page({
  data: {
    notice: {},
    // 这里的 baseUrl 取自 app.js 的 globalData
    baseUrl: app.globalData.baseUrl || 'http://localhost:8080'
  },

  onLoad(options) {
    // 页面加载时获取参数 id
    if (options.id) {
      this.getNoticeDetail(options.id);
    }
  },

  getNoticeDetail(id) {
    api.getNoticeDetail(id).then(res => {
      let data = res.data;
      
      // 【核心修复】处理富文本内容
      if (data.content) {
        const baseUrl = this.data.baseUrl;
        
        // 1. 【新增】处理带有 /dev-api 前缀的路径 (这是你之前缺失的逻辑)
        // 例如: src="/dev-api/profile/..." -> src="http://ip:port/profile/..."
        data.content = data.content.replace(/src="\/dev-api\/profile/g, `src="${baseUrl}/profile`);
        
        // 2. 处理标准 /profile 前缀的路径
        // 例如: src="/profile/..." -> src="http://ip:port/profile/..."
        data.content = data.content.replace(/src="\/profile/g, `src="${baseUrl}/profile`);
        
        // 3. 修复图片尺寸：防止大图撑破屏幕
        // 给所有 <img> 标签强制添加 style 样式
        data.content = data.content.replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;margin:10px 0;"');
      }

      this.setData({ notice: data });
    }).catch(err => {
      console.error("获取公告详情失败", err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  }
});