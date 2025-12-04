// 作业成绩查看页面 - 使用proj_fz接口
const api = require('../../../utils/api');

Page({
  data: {
    submissionId: null,
    submission: null,
    gradeInfo: null,
    loading: true,
    graded: false
  },

  onLoad(options) {
    const submissionId = options.submissionId;
    const homeworkId = options.homeworkId;

    if (!submissionId && !homeworkId) {
      wx.showToast({ title: '参数缺失', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }

    this.setData({
      submissionId: submissionId,
      homeworkId: homeworkId
    });
    this.loadSubmission();
  },

  // 加载提交记录（包含成绩）
  loadSubmission() {
    const { submissionId, homeworkId } = this.data;

    this.setData({ loading: true });

    // 通过查询我的所有提交记录，找到对应的记录
    api.getMyHomeworkSubmissions()
      .then(res => {
        console.log('我的提交记录', res);

        const list = res.rows || res.data || res || [];
        let submission = null;

        // 先通过submissionId查找
        if (submissionId) {
          const submissionIdNum = Number(submissionId);
          submission = list.find(item => {
            const itemId = Number(item.studentHomeworkId || item.student_homework_id || item.id);
            return itemId === submissionIdNum;
          });
        }

        // 如果没找到，通过homeworkId查找
        if (!submission && homeworkId) {
          const homeworkIdNum = Number(homeworkId);
          submission = list.find(item => {
            const itemHomeworkId = Number(item.homeworkId || item.homework_id);
            return itemHomeworkId === homeworkIdNum;
          });
        }

        if (!submission) {
          wx.showToast({ title: '未找到提交记录', icon: 'none' });
          this.setData({ loading: false });
          return;
        }

        console.log('找到的提交记录:', submission);

        // 统一分数字段
        const score = submission.score ?? submission.grade ?? null;

        // 统一评语字段（去掉空白后判断）
        const remarkRaw = submission.remark || submission.grade_comment || submission.gradeComment || '';
        const remark = (remarkRaw && remarkRaw.trim() !== '') ? remarkRaw.trim() : '';

        // 统一批改时间字段
        const correctedTime = submission.correctedTime || submission.corrected_time || '';

        // 检查是否已批改
        const hasScore = score !== null && score !== undefined && score !== '';
        const graded = submission.isGraded === 1 ||
                       submission.isGraded === '1' ||
                       submission.is_graded === 1 ||
                       submission.is_graded === '1' ||
                       Number(submission.status) === 2 ||
                       hasScore ||
                       correctedTime;

        // 构建成绩信息对象
        const gradeInfo = {
          score: score !== null && score !== undefined ? score : '--',
          remark: remark,
          correctedTime: correctedTime || '暂无',
          gradeAttachment: submission.gradeAttachment || submission.grade_attachment || ''
        };

        // 将评语中的换行符转换为<br>用于rich-text显示
        const remarkText = remark || '暂无评语';
        const remarkHtml = remarkText.replace(/\n/g, '<br/>');

        console.log('成绩信息:', gradeInfo);

        this.setData({
          submission: submission,
          gradeInfo: gradeInfo,
          remarkNodes: remarkHtml,
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

