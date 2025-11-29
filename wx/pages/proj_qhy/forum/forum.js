const api = require('../../../utils/api.js');
const app = getApp();

Page({
  data: {
    currentTab: 0, // 0: 论坛, 1: 文章
    postList: [],
    articleList: [],
    loading: false,
    baseUrl: app.globalData.baseUrl || 'http://localhost:8080',
    
    // (新增) 通知相关
    showNotice: false,
    noticeList: []
  },

  onShow() {
    this.refreshData();
  },

  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({ currentTab: index });
    if ((index === 0 && this.data.postList.length === 0) || 
        (index === 1 && this.data.articleList.length === 0)) {
      this.refreshData();
    }
  },

  refreshData() {
    if (this.data.currentTab === 0) {
      this.getForumPosts();
    } else {
      this.getArticles();
    }
  },

  // --- (新增) 通知逻辑 ---
  openNotices() {
    this.setData({ showNotice: true });
    this.getNotices();
  },

  closeNotices() {
    this.setData({ showNotice: false });
  },

  getNotices() {
    wx.showLoading({ title: '加载通知...' });
    api.getUserNotices().then(res => {
      const list = (res.data || []).map(item => {
        // 处理头像
        item.operatorAvatar = this.handleUrl(item.operatorAvatar);
        return item;
      });
      this.setData({ noticeList: list });
      wx.hideLoading();
    }).catch(() => {
      wx.hideLoading();
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  // --- 论坛逻辑 ---
  getForumPosts() {
    this.setData({ loading: true });
    api.getPostList().then(res => {
      const list = (res.data || []).map(item => {
        item.avatar = this.handleUrl(item.avatar);
        if (item.imageUrls) {
          item.imageArray = item.imageUrls.split(',').map(url => this.handleUrl(url));
        } else {
          item.imageArray = [];
        }
        item.comments = []; 
        item.likes = [];    
        return item;
      });
      this.setData({ postList: list, loading: false });
      
      list.forEach((post, index) => {
        this.getPostComments(post.postId, index);
        this.getPostLikes(post.postId, index);
      });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  getPostComments(postId, index) {
    api.getCommentsByPostId(postId).then(res => {
      const key = `postList[${index}].comments`;
      this.setData({ [key]: res.data || [] });
    });
  },

  getPostLikes(postId, index) {
    api.getLikesByPostId(postId).then(res => {
      const key = `postList[${index}].likes`;
      this.setData({ [key]: res.data || [] });
    });
  },

  refreshSinglePostComments(postId) {
    const index = this.data.postList.findIndex(p => p.postId === postId);
    if (index !== -1) {
      this.getPostComments(postId, index);
    }
  },

  handleLikePost(e) {
    const { id, index } = e.currentTarget.dataset;
    api.likePost(id).then(res => {
      const list = this.data.postList;
      const isLiked = !list[index].isLiked;
      list[index].isLiked = isLiked;
      list[index].likeCount += isLiked ? 1 : -1;
      this.setData({ postList: list });
      this.getPostLikes(id, index);
      wx.showToast({ title: isLiked ? '点赞成功' : '取消点赞', icon: 'none' });
    });
  },

  handleCommentPost(e) {
    const postId = e.currentTarget.dataset.id;
    const index = this.data.postList.findIndex(p => p.postId === postId);
    wx.showModal({
      title: '评论',
      editable: true,
      placeholderText: '请输入评论内容',
      success: (res) => {
        if (res.confirm && res.content) {
          const data = { postId: postId, content: res.content, parentId: 0, replyToUserId: null };
          api.addComment(data).then(() => {
            wx.showToast({ title: '评论成功', icon: 'success' });
            this.refreshSinglePostComments(postId);
            if (index !== -1) {
              const key = `postList[${index}].commentCount`;
              this.setData({ [key]: (this.data.postList[index].commentCount || 0) + 1 });
            }
          });
        }
      }
    });
  },

  handleReplyComment(e) {
    const { postid, comment } = e.currentTarget.dataset;
    const index = this.data.postList.findIndex(p => p.postId === postid);
    const replyName = comment.nickName;
    wx.showModal({
      title: `回复 ${replyName}`,
      editable: true,
      placeholderText: '请输入回复内容',
      success: (res) => {
        if (res.confirm && res.content) {
          const data = { postId: postid, content: res.content, parentId: comment.commentId, replyToUserId: comment.userId };
          api.addComment(data).then(() => {
            wx.showToast({ title: '回复成功', icon: 'success' });
            this.refreshSinglePostComments(postid);
            if (index !== -1) {
              const key = `postList[${index}].commentCount`;
              this.setData({ [key]: (this.data.postList[index].commentCount || 0) + 1 });
            }
          });
        }
      }
    });
  },

  previewImage(e) {
    const { current, urls } = e.currentTarget.dataset;
    wx.previewImage({ current, urls });
  },

  // --- 文章逻辑 ---
  getArticles() {
    this.setData({ loading: true });
    api.listArticle({}).then(res => {
      const list = (res.rows || []).map(item => {
        item.cover = this.handleUrl(item.cover);
        return item;
      });
      this.setData({ articleList: list, loading: false });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  goToArticle(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/proj_qhy/article/detail?id=${id}` });
  },

  handleUrl(url) {
    if (!url) return '/assets/images/profile.png';
    if (url.indexOf('/dev-api') === 0) {
      url = url.replace('/dev-api', '');
    }
    if (url.startsWith('http') || url.startsWith('wxfile')) return url;
    return this.data.baseUrl + url;
  }
});