<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  articles: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['preview'])
const router = useRouter()

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric' 
  })
}

function handlePreview(articleId) {
  emit('preview', articleId)
}

function goToArticle(articleId, title) {
  const slug = title.toLowerCase().replace(/[^\w\u4e00-\u9fa5]+/g, '-')
  router.push(`/article/${articleId}-${slug}`)
}

function goToCategory(slug) {
  router.push(`/category/${slug}`)
}

function goToTag(slug) {
  router.push(`/tag/${slug}`)
}

function getSlug(item) {
  if (!item) return ''
  return item.slug || item.name.toLowerCase().replace(/[^\w\u4e00-\u9fa5]+/g, '-')
}
</script>

<template>
  <div class="timeline">
    <div 
      v-for="article in articles" 
      :key="article.id"
      class="timeline-item"
    >
      <div class="timeline-index">
        <div class="timeline-tags">
          <span 
            v-if="article.category" 
            class="tag-item category-tag"
            @click="goToCategory(getSlug(article.category))"
          >
            {{ article.category.name }}
          </span>
          <span 
            v-for="tag in (article.tags || []).slice(0, 3)" 
            :key="tag.id || tag.name"
            class="tag-item"
            @click="goToTag(getSlug(tag))"
          >
            {{ tag.name }}
          </span>
        </div>
      </div>

      <div class="timeline-content">
        <div class="timeline-header">
          <p class="timeline-date text-green font-mono text-sm">
            {{ formatDate(article.createdAt) }}
          </p>
        </div>

        <h3 class="timeline-title text-lightest-slate">
          <button class="preview-btn" @click.stop="handlePreview(article.id)" title="Preview">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/>
            </svg>
          </button>
          <a class="title-link" @click="goToArticle(article.id, article.title)">
            {{ article.title }}
          </a>
        </h3>

        <p class="timeline-description">
          {{ article.summary || article.content?.substring(0, 150) + '...' || 'No description available' }}
        </p>
      </div>
    </div>

    <div v-if="articles.length === 0" class="empty-state">
      <p class="text-slate">No posts available yet.</p>
    </div>
  </div>
</template>

<style scoped>
.timeline {
  position: relative;
  padding-left: 0;
}

.timeline-item {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 30px;
  margin-bottom: 50px;
  transition: all 0.3s ease;
}

.timeline-item:hover {
  transform: translateY(-3px);
}

.timeline-index {
  display: flex;
  align-items: flex-start;
  padding-top: 5px;
}

.timeline-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
}

.tag-item {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--light-slate);
  padding: 4px 10px;
  background-color: var(--navy-light);
  border: 1px solid var(--lightest-navy);
  border-radius: 3px;
  white-space: nowrap;
  transition: all 0.3s ease;
  cursor: pointer;
}

.tag-item:hover {
  color: var(--green);
  border-color: var(--green);
}

.category-tag {
  color: var(--green);
  border-color: var(--green);
}

.timeline-content {
  padding-top: 5px;
}

.timeline-header {
  margin-bottom: 10px;
}

.timeline-date {
  color: var(--green);
}

.timeline-title {
  font-size: var(--fz-xxl);
  font-weight: 600;
  margin: 10px 0;
  line-height: 1.3;
  display: flex;
  align-items: center;
  gap: 10px;
}

.preview-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  background: none;
  border: 1px solid var(--lightest-navy);
  border-radius: 4px;
  color: var(--slate);
  cursor: pointer;
  transition: all 0.3s ease;
}

.preview-btn:hover {
  color: var(--green);
  border-color: var(--green);
  background-color: rgba(100, 255, 218, 0.1);
}

.title-link {
  color: var(--lightest-slate);
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.title-link:hover {
  color: var(--green);
  opacity: 1;
}

.timeline-description {
  color: var(--slate);
  line-height: 1.6;
  margin: 15px 0 20px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}

@media (max-width: 768px) {
  .timeline-item {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .timeline-index {
    padding-top: 0;
  }
}
</style>
