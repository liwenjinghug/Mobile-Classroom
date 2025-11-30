const api = require('../../utils/api.js')

Page({
  data: {
    activeTab: 'joined',
    
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

  // 我的课堂加载
  async loadJoinedClasses() {
    this.setData({ joinedLoading: true })
    try {
      const res = await api.getJoinedClasses(this.data.joinedQuery)
      const listData = res.rows || res.data || []
      
      const formattedList = listData.map(item => {
        return {
          ...item,
          formattedTime: this.formatSessionTime(item),
          formattedAssignedAt: this.formatDateTime(item.assignedAt)
        }
      })
      
      this.setData({
        joinedList: formattedList,
        joinedTotal: res.total || 0
      })
    } catch (error) {
      console.error('加载课堂失败:', error)
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

  // 发现课堂加载
  async loadAvailableClasses() {
    this.setData({ availableLoading: true })
    try {
      const res = await api.getAvailableClasses(this.data.availableQuery)
      const list = res.rows || []
      
      const formattedList = list.map(item => {
        return {
          ...item,
          formattedTime: this.formatSessionTime(item),
          applying: false,
          applied: false
        }
      })
      
      this.setData({
        availableList: formattedList,
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
      this.loadMyApplications()
    } catch (error) {
      wx.showToast({ title: error.message || '申请失败', icon: 'none' })
      this.setData({ [key]: false })
    }
  },

  // 我的申请加载
  async loadMyApplications() {
    this.setData({ applicationsLoading: true })
    try {
      const res = await api.getMyApplications(this.data.applicationsQuery)
      const listData = res.rows || res.data || []
      
      const formattedList = listData.map(item => {
        return {
          ...item,
          statusText: this.getApplicationStatusText(item.status),
          formattedApplyTime: this.formatDateTime(item.applyTime),
          formattedAuditTime: this.formatDateTime(item.auditTime)
        }
      })
      
      this.setData({
        applicationsList: formattedList,
        applicationsTotal: res.total || 0
      })
    } catch (error) {
      console.error('加载申请失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ applicationsLoading: false })
    }
  },

// 取消申请
async onCancelApplication(e) {
  const { applicationid } = e.currentTarget.dataset
  
  wx.showModal({
    title: '确认取消',
    content: '确定要取消申请吗？',
    confirmColor: '#ff4757',
    success: async (res) => {
      if (res.confirm) {
        try {
          await api.cancelApplication(applicationid)
          wx.showToast({ title: '取消成功', icon: 'success' })
          this.loadMyApplications()
        } catch (error) {
          wx.showToast({ title: error.message || '取消失败', icon: 'none' })
        }
      }
    }
  })
},

 // 退出课堂
async onQuitClass(e) {
  const { sessionid, classname } = e.currentTarget.dataset
  
  wx.showModal({
    title: '确认退出',
    content: `确定要退出「${classname}」课堂吗？`,
    confirmColor: '#ff4757',
    success: async (res) => {
      if (res.confirm) {
        try {
          await api.quitClass(sessionid)
          wx.showToast({ title: '退出成功', icon: 'success' })
          this.loadJoinedClasses()
        } catch (error) {
          wx.showToast({ title: error.message || '退出失败', icon: 'none' })
        }
      }
    }
  })
},

  // 进入课堂详情
  onEnterClass(e) {
    const sessionId = e.currentTarget.dataset.sessionid
    console.log('点击进入课堂，sessionId:', sessionId)
    
    if (!sessionId) {
      wx.showToast({
        title: '课堂ID缺失',
        icon: 'none'
      })
      return
    }
    
    wx.navigateTo({
      url: `/pages/course/detail?sessionId=${sessionId}`
    })
  },

  // 格式化上课时间
  formatSessionTime(session) {
    if (!session) return '时间待定'
    
    const weekDayMap = {
      '1': '周一', '2': '周二', '3': '周三', '4': '周四',
      '5': '周五', '6': '周六', '7': '周日'
    }
    
    const weekDay = session.weekDay
    const startTime = session.startTime
    const endTime = session.endTime
    
    if (!weekDay || !startTime || !endTime) {
      return '时间待定'
    }
    
    const weekDayText = weekDayMap[weekDay] || `周${weekDay}`
    return `${weekDayText} ${startTime}-${endTime}`
  },

  // 格式化日期时间
  formatDateTime(isoString) {
    if (!isoString) return '-'
    
    try {
      // 处理ISO格式：2025-11-30T12:33:12.000+08:00
      if (isoString.includes('T')) {
        const datePart = isoString.split('T')[0]
        const timePart = isoString.split('T')[1].split('.')[0] // 去掉毫秒
        return `${datePart} ${timePart}`
      }
      
      // 如果已经是普通格式，直接返回
      return isoString
    } catch (error) {
      console.error('时间格式化错误:', error)
      return isoString
    }
  },

  // 申请状态文本
  getApplicationStatusText(status) {
    if (status === undefined || status === null) {
      return '未知状态'
    }
    
    const statusStr = String(status)
    const statusMap = { 
      '0': '待审核', 
      '1': '已通过', 
      '2': '已拒绝'
    }
    
    return statusMap[statusStr] || '未知状态'
  }
})