import { adminHttp, publicHttp } from '../utils/http'

export const portfolioApi = {
  async getAll() {
    const response = await adminHttp.get('/admin/portfolios')
    return response.data
  },
  async getById(id) {
    const response = await adminHttp.get(`/admin/portfolios/${id}`)
    return response.data
  },
  async create(data) {
    const response = await adminHttp.post('/admin/portfolios', data)
    return response.data
  },
  async update(id, data) {
    const response = await adminHttp.put(`/admin/portfolios/${id}`, data)
    return response.data
  },
  async delete(id) {
    await adminHttp.delete(`/admin/portfolios/${id}`)
  },
  async getPublished() {
    const response = await publicHttp.get('/admin/portfolios/public')
    return response.data
  }
}
