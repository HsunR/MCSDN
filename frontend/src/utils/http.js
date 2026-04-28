import axios from 'axios'
import { useAuthStore } from '../stores/authStore'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

http.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.hash = '#/admin/login'
    }
    return Promise.reject(error)
  }
)

export const adminHttp = http

export const publicHttp = axios.create({
  baseURL: '/api',
  timeout: 10000
})
