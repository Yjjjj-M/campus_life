import request from './request'

export interface ShopCategory {
  id: number
  name: string
  icon: string
  sort: number
  status: number
}

export interface Shop {
  id?: number
  name: string
  categoryId: number
  categoryName?: string
  images: string
  area: string
  address: string
  longitude?: number
  latitude?: number
  avgPrice: number
  score: number
  sold: number
  status: number
  openHours: string
  createTime?: string
  updateTime?: string
}

export interface NearbyShop {
  shop: Shop
  distance: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// ==================== 用户端 API ====================

/** 用户端：查询分类列表 */
export function getShopCategoryList() {
  return request.get<any, { code: number; data: ShopCategory[]; message: string }>('/user/shop/category/list')
}

/** 用户端：分页查询营业中的商铺 */
export function getShopList(params: { page: number; size: number; categoryId?: number; name?: string }) {
  return request.get<any, { code: number; data: PageResult<Shop>; message: string }>('/user/shop/list', { params })
}

/** 用户端：查询商铺详情 */
export function getShopDetail(id: number) {
  return request.get<any, { code: number; data: Shop; message: string }>(`/user/shop/${id}`)
}

/** 用户端：查询附近商铺 */
export function getNearbyShops(params: { longitude: number; latitude: number; radius?: number }) {
  return request.get<any, { code: number; data: NearbyShop[]; message: string }>('/user/shop/nearby', { params })
}

// ==================== 管理端 API ====================

/** 管理端：分页查询所有商铺 */
export function getAdminShopPage(params: { page: number; size: number; name?: string; categoryId?: number }) {
  return request.get<any, { code: number; data: PageResult<Shop>; message: string }>('/admin/shop/page', { params })
}

/** 管理端：新增商铺 */
export function addShop(data: Shop) {
  return request.post<any, { code: number; message: string }>('/admin/shop', data)
}

/** 管理端：修改商铺 */
export function updateShop(data: Shop) {
  return request.put<any, { code: number; message: string }>('/admin/shop', data)
}

/** 管理端：删除商铺 */
export function deleteShop(id: number) {
  return request.delete<any, { code: number; message: string }>(`/admin/shop/${id}`)
}

/** 管理端：查询所有商铺（用于下拉选择） */
export function getAllShops() {
  return request.get<any, { code: number; data: Shop[]; message: string }>('/admin/shop/all')
}
