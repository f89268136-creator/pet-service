function pad(value) {
  return String(value).padStart(2, '0');
}

function formatDate(date) {
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate());
}

function today() {
  return formatDate(new Date());
}

function addDays(days) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return formatDate(date);
}

function money(value) {
  const number = Number(value);
  if (isNaN(number)) {
    return '0.00';
  }
  return number.toFixed(2);
}

function timeLabel(start, end) {
  const from = (start || '').slice(0, 5);
  const to = (end || '').slice(0, 5);
  return from + '-' + to;
}

function describeDistance(distance) {
  if (distance === null || distance === undefined) {
    return '';
  }
  const meters = Number(distance);
  if (isNaN(meters)) {
    return '';
  }
  if (meters < 1000) {
    return Math.round(meters) + 'm';
  }
  return (meters / 1000).toFixed(1) + 'km';
}

const APPOINTMENT_STATUS_CLASS = {
  1: 'warn',
  2: 'info',
  3: 'primary',
  4: 'primary',
  5: 'success',
  6: 'muted'
};

const SECKILL_STATUS_CLASS = {
  0: 'warn',
  1: 'primary',
  2: 'success',
  3: 'muted'
};

function speciesText(species) {
  if (species === 'DOG') {
    return '狗狗';
  }
  if (species === 'CAT') {
    return '猫咪';
  }
  if (species === 'OTHER') {
    return '其他';
  }
  return species || '';
}

function genderText(gender) {
  if (gender === 1) {
    return '公';
  }
  if (gender === 2) {
    return '母';
  }
  return '未知';
}

function initial(name, fallback) {
  const text = (name || '').trim();
  return text ? text.slice(0, 1) : (fallback || '宠');
}

module.exports = {
  today: today,
  addDays: addDays,
  money: money,
  timeLabel: timeLabel,
  describeDistance: describeDistance,
  appointmentStatusClass: function (status) {
    return APPOINTMENT_STATUS_CLASS[status] || '';
  },
  seckillStatusClass: function (status) {
    return SECKILL_STATUS_CLASS[status] || '';
  },
  speciesText: speciesText,
  genderText: genderText,
  initial: initial
};
