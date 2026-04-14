<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useArticleStore } from '../../stores/articleStore'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import MarkdownEditor from '../../components/admin/MarkdownEditor.vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const articleStore = useArticleStore()

const isEditing = computed(() => !!route.params.id)
const articleId = computed(() => route.params.id)

const title = ref('')
const content = ref('')
const status = ref('DRAFT')
const categoryId = ref(null)
const categories = ref([])
const tags = ref([])
const tagInput = ref('')
const saving = ref(false)

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

onMounted(async () => {
  await fetchCategories()
  if (isEditing.value) {
    await articleStore.fetchArticle(articleId.value)
    const article = articleStore.currentArticle
    if (article) {
      title.value = article.title
      content.value = article.content
      status.value = article.status
      categoryId.value = article.categoryId
      tags.value = article.tags?.map(t => t.name) || []
    }
  }
})

function addTag() {
  const tag = tagInput.value.trim()
  if (tag && !tags.value.includes(tag)) {
    tags.value.push(tag)
  }
  tagInput.value = ''
}

function removeTag(tag) {
  tags.value = tags.value.filter(t => t !== tag)
}

async function handleSave() {
  saving.value = true
  try {
    const data = {
      title: title.value,
      content: content.value,
      status: status.value,
      categoryId: categoryId.value,
      tags: tags.value
    }
    if (isEditing.value) {
      await articleStore.updateArticle(articleId.value, data)
    } else {
      await articleStore.createArticle(data)
    }
    router.push('/admin/articles')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />

    <main class="flex-1 p-8">
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-100">
          {{ isEditing ? 'Edit Article' : 'New Article' }}
        </h1>
        <button
          @click="handleSave"
          :disabled="saving"
          class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors disabled:opacity-50"
        >
          {{ saving ? 'Saving...' : 'Save' }}
        </button>
      </div>

      <div class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">Title</label>
          <input
            v-model="title"
            type="text"
            class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-gray-100 focus:ring-2 focus:ring-blue-500"
            placeholder="Article title"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">Content</label>
          <MarkdownEditor v-model="content" />
        </div>

        <div class="grid grid-cols-2 gap-6">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">Category</label>
            <select
              v-model="categoryId"
              class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-gray-100 focus:ring-2 focus:ring-blue-500"
            >
              <option :value="null" disabled>Select category</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">Status</label>
            <select
              v-model="status"
              class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-gray-100 focus:ring-2 focus:ring-blue-500"
            >
              <option value="DRAFT">Draft</option>
              <option value="PUBLISHED">Published</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">Tags</label>
            <div class="flex gap-2 mb-2">
              <input
                v-model="tagInput"
                @keydown.enter.prevent="addTag"
                type="text"
                class="flex-1 px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-gray-100 focus:ring-2 focus:ring-blue-500"
                placeholder="Add tag"
              />
              <button
                @click="addTag"
                type="button"
                class="px-4 py-2 bg-gray-700 hover:bg-gray-600 text-gray-200 rounded-lg"
              >
                Add
              </button>
            </div>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="tag in tags"
                :key="tag"
                class="px-2 py-1 bg-gray-700 text-gray-200 rounded text-sm flex items-center gap-1"
              >
                {{ tag }}
                <button @click="removeTag(tag)" class="text-gray-400 hover:text-gray-200">&times;</button>
              </span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
