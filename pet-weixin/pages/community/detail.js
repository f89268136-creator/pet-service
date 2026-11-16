const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

Page({
  data: {
    post: null,
    comments: [],
    loading: true,
    content: '',
    sending: false
  },

  onLoad: function (options) {
    this.postId = Number(options.id);
  },

  onShow: function () {
    if (!auth.requireLogin()) {
      return;
    }
    this.load();
  },

  decoratePost: function (post) {
    return Object.assign({}, post, {
      initial: fmt.initial(post.userName, '宠'),
      images: (post.images || []).filter(function (url) { return !!url; }),
      timeText: (post.createTime || '').slice(5)
    });
  },

  decorateComment: function (comment) {
    return Object.assign({}, comment, {
      initial: fmt.initial(comment.userName, '友'),
      timeText: (comment.createTime || '').slice(5)
    });
  },

  load: function () {
    const that = this;
    this.setData({ loading: true });
    Promise.all([
      api.community.detail(this.postId),
      api.community.comments(this.postId)
    ])
      .then(function (results) {
        that.setData({
          post: that.decoratePost(results[0] || {}),
          comments: (results[1] || []).map(that.decorateComment),
          loading: false
        });
      })
      .catch(function () {
        that.setData({ loading: false });
      });
  },

  toggleLike: function () {
    const that = this;
    api.community.like(this.postId)
      .then(function (liked) {
        const post = that.data.post;
        const count = (post.likeCount || 0) + (liked ? 1 : -1);
        that.setData({
          post: Object.assign({}, post, {
            liked: liked,
            likeCount: Math.max(0, count)
          })
        });
      })
      .catch(function () {});
  },

  previewImage: function (event) {
    const post = this.data.post;
    if (!post || !post.images.length) {
      return;
    }
    wx.previewImage({
      current: event.currentTarget.dataset.src,
      urls: post.images
    });
  },

  onCommentInput: function (event) {
    this.setData({ content: event.detail.value });
  },

  submitComment: function () {
    if (this.data.sending) {
      return;
    }
    const content = (this.data.content || '').trim();
    if (!content) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }
    const that = this;
    this.setData({ sending: true });
    api.community.comment(this.postId, content)
      .then(function (comments) {
        const list = (comments || []).map(that.decorateComment);
        that.setData({
          comments: list,
          post: Object.assign({}, that.data.post, { commentCount: list.length }),
          content: '',
          sending: false
        });
        wx.showToast({ title: '评论成功', icon: 'success' });
      })
      .catch(function () {
        that.setData({ sending: false });
      });
  }
});
