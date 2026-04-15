<script setup>
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import { useCommentStore } from '../../stores/commentStore'
import { onMounted, ref, computed } from 'vue'

const commentStore = useCommentStore()
const activeTab = ref('PENDING')
const deletingId = ref(null)

const tabs = ['ALL', 'PENDING', 'APPROVED', 'REJECTED']

const comments = computed(() => commentStore.comments)
const loading = computed(() => commentStore.loading)

function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getStatusBadgeClass(status) {
  switch (status) {
    case 'PENDING':
      return 'bg-yellow-600 text-yellow-100'
    case 'APPROVED':
      return 'bg-green-600 text-green-100'
    case 'REJECTED':
      return 'bg-red-600 text-red-100'
    default:
      return 'bg-gray-600 text-gray-100'
  }
}

async function handleTabClick(status) {
  activeTab.value = status
  await commentStore.fetchAll(status === 'ALL' ? null : status)
}

async function handleApprove(id) {
  await commentStore.approve(id)
  await commentStore.fetchAll(activeTab.value)
}

async function handleReject(id) {
  await commentStore.reject(id)
  await commentStore.fetchAll(activeTab.value)
}

function confirmDelete(id) {
  deletingId.value = id
}

async function handleDelete(id) {
  await commentStore.deleteComment(id)
  deletingId.value = null
  await commentStore.fetchAll(activeTab.value)
}

function cancelDelete() {
  deletingId.value = null
}

onMounted(() => {
  commentStore.fetchAll(activeTab.value)
})
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />

    <main class="flex-1 p-8">
      <h1 class="text-3xl font-bold text-gray-100 mb-8">Comments</h1>

      <!-- Status Tabs -->
      <div class="flex gap-2 mb-8">
        <button
          v-for="tab in tabs"
          :key="tab"
          @click="handleTabClick(tab)"
          class="px-6 py-2 rounded-lg font-medium transition-colors"
          :class="activeTab === tab
            ? 'bg-blue-600 text-white'
            : 'bg-gray-800 text-gray-400 hover:bg-gray-700 hover:text-gray-200'"
        >
          {{ tab }}
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-gray-400 text-center py-12">
        Loading comments...
      </div>

      <!-- Empty State -->
      <div v-else-if="comments.length === 0" class="text-gray-400 text-center py-12">
        No {{ activeTab.toLowerCase() }} comments.
      </div>

      <!-- Comment List -->
      <div v-else class="space-y-4">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="bg-gray-800 rounded-lg p-6 border border-gray-700"
        >
          <div class="flex items-start justify-between mb-3">
            <div>
              <span class="font-medium text-gray-100">{{ comment.authorName }}</span>
              <span class="mx-2 text-gray-500">on article #{{ comment.articleId }}</span>
              <span class="text-sm text-gray-500">{{ formatDate(comment.createdAt) }}</span>
            </div>
            <span
              class="px-2 py-1 text-xs font-medium rounded"
              :class="getStatusBadgeClass(comment.status)"
            >
              {{ comment.status }}
            </span>
          </div>

          <p class="text-gray-300 mb-4 whitespace-pre-wrap">{{ comment.content }}</p>

          <!-- Action Buttons -->
          <div class="flex gap-2">
            <button
              v-if="comment.status === 'PENDING'"
              @click="handleApprove(comment.id)"
              class="px-4 py-1.5 bg-green-600 hover:bg-green-700 text-white text-sm rounded transition-colors"
            >
              Approve
            </button>
            <button
              v-if="comment.status === 'PENDING'"
              @click="handleReject(comment.id)"
              class="px-4 py-1.5 bg-yellow-600 hover:bg-yellow-700 text-white text-sm rounded transition-colors"
            >
              Reject
            </button>
            <template v-if="deletingId === comment.id">
              <button
                @click="handleDelete(comment.id)"
                class="px-4 py-1.5 bg-red-600 hover:bg-red-700 text-white text-sm rounded transition-colors"
              >
                Confirm
              </button>
              <button
                @click="cancelDelete"
                class="px-4 py-1.5 bg-gray-600 hover:bg-gray-700 text-white text-sm rounded transition-colors"
              >
                Cancel
              </button>
            </template>
            <button
              v-else
              @click="confirmDelete(comment.id)"
              class="px-4 py-1.5 bg-red-600 hover:bg-red-700 text-white text-sm rounded transition-colors"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
