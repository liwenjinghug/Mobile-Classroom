const api = require('../../../utils/api.js');
const app = getApp();

Page({
  data: {
    userInfo: {},
    baseUrl: app.globalData.baseUrl || 'http://localhost:8080'
  },

  onShow() {
    this.getUserInfo();
  },

  getUserInfo() {
    api.getUserProfile().then(res => {
      const user = res.data;
      // 处理头像路径
      if (user.avatar && !user.avatar.startsWith('http')) {
        user.avatar = this.data.baseUrl + user.avatar;
      }
      this.setData({ userInfo: user });
    }).catch(err => {
      console.error("获取用户信息失败", err);
    });
  },

  // 修改头像
  handleAvatarClick() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0];
        wx.showLoading({ title: '上传中...' });
        
        api.uploadAvatar(tempFilePath).then(res => {
          wx.hideLoading();
          wx.showToast({ title: '上传成功' });
          this.getUserInfo(); // 刷新信息
        }).catch(err => {
          wx.hideLoading();
          wx.showToast({ title: '上传失败', icon: 'none' });
        });
      }
    });
  },

  navigateToPwd() {
    wx.navigateTo({ url: '/pages/proj_cyq/mine/pwd/index' });
  },

  // 【保留】关于我们
  handleAbout() {
    wx.showModal({ 
      title: '关于', 
      content: '移动课堂 v1.0.0\n基于若依(RuoYi)开发', 
      showCancel: false 
    });
  },

  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.reLaunch({ url: '/pages/login/login' });
        }
      }
    });
  }
});