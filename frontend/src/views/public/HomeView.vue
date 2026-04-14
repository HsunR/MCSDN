<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { usePublicArticleStore } from '../../stores/publicArticleStore'
import ArticleTimeline from '../../components/public/ArticleTimeline.vue'
import Pagination from '../../components/public/Pagination.vue'
import SearchBar from '../../components/public/SearchBar.vue'
import CategoryList from '../../components/public/CategoryList.vue'
import TagCloud from '../../components/public/TagCloud.vue'

const store = usePublicArticleStore()
const route = useRoute()

onMounted(() => {
  store.fetchArticles(route.query.page ? parseInt(route.query.page) : 1)
})

watch(() => route.query.page, (newPage) => {
  store.fetchArticles(newPage ? parseInt(newPage) : 1)
})
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 py-8">


    <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
      <div class="lg:col-span-3">
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-100 mb-6">Latest Articles</h1>
          <SearchBar />
        </div>
        <!-- Sidebar on top for mobile, then main content -->
        <aside class="lg:hidden space-y-6 mb-8">
          <CategoryList />
          <TagCloud />
        </aside>
        <div v-if="store.loading" class="text-center py-12">
          <p class="text-gray-500">Loading...</p>
        </div>
        <div v-else-if="store.error" class="text-center py-12">
          <p class="text-red-500">{{ store.error }}</p>
        </div>
        <div v-else>
          <ArticleTimeline :articles="store.articles" />
          <Pagination
            :current-page="store.currentPage"
            :total-pages="store.totalPages"
            @page-change="(p) => $router.push({ query: { page: p } })"
          />
        </div>
      </div>

      <aside class="hidden lg:block space-y-6">
        <CategoryList />
        <TagCloud />
      </aside>
    </div>
  </div>
</template>
