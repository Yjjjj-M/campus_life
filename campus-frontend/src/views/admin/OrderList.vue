<template>
  <div class="admin-order-list">
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
            <h2>订单管理</h2>
            <div class="header-actions">
              <el-button @click="$router.push('/shop')">返回前台</el-button>
              <el-button type="danger" @click="handleLogout">退出登录</el-button>
            </div>
          </div>
        </el-header>

        <el-main>
          <!-- 搜索区域 -->
          <el-card class="search-card">
            <el-form :inline="true" :model="searchForm">
              <el-form-item label="订单号">
                <el-input
                  v-model="searchForm.orderNo"
                  placeholder="请输入订单号"
                  clearable
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="用户手机">
                <el-input
                  v-model="searchForm.userPhone"
                  placeholder="请输入手机号"
                  clearable
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="订单状态">
                <el-select
                  v-model="searchForm.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 120px"
                >
                  <el-option label="待支付" :value="1" />
                  <el-option label="已支付" :value="2" />
                  <el-option label="已使用" :value="3" />
                  <el-option label="已取消" :value="4" />
                  <el-option label="已退款" :value="5" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 订单表格 -->
          <el-card class="table-card">
            <el-table :data="orders" border v-loading="loading">
              <el-table-column prop="orderNo" label="订单号" width="180" />
              <el-table-column prop="voucherTitle" label="优惠券" min-width="150" />
              <el-table-column prop="shopName" label="商铺" width="120" />
              <el-table-column prop="userName" label="用户" width="100" />
              <el-table-column prop="userPhone" label="手机号" width="120" />
              <el-table-column label="金额" width="100" align="right">
                <template #default="{ row }">
                  <div>¥{{ (row.payValue / 100).toFixed(2) }}</div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="下单时间" width="160">
                <template #default="{ row }">
                  {{ formatTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleViewDetail(row)">
                    详情
                  </el-button>
                  <el-button
                    v-if="row.status === 2"
                    type="success"
                    link
                    size="small"
                    @click="handleUseOrder(row)"
                  >
                    核销
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <el-pagination
              v-model:current-page="page"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataAnalysis, Shop, Menu, Ticket, Document, Goods } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { adminOrderApi, type Order } from '../../api/order'

const router = useRouter()
const userStore = useUserStore()

const orders = ref<Order[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  orderNo: '',
  userPhone: '',
  status: undefined as number | undefined
})

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const res = await adminOrderApi.getOrders(
      page.value,
      pageSize.value,
      searchForm.status,
      searchForm.orderNo || undefined,
      searchForm.userPhone || undefined
    )
    orders.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  loadOrders()
}

// 重置
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.userPhone = ''
  searchForm.status = undefined
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  loadOrders()
}

// 页码改变
const handlePageChange = () => {
  loadOrders()
}

// 查看详情
const handleViewDetail = (order: Order) => {
  router.push(`/admin/order/${order.id}`)
}

// 核销订单
const handleUseOrder = async (order: Order) => {
  try {
    await ElMessageBox.confirm(
      `确认核销订单 ${order.orderNo}？`,
      '核销确认',
      { confirmButtonText: '确认核销', cancelButtonText: '取消', type: 'warning' }
    )
    await adminOrderApi.useOrder(order.id)
    ElMessage.success('核销成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('核销失败:', error)
    }
  }
}

// 退出登录
const handleLogout = () => {
  userStore.clearAuth()
  router.push('/login')
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '待支付',
    2: '已支付',
    3: '已使用',
    4: '已取消',
    5: '已退款'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'warning',
    2: 'success',
    3: 'info',
    4: 'info',
    5: 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化时间
const formatTime = (time: string) => {
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.admin-order-list {
  height: 100vh;
  background: #f5f7fa;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h2 {
  margin: 0;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
}
</style>
