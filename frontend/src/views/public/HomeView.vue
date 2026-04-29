<script setup>
import { onMounted, watch, ref, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { usePublicArticleStore } from '../../stores/publicArticleStore'
import ArticleTimeline from '../../components/public/ArticleTimeline.vue'
import ArticlePreviewModal from '../../components/public/ArticlePreviewModal.vue'
import Pagination from '../../components/public/Pagination.vue'
import SearchBar from '../../components/public/SearchBar.vue'
import CategoryList from '../../components/public/CategoryList.vue'
import TagCloud from '../../components/public/TagCloud.vue'
import PortfolioGallery from '../../components/public/PortfolioGallery.vue'

const store = usePublicArticleStore()
const route = useRoute()
const previewArticleId = ref(null)
const postsSection = ref(null)

function handlePreview(articleId) {
  previewArticleId.value = articleId
}

function handleClosePreview() {
  previewArticleId.value = null
}

function scrollToPosts() {
  nextTick(() => {
    setTimeout(() => {
      if (postsSection.value) {
        postsSection.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    }, 100)
  })
}

function handlePageChange(page) {
  store.fetchArticles(page).then(() => {
    scrollToPosts()
  })
}

onMounted(() => {
  store.fetchArticles(route.query.page ? parseInt(route.query.page) : 1)
})

watch(() => route.query.page, (newPage) => {
  if (newPage) {
    store.fetchArticles(parseInt(newPage)).then(() => {
      scrollToPosts()
    })
  }
})
</script>

<template>
  <div class="home-page">
    <section class="hero" id="about">
      <div class="hero-content">
        <p class="hero-greeting text-green">Hi, welcome to my</p>
        <h1 class="hero-title text-lightest-slate">Tech Blog.</h1>
        <h2 class="hero-subtitle text-slate">I write about code, technology, and things that interest me.</h2>
        <p class="hero-description">
          I'm a software developer who loves building things that live on the internet. 
          This is where I share my thoughts, tutorials, and experiences.
        </p>
        <div class="hero-cta">
          <a href="#portfolios" class="btn-primary">Check out my portfolio</a>
        </div>
      </div>
    </section>

    <section class="featured-section" id="portfolios">
      <div class="section-header">
        <h2 class="section-title">
          <span class="section-number">01.</span>
          <span class="section-text text-lightest-slate">Portfolio</span>
        </h2>
        <div class="section-line"></div>
      </div>

      <div class="section-content">
        <PortfolioGallery />
      </div>
    </section>

    <section ref="postsSection" class="featured-section" id="posts">
      <div class="section-header">
        <h2 class="section-title">
          <span class="section-number">02.</span>
          <span class="section-text text-lightest-slate">Latest Posts</span>
        </h2>
        <div class="section-line"></div>
      </div>

      <div class="section-content">
        <div class="search-filter">
          <SearchBar />
        </div>

        <div v-if="store.loading" class="loading-state">
          <p class="text-green">Loading posts...</p>
        </div>
        
        <div v-else-if="store.error" class="error-state">
          <p class="text-red-400">{{ store.error }}</p>
        </div>
        
        <div v-else>
          <ArticleTimeline :articles="store.articles" @preview="handlePreview" />
          <div class="pagination-wrapper">
            <Pagination
              :current-page="store.currentPage"
              :total-pages="store.totalPages"
              @page-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </section>

    <section class="featured-section" id="categories">
      <div class="section-header">
        <h2 class="section-title">
          <span class="section-number">03.</span>
          <span class="section-text text-lightest-slate">Categories & Tags</span>
        </h2>
        <div class="section-line"></div>
      </div>

      <div class="section-content">
        <div class="grid-2-col">
          <CategoryList />
          <TagCloud />
        </div>
      </div>
    </section>
  </div>

  <ArticlePreviewModal
    :visible="previewArticleId !== null"
    :articleId="previewArticleId"
    @close="handleClosePreview"
  />
</template>

<style scoped>
.home-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 50px;
}

html {
  scroll-behavior: smooth;
}

.hero {
  min-height: 100vh;
  display: flex;
  align-items: center;
  padding: 100px 0;
}

.hero-content {
  max-width: 700px;
}

.hero-greeting {
  font-family: var(--font-mono);
  font-size: var(--fz-md);
  margin-bottom: 20px;
}

.hero-title {
  font-size: clamp(40px, 8vw, 80px);
  font-weight: 600;
  margin: 0;
  line-height: 1.1;
}

.hero-subtitle {
  font-size: clamp(40px, 8vw, 80px);
  font-weight: 600;
  margin: 10px 0 20px;
  line-height: 1.1;
}

.hero-description {
  max-width: 540px;
  font-size: var(--fz-xxl);
  line-height: 1.6;
  margin-bottom: 50px;
}

.hero-cta {
  margin-top: 50px;
}

.btn-primary {
  display: inline-block;
  padding: 16px 28px;
  border: 1px solid var(--green);
  border-radius: 4px;
  color: var(--green);
  font-family: var(--font-mono);
  font-size: var(--fz-md);
  text-decoration: none;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  background-color: rgba(100, 255, 218, 0.1);
  opacity: 1;
}

.featured-section {
  margin: 100px 0;
}

.section-header {
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.section-title {
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

.section-text {
  color: var(--lightest-slate);
}

.section-line {
  flex: 1;
  height: 1px;
  background-color: var(--lightest-navy);
  max-width: 300px;
}

.search-filter {
  margin-bottom: 40px;
}

.loading-state,
.error-state {
  text-align: center;
  padding: 60px 0;
}

.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.grid-2-col {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 40px;
}

@media (max-width: 768px) {
  .home-page {
    padding: 0 25px;
  }
  
  .hero {
    min-height: auto;
    padding: 60px 0;
  }
  
  .grid-2-col {
    grid-template-columns: 1fr;
  }
}
</style>
