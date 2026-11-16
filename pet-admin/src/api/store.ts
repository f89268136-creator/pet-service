import request from '@/utils/request'

export const getStoreList = () => {
  return request({
    url: '/store/list',
    method: 'get'
  })
}

export const updateStore = (data: any) => {
  return request({
    url: '/store',
    method: 'put',
    data
  })
}

export const syncStoreGeo = () => {
  return request({
    url: '/store/sync-geo',
    method: 'post'
  })
}