const config = require('../config');

let redirectingToLogin = false;

function goLogin() {
  if (redirectingToLogin) {
    return;
  }
  redirectingToLogin = true;
  wx.removeStorageSync('token');
  wx.removeStorageSync('user');
  const app = getApp();
  if (app) {
    app.globalData.user = null;
  }
  wx.reLaunch({
    url: '/pages/login/index',
    complete() {
      setTimeout(function () {
        redirectingToLogin = false;
      }, 800);
    }
  });
}

/**
 * 统一请求封装：
 * - 自动拼接 baseUrl、携带用户端 JWT（authentication 头）
 * - 按后端 Result 结构解包：code === 1 取 data，否则提示 msg
 * - 401 统一清理登录态并回到登录页
 */
function request(options) {
  const token = wx.getStorageSync('token');
  const header = Object.assign(
    { 'content-type': 'application/json' },
    options.header || {}
  );
  if (token) {
    header.authentication = token;
  }

  if (options.loading) {
    wx.showLoading({ title: '加载中', mask: true });
  }

  return new Promise(function (resolve, reject) {
    wx.request({
      url: config.baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      timeout: 15000,
      success: function (res) {
        if (res.statusCode === 401) {
          goLogin();
          reject({ code: 401, msg: '登录已过期' });
          return;
        }
        if (res.statusCode !== 200) {
          const msg = '服务异常（' + res.statusCode + '）';
          if (!options.silent) {
            wx.showToast({ title: msg, icon: 'none' });
          }
          reject({ code: res.statusCode, msg: msg });
          return;
        }
        const body = res.data || {};
        if (body.code === 1) {
          resolve(body.data);
          return;
        }
        if (!options.silent) {
          wx.showToast({ title: body.msg || '操作失败', icon: 'none' });
        }
        reject(body);
      },
      fail: function (err) {
        if (!options.silent) {
          wx.showToast({ title: '网络连接失败', icon: 'none' });
        }
        reject(err);
      },
      complete: function () {
        if (options.loading) {
          wx.hideLoading();
        }
      }
    });
  });
}

module.exports = request;
