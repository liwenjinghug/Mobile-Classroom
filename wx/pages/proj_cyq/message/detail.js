const api = require('../../../utils/api.js');
const app = getApp();

Page({
  data: {
    msg: {},
    baseUrl: app.globalData.baseUrl || 'http://localhost:8080'
  },

  onLoad(options) {
    if (options.item) {
      try {
        const item = JSON.parse(decodeURIComponent(options.item));
        // 格式化类型名称
        item.typeName = item.type === 'todo' ? '待办' : (item.type === 'homework' ? '作业' : '考试');
        
        // 修复内容中的图片路径
        if (item.content) {
           item.content = this.fixContentImg(item.content);
        }
        this.setData({ msg: item });
      } catch (e) {
        console.error('解析消息详情失败', e);
      }
    }
  },

  // 辅助：修复图片路径 (必须保留，否则图片不显示)
  fixContentImg(html) {
    if (!html) return '';
    const baseUrl = this.data.baseUrl;
    // 替换相对路径为绝对路径
    html = html.replace(/src="\/dev-api\/profile/g, `src="${baseUrl}/profile`);
    html = html.replace(/src="\/profile/g, `src="${baseUrl}/profile`);
    // 让图片自适应宽度，防止撑破屏幕
    html = html.replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;margin:10px auto;border-radius:8rpx;"');
    return html;
  }
});