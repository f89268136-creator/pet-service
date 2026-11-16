const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

Page({
  data: {
    pets: [],
    loading: true
  },

  onShow: function () {
    if (!auth.requireLogin()) {
      return;
    }
    this.load();
  },

  onPullDownRefresh: function () {
    this.load(true);
  },

  decorate: function (pet) {
    return Object.assign({}, pet, {
      initial: fmt.initial(pet.name, '宠'),
      speciesText: fmt.speciesText(pet.species),
      genderText: fmt.genderText(pet.gender),
      breedText: pet.breed || '品种未填写',
      birthdayText: pet.birthday || '未填写',
      weightText: pet.weight ? pet.weight + ' kg' : '未填写',
      sterilizedText: pet.sterilized === 1 ? '已绝育' : '未绝育',
      vaccinatedText: pet.vaccinated === 1 ? '已免疫' : '未免疫'
    });
  },

  load: function (fromPullDown) {
    const that = this;
    this.setData({ loading: true });
    api.pet.list()
      .then(function (list) {
        that.setData({
          pets: (list || []).map(that.decorate),
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

  addPet: function () {
    wx.navigateTo({ url: '/pages/pet/edit' });
  },

  editPet: function (event) {
    wx.navigateTo({
      url: '/pages/pet/edit?id=' + event.currentTarget.dataset.id
    });
  },

  removePet: function (event) {
    const id = Number(event.currentTarget.dataset.id);
    const that = this;
    wx.showModal({
      title: '删除宠物档案',
      content: '删除后不可恢复，存在进行中的预约时无法删除。',
      confirmText: '删除',
      confirmColor: '#e95f3c',
      success: function (res) {
        if (!res.confirm) {
          return;
        }
        api.pet.remove(id)
          .then(function () {
            wx.showToast({ title: '已删除', icon: 'success' });
            that.load();
          })
          .catch(function () {});
      }
    });
  }
});
