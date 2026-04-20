<script setup>
import { onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePublicArticleStore } from '../../stores/publicArticleStore'
import ArticleTimeline from '../../components/public/ArticleTimeline.vue'
import Pagination from '../../components/public/Pagination.vue'

const route = useRoute()
const router = useRouter()
const store = usePublicArticleStore()

const tagSlug = computed(() => route.params.name)

onMounted(() => {
  store.fetchByTag(tagSlug.value)
})

watch(() => route.params.name, (newSlug) => {
  store.fetchByTag(newSlug)
})

function goBack() {
  router.push('/')
}
</script>

<template>
  <div class="page-container">
    <button
      @click="goBack"
      class="back-button"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="m15 18-6-6 6-6"/>
      </svg>
      <span>Back</span>
    </button>

    <div class="page-header">
      <h1 class="page-title text-lightest-slate">
        <span class="section-number">01.</span>
        <span>Tag: {{ tagSlug }}</span>
      </h1>
      <div class="section-line"></div>
    </div>

    <div v-if="store.loading" class="loading-state">
      <p class="text-green font-mono">Loading articles...</p>
    </div>
    <div v-else-if="store.articles.length === 0" class="empty-state">
      <p class="text-slate">No articles found with this tag.</p>
    </div>
    <div v-else>
      <ArticleTimeline :articles="store.articles" />
      <div class="pagination-wrapper">
        <Pagination
          v-if="store.totalPages > 1"
          :current-page="store.currentPage"
          :total-pages="store.totalPages"
          @page-change="(p) => $router.push({ params: { name: tagSlug }, query: { page: p } })"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 50px 50px 100px;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  background: none;
  border: none;
  color: var(--green);
  font-family: var(--font-mono);
  font-size: var(--fz-md);
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 40px;
}

.back-button:hover {
  opacity: 0.8;
}

.page-header {
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-title {
  font-size: var(--fz-heading);
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  white-space: nowrap;
}

.section-number {
  font-family: var(--font-mono);
  font-size: var(--fz-md);
  color: var(--green);
  font-weight: 400;
  margin-right: 10px;
}

.section-line {
  flex: 1;
  height: 1px;
  background-color: var(--lightest-navy);
  max-width: 300px;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 80px 0;
}

.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .page-container {
    padding: 30px 25px 60px;
  }
}
</style>
