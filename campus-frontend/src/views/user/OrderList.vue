<template>
  <div class="orders-page">
    <!-- 顶部导航 -->
    <header class="top-nav">
      <div class="nav-content">
        <el-button @click="$router.push('/shop')" circle class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="nav-title">📋 我的订单</span>
      </div>
    </header>

    <main class="orders-main">
      <!-- 状态标签 -->
      <div class="tabs-bar">
        <div 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeTab === tab.value }"
          @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>

      <!-- 订单列表 -->
      <div class="order-list" v-if="orders.length > 0">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <!-- 订单头部 -->
          <div class="order-header">
            <div class="order-meta">
              <span class="order-no">🧾 {{ order.orderNo }}</span>
              <span class="order-time">{{ formatTime(order.createTime) }}</span>
            </div>
            <el-tag :type="getStatusType(order.status)" class="status-tag" effect="light">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>

          <!-- 订单内容 -->
          <div class="order-body" @click="handleViewDetail(order)">
            <div class="order-icon">{{ order.voucherId ? '🎫' : '🛒' }}</div>
            <div class="order-info">
              <h3>{{ order.voucherTitle }}</h3>
              <p>{{ order.shopName }}</p>
            </div>
            <div class="order-price">
              <div class="actual-price">¥{{ (order.payValue / 100).toFixed(2) }}</div>
              <div class="original-price" v-if="order.actualValue !== order.payValue">
                ¥{{ (order.actualValue / 100).toFixed(2) }}
              </div>
            </div>
          </div>

          <!-- 订单操作 -->
          <div class="order-actions" v-if="order.status === 1">
            <el-button type="primary" round @click.stop="handlePay(order)" class="pay-btn">
              💳 立即支付 ¥{{ (order.payValue / 100).toFixed(2) }}
            </el-button>
            <el-button round @click.stop="handleCancel(order)">
              取消订单
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="orders.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h2>暂无订单</h2>
        <p>快去选购商品或优惠券吧！</p>
        <el-button type="primary" round @click="$router.push('/shop')">🛍️ 去购物</el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadOrders"
          @current-change="loadOrders"
          background
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userOrderApi, type Order } from '../../api/order'

const router = useRouter()
const orders = ref<Order[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('all')

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: '1' },
  { label: '已支付', value: '2' },
  { label: '已使用', value: '3' },
  { label: '已取消', value: '4' }
]

const loadOrders = async () => {
  try {
    const status = activeTab.value === 'all' ? undefined : parseInt(activeTab.value)
    const res = await userOrderApi.getOrders(page.value, pageSize.value, status)
    orders.value = res.data.records
    total.value = typeof res.data.total === 'string' ? parseInt(res.data.total) : res.data.total
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败')
  }
}

const handleTabChange = (tab: string) => {
  activeTab.value = tab
  page.value = 1
  loadOrders()
}

const handleViewDetail = (order: Order) => {
  router.push(`/user/order/${order.id}`)
}

const handlePay = async (order: Order) => {
  try {
    await ElMessageBox.confirm(
      `确认支付订单 ${order.orderNo}？\n实付金额: ¥${(order.payValue / 100).toFixed(2)}`,
      '支付确认',
      { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'info' }
    )
    await userOrderApi.payOrder(order.id)
    ElMessage.success('支付成功 🎉')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') console.error('支付失败:', error)
  }
}

const handleCancel = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确认取消订单 ${order.orderNo}？`, '取消确认',
      { confirmButtonText: '确认取消', cancelButtonText: '返回', type: 'warning' })
    await userOrderApi.cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') console.error('取消订单失败:', error)
  }
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '待支付', 2: '已支付', 3: '已使用', 4: '已取消', 5: '已退款' }
  return map[status] || '未知'
}

const getStatusType = (status: number): '' | 'success' | 'warning' | 'danger' | 'info' => {
  const map: Record<number, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    1: 'warning', 2: 'success', 3: 'success', 4: 'info', 5: 'danger'
  }
  return map[status] || 'info'
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
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
  max-width: 800px;
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

.orders-main {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 32px 40px;
}

/* 标签栏 */
.tabs-bar {
  display: flex;
  gap: 8px;
  padding: 16px;
  background: white;
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow-x: auto;
}

.tab-item {
  padding: 10px 20px;
  border-radius: 25px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #636e72;
  white-space: nowrap;
  transition: all 0.2s;
}

.tab-item:hover {
  background: #f0f2f5;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 订单卡片 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f2f5;
}

.order-meta {
  display: flex;
  gap: 16px;
  align-items: center;
}

.order-no {
  font-size: 13px;
  color: #636e72;
  font-family: monospace;
}

.order-time {
  font-size: 12px;
  color: #b2bec3;
}

.status-tag {
  font-weight: 600;
}

.order-body {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  cursor: pointer;
}

.order-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #f8f9fa 0%, #edf2f7 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: #2d3436;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-info p {
  font-size: 13px;
  color: #b2bec3;
  margin: 0;
}

.order-price {
  text-align: right;
  flex-shrink: 0;
}

.actual-price {
  font-size: 22px;
  font-weight: 700;
  color: #ff6b6b;
}

.original-price {
  font-size: 13px;
  color: #b2bec3;
  text-decoration: line-through;
}

.order-actions {
  display: flex;
  gap: 10px;
  padding: 0 20px 16px;
  justify-content: flex-end;
}

.pay-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  font-weight: 600;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h2 {
  font-size: 20px;
  color: #2d3436;
  margin: 0 0 8px 0;
}

.empty-state p {
  color: #b2bec3;
  margin: 0 0 24px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
