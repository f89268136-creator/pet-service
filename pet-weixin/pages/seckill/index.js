const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

Page({
  data: {
    list: [],
    loading: true,
    grabbingId: 0
  },

  onShow: function () {
    if (!auth.requireLogin()) {
      return;
    }
    this.load();
  },

  onPullDownRefresh: function () {
    this.load(true);
  },

  decorate: function (activity) {
    const stock = Number(activity.stock || 0);
    const remain = Number(activity.remain || 0);
    const sold = Math.max(0, stock - remain);
    const percent = stock > 0 ? Math.round((sold / stock) * 100) : 0;
    return Object.assign({}, activity, {
      originalText: fmt.money(activity.originalPrice),
      seckillText: fmt.money(activity.seckillPrice),
      remain: remain,
      stock: stock,
      soldPercent: percent,
      timeText: (activity.beginTime || '').slice(5) + ' 至 ' + (activity.endTime || '').slice(5),
      initial: fmt.initial(activity.setmealName, '秒'),
      disabled: remain <= 0
    });
  },

  load: function (fromPullDown) {
    const that = this;
    this.setData({ loading: true });
    api.seckill.list()
      .then(function (list) {
        that.setData({
          list: (list || []).map(that.decorate),
          loading: false
        });
        if (fromPullDown) {
          wx.stopPullDownRefresh();
        }
      })
      .catch(function () {
        that.setData({ loading: false });
        if (fromPullDown) {
          wx.stopPullDownRefresh();
        }
      });
  },

  grab: function (event) {
    if (this.data.grabbingId) {
      return;
    }
    const id = Number(event.currentTarget.dataset.id);
    const that = this;
    this.setData({ grabbingId: id });
    api.seckill.grab(id)
      .then(function (result) {
        that.setData({ grabbingId: 0 });
        wx.showModal({
          title: '抢购成功',
          content: '订单号：' + result.orderNo + '\n状态：' + (result.statusText || '排队中') +
            '\n订单生成后可在“我的秒杀订单”中完成支付。',
          confirmText: '查看订单',
          cancelText: '继续逛逛',
          success: function (res) {
            if (res.confirm) {
              wx.navigateTo({ url: '/pages/seckill/order' });
            }
          }
        });
        that.load();
      })
      .catch(function () {
        that.setData({ grabbingId: 0 });
      });
  },

  goOrders: function () {
    wx.navigateTo({ url: '/pages/seckill/order' });
  }
});
