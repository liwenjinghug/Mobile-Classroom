// 作业详情页面 - 使用proj_fz接口
const api = require('../../../utils/api');

Page({
  data: {
    homeworkId: null,
    homework: null,
    submission: null,
    isExpired: false,
    status: '',
    loading: true,
    submitting: false,
    answer: '', // 作业答案文本
    uploadedFiles: [], // 已上传的文件列表
  },

  onLoad(options) {
    const homeworkId = options.homeworkId;

    if (!homeworkId) {
      wx.showToast({ title: '作业ID缺失', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }

    this.setData({ homeworkId: homeworkId });
    this.loadHomeworkDetail();
  },

  // 加载作业详情
  loadHomeworkDetail() {
    const { homeworkId } = this.data;

    this.setData({ loading: true });

    // 并行请求：作业详情 + 我的提交记录
    Promise.all([
      api.getHomeworkDetail(homeworkId),
      api.getMyHomeworkSubmissions()
    ]).then(([homework, submissions]) => {
      console.log('作业详情', homework);
      console.log('我的提交记录', submissions);

      // 从提交记录中找到当前作业的提交
      let mySubmission = null;
      if (submissions && Array.isArray(submissions.rows || submissions)) {
        const list = submissions.rows || submissions;
        mySubmission = list.find(item => item.homeworkId == homeworkId);
      }

      // 判断是否过期
      const now = new Date();
      const deadline = homework.deadline ? new Date(homework.deadline) : null;
      const isExpired = deadline ? now > deadline : false;

      // 判断状态
      let status = '未提交';
      if (mySubmission) {
        if (mySubmission.isGraded === 1) {
          status = '已批改';
        } else if (mySubmission.status == 1) {
          status = '已提交';
        }
      } else if (isExpired) {
        status = '已截止';
      }

      // 解析提交内容（分离答案和文件）
      const submissionContent = mySubmission?.submissionFiles
        ? this.parseSubmissionContent(mySubmission.submissionFiles)
        : { answer: '', files: [] };

      this.setData({
        homework: homework || {},
        submission: mySubmission,
        isExpired: isExpired,
        status: status,
        loading: false,
        answer: submissionContent.answer,
        uploadedFiles: submissionContent.files
      });
    }).catch(err => {
      console.error('加载失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    });
  },

  // 输入答案
  onAnswerInput(e) {
    this.setData({ answer: e.detail.value });
  },

  // 提交作业
  submitHomework() {
    const { homeworkId, answer, uploadedFiles, isExpired, status, submission } = this.data;

    if (isExpired && !submission) {
      wx.showToast({ title: '作业已截止，无法提交', icon: 'none' });
      return;
    }

    if (!answer.trim() && uploadedFiles.length === 0) {
      wx.showToast({ title: '请填写答案或上传文件', icon: 'none' });
      return;
    }

    wx.showModal({
      title: '确认提交',
      content: submission ? '确定要重新提交作业吗？' : '确定要提交作业吗？',
      success: (res) => {
        if (res.confirm) {
          this.doSubmit();
        }
      }
    });
  },

  // 执行提交
  doSubmit() {
    const { homeworkId, answer, uploadedFiles, submission } = this.data;

    this.setData({ submitting: true });

    // 构造提交内容：答案文本 + 文件URL列表（逗号分隔）
    let submissionContent = answer || '';

    if (uploadedFiles.length > 0) {
      // 只保存文件URL，用逗号分隔
      const fileUrls = uploadedFiles.map(file => file.url).join(',');
      if (submissionContent) {
        submissionContent = `${submissionContent}\n\n附件：${fileUrls}`;
      } else {
        submissionContent = fileUrls;
      }
    }

    // 构造提交数据
    const submissionData = {
      homeworkId: homeworkId,
      submissionFiles: submissionContent,
      status: 1 // 1=已提交
    };

    // 如果已有提交记录，则添加ID用于更新
    if (submission && submission.studentHomeworkId) {
      submissionData.studentHomeworkId = submission.studentHomeworkId;
    }

    console.log('提交数据:', submissionData);

    api.submitHomework(submissionData)
      .then(res => {
        wx.showToast({ title: '提交成功', icon: 'success' });
        this.setData({ submitting: false });

        // 延迟刷新
        setTimeout(() => {
          this.loadHomeworkDetail();
        }, 1000);
      })
      .catch(err => {
        console.error('提交失败', err);
        const msg = err.msg || err.message || '提交失败';
        wx.showToast({ title: msg, icon: 'none', duration: 2000 });
        this.setData({ submitting: false });
      });
  },

  // 查看成绩
  viewGrade() {
    const { submission } = this.data;
    if (!submission) {
      wx.showToast({ title: '暂无提交记录', icon: 'none' });
      return;
    }

    wx.navigateTo({
      url: `/pages/proj_fz/homework/homework-grade?submissionId=${submission.studentHomeworkId}`
    });
  },

  // 选择文件
  chooseFile() {
    const that = this;
    const { uploadedFiles, status } = this.data;

    if (status === '已批改') {
      wx.showToast({ title: '作业已批改，无法修改', icon: 'none' });
      return;
    }

    wx.chooseMessageFile({
      count: 5 - uploadedFiles.length,
      type: 'all',
      success(res) {
        const tempFiles = res.tempFiles;
        console.log('选择的文件', tempFiles);

        // 上传文件
        that.uploadFiles(tempFiles);
      }
    });
  },

  // 上传文件到服务器
  uploadFiles(files) {
    const that = this;
    const token = wx.getStorageSync('token');
    const baseUrl = getApp().globalData.baseUrl || 'http://localhost:8080';

    wx.showLoading({ title: '上传中...' });

    const uploadPromises = files.map(file => {
      return new Promise((resolve, reject) => {
        wx.uploadFile({
          url: baseUrl + '/common/upload',
          filePath: file.path,
          name: 'file',
          header: {
            'Authorization': 'Bearer ' + token
          },
          success(res) {
            try {
              const data = JSON.parse(res.data);
              if (data.code === 200) {
                resolve({
                  name: file.name,
                  url: data.url || data.fileName,
                  size: file.size
                });
              } else {
                reject(data.msg || '上传失败');
              }
            } catch (e) {
              reject('解析响应失败');
            }
          },
          fail: reject
        });
      });
    });

    Promise.all(uploadPromises)
      .then(uploadedFiles => {
        wx.hideLoading();
        const currentFiles = that.data.uploadedFiles;
        // 格式化文件大小
        const formattedFiles = uploadedFiles.map(file => ({
          name: file.name,
          url: file.url,
          size: file.size,
          sizeText: file.size ? (file.size > 1024 ? (file.size/1024).toFixed(1) + 'KB' : file.size + 'B') : ''
        }));

        that.setData({
          uploadedFiles: currentFiles.concat(formattedFiles)
        });
        wx.showToast({ title: `上传成功 ${formattedFiles.length} 个文件`, icon: 'success' });
      })
      .catch(err => {
        wx.hideLoading();
        console.error('上传失败', err);
        wx.showToast({ title: err || '上传失败', icon: 'none' });
      });
  },

  // 删除文件
  deleteFile(e) {
    const index = e.currentTarget.dataset.index;
    const { uploadedFiles } = this.data;

    wx.showModal({
      title: '确认删除',
      content: '确定要删除该文件吗？',
      success: (res) => {
        if (res.confirm) {
          uploadedFiles.splice(index, 1);
          this.setData({ uploadedFiles });
        }
      }
    });
  },

  // 预览或下载文件
  previewFile(e) {
    const url = e.currentTarget.dataset.url;
    if (!url) {
      wx.showToast({ title: '文件地址无效', icon: 'none' });
      return;
    }

    const baseUrl = getApp().globalData.baseUrl || 'http://localhost:8080';
    const fullUrl = url.startsWith('http') ? url : baseUrl + url;

    // 判断文件类型
    const ext = url.split('.').pop().toLowerCase();
    const fileName = url.split('/').pop();
    const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];
    const docExts = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf'];

    if (imageExts.includes(ext)) {
      // 图片直接预览
      this.previewImage(fullUrl);
    } else if (docExts.includes(ext)) {
      // 文档直接打开
      this.openDocument(fullUrl, ext);
    } else {
      // 其他文件下载到服务器
      wx.showModal({
        title: '下载确认',
        content: `是否将文件 ${fileName} 下载到服务器？`,
        success: (res) => {
          if (res.confirm) {
            this.downloadToServerStorage(fullUrl, fileName);
          }
        }
      });
    }
  },

  // 预览图片
  previewImage(url) {
    wx.previewImage({
      urls: [url],
      current: url
    });
  },

  // 打开文档
  openDocument(url, ext) {
    wx.showLoading({ title: '加载中...' });
    wx.downloadFile({
      url: url,
      success: (res) => {
        wx.hideLoading();
        if (res.statusCode === 200) {
          const filePath = res.tempFilePath;
          wx.openDocument({
            filePath: filePath,
            fileType: ext,
            showMenu: true,
            success: () => {
              console.log('打开文档成功');
            },
            fail: (err) => {
              console.error('打开文档失败', err);
              wx.showToast({ title: '无法打开该文档', icon: 'none' });
            }
          });
        } else {
          wx.hideLoading();
          wx.showToast({ title: '文件加载失败', icon: 'none' });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('下载失败', err);
        wx.showToast({ title: '下载失败', icon: 'none' });
      }
    });
  },

  // 下载文件到服务器存储
  downloadToServerStorage(fileUrl, fileName) {
    console.log('开始下载文件', { fileUrl, fileName });
    wx.showLoading({ title: '下载中...' });

    api.downloadFileToServer(fileUrl, fileName)
      .then(res => {
        console.log('下载成功，后端返回', res);
        wx.hideLoading();

        const path = res.path || res.absolutePath || ('uploadPath/upload/' + fileName);
        const size = res.size ? `(${(res.size / 1024).toFixed(2)} KB)` : '';

        wx.showModal({
          title: '✅ 下载成功',
          content: `文件已保存到项目目录\n\n路径：${path}\n${size}`,
          showCancel: false,
          confirmText: '知道了'
        });
      })
      .catch(err => {
        wx.hideLoading();
        console.error('下载失败，错误信息', err);

        const errorMsg = err.msg || err.message || err.errMsg || '网络错误';
        wx.showModal({
          title: '❌ 下载失败',
          content: `错误信息：${errorMsg}\n\n请检查网络连接或联系管理员`,
          showCancel: false,
          confirmText: '知道了'
        });
      });
  },

  // 解析文件列表（从submissionFiles字符串）
  parseFiles(filesStr) {
    if (!filesStr) return [];

    try {
      // 先尝试从"附件："后面提取
      const attachmentIndex = filesStr.indexOf('附件：');
      let filesPart = filesStr;

      if (attachmentIndex !== -1) {
        filesPart = filesStr.substring(attachmentIndex + 3).trim();
      }

      // 尝试解析JSON格式（旧格式兼容）
      if (filesPart.startsWith('[')) {
        const parsed = JSON.parse(filesPart);
        return parsed.map(file => ({
          ...file,
          sizeText: file.size ? (file.size > 1024 ? (file.size/1024).toFixed(1) + 'KB' : file.size + 'B') : ''
        }));
      }

      // 按逗号分隔URL列表
      const urls = filesPart.split(',').map(url => url.trim()).filter(url => url);
      return urls.map(url => {
        const fileName = url.split('/').pop().split('_').pop() || url.split('/').pop();
        return {
          name: decodeURIComponent(fileName),
          url: url,
          sizeText: ''
        };
      });
    } catch (e) {
      console.error('解析文件列表失败', e);
      return [];
    }
  },

  // 从submissionFiles中分离文本答案和文件列表
  parseSubmissionContent(filesStr) {
    if (!filesStr) return { answer: '', files: [] };

    const attachmentIndex = filesStr.indexOf('附件：');
    if (attachmentIndex === -1) {
      // 没有附件标记，全部作为答案
      return {
        answer: filesStr,
        files: []
      };
    }

    // 分离答案和附件
    const answer = filesStr.substring(0, attachmentIndex).trim();
    const files = this.parseFiles(filesStr);

    return { answer, files };
  },
});

