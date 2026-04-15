<script setup>
import { onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePublicArticleStore } from '../../stores/publicArticleStore'
import ArticleTimeline from '../../components/public/ArticleTimeline.vue'
import Pagination from '../../components/public/Pagination.vue'

const route = useRoute()
const router = useRouter()
const store = usePublicArticleStore()

const categorySlug = computed(() => route.params.name)

onMounted(() => {
  store.fetchByCategory(categorySlug.value)
})

watch(() => route.params.name, (newSlug) => {
  store.fetchByCategory(newSlug)
})

function goBack() {
  router.push('/')
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <button
      @click="goBack"
      class="mb-6 px-4 py-2 bg-gray-800 text-gray-300 rounded-lg hover:bg-gray-700 flex items-center gap-2"
    >
      <span>←</span> Back to Home
    </button>

    <h1 class="text-3xl font-bold text-gray-100 mb-8">
      Category: {{ categorySlug }}
    </h1>

    <div v-if="store.loading" class="text-center py-12">
      <p class="text-gray-500">Loading...</p>
    </div>
    <div v-else-if="store.articles.length === 0">
      <p class="text-gray-500">No articles found in this category.</p>
    </div>
    <div v-else>
      <ArticleTimeline :articles="store.articles" />
      <Pagination
        v-if="store.totalPages > 1"
        :current-page="store.currentPage"
        :total-pages="store.totalPages"
        @page-change="(p) => $router.push({ params: { name: categorySlug }, query: { page: p } })"
      />
    </div>
  </div>
</template>
