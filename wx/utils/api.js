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
          // Some backends wrap responses as { code: number, msg: string, data: ... }
          // Treat code !== 200 as an application-level error.
          try {
            const body = res.data;
            if (body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'code')) {
              const code = body.code;
              // convention: code === 200 means success
              // Special handling for 202 (Bind needed)
              if (Number(code) === 202) {
                 return reject(body); // Reject so we can catch it in the UI
              }
              if (Number(code) !== 200) {
                console.warn('[api.request] application-level error code != 200', body);
                return reject(body);
              }
              // unwrap common wrapper
              if (Object.prototype.hasOwnProperty.call(body, 'data')) return resolve(body.data);
            }
          } catch (e) { /* ignore */ }
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

function requestWithRetry(options, retries = 1, backoff = 500) {
  return new Promise((resolve, reject) => {
    function attempt(remaining) {
      request(options).then(resolve).catch(err => {
        const isServerError = err && err.statusCode && err.statusCode >= 500;
        if (remaining > 0 && isServerError) {
          console.warn('[api] retrying after server error, remaining=', remaining, err);
          setTimeout(() => attempt(remaining - 1), backoff);
        } else {
          // normalize error shape
          const normalized = Object.assign({}, err || {}, { _timestamp: Date.now() });
          reject(normalized);
        }
      });
    }
    attempt(retries);
  });
}

