<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const tags = ref([])
const loading = ref(true)

const http = axios.create({
  baseURL: '/api'
})

onMounted(async () => {
  try {
    const res = await http.get('/admin/tags')
    tags.value = res.data
  } catch (error) {
    console.error('Failed to fetch tags:', error)
    tags.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="bg-gray-900 border border-gray-800 rounded-lg p-4">
    <h3 class="text-lg font-semibold text-gray-100 mb-4">Tags</h3>
    <div v-if="loading" class="text-gray-500">Loading...</div>
    <div v-else-if="tags.length === 0" class="text-gray-500">No tags</div>
    <div v-else class="flex flex-wrap gap-2">
      <router-link
        v-for="tag in tags"
        :key="tag.id"
        :to="`/tag/${tag.name.toLowerCase().replace(/\s+/g, '-')}`"
        class="px-3 py-1 text-sm bg-gray-700 text-gray-300 rounded-full hover:bg-gray-600 hover:text-blue-400"
      >
        {{ tag.name }}
      </router-link>
    </div>
  </div>
</template>
