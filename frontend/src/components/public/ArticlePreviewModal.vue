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

// Reuse ArticleView.vue markdown-it config
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
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'short', day: 'numeric'
  })
}

function handleClose() {
  emit('close')
}

// Save and restore scroll position when modal opens/closes
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

// Fetch article when modal opens with articleId
watch(() => props.articleId, (newId) => {
  if (newId) {
    store.fetchArticle(newId)
  }
}, { immediate: true })

// Escape key listener
onMounted(() => document.addEventListener('keydown', handleEscape))
onUnmounted(() => document.removeEventListener('keydown', handleEscape))
</script>

<template>
  <!-- Overlay -->
  <div
    v-if="visible"
    class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
    @click.self="handleClose"
  >
    <!-- Modal Container -->
    <div class="bg-gray-900 rounded-lg w-full max-w-4xl max-h-[90vh] overflow-hidden border border-gray-700 shadow-2xl flex flex-col">
      <!-- Header -->
      <div class="flex-shrink-0 border-b border-gray-700 bg-gray-800 relative">
        <!-- Title + Meta -->
        <div class="p-6 pb-4">
          <h2 class="text-2xl font-bold text-gray-100 mb-3">{{ article?.title }}</h2>
          <!-- Meta line -->
          <div class="flex flex-wrap items-center gap-2">
            <router-link
              v-if="article?.category"
              :to="`/category/${article.category.slug || article.category.name.toLowerCase().replace(/\s+/g, '-')}`"
              class="px-2 py-0.5 text-xs rounded bg-blue-900 text-blue-300 hover:bg-blue-800"
            >
              {{ article.category.name }}
            </router-link>
            <router-link
              v-for="tag in article?.tags"
              :key="tag.id"
              :to="`/tag/${tag.name.toLowerCase().replace(/\s+/g, '-')}`"
              class="px-2 py-0.5 text-xs rounded bg-gray-700 text-gray-300 hover:bg-gray-600"
            >
              {{ tag.name }}
            </router-link>
            <time class="ml-auto text-sm text-gray-500">{{ formatDate(article?.createdAt) }}</time>
          </div>
        </div>
        <!-- Close Button -->
        <button
          type="button"
          @click.stop="handleClose"
          @mousedown.prevent
          class="absolute top-4 right-4 p-2 rounded-full hover:bg-gray-700 text-gray-400 hover:text-white transition-colors"
          title="关闭预览"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Scrollable Content -->
      <div class="flex-1 overflow-y-auto">
        <div v-if="store.loading" class="p-6 text-center text-gray-500">Loading...</div>
        <div v-else-if="store.error" class="p-6 text-center text-red-500">{{ store.error }}</div>
        <div
          v-else-if="article"
          class="prose prose-invert max-w-none p-6"
          v-html="renderedContent"
        ></div>
        <div v-else class="p-6 text-center text-gray-500">文章不存在</div>
      </div>
    </div>
  </div>
</template>
