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
            <h2>优惠券管理</h2>
            <div class="header-actions">
              <el-button @click="$router.push('/shop')">返回前台</el-button>
              <el-button type="danger" @click="handleLogout">退出登录</el-button>
            </div>
          </div>
        </el-header>

        <el-main>
          <el-card>
            <template #header>
              <div class="voucher-header">
                <el-button type="primary" @click="handleAdd">
                  <el-icon><Plus /></el-icon>
                  新增优惠券
                </el-button>
                <div class="filter-group">
                  <el-select v-model="filterShopId" placeholder="按商铺筛选" clearable style="width: 160px" @change="handleSearch">
                    <el-option v-for="shop in shops" :key="shop.id" :label="shop.name" :value="shop.id" />
                  </el-select>
                  <el-select v-model="filterType" placeholder="按类型筛选" clearable style="width: 120px" @change="handleSearch">
                    <el-option label="普通券" :value="0" />
                    <el-option label="秒杀券" :value="1" />
                  </el-select>
                  <el-select v-model="filterStatus" placeholder="按状态筛选" clearable style="width: 120px" @change="handleSearch">
                    <el-option label="上架" :value="1" />
                    <el-option label="下架" :value="2" />
                    <el-option label="过期" :value="3" />
                  </el-select>
                  <el-button @click="handleReset">重置</el-button>
                </div>
              </div>
            </template>

            <el-table :data="vouchers" border v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
              <el-table-column label="商铺" width="120">
                <template #default="{ row }">
                  {{ getShopName(row.shopId) }}
                </template>
              </el-table-column>
              <el-table-column label="类型" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.type === 1 ? 'danger' : 'primary'" size="small">
                    {{ row.type === 1 ? '秒杀券' : '普通券' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="价格" width="120" align="center">
                <template #default="{ row }">
                  <div>
                    <div style="color: #f56c6c">¥{{ (row.payValue / 100).toFixed(2) }}</div>
                    <div style="font-size: 12px; color: #909399; text-decoration: line-through">
                      ¥{{ (row.actualValue / 100).toFixed(2) }}
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="stock" label="库存" width="80" align="center" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag 
                    :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'" 
                    size="small"
                  >
                    {{ row.status === 1 ? '上架' : row.status === 2 ? '下架' : '过期' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="时间" width="160" v-if="filterType === 1">
                <template #default="{ row }">
                  <div v-if="row.type === 1" style="font-size: 12px">
                    <div>{{ formatDateTime(row.beginTime) }}</div>
                    <div>至 {{ formatDateTime(row.endTime) }}</div>
                  </div>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button 
                    v-if="row.status === 2" 
                    type="success" 
                    link 
                    @click="handlePublish(row)"
                  >
                    上架
                  </el-button>
                  <el-button 
                    v-if="row.status === 1" 
                    type="warning" 
                    link 
                    @click="handleUnpublish(row)"
                  >
                    下架
                  </el-button>
                  <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                  <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              v-model:current-page="page"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadVouchers"
              @current-change="loadVouchers"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </el-card>
        </el-main>
      </el-container>
    </el-container>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑优惠券' : '新增优惠券'"
      width="700px"
      @close="handleDialogClose"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="商铺" prop="shopId">
          <el-select v-model="formData.shopId" placeholder="请选择商铺" style="width: 100%">
            <el-option v-for="shop in shops" :key="shop.id" :label="shop.name" :value="shop.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入优惠券标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="formData.subTitle" placeholder="请输入副标题（可选）" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="使用规则">
          <el-input 
            v-model="formData.rules" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入使用规则（可选）" 
            maxlength="500" 
            show-word-limit 
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="实付金额" prop="payValue">
              <el-input-number 
                v-model="formData.payValue" 
                :min="1" 
                :max="999999" 
                :step="100" 
                controls-position="right" 
                style="width: 100%" 
              />
              <div class="form-tip">单位：分（100 = ¥1）</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抵扣金额" prop="actualValue">
              <el-input-number 
                v-model="formData.actualValue" 
                :min="1" 
                :max="999999" 
                :step="100" 
                controls-position="right" 
                style="width: 100%" 
              />
              <div class="form-tip">单位：分（必须大于实付金额）</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-radio-group v-model="formData.type" @change="handleTypeChange">
                <el-radio :label="0">普通券</el-radio>
                <el-radio :label="1">秒杀券</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number 
                v-model="formData.stock" 
                :min="0" 
                :max="99999" 
                controls-position="right" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 秒杀券时间设置 -->
        <template v-if="formData.type === 1">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开始时间" prop="beginTime">
                <el-date-picker
                  v-model="formData.beginTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束时间" prop="endTime">
                <el-date-picker
                  v-model="formData.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { DataAnalysis, Shop, Menu, Ticket, Document, Goods, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import {
  getAdminVoucherPage,
  createVoucher,
  updateVoucher,
  deleteVoucher,
  updateVoucherStatus,
  type Voucher
} from '../../api/voucher'
import { getAdminShopPage, type Shop as ShopType } from '../../api/shop'

const router = useRouter()
const userStore = useUserStore()

// 表格数据
const vouchers = ref<Voucher[]>([])
const shops = ref<ShopType[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选
const filterShopId = ref<number | undefined>(undefined)
const filterType = ref<number | undefined>(undefined)
const filterStatus = ref<number | undefined>(undefined)

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const defaultFormData: Voucher = {
  shopId: 0,
  title: '',
  subTitle: '',
  rules: '',
  payValue: 100,
  actualValue: 200,
  type: 0,
  status: 1,
  stock: 100
}

const formData = reactive<Voucher>({ ...defaultFormData })

// 表单验证规则
const formRules: FormRules = {
  shopId: [{ required: true, message: '请选择商铺', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 50, message: '标题不能超过50个字符', trigger: 'blur' }
  ],
  payValue: [{ required: true, message: '请输入实付金额', trigger: 'blur' }],
  actualValue: [{ required: true, message: '请输入抵扣金额', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  beginTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// 加载商铺列表
const loadShops = async () => {
  try {
    const res = await getAdminShopPage({ page: 1, size: 100 })
    shops.value = res.data.records
  } catch (error) {
    console.error('加载商铺列表失败:', error)
  }
}

// 加载优惠券列表
const loadVouchers = async () => {
  loading.value = true
  try {
    const res = await getAdminVoucherPage({
      page: page.value,
      size: pageSize.value,
      shopId: filterShopId.value,
      type: filterType.value,
      status: filterStatus.value
    })
    vouchers.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载优惠券列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadVouchers()
}

const handleReset = () => {
  filterShopId.value = undefined
  filterType.value = undefined
  filterStatus.value = undefined
  handleSearch()
}

// 获取商铺名称
const getShopName = (shopId: number) => {
  const shop = shops.value.find(s => s.id === shopId)
  return shop ? shop.name : '-'
}

// 格式化日期时间
const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 16)
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, { ...defaultFormData })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Voucher) => {
  isEdit.value = true
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: Voucher) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除优惠券"${row.title}"吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteVoucher(row.id!)
    ElMessage.success('删除成功')
    loadVouchers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 上架
const handlePublish = async (row: Voucher) => {
  try {
    await ElMessageBox.confirm(
      `确定要上架优惠券"${row.title}"吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
    await updateVoucherStatus(row.id!, 1)
    ElMessage.success('上架成功')
    loadVouchers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('上架失败:', error)
    }
  }
}

// 下架
const handleUnpublish = async (row: Voucher) => {
  try {
    await ElMessageBox.confirm(
      `确定要下架优惠券"${row.title}"吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await updateVoucherStatus(row.id!, 2)
    ElMessage.success('下架成功')
    loadVouchers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
    }
  }
}

// 类型切换时清空时间字段
const handleTypeChange = (type: number) => {
  if (type === 0) {
    formData.beginTime = undefined
    formData.endTime = undefined
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    // 额外验证：抵扣金额必须大于实付金额
    if (formData.actualValue <= formData.payValue) {
      ElMessage.error('抵扣金额必须大于实付金额')
      return
    }
    
    // 额外验证：秒杀券时间
    if (formData.type === 1) {
      if (!formData.beginTime || !formData.endTime) {
        ElMessage.error('秒杀券必须设置开始和结束时间')
        return
      }
      if (new Date(formData.endTime) <= new Date(formData.beginTime)) {
        ElMessage.error('结束时间必须晚于开始时间')
        return
      }
    }
    
    submitting.value = true

    if (isEdit.value) {
      await updateVoucher(formData)
      ElMessage.success('修改成功')
    } else {
      await createVoucher(formData)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadVouchers()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 退出登录
const handleLogout = () => {
  userStore.clearAuth()
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(async () => {
  await loadShops()
  await loadVouchers()
})
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

.voucher-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>
