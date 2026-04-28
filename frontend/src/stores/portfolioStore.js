import { defineStore } from 'pinia'
import { ref } from 'vue'
import { portfolioApi } from '../api/portfolioApi'

export const usePortfolioStore = defineStore('portfolio', () => {
  const portfolios = ref([])
  const currentPortfolio = ref(null)
  const loading = ref(false)
  const error = ref(null)

  async function fetchPortfolios() {
    loading.value = true
    error.value = null
    try {
      portfolios.value = await portfolioApi.getAll()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchPortfolio(id) {
    loading.value = true
    error.value = null
    try {
      currentPortfolio.value = await portfolioApi.getById(id)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function createPortfolio(data) {
    return await portfolioApi.create(data)
  }

  async function updatePortfolio(id, data) {
    return await portfolioApi.update(id, data)
  }

  async function deletePortfolio(id) {
    return await portfolioApi.delete(id)
  }

  async function fetchPublished() {
    loading.value = true
    error.value = null
    try {
      portfolios.value = await portfolioApi.getPublished()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  return { portfolios, currentPortfolio, loading, error, fetchPortfolios, fetchPortfolio, createPortfolio, updatePortfolio, deletePortfolio, fetchPublished }
})
