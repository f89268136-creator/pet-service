const api = require('../../api/index');
const auth = require('../../utils/auth');

Page({
  data: {
    user: null,
    displayName: '宠物家长',
    accountText: '',
    stats: null,
    statsLoading: true,
    signing: false,
    menus: [
      { label: '我的预约', desc: '查看预约与服务进度', icon: '约', url: '/pages/appointment/list', tab: true, tone: 'tone-a' },
      { label: '宠物档案', desc: '管理毛孩子的基础信息', icon: '宠', url: '/pages/pet/list', tone: 'tone-b' },
      { label: '秒杀订单', desc: '查看抢购记录与核销码', icon: '秒', url: '/pages/seckill/order', tone: 'tone-c' },
      { label: '附近门店', desc: '查找距离最近的门店', icon: '店', url: '/pages/store/list', tone: 'tone-d' },
      { label: '发布动态', desc: '分享养宠日常', icon: '发', url: '/pages/community/publish', tone: 'tone-e' }
    ]
  },

  onShow: function () {
    if (!auth.requireLogin()) {
      return;
    }
    const user = auth.getUser() || {};
    this.setData({
      user: user,
      displayName: user.name ? user.name : '宠物家长',
      accountText: user.phone ? this.maskPhone(user.phone) : '已绑定微信'
    });
    this.loadStats();
  },

  maskPhone: function (phone) {
    return String(phone).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2');
  },

  loadStats: function () {
    const that = this;
    this.setData({ statsLoading: true });
    api.signin.stats()
      .then(function (stats) {
        that.setData({
          stats: that.decorateStats(stats),
          statsLoading: false
        });
      })
      .catch(function () {
        that.setData({ statsLoading: false });
      });
  },

  decorateStats: function (stats) {
    const data = stats || {};
    return Object.assign({}, data, {
      pointsText: data.points || 0,
      signedDaysText: data.signedDays || 0,
      continuousDaysText: data.continuousDays || 0,
      todayPointsText: data.todayPoints || 0,
      monthText: data.month || ''
    });
  },

  signIn: function () {
    if (this.data.signing || (this.data.stats && this.data.stats.signedToday)) {
      return;
    }
    const that = this;
    this.setData({ signing: true });
    api.signin.sign()
      .then(function (stats) {
        const decorated = that.decorateStats(stats);
        that.setData({ stats: decorated, signing: false });
        wx.showToast({
          title: '签到成功 +' + decorated.todayPointsText + ' 积分',
          icon: 'none'
        });
      })
      .catch(function () {
        that.setData({ signing: false });
      });
  },

  goMenu: function (event) {
    const url = event.currentTarget.dataset.url;
    const tabFlag = event.currentTarget.dataset.tab;
    const isTab = tabFlag === true || tabFlag === 'true';
    if (isTab) {
      wx.switchTab({ url: url });
    } else {
      wx.navigateTo({ url: url });
    }
  },

  logout: function () {
    wx.showModal({
      title: '退出登录',
      content: '退出后需要重新登录才能预约和管理宠物档案。',
      confirmText: '退出',
      confirmColor: '#e95f3c',
      success: function (res) {
        if (!res.confirm) {
          return;
        }
        api.user.logout().catch(function () {}).then(function () {
          auth.logout();
          wx.reLaunch({ url: '/pages/login/index' });
        });
      }
    });
  }
});
