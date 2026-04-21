<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const categories = ref([])
const loading = ref(true)

const http = axios.create({
  baseURL: '/api'
})

function navigateToCategory(slug) {
  router.push(`/category/${slug}`)
}

onMounted(async () => {
  try {
    const res = await http.get('/admin/categories')
    categories.value = res.data
  } catch (error) {
    console.error('Failed to fetch categories:', error)
    categories.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="categories-section">
    <h3 class="section-title">
      <span class="title-number">01.</span>
      <span class="title-text text-lightest-slate">Categories</span>
    </h3>
    
    <div v-if="loading" class="loading-state">
      <p class="text-green font-mono text-sm">Loading...</p>
    </div>
    
    <div v-else-if="categories.length === 0" class="empty-state">
      <p class="text-slate">No categories yet.</p>
    </div>
    
    <div v-else class="category-grid">
      <a
        v-for="cat in categories"
        :key="cat.id"
        :href="`/category/${cat.slug || cat.name.toLowerCase().replace(/\s+/g, '-')}`"
        class="category-card"
        @click.prevent="navigateToCategory(cat.slug || cat.name.toLowerCase().replace(/\s+/g, '-'))"
      >
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <div class="card-content">
          <h4 class="card-title text-lightest-slate">{{ cat.name }}</h4>
          <p class="card-slug text-slate font-mono text-xs">{{ cat.slug || 'category' }}</p>
        </div>
      </a>
    </div>
  </div>
</template>

<style scoped>
.categories-section {
  padding: 20px 0;
}

.section-title {
  font-size: var(--fz-lg);
  font-weight: 600;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
}

.title-number {
  font-family: var(--font-mono);
  font-size: var(--fz-md);
  color: var(--green);
  font-weight: 400;
  margin-right: 10px;
}

.title-text {
  color: var(--lightest-slate);
}

.loading-state,
.empty-state {
  padding: 20px 0;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.category-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background-color: var(--navy-light);
  border: 1px solid var(--lightest-navy);
  border-radius: 4px;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.category-card:hover {
  transform: translateY(-5px);
  border-color: var(--green);
  opacity: 1;
}

.card-icon {
  color: var(--green);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: var(--fz-lg);
  font-weight: 600;
  margin: 0 0 5px 0;
  transition: all 0.3s ease;
}

.category-card:hover .card-title {
  color: var(--green) !important;
}

.card-slug {
  margin: 0;
  font-size: 12px;
}
</style>
