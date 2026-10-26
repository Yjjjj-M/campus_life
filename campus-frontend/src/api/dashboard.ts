import request from './request'

export interface DashboardStats {
  shopCount: number
  userCount: number
  orderCount: number
  todayOrderCount: number
  todayRevenue: number
}

export function getDashboardStats() {
  return request.get<any, { code: number; data: DashboardStats; message: string }>('/admin/dashboard/stats')
}
