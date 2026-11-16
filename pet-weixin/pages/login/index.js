const api = require('../../api/index');
const auth = require('../../utils/auth');

Page({
  data: {
    phone: '',
    wxLoading: false,
    devLoading: false
  },

  onLoad: function () {
    const user = auth.getUser();
    if (user && user.phone) {
      this.setData({ phone: user.phone });
    }
  },

  onPhoneInput: function (event) {
    this.setData({ phone: event.detail.value });
  },

  wxLogin: function () {
    if (this.data.wxLoading || this.data.devLoading) {
      return;
    }
    const that = this;
    this.setData({ wxLoading: true });
    wx.login({
      success: function (res) {
        if (!res.code) {
          wx.showToast({ title: '未获取到微信登录凭证', icon: 'none' });
          that.setData({ wxLoading: false });
          return;
        }
        api.user.wxLogin(res.code)
          .then(function (data) {
            that.finishLogin(data, '');
          })
          .catch(function () {
            wx.showModal({
              title: '微信登录失败',
              content: '请确认小程序 appid 与后端配置一致；本地联调可改用下方的演示手机号登录。',
              showCancel: false
            });
          })
          .then(function () {
            that.setData({ wxLoading: false });
          });
      },
      fail: function () {
        wx.showToast({ title: '微信登录调用失败', icon: 'none' });
        that.setData({ wxLoading: false });
      }
    });
  },

  devLogin: function () {
    if (this.data.wxLoading || this.data.devLoading) {
      return;
    }
    const phone = (this.data.phone || '').trim();
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入11位手机号', icon: 'none' });
      return;
    }
    const that = this;
    this.setData({ devLoading: true });
    api.user.devLogin(phone)
      .then(function (data) {
        that.finishLogin(data, phone);
      })
      .catch(function () {})
      .then(function () {
        that.setData({ devLoading: false });
      });
  },

  finishLogin: function (loginVO, phone) {
    auth.saveLogin(loginVO, phone);
    wx.showToast({ title: '登录成功', icon: 'success' });
    setTimeout(function () {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        wx.navigateBack();
      } else {
        wx.switchTab({ url: '/pages/index/index' });
      }
    }, 600);
  }
});
