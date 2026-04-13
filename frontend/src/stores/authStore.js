import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/authApi'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || null)
  const username = ref(localStorage.getItem('admin_username') || null)

  const isAuthenticated = computed(() => !!token.value)

  async function login(credentials) {
    const response = await authApi.login(credentials)
    token.value = response.token
    username.value = credentials.username
    localStorage.setItem('admin_token', response.token)
    localStorage.setItem('admin_username', credentials.username)
    return response
  }

  function logout() {
    token.value = null
    username.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
  }

  return { token, username, isAuthenticated, login, logout }
})
