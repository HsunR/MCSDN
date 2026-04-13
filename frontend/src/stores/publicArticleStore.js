import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { publicApi } from '../api/publicApi'

export const usePublicArticleStore = defineStore('publicArticle', () => {
  const articles = ref([])
  const currentArticle = ref(null)
  const total = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(1)
  const loading = ref(false)
  const error = ref(null)
  const currentFilter = ref({ type: 'all', value: '' })  // 'all' | 'category' | 'tag' | 'search'

  // PUBL-06: Helper to generate excerpt from markdown (150 chars, stripped)
  function generateExcerpt(markdown, maxLength = 150) {
    // Use striptags to strip HTML/Markdown
    const striptags = window.striptags || (() => {
      // Fallback simple strip if not loaded
      return (str) => str.replace(/<[^>]*>/g, '').replace(/[#*`_~\[\]()]/g, '')
    })
    const plain = striptags(markdown)
      .replace(/[#*`_~\[\]()]/g, '')  // remove markdown symbols
      .replace(/\s+/g, ' ')
      .trim()
    if (plain.length <= maxLength) return plain
    return plain.substring(0, maxLength).replace(/\s+\S*$/, '') + '...'
  }

  // Fetch paginated article list
  async function fetchArticles(page = 1) {
    loading.value = true
    error.value = null
    try {
      const data = await publicApi.getArticles(page)
      articles.value = data.articles.map(a => ({
        ...a,
        excerpt: generateExcerpt(a.content)
      }))
      total.value = data.total
      totalPages.value = data.totalPages
      currentPage.value = data.page
      currentFilter.value = { type: 'all', value: '' }
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  // Fetch article detail
  async function fetchArticle(id) {
    loading.value = true
    error.value = null
    try {
      currentArticle.value = await publicApi.getArticle(id)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  // Fetch by category
  async function fetchByCategory(categorySlug, page = 1) {
    loading.value = true
    error.value = null
    try {
      const data = await publicApi.getByCategory(categorySlug, page)
      articles.value = data.articles.map(a => ({
        ...a,
        excerpt: generateExcerpt(a.content)
      }))
      total.value = data.total
      totalPages.value = data.totalPages
      currentPage.value = data.page
      currentFilter.value = { type: 'category', value: categorySlug }
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  // Fetch by tag
  async function fetchByTag(tagSlug, page = 1) {
    loading.value = true
    error.value = null
    try {
      const data = await publicApi.getByTag(tagSlug, page)
      articles.value = data.articles.map(a => ({
        ...a,
        excerpt: generateExcerpt(a.content)
      }))
      total.value = data.total
      totalPages.value = data.totalPages
      currentPage.value = data.page
      currentFilter.value = { type: 'tag', value: tagSlug }
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  // Search
  async function searchArticles(keyword, page = 1) {
    loading.value = true
    error.value = null
    try {
      const data = await publicApi.search(keyword, page)
      articles.value = data.articles.map(a => ({
        ...a,
        excerpt: generateExcerpt(a.content)
      }))
      total.value = data.total
      totalPages.value = data.totalPages
      currentPage.value = data.page
      currentFilter.value = { type: 'search', value: keyword }
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  return {
    articles, currentArticle, total, totalPages, currentPage, loading, error, currentFilter,
    fetchArticles, fetchArticle, fetchByCategory, fetchByTag, searchArticles, generateExcerpt
  }
})
