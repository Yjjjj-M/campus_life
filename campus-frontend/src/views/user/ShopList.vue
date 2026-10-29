<template>
  <div class="shop-page">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-content">
        <div class="nav-left">
          <span class="logo">🎓</span>
          <span class="brand-name">校园生活</span>
        </div>
        <div class="nav-right">
          <div class="nav-item" @click="$router.push('/shop/nearby')">
            <span class="nav-icon">📍</span>
            <span>附近</span>
          </div>
          <div class="nav-item" @click="$router.push('/feed')">
            <span class="nav-icon">👥</span>
            <span>动态</span>
          </div>
          <div class="nav-item" @click="$router.push('/cart')">
            <span class="nav-icon">🛒</span>
            <span>购物车</span>
          </div>
          <div class="nav-item" @click="$router.push('/user/orders')">
            <span class="nav-icon">📋</span>
            <span>订单</span>
          </div>
          <div class="nav-item admin-link" v-if="userStore.role === 1" @click="$router.push('/admin/dashboard')">
            <span class="nav-icon">⚙️</span>
            <span>管理</span>
          </div>
          <div class="user-info" @click="handleLogout">
            <div class="avatar">{{ userStore.nickname?.charAt(0) || '👤' }}</div>
            <span class="user-name">{{ userStore.nickname }}</span>
          </div>
        </div>
      </div>
    </header>

    <!-- Hero Banner -->
    <section class="hero-section">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <h1>🍜 发现校园美食</h1>
        <p>探索周边优质商铺，享受专属优惠</p>
        
        <!-- 搜索框 -->
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <el-input 
            v-model="searchName" 
            placeholder="搜索商铺名称..." 
            clearable
            @keyup.enter="handleSearch"
            class="search-input"
          />
          <el-button type="primary" @click="handleSearch" class="search-btn">搜索</el-button>
        </div>
      </div>
    </section>

    <!-- 分类标签 -->
    <section class="category-section">
      <div class="category-scroll">
        <div 
          class="category-tag"
          :class="{ active: selectedCategory === '' }"
          @click="handleCategorySelect('')"
        >
          <span class="cat-icon">🏪</span>
          <span>全部</span>
        </div>
        <div 
          v-for="cat in categories" 
          :key="cat.id"
          class="category-tag"
          :class="{ active: selectedCategory === String(cat.id) }"
          @click="handleCategorySelect(String(cat.id))"
        >
          <span class="cat-icon">{{ getCategoryIcon(cat.name) }}</span>
          <span>{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <!-- 商铺列表 -->
    <section class="shop-section">
      <div class="section-header">
        <h2>{{ selectedCategory ? getSelectedCategoryName() : '推荐商铺' }}</h2>
        <span class="shop-count">共 {{ total }} 家</span>
      </div>

      <div class="shop-grid" v-if="shops.length > 0">
        <div 
          v-for="shop in shops" 
          :key="shop.id" 
          class="shop-card"
          @click="$router.push(`/shop/${shop.id}`)"
        >
          <!-- 图片区域 -->
          <div class="card-image">
            <el-image 
              :src="shop.images.split(',')[0]" 
              fit="cover"
              class="shop-img"
            >
              <template #error>
                <div class="img-fallback">
                  <span>🏪</span>
                </div>
              </template>
            </el-image>
            <div class="card-badge" v-if="shop.sold > 500">
              🔥 热销
            </div>
          </div>

          <!-- 信息区域 -->
          <div class="card-body">
            <div class="card-header">
              <h3 class="shop-name">{{ shop.name }}</h3>
              <div class="shop-rating">
                <span class="rating-star">⭐</span>
                <span class="rating-num">{{ (shop.score / 10).toFixed(1) }}</span>
              </div>
            </div>
            
            <div class="shop-tags">
              <span class="tag">{{ shop.categoryName }}</span>
              <span class="tag">人均 ¥{{ (shop.avgPrice / 100).toFixed(0) }}</span>
            </div>

            <div class="card-footer">
              <span class="shop-address">📍 {{ shop.address }}</span>
              <span class="shop-sales">月售 {{ shop.sold }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="shops.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">🔍</div>
        <p>暂无商铺</p>
        <el-button type="primary" @click="resetSearch">重置筛选</el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 48]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getShopCategoryList, getShopList, type ShopCategory, type Shop } from '../../api/shop'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const categories = ref<ShopCategory[]>([])
const shops = ref<Shop[]>([])
const selectedCategory = ref('')
const searchName = ref('')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)

