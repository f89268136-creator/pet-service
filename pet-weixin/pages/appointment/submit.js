const api = require('../../api/index');
const fmt = require('../../utils/format');

Page({
  data: {
    service: null,
    stores: [],
    storeNames: [],
    storeIndex: 0,
    date: fmt.today(),
    minDate: fmt.today(),
    maxDate: fmt.addDays(30),
    slots: [],
    slotsLoading: false,
    selectedSlotId: null,
    pets: [],
    petIndex: -1,
    remark: '',
    submitting: false
  },

  onLoad: function (options) {
    this.serviceId = Number(options.serviceId);
    this.loadService();
    this.loadStores();
    this.loadPets();
  },

  onShow: function () {
    if (!this.firstShow) {
      this.firstShow = true;
      return;
    }
    this.loadPets(true);
  },

  loadService: function () {
    const that = this;
    const serviceId = this.serviceId;
    const cached = wx.getStorageSync('currentService');
    if (cached && cached.id === serviceId) {
      this.applyService(cached);
      return;
    }
    api.catalog.serviceItems()
      .then(function (list) {
        const item = (list || []).find(function (service) {
          return service.id === serviceId;
        });
        if (item) {
          that.applyService(item);
        } else {
          wx.showToast({ title: '服务项目不存在或已下架', icon: 'none' });
        }
      })
      .catch(function () {});
  },

  applyService: function (service) {
    this.setData({
      service: Object.assign({}, service, {
        initial: fmt.initial(service.name, '服'),
        priceText: fmt.money(service.price)
      })
    });
  },

  loadStores: function () {
    const that = this;
    api.store.list()
      .then(function (list) {
        const stores = list || [];
        if (!stores.length) {
          that.setData({ stores: [], storeNames: [], storeIndex: 0 });
          return;
        }
        that.setData({
          stores: stores,
          storeNames: stores.map(function (store) { return store.name; }),
          storeIndex: 0
        });
        that.loadSlots();
      })
      .catch(function () {});
  },

  loadSlots: function () {
    const store = this.data.stores[this.data.storeIndex];
    if (!store || !this.serviceId) {
      return;
    }
    const that = this;
    this.setData({ slotsLoading: true, selectedSlotId: null });
    api.appointment.slots(store.id, this.serviceId, this.data.date)
      .then(function (list) {
        const slots = (list || []).map(function (slot) {
          const remaining = slot.remaining !== null && slot.remaining !== undefined
            ? Number(slot.remaining)
            : Number(slot.capacity || 0) - Number(slot.booked || 0);
          return Object.assign({}, slot, {
            label: fmt.timeLabel(slot.startTime, slot.endTime),
            remaining: remaining,
            disabled: remaining <= 0
          });
        });
        that.setData({ slots: slots, slotsLoading: false });
      })
      .catch(function () {
        that.setData({ slots: [], slotsLoading: false });
      });
  },

  loadPets: function (preserve) {
    const that = this;
    const currentPetId = preserve && this.data.petIndex >= 0 && this.data.pets[this.data.petIndex]
      ? this.data.pets[this.data.petIndex].id
      : null;
    api.pet.list()
      .then(function (list) {
        const pets = (list || []).map(function (pet) {
          return Object.assign({}, pet, {
            speciesText: fmt.speciesText(pet.species),
            initial: fmt.initial(pet.name, '宠')
          });
        });
        let petIndex = pets.length ? 0 : -1;
        if (currentPetId) {
          const index = pets.findIndex(function (pet) { return pet.id === currentPetId; });
          if (index >= 0) {
            petIndex = index;
          }
        }
        that.setData({ pets: pets, petIndex: petIndex });
      })
      .catch(function () {});
  },

  onStoreChange: function (event) {
    this.setData({ storeIndex: Number(event.detail.value) });
    this.loadSlots();
  },

  onDateChange: function (event) {
    this.setData({ date: event.detail.value });
    this.loadSlots();
  },

  selectSlot: function (event) {
    if (event.currentTarget.dataset.disabled) {
      return;
    }
    this.setData({ selectedSlotId: Number(event.currentTarget.dataset.id) });
  },

  onPetChange: function (event) {
    this.setData({ petIndex: Number(event.detail.value) });
  },

  onRemarkInput: function (event) {
    this.setData({ remark: event.detail.value });
  },

  addPet: function () {
    wx.navigateTo({ url: '/pages/pet/edit' });
  },

  submit: function () {
    if (this.data.submitting) {
      return;
    }
    const store = this.data.stores[this.data.storeIndex];
    const pet = this.data.pets[this.data.petIndex];
    if (!this.data.service) {
      wx.showToast({ title: '服务信息加载失败', icon: 'none' });
      return;
    }
    if (!store) {
      wx.showToast({ title: '暂无可预约门店', icon: 'none' });
      return;
    }
    if (!this.data.selectedSlotId) {
      wx.showToast({ title: '请选择预约时段', icon: 'none' });
      return;
    }
    if (!pet) {
      wx.showToast({ title: '请先添加宠物档案', icon: 'none' });
      return;
    }

    const that = this;
    this.setData({ submitting: true });
    api.appointment.submit({
      storeId: store.id,
      serviceId: this.serviceId,
      slotId: this.data.selectedSlotId,
      petId: pet.id,
      remark: this.data.remark
    })
      .then(function (result) {
        that.setData({ submitting: false });
        wx.showModal({
          title: '预约提交成功',
          content: '订单金额 ¥' + fmt.money(result.orderAmount) + '，是否立即支付？',
          confirmText: '立即支付',
          cancelText: '稍后支付',
          success: function (res) {
            if (res.confirm) {
              api.appointment.pay(result.id)
                .then(function () {
                  wx.showToast({ title: '支付成功', icon: 'success' });
                  setTimeout(function () {
                    wx.redirectTo({ url: '/pages/appointment/detail?id=' + result.id });
                  }, 600);
                })
                .catch(function () {});
            } else {
              wx.redirectTo({ url: '/pages/appointment/detail?id=' + result.id });
            }
          }
        });
      })
      .catch(function () {
        that.setData({ submitting: false });
      });
  }
});
