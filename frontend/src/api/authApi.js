import axios from 'axios'

const http = axios.create({
  baseURL: '/api'
})

export const authApi = {
  async login({ username, password }) {
    const response = await http.post('/auth/login', { username, password })
    return response.data
  },
  async logout() {
    const response = await http.post('/auth/logout')
    return response.data
  }
}
