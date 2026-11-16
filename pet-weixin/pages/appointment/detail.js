const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

const SUBTITLES = {
  1: '请尽快完成支付，超时订单将自动取消',
  2: '门店确认后即可按时到店',
  3: '请按预约时段到店并出示核销码',
  4: '服务进行中，请耐心等待',
  5: '服务已完成，感谢您的信任',
  6: '订单已取消'
};

Page({
  data: {
    order: null,
    loading: true
  },

  onLoad: function (options) {
    this.orderId = Number(options.id);
  },

  onShow: function () {
    if (!auth.requireLogin()) {
      return;
    }
    this.load();
  },

  load: function () {
    const that = this;
    this.setData({ loading: true });
    api.appointment.detail(this.orderId)
      .then(function (order) {
        that.setData({
          order: Object.assign({}, order, {
            amountText: fmt.money(order.amount),
            statusClass: fmt.appointmentStatusClass(order.status),
            subtitle: SUBTITLES[order.status] || '',
            initial: fmt.initial(order.serviceName, '约'),
            payStatusText: { 0: '未支付', 1: '已支付', 2: '已退款' }[order.payStatus] || '未知',
            petText: [order.petName, fmt.speciesText(order.petSpecies)].filter(function (value) {
              return !!value;
            }).join(' · ') || '未关联宠物',
            showVerify: (order.status === 3 || order.status === 4) && !!order.verifyCode,
            canPay: order.status === 1 && order.payStatus === 0,
            canCancel: [1, 2, 3].indexOf(order.status) >= 0,
            canCall: !!order.storeId
          }),
          loading: false
        });
      })
      .catch(function () {
        that.setData({ loading: false });
      });
  },

  pay: function () {
    const that = this;
    wx.showModal({
      title: '模拟支付',
      content: '确认支付 ¥' + this.data.order.amountText + '？',
      confirmText: '确认支付',
      success: function (res) {
        if (!res.confirm) {
          return;
        }
        api.appointment.pay(that.orderId)
          .then(function () {
            wx.showToast({ title: '支付成功', icon: 'success' });
            that.load();
          })
          .catch(function () {});
      }
    });
  },

  cancel: function () {
    const that = this;
    wx.showModal({
      title: '取消预约',
      editable: true,
      placeholderText: '请输入取消原因（可选）',
      confirmText: '确认取消',
      success: function (res) {
        if (!res.confirm) {
          return;
        }
        api.appointment.cancel(that.orderId, res.content || '')
          .then(function () {
            wx.showToast({ title: '已取消预约', icon: 'success' });
            that.load();
          })
          .catch(function () {});
      }
    });
  },

  callStore: function () {
    const order = this.data.order;
    if (!order) {
      return;
    }
    wx.showLoading({ title: '获取门店电话', mask: true });
    api.store.detail(order.storeId)
      .then(function (store) {
        wx.hideLoading();
        if (store && store.phone) {
          wx.makePhoneCall({ phoneNumber: store.phone });
        } else {
          wx.showToast({ title: '门店未配置联系电话', icon: 'none' });
        }
      })
      .catch(function () {
        wx.hideLoading();
        wx.showToast({ title: '门店电话获取失败', icon: 'none' });
      });
  }
});
