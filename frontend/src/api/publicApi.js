import axios from 'axios'

const http = axios.create({
  baseURL: '/api'
})

export const publicApi = {
  // PUBL-01: Paginated article list
  getArticles(page = 1, pageSize = 10) {
    return http.get('/articles', { params: { page, pageSize } })
      .then(res => res.data)
  },

  // PUBL-02: Single article detail
  getArticle(id) {
    return http.get(`/articles/${id}`)
      .then(res => res.data)
  },

  // PUBL-03: Articles by category
  getByCategory(categorySlug, page = 1, pageSize = 10) {
    return http.get(`/articles/category/${categorySlug}`, { params: { page, pageSize } })
      .then(res => res.data)
  },

  // PUBL-04: Articles by tag
  getByTag(tagSlug, page = 1, pageSize = 10) {
    return http.get(`/articles/tag/${tagSlug}`, { params: { page, pageSize } })
      .then(res => res.data)
  },

  // PUBL-05: Search articles
  search(keyword, page = 1, pageSize = 10) {
    return http.get('/articles/search', { params: { q: keyword, page, pageSize } })
      .then(res => res.data)
  }
}
