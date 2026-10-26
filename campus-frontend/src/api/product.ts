import request from './request'

export interface Product {
  id: number
  shopId: number
  categoryId: number
  name: string
  price: number
  image?: string
  description?: string
  stock: number
  status: number
  sales: number
  createTime: string
  updateTime: string
  categoryName?: string
  shopName?: string
}

export interface ProductCategory {
  id?: number
  name: string
  sort: number
  status: number
  createTime?: string
  updateTime?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
}

export const adminProductCategoryApi = {
  getCategories: () => {
    return request.get<any, { code: number; data: ProductCategory[]; message: string }>(
      '/admin/product-category/list'
    )
  },
  addCategory: (category: ProductCategory) => {
    return request.post<any, { code: number; message: string }>('/admin/product-category', category)
  },
  updateCategory: (category: ProductCategory) => {
    return request.put<any, { code: number; message: string }>('/admin/product-category', category)
  },
  deleteCategory: (id: number) => {
    return request.delete<any, { code: number; message: string }>(`/admin/product-category/${id}`)
  }
}

export const adminProductApi = {
  getProducts: (
    page: number,
    size: number,
    shopId?: number,
    categoryId?: number,
    name?: string,
    status?: number
  ) => {
    const params: any = { page, size }
    if (shopId) params.shopId = shopId
    if (categoryId) params.categoryId = categoryId
    if (name) params.name = name
    if (status !== undefined) params.status = status
    return request.get<any, { code: number; data: PageResult<Product>; message: string }>(
      '/admin/product/page',
      { params }
    )
  },
  getProduct: (id: number) => {
    return request.get<any, { code: number; data: Product; message: string }>(`/admin/product/${id}`)
  },
  addProduct: (product: Product) => {
    return request.post<any, { code: number; message: string }>('/admin/product', product)
  },
  updateProduct: (product: Product) => {
    return request.put<any, { code: number; message: string }>('/admin/product', product)
  },
  deleteProduct: (id: number) => {
    return request.delete<any, { code: number; message: string }>(`/admin/product/${id}`)
  },
  onlineProduct: (id: number) => {
    return request.put<any, { code: number; message: string }>(`/admin/product/${id}/online`)
  },
  offlineProduct: (id: number) => {
    return request.put<any, { code: number; message: string }>(`/admin/product/${id}/offline`)
  }
}

export const userProductApi = {
  getShopCategories: (shopId: number) => {
    return request.get<any, { code: number; data: any[]; message: string }>(
      `/user/product/shop/${shopId}/categories`
    )
  },
  getShopProducts: (shopId: number, categoryId?: number) => {
    const params: any = {}
    if (categoryId) params.categoryId = categoryId
    return request.get<any, { code: number; data: Product[]; message: string }>(
      `/user/product/shop/${shopId}/list`,
      { params }
    )
  },
  getProduct: (id: number) => {
    return request.get<any, { code: number; data: Product; message: string }>(`/user/product/${id}`)
  }
}
