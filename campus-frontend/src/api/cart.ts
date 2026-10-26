import request from './request'

export interface CartItem {
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  subtotal: number
  stock: number
}

export interface CartData {
  items: CartItem[]
  totalPrice: number
  totalCount: number
}

export const cartApi = {
  // 添加商品到购物车
  addToCart: (productId: number, quantity: number = 1) => {
    return request.post('/user/cart/add', null, { params: { productId, quantity } })
  },

  // 更新购物车商品数量
  updateCartItem: (productId: number, quantity: number) => {
    return request.put('/user/cart/update', null, { params: { productId, quantity } })
  },

  // 删除购物车商品
  removeFromCart: (productId: number) => {
    return request.delete(`/user/cart/${productId}`)
  },

  // 清空购物车
  clearCart: () => {
    return request.delete('/user/cart/clear')
  },

  // 获取购物车列表
  getCart: () => {
    return request.get<CartData>('/user/cart/list')
  },

  // 购物车结算
  checkout: (productIds: number[], couponId?: number) => {
    return request.post('/user/cart/checkout', null, { 
      params: { 
        productIds: productIds.join(','),
        couponId 
      } 
    })
  }
}
