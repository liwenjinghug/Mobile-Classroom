const api = require('../../../utils/api.js');
const app = getApp();

Page({
  data: {
    article: {},
    attachments: [],
    baseUrl: app.globalData.baseUrl || 'http://localhost:8080',
    showTxtPreview: false,
    txtContent: ''
  },

  onLoad(options) {
    const id = options.id;
    if (id) {
      this.getDetail(id);
      api.viewArticle(id);
    }
  },

  getDetail(id) {
    api.getArticle(id).then(res => {
      const data = res.data;
      data.cover = this.handleUrl(data.cover);
      
      const attachments = [];

      if (data.content) {
        data.content = data.content.replace(/src="\/dev-api\/profile/g, `src="${this.data.baseUrl}/profile`);
        data.content = data.content.replace(/src="\/profile/g, `src="${this.data.baseUrl}/profile`);
        data.content = data.content.replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;margin:10px 0;"');

        const linkRegex = /<a[^>]+href="([^"]+)"[^>]*>([\s\S]*?)<\/a>/g;
        let match;
        while ((match = linkRegex.exec(data.content)) !== null) {
          const href = this.handleUrl(match[1]); 
          const name = match[2].replace(/<[^>]+>/g, '').trim(); 
          attachments.push({ url: href, name: name });
        }

        data.content = data.content.replace(/<a /g, '<a style="color: #2d8cf0; text-decoration: underline; font-weight: bold;" ');
      }
      
      this.setData({ 
        article: data,
        attachments: attachments 
      });
    });
  },

  openAttachment(e) {
    const url = e.currentTarget.dataset.url;
    const fileType = url.split('.').pop().toLowerCase();
    
    // 拦截 TXT 文件
    if (fileType === 'txt') {
      this.previewTxt(url);
      return;
    }
    
    wx.showLoading({ title: '正在打开...' });
    
    wx.downloadFile({
      url: url,
      success: function (res) {
        const filePath = res.tempFilePath;
        wx.openDocument({
          filePath: filePath,
          fileType: fileType,
          success: function () {
            console.log('打开文档成功');
          },
          fail: function (err) {
            console.error(err);
            wx.showToast({ title: '无法预览此格式', icon: 'none' });
          },
          complete: function() {
            wx.hideLoading();
          }
        })
      },
      fail: function (err) {
        console.error(err);
        wx.hideLoading();
        wx.showToast({ title: '下载失败', icon: 'none' });
      }
    })
  },

  // (修改) 读取并预览 TXT，增加 GBK 兼容
  previewTxt(url) {
    wx.showLoading({ title: '加载文本...' });
    wx.downloadFile({
      url: url,
      success: (res) => {
        if (res.statusCode === 200) {
          const fs = wx.getFileSystemManager();
          fs.readFile({
            filePath: res.tempFilePath,
            // 重点：不指定 encoding，直接获取 ArrayBuffer 二进制数据
            success: (data) => {
              let content = '';
              const buffer = data.data;
              
              try {
                // 1. 优先尝试 UTF-8 (常用)
                // fatal: true 表示如果发现不是有效的 UTF-8 序列则抛出异常，转入 catch
                const decoder = new TextDecoder('utf-8', { fatal: true });
                content = decoder.decode(buffer);
              } catch (e) {
                // 2. 如果 UTF-8 失败，尝试 GB18030 (兼容 GBK/GB2312，Windows 记事本默认格式)
                try {
                  const decoder = new TextDecoder('gb18030');
                  content = decoder.decode(buffer);
                } catch (e2) {
                  content = '文本编码识别失败，请确保文件为 UTF-8 或 GBK 格式';
                }
              }

              this.setData({
                txtContent: content,
                showTxtPreview: true
              });
              wx.hideLoading();
            },
            fail: (err) => {
              wx.hideLoading();
              wx.showToast({ title: '读取文本失败', icon: 'none' });
            }
          });
        } else {
          wx.hideLoading();
          wx.showToast({ title: '下载失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },

  closeTxtPreview() {
    this.setData({ showTxtPreview: false });
  },
  
  stopProp() {},

  handleLike() {
    const id = this.data.article.id;
    api.likeArticle(id).then(() => {
      const art = this.data.article;
      if (art.userLikeStatus === 1) {
        art.userLikeStatus = 0;
        art.likeCount--;
        wx.showToast({ title: '取消点赞', icon: 'none' });
      } else {
        art.userLikeStatus = 1;
        art.likeCount++;
        wx.showToast({ title: '点赞成功', icon: 'none' });
      }
      this.setData({ article: art });
    });
  },

  handleUrl(url) {
    if (!url) return '';
    if (url.indexOf('/dev-api') === 0) {
      url = url.replace('/dev-api', '');
    }
    if (url.startsWith('http') || url.startsWith('wxfile')) return url;
    return this.data.baseUrl + url;
  }
});