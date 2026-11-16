const request = require('../utils/request');

function withQuery(url, params) {
  const query = Object.keys(params || {})
    .filter(function (key) {
      return params[key] !== undefined && params[key] !== null && params[key] !== '';
    })
    .map(function (key) {
      return encodeURIComponent(key) + '=' + encodeURIComponent(params[key]);
    })
    .join('&');
  return query ? url + '?' + query : url;
}

module.exports = {
  user: {
    wxLogin: function (code) {
      return request({ url: '/user/user/login', method: 'POST', data: { code: code } });
    },
    devLogin: function (phone) {
      return request({ url: '/user/user/dev-login', method: 'POST', data: { phone: phone } });
    }
  },

  catalog: {
    categories: function (type) {
      return request({ url: '/user/category/list', data: { type: type } });
    },
    serviceItems: function (categoryId) {
      return request({
        url: '/user/service-item/list',
        data: categoryId ? { categoryId: categoryId } : {}
      });
    },
    packages: function (categoryId) {
      return request({
        url: '/user/service-package/list',
        data: categoryId ? { categoryId: categoryId } : {}
      });
    },
    packageItems: function (id) {
      return request({ url: '/user/service-package/items/' + id });
    }
  },

  store: {
    shopStatus: function () {
      return request({ url: '/user/shop/status', silent: true });
    },
    list: function () {
      return request({ url: '/user/store/list' });
    },
    detail: function (id) {
      return request({ url: '/user/store/' + id, silent: true });
    },
    nearby: function (longitude, latitude, radius, limit) {
      return request({
        url: '/user/store/nearby',
        data: {
          longitude: longitude,
          latitude: latitude,
          radius: radius || 5,
          limit: limit || 10
        }
      });
    }
  },

  appointment: {
    slots: function (storeId, serviceId, date) {
      return request({
        url: '/user/appointment/slots',
        data: { storeId: storeId, serviceId: serviceId, date: date }
      });
    },
    submit: function (data) {
      return request({ url: '/user/appointment/submit', method: 'POST', data: data });
    },
    pay: function (id) {
      return request({ url: '/user/appointment/' + id + '/pay', method: 'POST' });
    },
    page: function (page, pageSize, status) {
      return request({
        url: '/user/appointment/page',
        data: { page: page, pageSize: pageSize, status: status }
      });
    },
    detail: function (id) {
      return request({ url: '/user/appointment/' + id });
    },
    cancel: function (id, reason) {
      return request({
        url: withQuery('/user/appointment/' + id + '/cancel', { reason: reason }),
        method: 'PUT'
      });
    }
  },

  pet: {
    list: function () {
      return request({ url: '/user/pet/list' });
    },
    add: function (data) {
      return request({ url: '/user/pet', method: 'POST', data: data });
    },
    update: function (id, data) {
      return request({ url: '/user/pet/' + id, method: 'PUT', data: data });
    },
    remove: function (id) {
      return request({ url: '/user/pet/' + id, method: 'DELETE' });
    }
  },

  community: {
    feed: function (sort, page, pageSize) {
      return request({
        url: '/user/community/feed',
        data: { sort: sort, page: page, pageSize: pageSize }
      });
    },
    publish: function (data) {
      return request({ url: '/user/community', method: 'POST', data: data });
    },
    detail: function (id) {
      return request({ url: '/user/community/' + id });
    },
    like: function (id) {
      return request({ url: '/user/community/' + id + '/like', method: 'POST' });
    },
    comments: function (id) {
      return request({ url: '/user/community/' + id + '/comments' });
    },
    comment: function (id, content) {
      return request({
        url: '/user/community/' + id + '/comment',
        method: 'POST',
        data: { content: content }
      });
    }
  },

  seckill: {
    list: function () {
      return request({ url: '/user/seckill/list' });
    },
    grab: function (id) {
      return request({ url: '/user/seckill/' + id + '/grab', method: 'POST' });
    },
    orders: function (page, pageSize, status) {
      return request({
        url: '/user/seckill/order/page',
        data: { page: page, pageSize: pageSize, status: status }
      });
    },
    pay: function (orderNo) {
      return request({ url: '/user/seckill/order/' + orderNo + '/pay', method: 'POST' });
    }
  },

  signin: {
    stats: function () {
      return request({ url: '/user/signin/stats' });
    },
    sign: function () {
      return request({ url: '/user/signin', method: 'POST' });
    }
  }
};
