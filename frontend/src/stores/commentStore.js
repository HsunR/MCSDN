import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminCommentApi } from '../api/adminCommentApi'

export const useCommentStore = defineStore('comment', () => {
  const comments = ref([])
  const loading = ref(false)
  const pendingCount = ref(0)
  const error = ref(null)

  async function fetchAll(status) {
    loading.value = true
    error.value = null
    try {
      comments.value = await adminCommentApi.getAll(status)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function approve(id) {
    error.value = null
    try {
      await adminCommentApi.approve(id)
      await refreshCurrentTab()
    } catch (e) {
      error.value = e.message
    }
  }

  async function reject(id) {
    error.value = null
    try {
      await adminCommentApi.reject(id)
      await refreshCurrentTab()
    } catch (e) {
      error.value = e.message
    }
  }

  async function deleteComment(id) {
    error.value = null
    try {
      await adminCommentApi.delete(id)
      await refreshCurrentTab()
    } catch (e) {
      error.value = e.message
    }
  }

  async function fetchPendingCount() {
    try {
      const pending = await adminCommentApi.getAll('PENDING')
      pendingCount.value = pending.length
    } catch (e) {
      // silently fail for badge count
      pendingCount.value = 0
    }
  }

  // Helper to refresh based on current active tab (called after mutations)
  async function refreshCurrentTab() {
    // Determine current status from existing comments or default to PENDING
    const currentStatus = comments.value.length > 0 ? comments.value[0].status : 'PENDING'
    await fetchAll(currentStatus)
  }

  return {
    comments,
    loading,
    pendingCount,
    error,
    fetchAll,
    approve,
    reject,
    deleteComment,
    fetchPendingCount
  }
})
