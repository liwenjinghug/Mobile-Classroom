const api = require('../../utils/api');
Page({
  data: { course: {}, loading: true, error: false },
  onLoad(options) {
    const sessionId = options.sessionId;  
    if (!sessionId) {
      wx.showToast({ title: '课堂ID缺失', icon: 'none' });
      return;
    }

    this.sessionId = sessionId;
    this.loadCourseDetail(sessionId);
  },

  loadCourseDetail(sessionId) {
    const that = this;
    that.setData({ loading: true, error: false });

    // 尝试使用新的课堂详情接口
    api.getClassDetail(sessionId)
      .then(res => {
        that.setData({
          course: res || { id: sessionId, title: '课堂 ' + sessionId },
          loading: false
        });
      })
      .catch(err => {
        console.warn('getClassDetail failed, trying getCourseDetail...', err);
        // 如果新接口失败，尝试旧接口
        return api.getCourseDetail(sessionId)
          .then(res => {
            that.setData({
              course: res || { id: sessionId, title: '课堂 ' + sessionId },
              loading: false
            });
          })
          .catch(err2 => {
            console.warn('getCourseDetail also failed', err2);
            // 两个接口都失败，使用默认数据
            that.setData({
              course: { id: sessionId, title: '课堂 ' + sessionId, className: '课堂 ' + sessionId },
              loading: false,
              error: true
            });
          });
      });
  },

  goToHomework() {
    const id = this.sessionId || (this.data.course && this.data.course.id);
    const name = this.data.course && (this.data.course.className || this.data.course.title) || '课堂';
    if (!id) {
      wx.showToast({ title: '课堂ID缺失', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: `/pages/proj_fz/homework/homework-list?sessionId=${id}&sessionName=${encodeURIComponent(name)}`
    });
  },

  goToExam() {
    const id = this.sessionId || (this.data.course && this.data.course.id);
    if (!id) {
      wx.showToast({ title: '课堂ID缺失', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: '/pages/proj_lwj/exam/list?sessionId=' + id });
  },

  goToSign() {
    wx.showToast({ title: '进入签到（后续实现）', icon: 'none' });
  },

  goToForum() {
    wx.navigateTo({ url: '/pages/forum/forum' });
  }
});
