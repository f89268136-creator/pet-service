import request from '@/utils/request'

export const getAppointmentPage = (params: any) => {
  return request({
    url: '/appointment/page',
    method: 'get',
    params
  })
}

export const getAppointmentDetail = (id: number) => {
  return request({
    url: `/appointment/${id}`,
    method: 'get'
  })
}

export const confirmAppointment = (id: number) => {
  return request({
    url: `/appointment/${id}/confirm`,
    method: 'put'
  })
}

export const verifyAppointment = (verifyCode: string) => {
  return request({
    url: '/appointment/verify',
    method: 'put',
    data: { verifyCode }
  })
}

export const completeAppointment = (id: number) => {
  return request({
    url: `/appointment/${id}/complete`,
    method: 'put'
  })
}

export const cancelAppointment = (id: number, cancelReason: string) => {
  return request({
    url: '/appointment/cancel',
    method: 'put',
    data: { id, cancelReason }
  })
}

export const getSlotList = (params: any) => {
  return request({
    url: '/appointment/slot/list',
    method: 'get',
    params
  })
}

export const createSlot = (data: any) => {
  return request({
    url: '/appointment/slot',
    method: 'post',
    data
  })
}

export const batchCreateSlots = (data: any) => {
  return request({
    url: '/appointment/slot/batch',
    method: 'post',
    data
  })
}

export const updateSlotStatus = (id: number, status: number) => {
  return request({
    url: `/appointment/slot/${id}/status`,
    method: 'put',
    params: { status }
  })
}