module.exports = {
  request, // Export the internal request function
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

// 获取课堂详情（单个）- 使用proj_fz无权限接口
getClassDetail(sessionId) {
  return request({
    url: `/proj_fz/class/${sessionId}`,
    method: 'GET'
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
  },

  // ================== proj_fz: 学生作业相关接口（无权限限制）==================

  // 获取作业列表
  getHomeworkList(sessionId, status) {
    const query = {
      pageNum: 1,
      pageSize: 1000  // 获取足够多的数据
    };
    if (sessionId) query.sessionId = sessionId;
    return request({
      url: '/proj_fz/homework/list',
      method: 'GET',
      data: query
    });
  },

  // 获取作业详情
  getHomeworkDetail(homeworkId) {
    return request({
      url: `/proj_fz/homework/${homeworkId}`,
      method: 'GET'
    });
  },

  // 提交作业
  submitHomework(data) {
    return request({
      url: '/proj_fz/homework/submit',
      method: 'POST',
      data: data
    });
  },

  // 获取学生的作业提交记录
  getMyHomeworkSubmissions() {
    return request({
      url: '/proj_fz/homework/my-submissions',
      method: 'GET'
    });
  },

  // 下载文件到服务器
  downloadFileToServer(fileUrl, fileName) {
    return request({
      url: '/proj_fz/file/download',
      method: 'POST',
      data: { fileUrl, fileName }
    });
  },

// --- 考试相关 ---
getExamSessions(sessionId) {
  // If dev mock mode enabled, return sample exams immediately
  try {
    const app = getApp();
    if (app && app.globalData && app.globalData.mockMode) {
      console.log('[api] mockMode enabled: returning mock exam sessions');
      const now = Date.now();
      return Promise.resolve([
        { id: 'exam-1', title: '期中考试（模拟）', startAt: now - 3600 * 1000, endAt: now + 3600 * 1000, questionTypes: '单选/多选', submitted: false },
        { id: 'exam-2', title: '章节测验（已提交）', startAt: now - 7200 * 1000, endAt: now - 3600 * 1000, questionTypes: '单选', submitted: true, score: 85 },
        { id: 'exam-3', title: '实验考核（未开始）', startAt: now + 3600 * 1000, endAt: now + 7200 * 1000, questionTypes: '问答', submitted: false }
      ]);
    }
  } catch (e) {}

  // If client provided a sessionId (课堂id), use the controller's list-by-session endpoint
  let url = '/proj_lwj/exam';
  if (sessionId) {
    url = '/proj_lwj/exam/list-by-session/' + encodeURIComponent(sessionId);
  } else {
    // fallback: student-facing available list
    url = '/proj_lwj/exam/available';
  }

  const attempted = [];
  const normalize = (res) => {
    if (!res) return [];
    if (Array.isArray(res)) return res;
    if (res && Array.isArray(res.rows)) return res.rows;
    if (res && res.data) {
      if (Array.isArray(res.data)) return res.data;
      if (Array.isArray(res.data.list)) return res.data.list;
      if (Array.isArray(res.data.rows)) return res.data.rows;
      if (Array.isArray(res.data.sessions)) return res.data.sessions;
      if (Array.isArray(res.data.records)) return res.data.records;
      if (Array.isArray(res.data.items)) return res.data.items;
      if (Array.isArray(res.data.content)) return res.data.content;
      if (Array.isArray(res.data.data)) return res.data.data;
    }
    if (res && Array.isArray(res.sessions)) return res.sessions;
    if (res && Array.isArray(res.records)) return res.records;
    if (res && Array.isArray(res.items)) return res.items;
    if (res && Array.isArray(res.content)) return res.content;
    if (res && typeof res === 'object') return [res];
    return [];
  };

  attempted.push(url + ' [GET]');
  const opt = { url: url, method: 'GET', data: sessionId && url.indexOf('list-by-session') === -1 ? { sessionId } : {} };
  return requestWithRetry(opt, 1, 600).then(res => {
    console.log('[api.getExamSessions] raw response for', url, res);
    const arr = normalize(res);
    return arr;
  }).catch(err => {
    // attach attempted info
    if (err && typeof err === 'object') err.attempted = attempted.slice();
    console.warn('getExamSessions failed for', url, err);
    return Promise.reject(err);
  });
},
getExamDetail(examId) {
  return request({ url: '/proj_lwj/exam/' + examId, method: 'GET' }).catch(err => {
    console.warn('getExamDetail failed, returning minimal mock', err);
    // minimal mock
    return {
      id: examId,
      title: '模拟考试 ' + examId,
      durationSeconds: 300,
      questions: [
        { id: 'q1', type: 'single', content: '1 + 1 = ?', options: [{ id: 'o1', text: '1' }, { id: 'o2', text: '2' }] },
        { id: 'q2', type: 'multiple', content: '选择偶数', options: [{ id: 'o1', text: '1' }, { id: 'o2', text: '2' }, { id: 'o3', text: '3' }, { id: 'o4', text: '4' }] },
        { id: 'q3', type: 'text', content: '简述 JavaScript 事件循环' }
      ]
    };
  });
},
  // 获取考试题目列表
  listExamQuestions(examId) {
    console.log('[api.listExamQuestions] request examId=', examId);
    return request({ url: '/proj_lwj/exam/question/list', method: 'GET', data: examId ? { examId } : {} }).then(res => { console.log('[api.listExamQuestions] raw response', res); return res; });
  },

  // 学生开始考试（创建或获取参与记录）
  startExamParticipant(payload) {
    // 自动添加学号信息
    const studentNo = wx.getStorageSync('studentNo');
    const data = Object.assign({}, payload || {});
    if (studentNo && !data.studentNo && !data.student_no) {
      data.studentNo = studentNo;
    }
    console.log('[api.startExamParticipant] payload=', data);
    return request({ url: '/proj_lwj/exam/start', method: 'POST', data: data }).then(res => { console.log('[api.startExamParticipant] raw response', res); return res; });
  },

  // 获取答案列表
  listAnswers(query) {
    console.log('[api.listAnswers] query=', query);
    return request({ url: '/proj_lwj/exam/answer/list', method: 'GET', data: query || {} }).then(res => { console.log('[api.listAnswers] raw response', res); return res; });
  },

  // 获取我的考试记录（已参加的考试）
  getMyExamRecords(sessionId) {
    const studentNo = wx.getStorageSync('studentNo');
    const params = { studentNo };
    if (sessionId) params.sessionId = sessionId;
    console.log('[api.getMyExamRecords] params=', params);
    return request({ url: '/proj_lwj/exam/participant/list', method: 'GET', data: params })
      .then(res => {
        console.log('[api.getMyExamRecords] raw response', res);
        // 规范化返回数据
        if (Array.isArray(res)) return res;
        if (res && Array.isArray(res.rows)) return res.rows;
        if (res && res.data) {
          if (Array.isArray(res.data)) return res.data;
          if (Array.isArray(res.data.rows)) return res.data.rows;
          if (Array.isArray(res.data.records)) return res.data.records;
        }
        return [];
      })
      .catch(err => {
        console.warn('[api.getMyExamRecords] failed', err);
        // 如果接口不存在，返回空数组
        return [];
      });
  },

  // 提交试卷（参与记录提交） - use participant submit endpoint
  submitExam(examId, payload) {
    const studentNo = wx.getStorageSync('studentNo');
    const body = Object.assign({}, payload || {});
    if (examId) body.examId = examId;
    if (studentNo && !body.studentNo && !body.student_no) {
      body.studentNo = studentNo;
    }
    console.log('[api.submitExam] request body=', body);
    return request({ url: '/proj_lwj/exam/participant/submit', method: 'POST', data: body }).then(res => { console.log('[api.submitExam] raw response', res); return res; }).catch(err => {
      console.warn('submitExam failed', err);
      return Promise.reject(err);
    });
  }
};
