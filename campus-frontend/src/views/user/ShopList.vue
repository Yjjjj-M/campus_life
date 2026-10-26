<template>
  <div class="shop-list-container">
    <el-header>
      <div class="header-content">
        <h1>校园商铺列表</h1>
        <div class="header-actions">
          <el-button type="success" @click="$router.push('/shop/nearby')">
            附近商铺
          </el-button>
          <el-button type="warning" @click="$router.push('/feed')">
            好友动态
          </el-button>
          <el-button type="primary" @click="$router.push('/user/orders')">
            我的订单
          </el-button>
          <el-button v-if="userStore.role === 1" type="primary" @click="$router.push('/admin/dashboard')">
            管理后台
          </el-button>
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </el-header>

    <el-main>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card>
            <template #header>
              <h3>分类筛选</h3>
            </template>
            <el-menu :default-active="selectedCategory" @select="handleCategorySelect">
              <el-menu-item index="">全部分类</el-menu-item>
              <el-menu-item v-for="cat in categories" :key="cat.id" :index="String(cat.id)">
                {{ cat.name }}
              </el-menu-item>
            </el-menu>
          </el-card>
        </el-col>

        <el-col :span="18">
          <el-card>
            <template #header>
              <div class="shop-header">
                <el-input v-model="searchName" placeholder="搜索商铺名称" clearable style="width: 300px" />
                <el-button type="primary" @click="handleSearch">搜索</el-button>
              </div>
            </template>

            <el-row :gutter="20" v-if="shops.length > 0">
              <el-col :span="8" v-for="shop in shops" :key="shop.id">
                <el-card shadow="hover" class="shop-card" @click="$router.push(`/shop/${shop.id}`)">
                  <el-image :src="shop.images.split(',')[0] || 'https://via.placeholder.com/400x200?text=暂无图片'" fit="cover" class="shop-image">
                    <template #error>
                      <div class="image-placeholder">暂无图片</div>
                    </template>
                  </el-image>
                  <div class="shop-info">
                    <h4>{{ shop.name }}</h4>
                    <p class="shop-category">{{ shop.categoryName }}</p>
                    <div class="shop-meta">
                      <div class="score-display">
                        <el-rate :model-value="shop.score / 10" disabled :max="5" />
                        <span class="score-text">{{ (shop.score / 10).toFixed(1) }}</span>
                      </div>
                      <span class="sold-text">销量: {{ shop.sold }}</span>
                    </div>
                    <p class="shop-address">{{ shop.address }}</p>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-empty v-if="shops.length === 0 && !loading" description="暂无商铺" />

            <el-pagination
              v-model:current-page="page"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
              style="margin-top: 20px; justify-content: center"
            />
          </el-card>
        </el-col>
      </el-row>
    </el-main>
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
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

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
.shop-list-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h1 {
  margin: 0;
}

.shop-header {
  display: flex;
  gap: 10px;
  align-items: center;
}

.shop-card {
  cursor: pointer;
  margin-bottom: 20px;
}

.shop-image {
  width: 100%;
  height: 200px;
}

.image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.shop-info h4 {
  margin: 10px 0 5px 0;
}

.shop-category {
  color: #909399;
  font-size: 12px;
  margin: 5px 0;
}

.shop-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0;
}

.score-display {
  display: flex;
  align-items: center;
  gap: 6px;
}

.score-text {
  color: #e6a23c;
  font-weight: bold;
  font-size: 14px;
}

.sold-text {
  color: #909399;
  font-size: 12px;
}

.shop-address {
  color: #606266;
  font-size: 12px;
  margin: 5px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
