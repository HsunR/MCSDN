<script setup>
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import axios from 'axios'
import { ref, onMounted } from 'vue'

const tags = ref([])
const loading = ref(false)
const newTagName = ref('')
const deletingId = ref(null)
const deleteError = ref('')

const http = axios.create({
  baseURL: '/api'
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

async function fetchTags() {
  loading.value = true
  try {
    const res = await http.get('/admin/tags')
    tags.value = res.data
  } catch (err) {
    console.error('Failed to fetch tags:', err)
  } finally {
    loading.value = false
  }
}

async function createTag() {
  if (!newTagName.value.trim()) return
  try {
    await http.post('/admin/tags', { name: newTagName.value })
    newTagName.value = ''
    await fetchTags()
  } catch (err) {
    console.error('Failed to create tag:', err)
  }
}

function startDelete(id) {
  deletingId.value = id
  deleteError.value = ''
}

function cancelDelete() {
  deletingId.value = null
  deleteError.value = ''
}

async function confirmDelete(id) {
  deleteError.value = ''
  try {
    await http.delete(`/admin/tags/${id}`)
    deletingId.value = null
    await fetchTags()
  } catch (err) {
    const msg = err.response?.data?.error || err.response?.data?.message || 'Failed to delete tag'
    deleteError.value = msg
    console.error('Failed to delete tag:', err)
  }
}

onMounted(fetchTags)
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />

    <main class="flex-1 p-8">
      <h1 class="text-3xl font-bold text-gray-100 mb-8">Tag Management</h1>

      <div class="bg-gray-800 rounded-lg p-6 border border-gray-700 mb-8">
        <h2 class="text-lg font-semibold text-gray-100 mb-4">Add New Tag</h2>
        <div class="flex gap-4">
          <input
            v-model="newTagName"
            type="text"
            placeholder="Tag name"
            class="flex-1 px-4 py-2 bg-gray-700 text-gray-100 border border-gray-600 rounded-lg focus:outline-none focus:border-blue-500"
            @keyup.enter="createTag"
          />
          <button
            @click="createTag"
            class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-500"
          >
            Add
          </button>
        </div>
      </div>

      <div v-if="loading" class="text-gray-400">Loading...</div>
      <div v-else-if="tags.length === 0" class="text-gray-400">No tags yet.</div>
      <div v-else class="bg-gray-800 rounded-lg border border-gray-700">
        <div
          v-for="tag in tags"
          :key="tag.id"
          class="flex items-center justify-between px-6 py-4 border-b border-gray-700 last:border-b-0"
        >
          <div>
            <span class="text-gray-100 font-medium">{{ tag.name }}</span>
            <span v-if="tag.slug" class="text-gray-500 text-sm ml-2">/tag/{{ tag.slug }}</span>
          </div>
          <div v-if="deletingId === tag.id" class="flex gap-2">
            <button
              @click="confirmDelete(tag.id)"
              class="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-500"
            >
              Confirm
            </button>
            <button
              @click="cancelDelete"
              class="px-3 py-1 text-sm bg-gray-600 text-gray-200 rounded hover:bg-gray-500"
            >
              Cancel
            </button>
          </div>
          <p v-if="deleteError && deletingId === tag.id" class="text-red-500 text-sm">{{ deleteError }}</p>
          <button
            v-else
            @click="startDelete(tag.id)"
            class="px-4 py-1 text-red-400 hover:bg-red-900 rounded"
          >
            Delete
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
