import request from './request'

// 订单接口类型
export interface Order {
  id: string
  orderNo: string
  userId: string
  voucherId: string
  shopId: string
  status: number
  payValue: number
  actualValue: number
  payTime?: string
  useTime?: string
  cancelTime?: string
  createTime: string
  updateTime: string
  voucherTitle?: string
  shopName?: string
  userName?: string
  userPhone?: string
}

export interface OrderDetail {
  id: string
  orderId: string
  voucherTitle: string
  voucherSubTitle?: string
  shopName: string
  shopAddress: string
  payValue: number
  actualValue: number
  rules?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 用户端订单接口
export const userOrderApi = {
  // 创建订单
  createOrder: (voucherId: number) => {
    return request.post<Order>(`/user/order/create?voucherId=${voucherId}`)
  },

  // 支付订单
  payOrder: (orderId: string) => {
    return request.post<void>(`/user/order/${orderId}/pay`)
  },

  // 取消订单
  cancelOrder: (orderId: string) => {
    return request.post<void>(`/user/order/${orderId}/cancel`)
  },

  // 查询订单列表
  getOrders: (page: number, size: number, status?: number) => {
    const params: any = { page, size }
    if (status !== undefined) params.status = status
    return request.get<PageResult<Order>>('/user/order/page', { params })
  },

  // 查询订单详情
  getOrderDetail: (orderId: string) => {
    return request.get<Order>(`/user/order/${orderId}`)
  }
}

// 管理端订单接口
export const adminOrderApi = {
  // 分页查询订单
  getOrders: (page: number, size: number, status?: number, orderNo?: string, userPhone?: string) => {
    const params: any = { page, size }
    if (status !== undefined) params.status = status
    if (orderNo) params.orderNo = orderNo
    if (userPhone) params.userPhone = userPhone
    return request.get<PageResult<Order>>('/admin/order/page', { params })
  },

  // 查询订单详情
  getOrderDetail: (orderId: string) => {
    return request.get<Order>(`/admin/order/${orderId}`)
  },

  // 核销订单
  useOrder: (orderId: string) => {
    return request.post<void>(`/admin/order/${orderId}/use`)
  }
}
