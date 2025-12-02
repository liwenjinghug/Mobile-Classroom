// 作业列表页面 - 使用proj_fz接口
const api = require('../../../utils/api');

Page({
  data: {
    sessionId: null,
    sessionName: '',
    tabIndex: 0,
    tabs: ['未完成', '已完成'],
    homeworkList: [],
    allHomeworkList: [], // 保存所有作业数据
    mySubmissions: [], // 保存我的提交记录
    loading: false,
    isEmpty: false,

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
      isExpired: '', // all/yes/no
      isGraded: '', // all/yes/no
      isCompleted: '' // all/yes/no
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

  // 切换标签
  onTabChange(e) {
    const index = e.detail.index || e.currentTarget.dataset.index;
    this.setData({ tabIndex: index });
    this.filterHomeworkList();
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

      // 提取课程和作业名称选项
      const courseSet = new Set();
      const classSet = new Set();
      const titleSet = new Set();

      allList.forEach(item => {
        if (item.courseName) courseSet.add(item.courseName);
        if (item.className) classSet.add(item.className);
        if (item.title) titleSet.add(item.title);
      });

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

  // 根据当前标签筛选作业
  filterHomeworkList() {
    const { allHomeworkList, mySubmissions, tabIndex, filterForm } = this.data;
    const now = new Date();

    let filteredList = allHomeworkList.map(homework => {
      // 查找该作业的提交记录
      const submission = mySubmissions.find(s => s.homeworkId === homework.homeworkId);

      // 判断是否过期
      const deadline = homework.deadline ? new Date(homework.deadline) : null;
      const isExpired = deadline ? now > deadline : false;

      // 判断完成状态：已提交即为已完成
      let isCompleted = false;
      let statusText = '未提交';

      if (submission) {
        if (submission.isGraded === 1) {
          isCompleted = true;
          statusText = '已批改';
        } else if (submission.status >= 1) {
          isCompleted = true;
          statusText = '已提交';
        }
      }

      return {
        ...homework,
        submission,
        isExpired,
        isCompleted,
        statusText,
        isGraded: submission?.isGraded === 1
      };
    });

    // 应用高级筛选
    filteredList = this.applyAdvancedFilter(filteredList);

    // 根据标签筛选
    if (tabIndex === 0) {
      // 进行中：未完成的
      filteredList = filteredList.filter(item => !item.isCompleted);
    } else if (tabIndex === 1) {
      // 已完成：已提交的（包括已批改和未批改）
      filteredList = filteredList.filter(item => item.isCompleted);
    }

    console.log('筛选后的作业列表', filteredList, '数量:', filteredList.length);

    this.setData({
      homeworkList: filteredList,
      isEmpty: filteredList.length === 0
    });
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

      // 过期状态筛选
      if (filterForm.isExpired === 'yes' && !item.isExpired) return false;
      if (filterForm.isExpired === 'no' && item.isExpired) return false;

      // 批改状态筛选
      if (filterForm.isGraded === 'yes' && !item.isGraded) return false;
      if (filterForm.isGraded === 'no' && item.isGraded) return false;

      // 完成状态筛选（已提交即为已完成）
      if (filterForm.isCompleted === 'yes' && !item.isCompleted) return false;
      if (filterForm.isCompleted === 'no' && item.isCompleted) return false;

      return true;
    });
  },

  // 显示/隐藏筛选面板
  toggleFilter() {
    this.setData({ showFilter: !this.data.showFilter });
  },

  // 筛选表单输入
  onFilterInput(e) {
    const field = e.currentTarget.dataset.field;
    let value = e.detail.value;

    // 处理picker选择器的值
    if (field === 'isExpired' || field === 'isGraded' || field === 'isCompleted') {
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

  // 应用筛选
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
        isGraded: '',
        isCompleted: ''
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

