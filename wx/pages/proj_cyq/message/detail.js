Page({
  data: {
    msg: {}
  },
  onLoad(options) {
    // 接收传递过来的消息对象字符串并解析
    if (options.item) {
      try {
        const item = JSON.parse(decodeURIComponent(options.item));
        // 格式化类型名称
        item.typeName = item.type === 'todo' ? '待办' : (item.type === 'homework' ? '作业' : '考试');
        this.setData({ msg: item });
      } catch (e) {
        console.error('解析消息详情失败', e);
      }
    }
  },
  handleAction() {
    // 如果是考试，可以跳转到考试入口
    if (this.data.msg.type === 'exam') {
      // 假设考试入口路径（需根据实际情况调整）
      // wx.navigateTo({ url: '/pages/proj_lwj_exam/exam_portal/index' });
      wx.showToast({ title: '请前往考试模块', icon: 'none' });
    }
  }
});