<template>
  <div class="nearby-shops">
    <h2>附近商铺</h2>
    
    <div class="location-info" v-if="userLocation">
      <p>您的位置：经度 {{ userLocation.longitude.toFixed(4) }}，纬度 {{ userLocation.latitude.toFixed(4) }}</p>
    </div>

    <div class="controls">
      <el-form :inline="true" size="small">
        <el-form-item label="搜索半径(米)">
          <el-input-number v-model="radius" :min="500" :max="10000" :step="500" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadNearbyShops" :loading="loading">
            搜索附近
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="shop-list" v-if="nearbyShops.length > 0">
      <div class="shop-card" v-for="item in nearbyShops" :key="item.shop.id">
        <div class="shop-image">
          <el-image 
            :src="item.shop.images.split(',')[0]" 
            fit="cover"
            style="width: 100%; height: 100%"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        
        <div class="shop-info">
          <h3>{{ item.shop.name }}</h3>
          <p class="category">{{ item.shop.categoryName }}</p>
          <p class="address">{{ item.shop.address }}</p>
          <div class="meta">
            <span class="distance">{{ formatDistance(item.distance) }}</span>
            <span class="score">
              <el-rate :model-value="item.shop.score / 10" disabled show-score text-color="#ff9900" />
            </span>
          </div>
          <div class="price">
            <span>人均 ¥{{ (item.shop.avgPrice / 100).toFixed(0) }}</span>
            <el-button type="primary" size="small" @click="goToDetail(item.shop.id!)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="empty" v-else-if="!loading">
      <el-empty description="附近没有找到商铺" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getNearbyShops, type NearbyShop } from '../../api/shop'

const router = useRouter()
const nearbyShops = ref<NearbyShop[]>([])
const loading = ref(false)
const radius = ref(3000)

// 写死的用户位置（北京大学校园中心）
const userLocation = ref({
  longitude: 116.310000,
  latitude: 39.992000
})

const loadNearbyShops = async () => {
  loading.value = true
  try {
    const response = await getNearbyShops({
      longitude: userLocation.value.longitude,
      latitude: userLocation.value.latitude,
      radius: radius.value
    })
    
    console.log('后端返回的完整响应:', response)
    console.log('response.code:', response.code)
    console.log('response.data:', response.data)
    
    if (response.code === 200 || response.code === 0) {
      nearbyShops.value = response.data
      console.log('nearbyShops 数组:', nearbyShops.value)
      console.log('nearbyShops 长度:', nearbyShops.value.length)
      if (nearbyShops.value.length === 0) {
        ElMessage.info('附近没有找到商铺，请尝试增大搜索半径')
      }
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询附近商铺失败:', error)
    ElMessage.error('查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const formatDistance = (meters: number): string => {
  if (meters < 1000) {
    return `${Math.round(meters)}m`
  } else {
    return `${(meters / 1000).toFixed(1)}km`
  }
}

const goToDetail = (id: number) => {
  router.push(`/shop/${id}`)
}

onMounted(() => {
  loadNearbyShops()
})
</script>

<style scoped>
.nearby-shops {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.location-info {
  background: #f5f7fa;
  padding: 10px 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  color: #666;
  font-size: 14px;
}

.controls {
  margin-bottom: 20px;
}

.shop-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.shop-card {
  display: flex;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.shop-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.shop-image {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  background: #f5f5f5;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.shop-info {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
}

.shop-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.category {
  margin: 0 0 4px 0;
  font-size: 13px;
  color: #999;
}

.address {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.distance {
  font-size: 14px;
  color: #409eff;
  font-weight: bold;
}

.price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.price span {
  font-size: 15px;
  color: #ff6600;
  font-weight: bold;
}

.empty {
  padding: 60px 0;
}
</style>
