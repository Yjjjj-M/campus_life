import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    redirect: '/shop'
  },
  {
    path: '/shop',
    name: 'ShopList',
    component: () => import('../views/user/ShopList.vue')
  },
  {
    path: '/shop/:id',
    name: 'ShopDetail',
    component: () => import('../views/user/ShopDetail.vue')
  },
  {
    path: '/shop/nearby',
    name: 'NearbyShops',
    component: () => import('../views/user/NearbyShops.vue')
  },
  {
    path: '/feed',
    name: 'Feed',
    component: () => import('../views/user/FeedView.vue')
  },
  {
    path: '/admin',
    redirect: '/admin/dashboard'
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('../views/admin/Dashboard.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/shops',
    name: 'AdminShopManage',
    component: () => import('../views/admin/ShopManage.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/categories',
    name: 'AdminCategoryManage',
    component: () => import('../views/admin/CategoryManage.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/vouchers',
    name: 'AdminVoucherManage',
    component: () => import('../views/admin/VoucherManage.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/user/orders',
    name: 'UserOrderList',
    component: () => import('../views/user/OrderList.vue')
  },
  {
    path: '/user/order/:id',
    name: 'UserOrderDetail',
    component: () => import('../views/user/OrderDetail.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/user/CartView.vue')
  },
  {
    path: '/admin/orders',
    name: 'AdminOrderList',
    component: () => import('../views/admin/OrderList.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/order/:id',
    name: 'AdminOrderDetail',
    component: () => import('../views/admin/OrderDetail.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/products',
    name: 'AdminProductManage',
    component: () => import('../views/admin/ProductManage.vue'),
    meta: { requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  // Public routes always accessible
  if (to.meta.public) {
    next()
    return
  }

  // Check if route requires login
  if (!userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // Check if route requires admin role
  if (to.meta.requiresAdmin && userStore.role !== 1) {
    next({ name: 'ShopList' })
    return
  }

  next()
})

export default router
