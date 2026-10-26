<template>
  <div class="feed-container">
    <div class="feed-header">
      <h2>好友动态</h2>
    </div>

    <div class="feed-list" v-if="feeds.length > 0">
      <div class="feed-item" v-for="review in feeds" :key="review.id">
        <div class="user-info">
          <el-avatar :size="40" :src="review.userAvatar" />
          <div class="user-details">
            <span class="username">{{ review.userNickname }}</span>
            <span class="time">{{ formatTime(review.createTime) }}</span>
          </div>
        </div>

        <div class="review-content">
          <div class="rating">
            <el-rate v-model="review.rating" disabled show-score text-color="#ff9900" />
          </div>
          <p class="content">{{ review.content }}</p>
          <div class="images" v-if="review.imageList && review.imageList.length > 0">
            <el-image
              v-for="(img, index) in review.imageList"
              :key="index"
              :src="img"
              :preview-src-list="review.imageList"
              fit="cover"
              class="review-image"
            />
          </div>
        </div>

        <div class="shop-info" @click="goToShop(review.shopId)">
          <span class="shop-name">{{ review.shopName }}</span>
          <el-icon><Location /></el-icon>
        </div>

        <div class="actions">
          <div class="action-item" @click="handleLike(review)">
            <el-icon v-if="review.hasLiked"><StarFilled /></el-icon>
            <el-icon v-else><Star /></el-icon>
            <span>{{ review.likeCount }}</span>
          </div>
        </div>
      </div>

      <div class="load-more" v-if="hasMore">
        <el-button @click="loadMore" :loading="loading">加载更多</el-button>
      </div>
    </div>

    <el-empty v-else description="还没有好友动态，快去关注其他用户吧！" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Location, Star, StarFilled } from '@element-plus/icons-vue'
import { getMyFeed } from '../../api/follow'
import { userReviewApi } from '../../api/review'
import { ElMessage } from 'element-plus'

const router = useRouter()
const feeds = ref<any[]>([])
const cursor = ref<number>()
const hasMore = ref(true)
const loading = ref(false)

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

const loadFeeds = async () => {
  loading.value = true
  try {
    const res = await getMyFeed(cursor.value)
    const data = res.data
    feeds.value = (data.records || []).map((item: any) => ({
      ...item,
      imageList: item.images ? item.images.split(',').filter(Boolean) : []
    }))
    hasMore.value = !!data.hasMore
    if (feeds.value.length > 0) {
      cursor.value = data.nextCursor
    }
  } catch (error) {
    console.error('加载 Feed 流失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  loading.value = true
  try {
    const res = await getMyFeed(cursor.value)
    const data = res.data
    const newFeeds = (data.records || []).map((item: any) => ({
      ...item,
      imageList: item.images ? item.images.split(',').filter(Boolean) : []
    }))
    feeds.value.push(...newFeeds)
    hasMore.value = !!data.hasMore
    if (newFeeds.length > 0) {
      cursor.value = data.nextCursor
    }
  } catch (error) {
    console.error('加载更多失败:', error)
  } finally {
    loading.value = false
  }
}

const handleLike = async (review: any) => {
  try {
    if (review.hasLiked) {
      await userReviewApi.unlike(review.id)
      review.hasLiked = false
      review.likeCount--
    } else {
      await userReviewApi.like(review.id)
      review.hasLiked = true
      review.likeCount++
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToShop = (shopId: number) => {
  router.push(`/shop/${shopId}`)
}

onMounted(() => {
  loadFeeds()
})
</script>

<style scoped>
.feed-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.feed-header {
  margin-bottom: 20px;
}

.feed-header h2 {
  font-size: 24px;
  font-weight: 600;
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feed-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.review-content {
  margin-bottom: 12px;
}

.rating {
  margin-bottom: 8px;
}

.content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
}

.images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 12px;
}

.shop-info:hover {
  background: #e8e8e8;
}

.shop-name {
  font-size: 14px;
  color: #666;
}

.actions {
  display: flex;
  gap: 20px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
}

.action-item:hover {
  color: #409eff;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}
</style>
