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
            <h2>分类管理</h2>
            <div class="header-actions">
              <el-button @click="$router.push('/shop')">返回前台</el-button>
              <el-button type="danger" @click="handleLogout">退出登录</el-button>
            </div>
          </div>
        </el-header>

        <el-main>
          <!-- 操作栏 -->
          <el-card class="search-card">
            <el-row :gutter="20" align="middle">
              <el-col :span="12">
                <el-button type="primary" @click="handleAdd">
                  <el-icon><Plus /></el-icon>
                  新增分类
                </el-button>
              </el-col>
              <el-col :span="12" style="text-align: right">
                <el-button @click="loadData">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </el-col>
            </el-row>
          </el-card>

          <!-- 表格 -->
          <el-card class="table-card">
            <el-table :data="tableData" v-loading="loading" border stripe>
              <el-table-column prop="id" label="ID" width="80" align="center" />
              <el-table-column prop="name" label="分类名称" min-width="150" />
              <el-table-column prop="icon" label="图标" width="100" align="center">
                <template #default="{ row }">
                  <span v-if="row.icon">{{ row.icon }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="sort" label="排序" width="100" align="center" />
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="180" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                  <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <el-pagination
              v-model:current-page="page"
              v-model:page-size="size"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadData"
              @current-change="loadData"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </el-card>
        </el-main>
      </el-container>
    </el-container>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="500px"
      @close="handleDialogClose"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入分类名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="图标名称" prop="icon">
          <el-select v-model="formData.icon" placeholder="选择图标" clearable style="width: 100%">
            <el-option label="餐厅 (Restaurant)" value="Restaurant" />
            <el-option label="购物车 (ShoppingCart)" value="ShoppingCart" />
            <el-option label="苹果 (Apple)" value="Apple" />
            <el-option label="咖啡杯 (CoffeeCup)" value="CoffeeCup" />
            <el-option label="阅读 (Reading)" value="Reading" />
            <el-option label="足球 (Football)" value="Football" />
            <el-option label="医药箱 (FirstAidKit)" value="FirstAidKit" />
            <el-option label="打印机 (Printer)" value="Printer" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="9999"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
import { DataAnalysis, Shop, Menu, Ticket, Document, Goods, Plus, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import {
  getAdminCategoryPage,
  addCategory,
  updateCategory,
  deleteCategory,
  type ShopCategory
} from '../../api/category'

const router = useRouter()
const userStore = useUserStore()

// 表格数据
const tableData = ref<ShopCategory[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = reactive<ShopCategory>({
  name: '',
  icon: '',
  sort: 0,
  status: 1
})

const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminCategoryPage(page.value, size.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  formData.name = ''
  formData.icon = ''
  formData.sort = 0
  formData.status = 1
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: ShopCategory) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: ShopCategory) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.name}"吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteCategory(row.id!)
    ElMessage.success('删除成功')
    loadData()
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
      await updateCategory(formData)
      ElMessage.success('修改成功')
    } else {
      await addCategory(formData)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    loadData()
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

onMounted(() => {
  loadData()
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}
</style>
