const api = require('../../api/index');
const auth = require('../../utils/auth');

Page({
  data: {
    wxLoading: false
  },

  wxLogin: function () {
    if (this.data.wxLoading) {
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
            that.finishLogin(data);
          })
          .catch(function () {
            wx.showModal({
              title: '微信登录失败',
              content: '请确认小程序 appid 与后端 pet.wechat 配置一致后重试。',
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

  finishLogin: function (loginVO) {
    auth.saveLogin(loginVO);
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
