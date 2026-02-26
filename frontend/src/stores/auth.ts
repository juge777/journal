import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginRequest } from '@/api/auth'

const TOKEN_KEY = 'journal_token'
const USER_KEY = 'journal_user'

export interface User {
  userId: number
  username: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const user = ref<User | null>(
    localStorage.getItem(USER_KEY)
      ? JSON.parse(localStorage.getItem(USER_KEY)!)
      : null
  )
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  const login = async (username: string, password: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.login({ username, password })
      token.value = response.token
      user.value = {
        userId: response.userId,
        username: response.username
      }
      localStorage.setItem(TOKEN_KEY, response.token)
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    } catch (e) {
      error.value = e instanceof Error ? e.message : '登录失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  const logout = () => {
    token.value = null
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return {
    token,
    user,
    loading,
    error,
    isAuthenticated,
    login,
    logout
  }
})
