const api = require('../../utils/api');
Page({
  data: { course: {} },
  onLoad(options) {
    const courseId = options.courseId;
    if (courseId) {
      api.getCourseDetail(courseId).then(res => {
        this.setData({ course: res || { id: courseId, title: '课堂 ' + courseId } });
      }).catch(() => {
        this.setData({ course: { id: courseId, title: '课堂 ' + courseId } });
      });
    }
  },
  goToHomework() { wx.showToast({ title: '进入作业（后续实现）', icon: 'none' }); },
  goToExam() { wx.showToast({ title: '进入考试（后续实现）', icon: 'none' }); },
  goToSign() { wx.showToast({ title: '进入签到（后续实现）', icon: 'none' }); },
  goToForum() { wx.navigateTo({ url: '/pages/forum/forum' }); }
});

