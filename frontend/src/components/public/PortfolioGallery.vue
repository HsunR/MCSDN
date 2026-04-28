<script setup>
import { portfolioApi } from '../../api/portfolioApi'
import { ref, onMounted } from 'vue'

const portfolios = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    portfolios.value = await portfolioApi.getPublished()
  } catch (err) {
    console.error('Failed to fetch portfolios:', err)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="portfolio-section">
    <h2 class="section-heading">作品集</h2>

    <div v-if="loading" class="loading-text">Loading...</div>
    
    <div v-else-if="portfolios.length === 0" class="empty-text">暂无作品集</div>
    
    <div v-else class="portfolio-grid">
      <div
        v-for="portfolio in portfolios"
        :key="portfolio.id"
        class="portfolio-card"
      >
        <router-link
          v-if="portfolio.articleId"
          :to="`/article/${portfolio.articleId}`"
          class="portfolio-link"
        >
          <div class="portfolio-cover">
            <img
              :src="portfolio.coverImage"
              :alt="portfolio.title"
              loading="lazy"
            />
          </div>
          <div class="portfolio-content">
            <h3 class="portfolio-title">{{ portfolio.title }}</h3>
            <p v-if="portfolio.article" class="portfolio-article">
              {{ portfolio.article.title }}
            </p>
          </div>
        </router-link>

        <div v-else class="portfolio-link">
          <div class="portfolio-cover">
            <img
              :src="portfolio.coverImage"
              :alt="portfolio.title"
              loading="lazy"
            />
          </div>
          <div class="portfolio-content">
            <h3 class="portfolio-title">{{ portfolio.title }}</h3>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.portfolio-section {
  padding: 2rem 0;
}

.section-heading {
  font-size: var(--fz-xxl);
  color: var(--lightest-slate);
  margin-bottom: 2rem;
  position: relative;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.section-heading::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--lightest-navy);
}

.loading-text,
.empty-text {
  color: var(--slate);
  text-align: center;
  padding: 2rem;
}

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.portfolio-card {
  background: var(--navy-light);
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid var(--lightest-navy);
}

.portfolio-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.portfolio-link {
  display: block;
  color: inherit;
  text-decoration: none;
}

.portfolio-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.portfolio-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.portfolio-card:hover .portfolio-cover img {
  transform: scale(1.05);
}

.portfolio-content {
  padding: 1rem;
}

.portfolio-title {
  font-size: var(--fz-lg);
  color: var(--lightest-slate);
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.portfolio-article {
  font-size: var(--fz-sm);
  color: var(--slate);
  line-height: 1.4;
}
</style>
