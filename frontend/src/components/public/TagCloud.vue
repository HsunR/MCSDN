<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const tags = ref([])
const loading = ref(true)

const http = axios.create({
  baseURL: '/api'
})

onMounted(async () => {
  try {
    const res = await http.get('/admin/tags')
    tags.value = res.data
  } catch (error) {
    console.error('Failed to fetch tags:', error)
    tags.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="tags-section">
    <h3 class="section-title">
      <span class="title-number">02.</span>
      <span class="title-text text-lightest-slate">Tags</span>
    </h3>
    
    <div v-if="loading" class="loading-state">
      <p class="text-green font-mono text-sm">Loading...</p>
    </div>
    
    <div v-else-if="tags.length === 0" class="empty-state">
      <p class="text-slate">No tags yet.</p>
    </div>
    
    <div v-else class="tag-cloud">
      <router-link
        v-for="tag in tags"
        :key="tag.id"
        :to="`/tag/${tag.slug || tag.name.toLowerCase().replace(/\s+/g, '-')}`"
        class="tag-item"
      >
        {{ tag.name }}
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.tags-section {
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

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-item {
  font-family: var(--font-mono);
  font-size: var(--fz-xs);
  color: var(--light-slate);
  padding: 8px 16px;
  background-color: var(--navy-light);
  border: 1px solid var(--lightest-navy);
  border-radius: 4px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.tag-item:hover {
  color: var(--green);
  border-color: var(--green);
  transform: translateY(-2px);
  opacity: 1;
}
</style>
