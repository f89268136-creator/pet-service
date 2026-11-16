const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

Page({
  data: {
    mode: 'list',
    stores: [],
    nearby: [],
    loading: false,
    nearbyLoading: false
  },

  onShow: function () {
    if (!auth.requireLogin()) {
      return;
    }
    if (this.data.stores.length === 0) {
      this.loadStores();
    }
  },

  onPullDownRefresh: function () {
    if (this.data.mode === 'nearby') {
      this.loadNearby(true);
    } else {
      this.loadStores(true);
    }
  },

  decorateStore: function (store) {
    return Object.assign({}, store, {
      initial: fmt.initial(store.name, '店'),
      openLabel: fmt.timeLabel(store.openTime, store.closeTime),
      statusText: store.status === 1 ? '营业中' : '休息中'
    });
  },

  decorateNearby: function (store) {
    return Object.assign({}, store, {
      initial: fmt.initial(store.name, '店'),
      distanceText: fmt.describeDistance(store.distance)
    });
  },

  loadStores: function (fromPullDown) {
    const that = this;
    this.setData({ loading: true });
    api.store.list()
      .then(function (list) {
        that.setData({
          stores: (list || []).map(that.decorateStore),
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

  switchMode: function (event) {
    const mode = event.currentTarget.dataset.mode;
    if (mode === this.data.mode) {
      return;
    }
    this.setData({ mode: mode });
    if (mode === 'nearby' && !this.nearbyLoaded) {
      this.loadNearby();
    }
  },

  loadNearby: function (fromPullDown) {
    const that = this;
    this.setData({ nearbyLoading: true });
    wx.getLocation({
      type: 'gcj02',
      success: function (location) {
        api.store.nearby(location.longitude, location.latitude)
          .then(function (list) {
            that.nearbyLoaded = true;
            that.setData({
              nearby: (list || []).map(that.decorateNearby),
              nearbyLoading: false
            });
            if (fromPullDown) {
              wx.stopPullDownRefresh();
            }
          })
          .catch(function () {
            that.setData({ nearbyLoading: false });
            if (fromPullDown) {
              wx.stopPullDownRefresh();
            }
          });
      },
      fail: function () {
        that.setData({ nearbyLoading: false });
        if (fromPullDown) {
          wx.stopPullDownRefresh();
        }
        wx.showModal({
          title: '需要位置权限',
          content: '开启位置权限后即可查询距离您最近的门店。',
          confirmText: '去设置',
          success: function (res) {
            if (res.confirm) {
              wx.openSetting();
            }
          }
        });
      }
    });
  },

  callStore: function (event) {
    const phone = event.currentTarget.dataset.phone;
    if (!phone) {
      wx.showToast({ title: '门店未配置联系电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: String(phone) });
  },

  openLocation: function (event) {
    const dataset = event.currentTarget.dataset;
    if (dataset.lat === undefined || dataset.lng === undefined) {
      wx.showToast({ title: '门店位置信息缺失', icon: 'none' });
      return;
    }
    wx.openLocation({
      latitude: Number(dataset.lat),
      longitude: Number(dataset.lng),
      name: dataset.name || '宠物门店',
      address: dataset.address || '',
      scale: 16
    });
  }
});
