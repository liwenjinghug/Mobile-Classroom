module.exports = {
  set(key, val) { wx.setStorageSync(key, val); },
  get(key) { return wx.getStorageSync(key); },
  remove(key) { wx.removeStorageSync(key); }
};

