const api = require('../../../../utils/api.js');

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  },

  bindInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  submit() {
    const { oldPassword, newPassword, confirmPassword } = this.data;

    if (!oldPassword) return wx.showToast({ title: '请输入旧密码', icon: 'none' });
    if (!newPassword) return wx.showToast({ title: '请输入新密码', icon: 'none' });
    if (newPassword !== confirmPassword) return wx.showToast({ title: '两次密码不一致', icon: 'none' });

    wx.showLoading({ title: '提交中...' });
    
    api.updateUserPwd(oldPassword, newPassword).then(res => {
      wx.hideLoading();
      if (res.code === 200) {
        wx.showToast({ title: '修改成功' });
        setTimeout(() => wx.navigateBack(), 1500);
      } else {
        wx.showToast({ title: res.msg || '修改失败', icon: 'none' });
      }
    }).catch(err => {
      wx.hideLoading();
      console.error(err);
      wx.showToast({ title: '请求失败', icon: 'none' });
    });
  }
});