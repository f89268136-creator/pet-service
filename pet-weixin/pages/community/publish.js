const api = require('../../api/index');
const auth = require('../../utils/auth');

Page({
  data: {
    content: '',
    petOptions: [{ id: null, name: '不关联宠物' }],
    petIndex: 0,
    submitting: false
  },

  onLoad: function () {
    if (!auth.requireLogin()) {
      return;
    }
    this.loadPets();
  },

  loadPets: function () {
    const that = this;
    api.pet.list()
      .then(function (pets) {
        const options = [{ id: null, name: '不关联宠物' }].concat((pets || []).map(function (pet) {
          return { id: pet.id, name: pet.name };
        }));
        that.setData({ petOptions: options });
      })
      .catch(function () {});
  },

  onContentInput: function (event) {
    this.setData({ content: event.detail.value });
  },

  onPetChange: function (event) {
    this.setData({ petIndex: Number(event.detail.value) });
  },

  publish: function () {
    if (this.data.submitting) {
      return;
    }
    const content = (this.data.content || '').trim();
    if (!content) {
      wx.showToast({ title: '请输入动态内容', icon: 'none' });
      return;
    }
    const that = this;
    const pet = this.data.petOptions[this.data.petIndex];
    this.setData({ submitting: true });
    api.community.publish({
      content: content,
      petId: pet ? pet.id : null,
      images: []
    })
      .then(function () {
        that.setData({ submitting: false });
        wx.showToast({ title: '发布成功', icon: 'success' });
        setTimeout(function () {
          wx.navigateBack();
        }, 600);
      })
      .catch(function () {
        that.setData({ submitting: false });
      });
  }
});
