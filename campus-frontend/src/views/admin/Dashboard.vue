<template>
  <div class="admin-container">
    <el-container style="height: 100vh">
      <el-aside width="200px">
        <el-menu :default-active="$route.path" router>
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/shops">
            <el-icon><Shop /></el-icon>
            <span>商铺管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Menu /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/vouchers">
            <el-icon><Ticket /></el-icon>
            <span>优惠券管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-content">
            <h2>管理后台</h2>
            <div class="header-actions">
              <el-button @click="$router.push('/shop')">返回前台</el-button>
              <el-button type="danger" @click="handleLogout">退出登录</el-button>
            </div>
          </div>
        </el-header>

        <el-main>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover">
                <el-statistic title="商铺总数" :value="stats.shopCount">
                  <template #prefix>
                    <el-icon><Shop /></el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <el-statistic title="用户总数" :value="stats.userCount">
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <el-statistic title="订单总数" :value="stats.orderCount">
                  <template #prefix>
                    <el-icon><Document /></el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <el-statistic title="今日订单" :value="stats.todayOrderCount">
                  <template #prefix>
                    <el-icon><Calendar /></el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="12">
              <el-card shadow="hover">
                <el-statistic title="今日销售额" :value="stats.todayRevenue / 100" :precision="2">
                  <template #prefix>
                    <el-icon><Money /></el-icon>
                  </template>
                  <template #suffix>元</template>
                </el-statistic>
              </el-card>
            </el-col>
          </el-row>

          <el-card style="margin-top: 20px">
            <template #header>
              <h3>欢迎使用校园生活服务平台管理后台</h3>
            </template>
            <p>这里是管理后台的数据概览页面，您可以在左侧菜单中管理商铺、分类、优惠券等功能。</p>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Shop, Menu, Ticket, Document, Goods, User, Calendar, Money } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { getDashboardStats, type DashboardStats } from '../../api/dashboard'

const router = useRouter()
const userStore = useUserStore()

const stats = ref<DashboardStats>({
  shopCount: 0,
  userCount: 0,
  orderCount: 0,
  todayOrderCount: 0,
  todayRevenue: 0
})

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

onMounted(() => {
  loadStats()
})

const handleLogout = () => {
  userStore.clearAuth()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.admin-container {
  height: 100vh;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

h2 {
  margin: 0;
}

h3 {
  margin: 0;
}
</style>
