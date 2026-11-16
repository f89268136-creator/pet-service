import request from '@/utils/request'
/**
 *
 * 服务套餐管理
 *
 **/

// 查询列表数据
export const getServicePackagePage = (params: any) => {
  return request({
    url: '/service-package/page',
    method: 'get',
    params,
  },)
}

// 删除数据接口
export const deleteServicePackage = (ids: string) => {
  return request({
    url: '/service-package',
    method: 'delete',
    params: { ids }
  })
}

// 修改数据接口
export const editServicePackage = (params: any) => {
  return request({
    url: '/service-package',
    method: 'put',
    data: { ...params }
  })
}

// 新增数据接口
export const addServicePackage = (params: any) => {
  return request({
    url: '/service-package',
    method: 'post',
    data: { ...params }
  })
}

// 查询详情接口
export const queryServicePackageById = (id: string | (string | null)[]) => {
  return request({
    url: `/service-package/${id}`,
    method: 'get'
  })
}

// 批量起售禁售
export const servicePackageStatusByStatus = (params: any) => {
  return request({
    url: `/service-package/status/${params.status}`,
    method: 'post',
    params: { id: params.ids }
  })
}

// 服务套餐分类数据查询
export const servicePackageCategoryList = (params: any) => {
  return request({
    url: `/category/list`,
    method: 'get',
    params: { ...params }
  })
}
