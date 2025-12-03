const api = require('../../../utils/api.js');
const app = getApp();

Page({
  data: {
    notice: {},
    baseUrl: app.globalData.baseUrl || 'http://localhost:8080'
  },

  onLoad(options) {
    if (options.id) {
      this.getNoticeDetail(options.id);
    }
  },

  getNoticeDetail(id) {
    api.getNoticeDetail(id).then(res => {
      // 【兼容修复】
      let data = res.data || res;
      
      if (data.content) {
        const baseUrl = this.data.baseUrl;
        data.content = data.content.replace(/src="\/dev-api\/profile/g, `src="${baseUrl}/profile`);
        data.content = data.content.replace(/src="\/profile/g, `src="${baseUrl}/profile`);
        data.content = data.content.replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;margin:10px 0;"');
      }

      this.setData({ notice: data });
    }).catch(err => {
      console.error("获取公告详情失败", err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  }
});