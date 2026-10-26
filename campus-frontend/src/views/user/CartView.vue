<template>
  <div class="cart-page">
    <el-container>
      <el-header>
        <div class="header-content">
          <el-button @click="router.push('/shop')" :icon="ArrowLeft">继续购物</el-button>
          <h1>购物车</h1>
          <el-button 
            type="danger" 
            plain 
            @click="handleClearCart"
            :disabled="!cartData || cartData.items.length === 0"
          >
            清空购物车
          </el-button>
        </div>
      </el-header>

      <el-main>
        <el-card v-if="cartData && cartData.items.length > 0">
          <el-table :data="cartData.items" style="width: 100%">
            <el-table-column label="商品" min-width="300">
              <template #default="{ row }">
                <div class="product-info">
                  <el-image 
                    :src="row.productImage" 
                    fit="cover"
                    style="width: 80px; height: 80px; border-radius: 4px"
                  />
                  <div class="product-details">
                    <h4>{{ row.productName }}</h4>
                    <p class="price">¥{{ (row.price / 100).toFixed(2) }}</p>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="单价" width="120" align="center">
              <template #default="{ row }">
                ¥{{ (row.price / 100).toFixed(2) }}
              </template>
            </el-table-column>

            <el-table-column label="数量" width="180" align="center">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.quantity" 
                  :min="1" 
                  :max="row.stock"
                  @change="(val: number) => handleUpdateQuantity(row.productId, val)"
                />
              </template>
            </el-table-column>

            <el-table-column label="小计" width="120" align="center">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.subtotal / 100).toFixed(2) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button 
                  type="danger" 
                  link 
                  @click="handleRemoveItem(row.productId)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="cart-footer">
            <div class="total-info">
              <span>共 {{ cartData.totalCount }} 件商品</span>
              <span class="total-price">总计：¥{{ (cartData.totalPrice / 100).toFixed(2) }}</span>
            </div>
            <el-button 
              type="primary" 
              size="large"
              @click="handleCheckout"
            >
              去结算
            </el-button>
          </div>
        </el-card>

        <el-empty v-else description="购物车是空的">
          <el-button type="primary" @click="router.push('/shop')">去购物</el-button>
        </el-empty>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
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
    ElMessage.success('更新成功')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
    await loadCart() // 恢复原数量
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
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
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
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
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
    ElMessage.success('订单创建成功')
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

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

h1 {
  margin: 0;
  flex: 1;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-details h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
}

.product-details .price {
  margin: 0;
  color: #f56c6c;
  font-size: 14px;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.cart-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.total-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
