const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

Page({
  data: {
    tabs: [
      { label: '全部', status: null },
      { label: '待支付', status: 0 },
      { label: '待使用', status: 1 },
      { label: '已使用', status: 2 },
      { label: '已取消', status: 3 }
    ],
    activeTab: 0,
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

  decorate: function (order) {
    return Object.assign({}, order, {
      amountText: fmt.money(order.amount),
      statusClass: fmt.seckillStatusClass(order.status),
      initial: fmt.initial(order.setmealName, '秒'),
      timeText: (order.createTime || '').slice(5),
      canPay: order.status === 0,
      showVerify: order.status === 1 && !!order.verifyCode
    });
  },

  load: function (page) {
    const seq = (this.loadSeq || 0) + 1;
    this.loadSeq = seq;
    const that = this;
    const tab = this.data.tabs[this.data.activeTab];
    this.setData({ loading: true });
    api.seckill.orders(page, this.data.pageSize, tab.status)
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

  onTabChange: function (event) {
    const index = Number(event.currentTarget.dataset.index);
    if (index === this.data.activeTab) {
      return;
    }
    this.setData({ activeTab: index, list: [], finished: false });
    this.load(1);
  },

  pay: function (event) {
    const orderNo = event.currentTarget.dataset.orderno;
    const that = this;
    wx.showModal({
      title: '模拟支付',
      content: '确认支付该秒杀订单？',
      confirmText: '确认支付',
      success: function (res) {
        if (!res.confirm) {
          return;
        }
        api.seckill.pay(orderNo)
          .then(function () {
            wx.showToast({ title: '支付成功', icon: 'success' });
            that.load(1);
          })
          .catch(function () {});
      }
    });
  }
});
