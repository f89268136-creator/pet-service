const TOKEN_KEY = 'token';
const USER_KEY = 'user';
const PHONE_KEY = 'loginPhone';

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || '';
}

function isLoggedIn() {
  return !!getToken();
}

function getUser() {
  return wx.getStorageSync(USER_KEY) || null;
}

/**
 * 登录成功后统一保存令牌与用户信息。
 * 后端用户端令牌请求头为 authentication，由 request.js 自动携带。
 */
function saveLogin(loginVO, phone) {
  wx.setStorageSync(TOKEN_KEY, loginVO.token);
  const user = {
    id: loginVO.id,
    openid: loginVO.openid || '',
    phone: phone || wx.getStorageSync(PHONE_KEY) || ''
  };
  wx.setStorageSync(USER_KEY, user);
  if (phone) {
    wx.setStorageSync(PHONE_KEY, phone);
  }
  const app = getApp();
  if (app) {
    app.globalData.user = user;
  }
}

function logout() {
  wx.removeStorageSync(TOKEN_KEY);
  wx.removeStorageSync(USER_KEY);
  const app = getApp();
  if (app) {
    app.globalData.user = null;
  }
}

/**
 * 页面进入前调用：未登录时跳转登录页并返回 false。
 */
function requireLogin() {
  if (isLoggedIn()) {
    return true;
  }
  wx.navigateTo({ url: '/pages/login/index' });
  return false;
}

module.exports = {
  getToken,
  isLoggedIn,
  getUser,
  saveLogin,
  logout,
  requireLogin
};
