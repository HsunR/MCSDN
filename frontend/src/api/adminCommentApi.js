import { adminHttp } from '../utils/http'

export const adminCommentApi = {
  async getAll(status) {
    const params = status ? { status } : {}
    const response = await adminHttp.get('/admin/comments', { params })
    return response.data
  },
  async approve(id) {
    const response = await adminHttp.patch(`/admin/comments/${id}/approve`)
    return response.data
  },
  async reject(id) {
    const response = await adminHttp.patch(`/admin/comments/${id}/reject`)
    return response.data
  },
  async delete(id) {
    await adminHttp.delete(`/admin/comments/${id}`)
  }
}
