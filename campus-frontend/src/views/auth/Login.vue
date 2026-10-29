<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-wrapper">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-icon">🎓</div>
          <h1>校园生活平台</h1>
          <p>探索校园美食，享受优惠生活</p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">🍜</span>
              <span>精选校园美食</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🎁</span>
              <span>超值优惠券</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span>限时秒杀活动</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-section">
        <div class="form-container">
          <h2>欢迎回来</h2>
          <p class="form-subtitle">请登录你的账户</p>

          <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
            <el-form-item prop="phone">
              <div class="input-group">
                <span class="input-icon">📱</span>
                <el-input 
                  v-model="form.phone" 
                  placeholder="请输入手机号" 
                  size="large"
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="password">
              <div class="input-group">
                <span class="input-icon">🔒</span>
                <el-input 
                  v-model="form.password" 
                  type="password" 
                  placeholder="请输入密码" 
                  size="large"
                  show-password
                />
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                size="large"
                @click="handleLogin" 
                :loading="loading" 
                class="login-btn"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
            
            <div class="form-footer">
              <span>还没有账户？</span>
              <el-button text type="primary" @click="$router.push('/register')">
                立即注册
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { loginApi } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const res = await loginApi(form)
      
      userStore.setToken(res.data.token)
      userStore.setUserInfo({
        id: res.data.userId,
        phone: form.phone,
        nickname: res.data.nickname,
        role: res.data.role
      })
      
      ElMessage.success('登录成功，欢迎回来！')
      
      const redirect = route.query.redirect as string
      if (res.data.role === 1) {
        router.push(redirect || '/admin/dashboard')
      } else {
        router.push(redirect || '/shop')
      }
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰圆 */
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: white;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
  animation: float 8s ease-in-out infinite reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 30%;
  animation: float 7s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

/* 登录容器 */
.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.brand-content {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
}

.logo-icon {
  font-size: 72px;
  margin-bottom: 20px;
  display: inline-block;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.brand-content h1 {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 12px 0;
  letter-spacing: 2px;
}

.brand-content p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 40px 0;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  font-size: 15px;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateX(5px);
}

.feature-icon {
  font-size: 24px;
}

/* 右侧表单区域 */
.form-section {
  flex: 1;
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-container {
  width: 100%;
  max-width: 360px;
}

.form-container h2 {
  font-size: 28px;
  font-weight: 700;
  color: #2d3436;
  margin: 0 0 8px 0;
}

.form-subtitle {
  color: #636e72;
  margin: 0 0 40px 0;
  font-size: 15px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.input-group {
  display: flex;
  align-items: center;
  width: 100%;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 4px 16px;
  transition: all 0.3s ease;
}

.input-group:focus-within {
  background: white;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.input-icon {
  font-size: 20px;
  margin-right: 12px;
}

.input-group :deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  padding: 8px 0;
}

.login-btn {
  width: 100%;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 12px !important;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4) !important;
}

.form-footer {
  text-align: center;
  color: #636e72;
  font-size: 14px;
}

.form-footer :deep(.el-button) {
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    max-width: 400px;
    min-height: auto;
  }
  
  .brand-section {
    padding: 40px 20px;
  }
  
  .brand-content h1 {
    font-size: 24px;
  }
  
  .brand-features {
    display: none;
  }
  
  .form-section {
    padding: 40px 30px;
  }
}
</style>
