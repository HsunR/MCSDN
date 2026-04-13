import { defineStore } from 'pinia'
import { ref } from 'vue'
import { articleApi } from '../api/articleApi'

export const useArticleStore = defineStore('article', () => {
  const articles = ref([])
  const currentArticle = ref(null)
  const loading = ref(false)
  const error = ref(null)

  async function fetchArticles() {
    loading.value = true
    error.value = null
    try {
      articles.value = await articleApi.getAll()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchArticle(id) {
    loading.value = true
    error.value = null
    try {
      currentArticle.value = await articleApi.getById(id)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function createArticle(data) {
    return await articleApi.create(data)
  }

  async function updateArticle(id, data) {
    return await articleApi.update(id, data)
  }

  async function deleteArticle(id) {
    return await articleApi.delete(id)
  }

  return { articles, currentArticle, loading, error, fetchArticles, fetchArticle, createArticle, updateArticle, deleteArticle }
})
