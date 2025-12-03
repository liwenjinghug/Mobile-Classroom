const api = require('../../../utils/api.js');

Page({
  data: {
    currentTab: 'msg',
    unreadCount: 0,
    messageList: [],
    todoList: [],
    noticeList: [],
    isRefreshing: false,
    tmplIds: [] 
  },

  onShow() {
    this.refreshData();
  },

  switchTab(e) {
    const key = e.currentTarget.dataset.key;
    this.setData({ currentTab: key });
    this.refreshData();
  },

  onRefresh() {
    this.setData({ isRefreshing: true });
    this.refreshData().then(() => {
      this.setData({ isRefreshing: false });
    });
  },

  refreshData() {
    if (this.data.currentTab === 'msg') {
      return Promise.all([this.loadMessages(), this.updateUnreadCount()]);
    } else if (this.data.currentTab === 'todo') {
      return this.loadTodos();
    } else if (this.data.currentTab === 'notice') {
      return this.loadNotices();
    }
  },

  // --- 消息模块 ---
  loadMessages() {
    return api.getMessageList().then(res => {
      // 【兼容修复】优先取 res.data，如果没有则取 res 本身
      const list = res.data || res || [];
      this.setData({ messageList: list });
    });
  },
  updateUnreadCount() {
    return api.getUnreadCount().then(res => {
      // 【兼容修复】如果是数字直接用，否则取 res.data
      const count = (typeof res === 'number') ? res : (res.data || 0);
      this.setData({ unreadCount: count });
    });
  },
  handleMarkAllRead() {
    api.markAllRead().then(() => {
      wx.showToast({ title: '已全部已读' });
      this.refreshData();
    });
  },
  handleMessageClick(e) {
    const item = e.currentTarget.dataset.item;
    if (item.isRead === '0') {
      let type = item.type; 
      let id = type === 'todo' ? item.todoId : (type === 'homework' ? item.homeworkId : item.examId);
      api.markRead(type, id).then(() => {
        const list = this.data.messageList.map(msg => {
          if (msg.messageId === item.messageId) {
            msg.isRead = '1';
          }
          return msg;
        });
        this.setData({ messageList: list });
        this.updateUnreadCount();
      });
    }
    item.isRead = '1';
    const itemStr = encodeURIComponent(JSON.stringify(item));
    wx.navigateTo({
      url: `/pages/proj_cyq/message/detail?item=${itemStr}`
    });
  },
  requestSubscribe() {
    if (this.data.tmplIds.length === 0) {
      return wx.showToast({ title: '请先配置模板ID', icon: 'none' });
    }
    wx.requestSubscribeMessage({
      tmplIds: this.data.tmplIds,
      success(res) { wx.showToast({ title: '设置成功' }); },
      fail(err) { console.error(err); }
    })
  },

  // --- 待办模块 ---
  loadTodos() {
    return api.getTodoList({ pageNum: 1, pageSize: 50 }).then(res => {
      // 【兼容修复】兼容 rows (若依标准分页) 和 data/直接数组
      const list = res.rows || res.data || res || [];
      this.setData({ todoList: list });
    });
  },
  navigateToAddTodo() {
    wx.navigateTo({ url: '/pages/proj_cyq/todo/edit' });
  },
  navigateToEditTodo(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/proj_cyq/todo/edit?id=${id}` });
  },
  toggleTodoStatus(e) {
    const id = e.currentTarget.dataset.id;
    const currentStatus = String(e.currentTarget.dataset.status);
    const newStatus = currentStatus === '1' ? '0' : '1';
    api.updateTodo({ todoId: id, status: newStatus }).then(() => {
      this.loadTodos();
    });
  },
  deleteTodoItem(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '删除',
      content: '确定删除该待办吗？',
      success: (res) => {
        if (res.confirm) {
          api.deleteTodo(id).then(() => {
            wx.showToast({ title: '已删除' });
            this.loadTodos();
          });
        }
      }
    })
  },

  // --- 通告模块 ---
  loadNotices() {
    return api.getNoticeList({ pageNum: 1, pageSize: 20 }).then(res => {
      // 【兼容修复】
      const list = res.rows || res.data || res || [];
      this.setData({ noticeList: list });
    });
  },
  navigateToNoticeDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/proj_cyq/notice/detail?id=${id}` });
  }
});