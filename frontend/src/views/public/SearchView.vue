<script setup>
import { onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { usePublicArticleStore } from '../../stores/publicArticleStore'
import ArticleTimeline from '../../components/public/ArticleTimeline.vue'
import Pagination from '../../components/public/Pagination.vue'

const route = useRoute()
const store = usePublicArticleStore()

const keyword = computed(() => route.query.q || '')

onMounted(() => {
  if (keyword.value) {
    store.searchArticles(keyword.value)
  }
})

watch(() => route.query.q, (newKeyword) => {
  if (newKeyword) {
    store.searchArticles(newKeyword)
  }
})
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-100 mb-4">
      Search Results
    </h1>
    <p v-if="keyword" class="text-gray-400 mb-8">
      Showing results for: <span class="text-blue-400">"{{ keyword }}"</span>
    </p>

    <div v-if="store.loading" class="text-center py-12">
      <p class="text-gray-500">Loading...</p>
    </div>
    <div v-else>
      <ArticleTimeline :articles="store.articles" />
      <Pagination
        v-if="store.totalPages > 1"
        :current-page="store.currentPage"
        :total-pages="store.totalPages"
        @page-change="(p) => $router.push({ path: '/search', query: { q: keyword, page: p } })"
      />
    </div>
  </div>
</template>
