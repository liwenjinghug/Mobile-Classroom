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
  }
};
