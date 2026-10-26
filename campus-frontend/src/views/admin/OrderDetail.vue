<template>
  <div class="admin-order-detail">
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
                      <p>订单编号: {{ order.orderNo }}</p>
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

                <!-- 用户信息 -->
                <el-card class="user-card">
                  <template #header>
                    <div class="card-header">
                      <span>用户信息</span>
                    </div>
                  </template>

                  <el-descriptions :column="2" border>
                    <el-descriptions-item label="用户昵称">
                      {{ order.userName }}
                    </el-descriptions-item>
                    <el-descriptions-item label="手机号">
                      {{ order.userPhone }}
                    </el-descriptions-item>
                  </el-descriptions>
                </el-card>

                <!-- 操作按钮 -->
                <el-card v-if="order.status === 2" class="action-card">
                  <div class="action-buttons">
                    <el-button type="success" size="large" @click="handleUseOrder">
                      核销订单
                    </el-button>
                  </div>
                </el-card>
              </div>

              <el-empty v-else description="订单不存在" />
            </template>
          </el-skeleton>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { DataAnalysis, Shop, Menu, Ticket, Document, Goods } from '@element-plus/icons-vue'
import { adminOrderApi, type Order } from '../../api/order'

const route = useRoute()
const order = ref<Order | null>(null)
const loading = ref(true)

// 加载订单详情
const loadOrder = async () => {
  try {
    const orderId = parseInt(route.params.id as string)
    const res = await adminOrderApi.getOrderDetail(orderId)
    order.value = res.data
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 核销订单
const handleUseOrder = async () => {
  if (!order.value) return
  try {
    await ElMessageBox.confirm(
      `确认核销订单 ${order.value.orderNo}？`,
      '核销确认',
      { confirmButtonText: '确认核销', cancelButtonText: '取消', type: 'warning' }
    )
    await adminOrderApi.useOrder(order.value.id)
    ElMessage.success('核销成功')
    loadOrder()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('核销失败:', error)
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
.admin-order-detail {
  height: 100vh;
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

.info-card,
.user-card {
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
