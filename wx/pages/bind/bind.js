// pages/bind/bind.js
const api = require('../../utils/api.js')
const app = getApp()

Page({
  data: {
    openId: '',
    username: '',
    password: '',
    nickname: '',
    avatarUrl: '', // 显示用
    avatarServerUrl: '' // 提交给后台用
  },

  onLoad(options) {
    if (options.openId) {
      this.setData({
        openId: options.openId
      })
    } else {
      wx.showToast({
        title: '缺少OpenID',
        icon: 'none'
      })
    }
  },

  onChooseAvatar(e) {
    const { avatarUrl } = e.detail 
    this.setData({
      avatarUrl
    })
    // 上传头像
    this.uploadAvatar(avatarUrl)
  },

  onNicknameChange(e) {
    this.setData({
      nickname: e.detail.value
    })
  },

  uploadAvatar(filePath) {
    const baseUrl = app.globalData.baseUrl || 'http://localhost:8080'
    wx.uploadFile({
      url: baseUrl + '/common/upload',
      filePath: filePath,
      name: 'file',
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          this.setData({
            avatarServerUrl: data.url 
          })
        } else {
          wx.showToast({
            title: '头像上传失败',
            icon: 'none'
          })
        }
      },
      fail: (err) => {
        console.error(err)
        wx.showToast({
          title: '上传出错',
          icon: 'none'
        })
      }
    })
  },

  handleBind() {
    if (!this.data.username || !this.data.password) {
      wx.showToast({
        title: '请输入学号和密码',
        icon: 'none'
      })
      return
    }
    if (!this.data.openId) {
      wx.showToast({
        title: 'OpenID缺失',
        icon: 'none'
      })
      return
    }

    // 构造请求参数
    const params = {
      username: this.data.username,
      password: this.data.password,
      openId: this.data.openId,
      nickname: this.data.nickname,
      avatar: this.data.avatarServerUrl || this.data.avatarUrl 
    }

    // 使用 api.request 发送请求
    api.request({
        url: '/app/login/bind',
        method: 'POST',
        data: params
    }).then(res => {
        // api.request 已经处理了 code != 200 的情况，但这里我们期望返回 token
        // 如果 api.request 封装返回的是 data 部分
        if (res.token) {
            wx.setStorageSync('token', res.token)
            wx.showToast({
                title: '绑定成功',
                icon: 'success'
            })
            setTimeout(() => {
                wx.switchTab({
                    url: '/pages/course/list'
                })
            }, 1500)
        } else {
             // 可能是业务错误，但 api.request 可能已经 reject 了
             wx.showToast({ title: '绑定失败', icon: 'none' })
        }
    }).catch(err => {
        wx.showToast({
            title: err.msg || '绑定失败',
            icon: 'none'
        })
    })
  }
})
