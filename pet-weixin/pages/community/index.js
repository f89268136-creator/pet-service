const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

Page({
  data: {
    sort: 'time',
    list: [],
    page: 1,
    pageSize: 10,
    loading: false,
    finished: false
  },

  onShow: function () {
    if (!auth.requireLogin()) {
      return;
    }
    this.load(1);
  },

  onPullDownRefresh: function () {
    this.load(1);
  },

  onReachBottom: function () {
    if (!this.data.finished && !this.data.loading) {
      this.load(this.data.page + 1);
    }
  },

  decorate: function (item) {
    return Object.assign({}, item, {
      initial: fmt.initial(item.userName, '宠'),
      images: (item.images || []).filter(function (url) { return !!url; }),
      timeText: (item.createTime || '').slice(5)
    });
  },

  load: function (page) {
    const seq = (this.loadSeq || 0) + 1;
    this.loadSeq = seq;
    const that = this;
    this.setData({ loading: true });
    api.community.feed(this.data.sort, page, this.data.pageSize)
      .then(function (result) {
        if (seq !== that.loadSeq) {
          return;
        }
        const records = (result.records || []).map(that.decorate);
        const list = page === 1 ? records : that.data.list.concat(records);
        const total = result.total || 0;
        that.setData({
          list: list,
          page: page,
          loading: false,
          finished: page * that.data.pageSize >= total
        });
        wx.stopPullDownRefresh();
      })
      .catch(function () {
        if (seq !== that.loadSeq) {
          return;
        }
        that.setData({ loading: false });
        wx.stopPullDownRefresh();
      });
  },

  changeSort: function (event) {
    const sort = event.currentTarget.dataset.sort;
    if (sort === this.data.sort) {
      return;
    }
    this.setData({ sort: sort, list: [], finished: false });
    this.load(1);
  },

  toggleLike: function (event) {
    const id = Number(event.currentTarget.dataset.id);
    const that = this;
    api.community.like(id)
      .then(function (liked) {
        const list = that.data.list.map(function (item) {
          if (item.id !== id) {
            return item;
          }
          const count = (item.likeCount || 0) + (liked ? 1 : -1);
          return Object.assign({}, item, {
            liked: liked,
            likeCount: Math.max(0, count)
          });
        });
        that.setData({ list: list });
      })
      .catch(function () {});
  },

  openDetail: function (event) {
    wx.navigateTo({
      url: '/pages/community/detail?id=' + event.currentTarget.dataset.id
    });
  },

  previewImage: function (event) {
    const id = Number(event.currentTarget.dataset.id);
    const current = event.currentTarget.dataset.src;
    const post = this.data.list.find(function (item) {
      return item.id === id;
    });
    if (!post || !post.images.length) {
      return;
    }
    wx.previewImage({ current: current, urls: post.images });
  },

  publish: function () {
    wx.navigateTo({ url: '/pages/community/publish' });
  }
});