const getCategoryIcon = (name: string) => {
  const iconMap: Record<string, string> = {
    '美食': '🍜', '甜品': '🍰', '咖啡': '☕',
    '奶茶': '🧋', '快餐': '🍔', '水果': '🍎',
    '小吃': '🍢', '烧烤': '🍖', '西餐': '🍝'
  }
  return iconMap[name] || '🏪'
}

const getSelectedCategoryName = () => {
  const cat = categories.value.find(c => String(c.id) === selectedCategory.value)
  return cat ? cat.name : '全部商铺'
}

const resetSearch = () => {
  searchName.value = ''
  selectedCategory.value = ''
  page.value = 1
  loadShops()
}

onMounted(async () => {
  await loadCategories()
  await loadShops()
})

const loadCategories = async () => {
  try {
    const res = await getShopCategoryList()
    categories.value = res.data.filter(cat => cat.status === 1)
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadShops = async () => {
  loading.value = true
  try {
    const res = await getShopList({
      page: page.value,
      size: pageSize.value,
      categoryId: selectedCategory.value ? Number(selectedCategory.value) : undefined,
      name: searchName.value || undefined
    })
    shops.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载商铺失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCategorySelect = (index: string) => {
  selectedCategory.value = index
  page.value = 1
  loadShops()
}

const handleSearch = () => {
  page.value = 1
  loadShops()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadShops()
}

const handlePageChange = (val: number) => {
  page.value = val
  loadShops()
}

const handleLogout = () => {
  userStore.clearAuth()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.shop-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40px;
}

/* 顶部导航 */
.top-nav {
  background: white;
  box-shadow: 0 1px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  font-size: 28px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #636e72;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f0f2f5;
  color: #2d3436;
}

.nav-icon {
  font-size: 16px;
}

.admin-link {
  color: #667eea;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px 6px 6px;
  border-radius: 30px;
  background: #f0f2f5;
  cursor: pointer;
  transition: all 0.2s;
}

.user-info:hover {
  background: #e8eaed;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #2d3436;
}

/* Hero Section */
.hero-section {
  position: relative;
  padding: 60px 32px 40px;
  text-align: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.hero-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23ffffff' fill-opacity='0.06' fill-rule='evenodd'/%3E%3C/svg%3E");
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
  margin: 0 auto;
}

.hero-content h1 {
  color: white;
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 12px 0;
}

.hero-content p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  margin: 0 0 30px 0;
}

.search-box {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 50px;
  padding: 6px 6px 6px 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  max-width: 500px;
  margin: 0 auto;
}

.search-icon {
  font-size: 18px;
  margin-right: 10px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  padding: 8px 0;
}

.search-btn {
  border-radius: 50px !important;
  padding: 10px 28px !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
}

/* 分类区域 */
.category-section {
  background: white;
  padding: 16px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.category-scroll {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  display: flex;
  gap: 12px;
  overflow-x: auto;
  scrollbar-width: none;
}

.category-scroll::-webkit-scrollbar {
  display: none;
}

.category-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 25px;
  background: #f5f7fa;
  cursor: pointer;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
  color: #636e72;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.category-tag:hover {
  background: #edf2ff;
  color: #667eea;
}

.category-tag.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.cat-icon {
  font-size: 18px;
}

/* 商铺列表区域 */
.shop-section {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 32px;
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #2d3436;
}

.shop-count {
  font-size: 14px;
  color: #b2bec3;
}

/* 商铺卡片网格 */
.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.shop-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.shop-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.card-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.shop-img {
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
}

.shop-card:hover .shop-img {
  transform: scale(1.05);
}

.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #dfe6e9 0%, #b2bec3 100%);
  font-size: 48px;
}

.card-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
}

.card-body {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.shop-name {
  font-size: 17px;
  font-weight: 600;
  color: #2d3436;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 15px;
  flex-shrink: 0;
}

.rating-star {
  font-size: 12px;
}

.rating-num {
  font-size: 13px;
  font-weight: 700;
  color: #d63031;
}

.shop-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  padding: 3px 10px;
  background: #f0f2f5;
  border-radius: 12px;
  font-size: 12px;
  color: #636e72;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.shop-address {
  font-size: 12px;
  color: #b2bec3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 10px;
}

.shop-sales {
  font-size: 12px;
  color: #636e72;
  font-weight: 500;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state p {
  color: #b2bec3;
  font-size: 16px;
  margin: 0 0 20px 0;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 响应式 */
@media (max-width: 768px) {
  .nav-right .nav-item span:not(.nav-icon) {
    display: none;
  }
  
  .hero-content h1 {
    font-size: 24px;
  }
  
  .shop-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
  }
  
  .shop-section {
    padding: 20px 16px;
  }
}
</style>
