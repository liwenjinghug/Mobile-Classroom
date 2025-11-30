const api = require('../../utils/api');
Page({
  data: { course: {} },
  onLoad(options) {
    const sessionId = options.sessionId;  
    if (sessionId) {
      api.getCourseDetail(sessionId).then(res => {
        this.setData({ course: res || { id: sessionId, title: '课堂 ' + sessionId } });
      }).catch(() => {
        this.setData({ course: { id: sessionId, title: '课堂 ' + sessionId } });
      });
    }
  },
  goToHomework() { wx.showToast({ title: '进入作业（后续实现）', icon: 'none' }); },
  goToExam() { wx.showToast({ title: '进入考试（后续实现）', icon: 'none' }); },
  goToSign() { wx.showToast({ title: '进入签到（后续实现）', icon: 'none' }); },
  goToForum() { wx.navigateTo({ url: '/pages/forum/forum' }); }
});
