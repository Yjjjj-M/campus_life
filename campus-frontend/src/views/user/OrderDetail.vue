<template>
  <div class="user-order-detail">
    <el-container>
      <el-header>
        <div class="header-content">
          <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
          <h2>订单详情</h2>
        </div>
      </el-header>

      <el-main>
        <el-skeleton :loading="loading" animated>
          <template #default>
            <div v-if="order">
              <!-- 订单状态 -->
              <el-card class="status-card">
                <div class="status-content">
                  <el-tag :type="getStatusType(order.status)" size="large">
                    {{ getStatusText(order.status) }}
                  </el-tag>
                  <div class="status-info">
                    <p v-if="order.status === 1">请尽快完成支付</p>
                    <p v-else-if="order.status === 2">订单已支付，请到店使用</p>
                    <p v-else-if="order.status === 3">订单已完成</p>
                    <p v-else-if="order.status === 4">订单已取消</p>
                  </div>
                </div>
              </el-card>

              <!-- 订单信息 -->
              <el-card class="info-card">
                <template #header>
                  <div class="card-header">
                    <span>订单信息</span>
                  </div>
                </template>

                <el-descriptions :column="2" border>
                  <el-descriptions-item label="订单编号">
                    {{ order.orderNo }}
                  </el-descriptions-item>
                  <el-descriptions-item label="下单时间">
                    {{ formatTime(order.createTime) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="商铺名称">
                    {{ order.shopName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="优惠券">
                    {{ order.voucherTitle }}
                  </el-descriptions-item>
                  <el-descriptions-item label="抵扣金额">
                    <span class="price">¥{{ (order.actualValue / 100).toFixed(2) }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="实付金额">
                    <span class="price highlight">¥{{ (order.payValue / 100).toFixed(2) }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item v-if="order.payTime" label="支付时间">
                    {{ formatTime(order.payTime) }}
                  </el-descriptions-item>
                  <el-descriptions-item v-if="order.useTime" label="使用时间">
                    {{ formatTime(order.useTime) }}
                  </el-descriptions-item>
                  <el-descriptions-item v-if="order.cancelTime" label="取消时间">
                    {{ formatTime(order.cancelTime) }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>

              <!-- 操作按钮 -->
              <el-card v-if="order.status === 1" class="action-card">
                <div class="action-buttons">
                  <el-button type="primary" size="large" @click="handlePay">
                    立即支付 ¥{{ (order.payValue / 100).toFixed(2) }}
                  </el-button>
                  <el-button size="large" @click="handleCancel">
                    取消订单
                  </el-button>
                </div>
              </el-card>
            </div>

            <el-empty v-else description="订单不存在" />
          </template>
        </el-skeleton>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { userOrderApi, type Order } from '../../api/order'

const route = useRoute()
const router = useRouter()
const order = ref<Order | null>(null)
const loading = ref(true)

// 加载订单详情
const loadOrder = async () => {
  try {
    const orderId = parseInt(route.params.id as string)
    const res = await userOrderApi.getOrderDetail(orderId)
    order.value = res.data
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 支付订单
const handlePay = async () => {
  if (!order.value) return
  try {
    await ElMessageBox.confirm(
      `确认支付订单 ${order.value.orderNo}？\n实付金额: ¥${(order.value.payValue / 100).toFixed(2)}`,
      '支付确认',
      { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'info' }
    )
    await userOrderApi.payOrder(order.value.id)
    ElMessage.success('支付成功')
    loadOrder()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
    }
  }
}

// 取消订单
const handleCancel = async () => {
  if (!order.value) return
  try {
    await ElMessageBox.confirm(
      `确认取消订单 ${order.value.orderNo}？`,
      '取消确认',
      { confirmButtonText: '确认取消', cancelButtonText: '返回', type: 'warning' }
    )
    await userOrderApi.cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    router.push('/user/orders')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
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
  loadOrder()
})
</script>

<style scoped>
.user-order-detail {
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

.status-card {
  margin-bottom: 16px;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.info-card {
  margin-bottom: 16px;
}

.card-header {
  font-weight: bold;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.price.highlight {
  font-size: 18px;
}

.action-card {
  margin-top: 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
