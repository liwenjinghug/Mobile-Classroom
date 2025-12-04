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
        const submissionIdNum = Number(submissionId);
        let submission = list.find(item => {
          const itemId = Number(item.studentHomeworkId || item.student_homework_id || item.id);
          return itemId === submissionIdNum;
        });

        if (!submission) {
          wx.showToast({ title: '未找到提交记录', icon: 'none' });
          this.setData({ loading: false });
          return;
        }

        // 统一分数字段
        if (submission.score === undefined && submission.grade !== undefined) {
          submission.score = submission.grade;
        }

        // 检查是否已批改 - 兼容多种格式
        // isGraded=1 或 is_graded=1 或 status=2 或 有分数(score/grade) 或 有批改时间
        const hasScore = (submission.score !== null && submission.score !== undefined && submission.score !== '') ||
                         (submission.grade !== null && submission.grade !== undefined && submission.grade !== '');
        const graded = submission.isGraded === 1 ||
                       submission.isGraded === '1' ||
                       submission.is_graded === 1 ||
                       submission.is_graded === '1' ||
                       Number(submission.status) === 2 ||
                       hasScore ||
                       submission.correctedTime ||
                       submission.corrected_time;

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

