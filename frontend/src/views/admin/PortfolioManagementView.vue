<script setup>
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import { portfolioApi } from '../../api/portfolioApi'
import { useArticleStore } from '../../stores/articleStore'
import { useAuthStore } from '../../stores/authStore'
import { ref, onMounted } from 'vue'
import axios from 'axios'

const articleStore = useArticleStore()
const authStore = useAuthStore()
const portfolios = ref([])
const loading = ref(false)
const editingId = ref(null)
const deletingId = ref(null)
const deleteError = ref('')
const fileInput = ref(null)
const form = ref({
  title: '',
  coverImage: '',
  articleId: null,
  sortOrder: 0
})
const uploading = ref(false)
const uploadError = ref('')

async function fetchPortfolios() {
  loading.value = true
  try {
    portfolios.value = await portfolioApi.getAll()
  } catch (err) {
    console.error('Failed to fetch portfolios:', err)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.value = {
    title: '',
    coverImage: '',
    articleId: null,
    sortOrder: 0
  }
  editingId.value = null
  uploadError.value = ''
}

async function handleSubmit() {
  if (!form.value.title.trim() || !form.value.coverImage.trim() || !form.value.articleId) return
  
  try {
    if (editingId.value) {
      await portfolioApi.update(editingId.value, form.value)
    } else {
      await portfolioApi.create(form.value)
    }
    resetForm()
    await fetchPortfolios()
  } catch (err) {
    console.error('Failed to save portfolio:', err)
  }
}

function startEdit(portfolio) {
  form.value = {
    title: portfolio.title,
    coverImage: portfolio.coverImage,
    articleId: portfolio.articleId,
    sortOrder: portfolio.sortOrder
  }
  editingId.value = portfolio.id
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
    await portfolioApi.delete(id)
    deletingId.value = null
    await fetchPortfolios()
  } catch (err) {
    const msg = err.response?.data?.error || err.response?.data?.message || 'Failed to delete portfolio'
    deleteError.value = msg
    console.error('Failed to delete portfolio:', err)
  }
}

function triggerFileInput() {
  fileInput.value.click()
}

async function handleUploadCoverImage(event) {
  const file = event.target.files[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    uploadError.value = 'Invalid file type. Allowed: jpg, png, gif, webp'
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    uploadError.value = 'File too large. Maximum size is 5MB.'
    return
  }

  uploading.value = true
  uploadError.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post('/api/admin/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${authStore.token}`
      }
    })

    form.value.coverImage = response.data.url
  } catch (e) {
    uploadError.value = e.response?.data?.error || 'Upload failed'
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  fetchPortfolios()
  articleStore.fetchArticles()
})
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />

    <main class="flex-1 p-8">
      <h1 class="text-3xl font-bold text-gray-100 mb-8">Portfolio Management</h1>

      <div class="bg-gray-800 rounded-lg p-6 border border-gray-700 mb-8">
        <h2 class="text-lg font-semibold text-gray-100 mb-4">{{ editingId ? 'Edit Portfolio' : 'Add New Portfolio' }}</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-gray-300 mb-2 text-sm">Title</label>
            <input
              v-model="form.title"
              type="text"
              placeholder="Portfolio title"
              class="w-full px-4 py-2 bg-gray-700 text-gray-100 border border-gray-600 rounded-lg focus:outline-none focus:border-blue-500"
            />
          </div>

          <div>
            <label class="block text-gray-300 mb-2 text-sm">Cover Image</label>
            <div class="flex gap-2">
              <input
                v-model="form.coverImage"
                type="text"
                placeholder="Image URL or upload"
                class="flex-1 px-4 py-2 bg-gray-700 text-gray-100 border border-gray-600 rounded-lg focus:outline-none focus:border-blue-500"
              />
              <input
                ref="fileInput"
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                class="hidden"
                @change="handleUploadCoverImage"
                :disabled="uploading"
              />
              <button
                @click="triggerFileInput"
                class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-500 disabled:opacity-50"
                :disabled="uploading"
              >
                {{ uploading ? 'Uploading...' : 'Upload' }}
              </button>
            </div>
            <p v-if="uploadError" class="text-red-500 text-xs mt-1">{{ uploadError }}</p>
            <img v-if="form.coverImage" :src="form.coverImage" class="mt-2 h-24 object-cover rounded" />
          </div>

          <div>
            <label class="block text-gray-300 mb-2 text-sm">Article Link</label>
            <select
              v-model="form.articleId"
              class="w-full px-4 py-2 bg-gray-700 text-gray-100 border border-gray-600 rounded-lg focus:outline-none focus:border-blue-500"
            >
              <option :value="null">Select an article</option>
              <option v-for="article in articleStore.articles" :key="article.id" :value="article.id">
                {{ article.title }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-gray-300 mb-2 text-sm">Sort Order</label>
            <input
              v-model.number="form.sortOrder"
              type="number"
              placeholder="0"
              class="w-full px-4 py-2 bg-gray-700 text-gray-100 border border-gray-600 rounded-lg focus:outline-none focus:border-blue-500"
            />
          </div>
        </div>

        <div class="flex gap-2">
          <button
            @click="handleSubmit"
            class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-500"
          >
            {{ editingId ? 'Update' : 'Add' }}
          </button>
          <button
            v-if="editingId"
            @click="resetForm"
            class="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-500"
          >
            Cancel
          </button>
        </div>
      </div>

      <div v-if="loading" class="text-gray-400">Loading...</div>
      <div v-else-if="portfolios.length === 0" class="text-gray-400">No portfolios yet.</div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="portfolio in portfolios"
          :key="portfolio.id"
          class="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden"
        >
          <img
            :src="portfolio.coverImage"
            :alt="portfolio.title"
            class="w-full h-48 object-cover"
            @error="$event.target.src=''"
          />
          <div class="p-4">
            <h3 class="text-gray-100 font-semibold mb-2">{{ portfolio.title }}</h3>
            <p class="text-gray-400 text-sm mb-2">Article: {{ portfolio.article?.title || 'N/A' }}</p>
            <p class="text-gray-500 text-xs mb-4">Sort Order: {{ portfolio.sortOrder }}</p>
            
            <div class="flex gap-2">
              <button
                @click="startEdit(portfolio)"
                class="flex-1 px-3 py-1 text-sm bg-blue-600 text-white rounded hover:bg-blue-500"
              >
                Edit
              </button>
              <template v-if="deletingId === portfolio.id">
                <button
                  @click="confirmDelete(portfolio.id)"
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
              </template>
              <button
                v-else
                @click="startDelete(portfolio.id)"
                class="px-3 py-1 text-sm bg-red-900 text-red-200 rounded hover:bg-red-800"
              >
                Delete
              </button>
            </div>
            <p v-if="deleteError && deletingId === portfolio.id" class="text-red-500 text-xs mt-2">{{ deleteError }}</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
