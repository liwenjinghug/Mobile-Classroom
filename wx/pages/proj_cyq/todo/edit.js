const api = require('../../../utils/api.js');

Page({
  data: {
    form: {
      todoId: null,
      title: '',
      content: '',
      priority: '0',
      todoType: 'other',
      status: '0',
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
        // 【兼容修复】
        const data = res.data || res;
        
        let date = '', time = '';
        if (data.endTime) {
          const parts = data.endTime.split(' ');
          date = parts[0];
          time = parts[1] ? parts[1].substring(0, 5) : ''; 
        }
        
        const typeIndex = this.data.typeOptions.findIndex(opt => opt.value === data.todoType);

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

  // ... (以下方法保持不变：onInput, onPriorityChange, onStatusChange, onTypeChange, onDateChange, onTimeChange) ...
  onInput(e) { this.setData({ [`form.${e.currentTarget.dataset.field}`]: e.detail.value }); },
  onPriorityChange(e) { this.setData({ 'form.priority': e.detail.value }); },
  onStatusChange(e) { this.setData({ 'form.status': e.detail.value }); },
  onTypeChange(e) {
    const index = e.detail.value;
    this.setData({ typeIndex: index, 'form.todoType': this.data.typeOptions[index].value });
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
      // 【兼容修复】如果 res 已经是数据本身，通常意味着成功；或者检查 code
      const code = res.code !== undefined ? res.code : 200; // 默认成功
      if (code === 200) {
        wx.showToast({ title: '保存成功' });
        setTimeout(() => wx.navigateBack(), 800);
      } else {
        wx.showModal({ title: '保存失败', content: res.msg || '未知错误', showCancel: false });
      }
    });
  }
});