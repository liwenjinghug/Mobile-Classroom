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
        // 1. 替换路径
        data.content = data.content.replace(/src="\/dev-api\/profile/g, `src="${this.data.baseUrl}/profile`);
        data.content = data.content.replace(/src="\/profile/g, `src="${this.data.baseUrl}/profile`);
        
        // 2. 优化图片样式
        data.content = data.content.replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;margin:10px 0;"');

        // 3. 提取附件链接 (修正正则逻辑)
        // 匹配 <a ...>...</a>
        const linkRegex = /<a[^>]+href="([^"]+)"[^>]*>([\s\S]*?)<\/a>/g;
        let match;
        while ((match = linkRegex.exec(data.content)) !== null) {
          const href = this.handleUrl(match[1]); 
          const innerHtml = match[2]; // a 标签内部的内容

          // (关键修复) 如果链接内容包含 <img，说明这是点击图片放大，不是附件下载
          if (innerHtml.indexOf('<img') !== -1) {
            continue;
          }

          const name = innerHtml.replace(/<[^>]+>/g, '').trim(); // 去除标签取纯文本文件名
          
          // 如果清洗后名字为空，也不显示
          if (!name) continue;

          attachments.push({ url: href, name: name });
        }

        // 4. 样式注入
        data.content = data.content.replace(/<a /g, '<a style="color: #2d8cf0; text-decoration: underline; font-weight: bold;" ');

        // 【紧急修复版】
        // 修复了之前正则无法匹配带有换行符的代码内容，导致代码全空的问题
        data.content = data.content.replace(/<div class="ql-code-block-container"[\s\S]*?<\/div>\s*<\/div>/g, function(match) {
          
          // 1. 【核心修正】将 . 改为 [\s\S]，确保能匹配到 div 里的任何内容（包括换行符）
          const lineRegex = /<div class="ql-code-block"[^>]*>([\s\S]*?)<\/div>/g;
          
          let lines = [];
          let item;
          
          // 2. 提取每一行
          while ((item = lineRegex.exec(match)) !== null) {
             // item[1] 是代码内容。trimRight() 去除行尾可能存在的多余空格
             lines.push(item[1].trimRight());
          }
          
          // 如果没有匹配到任何行（防止全空），直接返回原始内容，或者至少显示点什么
          if (lines.length === 0) {
             return match; 
          }

          // 3. 用换行符拼接
          const cleanCode = lines.join('\n');
          
          // 4. 返回
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