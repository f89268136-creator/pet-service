const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

Page({
  data: {
    loading: true,
    shopStatus: null,
    store: null,
    categories: [],
    activeCategoryId: null,
    serviceItems: [],
    itemsLoading: false,
    packages: [],
    packageModal: {
      visible: false,
      name: '',
      items: []
    },
    quickEntries: [
      { label: '我的预约', desc: '查看服务进度', icon: '约', url: '/pages/appointment/list', tone: 'tone-a' },
      { label: '宠物档案', desc: '管理毛孩子', icon: '宠', url: '/pages/pet/list', tone: 'tone-b' },
      { label: '限时秒杀', desc: '套餐特惠', icon: '秒', url: '/pages/seckill/index', tone: 'tone-c' },
      { label: '附近门店', desc: '找最近门店', icon: '店', url: '/pages/store/list', tone: 'tone-d' }
    ]
  },

  onShow: function () {
    if (!auth.requireLogin()) {
      return;
    }
    this.loadAll();
  },

  onPullDownRefresh: function () {
    const that = this;
    this.loadAll(function () {
      wx.stopPullDownRefresh();
    });
  },

  loadAll: function (done) {
    const that = this;
    const categoryId = this.data.activeCategoryId;
    this.setData({ loading: true });

    Promise.all([
      api.store.shopStatus().catch(function () { return null; }),
      api.store.list().catch(function () { return []; }),
      api.catalog.categories(1).catch(function () { return []; }),
      api.catalog.serviceItems(categoryId).catch(function () { return []; }),
      api.catalog.packages().catch(function () { return []; })
    ]).then(function (results) {
      const stores = results[1] || [];
      const store = stores.length ? that.decorateStore(stores[0]) : null;
      const update = {
        shopStatus: results[0],
        store: store,
        categories: results[2] || [],
        packages: (results[4] || []).map(that.decoratePackage),
        loading: false
      };
      if (that.data.activeCategoryId === categoryId) {
        update.serviceItems = (results[3] || []).map(that.decorateService);
      }
      that.setData(update);
      if (done) {
        done();
      }
    });
  },

  decorateStore: function (store) {
    return Object.assign({}, store, {
      openLabel: fmt.timeLabel(store.openTime, store.closeTime),
      initial: fmt.initial(store.name, '店')
    });
  },

  decorateService: function (item) {
    return Object.assign({}, item, {
      initial: fmt.initial(item.name, '服'),
      priceText: fmt.money(item.price)
    });
  },

  decoratePackage: function (item) {
    return Object.assign({}, item, {
      initial: fmt.initial(item.name, '套'),
      priceText: fmt.money(item.price)
    });
  },

  onCategoryTap: function (event) {
    const rawId = event.currentTarget.dataset.id;
    const categoryId = rawId ? Number(rawId) : null;
    if (categoryId === this.data.activeCategoryId) {
      return;
    }
    const seq = (this.itemSeq || 0) + 1;
    this.itemSeq = seq;
    const that = this;
    this.setData({ activeCategoryId: categoryId, itemsLoading: true });
    api.catalog.serviceItems(categoryId)
      .then(function (list) {
        if (seq !== that.itemSeq) {
          return;
        }
        that.setData({
          serviceItems: (list || []).map(that.decorateService),
          itemsLoading: false
        });
      })
      .catch(function () {
        if (seq !== that.itemSeq) {
          return;
        }
        that.setData({ itemsLoading: false });
      });
  },

  openService: function (event) {
    const id = Number(event.currentTarget.dataset.id);
    const item = this.data.serviceItems.find(function (service) {
      return service.id === id;
    });
    if (!item) {
      return;
    }
    wx.setStorageSync('currentService', item);
    wx.navigateTo({ url: '/pages/service/detail' });
  },

  openPackage: function (event) {
    const id = event.currentTarget.dataset.id;
    const name = event.currentTarget.dataset.name || '服务套餐';
    const that = this;
    wx.showLoading({ title: '加载中', mask: true });
    api.catalog.packageItems(id)
      .then(function (items) {
        that.setData({
          packageModal: {
            visible: true,
            name: name,
            items: items || []
          }
        });
      })
      .catch(function () {})
      .then(function () {
        wx.hideLoading();
      });
  },

  closePackage: function () {
    this.setData({
      packageModal: { visible: false, name: '', items: [] }
    });
  },

  goPage: function (event) {
    const url = event.currentTarget.dataset.url;
    const tabPages = [
      '/pages/index/index',
      '/pages/appointment/list',
      '/pages/community/index',
      '/pages/mine/index'
    ];
    if (tabPages.indexOf(url) >= 0) {
      wx.switchTab({ url: url });
    } else {
      wx.navigateTo({ url: url });
    }
  },

  noop: function () {}
});
