const TOKEN_KEY = 'token';
const USER_KEY = 'user';
const PHONE_KEY = 'loginPhone';
const EMAIL_KEY = 'loginEmail';

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
function saveLogin(loginVO, account) {
  wx.setStorageSync(TOKEN_KEY, loginVO.token);
  const user = {
    id: loginVO.id,
    openid: loginVO.openid || '',
    phone: loginVO.phone || wx.getStorageSync(PHONE_KEY) || '',
    email: loginVO.email || account || ''
  };
  wx.setStorageSync(USER_KEY, user);
  if (account && account.indexOf('@') > 0) {
    wx.setStorageSync(EMAIL_KEY, account);
  }
  const app = getApp();
  if (app) {
    app.globalData.user = user;
  }
}

/**
 * 自动续期后更新本地令牌。
 */
function updateToken(token) {
  if (token) {
    wx.setStorageSync(TOKEN_KEY, token);
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
  updateToken,
  logout,
  requireLogin
};
