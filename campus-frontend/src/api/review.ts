import request from './request'

export interface Review {
  id?: number
  userId?: number
  shopId: number
  orderId?: number
  rating: number
  content: string
  images?: string
  likeCount?: number
  status?: number
  createTime?: string
  updateTime?: string
  
  // 展示字段
  userNickname?: string
  userAvatar?: string
  shopName?: string
  hasLiked?: boolean
  isFollowed?: boolean
}

export interface ReviewPageResult {
  records: Review[]
  total: number
  hasMore?: boolean
  nextCursor?: number
}

// 用户端 API
export const userReviewApi = {
  // 发布评价
  publish: (review: Review) => {
    return request.post<Review>('/user/review', review)
  },
  
  // 查看商铺的评价列表（游标分页）
  getReviewsByShop: (shopId: number, cursor?: number, size: number = 10) => {
    return request.get<ReviewPageResult>(`/user/review/shop/${shopId}`, {
      params: { cursor, size }
    })
  },
  
  // 点赞
  like: (reviewId: number) => {
    return request.post(`/user/review/${reviewId}/like`)
  },
  
  // 取消点赞
  unlike: (reviewId: number) => {
    return request.delete(`/user/review/${reviewId}/like`)
  },
  
  // 查看我的评价
  getMyReviews: (page: number = 1, size: number = 10) => {
    return request.get<ReviewPageResult>('/user/review/my', {
      params: { page, size }
    })
  },
  
  // 查看评价详情
  getReviewDetail: (reviewId: number) => {
    return request.get<Review>(`/user/review/${reviewId}`)
  }
}

// 管理端 API
export const adminReviewApi = {
  // 查看所有评价
  getAllReviews: (page: number = 1, size: number = 10, shopId?: number) => {
    return request.get<ReviewPageResult>('/admin/review', {
      params: { page, size, shopId }
    })
  },
  
  // 删除评价
  delete: (reviewId: number) => {
    return request.delete(`/admin/review/${reviewId}`)
  }
}
