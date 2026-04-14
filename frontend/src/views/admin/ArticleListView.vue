<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useArticleStore } from '../../stores/articleStore'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import axios from 'axios'

const router = useRouter()
const articleStore = useArticleStore()

const categories = ref([])
const tags = ref([])
const selectedCategory = ref('')
const selectedTag = ref('')
const searchQuery = ref('')
const deletingId = ref(null)

const http = axios.create({
  baseURL: '/api'
})

async function fetchCategories() {
  try {
    const res = await http.get('/admin/categories')
    categories.value = res.data
  } catch (err) {
    console.error('Failed to fetch categories:', err)
  }
}

async function fetchTags() {
  try {
    const res = await http.get('/admin/tags')
    tags.value = res.data
  } catch (err) {
    console.error('Failed to fetch tags:', err)
  }
}

const filteredArticles = computed(() => {
  let articles = articleStore.articles

  if (selectedCategory.value) {
    articles = articles.filter(a => a.categoryId === parseInt(selectedCategory.value))
  }

  if (selectedTag.value) {
    articles = articles.filter(a =>
      a.tags && a.tags.some(t => t.name === selectedTag.value)
    )
  }

  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    articles = articles.filter(a =>
      a.title.toLowerCase().includes(query) ||
      (a.content && a.content.toLowerCase().includes(query))
    )
  }

  return articles
})

onMounted(async () => {
  await Promise.all([
    articleStore.fetchArticles(),
    fetchCategories(),
    fetchTags()
  ])
})

function createNew() {
  router.push('/admin/articles/new')
}

function editArticle(id) {
  router.push(`/admin/articles/${id}/edit`)
}

function startDelete(id) {
  deletingId.value = id
}

function cancelDelete() {
  deletingId.value = null
}

async function confirmDelete(id) {
  await articleStore.deleteArticle(id)
  deletingId.value = null
  articleStore.fetchArticles()
}

function clearFilters() {
  selectedCategory.value = ''
  selectedTag.value = ''
  searchQuery.value = ''
}
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />

    <main class="flex-1 p-8">
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-100">Articles</h1>
        <button
          @click="createNew"
          class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
        >
          New Article
        </button>
      </div>

      <div class="bg-gray-800 rounded-lg p-4 mb-6 border border-gray-700">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm text-gray-400 mb-1">Category</label>
            <select
              v-model="selectedCategory"
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-gray-100 text-sm"
            >
              <option value="">All Categories</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm text-gray-400 mb-1">Tag</label>
            <select
              v-model="selectedTag"
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-gray-100 text-sm"
            >
              <option value="">All Tags</option>
              <option v-for="tag in tags" :key="tag.id" :value="tag.name">
                {{ tag.name }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm text-gray-400 mb-1">Search</label>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search articles..."
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-gray-100 text-sm"
            />
          </div>

          <div class="flex items-end">
            <button
              @click="clearFilters"
              class="px-4 py-2 bg-gray-600 hover:bg-gray-500 text-gray-200 rounded transition-colors"
            >
              Clear
            </button>
          </div>
        </div>
      </div>

      <div v-if="articleStore.loading" class="text-gray-400">Loading...</div>
      <div v-else-if="filteredArticles.length === 0" class="text-gray-400">No articles found.</div>

      <div v-else class="space-y-4">
        <div
          v-for="article in filteredArticles"
          :key="article.id"
          class="bg-gray-800 border border-gray-700 rounded-lg p-6"
        >
          <div class="flex justify-between items-start">
            <div>
              <h3 class="text-xl font-semibold text-gray-100">{{ article.title }}</h3>
              <div class="flex gap-2 mt-2">
                <span
                  :class="article.status === 'PUBLISHED' ? 'bg-green-900 text-green-300' : 'bg-yellow-900 text-yellow-300'"
                  class="px-2 py-1 text-xs rounded"
                >
                  {{ article.status }}
                </span>
                <span class="text-gray-500 text-sm">
                  {{ new Date(article.createdAt).toLocaleDateString() }}
                </span>
              </div>
            </div>
            <div class="flex gap-2">
              <button
                @click="editArticle(article.id)"
                class="px-3 py-1 bg-gray-700 hover:bg-gray-600 text-gray-200 rounded transition-colors"
              >
                Edit
              </button>
              <div v-if="deletingId === article.id" class="flex gap-1">
                <button
                  @click="confirmDelete(article.id)"
                  class="px-3 py-1 bg-red-600 hover:bg-red-500 text-white rounded text-sm"
                >
                  Confirm
                </button>
                <button
                  @click="cancelDelete"
                  class="px-3 py-1 bg-gray-600 hover:bg-gray-500 text-gray-200 rounded text-sm"
                >
                  Cancel
                </button>
              </div>
              <button
                v-else
                @click="startDelete(article.id)"
                class="px-3 py-1 bg-red-900 hover:bg-red-800 text-red-300 rounded transition-colors"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
