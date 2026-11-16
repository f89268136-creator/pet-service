const auth = require('./utils/auth');

App({
  globalData: {
    user: null
  },
  onLaunch: function () {
    if (auth.isLoggedIn()) {
      this.globalData.user = auth.getUser();
    }
  }
});
