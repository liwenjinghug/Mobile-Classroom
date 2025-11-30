// 简易 API 封装，基于 wx.request
const STORAGE_TOKEN = 'token';
const DEFAULT_BASE = 'http://localhost:8080';

function request(options) {
  const baseUrl = getApp().globalData.baseUrl || DEFAULT_BASE;
  const url = baseUrl + options.url;
  const token = wx.getStorageSync(STORAGE_TOKEN);
  const headers = Object.assign({ 'Content-Type': 'application/json' }, token ? { 'Authorization': 'Bearer ' + token } : {});

  console.log('[api.request] =>', { url, method: options.method || 'GET', data: options.data || {}, headers });

  return new Promise((resolve, reject) => {
    wx.request({
      url,
      method: options.method || 'GET',
      data: options.data || {},
      header: headers,
      success(res) {
        console.log('[api.request] success =>', res);
        try {
          // 如果是登录请求，保存原始响应到 localStorage，便于调试
          if (options && options.url === '/login') {
            try { wx.setStorageSync('lastLoginResponse', res.data); console.log('[api.request] saved lastLoginResponse to storage'); } catch (e) { console.warn('save lastLoginResponse failed', e); }
          }
        } catch (e) {}
        if (res.statusCode === 401) {
          // 登录过期或未认证
          wx.removeStorageSync(STORAGE_TOKEN);
          wx.showToast({ title: '需要登录', icon: 'none' });
          wx.reLaunch({ url: '/pages/login/login' });
          return reject({ message: '未认证', statusCode: 401 });
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else {
          console.warn('[api.request] non-2xx =>', res.statusCode, res.data);
          reject(res.data || { message: '请求失败', statusCode: res.statusCode });
        }
      },
      fail(err) {
        console.error('[api.request] fail =>', err);
        // 提示开发者网络调用失败
        try { wx.showToast({ title: '网络请求失败，请检查后端或域名', icon: 'none' }); } catch (e) {}
        reject(err || { message: '请求失败' });
      }
    });
  });
}

function findToken(obj) {
  if (!obj || typeof obj !== 'object') return null;
  if (Object.prototype.hasOwnProperty.call(obj, 'token') && obj.token) return obj.token;
  for (const k in obj) {
    if (!Object.prototype.hasOwnProperty.call(obj, k)) continue;
    try {
      const v = obj[k];
      if (v && typeof v === 'object') {
        const t = findToken(v);
        if (t) return t;
      }
    } catch (e) {}
  }
  return null;
}

// 新增：获取验证码图片（captchaImage）
function getCaptcha() {
  return request({ url: '/captchaImage', method: 'GET' });
}

module.exports = {
  // 登录示例：后端需实现对应接口
  login(payload) {
    // 兼容前端传入 { studentNo, password } 或 { username, password }
    const username = payload.username || payload.studentNo || payload.userName || payload.user;
    const password = payload.password || '';
    const code = payload.code || '';
    const uuid = payload.uuid || '';
    // 后端期望 /login，返回 AjaxResult（顶层包含 token 字段）
    return request({ url: '/login', method: 'POST', data: { username, password, code, uuid } }).then(data => {
      // 递归查找 token，兼容各种嵌套
      const token = findToken(data);
      if (token) return { token, raw: data };
      return Promise.reject({ message: '登录接口未返回 token，请检查后端', code: 'NO_TOKEN', raw: data });
    });
  },
  getCaptcha,
  getMyCourses() {
    // 返回 { listening: [], teaching: [] }
    return request({ url: '/api/courses/my', method: 'GET' });
  },
  getCourseDetail(courseId) {
    return request({ url: '/api/courses/' + courseId, method: 'GET' });
  },

  //course 
// 在 module.exports 中添加以下接口：

// 获取已加入课堂
getJoinedClasses(query = {}) {
  return request({
    url: '/proj_lw/student/class/joined',
    method: 'GET',
    data: query
  })
},

// 获取可申请课堂
getAvailableClasses(query = {}) {
  return request({
    url: '/proj_lw/student/class/available',
    method: 'GET',
    data: query
  })
},

// 获取我的申请
getMyApplications(query = {}) {
  return request({
    url: '/proj_lw/student/class/applications',
    method: 'GET',
    data: query
  })
},

// 申请加入课堂
applyJoinClass(sessionId) {
  return request({
    url: `/proj_lw/student/class/apply/${sessionId}`,
    method: 'POST'
  })
},

// 取消申请
cancelApplication(applicationId) {
  return request({
    url: `/proj_lw/student/class/application/cancel/${applicationId}`,
    method: 'POST'
  })
},

// 退出课堂  
quitClass(sessionId) {
  return request({
    url: `/proj_lw/student/class/quit/${sessionId}`,
    method: 'POST'
  })
},

// --- 社区：论坛相关 (新增) ---
getPostList() {
  return request({ url: '/proj_qhy/forum/posts', method: 'get' });
},
likePost(postId) {
  return request({ url: '/proj_qhy/forum/like/' + postId, method: 'post' });
},
getLikesByPostId(postId) {
  return request({ url: `/proj_qhy/forum/likes/${postId}`, method: 'get' });
},
cancelLikePost(postId) {
  return request({ url: '/proj_qhy/forum/like/cancel/' + postId, method: 'post' });
},
getCommentsByPostId(postId) {
  return request({ url: `/proj_qhy/forum/comments/${postId}`, method: 'get' });
},
addComment(data) {
  return request({ url: '/proj_qhy/forum/comment/add', method: 'post', data: data });
},
getUserNotices() {
  return request({ url: '/proj_qhy/forum/notices', method: 'get' });
},

// --- 社区：文章相关 (新增) ---
listArticle(query) {
  return request({ url: '/proj_qhy/article/list', method: 'get', data: query });
},
getArticle(id) {
  return request({ url: '/proj_qhy/article/' + id, method: 'get' });
},
likeArticle(id) {
  return request({ url: '/proj_qhy/article/like/' + id, method: 'post' });
},
viewArticle(id) {
  return request({ url: '/proj_qhy/article/view/' + id, method: 'post' });
},
// ================== 1. 消息中心 (MessageController) ==================
getMessageList() {
  return request({ url: '/proj_cyq/message/list', method: 'GET' });
},
getUnreadCount() {
  return request({ url: '/proj_cyq/message/unreadCount', method: 'GET' });
},
// 标记已读 (type: todo/homework/exam)
markRead(type, id) {
  return request({ url: `/proj_cyq/message/read/${type}/${id}`, method: 'PUT' });
},
// 一键已读
markAllRead() {
  return request({ url: '/proj_cyq/message/read/all', method: 'PUT' });
},

// ================== 2. 待办事项 (TodoController) ==================
getTodoList(query) {
  return request({ url: '/proj_cyq/todo/list', method: 'GET', data: query });
},
getTodoDetail(todoId) {
  return request({ url: `/proj_cyq/todo/${todoId}`, method: 'GET' });
},
addTodo(data) {
  return request({ url: '/proj_cyq/todo', method: 'POST', data: data });
},
updateTodo(data) {
  return request({ url: '/proj_cyq/todo', method: 'PUT', data: data });
},
deleteTodo(todoId) {
  return request({ url: `/proj_cyq/todo/${todoId}`, method: 'DELETE' });
},

// ================== 3. 通告管理 (ClassNoticeController) ==================
getNoticeList(query) {
  return request({ url: '/proj_cyq/notice/list', method: 'GET', data: query });
},
getNoticeDetail(noticeId) {
  return request({ url: `/proj_cyq/notice/${noticeId}`, method: 'GET' });
},
// ================== 4. 用户个人中心 (系统接口) ==================
  // 获取个人信息
  getUserProfile() {
    return request({ url: '/system/user/profile', method: 'GET' });
  },
  // 修改密码
  updateUserPwd(oldPassword, newPassword) {
    return request({
      url: '/system/user/profile/updatePwd',
      method: 'PUT',
      data: { oldPassword, newPassword } // <--- 必须用 data 传递 JSON Body
    });
  },
  // 上传头像
  uploadAvatar(filePath) {
    const token = wx.getStorageSync('token');
    const baseUrl = getApp().globalData.baseUrl || 'http://localhost:8080';
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: baseUrl + '/system/user/profile/avatar',
        filePath: filePath,
        name: 'avatarfile',
        header: { 'Authorization': 'Bearer ' + token },
        success: (res) => {
          const data = JSON.parse(res.data);
          if (data.code === 200) resolve(data);
          else reject(data);
        },
        fail: reject
      });
    });
  }

};
