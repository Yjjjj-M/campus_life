<template>
  <div class="user-order-list">
    <el-container>
      <el-header>
        <div class="header-content">
          <el-button @click="$router.push('/shop')" :icon="ArrowLeft">返回商铺</el-button>
          <h2>我的订单</h2>
        </div>
      </el-header>

      <el-main>
        <!-- 状态筛选 -->
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待支付" name="1" />
          <el-tab-pane label="已支付" name="2" />
          <el-tab-pane label="已使用" name="3" />
          <el-tab-pane label="已取消" name="4" />
        </el-tabs>

        <!-- 订单列表 -->
        <div class="order-list">
          <el-card
            v-for="order in orders"
            :key="order.id"
            class="order-card"
            shadow="hover"
          >
            <div class="order-header">
              <span class="order-no">订单号: {{ order.orderNo }}</span>
              <el-tag :type="getStatusType(order.status)" size="small">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>

            <div class="order-body">
              <div class="order-info">
                <h3>{{ order.voucherTitle }}</h3>
                <p>{{ order.shopName }}</p>
              </div>
              <div class="order-price">
                <div class="actual-price">¥{{ (order.actualValue / 100).toFixed(2) }}</div>
                <div class="pay-price">实付: ¥{{ (order.payValue / 100).toFixed(2) }}</div>
              </div>
            </div>

            <div class="order-footer">
              <span class="order-time">{{ formatTime(order.createTime) }}</span>
              <div class="order-actions">
                <el-button
                  v-if="order.status === 1"
                  type="primary"
                  size="small"
                  @click="handlePay(order)"
                >
                  立即支付
                </el-button>
                <el-button
                  v-if="order.status === 1"
                  size="small"
                  @click="handleCancel(order)"
                >
                  取消订单
                </el-button>
                <el-button
                  size="small"
                  @click="handleViewDetail(order)"
                >
                  查看详情
                </el-button>
              </div>
            </div>
          </el-card>

          <el-empty v-if="orders.length === 0" description="暂无订单" />
        </div>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { userOrderApi, type Order } from '../../api/order'

const router = useRouter()
const orders = ref<Order[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('all')

// 加载订单列表
const loadOrders = async () => {
  try {
    const status = activeTab.value === 'all' ? undefined : parseInt(activeTab.value)
    const res = await userOrderApi.getOrders(page.value, pageSize.value, status)
    orders.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  }
}

// Tab切换
const handleTabChange = () => {
  page.value = 1
  loadOrders()
}

// 分页大小改变
const handleSizeChange = () => {
  loadOrders()
}

// 页码改变
const handlePageChange = () => {
  loadOrders()
}

// 支付订单
const handlePay = async (order: Order) => {
  try {
    await ElMessageBox.confirm(
      `确认支付订单 ${order.orderNo}？\n实付金额: ¥${(order.payValue / 100).toFixed(2)}`,
      '支付确认',
      { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'info' }
    )
    await userOrderApi.payOrder(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
    }
  }
}

// 取消订单
const handleCancel = async (order: Order) => {
  try {
    await ElMessageBox.confirm(
      `确认取消订单 ${order.orderNo}？`,
      '取消确认',
      { confirmButtonText: '确认取消', cancelButtonText: '返回', type: 'warning' }
    )
    await userOrderApi.cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
}

// 查看详情
const handleViewDetail = (order: Order) => {
  router.push(`/user/order/${order.id}`)
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
.user-order-list {
  min-height: 100vh;
  background: #f5f7fa;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

h2 {
  margin: 0;
}

.order-list {
  margin-top: 20px;
}

.order-card {
  margin-bottom: 16px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.order-no {
  font-size: 14px;
  color: #909399;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.order-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.order-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.order-price {
  text-align: right;
}

.actual-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.pay-price {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.order-time {
  font-size: 13px;
  color: #909399;
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
