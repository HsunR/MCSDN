<script setup>
import { onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePublicArticleStore } from '../../stores/publicArticleStore'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import CommentSection from '../../components/CommentSection.vue'
import ArticleToc from '../../components/public/ArticleToc.vue'

const route = useRoute()
const router = useRouter()
const store = usePublicArticleStore()

let headingCounter = 0

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  headingId: (content) => {
    return `heading-${headingCounter++}`
  },
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
      } catch (__) {}
    }
    try {
      const result = hljs.highlightAuto(str)
      return '<pre class="hljs"><code>' + result.value + '</code></pre>'
    } catch (__) {}
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

const article = computed(() => store.currentArticle)
const renderedContent = computed(() => {
  if (!article.value) return ''
  headingCounter = 0
  return md.render(article.value.content)
})

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric'
  })
}

function goBack() {
  router.back()
}

onMounted(() => {
  const id = route.params.idSlug.split('-')[0]
  store.fetchArticle(id)
})
</script>

<template>
  <div class="article-page">
    <ArticleToc v-if="article" :content="renderedContent" />
    
    <div class="article-container">
      <button
        @click="goBack()"
        class="back-button"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m15 18-6-6 6-6"/>
        </svg>
        <span>Back</span>
      </button>

      <div v-if="store.loading" class="loading-state">
        <p class="text-green font-mono">Loading article...</p>
      </div>
      
      <div v-else-if="store.error" class="error-state">
        <p class="text-red-400">{{ store.error }}</p>
      </div>
      
      <article v-else-if="article" class="article-detail">
        <header class="article-header">
          <p class="article-overline text-green font-mono">
            Article #{{ String(article.id).padStart(2, '0') }}
          </p>
          <h1 class="article-title">{{ article.title }}</h1>
          <p class="article-date">
            {{ formatDate(article.createdAt) }}
          </p>
          
          <div class="article-meta">
            <router-link
              v-if="article.category"
              :to="`/category/${article.category.slug || article.category.name.toLowerCase().replace(/\s+/g, '-')}`"
              class="meta-tag"
            >
              {{ article.category.name }}
            </router-link>
            <router-link
              v-for="tag in article.tags"
              :key="tag.id"
              :to="`/tag/${tag.slug || tag.name.toLowerCase().replace(/\s+/g, '-')}`"
              class="meta-tag"
            >
              {{ tag.name }}
            </router-link>
          </div>
        </header>

        <div class="article-content markdown-body" v-html="renderedContent"></div>
      </article>
      
      <div v-else class="not-found-state">
        <p class="text-slate">Article not found.</p>
      </div>
    </div>
    
    <CommentSection v-if="article" :articleId="article.id.toString()" />
  </div>
</template>

<style scoped>
.article-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 50px 0 100px;
}

.article-container {
  max-width: 700px;
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

.loading-state,
.error-state,
.not-found-state {
  text-align: center;
  padding: 80px 0;
}

.article-header {
  margin-bottom: 50px;
}

.article-overline {
  font-size: var(--fz-md);
  margin-bottom: 20px;
}

.article-title {
  font-size: clamp(32px, 5vw, 50px);
  font-weight: 600;
  color: var(--lightest-slate);
  margin: 0 0 10px 0;
  line-height: 1.1;
}

.article-date {
  font-family: var(--font-mono);
  font-size: var(--fz-md);
  color: var(--slate);
  margin: 0 0 20px 0;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
}

.meta-tag {
  font-family: var(--font-mono);
  font-size: var(--fz-xs);
  color: var(--light-slate);
  padding: 6px 12px;
  background-color: var(--navy-light);
  border: 1px solid var(--lightest-navy);
  border-radius: 4px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.meta-tag:hover {
  color: var(--green);
  border-color: var(--green);
  opacity: 1;
}

.article-content {
  color: var(--slate);
  line-height: 1.8;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4) {
  color: var(--lightest-slate);
  margin-top: 40px;
  margin-bottom: 20px;
  scroll-margin-top: 20px;
}

.article-content :deep(p) {
  margin-bottom: 20px;
}

.article-content :deep(a) {
  color: var(--green);
  text-decoration: none;
  transition: all 0.3s ease;
}

.article-content :deep(a:hover) {
  opacity: 0.8;
}

.article-content :deep(code) {
  font-family: var(--font-mono);
  font-size: 14px;
  background-color: var(--navy-light);
  color: var(--light-slate);
  padding: 2px 6px;
  border-radius: 4px;
}

.article-content :deep(pre) {
  background-color: var(--navy-light);
  border: 1px solid var(--lightest-navy);
  border-radius: 4px;
  padding: 20px;
  overflow-x: auto;
  margin: 20px 0;
}

.article-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  border: none;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  margin: 20px 0;
  padding-left: 30px;
}

.article-content :deep(li) {
  margin-bottom: 10px;
}

.article-content :deep(blockquote) {
  border-left: 2px solid var(--green);
  padding-left: 20px;
  margin: 20px 0;
  color: var(--light-slate);
  font-style: italic;
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 20px 0;
}

@media (max-width: 768px) {
  .article-page {
    padding: 30px 25px 60px;
  }
}
</style>
