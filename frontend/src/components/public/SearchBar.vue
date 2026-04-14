<template>
  <form @submit.prevent="handleSearch" class="flex gap-2">
    <input
      v-model="keyword"
      type="text"
      placeholder="Search articles..."
      class="flex-1 px-4 py-2 bg-gray-800 text-gray-100 border border-gray-700 rounded-lg focus:outline-none focus:border-blue-500"
    />
    <button
      type="submit"
      class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-500"
    >
      Search
    </button>
  </form>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const keyword = ref('')

function handleSearch() {
  if (keyword.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(keyword.value.trim())}`)
    keyword.value = ''
  }
}
</script>
