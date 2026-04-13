<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useArticleStore } from '../../stores/articleStore'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'

const router = useRouter()
const articleStore = useArticleStore()

onMounted(() => articleStore.fetchArticles())

function createNew() {
  router.push('/admin/articles/new')
}

function editArticle(id) {
  router.push(`/admin/articles/${id}/edit`)
}

async function deleteArticle(id) {
  if (confirm('Delete this article?')) {
    await articleStore.deleteArticle(id)
    articleStore.fetchArticles()
  }
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

      <div v-if="articleStore.loading" class="text-gray-400">Loading...</div>

      <div v-else class="space-y-4">
        <div
          v-for="article in articleStore.articles"
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
              <button
                @click="deleteArticle(article.id)"
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
