<script setup>
import { computed } from 'vue'
import ArticleCard from './ArticleCard.vue'

const props = defineProps({
  articles: {
    type: Array,
    required: true
    // [{ id, slug, title, excerpt, tags: [], date, category: { name, slug } }]
  }
})

const emit = defineEmits(['preview'])

// D-01: Group articles by date string
const groupedByDate = computed(() => {
  const groups = {}
  for (const article of props.articles) {
    const dateKey = new Date(article.createdAt).toLocaleDateString('zh-CN', {
      year: 'numeric', month: 'long', day: 'numeric'
    })
    if (!groups[dateKey]) groups[dateKey] = []
    groups[dateKey].push(article)
  }
  return groups
})
</script>

<template>
  <div class="timeline">
    <div v-for="(articles, date) in groupedByDate" :key="date" class="date-group mb-8">
      <div class="date-divider flex items-center mb-4">
        <div class="h-px bg-gray-700 flex-1"></div>
        <span class="px-4 py-1 text-sm font-medium text-gray-400 bg-gray-900 rounded-full mx-4">
          {{ date }}
        </span>
        <div class="h-px bg-gray-700 flex-1"></div>
      </div>
      <div class="articles space-y-4">
        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
          @preview="(id) => emit('preview', id)"
        />
      </div>
    </div>
    <div v-if="articles.length === 0" class="text-center py-12">
      <p class="text-gray-500">No articles found.</p>
    </div>
  </div>
</template>
