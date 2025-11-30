const api = require('../../../utils/api.js');

Page({
  data: {
    form: {
      todoId: null,
      title: '',
      content: '',
      priority: '0',
      todoType: 'other', 
      status: '0', // 【新增】默认为未完成
      endTime: ''
    },
    date: '',
    time: '',
    typeOptions: [
      { label: '学习', value: 'study' },
      { label: '工作', value: 'work' },
      { label: '生活', value: 'life' },
      { label: '其他', value: 'other' }
    ],
    typeIndex: 3 
  },

  onLoad(opts) {
    if (opts.id) {
      wx.setNavigationBarTitle({ title: '编辑待办' });
      api.getTodoDetail(opts.id).then(res => {
        const data = res.data;
        // 解析时间
        let date = '', time = '';
        if (data.endTime) {
          const parts = data.endTime.split(' ');
          date = parts[0];
          time = parts[1] ? parts[1].substring(0, 5) : ''; 
        }
        
        // 解析类型索引
        const typeIndex = this.data.typeOptions.findIndex(opt => opt.value === data.todoType);

        // data.status 会自动覆盖 form.status
        this.setData({ 
          form: data, 
          date: date, 
          time: time,
          typeIndex: typeIndex >= 0 ? typeIndex : 3
        });
      });
    } else {
      wx.setNavigationBarTitle({ title: '新增待办' });
      const now = new Date();
      const year = now.getFullYear();
      const month = (now.getMonth() + 1).toString().padStart(2, '0');
      const day = now.getDate().toString().padStart(2, '0');
      this.setData({ date: `${year}-${month}-${day}` });
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: e.detail.value });
  },

  onPriorityChange(e) { 
    this.setData({ 'form.priority': e.detail.value }); 
  },

  // 【新增】状态改变监听
  onStatusChange(e) {
    this.setData({ 'form.status': e.detail.value });
  },

  onTypeChange(e) {
    const index = e.detail.value;
    this.setData({
      typeIndex: index,
      'form.todoType': this.data.typeOptions[index].value
    });
  },

  onDateChange(e) { this.setData({ date: e.detail.value }); },
  onTimeChange(e) { this.setData({ time: e.detail.value }); },

  submit() {
    if (!this.data.form.title) return wx.showToast({ title: '请输入标题', icon: 'none' });
    if (!this.data.date) return wx.showToast({ title: '请选择截止日期', icon: 'none' });
    
    const formData = { ...this.data.form };
    
    let timeStr = this.data.time;
    if (!timeStr) timeStr = '23:59:59';
    else if (timeStr.length === 5) timeStr = timeStr + ':00';
    
    formData.endTime = `${this.data.date} ${timeStr}`;
    
    const req = formData.todoId ? api.updateTodo : api.addTodo;
    
    req(formData).then(res => {
      if (res.code === 200) {
        wx.showToast({ title: '保存成功' });
        setTimeout(() => wx.navigateBack(), 800);
      } else {
        wx.showModal({ title: '保存失败', content: res.msg, showCancel: false });
      }
    });
  }
});