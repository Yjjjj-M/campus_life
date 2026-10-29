<template>
  <div class="cart-page">
    <!-- 顶部导航 -->
    <header class="top-nav">
      <div class="nav-content">
        <el-button @click="$router.back()" circle class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="nav-title">🛒 购物车</span>
        <el-button 
          v-if="cartData && cartData.items.length > 0"
          type="danger" 
          plain
          size="small"
          @click="handleClearCart"
        >
          🗑️ 清空
        </el-button>
      </div>
    </header>

    <main class="cart-main">
      <div v-if="cartData && cartData.items.length > 0" class="cart-content">
        <!-- 商品列表 -->
        <div class="cart-items">
          <div v-for="item in cartData.items" :key="item.productId" class="cart-item">
            <div class="item-image">
              <el-image :src="item.productImage" fit="cover" class="item-img">
                <template #error>
                  <div class="img-fallback">🍽️</div>
                </template>
              </el-image>
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ item.productName }}</h4>
              <div class="item-price">¥{{ (item.price / 100).toFixed(2) }}</div>
            </div>
            <div class="item-quantity">
              <el-input-number 
                v-model="item.quantity" 
                :min="1" 
                :max="item.stock"
                size="small"
                @change="(val: number) => handleUpdateQuantity(item.productId, val)"
              />
            </div>
            <div class="item-subtotal">
              ¥{{ (item.subtotal / 100).toFixed(2) }}
            </div>
            <el-button 
              type="danger" 
              text
              @click="handleRemoveItem(item.productId)"
              class="remove-btn"
            >
              ✕
            </el-button>
          </div>
        </div>

        <!-- 结算栏 -->
        <div class="checkout-bar">
          <div class="checkout-info">
            <span class="item-count">共 <strong>{{ cartData.totalCount }}</strong> 件商品</span>
            <div class="total-price">
              <span>总计：</span>
              <span class="price-value">¥{{ (cartData.totalPrice / 100).toFixed(2) }}</span>
            </div>
          </div>
          <el-button 
            type="primary" 
            size="large"
            class="checkout-btn"
            @click="handleCheckout"
          >
            去结算
          </el-button>
        </div>
      </div>

      <!-- 空购物车 -->
      <div v-else class="empty-cart">
        <div class="empty-icon">🛒</div>
        <h2>购物车是空的</h2>
        <p>快去挑选心仪的商品吧！</p>
        <el-button type="primary" size="large" round @click="$router.push('/shop')">
          🛍️ 去购物
        </el-button>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cartApi, type CartData } from '../../api/cart'

const router = useRouter()
const cartData = ref<CartData | null>(null)

const loadCart = async () => {
  try {
    const res = await cartApi.getCart()
    cartData.value = res.data
  } catch (error) {
    console.error('加载购物车失败:', error)
    ElMessage.error('加载购物车失败')
  }
}

const handleUpdateQuantity = async (productId: number, quantity: number) => {
  try {
    await cartApi.updateCartItem(productId, quantity)
    await loadCart()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
    await loadCart()
  }
}

const handleRemoveItem = async (productId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartApi.removeFromCart(productId)
    await loadCart()
    ElMessage.success('已删除')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartApi.clearCart()
    await loadCart()
    ElMessage.success('购物车已清空')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('清空失败')
  }
}

const handleCheckout = async () => {
  if (!cartData.value || cartData.value.items.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  try {
    const productIds = cartData.value.items.map(item => item.productId)
    await cartApi.checkout(productIds)
    ElMessage.success('订单创建成功 🎉')
    router.push('/user/orders')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '结算失败')
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.top-nav {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 1px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 32px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.nav-title {
  flex: 1;
  font-size: 18px;
  font-weight: 600;
  color: #2d3436;
}

.cart-main {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 32px;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.cart-item:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.item-img {
  width: 100%;
  height: 100%;
}

.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  font-size: 32px;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: #2d3436;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 13px;
  color: #b2bec3;
}

.item-subtotal {
  font-size: 18px;
  font-weight: 700;
  color: #ff6b6b;
  min-width: 80px;
  text-align: right;
}

.remove-btn {
  font-size: 16px;
  color: #b2bec3 !important;
}

.remove-btn:hover {
  color: #ff6b6b !important;
}

/* 结算栏 */
.checkout-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.06);
  position: sticky;
  bottom: 20px;
}

.checkout-info {
  display: flex;
  align-items: baseline;
  gap: 24px;
}

.item-count {
  color: #636e72;
  font-size: 14px;
}

.item-count strong {
  color: #2d3436;
  font-size: 18px;
}

.total-price {
  font-size: 14px;
  color: #636e72;
}

.price-value {
  font-size: 28px;
  font-weight: 700;
  color: #ff6b6b;
}

.checkout-btn {
  padding: 12px 40px !important;
  border-radius: 30px !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  letter-spacing: 2px;
}

/* 空购物车 */
.empty-cart {
  text-align: center;
  padding: 100px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-cart h2 {
  font-size: 22px;
  color: #2d3436;
  margin: 0 0 8px 0;
}

.empty-cart p {
  color: #b2bec3;
  margin: 0 0 30px 0;
}
</style>
