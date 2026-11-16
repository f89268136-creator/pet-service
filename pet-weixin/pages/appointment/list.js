const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

Page({
  data: {
    tabs: [
      { label: '全部', status: null },
      { label: '待支付', status: 1 },
      { label: '待确认', status: 2 },
      { label: '待服务', status: 3 },
      { label: '服务中', status: 4 },
      { label: '已完成', status: 5 },
      { label: '已取消', status: 6 }
    ],
    activeTab: 0,
    list: [],
    page: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    finished: false
  },

  onShow: function () {
    if (!auth.requireLogin()) {
      return;
    }
    this.load(1);
  },

  onPullDownRefresh: function () {
    this.load(1);
  },

  onReachBottom: function () {
    if (!this.data.finished && !this.data.loading) {
      this.load(this.data.page + 1);
    }
  },

  decorate: function (item) {
    const petLine = [item.petName, item.petSpecies].filter(function (value) {
      return !!value;
    }).join(' · ');
    return Object.assign({}, item, {
      amountText: fmt.money(item.amount),
      statusClass: fmt.appointmentStatusClass(item.status),
      initial: fmt.initial(item.serviceName, '约'),
      petLine: petLine || '未关联宠物'
    });
  },

  load: function (page) {
    const seq = (this.loadSeq || 0) + 1;
    this.loadSeq = seq;
    const that = this;
    const tab = this.data.tabs[this.data.activeTab];
    this.setData({ loading: true });

    api.appointment.page(page, this.data.pageSize, tab.status)
      .then(function (result) {
        if (seq !== that.loadSeq) {
          return;
        }
        const records = (result.records || []).map(that.decorate);
        const list = page === 1 ? records : that.data.list.concat(records);
        const total = result.total || 0;
        that.setData({
          list: list,
          page: page,
          total: total,
          loading: false,
          finished: page * that.data.pageSize >= total
        });
        wx.stopPullDownRefresh();
      })
      .catch(function () {
        if (seq !== that.loadSeq) {
          return;
        }
        that.setData({ loading: false });
        wx.stopPullDownRefresh();
      });
  },

  onTabChange: function (event) {
    const index = Number(event.currentTarget.dataset.index);
    if (index === this.data.activeTab) {
      return;
    }
    this.setData({ activeTab: index, list: [], finished: false });
    this.load(1);
  },

  openDetail: function (event) {
    wx.navigateTo({
      url: '/pages/appointment/detail?id=' + event.currentTarget.dataset.id
    });
  },

  cancelOrder: function (event) {
    const id = Number(event.currentTarget.dataset.id);
    const that = this;
    wx.showModal({
      title: '取消预约',
      editable: true,
      placeholderText: '请输入取消原因（可选）',
      confirmText: '确认取消',
      success: function (res) {
        if (!res.confirm) {
          return;
        }
        api.appointment.cancel(id, res.content || '')
          .then(function () {
            wx.showToast({ title: '已取消预约', icon: 'success' });
            that.load(1);
          })
          .catch(function () {});
      }
    });
  }
});
