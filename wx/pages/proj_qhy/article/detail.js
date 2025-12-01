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
      // --- 兼容逻辑：自动判断是 res.data 还是直接 res ---
      const data = (res && res.data) ? res.data : res;

      // 安全检查
      if (!data) {
        console.error('获取文章详情失败，返回数据为空', res);
        return;
      }

      if (data.cover) {
        data.cover = this.handleUrl(data.cover);
      }
      
      const attachments = [];

      if (data.content) {
        // 1. 替换路径
        data.content = data.content.replace(/src="\/dev-api\/profile/g, `src="${this.data.baseUrl}/profile`);
        data.content = data.content.replace(/src="\/profile/g, `src="${this.data.baseUrl}/profile`);
        
        // 2. 【修复点】优化图片样式
        // 之前的代码里 < img 多了个空格，导致图片变成了文字链接
        // 现已修复为 <img
        data.content = data.content.replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;margin:10px 0;"');

        // 3. 提取附件链接
        const linkRegex = /<a[^>]+href="([^"]+)"[^>]*>([\s\S]*?)<\/a>/g;
        let match;
        while ((match = linkRegex.exec(data.content)) !== null) {
          const href = this.handleUrl(match[1]); 
          const innerHtml = match[2]; // a 标签内部的内容

          // 如果链接内容包含 <img，说明这是点击图片放大，不是附件下载
          // 注意：这里必须和上面替换后的标签一致（没有空格）
          if (innerHtml.indexOf('<img') !== -1) {
            continue;
          }

          const name = innerHtml.replace(/<[^>]+>/g, '').trim(); // 去除标签取纯文本文件名
          
          // 如果清洗后名字为空，也不显示
          if (!name) continue;

          attachments.push({ url: href, name: name });
        }

        // 4. 样式注入 (给附件链接加样式)
        data.content = data.content.replace(/<a /g, '<a style="color: #2d8cf0; text-decoration: underline; font-weight: bold;" ');

        // 5. 代码块修复 (保留换行符)
        data.content = data.content.replace(/<div class="ql-code-block-container"[\s\S]*?<\/div>\s*<\/div>/g, function(match) {
          const lineRegex = /<div class="ql-code-block"[^>]*>([\s\S]*?)<\/div>/g;
          let lines = [];
          let item;
          while ((item = lineRegex.exec(match)) !== null) {
             lines.push(item[1].trimRight());
          }
          if (lines.length === 0) return match; 
          const cleanCode = lines.join('\n');
          return '<div class="ql-code-unified">' + cleanCode + '</div>';
        });
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

  previewTxt(url) {
    wx.showLoading({ title: '加载文本...' });
    wx.downloadFile({
      url: url,
      success: (res) => {
        if (res.statusCode === 200) {
          const fs = wx.getFileSystemManager();
          fs.readFile({
            filePath: res.tempFilePath,
            success: (data) => {
              let content = '';
              const buffer = data.data;
              
              try {
                const decoder = new TextDecoder('utf-8', { fatal: true });
                content = decoder.decode(buffer);
              } catch (e) {
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