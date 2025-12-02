// 作业成绩查看页面 - 使用proj_fz接口
const api = require('../../../utils/api');

Page({
  data: {
    submissionId: null,
    submission: null,
    loading: true,
    graded: false
  },

  onLoad(options) {
    const submissionId = options.submissionId;

    if (!submissionId) {
      wx.showToast({ title: '提交记录ID缺失', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }

    this.setData({ submissionId: submissionId });
    this.loadSubmission();
  },

  // 加载提交记录（包含成绩）
  loadSubmission() {
    const { submissionId } = this.data;

    this.setData({ loading: true });

    // 通过查询我的所有提交记录，找到对应的记录
    api.getMyHomeworkSubmissions()
      .then(res => {
        console.log('我的提交记录', res);

        const list = res.rows || res || [];
        const submission = list.find(item => item.studentHomeworkId == submissionId || item.id == submissionId);

        if (!submission) {
          wx.showToast({ title: '未找到提交记录', icon: 'none' });
          this.setData({ loading: false });
          return;
        }

        // 检查是否已批改
        const graded = submission.isGraded === 1;

        this.setData({
          submission: submission,
          graded: graded,
          loading: false
        });
      })
      .catch(err => {
        console.error('加载失败', err);
        wx.showToast({ title: '加载失败', icon: 'none' });
        this.setData({ loading: false });
      });
  },

  // 返回详情页
  goBack() {
    wx.navigateBack();
  }
});

