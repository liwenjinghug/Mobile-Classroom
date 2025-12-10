const { request } = require('../../../../utils/api.js')

Page({
  data: {
    historyList: [],
    sessionId: null,
    emptyText: '暂无签到记录'
  },

  onLoad: function (options) {
    if (options && options.sessionId) {
      this.setData({ sessionId: options.sessionId });
      this.getHistory();
    } else {
      this.setData({ emptyText: '当前课堂暂无签到记录' });
    }
  },

  onPullDownRefresh: function () {
    if (!this.data.sessionId) {
      wx.stopPullDownRefresh();
      return;
    }
    this.getHistory(() => {
      wx.stopPullDownRefresh();
    });
  },

  getHistory: function (cb) {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.userId) {
      wx.showToast({ title: '未登录', icon: 'none' });
      if (cb) cb();
      return;
    }

    if (!this.data.sessionId) {
      if (cb) cb();
      return;
    }

    const params = { sessionId: this.data.sessionId };

    request({ 
      url: '/proj_myx/attendance/student/history', 
      method: 'GET',
      data: params
    }).then(res => {
      // 适配后端返回的数据结构
      const list = (res || []).map(item => {
        // 第二排内容逻辑
        let row2 = '';
        if (item.title) {
          row2 = item.title;
        } else {
          const typeStr = (item.type === 'location' ? '位置签到' : '二维码签到');
          row2 = typeStr;
        }

        // 第三排内容逻辑：发布时间 (使用 start_time)
        const startTimeStr = item.startTime || item.createdAt || '';
        const row3 = '发布时间：' + startTimeStr;

        // 第四排内容逻辑 (仅已签到或迟到显示)
        let row4 = '';
        if (item.attendanceStatus === 1 || item.attendanceStatus === 2) {
          row4 = '签到时间：' + (item.attendanceTime || '');
        }

        return {
          id: item.attendanceId || ('task_' + item.taskId), // 如果未签到可能没有 attendanceId
          courseName: item.courseName || '未知课程',
          row2: row2,
          row3: row3,
          row4: row4,
          status: item.attendanceStatus !== undefined ? item.attendanceStatus : 0 // 默认为0(缺勤)
        };
      });

      this.setData({
        historyList: list
      });
      if (cb) cb();
    }).catch(err => {
      console.error(err);
      wx.showToast({ title: '获取记录失败', icon: 'none' });
      if (cb) cb();
    });
  }
})