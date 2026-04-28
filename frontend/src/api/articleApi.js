import { adminHttp } from '../utils/http'

export const articleApi = {
  async getAll() {
    const response = await adminHttp.get('/admin/articles')
    return response.data
  },
  async getById(id) {
    const response = await adminHttp.get(`/admin/articles/${id}`)
    return response.data
  },
  async create(data) {
    const response = await adminHttp.post('/admin/articles', data)
    return response.data
  },
  async update(id, data) {
    const response = await adminHttp.put(`/admin/articles/${id}`, data)
    return response.data
  },
  async delete(id) {
    await adminHttp.delete(`/admin/articles/${id}`)
  }
}
