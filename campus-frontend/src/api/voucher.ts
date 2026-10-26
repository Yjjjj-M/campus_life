import request from './request'

export interface Voucher {
  id?: number
  shopId: number
  shopName?: string
  title: string
  subTitle?: string
  rules?: string
  payValue: number
  actualValue: number
  type: number // 0=普通券, 1=秒杀券
  status: number // 1=上架, 2=下架, 3=过期
  stock: number
  beginTime?: string
  endTime?: string
  createTime?: string
  updateTime?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 管理端：分页查询优惠券
export const getAdminVoucherPage = (params: {
  page: number
  size: number
  shopId?: number
  type?: number
  status?: number
}) => {
  return request.get<any, { code: number; data: PageResult<Voucher>; message: string }>(
    '/admin/voucher/page',
    { params }
  )
}

// 管理端：创建优惠券
export const createVoucher = (data: Voucher) => {
  return request.post<any, { code: number; message: string }>('/admin/voucher', data)
}

// 管理端：修改优惠券
export const updateVoucher = (data: Voucher) => {
  return request.put<any, { code: number; message: string }>('/admin/voucher', data)
}

// 管理端：删除优惠券
export const deleteVoucher = (id: number) => {
  return request.delete<any, { code: number; message: string }>(`/admin/voucher/${id}`)
}

// 管理端：修改优惠券状态（上架/下架）
export const updateVoucherStatus = (id: number, status: number) => {
  return request.put<any, { code: number; message: string }>(
    `/admin/voucher/${id}/status`,
    null,
    { params: { status } }
  )
}

// 用户端：查询商铺的优惠券列表
export const getUserVoucherList = (shopId: number) => {
  return request.get<any, { code: number; data: Voucher[]; message: string }>(
    '/user/voucher/list',
    { params: { shopId } }
  )
}

// 用户端：查询优惠券详情
export const getUserVoucherDetail = (id: number) => {
  return request.get<any, { code: number; data: Voucher; message: string }>(
    `/user/voucher/${id}`
  )
}
