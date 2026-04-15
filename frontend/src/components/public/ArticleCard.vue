<script setup>
const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'short', day: 'numeric'
  })
}
</script>

<template>
  <article class="article-card border-l-2 border-blue-600 pl-4 py-2 hover:border-blue-400 transition-colors">
    <!-- D-02: Title + excerpt (2 lines) -->
    <h3 class="text-xl font-semibold text-gray-100">
      <router-link
        :to="`/article/${article.id}-${article.slug}`"
        class="hover:text-blue-400 transition-colors"
      >
        {{ article.title }}
      </router-link>
    </h3>
    <p class="text-gray-400 mt-2 line-clamp-2">{{ article.excerpt }}</p>

    <!-- D-02: Tags + date + category badge -->
    <div class="flex flex-wrap items-center gap-2 mt-3">
      <!-- Category badge -->
      <router-link
        v-if="article.category"
        :to="`/category/${article.category.slug || article.category.name.toLowerCase().replace(/\s+/g, '-')}`"
        class="px-2 py-0.5 text-xs rounded bg-blue-900 text-blue-300 hover:bg-blue-800"
      >
        {{ article.category.name }}
      </router-link>

      <!-- Tags -->
      <router-link
        v-for="tag in article.tags"
        :key="tag.id"
        :to="`/tag/${tag.name.toLowerCase().replace(/\s+/g, '-')}`"
        class="px-2 py-0.5 text-xs rounded bg-gray-700 text-gray-300 hover:bg-gray-600"
      >
        {{ tag.name }}
      </router-link>

      <!-- Date -->
      <time class="text-sm text-gray-500 ml-auto">{{ formatDate(article.createdAt) }}</time>
    </div>
  </article>
</template>
