import request from '@/utils/request'
/**
 *
 * 服务项目管理
 *
 **/
// 查询列表接口
export const getServiceItemPage = (params: any) => {
  return request({
    url: '/service-item/page',
    method: 'get',
    params
  })
}

// 删除接口
export const deleteServiceItem = (ids: string) => {
  return request({
    url: '/service-item',
    method: 'delete',
    params: { ids }
  })
}

// 修改接口
export const editServiceItem = (params: any) => {
  return request({
    url: '/service-item',
    method: 'put',
    data: { ...params }
  })
}

// 新增接口
export const addServiceItem = (params: any) => {
  return request({
    url: '/service-item',
    method: 'post',
    data: { ...params }
  })
}

// 查询详情
export const queryServiceItemById = (id: string | (string | null)[]) => {
  return request({
    url: `/service-item/${id}`,
    method: 'get'
  })
}

// 获取服务项目分类列表
export const getCategoryList = (params: any) => {
  return request({
    url: '/category/list',
    method: 'get',
    params
  })
}

// 查服务项目列表的接口
export const queryServiceItemList = (params: any) => {
  return request({
    url: '/service-item/list',
    method: 'get',
    params
  })
}

// 上架下架---批量上架下架接口
export const serviceItemStatusByStatus = (params: any) => {
  return request({
    url: `/service-item/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
  })
}

// 服务项目分类数据查询
export const serviceCategoryList = (params: any) => {
  return request({
    url: `/category/list`,
    method: 'get',
    params: { ...params }
  })
}
