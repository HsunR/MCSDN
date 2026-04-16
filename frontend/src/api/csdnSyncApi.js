import axios from 'axios'

const http = axios.create({
  baseURL: '/api'
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function getSyncConfig() {
  return http.get('/admin/csdn-sync/config')
}

export function saveSyncConfig(data) {
  return http.post('/admin/csdn-sync/config', data)
}

export function triggerSync() {
  return http.post('/admin/csdn-sync/sync')
}
