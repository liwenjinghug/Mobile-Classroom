const api = require('../../utils/api');
Page({
  data: {
    listening: [],
    teaching: []
  },
  onLoad(options) {
    this.loadCourses();
  },
  onShow() {
    // 每次显示尝试刷新
    this.loadCourses();
  },
  loadCourses() {
    api.getMyCourses().then(res => {
      // backend should return {listening: [], teaching: []}
      this.setData({ listening: res.listening || [], teaching: res.teaching || [] });
    }).catch(err => {
      console.warn('loadCourses failed', err);
    });
  },
  onJoinClass() {
    wx.showModal({ title: '加入课堂', content: '请联系教师或输入课堂码（示例未实现）' });
  },
  onCreateClass() {
    wx.showModal({ title: '创建课堂', content: '创建课堂的表单示例未实现，后续可扩展' });
  },
  openCourse(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/course/detail?courseId=' + id });
  }
});

