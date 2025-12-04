// 作业列表页面 - 使用proj_fz接口
const api = require('../../../utils/api');

Page({
  data: {
    sessionId: null,
    sessionName: '',
    homeworkList: [],
    allHomeworkList: [], // 保存所有作业数据
    mySubmissions: [], // 保存我的提交记录
    loading: false,
    isEmpty: false,
    hasActiveFilter: false, // 是否有激活的筛选条件

    // 筛选条件
    showFilter: false,
    filterForm: {
      courseName: '',
      className: '',
      title: '',
      publishStartDate: '',
      publishEndDate: '',
      deadlineStartDate: '',
      deadlineEndDate: '',
      isExpired: '',    // ''/yes/no
      isSubmitted: '',  // ''/yes/no
      isGraded: ''      // ''/yes/no
    },

    // 课程和课堂选项
    courseOptions: [],
    classOptions: [],
    titleOptions: []
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

      const allList = homeworkRes.rows || homeworkRes || [];
      const submissions = submissionRes.rows || submissionRes || [];

      console.log('解析后的作业列表', allList, '数量:', allList.length);
      console.log('我的提交记录', submissions, '数量:', submissions.length);

      // 提取课程和作业名称选项 - 兼容不同的字段名
      const courseSet = new Set();
      const classSet = new Set();
      const titleSet = new Set();

      allList.forEach(item => {
        // 兼容 courseName 或 course_name
        const courseName = item.courseName || item.course_name;
        // 兼容 className 或 class_name
        const className = item.className || item.class_name;
        // 兼容 title 或 homeworkTitle
        const title = item.title || item.homeworkTitle;

        if (courseName) courseSet.add(courseName);
        if (className) classSet.add(className);
        if (title) titleSet.add(title);
      });

      console.log('筛选选项 - 课程:', Array.from(courseSet), '课堂:', Array.from(classSet), '作业:', Array.from(titleSet));

      this.setData({
        allHomeworkList: allList,
        mySubmissions: submissions,
        courseOptions: Array.from(courseSet),
        classOptions: Array.from(classSet),
        titleOptions: Array.from(titleSet),
        loading: false
      });

      // 筛选显示
      this.filterHomeworkList();
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

    console.log('开始筛选作业列表，mySubmissions:', mySubmissions);

    let filteredList = allHomeworkList.map(homework => {
      // 查找该作业的提交记录 - 使用Number()确保类型一致
      const homeworkIdNum = Number(homework.homeworkId || homework.homework_id);
      const submission = mySubmissions.find(s => {
        const submissionHomeworkId = Number(s.homeworkId || s.homework_id);
        return submissionHomeworkId === homeworkIdNum;
      });

      console.log(`作业ID: ${homeworkIdNum}, 找到提交记录:`, submission);

      // 判断是否过期
      const deadline = homework.deadline ? new Date(homework.deadline) : null;
      const isExpired = deadline ? now > deadline : false;

      // 判断提交状态 - 兼容多种字段格式
      const isSubmitted = !!(submission && (
        Number(submission.status) >= 1 ||
        submission.submitTime ||
        submission.submit_time
      ));

      // 判断批改状态 - 兼容多种字段格式
      // isGraded=1 或 status=2 或 有分数(score/grade) 或 有批改时间
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

      // 获取课程和课堂名称 - 兼容不同字段名
      const courseName = homework.courseName || homework.course_name || '';
      const className = homework.className || homework.class_name || '';
      const title = homework.title || homework.homeworkTitle || '';

      // 获取分数 - 兼容score和grade字段
      const score = submission ? (submission.score ?? submission.grade ?? null) : null;

      return {
        ...homework,
        homeworkId: homeworkIdNum,
        courseName,
        className,
        title,
        submission: submission ? { ...submission, score } : null,
        isExpired,
        isSubmitted,
        isGraded
      };
    });

    // 应用筛选条件
    filteredList = this.applyAdvancedFilter(filteredList);

    // 检查是否有激活的筛选条件
    const hasActiveFilter = this.checkHasActiveFilter();

    console.log('筛选后的作业列表', filteredList, '数量:', filteredList.length);

    this.setData({
      homeworkList: filteredList,
      isEmpty: filteredList.length === 0,
      hasActiveFilter
    });
  },

  // 检查是否有激活的筛选条件
  checkHasActiveFilter() {
    const { filterForm } = this.data;
    return !!(
      filterForm.courseName ||
      filterForm.className ||
      filterForm.title ||
      filterForm.publishStartDate ||
      filterForm.publishEndDate ||
      filterForm.deadlineStartDate ||
      filterForm.deadlineEndDate ||
      filterForm.isExpired ||
      filterForm.isSubmitted ||
      filterForm.isGraded
    );
  },

  // 应用高级筛选条件
  applyAdvancedFilter(list) {
    const { filterForm } = this.data;

    return list.filter(item => {
      // 课程名称筛选
      if (filterForm.courseName && item.courseName &&
          item.courseName.indexOf(filterForm.courseName) === -1) {
        return false;
      }

      // 课堂名称筛选
      if (filterForm.className && item.className &&
          item.className.indexOf(filterForm.className) === -1) {
        return false;
      }

      // 作业名称筛选
      if (filterForm.title && item.title &&
          item.title.indexOf(filterForm.title) === -1) {
        return false;
      }

      // 发布时间范围筛选
      if (filterForm.publishStartDate && item.createTime) {
        const createTime = new Date(item.createTime);
        const startDate = new Date(filterForm.publishStartDate);
        if (createTime < startDate) return false;
      }
      if (filterForm.publishEndDate && item.createTime) {
        const createTime = new Date(item.createTime);
        const endDate = new Date(filterForm.publishEndDate);
        endDate.setHours(23, 59, 59, 999);
        if (createTime > endDate) return false;
      }

      // 截止时间范围筛选
      if (filterForm.deadlineStartDate && item.deadline) {
        const deadline = new Date(item.deadline);
        const startDate = new Date(filterForm.deadlineStartDate);
        if (deadline < startDate) return false;
      }
      if (filterForm.deadlineEndDate && item.deadline) {
        const deadline = new Date(item.deadline);
        const endDate = new Date(filterForm.deadlineEndDate);
        endDate.setHours(23, 59, 59, 999);
        if (deadline > endDate) return false;
      }

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
  },

  // 显示/隐藏高级筛选面板
  toggleFilter() {
    this.setData({ showFilter: !this.data.showFilter });
  },

  // 快捷筛选变更（立即生效）
  onQuickFilterChange(e) {
    const field = e.currentTarget.dataset.field;
    const index = parseInt(e.detail.value);

    let value = '';
    if (index === 1) {
      value = 'yes';
    } else if (index === 2) {
      value = 'no';
    }

    this.setData({
      [`filterForm.${field}`]: value
    });

    // 立即应用筛选
    this.filterHomeworkList();
  },

  // 高级筛选表单输入
  onFilterInput(e) {
    const field = e.currentTarget.dataset.field;
    let value = e.detail.value;

    // 处理picker选择器的值
    if (field === 'isExpired' || field === 'isSubmitted' || field === 'isGraded') {
      const index = parseInt(value);
      if (index === 1) {
        value = 'yes';
      } else if (index === 2) {
        value = 'no';
      } else {
        value = '';
      }
    } else if (field === 'courseName' || field === 'className' || field === 'title') {
      // 处理课程、课堂、作业名称选择器
      const index = parseInt(value);
      const options = this.data[field + 'Options'];
      if (index === 0) {
        value = '';
      } else {
        value = options[index - 1] || '';
      }
    }

    this.setData({
      [`filterForm.${field}`]: value
    });
  },

  // 应用高级筛选
  applyFilter() {
    this.setData({ showFilter: false });
    this.filterHomeworkList();
  },

  // 重置筛选
  resetFilter() {
    this.setData({
      filterForm: {
        courseName: '',
        className: '',
        title: '',
        publishStartDate: '',
        publishEndDate: '',
        deadlineStartDate: '',
        deadlineEndDate: '',
        isExpired: '',
        isSubmitted: '',
        isGraded: ''
      },
      showFilter: false
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

