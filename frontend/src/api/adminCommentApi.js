import axios from 'axios'

const getAuthHeaders = () => {
  const token = localStorage.getItem('admin_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const http = axios.create({
  baseURL: '/api'
})

// Request interceptor adds JWT token
http.interceptors.request.use(config => {
  const headers = getAuthHeaders()
  Object.assign(config.headers, headers)
  return config
})

// Response interceptor handles 401 (redirect to login)
http.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token')
      window.location.hash = '#/admin/login'
    }
    return Promise.reject(error)
  }
)

export const adminCommentApi = {
  async getAll(status) {
    const params = status ? { status } : {}
    const response = await http.get('/admin/comments', { params })
    return response.data
  },
  async approve(id) {
    const response = await http.patch(`/admin/comments/${id}/approve`)
    return response.data
  },
  async reject(id) {
    const response = await http.patch(`/admin/comments/${id}/reject`)
    return response.data
  },
  async delete(id) {
    await http.delete(`/admin/comments/${id}`)
  }
}
