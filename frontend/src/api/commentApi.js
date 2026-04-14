import axios from 'axios'

const http = axios.create({
  baseURL: '/api'
})

// CMNT-05: Get approved comments for an article
export const commentApi = {
  getApproved(articleId) {
    return http.get(`/articles/${articleId}/comments`)
      .then(res => res.data)
  },

  // CMNT-01: Submit a new comment (public - no auth required)
  submit(articleId, data) {
    return http.post(`/articles/${articleId}/comments`, {
      authorName: data.authorName,
      content: data.content,
      honeypot: data.honeypot || ''
    }).then(res => res.data)
  }
}
