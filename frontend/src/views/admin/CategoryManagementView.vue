<script setup>
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import axios from 'axios'
import { ref, onMounted } from 'vue'

const categories = ref([])
const loading = ref(false)
const newCategoryName = ref('')

const http = axios.create({
  baseURL: '/api'
})

async function fetchCategories() {
  loading.value = true
  try {
    const res = await http.get('/admin/categories')
    categories.value = res.data
  } catch (err) {
    console.error('Failed to fetch categories:', err)
  } finally {
    loading.value = false
  }
}

async function createCategory() {
  if (!newCategoryName.value.trim()) return
  try {
    await http.post('/admin/categories', { name: newCategoryName.value })
    newCategoryName.value = ''
    await fetchCategories()
  } catch (err) {
    console.error('Failed to create category:', err)
    alert('Failed to create category')
  }
}

async function deleteCategory(id) {
  if (!confirm('Delete this category?')) return
  try {
    await http.delete(`/admin/categories/${id}`)
    await fetchCategories()
  } catch (err) {
    console.error('Failed to delete category:', err)
  }
}

onMounted(fetchCategories)
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />

    <main class="flex-1 p-8">
      <h1 class="text-3xl font-bold text-gray-100 mb-8">Category Management</h1>

      <!-- Add new category form -->
      <div class="bg-gray-800 rounded-lg p-6 border border-gray-700 mb-8">
        <h2 class="text-lg font-semibold text-gray-100 mb-4">Add New Category</h2>
        <div class="flex gap-4">
          <input
            v-model="newCategoryName"
            type="text"
            placeholder="Category name"
            class="flex-1 px-4 py-2 bg-gray-700 text-gray-100 border border-gray-600 rounded-lg focus:outline-none focus:border-blue-500"
            @keyup.enter="createCategory"
          />
          <button
            @click="createCategory"
            class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-500"
          >
            Add
          </button>
        </div>
      </div>

      <!-- Categories list -->
      <div v-if="loading" class="text-gray-400">Loading...</div>
      <div v-else-if="categories.length === 0" class="text-gray-400">No categories yet.</div>
      <div v-else class="bg-gray-800 rounded-lg border border-gray-700">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="flex items-center justify-between px-6 py-4 border-b border-gray-700 last:border-b-0"
        >
          <div>
            <span class="text-gray-100 font-medium">{{ cat.name }}</span>
            <span v-if="cat.slug" class="text-gray-500 text-sm ml-2">/category/{{ cat.slug }}</span>
          </div>
          <button
            @click="deleteCategory(cat.id)"
            class="px-4 py-1 text-red-400 hover:bg-red-900 rounded"
          >
            Delete
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
