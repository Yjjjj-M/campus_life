<template>
  <div class="detail-page">
    <!-- 顶部导航 -->
    <header class="top-nav">
      <div class="nav-content">
        <el-button @click="$router.back()" circle class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="nav-title">{{ shop?.name || '商铺详情' }}</span>
        <el-button @click="$router.push('/cart')" class="cart-btn">
          🛒 购物车
        </el-button>
      </div>
    </header>

    <template v-if="shop">
      <!-- 商铺头部 -->
      <section class="shop-hero">
        <div class="hero-image">
          <el-image :src="shop.images.split(',')[0]" fit="cover" class="hero-img">
            <template #error>
              <div class="hero-fallback">🏪</div>
            </template>
          </el-image>
          <div class="hero-overlay"></div>
        </div>
        <div class="hero-info">
          <h1>{{ shop.name }}</h1>
          <div class="hero-meta">
            <div class="meta-item">
              <span class="meta-icon">⭐</span>
              <span class="meta-value">{{ (shop.score / 10).toFixed(1) }}</span>
              <span class="meta-label">评分</span>
            </div>
            <div class="meta-divider"></div>
            <div class="meta-item">
              <span class="meta-icon">🔥</span>
              <span class="meta-value">{{ shop.sold }}</span>
              <span class="meta-label">月售</span>
            </div>
            <div class="meta-divider"></div>
            <div class="meta-item">
              <span class="meta-icon">💰</span>
              <span class="meta-value">¥{{ (shop.avgPrice / 100).toFixed(0) }}</span>
              <span class="meta-label">人均</span>
            </div>
            <div class="meta-divider"></div>
            <div class="meta-item">
              <span class="meta-icon">🕐</span>
              <span class="meta-value">{{ shop.openHours || '营业中' }}</span>
              <span class="meta-label">营业时间</span>
            </div>
          </div>
          <div class="hero-address">
            📍 {{ shop.address }}
            <el-tag :type="shop.status === 1 ? 'success' : 'danger'" size="small" style="margin-left: 10px">
              {{ shop.status === 1 ? '营业中' : '休息中' }}
            </el-tag>
          </div>
        </div>
      </section>

      <div class="detail-content">
        <div class="main-column">
          <!-- 商品列表 -->
          <section class="section-card">
            <div class="section-title">
              <span class="title-icon">🍽️</span>
              <h2>商品列表</h2>
              <span class="section-count">{{ products.length }} 件商品</span>
            </div>
            
            <div v-if="products.length === 0" class="empty-box">
              <div class="empty-icon">📦</div>
              <p>暂无商品</p>
            </div>

            <div v-else class="product-grid">
              <div v-for="product in products" :key="product.id" class="product-card">
                <div class="product-image">
                  <el-image :src="product.image" fit="cover" class="p-img">
                    <template #error>
                      <div class="p-fallback">🍽️</div>
                    </template>
                  </el-image>
                  <div class="stock-badge" v-if="product.stock < 10">
                    仅剩 {{ product.stock }} 件
                  </div>
                </div>
                <div class="product-info">
                  <h4 class="p-name">{{ product.name }}</h4>
                  <p class="p-desc">{{ product.description || '精选好物' }}</p>
                  <div class="p-footer">
                    <div class="p-price">
                      <span class="price-symbol">¥</span>
                      <span class="price-value">{{ (product.price / 100).toFixed(2) }}</span>
                    </div>
                    <el-button type="primary" size="small" round @click="handleAddToCart(product.id)" class="add-btn">
                      + 加入购物车
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- 用户评价 -->
          <section class="section-card">
            <div class="section-title">
              <span class="title-icon">💬</span>
              <h2>用户评价</h2>
              <el-button type="primary" size="small" round @click="showPublishDialog" class="publish-btn">
                ✍️ 写评价
              </el-button>
            </div>

            <div v-if="reviews.length === 0" class="empty-box">
              <div class="empty-icon">💭</div>
              <p>暂无评价，快来发表你的看法吧！</p>
            </div>

            <div v-else class="review-list">
              <div v-for="review in reviews" :key="review.id" class="review-card">
                <div class="review-header">
                  <div class="reviewer">
                    <el-avatar :size="40" :src="review.userAvatar" class="reviewer-avatar">
                      {{ review.userNickname?.charAt(0) }}
                    </el-avatar>
                    <div>
                      <div class="reviewer-name">{{ review.userNickname }}</div>
                      <div class="review-time">{{ formatTime(review.createTime) }}</div>
                    </div>
                  </div>
                  <el-rate v-model="review.rating" disabled :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
                </div>
                
                <p class="review-text">{{ review.content }}</p>
                
                <div v-if="review.images" class="review-photos">
                  <el-image 
                    v-for="(img, index) in review.images.split(',')" 
                    :key="index"
                    :src="img"
                    :preview-src-list="review.images.split(',')"
                    fit="cover"
                    class="review-photo"
                  />
                </div>
                
                <div class="review-actions">
                  <el-button 
                    :type="review.hasLiked ? 'danger' : 'default'"
                    size="small" 
                    text
                    @click="handleLike(review)"
                  >
                    {{ review.hasLiked ? '❤️' : '🤍' }} {{ review.likeCount || 0 }}
                  </el-button>
                  <el-button 
                    :type="review.isFollowed ? 'success' : 'default'"
                    size="small" 
                    text
                    @click="handleFollow(review)"
                  >
                    {{ review.isFollowed ? '✅ 已关注' : '➕ 关注' }}
                  </el-button>
                </div>
              </div>
              
              <div v-if="hasMoreReviews" class="load-more">
                <el-button @click="loadMoreReviews" :loading="loadingMore" round>
                  加载更多评价
                </el-button>
              </div>
            </div>
          </section>
        </div>

        <!-- 右侧栏 -->
        <aside class="side-column">
          <!-- 优惠活动 -->
          <div class="section-card voucher-card">
            <div class="section-title">
              <span class="title-icon">🎁</span>
              <h2>优惠活动</h2>
            </div>
            
            <div v-if="vouchers.length === 0" class="empty-box small">
              <p>暂无优惠活动</p>
            </div>

            <div v-else class="voucher-list">
              <div 
                v-for="voucher in vouchers" 
                :key="voucher.id" 
                class="voucher-item"
                :class="{ 'seckill': voucher.type === 1 }"
              >
                <div class="voucher-left">
                  <div class="voucher-amount">
                    <span class="amount-symbol">¥</span>
                    <span class="amount-value">{{ (voucher.actualValue / 100).toFixed(0) }}</span>
                  </div>
                  <div class="voucher-condition">满{{ (voucher.payValue / 100).toFixed(0) }}可用</div>
                </div>
                <div class="voucher-right">
                  <h4>{{ voucher.title }}</h4>
                  <p>{{ voucher.subTitle }}</p>
                  <el-button 
                    type="danger" 
                    size="small" 
                    round
                    class="buy-btn"
                    @click="handleBuyVoucher(voucher.id!)"
                  >
                    {{ voucher.type === 1 ? '⚡ 立即秒杀' : '立即抢购' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </template>

    <!-- 发布评价对话框 -->
    <el-dialog v-model="publishDialogVisible" title="✍️ 发布评价" width="500px" class="review-dialog">
      <el-form :model="publishForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="publishForm.rating" :colors="['#F56C6C', '#E6A23C', '#67C23A']" size="large" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="4"
            placeholder="分享你的使用体验，帮助其他同学做出选择..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="handlePublishReview" :loading="publishing" round>发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getShopDetail, type Shop } from '../../api/shop'
import { getUserVoucherList, type Voucher } from '../../api/voucher'
import { userOrderApi } from '../../api/order'
import { cartApi } from '../../api/cart'
import { userProductApi, type Product } from '../../api/product'
import { userReviewApi, type Review } from '../../api/review'
import { followUser, unfollowUser } from '../../api/follow'

const route = useRoute()
const router = useRouter()
const shop = ref<Shop | null>(null)
const vouchers = ref<Voucher[]>([])
const products = ref<Product[]>([])

const reviews = ref<Review[]>([])
const hasMoreReviews = ref(false)
const nextCursor = ref<number | undefined>()
const loadingMore = ref(false)

const publishDialogVisible = ref(false)
const publishing = ref(false)
const publishForm = ref({ rating: 5, content: '' })

onMounted(async () => {
  const shopId = Number(route.params.id)
  await loadShopDetail(shopId)
  await loadVouchers(shopId)
  await loadProducts(shopId)
  await loadReviews(shopId)
})

const loadShopDetail = async (id: number) => {
  try {
    const res = await getShopDetail(id)
    shop.value = res.data
  } catch (error) {
    console.error('加载商铺详情失败:', error)
  }
}

const loadVouchers = async (shopId: number) => {
  try {
    const res = await getUserVoucherList(shopId)
    vouchers.value = res.data
  } catch (error) {
    console.error('加载优惠券失败:', error)
  }
}

const loadProducts = async (shopId: number) => {
  try {
    const res = await userProductApi.getShopProducts(shopId)
    products.value = res.data
  } catch (error) {
    console.error('加载商品失败:', error)
  }
}

const handleBuyVoucher = async (voucherId: number) => {
  try {
    await ElMessageBox.confirm('确认购买该优惠券？', '购买确认',
      { confirmButtonText: '确认购买', cancelButtonText: '取消', type: 'info' })
    const res = await userOrderApi.createOrder(voucherId)
    ElMessage.success('订单创建成功')
    router.push(`/user/order/${res.data.id}`)
  } catch (error) {
    if (error !== 'cancel') console.error('购买失败:', error)
  }
}

const handleAddToCart = async (productId: number) => {
  try {
    await cartApi.addToCart(productId, 1)
    ElMessage.success('已添加到购物车 🛒')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

const loadReviews = async (shopId: number, cursor?: number) => {
  try {
    const res = await userReviewApi.getReviewsByShop(shopId, cursor, 10)
    const data = res.data
    if (cursor) {
      reviews.value.push(...data.records)
    } else {
      reviews.value = data.records
    }
    hasMoreReviews.value = data.hasMore || false
    nextCursor.value = data.nextCursor
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

const loadMoreReviews = async () => {
  if (!shop.value || !shop.value.id || !nextCursor.value) return
  loadingMore.value = true
  await loadReviews(shop.value.id, nextCursor.value)
  loadingMore.value = false
}

const showPublishDialog = () => {
  publishForm.value = { rating: 5, content: '' }
  publishDialogVisible.value = true
}

const handlePublishReview = async () => {
  if (!publishForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  if (!shop.value || !shop.value.id) return
  
  publishing.value = true
  try {
    await userReviewApi.publish({
      shopId: shop.value.id,
      rating: publishForm.value.rating,
      content: publishForm.value.content
    })
    ElMessage.success('评价发布成功 🎉')
    publishDialogVisible.value = false
    await loadReviews(shop.value.id)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

const handleLike = async (review: Review) => {
  if (!review.id) return
  try {
    if (review.hasLiked) {
      await userReviewApi.unlike(review.id)
      review.hasLiked = false
      review.likeCount = (review.likeCount || 1) - 1
    } else {
      await userReviewApi.like(review.id)
      review.hasLiked = true
      review.likeCount = (review.likeCount || 0) + 1
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const handleFollow = async (review: Review) => {
  if (!review.userId) return
  try {
    if (review.isFollowed) {
      await unfollowUser(review.userId)
      review.isFollowed = false
      ElMessage.success('已取消关注')
    } else {
      await followUser(review.userId)
      review.isFollowed = true
      ElMessage.success('关注成功 ✅')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const formatTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40px;
}

/* 导航 */
.top-nav {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 1px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  max-width: 1400px;
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

.cart-btn {
  border-radius: 20px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 商铺头部 */
.shop-hero {
  position: relative;
  margin-bottom: 30px;
}

.hero-image {
  height: 300px;
  overflow: hidden;
  position: relative;
}

.hero-img {
  width: 100%;
  height: 100%;
}

.hero-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #dfe6e9 0%, #b2bec3 100%);
  font-size: 72px;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 40%, rgba(0,0,0,0.6) 100%);
}

.hero-info {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  margin-top: -80px;
  position: relative;
  z-index: 2;
}

.hero-info h1 {
  color: white;
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 16px 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.hero-meta {
  display: inline-flex;
  align-items: center;
  gap: 20px;
  background: white;
  padding: 16px 28px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-icon {
  font-size: 18px;
}

.meta-value {
  font-size: 16px;
  font-weight: 600;
  color: #2d3436;
}

.meta-label {
  font-size: 13px;
  color: #b2bec3;
}

.meta-divider {
  width: 1px;
  height: 24px;
  background: #edf2f7;
}

.hero-address {
  color: white;
  font-size: 14px;
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.3);
}

/* 内容区域 */
.detail-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 24px;
}

.section-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.title-icon {
  font-size: 22px;
}

.section-title h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  flex: 1;
  color: #2d3436;
}

.section-count {
  font-size: 13px;
  color: #b2bec3;
}

.publish-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.product-card {
  border-radius: 12px;
  overflow: hidden;
  background: white;
  border: 1px solid #f0f2f5;
  transition: all 0.3s;
}

.product-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.product-image {
  height: 160px;
  overflow: hidden;
  position: relative;
}

.p-img {
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
}

.product-card:hover .p-img {
  transform: scale(1.05);
}

.p-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  font-size: 40px;
}

.stock-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 10px;
  background: rgba(255, 107, 107, 0.9);
  color: white;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.product-info {
  padding: 14px;
}

.p-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: #2d3436;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.p-desc {
  font-size: 12px;
  color: #b2bec3;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 32px;
}

.p-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.p-price {
  color: #ff6b6b;
}

.price-symbol {
  font-size: 13px;
  font-weight: 600;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
}

.add-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  font-size: 12px;
}

/* 评价 */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  padding: 16px;
  background: #fafbfc;
  border-radius: 12px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.reviewer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-name {
  font-weight: 600;
  font-size: 14px;
  color: #2d3436;
}

.review-time {
  font-size: 12px;
  color: #b2bec3;
}

.review-text {
  font-size: 14px;
  color: #636e72;
  line-height: 1.7;
  margin: 0 0 12px 0;
}

.review-photos {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.review-photo {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  cursor: pointer;
}

.review-actions {
  display: flex;
  gap: 12px;
}

.load-more {
  text-align: center;
  padding: 10px;
}

/* 优惠券 */
.voucher-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.voucher-item {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f0f2f5;
  transition: all 0.3s;
}

.voucher-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.voucher-item.seckill {
  border-color: #ffe0e0;
  background: #fff5f5;
}

.voucher-left {
  width: 100px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
}

.voucher-amount {
  color: white;
}

.amount-symbol {
  font-size: 14px;
}

.amount-value {
  font-size: 28px;
  font-weight: 700;
}

.voucher-condition {
  font-size: 11px;
  opacity: 0.9;
  margin-top: 4px;
}

.voucher-right {
  flex: 1;
  padding: 14px;
}

.voucher-right h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #2d3436;
}

.voucher-right p {
  font-size: 12px;
  color: #b2bec3;
  margin: 0 0 10px 0;
}

.buy-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%) !important;
  border: none !important;
}

/* 空状态 */
.empty-box {
  text-align: center;
  padding: 40px 20px;
}

.empty-box.small {
  padding: 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-box p {
  color: #b2bec3;
  font-size: 14px;
  margin: 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .detail-content {
    grid-template-columns: 1fr;
  }
  
  .hero-meta {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
