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

export const articleApi = {
  async getAll() {
    const response = await http.get('/admin/articles')
    return response.data
  },
  async getById(id) {
    const response = await http.get(`/admin/articles/${id}`)
    return response.data
  },
  async create(data) {
    const response = await http.post('/admin/articles', data)
    return response.data
  },
  async update(id, data) {
    const response = await http.put(`/admin/articles/${id}`, data)
    return response.data
  },
  async delete(id) {
    await http.delete(`/admin/articles/${id}`)
  }
}
