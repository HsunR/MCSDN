<script setup>
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import { useArticleStore } from '../../stores/articleStore'
import { onMounted } from 'vue'

const articleStore = useArticleStore()
onMounted(() => articleStore.fetchArticles())
</script>

<template>
  <div class="flex min-h-screen bg-gray-900">
    <AdminSidebar />

    <main class="flex-1 p-8">
      <h1 class="text-3xl font-bold text-gray-100 mb-8">Dashboard</h1>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-gray-800 rounded-lg p-6 border border-gray-700">
          <div class="text-3xl font-bold text-blue-400">{{ articleStore.articles.length }}</div>
          <div class="text-gray-400 mt-1">Total Articles</div>
        </div>

        <div class="bg-gray-800 rounded-lg p-6 border border-gray-700">
          <div class="text-3xl font-bold text-green-400">
            {{ articleStore.articles.filter(a => a.status === 'PUBLISHED').length }}
          </div>
          <div class="text-gray-400 mt-1">Published</div>
        </div>

        <div class="bg-gray-800 rounded-lg p-6 border border-gray-700">
          <div class="text-3xl font-bold text-yellow-400">
            {{ articleStore.articles.filter(a => a.status === 'DRAFT').length }}
          </div>
          <div class="text-gray-400 mt-1">Drafts</div>
        </div>
      </div>
    </main>
  </div>
</template>
