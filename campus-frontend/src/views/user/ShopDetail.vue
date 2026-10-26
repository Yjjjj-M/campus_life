<template>
  <div class="shop-detail-container">
    <el-header>
      <div class="header-content">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <h1>商铺详情</h1>
        <el-button type="primary" @click="$router.push('/cart')" :icon="ShoppingCart">
          购物车
        </el-button>
      </div>
    </el-header>

    <el-main v-if="shop">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card>
            <template #header>
              <h2>{{ shop.name }}</h2>
            </template>
            <el-image :src="shop.images.split(',')[0]" fit="cover" style="width: 100%; height: 400px" />
            <div class="shop-info-detail">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="分类">{{ shop.categoryName }}</el-descriptions-item>
                <el-descriptions-item label="评分">{{ (shop.score / 10).toFixed(1) }} 分</el-descriptions-item>
                <el-descriptions-item label="销量">{{ shop.sold }}</el-descriptions-item>
                <el-descriptions-item label="人均价格">¥{{ (shop.avgPrice / 100).toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="营业时间">{{ shop.openHours }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="shop.status === 1 ? 'success' : 'danger'">
                    {{ shop.status === 1 ? '营业中' : '休息中' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="地址" :span="2">{{ shop.address }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>

          <!-- 商品列表 -->
          <el-card style="margin-top: 20px">
            <template #header>
              <h3>商品列表</h3>
            </template>
            <el-empty v-if="products.length === 0" description="暂无商品" />
            <el-row v-else :gutter="16">
              <el-col :span="8" v-for="product in products" :key="product.id" style="margin-bottom: 16px">
                <el-card shadow="hover">
                  <el-image 
                    :src="product.image" 
                    fit="cover"
                    style="width: 100%; height: 180px; border-radius: 4px"
                  />
                  <div class="product-info">
                    <h4>{{ product.name }}</h4>
                    <p class="product-desc">{{ product.description }}</p>
                    <div class="product-bottom">
                      <span class="product-price">¥{{ (product.price / 100).toFixed(2) }}</span>
                      <el-button 
                        type="primary" 
                        size="small"
                        @click="handleAddToCart(product.id)"
                      >
                        加入购物车
                      </el-button>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </el-card>

          <!-- 评价列表 -->
          <el-card style="margin-top: 20px">
            <template #header>
              <div class="review-header">
                <h3>用户评价</h3>
                <el-button type="primary" size="small" @click="showPublishDialog">
                  发布评价
                </el-button>
              </div>
            </template>
            
            <el-empty v-if="reviews.length === 0" description="暂无评价，快来发表你的看法吧！" />
            
            <div v-else>
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-user">
                  <el-avatar :size="40" :src="review.userAvatar">
                    {{ review.userNickname?.charAt(0) }}
                  </el-avatar>
                  <div class="review-user-info">
                    <span class="user-name">{{ review.userNickname }}</span>
                    <el-rate v-model="review.rating" disabled :colors="['#F56C6C', '#F56C6C', '#F56C6C']" />
                  </div>
                  <span class="review-time">{{ formatTime(review.createTime) }}</span>
                </div>
                
                <div class="review-content">{{ review.content }}</div>
                
                <div v-if="review.images" class="review-images">
                  <el-image 
                    v-for="(img, index) in review.images.split(',')" 
                    :key="index"
                    :src="img"
                    :preview-src-list="review.images.split(',')"
                    fit="cover"
                    style="width: 80px; height: 80px; margin-right: 8px; border-radius: 4px"
                  />
                </div>
                
                <div class="review-actions">
                  <el-button 
                    :type="review.hasLiked ? 'danger' : 'default'"
                    size="small"
                    @click="handleLike(review)"
                  >
                    <el-icon><Star /></el-icon>
                    {{ review.hasLiked ? '已点赞' : '点赞' }}
                    ({{ review.likeCount }})
                  </el-button>
                  
                  <el-button 
                    :type="review.isFollowed ? 'success' : 'default'"
                    size="small"
                    @click="handleFollow(review)"
                  >
                    {{ review.isFollowed ? '已关注' : '关注' }}
                  </el-button>
                </div>
              </div>
              
              <div v-if="hasMoreReviews" class="load-more">
                <el-button @click="loadMoreReviews" :loading="loadingMore">
                  加载更多
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card>
            <template #header>
              <h3>优惠活动</h3>
            </template>
            <el-empty v-if="vouchers.length === 0" description="暂无优惠活动" />
            <el-list v-else>
              <el-list-item v-for="voucher in vouchers" :key="voucher.id">
                <div class="voucher-item">
                  <div class="voucher-info">
                    <h4>{{ voucher.title }}</h4>
                    <p>{{ voucher.subTitle }}</p>
                    <div class="voucher-price">
                      <span class="price">¥{{ (voucher.payValue / 100).toFixed(2) }}</span>
                      <span class="original-price">¥{{ (voucher.actualValue / 100).toFixed(2) }}</span>
                    </div>
                  </div>
                  <el-button type="primary" @click="handleBuyVoucher(voucher.id!)">立即抢购</el-button>
                </div>
              </el-list-item>
            </el-list>
          </el-card>
        </el-col>
      </el-row>
    </el-main>

    <!-- 发布评价对话框 -->
    <el-dialog v-model="publishDialogVisible" title="发布评价" width="500px">
      <el-form :model="publishForm" label-width="80px">
        <el-form-item label="评分" required>
          <el-rate v-model="publishForm.rating" :colors="['#F56C6C', '#F56C6C', '#F56C6C']" />
        </el-form-item>
        <el-form-item label="评价内容" required>
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="4"
            placeholder="请分享你的使用体验..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublishReview" :loading="publishing">
          发布
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ShoppingCart, Star } from '@element-plus/icons-vue'
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

// 评价相关
const reviews = ref<Review[]>([])
const hasMoreReviews = ref(false)
const nextCursor = ref<number | undefined>()
const loadingMore = ref(false)

// 发布评价对话框
const publishDialogVisible = ref(false)
const publishing = ref(false)
const publishForm = ref({
  rating: 5,
  content: ''
})

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
    await ElMessageBox.confirm(
      '确认购买该优惠券？',
      '购买确认',
      { confirmButtonText: '确认购买', cancelButtonText: '取消', type: 'info' }
    )
    const res = await userOrderApi.createOrder(voucherId)
    ElMessage.success('订单创建成功')
    router.push(`/user/order/${res.data.id}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('购买失败:', error)
    }
  }
}

const handleAddToCart = async (productId: number) => {
  try {
    await cartApi.addToCart(productId, 1)
    ElMessage.success('已添加到购物车')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

// 加载评价列表
const loadReviews = async (shopId: number, cursor?: number) => {
  try {
    const res = await userReviewApi.getReviewsByShop(shopId, cursor, 10)
    const data = res.data
    
    if (cursor) {
      // 加载更多
      reviews.value.push(...data.records)
    } else {
      // 首次加载
      reviews.value = data.records
    }
    
    hasMoreReviews.value = data.hasMore || false
    nextCursor.value = data.nextCursor
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

// 加载更多评价
const loadMoreReviews = async () => {
  if (!shop.value || !shop.value.id || !nextCursor.value) return
  loadingMore.value = true
  await loadReviews(shop.value.id, nextCursor.value)
  loadingMore.value = false
}

// 显示发布评价对话框
const showPublishDialog = () => {
  publishForm.value = { rating: 5, content: '' }
  publishDialogVisible.value = true
}

// 发布评价
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
    ElMessage.success('评价发布成功')
    publishDialogVisible.value = false
    // 重新加载评价列表
    await loadReviews(shop.value.id)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

// 点赞/取消点赞
const handleLike = async (review: Review) => {
  if (!review.id) return
  
  try {
    if (review.hasLiked) {
      await userReviewApi.unlike(review.id)
      review.hasLiked = false
      review.likeCount = (review.likeCount || 1) - 1
      ElMessage.success('已取消点赞')
    } else {
      await userReviewApi.like(review.id)
      review.hasLiked = true
      review.likeCount = (review.likeCount || 0) + 1
      ElMessage.success('点赞成功')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 关注/取消关注
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
      ElMessage.success('关注成功')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 格式化时间
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
.shop-detail-container {
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
}

.shop-info-detail {
  margin-top: 20px;
}

.voucher-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.voucher-info h4 {
  margin: 0 0 5px 0;
}

.voucher-info p {
  margin: 0 0 10px 0;
  color: #909399;
  font-size: 12px;
}

.voucher-price {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.original-price {
  color: #909399;
  text-decoration: line-through;
  font-size: 14px;
}

.product-info {
  padding: 12px 0;
}

.product-info h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  margin: 0 0 12px 0;
  color: #909399;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 36px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-header h3 {
  margin: 0;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.review-item:last-child {
  border-bottom: none;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.review-time {
  color: #909399;
  font-size: 12px;
}

.review-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-images {
  margin-bottom: 12px;
}

.review-actions {
  display: flex;
  gap: 12px;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}
</style>
