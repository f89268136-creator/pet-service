const fmt = require('../../utils/format');

Page({
  data: {
    service: null
  },

  onLoad: function () {
    const service = wx.getStorageSync('currentService');
    if (!service) {
      wx.showToast({ title: '服务信息已失效', icon: 'none' });
      setTimeout(function () {
        wx.navigateBack();
      }, 800);
      return;
    }
    this.setData({
      service: Object.assign({}, service, {
        priceText: fmt.money(service.price),
        initial: fmt.initial(service.name, '服')
      })
    });
  },

  book: function () {
    const service = this.data.service;
    if (!service) {
      return;
    }
    wx.navigateTo({
      url: '/pages/appointment/submit?serviceId=' + service.id
    });
  }
});
