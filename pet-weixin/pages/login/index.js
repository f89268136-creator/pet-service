const api = require('../../api/index');
const auth = require('../../utils/auth');

const EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

Page({
  data: {
    email: '',
    code: '',
    wxLoading: false,
    logging: false,
    sending: false,
    countdown: 0
  },

  onLoad: function () {
    const user = auth.getUser();
    const cachedEmail = wx.getStorageSync('loginEmail');
    this.setData({ email: (user && user.email) || cachedEmail || '' });
  },

  onUnload: function () {
    this.clearCountdown();
  },

  onEmailInput: function (event) {
    this.setData({ email: event.detail.value });
  },

  onCodeInput: function (event) {
    this.setData({ code: event.detail.value });
  },

  sendCode: function () {
    if (this.data.sending || this.data.countdown > 0) {
      return;
    }
    const email = (this.data.email || '').trim();
    if (!EMAIL_PATTERN.test(email)) {
      wx.showToast({ title: '请输入正确的邮箱', icon: 'none' });
      return;
    }
    const that = this;
    this.setData({ sending: true });
    api.user.sendCode(email)
      .then(function (result) {
        that.startCountdown(60);
        if (result && result.devCode) {
          that.setData({ code: result.devCode });
          wx.showModal({
            title: '验证码已发送',
            content: '开发环境未开启邮件发送，验证码已自动填入：' + result.devCode,
            showCancel: false
          });
        } else {
          wx.showToast({ title: '验证码已发送至邮箱', icon: 'none' });
        }
      })
      .catch(function () {})
      .then(function () {
        that.setData({ sending: false });
      });
  },

  emailLogin: function () {
    if (this.data.logging || this.data.wxLoading) {
      return;
    }
    const email = (this.data.email || '').trim();
    const code = (this.data.code || '').trim();
    if (!EMAIL_PATTERN.test(email)) {
      wx.showToast({ title: '请输入正确的邮箱', icon: 'none' });
      return;
    }
    if (!/^\d{6}$/.test(code)) {
      wx.showToast({ title: '请输入6位验证码', icon: 'none' });
      return;
    }
    const that = this;
    this.setData({ logging: true });
    api.user.emailLogin(email, code)
      .then(function (data) {
        that.finishLogin(data, email);
      })
      .catch(function () {})
      .then(function () {
        that.setData({ logging: false });
      });
  },

  wxLogin: function () {
    if (this.data.wxLoading || this.data.logging) {
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
              content: '请确认小程序 appid 与后端配置一致；本地联调可改用邮箱验证码登录。',
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

  startCountdown: function (seconds) {
    const that = this;
    this.clearCountdown();
    this.setData({ countdown: seconds });
    this.countdownTimer = setInterval(function () {
      const next = that.data.countdown - 1;
      if (next <= 0) {
        that.clearCountdown();
        that.setData({ countdown: 0 });
      } else {
        that.setData({ countdown: next });
      }
    }, 1000);
  },

  clearCountdown: function () {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
      this.countdownTimer = null;
    }
  },

  finishLogin: function (loginVO, email) {
    auth.saveLogin(loginVO, email);
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