import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface UserInfo {
  id: number
  phone: string
  nickname: string
  role: number
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  
  // 从 localStorage 读取用户信息
  const storedUserInfo = localStorage.getItem('userInfo')
  const userInfo = ref<UserInfo | null>(storedUserInfo ? JSON.parse(storedUserInfo) : null)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    // 保存到 localStorage
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function clearAuth() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 使用 computed 让 role 响应式跟随 userInfo 变化
  const role = computed(() => userInfo.value?.role ?? 0)

  return {
    token,
    userInfo,
    role,
    setToken,
    setUserInfo,
    clearAuth
  }
})
