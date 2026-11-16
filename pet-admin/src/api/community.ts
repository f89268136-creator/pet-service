import request from '@/utils/request'

export const getCommunityPage = (params: any) => {
  return request({
    url: '/community/page',
    method: 'get',
    params
  })
}

export const updateCommunityStatus = (id: number, status: number) => {
  return request({
    url: `/community/status/${status}`,
    method: 'post',
    params: { id }
  })
}