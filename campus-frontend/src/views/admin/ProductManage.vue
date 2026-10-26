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
            <h2>商品管理</h2>
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
              <el-form-item label="商品名称">
                <el-input
                  v-model="searchForm.name"
                  placeholder="请输入商品名称"
                  clearable
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="searchForm.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 120px"
                >
                  <el-option label="上架" :value="1" />
                  <el-option label="下架" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="handleReset">重置</el-button>
                <el-button type="success" @click="handleAdd">新增商品</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 商品表格 -->
          <el-card class="table-card">
            <el-table :data="products" border v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="商品名称" min-width="150" />
              <el-table-column prop="shopName" label="商铺" width="120" />
              <el-table-column prop="categoryName" label="分类" width="100" />
              <el-table-column label="价格" width="100" align="right">
                <template #default="{ row }">
                  ¥{{ (row.price / 100).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="stock" label="库存" width="80" align="right" />
              <el-table-column prop="sales" label="销量" width="80" align="right" />
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                    {{ row.status === 1 ? '上架' : '下架' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleEdit(row)">
                    编辑
                  </el-button>
                  <el-button
                    v-if="row.status === 0"
                    type="success"
                    link
                    size="small"
                    @click="handleOnline(row)"
                  >
                    上架
                  </el-button>
                  <el-button
                    v-if="row.status === 1"
                    type="warning"
                    link
                    size="small"
                    @click="handleOffline(row)"
                  >
                    下架
                  </el-button>
                  <el-button type="danger" link size="small" @click="handleDelete(row)">
                    删除
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商铺" prop="shopId">
          <el-select v-model="form.shopId" placeholder="请选择商铺" style="width: 100%">
            <el-option
              v-for="shop in shops"
              :key="shop.id"
              :label="shop.name"
              :value="shop.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格（元）" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="图片URL" prop="image">
          <el-input v-model="form.image" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { DataAnalysis, Shop, Menu, Ticket, Document, Goods } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { adminProductApi, adminProductCategoryApi, type Product, type ProductCategory } from '../../api/product'
import { getAllShops, type Shop as ShopType } from '../../api/shop'

const router = useRouter()
const userStore = useUserStore()

const products = ref<Product[]>([])
const shops = ref<ShopType[]>([])
const categories = ref<ProductCategory[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  name: '',
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增商品')
const formRef = ref<FormInstance>()
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  shopId: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  price: 0,
  stock: 100,
  image: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  shopId: [{ required: true, message: '请选择商铺', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// 加载商品列表
const loadProducts = async () => {
  loading.value = true
  try {
    const res = await adminProductApi.getProducts(
      page.value,
      pageSize.value,
      undefined,
      undefined,
      searchForm.name || undefined,
      searchForm.status
    )
    products.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

// 加载商铺列表
const loadShops = async () => {
  try {
    const res = await getAllShops()
    shops.value = res.data
  } catch (error) {
    console.error('加载商铺列表失败:', error)
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await adminProductCategoryApi.getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  loadProducts()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.status = undefined
  handleSearch()
}

// 分页
const handleSizeChange = () => {
  loadProducts()
}

const handlePageChange = () => {
  loadProducts()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增商品'
  form.id = undefined
  form.name = ''
  form.shopId = undefined
  form.categoryId = undefined
  form.price = 0
  form.stock = 100
  form.image = ''
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Product) => {
  dialogTitle.value = '编辑商品'
  form.id = row.id
  form.name = row.name
  form.shopId = row.shopId
  form.categoryId = row.categoryId
  form.price = row.price / 100
  form.stock = row.stock
  form.image = row.image || ''
  form.description = row.description || ''
  form.status = row.status
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    const data = {
      ...form,
      price: Math.round(form.price * 100)
    }

    try {
      if (form.id) {
        await adminProductApi.updateProduct(data as Product)
        ElMessage.success('修改成功')
      } else {
        await adminProductApi.addProduct(data as Product)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadProducts()
    } catch (error) {
      console.error('操作失败:', error)
    }
  })
}

// 上架
const handleOnline = async (row: Product) => {
  try {
    await adminProductApi.onlineProduct(row.id)
    ElMessage.success('上架成功')
    loadProducts()
  } catch (error) {
    console.error('上架失败:', error)
  }
}

// 下架
const handleOffline = async (row: Product) => {
  try {
    await adminProductApi.offlineProduct(row.id)
    ElMessage.success('下架成功')
    loadProducts()
  } catch (error) {
    console.error('下架失败:', error)
  }
}

// 删除
const handleDelete = async (row: Product) => {
  try {
    await ElMessageBox.confirm(
      `确认删除商品 "${row.name}"？`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
    await adminProductApi.deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 退出登录
const handleLogout = () => {
  userStore.clearAuth()
  router.push('/login')
}

onMounted(() => {
  loadProducts()
  loadShops()
  loadCategories()
})
</script>

<style scoped>
.admin-container {
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
