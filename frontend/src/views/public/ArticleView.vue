<script setup>
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePublicArticleStore } from '../../stores/publicArticleStore'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import CommentSection from '../../components/CommentSection.vue'

const route = useRoute()
const store = usePublicArticleStore()

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    const supportedLanguages = ['javascript', 'typescript', 'python', 'go', 'java', 'sql', 'json', 'bash', 'yaml']
    if (lang && supportedLanguages.includes(lang)) {
      try {
        return '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

const article = computed(() => store.currentArticle)
const renderedContent = computed(() => {
  if (!article.value) return ''
  return md.render(article.value.content)
})

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric'
  })
}

onMounted(() => {
  const id = route.params.idSlug.split('-')[0]  // D-03: extract id from id-slug
  store.fetchArticle(id)
})
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <div v-if="store.loading" class="text-center py-12">
      <p class="text-gray-500">Loading...</p>
    </div>
    <div v-else-if="store.error" class="text-center py-12">
      <p class="text-red-500">{{ store.error }}</p>
    </div>
    <article v-else-if="article" class="article-detail">
      <!-- Title -->
      <h1 class="text-4xl font-bold text-gray-100 mb-4">{{ article.title }}</h1>

      <!-- Meta -->
      <div class="flex flex-wrap items-center gap-3 mb-8 text-gray-400">
        <time>{{ formatDate(article.createdAt) }}</time>
        <router-link
          v-if="article.category"
          :to="`/category/${article.category.slug || article.category.name.toLowerCase().replace(/\s+/g, '-')}`"
          class="px-2 py-0.5 text-xs rounded bg-blue-900 text-blue-300 hover:bg-blue-800"
        >
          {{ article.category.name }}
        </router-link>
        <router-link
          v-for="tag in article.tags"
          :key="tag.id"
          :to="`/tag/${tag.name.toLowerCase().replace(/\s+/g, '-')}`"
          class="px-2 py-0.5 text-xs rounded bg-gray-700 text-gray-300 hover:bg-gray-600"
        >
          {{ tag.name }}
        </router-link>
      </div>

      <!-- Content with markdown rendering (PUBL-02) -->
      <div class="prose prose-invert max-w-none" v-html="renderedContent"></div>
    </article>
    <CommentSection v-if="article" :articleId="article.id.toString()" />
    <div v-else class="text-center py-12">
      <p class="text-gray-500">Article not found.</p>
    </div>
  </div>
</template>
