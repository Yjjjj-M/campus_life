import request from './request'

/**
 * 商铺分类类型
 */
export interface ShopCategory {
  id?: number
  name: string
  icon: string
  sort: number
  status: number
  createTime?: string
  updateTime?: string
}

/**
 * 用户端：查询所有启用的分类列表
 */
export const getUserCategoryList = () => {
  return request.get<any, { code: number; data: ShopCategory[]; message: string }>('/user/shop/category/list')
}

/**
 * 管理端：分页查询分类列表
 */
export const getAdminCategoryPage = (page: number, size: number) => {
  return request.get<any, { code: number; data: { records: ShopCategory[]; total: number; page: number; size: number }; message: string }>('/admin/category/page', {
    params: { page, size }
  })
}

/**
 * 管理端：新增分类
 */
export const addCategory = (data: ShopCategory) => {
  return request.post<any, { code: number; message: string }>('/admin/category', data)
}

/**
 * 管理端：修改分类
 */
export const updateCategory = (data: ShopCategory) => {
  return request.put<any, { code: number; message: string }>('/admin/category', data)
}

/**
 * 管理端：删除分类
 */
export const deleteCategory = (id: number) => {
  return request.delete<any, { code: number; message: string }>(`/admin/category/${id}`)
}
