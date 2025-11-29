const api = require('../../utils/api.js')

Page({
  data: {
    activeTab: 'joined', // 当前激活的标签：joined, available, applications
    
    // 我的课堂
    joinedList: [],
    joinedLoading: false,
    joinedQuery: { page: 1, limit: 10, className: '' },
    joinedTotal: 0,
    
    // 发现课堂
    availableList: [],
    availableLoading: false,
    availableQuery: { page: 1, limit: 10, className: '', teacher: '' },
    availableTotal: 0,
    
    // 我的申请
    applicationsList: [],
    applicationsLoading: false,
    applicationsQuery: { page: 1, limit: 10 },
    applicationsTotal: 0
  },

  onLoad() {
    this.loadJoinedClasses()
  },

  // 标签切换
  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    
    if (tab === 'available' && this.data.availableList.length === 0) {
      this.loadAvailableClasses()
    } else if (tab === 'applications' && this.data.applicationsList.length === 0) {
      this.loadMyApplications()
    }
  },

  // === 我的课堂相关 ===
  async loadJoinedClasses() {
    this.setData({ joinedLoading: true })
    try {
      const res = await api.getJoinedClasses(this.data.joinedQuery)
      this.setData({
        joinedList: res.rows || [],
        joinedTotal: res.total || 0
      })
    } catch (error) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ joinedLoading: false })
    }
  },

  onJoinedSearch() {
    this.setData({ 'joinedQuery.page': 1 })
    this.loadJoinedClasses()
  },

  onJoinedInput(e) {
    this.setData({ 'joinedQuery.className': e.detail.value })
  },

  // === 发现课堂相关 ===
  async loadAvailableClasses() {
    this.setData({ availableLoading: true })
    try {
      const res = await api.getAvailableClasses(this.data.availableQuery)
      const list = res.rows || []
      // 为每个课堂添加申请状态
      list.forEach(item => {
        item.applying = false
        item.applied = false
      })
      this.setData({
        availableList: list,
        availableTotal: res.total || 0
      })
    } catch (error) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ availableLoading: false })
    }
  },

  onAvailableSearch() {
    this.setData({ 'availableQuery.page': 1 })
    this.loadAvailableClasses()
  },

  onAvailableClassNameInput(e) {
    this.setData({ 'availableQuery.className': e.detail.value })
  },

  onAvailableTeacherInput(e) {
    this.setData({ 'availableQuery.teacher': e.detail.value })
  },

  // 申请加入课堂
  async onApplyJoin(e) {
    const { sessionid, index } = e.currentTarget.dataset
    const key = `availableList[${index}].applying`
    this.setData({ [key]: true })
    
    try {
      await api.applyJoinClass(sessionid)
      wx.showToast({ title: '申请成功', icon: 'success' })
      this.setData({ 
        [`availableList[${index}].applied`]: true,
        [key]: false 
      })
      // 刷新申请列表
      this.loadMyApplications()
    } catch (error) {
      wx.showToast({ title: error.message || '申请失败', icon: 'none' })
      this.setData({ [key]: false })
    }
  },

  // === 我的申请相关 ===
  async loadMyApplications() {
    this.setData({ applicationsLoading: true })
    try {
      const res = await api.getMyApplications(this.data.applicationsQuery)
      this.setData({
        applicationsList: res.rows || [],
        applicationsTotal: res.total || 0
      })
    } catch (error) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ applicationsLoading: false })
    }
  },

  // 取消申请
  async onCancelApplication(e) {
    const { applicationid } = e.currentTarget.dataset
    try {
      await wx.showModal({
        title: '确认取消',
        content: '确定要取消申请吗？',
        confirmColor: '#ff4757'
      })
      await api.cancelApplication(applicationid)
      wx.showToast({ title: '取消成功', icon: 'success' })
      this.loadMyApplications()
    } catch (error) {
      if (error.errMsg !== 'showModal:fail cancel') {
        wx.showToast({ title: error.message || '取消失败', icon: 'none' })
      }
    }
  },

  // === 通用方法 ===
  // 退出课堂
  async onQuitClass(e) {
    const { sessionid, classname } = e.currentTarget.dataset
    try {
      await wx.showModal({
        title: '确认退出',
        content: `确定要退出「${classname}」课堂吗？`,
        confirmColor: '#ff4757'
      })
      await api.quitClass(sessionid)
      wx.showToast({ title: '退出成功', icon: 'success' })
      this.loadJoinedClasses()
    } catch (error) {
      if (error.errMsg !== 'showModal:fail cancel') {
        wx.showToast({ title: error.message || '退出失败', icon: 'none' })
      }
    }
  },

  // 进入课堂详情
  onEnterClass(e) {
    const sessionId = e.currentTarget.dataset.sessionid
    wx.navigateTo({
      url: `/pages/course/detail?sessionId=${sessionId}`
    })
  },

  // 格式化时间
  formatSessionTime(session) {
    const weekDayMap = {
      '1': '周一', '2': '周二', '3': '周三', '4': '周四',
      '5': '周五', '6': '周六', '7': '周日'
    }
    let weekDay = weekDayMap[session.weekDay] || ''
    let startTime = session.startTime || ''
    let endTime = session.endTime || ''
    
    if (startTime && startTime.includes(' ')) {
      startTime = startTime.split(' ')[1].substring(0, 5)
    }
    if (endTime && endTime.includes(' ')) {
      endTime = endTime.split(' ')[1].substring(0, 5)
    }
    
    return weekDay && startTime ? `${weekDay} ${startTime}-${endTime}` : '时间待定'
  },

  // 申请状态文本
  getApplicationStatusText(status) {
    const statusMap = { '0': '待审核', '1': '已通过', '2': '已拒绝' }
    return statusMap[status] || '未知状态'
  },

  // 申请状态颜色
  getApplicationStatusColor(status) {
    const colorMap = { '0': 'warning', '1': 'success', '2': 'error' }
    return colorMap[status] || 'default'
  }
})