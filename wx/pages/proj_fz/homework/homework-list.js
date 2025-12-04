// 作业列表页面 - 使用proj_fz接口
const api = require('../../../utils/api');

Page({
  data: {
    sessionId: null,
    sessionName: '',
    homeworkList: [],
    allHomeworkList: [],
    mySubmissions: [],
    loading: false,
    isEmpty: false,
    hasActiveFilter: false,

    // 筛选条件 - 只保留状态筛选
    filterForm: {
      isExpired: '',    // ''/yes/no
      isSubmitted: '',  // ''/yes/no
      isGraded: ''      // ''/yes/no
    }
  },

  onLoad(options) {
    const sessionId = options.sessionId;
    // 解码sessionName，避免中文乱码
    const sessionName = options.sessionName ? decodeURIComponent(options.sessionName) : '课堂作业';

    if (!sessionId) {
      wx.showToast({ title: '课堂ID缺失', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }

    this.setData({
      sessionId: sessionId,
      sessionName: sessionName
    });

    this.loadHomeworkList();
  },

  // 页面显示时刷新（每次显示都刷新，确保数据最新）
  onShow() {
    // 如果已经加载过sessionId，每次显示都刷新数据
    if (this.data.sessionId) {
      this.loadHomeworkList();
    }
  },

  // 加载作业列表
  loadHomeworkList() {
    const { sessionId } = this.data;

    console.log('开始加载作业列表', { sessionId });
    this.setData({ loading: true, isEmpty: false });

    // 并行请求：作业列表 + 我的提交记录
    Promise.all([
      api.getHomeworkList(sessionId),
      api.getMyHomeworkSubmissions()
    ]).then(([homeworkRes, submissionRes]) => {
      console.log('作业列表接口返回', homeworkRes);
      console.log('提交记录接口返回', submissionRes);

      let allList = homeworkRes.rows || homeworkRes.data || homeworkRes || [];
      const submissions = submissionRes.rows || submissionRes.data || submissionRes || [];

      if (!Array.isArray(allList)) {
        allList = [];
      }

      console.log('作业列表数量:', allList.length, '提交记录数量:', submissions.length);

      this.setData({
        allHomeworkList: allList,
        mySubmissions: submissions,
        loading: false
      }, () => {
        this.filterHomeworkList();
      });
    }).catch(err => {
      console.error('加载失败', err);
      wx.showToast({ title: err.msg || '加载失败', icon: 'none' });
      this.setData({ loading: false, isEmpty: true });
    });
  },

  // 筛选作业列表
  filterHomeworkList() {
    const { allHomeworkList, mySubmissions, filterForm } = this.data;
    const now = new Date();

    let filteredList = allHomeworkList.map(homework => {
      // 查找该作业的提交记录
      const homeworkIdNum = Number(homework.homeworkId || homework.homework_id);
      const submission = mySubmissions.find(s => {
        const submissionHomeworkId = Number(s.homeworkId || s.homework_id);
        return submissionHomeworkId === homeworkIdNum;
      });

      // 判断是否过期
      const deadline = homework.deadline ? new Date(homework.deadline.replace(/-/g, '/')) : null;
      const isExpired = deadline ? now > deadline : false;

      // 判断提交状态
      const isSubmitted = !!(submission && (
        Number(submission.status) >= 1 ||
        submission.submitTime ||
        submission.submit_time
      ));

      // 判断批改状态
      const hasScore = submission && (
        (submission.score !== null && submission.score !== undefined && submission.score !== '') ||
        (submission.grade !== null && submission.grade !== undefined && submission.grade !== '')
      );
      const isGraded = !!(submission && (
        submission.isGraded === 1 ||
        submission.isGraded === '1' ||
        submission.is_graded === 1 ||
        submission.is_graded === '1' ||
        Number(submission.status) === 2 ||
        hasScore ||
        submission.correctedTime ||
        submission.corrected_time
      ));

      // 获取分数
      const score = submission ? (submission.score ?? submission.grade ?? null) : null;

      return {
        ...homework,
        homeworkId: homeworkIdNum,
        title: homework.title || '',
        submission: submission ? { ...submission, score } : null,
        isExpired,
        isSubmitted,
        isGraded
      };
    });

    // 应用状态筛选
    filteredList = filteredList.filter(item => {
      // 截止状态筛选
      if (filterForm.isExpired === 'yes' && !item.isExpired) return false;
      if (filterForm.isExpired === 'no' && item.isExpired) return false;

      // 提交状态筛选
      if (filterForm.isSubmitted === 'yes' && !item.isSubmitted) return false;
      if (filterForm.isSubmitted === 'no' && item.isSubmitted) return false;

      // 批改状态筛选
      if (filterForm.isGraded === 'yes' && !item.isGraded) return false;
      if (filterForm.isGraded === 'no' && item.isGraded) return false;

      return true;
    });

    // 检查是否有激活的筛选条件
    const hasActiveFilter = !!(filterForm.isExpired || filterForm.isSubmitted || filterForm.isGraded);

    this.setData({
      homeworkList: filteredList,
      isEmpty: filteredList.length === 0,
      hasActiveFilter
    });
  },

  // 筛选条件变更
  onFilterChange(e) {
    const field = e.currentTarget.dataset.field;
    const index = parseInt(e.detail.value);

    let value = '';
    if (index === 1) {
      value = field === 'isExpired' ? 'no' : 'yes';  // 进行中/已提交/已批改
    } else if (index === 2) {
      value = field === 'isExpired' ? 'yes' : 'no';  // 已截止/未提交/未批改
    }

    this.setData({
      [`filterForm.${field}`]: value
    });

    // 立即应用筛选
    this.filterHomeworkList();
  },

  // 重置筛选
  resetFilter() {
    this.setData({
      filterForm: {
        isExpired: '',
        isSubmitted: '',
        isGraded: ''
      }
    });
    this.filterHomeworkList();
  },

  // 进入作业详情
  goToDetail(e) {
    const homeworkId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/proj_fz/homework/homework-detail?homeworkId=${homeworkId}`
    });
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadHomeworkList();
    setTimeout(() => wx.stopPullDownRefresh(), 1000);
  }
});

