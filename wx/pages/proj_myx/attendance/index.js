const { request } = require('../../../utils/api.js')

Page({
  data: {
    activeTasks: [], // 活跃任务列表
    currentTask: null, // 当前选中的任务
    currentType: '', // 'location' or 'qr' (derived from currentTask)
    
    isSigning: false,
    signSuccess: false,
    code: '',
    message: '',
    locationText: '正在获取位置...',
    latitude: null,
    longitude: null,
    sessionId: null
  },

  onLoad: function (options) {
    if (options.sessionId) {
      this.setData({ sessionId: options.sessionId });
      this.loadActiveTasks(options.sessionId);
    } else {
      // 如果没有 sessionId，可能需要提示错误或返回
      wx.showToast({ title: '参数错误', icon: 'none' });
    }
  },

  // 获取当前活跃的签到任务
  loadActiveTasks: function(sessionId) {
    const that = this;
    wx.showLoading({ title: '加载任务中...' });

    request({ 
      url: '/proj_myx/attendance/active', 
      method: 'GET',
      data: { sessionId: sessionId } 
    }).then(res => {
      wx.hideLoading();
      const tasks = res || [];
      that.setData({ activeTasks: tasks });

      if (tasks.length === 0) {
        that.setData({ message: '当前暂无正在进行的签到' });
      } else if (tasks.length === 1) {
        // 只有一个任务，自动选中
        that.selectTask(tasks[0]);
      }
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: '获取任务失败', icon: 'none' });
      console.error(err);
    });
  },

  // 用户选择任务
  selectTask: function(task) {
    // 如果是从列表点击进来的，或者自动选中的
    if (task.currentTarget) {
      task = task.currentTarget.dataset.task;
    }

    this.setData({
      currentTask: task,
      currentType: task.type,
      message: '',
      signSuccess: false,
      code: ''
    });

    if (task.type === 'location') {
      this.getLocation();
    }
  },

  // 返回任务列表（仅当有多个任务时显示）
  backToTaskList: function() {
    this.setData({
      currentTask: null,
      currentType: '',
      message: ''
    });
  },

  goToHistory: function() {
    const sessionId = this.data.sessionId;
    wx.navigateTo({
      url: './history/index' + (sessionId ? '?sessionId=' + sessionId : '')
    });
  },

  getLocation: function () {
    const that = this;
    this.setData({ locationText: '正在定位...' });
    wx.getLocation({
      type: 'wgs84',
      success(res) {
        that.setData({
          latitude: res.latitude,
          longitude: res.longitude,
          locationText: '位置已更新'
        });
      },
      fail() {
        that.setData({
          locationText: '定位失败，请检查权限'
        });
      }
    });
  },

  // 位置签到
  handleLocationSignIn: function () {
    if (this.data.isSigning || this.data.signSuccess) return;
    
    if (this.data.currentTask && this.data.currentTask.attendanceStatus === 1) {
      wx.showToast({ title: '已签到，请勿重复点击', icon: 'none' });
      return;
    }

    if (!this.data.latitude || !this.data.longitude) {
      this.getLocation();
      wx.showToast({ title: '请先获取位置', icon: 'none' });
      return;
    }

    this.submitSignIn({
      type: 'location',
      latitude: this.data.latitude,
      longitude: this.data.longitude
    });
  },

  // 扫码签到
  handleScanCode: function () {
    if (this.data.currentTask && this.data.currentTask.attendanceStatus === 1) {
      wx.showToast({ title: '已签到，请勿重复点击', icon: 'none' });
      return;
    }

    const that = this;
    wx.scanCode({
      success(res) {
        that.setData({ code: res.result });
        that.handleCodeSignIn();
      },
      fail() {
        wx.showToast({ title: '扫码失败', icon: 'none' });
      }
    });
  },

  handleInputCode: function (e) {
    this.setData({
      code: e.detail.value
    });
  },

  // 码签到提交
  handleCodeSignIn: function () {
    if (this.data.isSigning || this.data.signSuccess) return;

    if (this.data.currentTask && this.data.currentTask.attendanceStatus === 1) {
      wx.showToast({ title: '已签到，请勿重复点击', icon: 'none' });
      return;
    }

    const code = this.data.code;
    if (!code) {
      wx.showToast({
        title: '请输入签到码',
        icon: 'none'
      });
      return;
    }

    this.submitSignIn({
      type: 'qr',
      code: code
    });
  },

  // 统一提交逻辑
  submitSignIn: function (data) {
    if (!this.data.currentTask) {
      wx.showToast({ title: '未选择签到任务', icon: 'none' });
      return;
    }

    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.userId) {
      wx.showToast({ title: '未登录或用户信息缺失', icon: 'none' });
      return;
    }

    this.setData({ isSigning: true, message: '正在签到...' });

    // 构造请求参数
    const payload = {
      taskId: this.data.currentTask.taskId || this.data.currentTask.id, // 兼容后端字段
      studentId: userInfo.userId,
      ...data
    };
    // 移除 type 字段，因为后端通过 URL 区分
    delete payload.type;

    const url = this.data.currentType === 'location' 
      ? '/proj_myx/attendance/sign/location' 
      : '/proj_myx/attendance/sign/qr';

    // 针对 location 接口参数名调整 (lat/lng)
    if (this.data.currentType === 'location') {
      payload.lat = data.latitude;
      payload.lng = data.longitude;
      delete payload.latitude;
      delete payload.longitude;
    }
    // 针对 qr 接口参数名调整 (token)
    if (this.data.currentType === 'qr') {
      payload.token = data.code;
      delete payload.code;
    }

    request({ 
      url: url, 
      method: 'POST', 
      data: payload 
    }).then(res => {
      this.setData({
        isSigning: false,
        signSuccess: true,
        message: '签到成功！'
      });
      wx.vibrateShort();
      
      // 延迟返回
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }).catch(err => {
      this.setData({
        isSigning: false,
        signSuccess: false,
        message: err.msg || '签到失败，请重试'
      });
    });
  }
})