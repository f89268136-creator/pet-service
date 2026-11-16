const api = require('../../api/index');
const auth = require('../../utils/auth');
const fmt = require('../../utils/format');

Page({
  data: {
    id: null,
    isEdit: false,
    name: '',
    speciesOptions: [
      { value: 'DOG', label: '狗狗' },
      { value: 'CAT', label: '猫咪' },
      { value: 'OTHER', label: '其他' }
    ],
    speciesIndex: 0,
    breed: '',
    genderOptions: [
      { value: 0, label: '未知' },
      { value: 1, label: '公' },
      { value: 2, label: '母' }
    ],
    gender: 0,
    birthday: '',
    birthdayDisplay: '请选择生日（选填）',
    endDate: fmt.today(),
    weight: '',
    sterilized: false,
    vaccinated: false,
    note: '',
    avatar: '',
    saving: false
  },

  onLoad: function (options) {
    if (!auth.requireLogin()) {
      return;
    }
    if (options.id) {
      this.setData({ id: Number(options.id), isEdit: true });
      wx.setNavigationBarTitle({ title: '编辑宠物' });
      this.loadPet();
    } else {
      wx.setNavigationBarTitle({ title: '新增宠物' });
    }
  },

  loadPet: function () {
    const that = this;
    api.pet.list()
      .then(function (list) {
        const pet = (list || []).find(function (item) {
          return item.id === that.data.id;
        });
        if (!pet) {
          wx.showToast({ title: '宠物档案不存在', icon: 'none' });
          setTimeout(function () {
            wx.navigateBack();
          }, 800);
          return;
        }
        const speciesIndex = that.data.speciesOptions.findIndex(function (option) {
          return option.value === pet.species;
        });
        that.setData({
          name: pet.name || '',
          speciesIndex: speciesIndex >= 0 ? speciesIndex : 2,
          breed: pet.breed || '',
          gender: pet.gender || 0,
          birthday: pet.birthday || '',
          birthdayDisplay: pet.birthday || '请选择生日（选填）',
          weight: pet.weight ? String(pet.weight) : '',
          sterilized: pet.sterilized === 1,
          vaccinated: pet.vaccinated === 1,
          note: pet.note || '',
          avatar: pet.avatar || ''
        });
      })
      .catch(function () {});
  },

  onNameInput: function (event) {
    this.setData({ name: event.detail.value });
  },

  onSpeciesChange: function (event) {
    this.setData({ speciesIndex: Number(event.detail.value) });
  },

  onBreedInput: function (event) {
    this.setData({ breed: event.detail.value });
  },

  onGenderChange: function (event) {
    this.setData({ gender: Number(event.detail.value) });
  },

  onBirthdayChange: function (event) {
    this.setData({
      birthday: event.detail.value,
      birthdayDisplay: event.detail.value
    });
  },

  clearBirthday: function () {
    this.setData({
      birthday: '',
      birthdayDisplay: '请选择生日（选填）'
    });
  },

  onWeightInput: function (event) {
    this.setData({ weight: event.detail.value });
  },

  onSterilizedChange: function (event) {
    this.setData({ sterilized: event.detail.value });
  },

  onVaccinatedChange: function (event) {
    this.setData({ vaccinated: event.detail.value });
  },

  onNoteInput: function (event) {
    this.setData({ note: event.detail.value });
  },

  save: function () {
    if (this.data.saving) {
      return;
    }
    const name = (this.data.name || '').trim();
    if (!name) {
      wx.showToast({ title: '请输入宠物名字', icon: 'none' });
      return;
    }

    const payload = {
      name: name,
      species: this.data.speciesOptions[this.data.speciesIndex].value,
      breed: (this.data.breed || '').trim(),
      gender: this.data.gender,
      sterilized: this.data.sterilized ? 1 : 0,
      vaccinated: this.data.vaccinated ? 1 : 0,
      note: (this.data.note || '').trim()
    };
    if (this.data.avatar) {
      payload.avatar = this.data.avatar;
    }
    if (this.data.birthday) {
      payload.birthday = this.data.birthday;
    }
    const weight = parseFloat(this.data.weight);
    if (!isNaN(weight) && weight > 0) {
      payload.weight = weight;
    }

    const that = this;
    const action = this.data.isEdit
      ? api.pet.update(this.data.id, payload)
      : api.pet.add(payload);

    this.setData({ saving: true });
    action
      .then(function () {
        that.setData({ saving: false });
        wx.showToast({ title: '保存成功', icon: 'success' });
        setTimeout(function () {
          wx.navigateBack();
        }, 600);
      })
      .catch(function () {
        that.setData({ saving: false });
      });
  }
});
