import request from '@/utils/request'

export const getSeckillPage = (params: any) => {
  return request({
    url: '/seckill/page',
    method: 'get',
    params
  })
}

export const createSeckill = (data: any) => {
  return request({
    url: '/seckill',
    method: 'post',
    data
  })
}

export const updateSeckillStatus = (id: number, status: number) => {
  return request({
    url: `/seckill/status/${status}`,
    method: 'post',
    params: { id }
  })
}

export const getSeckillOrderPage = (params: any) => {
  return request({
    url: '/seckill/order/page',
    method: 'get',
    params
  })
}

export const verifySeckillOrder = (verifyCode: string) => {
  return request({
    url: '/seckill/order/verify',
    method: 'put',
    data: { verifyCode }
  })
}