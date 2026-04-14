<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const categories = ref([])
const loading = ref(true)

const http = axios.create({
  baseURL: '/api'
})

onMounted(async () => {
  try {
    const res = await http.get('/admin/categories')
    categories.value = res.data
  } catch (error) {
    console.error('Failed to fetch categories:', error)
    categories.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="bg-gray-900 border border-gray-800 rounded-lg p-4">
    <h3 class="text-lg font-semibold text-gray-100 mb-4">Categories</h3>
    <div v-if="loading" class="text-gray-500">Loading...</div>
    <div v-else-if="categories.length === 0" class="text-gray-500">No categories</div>
    <div v-else class="space-y-2">
      <router-link
        v-for="cat in categories"
        :key="cat.id"
        :to="`/category/${cat.slug || cat.name.toLowerCase().replace(/\s+/g, '-')}`"
        class="block text-gray-300 hover:text-blue-400 hover:bg-gray-800 px-3 py-2 rounded"
      >
        {{ cat.name }}
      </router-link>
    </div>
  </div>
</template>
