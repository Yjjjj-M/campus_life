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
            <h2>商铺管理</h2>
            <div class="header-actions">
              <el-button @click="$router.push('/shop')">返回前台</el-button>
              <el-button type="danger" @click="handleLogout">退出登录</el-button>
            </div>
          </div>
        </el-header>

        <el-main>
          <el-card>
            <template #header>
              <div class="shop-header">
                <el-button type="primary" @click="handleAdd">
                  <el-icon><Plus /></el-icon>
                  新增商铺
                </el-button>
                <el-select v-model="searchCategoryId" placeholder="按分类筛选" clearable style="width: 160px" @change="handleSearch">
                  <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
                </el-select>
                <el-input v-model="searchName" placeholder="搜索商铺名称" clearable style="width: 240px" @keyup.enter="handleSearch" />
                <el-button type="primary" @click="handleSearch">搜索</el-button>
              </div>
            </template>

            <el-table :data="shops" border v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="商铺名称" min-width="140" />
              <el-table-column prop="categoryName" label="分类" width="120" />
              <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
              <el-table-column label="评分" width="80" align="center">
                <template #default="{ row }">
                  {{ row.score ? (row.score / 10).toFixed(1) : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="人均" width="80" align="center">
                <template #default="{ row }">
                  {{ row.avgPrice ? '¥' + (row.avgPrice / 100).toFixed(0) : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="sold" label="销量" width="80" align="center" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                    {{ row.status === 1 ? '营业中' : '休息中' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" fixed="right" align="center">
                <template #default="{ row }">
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
              @size-change="loadShops"
              @current-change="loadShops"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </el-card>
        </el-main>
      </el-container>
    </el-container>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商铺' : '新增商铺'"
      width="650px"
      @close="handleDialogClose"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="商铺名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入商铺名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片URL" prop="images">
          <el-input v-model="formData.images" type="textarea" :rows="2" placeholder="图片URL，多张图片用逗号分隔" />
          <div class="form-tip">暂时使用URL地址，多张图片用英文逗号分隔</div>
        </el-form-item>
        <el-form-item label="商圈/区域">
          <el-input v-model="formData.area" placeholder="如：东区食堂旁" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="人均价格">
              <el-input-number v-model="formData.avgPrice" :min="0" :max="999999" :step="100" controls-position="right" style="width: 100%" />
              <div class="form-tip">单位：分（100 = ¥1）</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评分">
              <el-input-number v-model="formData.score" :min="0" :max="50" :step="1" controls-position="right" style="width: 100%" />
              <div class="form-tip">1-50（显示时除以10，如45=4.5分）</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="营业时间">
          <el-input v-model="formData.openHours" placeholder="如：08:00-22:00" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">营业中</el-radio>
            <el-radio :label="0">休息中</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { DataAnalysis, Shop, Menu, Ticket, Document, Goods, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import {
  getAdminShopPage,
  addShop,
  updateShop,
  deleteShop,
  getShopCategoryList,
  type Shop as ShopType,
  type ShopCategory
} from '../../api/shop'

const router = useRouter()
const userStore = useUserStore()

// 表格数据
const shops = ref<ShopType[]>([])
const categories = ref<ShopCategory[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索
const searchName = ref('')
const searchCategoryId = ref<number | undefined>(undefined)

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const defaultFormData: ShopType = {
  name: '',
  categoryId: 0,
  images: '',
  area: '',
  address: '',
  avgPrice: 0,
  score: 0,
  sold: 0,
  status: 1,
  openHours: ''
}

const formData = reactive<ShopType>({ ...defaultFormData })

const formRules = {
  name: [
    { required: true, message: '请输入商铺名称', trigger: 'blur' },
    { max: 50, message: '商铺名称不能超过50个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  images: [
    { required: true, message: '请输入图片URL', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getShopCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

// 加载商铺列表
const loadShops = async () => {
  loading.value = true
  try {
    const res = await getAdminShopPage({
      page: page.value,
      size: pageSize.value,
      name: searchName.value || undefined,
      categoryId: searchCategoryId.value || undefined
    })
    shops.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载商铺列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadShops()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, { ...defaultFormData })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: ShopType) => {
  isEdit.value = true
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: ShopType) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商铺"${row.name}"吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteShop(row.id!)
    ElMessage.success('删除成功')
    loadShops()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      await updateShop(formData)
      ElMessage.success('修改成功')
    } else {
      await addShop(formData)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    loadShops()
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
  await loadCategories()
  await loadShops()
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

.shop-header {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>
