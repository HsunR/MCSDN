<script setup>
import { computed, watch, onMounted, onUnmounted, ref } from 'vue'
import { usePublicArticleStore } from '../../stores/publicArticleStore'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const props = defineProps({
  visible: Boolean,
  articleId: [Number, String]
})
const emit = defineEmits(['close'])

const store = usePublicArticleStore()
const scrollPosition = ref(0)

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
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
  return md.render(article.value.content)
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric'
  })
}

function handleClose() {
  emit('close')
}

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    scrollPosition.value = window.pageYOffset || document.documentElement.scrollTop
    document.body.style.position = 'fixed'
    document.body.style.top = `-${scrollPosition.value}px`
    document.body.style.width = '100%'
  } else {
    document.body.style.position = ''
    document.body.style.top = ''
    document.body.style.width = ''
    window.scrollTo(0, scrollPosition.value)
  }
})

function handleEscape(e) {
  if (e.key === 'Escape' && props.visible) {
    handleClose()
  }
}

watch(() => props.articleId, (newId) => {
  if (newId) {
    store.fetchArticle(newId)
  }
}, { immediate: true })

onMounted(() => document.addEventListener('keydown', handleEscape))
onUnmounted(() => document.removeEventListener('keydown', handleEscape))
</script>

<template>
  <div
    v-if="visible"
    class="modal-overlay"
    @click.self="handleClose"
  >
    <div class="modal-container">
      <div class="modal-header">
        <div class="header-content">
          <p class="modal-overline text-green font-mono">
            Article #{{ String(article?.id).padStart(2, '0') || '00' }}
          </p>
          <h2 class="modal-title">{{ article?.title }}</h2>
          <p class="modal-date">{{ formatDate(article?.createdAt) }}</p>
          <div class="modal-meta">
            <router-link
              v-if="article?.category"
              :to="`/category/${article.category.slug || article.category.name.toLowerCase().replace(/\s+/g, '-')}`"
              class="meta-tag"
            >
              {{ article.category.name }}
            </router-link>
            <router-link
              v-for="tag in article?.tags"
              :key="tag.id"
              :to="`/tag/${tag.slug || tag.name.toLowerCase().replace(/\s+/g, '-')}`"
              class="meta-tag"
            >
              {{ tag.name }}
            </router-link>
          </div>
        </div>
        <button
          type="button"
          @click.stop="handleClose"
          @mousedown.prevent
          class="close-button"
          title="Close"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 6 6 18"/><path d="m6 6 12 12"/>
          </svg>
        </button>
      </div>

      <div class="modal-content">
        <div v-if="store.loading" class="modal-loading">
          <p class="text-green font-mono">Loading...</p>
        </div>
        <div v-else-if="store.error" class="modal-error">
          <p class="text-red-400">{{ store.error }}</p>
        </div>
        <div
          v-else-if="article"
          class="modal-body markdown-body"
          v-html="renderedContent"
        ></div>
        <div v-else class="modal-empty">
          <p class="text-slate">Article not found.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(10, 25, 47, 0.85);
  backdrop-filter: blur(4px);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-container {
  background-color: var(--navy-light);
  border: 1px solid var(--lightest-navy);
  border-radius: 8px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.modal-header {
  position: relative;
  border-bottom: 1px solid var(--lightest-navy);
  padding: 20px 30px 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-shrink: 0;
}

.header-content {
  flex: 1;
}

.modal-overline {
  font-size: var(--fz-sm);
  margin-bottom: 8px;
}

.modal-title {
  font-size: clamp(20px, 3vw, 28px);
  font-weight: 600;
  color: var(--lightest-slate);
  margin: 0 0 6px 0;
  line-height: 1.2;
}

.modal-date {
  font-family: var(--font-mono);
  font-size: var(--fz-sm);
  color: var(--slate);
  margin: 0 0 10px 0;
}

.modal-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.meta-tag {
  font-family: var(--font-mono);
  font-size: var(--fz-xs);
  color: var(--light-slate);
  padding: 5px 10px;
  background-color: var(--navy);
  border: 1px solid var(--lightest-navy);
  border-radius: 3px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.meta-tag:hover {
  color: var(--green);
  border-color: var(--green);
  opacity: 1;
}

.close-button {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: 1px solid var(--lightest-navy);
  border-radius: 4px;
  color: var(--slate);
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-button:hover {
  color: var(--green);
  border-color: var(--green);
}

.modal-content {
  flex: 1;
  overflow-y: auto;
}

.modal-body {
  padding: 30px 40px 50px;
  color: var(--slate);
  line-height: 1.8;
}

.modal-body :deep(h1),
.modal-body :deep(h2),
.modal-body :deep(h3),
.modal-body :deep(h4) {
  color: var(--lightest-slate);
  margin-top: 30px;
  margin-bottom: 15px;
}

.modal-body :deep(p) {
  margin-bottom: 16px;
}

.modal-body :deep(a) {
  color: var(--green);
  text-decoration: none;
  transition: all 0.3s ease;
}

.modal-body :deep(a:hover) {
  opacity: 0.8;
}

.modal-body :deep(code) {
  font-family: var(--font-mono);
  font-size: 14px;
  background-color: var(--navy);
  color: var(--light-slate);
  padding: 2px 5px;
  border-radius: 3px;
}

.modal-body :deep(pre) {
  background-color: var(--navy);
  border: 1px solid var(--lightest-navy);
  border-radius: 4px;
  padding: 16px;
  overflow-x: auto;
  margin: 16px 0;
}

.modal-body :deep(pre code) {
  background-color: transparent;
  padding: 0;
  border: none;
}

.modal-body :deep(ul),
.modal-body :deep(ol) {
  margin: 16px 0;
  padding-left: 24px;
}

.modal-body :deep(li) {
  margin-bottom: 8px;
}

.modal-body :deep(blockquote) {
  border-left: 2px solid var(--green);
  padding-left: 16px;
  margin: 16px 0;
  color: var(--light-slate);
  font-style: italic;
}

.modal-body :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 16px 0;
}

.modal-loading,
.modal-error,
.modal-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  text-align: center;
}
</style>
